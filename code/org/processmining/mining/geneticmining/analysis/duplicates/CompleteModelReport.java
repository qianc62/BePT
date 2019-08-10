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
import java.util.Vector;

import org.processmining.framework.util.MethodsForFiles;
import org.processmining.mining.geneticmining.analysis.Report;

/**
 * <p>
 * Title: Complete Model's Report
 * </p>
 * 
 * <p>
 * Description: This class generates a report that contains the number of run in
 * which one of the models proper completes. In other words, at least one
 * individual in the run can parse all the traces in the respective input log.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class CompleteModelReport implements Report {
	private File dirResults = null;
	private String properCompletionFileName = null;
	private String columnSeparator = null;
	private int headerSize = 0;
	private File report = null;
	private String reportSeparator = null;

	public CompleteModelReport(String dirResults,
			String fileWithProperCompletionResults,
			int properCompletionHeaderSize,
			String properCompletionColumnSeparator, String reportName,
			String reportFieldsSeparator) throws FileNotFoundException {

		this.dirResults = new File(dirResults);

		if (!this.dirResults.exists()) {
			throw new FileNotFoundException("The directory \"" + dirResults
					+ "\" does not exist!");
		}

		this.properCompletionFileName = fileWithProperCompletionResults;

		this.headerSize = properCompletionHeaderSize;

		this.columnSeparator = properCompletionColumnSeparator;

		this.report = new File(reportName);
		this.reportSeparator = reportFieldsSeparator;

	}

	/**
	 * This method generates the "complete model" report.
	 */
	public void generateReport() {

		File[] firstLevelSubdirectories = MethodsForFiles
				.getDirectories(dirResults);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.report));
			bw
					.write("File" + this.reportSeparator + "Total Number Runs"
							+ this.reportSeparator
							+ "Number Runs Successful Proper Completion"
							+ this.reportSeparator
							+ "PC individual is 1st best"
							+ this.reportSeparator
							+ "PC individual is among 5 best \n");

			for (int j = 0; j < firstLevelSubdirectories.length; j++) {
				File[] properCompletionFiles = properCompletionFilesForLog(firstLevelSubdirectories[j]);
				int successfulRuns = 0;
				int bestIndividualIsFirst = 0;
				int bestIndividualsIsAmongFiveBest = 0;
				for (int i = 0; i < properCompletionFiles.length; i++) {
					int positionBestIndividualThatProperCompletes = getPositionProperCompletion(properCompletionFiles[i]);
					if (positionBestIndividualThatProperCompletes >= 0) {
						successfulRuns++;
						if (positionBestIndividualThatProperCompletes == 0) {
							bestIndividualIsFirst++;
						}

						if (positionBestIndividualThatProperCompletes < 5) {
							bestIndividualsIsAmongFiveBest++;
						}

					}
				}
				bw.write(firstLevelSubdirectories[j].getName()
						+ this.reportSeparator + properCompletionFiles.length
						+ this.reportSeparator + successfulRuns
						+ this.reportSeparator + bestIndividualIsFirst
						+ this.reportSeparator + bestIndividualsIsAmongFiveBest
						+ "\n");
				System.out.println("File = "
						+ firstLevelSubdirectories[j].getName()
						+ " => Successful runs = " + successfulRuns + "/"
						+ properCompletionFiles.length + " ("
						+ (double) successfulRuns
						/ properCompletionFiles.length
						+ "), first individual = " + bestIndividualIsFirst
						+ ", among 5 best = " + bestIndividualsIsAmongFiveBest);
			}
			bw.close();

		} catch (IOException ex) {
			System.err.println("Problems while generating report file: "
					+ this.report.getAbsolutePath());
		}

	}

	/*
	 * Returns the position of the best individual that proper completes. This
	 * method assumes that the individuals are logged in incremental value for
	 * the fitness measure in the proper completion file. The example of the
	 * assumed format is: <br> Individual@Fitness@FitnessType <br/>
	 * 0@1.0@ProperCompletion<br/> 1@1.0@ProperCompletion<br/> ... (more lines)
	 * <br/>
	 * 
	 * @param file File file with the proper completion fitness values per
	 * individual.
	 * 
	 * @return int position of the first individual that proper completes. The
	 * positions go from 0 to n, where n is the number of individuals.
	 */
	private int getPositionProperCompletion(File file) {
		int distanceToLastIndividualInTheFile = -1;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			// skipping the header
			for (int i = 0; i < this.headerSize; i++) {
				br.readLine();

			}

			// reading the fitness values
			String line;
			int bestPositionSoFar = -1;
			while ((line = br.readLine()) != null) {
				int indexSeparator = line.indexOf(this.columnSeparator);
				int currentIndividualPosition = Integer.parseInt(line
						.substring(0, indexSeparator));
				double fitness = Double.parseDouble(line.substring(
						indexSeparator + this.columnSeparator.length(), line
								.indexOf(this.columnSeparator, indexSeparator
										+ this.columnSeparator.length())));
				if (fitness == 1.0) {
					if (bestPositionSoFar < currentIndividualPosition) {
						bestPositionSoFar = currentIndividualPosition;
						distanceToLastIndividualInTheFile = 0;
					} else {
						distanceToLastIndividualInTheFile++;
					}
				}
				// else{
				// System.out.println("Proper Completion < 1 !! -> " +
				// file.getAbsolutePath());
				// }
			}

			br.close();
		} catch (IOException ex) {
		}

		return distanceToLastIndividualInTheFile;
	}

	private File[] properCompletionFilesForLog(File inputLogDir) {

		File[] pcFiles = new File[0];

		// filtering the "this.properCompletionFileName" files

		File[] files = MethodsForFiles.getAllSubFiles(inputLogDir,
				this.properCompletionFileName);
		int numRuns = 0;
		Vector tempPcFiles = new Vector();

		for (int i = 0; i < files.length; i++) {
			numRuns++;
			tempPcFiles.add(files[i]);

		}

		// filling in the pcFiles...
		pcFiles = new File[numRuns];
		for (int i = 0; i < pcFiles.length; i++) {
			pcFiles[i] = (File) tempPcFiles.get(i);
		}

		return pcFiles;

	}

	public static void main(String args[]) throws FileNotFoundException {
		CompleteModelReport cmr = new CompleteModelReport(args[0], args[1],
				Integer.parseInt(args[2]), args[3], args[4], args[5]);
		cmr.generateReport();

	}
}
