package org.processmining.framework.util;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * This class processes a process instance encoded as a charstream for the
 * presence of loops and replaces the loop construct with just one occurrence
 * (iteration) over the loop. The multi-pass strategy iterates over the
 * reduction process until no further loop is identified. This processing caters
 * only to simple loops and loops within loops. Complex constructs such as
 * parallelism/choice within loops need to be dealt differently
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */

public class MultiPassExactTandemRepeats {
	boolean[] globalFlagCharStream;
	String charStream;
	String modifiedStream;
	int encodingLength;

	public MultiPassExactTandemRepeats(String charStream, int encodingLength) {
		this.charStream = charStream;
		modifiedStream = charStream;
		this.encodingLength = encodingLength;

		int charStreamLength = charStream.length() / encodingLength;
		globalFlagCharStream = new boolean[charStreamLength];
		for (int i = 0; i < charStreamLength; i++)
			globalFlagCharStream[i] = true;

		UkkonenSuffixTree st;
		boolean tandemRepeatExists = true;
		boolean[] modifiedFlag;
		String currentStream = charStream;
		TreeSet<String> currentStreamTandemRepeats;
		do {
			st = new UkkonenSuffixTree(currentStream, encodingLength);
			st.LZDecomposition();
			currentStreamTandemRepeats = st.getRepeatTypes();
			if (currentStreamTandemRepeats.size() > 0) {
				modifiedFlag = processCharStreamWithTandemRepeats(
						currentStream, currentStreamTandemRepeats);
				modifiedStream = reconstructModifiedStream(charStream,
						modifiedFlag);
				currentStream = modifiedStream;
			} else {
				tandemRepeatExists = false;
			}
			st = null;
			currentStreamTandemRepeats = null;
		} while (tandemRepeatExists);
	}

	protected boolean[] processCharStreamWithTandemRepeats(
			String currentStream, TreeSet<String> tandemRepeatSet) {
		int currentCharStreamLength = currentStream.length() / encodingLength;
		boolean[] flag = new boolean[currentCharStreamLength];

		for (int i = 0; i < currentCharStreamLength; i++) {
			flag[i] = true;
		}

		HashMap<String, String> repeatPRMap = findRepeatPrimitiveRepeatMap(tandemRepeatSet);
		HashMap<String, TreeSet<String>> startAlphabetTandemRepeatMap = mapTandemRepeatsStartingAlphabet(tandemRepeatSet);

		String currentAlphabet, primitiveRepeat;
		int primtiveRepeatLength, trStartAlphabetLength;
		boolean found;
		String modifiedCharStream = "";
		for (int i = 0; i < currentCharStreamLength; i++) {
			currentAlphabet = currentStream.substring(i * encodingLength,
					(i + 1) * encodingLength);

			if (startAlphabetTandemRepeatMap.containsKey(currentAlphabet)) {
				found = false;
				for (String trStartAlphabet : startAlphabetTandemRepeatMap
						.get(currentAlphabet)) {
					if (currentStream.indexOf(trStartAlphabet, i
							* encodingLength) == i * encodingLength) {
						primitiveRepeat = repeatPRMap.get(trStartAlphabet);
						primtiveRepeatLength = primitiveRepeat.length()
								/ encodingLength;
						trStartAlphabetLength = trStartAlphabet.length()
								/ encodingLength;

						modifiedCharStream += primitiveRepeat;
						for (int j = i + primtiveRepeatLength; j < i
								+ trStartAlphabetLength; j++) {
							flag[j] = false;
						}
						i = i + trStartAlphabetLength - 1;
						found = true;
						break;
					}
				}
				if (!found) {
					modifiedCharStream += currentAlphabet;
				}
			} else {
				modifiedCharStream += currentAlphabet;
			}
		}

		return flag;
	}

	protected HashMap<String, String> findRepeatPrimitiveRepeatMap(
			TreeSet<String> tandemRepeatSet) {
		HashMap<String, String> trPrimitiveRepeatMap = new HashMap<String, String>();
		String currentTandemRepeatType;
		TreeSet<String> currentTandemRepeatAlphabet;
		int currentTandemRepeatTypeLength;

		for (String currentTandemRepeat : tandemRepeatSet) {
			currentTandemRepeatAlphabet = new TreeSet<String>();
			currentTandemRepeatTypeLength = currentTandemRepeat.length()
					/ encodingLength;
			currentTandemRepeatTypeLength /= 2; // To consider only one
			// occurrence of the repeat in
			// the pair
			currentTandemRepeatType = currentTandemRepeat.substring(0,
					currentTandemRepeatTypeLength * encodingLength);

			for (int i = 0; i < currentTandemRepeatTypeLength; i++) {
				currentTandemRepeatAlphabet
						.add(currentTandemRepeatType.substring(i
								* encodingLength, (i + 1) * encodingLength));
			}

			if (currentTandemRepeatAlphabet.size() == 1) {
				trPrimitiveRepeatMap.put(currentTandemRepeat,
						currentTandemRepeatType.substring(0, encodingLength));
			} else if (currentTandemRepeatTypeLength
					/ currentTandemRepeatAlphabet.size() < 2) {
				// If the tandem repeat \alpha's length (in \alpha\alpha) is not
				// more than twice the repaet alphabet size; then no reduction
				// is possible
				trPrimitiveRepeatMap.put(currentTandemRepeat,
						currentTandemRepeatType);
			} else {
				// Check if this tandem repeat is a tandem repeat in itself
				String pr = findPrimitiveRepeat(currentTandemRepeatType);
				trPrimitiveRepeatMap.put(currentTandemRepeat, pr);
			}
		}

		return trPrimitiveRepeatMap;
	}

