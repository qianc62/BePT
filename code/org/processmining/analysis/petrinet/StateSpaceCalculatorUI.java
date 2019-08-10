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

package org.processmining.analysis.petrinet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.RegionList;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.framework.ui.Message;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;
import org.processmining.framework.models.fsm.FSM;

/**
 * @author not attributable
 * @version 1.0
 */

public class StateSpaceCalculatorUI extends JPanel implements Provider {

	private FSM statespace;
	private JButton updateGraphButton = new JButton("Show Region");
	private JPanel tablePanel = new JPanel(new BorderLayout());
	private DoubleClickTable table;
	private JSplitPane splitPane;
	private ModelGraphPanel graphPanel;
	private RegionList minimalRegions;
	private RegionList allRegions = null;

	public StateSpaceCalculatorUI(FSM statespace) {
		this.statespace = statespace;
		try {
			minimalRegions = statespace.calculateMinimalRegions(statespace
					.getEdgeObjects());
			Message.add("<MinimalRegionsFound " + minimalRegions.size() + ">",
					Message.TEST);
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateTable(ArrayList items) {
		table.setModel(new ArrayListTableModel(items));
		tablePanel.invalidate();
		splitPane.invalidate();
		tablePanel.repaint();
		graphPanel.unSelectAll();
	}

	void jbInit() throws Exception {
		JButton showAllRegions = new JButton("get all regions");
		JButton showMinimalRegions = new JButton("get minimal regions");

		showMinimalRegions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable(minimalRegions);
				Message.add("<MinimalRegionsFound " + minimalRegions.size()
						+ ">", Message.TEST);
			}
		});

		showAllRegions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (allRegions == null) {
					allRegions = statespace.calculateAllRegions(statespace
							.getEdgeObjects());
				}
				updateTable(allRegions);
				Message.add("<AllRegionsFound " + minimalRegions.size() + ">",
						Message.TEST);
			}
		});

		table = new DoubleClickTable(new ArrayListTableModel(minimalRegions),
				updateGraphButton);

		graphPanel = statespace.getGrappaVisualization();
		graphPanel.addGrappaListener(new GrappaAdapter() {
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
			 *            the point where the action occurred (graph
			 *            coordinates)
			 * @param modifiers
			 *            mouse modifiers in effect
			 * @param panel
			 *            specific panel where the action occurred
			 */
			protected JMenuItem getCustomMenu(Subgraph subg, Element elem,
					GrappaPoint pt, int modifiers, GrappaPanel panel) {
				JMenuItem menu = null;

				if ((elem != null)
						&& (elem.object instanceof ModelGraphEdge)
						&& (((ModelGraphEdge) elem.object).object != null)
						&& (((ModelGraphEdge) elem.object).object instanceof Transition)) {
					menu = new JMenu("Select regions");
					final Transition t = (Transition) ((ModelGraphEdge) elem.object).object;
					JMenuItem preRegionMenuItem = new JMenuItem(
							"Show pre-regions of " + t.toString());
					preRegionMenuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateTable(statespace.getPreRegions(
									minimalRegions, t));
						}
					});
					JMenuItem postRegionMenuItem = new JMenuItem(
							"Show post-regions of " + t.toString());
					postRegionMenuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							updateTable(statespace.getPostRegions(
									minimalRegions, t));
						}
					});
					menu.add(preRegionMenuItem);
					menu.add(postRegionMenuItem);
				}
				return menu;
			}

		});

		updateGraphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HashSet region = (HashSet) table.getValueAt(table
						.getSelectedRow(), table.getSelectedColumn());

				graphPanel.unSelectAll();
				graphPanel.selectElements(region);
			}
		});

		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
		buttonPanel.add(updateGraphButton);
		buttonPanel.add(showMinimalRegions);
		buttonPanel.add(showAllRegions);

		tablePanel.add(buttonPanel, BorderLayout.SOUTH);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel,
				graphPanel);
		splitPane.setDividerLocation(150);
		splitPane.setOneTouchExpandable(true);

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList objects = new ArrayList();
		if (minimalRegions != null) {
			objects.add(new ProvidedObject("Statespace with minimal regions",
					new Object[] { statespace, minimalRegions }));
		}
		if (allRegions != null) {
			objects.add(new ProvidedObject("Statespace with all regions",
					new Object[] { statespace, allRegions }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

}

class ArrayListTableModel extends AbstractTableModel {

	private ArrayList data;

	public ArrayListTableModel(ArrayList data) {
		this.data = data;
	}

	public String getColumnName(int col) {
		return "<html>Minimal<br>Region</html>";
	}

	public int getRowCount() {
		return data.size();
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int row, int column) {
		return data.get(row);
	}
}
