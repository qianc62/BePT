/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 16, 2006 8:22:19 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the
 *      names of its contributors may be used to endorse or promote
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.filter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;

/**
 * Dialog for controlling the log event remapping log filter
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class RemapElementLogFilterParamDialog extends LogFilterParameterDialog {

	protected static final int MAPPING_ROWS = 512;

	protected RemapElementLogFilter remapFilter;
	protected JTable remapTable;
	protected MappingTestDialog testDialog = null;

	/**
	 * @param summary
	 * @param filter
	 */
	public RemapElementLogFilterParamDialog(LogSummary summary, LogFilter filter) {
		super(summary, filter);
		remapFilter = (RemapElementLogFilter) filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.filter.LogFilterParameterDialog#
	 * getAllParametersSet()
	 */
	protected boolean getAllParametersSet() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.framework.log.filter.LogFilterParameterDialog#
	 * getNewLogFilter()
	 */
	public LogFilter getNewLogFilter() {
		ArrayList<Pattern> patternList = new ArrayList<Pattern>();
		ArrayList<String> replacementList = new ArrayList<String>();
		String pattern, replacement;
		for (int i = 0; i < remapTable.getRowCount(); i++) {
			pattern = ((String) remapTable.getValueAt(i, 0)).trim();
			replacement = ((String) remapTable.getValueAt(i, 1)).trim();
			if (pattern != null && pattern.length() > 0) {
				try {
					Pattern p = Pattern.compile(pattern);
					patternList.add(p);
					replacementList.add(replacement);
				} catch (PatternSyntaxException e) {
					continue;
				}
			}
		}
		return new RemapElementLogFilter(patternList, replacementList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.filter.LogFilterParameterDialog#getPanel
	 * ()
	 */
	protected JPanel getPanel() {
		remapFilter = (RemapElementLogFilter) filter;
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setSize(400, 500);
		String columnTitles[] = { "Pattern (Perl-style regular expression)",
				"Replacement string (empty if to delete)" };
		String values[][] = new String[MAPPING_ROWS][2];
		ArrayList<Pattern> patternList = remapFilter.getPatternList();
		ArrayList<String> replacementList = remapFilter.getReplacementList();
		for (int i = 0; i < MAPPING_ROWS; i++) {
			if (i < patternList.size()) {
				values[i][0] = patternList.get(i).pattern();
				values[i][1] = replacementList.get(i);
			} else {
				values[i][0] = "";
				values[i][1] = "";
			}
		}
		remapTable = new JTable(values, columnTitles);
		remapTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		JScrollPane scrollPane = new JScrollPane(remapTable);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		String htmlDescription = "<html>"
				+ "Each event whose element name matches the given regular expression<br>"
				+ "has the element name replaced by the respective replacement string.<br>"
				+ "Replacement strings may contain backreferences (<code>$<BRACKET_NR></code>)<br>"
				+ "to dynamically include matched subsequences of the original element.<br>"
				+ "When an empty replacement string is given, all events matching the<br>"
				+ "given regular expression are removed from the log. Any faulty given<br>"
				+ "regular expression will be gracefully ignored!" + "</html>";
		JLabel descriptionLabel = new JLabel(htmlDescription);
		descriptionLabel
				.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		JLabel headerLabel = new JLabel("Mapping table:");
		JButton testButton = new JButton("Test mapping...");
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showTestMappings();
			}
		});
		JPanel headerPanel = new JPanel();
		headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		headerPanel.setLayout(new BorderLayout());
		headerPanel.add(headerLabel, BorderLayout.WEST);
		headerPanel.add(descriptionLabel, BorderLayout.NORTH);
		headerPanel.add(testButton, BorderLayout.EAST);
		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	public void showTestMappings() {
		if (testDialog == null) {
			testDialog = new MappingTestDialog(this);
		} else {
			testDialog.fillDataInTable();
			testDialog.repaint();
		}
		testDialog.setVisible(true);
	}

	public LogSummary getSummary() {
		return summary;
	}

	protected class MappingTestDialog extends JDialog {
		JTable table;
		JScrollPane scrollPane;
		RemapElementLogFilterParamDialog parent;

		public MappingTestDialog(RemapElementLogFilterParamDialog parent) {
			super(parent);
			this.setTitle("Mapping preview");
			this.parent = parent;
			this.setSize(480, 500);
			scrollPane = new JScrollPane();
			fillDataInTable();
			String htmlDescription = "<html>"
					+ "This table renders the mapping of model elements present in the log<br>"
					+ "to replacement strings, as specified in the filter's preferences.<br>"
					+ "Element names triggering a removal of respective events are marked<br>"
					+ "accordingly." + "</html>";
			JLabel descriptionLabel = new JLabel(htmlDescription);
			descriptionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10,
					10, 10));
			this.setLayout(new BorderLayout());
			this.add(descriptionLabel, BorderLayout.NORTH);
			this.add(scrollPane, BorderLayout.CENTER);
			JPanel updatePanel = new JPanel();
			updatePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.X_AXIS));
			JButton updateButton = new JButton("Update preview");
			updateButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fillDataInTable();
					repaint();
				}
			});
			updatePanel.add(Box.createHorizontalGlue());
			updatePanel.add(updateButton);
			this.add(updatePanel, BorderLayout.SOUTH);
		}

		public void fillDataInTable() {
			String modelElements[] = parent.getSummary().getModelElements();
			String tableHeaders[] = { "Original element name",
					"Matched replacement string" };
			String values[][] = new String[modelElements.length][2];
			RemapElementLogFilter filter = (RemapElementLogFilter) parent
					.getNewLogFilter();
			String match = null;
			for (int i = 0; i < modelElements.length; i++) {
				values[i][0] = modelElements[i];
				match = filter.match(modelElements[i]);
				if (match == null) {
					match = modelElements[i];
				} else if (match.length() == 0) {
					match = "WILL BE REMOVED!";
				}
				values[i][1] = match;
			}
			table = new JTable(values, tableHeaders);
			scrollPane.setViewportView(table);
		}
	}

}
