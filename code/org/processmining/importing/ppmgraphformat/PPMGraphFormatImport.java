/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.importing.ppmgraphformat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.models.epcpack.EPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.ui.About;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.epcmining.EPCResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PPMGraphFormatImport implements LogReaderConnectionImportPlugin {

	public String getName() {
		return "Aris graph format";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("xml");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			Document doc;
			NodeList netNodes;

			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = dbf.newDocumentBuilder();

			db.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) {
					if (systemId.indexOf("graph.dtd") != -1) {
						return new InputSource("file:" + About.EXTLIBLOCATION()
								+ "graph.dtd");
					} else {
						return null;
					}
				}
			});

			InputSource inpStream = new InputSource(input);
			inpStream.setSystemId("file:" + System.getProperty("user.dir", ""));
			doc = db.parse(inpStream);

			// check if root element is a <graphlist> tag
			if (!doc.getDocumentElement().getTagName().equals("graphlist")) {
				throw new Exception("graphlist tag not found");
			}

			netNodes = doc.getDocumentElement().getElementsByTagName("graph");

			// No EPC to be read
			if (netNodes.getLength() == 0) {
				return null;
			}
			EPCResult result = new EPCResult(null, (EPC) null);

			Message.add("<importEPCsPPM>", Message.TEST);

			for (int i = 0; i < netNodes.getLength(); i++) {
				EPC net = read(netNodes.item(i));
				net.Test("EPC" + i);
				result.addInHierarchy(net, null, net.getIdentifier());
			}
			Message.add("</importEPCsPPM>", Message.TEST);

			return result;

		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "This plugin imports an EPC in the Aris Graph Format used by Aris PPM.";
	}

	public EPC read(Node node) throws Exception {
		EPC result = new EPC(true);
		parseNet(node, result);
		return result;
	}

	private void parseNet(Node node, EPC net) throws Exception {
		HashMap mapping = new HashMap();
		// First, read all nodes
		NodeList nodes = node.getChildNodes();
		net.setIdentifier(node.getAttributes().getNamedItem("id")
				.getNodeValue());
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			if (!n.getNodeName().equals("node")) {
				continue;
			}

			String objName = "";
			String id = n.getAttributes().getNamedItem("id").getNodeValue();
			for (int j = 0; j < n.getChildNodes().getLength(); j++) {
				if (n.getChildNodes().item(j).getAttributes().getNamedItem(
						"type").getNodeValue().equals("AT_OBJNAME")) {
					try {
						objName = n.getChildNodes().item(j).getChildNodes()
								.item(0).getNodeValue();
					} catch (Exception e) {
					}
				}
			}

			// Check for event type, based on the last space in the AT_OBJNAME
			int spaceIndex = objName.lastIndexOf(" ");
			String event = objName.substring(spaceIndex + 1);
			if (spaceIndex > 0
					&& LogStateMachine.getInstance().isPossibleEvent(event)) {
				objName = objName.substring(0, spaceIndex).trim();
			} else {
				event = "unknown:normal";
			}

			if (nodes.item(i).getAttributes().getNamedItem("type")
					.getNodeValue().equals("OT_FUNC")) {
				EPCFunction f = net.addFunction(new EPCFunction(new LogEvent(
						objName, event), net));
				f.setIdentifier(objName);
				mapping.put(id, f);
			} else if (nodes.item(i).getAttributes().getNamedItem("type")
					.getNodeValue().equals("OT_EVT")) {
				EPCEvent e = net.addEvent(new EPCEvent(objName, net));
				mapping.put(id, e);
			} else if (nodes.item(i).getAttributes().getNamedItem("type")
					.getNodeValue().equals("OT_RULEAND")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.AND, net));
				mapping.put(id, c);
			} else if (nodes.item(i).getAttributes().getNamedItem("type")
					.getNodeValue().equals("OT_RULEXOR")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.XOR, net));
				mapping.put(id, c);
			} else if (nodes.item(i).getAttributes().getNamedItem("type")
					.getNodeValue().equals("OT_RULEOR")) {
				EPCConnector c = net.addConnector(new EPCConnector(
						EPCConnector.OR, net));
				mapping.put(id, c);
			}
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);

			if (!n.getNodeName().equals("edge")) {
				continue;
			}

			if (!n.getAttributes().getNamedItem("type").getNodeValue().equals(
					"CXN_FOLLOWS")) {
				continue;
			}

			String source = n.getAttributes().getNamedItem("source")
					.getNodeValue();
			if (mapping.get(source) == null) {
				continue;
			}
			String dest = n.getAttributes().getNamedItem("target")
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
