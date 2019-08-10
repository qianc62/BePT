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

package org.processmining.importing.heuristicsnet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;

/**
 * <p>
 * Title: Duplicate-task individual from file
 * </p>
 * <p>
 * Description: This class loads an duplicate-task individual from a file.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class HeuristicsNetFromFile {

	private LogEvents logEvents;
	private HeuristicsNet net;

	public HeuristicsNetFromFile(InputStream input) throws IOException {

		logEvents = new LogEvents();
		readIndividualFromFile(input);

	}

	public HeuristicsNet getNet() {
		return net;
	}

	private void readIndividualFromFile(InputStream input) throws IOException {
		BufferedReader reader = null;
		String fileLine = null;
		reader = new BufferedReader(new InputStreamReader(input));
		HNSubSet startTasks = new HNSubSet();
		HNSubSet endTasks = new HNSubSet();

		// reading start tasks...
		while ((fileLine = reader.readLine()) != null) {
			if (fileLine.trim().length() > 0
					&& !fileLine.equals(HeuristicsNet.FIELD_SEPARATOR)) {
				StringTokenizer st = new StringTokenizer(fileLine,
						HeuristicsNet.SETS_SEPARATOR);
				while (st.hasMoreElements()) {
					startTasks.add(Integer.parseInt(st.nextToken()));
				}
				break;
			}
		}

		// reading end task...
		while ((fileLine = reader.readLine()) != null) {
			if (fileLine.trim().length() > 0
					&& !fileLine.equals(HeuristicsNet.FIELD_SEPARATOR)) {
				StringTokenizer st = new StringTokenizer(fileLine,
						HeuristicsNet.SETS_SEPARATOR);
				while (st.hasMoreElements()) {
					endTasks.add(Integer.parseInt(st.nextToken()));
				}
				break;
			}
		}

		// reading the reverseDuplicatesMapping
		// advancing the file pointer to next non-empty line...
		int[] duplicatesMapping = new int[0];
		HNSubSet[] reverseDuplicatesMapping = new HNSubSet[0];

		while ((fileLine = reader.readLine()) != null) {
			if (fileLine.trim().length() > 0
					&& !fileLine.equals(HeuristicsNet.FIELD_SEPARATOR)) {
				Vector reverseDuplicatesMappingLines = new Vector();
				do {
					if (fileLine.trim().length() > 0) {
						reverseDuplicatesMappingLines.add(fileLine);
					}
					fileLine = reader.readLine();
				} while ((fileLine != null)
						&& (!fileLine.trim().equals(
								HeuristicsNet.FIELD_SEPARATOR)));
				reverseDuplicatesMapping = loadReverseDuplicatesMapping(reverseDuplicatesMappingLines);
				duplicatesMapping = buildDuplicatesMapping(reverseDuplicatesMapping);
				reverseDuplicatesMappingLines = null;
				break;
			}
		}

		// now the cursor is already at the correct position

		HNSet[] inputSets = new HNSet[duplicatesMapping.length];
		HNSet[] outputSets = new HNSet[duplicatesMapping.length];

		if (inputSets.length > 0) {
			do {
				if ((fileLine.trim().length() > 0)
						&& (!fileLine.trim().equals(
								HeuristicsNet.FIELD_SEPARATOR))) {
					loadSets(fileLine, inputSets, outputSets);
				}
				fileLine = reader.readLine();
			} while ((fileLine != null));
		}

		createDTHeuristicsNet(inputSets, outputSets, startTasks, endTasks,
				duplicatesMapping, reverseDuplicatesMapping);

	}

	private int[] buildDuplicatesMapping(HNSubSet[] reverseDuplicateMapping) {

		int[] duplicatesMapping = null;

		// counting the size of the duplicates mapping
		int size = 0;
		for (int i = 0; i < reverseDuplicateMapping.length; i++) {
			size += reverseDuplicateMapping[i].size();
		}

		duplicatesMapping = new int[size];

		// now, filling in the duplicates mapping
		for (int i = 0; i < reverseDuplicateMapping.length; i++) {
			for (int j = 0; j < reverseDuplicateMapping[i].size(); j++) {
				duplicatesMapping[reverseDuplicateMapping[i].get(j)] = i;
			}
		}
		return duplicatesMapping;
	}

	private HNSubSet[] loadReverseDuplicatesMapping(Vector inputLines) {
		HNSubSet[] reverseDuplicatesMapping = new HNSubSet[inputLines.size()];

		for (int i = 0; i < inputLines.size(); i++) {
			String line = (String) inputLines.get(i);

			// splitting the line
			String wmeEvent = line.substring(0, line
					.indexOf(HeuristicsNet.SETS_SEPARATOR));
			String duplicateCodes = line.substring(line
					.indexOf(HeuristicsNet.SETS_SEPARATOR)
					+ HeuristicsNet.SETS_SEPARATOR.length(), line.length());
			int code = mapWmeToCode(wmeEvent);
			insertIntoReverDuplicatesMapping(reverseDuplicatesMapping, code,
					duplicateCodes);

		}

		return reverseDuplicatesMapping;

	}

	private void insertIntoReverDuplicatesMapping(
			HNSubSet[] reverseDuplicatesMapping, int code, String duplicates) {

		int lastIndex = 0;
		int index = duplicates.indexOf(HeuristicsNet.AND_SEPARATOR, lastIndex);
		HNSubSet duplicatesSet = new HNSubSet();

		while ((index != -1)) {
			duplicatesSet.add(Integer.parseInt(duplicates.substring(lastIndex,
					index)));
			lastIndex = index + HeuristicsNet.AND_SEPARATOR.length();
			index = duplicates.indexOf(HeuristicsNet.AND_SEPARATOR, lastIndex);
		}
		reverseDuplicatesMapping[code] = duplicatesSet;
	}

	private void insertInLogEvents(String name, String type) {
		LogEvent le = null;
		if (logEvents.findLogEvent(name, type) == null) {
			le = new LogEvent(name, type);
			logEvents.add(le);
		}
	}

	private int mapWmeToCode(String wme) {
		String element = null;
		String wmeName = null;
		String wmeType = null;

		element = wme.trim();

		// testing if it is an unknown event type
		String unknownTag = HeuristicsNet.EVENT_SEPARATOR + "unknown"
				+ HeuristicsNet.EVENT_SEPARATOR;
		if (element.indexOf(unknownTag) >= 0) {
			// Note: I've used "lastIndexOf" because the original name of
			// the ate may contain ":unknown:" as well!
			wmeName = element.substring(0, element.lastIndexOf(unknownTag));
			wmeType = element.substring(element.lastIndexOf(unknownTag)
					+ HeuristicsNet.EVENT_SEPARATOR.length(), element.length());
		} else {
			// Note: I've used "lastIndexOf" because the original name of
			// the ate may contain "EVENT_SEPARATOR" as well!
			wmeName = element.substring(0, element
					.lastIndexOf(HeuristicsNet.EVENT_SEPARATOR));
			wmeType = element.substring(element
					.lastIndexOf(HeuristicsNet.EVENT_SEPARATOR)
					+ HeuristicsNet.EVENT_SEPARATOR.length(), element.length());
		}

		insertInLogEvents(wmeName, wmeType);

		return logEvents.findLogEventNumber(wmeName, wmeType);
	}

	private void loadSets(String fileLine, HNSet[] inputSets, HNSet[] outputSets) {

		String substring = null;
		int wmeCode = -1;
		HNSet input = null;
		HNSet output = null;
		int index = 0;
		int nextIndex = 0;

		// retrieving the workflow model element part
		index = 0;
		nextIndex = fileLine.indexOf(HeuristicsNet.SETS_SEPARATOR);
		substring = fileLine.substring(index, nextIndex);
		wmeCode = Integer.parseInt(substring);

		// retrieving the input set part
		index = nextIndex + HeuristicsNet.SETS_SEPARATOR.length();
		nextIndex = fileLine.indexOf(HeuristicsNet.SETS_SEPARATOR, index);
		substring = fileLine.substring(index, nextIndex);
		input = extractHNSet(substring);
		inputSets[wmeCode] = input;

		// retrieving the output set part
		index = nextIndex + HeuristicsNet.SETS_SEPARATOR.length();
		substring = fileLine.substring(index);
		output = extractHNSet(substring);
		outputSets[wmeCode] = output;

	}

	private void createDTHeuristicsNet(HNSet[] inputSets, HNSet[] outputSets,
			HNSubSet startTasks, HNSubSet endTasks, int[] duplicatesMapping,
			HNSubSet[] reverseDuplicatesMapping) {

		net = new HeuristicsNet(logEvents, duplicatesMapping,
				reverseDuplicatesMapping);
		net.setStartTasks(startTasks);
		net.setEndTasks(endTasks);

		for (int i = 0; i < inputSets.length; i++) {
			net.setInputSet(i, inputSets[i]);
			net.setOutputSet(i, outputSets[i]);
		}
	}

	private HNSet extractHNSet(String line) {
		HNSet set = new HNSet();

		line = line.trim();
		if (!line.equals(HeuristicsNet.EMPTY_SET)) {
			// extracting subsets...
			StringTokenizer andSets = new StringTokenizer(line,
					HeuristicsNet.AND_SEPARATOR);
			while (andSets.hasMoreTokens()) {
				// extraction subsets elements....
				StringTokenizer orSets = new StringTokenizer(andSets
						.nextToken().trim(), HeuristicsNet.OR_SEPARATOR);
				HNSubSet subset = new HNSubSet();
				while (orSets.hasMoreTokens()) {
					subset.add(Integer.parseInt(orSets.nextToken()));
				}
				set.add(subset);
			}

		}

		return set;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		HeuristicsNetFromFile obj = new HeuristicsNetFromFile(
				new FileInputStream(args[0]));
		HeuristicsNetFromFile obj1 = new HeuristicsNetFromFile(
				new FileInputStream(args[1]));
		// System.out.println(obj.getNet().toString());
		// System.out.println("-----------------------------");
		// System.out.println(obj.getNet().toStringWithEvents());
		System.out.println("-----------------------------");
		System.out.println(obj.getNet().toStringWithEvents());

		System.out.println("-----------------------------");
		System.out.println(obj1.getNet().toStringWithEvents());

	}
}
