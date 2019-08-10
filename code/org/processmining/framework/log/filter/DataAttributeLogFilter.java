package org.processmining.framework.log.filter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * Data attribute log filter can be used to both filter log events and process
 * instances based on the existence of specific data attributes, and based on
 * specific data attribute values.
 * 
 * @author Boudewijn van Dongen
 */
public class DataAttributeLogFilter extends LogFilter {

	private String attribute;
	private HashSet<String> values;

	public DataAttributeLogFilter() {
		this(null, null);
	}

	public DataAttributeLogFilter(String attribute, HashSet<String> values) {
		super(LogFilter.MODERATE, "Attribute value filter");
		this.attribute = attribute;
		this.values = values;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.LogFilter#doFiltering(org.processmining
	 * .framework.log.ProcessInstance)
	 */
	protected boolean doFiltering(ProcessInstance pi) {
		AuditTrailEntryList ates = pi.getAuditTrailEntryList();

		try {
			for (int i = 0; i < ates.size(); i++) {
				AuditTrailEntry ate = null;
				ate = ates.get(i);
				String attVal = ate.getAttributes().get(attribute);
				if (attVal == null) {
					ates.remove(i);
					i--;
				} else {
					boolean matched = false;
					for (String v : values) {
						if (attVal.matches(v)) {
							matched = true;
							break;
						}
					}
					if (!matched) {
						ates.remove(i);
						i--;
					}
				}
			}
		} catch (IOException e) {
			Message.add(
					"Problem while accessing attributes: " + e.getMessage(),
					Message.ERROR);
		} catch (IndexOutOfBoundsException e) {
			Message.add(
					"Problem while accessing attributes: " + e.getMessage(),
					Message.ERROR);
		}

		return !pi.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#thisFilterChangesLog()
	 */
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#getParameterDialog(org.
	 * processmining.framework.log.LogSummary)
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new AttributeFilterParameterDialog(summary, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.LogFilter#getHelpForThisLogFilter()
	 */
	protected String getHelpForThisLogFilter() {
		return "Checks for each audit trail entry if the value of the given attribute is in the set of given values. If not, the ATE is removed.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.LogFilter#writeSpecificXML(java.io.
	 * BufferedWriter)
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<attribute>" + this.attribute + "</attribute>\n");
		output.write("<values>" + toCSL(this.values) + "</values>\n");
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
			if (n.getNodeName().equals("attribute")) {
				attribute = n.getFirstChild().getNodeValue();
			} else if (n.getNodeName().equals("values")) {
				values = new HashSet<String>();
				StringTokenizer st = new StringTokenizer(n.getFirstChild()
						.getNodeValue(), ",");
				while (st.hasMoreTokens()) {
					values.add(st.nextToken());
				}
			}
		}
	}

	protected String toCSL(HashSet<String> set) {
		if (set.isEmpty()) {
			return "";
		}
		Iterator<String> it = set.iterator();
		String s = it.next();
		while (it.hasNext()) {
			s += "," + it.next();
		}
		return s;
	}

	public String getAttribute() {
		return attribute;
	}

	public HashSet<String> getValues() {
		return values;
	}

}

/**
 * Specifies parameters for the data attribute log filter. Provides the GUI for
 * the settings of the filter, and can create a filter object according to the
 * user configuration.
 * 
 * @author Boudewijn van Dongen
 */
class AttributeFilterParameterDialog extends LogFilterParameterDialog {

	private JTextField attribute;
	private JTextField values;

	/**
	 * Creates a new filter settings instance based on the log summary and a log
	 * filter instance.
	 * 
	 * @param summary
	 *            the Log summary for the log
	 * @param filter
	 *            - ?? TODO: check
	 */
	public AttributeFilterParameterDialog(LogSummary summary,
			DataAttributeLogFilter filter) {
		super(summary, filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.filter.LogFilterParameterDialog#
	 * getAllParametersSet()
	 */
	protected boolean getAllParametersSet() {
		// The attribute name needs to be set
		String z = attribute.getText();
		return !attribute.getText().equals("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.filter.LogFilterParameterDialog#getPanel
	 * ()
	 */
	protected JPanel getPanel() {
		attribute = new JTextField();
		values = new JTextField();
		attribute.setEditable(true);
		values.setEditable(true);
		String att = ((DataAttributeLogFilter) filter).getAttribute();
		HashSet<String> val = ((DataAttributeLogFilter) filter).getValues();

		if (att != null) {
			attribute.setText(att);
		}
		if (val != null) {
			values.setText(((DataAttributeLogFilter) filter).toCSL(val));
		}
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel(
				"Type an attribute and a comma separated list of allowed values");
		panel.add(label, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));
		attribute.setPreferredSize(new Dimension((int) label.getPreferredSize()
				.getWidth() / 2, (int) label.getPreferredSize().getHeight()));
		values.setPreferredSize(new Dimension((int) label.getPreferredSize()
				.getWidth() / 2, (int) label.getPreferredSize().getHeight()));
		panel.add(attribute, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));
		panel.add(values, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 0, 0));

		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.filter.LogFilterParameterDialog#
	 * getNewLogFilter()
	 */
	public LogFilter getNewLogFilter() {
		HashSet<String> valueSet = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(values.getText(), ",");
		while (st.hasMoreTokens()) {
			valueSet.add(st.nextToken());
		}
		return new DataAttributeLogFilter(attribute.getText(), valueSet);
	}
}
