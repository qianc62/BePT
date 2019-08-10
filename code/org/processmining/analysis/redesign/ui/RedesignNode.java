/*
 * Copyright (c) 2009 Christian W. Guenther (christian@deckfour.org)
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
package org.processmining.analysis.redesign.ui;

import java.awt.Color;

import org.deckfour.gantzgraf.model.GGNode;
import org.deckfour.gantzgraf.painter.GGNodePainter;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * @author Mariska Netjes (m.netjes@tue.nl)
 */
public class RedesignNode extends GGNode {

	/**
	 * Drawing the node
	 */
	// Reminder: Color(255,255,255) = white and Color(0,0,0) = black
	private static Color background = new Color(255, 255, 255);
	private static Color border = new Color(40, 50, 40);
	private static Color text = new Color(20, 20, 20);
	private static GGNodePainter painter = new RedesignNodePainter(background,
			border, text);
	/**
	 * The model associated with the node, the modelID, the lowerbound and the
	 * upperbound are used in the label on the node.
	 */
	private HLPetriNet model;
	private int modelID;
	private double lowerBound;
	private double mean;
	private double upperBound;

	/**
	 * Flag for indicating whether the model is selected for simulation.
	 */
	protected boolean isSelectedForSimulation = false;

	/**
	 * Flag for indicating whether the model has been simulated.
	 */
	protected boolean isSimulated = false;

	/**
	 * Flag for indicating whether the model performs better than the original
	 * model.
	 */
	protected boolean isBetterPerforming = false;

	/**
	 * Flag for indicating that this is the best performing model.
	 */
	protected boolean isBestThePerforming = false;

	/**
	 * Flag for indicating whether the model performs in the best class of
	 * models.
	 */
	protected boolean isBestPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the second best class.
	 */
	protected boolean isBestSecondPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the third best class.
	 */
	protected boolean isBestThirdPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the fourth best class,
	 * this is just above equal performance as the original model.
	 */
	protected boolean isBestFourthPerforming = false;

	/**
	 * Flag for indicating whether the model performs the same as the original
	 * model.
	 */
	protected boolean isEqualPerforming = false;

	/**
	 * Flag for indicating whether the model performs worse than the original
	 * model.
	 */
	protected boolean isWorsePerforming = false;

	/**
	 * Flag for indicating whether the model performs in the worst class of
	 * models.
	 */
	protected boolean isWorstPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the second worst class.
	 */
	protected boolean isWorstSecondPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the third worst class.
	 */
	protected boolean isWorstThirdPerforming = false;

	/**
	 * Flag for indicating whether the model performs in the fourth worst class,
	 * this is just below equal performance as the original model.
	 */
	protected boolean isWorstFourthPerforming = false;

	/**
	 * Initialization of the node
	 * 
	 * @param model
	 *            HLPetriNet the associated model
	 * @param label
	 *            String[] the label of the node
	 * @param id
	 *            int the id of the node
	 */
	public RedesignNode(HLPetriNet model, String[] label, int id) {
		super(label, painter);
		this.model = model;
		this.modelID = id;
	}

	/**
	 * Returns the model associated to the node
	 * 
	 * @return model HLPetriNet the associated model
	 */
	public HLPetriNet getModel() {
		return model;
	}

	/**
	 * Returns the id given to identify the model
	 * 
	 * @return modelID int the id of the model
	 */
	public int getModelID() {
		return modelID;
	}

	/**
	 * Probes, whether this node is currently selected for simulation.
	 */
	public boolean isSelectedForSimulation() {
		return isSelectedForSimulation;
	}

	/**
	 * Sets whether this node is currently selected for simulation.
	 * <p>
	 * Used by event listeners, not for end user consideration.
	 */
	public void setSelectedForSimulation(boolean sel) {
		this.isSelectedForSimulation = sel;
		updateView();
	}

	/**
	 * Probes, whether this node has been simulated.
	 */
	public boolean isSimulated() {
		return isSimulated;
	}

	/**
	 * Sets whether this node has been simulated.
	 */
	public void setSimulated(boolean sim) {
		this.isSimulated = sim;
		updateView();
	}

	/**
	 * Returns the lower bound of the 95% confidence interval of the selected
	 * performance indicator. Obtained through simulation.
	 * 
	 * @return lowerBound double the lower bound
	 */
	public double getLowerBound() {
		return lowerBound;
	}

	/**
	 * Returns the mean of the selected performance indicator. Obtained through
	 * simulation.
	 * 
	 * @return mean double the mean
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * Returns the upper bound of the 95% confidence interval of the selected
	 * performance indicator. Obtained through simulation.
	 * 
	 * @return upperBound double the upper bound
	 */
	public double getUpperBound() {
		return upperBound;
	}

