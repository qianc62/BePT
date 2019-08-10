package org.processmining.analysis.sequenceclustering;

import java.io.IOException;
import java.io.Writer;
import org.processmining.framework.models.DotFileWriter;

/**
 * @author Gabriel Veiga, IST - Technical University of Lisbon
 * @author Supervisor: Prof. Diogo Ferreira
 */
public class ClusterModel implements DotFileWriter {

	protected double nodeSignificance;
	protected double edgeSignificance;
	protected double nodeInvertedSignificance;
	protected double edgeInvertedSignificance;
	protected SCAlgorithm scAlg;
	protected Cluster cluster;
	protected int index;

	public ClusterModel(int index, SCAlgorithm sc) {
		this.index = index;
		this.scAlg = sc;
		this.nodeSignificance = 0.0;
		this.edgeSignificance = 0.0;
		this.nodeInvertedSignificance = 1.0;
		this.edgeInvertedSignificance = 1.0;
		this.cluster = scAlg.clusterList.get(index);
	}

	public ClusterModel(Writer bw, int index, SCAlgorithm sc,
			double nodeSignificance, double edgeSignificance,
			double nodeInvertedSignificance, double edgeInvertedSignificance) {
		this.index = index;
		this.scAlg = sc;
		this.nodeSignificance = nodeSignificance;
		this.edgeSignificance = edgeSignificance;
		this.nodeInvertedSignificance = nodeInvertedSignificance;
		this.edgeInvertedSignificance = edgeInvertedSignificance;
		this.cluster = scAlg.clusterList.get(index);

		try {
			writeToDot(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the dot that represents the Markov chain
	 */
	public void writeToDot(Writer bw) throws IOException {

		double cell;
		int grayEdgeValue, grayNodeValue;
		String fontColor;
		boolean draw = false;

		cluster.auxMatrix = new double[cluster.numLogEvents + 2][cluster.numLogEvents + 2];
		cluster.previousAuxMatrix = new double[cluster.numLogEvents + 2][cluster.numLogEvents + 2];

		cluster.buildMatrix();

		if (cluster.getLog().getInstances().size() > 0) {

			bw.write("digraph G {rankdir=LR; " + "\"" + "in" + "\""
					+ "[shape=doublecircle]; " + "\"" + "out" + "\""
					+ "[shape=doublecircle];");

			cluster.recalculateMatrix(nodeSignificance, edgeSignificance,
					nodeInvertedSignificance, edgeInvertedSignificance);

			for (int i = 1; i < cluster.numLogEvents + 1; i++) {
				if (cluster.isReachable(i)) {
					grayNodeValue = (int) (100 - ((cluster.log.getLogSummary()
							.getInstancesForEvent(
									cluster.log.getLogSummary().getLogEvents()
											.get(i - 1)).size() * 1.0 / cluster.log
							.numberOfInstances()) * 100));

					if (grayNodeValue > 50)
						fontColor = "black";
					else
						fontColor = "white";

					bw.write("\""
							+ cluster.log.getLogSummary().getLogEvents().get(
									i - 1).getModelElementName() + "\""
							+ "[shape=ellipse,fontcolor=" + fontColor
							+ ",style=filled,fillcolor=gray" + grayNodeValue
							+ "];\n");
				}
			}

			for (int i = 0; i < cluster.numLogEvents + 1; i++) {
				for (int j = 1; j < cluster.numLogEvents + 2; j++) {
					draw = false;
					cell = cluster.auxMatrix[i][j];

					if (cell != 0) {
						if (i == 0) {
							bw.write("\"" + "in" + "\"" + " -> ");
							bw.write("\""
									+ cluster.log.getLogSummary()
											.getLogEvents().get(j - 1)
											.getModelElementName() + "\"\n");
							draw = true;
						} else if (i != 0 && j == cluster.numLogEvents + 1) {
							bw.write("\""
									+ cluster.log.getLogSummary()
											.getLogEvents().get(i - 1)
											.getModelElementName() + "\"");
							bw.write(" -> " + "\"" + "out" + "\"" + "\n");
							draw = true;
						} else if (i != 0 && j != cluster.numLogEvents + 1) {
							bw.write("\""
									+ cluster.log.getLogSummary()
											.getLogEvents().get(i - 1)
											.getModelElementName() + "\""
									+ " -> ");
							bw.write("\""
									+ cluster.log.getLogSummary()
											.getLogEvents().get(j - 1)
											.getModelElementName() + "\"\n");
							draw = true;
						}

						if (cell > 0.05)
							grayEdgeValue = (int) (100 - cell * 100);
						else
							grayEdgeValue = 95;
						if (draw) {
							bw.write("[label=" + "\"" + cell + "\"" + ","
									+ "color=gray" + grayEdgeValue + "];\n");
						}
					}
				}
			}

			bw.write("}");
		}

	}

}
