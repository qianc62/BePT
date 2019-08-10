package org.processmining.analysis.redesign.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.deckfour.gantzgraf.canvas.event.GGToggleGraphSelectionModel;
import org.deckfour.gantzgraf.model.GGEdge;
import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.ui.GGGraphView;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.ui.SlickerComboBoxUI;
import org.processmining.analysis.edithlprocess.EditHighLevelProcessGui;
import org.processmining.analysis.petrinet.cpnexport.ColoredPetriNet;
import org.processmining.analysis.petrinet.cpnexport.CpnExport20;
import org.processmining.analysis.petrinet.cpnexport.CpnExportSettings;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.analysis.petrinet.cpnexport.ManagerConfiguration;
import org.processmining.analysis.redesign.*;
import org.processmining.analysis.redesign.ui.petri.*;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.pattern.Component;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.ComponentFrame;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

/**
 * The UI that provides the possibility to create redesigns for a given
 * high-level Petri net. The redesign is done step by step by transforming a
 * certain process part with a certain redesign type. In this way, gradually a
 * tree of redesign alternatives is created. The performance of the original and
 * the alternative models can be evaluated with simulation.
 * 
 * @see HLPetriNet
 * 
 * @author Mariska Netjes
 */

public class RedesignAnalysisUI extends JPanel implements Provider {

	private static final long serialVersionUID = 1L;

	/**
	 * Specifies the redesign type. The redesign type determines the specific
	 * transformation rule, e.g., parallel.
	 * 
	 * @return enumeration of the redesign types
	 */
	public enum RedesignType {
		Parallel, Sequence, Group, AddTask, RemoveTask, AddConstraint, RemoveConstraint, CopyModel;

		/**
		 * Specifies the various Key Performance Indicators. The kpi type
		 * determines which KPI is used when simulating, e.g., throughput time.
		 * 
		 * @return enumeration of the kpis.
		 */
	};

	public enum kpiType {
		ThroughputTime, WaitingTime, ResourceUtilization, InventoryCosts, CustomerSatisfaction, LaborFlexibility;
	}

	private HLPetriNet originalNet;
	private RedesignGraph redesignGraph;

	private JPanel contentPane;
	private JComponent view;
	private JComponent treeView;
	private GGGraphView graphView;
	private JComboBox redesignBox = new JComboBox(RedesignType.values());

	/**
	 * needed to show the online documentation of the CPN export
	 */
	private CpnExport20 myAlgorithm;

	/**
	 * the following strings are used to create the folder structure
	 */
	private static String folderPref = "C:" + "\\" + "RedesignAnalysis";
	private static String folderPrefCPN = "C:/RedesignAnalysis";
	private int experimentCounter = 1;
	public String locationForCurrentSimSettings = folderPref + "\\"
			+ "currentSimSettings";
	public static String locationForCurrentSimSettingsInCPNExport = folderPrefCPN
			+ "/currentSimSettings";
	public static String locationForCurrentSimModels = folderPref + "\\"
			+ "currentSimModels";
	public File currentSimFolder = new File(locationForCurrentSimModels);

