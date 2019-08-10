/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 *
 * LICENSE:
 *
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * EXEMPTION:
 *
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 *
 */
package org.processmining.framework.ui.slicker.logdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.SlickerSearchField;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.filter.LogFilterCollection;
import org.processmining.framework.log.filter.LogFilterParameterDialog;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SlickerAdvancedLogFilterConfiguration extends JPanel {

	protected Color colorOuterBg = new Color(100, 100, 100);
	protected Color colorBg = new Color(150, 150, 150);
	protected Color colorConfBg = new Color(170, 170, 170);
	protected Color colorListBg = new Color(110, 110, 110);
	protected Color colorListBorder = new Color(100, 100, 100);
	protected Color colorHeading = new Color(30, 30, 30);
	protected Color colorText = new Color(50, 50, 50);

	protected JList allFilterList;
	protected JList filterList;
	protected DefaultListModel filterListModel;
	protected JEditorPane helpPane;
	protected JLabel helpHeader;
	protected RoundedPanel filterPanel;
	protected RoundedPanel addFilterPanel;
	protected RoundedPanel configureFilterPanel;
	protected SlickerSearchField searchField;
	protected JButton addButton;
	protected JButton loadButton;
	protected JButton removeButton;
	protected JButton editButton;
	protected JPanel rightPanel;
	protected JComponent view;

	protected LogReader log;
	protected LogFilter filter = null;
	protected ChangeListener updateListener = null;

	public SlickerAdvancedLogFilterConfiguration(LogReader log,
			ChangeListener updateListener) {
		this.log = log;
		this.updateListener = updateListener;
		final ChangeListener listener = updateListener;
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				if (listener != null) {
					listener.stateChanged(new ChangeEvent(this));
				}
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		// this.setBackground(colorOuterBg);
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		// collect list of log filters
		ArrayList<LogFilter> filters = new ArrayList<LogFilter>();
		for (int i = 0; i < LogFilterCollection.getInstance().size(); i++) {
			filters.add(LogFilterCollection.getInstance().get(i));
		}
		// setup active filter panel
		filterPanel = new RoundedPanel(10, 5, 5);
		filterPanel.setMinimumSize(new Dimension(300, 200));
		filterPanel.setMaximumSize(new Dimension(320, 800));
		filterPanel.setPreferredSize(new Dimension(320, 500));
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		filterPanel.setBackground(colorBg);
		filterListModel = new DefaultListModel();
		filterList = new JList(filterListModel);
		filterList.setCellRenderer(new LogFilterListCellRenderer());
		filterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		filterList.setDragEnabled(true);
		filterList.setBackground(colorListBg);
		filterList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() > 1
						&& evt.getButton() == MouseEvent.BUTTON1) {
					// double click
					if (filterList.getSelectedIndex() >= 0) {
						LogFilter filter = (LogFilter) filterListModel
								.get(filterList.getSelectedIndex());
						showFilterConfiguration(filter, false);
						showHelp(filter);
						editButton.requestFocusInWindow();
					}
				}
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		});
		filterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (filterList.getSelectedIndex() >= 0) {
					LogFilter filter = (LogFilter) filterListModel
							.get(filterList.getSelectedIndex());
					showHelp(filter);
					editButton.requestFocusInWindow();
				}
			}
		});
		JScrollPane filterListScrollPane = new JScrollPane(filterList);
		filterListScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		filterListScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		filterListScrollPane.setViewportBorder(BorderFactory
				.createLineBorder(colorListBorder));
		filterListScrollPane.setBorder(BorderFactory.createEmptyBorder());
		JScrollBar vBar = filterListScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, colorBg, new Color(30, 30, 30),
				new Color(80, 80, 80), 4, 12));
		JLabel filterListHeader = new JLabel("Log filter chain");
		filterListHeader.setForeground(colorHeading);
		filterListHeader.setOpaque(false);
		filterListHeader.setFont(filterListHeader.getFont().deriveFont(16f));
		JPanel filterListHeaderPanel = new JPanel();
		filterListHeaderPanel.setOpaque(false);
		filterListHeaderPanel.setBorder(BorderFactory.createEmptyBorder());
		filterListHeaderPanel.setLayout(new BoxLayout(filterListHeaderPanel,
				BoxLayout.X_AXIS));
		filterListHeaderPanel.add(filterListHeader);
		filterListHeaderPanel.add(Box.createHorizontalGlue());
		JLabel descriptionLabel = new JLabel(
				"<html>All filters in this list will be applied "
						+ "sequentially, in a top-down order.</html>");
		descriptionLabel.setOpaque(false);
		descriptionLabel.setForeground(new Color(50, 50, 50));
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(10f));
		JPanel filterDescriptionPanel = new JPanel();
		filterDescriptionPanel.setOpaque(false);
		filterDescriptionPanel.setBorder(BorderFactory.createEmptyBorder());
		filterDescriptionPanel.setLayout(new BoxLayout(filterDescriptionPanel,
				BoxLayout.X_AXIS));
		filterDescriptionPanel.add(descriptionLabel);
		filterDescriptionPanel.add(Box.createHorizontalGlue());
		removeButton = new SlickerButton("remove");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedFilter();
			}
		});
		editButton = new SlickerButton("edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selectIndex = filterList.getSelectedIndex();
				if (selectIndex >= 0) {
					showFilterConfiguration((LogFilter) filterListModel
							.elementAt(selectIndex), false);
				}
			}
		});
		SlickerButton upButton = new SlickerButton("up");
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int index = filterList.getSelectedIndex();
				if (index > 0) {
					Object trans = filterListModel.remove(index);
					filterListModel.add(index - 1, trans);
					filterList.setSelectedIndex(index - 1);
					filterList.revalidate();
					triggerUpdate(true);
				}
			}
		});
		SlickerButton downButton = new SlickerButton("down");
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				int index = filterList.getSelectedIndex();
				if (index >= 0 && index < (filterListModel.getSize() - 1)) {
					Object trans = filterListModel.remove(index);
					filterListModel.add(index + 1, trans);
					filterList.setSelectedIndex(index + 1);
					filterList.revalidate();
					triggerUpdate(true);
				}
			}
		});
		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(false);
		lowerPanel.setBorder(BorderFactory.createEmptyBorder());
		lowerPanel.setMaximumSize(new Dimension(1000, 30));
		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.X_AXIS));
		lowerPanel.add(editButton);
		lowerPanel.add(Box.createHorizontalStrut(3));
		lowerPanel.add(removeButton);
		lowerPanel.add(Box.createHorizontalGlue());
		lowerPanel.add(upButton);
		lowerPanel.add(Box.createHorizontalStrut(3));
		lowerPanel.add(downButton);
		filterPanel.add(filterListHeaderPanel);
		filterPanel.add(Box.createVerticalStrut(3));
		filterPanel.add(filterDescriptionPanel);
		filterPanel.add(Box.createVerticalStrut(5));
		filterPanel.add(filterListScrollPane);
		filterPanel.add(Box.createVerticalStrut(5));
		filterPanel.add(lowerPanel);
		// setup add filter panel
		addFilterPanel = new RoundedPanel(10, 5, 5);
		addFilterPanel
				.setLayout(new BoxLayout(addFilterPanel, BoxLayout.Y_AXIS));
		addFilterPanel.setBackground(colorBg);
		JLabel addFilterListHeader = new JLabel("Add filters");
		addFilterListHeader.setForeground(colorHeading);
		addFilterListHeader.setOpaque(false);
		addFilterListHeader.setFont(addFilterListHeader.getFont().deriveFont(
				14f));
		searchField = new SlickerSearchField(150, 23, new Color(210, 210, 210),
				new Color(140, 140, 140), new Color(90, 90, 90), new Color(160,
						20, 20));
		searchField.addSearchListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String searchString = searchField.getSearchText().toLowerCase();
				if (searchString.trim().length() == 0) {
					allFilterList.clearSelection();
					return;
				}
				searchString.replaceAll("(\\s)+", "(.*)");
				searchString = "(.*)" + searchString + "(.*)";
				for (int i = 0; i < allFilterList.getModel().getSize(); i++) {
					String filterName = ((LogFilter) allFilterList.getModel()
							.getElementAt(i)).getName().toLowerCase();
					if (filterName.matches(searchString) == true) {
						allFilterList.setSelectedIndex(i);
						allFilterList.ensureIndexIsVisible(i);
						return;
					}
				}
				allFilterList.clearSelection();
			}
		});
		JPanel addFilterListHeaderPanel = new JPanel();
		addFilterListHeaderPanel.setOpaque(false);
		addFilterListHeaderPanel.setBorder(BorderFactory.createEmptyBorder());
		addFilterListHeaderPanel.setLayout(new BoxLayout(
				addFilterListHeaderPanel, BoxLayout.X_AXIS));
		addFilterListHeaderPanel.add(addFilterListHeader);
		addFilterListHeaderPanel.add(Box.createHorizontalGlue());
		addFilterListHeaderPanel.add(searchField);
		allFilterList = new JList(filters.toArray());
		allFilterList.setCellRenderer(new LogFilterListCellRenderer());
		allFilterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		allFilterList.setDragEnabled(false);
		allFilterList.setBackground(colorListBg);
		allFilterList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent evt) {
				int index = allFilterList.getSelectedIndex();
				if (index >= 0) {
					LogFilter filter = (LogFilter) allFilterList
							.getSelectedValue();
					showHelp(filter);
					if (evt.getClickCount() > 1
							&& evt.getButton() == MouseEvent.BUTTON1) {
						// double click, add filter
						addSelectedFilter();
					} else {
						// single click, put add button in focus
						addButton.requestFocusInWindow();
					}
				}
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		});
		JScrollPane allFilterListScrollPane = new JScrollPane(allFilterList);
		allFilterListScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		allFilterListScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		allFilterListScrollPane.setViewportBorder(BorderFactory
				.createLineBorder(colorListBorder));
		allFilterListScrollPane.setBorder(BorderFactory.createEmptyBorder());
		vBar = allFilterListScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, colorBg, new Color(30, 30, 30),
				new Color(80, 80, 80), 4, 12));
		addButton = new SlickerButton("add selected filter");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addSelectedFilter();
			}
		});
		loadButton = new SlickerButton("load...");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadFilter();
			}
		});
		JPanel lowerAddPanel = new JPanel();
		lowerAddPanel.setOpaque(false);
		lowerAddPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		lowerAddPanel.setLayout(new BoxLayout(lowerAddPanel, BoxLayout.X_AXIS));
		lowerAddPanel.add(loadButton);
		lowerAddPanel.add(Box.createHorizontalGlue());
		lowerAddPanel.add(addButton);
		// compose add filter panel
		addFilterPanel.add(addFilterListHeaderPanel);
		addFilterPanel.add(Box.createVerticalStrut(8));
		addFilterPanel.add(allFilterListScrollPane);
		addFilterPanel.add(Box.createVerticalStrut(8));
		addFilterPanel.add(lowerAddPanel);
		// compose filter help panel
		RoundedPanel helpPanel = new RoundedPanel(10, 5, 2);
		helpPanel.setMinimumSize(new Dimension(200, 90));
		helpPanel.setMaximumSize(new Dimension(2000, 90));
		helpPanel.setPreferredSize(new Dimension(300, 90));
		helpPanel.setLayout(new BorderLayout());
		helpPanel.setBackground(colorBg);
		helpPane = new JEditorPane();
		helpPane.setEditable(false);
		helpPane.setContentType("text/html");
		helpPane.setBackground(colorBg);
		helpPane.setForeground(colorText);
		helpPane.setFont(helpPane.getFont().deriveFont(10f));
		JScrollPane helpScrollPane = new JScrollPane(helpPane);
		helpScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		helpScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		helpScrollPane.setBorder(BorderFactory.createEmptyBorder());
		vBar = helpScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, colorBg, new Color(30, 30, 30),
				new Color(80, 80, 80), 4, 12));
		helpHeader = new JLabel("help:");
		helpHeader.setForeground(new Color(60, 60, 60));
		helpHeader.setOpaque(false);
		helpHeader.setFont(helpHeader.getFont().deriveFont(13f));
		JPanel helpHeaderPanel = new JPanel();
		helpHeaderPanel.setOpaque(false);
		helpHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
		helpHeaderPanel.setLayout(new BoxLayout(helpHeaderPanel,
				BoxLayout.Y_AXIS));
		helpHeaderPanel.add(helpHeader);
		helpHeaderPanel.add(Box.createVerticalGlue());
		helpPanel.add(helpHeaderPanel, BorderLayout.WEST);
		helpPanel.add(helpScrollPane, BorderLayout.CENTER);
		// compose right side
		this.view = addFilterPanel;
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.add(helpPanel, BorderLayout.SOUTH);
		rightPanel.add(addFilterPanel, BorderLayout.CENTER);
		// compose whole frame
		this.add(filterPanel, BorderLayout.WEST);
		this.add(rightPanel, BorderLayout.CENTER);
		revalidate();
	}

	protected void removeSelectedFilter() {
		int selectIndex = filterList.getSelectedIndex();
		if (selectIndex >= 0) {
			filterListModel.remove(selectIndex);
			filterList.revalidate();
			filterList.clearSelection();
			// helpHeader.setText("Log filter help");
			helpPane.setText("no filter selected");
			showAddFilterList();
			revalidate();
			repaint();
			triggerUpdate(true);
		}

	}

	protected void addSelectedFilter() {
		LogFilter filter = (LogFilter) allFilterList.getSelectedValue();
		if (filter != null) {
			showFilterConfiguration(filter, true);
			showHelp(filter);
		}
	}

	protected void showFilterConfiguration(LogFilter filter,
			final boolean filterToBeAdded) {
		filterList.setEnabled(false);
		removeButton.setEnabled(false);
		RoundedPanel confPanel = new RoundedPanel(10, 5, 5);
		confPanel.setLayout(new BorderLayout());
		confPanel.setBackground(colorConfBg);
		String heading = filter.getName();
		if (filterToBeAdded == true) {
			heading = "Add " + heading;
		} else {
			heading = "Modify " + heading;
		}
		JLabel confHeader = new JLabel(heading);
		confHeader.setForeground(colorHeading);
		confHeader.setOpaque(false);
		confHeader.setFont(confHeader.getFont().deriveFont(15f));
		JPanel confHeaderPanel = new JPanel();
		confHeaderPanel.setOpaque(false);
		confHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		confHeaderPanel.setLayout(new BoxLayout(confHeaderPanel,
				BoxLayout.X_AXIS));
		confHeaderPanel.add(confHeader);
		confHeaderPanel.add(Box.createHorizontalGlue());
		// get parameter dialog
		final LogFilterParameterDialog dialog = filter.getParameterDialog(log
				.getLogSummary());
		dialog.setVisible(false);
		JPanel filterConfPanel = dialog.getConfigurationPanel();
		if (filterConfPanel != null) {
			SlickerSwingUtils.injectBackgroundColor(filterConfPanel,
					colorConfBg);
			JScrollPane scrollPane = new JScrollPane(filterConfPanel);
			scrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			JScrollBar vBar = scrollPane.getVerticalScrollBar();
			vBar.setUI(new SlickerScrollBarUI(vBar, colorConfBg, new Color(30,
					30, 30), new Color(80, 80, 80), 4, 12));
			JScrollBar hBar = scrollPane.getHorizontalScrollBar();
			hBar.setUI(new SlickerScrollBarUI(hBar, colorConfBg, new Color(30,
					30, 30), new Color(80, 80, 80), 4, 12));
			scrollPane.setOpaque(false);
			// create button panel
			JPanel buttonPanel = new JPanel();
			buttonPanel.setOpaque(false);
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.setMaximumSize(new Dimension(1000, 30));
			String okText = "apply settings";
			if (filterToBeAdded == true) {
				okText = "add new filter";
			}
			JButton okButton = new AutoFocusButton(okText);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (dialog.isAllParametersSet() == false) {
						// TODO: show some kind of error dialog here!
						// (JOptionPane.showMessage..)
						return;
					}
					if (filterToBeAdded == false) {
						int index = filterList.getSelectedIndex();
						filterListModel.remove(index);
						filterListModel.add(index, dialog.getNewLogFilter());
						filterList.setSelectedIndex(index);
					} else {
						filterListModel.addElement(dialog.getNewLogFilter());
						filterList
								.setSelectedIndex(filterListModel.getSize() - 1);
					}
					add(filterPanel, BorderLayout.WEST);
					filterList.revalidate();
					filterList.setEnabled(true);
					removeButton.setEnabled(true);
					showAddFilterList();
					triggerUpdate(true);
				}
			});
			JButton cancelButton = new SlickerButton("cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					add(filterPanel, BorderLayout.WEST);
					filterList.revalidate();
					filterList.setEnabled(true);
					removeButton.setEnabled(true);
					showAddFilterList();
				}
			});
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(cancelButton);
			buttonPanel.add(Box.createHorizontalStrut(20));
			buttonPanel.add(okButton);
			// compose view
			confPanel.add(confHeaderPanel, BorderLayout.NORTH);
			confPanel.add(scrollPane, BorderLayout.CENTER);
			confPanel.add(buttonPanel, BorderLayout.SOUTH);
			rightPanel.remove(view);
			view = confPanel;
			rightPanel.add(view, BorderLayout.CENTER);
			this.remove(filterPanel);
			revalidate();
			repaint();
		} else {
			// no filter configuration
			if (filterToBeAdded == false) {
				int index = filterList.getSelectedIndex();
				filterListModel.remove(index);
				filterListModel.add(index, dialog.getNewLogFilter());
				filterList.setSelectedIndex(index);
			} else {
				filterListModel.addElement(dialog.getNewLogFilter());
				filterList.setSelectedIndex(filterListModel.getSize() - 1);
			}
			filterList.revalidate();
			filterList.setEnabled(true);
			removeButton.setEnabled(true);
			showAddFilterList();
			triggerUpdate(true);
		}
	}

	protected void loadFilter() {
		FileDialog dialog = new FileDialog(MainUI.getInstance(),
				"Load filter configuration", FileDialog.LOAD);
		dialog.setDirectory(UISettings.getInstance().getLastExportLocation()
				.getParent());
		dialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File file, String name) {
				return name.toLowerCase().endsWith(".xml");
			}
		});
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			// load selected file, if possible
			File file = new File(dialog.getDirectory() + File.separator
					+ dialog.getFile());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			LogFilter filter = null;
			try {
				Document doc = dbf.newDocumentBuilder().parse(file);
				// check if root element is a <pnml> tag
				if (!doc.getDocumentElement().getTagName().equals(
						"ProMLogFilter")) {
					throw new IOException("ProMLogFilter tag not found");
				}
				NodeList netNodes = doc.getDocumentElement()
						.getElementsByTagName("LogFilter");
				if (netNodes.getLength() > 0) {
					filter = LogFilter.readXML(netNodes.item(0));
				}
			} catch (Exception e) {
				filter = null;
				e.printStackTrace();
				Message.add("Could not load log filter from "
						+ file.getAbsolutePath(), Message.ERROR);
			}
			// check whether we loaded a good filter
			if (filter != null) {
				// untangle filter hierarchy
				ArrayList<LogFilter> filterList = new ArrayList<LogFilter>();
				while (filter != null) {
					filterList.add(filter);
					filter = filter.getFilter();
				}
				// add to list
				for (int i = filterList.size() - 1; i >= 0; i--) {
					this.filterListModel.addElement(filterList.get(i));
				}
				// update provided log
				triggerUpdate(true);
			} else {
				// error, notify user
				JOptionPane
						.showMessageDialog(
								MainUI.getInstance(),
								"The log filter you have specified could not be loaded!\n"
										+ "Please check whether you have specified a correct log\n"
										+ "reader serialization (you can find more information\n"
										+ "in the stderr messages of your operating system).",
								"Error loading log reader!",
								JOptionPane.ERROR_MESSAGE, null);
			}
		}
	}

	protected void showAddFilterList() {
		rightPanel.remove(view);
		view = addFilterPanel;
		rightPanel.add(view, BorderLayout.CENTER);
		// helpHeader.setText("Log filter help");
		helpPane.setText("");
		searchField.setSearchText("");
		revalidate();
		repaint();
	}

	protected void showHelp(LogFilter filter) {
		// helpHeader.setText("Help: " + filter.getName());
		String helpText = "<html><font face=\"helvetica, arial, sans-serif\" color=#303030 size=\"-1\">";
		helpText += filter.getHelp();
		helpText += "</font></html>";
		helpPane.setText(helpText);
		helpPane.setCaretPosition(0);
	}

	protected void triggerUpdate(boolean resetFilter) {
		// reset filter
		if (resetFilter == true) {
			filter = null;
		}
		if (updateListener != null) {
			updateListener.stateChanged(new ChangeEvent(this));
		}
	}

	protected LogFilter getLogFilter() {
		if (filter == null && filterListModel.size() > 0) {
			for (int i = 0; i < filterListModel.getSize(); i++) {
				LogFilter sFilter = (LogFilter) filterListModel.get(i);
				sFilter.setLowLevelFilter(filter);
				filter = sFilter;
			}
		}
		return filter;
	}

}
