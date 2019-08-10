package org.processmining.analysis.hierarchicaldatavisualization;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

public class PerformanceMetricsInOntologiesResult extends JPanel implements
		Provider {

	private static final long serialVersionUID = -177135863440325202L;

	private HierarchicalData throughputTimes;
	private HierarchicalData processingTimes;

	private JPanel contentPanel;
	private HierarchicalDataVisualizationResult shownResult = null;

	public PerformanceMetricsInOntologiesResult(
			HierarchicalData processingTimes, HierarchicalData throughputTimes) {
		this.setLayout(new BorderLayout());
		this.processingTimes = processingTimes;
		this.throughputTimes = throughputTimes;

		final JRadioButton selectProcessingTime = new JRadioButton(
				"Show processing times");
		final JRadioButton selectThroughputTime = new JRadioButton(
				"Show throughput times");
		selectProcessingTime.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(selectProcessingTime);
		group.add(selectThroughputTime);

		JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectionPanel.add(selectProcessingTime);
		selectionPanel.add(selectThroughputTime);
		selectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		ActionListener selectionChanged = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(selectProcessingTime.isSelected());
			}
		};
		selectProcessingTime.addActionListener(selectionChanged);
		selectThroughputTime.addActionListener(selectionChanged);

		HeaderBar header = new HeaderBar(
				"Visualize performance metrics in ontologies");
		header.setHeight(40);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(header, BorderLayout.NORTH);
		headerPanel.add(selectionPanel, BorderLayout.SOUTH);

		JPanel divisionPanel = new JPanel(new BorderLayout());
		divisionPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 5, 5, 5), BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED)));

		JPanel headerWithDivision = new JPanel(new BorderLayout());
		headerWithDivision.add(headerPanel, BorderLayout.NORTH);
		headerWithDivision.add(divisionPanel, BorderLayout.SOUTH);

		contentPanel = new JPanel(new BorderLayout());

		this.add(headerWithDivision, BorderLayout.NORTH);
		this.add(contentPanel, BorderLayout.CENTER);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				update(true);
			}
		});
	}

	private void update(boolean showProcessingTimes) {
		contentPanel.removeAll();
		shownResult = new HierarchicalDataVisualizationResult(
				showProcessingTimes ? processingTimes : throughputTimes);
		contentPanel.add(shownResult, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public ProvidedObject[] getProvidedObjects() {
		return shownResult == null ? new ProvidedObject[0] : shownResult
				.getProvidedObjects();
	}
}
