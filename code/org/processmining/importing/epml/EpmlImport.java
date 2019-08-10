package org.processmining.importing.epml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelHierarchyDirectory;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCDataObject;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCInfSysObject;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.models.epcpack.EPCOrgObject;
import org.processmining.framework.models.epcpack.EPCSubstFunction;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Jan Mendling
 * @version 1.0
 */

public class EpmlImport implements ImportPlugin,
		LogReaderConnectionImportPlugin {

	public String getName() {
		return "EPML file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("epml");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc;
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource inpStream = new InputSource(input);
			// inpStream.setSystemId("file:" + System.getProperty("user.dir",
			// ""));
			doc = db.parse(inpStream);

			// check if root element is a <epml> tag
			if (!(doc.getDocumentElement().getNodeName().equals("epml") || doc
					.getDocumentElement().getNodeName().indexOf(":epml") > -1)) {
				Message.add("epml tag not found", Message.ERROR);
				throw new Exception("epml tag not found");
			} else {
				Message.add("epml root element found");
			}

			EPCResult result = new EPCResult(null, (ConfigurableEPC) null);
			HashMap function_epc = new HashMap();
			HashMap epcId_net = new HashMap();
			HashMap noncfId_Node = new HashMap();
			Vector relation = new Vector();
			traverseForNonControlFlow(doc.getDocumentElement(), noncfId_Node,
					relation, "");
			// Message.add("noncf traversed\n"+relation.toString()+noncfId_Node.toString());
			HashMap Id_noncfNodes = getSourceAndNoncfNode(relation,
					noncfId_Node);
			// Message.add("Source and Noncf got\n"+Id_noncfNodes.toString());
			result = traverseEPML(result, doc.getDocumentElement(), null,
					function_epc, epcId_net, Id_noncfNodes);
			Iterator hierarchicalFunctions = function_epc.keySet().iterator();
			while (hierarchicalFunctions.hasNext()) {
				EPCSubstFunction f = (EPCSubstFunction) hierarchicalFunctions
						.next();
				f.setSubstitutedEPC((ConfigurableEPC) epcId_net
						.get(function_epc.get(f)));
				// Message.add(f.getSubstitutedEPC().getName());
			}

			return result;

		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "http://is.tm.tue.nl/trac/prom/wiki/ProMPlugins/EPMLImport";
	}

	public HashMap getSourceAndNoncfNode(Vector relation, HashMap noncfId_Node) {
		HashMap Id_noncfNodes = new HashMap();
		if (!noncfId_Node.isEmpty()) {
			Iterator relations = relation.iterator();
			while (relations.hasNext()) {
				String current = (String) relations.next();
				int space = current.indexOf(" ");
				String from = current.substring(0, space);
				String to = current.substring(space + 1, current.length());
				if (noncfId_Node.containsKey(from)) {
					if (Id_noncfNodes.containsKey(to)) {
						Vector list = (Vector) Id_noncfNodes.get(from);
						list.add(noncfId_Node.get(to));
					} else {
						Vector newlist = new Vector();
						newlist.add(noncfId_Node.get(from));
						Id_noncfNodes.put(to, newlist);
					}
				}
				if (noncfId_Node.containsKey(to)) {
					if (Id_noncfNodes.containsKey(from)) {
						Vector list = (Vector) Id_noncfNodes.get(to);
						list.add(noncfId_Node.get(from));
					} else {
						Vector newlist = new Vector();
						newlist.add(noncfId_Node.get(to));
						Id_noncfNodes.put(from, newlist);
					}
				}
			}
		}
		return Id_noncfNodes;
	}

	public static void traverseForNonControlFlow(Node currentNode,
			HashMap epcIdId_node, Vector relation, String epcId) {
		if (currentNode.hasChildNodes()) {
			for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
				Node currentChild = currentNode.getChildNodes().item(i);
				if (currentChild.getNodeName().equals("function")) {
					continue;
				}
				if (currentChild.getNodeName().equals("event")) {
					continue;
				}
				if (currentChild.getNodeName().equals("and")) {
					continue;
				}
				if (currentChild.getNodeName().equals("or")) {
					continue;
				}
				if (currentChild.getNodeName().equals("xor")) {
					continue;
				}
				if (currentChild.getNodeName().equals("processInterface")) {
					continue;
				}
				if (currentChild.getNodeName().equals("arc")) {
					continue;
				}
				if (currentChild.getNodeName().equals("directory")) {
					traverseForNonControlFlow(currentChild, epcIdId_node,
							relation, epcId);
				}
				if (currentChild.getNodeName().equals("epc")) {
					if (currentChild.getAttributes().getNamedItem("epcId") == null) {
						epcId = currentChild.getAttributes().getNamedItem(
								"EpcId").getNodeValue();
					} else {
						epcId = currentChild.getAttributes().getNamedItem(
								"epcId").getNodeValue();
					}
					traverseForNonControlFlow(currentChild, epcIdId_node,
							relation, epcId);
				}
				if (currentChild.getNodeName().equals("participant")) {
					String id = currentChild.getAttributes().getNamedItem("id")
							.getNodeValue();
					epcIdId_node.put(epcId + "_" + id, currentChild);
					continue;
				}
				if (currentChild.getNodeName().equals("dataField")) {
					String id = currentChild.getAttributes().getNamedItem("id")
							.getNodeValue();
					epcIdId_node.put(epcId + "_" + id, currentChild);
					continue;
				}
				if (currentChild.getNodeName().equals("application")) {
					String id = currentChild.getAttributes().getNamedItem("id")
							.getNodeValue();
					epcIdId_node.put(epcId + "_" + id, currentChild);
					continue;
				}
				if (currentChild.getNodeName().equals("relation")) {
					String from = currentChild.getAttributes().getNamedItem(
							"from").getNodeValue();
					String to = currentChild.getAttributes().getNamedItem("to")
							.getNodeValue();
					relation.add(epcId + "_" + from + " " + epcId + "_" + to);
					continue;
				}
			}
		}
	}

	public EPCResult traverseEPML(EPCResult partialResult, Node currentNode,
			Object parent, HashMap function_epc, HashMap epcId_net,
			HashMap Id_noncfNodes) {
		if (currentNode.hasChildNodes()) {
			for (int i = 0; i < currentNode.getChildNodes().getLength(); i++) {
				Node currentChild = currentNode.getChildNodes().item(i);
				if (currentChild.getNodeName().equals("directory")) {
					if (currentChild.getAttributes().getNamedItem("name") == null) {
						String name = currentChild.getAttributes()
								.getNamedItem("Name").getNodeValue();
						Message.add("directory " + name);
						String domstring = "-" + currentChild.hashCode();
						ModelHierarchyDirectory dir = new ModelHierarchyDirectory(
								domstring, name);
						partialResult.addInHierarchy(dir, parent, name);
						partialResult = traverseEPML(partialResult,
								currentChild, dir, function_epc, epcId_net,
								Id_noncfNodes);
					} else {
						String name = currentChild.getAttributes()
								.getNamedItem("name").getNodeValue();
						Message.add("directory " + name);
						String domstring = "-" + currentChild.hashCode();
						ModelHierarchyDirectory dir = new ModelHierarchyDirectory(
								domstring, name);
						partialResult.addInHierarchy(dir, parent, name);
						partialResult = traverseEPML(partialResult,
								currentChild, dir, function_epc, epcId_net,
								Id_noncfNodes);
					}
				}
				if (currentChild.getNodeName().equals("epc")) {
					try {
						ConfigurableEPC net = read(currentChild, function_epc,
								Id_noncfNodes);
						String name = null;
						if (currentChild.getAttributes().getNamedItem("name") == null) {
							name = currentChild.getAttributes().getNamedItem(
									"Name").getNodeValue();
						} else {
							name = currentChild.getAttributes().getNamedItem(
									"name").getNodeValue();
						}
						name = name.replaceAll("\n", " ");
						// Message.add("epc "+name);
						partialResult.addInHierarchy(net, parent, name);
						if (currentChild.getAttributes().getNamedItem("epcId") == null) {
							epcId_net.put(currentChild.getAttributes()
									.getNamedItem("EpcId").getNodeValue(), net);
						} else {
							epcId_net.put(currentChild.getAttributes()
									.getNamedItem("epcId").getNodeValue(), net);
						}
					} catch (Throwable x) {
						Message.add(x.getMessage());
						// throw new IOException(x.getMessage());
					}
				}
			}
		}
		return partialResult;
	}

	public static String getLinkedEpcId(Node epcnode) {
		String linkedEpc = "-1";
		if (epcnode.hasChildNodes()) {
			for (int i = 0; i < epcnode.getChildNodes().getLength(); i++) {
				Node current = epcnode.getChildNodes().item(i);
				if (!(current.getNodeName().equals("toProcess"))) {
					continue;
				}
				linkedEpc = current.getAttributes().getNamedItem("linkToEpcId")
						.getNodeValue();
				break;
			}
		}
		return linkedEpc;
	}

	public static boolean isConfigurable(Node inputNode) {
		if (inputNode.hasChildNodes()) {
			NodeList subnodes = inputNode.getChildNodes();
			for (int i = 0; i < subnodes.getLength(); i++) {
				Node current = subnodes.item(i);
				if (!(current.getNodeName().startsWith("configurable"))) {
					continue;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public static ConfigurableEPC read(Node node, HashMap function_epc,
			HashMap Id_noncfNodes) throws Exception {
		ConfigurableEPC result = new ConfigurableEPC(false);
		parseNet(node, result, function_epc, Id_noncfNodes);
		return result;
	}

	private static void parseNet(Node node, ConfigurableEPC net,
			HashMap function_epc, HashMap Id_noncfNodes) throws Exception {
		HashMap mapping = new HashMap();
		// First, read all nodes
		NodeList nodes = node.getChildNodes();
		String epcId = null;
		String epcname = null;
		if (node.getAttributes().getNamedItem("epcId") == null) {
			epcId = node.getAttributes().getNamedItem("EpcId").getNodeValue();
		} else {
			epcId = node.getAttributes().getNamedItem("epcId").getNodeValue();
		}
		if (node.getAttributes().getNamedItem("name") == null) {
			epcname = node.getAttributes().getNamedItem("Name").getNodeValue();
		} else {
			epcname = node.getAttributes().getNamedItem("name").getNodeValue();
		}

		net.setIdentifier(epcname); //
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			if (!(n.getNodeName().equals("function")
					|| n.getNodeName().equals("event")
					|| n.getNodeName().equals("and")
					|| n.getNodeName().equals("or") || n.getNodeName().equals(
					"xor"))) {
				continue;
			}

			String ownName = "";
			String id = n.getAttributes().getNamedItem("id").getNodeValue();
			for (int j = 0; j < n.getChildNodes().getLength(); j++) {
				if (n.getChildNodes().item(j).getNodeName().equals("name")) {
					try {
						ownName = n.getChildNodes().item(j).getFirstChild()
								.getNodeValue();
					} catch (Exception e) {
					}
				} else if ((n.getChildNodes().item(j).getAttributes() != null)
						&& (n.getChildNodes().item(j).getAttributes()
								.getNamedItem("defId") != null)) {
					try {
					} catch (Exception e) {
					}
				}
			}
			if (ownName == null) {
				ownName = id;
			}
			ownName = ownName.replaceAll("\n", "\\\\n");
			if (n.getNodeName().equals("function")) {
				String potentialLink = getLinkedEpcId(n);
				EPCFunction f = null;
				if (!potentialLink.equals("-1")) {
					f = (EPCSubstFunction) net
							.addFunction(new EPCSubstFunction(new LogEvent(
									ownName, "unknown:normal"),
									isConfigurable(n), net, null));
					f.setIdentifier(ownName);
					mapping.put(id, f);
					function_epc.put(f, potentialLink);
				} else {
					f = (EPCFunction) net.addFunction(new EPCFunction(
							new LogEvent(ownName, "unknown:normal"),
							isConfigurable(n), net));
					f.setIdentifier(ownName);
					mapping.put(id, f);
				}
				if (Id_noncfNodes != null) {
					if (Id_noncfNodes.containsKey(epcId + "_" + id)) {
						Vector noncfs = (Vector) Id_noncfNodes.get(epcId + "_"
								+ id);
						// Message.add(noncfs.toString());
						Iterator noncfList = noncfs.iterator();
						while (noncfList.hasNext()) {
							Node current = (Node) noncfList.next();
							if (current.getNodeName().equals("application")) {
								String appname = "";
								if (current.hasChildNodes()) {
									for (int j = 0; j < current.getChildNodes()
											.getLength(); j++) {
										if (current.getChildNodes().item(j)
												.getNodeName().equals("name")) {
											try {
												appname = current
														.getChildNodes()
														.item(j)
														.getFirstChild()
														.getNodeValue();
											} catch (Exception e) {
											}
										}
									}
								}
								f.addInfSysObject(new EPCInfSysObject(appname,
										f));
							}
							if (current.getNodeName().equals("participant")) {
								String orgname = "";
								if (current.hasChildNodes()) {
									for (int j = 0; j < current.getChildNodes()
											.getLength(); j++) {
										if (current.getChildNodes().item(j)
												.getNodeName().equals("name")) {
											try {
												orgname = current
														.getChildNodes()
														.item(j)
														.getFirstChild()
														.getNodeValue();
											} catch (Exception e) {
											}
										}
									}
								}
								f.addOrgObject(new EPCOrgObject(orgname, f));
							}

							if (current.getNodeName().equals("dataField")) {
								String dataname = "";
								if (current.hasChildNodes()) {
									for (int j = 0; j < current.getChildNodes()
											.getLength(); j++) {
										if (current.getChildNodes().item(j)
												.getNodeName().equals("name")) {
											try {
												dataname = current
														.getChildNodes()
														.item(j)
														.getFirstChild()
														.getNodeValue();
											} catch (Exception e) {
											}
										}
									}
								}
								f.addDataObject(new EPCDataObject(dataname, f));
							}
						}
					}
				}
				// Message.add(f.getNumDataObjects()+" "+
				// f.getNumInfSysObjects()+" "+ f.getNumOrgObjects());
			} else if (n.getNodeName().equals("event")) {
				EPCEvent e = net.addEvent(new EPCEvent(ownName, net));
				e.setIdentifier(ownName);
				mapping.put(id, e);
			} else if (n.getNodeName().equals("and")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.AND, isConfigurable(n), net));
				mapping.put(id, c);
			} else if (n.getNodeName().equals("or")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.OR, isConfigurable(n), net));
				mapping.put(id, c);
			} else if (n.getNodeName().equals("xor")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.XOR, isConfigurable(n), net));
				mapping.put(id, c);
			}
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			if (!n.getNodeName().equals("arc")) {
				continue;
			}

			Node flow = null;
			for (int j = 0; j < n.getChildNodes().getLength(); j++) {
				if (n.getChildNodes().item(j).getNodeName().equals("flow")) {
					flow = n.getChildNodes().item(j);
				}
			}
			if (flow == null) {
				continue;
			}

			String source = flow.getAttributes().getNamedItem("source")
					.getNodeValue();
			if (mapping.get(source) == null) {
				continue;
			}
			String dest = flow.getAttributes().getNamedItem("target")
					.getNodeValue();
			if (mapping.get(dest) == null) {
				continue;
			}
			if (net.addEdge((EPCObject) mapping.get(source),
					(EPCObject) mapping.get(dest)) == null) {
				throw (new Exception(
						"<html>Structural properties of EPCs are violated in input file.<br>"
								+ "The following edge could not be added:<br><br>"
								+ mapping.get(source).toString() + " ==> "
								+ mapping.get(dest).toString()
								+ "<br><br>Import aborted.</html>"));
			}
			;
		}

	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}

}
