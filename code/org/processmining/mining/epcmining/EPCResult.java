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

package org.processmining.mining.epcmining;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCHierarchy;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;

import javax.swing.JCheckBox;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class EPCResult implements LogReaderConnection, MiningResult, Provider {

	private EPCHierarchy hierarchy = new EPCHierarchy() {
		protected void selectionChanged(Object selectedObject) {
			if (center == null) {
				return;
			}
			if (selectedObject instanceof ModelGraph) {
				ConfigurableEPC epc = (ConfigurableEPC) selectedObject;
				epc.setShowObjects(adapter.showOrg(), adapter.showData(),
						adapter.showInfSys());
				ModelGraphPanel gp = epc.getGrappaVisualization();
				if (gp != null) {
					gp.addGrappaListener(adapter);
				}

				center.setViewportView(gp);
				mainPanel.validate();
				mainPanel.repaint();
			} else {
				JPanel p = new JPanel();
				p.add(new JLabel("Cannot visualize selection"));
				center.setViewportView(p);
				mainPanel.validate();
				mainPanel.repaint();
			}
		}
	};

	private LogReader log;
	private LogRelationBasedAlgorithm algorithm;
	private boolean showWarnings = true;

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JPanel buttonsPanel = new JPanel();
	private JScrollPane center;
	private JButton editLogRelationsButton = new JButton("Edit log relations");
	private EPCGrappaAdapter adapter = new EPCGrappaAdapter(this);

	public EPCResult(LogReader log, ConfigurableEPC epc) {
		this(log, epc, null);
	}

	public EPCResult(LogReader log, EPCHierarchy epcHierarchy) {
		this(log, null, null);
		this.hierarchy.copyFromHierarchy(epcHierarchy);
	}

	public EPCResult(LogReader log, ConfigurableEPC epc, MiningPlugin algorithm) {
		this.log = log;
		this.algorithm = (algorithm instanceof LogRelationBasedAlgorithm ? (LogRelationBasedAlgorithm) algorithm
				: null);
		if (log != null) {
			this.log.reset();
		}

		if (epc != null) {
			hierarchy.addHierarchyObject(epc, null);
			if (showWarnings) {
				String m = epc.isValidEPC();
				if (m.length() > 0) {
					showWarnings = showWarningDialog(m);
				}
			}
		}
	}

	private boolean showWarningDialog(String message) {
		final JDialog dialog = new JDialog(MainUI.getInstance(),
				"Warning about EPC:", true);

		JLabel argLabel = new JLabel(message);
		JCheckBox noMoreCheck = new JCheckBox(
				"<html>Don't show anymore error messages.</html>");
		noMoreCheck.setEnabled(true);
		noMoreCheck.setSelected(true);

		JButton okButton = new JButton("    Ok    ");
		okButton.setSelected(true);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		dialog.getContentPane().setLayout(new GridBagLayout());

		dialog.getContentPane().add(
				argLabel,
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		dialog.getContentPane().add(
				okButton,
				new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(15, 5, 5, 5), 0, 0));
		dialog.getContentPane().add(
				noMoreCheck,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(15, 5, 5, 5), 0, 0));

		dialog.pack();
		CenterOnScreen.center(dialog);
		okButton.requestFocusInWindow();
		dialog.setVisible(true);
		return !noMoreCheck.isSelected();
	}

	private ConfigurableEPC getSelectedEPC() {
		Object o = hierarchy.getSelectedNode();
		if ((o != null) && o instanceof ConfigurableEPC) {
			return (ConfigurableEPC) hierarchy.getSelectedNode();
		} else {
			return null;
		}
	}

	public ConfigurableEPC getEPC() {
		return getSelectedEPC();
	}
	
	@SuppressWarnings("unchecked")
	public ConfigurableEPC getFirstConfigurableEPC() {
		List<Object> oList = (List<Object>) hierarchy.getAllObjects();
		for(Object o : oList) {
			if ((o != null) && o instanceof ConfigurableEPC) {
				return (ConfigurableEPC) o;
			}
		}
		return null;
	}

	public void addInHierarchy(Object child, Object parent, String label) {
		if (child instanceof ConfigurableEPC) {
			if (showWarnings) {
				String m = ((ConfigurableEPC) child).isValidEPC();
				if (m.length() > 0) {
//					showWarnings = showWarningDialog(m);
				}
			}
		}
		hierarchy.addHierarchyObject(child, parent, label);
	}

	public LogReader getLogReader() {
		return log;
	}

	public void showEPC(ConfigurableEPC epc) {
		ModelGraphPanel gp;
		epc.setShowObjects(adapter.showOrg(), adapter.showData(), adapter
				.showInfSys());
		gp = epc.getGrappaVisualization();
		if (gp != null) {
			gp.addGrappaListener(adapter);
		}
		center.setViewportView(gp);
	}

	public JComponent getVisualization() {
		ModelGraphPanel gp;

		editLogRelationsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editRelations(null);
			}
		});

		if (algorithm instanceof LogRelationBasedAlgorithm) {
			buttonsPanel.add(editLogRelationsButton);
		}
		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		JComponent tv = hierarchy.getTreeVisualization();

		ConfigurableEPC epc = getSelectedEPC();
		if (epc != null) {
			epc.setShowObjects(adapter.showOrg(), adapter.showData(), adapter
					.showInfSys());
			gp = epc.getGrappaVisualization();
			if (gp != null) {
				gp.addGrappaListener(adapter);
			}
		} else {
			gp = null;
		}
		center = new JScrollPane(gp);
		if (tv != null) {
			JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
					tv, center);
			sp.setOneTouchExpandable(true);
			mainPanel.add(sp, BorderLayout.CENTER);
		} else {
			if (gp == null) {
				mainPanel
						.add(
								new JLabel(
										"There is nothing to show. There might be an error, check under \"error\" below."),
								BorderLayout.CENTER);
			} else {
				mainPanel.add(center, BorderLayout.CENTER);
			}
		}
		return mainPanel;
	}

	public void unSelectAll() {

	}

	void repaint() {
		ModelGraphPanel gp = getEPC().getGrappaVisualization();
		if (gp != null) {
			gp.addGrappaListener(adapter);
		}
		center.setViewportView(gp);
		mainPanel.validate();
		mainPanel.repaint();
	}

	public ProvidedObject[] getProvidedObjects() {
		ConfigurableEPC epc = getSelectedEPC();
		ProvidedObject[] providedObjects = new ProvidedObject[1 + (epc == null ? 0
				: 1)];

		if (log == null) {
			providedObjects[0] = new ProvidedObject("EPC Hierarchy",
					new Object[] { hierarchy });
		} else {
			providedObjects[0] = new ProvidedObject("EPC Hierarchy",
					new Object[] { hierarchy, log });
		}
		if (epc != null) {
			providedObjects[1] = new ProvidedObject("Selected EPC",
					new Object[] { getSelectedEPC() });
		}
		return providedObjects;
	}

	void editRelations(LogEvent event) {
		if (algorithm != null) {
			MiningResult result = algorithm.editRelations(event);

			if (result != null) {
				ModelGraphPanel gp;

				if (result instanceof EPCResult) {
					ConfigurableEPC epc = ((EPCResult) result).getEPC();
					gp = epc.getGrappaVisualization();
				} else {
					gp = null;
				}

				center.setViewportView(gp);
				mainPanel.validate();
				mainPanel.repaint();
			}
		}
	}

	public ArrayList getConnectableObjects() {
		Iterator it = hierarchy.getAllObjects().iterator();
		ArrayList functions = new ArrayList();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ConfigurableEPC) {
				ConfigurableEPC epc = (ConfigurableEPC) o;
				functions.addAll(epc.getFunctions());
			}
		}
		return functions;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		this.log = newLog;
		adapter = new EPCGrappaAdapter(this);
		if (eventsMapping == null) {
			return;
		}
		Iterator it = hierarchy.getAllObjects().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ConfigurableEPC) {
				ConfigurableEPC epc = (ConfigurableEPC) o;
				Iterator it2 = epc.getFunctions().iterator();
				while (it2.hasNext()) {
					EPCFunction f = (EPCFunction) it2.next();
					Object[] info = (Object[]) eventsMapping.get(f);

					f.setLogEvent((LogEvent) info[0]);
					f.setIdentifier((String) info[1]);
				}
			}
		}
	}

	void selectEPC(ConfigurableEPC epc) {
		hierarchy.setSelectedNode(epc);
	}

	public ConfigurableEPC getMainEPC() {
		Iterator it = hierarchy.getAllObjects().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ConfigurableEPC) {
				ConfigurableEPC epc = (ConfigurableEPC) o;
				if (!epc.getIdentifier().equals("Group.Root")) {
					return epc;
				}
			}
		}
		return null;
	}

	public ArrayList<ConfigurableEPC> getAllEPCs() {
		ArrayList<ConfigurableEPC> results = new ArrayList();
		Iterator it = hierarchy.getAllObjects().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof ConfigurableEPC) {
				results.add((ConfigurableEPC) o);
			}
		}
		return results;
	}
}
