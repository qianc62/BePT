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

package org.processmining.mining.geneticmining.population.duplicates;

import java.io.IOException;
import java.util.Random;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.Message;
import org.processmining.mining.geneticmining.duplicates.DTGeneticMinerConstants;
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
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DTGeneticMiningMatricesNoDuplicateTasks implements
		DTGeneticMiningMatrices {

	private double power = DTGeneticMinerConstants.POWER;

	// These matrices are derived from the log and do not consider duplicate
	// tasks
	private LogAbstraction logAbstraction = null;
	private DoubleMatrix1D basicL1L; // Length-One Loop
	private DoubleMatrix2D basicL2L; // Length-Two Loop
	private DoubleMatrix2D basicFollows;
	private DoubleMatrix1D basicStart;
	private DoubleMatrix1D basicEnd;

	private DoubleMatrix2D basicCausality;

	private DependencyRelationBuilder depRelBuilder = null;

	// These matrices do consider duplicate tasks. They are derived from the
	// basic matrices.
	private Random generator;
	private int[] duplicatesMapping;
	private HNSubSet[] reverseDuplicatesMapping;

	private DoubleMatrix2D causal = null;
	private DoubleMatrix1D start;
	private DoubleMatrix1D end;

	public DTGeneticMiningMatricesNoDuplicateTasks(Random gen,
			LogReader logReader, double power) {
		this.power = power;
		generator = gen;
		logAbstraction = new LogAbstractionImpl(logReader);
		try {
			basicFollows = logAbstraction.getFollowerInfo(1);
			basicStart = logAbstraction.getStartInfo();
			basicEnd = logAbstraction.getEndInfo();

			buildShortLoopMatrices(logAbstraction.getCloseInInfo(2));
		} catch (IOException ex) {
			Message.add("Error while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return;
		}

		depRelBuilder = new DependencyRelationBuilder(logReader);

		buildBasicCausalityRelation();

		setDuplicatesMapping();

		// build here the causal matrix that is used to the duplicates!!!
		buildMatrices(logReader.getLogSummary().getLogEvents().size());

	}

	/**
	 * Returns the mapping from the task code in the individuals to the task
	 * code in the LogReader object that was provided to the constructor of this
	 * class.
	 */
	public int[] getDuplicatesMapping() {
		return duplicatesMapping;
	}

	/**
	 * Returns the mapping from the task code in the LogReader object that was
	 * provided to the constructor of this class to the task code in the
	 * individuals.
	 */

	public HNSubSet[] getReverseDuplicatesMapping() {
		return reverseDuplicatesMapping;
	}

	private void buildBasicCausalityRelation() {

		basicCausality = new SparseDoubleMatrix2D(basicFollows.rows(),
				basicFollows.columns());

		// applying rules to set the causal relation.
		for (int row = 0; row < basicCausality.rows(); row++) {
			for (int column = 0; column < basicCausality.columns(); column++) {
				if (row != column) {
					if (basicL2L.get(row, column) > 0) {
						basicCausality.set(row, column, 1);
					} else if (basicFollows.get(row, column) > 0
							&& basicFollows.get(column, row) <= 0) {
						basicCausality.set(row, column, 1);
					}
				} else if (basicL1L.get(row) > 0) {
					basicCausality.set(row, column, 1);
				}
			}
		}
	}

	private void buildShortLoopMatrices(DoubleMatrix2D matrix) {
		double sum = 0;
		double value = 0;

		basicL1L = new SparseDoubleMatrix1D(matrix.rows());
		basicL2L = new SparseDoubleMatrix2D(matrix.rows(), matrix.columns());

		for (int row = 0; row < matrix.rows(); row++) {
			sum = 0;
			for (int column = 0; column < matrix.columns(); column++) {
				if (row != column) {
					value = matrix.get(row, column);
					basicL2L.set(row, column, value);
					sum += value;
				}
			}
			// updating row == column
			value = matrix.get(row, row);
			basicL2L.set(row, row, Math.floor((value - sum) / 2));
			basicL1L.set(row, basicFollows.get(row, row));
		}
	}

	private void buildMatrices(int size) {

		rebuildCausalMatrix();

		rebuildStartMatrix();

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
		// rebuildStartMatrix();
		// rebuildEndMatrix();
	}

	public DoubleMatrix2D rebuildCausalMatrix() {
		double random;
		causal = new SparseDoubleMatrix2D(duplicatesMapping.length,
				duplicatesMapping.length);

		for (int row = 0; row < this.reverseDuplicatesMapping.length; row++) {
			for (int column = 0; column < this.reverseDuplicatesMapping.length; column++) {
				random = generator.nextDouble();
				if (random < Math.pow(depRelBuilder.getFollowsDependency(row,
						column), power)) {
					// randomly choose a duplicate from row to column
					// and add an arc
					int duplicateTask_row = this.reverseDuplicatesMapping[row]
							.get(generator
									.nextInt(this.reverseDuplicatesMapping[row]
											.size()));
					int duplicateTask_column = this.reverseDuplicatesMapping[column]
							.get(generator
									.nextInt(this.reverseDuplicatesMapping[column]
											.size()));

					causal.set(duplicateTask_row, duplicateTask_column, 1);
				}
			}
		}

		return causal;

	}

	public DoubleMatrix1D rebuildStartMatrix() {
		start = new SparseDoubleMatrix1D(duplicatesMapping.length);
		for (int row = 0; row < start.size(); row++) {
			if (basicStart.get(duplicatesMapping[row]) > 0) {
				start.set(row, 1);
			}
		}
		return start;
	}

	public DoubleMatrix1D rebuildEndMatrix() {
		end = new SparseDoubleMatrix1D(duplicatesMapping.length);

		for (int row = 0; row < end.size(); row++) {
			if (basicEnd.get(duplicatesMapping[row]) > 0) {
				end.set(row, 1);
			}
		}
		return end;

	}

	private void setDuplicatesMapping() {
		int[] numDuplicatesPerTask = new int[basicCausality.rows()];
		int totalDuplicates = 0;
		int indexMapping = 0;

		for (int i = 0; i < basicCausality.rows(); i++) {
			numDuplicatesPerTask[i] = 1;
			totalDuplicates += numDuplicatesPerTask[i];
		}

		// building the mapping
		duplicatesMapping = new int[totalDuplicates];
		// reverseDuplicatesMapping = new HNSubSet[numDuplicatesPerTask.length];
		for (int i = 0; i < numDuplicatesPerTask.length; i++) {
			for (int j = 0; j < numDuplicatesPerTask[i]; j++) {
				// if (reverseDuplicatesMapping[i] == null) {
				// reverseDuplicatesMapping[i] = new HNSubSet();
				// }
				// reverseDuplicatesMapping[i].add(indexMapping);
				duplicatesMapping[indexMapping++] = i;
			}
		}

		reverseDuplicatesMapping = HeuristicsNet
				.buildReverseDuplicatesMapping(duplicatesMapping);

	}

}
