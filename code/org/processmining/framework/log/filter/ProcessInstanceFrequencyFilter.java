package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.GUIPropertyIntegerTextField;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;
import org.w3c.dom.Node;

/**
 * Log filter that keeps all processs instances with a No. of grouped instances
 * above the given frequency threshold.
 * 
 * @author Anne Rozinat
 */
public class ProcessInstanceFrequencyFilter extends LogFilter {

	protected int lowerLimitFrequency;

	public ProcessInstanceFrequencyFilter() {
		this(0);
	}

	public ProcessInstanceFrequencyFilter(int similarInstances) {
		super(LogFilter.MODERATE, "Process Instance Frequency filter");
		this.lowerLimitFrequency = similarInstances;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		return (MethodsForWorkflowLogDataStructures
				.getNumberSimilarProcessInstances(instance) > lowerLimitFrequency);
	}

	@Override
	protected String getHelpForThisLogFilter() {
		return "Only Process Instances whose 'Number of grouped instances' attribute (see Log export 'Same instances') is above the given frequency threshold will be kept.";
	}

	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {

		return new LogFilterParameterDialog(summary,
				ProcessInstanceFrequencyFilter.this) {

			GUIPropertyIntegerTextField piFreq;

			public LogFilter getNewLogFilter() {
				return new ProcessInstanceFrequencyFilter(piFreq.getValue());
			}

			protected JPanel getPanel() {
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
				p.add(Box.createHorizontalGlue());
				piFreq = new GUIPropertyIntegerTextField("Frequency Threshold",
						0, 0, Integer.MAX_VALUE);
				p.add(piFreq.getPropertyPanel());
				p.add(Box.createHorizontalGlue());
				return p;
			}

			protected boolean getAllParametersSet() {
				return true;
			}
		};
	}

	@Override
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// not implemented
	}

	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// not implemented
	}

}
