package org.processmining.analysis.traceclustering.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.analysis.traceclustering.charstreams.UkkonenSuffixTree;
import org.processmining.framework.log.LogReader;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class ActivityPatternAlphabetsProfile extends ActivityCharStreamProfile {
	public ActivityPatternAlphabetsProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super(
				"Activity Pattern Alphabets Profile",
				"Compares patterns with flexible ordering in process instances",
				log);
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

		maximalRepeats = suffixTree.getMaximalRepeats();

		suffixTree = null;
		currentCharStream = null;

		String splitPattern;
		String[] currentRepeatSplit;
		int currentRepeatSplitLength;

		splitPattern = "";
		for (int i = 0; i < encodingLength; i++)
			splitPattern += "\\" + EOS;

		HashMap<String, HashSet<String>> patternPatternAlphabetMap = new HashMap<String, HashSet<String>>();
		HashSet<HashSet<String>> repeatAlphabetSet = new HashSet<HashSet<String>>();
		HashSet<String> currentRepeatAlphabet;

		for (String currentRepeat : maximalRepeats) {
			currentRepeatSplit = currentRepeat.split(splitPattern);
			for (int i = 0; i < currentRepeatSplit.length; i++) {
				currentRepeatSplitLength = currentRepeatSplit[i].length()
						/ encodingLength;
				if (currentRepeatSplitLength > 0) {
					currentRepeatAlphabet = new HashSet<String>();
					for (int j = 0; j < currentRepeatSplitLength; j++)
						currentRepeatAlphabet.add(currentRepeatSplit[i]
								.substring(j * encodingLength, (j + 1)
										* encodingLength));
					patternPatternAlphabetMap.put(currentRepeatSplit[i],
							currentRepeatAlphabet);
					repeatAlphabetSet.add(currentRepeatAlphabet);
				}
			}
		}

		maximalRepeats = null;
		repeatAlphabetSet = null;

		int count, currentCharStreamLength;
		for (int i = 0; i < noCharStreams; i++) {
			currentCharStream = charStreams.get(i);
			currentCharStreamLength = currentCharStream.length()
					/ encodingLength;

			for (String currentRepeat : patternPatternAlphabetMap.keySet()) {
				currentRepeatAlphabet = patternPatternAlphabetMap
						.get(currentRepeat);
				if (currentCharStream.equals(currentRepeat)) {
					incrementValue(i, currentRepeatAlphabet.toString(),
							1.0 / currentCharStreamLength);
				} else if (currentCharStream.contains(currentRepeat)) {
					count = currentCharStream.split(currentRepeat).length > 1 ? currentCharStream
							.split(currentRepeat).length - 1
							: 1;
					incrementValue(i, currentRepeatAlphabet.toString(), count
							/ currentCharStreamLength);
				}
			}
		}
		patternPatternAlphabetMap = null;
		charStreams = null;
	}
}
