package org.processmining.analysis.performance.advanceddottedchartanalysis.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.analysis.performance.advanceddottedchartanalysis.model.DottedChartModel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.DottedChartAnalysis;

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
public class SettingPanel extends JPanel {

	private static final long serialVersionUID = -2720292789575028505L;

	protected JLabel colorsettingLabel = new JLabel(
			"Change colors by pressing buttons");
	protected ColorReference colorReference;
	private JPanel colorPanel;
	private JScrollPane colorScrollPane;
	private JPanel colorMainPanel;

	private DottedChartAnalysis dottedChartAnalysis;
	private DottedChartPanel dottedChartPanel;
	private DottedChartModel dcModel;

	// for color reference
	private JLabel[] tempLable;
	private JButton[] tempButton;
	private JPanel colorChoosePanel;
	private JPanel colorMPanel;

	// private JTextField colorFile = new JTextField();
	// private JButton chooseColorButton = new JButton();
	private JButton randomizeColorButton = new JButton();
	private JLabel fbackColorLabel;
	private JLabel sbackColorLabel;
	private JButton fbackColorButton = new JButton();
	private JButton sbackColorButton = new JButton();

	private Color colorLogDark = new Color(170, 170, 160);
	private Color colorLogBright = new Color(210, 210, 200);

	public SettingPanel(DottedChartAnalysis aDottedChartAnalysis) {
		dottedChartAnalysis = aDottedChartAnalysis;
		dottedChartPanel = aDottedChartAnalysis.getDottedChartPanel();
		dcModel = aDottedChartAnalysis.getDottedChartModel();
	}

