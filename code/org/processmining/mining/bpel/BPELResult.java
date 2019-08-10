package org.processmining.mining.bpel;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELProcess;
import org.processmining.framework.models.bpel.BPELStructured;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.importing.LogReaderConnection;
import org.processmining.mining.MiningResult;

import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Subgraph;
import org.processmining.framework.models.bpel.BPELActivity;

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
public class BPELResult implements MiningResult, Provider, LogReaderConnection {
	protected BPEL model;
	protected LogReader log;
	private BPELHierarchy hierarchy = new BPELHierarchy() {
		protected void selectionChanged(final Object newSelection) {
			Object selection = newSelection;
			ModelGraph graph = null;
			if (selection instanceof BPEL) {
				selection = (Object) ((BPEL) selection).getProcess();
			}
			if (selection instanceof BPELProcess) {
				final BPELProcess process = (BPELProcess) selection;
				graph = new ModelGraph("bla") {
					public void writeToDot(Writer bw) throws IOException {
						BPELActivity activity = process.getActivity();
						if (activity instanceof BPELStructured) {
							model.initModelGraph();
							((BPELStructured) activity).buildModelGraph(model);
							model.writeToDot(bw);
						}
					}
				};
			}
			if (selection instanceof BPELStructured) {
				final BPELStructured structuredActivity = (BPELStructured) selection;
				graph = new ModelGraph("bla") {
					public void writeToDot(Writer bw) throws IOException {
						model.initModelGraph();
						structuredActivity.buildModelGraph(model);
						model.writeToDot(bw);
					}
				};
			}

			if (graph != null) {
				ModelGraphPanel gp = graph.getGrappaVisualization();
				gp.addGrappaListener(new BPELGrappaAdapter(BPELResult.this));
				netContainer.setViewportView(gp);
				netContainer.invalidate();
				netContainer.repaint();
			}
		}
	};

	private JPanel mainPanel = new JPanel(new BorderLayout());
	private JScrollPane netContainer = new JScrollPane();

	public BPELResult(LogReader log, BPEL model) {
		this.model = model;
		if (model != null) {
			BPELHierarchyVisitor.Build(this.model, hierarchy);
		}
		this.log = log;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("BPEL model",
				new Object[] { model }) };
	}

	public JComponent getVisualization() {
		if (model == null)
			return new JLabel("Irreducible net!");

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				true, hierarchy.getTreeVisualization(), netContainer);
		splitPane.setOneTouchExpandable(true);
		mainPanel.add(splitPane, BorderLayout.CENTER);

		return mainPanel;
	}

	public LogReader getLogReader() {
		return log;
	}

	public ArrayList getConnectableObjects() {
		ArrayList list = new ArrayList();
		BPELConnectablesVisitor.Build(model, list);
		return list;
	}

	public void connectWith(LogReader newLog, HashMap eventsMapping) {
		BPELConnectVisitor.Build(model, newLog, eventsMapping);
	}

	void selectStructured(BPELStructured structuredActivity) {
		hierarchy.setSelectedNode(structuredActivity);
	}

	void selectProcess(BPELProcess process) {
		hierarchy.setSelectedNode(process);
	}
}

class BPELGrappaAdapter extends GrappaAdapter {

	private BPELResult result;

	public BPELGrappaAdapter(BPELResult result) {
		this.result = result;
	}

	public void grappaClicked(Subgraph subg, Element elem, GrappaPoint pt,
			int modifiers, int clickCount, GrappaPanel panel) {
		super.grappaClicked(subg, elem, pt, modifiers, clickCount, panel);
		int i = InputEvent.BUTTON1_MASK;
		int j = InputEvent.SHIFT_MASK;
		if ((modifiers & i) == i && (modifiers & j) == j && clickCount == 1
				&& elem != null && elem.object != null) {
			if (elem.object instanceof BPELStructured) {
				BPELStructured structuredActivity = (BPELStructured) elem.object;
				result.selectStructured(structuredActivity);
			} else if (elem.object instanceof BPELProcess) {
				BPELProcess process = (BPELProcess) elem.object;
				result.selectProcess(process);
			}
		}

	}
}
