package org.processmining.analysis.logclustering.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.analysis.logclustering.profiles.Profile;

/**
 * @author Minseok Song
 * 
 */
public class ProfileConfigurationUI extends JPanel implements ChangeListener {

	protected static DecimalFormat format = new DecimalFormat("0.000");
	protected static Color COLOR_UNARY = new Color(107, 59, 50);
	protected static Color COLOR_TRACE = new Color(54, 69, 111);
	protected static Color COLOR_BINSIG = new Color(112, 104, 61);
	protected static Color COLOR_BINCOR = new Color(80, 95, 66);
	protected static Color COLOR_FG = new Color(190, 190, 190);

	protected JCheckBox activeBox;
	protected JCheckBox invertBox;
	protected JSlider slider;
	protected JLabel weightLabel;
	protected double rememberWeight;
	protected Profile profile;

	// protected String profileType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public ProfileConfigurationUI(Profile aMetric) {
		profile = aMetric;
		this.setBackground(COLOR_BINSIG);
		/*
		 * if(profile instanceof TraceMetric) { this.setBackground(COLOR_TRACE);
		 * profileType = "trace profile"; } else if(profile instanceof
		 * UnaryMetric) { this.setBackground(COLOR_UNARY); profileType =
		 * "unary profile"; } else if(profile instanceof
		 * SignificanceBinaryLogMetric || profile instanceof
		 * SignificanceBinaryDerivateMetric) { this.setBackground(COLOR_BINSIG);
		 * profileType = "binary significance profile"; } else if(profile
		 * instanceof CorrelationBinaryLogMetric) {
		 * this.setBackground(COLOR_BINCOR); profileType =
		 * "binary correlation profile"; }
		 */
		this.setToolTipText(profile.getDescription());
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLineBorder(new Color(60, 60, 60), 1), BorderFactory
				.createEmptyBorder(15, 15, 15, 15)));
		this.setMinimumSize(new Dimension(500, 70));
		this.setMaximumSize(new Dimension(1000, 120));
		JLabel profileLabel = new JLabel(profile.getName());
		profileLabel.setOpaque(false);
		profileLabel.setForeground(new Color(240, 240, 240));
		activeBox = new JCheckBox("active");
		activeBox.setOpaque(false);
		activeBox.setSelected(profile.getNormalizationMaximum() > 0.0);
		activeBox.addChangeListener(this);
		activeBox.setForeground(COLOR_FG);
		invertBox = new JCheckBox("invert");
		invertBox.setOpaque(false);
		invertBox.setSelected(profile.getInvert());
		invertBox.addChangeListener(this);
		invertBox.setForeground(COLOR_FG);
		slider = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) (profile
				.getNormalizationMaximum() * 1000));
		slider.setOpaque(false);
		slider.addChangeListener(this);
		JLabel sliderLabel = new JLabel("weight:");
		sliderLabel.setOpaque(false);
		sliderLabel.setForeground(COLOR_FG);
		weightLabel = new JLabel(format.format(profile
				.getNormalizationMaximum()));
		weightLabel.setOpaque(false);
		weightLabel.setForeground(COLOR_FG);
		JPanel upperPanel = new JPanel();
		upperPanel.setOpaque(false);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		upperPanel.add(profileLabel);
		upperPanel.add(Box.createHorizontalGlue());
		upperPanel.add(invertBox);
		upperPanel.add(Box.createHorizontalStrut(10));
		upperPanel.add(activeBox);
		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(false);
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(sliderLabel, BorderLayout.WEST);
		lowerPanel.add(slider, BorderLayout.CENTER);
		lowerPanel.add(weightLabel, BorderLayout.EAST);
		JLabel descrLabel = new JLabel(profile.getDescription());
		descrLabel.setAlignmentX(LEFT_ALIGNMENT);
		descrLabel.setForeground(COLOR_FG);
		descrLabel.setOpaque(false);
		descrLabel.setFont(descrLabel.getFont().deriveFont(10.0f).deriveFont(
				Font.ITALIC));
		JPanel midPanel = new JPanel();
		midPanel.setOpaque(false);
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
		midPanel.add(descrLabel);
		midPanel.add(Box.createHorizontalGlue());
		this.add(upperPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(midPanel);
		this.add(Box.createVerticalStrut(4));
		this.add(lowerPanel);
		this.add(Box.createVerticalGlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			profile.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == activeBox) {
			if (activeBox.isSelected() == true) {
				slider.setEnabled(true);
				profile
						.setNormalizationMaximum((double) slider.getValue() / 1000.0);
			} else {
				slider.setEnabled(false);
				rememberWeight = profile.getNormalizationMaximum();
				profile.setNormalizationMaximum(0.0);
			}
			repaint();
		} else if (evt.getSource() == invertBox) {
			profile.setInvert(invertBox.isSelected());
			repaint();
		} else if (evt.getSource() == slider) {
			double value = (double) slider.getValue() / 1000.0;
			profile.setNormalizationMaximum(value);
			weightLabel.setText(format.format(value));
			repaint();
		}
	}

}
