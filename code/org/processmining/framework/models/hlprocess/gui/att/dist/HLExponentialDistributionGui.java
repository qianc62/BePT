package org.processmining.framework.models.hlprocess.gui.att.dist;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLExponentialDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Represents an exponential distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by one spinner representing the intensity value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLExponentialDistributionGui extends HLDistributionGui {

	protected GUIPropertyListEnumeration myParameters;
	protected GUIPropertyDoubleTextField myMean;
	protected GUIPropertyDoubleTextField myIntensity;
	protected JPanel resultPanel = new JPanel();

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLExponentialDistributionGui(HLExponentialDistribution theParent) {
		super(theParent);
		myMean = new GUIPropertyDoubleTextField("Mean value",
				((HLExponentialDistribution) parent).getMean(),
				new GUIPropertyDoubleMeanListener());
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLExponentialDistribution) parent).getIntensity(),
				new GUIPropertyDoubleIntensityListener());
		initialize();
	}

	/**
	 * Constructor providing a view onto the given meta distribution.
	 * <p>
	 * Changes will be propagated directly back to the HLGeneralDistribution
	 * object.
	 * 
	 * @param theParent
	 *            the meta distribution underlying this view
	 */
	public HLExponentialDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myMean = new GUIPropertyDoubleTextField("Mean value",
				((HLGeneralDistribution) parent).getMean(),
				new GUIPropertyDoubleMeanListener());
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLGeneralDistribution) parent).getIntensity(),
				new GUIPropertyDoubleIntensityListener());
		initialize();
	}

	/**
	 * Initializes remaining GUI properties.
	 */
	protected void initialize() {
		ArrayList<String> parametersList = new ArrayList<String>();
		parametersList.add("Mean");
		parametersList.add("Intensity");
		myParameters = new GUIPropertyListEnumeration("Properties",
				"Select one of the distributions available", parametersList,
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui
	 * #updateGUI()
	 */
	public void updateGUI() {
		resultPanel.removeAll();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(myParameters.getPropertyPanel());
		if (myParameters.getValue().toString().equals("Mean"))
			resultPanel.add(myMean.getPropertyPanel());
		else {
			resultPanel.add(myIntensity.getPropertyPanel());
		}
		resultPanel.validate();
		resultPanel.repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui
	 * #getPanel()
	 */
	public JPanel getPanel() {
		resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(myParameters.getPropertyPanel());
		if (myParameters.getValue().toString().equals("Mean")) {
			resultPanel.add(myMean.getPropertyPanel());
		} else {
			resultPanel.add(myIntensity.getPropertyPanel());
		}
		return resultPanel;
	}

	/**
	 * Listener to changes of the "Mean" property.
	 * <p>
	 * Changes affect the "Intensity" property value.
	 */
	class GUIPropertyDoubleMeanListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (myMean.getValue() == 0) {
				myIntensity.setValue(Double.MAX_VALUE);
				myMean.setValue(1 / myIntensity.getValue());
			} else {
				myIntensity.setValue(1 / myMean.getValue());
			}
			if (parent instanceof HLExponentialDistribution) {
				((HLExponentialDistribution) parent).setMean(myMean.getValue());
			} else if (parent instanceof HLGeneralDistribution) {
				((HLGeneralDistribution) parent).setMean(myMean.getValue());
			}
		}
	}

	/**
	 * Listener to changes of the "Intensity" property.
	 * <p>
	 * Changes affect the "Mean" property value.
	 */
	class GUIPropertyDoubleIntensityListener implements GuiNotificationTarget {
		public void updateGUI() {
			if (myIntensity.getValue() == 0) {
				myMean.setValue(Double.MAX_VALUE);
				myIntensity.setValue(1 / myMean.getValue());
			} else {
				myMean.setValue(1 / myIntensity.getValue());
			}
			if (parent instanceof HLExponentialDistribution) {
				((HLExponentialDistribution) parent).setIntensity(myIntensity
						.getValue());
			} else if (parent instanceof HLGeneralDistribution) {
				((HLGeneralDistribution) parent).setIntensity(myIntensity
						.getValue());
			}
		}
	}

}
