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
 * This class uses the follows matrices to set the maximum number of duplicated
 * tasks that an individual (HeuristicsNet) can have. Basically, the number of
 * duplicates of a task <i>t</i> is the minimum between the number of tasks that
 * directly follow <i>t</i> and the number of tasks that are directly followed
 * by <i>t</i>.
 * </p>
 * 
 * <p>
 * The arcs are set based on the dependency matrix and the power value. The more
 * often a task <i>t</i> is followed by a task <i>t'</i>, the higher the
 * probability that an arc from a duplicate of <i>t</i> to a duplicate of
 * <i>t'</i> will be set.
 * </p>
 * 
 * <p>
 * NOTE: It is assumed that an artificial START task and an artificial END task
 * were added to the log.
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class DTGeneticMiningMatricesWithMinimalHeuristics implements
		DTGeneticMiningMatrices {

	private double power = DTGeneticMinerConstants.POWER;
	// These matrices are derived from the log and do not consider duplicate
	// tasks
	private LogAbstraction logAbstraction = null;
	private DoubleMatrix2D basicFollows;
	private DoubleMatrix1D basicStart;
	private DoubleMatrix1D basicEnd;

	// These matrices do consider duplicate tasks. They are derived from the
	// basic matrices.
	private Random generator;
	private int[] duplicatesMapping;
	private HNSubSet[] reverseDuplicatesMapping;

	private DependencyRelationBuilder depRelBuilder = null;

	private DoubleMatrix2D causal = null;
	private DoubleMatrix1D start;
	private DoubleMatrix1D end;

	public DTGeneticMiningMatricesWithMinimalHeuristics(Random gen,
			LogReader logReader, double power) {
		this.power = power;

		generator = gen;
		logAbstraction = new LogAbstractionImpl(logReader);
		try {
			basicFollows = logAbstraction.getFollowerInfo(1);
			basicStart = logAbstraction.getStartInfo();
			basicEnd = logAbstraction.getEndInfo();
		} catch (IOException ex) {
			Message.add("Error while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return;
		}

		depRelBuilder = new DependencyRelationBuilder(logReader);

		setDuplicatesMapping();

		// build here the causal matrix that is used to the duplicates!!!
		buildMatrices();

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

	private void setDuplicatesMapping() {
		int[] inFollowsRelations = new int[basicFollows.rows()];
		int[] outFollowsRelations = new int[basicFollows.rows()];
		int[] numDuplicatesPerTask = new int[basicFollows.rows()];
		int totalDuplicates = 0;
		int indexMapping = 0;

		// checking how many duplicates are in the log
		// Heuristics: for every task, the
		// #duplicates = min(#inputFollowsRelations, #outputCausalRelations)
		for (int row = 0; row < basicFollows.rows(); row++) {
			for (int column = 0; column < basicFollows.columns(); column++) {
				if (basicFollows.get(row, column) > 0) {
					inFollowsRelations[row]++;
					outFollowsRelations[column]++;
				}
			}
		}

		for (int i = 0; i < outFollowsRelations.length; i++) {
			if (basicStart.get(i) > 0 || basicEnd.get(i) > 0) {
				numDuplicatesPerTask[i] = 1; // for the start/end tasks...
			} else {
				numDuplicatesPerTask[i] = Math.min(inFollowsRelations[i],
						outFollowsRelations[i]);
			}
			totalDuplicates += numDuplicatesPerTask[i];
		}

		// building the mapping
		duplicatesMapping = new int[totalDuplicates];
		for (int i = 0; i < numDuplicatesPerTask.length; i++) {
			for (int j = 0; j < numDuplicatesPerTask[i]; j++) {
				duplicatesMapping[indexMapping++] = i;
			}
		}

		reverseDuplicatesMapping = HeuristicsNet
				.buildReverseDuplicatesMapping(duplicatesMapping);
	}

	private void buildMatrices() {

		rebuildStartMatrix();

		rebuildEndMatrix();

		rebuildCausalMatrix();

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

}
