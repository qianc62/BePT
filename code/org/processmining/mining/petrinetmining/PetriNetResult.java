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

package org.processmining.mining.petrinetmining;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.LogEventLogFilter;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.PetriNetHierarchy;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.ComboBoxLogEvent;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PetriNetResult implements MiningResult, Provider,
		LogReaderConnection {

	protected LogReader log;
	protected LogRelationBasedAlgorithm algorithm;

	protected JPanel mainPanel = new JPanel(new BorderLayout());
	protected JScrollPane netContainer = new JScrollPane();
	protected JPanel buttonsPanel = new JPanel();
	protected JButton editLogRelationsButton = new JButton("Edit log relations");
	public PetriNetGrappaAdapter adapter;
	private PetriNet net;

	protected PetriNetHierarchy hierarchy;

	protected void newHierarchy() {
		hierarchy = new PetriNetHierarchy() {
			protected void selectionChanged(Object selectedObject) {
				if (netContainer == null) {
					return;
				}
				if (selectedObject instanceof ModelGraph) {
					ModelGraph pnet = (ModelGraph) selectedObject;
					ModelGraphPanel gp = pnet.getGrappaVisualization();
					if (gp != null) {
						gp.addGrappaListener(adapter);
					}
					netContainer.setViewportView(gp);
					mainPanel.validate();
					mainPanel.repaint();
				} else {
					JPanel p = new JPanel();
					p.add(new JLabel("Cannot visualize selection"));
					netContainer.setViewportView(p);
					mainPanel.validate();
					mainPanel.repaint();
				}
			}
		};

	}

	public void addInHierarchy(Object child, Object parent, String label) {
		hierarchy.addHierarchyObject(child, parent, label);
	}

	public PetriNetResult(PetriNet net) {
		this(null, net, null);
	}

	public PetriNetResult(LogReader log, PetriNet net) {
		this(log, net, null);
	}

	public void repaintNet() {
		repaint();
	}

	public PetriNetResult(LogReader log, PetriNet net, MiningPlugin algorithm) {
		newHierarchy();
		this.log = log;
		this.net = net;
		this.algorithm = (algorithm instanceof LogRelationBasedAlgorithm ? (LogRelationBasedAlgorithm) algorithm
				: null);

		if (net != null) {
			addInHierarchy(this.net, null, net.getIdentifier());
		}
		adapter = new PetriNetGrappaAdapter(this, algorithm != null);
	}

	public ProvidedObject[] getProvidedObjects() {
		if (log != null) {
			if (getPetriNet() == null) {
				return new ProvidedObject[] { new ProvidedObject(
						"Petri net hierarchy", new Object[] { hierarchy, log }) };
			} else {
				PetriNet net = getPetriNet();
				return new ProvidedObject[] {
						new ProvidedObject("Selected Petri net", new Object[] {
								net, log }),
						new ProvidedObject("Petri net hierarchy", new Object[] {
								hierarchy, log }) };
			}
		} else {
			if (getPetriNet() == null) {
				return new ProvidedObject[] { new ProvidedObject(
						"Petri net hierarchy", new Object[] { hierarchy }) };
			} else {
				PetriNet net = getPetriNet();
				return new ProvidedObject[] {
						new ProvidedObject("Selected Petri net",
								new Object[] { net }),
						new ProvidedObject("Petri net hierarchy",
								new Object[] { hierarchy }) };
			}
		}
	}

	public PetriNet getPetriNet() {
		Object o = hierarchy.getSelectedNode();
		if ((o != null) && (o instanceof PetriNet)) {
			return (PetriNet) o;
		} else {
			return null;
		}
	}

	public LogReader getLogReader() {
		return log;
	}

	public JComponent getVisualization() {
		editLogRelationsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editRelations(null);
			}
		});
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				hierarchy.getTreeVisualization(), netContainer);

		split.setOneTouchExpandable(true);

		if (algorithm != null) {
			buttonsPanel.add(editLogRelationsButton);
			mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
		}
		mainPanel.add(split, BorderLayout.CENTER);

		return mainPanel;
	}

	protected void repaint() {
		ModelGraphPanel gp = getPetriNet().getGrappaVisualization();
		if (gp != null) {
			gp.addGrappaListener(adapter);
		}
		netContainer.setViewportView(gp);
		mainPanel.validate();
		mainPanel.repaint();
	}

	public void editRelations(Transition t) {
		if (algorithm != null) {
			MiningResult result = algorithm.editRelations((t == null ? null : t
					.getLogEvent()));
			if (result != null) {
				ModelGraphPanel gp;

				if (result instanceof PetriNetResult) {
					// gp = (ModelGraphPanel)((PetriNetResult)
					// result).netContainer.getViewport().getView();
					newHierarchy();
					net = ((PetriNetResult) result).getPetriNet();
					addInHierarchy(net, null, net.getIdentifier());
					gp = net.getGrappaVisualization();
					gp.addGrappaListener(adapter);
				} else {
					gp = null;
				}
				netContainer.setViewportView(gp);
				mainPanel.validate();
				mainPanel.repaint();
			}
		}
	}

	public ArrayList getConnectableObjects() {
		ArrayList result = new ArrayList();
		Iterator it = hierarchy.getAllObjects().iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof PetriNet) {
				result.addAll(((PetriNet) o).getTransitions());
			}
		}
		return result;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		log = newLog;
		adapter = new PetriNetGrappaAdapter(this, algorithm != null);
		if (eventsMapping != null) {
			Iterator it = hierarchy.getAllObjects().iterator();
			while (it.hasNext()) {
				Object o = it.next();
				if (o instanceof PetriNet) {
					Iterator it2 = ((PetriNet) o).getTransitions().iterator();

					while (it2.hasNext()) {
						Transition t = (Transition) it2.next();
						Object[] mapped = (Object[]) eventsMapping.get(t);
						// if the imported transition does not specify a log
						// event type,
						// it is invisible by nature
						t.setLogEvent((LogEvent) mapped[0]);
						t.setIdentifier((String) mapped[1]);
						// t.setModelElement((LogEvent)
						// eventsMapping.get(t.getModelElement()));
					}
				}
			}
		}
	}

	/**
	 * unSelectAll
	 */
	public void unSelectAll() {
	}

	public void destroy() {
		this.adapter = null;
		algorithm = null;
		net = null;
		hierarchy.destroy();
		hierarchy = null;
		log = null;
		editLogRelationsButton.removeAll();
		editLogRelationsButton = null;
		buttonsPanel.removeAll();
		buttonsPanel = null;
		netContainer.removeAll();
		netContainer = null;
		mainPanel.removeAll();
		mainPanel = null;
	}

}

