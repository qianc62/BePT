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

package org.processmining.mining.geneticmining.analysis.duplicates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.util.Parameters;
import org.processmining.mining.geneticmining.GeneticMinerConstants;

/**
 * <p>
 * Title: Average Computational Time for GA's Runs
 * </p>
 * 
 * <p>
 * Description: This class calculates the average computational time that runs
 * of a log file take.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class AverageComputationalTimeReport {

	private RetrieveFiles desiredSolutions = null;
	private File[][] foundLogsWithInformationAboutRuns = null;
	private double[] averageTimeAllRuns = null;
	private double[] averageTimePerGeneration = null;
	private double[] averageNumberOfGenerationsPerRun = null;

	private String reportFileName = null;
	private String fieldsSeparator = null;
	private String fieldsSeparatorRunLogs = "@";

	public AverageComputationalTimeReport(String dirSolutionModels,
			String fileTerminationSolutionModels, String dirFoundModels,
			String fileTerminationRunsLogs, String fieldsSeparatorRunLogs,
			String reportFileName, String fieldsSeparator)
			throws FileNotFoundException, IOException {

		// retrieving the desired solutions
		desiredSolutions = new RetrieveFiles(dirSolutionModels,
				fileTerminationSolutionModels, false);

		// retrieving the logs of every run of the genetic algorithm to an event
		// log file
		foundLogsWithInformationAboutRuns = new RetrieveFilesPerElement(
				desiredSolutions.getNames(), dirFoundModels,
				fileTerminationRunsLogs, false).getFiles();

		this.fieldsSeparatorRunLogs = fieldsSeparatorRunLogs;

		calculateAverageComputationalTimes();

		this.reportFileName = reportFileName;
		this.fieldsSeparator = fieldsSeparator;

		generateReport();

	}

	private void generateReport() throws IOException {

		// creating report file
		BufferedWriter bw = new BufferedWriter(new FileWriter(reportFileName));

		// writing header
		bw.write("Net" + fieldsSeparator + "Number of Runs" + fieldsSeparator
				+ "Average Time per Run (seconds)" + fieldsSeparator
				+ "Average Time per Generation (seconds)" + fieldsSeparator
				+ "Average Number of Generations");
		bw.newLine();

		// writing the calculated values for every net
		for (int i = 0; i < foundLogsWithInformationAboutRuns.length; i++) {
			bw.write(desiredSolutions.getNames()[i] + fieldsSeparator
					+ foundLogsWithInformationAboutRuns[i].length
					+ fieldsSeparator + averageTimeAllRuns[i] + fieldsSeparator
					+ averageTimePerGeneration[i] + fieldsSeparator
					+ averageNumberOfGenerationsPerRun[i] + fieldsSeparator);
			bw.newLine();
		}
		bw.flush();
		bw.close();

	}

	private void calculateAverageComputationalTimes()
			throws FileNotFoundException, IOException {

		averageTimeAllRuns = new double[foundLogsWithInformationAboutRuns.length];
		averageTimePerGeneration = new double[foundLogsWithInformationAboutRuns.length];
		averageNumberOfGenerationsPerRun = new double[foundLogsWithInformationAboutRuns.length];

		for (int i = 0; i < foundLogsWithInformationAboutRuns.length; i++) {

			// creating the variables to store the average computational times
			averageTimeAllRuns[i] = 0.0;
			averageTimePerGeneration[i] = 0.0;
			averageNumberOfGenerationsPerRun[i] = 0.0;

			// retrieving the files for a given event log
			for (int j = 0; j < foundLogsWithInformationAboutRuns[i].length; j++) {

				File runLog = foundLogsWithInformationAboutRuns[i][j];
				BufferedReader br = new BufferedReader(new FileReader(runLog));
				String line = "";
				String previousLine = "";

				while ((line = br.readLine()) != null) {
					previousLine = line;
				}

				String[] lineTokens = previousLine
						.split(this.fieldsSeparatorRunLogs);

				double totalTimeRun = Double
						.parseDouble(lineTokens[GeneticMinerConstants
								.getIndexConstantInLogLine("Elapsed Time(ms)")]);
				double numGenerationsRun = (Double
						.parseDouble(lineTokens[GeneticMinerConstants
								.getIndexConstantInLogLine("Generation Number")])) + 1; // because
				// it
				// starts
				// at
				// zero

				// keeping the partial average times
				averageTimeAllRuns[i] += totalTimeRun;
				averageTimePerGeneration[i] += totalTimeRun / numGenerationsRun;
				averageNumberOfGenerationsPerRun[i] += numGenerationsRun;

			}

			// calculating the final values for the average times
			averageTimeAllRuns[i] /= (double) foundLogsWithInformationAboutRuns[i].length;
			averageTimePerGeneration[i] /= (double) foundLogsWithInformationAboutRuns[i].length;
			averageNumberOfGenerationsPerRun[i] /= (double) foundLogsWithInformationAboutRuns[i].length;

			// now, dividing by 1000 to convert from milliseconds to seconds

			averageTimeAllRuns[i] /= 1000.0;
			averageTimePerGeneration[i] /= 1000.0;

		}
	}

	public static void main(String args[]) {

		Parameters p = new Parameters(args[0]);
		try {
			LogReaderFactory.setLogReaderClass(LogReaderClassic.class);
			System.out.println(p.getParameter("DIR_SOLUTION_MODELS") + "  "
					+ p.getParameter("FILE_TERMINATION_SOLUTION_MODELS") + "  "
					+ p.getParameter("DIR_FOUND_MODELS") + "  "
					+ p.getParameter("FILE_TERMINATION_RUNS_LOGS") + "  "
					+ p.getParameter("FIELDS_SEPARATOR_RUNS_LOGS") + "  "
					+ p.getParameter("REPORT_FILE_NAME") + "  "
					+ p.getParameter("REPORT_FIELDS_SEPARATOR"));

			AverageComputationalTimeReport actr = new AverageComputationalTimeReport(
					p.getParameter("DIR_SOLUTION_MODELS"), p
							.getParameter("FILE_TERMINATION_SOLUTION_MODELS"),
					p.getParameter("DIR_FOUND_MODELS"), p
							.getParameter("FILE_TERMINATION_RUNS_LOGS"), p
							.getParameter("FIELDS_SEPARATOR_RUNS_LOGS"), p
							.getParameter("REPORT_FILE_NAME"), p
							.getParameter("REPORT_FIELDS_SEPARATOR"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
