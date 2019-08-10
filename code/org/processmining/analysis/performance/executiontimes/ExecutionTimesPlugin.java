package org.processmining.analysis.performance.executiontimes;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.LogReader;

public class ExecutionTimesPlugin {

	public final static String NAME = "Execution Times Using Availability Based on Hours Per Shift";

	@Analyzer(name = NAME, names = { "Log file" }, help = "http://prom.win.tue.nl/research/wiki/onlinedoc/wiki/exectimesbasedworkshifts?s=availability")
	public static ExecutionTimesResultUI analyse(LogReader log) {
		return new ExecutionTimesResultUI(log);
	}
}
