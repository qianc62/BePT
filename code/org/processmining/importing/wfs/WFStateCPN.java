package org.processmining.importing.wfs;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Helper class converting a WFState format to the corresponding SML initial
 * state file.
 * 
 * @author Moe Wynn (m.wynn at qut.edu.au)
 */
public class WFStateCPN {

	// Data structures for CPN SML file
	int maxCaseID = 0;
	String specID = "";
	HashMap TokensForExeTasks = new HashMap();
	HashMap TokensForEnabledConditions = new HashMap();
	HashMap CaseData = new HashMap();
	long CurrentTimeStamp = 0;
	// Supported formats Secs and Min or Hour.
	// default
	String TimeUnit = "Secs";
	// Warning messages
	HashMap<String, String> Warnings = new HashMap<String, String>();
	// These two statuses from the log files are used for simulation purposes.
	static String EnabledStatus = "marked";
	static String ExecutingStatus = "executing";
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final String schema_location = "http://www.yawlfoundation.org/yawlschema/WorkFlowState.xsd";

	public WFStateCPN() {
	}

	/**
	 * This method extracts the information about a workflow state from the
	 * workflow engine xml file for a given specification id and returns a
	 * string in current state cpn sml file format.
	 * 
	 * @param specID
	 *            the process name
	 * @param timeunit
	 *            the times in the SML file can be either "Hour", "Min", or
	 *            "Secs"
	 * @param xmlStr
	 *            the xml string of current state
	 * @return the string format in the cpn smil file format
	 */

	public String convertToSML(String specID, String timeunit, String xmlStr) {
		String response = "";
		TimeUnit = timeunit;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			factory.setNamespaceAware(true);
			// factory.setValidating(true);
			factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new WFStateErrorHandler());
			/*
			 * String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
			 * SchemaFactory schemafactory =
			 * SchemaFactory.newInstance(language); Schema schema =
			 * schemafactory.newSchema(new File(schema_location));
			 * factory.setSchema(schema);
			 */
			Document xmlDoc = builder.parse(new InputSource(new StringReader(
					xmlStr)));
			if (containsHierarchy(xmlDoc)) {
				Warnings
						.put(
								"HierarchicalModel",
								"This WorkFlow State xml file contains a specification with subnets and the cpn sml file generated may not accurately capture the initial state of the workflow.");
			}

