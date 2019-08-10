package org.processmining.importing.vdx;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Paul Barborka
 * @version 1.0
 */

public class vdxImport implements ImportPlugin, LogReaderConnectionImportPlugin {

	public String getName() {
		return "VDX file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("vdx");
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/VDXImport";
	}

	public MiningResult importFile(InputStream input) throws IOException {

		try {
			Message.add("starting importing vdx file", Message.DEBUG);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc;
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource inpStream = new InputSource(input);
			inpStream.setSystemId("file:" + System.getProperty("user.dir", ""));
			doc = db.parse(inpStream);
			Element rootElem = doc.getDocumentElement();

			// check if root element is a aml tag
			Message.add("parsing done" + doc, Message.DEBUG);
			if (!(rootElem.getNodeName().equals("VisioDocument"))) {
				Message.add("VisioDocument tag not found", Message.ERROR);
				throw new Exception("VisioDocument tag not found");
			} else {
				Message.add("VisioDocument root element found", Message.DEBUG);
			}

			EPCResult result = new EPCResult(null, (ConfigurableEPC) null);
			HashMap mastersMap = new HashMap();

			// **********************
			// first the masters
			// **********************
			NodeList mastersNode = rootElem.getElementsByTagName("Masters");
			if (mastersNode.getLength() == 0)
				Message.add("no Masters tag found", Message.ERROR);
			mastersNode = mastersNode.item(0).getChildNodes();
			parseMasters(mastersNode, mastersMap); // parse them into HashMap

			// **********************
			// then the single pages - can be more than one
			// the add them hierachically
			// **********************
			NodeList pagesNode = rootElem.getElementsByTagName("Pages");
			if (mastersNode.getLength() == 0)
				Message.add("no Pages tag found", Message.ERROR);
			pagesNode = pagesNode.item(0).getChildNodes();

			for (int i = 0; i < pagesNode.getLength() /* && i<5 */; i++) { // max
				// of
				// 5
				// pages

				// for the xml parsing
				HashMap shapesMap = new HashMap();
				HashMap shapesTextMap = new HashMap();
				HashMap connectsMap = new HashMap();

				// for the building of the EPC Objects
				HashMap shapesEPCObjectsMap = new HashMap();
				HashMap shapesNotInEPCObjectsMap = new HashMap();

				Element currentChild = (Element) pagesNode.item(i);

				// create a new EPC for each page
				ConfigurableEPC newEPC = new ConfigurableEPC(false);
				String pageName = currentChild.getAttribute("Name");
				if (pageName == null || pageName.equals(""))
					pageName = currentChild.getAttribute("NameU");
				if (pageName == null || pageName.equals(""))
					pageName = "A";
				newEPC.setIdentifier(pageName);

				Message.add("parsing page: " + pageName, Message.DEBUG);

				// parse the shapes
				NodeList shapesNode = currentChild
						.getElementsByTagName("Shapes");
				if (shapesNode.getLength() == 0)
					Message.add("no Shapes tag found", Message.ERROR);
				else {
					shapesNode = shapesNode.item(0).getChildNodes();
					parsePageShapes(shapesNode, shapesMap, shapesTextMap);
				}

				// parse the connectors
				NodeList connectsNode = currentChild
						.getElementsByTagName("Connects");
				if (connectsNode.getLength() == 0)
					Message.add("no Connects tag found", Message.ERROR);
				else {
					connectsNode = connectsNode.item(0).getChildNodes();
					parsePageConnects(connectsNode, connectsMap);
				}

				// now build the EPC
				Iterator shapesMapIt = shapesMap.keySet().iterator();
				while (shapesMapIt.hasNext()) {
					String currKey = (String) shapesMapIt.next();
					String whatIsIt = (String) mastersMap.get(shapesMap
							.get(currKey));
					Message.add("Shapes mapping: " + currKey + " " + whatIsIt,
							Message.DEBUG);

					// create the right object, also parse Text needed
					// then add into objects map
					if (whatIsIt.equals("Event")) {
						String identifier = (String) shapesTextMap.get(currKey);
						if (identifier == null || identifier.equals(""))
							identifier = "E" + currKey;
						EPCEvent e = newEPC.addEvent(new EPCEvent(identifier,
								newEPC));
						shapesEPCObjectsMap.put(currKey, e);
						continue;
					} else {
						if (whatIsIt.equals("Function")) {
							String identifier = (String) shapesTextMap
									.get(currKey);
							if (identifier == null || identifier.equals(""))
								identifier = "F" + currKey;
							EPCFunction f = (EPCFunction) newEPC
									.addFunction(new EPCFunction(new LogEvent(
											identifier, "unknown:normal"),
											true, newEPC));
							f.setIdentifier(identifier);
							shapesEPCObjectsMap.put(currKey, f);
							continue;
						} else {
							if (whatIsIt.equals("AND")) {
								EPCConnector c = newEPC
										.addConnector(new EPCConnector(
												EPCConnector.AND, true, newEPC));
								c.setIdentifier("AND" + currKey);
								shapesEPCObjectsMap.put(currKey, c);
								continue;
							} else {
								if (whatIsIt.equals("OR")) {
									EPCConnector c = newEPC
											.addConnector(new EPCConnector(
													EPCConnector.OR, true,
													newEPC));
									c.setIdentifier("OR" + currKey);
									shapesEPCObjectsMap.put(currKey, c);
									continue;
								} else {
									if (whatIsIt.equals("XOR")) {
										EPCConnector c = newEPC
												.addConnector(new EPCConnector(
														EPCConnector.XOR, true,
														newEPC));
										c.setIdentifier("XOR" + currKey);
										shapesEPCObjectsMap.put(currKey, c);
										continue;
									} else {
										if (!whatIsIt
												.equals("Dynamic connector")) {
											Message
													.add("Ignoring not available Object: "
															+ whatIsIt);
											shapesNotInEPCObjectsMap.put(
													currKey,
													"object not supported");
										}
									}
								}
							}
						}
					}
				}

				// add the connectors
				Iterator connectsMapIt = connectsMap.keySet().iterator();
				while (connectsMapIt.hasNext()) {
					String currKey = (String) connectsMapIt.next();
					String bothEnds = (String) connectsMap.get(currKey);
					StringTokenizer tknz = new StringTokenizer(bothEnds, ";");
					String beginID = tknz.nextToken();
					String endID = tknz.nextToken();
					// now draw the edge
					if (shapesMap.get(beginID) != null
							&& shapesMap.get(endID) != null) {
						// only draw edge if the objects are not ignored
						if (shapesNotInEPCObjectsMap.get(beginID) == null
								&& shapesNotInEPCObjectsMap.get(endID) == null) {
							newEPC.addEdge((EPCObject) shapesEPCObjectsMap
									.get(beginID),
									(EPCObject) shapesEPCObjectsMap.get(endID));
						}
					}

				}

				result.addInHierarchy(newEPC, result, pageName);

			}

			return result;

		} catch (Throwable x) {
			x.printStackTrace();
			Message.add(x.toString() + " " + x.getCause());
			throw new IOException(x.getMessage());
		}

	}

