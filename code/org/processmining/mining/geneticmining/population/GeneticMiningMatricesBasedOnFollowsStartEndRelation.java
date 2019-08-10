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

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.ui.Message;
import org.processmining.mining.logabstraction.DependencyRelationBuilder;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: No heuristics is used to build the individuals, but the follows,
 * start and end matrices are taken into consideration. This means that 0
 * entries in the follows matrix are kept 0 in the causal matrices of the
 * individuals. Follows >=0 maybe be set to causal = 1. A similar reasoning
 * holds for the other matrices.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class GeneticMiningMatricesBasedOnFollowsStartEndRelation implements
		GeneticMiningMatrices {

	private DoubleMatrix2D causal = null;
	private DoubleMatrix1D end = null;
	private DoubleMatrix1D start = null;
	private LogAbstraction logAbstraction = null;
	private DoubleMatrix2D followsLog = null;
	private DoubleMatrix1D startLog = null;
	private DoubleMatrix1D endLog = null;

	private Random generator = null;

	public GeneticMiningMatricesBasedOnFollowsStartEndRelation(Random gen,
			LogReader logReader) {
		// random generator
		generator = gen;

		// getting information about follows relation
		logAbstraction = new LogAbstractionImpl(logReader);
		try {
			followsLog = logAbstraction.getFollowerInfo(1);
			startLog = logAbstraction.getStartInfo();
			endLog = logAbstraction.getEndInfo();
		} catch (IOException ex) {
			Message.add("Error while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return;
		}
		logAbstraction = null;

		// building the matrices
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
		rebuildEndMatrix();
		rebuildStartMatrix();
	}

	public DoubleMatrix2D rebuildCausalMatrix() {
		boolean random = false;
		for (int row = 0; row < causal.rows(); row++) {
			for (int column = 0; column < causal.columns(); column++) {
				causal.set(row, column, 0);
				if (followsLog.get(row, column) > 0) {
					random = generator.nextBoolean();
					if (random) {
						causal.set(row, column, 1);
					}
				}

			}
		}

		return causal;

	}

	public DoubleMatrix1D rebuildStartMatrix() {
		boolean random = false;
		for (int row = 0; row < start.size(); row++) {
			start.set(row, 0);
			if (startLog.get(row) > 0) {
				random = generator.nextBoolean();
				if (random) {
					start.set(row, 1);
				}
			}
		}
		return start;

	}

	public DoubleMatrix1D rebuildEndMatrix() {
		boolean random = false;
		for (int row = 0; row < end.size(); row++) {
			end.set(row, 0);
			if (endLog.get(row) > 0) {
				random = generator.nextBoolean();
				if (random) {
					end.set(row, 1);
				}
			}
		}
		return end;

	}

	public static void main(String[] args) {
		DefaultLogFilter filter = null;
		LogReader log = null;
		DependencyRelationBuilder drb = null;
		GeneticMiningMatricesWithoutHeuristics gmm = null;

		filter = new DefaultLogFilter(DefaultLogFilter.DISCARD);

		filter.setProcess("0");

		filter.filterEventType("complete", DefaultLogFilter.INCLUDE);

		try {
			log = LogReaderFactory.createInstance(filter, LogFile
					.getInstance(args[0]));
		} catch (Exception e) {
			log = null;
			e.printStackTrace();
		}

		gmm = new GeneticMiningMatricesWithoutHeuristics(new Random(1), log);

		for (int i = 0; i < 100; i++) {
			System.out.println("\n\n\n");
			System.out.println(gmm.rebuildCausalMatrix());
			System.out.println(gmm.rebuildStartMatrix());
			System.out.println(gmm.rebuildEndMatrix());

		}

		log = null;
		filter = null;

	}

}
