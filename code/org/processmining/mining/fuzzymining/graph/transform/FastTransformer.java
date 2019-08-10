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
package org.processmining.mining.fuzzymining.graph.transform;

import java.util.ArrayList;
import java.util.Set;

import org.processmining.framework.ui.Message;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.MutableFuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

public class FastTransformer extends FuzzyGraphTransformer {

	protected double threshold;
	protected MutableFuzzyGraph graph;
	protected ArrayList<FuzzyGraphTransformer> preTransformers;
	protected ArrayList<FuzzyGraphTransformer> interimTransformers;
	protected ArrayList<FuzzyGraphTransformer> postTransformers;

	public FastTransformer() {
		super("Fast transformer");
		threshold = 1.0;
		graph = null;
		preTransformers = new ArrayList<FuzzyGraphTransformer>();
		interimTransformers = new ArrayList<FuzzyGraphTransformer>();
		postTransformers = new ArrayList<FuzzyGraphTransformer>();
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getThreshold() {
		return threshold;
	}

	public void transform(MutableFuzzyGraph graph) {
		Message.add("Fast transformer started.", Message.NORMAL);
		this.graph = graph;
		// apply pre-transformers
		for (FuzzyGraphTransformer pre : preTransformers) {
			Message.add("  * Applying pre-flight graph transformer '"
					+ pre.getName() + "'.", Message.NORMAL);
			pre.transform(graph);
		}
		// perform initial clustering
		ArrayList<ClusterNode> clusters = cluster();
		// apply interim-transformers
		for (FuzzyGraphTransformer interim : interimTransformers) {
			Message.add("  * Applying in-flight graph transformer '"
					+ interim.getName() + "'.", Message.NORMAL);
			interim.transform(graph);
		}
		// merge and reduce cluster set
		clusters = merge(clusters);
		clusters = removeIsolatedClusters(clusters);
		clusters = removeSingularClusters(clusters);
		// apply post-transformers
		for (FuzzyGraphTransformer post : postTransformers) {
			Message.add("  * Applying post-flight graph transformer '"
					+ post.getName() + "'.", Message.NORMAL);
			post.transform(graph);
		}
		Message.add("Fast transformer completed.", Message.NORMAL);
	}

	protected ArrayList<Node> getSimplificationVictims() {
		ArrayList<Node> victims = new ArrayList<Node>();
		Node probe;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			probe = graph.getPrimitiveNode(i);
			if (probe.getSignificance() < threshold) {
				victims.add(probe);
			}
		}
		return victims;
	}

	protected ArrayList<ClusterNode> cluster() {
		ArrayList<Node> victims = getSimplificationVictims();
		ArrayList<ClusterNode> clusters = new ArrayList<ClusterNode>();
		Node victim, neighbor;
		int clusterIndex = graph.getNumberOfInitialNodes() + 1;
		// create initial set of clusters
		while (victims.size() > 0) {
			victim = victims.get(0);
			neighbor = getMostCorrelatedNeighbor(victim);
			if (neighbor instanceof ClusterNode) {
				// most related neighbor is a cluster; merge
				ClusterNode cluster = (ClusterNode) neighbor;
				cluster.add(victim);
				graph.setNodeAliasMapping(victim.getIndex(), cluster);
				victims.remove(victim);
			} else {
				// create new unary cluster
				ClusterNode cluster = new ClusterNode(graph, clusterIndex);
				clusterIndex++;
				cluster.add(victim);
				graph.setNodeAliasMapping(victim.getIndex(), cluster);
				victims.remove(victim);
				if (victims.contains(neighbor)) {
					cluster.add(neighbor);
					graph.setNodeAliasMapping(neighbor.getIndex(), cluster);
					victims.remove(neighbor);
				}
				graph.addClusterNode(cluster);
				clusters.add(cluster);
			}
		}
		return clusters;
	}

