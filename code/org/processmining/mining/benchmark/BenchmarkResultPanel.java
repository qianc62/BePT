/**
 * Project: ProM
 * File: BenchmarkResultPanel.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 18, 2006, 6:40:13 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.mining.benchmark;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BenchmarkResultPanel extends JPanel implements MiningResult {

	protected LogReader reader = null;
	protected ArrayList<Long> timings = null;
	protected long atesRead = 0;

	public BenchmarkResultPanel(LogReader logReader, ArrayList<Long> itTimings,
			long readAtes) {
		reader = logReader;
		timings = itTimings;
		atesRead = readAtes;
		ArrayList<String> timingStrings = new ArrayList<String>();
		// calculate statistics
		long max = Long.MIN_VALUE;
		long min = Long.MAX_VALUE;
		long mean = 0;
		int i = 0;
		for (long val : timings) {
			if (val > max) {
				max = val;
			}
			if (val < min) {
				min = val;
			}
			mean += val;
			timingStrings.add("Complete log iteration #" + i + ": "
					+ convertMsToString(val));
			i++;
		}
		mean /= timings.size();
		// set up panel
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		this.add(packInPanel("Number of read audit trail entries: ", atesRead
				+ " (LogSummary: "
				+ reader.getLogSummary().getNumberOfAuditTrailEntries() + ")"));
		this.add(packInPanel("Mean time", convertMsToString(mean)));
		this.add(packInPanel("Maximal time", convertMsToString(max)));
		this.add(packInPanel("Minimal time", convertMsToString(min)));
		this.add(new JScrollPane(new JList(timingStrings.toArray())));
	}

	protected String convertMsToString(long millis) {
		String result = millis + " ms (";
		int hours = (int) (millis / 3600000);
		if (hours > 0) {
			result = hours + " hours, ";
		}
		millis %= 360000;
		int minutes = (int) (millis / 60000);
		if (minutes > 0) {
			result = result + minutes + " minuts, ";
		}
		millis %= 60000;
		int seconds = (int) (millis / 1000);
		if (seconds > 0) {
			result = result + seconds + " seconds, ";
		}
		millis %= 1000;
		result = result + "and " + millis + " milliseconds)";
		return result;
	}

	protected JPanel packInPanel(String label, String value) {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
		result.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		result.add(new JLabel(label + ":"));
		result.add(new JLabel(value));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return this;
	}

}
