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

import weka.clusterers.FarthestFirst;

/**
 * @author Minseok Song
 */
public class FarthestFirstAlgorithm extends WekaAlgorithm {

	public FarthestFirstAlgorithm() {
		super("Farthest First Clustering",
				"Farthest First Clustering allows the user to specify"
						+ " the number of clusters. The algorithm will return"
						+ " the number of clusters which users want.");
		clusters = null;
		clusterer = new FarthestFirst();
	}

	protected void doCluster() {
		try {
			((FarthestFirst) clusterer).setNumClusters(clusterSizeBox
					.getValue());
			((FarthestFirst) clusterer).setSeed(randomSeedBox.getValue());
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
