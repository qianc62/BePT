/*
 * Created on 7 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.patternsmining;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.epcpack.InstanceEPC;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.logabstraction.LogRelationBasedAlgorithm;

import cern.colt.matrix.DoubleMatrix2D;

public class PatternsResult implements MiningResult, Provider {

	private DependenciesTables relations;
	private LogReader log;
	private MiningPlugin algorithm;
	private JComponent visualization = null;
	private Object result = null;
	private ProvidedObject[] providedObjects;
	private LogEvents modelElements;

	private JTable table;
	private JRadioButton asPetrinet;
	private JRadioButton asEPC;
	private JRadioButton asGraph;
	private JPanel graphPanel;
	private JScrollPane netContainer;
	private JPanel buttonsPanel = new JPanel();
	private JButton editLogRelationsButton = new JButton("Edit log relations");

	private int[] rows;
	protected ModelGraph mainGraph;
	protected ArrayList vertices;

	private boolean aggregating = false;

	private JPanel mainPanel = new JPanel(new BorderLayout());

	public PatternsResult(MiningPlugin algorithm, LogReader log,
			DependenciesTables relations) {
		this.algorithm = algorithm;
		this.log = log;
		this.relations = relations;

	}

	public PatternsResult(LogReader log, ModelGraph net, MiningPlugin algorithm) {
		this.log = log;
		this.mainGraph = net;
		this.algorithm = (algorithm instanceof LogRelationBasedAlgorithm ? (LogRelationBasedAlgorithm) algorithm
				: null);

		if (this.log != null) {
			this.log.reset();
		}
	}

	public ModelGraph build() {

		DoubleMatrix2D finalcausalfollowers = relations
				.getfinalcausalfollowers();
		DoubleMatrix2D finalcausalprecedents = relations
				.getfinalcausalprecedents();
		modelElements = log.getLogSummary().getLogEvents();
		int s = modelElements.size();
		Vector[] Concurrent = relations.getConcurrent();
		Vector[] completePreceeder = relations.getcompletePreceeder();
		Vector[] partialPreceeder = relations.getpartialPreceeder();

		Vector[] completefollowers = relations.getcompletefollowers();
		Vector[] partialfollowers = relations.getpartialfollowers();

		int[] nbconcurrent = relations.getnbconcurrent();
		int[] nbcompletePreceeder = relations.getnbcompletePreceeder();
		int[] nbpartialPreceeder = relations.getnbpartialPreceeder();

		int[] nbcompletefollowers = relations.getnbcompletefollowers();
		int[] nbpartialfollowers = relations.getnbpartialfollowers();

		double[] sumfollowers = relations.getsumfollowers();
		double[] sumPreceeder = relations.getsumPreceeder();

		// Building WORKFLOW PATTERNS is done in the following way:
		//
		// 1) Build a DEPENDENCIES RELATION TAB (done by DependenciesTables
		// class)
		//
		// 2) Use patterns rules to build the patterns
		//    

		// //////////////////////////////////////////////////////
		// /RULES
		// //////////////////////////////////////////////////////

		mainGraph = new ModelGraph("main Graph");
		vertices = new ArrayList();
		mainGraph.setIdentifier("main_graph");
		InstanceEPC epc = new InstanceEPC(false);
		epc.setIdentifier("main_graph1");

		for (int i = 0; i < s; i++) {
			ModelGraphVertex v = new ModelGraphVertex(
					modelElements.getEvent(i), mainGraph);

			vertices.add(v);
			mainGraph.addVertex(v);
			v.setIdentifier(modelElements.getEvent(i).getModelElementName()
					+ "\\n" + modelElements.getEvent(i).getEventType());
		}

		for (int i = 0; i < s; i++) {
			// sequence pattern
			if (nbcompletePreceeder[i] == 1) {
				int source = ((Integer) completePreceeder[i].get(0)).intValue();
				if (nbcompletefollowers[source] == 1) {
					int temp = ((Integer) completefollowers[source].get(0))
							.intValue();
					if (temp == i) {
						mainGraph.addEdge((ModelGraphVertex) vertices
								.get(source), (ModelGraphVertex) vertices
								.get(i));
					}
				}

			}
			// and join pattern
			if (nbcompletePreceeder[i] > 1) {
				ModelGraphVertex andjoin = new ModelGraphVertex(mainGraph);
				mainGraph.addVertex(andjoin);
				andjoin.setIdentifier(" Synchronization pattern");
				// andjoin.setIdentifier(" AND JOIN pattern");
				mainGraph.addEdge(andjoin, (ModelGraphVertex) vertices.get(i));

				for (int j = 0; j < nbcompletePreceeder[i]; j++) {
					int source = ((Integer) completePreceeder[i].get(j))
							.intValue();
					mainGraph.addEdge((ModelGraphVertex) vertices.get(source),
							andjoin);

				}
			}

			if (nbpartialPreceeder[i] > 0) {
				if (sumPreceeder[i] == 1) {
					ModelGraphVertex xorjoin = new ModelGraphVertex(mainGraph);
					mainGraph.addVertex(xorjoin);
					xorjoin.setIdentifier(" Simple Merge pattern");
					// xorjoin.setIdentifier(" XOR JOIN pattern");
					mainGraph.addEdge(xorjoin, (ModelGraphVertex) vertices
							.get(i));

					for (int j = 0; j < nbpartialPreceeder[i]; j++) {
						int source = ((Integer) partialPreceeder[i].get(j))
								.intValue();
						mainGraph.addEdge((ModelGraphVertex) vertices
								.get(source), xorjoin);

					}
				}

				if (sumPreceeder[i] > 1) {
					ModelGraphVertex orjoin = new ModelGraphVertex(mainGraph);
					mainGraph.addVertex(orjoin);
					Double temp = new Double(Math.abs(sumPreceeder[i]));
					orjoin.setIdentifier(temp.intValue() + " out of"
							+ nbpartialPreceeder[i] + " pattern");
					mainGraph.addEdge(orjoin, (ModelGraphVertex) vertices
							.get(i));

					for (int j = 0; j < nbpartialPreceeder[i]; j++) {
						int source = ((Integer) partialPreceeder[i].get(j))
								.intValue();
						mainGraph.addEdge((ModelGraphVertex) vertices
								.get(source), orjoin);

					}
				}
			}

			if (nbcompletefollowers[i] > 1) {
				ModelGraphVertex andsplit = new ModelGraphVertex(mainGraph);
				mainGraph.addVertex(andsplit);
				andsplit.setIdentifier(" Parallel Split pattern");
				// andsplit.setIdentifier(" AND SPLIT pattern");
				mainGraph.addEdge((ModelGraphVertex) vertices.get(i), andsplit);

				for (int j = 0; j < nbcompletefollowers[i]; j++) {
					int dest = ((Integer) completefollowers[i].get(j))
							.intValue();
					mainGraph.addEdge(andsplit, (ModelGraphVertex) vertices
							.get(dest));

				}
			}

			if (nbpartialfollowers[i] > 0) {
				if (sumfollowers[i] == 1) {
					ModelGraphVertex xorsplit = new ModelGraphVertex(mainGraph);
					mainGraph.addVertex(xorsplit);
					xorsplit.setIdentifier(" Exclusive Choice pattern");
					// xorsplit.setIdentifier(" XOR SPLIT pattern");
					mainGraph.addEdge((ModelGraphVertex) vertices.get(i),
							xorsplit);

					for (int j = 0; j < nbpartialfollowers[i]; j++) {
						int dest = ((Integer) partialfollowers[i].get(j))
								.intValue();
						mainGraph.addEdge(xorsplit, (ModelGraphVertex) vertices
								.get(dest));

					}
				}

				if (sumfollowers[i] > 1) {
					ModelGraphVertex orsplit = new ModelGraphVertex(mainGraph);
					mainGraph.addVertex(orsplit);
					orsplit.setIdentifier(" Multi-choice pattern");
					// orsplit.setIdentifier(" OR SPLIT pattern");
					mainGraph.addEdge((ModelGraphVertex) vertices.get(i),
							orsplit);

					for (int j = 0; j < nbpartialfollowers[i]; j++) {
						int dest = ((Integer) partialfollowers[i].get(j))
								.intValue();
						mainGraph.addEdge(orsplit, (ModelGraphVertex) vertices
								.get(dest));

					}
				}
			}

		}

		// if (epc.getGrappaVisualization()==null)
		// System.out.println("nn**************");
		return mainGraph;
	}

	public JComponent getVisualization() {

		ModelGraph epc = build();

		netContainer = new JScrollPane(epc.getGrappaVisualization());

		editLogRelationsButton = new JButton("Edit Activities Dependencies");
		editLogRelationsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editRelations(null);
			}
		});
		if (algorithm != null) {
			buttonsPanel.add(editLogRelationsButton);
			mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
		}
		mainPanel.add(netContainer, BorderLayout.CENTER);
		// mainPanel.validate();
		// mainPanel.repaint();
		return mainPanel;
	}

	void editRelations(LogEvent event) {
		if (algorithm != null) {
			((WorkflowPatternsMiner) algorithm).editRelations(event);
			mainPanel.validate();
			mainPanel.repaint();
		}

	}

	public LogReader getLogReader() {

		return log;
	}

	public ProvidedObject[] getProvidedObjects() {

		return new ProvidedObject[] { new ProvidedObject("Patterns",
				new Object[] { relations, log }) };
	}

}
