/**
 * 
 */
package org.processmining.mining.traceclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.mining.MiningResult;
import org.processmining.mining.traceclustering.TraceClusterSet.TraceCluster;
import org.processmining.mining.traceclustering.TraceStats.SingleTraceStat;

/**
 * @author christian
 * 
 */
public class TraceClusterResult implements MiningResult, Provider {

	protected LogReader log;
	protected TraceClusterSet clusters;
	protected JScrollPane graphScrollPane;
	protected JSlider slider;
	protected JPanel panel;
	protected JLabel diameterLabel;
	protected static DecimalFormat format = new DecimalFormat("0.0000");
	protected static Color bgColor = new Color(160, 160, 160);
	protected static Color fgColor = new Color(50, 50, 50);

	public TraceClusterResult(LogReader log, TraceClusterSet clusterSet) {
		this.log = log;
		this.clusters = clusterSet;
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		graphScrollPane = new JScrollPane(new JPanel());
		panel.add(graphScrollPane, BorderLayout.CENTER);
		JPanel sliderPanel = new JPanel();
		sliderPanel.setBackground(bgColor);
		sliderPanel.setLayout(new BorderLayout());
		sliderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.add(sliderPanel, BorderLayout.EAST);
		JLabel headerLabel = new JLabel("Diameter limit");
		headerLabel.setBackground(bgColor);
		headerLabel.setForeground(fgColor);
		sliderPanel.add(headerLabel, BorderLayout.NORTH);
		slider = new JSlider(JSlider.VERTICAL, 0, 10000, 8000);
		slider.setBackground(bgColor);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double threshold = (double) slider.getValue() / 10000.0;
				diameterLabel.setText("  < " + format.format(threshold) + "  ");
				if (slider.getValueIsAdjusting() == false) {
					Message.add("Re-clustering with threshold " + threshold
							+ "...", Message.NORMAL);
					clusters.cluster(threshold);
					try {
						graphScrollPane.getViewport().setView(
								clusters.getGraphPanel());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		sliderPanel.add(slider, BorderLayout.CENTER);
		diameterLabel = new JLabel("  < 0.8000  ");
		diameterLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		diameterLabel.setBackground(bgColor);
		diameterLabel.setForeground(fgColor);
		sliderPanel.add(diameterLabel, BorderLayout.SOUTH);
		try {
			clusters.cluster(0.8);
			graphScrollPane.getViewport().setView(clusters.getGraphPanel());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		// add graph in dot format
		ProvidedObject[] objects = new ProvidedObject[clusters.size() + 2];
		objects[0] = new ProvidedObject("Trace cluster graph",
				new Object[] { new DotFileWriter() {
					public void writeToDot(Writer bw) throws IOException {
						clusters.writeToDot(bw);
					}
				} });
		int index = 1;
		// add all currently displayed clusters
		for (TraceCluster cluster : clusters.getClusters()) {
			int pitk[] = new int[cluster.size()];
			int piIndex = 0;
			for (SingleTraceStat stat : cluster.getTraces()) {
				pitk[piIndex] = stat.getTraceIndex();
				piIndex++;
			}
			try {
				objects[index] = new ProvidedObject("Trace cluster "
						+ (index - 1), new Object[] { LogReaderFactory
						.createInstance(log, pitk) });
			} catch (Exception e) {
				e.printStackTrace();
			}
			index++;
		}
		// add complete log
		objects[index] = new ProvidedObject("Complete log (all clusters)",
				new Object[] { log });
		return objects;
	}

}
