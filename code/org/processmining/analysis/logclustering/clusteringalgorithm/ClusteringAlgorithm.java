package org.processmining.analysis.logclustering.clusteringalgorithm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.processmining.analysis.logclustering.LogCluster;
import org.processmining.analysis.logclustering.LogClusterSet;
import org.processmining.analysis.logclustering.distancemeasure.CorrelationCoefficientDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.DistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.EuclidianDistanceMeasure;
import org.processmining.analysis.logclustering.distancemeasure.HammingDistanceMeasure;
import org.processmining.analysis.logclustering.profiles.AggregateProfiles;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.util.GUIPropertyListEnumeration;

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
public abstract class ClusteringAlgorithm implements DotFileWriter {
	protected String name;
	protected GUIPropertyListEnumeration distanceMeasures;

	protected ArrayList<Integer> traceList;
	protected AggregateProfiles agProfiles;
	protected LogClusterSet clusterSet;

	public ClusteringAlgorithm() {

	}

	public ClusteringAlgorithm(String aName) {
		name = aName;

		// distance measure
		ArrayList<DistanceMeasure> values = new ArrayList<DistanceMeasure>();
		values.add(new CorrelationCoefficientDistanceMeasure());
		values.add(new EuclidianDistanceMeasure());
		values.add(new HammingDistanceMeasure());

		distanceMeasures = new GUIPropertyListEnumeration("Distance Measure",
				values);
	}

	public void setAggregateProfiles(AggregateProfiles aAggregateProfiles) {
		agProfiles = aAggregateProfiles;
	}

	public AggregateProfiles getAggregateProfiles() {
		return agProfiles;
	}

	public abstract void build();

	public abstract JPanel getPanel();

	public abstract int size();

	public abstract List<LogCluster> getClusters();

	public JPanel getOptionPanel() {
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBorder(BorderFactory.createLineBorder(new Color(150,
				150, 150), 1));
		optionsPanel.add(distanceMeasures.getPropertyPanel());
		return optionsPanel;
	}

	public DistanceMeasure getDistanceMeasure() {
		return (DistanceMeasure) distanceMeasures.getValue();
	}

	public String toString() {
		return name;
	}

}
