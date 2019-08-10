package org.processmining.analysis.redesign;

/**
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import org.processmining.framework.models.hlprocess.hlmodel.*;
import org.processmining.framework.models.hlprocess.pattern.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.processmining.framework.models.petrinet.PNNode;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

/**
 * Defines the possible redesigns that can be made. <br>
 * There are eight types of redesigns: parallel, sequential, group, add task,
 * remove task, add constraint, remove constraint, copy model.
 */

public class RedesignFactory {
	public static final int PARALLEL_INDEX = 0;
	public static final int SEQUENCE_INDEX = 1;
	public static final int GROUP_INDEX = 2;
	public static final int ADDTASK_INDEX = 3;
	public static final int REMOVETASK_INDEX = 4;
	public static final int ADDCONSTRAINT_INDEX = 5;
	public static final int REMOVECONSTRAINT_INDEX = 6;
	public static final int COPYMODEL_INDEX = 7;

	public RedesignFactory() {
	}

	public static String[] getAllRedesignTypes() {
		return new String[] { "Parallel Redesign", "Sequence Redesign",
				"Group redesign", "Add Task", "Remove Task", "Add Constraint",
				"Remove Constraint", "Copy model" };
	}

	public static Set<Component> getProperComponents(HLPetriNet model,
			Set<Component> components, Set<PNNode> nodes, Set<PNNode> succNodes) {
		Set<PNNode> succSuccs = new HashSet<PNNode>();
		succSuccs.addAll(getSuccessors(succNodes));
		nodes.addAll(succSuccs);
		Component comp = new Component(model, nodes);
		if (comp.isProperComponent()) {
			components.add(comp);
		}
		if (hasSuccessors(succSuccs)) {
			getProperComponents(model, components, nodes, succSuccs);
		}
		return components;
	}

	public static Set<Component> getAcyclicMGComponents(HLPetriNet model,
			Set<Component> components, Set<PNNode> nodes, Set<PNNode> succNodes) {
		Set<PNNode> succSuccs = new HashSet<PNNode>();
		succSuccs.addAll(getSuccessors(succNodes));
		nodes.addAll(succSuccs);
		Component comp = new Component(model, nodes);
		if (comp.isAcyclicMGComponent()) {
			components.add(comp);
		}
		if (hasSuccessors(succSuccs)) {
			getAcyclicMGComponents(model, components, nodes, succSuccs);
		}
		return components;
	}

