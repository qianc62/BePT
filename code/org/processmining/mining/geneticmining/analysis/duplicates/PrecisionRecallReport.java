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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.framework.util.Parameters;
import org.processmining.importing.heuristicsnet.HeuristicsNetFromFile;
import org.processmining.mining.geneticmining.analysis.Report;

/**
 * <p>
 * Title: Precision and Recall Report
 * </p>
 * 
 * <p>
 * Description: This class generates statistic information about mined models in
 * batch.
 * </p>
 * 
 * 
 * @author Ana Karla A. de Medeiros.
 * @version 1.0
 */
public class PrecisionRecallReport implements Report {

	private RetrieveFiles desiredSolutions = null;
	private String processInstance = "";
	private String[][] logs = null;
	private File[][] foundSolutions = null;
	private FitnessReport[][] properCompletionFitnessValues = null;
	private FitnessReport[][] improvedContinuousSemanticsFitnessValues = null;
	private PrecisionRecall[][] structuralPrecisionRecall = null;
	private PrecisionRecall[][] behavioralPrecisionRecall = null;
	private PrecisionRecall[][] duplicatesPrecisionRecall = null;

	private String reportFileName = null;
	private String fieldsSeparator = null;

	public PrecisionRecallReport(String dirSolutionModels,
			String fileTerminationSolutionModels, String dirFoundModels,
			String fileTerminationFoundModels, String dirLogs,
			String fileTerminationLogs, String processInstance,
			String reportFileName, String fieldsSeparator) throws Exception {

		// retrieving the desired solutions
		desiredSolutions = new RetrieveFiles(dirSolutionModels,
				fileTerminationSolutionModels, true);

		// retrieving the logs for the desired solutions
		logs = extractZipEntry(new RetrieveFilesPerElement(desiredSolutions
				.getNames(), dirLogs, fileTerminationLogs, true).getFiles());
		this.processInstance = processInstance;

		// retrieving the found solutions for the desired solutions
		foundSolutions = new RetrieveFilesPerElement(desiredSolutions
				.getNames(), dirFoundModels, fileTerminationFoundModels, true)
				.getFiles();

		calculatePrecisionRecall();
		this.reportFileName = reportFileName;
		this.fieldsSeparator = fieldsSeparator;
		generateReport();

	}

	public static String[][] extractZipEntry(File[][] files) {
		String[][] refinedFiles = new String[files.length][];
		for (int i = 0; i < files.length; i++) {
			refinedFiles[i] = new String[files[i].length];
			for (int j = 0; j < files[i].length; j++) {
				try {
					refinedFiles[i][j] = MethodsForFiles
							.extractFiles(files[i][j].getAbsolutePath())[0];
				} catch (IOException ex) {
					refinedFiles[i] = new String[0];
				}
			}

		}
		return refinedFiles;
	}

