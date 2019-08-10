package org.processmining.analysis.hmm;

import java.io.BufferedWriter;
import java.io.IOException;

import org.processmining.framework.ui.Message;

/**
 * Class encapsulating gnuplot script files. Provides abstract interface for
 * creation and plotting.
 */
public class GnuplotScript {

	public enum PlotType {
		lines, points
	}

	private String name;
	private String title;
	private String xlabel;
	private String ylabel;
	private PlotType plottype;
	private BufferedWriter script;
	private boolean firstPlot = true;

	public GnuplotScript(String aFolder, String aName, String aTitle,
			String anXlabel, String aYlabel, PlotType aPlottype) {
		name = aName;
		title = aTitle;
		xlabel = anXlabel;
		ylabel = aYlabel;
		plottype = aPlottype;
		script = HmmExpUtils.createWriter(aFolder, name + "_"
				+ plottype.toString(), "gpl");
		initialize();
	}

	private void initialize() {
		try {
			script.write("set term postscript\n");
			script.write("set output \"" + name + "_" + plottype.toString()
					+ ".ps\"\n");
			script.write("set title \"" + title + "\"\n");
			script.write("set xlabel \"" + xlabel + "\"\n");
			script.write("set ylabel \"" + ylabel + "\"\n");
		} catch (IOException ex) {
			ex.printStackTrace();
			Message.add("Could not create " + name + " gnuplot file",
					Message.ERROR);
			finish();
		}
	}

	/**
	 * Adds the given .dat file as a plot in the script.
	 * 
	 * @param datfileName
	 *            the datfile to be plotted without suffix. It is assumed to be
	 *            in the same folder as the script and to have the suffix .dat
	 * @param plotName
	 *            the name by which the plotted data should be represented in
	 *            the graph
	 */
	public void addPlot(String datfileName, String plotName) {
		try {
			if (firstPlot == true) {
				script.write("plot \"" + datfileName + ".dat\" title \""
						+ plotName + "\" with " + plottype.toString());
				firstPlot = false;
			} else {
				script.write(", \\\n");
				script.write("\"" + datfileName + ".dat\" title \"" + plotName
						+ "\" with " + plottype.toString());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			Message.add("Could not write to " + name + " gnuplot file",
					Message.ERROR);
			finish();
		}
	}

	/**
	 * Finishes and closes the gnuplot file. Should be called after finishing
	 * plotting.
	 */
	public void finish() {
		try {
			script.flush();
			script.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			Message.add("Could not finish " + name + " gnuplot file",
					Message.ERROR);
			finish();
		}
	}

}
