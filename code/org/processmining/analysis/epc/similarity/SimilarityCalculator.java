package org.processmining.analysis.epc.similarity;

import java.util.*;

import org.processmining.framework.ui.*;
import org.processmining.mining.regionmining.*;
import org.processmining.framework.util.*;

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
public class SimilarityCalculator {

	private Checker checker = null;

	private double threshold;
	private double semanticWeight = 0;
	private double syntaxWeight = 1.0;
	private double structureWeight = 0;
	private SimilarityCalculator contextCalculator;

	public SimilarityCalculator(double threshold, double syntaxWeight,
			double semanticWeight, double structureWeight) {
		this.threshold = threshold;
		this.semanticWeight = semanticWeight;
		this.syntaxWeight = syntaxWeight;
		this.structureWeight = structureWeight;
		if (checker == null) {
			Message
					.add("Initializing semantic checker... (might take some time, but happens only once in a ProM session)");
			this.checker = new Checker(UISettings.getProMDirectoryPath()
					+ "lib" + System.getProperty("file.separator") + "plugins"
					+ System.getProperty("file.separator") + "similarity"
					+ System.getProperty("file.separator"));
			Message.add("Semantic checker ready for use.");
		}
	}

	public void setContextSimilarityCalculator(SimilarityCalculator calculator) {
		contextCalculator = calculator;
	}

	// calculates the similarity of two Context fragments,
	private double getSyntacticEquivalence(String f1, String f2) {
		if (!(syntaxWeight > 0)) {
			return 0;
		}
		String activityLabel1 = f1;
		String activityLabel2 = f2;
		// compare the two and return
		return checker
				.syntacticEquivalenceScore(activityLabel1, activityLabel2);
	}

	private double getSemanticEquivalence(String f1, String f2) {
		if (!(semanticWeight > 0)) {
			return 0;
		}
		String activityLabel1 = f1;
		String activityLabel2 = f2;
		// compare the two and return
		return checker.semanticEquivalenceScore(activityLabel1, activityLabel2);
	}

	private double getStructuralEquivalence(ActivityContextFragment f1,
			ActivityContextFragment f2) throws Exception {
		// For the structural equivalence, we compare the input and output sets
		if (!(structureWeight > 0)) {
			return 0;
		}
		return (getContextSimilarity(f1.getInputContext(), f2.getInputContext()) + getContextSimilarity(
				f1.getOutputContext(), f2.getOutputContext())) / 2;
	}

	private double getContextSimilarity(Vector<String> input1,
			Vector<String> input2) throws Exception {
		if (contextCalculator == null) {
			// ERROR, context similarity requested, but context calculator not
			// set.
			throw new Exception() {

			};
		}

		int[] mapping = contextCalculator
				.getBestPossibleMapping(input1, input2);
		// if mapping[i] >= 0 then there is a mapping from
		// input1.get(i) to input2.get(mapping[i]);

		// count the number of mapped elements
		int number = 0;
		for (int i = 0; i < mapping.length; i++) {
			if (mapping[i] > -1) {
				number++;
			}
		}
		// return the cosine of the angle between the two vectors
		return ((double) (number))
				/ (Math.sqrt(input1.size()) * Math.sqrt(input2.size()));
	}

	private double getSimilarity(Object f1, Object f2) throws Exception {
		if ((f1 instanceof ActivityContextFragment)
				&& (f2 instanceof ActivityContextFragment)) {
			return getSimilarity((ActivityContextFragment) f1,
					(ActivityContextFragment) f2);
		} else {
			return (syntaxWeight
					* getSyntacticEquivalence(f1.toString(), f2.toString()) + semanticWeight
					* getSemanticEquivalence(f1.toString(), f2.toString()))
					/ (syntaxWeight + semanticWeight);
		}
	}

