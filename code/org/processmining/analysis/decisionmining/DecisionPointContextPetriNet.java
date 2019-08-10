/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.util.GuiPropertyStringTextarea;

import weka.core.Attribute;
import weka.core.FastVector;
import att.grappa.GrappaPanel;

/**
 * A decision point context for Petri nets offers the attributes according
 * different selection scopes within a Petri net mode.
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionPointContextPetriNet implements DecisionPointContext {

	/** the belonging decision point */
	private DecisionPoint myDecisionPoint;
	/** the belonging petri net */
	private PetriNet myPetriNetModel;
	/** the belonging place */
	private Place myPlace;
	/** The visualization class for Petri net decision points. */
	private DecisionPointVisualization myVizModel;
	/** the whole panel to be displayed in the attributes tab */
	private JPanel myAttributesViewPanel;
	/** the actual content is put here (vertical flow) */
	private JPanel attributesContentPanel;
	/** the whole panel to be displayed in the model tab */
	private JPanel myModelViewPanel;
	/** the whole panel to be displayed in the result tab */
	private JPanel myResultViewPanel;
	/** the whole panel to be displayed in the evaluation tab */
	private JPanel myEvaluationViewPanel;
	/** the sub panel containing the graphical visualization of the model */
	private JPanel myModelGraphPanel = new JPanel(new BorderLayout());
	/** the sub panel containing the attributes to be selected or deselected */
	private JPanel mySelectionPanel = new JPanel();
	/** the combo box choosing the selection scope of the decision point */
	private JComboBox attributeScopeComboBox;
	/** contains the selection scope chosen at last - default is ALL_BEFORE */
	private AttributeSelectionScope myAttributeSelectionScope = AttributeSelectionScope.ALL_BEFORE;

	/**
	 * The set of relevant DecisionAttribute objects (according to the chosen
	 * selection scope).
	 */
	private HashSet myRelevantAttribues;

	/**
	 * Default constructor.
	 * 
	 * @param decisionPoint
	 *            the belonging decision point
	 */
	public DecisionPointContextPetriNet(PetriNet model, Place place,
			DecisionPoint decisionPoint) {
		myDecisionPoint = decisionPoint;
		myPetriNetModel = model;
		myPlace = place;

		// todo: move to init() procedure
		myVizModel = new DecisionPointVisualization(myPetriNetModel);
		myVizModel.toVisualize = myPlace;
	}

	// ///////////////// Interface implementing methods //////////////////

	/**
	 * {@inheritDoc}
	 */
	public JPanel getAttributesViewPanel() {
		// TODO: currently not the best solution - it must be rebuilt every time
		// since the panel created by
		// the decision attribute cannot be used by multiple panels at the same
		// time
		// if (myAttributesViewPanel == null) {
		buildAttributesPanel();
		// }
		return myAttributesViewPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * getModelViewPanel()
	 */
	public JPanel getModelViewPanel() {
		if (myModelViewPanel == null) {
			buildModelPanel();
		}
		return myModelViewPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * getResultViewPanel()
	 */
	public JPanel getResultViewPanel() {
		if (myResultViewPanel == null) {
			buildResultPanel();
		}
		return myResultViewPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * setResultViewPanel(javax.swing.JPanel)
	 */
	public void setResultViewPanel(JPanel newResult) {
		myResultViewPanel = newResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * getEvaluationViewPanel()
	 */
	public JPanel getEvaluationViewPanel() {
		if (myEvaluationViewPanel == null) {
			buildlEvaluationPanel();
		}
		return myEvaluationViewPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * setEvaluationViewPanel(javax.swing.JPanel)
	 */
	public void setEvaluationViewPanel(JPanel newResult) {
		myEvaluationViewPanel = newResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * getAttributeInfo()
	 */
	public FastVector getAttributeInfo() {
		// create attribute information for this decision point
		FastVector attributeInfo = new FastVector();
		Iterator attributesIterator = myRelevantAttribues.iterator();
		while (attributesIterator.hasNext()) {
			DecisionAttribute currentAtt = (DecisionAttribute) attributesIterator
					.next();
			// only include if not deselected by the user
			if (currentAtt.isIncluded()) {
				Attribute wekaAtt = currentAtt.getWekaAttribute();
				attributeInfo.addElement(wekaAtt);
			}
		}

		// add target concept
		Attribute targetConcept = myDecisionPoint
				.getTargetConceptAsWekaAttribute();
		attributeInfo.addElement(targetConcept);

		attributeInfo.trimToSize();
		return attributeInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.decisionmining.DecisionPointContext#
	 * getAttributeSelectionScope()
	 */
	public AttributeSelectionScope getAttributeSelectionScope() {
		return myAttributeSelectionScope;
	}

	// ///////////////// Private methods //////////////////

	/**
	 * {@inheritDoc}
	 */
	private GrappaPanel getDecisionPointVisualization() {
		GrappaPanel myResultVisualization = myVizModel.getGrappaVisualization();
		return myResultVisualization;
	}

	/**
	 * Initially builds the the attributes view for this decision point.
	 */
	private void buildAttributesPanel() {

		myAttributesViewPanel = new JPanel();
		myAttributesViewPanel.setLayout(new BoxLayout(myAttributesViewPanel,
				BoxLayout.X_AXIS));

		attributesContentPanel = new JPanel();
		attributesContentPanel.setLayout(new BoxLayout(attributesContentPanel,
				BoxLayout.Y_AXIS));

		JPanel selScopeManualPanel = new JPanel();
		selScopeManualPanel.setLayout(new BoxLayout(selScopeManualPanel,
				BoxLayout.X_AXIS));
		String scopeDescription = new String(
				"Please choose the desired attribute selection scope. The selection scope determines which attributes are used for analysis: \n"
						+ "(a) just before - only attributes written by tasks directly preceding the decision point\n"
						+ "(b) all before - the attributes written by all tasks preceding the decision point\n"
						+ "(c)whole case - all attributes (independent of the currently selected decision point and including attributes of the process instance)");
		GuiPropertyStringTextarea selectionScopeManual = new GuiPropertyStringTextarea(
				scopeDescription);
		selScopeManualPanel.add(selectionScopeManual.getPropertyPanel());
		selScopeManualPanel.add(Box.createHorizontalGlue());

		JPanel scopePanel = new JPanel();
		scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.X_AXIS));
		JLabel selectionScopeLabel = new JLabel("Attribute selection scope: ");
		selectionScopeLabel.setForeground(new Color(100, 100, 100));

		// initialize combo box based on current attribute selection scope
		attributeScopeComboBox = new JComboBox();
		Iterator<AttributeSelectionScope> possibleScopes = AttributeSelectionScope
				.getValues(myAttributeSelectionScope).iterator();
		while (possibleScopes.hasNext()) {
			AttributeSelectionScope scope = possibleScopes.next();
			attributeScopeComboBox.addItem(scope);
		}
		attributeScopeComboBox.setMaximumSize(attributeScopeComboBox
				.getMinimumSize());

		attributeScopeComboBox.addActionListener(new ActionListener() {
			// specify action when the attribute selection scope is changed
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				AttributeSelectionScope newSelectionType = (AttributeSelectionScope) cb
						.getSelectedItem();
				// only redraw if selection scope has actually changed
				if (myAttributeSelectionScope != newSelectionType) {
					// use global attribute as the same update procedure is also
					// called in case a new
					// decision point has been selected by the user
					myAttributeSelectionScope = newSelectionType;
					updateOfferedAttributes(false);
				}
			}
		});

		scopePanel.add(selectionScopeLabel);
		scopePanel.add(Box.createRigidArea(new Dimension(5, 0)));
		scopePanel.add(attributeScopeComboBox);
		scopePanel.add(Box.createHorizontalGlue());

		JPanel attributesManualPanel = new JPanel();
		attributesManualPanel.setLayout(new BoxLayout(attributesManualPanel,
				BoxLayout.X_AXIS));
		String attDescription = new String(
				"The following attributes have been found within the selected scope. "
						+ "Please deselect those attributes that you do not want to include in the analysis and determine the proper type for each of the others. "
						+ "The attribute type can be set to: "
						+ "(a) nominal - if it corresponds to a discrete enumeration of values \n"
						+ "(b) numeric - if the values represent (continuous) numbers");
		GuiPropertyStringTextarea attributesManual = new GuiPropertyStringTextarea(
				attDescription);
		attributesManualPanel.add(attributesManual.getPropertyPanel());
		attributesManualPanel.add(Box.createHorizontalGlue());

		mySelectionPanel = new JPanel();
		mySelectionPanel.setLayout(new BoxLayout(mySelectionPanel,
				BoxLayout.Y_AXIS));
		// actually builds the attributes view
		updateOfferedAttributes(true);

		attributesContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		attributesContentPanel.add(Box.createVerticalGlue());
		attributesContentPanel.add(selScopeManualPanel);
		attributesContentPanel.add(scopePanel);
		attributesContentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
		attributesContentPanel.add(Box.createVerticalGlue());
		attributesContentPanel.add(attributesManualPanel);
		attributesContentPanel.add(mySelectionPanel);
		attributesContentPanel.add(Box.createVerticalGlue());

		myAttributesViewPanel.add(Box.createHorizontalGlue());
		myAttributesViewPanel.add(attributesContentPanel);
		myAttributesViewPanel.add(Box.createHorizontalGlue());
		myAttributesViewPanel.setBorder(BorderFactory.createEmptyBorder(15, 15,
				15, 15));
	}

	/**
	 * Retrieves all those tasks that are contained within the currently
	 * selected attribute selection scope.
	 * 
	 * @return the list of transitions within the scope
	 */
	private ArrayList getTasksWithinAttributeSelectionScope() {
		ArrayList resultList = new ArrayList();

		if (myAttributeSelectionScope == AttributeSelectionScope.WHOLE_CASE) {
			// current decision point does not matter if selection scope is
			// "whole log"
			resultList = myPetriNetModel.getTransitions();
		} else if (myAttributeSelectionScope == AttributeSelectionScope.JUST_BEFORE) {
			Iterator direclyPrecedingTransitions = myDecisionPoint.getParent()
					.getModelAnalyzer().getDirectPredecessors(myDecisionPoint)
					.iterator();
			while (direclyPrecedingTransitions.hasNext()) {
				Transition trans = (Transition) direclyPrecedingTransitions
						.next();
				resultList.add(trans);
			}
		} else if (myAttributeSelectionScope == AttributeSelectionScope.ALL_BEFORE) {
			Iterator allPrecedingTransitions = myDecisionPoint.getParent()
					.getModelAnalyzer().getAllPredecessors(myDecisionPoint)
					.iterator();
			while (allPrecedingTransitions.hasNext()) {
				Transition trans = (Transition) allPrecedingTransitions.next();
				resultList.add(trans);
			}
		}
		return resultList;
	}

	/**
	 * Updates the attributes offered for selection after the selection scope
	 * has been changed.
	 */
	private void updateOfferedAttributes(boolean initial) {
		// todo: solve without this boolean flag (seperate methods differently)
		if (initial == false) {
			mySelectionPanel.removeAll();
		}

		// clean the relevant attribute list and re-fill based on new selection
		// scope
		myRelevantAttribues = new HashSet();
		// consider all relevant tasks
		Iterator allRelevantTasks = getTasksWithinAttributeSelectionScope()
				.iterator();
		while (allRelevantTasks.hasNext()) {
			Transition trans = (Transition) allRelevantTasks.next();
			// retrieve all attributes recorded for current task
			Iterator<DecisionAttribute> attributesCurrentTrans = myDecisionPoint
					.getParent().getLogAnalyzer().getAttributesForLogEvent(
							trans.getLogEvent()).iterator();
			while (attributesCurrentTrans.hasNext()) {
				DecisionAttribute currentAtt = attributesCurrentTrans.next();
				myRelevantAttribues.add(currentAtt);

				// include this attribute in the GUI
				JPanel currentAttPanel = currentAtt.getAttributePanel();
				mySelectionPanel.add(currentAttPanel);
			}
		}

		// include also global attributes if selection scope is 'global'
		if (myAttributeSelectionScope == AttributeSelectionScope.WHOLE_CASE) {
			Iterator<DecisionAttribute> globalAtts = myDecisionPoint
					.getParent().getLogAnalyzer().getGlobalAttributes()
					.iterator();
			while (globalAtts.hasNext()) {
				DecisionAttribute globalAtt = globalAtts.next();
				myRelevantAttribues.add(globalAtt);

				// include this attribute in the GUI
				JPanel currentAttPanel = globalAtt.getAttributePanel();
				mySelectionPanel.add(currentAttPanel);
			}
		}

		// TODO : solve without this boolean flag (seperate methods differently)
		if (initial == false) {
			mySelectionPanel.revalidate();
			mySelectionPanel.repaint();
		}

		// update visualization only in the case the attribute selection scope
		// visualization option has been chosen
		if (myVizModel.attributeSelectionScope != null) {
			// collect the tasks belonging to the current attribute selection
			// scope
			ArrayList associatedTasks = getTasksWithinAttributeSelectionScope();
			// make the belonging tasks known to the visualization
			myVizModel.attributeSelectionScope = associatedTasks;

			myModelGraphPanel.removeAll();
			myModelGraphPanel.add(getDecisionPointVisualization());
			myModelGraphPanel.validate();
			myModelGraphPanel.repaint();
		}
	}

	/**
	 * Initially builds the the model view for this decision point.
	 */
	private void buildModelPanel() {
		myModelViewPanel = new JPanel(new BorderLayout());
		myModelGraphPanel.add(getDecisionPointVisualization());
		myModelViewPanel.add(myModelGraphPanel, BorderLayout.CENTER);
		JLabel visualizationHeading = new JLabel("Highlight decision class ");
		visualizationHeading.setForeground(new Color(100, 100, 100));

		// build decision class visualizing option
		Iterator decisionClassesIterator = myDecisionPoint.getTargetConcept()
				.iterator();
		String[] vizualizationModes = { "none" };
		JComboBox visualizationType = new JComboBox(vizualizationModes);

		while (decisionClassesIterator.hasNext()) {
			DecisionCategory currentClass = (DecisionCategory) decisionClassesIterator
					.next();
			visualizationType.addItem(currentClass);
		}

		visualizationType.setSelectedIndex(0);
		// invoke highlighting the associated model tasks
		visualizationType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				Object selectedObject = cb.getSelectedItem();
				// check selected object to be a decision class
				if (selectedObject.getClass() != DecisionCategory.class) {
					// "none" was chosen --> remove previous class
					// visualizations
					myVizModel.currentDecisionClass = null;
				} else {
					DecisionCategory classToVisualize = (DecisionCategory) selectedObject;
					// collect the tasks belonging to the log events
					// charecterizing the decision class
					ArrayList associatedTasks = new ArrayList();
					Iterator associatedLogEvents = classToVisualize
							.getAssociatedLogEvents().iterator();
					while (associatedLogEvents.hasNext()) {
						LogEvent currentLE = (LogEvent) associatedLogEvents
								.next();
						// since duplicate tasks are not used to characterize
						// decision classes
						// there is only one transition associated
						Transition trans = myVizModel
								.findRandomTransition(currentLE);
						associatedTasks.add(trans);
					}

					// make the belonging tasks known to the visualization
					myVizModel.currentDecisionClass = associatedTasks;
				}

				// update visualization
				myModelGraphPanel.removeAll();
				myModelGraphPanel.add(getDecisionPointVisualization());
				myModelGraphPanel.validate();
				myModelGraphPanel.repaint();
			}
		});

		JPanel graphTypePanel = new JPanel();
		graphTypePanel.setLayout(new BoxLayout(graphTypePanel,
				BoxLayout.LINE_AXIS));
		graphTypePanel.add(visualizationHeading);
		graphTypePanel.add(visualizationType);

		JCheckBox attSelectionScope = new JCheckBox(
				"Show attribute selection scope ");
		attSelectionScope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// check selected object to be a decision class
				if (myVizModel.attributeSelectionScope != null) {
					// was selected before --> now deselected
					myVizModel.attributeSelectionScope = null;
				} else {
					// collect the tasks belonging to the current attribute
					// selection scope
					ArrayList associatedTasks = getTasksWithinAttributeSelectionScope();
					// make the belonging tasks known to the visualization
					myVizModel.attributeSelectionScope = associatedTasks;
				}

				// update visualization
				myModelGraphPanel.removeAll();
				myModelGraphPanel.add(getDecisionPointVisualization());
				myModelGraphPanel.validate();
				myModelGraphPanel.repaint();
			}
		});
		graphTypePanel.add(Box.createRigidArea(new Dimension(30, 0)));
		graphTypePanel.add(attSelectionScope);
		graphTypePanel.setBorder(BorderFactory
				.createEmptyBorder(15, 15, 15, 15));

		JPanel myModelVisualizationOptionsPanel = new JPanel(new BorderLayout());
		myModelVisualizationOptionsPanel.add(graphTypePanel, BorderLayout.EAST);
		myModelViewPanel.add(myModelVisualizationOptionsPanel,
				BorderLayout.SOUTH);
	}

	/**
	 * Initially builds the the result view for this decision point.
	 */
	private void buildResultPanel() {
		myResultViewPanel = new JPanel(new BorderLayout());
		JLabel message = new JLabel(
				"     Please press the button 'Update results'.");
		message.setForeground(new Color(100, 100, 100));
		myResultViewPanel.add(message, BorderLayout.CENTER);
	}

	/**
	 * Initially builds the the result view for this decision point.
	 */
	private void buildlEvaluationPanel() {
		myEvaluationViewPanel = new JPanel(new BorderLayout());
		JLabel message = new JLabel(
				"     Please press the button 'Update results'.");
		message.setForeground(new Color(100, 100, 100));
		myEvaluationViewPanel.add(message, BorderLayout.CENTER);
	}
}
