package org.processmining.exporting.fsm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.models.fsm.FSM;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.ui.Message;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.mining.fsm.FsmMinerPayload;
import org.processmining.mining.fsm.FsmSettings;
import org.processmining.mining.fsm.FsmHorizonSettings;
import org.processmining.exporting.Exporter;

/**
 * <p>
 * Title: FSMPayloadExport
 * </p>
 * 
 * <p>
 * Description:
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
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FSMPayloadExport {

	@Exporter(name = "Payload-attributed FSM file", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:export:fsm2pfsm", extension = "fsm")
	/*
	 * * Exports the given FSM to a fsm file. If possible, the payload
	 * information stored at the FSM states is used to generate attribute
	 * information.
	 * 
	 * @param fsm FSM the FSM to export.
	 * 
	 * @param out OutputStream the stream to export to.
	 * 
	 * @throws IOException
	 */
	public static void FSMExport(FSM fsm, OutputStream out) throws IOException {
		// Check whether FSM is empty
		if (!fsm.getVerticeList().isEmpty()) {
			// Not empty, get first state to retrieve the settings used to
			// obtain
			// the FSM.
			FSMState state = (FSMState) fsm.getVerticeList().get(0);
			// If the payload is of class FsmMinerPayload, then we can use this
			// payload
			// to generate attrbiute information.
			if (state.getPayload() instanceof FsmMinerPayload) {
				// Get the settings and strat the export.
				FsmMinerPayload payload = (FsmMinerPayload) state.getPayload();
				FsmSettings settings = payload.getSettings();

				export(fsm, out, settings);
			} else {
				// Cannot use th epayload to generate attributes. Revert to the
				// binary FSM export (which codes all states using a minimal set
				// of
				// boolean attrbiutes.)
				Message
						.add("No suitable payload found, reverting to binary-attributed export");
				FSMExport.FSMExport(fsm, out);
			}
		}
	}

	/**
	 * Exports the FSM using the payload information.
	 * 
	 * @param fsm
	 *            FSM the FSM with payload information.
	 * @param out
	 *            OutputStream the stream to export to.
	 * @param settings
	 *            FsmSettings the settings used to generate the FSM. These
	 *            settigns determine which attributes should be there. Not in
	 *            every state every selected attribute is defined.
	 * @throws IOException
	 */
	private static void export(FSM fsm, OutputStream out, FsmSettings settings)
			throws IOException {

		/**
		 * Create two maps to store all attribute info. The first map, sysMap,
		 * will contain system information (model element, originator, even
		 * type), whereas the second map (dataMap) will contain log-based
		 * attribute info.
		 */
		TreeMap<String, TreeSet<String>> sysMap = new TreeMap<String, TreeSet<String>>();
		TreeMap<String, TreeSet<String>> dataMap = new TreeMap<String, TreeSet<String>>();

		// Construct the sysMap.
		for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
			FsmHorizonSettings horizonSettings = settings.getHorizonSettings(
					true, mode);
			if (horizonSettings.getUse()) {
				TreeSet<String> set = new TreeSet<String>();
				for (ModelGraphVertex vertex : fsm.getVerticeList()) {
					FSMState state = (FSMState) vertex;
					FsmMinerPayload payload = (FsmMinerPayload) state
							.getPayload();
					set.add(payload.getBwdPayload(mode));
				}
				sysMap.put(FsmMinerPayload.getLabel(mode) + ", backward", set);
			}
			horizonSettings = settings.getHorizonSettings(false, mode);
			if (horizonSettings.getUse()) {
				TreeSet<String> set = new TreeSet<String>();
				for (ModelGraphVertex vertex : fsm.getVerticeList()) {
					FSMState state = (FSMState) vertex;
					FsmMinerPayload payload = (FsmMinerPayload) state
							.getPayload();
					set.add(payload.getFwdPayload(mode));
				}
				sysMap.put(FsmMinerPayload.getLabel(mode) + ", forward", set);
			}
		}

		// Construct the dataMap.
		for (ModelGraphVertex vertex : fsm.getVerticeList()) {
			FSMState state = (FSMState) vertex;
			FsmMinerPayload payload = (FsmMinerPayload) state.getPayload();
			Map<String, String> attributeValues = payload.getAttributePayload();
			for (String attribute : attributeValues.keySet()) {
				TreeSet<String> set;
				if (dataMap.keySet().contains(attribute)) {
					set = dataMap.get(attribute);
				} else {
					set = new TreeSet<String>();
				}
				set.add(attributeValues.get(attribute));
				dataMap.put(attribute, set);
			}
		}

		/**
		 * For the FSM file format, we need to know the index of values.
		 * Therefore, we need to store the constructed informationis a slightly
		 * different way.
		 */
		// First, copy all attribute names into an array.
		String[] attributes = new String[sysMap.keySet().size()
				+ dataMap.keySet().size()];
		int index = 0;
		for (String attribute : sysMap.keySet()) {
			attributes[index] = attribute;
			index++;
		}
		for (String attribute : dataMap.keySet()) {
			attributes[index] = "Attribute " + attribute;
			index++;
		}

		// Second, copy all attrbiute values etc. into a map of arrays and a map
		// of maps (the inverted first map).
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		HashMap<String, HashMap<String, Integer>> mapIndices = new HashMap<String, HashMap<String, Integer>>();
		for (String attribute : sysMap.keySet()) {
			String[] values = new String[sysMap.get(attribute).size()];
			HashMap<String, Integer> valueIndices = new HashMap<String, Integer>();
			index = 0;
			for (String value : sysMap.get(attribute)) {
				values[index] = value;
				valueIndices.put(value, index);
				index++;
			}
			map.put(attribute, values);
			mapIndices.put(attribute, valueIndices);
		}
		for (String attribute : dataMap.keySet()) {
			// Log-based attributes may be undefined
			String[] values = new String[dataMap.get(attribute).size() + 1];
			values[0] = "<undefined>";
			HashMap<String, Integer> valueIndices = new HashMap<String, Integer>();
			valueIndices.put("<undefined>", 0);
			index = 1;
			for (String value : dataMap.get(attribute)) {
				values[index] = value;
				valueIndices.put(value, index);
				index++;
			}
			map.put("Attribute " + attribute, values);
			mapIndices.put("Attribute " + attribute, valueIndices);
		}

		/**
		 * Now we can export everything.
		 */
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		// Frist, write the attribute info. For sake of simplicity, all have
		// type "stirng".
		for (String attribute : attributes) {
			bw.write(attribute.replaceAll(" ", "_"));
			bw.write("(" + map.get(attribute).length + ") string");
			for (String value : map.get(attribute)) {
				bw.write(" \"" + value.replaceAll(" ", "_") + "\"");
			}
			bw.write("\n");
		}
		bw.write("---\n");
		// Second, write the states. A state corresponds to a value index for
		// every attribute.
		HashMap<ModelGraphVertex, Integer> stateIndices = new HashMap<ModelGraphVertex, Integer>();
		int stateIndex = 1;
		for (ModelGraphVertex vertex : fsm.getVerticeList()) {
			stateIndices.put(vertex, stateIndex++);
			FSMState state = (FSMState) vertex;
			FsmMinerPayload payload = (FsmMinerPayload) state.getPayload();
			String prefix = "";
			for (index = 0; index < attributes.length; index++) {
				String attribute = attributes[index];
				// Get the value for this attribute.
				String value = null;
				if (attribute.startsWith("Model element, backward")) {
					value = payload.getBwdPayload(FsmMinerPayload.MODELELEMENT);
				} else if (attribute.startsWith("Model element, forward")) {
					value = payload.getFwdPayload(FsmMinerPayload.MODELELEMENT);
				} else if (attribute.startsWith("Originator, backward")) {
					value = payload.getBwdPayload(FsmMinerPayload.ORIGINATOR);
				} else if (attribute.startsWith("Originator, forward")) {
					value = payload.getFwdPayload(FsmMinerPayload.ORIGINATOR);
				} else if (attribute.startsWith("Event type, backward")) {
					value = payload.getBwdPayload(FsmMinerPayload.EVENTTYPE);
				} else if (attribute.startsWith("Event type, forward")) {
					value = payload.getFwdPayload(FsmMinerPayload.EVENTTYPE);
				} else if (attribute.startsWith("Attribute ")) {
					value = payload.getAttributePayload().get(
							attribute.replaceFirst("Attribute ", ""));
					if (value == null) {
						value = "<undefined>";
					}
				}
				// Get the index of this value, and export this index.
				int valueIndex = mapIndices.get(attribute).get(value);
				bw.write(prefix + String.valueOf(valueIndex));
				prefix = " ";
			}
			bw.write("\n");
		}
		bw.write("---\n");
		// Third, write the transitions
		for (Object object : fsm.getEdges().toArray()) {
			ModelGraphEdge edge = (ModelGraphEdge) object;
			FSMTransition transition = (FSMTransition) edge;
			bw.write(String.valueOf(stateIndices.get(edge.getSource())) + " "
					+ String.valueOf(stateIndices.get(edge.getDest())) + " \""
					+ transition.getCondition() + "\"\n");
		}
		// Last, close the export. We're done.
		bw.close();
	}
}
