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

package org.processmining.framework.log.filter;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.algorithms.OmmlReader;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.w3c.dom.Node;

/**
 * <p>
 * Title: LogAttributeReplacementFilter
 * </p>
 * 
 * <p>
 * Description: This class make it possible to replace activity names or
 * originators by other attributes (e.g. activity name, role, etc.)
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class LogAttributeReplacementFilter extends LogFilter {

	private String repFrom = null;
	private String repTo = null;

	private String orgModelFileName = null;
	private boolean useOrgModel = false;
	// private JTextField orgModelFile = new JTextField();
	// private JButton chooseOrgModelButton = new JButton();
	// private GUIPropertyBoolean assignOrgModelChecked = new
	// GUIPropertyBoolean("Assign Org Model", false);
	private OrgModel orgModel = null;

	public LogAttributeReplacementFilter() {
		super(LogFilter.MODERATE, "Replacement Filter");
	}

	public LogAttributeReplacementFilter(String repFrom, String repTo) {
		super(LogFilter.MODERATE, "Replacement Filter");
		this.repFrom = repFrom;
		this.repTo = repTo;
	}

	public LogAttributeReplacementFilter(String repFrom, String repTo,
			JTextField orgModelFile) {
		super(LogFilter.MODERATE, "Replacement Filter");
		this.repFrom = repFrom;
		this.repTo = repTo;
		this.orgModelFileName = orgModelFile.getText();
		this.useOrgModel = true;
		// assignOrgModelChecked.setSelected(true);
		orgModel = OmmlReader.read(orgModelFile.getText());
	}

	protected String getHelpForThisLogFilter() {
		return "replace task names or originators by other things.";
	}

	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList entries = instance.getAuditTrailEntryList();

		try {
			if (repFrom.equals("Originator")) {
				if (repTo.equals("Task")) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						entry.setOriginator(entry.getElement());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Task+Originator")) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						entry.setOriginator(entry.getElement() + "_"
								+ entry.getOriginator());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Role") && useOrgModel) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						List<String> list = orgModel.getOrgEntityList(entry
								.getOriginator(), OrgEntity.ORGENTITYTYPE_ROLE);
						if (list.size() == 0) {
							continue;
						}
						entry.setOriginator(list.toString());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Org Unit") && useOrgModel) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						List<String> list = orgModel.getOrgEntityList(entry
								.getOriginator(),
								OrgEntity.ORGENTITYTYPE_ORGUNIT);
						if (list.size() == 0) {
							continue;
						}
						entry.setOriginator(list.toString());
						entries.replace(entry, i);
					}
				}
			} else {
				if (repTo.equals("Originator")) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						entry.setElement(entry.getOriginator());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Task+Originator")) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						entry.setElement(entry.getElement() + "_"
								+ entry.getOriginator());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Role") && useOrgModel) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						List<String> list = orgModel.getOrgEntityList(entry
								.getOriginator(), OrgEntity.ORGENTITYTYPE_ROLE);
						if (list.size() == 0) {
							continue;
						}
						entry.setElement(list.toString());
						entries.replace(entry, i);
					}
				} else if (repTo.equals("Org Unit") && useOrgModel) {
					for (int i = 0; i < entries.size(); i++) {
						AuditTrailEntry entry = entries.get(i);
						List<String> list = orgModel.getOrgEntityList(entry
								.getOriginator(),
								OrgEntity.ORGENTITYTYPE_ORGUNIT);
						if (list.size() == 0) {
							continue;
						}
						entry.setElement(list.toString());
						entries.replace(entry, i);
					}
				}
			}
		} catch (IOException ex) {
			Message.add(ex.getMessage(), Message.ERROR);
		} catch (IndexOutOfBoundsException ex) {
			Message.add(ex.getMessage(), Message.ERROR);
		}
		return entries.size() > 0;
	}

	/**
	 * The log is changed.
	 * 
	 * @return boolean true
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				LogAttributeReplacementFilter.this) {

			JComboBox fromList;
			JComboBox toList;
			JTextField orgModelFile;
			JButton chooseOrgModelButton;
			GUIPropertyBoolean assignOrgModelChecked;

			public LogFilter getNewLogFilter() {
				if (assignOrgModelChecked.getValue()) {
					return new LogAttributeReplacementFilter(fromList
							.getSelectedItem().toString(), toList
							.getSelectedItem().toString(), orgModelFile);
				}
				return new LogAttributeReplacementFilter(fromList
						.getSelectedItem().toString(), toList.getSelectedItem()
						.toString());
			}

			protected JPanel getPanel() {

				String[] valuesFrom = { "Task", "Originator" };
				fromList = new JComboBox(valuesFrom);
				if (repFrom != null) {
					for (int i = 0; i < valuesFrom.length; i++) {
						if (repFrom.equals(valuesFrom[i])) {
							fromList.setSelectedIndex(i);
							break;
						}
					}
				} else {
					fromList.setSelectedIndex(0);
				}
				fromList.setMinimumSize(new Dimension(120, 21));
				fromList.setPreferredSize(new Dimension(120, 21));

				JPanel fromPanel = new JPanel();
				JLabel fromLabel = new JLabel("Replace : ");
				fromPanel.add(fromLabel);
				fromPanel.add(fromList);

				String[] valuesTo = { "Task", "Originator", "Task+Originator",
						"Role", "Org Unit" };
				toList = new JComboBox(valuesTo);
				if (repTo != null) {
					for (int i = 0; i < valuesTo.length; i++) {
						if (repTo.equals(valuesTo[i])) {
							toList.setSelectedIndex(i);
							break;
						}
					}
				} else {
					toList.setSelectedIndex(0);
				}

				toList.setMinimumSize(new Dimension(120, 21));
				toList.setPreferredSize(new Dimension(120, 21));

				JPanel toPanel = new JPanel();
				JLabel toLabel = new JLabel("   By   : ");
				toPanel.add(toLabel);
				toPanel.add(toList);

				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

				p.add(fromPanel);
				p.add(toPanel);

				orgModelFile = new JTextField();
				chooseOrgModelButton = new JButton();
				assignOrgModelChecked = new GUIPropertyBoolean(
						"Assign Org Model", false);

				orgModelFile.setMinimumSize(new Dimension(120, 21));
				orgModelFile.setPreferredSize(new Dimension(120, 21));
				orgModelFile.setEditable(false);
				chooseOrgModelButton.setMaximumSize(new Dimension(100, 25));
				chooseOrgModelButton.setMinimumSize(new Dimension(100, 25));
				chooseOrgModelButton.setPreferredSize(new Dimension(100, 25));
				chooseOrgModelButton.setActionCommand("");
				chooseOrgModelButton.setText("Browse...");

				chooseOrgModelButton
						.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(ActionEvent e) {
								chooseOrgModelButton_actionPerformed(e);
							}
						});

				JPanel filePanel = new JPanel();

				filePanel.setLayout(new GridLayout(0, 1));
				filePanel.add(assignOrgModelChecked.getPropertyPanel());
				assignOrgModelChecked.setSelected(useOrgModel);
				if (useOrgModel) {
					orgModelFile.setText(orgModelFileName);
				}

				filePanel.add(orgModelFile);
				filePanel.add(chooseOrgModelButton);

				filePanel.setMaximumSize(new Dimension(200, 75));
				filePanel.setMinimumSize(new Dimension(200, 75));
				filePanel.setPreferredSize(new Dimension(200, 75));

				p.add(filePanel);

				return p;
			}

			private void chooseOrgModelButton_actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();

				chooser.setFileFilter(new GenericMultipleExtFilter(
						new String[] { "xml" }, "XML file (*.xml)"));
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					String name = chooser.getSelectedFile().getPath();
					setChosenXMLFile(name);
				}
			}

			public boolean isAssignOrgModelChecked() {
				return assignOrgModelChecked.getValue()
						&& (orgModelFile.getText() != "");
			}

			private void setChosenXMLFile(String logFileName) {
				orgModelFile.setText(logFileName);
			}

			public String getOrgModelFileName() {
				return orgModelFile.getText();
			}

			protected boolean getAllParametersSet() {
				return true;
			}

		};
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		output.write("<logReplaceFrom>" + repFrom + "</logReplaceFrom>\n");
		output.write("<logReplaceTo>" + repTo + "</logReplaceTo>\n");
		if (useOrgModel) {
			output.write("<logReplaceOrgFile>" + orgModelFileName
					+ "</logReplaceOrgFile>\n");
		}
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		for (int i = 0; i < logFilterSpecifcNode.getChildNodes().getLength(); i++) {
			Node n = logFilterSpecifcNode.getChildNodes().item(i);
			if (n.getNodeName().equals("logReplaceFrom")) {
				repFrom = n.getFirstChild().getNodeValue();
			} else if (n.getNodeName().equals("logReplaceTo")) {
				repTo = n.getFirstChild().getNodeValue();
			} else if (n.getNodeName().equals("logReplaceOrgFile")) {
				orgModelFileName = n.getFirstChild().getNodeValue();
				useOrgModel = true;
			}
		}
	}
}
