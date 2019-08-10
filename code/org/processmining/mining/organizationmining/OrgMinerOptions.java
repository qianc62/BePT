package org.processmining.mining.organizationmining;

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.mining.organizationmining.distance.CorrelationCoefficientDistance;
import org.processmining.mining.organizationmining.distance.DistanceMetric;
import org.processmining.mining.organizationmining.distance.EuclideanDistance;
import org.processmining.mining.organizationmining.distance.HammingDistance;
import org.processmining.mining.organizationmining.distance.JaccardIndexDistance;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgMinerOptions extends JPanel {

	public final static int DEFAULT_MINING = 0;
	public final static int AHC_MINING = 1000;
	public final static int WORKING_TOGETHER = 2000;
	public final static int SIMILAR_TASK = 3000;
	public final static int SOM_MINING = 4000;
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	private JRadioButton jrbDefaultMining;
	private JRadioButton jrbDoingSimilarTask;
	private JRadioButton jrbWorkingTogether;
	private JRadioButton jrbHierarchicalMining;
	private JRadioButton jrbSOMMining;

	// constants for 'similar task' setting
	public final static int EUCLIDIAN_DISTANCE = 0;
	public final static int CORRELATION_COEFFICIENT = 1;
	public final static int SIMILARITY_COEFFICIENT = 2;
	public final static int HAMMING_DISTANCE = 3;

	// constants for 'working together' setting
	public final static int SIMULTANEOUS_APPEARANCE_RATIO = 0;
	public final static int DISTANCE_WITHOUT_CAUSALITY = 1;
	public final static int DISTANCE_WITH_CAUSALITY = 2;

	private ButtonGroup group;
	private GUIPropertyListEnumeration doingSimilarTask;
	private GUIPropertyListEnumeration workingTogether;
	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	protected List<DistanceMetric> distanceMetrics;
	protected JComboBox distanceMetricsBox;
	protected JTextArea metricDescription;

	private void init() {

		// default mining
		jrbDefaultMining = new JRadioButton("Default Mining");
		jrbDefaultMining.setSelected(true);
		JPanel content1 = new JPanel();
		content1.setLayout(new BoxLayout(content1, BoxLayout.LINE_AXIS));
		content1.add(jrbDefaultMining);

		// doing similar task
		jrbDoingSimilarTask = new JRadioButton("Doing Similar Task (DST)");
		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("Correlation coefficient");
		values2.add("Euclidian distance");
		values2.add("Similarity coefficient");
		values2.add("Hamming distance");
		doingSimilarTask = new GUIPropertyListEnumeration(
				" : Options for DST - ", null, values2, null, 260);
		JPanel content2 = new JPanel();
		content2.setLayout(new BoxLayout(content2, BoxLayout.LINE_AXIS));
		content2.add(jrbDoingSimilarTask);
		content2.add(doingSimilarTask.getPropertyPanel());

		// working together
		jrbWorkingTogether = new JRadioButton("Working Together (WT)");
		ArrayList<String> values3 = new ArrayList<String>();
		values3.add("Simultaneous appearance ratio");
		values3.add("Consider distance with causality");
		values3.add("Consider distance without causality (beta=0.5)");
		workingTogether = new GUIPropertyListEnumeration(
				"   : Options for WT  - ", null, values3, null, 260);
		JPanel content3 = new JPanel();
		content3.setLayout(new BoxLayout(content3, BoxLayout.LINE_AXIS));
		content3.add(jrbWorkingTogether);
		content3.add(workingTogether.getPropertyPanel());

		// Hierarchical mining
		jrbHierarchicalMining = new JRadioButton("Hierarchical Mining (AHC)");
		JPanel content4 = new JPanel();
		content4.setLayout(new BoxLayout(content4, BoxLayout.LINE_AXIS));
		content4.add(jrbHierarchicalMining);

		// Hierarchical mining
		jrbSOMMining = new JRadioButton("Self Organizing Map (SOM) Mining ");
		JPanel content5 = new JPanel();
		content5.setLayout(new BoxLayout(content5, BoxLayout.LINE_AXIS));
		content5.add(jrbSOMMining);

		JPanel content6 = new JPanel();
		content6.setLayout(new BoxLayout(content6, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel("    * Distance measure for AHC/SOM - ");
		distanceMetrics = new ArrayList<DistanceMetric>();
		distanceMetrics.add(new EuclideanDistance());
		distanceMetrics.add(new JaccardIndexDistance());
		distanceMetrics.add(new HammingDistance());
		distanceMetrics.add(new CorrelationCoefficientDistance());
		distanceMetricsBox = new JComboBox(distanceMetrics.toArray());
		content6.add(myNameLabel);
		content6.add(distanceMetricsBox);

		group = new ButtonGroup();
		group.add(jrbDefaultMining);
		group.add(jrbDoingSimilarTask);
		group.add(jrbWorkingTogether);
		group.add(jrbHierarchicalMining);
		group.add(jrbSOMMining);

		this.setLayout(gridBagLayout4);
		this.add(content1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(content2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(content3, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(content4, new GridBagConstraints(0, 16, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(content5, new GridBagConstraints(0, 22, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
		this.add(content6, new GridBagConstraints(0, 28, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));

	}

	public int getSimilarTaskSetting() {
		String st = doingSimilarTask.getValue().toString();
		if (st.equals("Euclidian distance"))
			return OrgMinerOptions.EUCLIDIAN_DISTANCE;
		else if (st.equals("Correlation coefficient"))
			return OrgMinerOptions.CORRELATION_COEFFICIENT;
		else if (st.equals("Similarity coefficient"))
			return OrgMinerOptions.SIMILARITY_COEFFICIENT;
		else
			return OrgMinerOptions.HAMMING_DISTANCE;
	}

	public int getWorkingTogetherSetting() {
		String st = workingTogether.getValue().toString();
		if (st.equals("Simultaneous appearance ratio"))
			return OrgMinerOptions.SIMULTANEOUS_APPEARANCE_RATIO;
		else if (st.equals("Consider distance with causality"))
			return OrgMinerOptions.DISTANCE_WITH_CAUSALITY;
		else
			return OrgMinerOptions.DISTANCE_WITHOUT_CAUSALITY;
	}

	public DistanceMetric getDistanceMetrics() {
		return (DistanceMetric) distanceMetricsBox.getSelectedItem();
	}

	public int getSelectedMetrics() {
		int nIndex = 0;
		if (group.getSelection() == jrbDefaultMining.getModel())
			nIndex = OrgMinerOptions.DEFAULT_MINING;
		else if (group.getSelection() == jrbHierarchicalMining.getModel())
			nIndex = OrgMinerOptions.AHC_MINING;
		else if (group.getSelection() == jrbDoingSimilarTask.getModel())
			nIndex = OrgMinerOptions.SIMILAR_TASK;
		else if (group.getSelection() == jrbSOMMining.getModel())
			nIndex = OrgMinerOptions.SOM_MINING;
		else
			nIndex = OrgMinerOptions.WORKING_TOGETHER;
		return nIndex;
	}

	public OrgMinerOptions() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		init();
	}

	private void jbInit() throws Exception {

	}
}