class PetriNetGrappaAdapter extends GrappaAdapter {

	private final PetriNetResult result;
	private boolean editRelations;
	private InvisTransMenuItem invisItem;
	private JMenuItem visualAll;
	private LogFilter oldFilter = null;
	private LogEventLogFilter eventLogFilter = null;
	private LogEvents allEvents;

	public PetriNetGrappaAdapter(PetriNetResult r, boolean editRelations) {
		this.result = r;
		allEvents = new LogEvents();
		if (r.log != null) {
			allEvents.addAll(result.getLogReader().getLogSummary()
					.getLogEvents());
		}
		this.editRelations = editRelations;
		if (r.getLogReader() != null) {
			eventLogFilter = new LogEventLogFilter(r.getLogReader()
					.getLogSummary().getLogEvents());
			eventLogFilter.setLowLevelFilter(oldFilter);
			oldFilter = r.getLogReader().getLogFilter();
		}
		visualAll = new JMenuItem("Make all transitions visible");
		visualAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean done = false;
				Iterator it = result.getPetriNet().getTransitions().iterator();
				while (it.hasNext()) {
					Transition t = (Transition) it.next();
					if (t.isInvisibleTask()) {
						makeVisible(t, false);
						done = true;
					}
				}
				if (done) {
					result.repaint();
				}
			}
		});
		invisItem = new InvisTransMenuItem("Is invisible transition.");
		invisItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				Transition t = invisItem.getTransition();
				if (invisItem.isSelected() != t.isInvisibleTask()) {
					if (t.isInvisibleTask()) {
						makeVisible(t, true);
					} else {
						makeInvisible(t);
					}
					result.repaint();
				}
			}
		});
	}

	private void makeVisible(Transition t, boolean showDialog) {
		// create a new LogEvent, or get it from the ones we
		// removed already, which are stored in oldLogFilter;
		LogEvent newEvent = null;
		if (showDialog) {
			LogEventIntroductionDialog d = new LogEventIntroductionDialog(
					allEvents, t.getIdentifier());
			newEvent = d.showDialog();
			if (newEvent == null) {
				invisItem.setSelected(t.isInvisibleTask());
				return;
			}
			t.setIdentifier(d.getLabel());
		} else {
			newEvent = new LogEvent(ComboBoxLogEvent.NONE,
					ComboBoxLogEvent.NONE);
		}
		t.setLogEvent(newEvent);
		// Check for filterChange
		if (result.log != null) {
			eventLogFilter.addLogEvent(newEvent);
			// If the current EventLogFilter supports all the events
			// that are in allEvents, then it can be removed.
			if ((result.log.getLogFilter() == eventLogFilter)
					&& eventLogFilter.acceptsAll(allEvents)) {
				// The original set of logevents is accepted
				try {
					result.log = LogReaderFactory.createInstance(oldFilter,
							result.log);
				} catch (Exception e) {
					e.printStackTrace();
					result.log = null;
				}
			}
		}

	}

	private void makeInvisible(Transition t) {
		LogEvent event = t.getLogEvent();
		t.setLogEvent(null);
		if ((result.log != null)
				&& (result.getPetriNet().findTransitions(event).size() == 0)) {
			// The logEvent is no longer used in the PetriNet. This
			// means it should be removed from the LogReader (if there is one)
			eventLogFilter.removeLogEvent(event);
			// construct a new LogReader on the same file, with the new
			// filter, since this filter is removing one logEvent extra...
			try {
				result.log = LogReaderFactory.createInstance(eventLogFilter,
						result.log);
			} catch (Exception e) {
				e.printStackTrace();
				result.log = null;
			}
		}
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		int i = InputEvent.BUTTON1_MASK;
		int j = InputEvent.SHIFT_MASK;

		if (editRelations && (modifiers & i) == i && ((modifiers & j) == j)
				&& clickCount == 1 && elem != null && elem.object != null
				&& elem.object instanceof Transition) {
			Transition t = (Transition) elem.object;
			result.editRelations(t);
		}
	}

	/**
	 * The method is called when a mouse press occurs on a displayed subgraph.
	 * The returned menu is added to the end of the default right-click menu
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

		if (elem != null && elem.object != null
				&& elem.object instanceof Transition) {
			Transition t = (Transition) elem.object;
			invisItem.setTransition(t);
			invisItem.setSelected(t.isInvisibleTask());
			return invisItem;
		}
		return visualAll;

	}

	private class InvisTransMenuItem extends JCheckBoxMenuItem {
		private Transition t = null;

		public InvisTransMenuItem(String s) {
			super(s);
		}

		public void setTransition(Transition t) {
			this.t = t;
		}

		public Transition getTransition() {
			return t;
		}
	};

	private class LogEventIntroductionDialog extends JDialog {
		private LogEvents events;
		private JTextField eventName = new JTextField("");
		private JTextField eventType = new JTextField("");
		private JTextField eventLabel = new JTextField("logEvent label");
		private ToolTipComboBox logEvents;
		boolean isCanceled = false;

		public LogEventIntroductionDialog(LogEvents events, String label) {
			super(MainUI.getInstance(), "Choose Log Event to attach", true);
			this.events = events;

			ComboBoxLogEvent[] logEventsArray;
			logEventsArray = new ComboBoxLogEvent[events.size() + 1];
			final LogEvent nullEvent = new LogEvent(" New event", "none");
			logEventsArray[0] = new ComboBoxLogEvent(nullEvent);
			for (int i = 0; i < events.size(); i++) {
				LogEvent le = events.getEvent(i);
				logEventsArray[i + 1] = new ComboBoxLogEvent(le);
			}
			Arrays.sort(logEventsArray);

			int j = 0;
			for (int i = 1; i < logEventsArray.length; i++) {
				LogEvent le = logEventsArray[i].getLogEvent();
				if (label.startsWith(le.getModelElementName())
						&& label.endsWith(le.getEventType())) {
					j = i;
				}
			}
			logEvents = new ToolTipComboBox(logEventsArray);
			logEvents.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					LogEvent selected = ((ComboBoxLogEvent) logEvents
							.getSelectedItem()).getLogEvent();
					if (selected == nullEvent) {
						eventName.setEnabled(true);
						eventType.setEnabled(true);
					} else {
						eventName.setText(selected.getModelElementName());
						eventType.setText(selected.getEventType());
						// eventLabel.setText(selected.getModelElementName()+"\\n"+selected.getEventType());
						eventName.setEnabled(false);
						eventType.setEnabled(false);
					}
				}
			});
			logEvents.setSelectedIndex(j);

			JPanel p = new JPanel(new GridLayout(4, 2));
			p.add(new JLabel("Existing Log Event:"));
			p.add(logEvents);
			p.add(new JLabel("Name of new Log Event"));
			p.add(eventName);
			p.add(new JLabel("Type of new Log Event"));
			p.add(eventType);
			p.add(new JLabel("Label of new Log Event"));
			p.add(eventLabel);
			eventLabel.setText(label);

			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton okButton = new JButton(" OK ");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// LogEventIntroductionDialog.this.setVisible(false);
					LogEventIntroductionDialog.this.dispose();
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					isCanceled = true;
					// LogEventIntroductionDialog.this.setVisible(false);
					LogEventIntroductionDialog.this.dispose();
				}
			});

			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);

			JPanel mainPanel = new JPanel(new BorderLayout());

			mainPanel.add(p, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);

			this.getContentPane().add(mainPanel);
			pack();
			CenterOnScreen.center(this);
		}

		public LogEvent showDialog() {
			setVisible(true);
			if (isCanceled) {
				return null;
			}
			LogEvent e = events.findLogEvent(eventName.getText(), eventType
					.getText());
			if (e == null) {
				e = new LogEvent(eventName.getText(), eventType.getText());
			}
			return e;
		}

		public String getLabel() {
			return eventLabel.getText();
		}
	}
}
