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

import java.util.Random;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.mining.logabstraction.DependencyRelationBuilder;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

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
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class GeneticMiningMatrices {

	private double power = GeneticMinerConstants.POWER;
	private DoubleMatrix2D causal = null;
	private DoubleMatrix1D end = null;
	private DoubleMatrix1D start = null;

	private Random generator = null;
	private DependencyRelationBuilder depRelBuilder = null;

	public GeneticMiningMatrices(Random gen, LogReader logReader) {
		generator = gen;
		depRelBuilder = new DependencyRelationBuilder(logReader);
		buildMatrices(logReader.getLogSummary().getLogEvents().size());
	}

	public GeneticMiningMatrices(Random gen, LogReader logReader, double power) {
		this.power = power;
		generator = gen;
		depRelBuilder = new DependencyRelationBuilder(logReader);
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
		double random = 0;
		for (int row = 0; row < start.size(); row++) {
			random = generator.nextDouble();
			if (random < Math.pow(depRelBuilder.getStartDependency(row), power)) {
				start.set(row, 1);
			} else {
				start.set(row, 0);
			}
		}
		return start;

	}

	public DoubleMatrix1D rebuildEndMatrix() {
		double random = 0;
		for (int row = 0; row < end.size(); row++) {
			random = generator.nextDouble();
			if (random < Math.pow(depRelBuilder.getEndDependency(row), power)) {
				end.set(row, 1);
			} else {
				end.set(row, 0);
			}
		}
		return end;

	}

	public static void main(String[] args) {
		DefaultLogFilter filter = null;
		LogReader log = null;
		DependencyRelationBuilder drb = null;
		GeneticMiningMatrices gmm = null;

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

		if (args[1] != null) {
			gmm = new GeneticMiningMatrices(new Random(1), log, Double
					.parseDouble(args[1]));
		} else {
			gmm = new GeneticMiningMatrices(new Random(1), log);
		}

		for (int i = 0; i < 100; i++) {
			System.out.println("\n\n\n");
			// System.out.println(gmm.rebuildCausalMatrix());
			// System.out.println(gmm.rebuildStartMatrix());
			System.out.println(gmm.rebuildEndMatrix());

		}

		log = null;
		filter = null;

	}

}