package org.processmining.analysis.epc.similarity;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

import org.processmining.framework.ui.*;
import org.processmining.framework.util.*;

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
public class SimilarityOptionDialog extends JDialog {

	private SimilarityOptions options;

	private Insets INSETS = new Insets(3, 3, 3, 3);

	private JSlider eventThresholdSlider;
	private JLabel eventThresholdLabel = new JLabel("Event threshold:");

	private JSlider eventSemanticWeightSlider;
	private JLabel eventSemanticWeightLabel = new JLabel(
			"Event semantic weight:");

	private JSlider eventSyntaxWeightSlider;
	private JLabel eventSyntaxWeightLabel = new JLabel("Event syntax weight:");

	private JSlider functionThresholdSlider;
	private JLabel functionThresholdLabel = new JLabel("Function threshold:");

	private JSlider functionSemanticWeightSlider;
	private JLabel functionSemanticWeightLabel = new JLabel(
			"Function semantic weight:");

	private JSlider functionSyntaxWeightSlider;
	private JLabel functionSyntaxWeightLabel = new JLabel(
			"Function syntax weight:");

	private JSlider functionStructureWeightSlider;
	private JLabel functionStructureWeightLabel = new JLabel(
			"Function structural weight:");

	private JSpinner parallelThreadsSpinner;
	private JLabel parallelThreadsLabel = new JLabel(
			"Number of parallel threads:");

	private JTextField folderField;
	private JLabel folderLabel = new JLabel("Folder for footprint files:");

	private JCheckBox removalCheck;
	private JCheckBox useSimCheck;

	private boolean ok;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");
	private JButton defaultButton = new JButton("Restore defaults");