	/**
	 * Sets the 95% confidence interval including mean of the selected
	 * performance indicator.
	 * <p>
	 * Used by event listeners, not for end user consideration.
	 */
	public void setPerformance(double lowerBound, double mean, double upperBound) {
		this.lowerBound = lowerBound;
		this.mean = mean;
		this.upperBound = upperBound;
	}

	/**
	 * Probes, whether this node is better performing.
	 */
	public boolean isBetterPerforming() {
		return isBetterPerforming;
	}

	/**
	 * Sets whether this node is better performing.
	 */
	public void setBetterPerforming(boolean better) {
		this.isBetterPerforming = better;
		updateView();
	}

	// We divide the better performing nodes in four classes:
	// best, second best, third best, fourth best = just above equal
	/**
	 * Probes, whether this node is the best performing model.
	 */
	public boolean isBestThePerforming() {
		return isBestThePerforming;
	}

	/**
	 * Sets whether this node is the best performing model.
	 */
	public void setBestThePerforming(boolean better) {
		this.isBestThePerforming = better;
		updateView();
	}

	/**
	 * Probes, whether this node is in the best performing class.
	 */
	public boolean isBestPerforming() {
		return isBestPerforming;
	}

	/**
	 * Sets whether this node is in the best performing class.
	 */
	public void setBestPerforming(boolean better) {
		this.isBestPerforming = better;
		updateView();
	}

	/**
	 * Probes, whether this node is in the second best performing class.
	 */
	public boolean isBestSecondPerforming() {
		return isBestSecondPerforming;
	}

	/**
	 * Sets whether this node is in the second best performing class.
	 */
	public void setBestSecondPerforming(boolean better) {
		this.isBestSecondPerforming = better;
		updateView();
	}

	/**
	 * Probes, whether this node is the third in the best performing class.
	 */
	public boolean isBestThirdPerforming() {
		return isBestThirdPerforming;
	}

	/**
	 * Sets whether this node is in the third best performing class.
	 */
	public void setBestThirdPerforming(boolean better) {
		this.isBestThirdPerforming = better;
		updateView();
	}

	/**
	 * Probes, whether this node is in the fourth best performing class.
	 */
	public boolean isBestFourthPerforming() {
		return isBestFourthPerforming;
	}

	/**
	 * Sets whether this node is in the fourth best performing class.
	 */
	public void setBestFourthPerforming(boolean better) {
		this.isBestFourthPerforming = better;
		updateView();
	}

	/**
	 * Probes, whether this node is equally performing.
	 */
	public boolean isEqualPerforming() {
		return isEqualPerforming;
	}

	/**
	 * Sets whether this node is equally performing.
	 */
	public void setEqualPerforming(boolean equal) {
		this.isEqualPerforming = equal;
		updateView();
	}

	/**
	 * Probes, whether this node is worse performing.
	 */
	public boolean isWorsePerforming() {
		return isWorsePerforming;
	}

	/**
	 * Sets whether this node is worse performing.
	 */
	public void setWorsePerforming(boolean worse) {
		this.isWorsePerforming = worse;
		updateView();
	}

	// We divide the worse performing nodes in four classes:
	// worst, second worst, third worst, fourth worst = just below equal
	/**
	 * Probes, whether this node is in the worst performing class.
	 */
	public boolean isWorstPerforming() {
		return isWorstPerforming;
	}

	/**
	 * Sets whether this node is in the worst performing class.
	 */
	public void setWorstPerforming(boolean worse) {
		this.isWorstPerforming = worse;
		updateView();
	}

	/**
	 * Probes, whether this node is in the second Worst performing class.
	 */
	public boolean isWorstSecondPerforming() {
		return isWorstSecondPerforming;
	}

	/**
	 * Sets whether this node is in the second Worst performing class.
	 */
	public void setWorstSecondPerforming(boolean worse) {
		this.isWorstSecondPerforming = worse;
		updateView();
	}

	/**
	 * Probes, whether this node is the third in the Worst performing class.
	 */
	public boolean isWorstThirdPerforming() {
		return isWorstThirdPerforming;
	}

	/**
	 * Sets whether this node is in the third Worst performing class.
	 */
	public void setWorstThirdPerforming(boolean worse) {
		this.isWorstThirdPerforming = worse;
		updateView();
	}

	/**
	 * Probes, whether this node is in the fourth Worst performing class.
	 */
	public boolean isWorstFourthPerforming() {
		return isWorstFourthPerforming;
	}

	/**
	 * Sets whether this node is in the fourth Worst performing class.
	 */
	public void setWorstFourthPerforming(boolean worse) {
		this.isWorstFourthPerforming = worse;
		updateView();
	}

	/**
	 * Returns a clone of the node
	 */
	public Object clone() {
		RedesignNode clone = (RedesignNode) super.clone();
		clone.model = model;
		return clone;
	}

}
