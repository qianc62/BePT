package org.processmining.analysis.originator;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.LogReader;

public class SemanticOriginatorByTaskMatrixPlugin {
	@Analyzer(name = "Semantic Originator by Task Matrix", names = { "Log file" })
	public static OriginatorUI analyse(LogReader log) {
		return new OriginatorUI(log, new SemanticOTMatrix2DTableModel(log));
	}
}
