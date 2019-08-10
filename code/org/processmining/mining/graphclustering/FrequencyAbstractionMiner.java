/**
 * Project: ProM
 * File: FrequencyAbstractionMiner.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jun 30, 2006, 3:54:03 PM
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
package org.processmining.mining.graphclustering;

import java.io.IOException;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FrequencyAbstractionMiner implements MiningPlugin {

	protected LogEvents logEvents = null;
	protected DoubleMatrix2D follows = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		// extract and copy events (for faster indexing)
		logEvents = log.getLogSummary().getLogEvents();
		// build follower matrix
		follows = buildFollowerMatrix(log);
		return new FrequencyAbstractionResult(log, logEvents, follows);
	}

	protected DoubleMatrix2D buildFollowerMatrix(LogReader log) {
		// build follower matrix by traversing the log
		DoubleMatrix2D followsMatrix = DoubleFactory2D.dense.make(logEvents
				.size(), logEvents.size(), 0);
		AuditTrailEntryList atel = null;
		try {
			for (int j = 0; j < log.numberOfInstances(); j++) {
				atel = log.getInstance(j).getAuditTrailEntryList();
				AuditTrailEntry lastAte = atel.get(0);
				AuditTrailEntry currentAte = null;
				for (int k = 1; k < atel.size(); k++) {
					currentAte = atel.get(k);
					int fromIndex = logEvents.findLogEventNumber(lastAte
							.getElement(), lastAte.getType());
					int toIndex = logEvents.findLogEventNumber(currentAte
							.getElement(), currentAte.getType());
					followsMatrix.set(fromIndex, toIndex, followsMatrix.get(
							fromIndex, toIndex) + 1); // increase follower count
					lastAte = currentAte; // swap ATEs
				}
			}
		} catch (IOException e) {
			// oops...
			Message
					.add(
							"Error occurred during mining, check STDERR for stack trace!",
							Message.ERROR);
			e.printStackTrace();
		}
		return followsMatrix;
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
		return "Frequency abstraction miner";
	}

}
