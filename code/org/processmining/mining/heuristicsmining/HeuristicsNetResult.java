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

package org.processmining.mining.heuristicsmining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class HeuristicsNetResult implements MiningResult, Provider {

	protected LogReader log;
	protected HeuristicsNet net;

	private JSplitPane splitter = null;
	private JPanel graphPanel = null;
	private JPanel descriptionPanel = null;
	private boolean showSplitJoinSemantics = false;
	private JCheckBoxMenuItem checkMenu;

	private GrappaAdapter grappaAdapter = new GrappaAdapter() {

		/**
		 * The method is called when a mouse press occurs on a displayed
		 * subgraph. The returned menu is added to the end of the default
		 * right-click menu
		 * 
		 * @param subg
		 *            displayed subgraph where action occurred
		 * @param elem
		 *            subgraph element in which action occurred
		 * @param pt
		 *            the point where the action occurred (graph coordinates)
		 * @param modifiers
		 *            mouse modifiers in effect
		 * @param panel
		 *            specific panel where the action occurred
		 */
		protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
				GrappaPoint pt, int modifiers, GrappaPanel panel) {
			return checkMenu;
		}
	};

	public HeuristicsNetResult(HeuristicsNet net, LogReader log,
			boolean showSplitJoinSemantics) {
		this.net = net;
		this.log = log;
		this.showSplitJoinSemantics = showSplitJoinSemantics;
	}

	public boolean getShowSplitJoinSemantics() {
		return showSplitJoinSemantics;
	}

	public HeuristicsNetResult(HeuristicsNet net, LogReader log) {
		this(net, log, false);
	}

	public JComponent getVisualization() {
		buildPanels();
		showIndividual();

		return splitter;

	}

	public LogReader getLogReader() {
		return log;
	}

	public HeuristicsNet getHeuriticsNet() {
		return net;
	}

	public ProvidedObject[] getProvidedObjects() {
		if (log == null) {
			return new ProvidedObject[] { new ProvidedObject("Heuristics net",
					new Object[] { net }) };

		} else {
			return new ProvidedObject[] { new ProvidedObject("Heuristics net",
					new Object[] { net, log }) };
		}
	}

	private void buildPanels() {

		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setBackground(Color.WHITE);

		descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.setBackground(Color.WHITE);

		splitter = new JSplitPane();
		splitter.setContinuousLayout(true);
		splitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitter.setTopComponent(graphPanel);
		splitter.setBottomComponent(descriptionPanel);
		splitter.setOneTouchExpandable(true);
		splitter.setResizeWeight(1.0);

		checkMenu = new JCheckBoxMenuItem("Display split/join semantics");
		checkMenu.setSelected(showSplitJoinSemantics);

		checkMenu.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				showSplitJoinSemantics = (e.getStateChange() == e.SELECTED);
				showIndividual();
			}
		});
	}

	private void showIndividual() {

		JScrollPane scrollPane = null;
		JTextArea text = null;
		ModelGraphPanel gp = null;

		// graph representation
		if (this.showSplitJoinSemantics) {
			gp = net.getGrappaVisualizationWithSplitJoinSemantics();

		} else {
			gp = net.getGrappaVisualization();
		}
		gp.addGrappaListener(grappaAdapter);

		scrollPane = new JScrollPane(gp);
		graphPanel.removeAll();
		graphPanel.add(scrollPane, BorderLayout.CENTER);
		// internal description

		// scrollPane = new JScrollPane();
		text = new JTextArea(net.toStringWithEvents(), 15, 40);
		text.setEditable(false);

		descriptionPanel.removeAll();
		descriptionPanel.add(new JScrollPane(text), BorderLayout.CENTER);

		graphPanel.validate();
		graphPanel.repaint();

		descriptionPanel.validate();
		descriptionPanel.repaint();
	}

}
