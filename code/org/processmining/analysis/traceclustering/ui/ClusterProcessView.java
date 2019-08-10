package org.processmining.analysis.traceclustering.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SlickTabbedPane;
import org.processmining.analysis.traceclustering.model.Cluster;
import org.processmining.analysis.traceclustering.model.ClusterSet;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * @author Minseok Song
 * 
 */
public class ClusterProcessView extends JPanel {

	public ClusterProcessView(ClusterSet clusters) {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		SlickTabbedPane tabbedPane = new SlickTabbedPane();
		// draw model
		for (Cluster cluster : clusters.getClusters()) {
			try {
				AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
				PetriNetResult result = (PetriNetResult) miningPlugin
						.mine(cluster.getFilteredLog());
				JScrollPane scrollPane = new JScrollPane(result
						.getVisualization());
				tabbedPane.addTab(cluster.getName(), scrollPane);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		HeaderBar header = new HeaderBar("Clustering Result Analysis");
		header.setHeight(40);
		this.add(header, BorderLayout.NORTH);
		this.add(tabbedPane, BorderLayout.CENTER);
	}
}