	public static void parseMasters(NodeList mastersNode, HashMap mastersMap) {

		// get the single master nodes
		Message.add(mastersNode.getLength() + " master tags found",
				Message.DEBUG);
		for (int i = 0; i < mastersNode.getLength(); i++) {
			Element currentChild = (Element) mastersNode.item(i);
			mastersMap.put(currentChild.getAttributeNode("ID").getValue(),
					currentChild.getAttributeNode("NameU").getValue());
			Message.add(currentChild.getAttributeNode("ID").getValue(),
					Message.DEBUG);
			Message.add(currentChild.getAttributeNode("NameU").getValue(),
					Message.DEBUG);
		}

	}

	public static void parsePageShapes(NodeList shapeNodes, HashMap shapesMap,
			HashMap shapesTextMap) {

		// get the single page shapes
		for (int i = 0; i < shapeNodes.getLength(); i++) {
			Element currentChild = (Element) shapeNodes.item(i);
			String shapeId = currentChild.getAttributeNode("ID").getValue();
			String masterId = currentChild.getAttributeNode("Master")
					.getValue();
			shapesMap.put(shapeId, masterId);

			String labelText = "";
			for (int j = 0; j < currentChild.getChildNodes().getLength(); j++) {
				if (currentChild.getChildNodes().item(j).getNodeName().equals(
						"Text")) {
					try {
						labelText = currentChild.getChildNodes().item(j)
								.getFirstChild().getNodeValue();
					} catch (Exception e) {
					}
				}
			}

			labelText = (labelText == null ? "" : labelText.replaceAll("\n",
					"\\\\n"));
			shapesTextMap.put(shapeId, labelText);
			Message.add("Shape " + shapeId + " " + labelText, Message.DEBUG);
		}

	}

	public static void parsePageConnects(NodeList connectNodes,
			HashMap connectsMap) {

		// get the single page shapes
		for (int i = 0; i < connectNodes.getLength(); i += 2) {
			Element currentChildBegin = (Element) connectNodes.item(i);
			Element currentChildEnd = (Element) connectNodes.item(i + 1);

			String connectIDBegin = currentChildBegin.getAttributeNode(
					"FromSheet").getValue();
			String connectIDEnd = null;
			try {
				connectIDEnd = currentChildEnd.getAttributeNode("FromSheet")
						.getValue();
			} catch (Exception e) {
				Message.add("XML-structure problem with id " + currentChildEnd);
			}

			if (!connectIDBegin.equals(connectIDEnd)) {
				// begin und end ID des connectors sind nicht gleich!
				Message.add("Ordering problem with connectors!", Message.ERROR);
				i--;

			} else {
				String whatIDBegin = currentChildBegin.getAttributeNode(
						"ToSheet").getValue(); // which shape ID
				String whatIDEnde = currentChildEnd.getAttributeNode("ToSheet")
						.getValue(); // which shape ID
				connectsMap.put(connectIDBegin, whatIDBegin + ";" + whatIDEnde);

			}

		}

	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}

}