	protected String findPrimitiveRepeat(String tandemRepeatType) {
		UkkonenSuffixTree st = new UkkonenSuffixTree(tandemRepeatType,
				encodingLength);
		st.LZDecomposition();
		TreeSet<String> subTandemRepeats = st.getRepeatTypes();
		// Check if there is one or more than one tandem repeats within this
		// type
		if (subTandemRepeats.size() == 0) {
			// this repeat type is not a tandem repeat; so return the repeat
			// type as is
			return tandemRepeatType;
		} else if (subTandemRepeats.size() == 1) {
			// check if the subtandemRepeat identified is the same as the
			// tandemRepeatType;
			for (String newTandemRepeat : subTandemRepeats) {
				int newTandemRepeatLength = newTandemRepeat.length()
						/ encodingLength;
				newTandemRepeatLength /= 2;
				if (newTandemRepeat.equals(tandemRepeatType)) {
					return findPrimitiveRepeat(newTandemRepeat.substring(0,
							newTandemRepeatLength * encodingLength));
				} else {
					return tandemRepeatType;
				}
			}
		} else {
			for (String newTandemRepeat : subTandemRepeats) {
				int newTandemRepeatLength = newTandemRepeat.length()
						/ encodingLength;
				newTandemRepeatLength /= 2;
				if (newTandemRepeatLength > 1
						&& tandemRepeatType.indexOf(newTandemRepeat) == 0) {
					String pr = findPrimitiveRepeat(newTandemRepeat.substring(
							0, newTandemRepeatLength * encodingLength));
					if (tandemRepeatType.equals(newTandemRepeat + pr)) {
						return pr;
					}
				} else {
					return tandemRepeatType;
				}
			}
		}
		return "";
	}

	protected HashMap<String, TreeSet<String>> mapTandemRepeatsStartingAlphabet(
			TreeSet<String> tandemRepeatSet) {
		HashMap<String, TreeSet<String>> startAlphabetTandemRepeatMap = new HashMap<String, TreeSet<String>>();

		String currentTandemRepeatStartAlphabet;
		TreeSet<String> tempSet;
		for (String currentTandemRepeat : tandemRepeatSet) {
			currentTandemRepeatStartAlphabet = currentTandemRepeat.substring(0,
					encodingLength);
			if (startAlphabetTandemRepeatMap
					.containsKey(currentTandemRepeatStartAlphabet)) {
				tempSet = startAlphabetTandemRepeatMap
						.get(currentTandemRepeatStartAlphabet);
			} else {
				tempSet = new TreeSet<String>(new DescStrComparator());
			}
			tempSet.add(currentTandemRepeat);
			startAlphabetTandemRepeatMap.put(currentTandemRepeatStartAlphabet,
					tempSet);
		}

		return startAlphabetTandemRepeatMap;
	}

	protected String reconstructModifiedStream(String currentStream,
			boolean[] modifiedFlag) {
		String modifiedStream = "";
		int globalFlagLength, modifiedFlagLength, globalTrueIndex = 0;

		globalFlagLength = globalFlagCharStream.length;
		modifiedFlagLength = modifiedFlag.length;

		if (globalFlagLength != modifiedFlagLength) {
			for (int i = 0; i < modifiedFlagLength; i++) {
				if (modifiedFlag[i]) {
					modifiedStream += getSymbol(i, currentStream);
				}
			}
			// Modify global flag
			int j = 0;
			for (int i = 0; i < modifiedFlagLength; i++) {
				if (!modifiedFlag[i]) {
					for (; j < globalFlagLength; j++) {
						if (globalFlagCharStream[j]) {
							if (globalTrueIndex == i) {
								globalFlagCharStream[j] = false;
								globalTrueIndex++;
								break;
							}
							globalTrueIndex++;
						}
					}
				}
			}
		} else {
			for (int i = 0; i < modifiedFlagLength; i++) {
				if (modifiedFlag[i]) {
					modifiedStream += currentStream.substring(i
							* encodingLength, (i + 1) * encodingLength);
				} else {
					globalFlagCharStream[i] = false;
				}
			}
		}

		return modifiedStream;
	}

	protected String getSymbol(int trueIndex, String currentCharStream) {
		int globalTrueIndex = 0;
		for (int i = 0; i < globalFlagCharStream.length; i++) {
			if (globalFlagCharStream[i]) {
				if (globalTrueIndex == trueIndex)
					return currentCharStream.substring(i * encodingLength,
							(i + 1) * encodingLength);
				else
					globalTrueIndex++;
			}
		}
		return "";
	}

	public boolean[] getGlobalFlagCharStream() {
		return globalFlagCharStream;
	}

	public String getCharStream() {
		return charStream;
	}

	public String getModifiedStream() {
		return modifiedStream;
	}

	public int getEncodingLength() {
		return encodingLength;
	}

}
