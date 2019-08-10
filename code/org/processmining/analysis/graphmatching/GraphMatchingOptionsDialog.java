package org.processmining.analysis.graphmatching;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicSliderUI;

import org.processmining.analysis.graphmatching.algos.DistanceAlgoAbstr;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceAStarSim;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceAllOptions;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceGreedy;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceLexical;
import org.processmining.analysis.graphmatching.algos.GraphEditDistanceProcessHeuristic;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;

public class GraphMatchingOptionsDialog extends JDialog {

	private static final long serialVersionUID = 3787682118828884684L;
	boolean ok;
	private JButton okButton = new JButton("OK");
	private JButton cancelButton = new JButton("Cancel");

	private Insets INSETS = new Insets(3, 3, 3, 3);

	private JComboBox algoList;
	private JLabel algoLabel = new JLabel("Algorithm:");

	private JSlider wskipvSlider;
	private JLabel wskipvLabel = new JLabel("Weight skipped vertex (%):");

	private JSlider wskipeSlider;
	private JLabel wskipeLabel = new JLabel("Weight skipped edge (%):");

	private JSlider wsubvSlider;
	private JLabel wsubvLabel = new JLabel("Weight substituted vertex (%):");

	private JSlider ledcutoffSlider;
	private JLabel ledcutoffLabel = new JLabel("Label similarity cut-off (%):");

	private JTextField pruneatTextBox;
	private JLabel pruneatLabel = new JLabel("Prune at:");

	private JTextField prunetoTextBox;
	private JLabel prunetoLabel = new JLabel("Prune to:");

	private JCheckBox eventsCheck;
	private JLabel eventsLabel = new JLabel("Consider events:");

	private JCheckBox groupingCheck;
	private JLabel groupingLabel = new JLabel("Allow task grouping:");

	public GraphMatchingOptionsDialog() {
		super(MainUI.getInstance(), "Options", true);
		initialize();

		this.setLayout(new GridBagLayout());

		int i = 0;

		this
				.add(algoLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(algoList, new GridBagConstraints(1, i++, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		algoList.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setPreferences();
				}
			}
		});