	public static boolean hasSuccessors(Set<PNNode> nodes) {
		if (!nodes.isEmpty()) {
			for (PNNode node : nodes) {
				if (node.getSuccessors().size() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	public static Set<PNNode> getSuccessors(Set<PNNode> nodes) {
		Set<PNNode> succNodes = new HashSet<PNNode>();
		for (PNNode node : nodes) {
			succNodes.addAll(node.getSuccessors());
		}
		return succNodes;
	}

	/**
	 * Returns a set including all components that can be redesigned with the
	 * selected redesign type.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @return components Set<Component> set of components.
	 */
	public static Set<Component> getAllComponents(int indexRedesignType,
			HLPetriNet model) throws Exception {
		Set<Component> components = new HashSet<Component>();
		PetriNet net = model.getPNModel();
		Set<PNNode> nodes = new HashSet<PNNode>();
		switch (indexRedesignType) {
		case PARALLEL_INDEX:
			for (PNNode node : net.getNodes()) {
				nodes.add(node);
				if (hasSuccessors(nodes)) {
					Set<PNNode> succNodes = node.getSuccessors();
					nodes.addAll(succNodes);
					Component comp = new Component(model, nodes);
					if (comp != null && comp.isProperComponent()) {
						components.add(comp);
					}
					if (hasSuccessors(succNodes)) {
						getProperComponents(model, components, nodes, succNodes);
					}
				}
				/**
				 * clean up for next node
				 */
				nodes.clear();
			}
			break;
		case SEQUENCE_INDEX:
			for (PNNode node : net.getNodes()) {
				nodes.add(node);
				if (hasSuccessors(nodes)) {
					Set<PNNode> succNodes = node.getSuccessors();
					nodes.addAll(succNodes);
					Component comp = new Component(model, nodes);
					if (comp != null && comp.isAcyclicMGComponent()) {
						components.add(comp);
					}
					if (hasSuccessors(succNodes)) {
						getAcyclicMGComponents(model, components, nodes,
								succNodes);
					}
				}
				/**
				 * clean up for next node
				 */
				nodes.clear();
			}
			break;
		case GROUP_INDEX:

			break;
		case ADDTASK_INDEX:

			break;
		case REMOVETASK_INDEX:

			break;
		case ADDCONSTRAINT_INDEX:

			break;
		case REMOVECONSTRAINT_INDEX:

			break;
		case COPYMODEL_INDEX:
			Component comp = new Component(model, model.getPNModel().getNodes());
			components.add(comp);
			break;
		}
		return components;
	}

	/**
	 * Returns the component that has to be redesigned.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @param transitions
	 *            List<Transition> the transitions to redesign, can be all
	 *            transitions.
	 * @returns comp Component The component including the nodes to be
	 *          redesigned.
	 */
	public static Component getComponent(int indexRedesignType,
			HLPetriNet model, List<Transition> transitions) throws Exception {
		Component comp = null;
		/**
		 * all transitions in the model are selected for redesign. Find the
		 * maximal component in the model.
		 */
		if (transitions.size() == model.getPNModel().getTransitions().size()) {
			comp = getMaximalComponent(indexRedesignType, model, transitions);
		} else {
			/**
			 * some transitions in the model are selected for redesign. Find the
			 * minimal component that encloses these transitions.
			 */
			comp = getMinimalComponent(indexRedesignType, model, transitions);
		}
		return comp;
	}

	/**
	 * Returns the minimal component that includes all transitions.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @param transitions
	 *            List<Transition> the transitions to redesign.
	 * @returns comp Component The process part that should be redesigned.
	 *          Includes the transitions
	 */
	public static Component getMinimalComponent(int indexRedesignType,
			HLPetriNet model, List<Transition> transitions) throws Exception {
		Component comp = null;
		Set<Component> components = getAllComponents(indexRedesignType, model);
		Set<Component> inclComps = new HashSet<Component>();
		switch (indexRedesignType) {
		case PARALLEL_INDEX:
			if (!components.isEmpty()) {
				for (Component c : components) {
					if (c.getTransitions().equals(transitions)) {
						/**
						 * the selected transitions form a component, return
						 * component
						 */
						comp = c;
						break;
					} else if (c.getTransitions().containsAll(transitions)) {
						/**
						 * collect the components that include all selected
						 * transitions
						 */
						inclComps.add(c);
					}
				}
				/**
				 * find the minimal component of the components that include all
				 * selected transitions
				 */
				if (inclComps.size() == 0) {
					Message
							.add(
									"There are no components that include all selected transitions.",
									Message.ERROR);
					JOptionPane
							.showMessageDialog(
									null,
									"It is not possible to apply "
											+ "the selected transformation rule on the current selection of transitions.");
				} else if (inclComps.size() == 1) {
					comp = (Component) inclComps.toArray()[0];
				} else {
					Component ic1 = (Component) inclComps.toArray()[0];
					inclComps.remove(ic1);
					for (Component ic2 : inclComps) {
						if (ic1.getNodes().size() > ic2.getNodes().size()) {
							ic1 = ic2;
						}
					}
					comp = ic1;
				}
			}
			break;
		case SEQUENCE_INDEX:
			if (!components.isEmpty()) {
				for (Component c : components) {
					if (c.getTransitions().equals(transitions)) {
						/**
						 * the selected transitions form a component, return
						 * component
						 */
						comp = c;
						break;
					} else if (c.getTransitions().containsAll(transitions)) {
						/**
						 * collect the components that include all selected
						 * transitions
						 */
						inclComps.add(c);
					}
				}
				/**
				 * find the minimal component of the components that include all
				 * selected transitions
				 */
				if (inclComps.size() == 0) {
					Message
							.add(
									"There are no components that include all selected transitions.",
									Message.ERROR);
					JOptionPane
							.showMessageDialog(
									null,
									"It is not possible to apply "
											+ "the selected transformation rule on the current selection of transitions.");
				} else if (inclComps.size() == 1) {
					comp = (Component) inclComps.toArray()[0];
				} else {
					Component ic1 = (Component) inclComps.toArray()[0];
					inclComps.remove(ic1);
					for (Component ic2 : inclComps) {
						if (ic1.getNodes().size() > ic2.getNodes().size()) {
							ic1 = ic2;
						}
					}
					comp = ic1;
				}
			}
			break;
		case GROUP_INDEX:

			break;
		case ADDTASK_INDEX:

			break;
		case REMOVETASK_INDEX:

			break;
		case ADDCONSTRAINT_INDEX:

			break;
		case REMOVECONSTRAINT_INDEX:

			break;
		case COPYMODEL_INDEX:
			comp = new Component(model, model.getPNModel().getNodes());
			break;
		}
		return comp;
	}

	/**
	 * Returns the maximal component that can be found in the set of
	 * transitions.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @param transitions
	 *            List<Transition> the transitions to redesign, can be all
	 *            transitions.
	 * @returns comp Component The process part that should be redesigned.
	 *          Includes part of the transitions
	 */
	public static Component getMaximalComponent(int indexRedesignType,
			HLPetriNet model, List<Transition> transitions) throws Exception {
		Set<Component> components = getAllComponents(indexRedesignType, model);
		Component comp = null;
		Set<Component> inclComps = new HashSet<Component>();
		/**
		 * copy the set of components
		 */
		if (!components.isEmpty()) {
			for (Component c : components) {
				inclComps.add(c);
			}
			/**
			 * find the maximal component
			 */
			if (inclComps.size() == 0) {
				Message
						.add(
								"There are no components for this transformation rule.",
								Message.ERROR);
				JOptionPane
						.showMessageDialog(
								null,
								"It is not possible to apply "
										+ "the selected transformation rule on this process.");
			} else if (inclComps.size() == 1) {
				comp = (Component) inclComps.toArray()[0];
			} else {
				Component ic1 = (Component) inclComps.toArray()[0];
				inclComps.remove(ic1);
				for (Component ic2 : inclComps) {
					if (ic1.getNodes().size() < ic2.getNodes().size()) {
						ic1 = ic2;
					}
				}
				comp = ic1;
			}
		}
		return comp;
	}

	/**
	 * Creates the specified redesign type object.
	 * 
	 * @param indexRedesignType
	 *            int Redesign type.
	 * @param model
	 *            HLPetriNet Model to be redesigned.
	 * @param comp
	 *            Component The component including the transitions to be
	 *            redesigned.
	 * @return alt HLPetriNet Result of a redesign with the selected redesign
	 *         type. Returns alt = null if the redesign fails, this should be
	 *         handled where this method is called.
	 */
	public static HLPetriNet getRedesign(int indexRedesignType,
			HLPetriNet model, Component comp) throws Exception {
		HLPetriNet alt = null;
		/**
		 * perform the redesign for the selected redesign type with the created
		 * component. If there is no component, HLPetriNet alt is null, this
		 * should be handled where this method is used.
		 */
		if (comp != null) {
			Transformation trans = new Transformation(model, comp);
			switch (indexRedesignType) {
			case PARALLEL_INDEX:
				if (comp.isProperComponent()) {
					alt = trans.createParallelRedesign();
				} else {
					Message
							.add(
									"The selected transitions do not form a proper component",
									Message.ERROR);
					JOptionPane
							.showMessageDialog(
									null,
									"It is not possible to apply "
											+ "the selected transformation rule on the current selection of transitions.");
					/**
					 * note that alt remains null, is handled in
					 * RedesignAnalysisUI.
					 */
				}
				break;
			case SEQUENCE_INDEX:
				if (comp.isAcyclicMGComponent()) {
					alt = trans.createSequentialRedesign();
				} else {
					Message
							.add(
									"The selected transitions do not form an acyclic MG component",
									Message.ERROR);
					JOptionPane
							.showMessageDialog(
									null,
									"It is not possible to apply "
											+ "the selected transformation rule on the current selection of transitions.");
					// note that alt remains null, is handled in
					// RedesignAnalysisUI.
				}
				break;
			case GROUP_INDEX:
				break;
			case ADDTASK_INDEX:
				// give warning if input data is not available, provide
				// possibility to cancel
				// It is possible to continue, because yet another task may be
				// added to create the input
				if (!comp.isComponent()) {
					Message.add(
							"Please be aware that not all input data elements "
									+ "for the added node are available",
							Message.WARNING);
					// provide user with a message asking (s)he is sure
					final JOptionPane optionPane = new JOptionPane(
							"Please be aware that not all "
									+ "input data elements for the added node are available",
							JOptionPane.WARNING_MESSAGE,
							JOptionPane.OK_CANCEL_OPTION);
					final JDialog dialog = new JDialog(MainUI.getFrames()[0],
							"Warning", true);
					dialog.setContentPane(optionPane);
					optionPane
							.addPropertyChangeListener(new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent e) {
									String prop = e.getPropertyName();
									if (dialog.isVisible()
											&& (e.getSource() == optionPane)
											&& (prop
													.equals(JOptionPane.VALUE_PROPERTY))) {
										dialog.setVisible(false);
									}
								}
							});
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);

					int value = ((Integer) optionPane.getValue()).intValue();
					if (value == JOptionPane.OK_OPTION) {
						// TO DO: add task redesign
					}
				}
				break;
			case REMOVETASK_INDEX:
				// give warning if necessary data will be removed, provide
				// possibility to cancel
				// It is possible to continue, because these data may be added
				// to other tasks
				if (!comp.isComponent()) {
					Message
							.add(
									"Please be aware that data elements necessary for the "
											+ "execution of other tasks will be removed",
									Message.WARNING);
					// provide user with a message asking (s)he is sure
					final JOptionPane optionPane = new JOptionPane(
							"Please be aware that data elements necessary for the "
									+ "execution of other tasks will be removed",
							JOptionPane.WARNING_MESSAGE,
							JOptionPane.OK_CANCEL_OPTION);
					final JDialog dialog = new JDialog(MainUI.getFrames()[0],
							"Warning", true);
					dialog.setContentPane(optionPane);
					optionPane
							.addPropertyChangeListener(new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent e) {
									String prop = e.getPropertyName();
									if (dialog.isVisible()
											&& (e.getSource() == optionPane)
											&& (prop
													.equals(JOptionPane.VALUE_PROPERTY))) {
										dialog.setVisible(false);
									}
								}
							});
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);

					int value = ((Integer) optionPane.getValue()).intValue();
					if (value == JOptionPane.OK_OPTION) {
						// TO DO: remove task redesign
					}
				}
				break;
			case ADDCONSTRAINT_INDEX:
				if (comp.netIncludesNodes() && comp.isTwoTransitions()) {
					/**
					 * Two transitions are selected in the model, so the
					 * constraint can be added. Decide which transition is the
					 * first, the constraint is added from the first to the
					 * second transition.
					 */
					String t1 = comp.getNodeList().get(0).getIdentifier();
					String t2 = comp.getNodeList().get(1).getIdentifier();
					final JOptionPane optionPane = new JOptionPane(
							"You have selected the transitions "
									+ t1
									+ " and "
									+ t2
									+ ". Would you like transition "
									+ t1
									+ " to be the start point of the constraint?",
							JOptionPane.QUESTION_MESSAGE,
							JOptionPane.YES_NO_OPTION);
					final JDialog dialog = new JDialog(MainUI.getFrames()[0],
							"Question", true);
					dialog.setContentPane(optionPane);
					optionPane
							.addPropertyChangeListener(new PropertyChangeListener() {
								public void propertyChange(PropertyChangeEvent e) {
									String prop = e.getPropertyName();
									if (dialog.isVisible()
											&& (e.getSource() == optionPane)
											&& (prop
													.equals(JOptionPane.VALUE_PROPERTY))) {
										dialog.setVisible(false);
									}
								}
							});
					dialog.pack();
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);

					int value = ((Integer) optionPane.getValue()).intValue();
					if (value == JOptionPane.OK_OPTION) {
						alt = trans.addConstraint(t1, t2, t1 + "Before" + t2);
					} else {
						alt = trans.addConstraint(t2, t1, t2 + "Before" + t1);
					}
				} else {
					Message.add(
							"Exactly two transitions have to be selected for the "
									+ "execution of add constraint",
							Message.ERROR);
					JOptionPane.showMessageDialog(null,
							"Please select two transitions.");
					// note that alt remains null, is handled in
					// RedesignAnalysisUI.
				}
				break;
			case REMOVECONSTRAINT_INDEX:
				break;
			case COPYMODEL_INDEX:
				if (comp.netEqualsNodes()) {
					/**
					 * the whole model is selected, so the model can be copied.
					 */
					alt = trans.createCopy();
				} else {
					Message.add(
							"The complete model has to be selected for the "
									+ "execution of copy model", Message.ERROR);
					JOptionPane.showMessageDialog(null,
							"Please select the complete "
									+ "model before copying");
					// note that alt remains null, is handled in
					// RedesignAnalysisUI.
				}
				break;
			}
		} else {
			JOptionPane.showMessageDialog(null, "There is no component "
					+ "available for redesign");
		}
		return alt;
	}
}
