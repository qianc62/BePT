package org.processmining.mining.organizationmining.ui;

import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.mining.organizationmining.OrgMinerOptions;

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
 * @author Minseok Song
 * @version 1.0
 */

public class SimilarTaskPanel extends JPanel {

	// private GuiPropertyListRadio doingSimilarTask;
	private GUIPropertyListEnumeration doingSimilarTask;
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private JRadioButton stEuclidianDistance = new JRadioButton();
	private JRadioButton stCorrelationCoefficient = new JRadioButton();
	private JRadioButton stSimilarityCoefficient = new JRadioButton();
	private JRadioButton stHammingDistance = new JRadioButton();

	public SimilarTaskPanel() {
		init();
	}

	private void jbInit() throws Exception {

	}

	private void init() {
		// ButtonGroup similarTaskGroup = new ButtonGroup();

		// ----------- Similar task -----------------------------------

		JPanel testPanel = new JPanel();
		// testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS));
		ArrayList<String> values = new ArrayList<String>();
		values.add("Correlation coefficient");
		values.add("Euclidian distance");
		values.add("Similarity coefficient");
		values.add("Hamming distance");
		doingSimilarTask = new GUIPropertyListEnumeration(
				"Doing Similar Task Options", values);
		testPanel.add(doingSimilarTask.getPropertyPanel());
		this.add(testPanel);

		/*
		 * JPanel testPanel = new Panel(); // create parent panel <br>
		 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS));
		 * <br> ArrayList<String> values = new ArrayList<String>();
		 * values.add("Male"); values.add("Female"); GUIPropertyListEnumeration
		 * gender = new GUIPropertyListEnumeration("Gender", values); <br>
		 * testPanel.add(gender.getPropertyPanel()); // add one property <br>
		 * return testPanel; <br>
		 */
		// this.add(doingSimilarTask.getPropertyPanel());
		/*
		 * this.setLayout(gridBagLayout4);
		 * stEuclidianDistance.setText("Euclidian distance");
		 * stCorrelationCoefficient.setText("Correlation coefficient");
		 * stSimilarityCoefficient.setText("Similarity coefficient");
		 * stHammingDistance.setText("Hamming distance");
		 * stEuclidianDistance.setSelected(true); this.add(stEuclidianDistance,
		 * new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0 ,
		 * GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0,
		 * 0), 0, 0)); this.add(stCorrelationCoefficient, new
		 * GridBagConstraints(0, 1, 1, 1, 0.0, 0.0 , GridBagConstraints.WEST,
		 * GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		 * this.add(stSimilarityCoefficient, new GridBagConstraints(0, 2, 1, 1,
		 * 0.0, 0.0 , GridBagConstraints.WEST, GridBagConstraints.NONE, new
		 * Insets(0, 0, 0, 0), 0, 0)); this.add(stHammingDistance, new
		 * GridBagConstraints(0, 3, 1, 1, 0.0, 0.0 , GridBagConstraints.WEST,
		 * GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		 * similarTaskGroup.add(stEuclidianDistance);
		 * similarTaskGroup.add(stCorrelationCoefficient);
		 * similarTaskGroup.add(stSimilarityCoefficient);
		 * similarTaskGroup.add(stHammingDistance);
		 */
	}

	public int getSimilarTaskSetting() {
		String st = doingSimilarTask.getValue().toString();
		System.out.println(st);
		if (st.equals("Euclidian distance"))
			return OrgMinerOptions.EUCLIDIAN_DISTANCE;
		else if (st.equals("Correlation coefficient"))
			return OrgMinerOptions.CORRELATION_COEFFICIENT;
		else if (st.equals("Similarity coefficient"))
			return OrgMinerOptions.SIMILARITY_COEFFICIENT;
		else
			return OrgMinerOptions.HAMMING_DISTANCE;
		/*
		 * if (getEuclidianDistance())return OrgMinerOptions.EUCLIDIAN_DISTANCE;
		 * else if (getCorrelationCoefficient())return
		 * OrgMinerOptions.CORRELATION_COEFFICIENT; else if
		 * (getSimilarityCoefficient())return
		 * OrgMinerOptions.SIMILARITY_COEFFICIENT; else return
		 * OrgMinerOptions.HAMMING_DISTANCE;
		 */

	}

	public boolean getEuclidianDistance() {
		return stEuclidianDistance.isSelected();
	}

	public boolean getCorrelationCoefficient() {
		return stCorrelationCoefficient.isSelected();
	}

	public boolean getSimilarityCoefficient() {
		return stSimilarityCoefficient.isSelected();
	}
}
