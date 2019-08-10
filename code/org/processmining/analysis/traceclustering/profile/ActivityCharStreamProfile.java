/**
 * 
 */
package org.processmining.analysis.traceclustering.profile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.MainUI;

/**
 * @author R. P. Jagadeesh Chandra 'JC' Bose
 * 
 */
public class ActivityCharStreamProfile extends AbstractProfile {

	protected int noActivities;
	protected int encodingLength;

	protected HashSet<String> activitySet;
	HashMap<String, String> activityCharMap;
	HashMap<String, String> charActivityMap;

	protected static final String EOS = "$";
	protected String suffix = EOS;

	protected Vector<String> charStreams;

	/**
	 * @param log
	 *            The log file to be profiled
	 * @throws IOException
	 * @throws IndexOutOfBoundsException
	 */

	public ActivityCharStreamProfile(String name, String description,
			LogReader log) throws IndexOutOfBoundsException, IOException {
		super(name, description, log);
		buildProfile(log);
	}

	public ActivityCharStreamProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super("Activity Char Streams", "Compares process instances as streams",
				log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		activitySet = new HashSet<String>();
		for (int i = 0; i < log.numberOfInstances(); i++) {
			AuditTrailEntryList ateList = log.getInstance(i)
					.getAuditTrailEntryList();
			for (int a = 0; a < ateList.size(); a++) {
				activitySet.add(encodeActivity(ateList.get(a)));
			}
		}

		noActivities = activitySet.size();
		// System.out.println("No. Activities: "+noActivities);

		generateActivityCharMap();
		// printActivityCharMap();

		encodeLog(log);
		// printCharStreams();
	}

	public static String encodeActivity(AuditTrailEntry ate) {
		return ate.getElement() + "--" + ate.getType();
	}

	protected void generateActivityCharMap() {

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

		activityCharMap = new HashMap<String, String>();
		charActivityMap = new HashMap<String, String>();

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
		} else {
			System.out
					.println("More than 6760 activities; Can't handle this much");
		}
	}

	protected void encode(String[] strArray) {
		int currentActivityIndex = 0;
		String charEncoding;

		for (String activity : activitySet) {
			charEncoding = strArray[currentActivityIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentActivityIndex++;
		}
	}

	protected void encode(String[] strArray, String[] intArray) {
		int currentActivityIndex = 0;

		int firstCharIndex, secondCharIndex;
		String charEncoding;
		for (String activity : activitySet) {
			firstCharIndex = currentActivityIndex / intArray.length;
			secondCharIndex = currentActivityIndex % intArray.length;

			charEncoding = strArray[firstCharIndex] + intArray[secondCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				MainUI
						.getInstance()
						.showGlassDialog(
								"Wrong Encoding",
								"Something went wrong in encoding; Don't use Activity CharStreams further for Analysis");
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentActivityIndex++;
		}
	}

	protected void encode(String[] strArray1, String[] strArray2,
			String[] strArray3) {
		int currentActivityIndex = 0;

		int firstCharIndex, secondCharIndex, thirdCharIndex;
		String charEncoding;
		for (String activity : activitySet) {
			thirdCharIndex = currentActivityIndex % strArray3.length;
			secondCharIndex = (currentActivityIndex / strArray3.length)
					% strArray2.length;
			firstCharIndex = (currentActivityIndex / (strArray3.length * strArray2.length));
			// secondCharIndex = currentActivityIndex /
			// (strArray1.length*strArray2.length);
			// firstCharIndex = currentActivityIndex/strArray1.length;

			charEncoding = strArray1[firstCharIndex]
					+ strArray2[secondCharIndex] + strArray3[thirdCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				System.out
						.println("Something wrong with encoding: Already present charEncoding");
				MainUI
						.getInstance()
						.showGlassDialog(
								"Wrong Encoding",
								"Something went wrong in encoding; Don't use Activity CharStreams further for Analysis");
				// System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentActivityIndex++;
		}
	}

	protected void printActivityCharMap() {
		System.out.println("Encoding Length: " + encodingLength);
		for (String activity : activitySet) {
			System.out
					.println(activity + " @ " + activityCharMap.get(activity));
		}
	}

	protected void encodeLog(LogReader log) throws IOException {
		String currentCharStream;

		charStreams = new Vector<String>();

		int noProcessInstances = log.numberOfInstances();
		for (int i = 0; i < noProcessInstances; i++) {
			currentCharStream = "";
			AuditTrailEntryList ateList = log.getInstance(i)
					.getAuditTrailEntryList();
			for (int a = 0; a < ateList.size(); a++) {
				currentCharStream += activityCharMap.get(encodeActivity(ateList
						.get(a)));
			}
			charStreams.add(currentCharStream);
		}
	}

	protected void printCharStreams() {
		System.out.println("No. Process Instances: " + charStreams.size());
		for (String currentCharStream : charStreams)
			System.out.println(currentCharStream);
	}

	public int getEncodingLength() {
		return encodingLength;
	}

	public Vector<String> getCharStreams() {
		return charStreams;
	}

	public HashMap<String, String> getActivityCharMap() {
		return activityCharMap;
	}
}
