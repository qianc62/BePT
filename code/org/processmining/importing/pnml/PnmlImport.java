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

package org.processmining.importing.pnml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.PnmlReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PnmlImport extends PnmlReader implements
		LogReaderConnectionImportPlugin {

	public PnmlImport() {
	}

	public String getName() {
		return "PNML file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("pnml");
	}

	public MiningResult importFile(InputStream input) throws IOException {
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc;
			// PetriNet result = new PetriNet();
			NodeList netNodes;

			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);

			doc = dbf.newDocumentBuilder().parse(input);

			// check if root element is a <pnml> tag
			if (!doc.getDocumentElement().getTagName().equals("pnml")) {
				throw new Exception("pnml tag not found");
			}

			netNodes = doc.getDocumentElement().getElementsByTagName("net");
			PetriNetResult result = new PetriNetResult(null, null);

			if (netNodes.getLength() == 0) {
				// no nets!
				return null;
			}
			Message.add("<openPNML>", Message.TEST);
			for (int i = 0; i < netNodes.getLength(); i++) {
				PetriNet net = read(netNodes.item(i));

				if (!foundToolSpecificInfo) {
					// Only suggest LogEvents if no toolspecific info was found.
					LogEvents logEvents = new LogEvents();
					Iterator it = net.getTransitions().iterator();

					while (it.hasNext()) {
						Transition t = (Transition) it.next();
						String s = t.getIdentifier().replaceAll("\\\\n", "#");
						StringTokenizer st = new StringTokenizer(s, "#");

						if (st.countTokens() == 2) {
							LogEvent e = new LogEvent(st.nextToken(), st
									.nextToken());
							logEvents.add(e);
							t.setLogEvent(e);
						}
					}
				}
				net.Test("PetriNet_" + i);
				result.addInHierarchy(net, null, net.getIdentifier());
			}
			Message.add("</openPNML>", Message.TEST);
			return result;
		} catch (Throwable x) {
			throw new IOException(x.getMessage());
		}
	}

	public String getHtmlDescription() {
		return "Opens A PNML file according to the PNML 2.0 specification";
	}

	private boolean foundToolSpecificInfo = false;

	public boolean shouldFindFuzzyMatch() {
		return !foundToolSpecificInfo;
	}

	protected void foundToolSpecific() {
		foundToolSpecificInfo = true;
	}

}
