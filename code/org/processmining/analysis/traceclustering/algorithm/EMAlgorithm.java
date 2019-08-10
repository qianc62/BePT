package org.processmining.analysis.traceclustering.algorithm;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyInteger;
import weka.clusterers.EM;

/**
 * @author Minseok Song
 */
public class EMAlgorithm extends WekaAlgorithm {

	protected GUIPropertyInteger maxIterationBox = new GUIPropertyInteger(
			"Max Iterations = ", 100, 1, 1000);

	public EMAlgorithm() {
		super("EM Clustering", "EM Clustering allows the user to specify"
				+ " the number of clusters. The algorithm will return"
				+ " the number of clusters which users want.");
		clusters = null;
		clusterer = new EM();
	}

	protected void doCluster() {
		try {
			((EM) clusterer).setNumClusters(clusterSizeBox.getValue());
			((EM) clusterer).setSeed(randomSeedBox.getValue());
			((EM) clusterer).setMaxIterations(maxIterationBox.getValue());
			clusterer.buildClusterer(data);
			assignInstace();
		} catch (Exception c) {
			Message.add("Weka Error: " + c.toString(), Message.ERROR);
		}
	}

	protected SmoothPanel getMenuPanel() {
		SmoothPanel menuPanel = new SmoothPanel();
		menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.PAGE_AXIS));

		menuPanel.add(clusterSizeBox.getPropertyPanel());
		menuPanel.add(randomSeedBox.getPropertyPanel());
		menuPanel.add(maxIterationBox.getPropertyPanel());

		startButton = new JButton("cluster");
		startButton.setOpaque(false);
		startButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clusterSize = clusterSizeBox.getValue();
				cluster();
			}
		});
		menuPanel.add(startButton);
		return menuPanel;
	}
}
