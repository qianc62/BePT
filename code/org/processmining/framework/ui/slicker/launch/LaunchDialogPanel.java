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
package org.processmining.framework.ui.slicker.launch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.deckfour.slickerbox.components.MouseOverLabel;
import org.deckfour.slickerbox.components.ResetButton;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerSearchField;
import org.deckfour.slickerbox.components.ToggleSwitchBar;
import org.deckfour.slickerbox.ui.SlickerComboBoxUI;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LaunchDialogPanel extends RoundedPanel {

	protected static String typeToggles[] = { "Mining", "Analysis",
			"Conversion", "Export" };

	protected LaunchGlassPane parent;

	protected LaunchActionList actionList;
	protected LaunchActionListModel actionListModel;

	protected JComboBox objectBox;
	protected JList actionChoiceList;
	protected RoundedPanel actionListPanel;
	protected JScrollPane actionScrollPane;

	protected ToggleSwitchBar typeFilterBar;
	protected SlickerSearchField searchField;

	public LaunchDialogPanel() {
		super(24, 5, 0);
		RoundedPanel innerPanel = new RoundedPanel(20, 0, 5);
		innerPanel.setBackground(new Color(180, 180, 180, 120));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		this.parent = null;
		this.setBackground(new Color(0, 0, 0, 180));
		this.setLayout(new BorderLayout());
		this.add(innerPanel, BorderLayout.CENTER);
		Dimension dim = new Dimension(500, 550);
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
		ActionListener searchListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateActionFilter();
			}
		};
		this.objectBox = new JComboBox();
		this.objectBox.setUI(new SlickerComboBoxUI());
		if (RuntimeUtils.isRunningMacOsX() == false) {
			// Fixes JComboBox bug on Win32 / Linux (Sun JVM)
			// Lightweight popups will not open in Glass pane!
			this.objectBox.setLightWeightPopupEnabled(false);
			// do something nice while we're at it
			this.objectBox.setBackground(new Color(160, 160, 160));
			this.objectBox.setForeground(new Color(40, 40, 40));
			this.objectBox.setBorder(BorderFactory.createEmptyBorder());
		}
		this.objectBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (objectBox.isPopupVisible() == false) {
					return;
				}
				updateActionList();
				typeFilterBar.setAllToggles(true);
				searchField.requestFocusInWindow();
				searchField.setSearchText("");
				repaint();
			}
		});
		this.typeFilterBar = new ToggleSwitchBar(typeToggles, true);
		this.typeFilterBar.addActionListener(searchListener);
		this.searchField = new SlickerSearchField(380, 25, new Color(200, 200,
				200), new Color(120, 120, 120), new Color(60, 60, 60),
				new Color(120, 20, 20));
		this.searchField.addSearchListener(searchListener);
		this.searchField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					actionSelected();
				} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					// searchField.setSearchText("");
					// Changed to fade out action
					parent.fadeOut();
				} else if (e.getKeyChar() == KeyEvent.VK_UP) {
					System.out.println("up");
					if (actionListModel.getSize() > 0
							&& actionChoiceList.getSelectedIndex() < actionListModel
									.getSize() - 1) {
						actionChoiceList.setSelectedIndex(actionChoiceList
								.getSelectedIndex() + 1);
					}
				} else if (e.getKeyChar() == KeyEvent.VK_DOWN) {
					if (actionListModel.getSize() > 0
							&& actionChoiceList.getSelectedIndex() > 0) {
						actionChoiceList.setSelectedIndex(actionChoiceList
								.getSelectedIndex() - 1);
					}
				}
			}

			public void keyReleased(KeyEvent e) { /* handle only full clicks */
			}

			public void keyTyped(KeyEvent e) {
				/* ignore */
			}
		});
		this.actionChoiceList = new JList();
		this.actionChoiceList.setCellRenderer(new LaunchActionListCellRenderer(
				30));
		this.actionChoiceList.setOpaque(true);
		this.actionChoiceList.setBorder(BorderFactory.createEmptyBorder());
		this.actionChoiceList.setBackground(new Color(20, 20, 20));
		this.actionChoiceList.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) { /* handle only full clicks */
			}

			public void keyReleased(KeyEvent e) { /* handle only full clicks */
			}

			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					actionSelected();
				}
			}
		});
		this.actionChoiceList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					// double click
					actionSelected();
				}
			}

			public void mouseEntered(MouseEvent e) { /* ignore */
			}

			public void mouseExited(MouseEvent e) { /* ignore */
			}

			public void mousePressed(MouseEvent e) { /* ignore */
			}

			public void mouseReleased(MouseEvent e) { /* ignore */
			}
		});
		actionScrollPane = new JScrollPane(this.actionChoiceList);
		actionScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		actionScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		actionScrollPane.setBorder(BorderFactory.createEmptyBorder());
		actionScrollPane.setOpaque(true);
		actionScrollPane.setBackground(new Color(20, 20, 20));
		JScrollBar vBar = actionScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(20, 20, 20),
				new Color(140, 140, 140), new Color(80, 80, 80), 4, 12));
		MouseOverLabel actionListLabel = new MouseOverLabel(
				"available actions", 11f, new Color(100, 100, 100));
		JPanel actionLabelPanel = new JPanel();
		actionLabelPanel.setOpaque(false);
		actionLabelPanel.setLayout(new BoxLayout(actionLabelPanel,
				BoxLayout.X_AXIS));
		actionLabelPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		actionLabelPanel.add(Box.createHorizontalGlue());
		actionLabelPanel.add(actionListLabel);
		actionListPanel = new RoundedPanel(10, 5, 5);
		actionListPanel.setBackground(new Color(20, 20, 20));
		actionListPanel.setLayout(new BorderLayout());
		actionListPanel.add(actionLabelPanel, BorderLayout.SOUTH);
		actionListPanel.add(actionScrollPane, BorderLayout.CENTER);
		ResetButton closeButton = new ResetButton(25);
		closeButton.setPassive(new Color(100, 100, 100));
		closeButton.setMouseOver(new Color(180, 180, 180));
		closeButton.setActive(new Color(220, 20, 20));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.fadeOut();
			}
		});
		if (RuntimeUtils.isRunningMacOsX() == true) {
			this.objectBox.setOpaque(false);
		}
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		JLabel headerLabel = new JLabel("Action Trigger");
		headerLabel.setOpaque(false);
		if (RuntimeUtils.isRunningMacOsX() == false) {
			headerLabel.setFont(headerLabel.getFont().deriveFont(16f)
					.deriveFont(Font.BOLD));
		} else {
			headerLabel.setFont(headerLabel.getFont().deriveFont(16f));
		}
		headerLabel.setForeground(new Color(200, 200, 200));
		headerPanel.add(headerLabel);
		headerPanel.add(Box.createHorizontalGlue());
		headerPanel.add(closeButton);
		JPanel objectPanel = new JPanel();
		objectPanel.setOpaque(false);
		objectPanel.setLayout(new BoxLayout(objectPanel, BoxLayout.X_AXIS));
		MouseOverLabel objectLabel = new MouseOverLabel(
				"select the object to act on", 11f, new Color(10, 10, 10));
		objectPanel.add(objectLabel);
		objectPanel.add(Box.createHorizontalGlue());
		JPanel filterPanel = new JPanel();
		filterPanel.setOpaque(false);
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
		MouseOverLabel filterLabel = new MouseOverLabel(
				"<html>Type a search string or disable plugin types to filter the available actions. "
						+ "Hit return or double click to trigger the currently selected action.</html>",
				11f, new Color(10, 10, 10));
		filterPanel.add(filterLabel);
		filterPanel.add(Box.createHorizontalGlue());
		// assemble UI
		this.add(headerPanel, BorderLayout.NORTH);
		innerPanel.add(objectPanel);
		innerPanel.add(Box.createVerticalStrut(2));
		innerPanel.add(this.objectBox);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(this.searchField);
		innerPanel.add(Box.createVerticalStrut(7));
		innerPanel.add(this.typeFilterBar);
		innerPanel.add(Box.createVerticalStrut(7));
		innerPanel.add(actionListPanel);
		innerPanel.add(Box.createVerticalStrut(3));
		innerPanel.add(filterPanel);
	}

	public void setParent(LaunchGlassPane parent) {
		this.parent = parent;
	}

	public void initialize() {
		initialize(null);
	}

	public void setFocusOnSearch() {
		this.searchField.requestFocusInWindow();
	}

	public void actionSelected() {
		if (this.actionListModel == null || this.actionListModel.getSize() == 0) {
			return; // nothing to select
		}
		final AbstractAction action = (AbstractAction) this.actionChoiceList
				.getSelectedValue();
		if (action != null) {
			parent.fadeOut();
			Thread actionThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// nevermind
						e.printStackTrace();
					}
					action.actionPerformed(new ActionEvent(this, 1,
							"launcher triggered this"));
				}
			};
			actionThread.start();
		}
	}

	public void updateActionList() {
		Thread updateThread = new Thread() {
			public void run() {
				Thread.yield();
				ProvidedObject obj = (ProvidedObject) objectBox
						.getSelectedItem();
				actionList = new LaunchActionList(obj);
				actionListModel = new LaunchActionListModel(actionList);
				actionListModel.addListDataListener(new ListDataListener() {
					public void contentsChanged(ListDataEvent evt) {
						if (actionListModel.getSize() > 0) {
							actionChoiceList.setSelectedIndex(0);
						}
						repaint();
					}

					public void intervalAdded(ListDataEvent evt) {
						actionChoiceList.setSelectedIndex(0);
						repaint();
					}

					public void intervalRemoved(ListDataEvent evt) {
						actionChoiceList.setSelectedIndex(0);
						repaint();
					}
				});
				updateActionFilter();
				actionChoiceList.setModel(actionListModel);
				if (actionListModel.getSize() > 0) {
					actionChoiceList.setSelectedIndex(0);
				}
				repaint();
			}
		};
		updateThread.start();

	}

	public void updateActionFilter() {
		if (this.actionListModel == null) {
			return;
		}
		ActionFilter filter = new ActionFilter(searchField.getSearchText(),
				typeFilterBar.getToggle(0), typeFilterBar.getToggle(1),
				typeFilterBar.getToggle(2), typeFilterBar.getToggle(3));
		this.actionListModel.filter(filter);
		if (this.actionListModel.getSize() > 0) {
			this.actionChoiceList.setSelectedIndex(0);
		}
	}

	public void initialize(final ProvidedObject object) {
		Thread initThread = new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.yield();
				if (object == null) {
					Provider provider = (Provider) MainUI.getInstance()
							.getDesktop().getSelectedFrame();
					objectBox.setModel(new DefaultComboBoxModel(provider
							.getProvidedObjects()));
				} else {
					objectBox.setModel(new DefaultComboBoxModel(
							new ProvidedObject[] { object }));
				}
				objectBox.setSelectedIndex(0);
				typeFilterBar.setAllToggles(true);
				searchField.reset();
				updateActionList();
				searchField.requestFocusInWindow();
				repaint();
			}
		};
		initThread.start();
	}
}
