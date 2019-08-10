/**
 * Project: ProM
 * File: ClusterGraphAdapter.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Jul 12, 2006, 5:56:32 PM
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
public class ClusterGraphAdapter extends GrappaAdapter {

	protected ClusterGraph graph = null;

	public ClusterGraphAdapter(ClusterGraph aGraph) {
		graph = aGraph;
	}

	@Override
	protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
			GrappaPoint pt, int modifiers, GrappaPanel panel) {
		// TODO Auto-generated method stub
		return super.getCustomMenu(subg, elem, pt, modifiers, panel);
	}

	@Override
	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		if (elem != null && elem.getName() != null) { // sanity checks
			ClusterNode target = graph.resolveCluster(elem.getName());
			if (target != null) {
				System.out.println("Clicked on cluster: " + target.getId());
				try {
					MainUI.getInstance().createFrame(
							"Detailled graph for 'Cluster "
									+ target.getInstanceNumber() + "'",
							target.getClusterGraphPanel());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, GrappaPanel panel) {
		if (elem != null && elem.getName() != null) { // sanity checks
			Node target = graph.resolveNode(elem.getName());
			if (target != null) {
				return target.getToolTipText();
			} else {
				return super.grappaTip(subg, elem, pt, modifiers, panel);
			}
		} else {
			return super.grappaTip(subg, elem, pt, modifiers, panel);
		}
	}

}
