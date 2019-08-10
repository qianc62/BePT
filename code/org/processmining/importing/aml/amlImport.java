package org.processmining.importing.aml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelHierarchyDirectory;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.ui.About;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Jan Mendling
 * @version 1.0
 */

public class amlImport implements ImportPlugin, LogReaderConnectionImportPlugin {

	public String getName() {
		return "AML file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc;
			// NodeList netNodes;
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			// dbf.setExpandEntityReferences(false);
			// dbf.setNamespaceAware(false);

			DocumentBuilder db = dbf.newDocumentBuilder();

			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) {
					if (systemId.indexOf("ARIS-Export") != -1) {
						return new InputSource("file:" + About.EXTLIBLOCATION()
								+ "ARIS-Export101.dtd");
					} else {
						return null;
					}
				}
			});

			InputSource inpStream = new InputSource(input);
			inpStream.setSystemId("file:" + System.getProperty("user.dir", ""));
			doc = db.parse(inpStream);

			// check if root element is a aml tag
			Message.add("parsing done" + doc, Message.DEBUG);
			if (!(doc.getDocumentElement().getNodeName().equals("AML"))) {
				Message.add("aml tag not found", Message.ERROR);
				throw new Exception("aml tag not found");
			} else {
				Message.add("aml root element found");
			}

			EPCResult result = new EPCResult(null, (EPC) null);
			HashMap ObjDef_LinkId = new HashMap();
			HashMap modelid_net = new HashMap();
			HashMap ObjDef_Name = new HashMap();
			HashMap function_LinkId = new HashMap();
			HashMap ModelId_ModelType = new HashMap();
			traverseAMLforObjectNames(ObjDef_Name, doc.getDocumentElement(),
					ObjDef_LinkId, ModelId_ModelType);
			Iterator findLinkToEpc = ObjDef_LinkId.keySet().iterator();
			while (findLinkToEpc.hasNext()) {
				String currentObjDef = (String) findLinkToEpc.next();
				String Links = (String) ObjDef_LinkId.get(currentObjDef);
				StringTokenizer linkSet = new StringTokenizer(Links);
				String realEpcLink = "";
				while (linkSet.hasMoreTokens()) {
					String currentLink = linkSet.nextToken();
					if (ModelId_ModelType.get(currentLink).equals("MT_EEPC")) {
						realEpcLink = currentLink;
						break;
					}
				}
				if (realEpcLink.equals(" ")) {
					ObjDef_LinkId.remove(currentObjDef);
				} else {
					ObjDef_LinkId.put(currentObjDef, realEpcLink);
				}
			}
			result = traverseAML(result, doc.getDocumentElement(), null,
					ObjDef_Name, ObjDef_LinkId, modelid_net, function_LinkId);
			Iterator hierarchicalFunctions = function_LinkId.keySet()
					.iterator();
			while (hierarchicalFunctions.hasNext()) {
				EPCSubstFunction f = (EPCSubstFunction) hierarchicalFunctions
						.next();
				f.setSubstitutedEPC((EPC) modelid_net.get(function_LinkId
						.get(f)));
				// Message.add(f.getSubstitutedEPC().getName());
			}

			return result;

		} catch (Throwable x) {
			Message.add(x.toString());
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/AMLImport";
	}

	public static void traverseAMLforObjectNames(HashMap partialMap,
			Node currentNode, HashMap ObjDef_LinkId, HashMap ModelId_ModelType) {
		if (currentNode.hasChildNodes()) {
			for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
				Node currentChild = currentNode.getChildNodes().item(i);
				if (currentChild.getNodeName().equals("Group")) {
					traverseAMLforObjectNames(partialMap, currentChild,
							ObjDef_LinkId, ModelId_ModelType);
				}
				if (currentChild.getNodeName().equals("Model")) {
					if (currentChild.hasAttributes()) {
						String mid = currentChild.getAttributes().getNamedItem(
								"Model.ID").getNodeValue();
						String type = currentChild.getAttributes()
								.getNamedItem("Model.Type").getNodeValue();
						ModelId_ModelType.put(mid, type);
					}
					// traverseAMLforObjectNames(partialMap, currentChild,
					// ObjDef_LinkId);
				}
				if (currentChild.getNodeName().equals("ObjDef")) {
					String id = currentChild.getAttributes().getNamedItem(
							"ObjDef.ID").getNodeValue();
					NodeList currentChildren = currentChild.getChildNodes();
					String ObjName = "";
					for (int k = 0; k < currentChildren.getLength(); k++) {
						Node Child = currentChildren.item(k);
						if (!Child.getNodeName().equals("AttrDef")) {
							continue;
						} else if (!Child.getAttributes().getNamedItem(
								"AttrDef.Type").getNodeValue()
								.equals("AT_NAME")) {
							continue;
						} else if (Child.hasChildNodes()) {
							for (int l = 0; l < Child.getChildNodes()
									.getLength(); l++) {
								if (!(Child.getChildNodes().item(l)
										.getNodeName().equals("AttrValue"))) {
									continue;
								} else {
									ObjName = getTextContent(Child
											.getChildNodes().item(l));
									ObjName = ObjName.replaceAll("\n", "\\\\n");
									break;
								}
							}
						}
					}
					partialMap.put(id, ObjName);
					for (int j = 0; j < currentChild.getAttributes()
							.getLength(); j++) {
						if (currentChild.getAttributes().item(j).getNodeName()
								.equals("LinkedModels.IdRefs")) {
							String links = currentChild.getAttributes()
									.getNamedItem("LinkedModels.IdRefs")
									.getNodeValue();
							/*
							 * if (links.indexOf(" ") > -1) {
							 * Message.add("yes, yes, yes"); links =
							 * links.substring(0, links.indexOf(" ")); }
							 */
							ObjDef_LinkId.put(id, links);
						}
					}
				}
			}
		}
	}

	public EPCResult traverseAML(EPCResult partialResult, Node currentNode,
			Object parent, HashMap ObjDef_Name, HashMap ObjDef_LinkId,
			HashMap modelid_net, HashMap function_LinkId) throws Exception {
		if (currentNode.hasChildNodes()) {
			for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
				Node currentChild = currentNode.getChildNodes().item(i);
				if (currentChild.getNodeName().equals("Group")) {
					String id = currentChild.getAttributes().getNamedItem(
							"Group.ID").getNodeValue();
					String GroupName = "";
					if (currentChild.hasChildNodes()) {
						NodeList currentChildren = currentChild.getChildNodes();
						for (int j = 0; j < currentChildren.getLength(); j++) {
							Node Child = currentChildren.item(j);
							if (!(Child.getNodeName().equals("AttrDef"))) {
								continue;
							}
							if (Child.getAttributes().getNamedItem(
									"AttrDef.Type").getNodeValue().equals(
									"AT_NAME")) {
								if (Child.hasChildNodes()) {
									for (int l = 0; l < Child.getChildNodes()
											.getLength(); l++) {
										if (!(Child.getChildNodes().item(l)
												.getNodeName()
												.equals("AttrValue"))) {
											continue;
										} else {
											GroupName = getTextContent(Child
													.getChildNodes().item(l));
										}
									}
									break;
								}
							}
						}
						if (GroupName.equals("")) {
							GroupName = id;
						}
					}
					ModelHierarchyDirectory dir = new ModelHierarchyDirectory(
							id, GroupName);
					partialResult.addInHierarchy(dir, parent, GroupName);
					partialResult = traverseAML(partialResult, currentChild,
							dir, ObjDef_Name, ObjDef_LinkId, modelid_net,
							function_LinkId);
				}
				if (currentChild.getNodeName().equals("Model")
						&& currentChild.getAttributes().getNamedItem(
								"Model.Type").getNodeValue().equals("MT_EEPC")) {
					String ModelName = "gaga";
					if (currentChild.hasChildNodes()) {
						NodeList currentChildren = currentChild.getChildNodes();
						for (int j = 0; j < currentChildren.getLength(); j++) {
							Node Child = currentChildren.item(j);
							if (!(Child.getNodeName().equals("AttrDef"))) {
								continue;
							}
							if (Child.getAttributes().getNamedItem(
									"AttrDef.Type").getNodeValue().equals(
									"AT_NAME")) {
								if (Child.hasChildNodes()) {
									for (int l = 0; l < Child.getChildNodes()
											.getLength(); l++) {
										if (!(Child.getChildNodes().item(l)
												.getNodeName()
												.equals("AttrValue"))) {
											continue;
										} else {
											ModelName = getTextContent(Child
													.getChildNodes().item(l));
										}
									}
								}
								break;
							}
						}
					}
					try {
						ModelName = ModelName.replaceAll("\n", " ");
						EPC net = read(currentChild, ObjDef_Name,
								ObjDef_LinkId, function_LinkId, ModelName);
						partialResult.addInHierarchy(net, parent, ModelName);
						modelid_net.put(currentChild.getAttributes()
								.getNamedItem("Model.ID").getNodeValue(), net);
					} catch (Throwable x) {
						Message.add(x.getClass().getName());
						// throw new IOException(x.getMessage());
					}
				}
			}
		}
		return partialResult;
	}

	public static EPC read(Node node, HashMap ObjDef_Name,
			HashMap ObjDef_LinkId, HashMap function_LinkId, String ModelName)
			throws Exception {
		EPC result = new EPC(true);
		// Message.add("1.read " +
		// node.getAttributes().getNamedItem("Model.ID").getNodeValue());
		parseNet(node, result, ObjDef_Name, ObjDef_LinkId, function_LinkId,
				ModelName);
		// Message.add("2.read " + node.getNodeName());
		return result;
	}

	private static void parseNet(Node node, EPC net, HashMap ObjDef_Name,
			HashMap ObjDef_LinkId, HashMap function_LinkId, String ModelName)
			throws Exception {
		HashMap mapping = new HashMap();

		// read all nodes
		NodeList nodes = node.getChildNodes();
		net.setIdentifier(ModelName);
		// Message.add("here I am still happy");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (!n.getNodeName().equals("ObjOcc")) {
				continue;
			}
			if (n.getAttributes().getNamedItem("SymbolNum") == null) {
				continue;
			}
			String symbolnum = n.getAttributes().getNamedItem("SymbolNum")
					.getNodeValue();
			if (!(symbolnum.equals("ST_FUNC") || symbolnum.equals("ST_EV")
					|| symbolnum.equals("ST_OPR_AND_1")
					|| symbolnum.equals("ST_OPR_OR_1") || symbolnum
					.equals("ST_OPR_XOR_1"))) {
				continue;
			}

			String ObjDef = n.getAttributes().getNamedItem("ObjDef.IdRef")
					.getNodeValue();
			String ownName = (String) ObjDef_Name.get(ObjDef);
			// Message.add("YES " +
			// n.getAttributes().getNamedItem("ObjOcc.ID").getNodeValue());
			String id = n.getAttributes().getNamedItem("ObjOcc.ID")
					.getNodeValue();
			// Message.add(id + " " + ownName);
			if (ownName == null || ownName == "") {
				ownName = id.substring(7, 11);
			}
			if (symbolnum.equals("ST_FUNC")) {
				if (ObjDef_LinkId.containsKey(ObjDef)) {
					EPCSubstFunction sf = (EPCSubstFunction) net
							.addFunction(new EPCSubstFunction(new LogEvent(
									ownName, "unknown:normal"), net, null));
					sf.setIdentifier(ownName);
					mapping.put(id, sf);
					function_LinkId.put(sf, ObjDef_LinkId.get(ObjDef));
				} else {
					EPCFunction f = net.addFunction(new EPCFunction(
							new LogEvent(ownName, "unknown:normal"), net));
					f.setIdentifier(ownName);
					mapping.put(id, f);
				}
			} else if (symbolnum.equals("ST_EV")) {
				EPCEvent e = net.addEvent(new EPCEvent(ownName, net));
				e.setIdentifier(ownName);
				mapping.put(id, e);
			} else if (symbolnum.equals("ST_OPR_AND_1")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.AND, net));
				mapping.put(id, c);
			} else if (symbolnum.equals("ST_OPR_OR_1")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.OR, net));
				mapping.put(id, c);
			} else if (symbolnum.equals("ST_OPR_XOR_1")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.XOR, net));
				mapping.put(id, c);
			}
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (!n.getNodeName().equals("ObjOcc")) {
				continue;
			}
			if (n.getAttributes().getNamedItem("SymbolNum") == null) {
				continue;
			}
			String symbolnum = n.getAttributes().getNamedItem("SymbolNum")
					.getNodeValue();
			if (!(symbolnum.equals("ST_FUNC") || symbolnum.equals("ST_EV")
					|| symbolnum.equals("ST_OPR_AND_1")
					|| symbolnum.equals("ST_OPR_OR_1") || symbolnum
					.equals("ST_OPR_XOR_1"))) {
				continue;
			}
			String source = n.getAttributes().getNamedItem("ObjOcc.ID")
					.getNodeValue();
			if (n.hasChildNodes()) {
				for (int j = 0; j < n.getChildNodes().getLength(); j++) {
					if (n.getChildNodes().item(j).getNodeName()
							.equals("CxnOcc")) {
						Node CxnOcc = n.getChildNodes().item(j);
						String dest = CxnOcc.getAttributes().getNamedItem(
								"ToObjOcc.IdRef").getNodeValue();
						if (mapping.get(dest) == null) {
							continue;
						}
						if (net.addEdge((EPCObject) mapping.get(source),
								(EPCObject) mapping.get(dest)) == null) {
							throw (new Exception(
									"<html>Structural properties of EPCs are violated in input file.<br>"
											+ "The following edge could not be added:<br><br>"
											+ mapping.get(source).toString()
											+ " ==> "
											+ mapping.get(dest).toString()
											+ "<br><br>Import aborted.</html>"));
						}
					}
				}
			}
		}
	}

	private static String getTextContent(Node n) {
		NodeList nodeList = n.getChildNodes();
		String textContent = "";
		for (int j = 0; j < nodeList.getLength(); j++) {
			Node k = nodeList.item(j);
			if (k.getNodeType() == Node.TEXT_NODE) {
				textContent = k.getNodeValue();
				break;
			}
		}

		return textContent;
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