			NodeList list = xmlDoc.getElementsByTagName("Timestamp");
			Node node = list.item(0);
			String timestamp = node.getTextContent();
			this.CurrentTimeStamp = Long
					.parseLong(convertTimestampToMs(timestamp));
			NodeList WFModelElementRecordList = xmlDoc
					.getElementsByTagName("WFModelElementRecord");
			for (int i = 0; i < WFModelElementRecordList.getLength(); i++) {
				Node elementNode = WFModelElementRecordList.item(i);
				Node processInstanceNode = elementNode.getParentNode();
				Node processNode = processInstanceNode.getParentNode();
				// get the specid from the Process id attribute
				NamedNodeMap attList = processNode.getAttributes();
				Node specidAtt = attList.getNamedItem("id");
				String id = specidAtt.getNodeValue();
				if (specID.equalsIgnoreCase(id)) {
					NodeList childList = elementNode.getChildNodes();
					String elementid = "";
					String caseid = "";
					String caseStartTime = "";
					String resource = "";
					String time = "";
					String status = "";
					String wfmodelelementtype = "";
					// get casestarttime from PI
					NodeList piChildren = processInstanceNode.getChildNodes();
					for (int ci = 0; ci < piChildren.getLength(); ci++) {
						Node childNode = piChildren.item(ci);
						String childNodeName = childNode.getNodeName();
						String childNodeValue = childNode.getTextContent();
						if (childNodeName == "Timestamp") {
							caseStartTime = convertTimestampToMs(childNodeValue);
						}
					}
					// get the caseid from the ProcessInstance id attribute
					NamedNodeMap piAttList = processInstanceNode
							.getAttributes();
					Node caseidAtt = piAttList.getNamedItem("id");
					caseid = caseidAtt.getNodeValue();
					// flatten the structure
					if (caseid.contains(".")) {
						caseid = caseid.substring(0, caseid.indexOf('.'));
					}
					// To get maxCaseID information
					int caseIDint = Integer.parseInt(caseid);
					if (caseIDint > maxCaseID) {
						maxCaseID = caseIDint;
					}
					// for each WFModelElementRecord information
					for (int ci = 0; ci < childList.getLength(); ci++) {
						Node childNode = childList.item(ci);
						String childNodeName = childNode.getNodeName();
						String childNodeValue = childNode.getTextContent();
						if (childNodeName == "Status") {
							status = childNodeValue;
						}
						if (childNodeName == "WFModelElement") {
							elementid = childNodeValue;
							// get the wfmodelelement type from type attribute
							NamedNodeMap eleAttList = childNode.getAttributes();
							Node typeAtt = eleAttList.getNamedItem("type");
							wfmodelelementtype = typeAtt.getNodeValue();
						}
						if (childNodeName == "Originator") {
							resource = childNodeValue;
						}
						// not being used at this time
						if (childNodeName == "Timestamp") {
							time = convertTimestampToMs(childNodeValue);
						}
					} // end for all child elements WFModelElementRecord
					// Now populate the two HashMaps
					if (status.equalsIgnoreCase(ExecutingStatus)) {
						if (resource == "") {
							Warnings.put("MissingResource: " + elementid,
									"No resource info available for the executing task "
											+ elementid + ".");
						}
						int count = 1;
						CPNToken token = new CPNToken(elementid, caseid,
								resource, count,
								getCaseStartTimeDuration(caseStartTime));
						List tokensList = null;
						Object obj = TokensForExeTasks.get(elementid);
						if (obj == null) {
							tokensList = new ArrayList();
						} else {
							tokensList = (List) obj;
						}
						tokensList.add(token);
						TokensForExeTasks.put(elementid, tokensList);
					}
					// For enabled explicit and implicit conditions
					if (status.equalsIgnoreCase(EnabledStatus)
							&& wfmodelelementtype.equalsIgnoreCase("COND")) {
						// One token only
						int count = 1;
						CPNToken token = new CPNToken(elementid, caseid, "",
								count, getCaseStartTimeDuration(caseStartTime));
						List tokensList = null;
						Object obj = TokensForEnabledConditions.get(elementid);
						if (obj == null) {
							tokensList = new ArrayList();
						} else {
							tokensList = (List) obj;
						}
						tokensList.add(token);
						TokensForEnabledConditions.put(elementid, tokensList);
					}
				}// end if
			}// end for each WFModelElementRecord
			NodeList ProcessInstancesList = xmlDoc
					.getElementsByTagName("ProcessInstance");
			// GetCaseData for each ProcessInstance information
			for (int i = 0; i < ProcessInstancesList.getLength(); i++) {
				Node processInstanceNode = ProcessInstancesList.item(i);
				// As it is possible to have more than one process in the xml
				// file
				Node processNode = processInstanceNode.getParentNode();
				// get the specid from the Process id attribute
				NamedNodeMap attList = processNode.getAttributes();
				Node specidAtt = attList.getNamedItem("id");
				String id = specidAtt.getNodeValue();
				if (specID.equalsIgnoreCase(id)) {
					NodeList childNodeList = processInstanceNode
							.getChildNodes();
					// GetCaseData for each ProcessInstance information
					for (int ci = 0; ci < childNodeList.getLength(); ci++) {
						Node childNode = childNodeList.item(ci);
						String childNodeName = childNode.getNodeName();
						if (childNodeName == "Data") {
							// get the caseid from the ProcessInstance id
							// attribute
							NamedNodeMap piAttList = processInstanceNode
									.getAttributes();
							Node caseidAtt = piAttList.getNamedItem("id");
							String caseid = caseidAtt.getNodeValue();
							// flatten the structure
							if (caseid.contains(".")) {
								caseid = caseid.substring(0, caseid
										.indexOf('.'));
							}
							HashMap<String, String> caseDataOneCase = new HashMap<String, String>();
							NodeList DataAttributesList = childNode
									.getChildNodes();
							for (int di = 0; di < DataAttributesList
									.getLength(); di++) {
								Node attNode = DataAttributesList.item(di);
								NamedNodeMap dataAttList = attNode
										.getAttributes();
								Node nameAtt = dataAttList.getNamedItem("name");
								String name = nameAtt.getNodeValue();
								String value = attNode.getTextContent();
								caseDataOneCase.put(name, value);
							}// end for each data attribute
							CaseData.put(caseid, caseDataOneCase);
						}// end if not data
					}// end for looping all child nodes of PI
				}// end if specid=caseid
			}// end for each case
			// Generate cpn file formatted string
			String caseID = String.valueOf(maxCaseID + 1);
			response = generateSMLFile(CaseData, caseID, TokensForExeTasks,
					TokensForEnabledConditions, TokensForExeTasks);
		} catch (IllegalArgumentException x) {
			// Happens if the parser does not support JAXP 1.2
			x.printStackTrace();
			Warnings.put("XMLExportError", x.getMessage());
			response = "";
		} catch (SAXException sxe) {
			// Error generated during parsing
			sxe.printStackTrace();
			Warnings.put("XMLExportError", sxe.getMessage());
			response = "";
		} catch (ParserConfigurationException pce) {
			// Parser with specified options can't be built
			pce.printStackTrace();
			Warnings.put("XMLExportError", pce.getMessage());
			response = "";
		} catch (IOException ioe) {
			// I/O error
			ioe.printStackTrace();
			Warnings.put("XMLExportError", ioe.getMessage());
			response = "";
		}
		return response;
	}

	/**
	 * This method returns a set of user friendly warning/error messages
	 * generated during workflow state xml import.
	 * 
	 * @return a HashMap that contains warning messages.
	 */
	public HashMap<String, String> getWarnings() {
		return Warnings;
	}

	private Boolean containsHierarchy(Document xmlDoc) {
		NodeList nodeList = xmlDoc
				.getElementsByTagName("ParentProcessInstance");
		return nodeList.getLength() > 0;
	}

	private String getCaseStartTimeDuration(String startTime) {
		long startTimeStamp = 0;
		long duration = 0;
		if (startTime != "") {
			startTimeStamp = Long.parseLong(startTime);
			duration = startTimeStamp - CurrentTimeStamp;
		}
		return String.valueOf(convertTimeStamp(TimeUnit, duration));
	}

	/**
	 * 
	 * @param unit
	 *            "Hour", "Min", "Secs"
	 * @param timestamp
	 *            timestamp in milliseconds
	 * @return timestamp in either Min, Secs, or Hour
	 */
	private long convertTimeStamp(String unit, long timestamp) {
		try {
			if (unit.equalsIgnoreCase("Secs")) {
				timestamp = timestamp / 1000;
			} else if (unit.equalsIgnoreCase("Min")) {
				timestamp = timestamp / 60000;
			} else if (unit.equalsIgnoreCase("Hour")) {
				timestamp = timestamp / 360000;
			}
			return timestamp;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private String convertTimestampToMs(String ts) {
		String timestamp = ts;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		if (ts != "") { // convert it back to 'Z' format of +1000 vs. +10:00
			int index = ts.indexOf("+");
			if (index > 0) { // remove ":"
				String timezone = ts.substring(index);
				timezone = timezone.replaceAll(":", "");
				ts = ts.substring(0, index - 1) + timezone;
			}
			try {
				Date d = df.parse(ts);
				long t = d.getTime();
				timestamp = Long.toString(t);
			} catch (ParseException x) {
				x.printStackTrace();
				Warnings.put("TimeConversionError",
						"Parse exception for timestamp " + ts);
			}
		}
		return timestamp;
	}

	private String generateSMLFile(HashMap caseData, String caseID,
			HashMap execTokens, HashMap enabledTokens, HashMap busy) {
		return generateFunGetInitialCaseData(caseData) + "\n"
				+ generateFunGetNextCaseID(caseID) + "\n"
				+ generateFunGetInitialTokensExePlace(execTokens) + "\n"
				+ generateFunGetInitialTokens(enabledTokens) + "\n"
				+ generateFunGetBusyResources(busy) + "\n"
				+ generateFunGetCurrentTimeStamp() + "\n"
				+ generateFunGetTimeUnit();
	}

	private String generateFunGetInitialCaseData(HashMap caseDataMap) {
		String funHeader = "fun getInitialCaseData()=[";
		String funBody = "";
		String funFooter = "];";
		if (caseDataMap.size() > 0) {
			Iterator it = caseDataMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String caseid = pairs.getKey().toString();
				HashMap caseDataOneCase = (HashMap) pairs.getValue();
				String caseDataStr = "(" + caseid + ", {";
				String onecaseDataStr = "";
				Iterator i = caseDataOneCase.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry variable = (Map.Entry) i.next();
					String varName = variable.getKey().toString();
					String varValue = variable.getValue().toString();
					/*
					 * This is needed as CPN Tools cannot handle a double value
					 * provided by the xml data.
					 */
					try {
						int var_int = Double.valueOf(varValue).intValue();
						varValue = Integer.toString(var_int);
					} catch (NumberFormatException e) {
					}
					onecaseDataStr += varName + " = " + varValue + ",";
				}
				// end of a case
				if (onecaseDataStr.endsWith(",")) {
					caseDataStr += onecaseDataStr.substring(0, onecaseDataStr
							.length() - 1);
				}
				caseDataStr += "})";
				funBody += caseDataStr + ",";
			}
			// end of all cases
			if (funBody.endsWith(",")) {
				funBody = funBody.substring(0, funBody.length() - 1);
			}
		}
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetNextCaseID(String funBody) {
		String funHeader = "fun getNextCaseID() =";
		String funFooter = ";";
		if (funBody == null || funBody == "") {
			funBody = "1";
		}
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetInitialTokensExePlace(HashMap tokenMap) {
		String funHeader = "fun getInitialTokensExePlace(pname:STRING) =";
		String funBody = "empty";
		String funFooter = ";";
		if (tokenMap.size() > 0) {
			funBody = "case pname of ";
			Iterator it = tokenMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String task = pairs.getKey().toString();
				// make sure that the cpn mappings are maintained.
				String condition = "\""
						+ YAWLCPNUtils.getYawlCpnValidConditionName(task,
								"EXEC") + "\"";
				String tokensStr = "";
				List tokens = (List) pairs.getValue();
				tokensStr = "[";
				Iterator listIt = tokens.iterator();
				while (listIt.hasNext()) {
					CPNToken token = (CPNToken) listIt.next();
					tokensStr += token.convertToCPNTokenFormat();
					tokensStr += ",";
				}
				tokensStr = tokensStr.substring(0, tokensStr.length() - 1)
						+ "]";
				funBody += condition + "=>" + tokensStr + " | ";
			}
			funBody += "_ => empty";
		}
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetInitialTokens(HashMap tokenMap) {
		String funHeader = "fun getInitialTokens(pname:STRING) =";
		String funBody = "empty";
		String funFooter = ";";
		if (tokenMap.size() > 0) {
			funBody = "case pname of ";
			Iterator it = tokenMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String condition = pairs.getKey().toString();
				// make sure that the cpn mappings are maintained.
				condition = "\""
						+ YAWLCPNUtils.getYawlCpnValidConditionName(condition,
								"ENABLE") + "\"";
				String tokensStr = "";
				List tokens = (List) pairs.getValue();
				tokensStr = "[";
				Iterator listIt = tokens.iterator();
				while (listIt.hasNext()) {
					CPNToken token = (CPNToken) listIt.next();
					tokensStr += token.convertToCPNTokenFormat();
					tokensStr += ",";
				}
				tokensStr = tokensStr.substring(0, tokensStr.length() - 1)
						+ "]";
				funBody += condition + "=>" + tokensStr + " | ";
			}
			funBody += "_ => empty";
		}
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetBusyResources(HashMap TokensForExeTasks) {
		String funHeader = "fun getBusyResources() =[";
		String funBody = "";
		String funFooter = "];";
		List resources = new ArrayList();
		Collection tokensList = TokensForExeTasks.values();
		Iterator it = tokensList.iterator();
		while (it.hasNext()) {
			List tokens = (List) it.next();
			Iterator listIt = tokens.iterator();
			while (listIt.hasNext()) {
				CPNToken token = (CPNToken) listIt.next();
				String resource = token.getResource();
				if (resource != "") {
					if (resources.contains(resource)) {
						System.out
								.println("This resource is working on more than one tasks at the same time: "
										+ resource);
					} else {
						resources.add(resource);
						funBody += "\"" + resource + "\",";
					}
				}
			}
		}
		// remove the last ","
		if (funBody.length() >= 1) {
			funBody = funBody.substring(0, funBody.length() - 1);
		}
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetCurrentTimeStamp() {
		String funHeader = "fun getCurrentTimeStamp() =";
		String funBody = "\""
				+ String.valueOf(convertTimeStamp(TimeUnit, CurrentTimeStamp))
				+ "\"";
		String funFooter = ";";
		return funHeader + funBody + funFooter;
	}

	private String generateFunGetTimeUnit() {
		String funHeader = "fun getTimeUnit() =";
		String funBody = "\"" + TimeUnit + "\"";
		String funFooter = ";";
		return funHeader + funBody + funFooter;
	}
}

class WFStateErrorHandler implements ErrorHandler {

	public void error(SAXParseException e) throws SAXException {
		throw e;
	}

	public void fatalError(SAXParseException e) throws SAXException {
		throw e;
	}

	public void warning(SAXParseException e) throws SAXException {
		throw e;
	}

}
