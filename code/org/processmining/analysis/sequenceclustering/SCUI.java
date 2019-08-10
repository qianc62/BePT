package org.processmining.analysis.sequenceclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.framework.util.StopWatch;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class SCUI extends JPanel implements Provider {

	protected SCAlgorithm scAlgorithm;
	protected MarkovChain markovChain;

	protected SlickerOpenLogSettings parent;
	protected LogReader log = null;
	protected LogReader originalLog = null;
	protected JComponent view;

	protected int numClusters;
	protected double minEventSupport, maxEventSupport;
	protected boolean preprocessed;

	protected ProgressPanel progressPanel;
	protected FlatTabbedPane tabPane;
	protected ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	public SCUI(LogReader log, LogReader originalLog, double minEventSupport,
			double maxEventSupport, int numberClusters, boolean preprocessed) {
		this.setBackground(new Color(40, 40, 40));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.numClusters = numberClusters;
		this.log = log;
		this.originalLog = originalLog;
		this.minEventSupport = minEventSupport;
		this.maxEventSupport = maxEventSupport;
		this.preprocessed = preprocessed;

		startCalculating();

	}

	protected void startCalculating() {
		this.removeAll();

		progressPanel = new ProgressPanel("Clustering");
		progressPanel.setMinMax(0, 6);
		progressPanel.setProgress(0);
		progressPanel.setNote("Initialization");
		this.add(progressPanel.getPanel(), BorderLayout.CENTER);

		Thread constructionThread = new Thread() {
			public void run() {
				StopWatch watch = new StopWatch();
				watch.start();
				construct();
				watch.stop();
			}
		};
		constructionThread.start();
	}

	/**
	 * Invokes the sequence clustering algorithm and prepares the display of the
	 * results
	 */
	protected void construct() {

		tabPane = new FlatTabbedPane("Sequence Clustering", new Color(200, 200,
				200), new Color(100, 100, 100), new Color(100, 100, 100));
		scAlgorithm = new SCAlgorithm(log, originalLog, minEventSupport,
				maxEventSupport, numClusters, progressPanel, preprocessed);

		for (int i = 0; i < scAlgorithm.clusterList.size(); i++) {
			clusters.add(scAlgorithm.clusterList.get(i));
		}

		markovChain = new MarkovChain(log, numClusters, scAlgorithm);

		tabPane.addTab("Cluster Inspector", scAlgorithm);
		tabPane.addTab("Markov Chain", markovChain);

		this.removeAll();
		this.add(tabPane);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[numClusters + numClusters
				+ 1];
		int index = 0;

		for (Cluster cluster : clusters) {
			// add complete log
			objects[index] = objects[index] = cluster.getProvidedObject();
			index++;
			objects[index] = objects[index] = cluster.getProvidedObjectMarkov();
			index++;
		}
		objects[index] = new ProvidedObject(
				"Preprocessed for Sequence Clustering", new Object[] { log });
		return objects;
	}

}
