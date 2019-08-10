/**
 * Project: ProM
 * File: LogReaderBenchmark.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 18, 2006, 5:53:55 PM
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Progress;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LogReaderBenchmark implements MiningPlugin {

	protected BenchmarkPanel panel = null;
	protected Random rnd = null;
	protected ArrayList<Long> timings = null;
	protected long readAtes = 0;

	public LogReaderBenchmark() {
		panel = new BenchmarkPanel();
		rnd = new Random();
		timings = new ArrayList<Long>();
		readAtes = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		readAtes = 0;
		AuditTrailEntryList ateList = null;
		int iterations = log.numberOfInstances() * panel.getNumberOfRuns();
		int iteration = 0;
		Progress progress = new Progress("Reading process instances..", 0,
				iterations);
		try {
			for (int i = 0; i < panel.getNumberOfRuns(); i++) {
				long timing = System.currentTimeMillis();
				log.reset();
				for (int j = 0; j < log.numberOfInstances(); j++) {
					iteration++;
					progress.setProgress(iteration);
					if (panel.isRandomReading()) {
						ateList = log.getInstance(
								rnd.nextInt(log.numberOfInstances()))
								.getAuditTrailEntryList();
					} else {
						ateList = log.next().getAuditTrailEntryList();
					}
					readInstance(ateList);
				}
				timings.add(System.currentTimeMillis() - timing);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BenchmarkResultPanel(log, timings, readAtes);
	}

	protected void readInstance(AuditTrailEntryList list)
			throws IndexOutOfBoundsException, IOException {
		int index = 0;
		int size = list.size();
		AuditTrailEntry ate = null;
		if (panel.isRandomReading()) {
			for (int i = 0; i < size; i++) {
				ate = list.get(index);
				ate.getElement();
				if (panel.getModifyLog() == true) {
					ate.setElement(ate.getElement() + "_modified");
					list.replace(ate, index);
				}
				readAtes++;
				index = calculateRandomIndex(index, size);
			}
		} else {
			for (int i = 0; i < size; i++) {
				ate = list.get(i);
				ate.getElement();
				if (panel.getModifyLog() == true) {
					ate.setElement(ate.getElement() + "_modified");
					list.replace(ate, i);
				}
				readAtes++;
			}
		}
	}

	protected int calculateRandomIndex(int currentIndex, int listSize) {
		double probe = rnd.nextDouble();
		if (probe < panel.getRandomJumpProbability()) {
			// return random index
			if ((listSize - currentIndex) < panel.getMaximalRandomJump()) {
				return (currentIndex + rnd.nextInt(listSize - currentIndex));
			} else {
				return (currentIndex + rnd
						.nextInt(panel.getMaximalRandomJump()));
			}
		} else {
			// return next index, or 0 if end of list reached
			if (currentIndex < (listSize - 1)) {
				return (currentIndex + 1);
			} else {
				return 0;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Logreader Benchmark";
	}

}
