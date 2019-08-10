package org.processmining.analysis.sequenceclustering;

import java.io.IOException;
import java.io.Writer;

import java.util.ArrayList;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class Cluster {

	protected int ID, numLogEvents;
	protected double minEventSupport, maxEventSupport;
	public boolean changed;
	protected String name;
	protected int[][] absoluteOccurences;
	protected double[][] markovChain;
	protected double[][] auxMatrix;
	protected double[][] previousAuxMatrix;
	protected boolean preprocessed;
	protected LogReader log;
	protected LogReader logClustered;
	protected LogReader originalLog;

	protected ArrayList<String> instancesToKeep;
	protected ArrayList<Integer> instancesToKeepId = new ArrayList<Integer>();
	protected ArrayList<Integer> previousInstancesToKeepId;
	protected ArrayList<LogEvent> events = new ArrayList<LogEvent>();
	protected ArrayList<String> instancesTypesName = new ArrayList<String>();
	protected ArrayList<Integer> instancesTypesRepresentative = new ArrayList<Integer>();

	protected SCLogFilter filter = null;

	protected ClusterModel clusterModel;
	protected SCAlgorithm scAlg;

	public Cluster(int index, int numberEvents, LogReader log,
			LogReader originalLog, SCAlgorithm sc, double minEventSupport,
			double maxEventSupport, boolean preprocessed) {
		this.absoluteOccurences = new int[numberEvents + 2][numberEvents + 2];
		this.markovChain = new double[numberEvents + 2][numberEvents + 2];
		this.name = "Cluster " + index;
		this.numLogEvents = numberEvents;
		this.ID = index;
		this.log = log;
		this.logClustered = log;
		this.originalLog = originalLog;
		this.changed = true;
		this.minEventSupport = minEventSupport;
		this.maxEventSupport = maxEventSupport;
		this.preprocessed = preprocessed;
		this.scAlg = sc;

		initializeInstances();
		initializeMarkovChain();

	}

	public void initializeMarkovChain() {

		for (int i = 0; i < numLogEvents + 2; i++) {
			for (int j = 0; j < numLogEvents + 2; j++) {
				markovChain[i][j] = 0.0;
				absoluteOccurences[i][j] = 0;
			}
		}
	}

	public void initializeInstances() {
		previousInstancesToKeepId = instancesToKeepId;

		instancesToKeep = new ArrayList<String>();
		instancesToKeepId = new ArrayList<Integer>();
	}

	protected double round(double number, int decimals) {

		double factor = Math.pow(10, decimals);

		return Math.round(number * factor) / factor;

	}

	public void buildCluster() {
		double eventOccurence;

		for (int i = 0; i < originalLog.getLogSummary().getLogEvents().size(); i++) {
			eventOccurence = round(
					((((double) (originalLog.getLogSummary().getLogEvents()
							.get(i).getOccurrenceCount())) / (originalLog
							.getLogSummary().getNumberOfAuditTrailEntries())) * 100),
					3);

			if (eventOccurence < minEventSupport
					|| eventOccurence > maxEventSupport)
				events.add(originalLog.getLogSummary().getLogEvents().get(i));
		}

		filter = new SCLogFilter();

		if (preprocessed) {
			filter.setFlag(1);
			filter.setFilterEvents(events.toArray(new LogEvent[0]));
		} else {
			filter.setFlag(2);
		}

		filter.setFilterSequences(instancesToKeep.toArray(new String[0]));

		try {
			logClustered = LogReaderFactory.createInstance(filter, log);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void buildInstancesTypes() {
		boolean exists = false;
		ArrayList<String> sequences = new ArrayList<String>();
		ArrayList<Integer> typesOccurrences = new ArrayList<Integer>();
		ArrayList<String> typesInstancesList = new ArrayList<String>();
		ArrayList<String> instancesTypesString = new ArrayList<String>();

		if (this.getLog().getInstances().size() > 0) {

			for (int i = 0; i < this.getLog().getInstances().size(); i++) {
				sequences.add(i, "");
			}

			// build sequences

			for (int i = 0; i < this.getLog().getInstances().size(); i++) {
				for (int j = 0; j < this.getLog().getInstances().get(i)
						.getAuditTrailEntryList().size(); j++) {
					try {
						sequences.set(i, sequences.get(i).concat(
								this.getLog().getInstances().get(i)
										.getAuditTrailEntryList().get(j)
										.getElement()));
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			for (int i = 0; i < this.getLog().getInstances().size(); i++) {
				for (int j = 0; j < instancesTypesString.size(); j++) {
					if (sequences.get(i).equals(instancesTypesString.get(j))) {
						exists = true;
						typesOccurrences.set(j, typesOccurrences.get(j) + 1);
						typesInstancesList
								.set(j, typesInstancesList.get(j)
										+ ", "
										+ this.getLog().getInstances().get(i)
												.getName());
					}
				}
				if (!exists) {
					instancesTypesString.add(sequences.get(i));

					instancesTypesRepresentative.add(i);
					instancesTypesName.add("");

					typesOccurrences.add(1);
					typesInstancesList.add(""
							+ this.getLog().getInstances().get(i).getName());
				}
				exists = false;
			}

			// Assign names

			for (int i = 0; i < instancesTypesString.size(); i++) {
				if (typesOccurrences.get(i) > 1)
					instancesTypesName.set(i, typesOccurrences.get(i)
							+ " Instances: " + typesInstancesList.get(i));
				else
					instancesTypesName.set(i, typesOccurrences.get(i)
							+ " Instance: " + typesInstancesList.get(i));
			}

		}

		// creates the Markov chain with default values for the thresholds
		clusterModel = new ClusterModel(ID, scAlg);
	}

	public boolean compareInstances() {
		if (instancesToKeepId.equals(previousInstancesToKeepId))
			return true;

		return false;
	}

	public String getName() {
		return this.name;
	}

	public LogReader getLog() {
		return this.logClustered;
	}

	/**
	 * Determines whether a node can be reached by a transition
	 * 
	 * @param index
	 * @return
	 */
	public boolean isReachable(int index) {

		for (int i = 0; i < numLogEvents + 1; i++) {
			if (auxMatrix[i][index] != 0.0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes a node from the graph
	 * 
	 * @param index
	 */
	public void removeNode(int index) {

		for (int i = 0; i < numLogEvents + 2; i++) {
			auxMatrix[i][index] = 0.0;
			auxMatrix[index][i] = 0.0;
		}
	}

	public void buildMatrix() {
		for (int i = 0; i < numLogEvents + 2; i++) {
			for (int j = 0; j < numLogEvents + 2; j++) {
				auxMatrix[i][j] = markovChain[i][j];
				previousAuxMatrix[i][j] = markovChain[i][j];
			}
		}
	}

	public void copyMatrix() {
		for (int i = 0; i < numLogEvents + 2; i++) {
			for (int j = 0; j < numLogEvents + 2; j++) {
				previousAuxMatrix[i][j] = auxMatrix[i][j];
			}
		}
	}

	public boolean auxMatrixChanged() {

		for (int i = 0; i < numLogEvents + 2; i++) {
			for (int j = 0; j < numLogEvents + 2; j++) {
				if (auxMatrix[i][j] != previousAuxMatrix[i][j]) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Recalculates the matrix that represents the Markov chain, according to
	 * the thresholds
	 * 
	 * @param nodeSignificance
	 * @param edgeSignificance
	 * @param nodeInvertedSignificance
	 * @param edgeInvertedSignificance
	 */
	public void recalculateMatrix(double nodeSignificance,
			double edgeSignificance, double nodeInvertedSignificance,
			double edgeInvertedSignificance) {
		int iteration = 0;

		// remove edges below minimum threshold or above maximum threshold
		for (int i = 0; i < numLogEvents + 2; i++) {
			for (int j = 0; j < numLogEvents + 2; j++) {
				if (auxMatrix[i][j] < edgeSignificance
						|| auxMatrix[i][j] > edgeInvertedSignificance) {
					auxMatrix[i][j] = 0.0;
					previousAuxMatrix[i][j] = 0.0;
				}
			}
		}

		while (auxMatrixChanged() || iteration == 0) {
			iteration++;
			copyMatrix();

			// remove nodes below minimum threshold or above maximum threshold
			for (int i = 1; i < numLogEvents + 1; i++) {
				if ((!isReachable(i))
						|| ((log.getLogSummary().getInstancesForEvent(
								log.getLogSummary().getLogEvents().get(i - 1))
								.size() * 1.0 / log.numberOfInstances()) < nodeSignificance)
						|| ((log.getLogSummary().getInstancesForEvent(
								log.getLogSummary().getLogEvents().get(i - 1))
								.size() * 1.0 / log.numberOfInstances()) > nodeInvertedSignificance)) {
					removeNode(i);
				}
			}

		}

	}

	/**
	 * Writes the dot representing the Markov chain
	 * 
	 * @param bw
	 * @param nodeSignificance
	 * @param edgeSignificance
	 * @param nodeInvertedSignificance
	 * @param edgeInvertedSignificance
	 */
	public void writeToDot(Writer bw, double nodeSignificance,
			double edgeSignificance, double nodeInvertedSignificance,
			double edgeInvertedSignificance) {

		clusterModel = new ClusterModel(bw, ID, scAlg, nodeSignificance,
				edgeSignificance, nodeInvertedSignificance,
				edgeInvertedSignificance);

	}

	public ProvidedObject getProvidedObject() {
		return new ProvidedObject(name, new Object[] { getLog() });
	}

	public ProvidedObject getProvidedObjectMarkov() {
		return new ProvidedObject(name + " - Markov chain",
				new Object[] { clusterModel });
	}

}