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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

/**
 * @author christian
 * 
 */
public class TraceReplay {

	public enum MatchType {
		REMOVED, INCLUSTER, INVALID, VALID;
	}

	protected FuzzyGraph graph;
	protected LogReader log;
	protected ProcessInstance instance;
	protected double coverage;
	protected MatchType[] matches;
	protected int countRemoved = 0;
	protected int countIncluster = 0;
	protected int countIncorrect = 0;
	protected int countCorrect = 0;

	public TraceReplay(FuzzyGraph graph, LogReader log, int traceIndex)
			throws IndexOutOfBoundsException, IOException {
		this.graph = graph;
		this.log = log;
		this.instance = log.getInstance(traceIndex);
		this.coverage = 0.0;
		this.matches = new MatchType[this.instance.getAuditTrailEntryList()
				.size()];
		coverage = replayTrace();
	}

	public double getCoverage() {
		return coverage;
	}

	public MatchType[] getMatches() {
		return matches;
	}

	public MatchType getMatch(int index) {
		return matches[index];
	}

	public int size() {
		return instance.getAuditTrailEntryList().size();
	}

	public ProcessInstance getInstance() {
		return instance;
	}

	public double replayTrace() throws IndexOutOfBoundsException, IOException {
		// initialize data structures
		// calculate result
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		int size = ateList.size();
		int deviations = 0;
		countRemoved = 0;
		countIncluster = 0;
		countIncorrect = 0;
		countCorrect = 0;
		HashMap<Node, Set<Node>> followers = new HashMap<Node, Set<Node>>();
		HashMap<Node, Set<Node>> possibleFollowers = new HashMap<Node, Set<Node>>();
		for (int i = 0; i < graph.getNumberOfInitialNodes(); i++) {
			Node node = graph.getNodeMappedTo(i);
			if (node != null && followers.keySet().contains(node) == false) {
				followers.put(node, node.getSuccessors());
				possibleFollowers.put(node, new HashSet<Node>());
			}
		}
		LogEvents events = log.getLogSummary().getLogEvents();
		int initialIndex = 0;
		int lastIndex = 0;
		Node lastNode = null;
		AuditTrailEntry lastAte = null;
		// find first, valid event (mapped to valid node)
		while (lastNode == null) {
			lastAte = ateList.get(initialIndex);
			lastIndex = events.findLogEventNumber(lastAte.getElement(), lastAte
					.getType());
			lastNode = graph.getNodeMappedTo(lastIndex);
			if (lastNode != null) {
				matches[initialIndex] = MatchType.VALID;
			} else {
				matches[initialIndex] = MatchType.REMOVED;
				deviations++;
				countRemoved++;
			}
			initialIndex++;
			if (initialIndex >= size) {
				// no valid events in this trace, return
				return (double) (size - deviations + 1) / (double) (size + 1);
			}
		}
		// check first event whether it corresponds to a 'real' start node,
		// i.e. if there are no incoming arcs.
		if (lastNode.getPredecessors().size() > 0) {
			deviations++; // no real start node, punish.
			matches[initialIndex - 1] = MatchType.INVALID;
			countRemoved++;
		}
		// update node activation counter
		possibleFollowers.put(lastNode, new HashSet<Node>(followers
				.get(lastNode)));
		AuditTrailEntry currentAte = null;
		int currentIndex;
		Node currentNode = null;
		// replay
		for (int i = 1; i < ateList.size(); i++) {
			currentAte = ateList.get(i);
			currentIndex = events.findLogEventNumber(currentAte.getElement(),
					currentAte.getType());
			currentNode = graph.getNodeMappedTo(currentIndex);
			// check for valid node
			if (currentNode == null) {
				// node had been removed, punish
				deviations++;
				matches[i] = MatchType.REMOVED;
				countRemoved++;
				continue;
			}
			// analyze state transition
			else if (currentNode.equals(lastNode)) {
				if (currentNode instanceof ClusterNode) {
					// transition within one cluster, valid.
					// no need to change state
					matches[i] = MatchType.INCLUSTER;
					countIncluster++;
				} else {
					// the same node in succession, check if valid
					if (graph.getBinarySignificance(currentNode.getIndex(),
							currentNode.getIndex()) > 0.0) {
						// valid succession, graph has self-loop for node
						// no need to change state
						matches[i] = MatchType.VALID;
						countCorrect++;
					} else {
						// invalid succession, no self-loop for node
						deviations++;
						matches[i] = MatchType.INVALID;
						countIncorrect++;
					}
				}
			} else if (checkNode(currentNode, possibleFollowers) == false) {
				// invalid transition
				deviations++;
				matches[i] = MatchType.INVALID;
				countIncorrect++;
			} else {
				// valid transition;
				// update activation for current node
				possibleFollowers.put(currentNode, new HashSet<Node>(followers
						.get(currentNode)));
				matches[i] = MatchType.VALID;
				countCorrect++;
			}
			lastNode = currentNode;
			lastAte = currentAte;
			lastIndex = currentIndex;
		}
		// calculate result
		return (double) (size - deviations + 1) / (double) (size + 1);
	}

	protected boolean checkNode(Node current,
			HashMap<Node, Set<Node>> possibleFollowers) {
		boolean isValid = false;
		// check all active nodes if they are valid predecessors
		for (Node node : possibleFollowers.keySet()) {
			if (possibleFollowers.get(node).contains(current)) {
				// valid possible predecessor found, remove from set
				possibleFollowers.get(node).remove(current);
				isValid = true;
			}
		}
		return isValid;
	}
}