	private JSlider getSlider(double initValue) {
		int val = (int) Math.round(100 * initValue);
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, val);
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(25);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setUI(new ToolTipSliderUI(slider));
		return slider;
	}

	private void initialize() {
		eventThresholdSlider = getSlider(options.getEventThreshold());
		eventSemanticWeightSlider = getSlider(options.getEventSemanticWeight());
		eventSyntaxWeightSlider = getSlider(options.getEventSyntaxWeight());
		functionThresholdSlider = getSlider(options.getFunctionThreshold());
		functionSemanticWeightSlider = getSlider(options
				.getFunctionSemanticWeight());
		functionSyntaxWeightSlider = getSlider(options
				.getFunctionSyntaxWeight());
		functionStructureWeightSlider = getSlider(options
				.getFunctionStructureWeight());
		parallelThreadsSpinner = new JSpinner(new SpinnerNumberModel(options
				.getParallelThreads(), 1, Integer.MAX_VALUE, 1));
		folderField = new JTextField(options.getFootprintFolder());
		removalCheck = new JCheckBox("Remove initial and final nodes");
		removalCheck.setSelected(options.getRemoveInitialFinalNodes());
		useSimCheck = new JCheckBox("Use Similarity Values after Matching");
		useSimCheck.setSelected(options.getUseSimilarityValues());
	}

	private void restoreDefaults() {
		eventThresholdSlider.setValue((int) Math.round(100 * options
				.getEventThreshold()));
		eventSemanticWeightSlider.setValue((int) Math.round(100 * options
				.getEventSemanticWeight()));
		eventSyntaxWeightSlider.setValue((int) Math.round(100 * options
				.getEventSyntaxWeight()));
		functionThresholdSlider.setValue((int) Math.round(100 * options
				.getFunctionThreshold()));
		functionSemanticWeightSlider.setValue((int) Math.round(100 * options
				.getFunctionSemanticWeight()));
		functionSyntaxWeightSlider.setValue((int) Math.round(100 * options
				.getFunctionSyntaxWeight()));
		functionStructureWeightSlider.setValue((int) Math.round(100 * options
				.getFunctionStructureWeight()));
		parallelThreadsSpinner.setValue(options.getParallelThreads());
		folderField.setText(options.getFootprintFolder());
		removalCheck.setSelected(options.getRemoveInitialFinalNodes());
		useSimCheck.setSelected(options.getUseSimilarityValues());
	}

	public SimilarityOptionDialog(final SimilarityOptions options) {
		super(MainUI.getInstance(), "Options for EPC similarity calculation",
				true);
		this.options = options;
		initialize();

		this.setLayout(new GridBagLayout());

		int i = 0;

		this.add(eventThresholdLabel, new GridBagConstraints(0, i, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, INSETS,
				0, 0));
		this.add(eventThresholdSlider, new GridBagConstraints(1, i++, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this.add(eventSemanticWeightLabel, new GridBagConstraints(0, i, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(eventSemanticWeightSlider, new GridBagConstraints(1, i++, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this.add(eventSyntaxWeightLabel, new GridBagConstraints(0, i, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(eventSyntaxWeightSlider, new GridBagConstraints(1, i++, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this.add(functionThresholdLabel, new GridBagConstraints(0, i, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(functionThresholdSlider, new GridBagConstraints(1, i++, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this.add(functionSemanticWeightLabel, new GridBagConstraints(0, i, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(functionSemanticWeightSlider, new GridBagConstraints(1, i++,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));

		this.add(functionSyntaxWeightLabel, new GridBagConstraints(0, i, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(functionSyntaxWeightSlider, new GridBagConstraints(1, i++, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this.add(functionStructureWeightLabel, new GridBagConstraints(0, i, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
				INSETS, 0, 0));
		this.add(functionStructureWeightSlider, new GridBagConstraints(1, i++,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, INSETS, 0, 0));

		this.add(parallelThreadsLabel, new GridBagConstraints(0, i, 1, 1, 0.0,
				0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, INSETS,
				0, 0));
		this.add(parallelThreadsSpinner, new GridBagConstraints(1, i++, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
				INSETS, 0, 0));

		this
				.add(folderLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(folderField, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(removalCheck, new GridBagConstraints(0, i++, 2, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(useSimCheck, new GridBagConstraints(0, i++, 2, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
		buttonPanel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				options.setDefaults();
				restoreDefaults();
				repaint();
			}
		});
		// empty slot
		buttonPanel.add(new JPanel());
		buttonPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		okButton.requestFocusInWindow();

		this
				.add(buttonPanel, new GridBagConstraints(0, i++, 2, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						INSETS, 0, 0));

		pack();
		CenterOnScreen.center(this);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public boolean showSettings() {
		setVisible(true);
		updateOptions();
		return ok;
	}

	private void updateOptions() {
		options.setEventSemanticWeight((double) eventSemanticWeightSlider
				.getValue() / 100.0);
		options.setEventSyntaxWeight((double) eventSyntaxWeightSlider
				.getValue() / 100.0);
		options
				.setEventThreshold((double) eventThresholdSlider.getValue() / 100.0);

		options.setFunctionSemanticWeight((double) functionSemanticWeightSlider
				.getValue() / 100.0);
		options
				.setFunctionStructureWeight((double) functionStructureWeightSlider
						.getValue() / 100.0);
		options.setFunctionSyntaxWeight((double) functionSyntaxWeightSlider
				.getValue() / 100.0);
		options.setFunctionThreshold((double) functionThresholdSlider
				.getValue() / 100.0);

		options
				.setParallelThreads(((Integer) parallelThreadsSpinner
						.getValue()).intValue());

		options.setFootprintFolder(folderField.getText());

		options.setRemoveInitialFinalNodes(removalCheck.isSelected());
		options.setUseSimilarityValues(useSimCheck.isSelected());
	}
}

class ToolTipSliderUI extends BasicSliderUI implements MouseMotionListener,
		MouseListener {

	final JPopupMenu pop = new JPopupMenu();
	JMenuItem item = new JMenuItem();

	public ToolTipSliderUI(JSlider slider) {
		super(slider);
		slider.addMouseMotionListener(this);
		slider.addMouseListener(this);
		pop.add(item);

		pop.setDoubleBuffered(true);
	}

	public void showToolTip(MouseEvent me) {
		item.setText(slider.getValue() + " %");

		// limit the tooltip location relative to the slider
		Rectangle b = me.getComponent().getBounds();
		int x = me.getX();
		x = (x > b.x ? b.x : (x < b.x - b.width ? b.x - b.width : x));

		pop.show(me.getComponent(), x, -30);

		item.setArmed(false);
	}

	public void mouseDragged(MouseEvent me) {
		showToolTip(me);
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		showToolTip(me);
	}

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseReleased(MouseEvent me) {
		pop.setVisible(false);
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}
}
