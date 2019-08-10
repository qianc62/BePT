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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.processmining.exporting.heuristicsNet.HnExport;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForFiles;
import org.processmining.importing.heuristicsnet.HeuristicsNetFromFile;

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
 * @author not attributable
 * @version 1.0
 */

public class LoadPopulation {
	HeuristicsNet[] population = null;
	String[] individualsPath = null;

	public LoadPopulation(String inputDirIndividuals) {
		File f = new File(inputDirIndividuals);
		if (!f.isDirectory()) {
			System.err.println("Input directory for runs = '"
					+ f.getAbsolutePath() + "' is not a directory!");
		}
		try {
			loadPopulation(inputDirIndividuals);
		} catch (IOException ioe) {
			System.err.println("No individuals in directory '"
					+ inputDirIndividuals + "'");

		}

	}

	private void loadPopulation(String inputDirIndividuals) throws IOException {

		ArrayList auxPopulation = new ArrayList();
		individualsPath = MethodsForFiles.listOfFilesToMine(
				inputDirIndividuals, HnExport.FILE_TERMINATION);
		if (individualsPath.length > 0) {
			for (int i = 0; i < individualsPath.length; i++) {
				try {
					auxPopulation.add(new HeuristicsNetFromFile(
							new FileInputStream(individualsPath[i])).getNet());
				} catch (Exception exc) {
					System.err.println("Could not load individual at => "
							+ individualsPath[i]);
				}
			}
		}
		if (auxPopulation.size() > 0) {
			this.population = new HeuristicsNet[auxPopulation.size()];
			auxPopulation.toArray(this.population);
		}

	}

	// private EnhancedHeuristicsNet[]
	// removeNullIndividuals(EnhancedHeuristicsNet[] population){
	// Vector v = new Vector();
	//
	// }

	public HeuristicsNet[] getPopulation() {
		return population;

	}

	public void setPopulation(HeuristicsNet[] newPop) {
		population = newPop;
	}

	public String[] getIndividualsPath() {
		return individualsPath;
	}
}
