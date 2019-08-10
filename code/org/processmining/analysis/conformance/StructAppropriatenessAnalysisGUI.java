package org.processmining.analysis.conformance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisGUI;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisResult;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GuiNotificationTarget;

import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

public class StructAppropriatenessAnalysisGUI extends AnalysisGUI implements
		GuiNotificationTarget {

	// technical attributes
	private DiagnosticPetriNet analyzedPetriNet;
	private HashMap mapping;
	private PetriNet reducedPetriNet;
	private StructuralAnalysisResult structuralResult;
	private StateSpaceExplorationResult stateSpaceResult;
	private DisplayState currentVisualization = DisplayState.STRUCTURAL;

	// user interface related attributes
	private JLabel structuralAppropriatenessMeasureLabel = new JLabel(
			"<html>Simple<br>Structural Appropriateness:<html>");
	private JLabel structuralAppropriatenessMeasure = new JLabel();
	private JLabel advStructuralAppropriatenessMeasureLabel = new JLabel(
			"<html>Advanced<br>Structural Appropriateness:<html>");
	private JLabel advStructuralAppropriatenessMeasure = new JLabel();
	private JScrollPane modelContainer;
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	private JPanel bottomNorthPanel = new JPanel(new BorderLayout());
	private JPanel graphPanel = new JPanel(new BorderLayout());
	private JPanel measurementPanel = new JPanel();
	private JLabel measuresHeading = new JLabel("Measures");
	private GrappaPanel grappaPanel;
	private JPanel graphTypePanel = new JPanel();
	private JPanel vizOptionsPanel = new JPanel();
	private GUIPropertyBoolean redundantInvisiblesOption; // visualization
	// option
	private GUIPropertyBoolean alternativeDuplicatesOption; // visualization

	// option

	/**
	 * Creates the results view for the Performance category.
	 * 
	 * @param analysisResults
	 *            contains the {@link AnalysisResult AnalysisResult} objects
	 *            that have been requested by this category
	 */
	public StructAppropriatenessAnalysisGUI(Set<AnalysisResult> analysisResults) {
		this(analysisResults, true, true);
	}

	/**
	 * Creates the results view for the Performance category.
	 * 
	 * @param analysisResults
	 *            contains the {@link AnalysisResult AnalysisResult} objects
	 *            that have been requested by this category
	 * @param redundantInvisibles
	 *            indicates whether this visualization option is switched on at
	 *            startup
	 * @param alternativeDuplicate
	 *            indicates whether this visualization option is switched on at
	 *            startup
	 */
	public StructAppropriatenessAnalysisGUI(
			Set<AnalysisResult> analysisResults, boolean redundantInvisibles,
			boolean alternativeDuplicate) {
		super(analysisResults);
		// retrieve the result objects
		Iterator<AnalysisResult> results = analysisResults.iterator();
		while (results.hasNext()) {
			AnalysisResult currentResult = results.next();
			if (currentResult != null) {
				if (currentResult instanceof StructuralAnalysisResult) {
					structuralResult = (StructuralAnalysisResult) currentResult;
				}
				if (currentResult instanceof StateSpaceExplorationResult) {
					stateSpaceResult = (StateSpaceExplorationResult) currentResult;
				}
			} else {
				// an analysis result can be null if the user aborted the
				// computation process.. --> display empty tab
				return;
			}
		}

		// init GUI properties for visualization options
		redundantInvisiblesOption = new GUIPropertyBoolean(
				"Redundant Invisible Tasks",
				"Highlights invisible tasks that are redundant (not affect the behavior)",
				redundantInvisibles, this);
		alternativeDuplicatesOption = new GUIPropertyBoolean(
				"Alternative Duplicate Tasks",
				"Highlights duplicate tasks that list alternative behavior",
				alternativeDuplicate, this);

		// may be null if aaS not computed or no redundant invisibles were found
		reducedPetriNet = structuralResult.getReducedPetriNet();
		analyzedPetriNet = (DiagnosticPetriNet) structuralResult.analyzedPetriNet;
		// add the information about redundant invisibles to the diagnostic
		// petri net
		ArrayList<Transition> redundantInv = structuralResult
				.getRedundantInvisibleTasks();
		// record number of redundant invisibles for aaS metric
		stateSpaceResult.updateNumberOfRedundantInvisibles(redundantInv.size());
		// now walk through all the redundant invisibles ..
		Iterator<Transition> redundantInvIt = redundantInv.iterator();
		while (redundantInvIt.hasNext()) {
			Transition currentRedundant = redundantInvIt.next();
			DiagnosticTransition diagnosticPendent = (DiagnosticTransition) analyzedPetriNet
					.findTransition(currentRedundant);
			// .. and record redundant invisible status for visualization
			diagnosticPendent.setRedundantInvisibleTask();
		}

		// add the information about alternative to the diagnostic petri net
		Iterator<Transition> allTasks = stateSpaceResult.exploredPetriNet
				.getTransitions().iterator();
		// now walk through all tasks..
		while (allTasks.hasNext()) {
			DiagnosticTransition current = (DiagnosticTransition) allTasks
					.next();
			DiagnosticTransition diagnosticPendent = (DiagnosticTransition) analyzedPetriNet
					.findTransition(current);

			if (current.isAlternativeDuplicateTask() == true) {
				// .. and potentially record alternative duplicate status for
				// visualization
				diagnosticPendent.setAlternativeDuplicateTask();
			}
		}

		try {
			// build GUI
			jbInit();
			// connect functionality to GUI elements
			registerGuiActionListener();
		} catch (Exception ex) {
			Message.add(
					"Program exception while displaying conformance check results:\n"
							+ ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	/**
	 * Construct the user interface for this conformance check analysis.
	 * 
	 * @throws Exception
	 *             Any exception is just passed to the caller.
	 */
	private void jbInit() throws Exception {
		measuresHeading.setForeground(new Color(100, 100, 100));
		// set up all help texts
		structuralAppropriatenessMeasure
				.setToolTipText("Degree of structural appropriateness based on the size of the model.");
		advStructuralAppropriatenessMeasure
				.setToolTipText("Degree of structural appropriateness based on the number of redundant invisible and alternative duplicate tasks");

		measurementPanel.setLayout(new BoxLayout(measurementPanel,
				BoxLayout.PAGE_AXIS));
		measurementPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
				10));
		measurementPanel.add(measuresHeading);
		measurementPanel.add(Box.createRigidArea(new Dimension(0, 20)));

		// build the layout (only the selected options)
		if (structuralResult.calculateStructuralAppropriateness() == true) { // configurated
			float strApp = structuralResult
					.getStructuralAppropriatenessMeasure();
			structuralAppropriatenessMeasure.setText("" + strApp);
			measurementPanel.add(structuralAppropriatenessMeasureLabel);
			measurementPanel.add(structuralAppropriatenessMeasure);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 15)));

			// / PLUGIN TEST START
			Message.add("Structural Appropriateness: " + strApp, Message.TEST);
			// PLUGIN TEST END
		}
		if (stateSpaceResult.calculateImprovedStructuralAppropriateness() == true) { // configurated
			float advStrApp = stateSpaceResult
					.getImprovedStructuralAppropriatenessMeasure();
			advStructuralAppropriatenessMeasure.setText("" + advStrApp);
			measurementPanel.add(advStructuralAppropriatenessMeasureLabel);
			measurementPanel.add(advStructuralAppropriatenessMeasure);
			measurementPanel.add(Box.createRigidArea(new Dimension(0, 5)));

			// / PLUGIN TEST START
			Message.add("Improved Structural Appropriateness: " + advStrApp,
					Message.TEST);
			// PLUGIN TEST END
		}

		// / aaS must have been calculated out in order to show diagnostic
		// visualization
		if (stateSpaceResult.calculateImprovedStructuralAppropriateness() == true) { // configurated
			vizOptionsPanel.add(redundantInvisiblesOption.getPropertyPanel());
			vizOptionsPanel.add(alternativeDuplicatesOption.getPropertyPanel());
			graphTypePanel.add(vizOptionsPanel);
		}

		currentVisualization.redundantInvisiblesOption = redundantInvisiblesOption
				.getValue();
		currentVisualization.alternativeDuplicatesOption = alternativeDuplicatesOption
				.getValue();

		grappaPanel = structuralResult.getVisualization(currentVisualization);
		if (grappaPanel != null) {
			grappaPanel.addGrappaListener(new DiagnosticGrappaAdapter());
		}
		mapping = new HashMap();
		buildGraphMapping(mapping, grappaPanel.getSubgraph());

		modelContainer = new JScrollPane(grappaPanel);
		graphPanel.add(modelContainer, BorderLayout.CENTER);
		JSplitPane resultSplitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, graphPanel, measurementPanel);
		resultSplitPane.setDividerLocation(resultSplitPane.getSize().width
				- resultSplitPane.getInsets().right
				- resultSplitPane.getDividerSize()
				- measurementPanel.getWidth());
		resultSplitPane.setOneTouchExpandable(true);
		resultSplitPane.setDividerSize(3);
		resultSplitPane.setResizeWeight(1.0);

		bottomNorthPanel.add(graphTypePanel, BorderLayout.EAST);
		bottomPanel.add(bottomNorthPanel, BorderLayout.NORTH);

		this.setLayout(new BorderLayout());
		this.add(resultSplitPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.validate();
		this.repaint();
	}

	/**
	 * Connect GUI elements like, e.g., buttons with functionality to create
	 * interaction.
	 */
	private void registerGuiActionListener() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		try {
			currentVisualization.redundantInvisiblesOption = redundantInvisiblesOption
					.getValue();
			currentVisualization.alternativeDuplicatesOption = alternativeDuplicatesOption
					.getValue();

			// update the visualization
			grappaPanel = structuralResult
					.getVisualization(currentVisualization);
			grappaPanel.addGrappaListener(new DiagnosticGrappaAdapter());
			mapping = new HashMap();
			buildGraphMapping(mapping, grappaPanel.getSubgraph());

			modelContainer = new JScrollPane(grappaPanel);
			graphPanel.removeAll();
			graphPanel.add(modelContainer, BorderLayout.CENTER);
			graphPanel.validate();
			graphPanel.repaint();

			// update the provided object (diagnostic visualization might have
			// changed)
			analyzedPetriNet = (DiagnosticPetriNet) structuralResult.analyzedPetriNet;
		} catch (Exception ex) {
			JOptionPane
					.showMessageDialog(this,
							"An internal error occured while\nupdating the visualization.");
			Message.add("Probably not found.\n" + ex.toString(), 2);
			ex.printStackTrace();
		}
	}

	// ///////// GRAPPA RELATED METHODS //////////

	/**
	 * Create a mapping from the Petri net graph structure back to the Grappa
	 * nodes. Copied from the template GUI but not used so far.
	 * 
	 * @param mapping
	 * @param subGraph
	 */
	private void buildGraphMapping(Map mapping, Subgraph subGraph) {
		Enumeration e = subGraph.nodeElements();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			mapping.put(n.object, n);
		}
		e = subGraph.edgeElements();
		while (e.hasMoreElements()) {
			Edge n = (Edge) e.nextElement();
			mapping.put(n.object, n);
		}
		e = subGraph.subgraphElements();
		while (e.hasMoreElements()) {
			Subgraph n = (Subgraph) e.nextElement();
			buildGraphMapping(mapping, n);
		}
	}

	// //////// INTERFACE IMPLEMENTATION RELATED METHODS //////////

	/**
	 * Specifiy provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return An Array containing a provided object called
	 *         "Diagnostic Petri net"; which contains a Petri net model
	 *         exporting the current diagnostic visualization.
	 */
	public ProvidedObject[] getProvidedObjects() {
		try {

			if (reducedPetriNet != null) {
				ProvidedObject[] objects = {
						new ProvidedObject("Diagnostic visualization",
								new Object[] { new DotFileWriter() {
									public void writeToDot(Writer bw)
											throws IOException {
										analyzedPetriNet.writeToDot(bw);
									}
								} }),
						new ProvidedObject("Reduced Petri net",
								new Object[] { reducedPetriNet }) };
				return objects;
			} else {
				ProvidedObject[] objects = { new ProvidedObject(
						"Diagnostic visualization",
						new Object[] { new DotFileWriter() {
							public void writeToDot(Writer bw)
									throws IOException {
								analyzedPetriNet.writeToDot(bw);
							}
						} }) };
				return objects;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ///////// INTERNAL CLASS DEFINITIONS //////////

	/**
	 * A custom listener class for the grappa graph panel.
	 */
	public class DiagnosticGrappaAdapter extends GrappaAdapter {

		/**
		 * Create custom diagnostic tool tips for the grappa elements in the
		 * graph panel.
		 * 
		 * @param subg
		 *            Not used.
		 * @param elem
		 *            The element currently being moved over.
		 * @param pt
		 *            Not used.
		 * @param modifiers
		 *            Not used.
		 * @param panel
		 *            Not used.
		 * @return The string to be displayed.
		 */
		public String grappaTip(Subgraph subg, Element elem, GrappaPoint pt,
				int modifiers, GrappaPanel panel) {
			if (elem == null) {
				return "";
			}
			if (elem.object == null) {
				return "";
			}
			// if (elem.object instanceof DiagnosticPlace &&
			// (replayResult.performLogReplay() == true)) {
			// DiagnosticPlace p = (DiagnosticPlace) elem.object;
			// return p.getPerformanceToolTip(getSelectedInstanceIDs());
			// }
			// for anything else
			return "";
		}
	}
}
