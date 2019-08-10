package org.processmining.mining.heuristicsmining.fitness;

import java.util.Random;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.ContinuousSemanticsParser;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.mining.geneticmining.fitness.Fitness;
import org.processmining.mining.geneticmining.util.MapIdenticalIndividuals;

public class ContinuousParsingMeasure implements Fitness {

	private LogReader logReader = null;
	private HeuristicsNet[] population = null;
	private ContinuousSemanticsParser[] parser = null;

	private MapIdenticalIndividuals mapping = null;

	private double[] numDisabledWMEs = null; // WME = Workflow Model Element
	private double[] numParsedWMEs = null; // WME = Workflow Model Element
	private double[] numProperlyCompletedPIs = null; // PI = process instance

	private double numEnabledConstant = 0.40;
	private double numProperlyCompletedConstant = 0.60;

	private Random generator = null;

	public ContinuousParsingMeasure(LogReader log) {
		logReader = log;
		generator = new Random(Long.MAX_VALUE);
	}

	public HeuristicsNet[] calculate(HeuristicsNet[] population) {
		// TODO Auto-generated method stub
		return null;
	}

}
