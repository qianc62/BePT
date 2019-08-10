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
import java.util.Arrays;

import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.util.Parameters;
import org.processmining.mining.geneticmining.GeneticMinerConstants;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */
public class DoubleFieldValuesPerGeneration {

	private RetrieveFiles desiredSolutions = null;
	private File[][] foundLogsWithInformationAboutRuns = null;
	private int[] listOfGenerationsToConsider = null;
	private double[][] averageFieldValuesPerGeneration = null;
	private double[][] numberOfFoundGenerations = null;
	private String fieldColumnName = null;
	private String generationNumberColumnName = null;

	private String reportFileName = null;
	private String fieldsSeparator = null;
	private String fieldsSeparatorRunLogs = "@";

	public DoubleFieldValuesPerGeneration(String dirSolutionModels,
			String fileTerminationSolutionModels, String dirFoundModels,
			String fileTerminationRunsLogs, String fieldsSeparatorRunLogs,
			String reportFileName, String fieldsSeparator,
			String generationNumberColumnName, int[] generationsToConsider,
			String fieldColumnName) throws FileNotFoundException, IOException {

		// retrieving the desired solutions
		desiredSolutions = new RetrieveFiles(dirSolutionModels,
				fileTerminationSolutionModels, false);

		// retrieving the logs of every run of the genetic algorithm to an event
		// log file
		foundLogsWithInformationAboutRuns = new RetrieveFilesPerElement(
				desiredSolutions.getNames(), dirFoundModels,
				fileTerminationRunsLogs, false).getFiles();

		this.fieldsSeparatorRunLogs = fieldsSeparatorRunLogs;
		this.fieldColumnName = fieldColumnName;
		this.generationNumberColumnName = generationNumberColumnName;

		this.listOfGenerationsToConsider = generationsToConsider;
		Arrays.sort(this.listOfGenerationsToConsider); // ordering the
		// generations

		calculateDoubleValuesForField();

		this.reportFileName = reportFileName;
		this.fieldsSeparator = fieldsSeparator;

		generateReport();

	}

	private void calculateDoubleValuesForField() throws FileNotFoundException,
			IOException {

		averageFieldValuesPerGeneration = new double[foundLogsWithInformationAboutRuns.length][listOfGenerationsToConsider.length];

		numberOfFoundGenerations = new double[foundLogsWithInformationAboutRuns.length][listOfGenerationsToConsider.length];

		for (int i = 0; i < foundLogsWithInformationAboutRuns.length; i++) {

			// retrieving the files for a given event log
			for (int j = 0; j < foundLogsWithInformationAboutRuns[i].length; j++) {

				File runLog = foundLogsWithInformationAboutRuns[i][j];
				BufferedReader br = new BufferedReader(new FileReader(runLog));
				String line = "";

				int indexListGenerations = 0;

				while ((line = br.readLine()) != null) {
					if (line.indexOf(generationNumberColumnName) < 0) {
						String[] lineTokens = line
								.split(this.fieldsSeparatorRunLogs);
						int numGenerationsRun = (Integer
								.parseInt(lineTokens[GeneticMinerConstants
										.getIndexConstantInLogLine(generationNumberColumnName)]));
						if (numGenerationsRun == this.listOfGenerationsToConsider[indexListGenerations]) {
							averageFieldValuesPerGeneration[i][indexListGenerations] += Double
									.parseDouble(lineTokens[GeneticMinerConstants
											.getIndexConstantInLogLine(this.fieldColumnName)]);
							numberOfFoundGenerations[i][indexListGenerations] += 1;

							if (indexListGenerations >= (listOfGenerationsToConsider.length - 1)) {
								// all the elements in the list of generations
								// have been considered!
								break;
							} else {
								// increment to the next generation in the list
								indexListGenerations++;
							}
						} else if (numGenerationsRun > this.listOfGenerationsToConsider[indexListGenerations]) {
							// testing if the other generations in the list of
							// generations to consider (variable
							// 'listOfGenerationsToConsider') can be equal to
							// the current generation in the file

							for (int index = indexListGenerations; index < listOfGenerationsToConsider.length; index++) {
								if (numGenerationsRun == this.listOfGenerationsToConsider[index]) {
									averageFieldValuesPerGeneration[i][indexListGenerations] += Double
											.parseDouble(lineTokens[GeneticMinerConstants
													.getIndexConstantInLogLine(this.fieldColumnName)]);
									numberOfFoundGenerations[i][indexListGenerations] += 1;

									indexListGenerations = index + 1;

									break;
								}
								if (indexListGenerations >= listOfGenerationsToConsider.length) {
									break; // all the list of generation has
									// been considered
								}
							}
						}
					}
				}
			}
		}
	}

	private void generateReport() throws IOException {
		// creating report file
		BufferedWriter bw = new BufferedWriter(new FileWriter(reportFileName));

		// writing header
		bw.write("Net" + fieldsSeparator + "Selected Field Name");
		for (int i = 0; i < this.listOfGenerationsToConsider.length; i++) {
			bw.write(fieldsSeparator + "gen"
					+ this.listOfGenerationsToConsider[i]);
		}
		bw.newLine();

		// writing the calculated values for every net
		for (int i = 0; i < foundLogsWithInformationAboutRuns.length; i++) {
			bw.write(desiredSolutions.getNames()[i] + fieldsSeparator
					+ this.fieldColumnName);
			for (int j = 0; j < this.listOfGenerationsToConsider.length; j++) {
				double value = (numberOfFoundGenerations[i][j] > 0.0 ? (this.averageFieldValuesPerGeneration[i][j] / numberOfFoundGenerations[i][j])
						: 0.0);
				bw.write(fieldsSeparator + value);
			}

			bw.newLine();
		}
		bw.flush();
		bw.close();

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
					+ p.getParameter("REPORT_FIELDS_SEPARATOR") + "  "
					+ p.getArrayIntegerParameter("GENERATIONS_TO_CONSIDER"));

			DoubleFieldValuesPerGeneration fvpg = new DoubleFieldValuesPerGeneration(
					p.getParameter("DIR_SOLUTION_MODELS"), p
							.getParameter("FILE_TERMINATION_SOLUTION_MODELS"),
					p.getParameter("DIR_FOUND_MODELS"), p
							.getParameter("FILE_TERMINATION_RUNS_LOGS"), p
							.getParameter("FIELDS_SEPARATOR_RUNS_LOGS"), p
							.getParameter("REPORT_FILE_NAME"), p
							.getParameter("REPORT_FIELDS_SEPARATOR"), p
							.getParameter("GENERATION_NUMBER_COLUMN_NAME"),
					p.getArrayIntegerParameter("GENERATIONS_TO_CONSIDER"), p
							.getParameter("NAME_COLUMN_FIELD_TO_CONSIDER"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
