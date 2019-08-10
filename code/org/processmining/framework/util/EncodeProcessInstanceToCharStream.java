package org.processmining.framework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.MainUI;

/**
 * This class encodes a process instance into a charstream. The charstream
 * encoding is fundamental to many of the mining/analysis/filter plugins.
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */
public class EncodeProcessInstanceToCharStream {
	int encodingLength; // The length of encoding of an activity
	String charStream; // The encoded charStream of the process instance

	/**
	 * The constructor takes a process instance and identifies the encoding
	 * length based on the number of activities; It later encodes the process
	 * instance as a charStream
	 * 
	 * Sets the encoding length for this process instance and stores the
	 * charstream encoding in the class variables
	 */
	public EncodeProcessInstanceToCharStream(ProcessInstance instance) {
		AuditTrailEntryList entries = instance.getAuditTrailEntryList();

		HashSet<String> activitySet = new HashSet<String>();

		HashMap<String, String> activityCharMap = new HashMap<String, String>();
		String strArray[] = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
				"k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
				"w", "x", "y", "z" };
		String intArray[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

		Iterator<AuditTrailEntry> it = entries.iterator();
		AuditTrailEntry currentAuditTrailEntry;
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			activitySet.add(currentAuditTrailEntry.getElement());
		}
		// System.out.println(activitySet.size());

		int noActivities = activitySet.size();

		encodingLength = 1;

		if (noActivities > strArray.length
				&& noActivities < strArray.length * intArray.length + 1) {
			encodingLength = 2;
		} else if (noActivities >= strArray.length * intArray.length + 1) {
			MainUI
					.getInstance()
					.showGlassDialog(
							"Too Many Activities",
							"More than "
									+ (strArray.length * intArray.length + 1)
									+ " Activities in this log; Encoding not possible at this moment");
		}

		Iterator<String> activityIterator = activitySet.iterator();
		String currentActivity;
		int strIndex, intIndex, currentActivityIndex = 0;
		while (activityIterator.hasNext()) {
			currentActivity = activityIterator.next();

			strIndex = currentActivityIndex % strArray.length;
			intIndex = currentActivityIndex / strArray.length;

			if (encodingLength == 1) {
				activityCharMap.put(currentActivity, strArray[strIndex]);
			} else {
				activityCharMap.put(currentActivity, strArray[strIndex]
						+ intArray[intIndex]);
			}

			currentActivityIndex++;
		}

		// Convert to CharStream
		charStream = "";
		it = entries.iterator();
		while (it.hasNext()) {
			currentAuditTrailEntry = it.next();
			currentActivity = currentAuditTrailEntry.getElement();
			charStream += activityCharMap.get(currentActivity);
		}
	}

	public int getEncodingLength() {
		return encodingLength;
	}

	public String getCharStream() {
		return charStream;
	}
}
