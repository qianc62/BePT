/*
 * Created on 9 juin 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.patternsmining;

import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author WALID
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class WorkflowPatternsMiner implements MiningPlugin {

	private LogEvents events;
	private DependenciesTables Deptab;
	private DoubleMatrix2D directPrecedents; // =
	// logAbstraction.getFollowerInfo(1).copy();
	private DoubleMatrix2D directfollowers;
	private DoubleMatrix2D causalPrecedents; // calculated in makeBasicRelations
	private DoubleMatrix2D causalSuccession; // calculated in makeBasicRelations
	private DoubleMatrix1D ACWWidth;
	private LogEvents modelElements;
	private LogReader log;
	private LogFilter filter;
	private LogRelationEditor relationsEditor;

	// private LogFile filename;

	private JFrame frametest;

	public WorkflowPatternsMiner() {

	}

	public String getName() {
		return ("Workflow patterns miner");
	}

	public JPanel getOptionsPanel(LogSummary arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void message(String msg, int stage, Progress progress) {
		Message.add(msg, Message.DEBUG);
		if (progress != null) {
			progress.setNote(msg);
			progress.setProgress(stage);
		}
	}

	public MiningResult mine(LogReader log) {
		/*
		 * Progress progress; progress = new Progress("Mining " +
		 * log.getFile().getShortName() + " using " + getName());
		 * progress.setMinMax(0, 5);
		 * 
		 * message(
		 * "Starting workflow patterns mining...                                    "
		 * , 1, progress);
		 */
		// System.out.println(log.toString());
		LogAbstraction logAbstraction;

		// System.out.println("Starting heuristics mining 1...                                    ");
		logAbstraction = new LogAbstractionImpl(log);
		events = log.getLogSummary().getLogEvents();
		// System.out.println(events.toString());

		// System.out.println(log.getLogSummary().toString());
		// System.out.println("LogSummary 2...");
		// System.out.println(log.getLogSummary().getLogEvents());
		// System.out.println(events.size());

		Deptab = new DependenciesTables(log);

		// System.out.println("getfinalcausalfollowers()...");
		// System.out.println(Deptab.getfinalcausalfollowers().toString());

		PatternsResult resultdebug = new PatternsResult(this, log, Deptab);
		ModelGraph epc = resultdebug.build();

		/*
		 * // walid on le fait pour le test externe frametest= new
		 * JFrame("Workflow Patterns Miner"); frametest.setBounds(0,0,700,500);
		 * frametest.getContentPane().add(resultdebug.getVisualization(),
		 * BorderLayout.CENTER); frametest.show(); relationsEditor = new
		 * LogRelationEditor(frametest,Deptab);
		 */

		relationsEditor = new LogRelationEditor(MainUI.getInstance(), Deptab);

		return resultdebug;
		// return new PatternsResult(log, epc, this);
	}

	public void editRelations(LogEvent event) {
		relationsEditor.edit(frametest, Deptab, event);

		// logRelations = relationsEditor.getLogRelations();
		// log.reset();
		// return mine(log);

	}

	/**
	 * @param log
	 * @return first matrix to do: factorize logeevents param modelElements to
	 *         do: division by act freq may be done in the last not here
	 */
	private DoubleMatrix2D getDirectPreviousInfo(LogReader log) {

		int numSimilarPIs = 0;
		log.reset();

		modelElements = log.getLogSummary().getLogEvents();
		System.out.println("walid fonc1");

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		// Walk through the log to create all ModelElements
		while (log.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = log.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);

			AuditTrailEntries ates = pi.getAuditTrailEntries();

			// Create a memory for the required distance
			AuditTrailEntry ate = ates.next();
			int memory = modelElements.findLogEventNumber(ate.getElement(), ate
					.getType());

			while (ates.hasNext()) {
				// Walk trough all audittrailentries
				ate = ates.next();
				int index = modelElements.findLogEventNumber(ate.getElement(),
						ate.getType());
				D.set(index, memory, D.get(index, memory) + numSimilarPIs);
				memory = index;
			}
		}

		for (int j = 0; j < s; j++) {
			for (int k = 0; k < j; k++) {
				if (D.get(j, k) > 0 && D.get(k, j) > 0) {
					D.set(j, k, -D.get(j, k));
					D.set(k, j, -D.get(k, j));
				}
			}
		}
		return D;
	}

	/**
	 * @param log
	 * @return first matrix to do: factorize logeevents param modelElements to
	 *         do: division by act freq may be done in the last not here
	 */
	private DoubleMatrix2D getDirectFollowerInfo(LogReader log) {

		int numSimilarPIs = 0;
		log.reset();

		modelElements = log.getLogSummary().getLogEvents();
		// System.out.println("walid fonc1");

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		// Walk through the log to create all ModelElements
		while (log.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = log.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);

			AuditTrailEntries ates = pi.getAuditTrailEntries();

			// Create a memory for the required distance
			AuditTrailEntry ate = ates.next();
			int memory = modelElements.findLogEventNumber(ate.getElement(), ate
					.getType());

			while (ates.hasNext()) {
				// Walk trough all audittrailentries
				ate = ates.next();
				int index = modelElements.findLogEventNumber(ate.getElement(),
						ate.getType());
				D.set(memory, index, D.get(memory, index) + numSimilarPIs);
				memory = index;
			}
		}

		for (int j = 0; j < s; j++) {
			for (int k = 0; k < j; k++) {
				if (D.get(j, k) > 0 && D.get(k, j) > 0) {
					D.set(j, k, -D.get(j, k));
					D.set(k, j, -D.get(k, j));
				}
			}
		}
		return D;
	}

	private DoubleMatrix2D getCausalPrecedents(DoubleMatrix2D directPrecedents) {
		DoubleMatrix2D D = directPrecedents.copy();
		int s = modelElements.size();
		for (int j = 0; j < s; j++) {
			int freq = modelElements.getEvent(j).getOccurrenceCount();
			for (int k = 0; k < s; k++) {
				D.set(j, k, D.get(j, k) / freq);
			}
		}
		return D;
	}

	private DoubleMatrix2D getCausalSuccession(DoubleMatrix2D directPrecedents) {
		DoubleMatrix2D D = directPrecedents.copy();
		int s = modelElements.size();
		for (int j = 0; j < s; j++) {
			int freq = modelElements.getEvent(j).getOccurrenceCount();
			for (int k = 0; k < s; k++) {
				D.set(k, j, D.get(k, j) / freq);
			}
		}
		return D;
	}

	private DoubleMatrix1D getBackwardACWWidth(DoubleMatrix2D directPrecedents) {
		int s = modelElements.size();
		DoubleMatrix1D D = DoubleFactory1D.sparse.make(s, 1);
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < i; j++) {
				if (directPrecedents.get(i, j) < 0) {
					D.set(i, D.get(i) + 1);
					D.set(j, D.get(j) + 1);
				}
			}
		}
		DoubleMatrix1D D1 = D.copy();
		for (int k = 0; k < s; k++) {
			if (D.get(k) == 1) {
				for (int l = 0; l < s; l++) {
					if (directPrecedents.get(k, l) > 0 && D1.get(l) > D1.get(k)) {
						D.set(k, D.get(l));
					}
				}
			}
		}
		return D;
	}

	private DoubleMatrix1D getForwardACWWidth(DoubleMatrix2D directPrecedents) {
		int s = modelElements.size();
		DoubleMatrix1D D = DoubleFactory1D.sparse.make(s, 1);
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < i; j++) {
				if (directPrecedents.get(i, j) < 0) {
					D.set(i, D.get(i) + 1);
					D.set(j, D.get(j) + 1);
				}
			}
		}
		DoubleMatrix1D D1 = D.copy();
		for (int k = 0; k < s; k++) {
			if (D.get(k) == 1) {
				for (int l = 0; l < s; l++) {
					if (directPrecedents.get(k, l) > 0 && D1.get(l) > D1.get(k)) {
						D.set(k, D.get(l));
					}
				}
			}
		}
		return D;
	}

	private DoubleMatrix2D getfinalSuccession(LogReader log, DoubleMatrix1D ACW) {
		int numSimilarPIs = 0;
		log.reset();
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		while (log.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = log.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			// Create a memory for the required distance

			AuditTrailEntries ates = pi.getAuditTrailEntries();
			ArrayList instanceList = ates.toArrayList();
			int i = 0;

			while (i < instanceList.size() - 1) {
				AuditTrailEntry ate = (AuditTrailEntry) instanceList.get(i);
				int courantActivity = modelElements.findLogEventNumber(ate
						.getElement(), ate.getType());
				int ACWwidth = (int) ACW.get(courantActivity);
				for (int k = 1; k < ACWwidth + 1; k++) {
					if (i + k < instanceList.size()) {
						AuditTrailEntry atefollow = (AuditTrailEntry) instanceList
								.get(i + k);
						int followerActivity = modelElements
								.findLogEventNumber(atefollow.getElement(),
										atefollow.getType());
						D.set(courantActivity, followerActivity, D.get(
								courantActivity, followerActivity)
								+ numSimilarPIs);
					}
				}
				i++;
			}
		}
		for (int j = 0; j < s; j++) {
			for (int k = 0; k < j; k++) {
				if (D.get(j, k) > 0 && D.get(k, j) > 0) {
					D.set(j, k, -D.get(j, k));
					D.set(k, j, -D.get(k, j));
				}
			}
		}

		return D;
	}

	public DoubleMatrix2D getPrecedentInfo(int distance, LogReader log) {

		int numSimilarPIs = 0;
		log.reset();

		modelElements = log.getLogSummary().getLogEvents();
		System.out.println("walid");

		boolean update = false;

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		// Walk through the log to create all ModelElements
		while (log.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = log.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			// Create a memory for the required distance
			int[] memory = new int[distance];

			int i = 0;
			AuditTrailEntries ates = pi.getAuditTrailEntries();

			while (ates.hasNext()) {
				// Walk trough all audittrailentries
				AuditTrailEntry ate = ates.next();

				if (i < distance) {
					memory[i] = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());
					i++;
					continue;
				}

				int index = modelElements.findLogEventNumber(ate.getElement(),
						ate.getType());

				if (distance == 0) {
					D.set(index, index, D.get(index, index) + numSimilarPIs);
				} else {
					// Now, start adding values to the distance matrix
					// D.set(memory[0], index, D.get(memory[0], index) +
					// numSimilarPIs);
					// ******walid modif
					D.set(index, memory[0], D.get(index, memory[0])
							+ numSimilarPIs);
					for (int j = 0; j < distance - 1; j++) {
						memory[j] = memory[j + 1];
					}
					memory[distance - 1] = index;
				}
			}
		}
		return D;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "<h1>"
				+ getName()
				+ "</h1>"
				+ "<p>"
				+ "For more information, please refer to publication:<br>"
				+ "&nbsp;&nbsp;&nbsp;Walid Gaaloul et al.<br>"
				+ "&nbsp;&nbsp;&nbsp;<i>Towards Mining Structural Workflow Patterns</i><br>"
				+ "&nbsp;&nbsp;&nbsp;in 16th International Conference on Database and Expert Systems Applications DEXA'05 August 22-26, 2005, Copenhagen Danemark.<br>"
				+ "at http://www.loria.fr/~gaaloul" + "</p>";
	}

}
