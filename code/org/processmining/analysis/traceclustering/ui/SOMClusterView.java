package org.processmining.analysis.traceclustering.ui;

import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.traceclustering.algorithm.ClusteringInput;
import org.processmining.analysis.traceclustering.model.ClusterSet;

/**
 * 
 * @author Minseok Song
 * 
 */
public class SOMClusterView extends SlickTabbedPane {

	protected ClusterSet clusters;
	protected ClusteringInput input;

	public SOMClusterView(ClusterSet clusters, ClusteringInput input,
			int colNumber, int rowNumber, int aScatteredRatio,
			String aBackgroundStyle, String aColorStyle, double[][] uMatrix) {
		this.clusters = clusters;
		this.input = input;
		this.addTab("Self Organizing Map", new SOMUI(clusters, colNumber,
				rowNumber, aScatteredRatio, aBackgroundStyle, aColorStyle,
				uMatrix));
		// this.addTab("Comparison matrix", new DisjointClusterSetUI(clusters,
		// input));
		this.addTab("Cluster inspector", new ClusterSetView(clusters));
	}

}