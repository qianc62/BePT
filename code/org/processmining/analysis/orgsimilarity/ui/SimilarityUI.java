/*
 * Copyright (c) 2008 Minseok Song
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

package org.processmining.analysis.orgsimilarity.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.orgsimilarity.SimilarityItem;
import org.processmining.analysis.orgsimilarity.SimilarityModel;
import org.processmining.analysis.orgsimilarity.SimilarityResultTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.RuntimeUtils;

/**
 * UI for the benchmark analysis plug-in.
 * 
 * @author Minseok Song
 */
public class SimilarityUI extends JPanel implements Provider {

	protected static final String NO_SELECTION = "- no item selected -";

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_OUTER_BG = new Color(80, 80, 80);
	protected static Color COLOR_TEXT = new Color(50, 50, 50);

	public static String HURESTIC = "HURESTIC";
	public static String EUCLIDIAN_DISTANCE = "EUCLIDIAN DISTANCE";
	public static String CORRELATION_COEFFICIENT = "CORRELATION COEFFICIENT";
	// protected static String SIMILARITY_COEFFICIENT = 2;
	public static String HAMMING_DISTANCE = "HAMMING DISTANCE";

	protected Map<String, OrgModel> orgModels;
	protected Map<String, LogReader> logReaders;
	protected SimilarityModel simModel;

	protected SimilarityItemList itemList;
	protected JComponent view;
	protected JPanel configurationPanel;
	protected JPanel rightPanel;
	protected JPanel metricsPanel;
	// protected JLabel helpHeader;
	protected SimilarityResultTableUI resultTable = null;
	protected GUIPropertyListEnumeration metricsEnumeration;

	protected JButton startButton;

	public SimilarityUI() {
		view = null;
		configurationPanel = null;
		orgModels = null;
		logReaders = null;
		simModel = null;
		setLayout(new BorderLayout());
		setBackground(COLOR_OUTER_BG);
		HeaderBar header = new HeaderBar("Organizational Model Similarity");
		header.setHeight(40);
		add(header, BorderLayout.NORTH);
		showConfigurationPanel();
	}

	public synchronized Map<String, OrgModel> getOrgModels() {
		return orgModels;
	}

	public synchronized Map<String, LogReader> getLogReaders() {
		return logReaders;
	}

