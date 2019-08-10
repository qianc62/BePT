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
package org.processmining.mining.fuzzymining.vis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.NiceIntegerSlider;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerCheckBoxUI;
import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.filter.TimestampInjectionFilter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.fuzzymining.filter.FuzzyGraphProjectionFilter;
import org.processmining.mining.fuzzymining.graph.FuzzyGraph;
import org.processmining.mining.fuzzymining.vis.ui.AnimationFrame;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FuzzyAnimationAnalysis extends JPanel implements AnalysisPlugin,
		Provider {

	protected FuzzyGraph graph = null;
	protected LogReader log = null;
	protected boolean discreteAnimation = false;

	protected AnimationFrame animationFrame = null;
	protected NiceIntegerSlider lookaheadSlider;
	protected NiceIntegerSlider extraLookaheadSlider;

	protected HashMap<String, LogReader> logMap;
	protected HashMap<String, FuzzyGraph> modelMap;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		updateResources();
		graph = null;
		log = null;
		discreteAnimation = false;
		animationFrame = null;
		// check input to see what we can use
		for (AnalysisInputItem item : inputs) {
			for (ProvidedObject po : item.getProvidedObjects()) {
				for (Object o : po.getObjects()) {
					if (o instanceof FuzzyGraph) {
						graph = (FuzzyGraph) o;
					} else if (o instanceof LogReader) {
						log = (LogReader) o;
					}
				}
			}
		}
		// setup UI
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		JPanel confPanel = new JPanel();
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.X_AXIS));
		confPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		confPanel.setBackground(new Color(80, 80, 80));
		RoundedPanel innerPanel = new RoundedPanel(20);
		innerPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBackground(new Color(140, 140, 140));
		innerPanel.setMaximumSize(new Dimension(390, 300));
		final JCheckBox discreteAnimBox = new JCheckBox(
				"discrete animation (inject timestamps)");
		discreteAnimation = false;
		if (log != null) {
			try {
				discreteAnimation = (log.getInstance(0)
						.getAuditTrailEntryList().get(0).getTimestamp() == null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		discreteAnimBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
		discreteAnimBox.setSelected(discreteAnimation);
		discreteAnimBox.setForeground(new Color(40, 40, 40));
		discreteAnimBox.setUI(new SlickerCheckBoxUI());
		discreteAnimBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				discreteAnimation = discreteAnimBox.isSelected();
			}
		});
		JButton animButton = new AutoFocusButton("view animation");
		animButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		animButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAnimation();
			}
		});
		// graph and log selector boxes
		final JComboBox graphSelector = new JComboBox(modelMap.keySet()
				.toArray());
		final JComboBox logSelector = new JComboBox(logMap.keySet().toArray());
		Dimension boxDim = new Dimension(300, 22);
		graphSelector.setMaximumSize(boxDim);
		logSelector.setMaximumSize(boxDim);
		// preselect configured objects
		for (String graphKey : modelMap.keySet()) {
			if (modelMap.get(graphKey) == graph) {
				graphSelector.setSelectedItem(graphKey);
				break;
			}
		}
		if (graph == null) {
			graph = modelMap.get(graphSelector.getSelectedItem());
		}
		if (log == null) {
			log = logMap.get(logSelector.getSelectedItem());
		}
		for (String logKey : logMap.keySet()) {
			if (logMap.get(logKey) == log) {
				logSelector.setSelectedItem(logKey);
			}
		}
		// add listeners
		graphSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				graph = modelMap.get(graphSelector.getSelectedItem());
			}
		});
		logSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				log = logMap.get(logSelector.getSelectedItem());
			}
		});
		if (RuntimeUtils.isRunningMacOsX()) {
			graphSelector.setOpaque(false);
			logSelector.setOpaque(false);
		}
		// labels
		JLabel graphLabel = new JLabel("Select Fuzzy Model to animate:");
		graphLabel.setOpaque(false);
		graphLabel.setForeground(new Color(30, 30, 30));
		graphLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		JLabel logLabel = new JLabel("Select Log for animation:");
		logLabel.setOpaque(false);
		logLabel.setForeground(new Color(30, 30, 30));
		logLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		// lookahead sliders
		lookaheadSlider = new NiceIntegerSlider("Lookahead", 1, 25, 5);
		lookaheadSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int max = lookaheadSlider.getValue() - 1;
				extraLookaheadSlider.getSlider().setMaximum(max);
				if (extraLookaheadSlider.getValue() > max) {
					extraLookaheadSlider.setValue(max);
				}
			}
		});
		extraLookaheadSlider = new NiceIntegerSlider("Extra lookahead", 0, 15,
				3);
		// assemble GUI
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(graphLabel);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(graphSelector);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(logLabel);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(logSelector);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(discreteAnimBox);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(lookaheadSlider);
		innerPanel.add(Box.createVerticalStrut(8));
		innerPanel.add(extraLookaheadSlider);
		innerPanel.add(Box.createVerticalStrut(15));
		innerPanel.add(animButton);
		innerPanel.add(Box.createVerticalStrut(15));
		confPanel.add(Box.createHorizontalGlue());
		confPanel.add(innerPanel);
		confPanel.add(Box.createHorizontalGlue());
		this.add(confPanel, BorderLayout.CENTER);
		HeaderBar header = new HeaderBar("Fuzzy Model Animation");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
		return this;
	}

	protected void createAnimation() {
		if (graph == null || log == null) {
			// cannot create animation;
			JOptionPane
					.showMessageDialog(MainUI.getInstance(),
							"This feature needs a Fuzzy Model\n"
									+ "and a log to work!",
							"Cannot create animation!",
							JOptionPane.WARNING_MESSAGE);
			return;
		}
		LogFilter projectionFilter = new FuzzyGraphProjectionFilter(graph);
		projectionFilter.setLowLevelFilter(log.getLogFilter());
		if (discreteAnimation == true) {
			TimestampInjectionFilter tsFilter = new TimestampInjectionFilter();
			tsFilter.setLowLevelFilter(projectionFilter);
			projectionFilter = tsFilter;
		}
		try {
			LogReader filteredLog = LogReaderFactory.createInstance(
					projectionFilter, log);
			animationFrame = new AnimationFrame(filteredLog, graph,
					lookaheadSlider.getValue(), extraLookaheadSlider.getValue());
			this.removeAll();
			this.add(animationFrame, BorderLayout.CENTER);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected void updateResources() {
		modelMap = new HashMap<String, FuzzyGraph>();
		logMap = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof FuzzyGraph) {
							modelMap.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(FuzzyGraph) object);
						} else if (object instanceof LogReader) {
							logMap.put(frame.getTitle() + " - "
									+ providedObject.getName(),
									(LogReader) object);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		/*
		 * AnalysisInputItem optLog = new AnalysisInputItem("Log file", 0, 1) {
		 * public boolean accepts(ProvidedObject object) { for(Object o :
		 * object.getObjects()) { if(o instanceof LogReader) { return true; } }
		 * return false; } };
		 */
		AnalysisInputItem fGraph = new AnalysisInputItem("Fuzzy Model", 1, 1) {
			public boolean accepts(ProvidedObject object) {
				for (Object o : object.getObjects()) {
					if (o instanceof FuzzyGraph) {
						return true;
					}
				}
				return false;
			}
		};
		return new AnalysisInputItem[] { fGraph };// , optLog};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "Forges an animation from a Fuzzy Model and a log";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Model Animation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (animationFrame != null) {
			return new ProvidedObject[] {
					new ProvidedObject("Fuzzy Model Animation",
							new Object[] { animationFrame.getAnimation() }),
					new ProvidedObject("Animation graph",
							new Object[] { animationFrame.getGraph() }),
					new ProvidedObject("Animation log",
							new Object[] { animationFrame.getLog() }) };
		} else {
			return new ProvidedObject[] {};
		}
	}

}
