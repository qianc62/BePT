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

package org.processmining.mining.geneticmining.population;

import java.io.IOException;
import java.util.Random;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.GeneticMinerConstants;
import org.processmining.mining.logabstraction.DependencyRelationBuilder;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * <p>
 * Title: Genetic Mining Matrices with Heuristics
 * </p>
 * <p>
 * Description: This class builds causal matrices based on the dependency
 * relations. It is assumed that an artificial START task and an artificial END
 * task were added to the log.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class GeneticMiningMatricesWithHeuristics implements
		GeneticMiningMatrices {

	private double power = GeneticMinerConstants.POWER;
	private DoubleMatrix2D causal = null;
	private DoubleMatrix1D end = null;
	private DoubleMatrix1D start = null;

	private Random generator = null;
	private DependencyRelationBuilder depRelBuilder = null;

	private LogAbstraction logAbstraction = null;

	public GeneticMiningMatricesWithHeuristics(Random gen, LogReader logReader) {
		this(gen, logReader, GeneticMinerConstants.POWER);
	}

	public GeneticMiningMatricesWithHeuristics(Random gen, LogReader logReader,
			double power) {
		this.power = power;
		generator = gen;
		depRelBuilder = new DependencyRelationBuilder(logReader);
		logAbstraction = new LogAbstractionImpl(logReader);
		buildMatrices(logReader.getLogSummary().getLogEvents().size());
	}

	private void buildMatrices(int size) {

		causal = new SparseDoubleMatrix2D(size, size);
		rebuildCausalMatrix();

		start = new SparseDoubleMatrix1D(size);
		rebuildStartMatrix();

		end = new SparseDoubleMatrix1D(size);
		rebuildEndMatrix();
	}

	public DoubleMatrix2D getCausalMatrix() {
		return causal;

	}

	public DoubleMatrix1D getEndMatrix() {
		return end;
	}

	public DoubleMatrix1D getStartMatrix() {
		return start;
	}

	public void rebuildAllMatrices() {
		rebuildCausalMatrix();
		// rebuildEndMatrix(); //because we assume a single start/end task.
		// rebuildStartMatrix();
	}

	public DoubleMatrix2D rebuildCausalMatrix() {
		double random = 0;
		for (int row = 0; row < causal.rows(); row++) {
			for (int column = 0; column < causal.columns(); column++) {
				random = generator.nextDouble();
				if (random < Math.pow(depRelBuilder.getFollowsDependency(row,
						column), power)) {
					causal.set(row, column, 1);
				} else {
					causal.set(row, column, 0);
				}

			}
		}

		return causal;

	}

	public DoubleMatrix1D rebuildStartMatrix() {
		for (int row = 0; row < start.size(); row++) {
			try {
				if (logAbstraction.getStartInfo().get(row) > 0) {
					start.set(row, 1);
				} else {
					start.set(row, 0);
				}
			} catch (IOException ex) {
				Message.add("Error while reading the log: " + ex.getMessage(),
						Message.ERROR);
				return null;
			}
		}
		return start;
	}

	public DoubleMatrix1D rebuildEndMatrix() {
		for (int row = 0; row < end.size(); row++) {
			try {
				if (logAbstraction.getEndInfo().get(row) > 0) {
					end.set(row, 1);
				} else {
					end.set(row, 0);
				}
			} catch (IOException ex) {
				Message.add("Error while reading the log: " + ex.getMessage(),
						Message.ERROR);
				return null;
			}
		}
		return end;

	}

}
