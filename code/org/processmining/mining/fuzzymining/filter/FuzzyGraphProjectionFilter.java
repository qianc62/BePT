package org.processmining.mining.fuzzymining.filter;

import java.io.BufferedWriter;
import java.io.IOException;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.LogFilterParameterDialog;
import org.processmining.framework.ui.Message;
import org.processmining.mining.fuzzymining.graph.ClusterNode;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.graph.Node;

public class FuzzyGraphProjectionFilter extends LogFilter {

	protected FuzzyGraph graph;
	protected LogEvents events;

	public FuzzyGraphProjectionFilter(FuzzyGraph aGraph) {
		super(LogFilter.FAST, "Fuzzy Graph Projection Filter");
		graph = aGraph;
		events = graph.getLogEvents();
	}

	@Override
	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		ClusterNode lastCluster = null;
		AuditTrailEntry current = null;
		Node node = null;
		for (int i = 0; i < ateList.size(); i++) {
			try {
				current = ateList.get(i);
				int logEventIndex = events.findLogEventNumber(current
						.getElement(), current.getType());
				if (logEventIndex < 0) {
					// no valid log event found, look for cluster match
					node = null;
					if (current.getElement().startsWith("cluster")) {
						// log already filtered
						for (ClusterNode cluster : graph.getClusterNodes()) {
							if (cluster.id().equals(current.getElement())) {
								node = cluster;
								break;
							}
						}
					}
					if (node == null) {
						// not found
						Message.add(
								"Fuzzy Graph projection filter: Could not find log event for "
										+ current.getElement() + "/"
										+ current.getType() + "!",
								Message.WARNING);
					}
				} else {
					node = graph.getNodeMappedTo(logEventIndex);
				}
				if (node == null) {
					// event class has been removed
					ateList.remove(i);
					i--; // correct index
				} else if (node instanceof ClusterNode) {
					// event class has been clustered
					ClusterNode cluster = (ClusterNode) node;
					if (lastCluster != null && lastCluster.equals(cluster)) {
						// ignore repetitions of the same cluster, i.e. remove
						// event
						ateList.remove(i);
						i--;
					} else {
						// replace event with reference to cluster
						current.setAttribute("FMFILTER_OriginalElement",
								current.getElement());
						current.setAttribute("FMFILTER_OriginalEventType",
								current.getType());
						current.setElement(cluster.id());
						ateList.replace(current, i);
						// remember last cluster which we mapped to
						lastCluster = cluster;
					}
				} else {
					// else: normal, unclustered event, leave as is.
					// reset last cluster
					lastCluster = null;
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
