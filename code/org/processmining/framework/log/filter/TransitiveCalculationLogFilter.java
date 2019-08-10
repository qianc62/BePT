package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class TransitiveCalculationLogFilter extends LogFilter {

	private HashMap<String, Integer> mapping;

	public TransitiveCalculationLogFilter(int load, String name) {
		super(load, name);
	}

	protected boolean doFiltering(ProcessInstance pi) {
		if (!pi.getAttributes().containsKey(ProcessInstance.ATT_PI_PO)
				|| !pi.getAttributes().get(ProcessInstance.ATT_PI_PO).equals(
						"true")) {
			// skip this pi, since it is not a partial order
			return true;
		}
		mapping = new HashMap();

		try {
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			for (int i = 0; i < ates.size(); i++) {
				AuditTrailEntry ate = ates.get(i);
				mapping.put((String) ate.getAttributes().get(
						ProcessInstance.ATT_ATE_ID), new Integer(i));
			}

			int[][] adj = new int[mapping.keySet().size()][mapping.keySet()
					.size()];
			int N = 2 * adj.length;
			for (int i = 0; i < adj.length; i++) {
				Arrays.fill(adj[i], N);
			}
			Iterator it = ates.iterator();
			while (it.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) it.next();

				int ateID = mapping.get(
						ate.getAttributes().get(ProcessInstance.ATT_ATE_ID))
						.intValue();
				adj[ateID][ateID] = 0;
				for (String s : getPostSet(ate)) {
					if (mapping.keySet().contains(s)) {
						adj[ateID][mapping.get(s).intValue()] = 1;
					}
				}
				for (String s : getPreSet(ate)) {
					if (mapping.keySet().contains(s)) {
						adj[mapping.get(s).intValue()][ateID] = 1;
					}
				}
				/*
				 * if
				 * (ate.getAttributes().get(ProcessInstance.ATT_ATE_PRE).equals
				 * ("")) { extendPO(ate, mapping, pi.getAuditTrailEntryList(),
				 * new HashSet()); }
				 */
			}
			doCalculation(adj, N);

			for (int i = 0; i < adj.length; i++) {
				AuditTrailEntry ate = ates.get(i);
				HashSet<String> preset = new HashSet<String>();
				HashSet<String> postset = new HashSet<String>();
				for (int j = 0; j < adj.length; j++) {
					if (adj[i][j] > 0 && adj[i][j] < N) {
						postset.add(ates.get(j).getAttributes().get(
								ProcessInstance.ATT_ATE_ID));
					}
					if (adj[j][i] > 0 && adj[j][i] < N) {
						preset.add(ates.get(j).getAttributes().get(
								ProcessInstance.ATT_ATE_ID));
					}
				}
				ate.setAttribute(ProcessInstance.ATT_ATE_PRE, toCSL(preset));
				ate.setAttribute(ProcessInstance.ATT_ATE_POST, toCSL(postset));
				ates.replace(ate, i);
			}

		} catch (Exception e) {
			dealWithError(e);
		}

		return !pi.isEmpty();
	}

	protected abstract void doCalculation(int[][] adj, int N);

	private void dealWithError(Exception e) {
		Message.add("Problem while transitively closing pi: " + e.getMessage(),
				Message.ERROR);
	}

	private HashSet<String> getPreSet(AuditTrailEntry ate) {
		String pre = (String) ate.getAttributes().get(
				ProcessInstance.ATT_ATE_PRE);
		StringTokenizer st = new StringTokenizer(pre, ",");
		HashSet<String> preNodes = new HashSet();
		while (st.hasMoreTokens()) {
			preNodes.add(st.nextToken());
		}
		return preNodes;
	}

	private HashSet<String> getPostSet(AuditTrailEntry ate) {
		String post = (String) ate.getAttributes().get(
				ProcessInstance.ATT_ATE_POST);
		StringTokenizer st = new StringTokenizer(post, ",");
		HashSet<String> postNodes = new HashSet();
		while (st.hasMoreTokens()) {
			postNodes.add(st.nextToken());
		}
		return postNodes;
	}

	private String toCSL(HashSet<String> set) {
		if (set.isEmpty()) {
			return "";
		}
		Iterator<String> it = set.iterator();
		String s = it.next();
		while (it.hasNext()) {
			s += "," + it.next();
		}
		return s;
	}

	protected boolean thisFilterChangesLog() {
		return true;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				TransitiveCalculationLogFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				try {
					return TransitiveCalculationLogFilter.this.getClass()
							.newInstance();
				} catch (IllegalAccessException ex) {
					Message
							.add(
									"ERROR in instantiating new TransitiveCalculationFilter",
									Message.ERROR);
					return null;
				} catch (InstantiationException ex) {
					Message
							.add(
									"ERROR in instantiating new TransitiveCalculationFilter",
									Message.ERROR);
					return null;
				}
			}
		};
	}

	protected abstract String getHelpForThisLogFilter();

	protected void writeSpecificXML(BufferedWriter bufferedWriter)
			throws IOException {
	}

	protected void readSpecificXML(Node node) throws IOException {
	}

}
