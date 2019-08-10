package org.processmining.analysis.performance.executiontimes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalData;
import org.processmining.analysis.hierarchicaldatavisualization.HierarchicalDataVisualizationResult;
import org.processmining.analysis.originatoravailability.OriginatorAvailability;
import org.processmining.analysis.originatoravailability.ShiftTimeIntervals;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;

public class ExecutionTimesResultUI extends JPanel implements Provider {

	private static final String USE_ORIGINATOR_ONTOLOGY = "Use originator ontology";

	private static final String USE_TASK_ONTOLOGY = "Use task ontology";

	private static final String DON_T_USE_ONTOLOGIES = "Don't use ontologies";

	private static final long serialVersionUID = 1L;

	private LogReader log = null;

	private JPanel contentPanel;
	private SpinnerNumberModel hoursPerShift = null;
	private JPanel timeShiftPanel = null;
	private JButton start = null;
	private ExecutionTimesResult executionTimesresult = null;
	private JComboBox stat;
	private MeasurementModel model;
	private TableModel taskModel;
	protected TableModel availabilityModel;
	protected HierarchicalDataVisualizationResult hierarchicalDataVisualization;
	private JComboBox useOntologies;

	public ExecutionTimesResultUI(LogReader log) {
		this.log = log;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {
		JLabel hoursPerShiftLabel = new JLabel(" Hours per Shift ");
		hoursPerShiftLabel.setToolTipText("Number of hours per shift.");

		hoursPerShift = new SpinnerNumberModel(6, 1, 24, 1);
		JSpinner hoursPerShiftSpinner = new JSpinner(hoursPerShift);
		hoursPerShiftSpinner.setToolTipText("Number of hours per shift.");

		useOntologies = new JComboBox(new String[] { DON_T_USE_ONTOLOGIES,
				USE_TASK_ONTOLOGY, USE_ORIGINATOR_ONTOLOGY });

		start = new JButton("Calculate");
		start.setToolTipText("Start calculating the execution times of tasks.");

		timeShiftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		timeShiftPanel.add(hoursPerShiftLabel);
		timeShiftPanel.add(hoursPerShiftSpinner);
		timeShiftPanel.add(useOntologies);
		timeShiftPanel.add(start);

		start.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				OriginatorAvailability availability = new OriginatorAvailability(
						log, new ShiftTimeIntervals(hoursPerShift.getNumber()
								.intValue()));

				availabilityModel = availability.toTableModel();
				executionTimesresult = new ExecutionTimesResult(log,
						availability, USE_TASK_ONTOLOGY.equals(useOntologies
								.getSelectedItem()), USE_ORIGINATOR_ONTOLOGY
								.equals(useOntologies.getSelectedItem()));

				stat = new JComboBox(new String[] { "Average", "Sum",
						"Frequency", "Standard deviation", "Variance",
						"Minimum", "Maximum" });
				stat.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						updateTable();
					}
				});

				taskModel = executionTimesresult.getTaskTableModel();
				model = executionTimesresult.getMeasurementTableModel();

				JTable taskTable = new JTable(taskModel);
				JTable table = new JTable(model);

				for (int i = 1; i < table.getColumnCount(); i++) {
					DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
					table.getColumnModel().getColumn(i).setCellRenderer(
							renderer);
				}

				for (int i = 1; i < taskTable.getColumnCount(); i++) {
					DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
					taskTable.getColumnModel().getColumn(i).setCellRenderer(
							renderer);
				}

				JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				statsPanel.add(new JLabel("Show (seconds):"));
				statsPanel.add(stat);

				JPanel measurementsPanel = new JPanel(new BorderLayout());
				measurementsPanel.add(statsPanel, BorderLayout.NORTH);
				measurementsPanel.add(new JScrollPane(table),
						BorderLayout.CENTER);

				JTable availabilityTable = new JTable(availabilityModel);
				for (int i = 1; i < availabilityTable.getColumnCount(); i++) {
					DefaultTableCellRenderer renderer = new ColoredCellRenderer();

					renderer.setHorizontalAlignment(SwingConstants.RIGHT);
					availabilityTable.getColumnModel().getColumn(i)
							.setCellRenderer(renderer);
				}

				hierarchicalDataVisualization = new HierarchicalDataVisualizationResult(
						createHierarchicalData(executionTimesresult
								.getRawMeasurements(), USE_TASK_ONTOLOGY
								.equals(useOntologies.getSelectedItem()),
								USE_ORIGINATOR_ONTOLOGY.equals(useOntologies
										.getSelectedItem())));

				JTabbedPane tabs = new JTabbedPane();
				tabs.addTab("Graphical View of Execution Times",
						hierarchicalDataVisualization);
				tabs.addTab("Originator vs Task", measurementsPanel);
				tabs.addTab("Task Statistics", new JScrollPane(taskTable));
				tabs.addTab("Originator Availability", new JScrollPane(
						availabilityTable));

				contentPanel.removeAll();
				contentPanel.add(tabs, BorderLayout.CENTER);
				contentPanel.repaint();
				contentPanel.validate();
			}
		});

		HeaderBar header = new HeaderBar(ExecutionTimesPlugin.NAME);
		header.setHeight(40);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(header, BorderLayout.NORTH);
		headerPanel.add(timeShiftPanel, BorderLayout.SOUTH);

		JPanel divisionPanel = new JPanel(new BorderLayout());
		divisionPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 5, 5, 5), BorderFactory
						.createEtchedBorder(EtchedBorder.LOWERED)));

		JPanel headerWithDivision = new JPanel(new BorderLayout());
		headerWithDivision.add(headerPanel, BorderLayout.NORTH);
		headerWithDivision.add(divisionPanel, BorderLayout.SOUTH);

		contentPanel = new JPanel(new BorderLayout());

		this.setLayout(new BorderLayout());
		this.add(headerWithDivision, BorderLayout.NORTH);
		this.add(contentPanel, BorderLayout.CENTER);
	}

	protected HierarchicalData createHierarchicalData(
			ExecutionTimes executionTimes, boolean useTaskOntologies,
			boolean useOriginatorOntologies) {
		return new HierarchicalExecutionTimesInOntologies(log, executionTimes,
				useTaskOntologies, useOriginatorOntologies);
	}

	public ProvidedObject[] getProvidedObjects() {
		List<ProvidedObject> objects = new ArrayList<ProvidedObject>();

		if (hierarchicalDataVisualization != null) {
			ProvidedObject[] hdv = hierarchicalDataVisualization
					.getProvidedObjects();

			if (hdv.length > 0) {
				objects.add(new ProvidedObject("Table with raw numbers", hdv[0]
						.getObjects()));
			}
		}
		if (stat != null && stat.getSelectedItem() != null && model != null) {
			objects.add(new ProvidedObject(stat.getSelectedItem()
					+ " of Originator vs Task Matrix", model));
		}
		if (taskModel != null) {
			objects
					.add(new ProvidedObject("Task Statistics Matrix", taskModel));
		}
		if (availabilityModel != null) {
			objects.add(new ProvidedObject("Originator Availability",
					availabilityModel));
		}
		return objects.toArray(new ProvidedObject[0]);
	}

	protected void updateTable() {
		if (stat.getSelectedItem().equals("Average")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getMean();
				}
			});
		} else if (stat.getSelectedItem().equals("Sum")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getSum();
				}
			});
		} else if (stat.getSelectedItem().equals("Frequency")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getN();
				}
			});
		} else if (stat.getSelectedItem().equals("Standard deviation")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getStandardDeviation();
				}
			});
		} else if (stat.getSelectedItem().equals("Variance")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getVariance();
				}
			});
		} else if (stat.getSelectedItem().equals("Minimum")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getMin();
				}
			});
		} else if (stat.getSelectedItem().equals("Maximum")) {
			model.setStatistic(new StatisticFunction() {
				public double getValue(SummaryStatistics stats) {
					return stats.getMax();
				}
			});
		}
	}
}

class ColoredCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8540474103226881230L;

	public ColoredCellRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		setText(value.toString());
		setForeground(table.getForeground());
		setBackground(value.toString().equals("true") ? new Color(0, 0xFF, 0)
				: new Color(0xFF, 0xFF, 0xFF));
		return this;
	}
}
