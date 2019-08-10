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

package org.processmining.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: This class reads configuration files. The parameters are
 * separated by "=". As an example, please see"C:\processmining\ProcessMining\configurationFiles\geneticMiningExperiments\basicFile.t
 * x t "
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla Alves de Medeiros
 * @version 1.0
 */

public class Parameters {

	final static String errorID = "Parameters_ERROR";

	private Hashtable parameters = null;

	/**
	 * Constructor method.
	 * 
	 * @param confFile
	 *            full filepath.
	 */

	public Parameters(String confFile) {
		this.parameters = new Hashtable();
		readParameters(confFile);
	}

	private void readParameters(String file) {

		String line = null;
		StringTokenizer str = null;
		String key = null;
		String content = null;
		RandomAccessFile raf = null;
		String currentDirectory = null;

		try {
			currentDirectory = "";
			if (file.indexOf(File.separator) >= 0) {
				currentDirectory = file.substring(0, file
						.lastIndexOf(File.separator) + 1);
			}

			raf = new RandomAccessFile(file, "r");
			while ((line = raf.readLine()) != null) {
				if (!line.trim().startsWith("#")) {
					if (line.startsWith("." + File.separator)
							|| line.startsWith(".." + File.separator)) {
						readParameters(currentDirectory + line);
					}
					str = new StringTokenizer(line, "=");

					if (str.countTokens() > 1) {
						key = str.nextToken().trim();
						content = str.nextToken().trim();
						this.parameters.put(key, content);
					}
				}
			}
		} catch (IOException ioe) {
			System.err.println(this.errorID + " Cannot read file: " + file);
			System.err.println(this.errorID + " " + ioe.getMessage());
		}

	}

	/**
	 * Retrieves a parameter's value.
	 * 
	 * @param name
	 *            parameter name.
	 * @return parameter's value.
	 */
	public String getParameter(String name) {
		return ((String) this.parameters.get(name)).trim();
	}

	public int getIntParameter(String name) {
		return Integer.parseInt(this.getParameter(name));
	}

	public long getLongParameter(String name) {
		return Long.parseLong(this.getParameter(name));
	}

	public double getDoubleParameter(String name) {
		return Double.parseDouble(this.getParameter(name));
	}

	public boolean getBooleanParameter(String name) {
		if (this.getParameter(name).toLowerCase().equals("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Expects an array in the format "[value1 value2 ... valuen]". The double
	 * string should be parseable by the method
	 * <code> Double.parseDouble </code>.
	 * 
	 * @return double[] the array with the double parameters. This array will be
	 *         empty (size = 0) if the parameter name is not defined or if it is
	 *         equal to "[]".
	 */

	public double[] getArrayDoublesParameter(String name) {
		double[] arrayDoubles;
		try {
			StringTokenizer stringArray = new StringTokenizer(this
					.getParameter(name), "[ ]");
			arrayDoubles = new double[stringArray.countTokens()];

			for (int i = 0; i < arrayDoubles.length; i++) {
				arrayDoubles[i] = Double.parseDouble(stringArray.nextToken());
			}
		} catch (NullPointerException npe) {
			arrayDoubles = new double[0];
		}

		return arrayDoubles;

	}

	/**
	 * Expects an array in the format "[value1 value2 ... valuen]". The integer
	 * string should be parseable by the method <code> Integer.parseInt </code>.
	 * 
	 * @return int[] the array with the integer parameters. This array will be
	 *         empty (size = 0) if the parameter name is not defined or if it is
	 *         equal to "[]".
	 */

	public int[] getArrayIntegerParameter(String name) {
		int[] arrayDoubles;

		try {
			StringTokenizer stringArray = new StringTokenizer(this
					.getParameter(name), "[ ]");
			arrayDoubles = new int[stringArray.countTokens()];

			for (int i = 0; i < arrayDoubles.length; i++) {
				arrayDoubles[i] = Integer.parseInt(stringArray.nextToken());
			}
		} catch (NullPointerException npe) {
			arrayDoubles = new int[0];
		}

		return arrayDoubles;

	}

	public static void main(String[] args) {
		Parameters param = new Parameters(args[0]);

		System.out.println(param.getIntParameter("POP_SIZE") + " "
				+ param.getIntParameter("MAX_GENERATIONS") + " "
				+ param.getDoubleParameter("MUTATION_RATE") + " "
				+ param.getIntParameter("CROSSOVER_TYPE") + " "
				+ param.getDoubleParameter("CROSSOVER_RATE") + " "
				+ param.getIntParameter("FITNESS_TYPE") + " "
				+ param.getIntParameter("SELECTION_METHOD") + " "
				+ param.getDoubleParameter("ELITISM_RATE") + " "
				+ param.getIntParameter("MUTATION_TYPE") + " "
				+ param.getLongParameter("POWER") + " "
				+ param.getIntParameter("INITIAL_POPULATION_TYPE") + " "
				+ param.getBooleanParameter("USE_GENETIC_OPERATORS") + " "
				+ param.getArrayDoublesParameter("FITNESS_PARAMETERS").length);

	}

}
