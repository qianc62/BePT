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

package org.processmining.mining.geneticmining.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import mathCollection.HashMultiset;
import mathCollection.Multiset;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.framework.util.Parameters;
import org.processmining.mining.geneticmining.duplicates.DTGeneticMiner;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.geneticmining.fitness.duplicates.DTFitnessFactory;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author attributable
 * @version 1.0
 */

public class SummaryReport implements Report {

	public static final String REPORT_SEPARATOR = "@";
	private int fitness = 0;
	private String[] logs = null;
	private TreeSet[] runs = null;
	private BufferedWriter report = null;
	private String inputDirLogs = null;
	private String inputDirRuns = null;

	public SummaryReport(String inputDirLogs, String inputDirRuns,
			String logTermination, int fitness, String reportName)
			throws IOException {

		File f = null;

		this.fitness = fitness;
		checkInput(inputDirLogs, inputDirRuns);
		mapLogsToRuns(inputDirLogs, inputDirRuns, logTermination);

		this.inputDirLogs = inputDirLogs;
		this.inputDirRuns = inputDirRuns;

		// creating report file
		f = new File(inputDirRuns + File.separator + reportName);
		System.out.println("The report file is at: " + f.getAbsolutePath());
		if (f.exists()) {
			f.delete();
		}
		this.report = new BufferedWriter(new FileWriter(f));

	}

	private void mapLogsToRuns(String inputDirLogs, String inputDirRuns,
			String logTermination) throws IOException {
		TreeSet filteredLogs = null;

		String[] dirs = null;
		HashSet result = null;
		Iterator iter = null;

		// getting the logs
		result = MethodsForFiles.getAllSubDirectories(new File(inputDirLogs));
		dirs = new String[result.size()];

		iter = result.iterator();
		for (int i = 0; i < dirs.length; i++) {
			dirs[i] = ((File) iter.next()).getAbsolutePath();
		}

		filteredLogs = new TreeSet();
		for (int i = 0; i < dirs.length; i++) {
			filteredLogs.addAll(Arrays.asList(MethodsForFiles
					.listOfFilesToMine(dirs[i], logTermination)));
		}

		logs = new String[filteredLogs.size()];
		iter = filteredLogs.iterator();
		for (int i = 0; i < filteredLogs.size(); i++) {
			logs[i] = (String) iter.next();
		}

		// getting the runs for every log
		runs = new TreeSet[logs.length];
		result = MethodsForFiles.getAllSubDirectories(new File(inputDirRuns));
		dirs = new String[result.size()];

		iter = result.iterator();
		for (int i = 0; i < dirs.length; i++) {
			dirs[i] = ((File) iter.next()).getAbsolutePath();
		}

		for (int i = 0; i < dirs.length; i++) {
			for (int j = 0; j < logs.length; j++) {
				if (dirs[i].indexOf(new File(logs[j]).getName()) > -1) {
					// this run is for the logs[j].
					if (runs[j] == null) {
						runs[j] = new TreeSet();
					}
					runs[j].add(dirs[i]);
					break;
				}

			}
		}

	}

	private void checkInput(String inputDirLogs, String inputDirRuns) {

		File f = null;

		f = new File(inputDirLogs);
		if (!f.isDirectory()) {
			System.err.println("Input directory for log files = '"
					+ f.getAbsolutePath() + "' is not a directory!");
			System.exit(-1);
		}

		f = new File(inputDirRuns);
		if (!f.isDirectory()) {
			System.err.println("Input directory for runs = '"
					+ f.getAbsolutePath() + "' is not a directory!");
			System.exit(-1);
		}

	}

	public void generateReport() {

		Iterator it = null;
		int j = 0;

		writeReportHeader();
		for (int i = 0; i < logs.length; i++) {
			// System.out.println("LOG = " + logs[i]);
			// System.out.println("Runs = ");
			if (runs[i] != null) {
				// j = 0;
				writePerformance(logs[i], runs[i]);
				// it = runs[i].iterator();
				// while (it.hasNext()) {
				// System.out.println(j++ +" " + it.next());
				// }
			} else {
				System.err.println("NO RUN FOR FILE: " + logs[i] + "!!");
			}
			try {
				this.report.flush();
			} catch (IOException ioe) {

			}
		}
		try {
			this.report.flush();
			this.report.close();
			this.report = null;
		} catch (IOException ioe) {
			// method finalize will send the error message.
		}

	}

