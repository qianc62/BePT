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

package org.processmining.mining.logabstraction;

import java.io.IOException;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.ui.Message;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * <p>
 * Title: Dependency Relation Builder
 * </p>
 * <p>
 * Description: This class has information about the dependency relation D
 * between workflow model elements.
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

public class DependencyRelationBuilder {

	private LogAbstraction logAbstraction = null;
	private DoubleMatrix1D L1L = null; // Length-One Loop
	private DoubleMatrix2D L2L = null; // Length-Two Loop
	private DoubleMatrix2D follows = null;
	private DoubleMatrix1D start = null;
	private DoubleMatrix1D end = null;

	public DependencyRelationBuilder(LogReader log) {

		logAbstraction = new LogAbstractionImpl(log);
		try {
			follows = logAbstraction.getFollowerInfo(1);
			start = logAbstraction.getStartInfo();
			end = logAbstraction.getEndInfo();

			buildShortLoopMatrices(logAbstraction.getCloseInInfo(2));
		} catch (IOException ex) {
			Message.add("Problem while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return;
		}

	}

	private void buildShortLoopMatrices(DoubleMatrix2D matrix) {
		double sum = 0;
		double value = 0;

		L1L = new SparseDoubleMatrix1D(matrix.rows());
		L2L = new SparseDoubleMatrix2D(matrix.rows(), matrix.columns());

		for (int row = 0; row < matrix.rows(); row++) {
			sum = 0;
			for (int column = 0; column < matrix.columns(); column++) {
				if (row != column) {
					value = matrix.get(row, column);
					L2L.set(row, column, value);
					sum += value;
				}
			}
			// updating row == column
			value = matrix.get(row, row);
			L2L.set(row, row, Math.floor((value - sum) / 2));
			L1L.set(row, follows.get(row, row));
		}
	}

	public double getFollowsDependency(int fromElement, int toElement) {
		double numerator = 0;
		double denominator = 0;

		if (fromElement != toElement) {
			if (L2L.get(fromElement, toElement) > 0) {
				numerator = L2L.get(fromElement, toElement)
						+ L2L.get(toElement, fromElement);
				denominator = numerator;
			} else {
				numerator = follows.get(fromElement, toElement)
						- follows.get(toElement, fromElement);
				denominator = follows.get(fromElement, toElement)
						+ follows.get(toElement, fromElement);
			}
		} else {
			numerator = L1L.get(fromElement);
			denominator = numerator;
		}

		return numerator / (denominator + 1);

	}

	public double getStartDependency(int element) {
		return getDependency(start, element);

	}

	public double getEndDependency(int element) {
		return getDependency(end, element);
	}

	private double getDependency(DoubleMatrix1D matrix, int element) {
		double value = matrix.get(element);

		return value / (value + 1);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("<< Start >>").append("\n");
		sb.append(start).append("\n");
		sb.append("<< End >>").append("\n");
		sb.append(end).append("\n");
		sb.append("<< Follows >>").append("\n");
		sb.append(follows).append("\n");
		sb.append("<< L1L >>").append("\n");
		sb.append(L1L).append("\n");
		sb.append("<< L2L >>").append("\n");
		sb.append(L2L).append("\n");

		return sb.toString();
	}

	public static void main(String[] args) {

		DefaultLogFilter filter = null;
		LogReader log = null;
		DependencyRelationBuilder drb = null;

		filter = new DefaultLogFilter(DefaultLogFilter.DISCARD);

		filter.setProcess("0");

		filter.filterEventType("complete", DefaultLogFilter.INCLUDE);

		try {
			log = LogReaderFactory.createInstance(filter, LogFile
					.getInstance(args[0]));
		} catch (Exception e) {
			e.printStackTrace();
			log = null;
		}

		drb = new DependencyRelationBuilder(log);

		System.out.println(drb);

		System.out.println(log.getLogSummary().getLogEvents().toString());
		for (int i = 0; i < log.getLogSummary().getLogEvents().size(); i++) {
			System.out.print("row " + i + ": ");
			for (int j = 0; j < log.getLogSummary().getLogEvents().size(); j++) {
				System.out.print(drb.getFollowsDependency(i, j) + " ");
			}
			System.out.println();
		}

		System.out.println(drb.getFollowsDependency(1, 2));
		System.out.println(drb.getFollowsDependency(2, 1));
		System.out.println(drb.getStartDependency(0));
		System.out.println(drb.getEndDependency(3));

		log = null;
		filter = null;

	}

}