	public static LogReader createLogReader(String logFile,
			String processInstance) {
		DefaultLogFilter logFilter = new DefaultLogFilter(
				DefaultLogFilter.INCLUDE);
		logFilter.setProcess(processInstance);
		try {
			return LogReaderFactory.createInstance(logFilter, LogFile
					.getInstance(logFile));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static HeuristicsNet createNet(File netFile) throws IOException {
		return new HeuristicsNetFromFile(new FileInputStream(netFile)).getNet();
	}

	private void calculatePrecisionRecall() throws Exception {

		// Allocating enough space to store the precision and recall of every
		// found solution,
		// and also the fitness values
		structuralPrecisionRecall = new PrecisionRecall[foundSolutions.length][];
		duplicatesPrecisionRecall = new PrecisionRecall[foundSolutions.length][];
		behavioralPrecisionRecall = new PrecisionRecall[foundSolutions.length][];

		properCompletionFitnessValues = new FitnessReport[foundSolutions.length][];
		improvedContinuousSemanticsFitnessValues = new FitnessReport[foundSolutions.length][];

		for (int i = 0; i < foundSolutions.length; i++) {
			structuralPrecisionRecall[i] = new PrecisionRecall[foundSolutions[i].length];
			duplicatesPrecisionRecall[i] = new PrecisionRecall[foundSolutions[i].length];
			behavioralPrecisionRecall[i] = new PrecisionRecall[foundSolutions[i].length];

			properCompletionFitnessValues[i] = new FitnessReport[foundSolutions[i].length];
			improvedContinuousSemanticsFitnessValues[i] = new FitnessReport[foundSolutions[i].length];
		}

		// calculating the structural precision and recall for every found
		// solution,
		// as well as the fitness values.
		for (int i = 0; i < foundSolutions.length; i++) {
			// loading the desired solution

			HeuristicsNet hnSolution = createNet(desiredSolutions.getFiles()[i]);
			// loading the found solutions for this desired solution
			File[] foundSolutionForHnSolution = foundSolutions[i];

			LogReader logReader = null;
			if (logs[i].length > 0) {
				logReader = createLogReader(logs[i][0], processInstance);

				for (int j = 0; j < foundSolutionForHnSolution.length; j++) {

					HeuristicsNet hnFoundSolution = createNet(foundSolutionForHnSolution[j]);
					// System.out.println("Processing net = " +
					// desiredSolutions.getNames()[i]);

					try {
						structuralPrecisionRecall[i][j] = calculateStructuralPrecisionRecall(
								hnSolution, hnFoundSolution);

						duplicatesPrecisionRecall[i][j] = calculateDuplicatesPrecisionRecall(
								hnSolution, hnFoundSolution);

						behavioralPrecisionRecall[i][j] = calculateBehavioralPrecisionRecall(
								logReader, hnSolution, hnFoundSolution);

						properCompletionFitnessValues[i][j] = new FitnessReport(
								logReader, hnFoundSolution, 0);

						improvedContinuousSemanticsFitnessValues[i][j] = new FitnessReport(
								logReader, hnFoundSolution, 3);
					} catch (ArrayIndexOutOfBoundsException exception) {
						System.err
								.println("Could not calculate metrics for net '"
										+ foundSolutionForHnSolution[j]
												.toString()
										+ "'. "
										+ exception.getMessage());
					}
				}
			} else {
				System.err.println("No log for net = "
						+ desiredSolutions.getNames()[i]);
			}

		}

	}

	private static PrecisionRecall calculateBehavioralPrecisionRecall(
			LogReader logReader, HeuristicsNet baseHN, HeuristicsNet foundHN)
			throws Exception {

		return PrecisionRecallFactory.getPrecisionRecall(
				PrecisionRecallFactory.IND_BEHAVIORAL_PR, logReader, baseHN,
				foundHN);
	}

	/*
	 * Calculates structural precision and recall based on two nets: the base
	 * solution and the found solution.
	 */
	private static PrecisionRecall calculateStructuralPrecisionRecall(
			HeuristicsNet baseHN, HeuristicsNet foundHN) throws Exception {

		return PrecisionRecallFactory
				.getPrecisionRecall(PrecisionRecallFactory.IND_STRUCTURAL_PR,
						null, baseHN, foundHN);
	}

	/*
	 * Calculates duplicates precision and recall based on two nets: the base
	 * solution and the found solution.
	 */
	private static PrecisionRecall calculateDuplicatesPrecisionRecall(
			HeuristicsNet baseHN, HeuristicsNet foundHN) throws Exception {

		return PrecisionRecallFactory
				.getPrecisionRecall(PrecisionRecallFactory.IND_DUPLICATES_PR,
						null, baseHN, foundHN);
	}

	public void generateReport() {
		try {

			BufferedWriter report = new BufferedWriter(new FileWriter(
					this.reportFileName));
			BufferedWriter reportAllValues = new BufferedWriter(new FileWriter(
					this.reportFileName + "_allPrecisionRecallValues.txt"));

			// writing header
			report.write("Net" + this.fieldsSeparator + "Number of Runs"
					+ this.fieldsSeparator + "Number of Valid Runs"
					+ this.fieldsSeparator + "Average Fitness"
					+ this.fieldsSeparator
					+ "Average Proper Completion Fitness"
					+ this.fieldsSeparator + "Number of Complete Models"
					+ this.fieldsSeparator
					+ "Number of Complete and Precise Models"
					+ this.fieldsSeparator + "% of Complete Models"
					+ this.fieldsSeparator
					+ "% of Complete Models that are Precise"
					+ this.fieldsSeparator + "Average Structural Precision"
					+ this.fieldsSeparator + "Average Structural Recall"
					+ this.fieldsSeparator + "Average Duplicates Precision"
					+ this.fieldsSeparator + "Average Duplicates Recall"
					+ this.fieldsSeparator + "Average Behavioral Precision"
					+ this.fieldsSeparator + "Average Behavioral Recall");
			report.write("\n");

			// writing header
			reportAllValues.write("Net" + this.fieldsSeparator + "All values");
			reportAllValues.write("\n");

			// calculating the average and best precision and recall for every
			// found solution
			for (int i = 0; i < foundSolutions.length; i++) {

				double averageStructPrecision = 0;
				double averageStructRecall = 0;
				MyDoubleMultiset msStructPrecision = new MyDoubleMultiset();
				MyDoubleMultiset msStructRecall = new MyDoubleMultiset();

				double averageDuplicatesPrecision = 0;
				double averageDuplicatesRecall = 0;
				MyDoubleMultiset msDuplicatesPrecision = new MyDoubleMultiset();
				MyDoubleMultiset msDuplicatesRecall = new MyDoubleMultiset();

				double averageBehavPrecision = 0;
				double averageBehavRecall = 0;
				double averageProperCompletion = 0;
				double numberProperCompleteAndPreciseModels = 0;
				double numberProperCompleteModels = 0;
				MyDoubleMultiset msBehavPrecision = new MyDoubleMultiset();
				MyDoubleMultiset msBehavRecall = new MyDoubleMultiset();

				MyDoubleMultiset msProperCompletion = new MyDoubleMultiset();
				MyDoubleMultiset msImprovedContinuousSemantics = new MyDoubleMultiset();

				for (int j = 0; j < foundSolutions[i].length; j++) {

					if ((structuralPrecisionRecall[i][j] != null)
							&& (behavioralPrecisionRecall[i][j] != null)
							&& (duplicatesPrecisionRecall[i][j] != null)) {

						// structural precision
						averageStructPrecision += structuralPrecisionRecall[i][j]
								.getPrecision();
						msStructPrecision.add(structuralPrecisionRecall[i][j]
								.getPrecision());

						averageStructRecall += structuralPrecisionRecall[i][j]
								.getRecall();
						msStructRecall.add(structuralPrecisionRecall[i][j]
								.getRecall());

						// duplicates precision
						averageDuplicatesPrecision += duplicatesPrecisionRecall[i][j]
								.getPrecision();
						msDuplicatesPrecision
								.add(duplicatesPrecisionRecall[i][j]
										.getPrecision());

						averageDuplicatesRecall += duplicatesPrecisionRecall[i][j]
								.getRecall();
						msDuplicatesRecall.add(duplicatesPrecisionRecall[i][j]
								.getRecall());

						// behavioral precision
						averageBehavPrecision += behavioralPrecisionRecall[i][j]
								.getPrecision();
						msBehavPrecision.add(behavioralPrecisionRecall[i][j]
								.getPrecision());

						averageBehavRecall += behavioralPrecisionRecall[i][j]
								.getRecall();
						msBehavRecall.add(behavioralPrecisionRecall[i][j]
								.getRecall());

						msProperCompletion
								.add(properCompletionFitnessValues[i][j]
										.getFitness());
						msImprovedContinuousSemantics
								.add(improvedContinuousSemanticsFitnessValues[i][j]
										.getFitness());

						averageProperCompletion += properCompletionFitnessValues[i][j]
								.getFitness();

						if (properCompletionFitnessValues[i][j].getFitness() == 1.0
								&& behavioralPrecisionRecall[i][j]
										.getPrecision() == 1.0) {
							numberProperCompleteAndPreciseModels++;
							numberProperCompleteModels++;
						} else if (properCompletionFitnessValues[i][j]
								.getFitness() == 1.0) {
							numberProperCompleteModels++;
						}
					}
				}
				averageStructPrecision /= msStructPrecision.size();
				averageStructRecall /= msStructRecall.size();
				averageBehavPrecision /= msBehavPrecision.size();
				averageBehavRecall /= msStructRecall.size();
				averageProperCompletion /= msProperCompletion.size();

				// writing entry in the report

				report.write(
				// "Net"
						desiredSolutions.getNames()[i]
								+ this.fieldsSeparator
								+
								// "Number of Runs"
								foundSolutions[i].length
								+ this.fieldsSeparator
								+
								// "Number of Valid Runs"
								msStructPrecision.size()
								+ this.fieldsSeparator
								+
								// "Average Fitness"
								msImprovedContinuousSemantics.getAverage()
								+ this.fieldsSeparator
								+
								// "Average Proper Completion Fitness"
								averageProperCompletion
								+ this.fieldsSeparator
								+
								// "Number of Complete Models"
								numberProperCompleteModels
								+ this.fieldsSeparator
								+
								// "Number of Complete and Precise Models"
								numberProperCompleteAndPreciseModels
								+ this.fieldsSeparator
								+
								// "% of Complete Models"
								numberProperCompleteModels
										/ msStructPrecision.size()
								+ this.fieldsSeparator
								+
								// "% of Complete Models that are Precise"
								numberProperCompleteAndPreciseModels
										/ numberProperCompleteModels
								+ this.fieldsSeparator
								+
								// "Average Structural Precision"
								averageStructPrecision
								+ this.fieldsSeparator
								+
								// "Average Structural Recall"
								averageStructRecall
								+ this.fieldsSeparator
								+
								// "Average Duplicates Precision"
								msDuplicatesPrecision.getAverage()
								+ this.fieldsSeparator
								+
								// "Average Duplicates Recall"
								msDuplicatesRecall.getAverage()
								+ this.fieldsSeparator +
								// "Average Behavioral Precision"
								averageBehavPrecision + this.fieldsSeparator +
								// "Average Behavioral Recall"
								averageBehavRecall);

				report.write("\n");

				// structural precision/recall
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Structural Precision",
						msStructPrecision);
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Structural Recall", msStructRecall);

				// behavioral precisition/recall
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Behavioral Precision",
						msBehavPrecision);
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Behavioral Recall", msBehavRecall);

				// duplicates precisition/recall
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Duplicates Precision",
						msDuplicatesPrecision);
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Duplicates Recall",
						msDuplicatesRecall);

				// fitness
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Proper Completion",
						msProperCompletion);
				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - All Values of Improved Continuous Semantics",
						msImprovedContinuousSemantics);

				writeLineToReportAllValues(reportAllValues, desiredSolutions
						.getNames()[i]
						+ " - Individuals", foundSolutions[i]);

				reportAllValues.write("\n");

			}
			report.close();
			reportAllValues.close();
		} catch (IOException ioe) {
			System.err.println("Problems while creating the file: "
					+ this.reportFileName);
		}
	}

	private void writeLineToReportAllValues(BufferedWriter reportAllValues,
			String netDescription, File[] individuals) throws IOException {

		reportAllValues.write(
		// "Net"
				netDescription + this.fieldsSeparator +
				// "All values"
						buildLogLineForArrayFile(individuals));
		reportAllValues.write("\n");

	}

	private String buildLogLineForArrayFile(File[] files) {
		StringBuffer line = new StringBuffer();
		for (int i = 0; i < files.length; i++) {
			line.append(files[i].getAbsolutePath()).append(fieldsSeparator);
		}
		return line.toString();

	}

	private void writeLineToReportAllValues(BufferedWriter reportAllValues,
			String netDescription, MyDoubleMultiset ms) throws IOException {

		reportAllValues.write(
		// "Net"
				netDescription + this.fieldsSeparator +
				// "All values"
						buildLogLineForMyMultiset(ms));
		reportAllValues.write("\n");

	}

	private String buildLogLineForMyMultiset(MyDoubleMultiset ms) {
		StringBuffer line = new StringBuffer();
		line.append(ms.toString().replace('[', ' ').replace(']', ' ').trim()
				.replaceAll(",", fieldsSeparator));
		return line.toString();

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
					+ p.getParameter("REPORT_FILE_NAME") + "  "
					+ p.getParameter("REPORT_FIELDS_SEPARATOR"));

			PrecisionRecallReport prr = new PrecisionRecallReport(p
					.getParameter("DIR_SOLUTION_MODELS"), p
					.getParameter("FILE_TERMINATION_SOLUTION_MODELS"), p
					.getParameter("DIR_FOUND_MODELS"), p
					.getParameter("FILE_TERMINATION_FOUND_MODELS"), p
					.getParameter("DIR_LOGS"), p
					.getParameter("FILE_TERMINATION_LOGS"), p
					.getParameter("PROCESS_INSTANCE"), p
					.getParameter("REPORT_FILE_NAME"), p
					.getParameter("REPORT_FIELDS_SEPARATOR"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

/**
 * 
 * Implements a multiset of <code>double</code> that is backed by
 * <code>LinkedList</code>. The advantage is that the order of insertion in the
 * multiset is preserved.
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
class MyDoubleMultiset {
	LinkedList list = null;

	public MyDoubleMultiset() {
		list = new LinkedList();
	}

	public boolean add(double d) {
		return list.add(new Double(d));
	}

	public int size() {
		return list.size();
	}

	public int getQuantity(double d) {
		int quantity = 0;
		Double obj = new Double(d);
		Iterator i = list.iterator();
		while (i.hasNext()) {
			if (i.next().equals(obj)) {
				quantity++;

			}
		}
		return quantity;
	}

	public double getBestValue() {
		Double d = new Double(Double.NaN);
		if (list.size() > 0) {
			d = (Double) list.getFirst();
			Iterator i = list.iterator();
			while (i.hasNext()) {
				Double currentDouble = (Double) i.next();
				if (d.compareTo(currentDouble) < 0) {
					d = currentDouble;
				}
			}

		}
		return d.doubleValue();
	}

	public double getWorstValue() {
		Double d = new Double(Double.NaN);
		if (list.size() > 0) {
			d = (Double) list.getFirst();
			Iterator i = list.iterator();
			while (i.hasNext()) {
				Double currentDouble = (Double) i.next();
				if (d.compareTo(currentDouble) > 0) {
					d = currentDouble;
				}
			}

		}
		return d.doubleValue();
	}

	public String toString() {
		return list.toString();
	}

	public double getAverage() {
		double average = 0.0;
		if (list.size() > 0) {
			Iterator i = list.iterator();
			while (i.hasNext()) {
				average += ((Double) i.next()).doubleValue();
			}
			average /= list.size();
		}
		return average;
	}
}
