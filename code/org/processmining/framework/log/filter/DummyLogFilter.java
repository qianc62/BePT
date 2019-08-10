package org.processmining.framework.log.filter;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.LogSummary;
import java.io.BufferedWriter;
import java.io.IOException;
import org.w3c.dom.Node;

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
public class DummyLogFilter extends LogFilter {

	protected boolean doFiltering(ProcessInstance processInstance) {
		// don't filter at all;
		return true;
	}

	protected boolean thisFilterChangesLog() {
		// No filtering, hence no changes
		return false;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary logSummary) {
		// No parameters
		return null;
	}

	protected String getHelpForThisLogFilter() {
		return "This logfilter is a dummy. It does not change any process instance, nor removes any process instance.";
	}

	protected void writeSpecificXML(BufferedWriter bufferedWriter)
			throws IOException {
		// write nothing
	}

	protected void readSpecificXML(Node node) throws IOException {
		// read nothing
	}

	public DummyLogFilter() {
		super(LogFilter.FAST, "Dummy log filter");
	}
}
