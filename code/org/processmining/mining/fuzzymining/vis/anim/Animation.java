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
package org.processmining.mining.fuzzymining.vis.anim;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.deckfour.gantzgraf.layout.GGCurve;
import org.deckfour.gantzgraf.layout.GGDotParser;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.Edge;
import org.processmining.mining.fuzzymining.graph.Edges;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;
import org.processmining.mining.fuzzymining.vis.anim.TaskAnimation.Type;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Animation {

	protected LogReader log;
	protected FuzzyGraph graph;
	protected Set<Node> graphNodes;
	protected Edges graphEdges;
	protected GGDotParser parser;

	protected int maxLookahead = 5;
	protected int maxExtraLookahead = 2;

	protected HashMap<String, TaskAnimation> taskAnimations;
	protected HashMap<String, ArcAnimation> arcAnimations;
	protected ArrayList<TaskAnimation> primitiveTaskAnimationList;
	protected ArrayList<TaskAnimation> clusterTaskAnimationList;
	protected ArrayList<ArcAnimation> arcAnimationList;
	protected ArcAnimation caseAnimation;

	protected long startTime;
	protected long endTime;

	protected int minTraverseCount;
	protected int maxTraverseCount;
	protected int bestQuarterTraverseCount;
	protected long meanCaseDuration;

	protected long fadeTime;
	protected long meanBetweenTime;

	public static Animation generate(FuzzyGraph graph, LogReader log,
			int maxLookahead, int maxExtraLookahead, ProgressPanel progress)
			throws IndexOutOfBoundsException, IOException {
		progress.setProgress(-1);
		progress.setNote("Projecting log onto graph...");
		log.getLogSummary(); // trigger filtering
		Animation animation = new Animation(graph, log, maxLookahead,
				maxExtraLookahead);
		animation.initialize(progress);
		progress.setProgress(-1);
		progress.setNote("Finished.");
		return animation;
	}

	public Animation(FuzzyGraph graph, LogReader log, int maxLookahead,
			int maxExtraLookahead) {
		this.log = log;
		this.graph = graph;
		this.maxLookahead = maxLookahead;
		this.maxExtraLookahead = maxExtraLookahead;
		this.graphNodes = graph.getNodes();
		this.graphEdges = graph.getEdges();
		this.taskAnimations = new HashMap<String, TaskAnimation>();
		this.arcAnimations = new HashMap<String, ArcAnimation>();
		this.primitiveTaskAnimationList = new ArrayList<TaskAnimation>();
		this.clusterTaskAnimationList = new ArrayList<TaskAnimation>();
		this.arcAnimationList = new ArrayList<ArcAnimation>();
		this.startTime = Long.MAX_VALUE;
		this.endTime = Long.MIN_VALUE;
	}

	public GGDotParser getParser() {
		return parser;
	}

	public List<TaskAnimation> getPrimitiveTaskAnimations() {
		return primitiveTaskAnimationList;
	}

	public List<TaskAnimation> getClusterTaskAnimations() {
		return clusterTaskAnimationList;
	}

	public List<ArcAnimation> getArcAnimations() {
		return arcAnimationList;
	}

	public ArcAnimation getCaseAnimation() {
		return caseAnimation;
	}

	public long getStart() {
		return startTime;
	}

	public long getEnd() {
		return endTime;
	}

	public int getMinTraverseCount() {
		return minTraverseCount;
	}

	public int getMaxTraverseCount() {
		return maxTraverseCount;
	}

	public int getBestQuarterTraverseCount() {
		return bestQuarterTraverseCount;
	}

	public long getFadeTime() {
		return fadeTime;
	}

	public long getMeanBetweenTime() {
		return meanBetweenTime;
	}

	public long getMeanCaseDuration() {
		return meanCaseDuration;
	}

	public int getActivityBetween(long left, long right) {
		int activity = 0;
		for (TaskAnimation anim : taskAnimations.values()) {
			activity += anim.getActivityBetween(left, right);
		}
		return activity;
	}

	public void initialize(ProgressPanel progress)
			throws IndexOutOfBoundsException, IOException {
		int progressMax = log.getLogSummary().getNumberOfAuditTrailEntries();
		progress.setProgress(-1);
		progress.setNote("Creating and parsing graph layout...");
		// retrieve coordinates of elements
		StringWriter writer = new StringWriter();
		try {
			graph.writeToDot(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser = new GGDotParser(writer.getBuffer().toString());
		// initialize data structures
		progress.setNote("Initializing animation...");
		// initialize node / task animations from graph
		for (Node node : graphNodes) {
			String id = node.id();
			Type type = TaskAnimation.Type.PRIMITIVE;
			if (node instanceof ClusterNode) {
				type = TaskAnimation.Type.CLUSTER;
			}
			float[] coords = parser.getNodeCoordinates(id);
			TaskAnimation anim = new TaskAnimation(node, id, type, coords[0],
					coords[1], coords[2], coords[3]);
			taskAnimations.put(id, anim);
			if (type.equals(TaskAnimation.Type.CLUSTER)) {
				clusterTaskAnimationList.add(anim);
			} else {
				primitiveTaskAnimationList.add(anim);
			}
		}
		// initialize edge / arc animations from graph
		for (Edge edge : graphEdges.getEdges()) {
			String sourceId = edge.getSource().id();
			String targetId = edge.getTarget().id();
			String id = createEdgeId(edge.getSource(), edge.getTarget());
			float[] coords = parser.getEdgeCoordinates(id);
			GGCurve curve = new GGCurve(coords);
			ArcAnimation anim = new ArcAnimation(id, sourceId, targetId, curve);
			arcAnimations.put(id, anim);
			arcAnimationList.add(anim);
		}
		// initialize case animation
		this.caseAnimation = new ArcAnimation("cases", "start", "end", null);
		// prepare for parsing
		progress.setMinMax(0, progressMax);
		// parse all instances from log
		for (int i = 0; i < log.numberOfInstances(); i++) {
			progress.setNote("Reading log: trace " + (i + 1) + " / "
					+ log.numberOfInstances());
			parse(log.getInstance(i), progress);
		}
		// prepare for finalizing
		progress.setProgress(-1);
		progress.setNote("Preparing playback...");
		// finalize all data structures
		for (ArcAnimation anim : arcAnimations.values()) {
			anim.finalizeData();
		}
		for (TaskAnimation anim : taskAnimations.values()) {
			anim.finalizeData();
		}
		this.caseAnimation.finalizeData();
		// calculate traverse counts
		int[] traverseCounts = new int[arcAnimationList.size()];
		for (int i = 0; i < traverseCounts.length; i++) {
			traverseCounts[i] = arcAnimationList.get(i).getTraverseCount();
		}
		if (traverseCounts.length > 0) {
			Arrays.sort(traverseCounts);
			this.minTraverseCount = traverseCounts[0];
			this.maxTraverseCount = traverseCounts[traverseCounts.length - 1];
			int bqtcIndex = (int) ((float) traverseCounts.length * 0.75f);
			this.bestQuarterTraverseCount = traverseCounts[bqtcIndex];
		} else {
			this.minTraverseCount = 0;
			this.maxTraverseCount = 0;
			this.bestQuarterTraverseCount = 0;
		}
		// calculate fade time
		meanBetweenTime = 0;
		meanCaseDuration = 0;
		long start, end, tmp;
		int divisor = 0;
		AuditTrailEntryList ateList;
		for (int i = 0; i < log.numberOfInstances(); i++) {
			ateList = log.getInstance(i).getAuditTrailEntryList();
			if (ateList.size() < 2) {
				continue;
			}
			divisor++;
			start = ateList.get(0).getTimestamp().getTime();
			end = ateList.get(ateList.size() - 1).getTimestamp().getTime();
			tmp = (end - start) / (ateList.size() - 1);
			meanBetweenTime += tmp;
			meanCaseDuration += (end - start);
		}
		meanCaseDuration /= log.numberOfInstances();
		meanBetweenTime /= divisor;
		this.fadeTime = meanBetweenTime * 5;
		this.startTime -= 1000;
		this.endTime += this.fadeTime;
	}

	protected void parse(ProcessInstance instance, ProgressPanel progress)
			throws IndexOutOfBoundsException, IOException {
		int counter = 0;
		String caseId = instance.getName();
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		// insert case animation
		TokenAnimation caseTokenAnim = new TokenAnimation(caseId, ateList
				.get(0).getTimestamp().getTime(), ateList.get(
				ateList.size() - 1).getTimestamp().getTime());
		this.caseAnimation.addTokenAnimation(caseTokenAnim);
		// parse event list
		boolean[] seeded = new boolean[ateList.size()];
		Arrays.fill(seeded, false);
		AuditTrailEntry ref, comp;
		Node refNode, compNode;
		boolean swallowed;
		// step through event list
		for (int i = 0; i < ateList.size(); i++) {
			counter++;
			if (counter > 299) {
				progress.setProgress(progress.getValue() + counter);
				counter = 0;
			}
			swallowed = true; // reset swallowed marker
			ref = ateList.get(i);
			refNode = resolveNode(ref);
			if (refNode == null) {
				continue; // invalid event, ignore
			}
			// look ahead for potentially connected events
			int countdown = maxExtraLookahead;
			for (int j = i + 1; (j <= i + maxLookahead) && (j < ateList.size()); j++) {
				comp = ateList.get(j);
				compNode = resolveNode(comp);
				if (compNode == null) {
					continue; // invalid event, ignore
				}
				Edge edge = graphEdges.getEdge(refNode, compNode);
				if (edge != null) {
					// connected, mark target position
					seeded[j] = true;
					// mark as non-swallowed
					swallowed = false;
					// create and add token animation
					TokenAnimation tokenAnim = new TokenAnimation(caseId, ref
							.getTimestamp().getTime(), comp.getTimestamp()
							.getTime());
					ArcAnimation arcAnim = arcAnimations.get(createEdgeId(
							refNode, compNode));
					arcAnim.addTokenAnimation(tokenAnim);
				}
				// stop looking after the extra lookahead after
				// finding the first valid connection.
				if (swallowed == false) {
					countdown--;
					if (countdown < 0) {
						break;
					}
				}
			}
			// create and add task animation
			TaskAnimationKeyframe taskKeyframe = new TaskAnimationKeyframe(
					caseId, ref.getTimestamp().getTime(), !seeded[i], swallowed);
			TaskAnimation taskAnim = taskAnimations.get(refNode.id());
			taskAnim.addKeyframe(taskKeyframe);
			// update boundaries
			updateBoundaries(ref.getTimestamp().getTime());
		}
		progress.setProgress(progress.getValue() + counter);
	}

	protected Node resolveNode(AuditTrailEntry ate) {
		String element = ate.getElement();
		if (element.startsWith("cluster_")) {
			// referred node is a cluster
			for (ClusterNode cluster : graph.getClusterNodes()) {
				if (cluster.id().equals(element)) {
					return cluster;
				}
			}
		}
		// look for primitive node
		int index = graph.getEventClassIndex(ate.getElement(), ate.getType());
		if (index >= 0) {
			return graph.getNodeMappedTo(index);
		} else {
			return null;
		}
	}

	protected String createEdgeId(Node source, Node target) {
		return source.id() + "->" + target.id();
	}

	protected void updateBoundaries(long time) {
		if (time < startTime) {
			startTime = time;
		}
		if (time > endTime) {
			endTime = time;
		}
	}

}
