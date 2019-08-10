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
package org.processmining.mining.fuzzymining.graph;

import javax.swing.JMenuItem;

import org.processmining.framework.ui.MainUI;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyGraphAdapter extends GrappaAdapter {

	protected FuzzyGraph graph = null;

	public FuzzyGraphAdapter(FuzzyGraph aGraph) {
		graph = aGraph;
	}

	protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
			GrappaPoint pt, int modifiers, GrappaPanel panel) {
		return super.getCustomMenu(subg, elem, pt, modifiers, panel);
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		if (elem != null && elem.getName() != null) { // sanity checks
			ClusterNode target = resolveCluster(elem.getName());
			if (target != null) {
				System.out.println("Clicked on cluster: " + target.id());
				try {
					MainUI.getInstance().createFrame(
							"Detailled graph for 'Cluster " + target.getIndex()
									+ "'", target.getClusterGraphPanel());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel) {
		if (elem != null && elem.getName() != null) { // sanity checks
			Node target = resolveNode(elem.getName());
			if (target == null) {
				target = resolveCluster(elem.getName());
			}
			if (target != null) {
				return target.getToolTipText();
			} else {
				return super.grappaTip(subg, elem, pt, modifiers, panel);
			}
		} else {
			return super.grappaTip(subg, elem, pt, modifiers, panel);
		}
	}

	protected ClusterNode resolveCluster(String id) {
		for (ClusterNode cluster : graph.getClusterNodes()) {
			if (cluster.id().equals(id)) {
				return cluster;
			}
		}
		return null;
	}

	protected Node resolveNode(String id) {
		Node node;
		for (int i = graph.getNumberOfInitialNodes() - 1; i >= 0; i--) {
			node = graph.getPrimitiveNode(i);
			if (node.id().equals(id)) {
				return node;
			}
		}
		return null;
	}

}
