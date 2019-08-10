package org.processmining.analysis.abstractions.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.analysis.abstractions.util.FileIO;
import org.processmining.framework.ui.MainUI;

public class ActivitySet {
	HashSet<String> activitySet;
	HashMap<String, String> activityCharMap;
	HashMap<String, String> charActivityMap;
	int encodingLength;

	public ActivitySet() {
		this.activitySet = new HashSet<String>();
		activityCharMap = new HashMap<String, String>();
		charActivityMap = new HashMap<String, String>();
	}

	public ActivitySet(HashSet<String> activitySet) {
		this.activitySet = activitySet;
		activityCharMap = new HashMap<String, String>();
		charActivityMap = new HashMap<String, String>();
	}

	public void readActivitySet(String inputDir, String fileName) {
		BufferedReader reader;
		String currentLine;

		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			activitySet = new HashSet<String>();
			while ((currentLine = reader.readLine()) != null) {
				activitySet.add(currentLine.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found Exception: " + inputDir + "\\"
					+ fileName);
			System.exit(0);
		} catch (IOException e) {
			System.err.println("IO Exception while reading: " + inputDir + "\\"
					+ fileName);
			System.exit(0);
		}

		// Free memory
		reader = null;
		currentLine = null;
	}

	public void readActivityCharMap(String inputDir, String fileName,
			String delimiter) {
		FileIO fileIO = new FileIO();
		this.activityCharMap = fileIO.readMapFromFile(inputDir, fileName,
				delimiter);

		// Free memory
		fileIO = null;
	}

	public void readCharActivityMap(String inputDir, String fileName,
			String delimiter) {
		FileIO fileIO = new FileIO();
		this.charActivityMap = fileIO.readMapFromFile(inputDir, fileName,
				delimiter);

		// Free memory
		fileIO = null;
	}

	public void encodeAbstractionSet() {
		String[] upperCaseArray = { "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
				"U", "V", "W", "X", "Y", "Z" };
		int noActivities = activitySet.size();

		encodingLength = 2;
		if (noActivities <= upperCaseArray.length) {
			encodingLength = 1;
			encode(upperCaseArray);
		} else if (noActivities <= upperCaseArray.length
				* upperCaseArray.length) {
			encode(upperCaseArray, upperCaseArray);
		}
	}

	public void encodeActivitySet() {
		String[] lowerCaseArray = { "a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
				"u", "v", "w", "x", "y", "z" };
		String[] upperCaseArray = { "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
				"U", "V", "W", "X", "Y", "Z" };
		String[] alphaArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i",
				"j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
				"v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G",
				"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
				"T", "U", "V", "W", "X", "Y", "Z" };
		String[] lowerCaseIntArray = { "a", "b", "c", "d", "e", "f", "g", "h",
				"i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
				"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
				"6", "7", "8", "9" };
		String[] intArray = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		String[] allArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
				"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
				"w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
				"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5",
				"6", "7", "8", "9" };
		int noActivities = activitySet.size();

		encodingLength = 2;
		if (noActivities <= lowerCaseArray.length) {
			encodingLength = 1;
			encode(lowerCaseArray);
		} else if (noActivities > lowerCaseArray.length
				&& noActivities <= lowerCaseArray.length * intArray.length) {
			encode(lowerCaseArray, intArray);
		} else if (noActivities > lowerCaseArray.length * intArray.length
				&& noActivities <= alphaArray.length * intArray.length) {
			encode(alphaArray, intArray);
		} else if (noActivities > alphaArray.length * intArray.length
				&& noActivities < lowerCaseIntArray.length
						* upperCaseArray.length) {
			encode(lowerCaseIntArray, upperCaseArray);
		} else if (noActivities <= lowerCaseArray.length
				* upperCaseArray.length * intArray.length) {
			encodingLength = 3;
			encode(lowerCaseArray, upperCaseArray, intArray);
		} else if (noActivities <= allArray.length * allArray.length
				* allArray.length) {
			encodingLength = 3;
			encode(allArray, allArray, allArray);
		} else {
			MainUI
					.getInstance()
					.showGlassDialog("",
							"More than  238000 Activities/Errors/Warnings; Can't handle this much");
			// System.exit(0);
		}

		// Free memory
		lowerCaseArray = null;
		upperCaseArray = null;
		alphaArray = null;
		lowerCaseIntArray = null;
		intArray = null;
	}

	protected void encode(String[] strArray) {
		int currentactivityIndex = 0;
		String charEncoding;

		for (String activity : activitySet) {
			charEncoding = strArray[currentactivityIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		// Free Memory
		charEncoding = null;
	}

	protected void encode(String[] strArray, String[] intArray) {
		int currentactivityIndex = 0;

		int firstCharIndex, secondCharIndex;
		String charEncoding;
		for (String activity : activitySet) {
			firstCharIndex = currentactivityIndex / intArray.length;
			secondCharIndex = currentactivityIndex % intArray.length;

			charEncoding = strArray[firstCharIndex] + intArray[secondCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		// Free Memory
		charEncoding = null;
	}

	protected void encode(String[] strArray1, String[] strArray2,
			String[] strArray3) {
		int currentactivityIndex = 0;

		int firstCharIndex, secondCharIndex, thirdCharIndex;
		String charEncoding;
		for (String activity : activitySet) {
			thirdCharIndex = currentactivityIndex % strArray3.length;
			secondCharIndex = (currentactivityIndex / strArray3.length)
					% (strArray2.length);
			firstCharIndex = currentactivityIndex
					/ (strArray3.length * strArray2.length);

			charEncoding = strArray1[firstCharIndex]
					+ strArray2[secondCharIndex] + strArray3[thirdCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		// Free Memory
		charEncoding = null;
	}

	public int getEncodingLength() {
		return encodingLength;
	}

	public HashSet<String> getActivitySet() {
		return activitySet;
	}

	public HashMap<String, String> getActivityCharMap() {
		return activityCharMap;
	}

	public HashMap<String, String> getCharActivityMap() {
		return charActivityMap;
	}

	/*
	 * public static void main(String[] args){ String dir =
	 * "D:\\JC\\PMS\\Data\\UPs\\Txt"; String charStreamDir = "charStreams";
	 * String abstractionSetFileName = "abstractionMaximalElements.txt"; String
	 * fileName = "ActivitySet.txt"; String delim = "@"; String
	 * charActivityMapFile = "charActivityMap.txt";
	 * 
	 * 
	 * Watch w = new Watch(); w.start();
	 * 
	 * ActivitySet a = new ActivitySet();
	 * a.readActivitySet(dir+"\\"+charStreamDir, abstractionSetFileName);
	 * a.encodeAbstractionSet(); // a.encodeActivitySet();
	 * System.out.println("Encoding Length: "+a.getEncodingLength());
	 * 
	 * FileIO fileIO = new FileIO(); fileIO.writeToFile(dir,
	 * "AbstractionCharMap.txt", a.getActivityCharMap(),delim);
	 * fileIO.writeToFile(dir, "CharAbstractionMap.txt",
	 * a.getCharActivityMap(),delim);
	 * 
	 * 
	 * 
	 * System.out.println("Took "+w.msecs()+" msecs.");
	 * 
	 * //Free Memory w = null; a = null; fileIO = null; }
	 */
}
