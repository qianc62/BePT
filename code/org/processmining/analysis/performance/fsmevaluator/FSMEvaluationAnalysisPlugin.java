package org.processmining.analysis.performance.fsmevaluator;

import javax.swing.JComponent;
import org.processmining.analysis.Analyzer;
import org.processmining.analysis.performance.fsmanalysis.FSMStatistics;

public class FSMEvaluationAnalysisPlugin {
	@Analyzer(name = "FSM Evaluator", names = { "Log", "FSM Statistics" })
	public JComponent analyze(FSMStatistics fsmStat) {
		return new FSMEvaluationMenuUI(fsmStat);
	}
}
