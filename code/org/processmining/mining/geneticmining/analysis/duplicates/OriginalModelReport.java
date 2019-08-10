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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.importing.heuristicsnet.HeuristicsNetFromFile;
import org.processmining.mining.geneticmining.analysis.Report;

/**
 *<p>
 * This class generates a report that contains the number of run in which one of
 * the models are like the original one. In other words, at least one individual
 * in the run is like the original model.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class OriginalModelReport implements Report {
	private File dirResults = null;
	private File dirOriginalModels = null;
	private String originalModelsFileTermination = null;
	private File report = null;
	private String reportSeparator = null;
	private String generationDirName = null;

	public OriginalModelReport(String dirOriginalModels,
			String originalModelsFileTermination, String dirResults,
			String generationDirName, String reportName,
			String reportFieldsSeparator) throws FileNotFoundException {

		// checking if the directories exist...
		this.dirOriginalModels = new File(dirOriginalModels);
		RetrieveFiles.checkIfDirectoryExists(this.dirOriginalModels);

		this.dirResults = new File(dirResults);
		RetrieveFiles.checkIfDirectoryExists(this.dirResults);

		this.originalModelsFileTermination = originalModelsFileTermination;
		this.generationDirName = generationDirName;

		this.report = new File(reportName);
		this.reportSeparator = reportFieldsSeparator;

	}

	/**
	 * This method generates the "complete model" report.
	 */
	public void generateReport() {

		File[] desiredSolutions = MethodsForFiles.getAllSubFiles(
				this.dirOriginalModels, this.originalModelsFileTermination);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.report));
			bw
					.write("File" + this.reportSeparator + "Total Number Runs"
							+ this.reportSeparator
							+ "Number Runs With Original Model (OM)"
							+ this.reportSeparator
							+ "OM individual is 1st best"
							+ this.reportSeparator
							+ "OM individual is among 5 best \n");

			for (int j = 0; j < desiredSolutions.length; j++) {

				File dirLogResults = new File(this.dirResults.getAbsolutePath()
						+ File.separator
						+ desiredSolutions[j].getName().substring(
								0,
								desiredSolutions[j].getName().indexOf(
										this.originalModelsFileTermination)));
				// System.out.println(dirLogResults);

				// getting the individuals for every run in every log
				File[] dirRunsPerLog = MethodsForFiles.getAllSubDirectories(
						dirLogResults, this.generationDirName);
				// System.out.println(dirRunsPerLog.length);
				HeuristicsNet originalModel = new HeuristicsNetFromFile(
						new FileInputStream(desiredSolutions[j])).getNet();
				int successfulRuns = 0;
				int equalIndividualIsFirst = 0;
				int equalIndividualIsAmongFiveBest = 0;
				for (int i = 0; i < dirRunsPerLog.length; i++) {
					// System.out.println(dirRunsPerLog[i]);

					int positionBestIndividualThatIsEqual = getPositionBestEqualIndividual(
							MethodsForFiles.getAllSubFiles(dirRunsPerLog[i],
									this.originalModelsFileTermination),
							originalModel);
					if (positionBestIndividualThatIsEqual >= 0) {
						successfulRuns++;
						if (positionBestIndividualThatIsEqual == 0) {
							equalIndividualIsFirst++;
							System.out.println("Equal individual = "
									+ dirRunsPerLog[i]);
						}

						if (positionBestIndividualThatIsEqual < 5) {
							equalIndividualIsAmongFiveBest++;
						}

					}
				}
				bw.write(dirLogResults.getName() + this.reportSeparator
						+ dirRunsPerLog.length + this.reportSeparator
						+ successfulRuns + this.reportSeparator
						+ equalIndividualIsFirst + this.reportSeparator
						+ equalIndividualIsAmongFiveBest + "\n");
				System.out.println("File = " + dirLogResults.getName()
						+ " => Successful runs = " + successfulRuns + "/"
						+ dirRunsPerLog.length + " (" + (double) successfulRuns
						/ dirRunsPerLog.length + "), first individual = "
						+ equalIndividualIsFirst + ", among 5 best = "
						+ equalIndividualIsAmongFiveBest);

			}
			bw.close();
		} catch (IOException ex) {
			System.err.println("Problems while generating report file: "
					+ this.report.getAbsolutePath());
		}

	}

	private int getPositionBestEqualIndividual(File[] individuals,
			HeuristicsNet originalModel) throws IOException {
		int indexBestEqualIndividual = -1;
		// ordering the individuals. We assume that they are written like
		// <name>_0, <name>_1, ..., <name>_n. The individuals number should be
		// in
		// ascending order. So, individual 0 had the worst fitness among the
		// individuals. Individual
		// n had the best one.
		Arrays.sort(individuals);

		for (int i = 0; i < individuals.length; i++) {
			HeuristicsNet currentIndividual = new HeuristicsNetFromFile(
					new FileInputStream(individuals[i])).getNet();
			if (originalModel.diffForSets(currentIndividual).trim().length() == 0) {
				indexBestEqualIndividual = individuals.length - (i + 1); // because
				// i
				// starts
				// at
				// zero...
			}

		}

		return indexBestEqualIndividual;
	}

	public static void main(String args[]) {
		try {
			OriginalModelReport cmr = new OriginalModelReport(args[0], args[1],
					args[2], args[3], args[4], args[5]);
			cmr.generateReport();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