	public void initSettingPanel() {
		if (dottedChartPanel == null) {
			dottedChartPanel = dottedChartAnalysis.getDottedChartPanel();
			dcModel = dottedChartAnalysis.getDottedChartModel();
		}
		this.removeAll();
		// event panel
		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0, BoxLayout.PAGE_AXIS));
		p0.add(Box.createRigidArea(new Dimension(5, 10)));

		// init paint panel
		initColorPanel();
	}

	// method dealing with color
	private void initColorPanel() {

		colorReference = dottedChartPanel.getColorReference();

		String type = dottedChartAnalysis.getDottedChartOptionPanel()
				.getColorStandard();

		if (type.equals(DottedChartModel.STR_NONE))
			return;
		ArrayList<String> keySet = dcModel.getItemArrayList(type);

		colorPanel = new JPanel(new GridLayout(keySet.size(), 1));
		colorPanel.setPreferredSize(new Dimension(250, keySet.size() * 15));
		colorPanel.setMaximumSize(new Dimension(250, keySet.size() * 15));

		tempLable = new JLabel[keySet.size()];
		tempButton = new JButton[keySet.size()];

		int i = 0;
		for (Iterator<String> itr = keySet.iterator(); itr.hasNext();) {
			String tempString = itr.next();
			tempLable[i] = new JLabel(tempString + ":");
			tempLable[i].setToolTipText(tempString);
			tempButton[i] = new JButton("push to change");
			tempButton[i].setForeground(colorReference.getColor(tempString));
			tempButton[i].setToolTipText(tempString);
			tempButton[i].setActionCommand(tempString);
			tempButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						tempButton.setForeground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
					}
				}
			});
			colorChoosePanel = new JPanel(new GridLayout(1, 2));
			colorChoosePanel.add(tempLable[i]);
			colorChoosePanel.add(tempButton[i]);
			colorPanel.add(colorChoosePanel);
			i++;
		}
		colorMPanel = null;
		colorMPanel = new JPanel();
		colorMPanel.setLayout(new BoxLayout(colorMPanel, BoxLayout.Y_AXIS));
		colorMPanel.add(colorPanel);
		colorScrollPane = new JScrollPane(colorMPanel);
		colorScrollPane.setPreferredSize(new Dimension(260, 410));
		colorMainPanel = new JPanel();
		colorMainPanel
				.setLayout(new BoxLayout(colorMainPanel, BoxLayout.Y_AXIS));
		JLabel changeColorLabel = new JLabel("           Set colors");
		colorMainPanel.add(changeColorLabel);
		colorMainPanel.add(colorScrollPane);

		sbackColorLabel = new JLabel("First Background");
		sbackColorLabel.setToolTipText("First Background");
		sbackColorButton = new JButton("push to change");
		sbackColorButton.setForeground(colorLogBright);
		sbackColorButton.setToolTipText("First Background");
		sbackColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton tempButton = (JButton) e.getSource();
				Color newColor = JColorChooser.showDialog(tempButton,
						"Choose Background Color", tempButton.getBackground());
				if (newColor != null) {
					tempButton.setForeground(newColor);
					colorLogBright = newColor;
				}
			}
		});
		fbackColorLabel = new JLabel("Second Background");
		fbackColorLabel.setToolTipText("Second Background");
		fbackColorButton = new JButton("push to change");
		fbackColorButton.setForeground(colorLogDark);
		fbackColorButton.setToolTipText("Second Background");
		fbackColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton tempButton = (JButton) e.getSource();
				Color newColor = JColorChooser.showDialog(tempButton,
						"Choose Background Color", tempButton.getBackground());
				if (newColor != null) {
					tempButton.setForeground(newColor);
					colorLogDark = newColor;
				}
			}
		});
		JPanel tempPanel = new JPanel(new GridLayout(1, 2));
		tempPanel.add(sbackColorLabel);
		tempPanel.add(sbackColorButton);
		tempPanel.add(fbackColorLabel);
		tempPanel.add(fbackColorButton);
		colorMainPanel.add(tempPanel);

		randomizeColorButton.setMaximumSize(new Dimension(160, 25));
		randomizeColorButton.setMinimumSize(new Dimension(160, 25));
		randomizeColorButton.setPreferredSize(new Dimension(160, 25));
		randomizeColorButton.setActionCommand("");
		randomizeColorButton.setText("Randomize Colors");
		randomizeColorButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						randomizeColor(e);
					}
				});
		colorMainPanel.add(randomizeColorButton);

		// todo for color
		// colorFile.setMinimumSize(new Dimension(150, 21));
		// colorFile.setPreferredSize(new Dimension(150, 21));
		// colorFile.setEditable(false);
		// colorMainPanel.add(colorFile);
		// chooseColorButton.setMaximumSize(new Dimension(120, 25));
		// chooseColorButton.setMinimumSize(new Dimension(120, 25));
		// chooseColorButton.setPreferredSize(new Dimension(120, 25));
		// chooseColorButton.setActionCommand("");
		// chooseColorButton.setText("Browse...");
		// chooseColorButton.addActionListener(new
		// java.awt.event.ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// chooseColorButton_actionPerformed(e);
		// }
		// });
		//
		// colorMainPanel.add(chooseColorButton);

		this.add(colorMainPanel);
	}

	private void redrawColorPart() {
		String type = dottedChartAnalysis.getDottedChartOptionPanel()
				.getColorStandard();
		colorMPanel.remove(colorPanel);
		this.remove(colorMainPanel);

		if (type.equals(DottedChartModel.STR_NONE))
			return;
		ArrayList<String> keySet = dcModel.getItemArrayList(type);

		colorPanel = null;
		colorPanel = new JPanel(new GridLayout(keySet.size(), 1));
		colorPanel.setPreferredSize(new Dimension(250, keySet.size() * 15));
		colorPanel.setMaximumSize(new Dimension(250, keySet.size() * 15));

		int i = 0;
		for (Iterator<String> itr = keySet.iterator(); itr.hasNext();) {
			String tempString = itr.next();
			tempLable[i] = new JLabel(tempString + ":");
			tempLable[i].setToolTipText(tempString);
			tempButton[i] = new JButton("push to change");
			tempButton[i].setForeground(colorReference.getColor(tempString));
			tempButton[i].setToolTipText(tempString);
			tempButton[i].setActionCommand(tempString);
			tempButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton tempButton = (JButton) e.getSource();
					Color newColor = JColorChooser.showDialog(tempButton,
							"Choose Background Color", tempButton
									.getBackground());
					if (newColor != null) {
						tempButton.setBackground(newColor);
						assignColor(tempButton.getActionCommand(), newColor);
					}
				}
			});
			colorChoosePanel = new JPanel(new GridLayout(1, 2));
			colorChoosePanel.add(tempLable[i]);
			colorChoosePanel.add(tempButton[i]);
			colorPanel.add(colorChoosePanel);
			i++;
		}
		colorMPanel.add(colorPanel);
		colorMPanel = null;
		colorMPanel = new JPanel();
		colorMPanel.setLayout(new BoxLayout(colorMPanel, BoxLayout.Y_AXIS));
		colorMPanel.add(colorPanel);
		colorMPanel.repaint();
		colorMPanel.revalidate();
		colorScrollPane = new JScrollPane(colorMPanel);
		colorScrollPane.setPreferredSize(new Dimension(260, 410));
		colorMainPanel = null;
		colorMainPanel = new JPanel();
		colorMainPanel
				.setLayout(new BoxLayout(colorMainPanel, BoxLayout.Y_AXIS));
		JLabel changeColorLabel = new JLabel("           Set colors");
		colorMainPanel.add(changeColorLabel);
		colorMainPanel.add(colorScrollPane);

		JPanel tempPanel = new JPanel(new GridLayout(1, 2));
		tempPanel.add(sbackColorLabel);
		tempPanel.add(sbackColorButton);
		tempPanel.add(fbackColorLabel);
		tempPanel.add(fbackColorButton);
		colorMainPanel.add(tempPanel);
		colorMainPanel.add(randomizeColorButton);

		// colorMainPanel.add(colorFile);
		// colorMainPanel.add(chooseColorButton);

		this.add(colorMainPanel);
		this.repaint();
		this.revalidate();
	}

	private void randomizeColor(ActionEvent e) {
		ArrayList<String> keySet = dcModel.getItemArrayList(dottedChartAnalysis
				.getDottedChartOptionPanel().getColorStandard());
		for (String name : keySet) {
			colorReference.randomizeColor(name);
		}
		redrawColorPart();
	}

	// private void chooseColorButton_actionPerformed(ActionEvent e) {
	// JFileChooser chooser = new JFileChooser();
	//
	// chooser.setFileFilter(new GenericMultipleExtFilter(new String[] {"xml"}
	// , "XML file (*.xml)"));
	// if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	// String name = chooser.getSelectedFile().getPath();
	// setChosenXMLFile(name);
	// colorReference.readFile(name);
	// redrawColorPart();
	// }
	// }
	// private void setChosenXMLFile(String logFileName) {
	// colorFile.setText(logFileName);
	// }

	public Color getFBcolor() {
		return colorLogDark;
	}

	public Color getSBcolor() {
		return colorLogBright;
	}

	public void assignColor(String name, Color newColor) {
		colorReference.assignColor(name, newColor);
	}

	public void changeColorPanel() {
		if (colorMainPanel != null)
			this.remove(colorMainPanel);
		colorMainPanel = null;
		initColorPanel();
		this.repaint();
	}
}