		this
				.add(wskipvLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(wskipvSlider, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(wskipeLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(wskipeSlider, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(wsubvLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(wsubvSlider, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(ledcutoffLabel, new GridBagConstraints(0, i, 1, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this.add(ledcutoffSlider, new GridBagConstraints(1, i++, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, INSETS,
				0, 0));

		this
				.add(pruneatLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(pruneatTextBox, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(prunetoLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(prunetoTextBox, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(eventsLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(eventsCheck, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		this
				.add(groupingLabel, new GridBagConstraints(0, i, 1, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
						INSETS, 0, 0));
		this
				.add(groupingCheck, new GridBagConstraints(1, i++, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
						INSETS, 0, 0));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

		buttonPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		okButton.requestFocusInWindow();

		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});

		this
				.add(buttonPanel, new GridBagConstraints(0, i++, 2, 1, 0.0,
						0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						INSETS, 0, 0));

		setPreferences();
		pack();
		CenterOnScreen.center(this);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	}

	private JSlider getSlider() {
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(25);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setUI(new ToolTipSliderUI(slider));
		return slider;
	}

	private void initialize() {
		String algos[] = { "Greedy", "Exhaustive", "Heuristic", "A-star",
				"Lexical" };
		algoList = new JComboBox(algos);

		wskipvSlider = getSlider();
		wskipeSlider = getSlider();
		wsubvSlider = getSlider();
		ledcutoffSlider = getSlider();

		pruneatTextBox = new JTextField();
		prunetoTextBox = new JTextField();

		eventsCheck = new JCheckBox();
		groupingCheck = new JCheckBox();
	}

	public DistanceAlgoAbstr showDialog() {
		setVisible(true);
		if (ok) {
			DistanceAlgoAbstr algo = null;
			double vweight = ((double) wskipvSlider.getValue()) / 100.0;
			double sweight = ((double) wsubvSlider.getValue()) / 100.0;
			double eweight = ((double) wskipeSlider.getValue()) / 100.0;
			double ledcutoff = ((double) ledcutoffSlider.getValue()) / 100.0;
			double prunewhen = 100.0;
			try {
				prunewhen = (double) Integer.parseInt(pruneatTextBox.getText());
			} catch (Exception e) {
			}
			double pruneto = 10.0;
			try {
				pruneto = (double) Integer.parseInt(prunetoTextBox.getText());
			} catch (Exception e) {
			}
			double useepsilon = 0.0;
			double useevents = 0.0;
			if (eventsCheck.isSelected()) {
				useevents = 1.0;
			}
			double dogrouping = 0.0;
			if (groupingCheck.isSelected()) {
				dogrouping = 1.0;
			}
			if (algoList.getSelectedIndex() == 3) {
				useepsilon = 1.0;
			}
			Object weights[] = { "vweight", vweight, "sweight", sweight,
					"eweight", eweight, "ledcutoff", ledcutoff, "prunewhen",
					prunewhen, "pruneto", pruneto, "useepsilon", useepsilon,
					"useevents", useevents, "dogrouping", dogrouping };
			switch (algoList.getSelectedIndex()) {
			case 0: // Greedy
				algo = new GraphEditDistanceGreedy();
				break;
			case 1: // Exhaustive
				algo = new GraphEditDistanceAllOptions();
				break;
			case 2: // Heuristic
				algo = new GraphEditDistanceProcessHeuristic();
				break;
			case 3: // A-star
				algo = new GraphEditDistanceAStarSim();
				break;
			case 4: // Lexical
				algo = new GraphEditDistanceLexical();
				break;
			}
			algo.setWeight(weights);
			return algo;
		} else {
			return null;
		}
	}

	private void setPreferences() {
		// These preferences are determined in the paper
		switch (algoList.getSelectedIndex()) {
		case 0: // Greedy
			this.wskipvSlider.setValue(10);
			this.wsubvSlider.setValue(90);
			this.wskipeSlider.setValue(40);
			this.ledcutoffSlider.setValue(0);
			this.pruneatTextBox.setText("N/A");
			this.prunetoTextBox.setText("N/A");
			this.eventsCheck.setSelected(false);
			this.groupingCheck.setSelected(false);

			this.wskipvSlider.setEnabled(true);
			this.wsubvSlider.setEnabled(true);
			this.wskipeSlider.setEnabled(true);
			this.ledcutoffSlider.setEnabled(true);
			this.pruneatTextBox.setEnabled(false);
			this.prunetoTextBox.setEnabled(false);
			this.eventsCheck.setEnabled(true);
			this.groupingCheck.setEnabled(true);

			break;
		case 1: // Exhaustive
			this.wskipvSlider.setValue(10);
			this.wsubvSlider.setValue(80);
			this.wskipeSlider.setValue(20);
			this.ledcutoffSlider.setValue(0);
			this.pruneatTextBox.setText("100");
			this.prunetoTextBox.setText("10");
			this.eventsCheck.setSelected(false);
			this.groupingCheck.setSelected(false);

			this.wskipvSlider.setEnabled(true);
			this.wsubvSlider.setEnabled(true);
			this.wskipeSlider.setEnabled(true);
			this.ledcutoffSlider.setEnabled(true);
			this.pruneatTextBox.setEnabled(true);
			this.prunetoTextBox.setEnabled(true);
			this.eventsCheck.setEnabled(true);
			this.groupingCheck.setEnabled(false);

			break;
		case 2: // Heuristic
			this.wskipvSlider.setValue(10);
			this.wsubvSlider.setValue(80);
			this.wskipeSlider.setValue(20);
			this.ledcutoffSlider.setValue(0);
			this.pruneatTextBox.setText("100");
			this.prunetoTextBox.setText("10");
			this.eventsCheck.setSelected(false);
			this.groupingCheck.setSelected(false);

			this.wskipvSlider.setEnabled(true);
			this.wsubvSlider.setEnabled(true);
			this.wskipeSlider.setEnabled(true);
			this.ledcutoffSlider.setEnabled(true);
			this.pruneatTextBox.setEnabled(true);
			this.prunetoTextBox.setEnabled(true);
			this.eventsCheck.setEnabled(true);
			this.groupingCheck.setEnabled(false);

			break;
		case 3: // A-star
			this.wskipvSlider.setValue(20);
			this.wsubvSlider.setValue(10);
			this.wskipeSlider.setValue(70);
			this.ledcutoffSlider.setValue(48);
			this.pruneatTextBox.setText("N/A");
			this.prunetoTextBox.setText("N/A");
			this.eventsCheck.setSelected(true);
			this.groupingCheck.setSelected(false);

			this.wskipvSlider.setEnabled(true);
			this.wsubvSlider.setEnabled(true);
			this.wskipeSlider.setEnabled(true);
			this.ledcutoffSlider.setEnabled(true);
			this.pruneatTextBox.setEnabled(false);
			this.prunetoTextBox.setEnabled(false);
			this.eventsCheck.setEnabled(true);
			this.groupingCheck.setEnabled(true);

			break;
		case 4: // Lexical
			this.wskipvSlider.setValue(100);
			this.wsubvSlider.setValue(100);
			this.wskipeSlider.setValue(0);
			this.ledcutoffSlider.setValue(50);
			this.pruneatTextBox.setText("N/A");
			this.prunetoTextBox.setText("N/A");
			this.eventsCheck.setSelected(false);
			this.groupingCheck.setSelected(true);

			this.wskipvSlider.setEnabled(true);
			this.wsubvSlider.setEnabled(true);
			this.wskipeSlider.setEnabled(false);
			this.ledcutoffSlider.setEnabled(true);
			this.pruneatTextBox.setEnabled(false);
			this.prunetoTextBox.setEnabled(false);
			this.eventsCheck.setEnabled(true);
			this.groupingCheck.setEnabled(false);

			break;
		}
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