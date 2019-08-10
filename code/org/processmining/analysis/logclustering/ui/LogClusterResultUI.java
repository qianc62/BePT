package org.processmining.analysis.logclustering.ui;

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.analysis.logclustering.LogCluster;
import org.processmining.analysis.logclustering.clusteringalgorithm.ClusteringAlgorithm;
import org.processmining.analysis.logclustering.model.AHCTreeNode;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

/**
 * @author Minseok Song
 * @version 1.0
 */
public class LogClusterResultUI extends JPanel implements Provider {

	private LogReader log;
	private LogSummary summary;

	protected JScrollPane metricsScrollPane;
	protected JPanel metricsList;
	protected ClusteringAlgorithm clusteringAlgorithm;

	// log profiles
	// private AggregateProfiles agProfiles;

	public LogClusterResultUI(LogReader log,
			ClusteringAlgorithm aClusteringAlgorithm) {
		this.log = log;
		this.clusteringAlgorithm = aClusteringAlgorithm;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {
		clusteringAlgorithm.build();
		this.setLayout(new BorderLayout());
		this.add(clusteringAlgorithm.getPanel(), BorderLayout.CENTER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		// add graph in dot format
		ProvidedObject[] objects = new ProvidedObject[clusteringAlgorithm
				.size() + 2];
		objects[0] = new ProvidedObject("Trace cluster graph",
				new Object[] { new DotFileWriter() {
					public void writeToDot(Writer bw) throws IOException {
						clusteringAlgorithm.writeToDot(bw);
					}
				} });
		int index = 1;
		// add all currently displayed clusters
		for (LogCluster cluster : clusteringAlgorithm.getClusters()) {
			if (cluster.size() == 0)
				continue;
			int pitk[] = new int[cluster.size()];
			int piIndex = 0;
			for (Integer stat : cluster.getTraces()) {
				pitk[piIndex] = stat.intValue();
				piIndex++;
			}
			try {
				if (cluster instanceof AHCTreeNode) {
					objects[index] = new ProvidedObject("Trace cluster "
							+ ((AHCTreeNode) cluster).getName(),
							new Object[] { LogReaderFactory.createInstance(log,
									pitk) });
				} else {
					objects[index] = new ProvidedObject("Trace cluster "
							+ (index - 1), new Object[] { LogReaderFactory
							.createInstance(log, pitk) });

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			index++;
		}
		// add complete log
		objects[index] = new ProvidedObject("Complete log (all clusters)",
				new Object[] { log });
		return objects;
	}
}
