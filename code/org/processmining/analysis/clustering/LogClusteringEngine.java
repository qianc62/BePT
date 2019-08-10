package org.processmining.analysis.clustering;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.processmining.analysis.clustering.algorithm.AHC;
import org.processmining.analysis.clustering.model.LogSequence;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;

import att.grappa.Graph;
import att.grappa.GrappaPanel;
import att.grappa.Parser;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

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
public class LogClusteringEngine {

	private LogSequence logS = null;
	private SimCalculator simC = null;
	private AHC ahc = null;

	private double meanActSimilarityOfProcLog = 0.0;
	private double meanImpTranSimilarityOfProcLog = 0.0;
	private double meanExpTranSimilarityOfProcLog = 0.0;

	private java.text.DecimalFormat dformat3 = new java.text.DecimalFormat(
			"###.####");
	LogClusteringResultUI resultUI = null;

	GrappaPanel gp = null;

	public LogClusteringEngine(LogReader log) {

		this.logS = new LogSequence(log);
		// this.simC = new SimCalculator(logS);
	}

	public SimCalculator getSimCalculator() {

		return simC;
	}

	public void calculateSim(double alpha, int tranType, int measureType,
			int freqOption) {

		simC = new SimCalculator(logS, alpha, tranType, measureType, freqOption);
	}

	public void clusteringByAHC() {
		ahc = new AHC(simC, 1); // simple setting
		ahc.AHCAlgorithm(); // calculation
	}

