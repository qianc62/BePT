package org.processmining.framework.log.filter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.Node;

public class ProcessInstanceNameLogFilter extends LogFilter {

	private String regex;

	public ProcessInstanceNameLogFilter() {
		this("");
	}

	public ProcessInstanceNameLogFilter(String value) {
		super(LogFilter.MODERATE, "Process Instance Name filter");
		this.regex = value;
	}

	@Override
	protected boolean doFiltering(ProcessInstance pi) {
		return !pi.getName().matches(regex);
	}

	@Override
	protected boolean thisFilterChangesLog() {
		return true;
	}

	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new ProcessInstanceNameFilterParameterDialog(summary, this);
	}

	@Override
	protected String getHelpForThisLogFilter() {
		return "Removes process instances whose name matches the given regular expression.";
	}

	@Override
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<regex>" + this.regex + "</regex>\n");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#readSpecificXML(org.w3c.dom
	 * .Node)
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("regex")) {
				regex = n.getFirstChild().getNodeValue();
			}
		}
	}

	public String getRegex() {
		return regex;
	}
}

class ProcessInstanceNameFilterParameterDialog extends LogFilterParameterDialog {

	private static final long serialVersionUID = -7335089473600899187L;

	private JTextField regex;

	public ProcessInstanceNameFilterParameterDialog(LogSummary summary,
			ProcessInstanceNameLogFilter filter) {
		super(summary, filter);
	}

	@Override
	protected boolean getAllParametersSet() {
		return !regex.getText().equals("");
	}

	@Override
	protected JPanel getPanel() {
		regex = new JTextField();
		regex.setEditable(true);

		String regexValue = ((ProcessInstanceNameLogFilter) filter).getRegex();

		if (regexValue != null) {
			regex.setText(regexValue);
		}
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel(
				"Please specify the regular expression you want to use for filtering process instance names:");
		panel.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));
		regex.setPreferredSize(new Dimension((int) label.getPreferredSize()
				.getWidth(), (int) regex.getPreferredSize().getHeight()));
		panel.add(regex, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));
		JLabel help = new JLabel(
				"<html><br>All process instances whose name matches the given regular expression will be filtered out.<br>"
						+ "The regular expression should follow the syntax of Java regular expressions, see<br>"
						+ "<a href=\"http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html#sum\">http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html#sum</a> for a detailed<br>"
						+ "reference. Short introduction: a regular expression is a normal string, except<br>"
						+ "that some characters have a special meaning. The most impotant ones are the dot (.)<br>"
						+ "which matches any character, the star (*) which matches the character before it<br>"
						+ "zero or more times and the plus (+) which matches the preceding character one or<br>"
						+ "more times. For example 'a*' matches zero or more 'a's, while 'ab.*c' matches<br>"
						+ "anything which starts with 'ab' and ends with 'c' and has any number of characters<br>"
						+ "('.*') in between. Parentheses, square brackets, curly braces and backslashes also<br>"
						+ "have special meanings, please see the reference or a tutorial on regular expressions<br>"
						+ "for more information." + "</html>");
		panel.add(help, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
						1, 1, 1, 1), 50, 0));

		return panel;
	}

	@Override
	public LogFilter getNewLogFilter() {
		return new ProcessInstanceNameLogFilter(regex.getText());
	}
}
