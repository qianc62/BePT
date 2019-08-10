package org.processmining.analysis.hmm.metrics;

import java.io.BufferedWriter;

import org.processmining.analysis.hmm.GnuplotScript;
import org.processmining.analysis.hmm.HmmAnalyzer;
import org.processmining.analysis.hmm.HmmExpUtils;
import org.processmining.analysis.hmm.GnuplotScript.PlotType;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;

/**
 * Base class for metrics to be used in HMM experiment. Handles metric-specific
 * gnuplot script writing.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public abstract class HmmExpMetric {

	public enum MetricType {
		Fitness, Precision
	}

	public enum ExpType {
		Noise, Completeness
	}

	private String folder; // target folder for gnuplots and dat files
	private String name; // e.g. "Model Level"
	private String filename; // becomes "Model_Level.bat etc."
	private MetricType mType;
	private ExpType eType;
	private BufferedWriter valueWriter;
	private BufferedWriter valueRatioWriter;
	private BufferedWriter averageWriter;
	private BufferedWriter averageRatioWriter;
	private BufferedWriter varianceWriter;

	public HmmExpMetric(String aFolder, String aName, MetricType metType,
			ExpType expType) {
		folder = aFolder;
		name = aName;
		mType = metType;
		eType = expType;
		filename = name.replaceAll(" ", "") + mType;
		// for non-replications
		valueWriter = HmmExpUtils.createWriter(folder, filename, "dat");
		valueRatioWriter = HmmExpUtils.createWriter(folder, filename + "Ratio",
				"dat");
		// for replications
		averageWriter = HmmExpUtils.createWriter(folder, filename + "Average",
				"dat");
		averageRatioWriter = HmmExpUtils.createWriter(folder, filename
				+ "AverageRatio", "dat");
		varianceWriter = HmmExpUtils.createWriter(folder,
				filename + "Variance", "dat");
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public BufferedWriter getValueWriter() {
		return valueWriter;
	}

	public BufferedWriter getRatioWriter() {
		return valueRatioWriter;
	}

	public BufferedWriter getAverageWriter() {
		return averageWriter;
	}

	public BufferedWriter getAverageRatioWriter() {
		return averageRatioWriter;
	}

	public BufferedWriter getVarianceWriter() {
		return varianceWriter;
	}

	public void writeRatioGnuplot() {
		GnuplotScript gnuplot = new GnuplotScript(folder, filename + "Ratio",
				name + " " + mType + " Ratio for Different " + eType
						+ " Levels", eType + " Level", mType + " Ratio",
				PlotType.lines);
		gnuplot.addPlot(filename + "Ratio", "Ratio");
		gnuplot.finish();
	}

	public void writeAverageRatioGnuplot() {
		GnuplotScript gnuplot = new GnuplotScript(folder, filename + "Ratio",
				name + " " + mType + " Ratio and Variance for Different "
						+ eType + " Levels", eType + " Level",
				mType + " Ratio", PlotType.lines);
		gnuplot.addPlot(filename + "AverageRatio", "Average Ratio");
		gnuplot.addPlot(filename + "Variance", "Variance");
		gnuplot.finish();
	}

	public void finishDatFiles() {
		HmmExpUtils.finishFile(valueWriter);
		HmmExpUtils.finishFile(valueRatioWriter);
		HmmExpUtils.finishFile(averageWriter);
		HmmExpUtils.finishFile(averageRatioWriter);
		HmmExpUtils.finishFile(varianceWriter);
	}

	public abstract double calculateValue(HmmAnalyzer analyzer, PetriNet pnet,
			LogReader log);

}