	public synchronized void updateFrameworkResources() {
		orgModels = new HashMap<String, OrgModel>();
		logReaders = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof OrgModel) {
							orgModels.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(OrgModel) object);
						} else if (object instanceof LogReader) {
							logReaders.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(LogReader) object);
						}
					}
				}
			}
		}
	}

	protected synchronized void setView(JComponent component) {
		if (view != null) {
			remove(view);
		}
		add(component, BorderLayout.CENTER);
		view = component;
		revalidate();
		repaint();
	}

	public Object[] getLogReaderBoxKeys() {
		Object[] readerKeys = logReaders.keySet().toArray();
		Object[] allKeys = new Object[readerKeys.length + 1];
		allKeys[0] = SimilarityUI.NO_SELECTION;
		for (int i = 0; i < readerKeys.length; i++) {
			allKeys[i + 1] = readerKeys[i];
		}
		return allKeys;
	}

	public Object[] getOrgModelBoxKeys() {
		Object[] modelKeys = orgModels.keySet().toArray();
		Object[] allKeys = new Object[modelKeys.length + 1];
		allKeys[0] = SimilarityUI.NO_SELECTION;
		for (int i = 0; i < modelKeys.length; i++) {
			allKeys[i + 1] = modelKeys[i];
		}
		return allKeys;
	}

	public void checkStartEnabled() {
		if (itemList != null) {
			boolean enabled = (itemList.getSimilarityItems().size() > 0);
			startButton.setEnabled(enabled);
		}
	}

	protected void showConfigurationPanel() {
		if (configurationPanel == null) {
			// setup configuration panel
			updateFrameworkResources();
			configurationPanel = new JPanel();
			configurationPanel.setLayout(new BorderLayout());
			configurationPanel.setBackground(COLOR_OUTER_BG);
			// setup metrics panel
			ArrayList<String> values = new ArrayList<String>();
			values.add(HURESTIC);
			values.add(CORRELATION_COEFFICIENT);
			values.add(EUCLIDIAN_DISTANCE);
			values.add(HAMMING_DISTANCE);
			metricsEnumeration = new GUIPropertyListEnumeration("Metrics :",
					null, values, null, 180);
			RoundedPanel content = new RoundedPanel(10, 5, 5);
			content.setBackground(COLOR_BG);
			content.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
			content.add(metricsEnumeration.getPropertyPanel());

			// setup reference model / log configuration panel
			JPanel startPanel = new JPanel();
			startPanel.setOpaque(false);
			startPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
			startButton = new AutoFocusButton("start calculation");
			if (RuntimeUtils.isRunningMacOsX() == true) {
				startButton.setOpaque(false);
			}
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					startCalculation();
				}
			});
			startButton.setEnabled(false);
			startPanel.add(Box.createHorizontalGlue());
			startPanel.add(startButton);
			rightPanel = new JPanel();
			rightPanel.setOpaque(false);
			rightPanel.setBorder(BorderFactory.createEmptyBorder());
			rightPanel.setLayout(new BorderLayout());
			// blank panel
			JPanel blankPanel = new JPanel();
			blankPanel.setOpaque(false);
			blankPanel.setBorder(BorderFactory.createEmptyBorder());
			blankPanel.setLayout(new BorderLayout());

			JPanel leftPanel = new JPanel();
			leftPanel.setOpaque(false);
			leftPanel.setBorder(BorderFactory.createEmptyBorder());
			leftPanel.setLayout(new BorderLayout());
			itemList = new SimilarityItemList(this);
			leftPanel.add(itemList, BorderLayout.CENTER);
			leftPanel.add(content, BorderLayout.SOUTH);
			// add benchmark item list to west
			rightPanel.add(blankPanel, BorderLayout.CENTER);
			rightPanel.add(startPanel, BorderLayout.SOUTH);
			configurationPanel.add(leftPanel, BorderLayout.WEST);
			configurationPanel.add(rightPanel, BorderLayout.CENTER);
		}
		// switch to configuration view
		setView(configurationPanel);
	}

	public void startCalculation() {
		if (resultTable != null) {
			rightPanel.remove(resultTable);
			resultTable = null;
		}
		final List<SimilarityItem> similarityItems = itemList
				.getSimilarityItems();
		simModel = new SimilarityModel(similarityItems);
		SimilarityResultTableModel tableModel = simModel.getResult(0, 1,
				metricsEnumeration.getValue().toString());
		resultTable = new SimilarityResultTableUI(this, tableModel);
		resultTable.setOpaque(false);
		resultTable.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.add(resultTable, BorderLayout.CENTER);
		rightPanel.revalidate();
		rightPanel.repaint();
	}

	protected static JPanel packHorizontallyLeftAligned(Component[] components,
			int leftOffset) {
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		if (leftOffset > 0) {
			packed.add(Box.createHorizontalStrut(leftOffset));
		}
		int minW = 0, minH = 0;
		for (Component comp : components) {
			packed.add(comp);
			Dimension dim = comp.getMinimumSize();
			minW += dim.getWidth();
			minH = Math.max(minH, (int) dim.getHeight());
		}
		packed.add(Box.createHorizontalGlue());
		packed.setMinimumSize(new Dimension(minW, minH));
		packed.setMaximumSize(new Dimension(4000, minH));
		packed.setPreferredSize(new Dimension(4000, minH));
		return packed;
	}

	public String getSelectedMetric() {
		return metricsEnumeration.getValue().toString();
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[0];

		objects = new ProvidedObject[] { new ProvidedObject("Similarity Model",
				new Object[] { simModel }) };

		return objects;
	}
}