	protected ArrayList<ClusterNode> merge(ArrayList<ClusterNode> clusters) {
		int stopCounter = clusters.size();
		ClusterNode cluster, target;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			target = getPreferredMergeTarget(cluster);
			if (target != null) {
				// merge into target
				mergeWith(target, cluster);
				clusters.remove(cluster);
				stopCounter--;
			} else {
				// nothing to merge here; move along..
				index++;
			}
		}
		return clusters;
	}

	protected ClusterNode mergeWith(ClusterNode winner, ClusterNode loser) {
		for (Node node : loser.getPrimitives()) {
			winner.add(node);
			graph.setNodeAliasMapping(node.getIndex(), winner);
		}
		graph.removeClusterNode(loser);
		return winner;
	}

	protected ClusterNode getPreferredMergeTarget(ClusterNode subject) {
		ClusterNode preTarget = null;
		ClusterNode postTarget = null;
		double maxPreCorrelation = 0.0;
		double maxPostCorrelation = 0.0;
		ClusterNode object;
		double correlation;
		// process subject's preset nodes
		for (Node node : subject.getPredecessors()) {
			if (node instanceof ClusterNode) {
				object = (ClusterNode) node;
				correlation = getAggregateCorrelation(subject, object);
				if (correlation > maxPreCorrelation) {
					// new preferred target found
					maxPreCorrelation = correlation;
					preTarget = object;
				}
			} else {
				// abort search in preset
				preTarget = null;
				maxPreCorrelation = 0.0;
				break;
			}
		}
		// process subject's postset nodes
		for (Node node : subject.getSuccessors()) {
			if (node instanceof ClusterNode) {
				object = (ClusterNode) node;
				correlation = getAggregateCorrelation(subject, object);
				if (correlation > maxPostCorrelation) {
					// new preferred target found
					maxPostCorrelation = correlation;
					postTarget = object;
				}
			} else {
				// abort search in postset
				if (preTarget != null) {
					return preTarget;
				} else {
					return null;
				}
			}
		}
		// subject has both only clusters as pre- and postset
		// nodes: return most correlated neighbor out of both
		if (maxPreCorrelation > maxPostCorrelation) {
			return preTarget;
		} else {
			return postTarget;
		}
	}

	protected double getAggregateCorrelation(ClusterNode a, ClusterNode b) {
		Set<Node> aPrimitives = a.getPrimitives();
		Set<Node> bPrimitives = b.getPrimitives();
		double aggregateCorrelation = 0.0;
		for (Node aNode : aPrimitives) {
			for (Node bNode : bPrimitives) {
				aggregateCorrelation += graph.getBinaryCorrelation(aNode
						.getIndex(), bNode.getIndex());
				aggregateCorrelation += graph.getBinaryCorrelation(bNode
						.getIndex(), aNode.getIndex());
			}
		}
		return aggregateCorrelation;
	}

	protected ArrayList<ClusterNode> removeIsolatedClusters(
			ArrayList<ClusterNode> clusters) {
		Set<Node> preset;
		Set<Node> postset;
		int stopCounter = clusters.size();
		ClusterNode cluster;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			preset = cluster.getPredecessors();
			postset = cluster.getSuccessors();
			if (preset.size() == 0 && postset.size() == 0) {
				// cluster is isolated; remove from graph
				for (Node node : cluster.getPrimitives()) {
					graph.setNodeAliasMapping(node.getIndex(), null);
				}
				graph.removeClusterNode(cluster);
				clusters.remove(index);
				stopCounter--;
			} else {
				index++;
			}
		}
		return clusters;
	}

	protected ArrayList<ClusterNode> removeSingularClusters(
			ArrayList<ClusterNode> clusters) {
		int stopCounter = clusters.size();
		ClusterNode cluster;
		for (int index = 0; index < stopCounter;) {
			cluster = clusters.get(index);
			if (cluster.size() == 1) {
				// remove cluster here
				eliminateSingularClusterPreservingLinks(cluster);
				clusters.remove(index);
				stopCounter--;
			} else {
				index++;
			}
		}
		return clusters;
	}

	protected void eliminateSingularClusterPreservingLinks(ClusterNode cluster) {
		Node singularNode = cluster.getPrimitives().toArray(new Node[1])[0];
		int ownIndex = singularNode.getIndex();
		int preIndex, postIndex;
		double fromSig, toSig, fromCorr, toCorr;
		Set<Node> preSet = singularNode.getPredecessors();
		Set<Node> postSet = singularNode.getSuccessors();
		for (Node pre : preSet) {
			if (pre instanceof ClusterNode) {
				continue;
			}
			preIndex = pre.getIndex();
			for (Node post : postSet) {
				if (post instanceof ClusterNode) {
					continue;
				}
				postIndex = post.getIndex();
				if (graph.getBinarySignificance(preIndex, postIndex) == 0.0) {
					// no link previously existing
					fromSig = graph.getBinarySignificance(preIndex, ownIndex);
					toSig = graph.getBinarySignificance(ownIndex, postIndex);
					fromCorr = graph.getBinaryCorrelation(preIndex, ownIndex);
					toCorr = graph.getBinaryCorrelation(ownIndex, postIndex);
					graph.setBinarySignificance(preIndex, postIndex,
							(fromSig + toSig) / 2.0);
					graph.setBinaryCorrelation(preIndex, postIndex,
							(fromCorr + toCorr) / 2.0);
				}
				// delete edges to singular node
				graph.setBinaryCorrelation(preIndex, ownIndex, 0.0);
				graph.setBinarySignificance(preIndex, ownIndex, 0.0);
				graph.setBinaryCorrelation(ownIndex, postIndex, 0.0);
				graph.setBinarySignificance(ownIndex, postIndex, 0.0);
			}
		}
		graph.setNodeAliasMapping(singularNode.getIndex(), null);
		graph.removeClusterNode(cluster);
	}

	protected Node getMostSignificantNeighbor(Node node) {
		int refIndex = node.getIndex();
		double maxSignificance = 0.0;
		double curSignificance;
		Node winner = null;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			if (i == refIndex) {
				continue;
			} // skip self
			// check forward relation
			curSignificance = graph.getBinarySignificance(refIndex, i);
			if (curSignificance > maxSignificance) {
				winner = graph.getNodeMappedTo(i);
				maxSignificance = curSignificance;
			}
			// check backward relation
			curSignificance = graph.getBinarySignificance(i, refIndex);
			if (curSignificance > maxSignificance) {
				winner = graph.getNodeMappedTo(i);
				maxSignificance = curSignificance;
			}
		}
		return winner;
	}

	protected Node getMostCorrelatedNeighbor(Node node) {
		int refIndex = node.getIndex();
		double maxCorrelation = 0.0;
		double curCorrelation;
		Node winner = null;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			if (i == refIndex) {
				continue;
			} // skip self
			// check forward relation
			curCorrelation = graph.getBinaryCorrelation(refIndex, i);
			if (curCorrelation > maxCorrelation) {
				winner = graph.getNodeMappedTo(i);
				maxCorrelation = curCorrelation;
			}
			// check backward relation
			curCorrelation = graph.getBinaryCorrelation(i, refIndex);
			if (curCorrelation > maxCorrelation) {
				winner = graph.getNodeMappedTo(i);
				maxCorrelation = curCorrelation;
			}
		}
		return winner;
	}

	public void addPreTransformer(FuzzyGraphTransformer transformer) {
		if (preTransformers.contains(transformer) == false) {
			preTransformers.add(transformer);
		}
	}

	public boolean removePreTransformer(FuzzyGraphTransformer transformer) {
		return preTransformers.remove(transformer);
	}

	public void clearPreTransformers() {
		preTransformers.clear();
	}

	public void addInterimTransformer(FuzzyGraphTransformer transformer) {
		if (interimTransformers.contains(transformer) == false) {
			interimTransformers.add(transformer);
		}
	}

	public boolean removeInterimTransformer(FuzzyGraphTransformer transformer) {
		return interimTransformers.remove(transformer);
	}

	public void clearInterimTransformers() {
		interimTransformers.clear();
	}

	public void addPostTransformer(FuzzyGraphTransformer transformer) {
		if (postTransformers.contains(transformer) == false) {
			postTransformers.add(transformer);
		}
	}

	public boolean removePostTransformer(FuzzyGraphTransformer transformer) {
		return postTransformers.remove(transformer);
	}

	public void clearPostTransformers() {
		postTransformers.clear();
	}

}
