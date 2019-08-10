package org.processmining.exporting.petrinet;

import org.processmining.exporting.ExportPlugin;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.util.StringTokenizer;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.ModelGraphEdge;
import java.util.Iterator;
import java.util.HashMap;

/**
 * <p>
 * Title: oWF net export plug-in
 * </p>
 * 
 * <p>
 * Description: Exports an oWF net to file, as suggested by Niels Lohmann.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class oWFNExport implements ExportPlugin {

	/**
	 * Create the export plug-in.
	 */
	public oWFNExport() {
	}

	/**
	 * Menu item.
	 * 
	 * @return String
	 */
	public String getName() {
		return "oWFN";
	}

	/**
	 * Accepts any oWF net.
	 * 
	 * @param object
	 *            ProvidedObject
	 * @return boolean
	 */
	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Export the given oWF net to the given stream.
	 * 
	 * @param object
	 *            ProvidedObject
	 * @param output
	 *            OutputStream
	 * @throws IOException
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof PetriNet) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
						output));
				String export = write((oWFNet) o[i]);
				bw.write(export);
				bw.close();
				StringTokenizer lineTokenizer = new StringTokenizer(export,
						"\n");
				int nofLines = lineTokenizer.countTokens();
				int nofChars = export.length();
				Message.add("<oWFNExport nofLines=\"" + nofLines
						+ "\" nofChars=\"" + nofChars + "\"/>", Message.TEST);
				return;
			}
		}
	}

	/**
	 * Writes the given oWF net to string.
	 * 
	 * @param owfn
	 *            oWFNet
	 * @return String
	 */
	public String write(oWFNet owfn) {

		// First, the internal places.
		String result = "PLACE\nINTERNAL\n";
		String sep = "  ";
		for (Place place : owfn.getPlaces()) {
			result += sep + place.getIdentifier().replaceAll(" ", "_");
			sep = ", ";
		}
		result += ";\n";

		// Second, the input interface places.
		result += "INPUT\n";
		sep = "  ";
		for (String input : owfn.getInputs().keySet()) {
			result += sep + input.replaceAll(" ", "_");
			sep = ", ";
		}
		result += ";\n";

		// Third, the output interface places.
		result += "OUTPUT\n";
		sep = "  ";
		for (String output : owfn.getOutputs().keySet()) {
			result += sep + output.replaceAll(" ", "_");
			sep = ", ";
		}
		result += ";\n\n";

		// Fourth, the initial marking.
		result += "INITIALMARKING\n";
		result += "  "
				+ owfn.getSourcePlace().getIdentifier().replaceAll(" ", "_")
				+ ": 1;\n";

		// Fifth, the final condition.
		result += "FINALCONDITION\n";
		result += "  ("
				+ owfn.getSinkPlace().getIdentifier().replaceAll(" ", "_")
				+ " = 1) AND ALL_OTHER_PLACES_EMPTY;\n\n";

		// Sixth, the transitions.
		for (Transition transition : owfn.getTransitions()) {
			result += "TRANSITION "
					+ transition.getIdentifier().replaceAll(" ", "_") + "\n";
			result += "  CONSUME ";

			// Collect all input places for this transition.
			Iterator it2 = transition.getInEdgesIterator();
			HashMap<String, Integer> inputs = new HashMap<String, Integer>();
			while (it2.hasNext()) {
				ModelGraphEdge edge = (ModelGraphEdge) it2.next();
				Place place = (Place) edge.getSource();
				if (inputs.keySet().contains(place.getIdentifier())) {
					inputs.put(place.getIdentifier(), inputs.get(place
							.getIdentifier()) + 1);
				} else {
					inputs.put(place.getIdentifier(), 1);
				}
			}
			for (String input : owfn.getInputs().keySet()) {
				// owfn.getInputs().get(input).contains(transition) seems not to
				// work (?).
				for (Transition t : owfn.getInputs().get(input)) {
					if (transition.equals(t)) {
						inputs.put(input, 1);
					}
				}
			}

			// Write out all input places.
			sep = "";
			for (String input : inputs.keySet()) {
				result += sep + input.replaceAll(" ", "_") + ": "
						+ inputs.get(input);
				sep = ", ";
			}
			result += ";\n";

			result += "  PRODUCE ";

			// Collect all output places for this transition.
			it2 = transition.getOutEdgesIterator();
			HashMap<String, Integer> outputs = new HashMap<String, Integer>();

			while (it2.hasNext()) {
				ModelGraphEdge edge = (ModelGraphEdge) it2.next();
				Place place = (Place) edge.getDest();
				if (outputs.keySet().contains(place.getIdentifier())) {
					outputs.put(place.getIdentifier(), outputs.get(place
							.getIdentifier()) + 1);
				} else {
					outputs.put(place.getIdentifier(), 1);
				}
			}
			for (String output : owfn.getOutputs().keySet()) {
				for (Transition t : owfn.getOutputs().get(output)) {
					if (transition.equals(t)) {
						outputs.put(output, 1);
					}
				}
			}

			// Write out all output places.
			sep = "";
			for (String output : outputs.keySet()) {
				result += sep + output.replaceAll(" ", "_") + ": "
						+ outputs.get(output);
				sep = ", ";
			}
			result += ";\n\n";
		}
		// Done.
		return result;
	}

	/**
	 * File extension.
	 * 
	 * @return String
	 */
	public String getFileExtension() {
		return "owfn";
	}

	/**
	 * Help page.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:owfn";
	}
}
