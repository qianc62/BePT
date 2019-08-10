/*
 * Copyright (c) 2008 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.streamscope;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.processmining.analysis.streamscope.cluster.ClusterNode;
import org.processmining.analysis.streamscope.cluster.ClusterSet;
import org.processmining.analysis.streamscope.cluster.Node;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.LogFilterParameterDialog;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class StreamScopeFilter extends LogFilter {

	protected ClusterSet clusters;
	protected List<Node> nodes;
	protected EventClassTable ecTable;
	protected int level;

	public StreamScopeFilter(ClusterSet clusters, int level) {
		super(LogFilter.FAST, "Fuzzy Graph Projection Filter");
		this.clusters = clusters;
		this.level = level;
		this.ecTable = clusters.getOrderedEventClassTable();
		this.nodes = clusters.getOrderedNodesForLevel(level);
	}

	protected ClusterNode findCluster(AuditTrailEntry ate) {
		int ateIndex = ecTable.getIndex(ate.getElement());
		for (Node node : nodes) {
			int[] indices = node.getIndices();
			for (int index : indices) {
				if (index == ateIndex) {
					if (node instanceof ClusterNode) {
						return (ClusterNode) node;
					} else {
						return null;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		AuditTrailEntry current = null;
		ClusterNode currentCluster = null;
		for (int i = 0; i < ateList.size(); i++) {
			try {
				current = ateList.get(i);
				currentCluster = findCluster(current);
				if (currentCluster != null) {
					current.setElement(currentCluster.getName());
					current.setType("complete");
					ateList.replace(current, i);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false; // abort
			}
		}
		if (ateList.size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected String getHelpForThisLogFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void readSpecificXML(org.w3c.dom.Node logFilterSpecifcNode)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// TODO Auto-generated method stub

	}

}
