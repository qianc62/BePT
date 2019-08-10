/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author christian
 * 
 */
public class TraceClusteringMiner extends JPanel implements MiningPlugin {

	protected LogSummary summary;
	protected JComboBox metricsBox;

	protected static String CHOICE_EVENTSET = "Event set overlap (Jaccard's coefficient)";
	protected static String CHOICE_FOLLOWERMATRIX = "Euclidean distance of follower matrices";

	/**
	 * 
	 */
	public TraceClusteringMiner() {
		metricsBox = new JComboBox(new Object[] { CHOICE_FOLLOWERMATRIX,
				CHOICE_EVENTSET });
		metricsBox.setBackground(TraceClusterResult.bgColor);
		metricsBox.setAlignmentX(LEFT_ALIGNMENT);
		metricsBox.setMaximumSize(new Dimension(500, 30));
		this.setBackground(TraceClusterResult.bgColor);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel metricsPanel = new JPanel();
		metricsPanel.setBackground(TraceClusterResult.bgColor);
		metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.Y_AXIS));
		JLabel metricsLabel = new JLabel("Choose trace distance metrics: ");
		metricsLabel.setForeground(TraceClusterResult.fgColor);
		metricsLabel.setBackground(TraceClusterResult.bgColor);
		metricsLabel.setAlignmentX(LEFT_ALIGNMENT);
		metricsPanel.add(Box.createVerticalGlue());
		metricsPanel.add(metricsLabel);
		metricsPanel.add(Box.createVerticalStrut(10));
		metricsPanel.add(metricsBox);
		metricsPanel.add(Box.createVerticalGlue());
		this.add(Box.createHorizontalGlue());
		this.add(metricsPanel);
		this.add(Box.createHorizontalGlue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#getOptionsPanel(org.processmining
	 * .framework.log.LogSummary)
	 */
	public JPanel getOptionsPanel(LogSummary summary) {
		this.summary = summary;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.MiningPlugin#mine(org.processmining.framework
	 * .log.LogReader)
	 */
	public MiningResult mine(LogReader log) {
		TraceStats stats = new TraceStats(log);
		TraceStatsComparator comparator = null;
		String chosenComparator = (String) metricsBox.getSelectedItem();
		if (chosenComparator.equals(CHOICE_EVENTSET)) {
			comparator = new TraceEventSetComparator(stats);
		} else if (chosenComparator.equals(CHOICE_FOLLOWERMATRIX)) {
			comparator = new TraceFollowerMatrixComparator(stats);
		}
		Message.add("Using distance metrics: " + chosenComparator,
				Message.NORMAL);
		TraceClusterSet clusters = new TraceClusterSet(stats, comparator);
		return new TraceClusterResult(log, clusters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Trace Clustering Miner";
	}

}
