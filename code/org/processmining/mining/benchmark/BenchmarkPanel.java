/**
 * Project: ProM
 * File: BenchmarkPanel.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 18, 2006, 6:00:20 PM
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyDouble;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BenchmarkPanel extends JPanel {

	protected GUIPropertyInteger numberOfRuns = null;
	protected GUIPropertyBoolean randomReading = null;
	protected GUIPropertyDouble randomJumpProbability = null;
	protected GUIPropertyInteger maximalRandomJump = null;
	protected GUIPropertyBoolean modifyLog = null;

	public BenchmarkPanel() {
		numberOfRuns = new GUIPropertyInteger("Number of log iterations",
				"The log will be iterated over the specified number of times.",
				5, 1, 1000);
		randomReading = new GUIPropertyBoolean(
				"Read the log in a random fashion",
				"If this option is checked, the log will be read in a random access fashion.",
				false);
		randomJumpProbability = new GUIPropertyDouble(
				"Random jump probability",
				"Probability, that the next read audit trail entry will be resolved in a random fashion",
				1.0, 0.0, 1.0, 0.01);
		maximalRandomJump = new GUIPropertyInteger(
				"Random jump limit",
				"Maximal offset of a randomly read audit trail entry from the last read entry.",
				50, 1, 1000);
		modifyLog = new GUIPropertyBoolean("Modify log",
				"Modify audit trail entries in the log", true);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		this.add(numberOfRuns.getPropertyPanel());
		this.add(randomReading.getPropertyPanel());
		this.add(randomJumpProbability.getPropertyPanel());
		this.add(maximalRandomJump.getPropertyPanel());
		this.add(modifyLog.getPropertyPanel());
	}

	public int getNumberOfRuns() {
		return numberOfRuns.getValue();
	}

	public boolean isRandomReading() {
		return randomReading.getValue();
	}

	public double getRandomJumpProbability() {
		return randomJumpProbability.getValue();
	}

	public int getMaximalRandomJump() {
		return maximalRandomJump.getValue();
	}

	public boolean getModifyLog() {
		return modifyLog.getValue();
	}
}
