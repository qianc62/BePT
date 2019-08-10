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

package org.processmining.importing.owfn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: oWFNImport
 * </p>
 * 
 * <p>
 * Description: Import an oWFN file. Result will be an oWFNet object.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class oWFNImport implements LogReaderConnectionImportPlugin {

	public oWFNImport() {
	}

	public String getName() {
		return "oWFN file";
	}

	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("owfn");
	}

	public PetriNetResult importFile(InputStream input) throws IOException {
		oWFNet petriNet = new oWFNet();
		// Deflate the input, helps parsing it. Deflating means removing
		// comments and white spaces.
		String deflated = deflate(input);
		Message.add(deflated, Message.DEBUG);
		// Parse the deflated input.
		String contents = parseContents(deflated, petriNet);
		if (contents.length() > 0) {
			Message.add(contents, Message.DEBUG);
		}
		Iterator it = petriNet.getPlaces().iterator();
		// Set the source and sink place.
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if (place.getPredecessors().isEmpty()) {
				petriNet.setSourcePlace(place);
			}
			if (place.getSuccessors().isEmpty()) {
				petriNet.setSinkPlace(place);
			}
		}

		petriNet.Test("oWFNImport");

		return new PetriNetResult(petriNet);
	}

	/**
	 * 
	 * @param contents
	 *            String Deflated oWF net formatted string.
	 * @param petriNet
	 *            oWFNet open WF net to hold the parsing results.
	 * @return String The unparsed end of contents.
	 */
	public String parseContents(String contents, oWFNet petriNet) {
		// First, we expect PLACE
		contents = parseToken(contents, "PLACE");
		// Second, INTERNAL
		contents = parseToken(contents, "INTERNAL");
		// Third, a list of (internal) places
		contents = parsePlaces(contents, petriNet, 0);
		// Fourth, INPUT
		contents = parseToken(contents, "INPUT");
		// 5th, a list of (input) places
		contents = parsePlaces(contents, petriNet, 1);
		// 6th, OUTPUT
		contents = parseToken(contents, "OUTPUT");
		// 7th, a list of (output) places
		contents = parsePlaces(contents, petriNet, 2);
		// 8th, INITIALMARKING
		contents = parseToken(contents, "INITIALMARKING");
		// 9th, a weighted list a places (the initial marking)
		contents = parseMarking(contents, petriNet);
		// 10th, FINALMARKING
		contents = parseToken(contents, "FINALMARKING");
		// 11th, a weighted list of places (the final marking)
		contents = parseMarking(contents, petriNet);
		// 12th, a list of transitions
		while (contents.length() > 0) {
			Transition transition = null;
			// First, TRANSITION
			contents = parseToken(contents, "TRANSITION");
			// Second, the label of the transition
			// This is a bit awkward: to find the end of the transition label,
			// we need to find the next keyowrd (CONSUME).
			int i = contents.indexOf("CONSUME");
			if (i > 0) {
				String name = contents.substring(0, i);
				transition = new Transition(name, petriNet);
				petriNet.addTransition(transition);
				contents = contents.substring(i);
				// 3rd, CONSUME
				contents = parseToken(contents, "CONSUME");
				// 4th, a list of weighted places (the preset)
				contents = parseEdges(contents, petriNet, transition, true);
				// 5th, PRODUCE
				contents = parseToken(contents, "PRODUCE");
				// 6th, a list of weighted places (the postset)
				contents = parseEdges(contents, petriNet, transition, false);
			} else {
				break;
			}
		}
		return contents;
	}

	/**
	 * Read the given token from the given contents, if possible.
	 * 
	 * @param contents
	 *            String
	 * @param token
	 *            String
	 * @return String The contents with the token removed, if possible.
	 *         Otherwise, the contents.
	 */
	public String parseToken(String contents, String token) {
		if (contents.startsWith(token)) {
			contents = contents.substring(token.length());
		}
		return contents;
	}

	/**
	 * Read a list of places from the contents. Store the places in the given
	 * oWF net. Mode specifies whether the places are internal (0), input (1),
	 * or output (2).
	 * 
	 * @param contents
	 *            String
	 * @param petriNet
	 *            oWFNet
	 * @param mode
	 *            int
	 * @return String
	 */
	public String parsePlaces(String contents, oWFNet petriNet, int mode) {
		int i = contents.indexOf(";");
		String data = contents.substring(0, i < 0 ? 0 : i) + ",";
		while (data.length() > 1) {
			int j = data.indexOf(",");
			if (j > 0) {
				String place = data.substring(0, j);
				if (mode == 0) {
					petriNet.addPlace(place);
				}
				data = data.substring(j + 1);
			}
		}
		return contents.substring(i + 1);
	}

	/**
	 * Read a list of weighted places, and ignore it.
	 * 
	 * @param contents
	 *            String
	 * @param petriNet
	 *            oWFNet
	 * @return String
	 */
	public String parseMarking(String contents, oWFNet petriNet) {
		int i = contents.indexOf(";");
		String data = contents.substring(0, i < 0 ? 0 : i) + ",";
		while (data.length() > 1) {
			int j = data.indexOf(",");
			int k = data.indexOf(":");
			if (j > 0 && k > 0 && k < j) {
				String place = data.substring(0, k);
				String weight = data.substring(k + 1, j); // assumed to be 1 for
				// the time being
				// Add (place,weight) to initial or final marking
			} else {
				String place = data.substring(0, j);
				// Add (place, "1") to initial or final marking
			}
			data = data.substring(j + 1);
		}
		return contents.substring(i + 1);
	}

	/**
	 * Read a list of weighted places, and store it as preset (isInput = true)
	 * or postset (isInpuyt = false) of the given transition in the given open
	 * WF net.
	 * 
	 * @param contents
	 *            String
	 * @param petriNet
	 *            oWFNet
	 * @param transition
	 *            Transition
	 * @param isInput
	 *            boolean
	 * @return String
	 */
	public String parseEdges(String contents, oWFNet petriNet,
			Transition transition, boolean isInput) {
		int i = contents.indexOf(";");
		String data = contents.substring(0, i < 0 ? 0 : i) + ",";
		while (data.length() > 1) {
			String name;
			int j = data.indexOf(",");
			int k = data.indexOf(":");
			if (j > 0 && k > 0 && k < j) {
				name = data.substring(0, k);
				// String weight = data.substring(k+1, j); // assumed to be 1
				// for the time being
			} else {
				name = data.substring(0, j);
				// Weight is ignored anyway for the time being.
			}
			Place place = petriNet.findPlace(name);
			if (isInput) {
				if (place != null) {
					petriNet.addEdge(place, transition);
				} else {
					petriNet.addInput(name, transition);
				}
			} else {
				if (place != null) {
					petriNet.addEdge(transition, place);
				} else {
					petriNet.addOutput(transition, name);
				}
			}
			// Add (place,weight) to initial or final marking
			data = data.substring(j + 1);
		}
		return contents.substring(i + 1);
	}

	/**
	 * Convert the given stream to a deflated string, where deflating means
	 * removing comments and white spaces.
	 * 
	 * @param input
	 *            InputStream
	 * @return String
	 * @throws IOException
	 */
	public String deflate(InputStream input) throws IOException {
		StringBuffer buffer = new StringBuffer();
		boolean comment = false;
		int c;
		while ((c = input.read()) != -1) {
			if (comment) {
				if (c == '}') {
					comment = false;
				}
			} else if (c == '{') {
				comment = true;
			} else if (!Character.isWhitespace(c)) {
				buffer.append((char) c);
			}
		}
		return buffer.toString();
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:owfn";
	}

	public boolean shouldFindFuzzyMatch() {
		return true;
	}
}
