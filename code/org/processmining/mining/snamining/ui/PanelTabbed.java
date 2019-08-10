package org.processmining.mining.snamining.ui;

import javax.swing.JTabbedPane;

import org.processmining.mining.snamining.SocialNetworkOptions;

public class PanelTabbed extends JTabbedPane {

	private PanelHandoverOfWork panelHandoverOfWork = new PanelHandoverOfWork();
	private PanelSubcontracting panelSubcontracting = new PanelSubcontracting();
	private PanelWorkingTogether panelWorkingTogether = new PanelWorkingTogether();
	private PanelReassignment panelReassignment = new PanelReassignment();
	private PanelSimilarTask panelSimilarTask = new PanelSimilarTask();

	public PanelTabbed() {
		this.add(panelSubcontracting, "Subcontracting");
		this.add(panelHandoverOfWork, "Handover of work");
		this.add(panelWorkingTogether, "Working together");
		this.add(panelSimilarTask, "Similar task");
		this.add(panelReassignment, "Reassignment");
	}

	public int getSelectedMetrics() {
		int nIndex = 0;
		switch (this.getSelectedIndex()) {
		case 0:
			nIndex = SocialNetworkOptions.SUBCONTRACTING;
			break;
		case 1:
			nIndex = SocialNetworkOptions.HANDOVER_OF_WORK;
			break;
		case 2:
			nIndex = SocialNetworkOptions.WORKING_TOGETHER;
			break;
		case 3:
			nIndex = SocialNetworkOptions.SIMILAR_TASK;
			break;
		case 4:
			nIndex = SocialNetworkOptions.REASSIGNMENT;
			break;
		case 5:
			nIndex = SocialNetworkOptions.SETTINGS;
			break;
		}
		return nIndex;
	}

	/**
	 * Returns the beta value specified by the user. This value only applies to
	 * SUBCONTRACTING, HANDOVER_OF_WORK and REASSIGNMENT.
	 * 
	 * @return the beta value
	 */
	public double getBeta() {
		switch (getSelectedMetrics()) {
		case SocialNetworkOptions.SUBCONTRACTING:
			return Double.parseDouble(panelSubcontracting.getBeta());
		case SocialNetworkOptions.HANDOVER_OF_WORK:
			return Double.parseDouble(panelHandoverOfWork.getBeta());
			// case REASSIGNMENT:
			// return Double.parseDouble(raBeta.getText());
		default:
			return 0.0;
		}
	}

	/**
	 * Returns the specified depth of calculation. This value only applies to
	 * SUBCONTRACTING and HANDOVER_OF_WORK.
	 * 
	 * @return the depth of calculation
	 */
	public int getDepthOfCalculation() {
		switch (getSelectedMetrics()) {
		case SocialNetworkOptions.SUBCONTRACTING:
			return Integer.parseInt(panelSubcontracting.getDepth());
		case SocialNetworkOptions.HANDOVER_OF_WORK:
			return Integer.parseInt(panelHandoverOfWork.getDepth());
		default:
			return 0;
		}
	}

	/**
	 * Returns whether causality should be ignored. This value only applies to
	 * SUBCONTRACTING and HANDOVER_OF_WORK.
	 * 
	 * @return true if causality should be ignored, false otherwise
	 */
	public boolean getConsiderCausality() {
		switch (getSelectedMetrics()) {
		case SocialNetworkOptions.SUBCONTRACTING:
			return panelSubcontracting.getConsiderCausality();
		case SocialNetworkOptions.HANDOVER_OF_WORK:
			return panelHandoverOfWork.getConsiderCausality();
		default:
			return false;
		}
	}

	/**
	 * Returns whether multiple transfers should be ignored. This value only
	 * applies to SUBCONTRACTING and HANDOVER_OF_WORK.
	 * 
	 * @return true if multiple transfers should be ignored, false otherwise
	 */
	public boolean getConsiderMultipleTransfers() {
		switch (getSelectedMetrics()) {
		case SocialNetworkOptions.SUBCONTRACTING:
			return panelSubcontracting.getConsiderMultipleTransfers();
		case SocialNetworkOptions.HANDOVER_OF_WORK:
			return panelHandoverOfWork.getConsiderMultipleTransfers();
		case SocialNetworkOptions.REASSIGNMENT:
			return panelReassignment.getIgnoreMultipleTransfers();
		default:
			return false;
		}
	}

	/**
	 * Returns whether only direct succession/subcontracts should be considered.
	 * This value only applies to SUBCONTRACTING and HANDOVER_OF_WORK.
	 * 
	 * @return true if only direct succession/subcontracts should be considered,
	 *         false otherwise
	 */
	public boolean getOnlyDirectSuccession() {
		switch (getSelectedMetrics()) {
		case SocialNetworkOptions.SUBCONTRACTING:
			return panelSubcontracting.getConsiderDirectSuccession();
		case SocialNetworkOptions.HANDOVER_OF_WORK:
			return panelHandoverOfWork.getConsiderDirectSuccession();
		default:
			return false;
		}
	}

	/**
	 * Returns the currently selected option for WORKING_TOGETHER. Can be one of
	 * the constants: SIMULTANEOUS_APPEARANCE_RATIO, DISTANCE_WITH_CAUSALITY or
	 * DISTANCE_WITHOUT_CAUSALITY.
	 * 
	 * @return the currently selected option for WORKING_TOGETHER
	 */
	public int getWorkingTogetherSetting() {
		if (panelWorkingTogether.getSimultaneousAppearance()) {
			return SocialNetworkOptions.SIMULTANEOUS_APPEARANCE_RATIO;
		} else if (panelWorkingTogether.getDistanceWithCausality()) {
			return SocialNetworkOptions.DISTANCE_WITH_CAUSALITY;
		} else { // panelWorkingTogether.getDistanceWithoutCausality()
			return SocialNetworkOptions.DISTANCE_WITHOUT_CAUSALITY;
		}
	}

	/**
	 * Returns the currently selected option for REASSIGNMENT. Can be one of the
	 * constants: DIRECT_REASSIGNMENT or CONSIDER_DISTANCE.
	 * 
	 * @return the currently selected option for REASSIGNMENT
	 */
	public int getReassignmentSetting() {
		return panelReassignment.getIgnoreMultipleTransfers() ? 0
				: SocialNetworkOptions.MULTIPLE_REASSIGNMENT;
	}

	/**
	 * Returns the currently selected option for SIMILAR_TASK. Can be one of the
	 * constants: EUCLIDIAN_DISTANCE, CORRELATION_COEFFICIENT,
	 * SIMILARITY_COEFFICIENT or HAMMING_DISTANCE.
	 * 
	 * @return the currently selected option for SIMILAR_TASK
	 */
	public int getSimilarTaskSetting() {
		if (panelSimilarTask.getEuclidianDistance()) {
			return SocialNetworkOptions.EUCLIDIAN_DISTANCE;
		} else if (panelSimilarTask.getCorrelationCoefficient()) {
			return SocialNetworkOptions.CORRELATION_COEFFICIENT;
		} else if (panelSimilarTask.getSimilarityCoefficient()) {
			return SocialNetworkOptions.SIMILARITY_COEFFICIENT;
		} else { // stHammingDistance.isSelected()
			return SocialNetworkOptions.HAMMING_DISTANCE;
		}
	}
}
