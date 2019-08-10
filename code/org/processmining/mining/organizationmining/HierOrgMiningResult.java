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

package org.processmining.mining.organizationmining;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.activitygraph.ActivityGraph;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.Perspective;
import org.processmining.framework.models.hlprocess.hlmodel.HLActivitySet;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;
import org.processmining.mining.organizationmining.algorithm.ClusteringAlgorithm;
import org.processmining.mining.organizationmining.ui.OrgMiningResultPanel;
import org.processmining.mining.organizationmining.ui.hierarchicalui.HierOrgModelGraph;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class HierOrgMiningResult extends OrgMiningResult {// implements
	// MiningResult,
	// Provider,
	// LogReaderConnection
	// {

	private ClusteringAlgorithm algorithm;
	private JTabbedPane tabbedPane;
	private HierOrgModelGraph orgModelGraph;

	/**
	 * If this plugin is used multiple times, each time the simulation model to
	 * be provided will have an incremented number (in order to distinguish them
	 * later when they e.g., need to be joined)
	 */
	private static int simulationModelCounter = 0;

	public HierOrgMiningResult(LogReader log, OrgModel orgModel,
			ClusteringAlgorithm algorithm) {
		super(log, orgModel);
		this.algorithm = algorithm;
		generateActivitySet();
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = {
				new ProvidedObject("Organization Model",
						new Object[] { algorithm.getOrgModel() }),
				new ProvidedObject("Organizational Simulation Model No."
						+ simulationModelCounter, new Object[] { actSet }),
				new ProvidedObject("Whole Log", new Object[] { log }), };
		return objects;
	}

	public JComponent getVisualization() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	// TODO some work
	private void jbInit() throws Exception {
		tabbedPane = new JTabbedPane();
		orgModelGraph = null;
		if (algorithm != null) {
			tabbedPane.add(algorithm.getUI(), algorithm.getName());
		}

		orgModelGraph = new HierOrgModelGraph(algorithm.getOrgModel());
		tabbedPane.add(orgModelGraph, "Org Model");

		tabbedPane.addChangeListener(new ChangeListener() {
			// This method is called whenever the selected tab changes
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane pane = (JTabbedPane) evt.getSource();
				// Get current tab
				int sel = pane.getSelectedIndex();
				if (sel == 1) {
					orgModel = algorithm.getOrgModel();
					tabbedPane.setComponentAt(1,
							new HierOrgModelGraph(orgModel));
					updateActivitySet();
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);
	}

}