	private void writePerformance(String log, TreeSet runs) {
		String currentRunDir = null;
		Iterator iter = null;
		LoadPopulation pop = null;
		HashSet pops4log = null;
		Performance perf = null;
		DefaultLogFilter logFilter = null;
		LogReader logReader = null;
		int maxGeneration = Integer.MIN_VALUE;
		int minGeneration = Integer.MAX_VALUE;
		int sumGenerations = 0;
		int generation = 0;
		Multiset msGenerations = new HashMultiset();

		// creating log reader...
		logFilter = new DefaultLogFilter(DefaultLogFilter.DISCARD);
		logFilter.setProcess("0"); // TO DO!!! move to configuration file

		logFilter.filterEventType("complete", DefaultLogFilter.INCLUDE);

		try {
			logReader = LogReaderFactory.createInstance(logFilter, LogFile
					.getInstance(log));
		} catch (Exception e) {
			e.printStackTrace();
			logReader = null;
		}

		pops4log = new HashSet();
		iter = runs.iterator();
		while (iter.hasNext()) {
			currentRunDir = (String) iter.next();
			pop = new LoadPopulation(currentRunDir);
			if (pop != null && pop.getPopulation() != null) {
				pops4log.add(pop);
				if (currentRunDir.lastIndexOf(DTGeneticMiner.GENERATION_TAG) > 0) {
					generation = Integer.parseInt(currentRunDir
							.substring(currentRunDir
									.lastIndexOf(DTGeneticMiner.GENERATION_TAG)
									+ DTGeneticMiner.GENERATION_TAG.length()));
					if (generation > maxGeneration) {
						maxGeneration = generation;
					}
					if (generation < minGeneration) {
						minGeneration = generation;
					}
					sumGenerations += generation;
					msGenerations.add(new Integer(generation));

				}
			}
		}

		perf = new Performance(FitnessFactory.getFitness(fitness, logReader,
				DTFitnessFactory.ALL_FITNESS_PARAMETERS), pops4log);

		logReader = null;

		writeReportLine(log, pops4log.size(), Double.toString(perf.getBFE()),
				Double.toString(perf.getWFE()), Double.toString(perf.getMBF()),
				Double.toString(perf.getNumRunsBFE()), Double.toString(perf
						.getNumRunsWFE()), maxGeneration, minGeneration,
				sumGenerations, DescriptiveStatistics.mode(msGenerations),
				msGenerations.toString(), perf.getBestIndividuals());

	}

	private void writeReportHeader() {
		StringBuffer sb = null;

		sb = new StringBuffer();
		sb.append("log").append(REPORT_SEPARATOR);
		sb.append("BFE").append(REPORT_SEPARATOR);
		sb.append("WFE").append(REPORT_SEPARATOR);
		sb.append("MBF").append(REPORT_SEPARATOR);
		sb.append("num of runs with BFE").append(REPORT_SEPARATOR);
		sb.append("num of runs with WFE").append(REPORT_SEPARATOR);
		sb.append("max gen").append(REPORT_SEPARATOR);
		sb.append("min gen").append(REPORT_SEPARATOR);
		sb.append("mean gen").append(REPORT_SEPARATOR);
		sb.append("mode").append(REPORT_SEPARATOR);
		sb.append("multiset gen").append(REPORT_SEPARATOR);
		sb.append("inputDirLogs").append(REPORT_SEPARATOR);
		sb.append("inputDirRuns").append(REPORT_SEPARATOR);
		sb.append("numRuns").append(REPORT_SEPARATOR);
		sb.append("fitness").append(REPORT_SEPARATOR);
		sb.append("bestIndividuals").append(REPORT_SEPARATOR);

		try {
			this.report.write(sb.toString());
			this.report.newLine();
		} catch (IOException ioe) {
			System.err.println("Could not write line = '" + sb.toString()
					+ "' to the report!");
		}

	}

	private void writeReportLine(String log, int numRuns, String BFE,
			String WFE, String MBF, String numRunsBFE, String numRunsWFE,
			int maxGeneration, int minGeneration, int sumGenerations, int mode,
			String multisetGenerations, StringBuffer bestIndividuals) {
		StringBuffer sb = null;

		sb = new StringBuffer();
		sb.append(log).append(REPORT_SEPARATOR);
		sb.append(BFE).append(REPORT_SEPARATOR);
		sb.append(WFE).append(REPORT_SEPARATOR);
		sb.append(MBF).append(REPORT_SEPARATOR);
		sb.append(numRunsBFE).append(REPORT_SEPARATOR);
		sb.append(numRunsWFE).append(REPORT_SEPARATOR);
		sb.append(maxGeneration).append(REPORT_SEPARATOR);
		sb.append(minGeneration).append(REPORT_SEPARATOR);
		if (numRuns > 0) {
			sb.append(sumGenerations / numRuns).append(REPORT_SEPARATOR);
		} else {
			sb.append("division by zero").append(REPORT_SEPARATOR);
		}
		sb.append(mode).append(REPORT_SEPARATOR);
		sb.append(multisetGenerations).append(REPORT_SEPARATOR);
		sb.append(this.inputDirLogs).append(REPORT_SEPARATOR);
		sb.append(this.inputDirRuns).append(REPORT_SEPARATOR);
		sb.append(numRuns).append(REPORT_SEPARATOR);
		sb.append(FitnessFactory.getAllFitnessTypes()[this.fitness]).append(
				REPORT_SEPARATOR);
		sb.append(bestIndividuals).append(REPORT_SEPARATOR);

		try {
			this.report.write(sb.toString());
			this.report.newLine();
		} catch (IOException ioe) {
			System.err.println("Could not write line = '" + sb.toString()
					+ "' to the report!");
		}

	}

	protected void finalize() {
		if (this.report != null) {
			try {
				this.report.flush();
				this.report.close();
			} catch (IOException ioe) {
				System.err.println("Could not close the report file!");
			}
		}

	}

	public static void main(String[] args) throws IOException {
		SummaryReport sr = null;

		Parameters param = null;

		param = new Parameters(args[0]);

		sr = new SummaryReport(param.getParameter("DIR_LOGS"), param
				.getParameter("DIR_INDIVIDUALS"), param
				.getParameter("LOGS_FILE_TERMINATION"), Integer.parseInt(param
				.getParameter("FITNESS_TYPE")), param
				.getParameter("OUTPUT_FILE"));
		sr.generateReport();

	}

}
