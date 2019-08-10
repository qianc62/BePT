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

package org.processmining.mining.geneticmining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import org.processmining.exporting.heuristicsNet.HnExport;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.MessageConsole;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.ProgressDummy;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.framework.util.Parameters;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.geneticmining.analysis.DescriptiveStatistics;
import org.processmining.mining.geneticmining.duplicates.DTGeneticMiner;
import org.processmining.mining.geneticmining.duplicates.DTGeneticMinerResult;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.fitness.FitnessFactory;
import org.processmining.mining.geneticmining.geneticoperations.Crossover;
import org.processmining.mining.geneticmining.geneticoperations.CrossoverFactory;
import org.processmining.mining.geneticmining.geneticoperations.Mutation;
import org.processmining.mining.geneticmining.geneticoperations.MutationFactory;
import org.processmining.mining.geneticmining.population.BuildPopulation;
import org.processmining.mining.geneticmining.population.InitialPopulationFactory;
import org.processmining.mining.geneticmining.population.NextPopulationFactory;
import org.processmining.mining.geneticmining.selection.SelectionMethod;
import org.processmining.mining.geneticmining.selection.duplicates.DTSelectionMethodFactory;
import org.processmining.mining.geneticmining.util.MethodsOverIndividuals;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class RealGeneticMiner implements MiningPlugin {

	public GeneticMinerUI ui = null;
	private LogReader logReader = null;
	private GeneticMinerSettings settings = null;
	private Progress progress = null;

	public HeuristicsNet[] population = null;
	private Random generator = null;

	private BuildPopulation initialPopulation = null;
	private BuildPopulation nextGeneration = null;

	private Fitness fitness = null;
	private Crossover crossover = null;
	private Mutation mutation = null;
	private SelectionMethod selectionMethod = null;

	private RandomAccessFile raf = null;
	private String reportColumnSeparator = "@";
	private Hashtable values = null;

	private boolean logIndividuals = false;
	private int logIndividualsInterval = 1;
	private boolean logLastGeneration = false;
	private int numIndividualsToLog = 1;
	private File outputDirForIndividuals = null;

	private long startTime = 0;

	public RealGeneticMiner() {

	}

	public RealGeneticMiner(String fileDir, String fileName,
			String columnSeparator, int numIndividualsToLog,
			boolean logIndividuals, int logIndividualsInterval,
			boolean logLastGeneration, String outputDirForIndividuals)
			throws IOException {

		String outputFile = null;
		File f = null;
		File fDir = null;

		outputFile = fileDir + File.separator + fileName;
		f = new File(outputFile);
		fDir = new File(fileDir);

		if (!fDir.exists()) {
			fDir.mkdirs();
		}

		if (f.exists()) {
			f.delete();
		}

		reportColumnSeparator = columnSeparator;

		Message.add("Report file = " + outputFile);
		raf = new RandomAccessFile(f, "rw");
		raf.writeBytes(createLogHeader() + "\n");

		if (numIndividualsToLog > 0) {
			this.numIndividualsToLog = numIndividualsToLog;
		}
		this.logIndividuals = logIndividuals;
		this.logIndividualsInterval = logIndividualsInterval;
		this.logLastGeneration = logLastGeneration;
		this.outputDirForIndividuals = new File(
				outputDirForIndividuals
						+ File.separator
						+ outputFile.substring((outputFile
								.lastIndexOf(File.separator) > -1 ? outputFile
								.lastIndexOf(File.separator) + 1 : 0),
								outputFile.length()));

		if (this.logIndividuals || this.logLastGeneration) {
			if (!this.outputDirForIndividuals.isDirectory()) {
				if (!this.outputDirForIndividuals.exists()) {
					if (!this.outputDirForIndividuals.mkdirs()) {
						System.err
								.println("Error >>> "
										+ outputDirForIndividuals
										+ "  couldn't be created. Individuals won't be logged!");
						this.logIndividuals = false;
						this.logLastGeneration = false;
					}
				}
			}
		}

	}

	private String createLogHeader() {
		String log = "";
		int i = 0;

		for (i = 0; i < GeneticMinerConstants.logLine.length - 1; i++) {
			log += GeneticMinerConstants.logLine[i] + reportColumnSeparator;
		}
		log += GeneticMinerConstants.logLine[i];

		return log;
	}

	private String createLogEntry(Hashtable hash) {
		String logEntry = "";
		int i = 0;
		String value = null;

		if (hash != null) {
			for (i = 0; i < GeneticMinerConstants.logLine.length - 1; i++) {
				value = (String) hash.get(GeneticMinerConstants.logLine[i]);
				logEntry += value + reportColumnSeparator;
			}
			logEntry += hash.get(GeneticMinerConstants.logLine[i]);

		}
		return logEntry;
	}

	private void createValues() {
		if (raf != null) {
			// a log will be created to store the results...
			// we need to create the hashtable 'values'
			values = new Hashtable();
			for (int i = 0; i < GeneticMinerConstants.logLine.length; i++) {
				values.put(GeneticMinerConstants.logLine[i], "");
			}
			values.put(GeneticMinerConstants.FN, logReader.getFile()
					.getShortName());
			values.put(GeneticMinerConstants.PC, "true");
			values.put(GeneticMinerConstants.PS, settings.getPopulationSize()
					+ "");
			values.put(GeneticMinerConstants.FT, FitnessFactory
					.getAllFitnessTypes()[settings.getFitnessType()]);
			values.put(GeneticMinerConstants.ST, DTSelectionMethodFactory
					.getAllSelectionMethodsTypes()[settings
					.getSelectionMethodType()]);
			values.put(GeneticMinerConstants.MR, settings
					.getMaxNumGenerations()
					+ "");
			values.put(GeneticMinerConstants.S, settings.getSeed() + "");
			values.put(GeneticMinerConstants.UGO, settings
					.getUseGeneticOperators()
					+ "");
			values.put(GeneticMinerConstants.MRt, settings.getMutationRate()
					+ "");
			values.put(GeneticMinerConstants.MTp, MutationFactory
					.getAllMutationTypes()[settings.getMutationType()]
					+ "");
			values.put(GeneticMinerConstants.CRt, settings.getCrossoverRate()
					+ "");
			values.put(GeneticMinerConstants.CTp, CrossoverFactory
					.getAllCrossoverTypes()[settings.getCrossoverType()]
					+ "");
			values.put(GeneticMinerConstants.IPT, InitialPopulationFactory
					.getInitialPopulationTypes()[settings
					.getInitialPopulationType()]
					+ "");
			values.put(GeneticMinerConstants.POW, settings.getPower() + "");
			values.put(GeneticMinerConstants.ELI, settings.getElitismRate()
					+ "");
			values.put(GeneticMinerConstants.FP, toStringDoubleArray(settings
					.getFitnessParameters())
					+ "");

		}
	}

	private String toStringDoubleArray(double[] array) {
		StringBuffer s = new StringBuffer("[ ");

		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				s.append(array[i]).append(" ");
			}
		}
		s.append("]");

		return s.toString();
	}

	private void updateValues(double bestFitnessSoFar, double mean,
			double standardDeviation, int numRuns, long elapsedTime) {

		if (raf != null) {
			values.put(GeneticMinerConstants.BF, bestFitnessSoFar + "");
			values.put(GeneticMinerConstants.AF, mean + "");
			values.put(GeneticMinerConstants.SD, standardDeviation + "");
			values.put(GeneticMinerConstants.GN, numRuns + "");
			values.put(GeneticMinerConstants.ET, elapsedTime + "");
		}

	}

	public JPanel getOptionsPanel(LogSummary summary) {
		if (ui == null) {
			ui = new GeneticMinerUI(summary);
		}
		return ui;
	}

	public synchronized MiningResult mine(LogReader log) {
		MiningResult result;

		progress = new Progress("Mining " + log.getFile().getShortName()
				+ " using " + getName());

		settings = ui.getSettings();
		result = internalMine(log, progress);

		progress.close();
		return result;
	}

	private void logPopulation(int run,
			HeuristicsNet[] populationWithPossibleDuplicates) {
		File outDir = null;
		HeuristicsNet[] pop = null;
		int toLog = this.numIndividualsToLog > populationWithPossibleDuplicates.length ? populationWithPossibleDuplicates.length
				: this.numIndividualsToLog;

		// copying the "toLog" fittest individuals...
		pop = new HeuristicsNet[toLog];
		for (int i = populationWithPossibleDuplicates.length - toLog, j = toLog - 1; i < populationWithPossibleDuplicates.length; i++) {
			pop[j--] = populationWithPossibleDuplicates[i].copyNet();
		}

		// eliminating the duplicate individuals.
		pop = MethodsOverIndividuals.removeDuplicates(pop);
		pop = MethodsOverIndividuals.removeUnusedElements(pop, fitness);

		Arrays.sort(pop);
		toLog = this.numIndividualsToLog > pop.length ? pop.length
				: this.numIndividualsToLog;

		// writing the population
		outDir = new File(this.outputDirForIndividuals,
				DTGeneticMiner.GENERATION_TAG + run);
		if (outDir.mkdir()) {
			for (int i = 0; i < toLog; i++) {
				try {
					pop[i].toFile(new FileOutputStream(outDir.getPath()
							+ File.separator +
							// "fitness" +
							// new Double(pop[i].getFitness() * 100).intValue()
							"ind_" + i + "." + HnExport.FILE_TERMINATION));
				} catch (IOException ioe) {
					System.err
							.println("Warning >>> Could not log individual "
									+ i + " for run " + run + " at "
									+ outDir.getPath());
				}
			}

			// now writing the fitness for each of the logged individuals
			try {

				// first writing with the fitness that was used to build the
				// population
				writeFitnessFile(outDir, "fitnessValues.txt", FitnessFactory
						.getAllFitnessTypes()[settings.getFitnessType()], pop);

				// now writing with proper completion fitness
				Fitness properCompletionFitness = FitnessFactory
						.getFitness(FitnessFactory.PROPER_COMPLETION_INDEX,
								logReader, null);
				properCompletionFitness.calculate(pop);

				writeFitnessFile(
						outDir,
						"fitnessProperCompletion.txt",
						FitnessFactory.getAllFitnessTypes()[FitnessFactory.PROPER_COMPLETION_INDEX],
						pop);

			} catch (IOException ex) {
				System.err
						.println("Warning >>> Could not log the fitness values for the individuals  at "
								+ outDir.getPath());

			}

		} else {
			System.err.println("Error >>> Could create the output directory "
					+ outDir.getPath());

		}

	}

	private void writeFitnessFile(File outDir, String fileName,
			String fitnessType, HeuristicsNet[] pop)
			throws FileNotFoundException, IOException {

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outDir.getPath() + File.separator
						+ fileName)));
		// writing header
		bw.write("Individual" + this.reportColumnSeparator + "Fitness"
				+ this.reportColumnSeparator + "FitnessType");
		bw.newLine();

		for (int i = 0; i < pop.length; i++) {
			bw.write(i + this.reportColumnSeparator + pop[i].getFitness()
					+ this.reportColumnSeparator + fitnessType);
			bw.newLine();
		}

		// logging with proper completion fitness

		bw.close();

	}

	public MiningResult internalMine(LogReader log, Progress progress) {
		int numRuns = 0;
		HeuristicsNet bestIndividualSoFar = null;
		int numTimesBestIndividualIsTheSame = 0;
		double mean = 0;
		double variance = 0;
		double standardDeviation = 0;
		long elapsedTime = 0;

		startGlobalVariables(log);

		progress.setMinMax(0, 2);
		progress.setNote("Building the initial population...");
		progress.setProgress(0);

		if (stopMining()) {
			return null;
		}

		this.startTime = (new Date()).getTime();

		population = initialPopulation.build(new HeuristicsNet[settings
				.getPopulationSize()]);
		progress.setProgress(1);
		population = fitness.calculate(population);
		Arrays.sort(population);
		if (logIndividuals) {
			if ((numRuns % logIndividualsInterval) == 0) {
				logPopulation(numRuns, population);
			}
		}

		progress.setProgress(2);
		if (stopMining()) {
			population = MethodsOverIndividuals.removeUnusedElements(
					population, fitness);
			return new DTGeneticMinerResult(population, log);
		}

		bestIndividualSoFar = population[population.length - 1];
		mean = DescriptiveStatistics.mean(population);
		standardDeviation = DescriptiveStatistics.standardDeviation(population);
		variance = DescriptiveStatistics.variance(population);
		elapsedTime = (new Date()).getTime() - startTime;

		updateValues(population[population.length - 1].getFitness(), mean,
				standardDeviation, numRuns, elapsedTime);
		writeToFile(createLogEntry(values));

		printStatistics(numRuns, mean, variance, standardDeviation,
				population[population.length - 1].getFitness(), elapsedTime);

		numRuns++;

		progress.setMinMax(0, settings.getMaxNumGenerations());

		while (population[population.length - 1].getFitness() < 1.0
				&& numRuns < settings.getMaxNumGenerations()
				&& numTimesBestIndividualIsTheSame < (settings
						.getMaxNumGenerations() / 2)) {

			progress.setNote("Building generation " + numRuns + "...");
			progress.setProgress(numRuns);

			if (bestIndividualSoFar.equals(population[population.length - 1])) {
				numTimesBestIndividualIsTheSame++;
			} else {
				bestIndividualSoFar = population[population.length - 1];
				numTimesBestIndividualIsTheSame = 0;
			}

			population = nextGeneration.build(population);
			population = fitness.calculate(population);
			Arrays.sort(population);

			if (logIndividuals) {
				if ((numRuns % logIndividualsInterval) == 0) {
					logPopulation(numRuns, population);
				}
			}

			mean = DescriptiveStatistics.mean(population);
			standardDeviation = DescriptiveStatistics
					.standardDeviation(population);
			variance = DescriptiveStatistics.variance(population);
			elapsedTime = (new Date()).getTime() - startTime;

			updateValues(population[population.length - 1].getFitness(), mean,
					standardDeviation, numRuns, elapsedTime);
			writeToFile(createLogEntry(values));

			printStatistics(numRuns, mean, variance, standardDeviation,
					population[population.length - 1].getFitness(), elapsedTime);

			if (stopMining()) {
				population = MethodsOverIndividuals.removeUnusedElements(
						population, fitness);
				return new DTGeneticMinerResult(population, log);
			}

			numRuns++;

		}

		Arrays.sort(population);
		if (logLastGeneration) {
			logPopulation(numRuns - 1, population);
		}

		population = MethodsOverIndividuals.removeUnusedElements(population,
				fitness);

		return new DTGeneticMinerResult(population, log);
	}

	private void printStatistics(int run, double mean, double variance,
			double standardDeviation, double bestFitness, long elapsedTime) {

		// setting the separator for the decimals
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');

		// creating the object that will format the doubles
		DecimalFormat df = new DecimalFormat("0.000", dfs);
		df.setDecimalSeparatorAlwaysShown(true);

		// printing the actual numbers
		Message.add("Generation = " + run + " <|> Average fitness = "
				+ df.format(mean) + " <|> Variance = " + df.format(variance)
				+ " <|> Standard Deviation = " + df.format(standardDeviation)
				+ " <|> Fittest = " + df.format(bestFitness)
				+ " <|> ElapsedTime (ms) = " + convertElapsedTime(elapsedTime));

	}

	private String convertElapsedTime(long elapsedTime) {

		int day = 24 * 60 * 60 * 1000; // in milliseconds
		int hour = 60 * 60 * 1000;
		int minute = 60 * 1000;
		int second = 1000;
		int time = Integer.parseInt("" + elapsedTime);
		int result = 0;
		int rest = 0;
		char separator = ':';
		StringBuffer sb = new StringBuffer();

		result = time / day;
		sb.append(result).append(separator);
		rest = time % day;
		result = rest / hour;
		sb.append(result).append(separator);
		rest = rest % hour;
		result = rest / minute;
		sb.append(result).append(separator);
		rest = rest % minute;
		result = rest / second;
		sb.append(result).append('.');
		rest = rest % second;
		sb.append(rest);

		return sb.toString();
	}

	private void startGlobalVariables(LogReader log) {

		logReader = log;

		generator = new Random(settings.getSeed());

		initialPopulation = InitialPopulationFactory.getPopulation(settings
				.getInitialPopulationType(), generator, logReader, settings
				.getPower());

		fitness = FitnessFactory.getFitness(settings.getFitnessType(),
				logReader, settings.getFitnessParameters());

		selectionMethod = DTSelectionMethodFactory.getSelectionMethods(settings
				.getSelectionMethodType(), generator);
		crossover = CrossoverFactory.getCrossover(settings.getCrossoverType(),
				generator);

		mutation = MutationFactory.getMutation(settings.getMutationType(),
				generator, settings.getMutationRate());

		nextGeneration = NextPopulationFactory.getPopulation(settings
				.getUseGeneticOperators(), this.selectionMethod,
				this.generator, settings.getCrossoverRate(), settings
						.getMutationRate(), settings.getElitismRate(),
				this.crossover, this.mutation, this.initialPopulation);
		createValues();
	}

	public String getName() {
		return "Genetic algorithm plugin";
	}

	private boolean stopMining() {
		return logReader == null
				|| (progress != null ? progress.isCanceled() : false);
	}

	protected void finalize() {
		try {
			if (raf != null) {
				raf.close();
			}
		} catch (IOException ioe) {
			Message.add("Could not close the report file...");
			ioe.printStackTrace();
		}
	}

	private void writeToFile(String line) {

		try {
			if (raf != null) {
				raf.writeBytes(line + "\n");
			}
		} catch (IOException ioe) {
			Message.add("Could not write to report file...");
			ioe.printStackTrace();

		}

	}

	public static void main(String[] args) {

		File confFileDir = null;
		String[] confFiles = null;
		String[] filesToMine = null;
		Parameters param = null;
		long seed = 0;
		long seedIncrement = 0;
		StringTokenizer events = null;

		RealGeneticMiner geneticMiner = null;
		DefaultLogFilter logFilter = null;
		LogReader logReader = null;

		new MessageConsole(); // writes Message.add(...) calls to the console
		// window.

		confFileDir = new File(args[0]);
		confFiles = confFileDir.list();

		// for every configuration file do
		for (int i = 0; i < confFiles.length; i++) {
			try {

				param = new Parameters(args[0].trim() + File.separator
						+ confFiles[i]);

				filesToMine = MethodsForFiles.listOfFilesToMine(param
						.getParameter("INPUT_DIR"), param
						.getParameter("FILE_ENDS_WITH"));

				for (int j = 0; j < filesToMine.length; j++) {

					// mine every file with different seeds...
					seed = param.getLongParameter("START_SEED");
					seedIncrement = (Long.MAX_VALUE - seed)
							/ param.getIntParameter("NUMBER_SEEDS");

					// creating log reader...
					logFilter = new DefaultLogFilter(DefaultLogFilter.DISCARD);
					logFilter
							.setProcess(param.getParameter("PROCESS_INSTANCE"));
					events = new StringTokenizer(param
							.getParameter("EVENT_TYPES"), ",");
					while (events.hasMoreElements()) {
						logFilter.filterEventType(events.nextToken(),
								DefaultLogFilter.INCLUDE);
					}
					events = null;
					try {
						logReader = LogReaderFactory.createInstance(logFilter,
								LogFile.getInstance(filesToMine[j]));
					} catch (Exception e) {
						logReader = null;
						e.printStackTrace();
					}
					System.out.println("Mine file = " + filesToMine[j]);

					for (int k = 0; k < param.getIntParameter("NUMBER_SEEDS"); k++) {

						// creating real genetic miner...
						geneticMiner = new RealGeneticMiner(
								param.getParameter("REPORT_DIR"),
								param.getParameter("REPORT_NAME")
										+ "_"
										+ filesToMine[j]
												.substring(filesToMine[j]
														.lastIndexOf(File.separator) + 1)
										+ "_" + "seed" + seed
										+ param.getParameter("REPORT_EXT"),
								param.getParameter("REPORT_COLUMNS_SEPARATOR"),
								param.getIntParameter("LOG_NUM_INDIVIDUALS"),
								param.getBooleanParameter("LOG_INDIVIDUALS"),
								param
										.getIntParameter("LOG_INDIVIDUALS_INTERVAL"),
								param
										.getBooleanParameter("LOG_LAST_GENERATION"),
								param.getParameter("REPORT_DIR")
										+ File.separator + "individuals");
						geneticMiner.settings = new GeneticMinerSettings(
								param.getIntParameter("POP_SIZE"),
								param.getIntParameter("MAX_GENERATIONS"),
								param.getDoubleParameter("MUTATION_RATE"),
								param.getIntParameter("CROSSOVER_TYPE"),
								param.getDoubleParameter("CROSSOVER_RATE"),
								seed,
								param.getIntParameter("FITNESS_TYPE"),
								param.getIntParameter("SELECTION_METHOD"),
								param.getDoubleParameter("ELITISM_RATE"),
								param.getIntParameter("MUTATION_TYPE"),
								param.getLongParameter("POWER"),
								param
										.getIntParameter("INITIAL_POPULATION_TYPE"),
								param
										.getBooleanParameter("USE_GENETIC_OPERATORS"),
								param
										.getArrayDoublesParameter("FITNESS_PARAMETERS"));
						// mining...
						geneticMiner.internalMine(logReader,
								new ProgressDummy());
						seed += seedIncrement;
					}

				}

			} catch (IOException ioe) {
				System.err.println("ERROR >>> Could not mine files at "
						+ param.getParameter("INPUT_DIR") + File.separator
						+ "*" + param.getParameter("FILE_ENDS_WITH"));
			} catch (NoSuchElementException nse) {
				System.err
						.println("ERROR >>> Problems with the configuration file "
								+ args[0].trim()
								+ File.separator
								+ confFiles[i]);
			} catch (NullPointerException npe) {
				System.err
						.println("ERROR >>> Problems while retrieving parameters from the configuration file "
								+ args[0].trim()
								+ File.separator
								+ confFiles[i]);
			}

		}
	}

	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}
}
