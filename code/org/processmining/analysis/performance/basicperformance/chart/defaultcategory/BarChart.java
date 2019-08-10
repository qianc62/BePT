package org.processmining.analysis.performance.basicperformance.chart.defaultcategory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;

import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;
import org.jfree.ui.RectangleInsets;

public class BarChart extends DefaultCategoryChart implements ChangeListener {

	protected JSlider zoomSliderX = null; // slider for zooming the view
	protected JSlider zoomSliderY = null; // slider for zooming the view

	public BarChart() {
		super("Bar Chart", "Bar Chart");
	}

	// Bar Chart
	// //////////////////////////////////////////////////////////////////////////////
	protected JScrollPane getGraphPanel() {
		bLegend = true;
		DefaultCategoryDataset dataset;

		if (absPerformance instanceof AbstractPerformance2D) {
			dataset = getDefaultCategoryDataset((AbstractPerformance2D) absPerformance);
		} else {
			dataset = getDefaultCategoryDataset(absPerformance);
		}

		PlotOrientation order = PlotOrientation.VERTICAL;

		String strCategory = absPerformance.getItemName();

		if (absPerformance instanceof AbstractPerformance2D) {
			if (isItemA) {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getSecondItemName();
			} else {
				strCategory = ((AbstractPerformance2D) absPerformance)
						.getItemName();
			}
		}

		if (dimSort.getValue().equals("2D")) {
			chart = ChartFactory.createBarChart(null, strCategory, null,
					dataset, order, bLegend, true, false);
		} else if (dimSort.getValue().equals("3D")) {
			chart = ChartFactory.createBarChart3D(null, strCategory, null,
					dataset, order, bLegend, true, false);
		}

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		CategoryAxis axis = plot.getDomainAxis();
		axis.setLowerMargin(0.03);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setUpperMargin(0.10);

		chartPanel = new ChartPanel(chart, true);
		// added
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		// chartPanel.setMinimumDrawHeight( 10 );
		// chartPanel.setMaximumDrawHeight( 2000 );
		// chartPanel.setMinimumDrawWidth( 20 );
		// chartPanel.setMaximumDrawWidth( 2000 );

		// chart domain axis (hours) allows user to use mouse to zoom in
		// and out by dragging cursor across domain axis
		chartPanel.setMouseZoomable(true);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);

		scrollPane = new JScrollPane(chartPanel);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		JPanel panel = super.getOptionPanel();
		panel.add(dimSort.getPropertyPanel());
		// for zoom slide bar
		zoomSliderX = new JSlider(JSlider.VERTICAL, 0, 2000, 0); // zooms in
		// view
		zoomSliderX.setMaximumSize(new Dimension(50, 90));
		Hashtable zoomLabelTableX = new Hashtable();
		zoomLabelTableX.put(new Integer(0), new JLabel("1"));
		zoomLabelTableX.put(new Integer(1000), new JLabel("10"));
		zoomLabelTableX.put(new Integer(2000), new JLabel("20"));
		zoomSliderX.setLabelTable(zoomLabelTableX);
		zoomSliderX.setMajorTickSpacing(1000);
		zoomSliderX.setMinorTickSpacing(500);
		zoomSliderX.setPaintTicks(true);
		zoomSliderX.setPaintLabels(true);
		zoomSliderX.addChangeListener(this);
		zoomSliderX.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(zoomSliderX);
		/*
		 * ChartPanel panel = new ChartPanel( chart, true );
		 * panel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
		 * panel.setMinimumDrawHeight( 10 ); panel.setMaximumDrawHeight( 2000 );
		 * panel.setMinimumDrawWidth( 20 ); panel.setMaximumDrawWidth( 2000 );
		 * 
		 * // chart domain axis (hours) allows user to use mouse to zoom in //
		 * and out by dragging cursor across domain axis panel.setMouseZoomable(
		 * true ); panel.setDomainZoomable( true ); panel.setRangeZoomable( true
		 * );
		 */
		return panel;
	}

	/*
	 * // Listener for GUI /* (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		// handle zoom events
		if ((e.getSource() == zoomSliderX)) {
			double zoom = Math
					.max(1.0, (double) zoomSliderX.getValue() / 100.0);
			System.out.println("zoom = " + zoom);
			System.out.println("ori width = " + chartPanel.getWidth());
			int updWidth = (int) ((double) scrollPane.getWidth() * zoom);

			Dimension dim = new Dimension(updWidth, chartPanel.getHeight());
			chartPanel.setMinimumDrawWidth(updWidth);

			chartPanel.setPreferredSize(dim);
			chartPanel.validate();
			chartPanel.revalidate();
			chart.createBufferedImage(updWidth, chartPanel.getHeight());
			chartPanel.setRefreshBuffer(true);
			CategoryPlot plot = chart.getCategoryPlot();

			// NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			// rangeAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
			// rangeAxis.setUpperMargin(0.15);

			CategoryAxis axis = plot.getDomainAxis(); // Ⱦ�� ��ü ���ϱ�
			axis.setLabelFont(new Font("SansSerif", Font.PLAIN,
					(10 - updWidth / 300)));
			axis.setLowerMargin(0.03 / zoom);
			axis.setUpperMargin(0.03 / zoom);
			// axis.setLowerMargin(0.03); // Ⱦ���� ���� ���ʰ� ���� ���� ������
			// ����
			// axis.setUpperMargin(0.03); // Ⱦ���� ���� �����ʰ� ���� ������
			// ������ ����
			// axis.setCategoryLabelPositions
			// (CategoryLabelPositions.createUpRotationLabelPositions(Math.PI /
			// 6.0));
		}
	}
}
