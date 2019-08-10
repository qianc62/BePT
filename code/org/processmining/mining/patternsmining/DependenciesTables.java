/*
 * Created on 5 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.patternsmining;

import java.util.ArrayList;
import java.util.Vector;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

public class DependenciesTables {

	protected DoubleMatrix2D causal;
	protected DoubleMatrix2D parallel;
	protected DoubleMatrix1D end;
	protected DoubleMatrix1D start;
	protected DoubleMatrix1D loops;

	protected LogEvents events;
	private DoubleMatrix1D ACWWidth;
	private LogEvents modelElements;
	private LogReader log;
	private LogFilter filter;
	private DoubleMatrix2D directPrecedents; // =
	// logAbstraction.getFollowerInfo(1).copy();
	private DoubleMatrix2D directfollowers;
	private DoubleMatrix2D finalfollowers;
	private DoubleMatrix2D finalcausalfollowers;
	private DoubleMatrix2D finalcausalprecedents;

	private Vector[] Concurrent;
	private Vector[] completePreceeder;
	private Vector[] partialPreceeder;

	private Vector[] completefollowers;
	private Vector[] partialfollowers;

	private int[] nbconcurrent;
	private int[] nbcompletePreceeder;
	private int[] nbpartialPreceeder;

	private int[] nbcompletefollowers;
	private int[] nbpartialfollowers;

	private double[] sumfollowers;
	private double[] sumPreceeder;

	public DependenciesTables(LogReader log) {
		this.log = log;
		this.directfollowers = DirectFollowerInfo(log);
		this.directPrecedents = DirectPreviousInfo(log);
		this.ACWWidth = ForwardACWWidth(directfollowers);
		this.finalfollowers = finalSuccessionright(log, ACWWidth,
				directfollowers);
		this.finalcausalfollowers = Causalfollowers(finalfollowers);
		this.finalcausalprecedents = Causalprecedents(finalfollowers);
		this.events = log.getLogSummary().getLogEvents();
		build();

	}

	public DependenciesTables(DoubleMatrix2D causal, DoubleMatrix2D parallel,
			DoubleMatrix1D end, DoubleMatrix1D start, DoubleMatrix1D loops,
			LogEvents events) {
		this.causal = causal;
		this.parallel = parallel;
		this.end = end;
		this.start = start;
		this.loops = loops;
		this.events = events;
		build();
	}

	public DependenciesTables(DependenciesTables relations) {
		this(relations.getCausalFollowerMatrix(),
				relations.getParallelMatrix(), relations.getEndInfo(),
				relations.getStartInfo(), relations.getOneLengthLoopsInfo(),
				relations.getLogEvents());
	}

	/**
	 * @param log
	 * @return first matrix to do: factorize logeevents param modelElements to
	 *         do: division by act freq may be done in the last not here
	 */

	private DoubleMatrix2D DirectPreviousInfo(LogReader log) {

		int numSimilarPIs = 0;
		log.reset();

		modelElements = log.getLogSummary().getLogEvents();

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
	private DoubleMatrix2D DirectFollowerInfo(LogReader log) {

		int numSimilarPIs = 0;
		log.reset();

		modelElements = log.getLogSummary().getLogEvents();

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

	private DoubleMatrix2D Causalfollowers(DoubleMatrix2D directPrecedents) {
		DoubleMatrix2D D = directPrecedents.copy();
		int s = modelElements.size();
		for (int j = 0; j < s; j++) {
			int freq = modelElements.getEvent(j).getOccurrenceCount();
			for (int k = 0; k < s; k++) {
				// D.set(j, k, D.get(j, k)/freq); a cause des boucle dans thread
				// concurrent
				if (D.get(j, k) / freq / freq > 1) {
					D.set(j, k, 1);
				} else {
					D.set(j, k, D.get(j, k) / freq);
				}
			}
		}
		return D;
	}

	private DoubleMatrix2D Causalprecedents(DoubleMatrix2D directPrecedents) {
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);
		for (int i = 0; i < s; i++) {
			int freq = modelElements.getEvent(i).getOccurrenceCount();
			for (int j = 0; j < s; j++) {
				// D.set(i, j, directPrecedents.get(j, i)/freq); a cause des
				// boucle dans thread concurrent
				if (directPrecedents.get(j, i) / freq > 1) {
					D.set(i, j, 1);
				} else {
					D.set(i, j, directPrecedents.get(j, i) / freq);
				}

			}
		}
		return D;
	}

	private DoubleMatrix1D BackwardACWWidth(DoubleMatrix2D directPrecedents) {
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
			if (D1.get(k) == 1) {
				for (int l = 0; l < s; l++) {
					if (directPrecedents.get(k, l) > 0 && D1.get(l) > D.get(k)) {
						D.set(k, D.get(l));
					}
				}
			}
		}
		return D;
	}

	private DoubleMatrix1D ForwardACWWidth(DoubleMatrix2D directPrecedents) {
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
			if (D1.get(k) == 1) {
				for (int l = 0; l < s; l++) {
					if (directPrecedents.get(k, l) > 0 && D1.get(l) > D.get(k)) {
						D.set(k, D.get(l));
					}
				}
			}
		}
		return D;
	}

	// il faut bosser ici walid pour les boucles

	private DoubleMatrix2D finalSuccessionright(LogReader log,
			DoubleMatrix1D ACW, DoubleMatrix2D directfollowers) {
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

				// ////////////////////////////////////////////////////////////////////////
				// ///////////////////////////// changement a cause des boucle
				// dans le comportement concurrent
				int k = 1;
				int m = 0;
				boolean[] boucle = new boolean[s];
				for (int l = 0; l < s; l++) {
					boucle[l] = true;
				}
				while (k < ACWwidth + 1 && i + m + 1 < instanceList.size()) {
					m++;
					if (i + m < instanceList.size()) {
						AuditTrailEntry atefollow = (AuditTrailEntry) instanceList
								.get(i + m);
						int followerActivity = modelElements
								.findLogEventNumber(atefollow.getElement(),
										atefollow.getType());
						if (boucle[followerActivity]) {
							if (directfollowers.get(courantActivity,
									followerActivity) != 0) {
								D.set(courantActivity, followerActivity, D.get(
										courantActivity, followerActivity)
										+ numSimilarPIs);
							}
							boucle[followerActivity] = false;
							k++;
						}

					}
				}
				// ////////////////

				/*
				 * for(int k=1; k<ACWwidth+1; k++){ if
				 * (i+k<instanceList.size()){ AuditTrailEntry atefollow
				 * =(AuditTrailEntry) instanceList.get(i+k); int
				 * followerActivity =
				 * modelElements.findLogEventNumber(atefollow.getElement(),
				 * atefollow.getType()); if (directfollowers.get(courantActivity
				 * , followerActivity)!=0){ D.set(courantActivity ,
				 * followerActivity, D.get(courantActivity, followerActivity) +
				 * numSimilarPIs); } } }
				 */
				// //////////////////////////
				// /////////////////////////////////////
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

	// a corriger il faut changer seulement les valeurs qui ne sont pas dans la
	// matrice directfollower

	private DoubleMatrix2D finalSuccession(LogReader log, DoubleMatrix1D ACW) {
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

	public void build() {

		int s = modelElements.size();
		Concurrent = new Vector[s];
		completePreceeder = new Vector[s];
		partialPreceeder = new Vector[s];

		completefollowers = new Vector[s];
		partialfollowers = new Vector[s];

		nbconcurrent = new int[s];
		nbcompletePreceeder = new int[s];
		nbpartialPreceeder = new int[s];

		nbcompletefollowers = new int[s];
		nbpartialfollowers = new int[s];

		sumfollowers = new double[s];
		sumPreceeder = new double[s];

		this.finalcausalfollowers = getfinalcausalfollowers();
		this.finalcausalprecedents = getfinalcausalprecedents();
		for (int i = 0; i < s; i++) {
			Vector localConpletePreceder = new Vector();
			Vector localPartialPreceder = new Vector();
			Vector localconcurrent = new Vector();
			sumPreceeder[i] = 0;
			for (int j = 0; j < s; j++) {
				if (finalcausalprecedents.get(i, j) == 1) {
					localConpletePreceder.add(new Integer(j));
					// sumPreceeder[i]=sumPreceeder[i]+1;
				} else {
					if (finalcausalprecedents.get(i, j) < 0) {
						localconcurrent.add(new Integer(j));
					} else {
						if (finalcausalprecedents.get(i, j) > 0) {
							localPartialPreceder.add(new Integer(j));
							sumPreceeder[i] = sumPreceeder[i]
									+ finalcausalprecedents.get(i, j);
						}
					}
				}
			}
			this.Concurrent[i] = localconcurrent;
			this.completePreceeder[i] = localConpletePreceder;
			this.partialPreceeder[i] = localPartialPreceder;

			this.nbconcurrent[i] = localconcurrent.size();
			this.nbcompletePreceeder[i] = localConpletePreceder.size();
			this.nbpartialPreceeder[i] = localPartialPreceder.size();

		}

		for (int i = 0; i < s; i++) {
			Vector localConpletefollowers = new Vector();
			Vector localPartialfollowers = new Vector();
			sumfollowers[i] = 0;
			for (int j = 0; j < s; j++) {
				if (finalcausalfollowers.get(i, j) == 1) {
					localConpletefollowers.add(new Integer(j));
					// sumfollowers[i]=sumfollowers[i]+1;
				} else {
					if (finalcausalfollowers.get(i, j) > 0) {
						localPartialfollowers.add(new Integer(j));
						sumfollowers[i] = sumfollowers[i]
								+ finalcausalfollowers.get(i, j);
					}
				}
			}
			this.completefollowers[i] = localConpletefollowers;
			this.partialfollowers[i] = localPartialfollowers;

			nbcompletefollowers[i] = localConpletefollowers.size();
			nbpartialfollowers[i] = localPartialfollowers.size();

		}
		start = DoubleFactory1D.sparse.make(s, 0);
		end = DoubleFactory1D.sparse.make(s, 0);

		for (int i = 0; i < s; i++) {
			getEndInfo().set(i, nbpartialfollowers[i] + nbcompletefollowers[i]);
			getStartInfo().set(i,
					nbcompletePreceeder[i] + nbpartialPreceeder[i]);
		}

	}

	public Vector[] getConcurrent() {
		return this.Concurrent;
	}

	public Vector[] getcompletePreceeder() {
		return this.completePreceeder;
	}

	public Vector[] getpartialPreceeder() {
		return this.partialPreceeder;
	}

	public Vector[] getcompletefollowers() {
		return this.completefollowers;
	}

	public Vector[] getpartialfollowers() {
		return this.partialfollowers;
	}

	public int[] getnbconcurrent() {
		return this.nbconcurrent;
	}

	public int[] getnbcompletePreceeder() {
		return this.nbcompletePreceeder;
	}

	public int[] getnbpartialPreceeder() {
		return this.nbpartialPreceeder;
	}

	public int[] getnbcompletefollowers() {
		return this.nbcompletefollowers;
	}

	public int[] getnbpartialfollowers() {
		return this.nbpartialfollowers;
	}

	public double[] getsumfollowers() {
		return this.sumfollowers;
	}

	public double[] getsumPreceeder() {
		return this.sumPreceeder;
	}

	public DoubleMatrix2D getdirectPrecedentsMatrix() {
		return directPrecedents;
	}

	public DoubleMatrix2D getdirectfollowersMatrix() {
		return directfollowers;
	}

	public DoubleMatrix1D getACWWidthMatrix() {
		return ACWWidth;
	}

	public DoubleMatrix2D getfinalfollowersMatrix() {
		return finalfollowers;
	}

	public DoubleMatrix2D getfinalcausalfollowers() {
		return finalcausalfollowers;
	}

	public DoubleMatrix2D getfinalcausalprecedents() {
		return finalcausalprecedents;
	}

	public DoubleMatrix2D getCausalFollowerMatrix() {
		return causal;
	}

	public DoubleMatrix2D getParallelMatrix() {
		return parallel;
	}

	public DoubleMatrix1D getEndInfo() {
		return end;
	}

	public DoubleMatrix1D getStartInfo() {
		return start;
	}

	public DoubleMatrix1D getOneLengthLoopsInfo() {
		return loops;
	}

	public LogEvents getLogEvents() {
		return events;
	}
}
