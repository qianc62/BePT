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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.processmining.analysis.genetic.PruneNoisyArcs;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.Parameters;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;

/**
 * <p>
 * Title: Prune Mined Models in Batch
 * </p>
 * 
 * <p>
 * Description: This class can prune arcs from mined models that are stored in a
 * certain directory of the computer. It works pretty much like the
 * "Prune Arcs Analysis Plug-in" of the ProM tool. The main difference is that
 * the whole pruning process is executed in batch.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class PruneMinedModelInBatch {

	private RetrieveFiles desiredSolutions = null;
	private String processInstance = "";
	private String[][] logs = null;
	private File[][] foundSolutionsFiles = null;
	private HeuristicsNet[][] parsedSolutions = null;
	private String fileTerminationFoundModels = null;

	private double pruningPercentage = 0.0d;
	private int fitnessType = FitnessFactory.IMPROVED_CONTINUOUS_SEMANTICS_INDEX;
	private File outputDirPrunedModels = null;

	public PruneMinedModelInBatch(String dirSolutionModels,
			String fileTerminationSolutionModels, String dirFoundModels,
			String fileTerminationFoundModels, String dirLogs,
			String fileTerminationLogs, String processInstance,
			double pruningPercentage, int fitnessType,
			String outputDirPrunedModels) throws FileNotFoundException,
			IOException {

		// retrieving the desired solutions
		desiredSolutions = new RetrieveFiles(dirSolutionModels,
				fileTerminationSolutionModels, true);

		// retrieving the logs for the desired solutions
		logs = PrecisionRecallReport
				.extractZipEntry(new RetrieveFilesPerElement(desiredSolutions
						.getNames(), dirLogs, fileTerminationLogs, true)
						.getFiles());

		// retrieving the found solutions for the desired solutions
		foundSolutionsFiles = new RetrieveFilesPerElement(desiredSolutions
				.getNames(), dirFoundModels, fileTerminationFoundModels, true)
				.getFiles();

		// allocating space for the parsed models
		parsedSolutions = new HeuristicsNet[foundSolutionsFiles.length][];
		for (int i = 0; i < foundSolutionsFiles.length; i++) {
			parsedSolutions[i] = new HeuristicsNet[foundSolutionsFiles[i].length];
		}

		// initializing the remaing variables
		this.fileTerminationFoundModels = fileTerminationFoundModels;
		this.processInstance = processInstance;
		this.pruningPercentage = pruningPercentage;
		this.fitnessType = fitnessType;
		this.outputDirPrunedModels = new File(outputDirPrunedModels);
		if (!this.outputDirPrunedModels.exists()) {
			// creating the directory
			if (!this.outputDirPrunedModels.mkdirs()) {
				throw new IOException("The directory '" + outputDirPrunedModels
						+ "' could not be created! Aborting the program...");
			}
		} else if (!this.outputDirPrunedModels.isDirectory()) {
			// checking if it is indeed a directory
			throw new IOException("The '" + outputDirPrunedModels
					+ "' is not a directory! Aborting the program...");

		}

		// pruning the models
		pruneModels();

		// writing the models to the output directory
		writePrunedModels();

	}

	private void pruneModels() throws IOException {

		// calculate fitness for every found solution
		// and prune the arcs from the model

		for (int i = 0; i < foundSolutionsFiles.length; i++) {
			if (logs[i].length > 0) {
				// if there are logs for this solution, do.
				LogReader logReader = logReader = PrecisionRecallReport
						.createLogReader(logs[i][0], processInstance);
				// loading the found solutions for this desired solution
				File[] foundSolutionForHnSolution = foundSolutionsFiles[i];
				for (int j = 0; j < foundSolutionForHnSolution.length; j++) {
					HeuristicsNet hnFoundSolution = PrecisionRecallReport
							.createNet(foundSolutionForHnSolution[j]);

					// calculating the fitness
					parsedSolutions[i][j] = new FitnessReport(logReader,
							hnFoundSolution, fitnessType).getNet();

					// pruning the arcs
					parsedSolutions[i][j]
							.disconnectArcsUsedBelowThreshold(PruneNoisyArcs
									.calculatePruningThreshold(
											parsedSolutions[i][j],
											this.pruningPercentage));

				}
			}
		}
	}

	private void writePrunedModels() throws IOException {

		// creating the subdirectories based on the desired solutions
		for (int i = 0; i < desiredSolutions.getNames().length; i++) {
			File subDirToPrunedModels = new File(this.outputDirPrunedModels,
					desiredSolutions.getNames()[i]);

			// checking if the directory indeed exists.
			if (!subDirToPrunedModels.exists()) {
				if (!subDirToPrunedModels.mkdirs()) {
					System.err.println("Could not create the directory '"
							+ subDirToPrunedModels.getAbsolutePath() + "'.");
					continue; // directory does not exist, go to next iteration
				}

			}

			// writing to the directory
			for (int j = 0; j < parsedSolutions[i].length; j++) {
				try {
					parsedSolutions[i][j].toFile(new FileOutputStream(
							subDirToPrunedModels.getAbsolutePath()
									+ File.separator + "ind_" + j
									+ fileTerminationFoundModels));

				} catch (IOException ioe) {
					System.err
							.println("Warning >>> Could not log the pruned individual for  the run "
									+ foundSolutionsFiles[i][j]
											.getAbsolutePath());
				}
			}

		}
	}

	public static void main(String args[]) {

		Parameters p = new Parameters(args[0]);
		try {
			LogReaderFactory.setLogReaderClass(LogReaderClassic.class);
			System.out.println(p.getParameter("DIR_SOLUTION_MODELS") + "  "
					+ p.getParameter("FILE_TERMINATION_SOLUTION_MODELS") + "  "
					+ p.getParameter("DIR_FOUND_MODELS") + "  "
					+ p.getParameter("FILE_TERMINATION_FOUND_MODELS") + "  "
					+ p.getParameter("DIR_LOGS") + "  "
					+ p.getParameter("FILE_TERMINATION_LOGS") + "  "
					+ p.getParameter("PROCESS_INSTANCE") + "  "
					+ p.getDoubleParameter("PRUNING_PERCENTAGE") + "  "
					+ p.getIntParameter("FITNESS_TYPE") + "  "
					+ p.getParameter("OUT_DIR_PRUNED_MODELS"));

			PruneMinedModelInBatch prr = new PruneMinedModelInBatch(p
					.getParameter("DIR_SOLUTION_MODELS"), p
					.getParameter("FILE_TERMINATION_SOLUTION_MODELS"), p
					.getParameter("DIR_FOUND_MODELS"), p
					.getParameter("FILE_TERMINATION_FOUND_MODELS"), p
					.getParameter("DIR_LOGS"), p
					.getParameter("FILE_TERMINATION_LOGS"), p
					.getParameter("PROCESS_INSTANCE"), p
					.getDoubleParameter("PRUNING_PERCENTAGE"), p
					.getIntParameter("FITNESS_TYPE"), p
					.getParameter("OUT_DIR_PRUNED_MODELS"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