	public DoubleMatrix2D filterResultMatrix() {
		DoubleMatrix2D m = DoubleFactory2D.sparse.make(3, 3, 0);

		int k = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				m.set(i, j, i + j);
			}
		}

		return m;
	}

	public int countZeroValueOfSimilarity() {
		double[] sim = ahc.getClusterProcedureSim();
		int zeroValueCount = 0;
		for (int i = 0; i < sim.length; i++) {
			if (sim[i] <= 0) {
				zeroValueCount++;
			}
		}
		return zeroValueCount;
	}

	public void setLogClusteringResultUI(LogClusteringResultUI resultUI) {
		this.resultUI = resultUI;
	}

	public void setObjectSelection(String nodeName) {
		Message.add("DoubleClicked Node = " + nodeName);

		JTable jTable1 = resultUI.jTable1;
		ListSelectionModel mod = jTable1.getSelectionModel();
		mod.clearSelection();
		mod.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		int index = Integer.parseInt(nodeName.substring(1));
		Vector v = null;
		if (nodeName.substring(0, 1).equals("C")) {
			v = (Vector) ahc.logList[index];
		} else {
			v = (Vector) logS.phm_index.get(logS.proc[index]);
		}

		for (int i = 0; i < v.size(); i++) {
			Integer j = (Integer) v.get(i);
			mod.addSelectionInterval(j.intValue(), j.intValue());
		}
		Message.add(v.toString());
	}

	public JPanel getAHCDiagram(int comp, double sim) {

		int leng = simC.proc.length;
		int zeroValueCount = countZeroValueOfSimilarity();

		String[] nodeList = new String[leng + (leng - 1) - zeroValueCount];
		for (int i = 0; i < leng; i++) {
			nodeList[i] = "S" + i; // +simC.proc[i];
			// nodeList[i]="["+i+"]";//+simC.proc[i];
			// nodeList[i]=nodeList[i]+" (n="+logS.getFrequencyOfProcSequence(simC.proc[i])+")";
		}
		for (int j = 0; j < leng - 1 - zeroValueCount; j++) {
			nodeList[leng + j] = "C" + j;
		}

		return getGraphPanel(comp, sim, nodeList, makeClusteringMatrix2D(leng,
				zeroValueCount), leng);
	}

	public DoubleMatrix2D makeClusteringMatrix2D(int leng, int zeroValueCount) {
		int[][] clusterProcedure = ahc.getClusterProcedure();
		double[] sim = ahc.getClusterProcedureSim();

		int matrixSize = leng + leng - 1 - zeroValueCount;
		DoubleMatrix2D matrix = DoubleFactory2D.sparse.make(matrixSize,
				matrixSize, 0);
		TreeMap m = new TreeMap();
		int c2, c1;
		int[] memory = new int[leng];
		for (int i = 0; i < clusterProcedure.length - zeroValueCount; i++) {
			c2 = clusterProcedure[i][0];
			c1 = clusterProcedure[i][1];
			matrix.set(memory[c2] == 0 ? c2 : memory[c2], leng + i, sim[i]);
			matrix.set(memory[c1] == 0 ? c1 : memory[c1], leng + i, sim[i]);
			memory[c2] = leng + i;
			memory[c1] = leng + i;
		}
		return matrix;
	}

	public JPanel getGraphPanel(int comp, double sim, String[] nodeList,
			DoubleMatrix2D matrix, int procCount) {
		BufferedWriter bw;
		Process dot;
		Parser parser;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			bw.write("digraph G {");
			// bw.write("ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
			// bw.write("fontname=\"Arial\";rankdir=\"LR\"; \n");
			// bw.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");

			bw
					.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"14\"];\n");
			Vector v = new Vector();
			Vector startingNodes = new Vector();
			Vector endingNodes = new Vector();
			for (int i = 0; i < matrix.rows(); i++) {
				for (int j = 0; j < matrix.columns(); j++) {
					double value = matrix.get(i, j);

					if (comp == 0) {
						if (value > sim) {
							bw.write("" + nodeList[i] + " -> " + nodeList[j]
									+ " [label=\"" + nf.format(value)
									+ "\"];\n");
							v.add(nodeList[i]);
							startingNodes.add(nodeList[i]);
							endingNodes.add(nodeList[i]);
						}
					} else {
						if (value > 0 && value < sim) {
							bw.write("" + nodeList[i] + " -> " + nodeList[j]
									+ " [label=\"" + nf.format(value)
									+ "\"];\n");
							v.add(nodeList[i]);
							startingNodes.add(nodeList[i]);
							endingNodes.add(nodeList[i]);
						}
					}
				}
			}
			for (int i = 0; i < matrix.rows(); i++) {
				for (int j = 0; j < matrix.columns(); j++) {
					double value = matrix.get(i, j);

					if (comp == 0) {
						if (value > sim) {
							startingNodes.remove(nodeList[j]);
							endingNodes.remove(nodeList[i]);
							endingNodes.remove(nodeList[i]);
						}
					} else {
						if (value > 0 && value < sim) {
							startingNodes.remove(nodeList[j]);
							endingNodes.remove(nodeList[i]);
							endingNodes.remove(nodeList[i]);
						}
					}
				}
			}
			// int numOfOutliers = logS.piSeq.length - startingNodes.size();
			// Message.add("# of logS.piSeq.length = " + logS.piSeq.length);
			// Message.add("# of outliers = " + numOfOutliers);

			Message.add("Base nodes = " + startingNodes.toString());
			Message.add("# of top nodes = " + endingNodes.size() / 2);

			for (int i = 0; i < v.size(); i++) {
				// bw.write("t" + i + " [layer=\"1\",shape=\"box\",label=\"" +
				// users[i] + "\"];\n");
				bw.write("" + v.get(i) + " [layer=\"1\",shape=\"box\",label=\""
						+ v.get(i) + "\"];\n");
			}

			bw.write("{ rank = same;");
			String s = "";
			for (int i = 0; i < startingNodes.size(); i++) {
				// bw.write("\""+nodeList[i]+"\";");
				s = "" + startingNodes.get(i);
				// if (s.substring(0, 1).equals("S"))
				bw.write("\"" + s + "\";");
			}
			bw.write("};\n");

			bw.write("}\n");
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			System.out.println(dotFile.getAbsolutePath());
			graph = Dot.execute(dotFile.getAbsolutePath(), true);

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			gp = new GrappaPanel(graph);
			ResultGrappaAdapter ga = new ResultGrappaAdapter(this);
			gp.addGrappaListener(ga);
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

	public JScrollPane getProcTablePane() {

		JTable table = new JTable(logS.getProcSeqeunceTable(), // Table Value
				new String[] { "id", "log sequences", "frequency",
						"process instances" });
		table.setPreferredScrollableViewportSize(new Dimension(350, 100));
		// table.sizeColumnsToFit(0);
		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

	public JScrollPane getActTablePane() {

		JTable table = new JTable(logS.getActSeqeunceTable(), // Table Value
				new String[] { " code ", "activity name" });
		table.setPreferredScrollableViewportSize(new Dimension(300, 100));
		JScrollPane scrollPane = new JScrollPane(table);
		return scrollPane;
	}

}