	public double getSimilarity(ActivityContextFragment f1,
			ActivityContextFragment f2) throws Exception {
		// This method should consider a weighted combination of the
		// syntactic,
		// semantic, and
		// structural equivalence
		double syntax = getSyntacticEquivalence(f1.getActivityName(), f2
				.getActivityName());
		double semantic = getSemanticEquivalence(f1.getActivityName(), f2
				.getActivityName());
		double structural = getStructuralEquivalence(f1, f2);
		double overall = (syntaxWeight * syntax + semanticWeight * semantic + structureWeight
				* structural)
				/ (syntaxWeight + semanticWeight + structureWeight);

		// System.out.println(f1);
		// System.out.println(f2);
		// System.out.println("syntax: "+syntax+", semantic: "+semantic+", structural: "+structural+", overall: "+
		// overall);
		// System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

		return overall;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getThreshold() {
		return threshold;
	}

	public double[] getSimilarityMatrix(List frags1, List frags2)
			throws Exception {
		double[] similarities = new double[frags1.size() * frags2.size()];

		int index = 0;
		Iterator it1 = frags1.iterator();
		while (it1.hasNext()) {
			Object frag1 = it1.next();
			Iterator it2 = frags2.iterator();
			while (it2.hasNext()) {
				Object frag2 = it2.next();
				similarities[index++] = getSimilarity(frag1, frag2);
			}
		}
		return similarities;
	}

	public int[] getBestPossibleMapping(Vector<String> frags1,
			Vector<String> frags2) throws Exception {

		double[] similarities = getSimilarityMatrix(frags1, frags2);

		return getBestPossibleMapping(similarities, frags1, frags2);
	}

	public int[] getBestPossibleMapping(double[] similarities, List from,
			List to) {

		// Now, we set up a linear programming problem, such that:
		// there are as many variables y.length = similarities.length
		// bounds: for all y_i \in {0,1}
		// bounds: if x_i < getTreshHold() then y_i =0;
		// for all 0 < i < frags1.size(): sum 0 < j < frags2.size()
		// y[i*frags2.size()+j] <= 1
		// for all 0 < j < frags2.size(): sum 0 < i < frags1.size()
		// y[i*frags2.size()+j] <= 1
		// maximize sum 0 < i < frags1.size(), 0 < j < frags2.size()
		// y[i*frags2.size()+j]* x[i*frags2.size()+j]

		int[] result = new int[from.size()];
		Arrays.fill(result, -1);
		ProMLpSolve solver = null;
		try {
			solver = new ProMLpSolve(from.size() + to.size(),
					similarities.length);
		} catch (ProMLpSolveException ex1) {
			return result;
		}

		try {
			solver.setMaximizing();

			// Set the bounds of the variables
			for (int i = 0; i < similarities.length; i++) {
				solver.setColName(i + 1, "y_" + i);
				solver.setBinary(i + 1, true);
				solver.setLowbo(i + 1, 0);
				solver.setUpbo(i + 1,
						(similarities[i] > getThreshold() ? 1 : 0));
			}

			solver.setAddRowmode(true);
			solver.addTarget(similarities);

			int[] constraint = new int[similarities.length];
			for (int i = 0; i < from.size(); i++) {
				Arrays.fill(constraint, 0);
				for (int j = 0; j < to.size(); j++) {
					constraint[i * to.size() + j] = 1;
				}
				solver.addConstraint(constraint, ProMLpSolve.LE, 1.0);
			}

			for (int j = 0; j < to.size(); j++) {
				Arrays.fill(constraint, 0);
				for (int i = 0; i < from.size(); i++) {
					constraint[i * to.size() + j] = 1;
				}
				solver.addConstraint(constraint, ProMLpSolve.LE, 1.0);
			}
			solver.setAddRowmode(false);

			solver.solve();
			double[] solution = solver.getColValuesSolution();
			for (int i = 0; i < from.size(); i++) {
				for (int j = 0; j < to.size(); j++) {
					if (solution[i * to.size() + j] == 1) {
						result[i] = j;
					}
				}
			}

			solver.deleteLp();
		} catch (ProMLpSolveException ex) {
			solver.deleteLp();
		}
		solver = null;
		return result;
	}

}
