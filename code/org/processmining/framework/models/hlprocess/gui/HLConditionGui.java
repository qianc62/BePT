/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/
package org.processmining.framework.models.hlprocess.gui;

import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLTypes.ChoiceEnum;
import org.processmining.framework.util.GUIPropertyDouble;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.util.GUIPropertyStringArea;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Gui class for pre-conditions concerning one high level activity. <br>
 * Allows to view and edit the condition characteristics through a graphical
 * user interface.
 * 
 * @see HLChoiceGui
 * @see HLProcessGui
 */
public class HLConditionGui {

	/** The associated condition object */
	protected HLCondition condition;
	/** The target activity for this condition */
	protected HLActivity conditionTarget;
	/** Gui elements */
	protected HashMap<ChoiceEnum, GuiDisplayable> conditionViews;

	/**
	 * Constructor.
	 * 
	 * @param the
	 *            associated high level condition
	 */
	public HLConditionGui(HLCondition aCondition, HLActivity target) {
		condition = aCondition;
		conditionTarget = target;
		conditionViews = new HashMap<ChoiceEnum, GuiDisplayable>();
		conditionViews.put(ChoiceEnum.DATA, new HLDataConditionGui());
		conditionViews.put(ChoiceEnum.PROB, new HLProbabilityConditionGui());
		conditionViews.put(ChoiceEnum.FREQ, new HLFrequencyConditionGui());
	}

	/**
	 * Retrieves the condition view according to the specified choice enum.
	 * 
	 * @param view
	 *            the view that is requested for this condition
	 * @return the specified view of this condition
	 */
	public GuiDisplayable getConditionView(ChoiceEnum view) {
		return conditionViews.get(view);
	}

	/**
	 * Gui class for displaying the data conditions.
	 * 
	 * @author Anne Rozinat
	 */
	public class HLDataConditionGui implements GuiNotificationTarget,
			GuiDisplayable {

		// Gui attributes
		protected GUIPropertyStringArea expression;

		/**
		 * Constructor initializes view based on high level information.
		 */
		public HLDataConditionGui() {
			expression = new GUIPropertyStringArea(
					"Data expression (precondition to enable branch)",
					"Fill in a CPN compliant expression over existing data attributes",
					condition.getExpression().toString(), this, 250);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
		 */
		public JPanel getPanel() {
			JPanel resultPanel = new JPanel();
			resultPanel.setLayout(new BoxLayout(resultPanel,
					BoxLayout.PAGE_AXIS));
			resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
			// add label indicating which alternative branch is meant
			JPanel dependencyPanel = new JPanel();
			dependencyPanel.setLayout(new BoxLayout(dependencyPanel,
					BoxLayout.LINE_AXIS));
			dependencyPanel.add(new JLabel("Alternative branch:  "
					+ conditionTarget.getName()));
			dependencyPanel.add(Box.createHorizontalGlue());
			resultPanel.add(dependencyPanel);
			resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			// add the expression property (modifiable)
			resultPanel.add(expression.getPropertyPanel());
			expression.disable();
			return resultPanel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.framework.util.GuiNotificationTarget#updateGUI()
		 */
		public void updateGUI() {
			// update data expression
			// TODO: editable GUI that can be propagated back to the actual data
			// expression
			// in the high level process (currently changing the string in the
			// text area does
			// not have any effect on the data dependency - so it is not
			// editable)
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return condition.getTarget().getName();
		}
	}

	/**
	 * Gui class for displaying the probability conditions.
	 * 
	 * @author Anne Rozinat
	 */
	public class HLProbabilityConditionGui implements GuiNotificationTarget,
			GuiDisplayable {

		// Gui attributes
		private GUIPropertyDouble probability;

		/**
		 * Constructor initializes view based on high level information.
		 */
		public HLProbabilityConditionGui() {
			probability = new GUIPropertyDouble(
					"Probability",
					"Fill in the probability for chosing this alternative branch",
					condition.getProbability(), 0.0, 1.0, 0.01, this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
		 */
		public JPanel getPanel() {
			JPanel resultPanel = new JPanel();
			resultPanel.setLayout(new BoxLayout(resultPanel,
					BoxLayout.PAGE_AXIS));
			resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
			// add label indicating which alternative branch is meant
			JPanel dependencyPanel = new JPanel();
			dependencyPanel.setLayout(new BoxLayout(dependencyPanel,
					BoxLayout.LINE_AXIS));
			dependencyPanel.add(new JLabel("Alternative branch:  "
					+ conditionTarget.getName()));
			dependencyPanel.add(Box.createHorizontalGlue());
			resultPanel.add(dependencyPanel);
			resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			// add the expression property (modifyable)
			resultPanel.add(probability.getPropertyPanel());
			return resultPanel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.framework.util.GuiNotificationTarget#updateGUI()
		 */
		public void updateGUI() {
			// update probability
			condition.setProbability(probability.getValue());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return condition.getTarget().getName();
		}
	}

	/**
	 * Gui class for displaying the frequency conditions.
	 * 
	 * @author Anne Rozinat
	 */
	public class HLFrequencyConditionGui implements GuiNotificationTarget,
			GuiDisplayable {

		// Gui attributes
		protected GUIPropertyInteger frequencyProperty;

		/**
		 * Constructor initializes view based on high level information.
		 */
		public HLFrequencyConditionGui() {
			frequencyProperty = new GUIPropertyInteger(
					"Frequency",
					"Indicates the relative frequency of chosing this alternative path",
					condition.getFrequency(), 1, 1000, this, 200, true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
		 */
		public JPanel getPanel() {
			JPanel resultPanel = new JPanel();
			resultPanel.setLayout(new BoxLayout(resultPanel,
					BoxLayout.PAGE_AXIS));
			resultPanel.add(Box.createRigidArea(new Dimension(0, 15)));
			// add label indicating which alternative branch is meant
			JPanel dependencyPanel = new JPanel();
			dependencyPanel.setLayout(new BoxLayout(dependencyPanel,
					BoxLayout.LINE_AXIS));
			dependencyPanel
					.add(new JLabel("Alternative branch:  " + toString()));
			dependencyPanel.add(Box.createHorizontalGlue());
			resultPanel.add(dependencyPanel);
			resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			// add the frequency property (modifyable)
			resultPanel.add(frequencyProperty.getPropertyPanel());
			return resultPanel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.framework.util.GuiNotificationTarget#updateGUI()
		 */
		public void updateGUI() {
			condition.setFrequency(frequencyProperty.getValue());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return condition.getTarget().getName();
		}
	}
}
