package org.processmining.mining.semanticorganizationmining;

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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiPropertyListRadio;
import org.processmining.framework.util.GUIPropertyBoolean;
import javax.swing.JLabel;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class SemanticOrgMinerOptions extends JPanel implements
		GuiNotificationTarget {

	public final static int DEFAULT_MINING = 0;
	public final static int WORKING_TOGETHER = 2000;
	public final static int SIMILAR_TASK = 3000;
	public final static int CORRESPONDENCE_ANALYSIS = 4000;

	// constants for 'similar task' setting
	public final static int EUCLIDIAN_DISTANCE = 0;
	public final static int CORRELATION_COEFFICIENT = 1;
	public final static int SIMILARITY_COEFFICIENT = 2;
	public final static int HAMMING_DISTANCE = 3;

	// constants for 'working together' setting
	public final static int SIMULTANEOUS_APPEARANCE_RATIO = 0;
	public final static int DISTANCE_WITHOUT_CAUSALITY = 1;
	public final static int DISTANCE_WITH_CAUSALITY = 2;

	private GuiPropertyListRadio doingSimilarTask;
	private GuiPropertyListRadio options;
	// private GuiPropertyListRadio workingTogether;
	private GUIPropertyBoolean bSuperTask;
	private GUIPropertyBoolean bSubTask;
	private GUIPropertyBoolean bSuperOri;
	private GUIPropertyBoolean bSubOri;
	private GridBagLayout gridBagLayout4 = new GridBagLayout();

	private void init() {

		ArrayList<String> values = new ArrayList<String>();
		values.add("Default Mining");
		values.add("Doing Similar Task (DST)");
		// values.add("Working Together (WT)");
		options = new GuiPropertyListRadio("Method for OM", values, this);

		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("Euclidian distance");
		values2.add("Correlation coefficient");
		values2.add("Similarity coefficient");
		values2.add("Hamming distance");
		doingSimilarTask = new GuiPropertyListRadio("Options for DST", values2);

		JLabel labelExtraOptions = new JLabel(
				"While mining the organizational groups also consider:");

		bSuperTask = new GUIPropertyBoolean("Super concepts for tasks", false);
		bSubTask = new GUIPropertyBoolean("Sub concepts for tasks", false);
		bSuperOri = new GUIPropertyBoolean("Super concepts for originators",
				false);
		bSubOri = new GUIPropertyBoolean("Sub concepts for originators", false);

		// //
		/*
		 * ArrayList<String> values3 = new ArrayList<String>();
		 * values3.add("Simultaneous appearance ratio");
		 * values3.add("Consider distance with causality");
		 * values3.add("Consider distance without causality (beta=0.5)");
		 * 
		 * 
		 * workingTogether = new GuiPropertyListRadio("Options for WT",
		 * values3);
		 */
		for (int i = 0; i < values2.size(); i++) {
			doingSimilarTask.disable(values2.get(i));
		}

		/*
		 * for(int i=0;i<values3.size();i++)
		 * workingTogether.disable(values3.get(i));
		 */
		this.setLayout(gridBagLayout4);
		this.add(options.getPropertyPanel(), new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(doingSimilarTask.getPropertyPanel(), new GridBagConstraints(0,
				4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		// this.add(workingTogether.getPropertyPanel(), new
		// GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
		// , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0,
		// 0, 0), 0, 0));
		this.add(labelExtraOptions, new GridBagConstraints(0, 10, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(bSuperTask.getPropertyPanel(), new GridBagConstraints(0, 11,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(bSubTask.getPropertyPanel(), new GridBagConstraints(0, 12, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(bSuperOri.getPropertyPanel(), new GridBagConstraints(0, 13, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(bSubOri.getPropertyPanel(), new GridBagConstraints(0, 14, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public int getSimilarTaskSetting() {
		String st = doingSimilarTask.getValue().toString();
		if (st.equals("Euclidian distance")) {
			return EUCLIDIAN_DISTANCE;
		} else if (st.equals("Correlation coefficient")) {
			return CORRELATION_COEFFICIENT;
		} else if (st.equals("Similarity coefficient")) {
			return SIMILARITY_COEFFICIENT;
		} else {
			return HAMMING_DISTANCE;
		}
	}

	/*
	 * public int getWorkingTogetherSetting() { String st =
	 * workingTogether.getValue().toString(); if
	 * (st.equals("Simultaneous appearance ratio"))return
	 * SIMULTANEOUS_APPEARANCE_RATIO; else if
	 * (st.equals("Consider distance with causality"))return
	 * DISTANCE_WITH_CAUSALITY; else return DISTANCE_WITHOUT_CAUSALITY; }
	 */
	public int getSelectedMetrics() {
		int nIndex = 0;
		if (options.getValue().equals("Default Mining")) {
			nIndex = DEFAULT_MINING;
		} else if (options.getValue().equals("Doing Similar Task (DST)")) {
			nIndex = SIMILAR_TASK;
		} else {
			nIndex = WORKING_TOGETHER;
		}
		return nIndex;
	}

	public boolean isSuperTask() {
		return bSuperTask.getValue();
	}

	public boolean isSubTask() {
		return bSubTask.getValue();
	}

	public boolean isSuperOri() {
		return bSuperOri.getValue();
	}

	public boolean isSubOri() {
		return bSubOri.getValue();
	}

	public SemanticOrgMinerOptions() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		init();
	}

	private void jbInit() throws Exception {

	}

	public void updateGUI() {
		List<String> values = doingSimilarTask.getAllValues();
		if (options.getValue().equals("Doing Similar Task (DST)")) {
			for (int i = values.size() - 1; i >= 0; i--) {
				doingSimilarTask.enable((String) values.get(i));
			}
		} else {
			for (int i = values.size() - 1; i >= 0; i--) {
				doingSimilarTask.disable((String) values.get(i));
			}
		}

		doingSimilarTask.setSelected("Euclidian distance", true);
		/*
		 * values = workingTogether.getAllValues();
		 * if(options.getValue().equals("Working Together (WT)")) for(int
		 * i=values.size()-1;i>=0;i--)
		 * workingTogether.enable((String)values.get(i)); else for(int
		 * i=values.size()-1;i>=0;i--)
		 * workingTogether.disable((String)values.get(i));
		 */
	}
}