	public RedesignAnalysisUI(HLPetriNet net) {
		originalNet = net;
		redesignGraph = new RedesignGraph(this, originalNet);

		/**
		 * give the original model a unique identifier, namely redesign0.
		 */
		originalNet.getPNModel().setIdentifier("redesign0");

		/**
		 * create the RedesignAnalysis folder at the preferred location.
		 */
		try {
			(new File(folderPref)).mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * create the first experiment folder
		 */
		String dirDest = folderPref + "\\" + "experiment_" + experimentCounter;
		try {
			(new File(dirDest)).mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * create the current simulation settings folder (CPN models look for
		 * these settings on a fixed location)
		 */
		File currentSetFolder = new File(locationForCurrentSimSettings);
		try {
			currentSetFolder.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * create all setting files in the created folder
		 */
		SimSettingsUI set = new SimSettingsUI(originalNet.getHLProcess()
				.getGlobalInfo(), locationForCurrentSimSettings);
		set.createSettingFiles(locationForCurrentSimSettings, originalNet
				.getHLProcess().getGlobalInfo().getCaseGenerationScheme());
		// create current simulation models folder
		try {
			currentSimFolder.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * create first simulation model, i.e., the CPN model of the original
		 * HLPetriNet
		 */
		createSimModel(originalNet);
		/**
		 * construct the UI
		 */
		constructUI();
	}

	/**
	 * Specifies the UI of the plug-in. The upper part of the UI contains the
	 * tree of alternatives models and the simulation functionality. The lower
	 * part of the UI contains the selected model and the redesign
	 * possibilities. Initially, no model is selected in the tree.
	 */
	private void constructUI() {
		/**
		 * Create the panel that contains all UI content.
		 */
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		HeaderBar header = new HeaderBar("Evolutionary Redesign");
		header.setHeight(35);
		this.add(header, BorderLayout.NORTH);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder());
		/**
		 * Create the upper part of the UI and the control panel for simulation
		 * buttons.
		 */
		GradientPanel upperView = new GradientPanel(new Color(60, 60, 60),
				new Color(90, 90, 90));
		upperView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		upperView.setLayout(new BorderLayout());
		JPanel controlPanel = new RoundedPanel(10, 5, 3);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBackground(new Color(140, 140, 140));
		controlPanel.setMinimumSize(new Dimension(200, 100));
		controlPanel.setMaximumSize(new Dimension(200, 300));
		controlPanel.setPreferredSize(new Dimension(200, 270));
		/**
		 * Create the button for the KPI selection.
		 */
		SlickerButton kpiButton = new SlickerButton("Select KPI");
		kpiButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		kpiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
			}
		});
		/**
		 * Create the possibilities for the KPI selection.
		 */
		final JComboBox kpiBox = new JComboBox(kpiType.values());
		kpiBox.setUI(new SlickerComboBoxUI());
		kpiBox.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
		/**
		 * Create the button for the selection of the simulation settings.
		 */
		SlickerButton settingsButton = new SlickerButton("Select settings");
		settingsButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		settingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * create UI for changing the simulation settings
				 */
				SimSettingsUI simUI = new SimSettingsUI(originalNet
						.getHLProcess().getGlobalInfo(),
						locationForCurrentSimSettings);
				MainUI.getInstance().createFrame("Set simulation settings",
						simUI.getPanel());
			}
		});
		/**
		 * Create the button for simulation of all selected models.
		 */
		SlickerButton simulateButton = new SlickerButton("Simulate models");
		simulateButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		simulateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent eventvent) {
				performSimulation();
			}
		});
		/**
		 * Create the button for removal of a model from the tree.
		 */
		SlickerButton removeButton = new SlickerButton("Remove model");
		removeButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeSelectedNode();
			}
		});
		/**
		 * Create the button for (de)selection of a model for simulation.
		 */
		SlickerButton addForSimButton = new SlickerButton(
				"(De)select for simulation");
		addForSimButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		addForSimButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inOrExcludeNodeForSimulation();
			}
		});
		/**
		 * Add all buttons to the control panel.
		 */
		JLabel title1 = new JLabel("Simulation actions");
		title1.setOpaque(false);
		title1.setFont(title1.getFont().deriveFont(14f));
		JLabel title2 = new JLabel("Edit actions");
		title2.setOpaque(false);
		title2.setFont(title2.getFont().deriveFont(14f));
		controlPanel.add(title1);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(kpiButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(kpiBox);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(settingsButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(simulateButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(title2);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(removeButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(addForSimButton);
		controlPanel.add(Box.createVerticalStrut(8));
		/**
		 * Add the control panel and the graph to the tree view
		 */
		this.graphView = new GGGraphView(redesignGraph, 1.5f);
		this.graphView.setMaximumSize(new Dimension(3000, 260));
		this.graphView.setMinimumSize(new Dimension(200, 260));
		this.graphView.setPreferredSize(new Dimension(2000, 260));
		upperView.add(graphView, BorderLayout.CENTER);
		upperView.add(controlPanel, BorderLayout.WEST);
		/**
		 * Add the tree view to the content pane. This finishes the upper part
		 * of the UI.
		 */
		this.treeView = upperView;
		contentPane.add(this.treeView, BorderLayout.NORTH);
		/**
		 * Create the lower part of the UI. Note that this is for the initial
		 * UI, i.e., no model is selected.
		 */
		GradientPanel tmpView = new GradientPanel(new Color(80, 80, 80),
				new Color(50, 50, 50));
		tmpView.setLayout(new BoxLayout(tmpView, BoxLayout.X_AXIS));
		RoundedPanel innerPanel = new RoundedPanel(20, 0, 0);
		innerPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		innerPanel.setBackground(new Color(150, 150, 150, 200));
		innerPanel.setMinimumSize(new Dimension(200, 50));
		innerPanel.setMaximumSize(new Dimension(300, 50));
		innerPanel.setPreferredSize(new Dimension(200, 50));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		innerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		JLabel innerLabel = new JLabel("Select a model.");
		innerLabel.setFont(innerLabel.getFont().deriveFont(16f));
		innerLabel.setForeground(new Color(20, 20, 20));
		innerLabel.setOpaque(false);
		innerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		innerLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		innerPanel.add(Box.createVerticalGlue());
		innerPanel.add(innerLabel);
		innerPanel.add(Box.createVerticalGlue());
		tmpView.add(Box.createHorizontalGlue());
		tmpView.add(innerPanel);
		tmpView.add(Box.createHorizontalGlue());
		this.view = tmpView;
		contentPane.add(tmpView, BorderLayout.CENTER);
		this.add(contentPane, BorderLayout.CENTER);
		revalidate();
	}

	/**
	 * When a model is selected in the upper part of the UI, the lower part of
	 * the UI has to show the selected model.
	 */
	public void showModel(final HLPetriNet model) {
		/**
		 * Retrieve the model that has to be shown.
		 */
		final PetriNetGraph pnGraph = new PetriNetGraph(this, model);
		/**
		 * Create the lower part of the UI and the control panel.
		 */
		GradientPanel modelView = new GradientPanel(new Color(60, 60, 60),
				new Color(90, 90, 90));
		modelView.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		modelView.setLayout(new BorderLayout());
		JPanel controlPanel = new RoundedPanel(10, 5, 3);
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBackground(new Color(140, 140, 140));
		controlPanel.setMinimumSize(new Dimension(200, 100));
		controlPanel.setMaximumSize(new Dimension(200, 300));
		controlPanel.setPreferredSize(new Dimension(200, 260));
		/**
		 * Create the possible transformation rules to select from.
		 */
		redesignBox.setUI(new SlickerComboBoxUI());
		redesignBox.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
		/**
		 * Create the button for selection of a transformation rule.
		 */
		SlickerButton selectTButton = new SlickerButton("Select transformation");
		selectTButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		selectTButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * display components in model
				 */
				displaySelectedComponents(model, pnGraph);
			}
		});
		/**
		 * Create the button for the deselection of a process part.
		 */
		SlickerButton selectButton = new SlickerButton("Deselect process part");
		selectButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		/**
		 * Reset selection of nodes
		 */
		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (GGNode node : pnGraph.nodes()) {
					node.setSelected(false);
				}
				for (GGEdge edge : pnGraph.edges()) {
					edge.setSelected(false);
				}
				/**
				 * display components in model
				 */
				displaySelectedComponents(model, pnGraph);
			}
		});
		/**
		 * Create the button for the creation of a redesign.
		 */
		SlickerButton redesignButton = new SlickerButton("Redesign model");
		redesignButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		/**
		 * the button is only enabled if a component is selected in the model.
		 */
		// updateButton(redesignButton, model, pnGraph);
		redesignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				/**
				 * get the selected nodes and the selected redesign type. NB. If
				 * no nodes are selected, then getSelectedPetriNetNodes()
				 * returns all nodes in the model.
				 */
				List<Transition> selectedTransitions = pnGraph
						.getSelectedPetriNetTransitions();
				RedesignType type = (RedesignType) redesignBox
						.getSelectedItem();
				Component comp = null;
				if (isIncludingAllSelTransitions(type.ordinal(), model,
						selectedTransitions)) {
					try {
						comp = RedesignFactory.getMinimalComponent(type
								.ordinal(), model, selectedTransitions);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				/**
				 * perform the selected transformation on the component.
				 */
				performRedesign(model, type, comp);
			}
		});

		/**
		 * Create the button for modification a model, i.e., for the refinement
		 * of the settings in the model.
		 */
		SlickerButton analysisButton = new SlickerButton("Modify model");
		analysisButton.setAlignmentX(SlickerButton.LEFT_ALIGNMENT);
		analysisButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				/**
				 * edit of the model through EditHighLevelProcessGui
				 */
				EditHighLevelProcessGui gui = new EditHighLevelProcessGui(model);
				ComponentFrame frame = MainUI.getInstance()
						.createAndReturnFrame("View/Edit High Level Process",
								gui.getVisualization());
				/**
				 * perform some actions after editing, more precisely, when the
				 * frame is closing
				 */
				frame.addInternalFrameListener(new InternalFrameListener() {
					public void internalFrameClosing(InternalFrameEvent e) {
						/**
						 * save of simulation model with changes
						 */
						HLPetriNet cloned = (HLPetriNet) model.clone();
						ColoredPetriNet netToExport = new ColoredPetriNet(
								cloned);
						CpnExportSettings exportCPN = new CpnExportSettings(
								myAlgorithm, netToExport, false);
						exportCPN.saveCPNmodel();
						/**
						 * save of case gen. scheme to file, i.e., overwrite the
						 * arrival rate file
						 */
						String distrLoc = locationForCurrentSimSettings + "\\"
								+ "arrivalRate.sml";
						FileWriter dout = null;
						try {
							dout = new FileWriter(distrLoc);
							dout.write("fun readArrivalRate() = "
									+ CpnUtils.getCpnDistributionFunction(model
											.getHLProcess().getGlobalInfo()
											.getCaseGenerationScheme()) + ";");
							dout.close();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}

					public void internalFrameActivated(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

					public void internalFrameClosed(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

					public void internalFrameDeiconified(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

					public void internalFrameIconified(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

					public void internalFrameOpened(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

					public void internalFrameDeactivated(InternalFrameEvent e) {
						// TODO Auto-generated method stub
					}

				});
			}
		});
		/**
		 * Add the buttons to the control panel.
		 */
		JLabel title = new JLabel("Redesign actions");
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(14f));
		JLabel title2 = new JLabel("Refinement actions");
		title2.setOpaque(false);
		title2.setFont(title2.getFont().deriveFont(14f));
		controlPanel.add(title);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(selectTButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(redesignBox);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(selectButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(redesignButton);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(title2);
		controlPanel.add(Box.createVerticalStrut(8));
		controlPanel.add(analysisButton);
		/**
		 * Add the control panel and the graph to the lower half of the UI.
		 */
		final GGGraphView graphView = new GGGraphView();
		graphView.setGraphSelectionModel(new GGToggleGraphSelectionModel());
		modelView.add(graphView, BorderLayout.CENTER);
		modelView.add(controlPanel, BorderLayout.WEST);
		switchView(modelView);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				graphView.setGraph(pnGraph);
			}
		});
	}

	/**
	 * Determine all components on which the selected redesign type can be
	 * applied. If any transitions in the model are selected, these transitions
	 * need to be included in the resulting components.
	 * 
	 * @param type
	 *            RedesignType the selected redesign type
	 * @param selectedTransitions
	 *            List<Transition> the selected transitions
	 * @param model
	 *            HLPetriNet the input model for redesign
	 * @return comps Set<Component> set of all components, if no transitions
	 *         selected
	 * @return selComps Set<Component> set of components including all selected
	 *         transitions
	 */
	public Set<Component> getComponentsToDisplay(RedesignType type,
			List<Transition> selectedTransitions, HLPetriNet model) {
		Set<Component> comps = new HashSet<Component>();
		/**
		 * start with retrieving all components present in the model.
		 */
		try {
			comps = RedesignFactory.getAllComponents(type.ordinal(), model);
		} catch (Exception ex) {
			Message.add("No components created!", Message.ERROR);
			ex.printStackTrace();
		}
		/**
		 * if no Transitions are selected, selectedTransitions includes all
		 * Transitions. All components in the model are returned
		 */
		if (selectedTransitions.size() == model.getPNModel().getTransitions()
				.size()) {
			return comps;
		}
		/**
		 * All components enclosing all selectedTransitions are returned
		 */
		else {
			Set<Component> selComps = new HashSet<Component>();
			for (Component comp : comps) {
				if (comp.getNodeList().containsAll(selectedTransitions)) {
					selComps.add(comp);
				}
			}
			return selComps;
		}

	}

	/**
	 * highlight the transitions in the model that are included in the set of
	 * components
	 * 
	 * @param comps
	 *            Set<Component> the components to display
	 * @param model
	 *            HLPetriNet the input model for redesign
	 * @param pnGraph
	 *            PetriNetGraph the visualization of the PetriNet in model
	 */
	public void displayComponents(Set<Component> comps, HLPetriNet model,
			PetriNetGraph pnGraph) {
		/**
		 * first, reset highlight of all transitions.
		 */
		// showModel(model);
		resetDisplay(model, pnGraph);
		/**
		 * then, highlight nodes nodes that are selected: green nodes that are
		 * in a component enclosing all selected nodes: yellow other nodes: red
		 */
		if (!comps.isEmpty()) {
			Set<Transition> allIncludedNodes = new HashSet<Transition>();
			Set<Transition> yellowNodes = new HashSet<Transition>();
			for (Component comp : comps) {
				/**
				 * determine nodes included in any of the components
				 */
				if (comp.getTransitions().size() != 0) {
					for (Transition node : comp.getTransitions()) {
						allIncludedNodes.add(node);
						if (model.getPNModel().getTransitions().contains(node)) {
							PetriNetTransition t = (PetriNetTransition) pnGraph
									.getNodeByIdentifier(node.getIdentifier());
							/**
							 * node is in component, if selected then set to
							 * IsSelectedInComponent
							 */
							if (t.isSelected()) {
								t.setIsSelectedInComponent(true);
								/**
								 * else, add to the set of yellow nodes, it is
								 * in a component, but not selected.
								 */
							} else {
								yellowNodes.add(node);
							}
						}
					}
				}
			}
			/**
			 * set yellow nodes to ToBeSelectedInComponent
			 */
			if (!yellowNodes.isEmpty()) {
				for (Transition yNode : yellowNodes) {
					PetriNetTransition t = (PetriNetTransition) pnGraph
							.getNodeByIdentifier(yNode.getIdentifier());
					t.setToBeSelectedInComponent(true);
				}
			}
			/**
			 * set remaining nodes, i.e., the nodes not in a component to
			 * NotToBeSelectedInComponent. This are the nodes that are not in
			 * allIncludedNodes
			 */
			for (Transition node : model.getPNModel().getTransitions()) {
				if (!allIncludedNodes.contains(node)) {
					PetriNetTransition t = (PetriNetTransition) pnGraph
							.getNodeByIdentifier(node.getIdentifier());
					t.setNotToBeSelectedInComponent(true);
				}
			}
		} else {
			Message.add("There are no components to highlight.", Message.ERROR);
			/**
			 * selected nodes are green, all other nodes are red
			 */
			for (Transition node : model.getPNModel().getTransitions()) {
				PetriNetTransition t = (PetriNetTransition) pnGraph
						.getNodeByIdentifier(node.getIdentifier());
				if (t.isSelected()) {
					t.setIsSelectedInComponent(true);
				} else {
					t.setNotToBeSelectedInComponent(true);
				}
			}
		}
	}

	/**
	 * highlight the transitions in the model that are included in a component
	 * 
	 * @param type
	 *            RedesignType the selected redesign type
	 * @param model
	 *            HLPetriNet the input model for redesign
	 * @param pnGraph
	 *            PetriNetGraph the visualization of the PetriNet in model
	 */
	public void displaySelectedComponents(HLPetriNet model,
			PetriNetGraph pnGraph) {
		RedesignType type = (RedesignType) redesignBox.getSelectedItem();
		List<Transition> selectedTransitions = pnGraph
				.getSelectedPetriNetTransitions();
		Set<Component> comps = getComponentsToDisplay(type,
				selectedTransitions, model);
		displayComponents(comps, model, pnGraph);
	}

	/**
	 * Displays a given component in the model by highlighting its transitions
	 * 
	 * @param comp
	 *            Component the given component
	 * @param model
	 *            HLPetriNet the input model for redesign
	 * @param pnGraph
	 *            PetriNetGraph the visualization of the PetriNet in model
	 */
	public void displayComponent(Component comp, HLPetriNet model,
			PetriNetGraph pnGraph) {
		/**
		 * first, reset highlight of all transitions and check if redesignButton
		 * needs to be enabled.
		 */
		resetDisplay(model, pnGraph);
		/**
		 * then, highlight the transitions in the model
		 */
		if (comp != null) {
			if (comp.getTransitions().size() != 0) {
				for (Transition node : comp.getTransitions()) {
					if (model.getPNModel().getTransitions().contains(node)) {
						PetriNetTransition t = (PetriNetTransition) pnGraph
								.getNodeByIdentifier(node.getIdentifier());
						t.setIsSelectedInComponent(true);
					}
				}
			} else {
				Message.add("There is no component to highlight.",
						Message.ERROR);
			}
		} else {
			Message.add("There is no component to highlight.", Message.ERROR);
		}
	}

	/**
	 * Reset highlight of all transitions
	 */
	public void resetDisplay(HLPetriNet model, PetriNetGraph pnGraph) {
		for (Transition t : model.getPNModel().getTransitions()) {
			PetriNetTransition pt = (PetriNetTransition) pnGraph
					.getNodeByIdentifier(t.getIdentifier());
			pt.setIsSelectedInComponent(false);
			pt.setToBeSelectedInComponent(false);
			pt.setNotToBeSelectedInComponent(false);
		}
		// showModel(model);
	}

	/**
	 * Determines if button is enabled or disabled
	 * 
	 * @param button
	 *            SlickerButton the involved button
	 */
	public void updateButton(SlickerButton button, HLPetriNet model,
			PetriNetGraph pnGraph) {
		RedesignType type = (RedesignType) redesignBox.getSelectedItem();
		List<Transition> selectedTransitions = pnGraph
				.getSelectedPetriNetTransitions();
		if (isIncludingAllSelTransitions(type.ordinal(), model,
				selectedTransitions)) {
			button.setEnabled(true);
		} else {
			button.setEnabled(false);
		}
		// showModel(model);
	}

	/**
	 * Determines the minimal component enclosing the selected nodes and checks
	 * if the selected nodes enclose all transitions in the component, i.e., the
	 * component does not enclose more transitions than the selected ones.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @param nodes
	 *            List<PNNode> the nodes to redesign, can be all nodes.
	 * @returns true Boolean returned if component encloses exactly the selected
	 *          transitions.
	 */
	public static boolean isIncludingAllSelTransitions(int indexRedesignType,
			HLPetriNet model, List<Transition> transitions) {
		Component comp = null;
		try {
			comp = RedesignFactory.getMinimalComponent(indexRedesignType,
					model, transitions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (comp != null && transitions.containsAll(comp.getTransitions())) {
			return true;
		}
		return false;
	}

	/**
	 * Specifies the creation of a redesign, i.e., an alternative model.
	 * 
	 * @param model
	 *            HLPetriNet the input model of the redesign
	 * @param type
	 *            RedesignType the selected transformation rule
	 * @param comp
	 *            Component the component including the nodes to be redesigned
	 */
	private void performRedesign(HLPetriNet model, RedesignType type,
			Component comp) {
		try {
			/**
			 * create redesign model and add it to a (new) node in the
			 * alternatives tree
			 */
			HLPetriNet alt = RedesignFactory.getRedesign(type.ordinal(), model,
					comp);
			if (alt != null) {
				RedesignNode redesignNode = redesignGraph.addRedesign(model,
						alt, type);

				/**
				 * give the redesign model a unique ID, which is the modelID
				 * created with the redesign node creation.
				 */
				int modelID = redesignNode.getModelID();
				alt.getPNModel().setIdentifier("redesign" + modelID);

				/**
				 * create corresponding CPN model
				 */
				createSimModel(alt);

				/**
				 * reset of selection of the input model in the tree
				 */
				for (GGNode node : redesignGraph.nodes()) {
					node.setSelected(false);
				}
				/**
				 * trigger selection of active model, i.e., the alternative
				 * model.
				 */
				graphView.getCanvas().getGraphSelectionModel().clickedNode(
						redesignNode, graphView.getCanvas(), 0, 0);
				redesignGraph.setWidth(0);
				redesignGraph.setHeight(0);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						graphView.setGraph(redesignGraph);
					}
				});
				showModel(alt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Specifies the removal of an alternative model from the tree. Note that
	 * the original model can not be removed.
	 */
	private void removeSelectedNode() {
		/**
		 * check if a model is selected
		 */
		if (redesignGraph.getSelectedRedesignNodes().size() != 1) {
			/**
			 * if no model or multiple models are selected, notify user.
			 */
			JOptionPane.showMessageDialog(null,
					"Please make sure that exactly one model is selected.");
			/**
			 * if the original model is selected, notify user.
			 */
		} else if (redesignGraph.getSelectedRedesignNodes().get(0).getModelID() == 0) {
			JOptionPane.showMessageDialog(null,
					"You are not allowed to remove the first node, i.e., \n"
							+ "the node related to the original model.");
		} else {
			/**
			 * if one model is selected, provide user with a message asking
			 * (s)he is sure
			 */
			final JOptionPane optionPane = new JOptionPane(
					"You are about to remove the selected node and its successors.",
					JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			final JDialog dialog = new JDialog(MainUI.getFrames()[0],
					"Warning", true);
			dialog.setContentPane(optionPane);
			optionPane.addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent e) {
					String prop = e.getPropertyName();
					if (dialog.isVisible() && (e.getSource() == optionPane)
							&& (prop.equals(JOptionPane.VALUE_PROPERTY))) {
						dialog.setVisible(false);
					}
				}
			});
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);

			int value = ((Integer) optionPane.getValue()).intValue();
			if (value == JOptionPane.OK_OPTION) {
				/**
				 * user select OK, remove the selected model and its successors
				 */
				RedesignNode nodeToRemove = redesignGraph
						.getSelectedRedesignNodes().get(0);
				/**
				 * but first, remove related CPN models from currentSimModels
				 * folder
				 */
				String rName = currentSimFolder + "\\redesign"
						+ nodeToRemove.getModelID() + ".cpn";
				File r = new File(rName);
				r.delete();
				for (RedesignNode suc : redesignGraph
						.getAllSuccessors(nodeToRemove)) {
					String fileName = currentSimFolder + "\\redesign"
							+ suc.getModelID() + ".cpn";
					File f = new File(fileName);
					f.delete();
				}
				/**
				 * then, remove model and successors from the tree
				 */
				redesignGraph.removeAllSuccessors(nodeToRemove);
				redesignGraph.remove(nodeToRemove);
				redesignGraph.removeDanglingEdges();
				// and select first node to show an existing process model
				RedesignNode firstNode = redesignGraph.getNode(0);
				firstNode.setSelected(true);
				this.showModel((firstNode).getModel());
				redesignGraph.updateView();
			}
		}
	}

	/**
	 * Specifies the (de)selection of a model for simulation.
	 * 
	 */
	private void inOrExcludeNodeForSimulation() {
		/**
		 * check if a model is selected
		 */
		if (redesignGraph.getSelectedRedesignNodes().size() != 1) {
			/**
			 * if no model or multiple models are selected, notify user.
			 */
			JOptionPane.showMessageDialog(null,
					"Please make sure that exactly one node is selected.");
		} else {
			/**
			 * in- or exclude the selected model for simulation
			 */
			RedesignNode nodeToSim = redesignGraph.getSelectedRedesignNodes()
					.get(0);
			/**
			 * original model can not be excluded, has to remain on
			 * selectedForSimulation
			 */
			if (redesignGraph.getRedesignNodesForSimulation().contains(
					nodeToSim)
					&& nodeToSim.getModelID() != 0) {
				/**
				 * model has to be excluded
				 */
				nodeToSim.setSelectedForSimulation(false);
			} else {
				/**
				 * model has to be included
				 */
				nodeToSim.setSelectedForSimulation(true);
			}
			redesignGraph.updateView();
		}
	}

	/**
	 * Specifies the simulation of all models that are selected for simulation.
	 */
	private void performSimulation() {
		/**
		 * the folder location to place all results in
		 */
		String dirDest = folderPref + "\\" + "experiment_" + experimentCounter;
		/**
		 * create a file for the means of all nodes to calculate statistics over
		 * all simulation results.
		 */
		String meansDest = dirDest + "\\" + "meansOfAllNodes";
		File meansFile = new File(meansDest);
		try {
			meansFile.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * copy the folder with the current simulation settings first, create a
		 * folder
		 */
		String settingDest = dirDest + "\\" + "simSettings";
		File newSettingFolder = new File(settingDest);
		try {
			newSettingFolder.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * then, copy current simulation setting files to the created
		 * simSettings folder
		 */
		File oldSettingFolder = new File(locationForCurrentSimSettings);
		copyFiles(oldSettingFolder, newSettingFolder);

		/**
		 * set boolean isSimulated to false for all models to distinguish last
		 * experiment from previous experiments
		 */
		for (RedesignNode node : redesignGraph.getRedesignNodes()) {
			node.setSimulated(false);
		}

		/**
		 * start the simulation engine
		 */
		// SingleStartDeamonSimulator.startSingleDeamon();
		// /**
		// * decide which models to simulate
		// */
		// ArrayList<RedesignNode> nodesToSimulate = new
		// ArrayList<RedesignNode>();
		// /**
		// * all models are simulated if no models or only the original model
		// are selected for simulation.
		// */
		// if (redesignGraph.getRedesignNodesForSimulation().size() <= 1) {
		// nodesToSimulate = redesignGraph.getRedesignNodes();
		// } else {
		// /**
		// * models that are selected for simulation are simulated.
		// */
		// nodesToSimulate = redesignGraph.getRedesignNodesForSimulation();
		// }
		// /**
		// * perform the simulation and calculate statistics per model.
		// */
		// for (RedesignNode node : nodesToSimulate) {
		// int i = node.getModelID();
		// /**
		// * create a folder for the (simulation) results of this node
		// */
		// String nodeDest = dirDest + "\\" + node.label()[0];
		// File nodeFile = new File(nodeDest);
		// try {
		// nodeFile.mkdir();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// /**
		// * copy the simulation model from the currentSimModels folder to the
		// result folder
		// */
		// copyOneFile(locationForCurrentSimModels + "\\" + "redesign" + i +
		// ".cpn", nodeDest + "\\" + "redesign" + i + ".cpn");
		// /**
		// * run simulations for the specified number of sub runs
		// */
		// SimSettingsUI ui = new
		// SimSettingsUI(originalNet.getHLProcess().getGlobalInfo(),
		// locationForCurrentSimSettings);
		// for (int j=1; j<=ui.readRunFromFile(); j++) {
		// /**
		// * create a folder for the simulation results of this sub run
		// */
		// String simDest = nodeDest + "\\" + "sim_" + j;
		// try {
		// (new File(simDest)).mkdir();
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// /**
		// * change file with folder location to write to the current sim folder
		// */
		// String locDest = locationForCurrentSimSettings + "\\" +
		// "valFolder.sml";
		// FileWriter out = null;
		// try {
		// out = new FileWriter(locDest);
		// // an example:
		// out.write("val FOLDER = \"C:/RedesignAnalysis/experiment_1/Original_0/sim_1\"");
		// //
		// out.write("val FOLDER = \"" + folderPrefCPN + "/experiment_" +
		// experimentCounter + "/" + node.label()[0] + "/sim_" + j + "\"");
		// out.close();
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
		// /**
		// * perform simulation for this sub run
		// */
		// Simulation sim = new Simulation(locationForCurrentSimModels + "\\" +
		// "redesign" + i + ".cpn");
		// sim.executeSteps(10000);
		// sim.destroy();
		// }
		//         
		// /**
		// * calculate statistics over all sub run results for this model
		// */
		// Statistics calc = new Statistics(nodeFile);
		// calc.calc();
		// /**
		// * create a file for the simulation results of this model
		// */
		// String resultDest = nodeDest + "\\" + "simResult.txt";
		// calc.createResultFile(resultDest);
		//         
		// /**
		// * add the calculated mean of this model to the meansFile
		// */
		// FileWriter out = null;
		// try {
		// out = new FileWriter(meansFile,true);
		// out.write(calc.getMean() + "\n");
		// out.flush();
		// out.close();
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
		//         
		// /**
		// * show the calculated statistics on the model in the tree
		// */
		// Double mean = calc.getMean();
		// Double lowerBound = calc.getConf95LowerBound();
		// Double upperBound = calc.getConf95UpperBound();
		// NumberFormat numberFormatter;
		// String meanOut;
		// String lowerOut;
		// String upperOut;
		// numberFormatter = NumberFormat.getNumberInstance(getLocale());
		// meanOut = numberFormatter.format(mean);
		// lowerOut = numberFormatter.format(lowerBound);
		// upperOut = numberFormatter.format(upperBound);
		//         
		// String[] s = {node.label()[0], "tpt: mean = " + meanOut, "conf95 = ("
		// + lowerOut + " ; " + upperOut + ")"};
		// node.setLabel(s);
		// node.setSimulated(true);
		// node.setPerformance(lowerBound, mean, upperBound);
		// }
		// /**
		// * done with the simulation
		// */
		// /**
		// * compare performance of simulated models with original model.
		// * Note that the meansFile is filled with the means of all nodes.
		// */
		// RedesignNode origNode = redesignGraph.getNode(0);
		// double origLowerBound = origNode.getLowerBound();
		// double origUpperBound = origNode.getUpperBound();
		// /**
		// * calculate statistics over all means
		// */
		// Statistics calc = new Statistics(meansDest);
		// calc.calcForOneFile();
		// double min = calc.getMin();
		// double max = calc.getMax();
		// double firstQuartile = calc.getFirstQuartile();
		// double median = calc.getMedian();
		// double thirdQuartile = calc.getThirdQuartile();
		// Message.add("The first quartile: " + firstQuartile + ", the median:"
		// +
		// median + ", the third quartile: " + thirdQuartile + ".");
		// /**
		// * compare all simulated nodes with the original node and
		// * set the relative performance for the simulated nodes.
		// */
		// for (RedesignNode simNode :
		// redesignGraph.getSimulatedRedesignNodes()) {
		// double simLowerBound = simNode.getLowerBound();
		// double simUpperBound = simNode.getUpperBound();
		// double simMean = simNode.getMean();
		// if (simUpperBound < origLowerBound) {
		// /**
		// * simulated model performs significantly better than original
		// */
		// simNode.setBetterPerforming(true);
		// /**
		// * determine how much better the node is
		// */
		// if (simMean == min) {
		// simNode.setBestThePerforming(true);
		// } else
		// if (simMean < ((firstQuartile + min) / 2)) {
		// simNode.setBestPerforming(true);
		// } else
		// if (simMean < firstQuartile) {
		// simNode.setBestSecondPerforming(true);
		// } else
		// if (simMean < ((median + firstQuartile) / 2)) {
		// simNode.setBestThirdPerforming(true);
		// } else {
		// simNode.setBestFourthPerforming(true);
		// }
		// }
		// else if (origUpperBound < simLowerBound) {
		// /**
		// * simulated model performs significantly worse than original
		// */
		// simNode.setWorsePerforming(true);
		// /**
		// * determine how much worse the node is
		// */
		// if (simMean < ((thirdQuartile + median) / 2)) {
		// simNode.setWorstFourthPerforming(true);
		// } else
		// if (simMean < thirdQuartile) {
		// simNode.setWorstThirdPerforming(true);
		// } else
		// if (simMean < ((max + thirdQuartile) / 2)) {
		// simNode.setWorstSecondPerforming(true);
		// } else {
		// simNode.setWorstPerforming(true);
		// }
		// } else {
		// /**
		// * simulated model performs equal to original
		// */
		// simNode.setEqualPerforming(true);
		// }
		// }
		//         
		// /**
		// * update the visualization
		// */
		// redesignGraph.setWidth(0);
		// redesignGraph.setHeight(0);
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// graphView.setGraph(redesignGraph);
		// }
		// });
		/**
		 * increase the experimentCounter
		 */
		experimentCounter++;
		/**
		 * create a folder for the results of the next experiment
		 */
		String newDest = folderPref + "\\" + "experiment_" + experimentCounter;
		try {
			(new File(newDest)).mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Specifies the creation of a simulation model.
	 * 
	 * @see CPN Export
	 * @param net
	 *            HLPetriNet the net from which a simulation model is created.
	 */
	private void createSimModel(HLPetriNet net) {
		HLPetriNet cloned = (HLPetriNet) net.clone();
		ColoredPetriNet netToExport = new ColoredPetriNet(cloned);
		String filename = locationForCurrentSimModels + "\\"
				+ net.getPNModel().getIdentifier() + ".cpn";
		try {
			FileOutputStream out = new FileOutputStream(filename);
			BufferedWriter outWriter = new BufferedWriter(
					new OutputStreamWriter(out));
			netToExport.isRedesign(true);
			ManagerConfiguration.getInstance().setRedesignConfiguration();
			netToExport.writeToFile(outWriter, null, null);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Specifies the copying of files from one folder to another folder.
	 * 
	 * @param oldFolder
	 *            File the folder from which files have to be copied
	 * @param newFolder
	 *            File the folder to which files are copied
	 */
	private void copyFiles(File oldFolder, File newFolder) {
		for (File fileToCopy : oldFolder.listFiles()) {
			File copiedFile = new File(newFolder.getAbsolutePath() + "\\"
					+ fileToCopy.getName());
			try {
				FileInputStream source = new FileInputStream(fileToCopy);
				FileOutputStream destination = new FileOutputStream(copiedFile);

				FileChannel sourceFileChannel = source.getChannel();
				FileChannel destinationFileChannel = destination.getChannel();

				long size = sourceFileChannel.size();
				sourceFileChannel.transferTo(0, size, destinationFileChannel);
				source.close();
				destination.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}

	/**
	 * Specifies the copying of one file.
	 * 
	 * @param oldPath
	 *            String the old path of the file
	 * @param newPath
	 *            String the new path of the file
	 */
	private void copyOneFile(String oldPath, String newPath) {
		File copiedFile = new File(newPath);
		try {
			FileInputStream source = new FileInputStream(oldPath);
			FileOutputStream destination = new FileOutputStream(copiedFile);

			FileChannel sourceFileChannel = source.getChannel();
			FileChannel destinationFileChannel = destination.getChannel();

			long size = sourceFileChannel.size();
			sourceFileChannel.transferTo(0, size, destinationFileChannel);
			source.close();
			destination.close();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Specifies the switch from the view on the lower part of the UI when no
	 * model is selected to the view on a selected model.
	 */
	private void switchView(JComponent updatedView) {
		contentPane.remove(this.view);
		this.view = updatedView;
		contentPane.add(this.view, BorderLayout.CENTER);
		contentPane.revalidate();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		// TODO Auto-generated method stub
		return null;
	}
}
