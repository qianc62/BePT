package org.processmining.analysis.traceclustering.profile;

import java.io.IOException;
import java.util.HashSet;

import org.processmining.analysis.traceclustering.charstreams.UkkonenSuffixTree;
import org.processmining.framework.log.LogReader;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class ActivityPatternsProfile extends ActivityCharStreamProfile {

	public ActivityPatternsProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super("Activity Patterns Profile",
				"Compares patterns in process instances", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super.buildProfile(log);
		int noCharStreams = charStreams.size();
		for (int i = 1; i < encodingLength; i++)
			suffix += EOS;

		String currentCharStream;

		currentCharStream = charStreams.get(0);
		for (int i = 1; i < noCharStreams; i++)
			currentCharStream += suffix + charStreams.get(i);

		UkkonenSuffixTree suffixTree;
		suffixTree = new UkkonenSuffixTree(currentCharStream, encodingLength);
		suffixTree.findLeftDiverseNodes();

		HashSet<String> maximalRepeats;
		HashSet<String> repeatSet;

		maximalRepeats = suffixTree.getMaximalRepeats();
		repeatSet = new HashSet<String>();

		suffixTree = null;
		currentCharStream = null;

		String splitPattern;
		String[] currentRepeatSplit;

		splitPattern = "";
		for (int i = 0; i < encodingLength; i++)
			splitPattern += "\\" + EOS;

		for (String currentRepeat : maximalRepeats) {
			currentRepeatSplit = currentRepeat.split(splitPattern);
			for (int i = 0; i < currentRepeatSplit.length; i++)
				if (currentRepeatSplit[i].length() > 0) {
					repeatSet.add(currentRepeatSplit[i]);
				}
		}

		// System.out.println(maximalRepeats);
		maximalRepeats = null;

		// System.out.println("RepeatSetSize: "+repeatSet.size());
		// System.out.println(repeatSet);

		int count, currentCharStreamLength;
		for (int i = 0; i < noCharStreams; i++) {
			currentCharStream = charStreams.get(i);
			currentCharStreamLength = currentCharStream.length()
					/ encodingLength;
			// currentStreamPatterns = new HashMap<String,Integer>();

			for (String currentRepeat : repeatSet) {
				if (currentCharStream.equals(currentRepeat)) {
					// currentStreamPatterns.put(currentRepeat, 1);
					incrementValue(i, currentRepeat,
							1.0 / currentCharStreamLength);
				} else if (currentCharStream.contains(currentRepeat)) {
					count = currentCharStream.split(currentRepeat).length > 1 ? currentCharStream
							.split(currentRepeat).length - 1
							: 1;
					// currentStreamPatterns.put(currentRepeat, count);
					incrementValue(i, currentRepeat, count
							/ currentCharStreamLength);
				}
			}
		}
		charStreams = null;
		repeatSet = null;
	}
}
