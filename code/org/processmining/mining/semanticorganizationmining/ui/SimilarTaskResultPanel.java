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

package org.processmining.mining.semanticorganizationmining.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMiningResult;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author mssong
 * @version 1.0
 */

public class SimilarTaskResultPanel extends JPanel {

	public SimilarTaskResultPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected JSlider thresholdSlider = null;
	private JSplitPane splitPane;
	private JPanel menuPane = new JPanel();
	private JPanel graphPanel;
	private JScrollPane jscroll;

	private LogReader log;

	private OrgModel orgModel;
	private OrgModel orgOriginalModel;
	private SocialNetworkMatrix snMatrix;
	private SemanticOrgMiningResult parentPanel;

	private JLabel thresholdLabel = new JLabel();
	private JLabel thresholdValueLabel = new JLabel();

	// private JButton generateButton = new JButton("Generate Org Model ");

	public SimilarTaskResultPanel(OrgModel orgmodel,
			SocialNetworkMatrix snmatrix, SemanticOrgMiningResult parent) {

		this.orgModel = orgmodel;
		this.orgOriginalModel = (OrgModel) orgModel.clone();
		this.snMatrix = snmatrix;
		this.parentPanel = parent;

		thresholdLabel = new JLabel("threshold : ");
		thresholdValueLabel = new JLabel("0");

		thresholdLabel.setOpaque(true);
		thresholdLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		thresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 1001, 0);
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateThresholdLabel();
			}
		});

		menuPane.setLayout(new GridBagLayout());
		menuPane.add(thresholdLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		menuPane.add(thresholdValueLabel, new GridBagConstraints(1, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		menuPane.add(thresholdSlider, new GridBagConstraints(0, 2, 3, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// initialize org model
		orgModel.reallocateOrgModel(snMatrix.listGroupOriginator(Double
				.valueOf(getThresholdFromSlider())), orgOriginalModel);

		// draw graph panel
		drawGraphPanel(this.snMatrix.getNodeNames(), this.snMatrix.getMatrix());
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, menuPane, jscroll);

		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	protected double getThresholdFromSlider() {
		double threshold = (double) thresholdSlider.getValue() / 1000.0;
		// normalize threshold to minimal node frequency
		threshold = (snMatrix.getMaxValue() - 0) * threshold + 0;
		return threshold;
	}

	protected void generateGroup() {
		orgModel.reallocateOrgModel(snMatrix.listGroupOriginator(Double
				.valueOf(getThresholdFromSlider())), orgOriginalModel);
	}

	protected void updateThresholdLabel() {
		thresholdValueLabel.setText(Double.valueOf(getThresholdFromSlider())
				.toString());
		if (thresholdSlider.getValueIsAdjusting() == false) {
			generateGroup();
			redrawGraphPanel(snMatrix.getNodeNames(), getSNMatrix()
					.applyThresholdValueToTempMatrix(
							Double.valueOf(getThresholdFromSlider())));
		}
	}

	public SocialNetworkMatrix getSNMatrix() {
		return snMatrix;
	}

	private JPanel getGraphPanel(String[] users, DoubleMatrix2D matrix) {
		BufferedWriter bw;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			bw
					.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
			bw.write("fontname=\"Arial\";rankdir=\"LR\"; \n");
			bw
					.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");
			bw
					.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");

			for (int i = 0; i < users.length; i++) {
				bw.write("t" + i + " [shape=\"box\",label=\"" + users[i]
						+ "\"];\n");
			}

			for (int i = 0; i < matrix.rows(); i++) {
				for (int j = 0; j < matrix.columns(); j++) {
					double value = matrix.get(i, j);

					if (value > 0) {
						bw.write("t" + i + " -> t" + j + " [label=\""
								+ nf.format(value) + "\"];\n");
					}
				}
			}
			bw.write("}\n");
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			System.out.println(dotFile.getAbsolutePath());
			graph = Dot.execute(dotFile.getAbsolutePath());

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			GrappaPanel gp = new GrappaPanel(graph);
			gp.addGrappaListener(new GrappaAdapter());
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

	public void drawGraphPanel(String[] users, DoubleMatrix2D matrix) {
		jscroll = null;
		graphPanel = null;
		graphPanel = getGraphPanel(users, matrix);
		jscroll = new JScrollPane(graphPanel);
	}

	public void redrawGraphPanel(String[] users, DoubleMatrix2D matrix) {
		drawGraphPanel(users, matrix);
		splitPane.setRightComponent(jscroll);
		splitPane.validate();
		splitPane.repaint();
	}

	private void jbInit() throws Exception {
	}

}
