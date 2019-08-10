package org.processmining.mining.regionmining;

import javax.swing.JPanel;
import org.processmining.framework.log.LogSummary;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import javax.swing.JSpinner;
import org.processmining.analysis.AnalysisPlugin;
import java.awt.GridBagConstraints;
import javax.swing.event.ChangeListener;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import org.processmining.framework.plugin.Plugin;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ParikhLanguageRegionMinerUI extends JPanel {

	private String[] OPTIONS = new String[] {
			ParikhLanguageRegionMinerOptions.NO_RES,
			ParikhLanguageRegionMinerOptions.STATEMACHINE,
			ParikhLanguageRegionMinerOptions.MARKEDGRAPH,
			ParikhLanguageRegionMinerOptions.EXT_FREE_CHOICE,
			ParikhLanguageRegionMinerOptions.NO_PLACES };
	private JComboBox netTypeCombo = new JComboBox(OPTIONS);

	private JPanel elementaryNetPanel = new JPanel();
	private JCheckBox elementaryNetCheck = new JCheckBox();

	private JPanel useCausalDepPanel = new JPanel();
	private JCheckBox useCausalDepCheck = new JCheckBox();

	private JPanel useIntVarPanel = new JPanel();
	private JCheckBox useIntVarCheck = new JCheckBox();

	private JPanel searchInitMarkingPanel = new JPanel();
	private JCheckBox searchInitMarkingCheck = new JCheckBox();

	private JPanel disAllowNonCausalPanel = new JPanel();
	private JCheckBox disAllowNonCausalCheck = new JCheckBox();

	private JPanel emptyAfterCasePanel = new JPanel();
	private JCheckBox emptyAfterCaseCheck = new JCheckBox();

	private JPanel noSelfLoopsPanel = new JPanel();
	private JCheckBox noSelfLoopsCheck = new JCheckBox();

	private JPanel restrictToDisconnectedTransPanel = new JPanel();
	private JCheckBox restrictToDisconnectedTransCheck = new JCheckBox();

	private JPanel removeExistingPlacesPanel = new JPanel();
	private JCheckBox removeExistingPlacesCheck = new JCheckBox();

	private JPanel maxEdgePanel = new JPanel();
	private JSpinner maxInEdgeSpin = new JSpinner();
	private JSpinner maxOutEdgeSpin = new JSpinner();
	private JSpinner maxEdgeSpin = new JSpinner();
	private JLabel maxOutEdgeLabel = new JLabel(
			"Maximum number of outgoing edges per place.");
	private JLabel maxEdgeLabel = new JLabel(
			"Maximum number of total edges per place.");
	private JLabel maxInEdgeLabel = new JLabel(
			"Maximum number of incoming edges per place.");

	private JPanel maxPlacePanel = new JPanel();
	private JSpinner maxPlaceSpin = new JSpinner();

	private JPanel boundedNessPanel = new JPanel();
	private JSpinner boundedNessSpin = new JSpinner();
	private JLabel boundedNessLabel = new JLabel();

	private Insets INSETS = new Insets(1, 1, 1, 1);

	private ParikhLanguageRegionMinerOptions options;

	public ParikhLanguageRegionMinerUI(LogSummary summary, JPanel rightPanel,
			Plugin owner) {

		options = new ParikhLanguageRegionMinerOptions(summary);

		boolean ISANALYSISPLUGIN = owner instanceof AnalysisPlugin;

		JPanel thisOptionsPanel = new JPanel(new GridBagLayout());

		int i = 0;
		// ==========================================================================
		JPanel netTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel netTypeLabel = new JLabel("Select resulting net type:");
		netTypePanel.add(netTypeLabel);
		netTypePanel.add(netTypeCombo);
		thisOptionsPanel.add(netTypePanel, new GridBagConstraints(0, i++, 2, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		elementaryNetCheck.setText("Return elementary net.");
		elementaryNetCheck.setEnabled(true);
		elementaryNetCheck.setSelected(options.getElementaryNet());
		elementaryNetPanel.setLayout(new BorderLayout());
		elementaryNetPanel.add(elementaryNetCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(elementaryNetPanel, new GridBagConstraints(0, i++,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		useCausalDepCheck.setText("Use causal dependencies to direct search.");
		useCausalDepCheck.setEnabled(true);
		useCausalDepCheck.setSelected(options.getUseCausalDependencies());
		useCausalDepPanel.setLayout(new BorderLayout());
		useCausalDepPanel.add(useCausalDepCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(useCausalDepPanel, new GridBagConstraints(0, i++,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		disAllowNonCausalCheck
				.setText("Do not allow places that express causal dependencies not observed.");
		disAllowNonCausalCheck.setEnabled(true);
		disAllowNonCausalCheck.setSelected(options
				.getRestrictToCausalDependencies());
		disAllowNonCausalPanel.setLayout(new BorderLayout());
		disAllowNonCausalPanel.add(disAllowNonCausalCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(disAllowNonCausalPanel, new GridBagConstraints(0,
				i++, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		searchInitMarkingCheck.setText("Search for initial marking.");
		searchInitMarkingCheck.setEnabled(true);
		searchInitMarkingCheck.setSelected(options.getSearchInitialMarking());
		searchInitMarkingPanel.setLayout(new BorderLayout());
		searchInitMarkingPanel.add(searchInitMarkingCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(searchInitMarkingPanel, new GridBagConstraints(0,
				i++, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		noSelfLoopsCheck.setText("Do not allow for self loops.");
		noSelfLoopsCheck.setEnabled(!elementaryNetCheck.isSelected());
		noSelfLoopsCheck.setSelected(options.getDenySelfLoops());
		noSelfLoopsPanel.setLayout(new BorderLayout());
		noSelfLoopsPanel.add(noSelfLoopsCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(noSelfLoopsPanel, new GridBagConstraints(0, i++,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		emptyAfterCaseCheck.setText("Enforce empty net after case completion.");
		emptyAfterCaseCheck.setEnabled(true);
		emptyAfterCaseCheck.setSelected(options.getEmptyAfterCompleteCase());
		emptyAfterCasePanel.setLayout(new BorderLayout());
		emptyAfterCasePanel.add(emptyAfterCaseCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(emptyAfterCasePanel, new GridBagConstraints(0,
				i++, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		maxInEdgeSpin.setModel(new SpinnerNumberModel(options.getMaxInEdges(),
				1, summary.getLogEvents().size(), 1));
		maxOutEdgeSpin.setModel(new SpinnerNumberModel(
				options.getMaxOutEdges(), 1, summary.getLogEvents().size(), 1));
		maxEdgeSpin.setModel(new SpinnerNumberModel(options.getMaxTotalEdges(),
				1, 2 * summary.getLogEvents().size(), 1));

		JPanel inPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		inPanel.add(maxInEdgeSpin);
		inPanel.add(maxInEdgeLabel);

		JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		totalPanel.add(maxEdgeSpin);
		totalPanel.add(maxEdgeLabel);

		JPanel outPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		outPanel.add(maxOutEdgeSpin);
		outPanel.add(maxOutEdgeLabel);

		maxEdgePanel.setLayout(new BorderLayout());
		maxEdgePanel.add(inPanel, BorderLayout.NORTH);
		maxEdgePanel.add(totalPanel, BorderLayout.CENTER);
		maxEdgePanel.add(outPanel, BorderLayout.SOUTH);

		thisOptionsPanel.add(maxEdgePanel, new GridBagConstraints(0, i++, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		// ==========================================================================

		// ==========================================================================
		boundedNessSpin.setModel(new SpinnerNumberModel(options
				.getSimulationBoundedness(), 0, summary
				.getNumberOfAuditTrailEntries(), 1));
		boundedNessLabel
				.setText("Maximum number of tokens per place while replaying log (0 = infinity).");

		boundedNessPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		boundedNessPanel.add(boundedNessSpin);
		boundedNessPanel.add(boundedNessLabel);

		thisOptionsPanel.add(boundedNessPanel, new GridBagConstraints(0, i++,
				2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		maxPlaceSpin.setModel(new SpinnerNumberModel(options.getMaxPlaces(), 0,
				summary.getLogEvents().size() * summary.getLogEvents().size(),
				1));
		JLabel maxPlaceLabel = new JLabel("Maximum number of places.");

		maxPlacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		maxPlacePanel.add(maxPlaceSpin);
		maxPlacePanel.add(maxPlaceLabel);

		thisOptionsPanel.add(maxPlacePanel, new GridBagConstraints(0, i++, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		// ==========================================================================

		// ==========================================================================
		restrictToDisconnectedTransCheck
				.setText("Restrict search to disconnected transitions.");
		restrictToDisconnectedTransCheck.setEnabled(true);
		restrictToDisconnectedTransCheck.setSelected(options
				.getRestrictToDisconnectedTrans());
		restrictToDisconnectedTransPanel.setLayout(new BorderLayout());
		restrictToDisconnectedTransPanel.add(restrictToDisconnectedTransCheck,
				BorderLayout.CENTER);
		if (ISANALYSISPLUGIN) {
			thisOptionsPanel.add(restrictToDisconnectedTransPanel,
					new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
							GridBagConstraints.WEST, GridBagConstraints.BOTH,
							INSETS, 0, 0));
		}
		// ==========================================================================

		// ==========================================================================
		removeExistingPlacesCheck
				.setText("Remove existing places violating fitness.");
		removeExistingPlacesCheck.setEnabled(true);
		removeExistingPlacesCheck
				.setSelected(options.getRemoveExistingPlaces());
		removeExistingPlacesPanel.setLayout(new BorderLayout());
		removeExistingPlacesPanel.add(removeExistingPlacesCheck,
				BorderLayout.CENTER);
		if (ISANALYSISPLUGIN) {
			thisOptionsPanel.add(removeExistingPlacesPanel,
					new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0,
							GridBagConstraints.WEST, GridBagConstraints.BOTH,
							INSETS, 0, 0));
		}
		// ==========================================================================

		// ==========================================================================
		useIntVarCheck
				.setText("Use integer variables (not doing so gives an approximation).");
		useIntVarCheck.setEnabled(true);
		useIntVarCheck.setSelected(options.getIntVars());
		useIntVarPanel.setLayout(new BorderLayout());
		useIntVarPanel.add(useIntVarCheck, BorderLayout.CENTER);
		thisOptionsPanel.add(useIntVarPanel, new GridBagConstraints(0, i++, 2,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		// ==========================================================================

		setLayout(new FlowLayout());
		add(thisOptionsPanel);
		add(rightPanel);

		// =========LISTENERSECTION===================================================
		elementaryNetCheck.addActionListener(new ActionListener() {
			private boolean selfSel = noSelfLoopsCheck.isSelected();
			private int bound = ((Integer) boundedNessSpin.getValue())
					.intValue();

			public void actionPerformed(ActionEvent e) {
				if (noSelfLoopsCheck.isEnabled()) {
					// The self-loop check is currently enabled, so store state
					selfSel = noSelfLoopsCheck.isSelected();
					bound = ((Integer) boundedNessSpin.getValue()).intValue();
				}
				noSelfLoopsCheck.setEnabled(!elementaryNetCheck.isSelected());
				noSelfLoopsCheck.setSelected(elementaryNetCheck.isSelected());
				boundedNessSpin.setEnabled(!elementaryNetCheck.isSelected());
				boundedNessLabel.setEnabled(!elementaryNetCheck.isSelected());
				boundedNessSpin.setValue(new Integer(1));

				if (noSelfLoopsCheck.isEnabled()) {
					// I just enabled it, hence return to old state
					noSelfLoopsCheck.setSelected(selfSel);
					boundedNessSpin.setValue(new Integer(bound));
				}
				updateOptions();
			}
		});
		// ==========================================================================
		if (elementaryNetCheck.isSelected()) {
			boundedNessSpin.setValue(new Integer(1));
			boundedNessSpin.setEnabled(false);
			boundedNessLabel.setEnabled(false);
			updateOptions();
		}

		// ==========================================================================
		useCausalDepCheck.addActionListener(new ActionListener() {
			private boolean disAllowNon = disAllowNonCausalCheck.isSelected();

			public void actionPerformed(ActionEvent e) {
				if (disAllowNonCausalCheck.isEnabled()) {
					// The disallow check is currently enabled, so store state
					disAllowNon = disAllowNonCausalCheck.isSelected();
				}
				disAllowNonCausalCheck.setEnabled(useCausalDepCheck
						.isSelected());
				disAllowNonCausalCheck.setSelected(false);
				if (disAllowNonCausalCheck.isEnabled()) {
					// I just enabled it, hence return to old state
					disAllowNonCausalCheck.setSelected(disAllowNon);
				}
				updateOptions();
			}
		});
		// ==========================================================================

		// ==========================================================================
		netTypeCombo.addActionListener(new ActionListener() {
			private int inBound = ((Integer) maxInEdgeSpin.getValue())
					.intValue();

			private int outBound = ((Integer) maxOutEdgeSpin.getValue())
					.intValue();

			private int total = ((Integer) maxEdgeSpin.getValue()).intValue();

			private boolean mgSelected = netTypeCombo.getSelectedItem().equals(
					ParikhLanguageRegionMinerOptions.MARKEDGRAPH);

			public void actionPerformed(ActionEvent e) {
				if (netTypeCombo.getSelectedItem().equals(
						ParikhLanguageRegionMinerOptions.MARKEDGRAPH)) {
					mgSelected = true;
					// selected the marked-graph
					if (maxInEdgeSpin.isEnabled()) {
						// The in/out spinners arecurrently enabled, so store
						// states
						inBound = ((Integer) maxInEdgeSpin.getValue())
								.intValue();
						outBound = ((Integer) maxOutEdgeSpin.getValue())
								.intValue();
						total = ((Integer) maxEdgeSpin.getValue()).intValue();
					}
					maxInEdgeSpin.setEnabled(false);
					maxInEdgeLabel.setEnabled(false);
					maxInEdgeSpin.setValue(new Integer(1));

					maxOutEdgeSpin.setEnabled(false);
					maxOutEdgeLabel.setEnabled(false);
					maxOutEdgeSpin.setValue(new Integer(1));

					maxEdgeSpin.setEnabled(false);
					maxEdgeLabel.setEnabled(false);
					maxEdgeSpin.setValue(new Integer(2));
				} else if (mgSelected) {
					mgSelected = false;
					// we unselected markedgraph
					maxInEdgeSpin.setEnabled(true);
					maxInEdgeLabel.setEnabled(true);
					maxInEdgeSpin.setValue(new Integer(inBound));

					maxOutEdgeSpin.setEnabled(true);
					maxOutEdgeLabel.setEnabled(true);
					maxOutEdgeSpin.setValue(new Integer(outBound));

					maxEdgeSpin.setEnabled(true);
					maxEdgeLabel.setEnabled(true);
					maxEdgeSpin.setValue(new Integer(total));

				}
				updateOptions();
			}
		});
		// ==========================================================================

		// ==========================================================================
		maxEdgeSpin.addChangeListener(new ChangeListener() {
			private int old = ((Integer) maxEdgeSpin.getValue()).intValue();

			public void stateChanged(ChangeEvent e) {
				int total = ((Integer) maxEdgeSpin.getValue()).intValue();
				int in = ((Integer) maxInEdgeSpin.getValue()).intValue();
				int out = ((Integer) maxOutEdgeSpin.getValue()).intValue();
				if (old > total) {
					// going down
					if (total < in) {
						// update the maximum inputs
						maxInEdgeSpin.setValue(new Integer(total));
					}
					if (total < out) {
						// update the maximum outputs
						maxOutEdgeSpin.setValue(new Integer(total));
					}
				}
				old = total;
				updateOptions();
			}
		});
		// ==========================================================================

		// ==========================================================================
		maxOutEdgeSpin.addChangeListener(new ChangeListener() {
			private int old = ((Integer) maxOutEdgeSpin.getValue()).intValue();

			public void stateChanged(ChangeEvent e) {
				int total = ((Integer) maxEdgeSpin.getValue()).intValue();
				int this_val = ((Integer) maxOutEdgeSpin.getValue()).intValue();
				if (this_val > old) {
					// going up
					if (this_val > total) {
						maxEdgeSpin.setValue(new Integer(this_val));
					}
				}
				old = this_val;
				updateOptions();
			}
		});
		// ==========================================================================

		// ==========================================================================
		maxInEdgeSpin.addChangeListener(new ChangeListener() {
			private int old = ((Integer) maxInEdgeSpin.getValue()).intValue();

			public void stateChanged(ChangeEvent e) {
				int total = ((Integer) maxEdgeSpin.getValue()).intValue();
				int this_val = ((Integer) maxInEdgeSpin.getValue()).intValue();
				if (this_val > old) {
					// going up
					if (this_val > total) {
						maxEdgeSpin.setValue(new Integer(this_val));
					}
				}
				old = this_val;
				updateOptions();
			}
		});
		// ==========================================================================

		updateOptions();

	}

	public ParikhLanguageRegionMinerOptions getOptions() {
		return options;
	}

	protected void updateOptions() {
		options.setDenySelfLoops(noSelfLoopsCheck.isSelected());
		options.setElementaryNet(elementaryNetCheck.isSelected());
		options.setEmptyAfterCompleteCase(emptyAfterCaseCheck.isSelected());
		options.setRestrictToDisconnectedTrans(restrictToDisconnectedTransCheck
				.isSelected());
		options.setIntVars(useIntVarCheck.isSelected());
		options.setMaxInEdges(((Integer) maxInEdgeSpin.getValue()).intValue());
		options
				.setMaxOutEdges(((Integer) maxOutEdgeSpin.getValue())
						.intValue());
		options.setMaxPlaces(((Integer) maxPlaceSpin.getValue()).intValue());
		options.setMaxTotalEdges(((Integer) maxEdgeSpin.getValue()).intValue());
		options.setRestrictToCausalDependencies(disAllowNonCausalCheck
				.isSelected());
		options.setSearchInitialMarking(searchInitMarkingCheck.isSelected());
		options.setSimulationBoundedness(((Integer) boundedNessSpin.getValue())
				.intValue());
		options.setUseCausalDependencies(useCausalDepCheck.isSelected());
		options.setRestrictions((String) netTypeCombo.getSelectedItem());
		options.setRemoveExistingPlaces(removeExistingPlacesCheck.isSelected());
	}

}
