/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.mining.fuzzymining.replay;

import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;

public class FuzzyReplay {

	protected FuzzyGraph graph;
	protected LogReader log;
	protected ArrayList<TraceReplay> traceReplays;
	protected double value;
	protected ReplayListener listener;

	public FuzzyReplay(FuzzyGraph graph, LogReader log, ReplayListener listener)
			throws IndexOutOfBoundsException, IOException {
		this.graph = graph;
		this.log = log;
		this.listener = listener;
		replay(this.log);
	}

	public double getValue() {
		return value;
	}

	public int numberOfTraces() {
		return traceReplays.size();
	}

	public TraceReplay getTraceReplay(int traceIndex) {
		return traceReplays.get(traceIndex);
	}

	public void replay(LogReader reader) throws IndexOutOfBoundsException,
			IOException {
		listener.setProgress(0.0);
		LogReader log = this.log;
		if (reader != null) {
			log = reader;
		}
		// replay all instances
		double aggregated = 0.0;
		this.traceReplays = new ArrayList<TraceReplay>();
		for (int i = 0; i < log.numberOfInstances(); i++) {
			listener.setProgress((double) i / (double) log.numberOfInstances());
			TraceReplay replay = new TraceReplay(this.graph, log, i);
			aggregated += replay.getCoverage();
			traceReplays.add(replay);
		}
		// rectify aggregated value
		value = aggregated / traceReplays.size();
	}

}
