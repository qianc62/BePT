package org.processmining.analysis.socialsuccess.ui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.ui.summary.SummaryUI;

public class SpiderWebUI extends JComponent {
	private static final long serialVersionUID = -7012931195422166004L;
	private BigFive bigFive;
	protected SummaryUI parent = null;
	private JPanel chartPanel;

	public SpiderWebUI(BigFive bf) {
		bigFive = bf;
		// lijst met gebruikers links.
		LogPreviewUI();
	}

	public void LogPreviewUI() {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		chartPanel = new JPanel(new BorderLayout());
		// assemble GUI
		this.add(chartPanel);
	}

	protected void instancesSelectionChanged(String[] users) {

		if (users.length > 0) {
			chartPanel.removeAll();
			chartPanel.add(new ChartPanel(createChart(createDataset(users))));
			chartPanel.revalidate();
			chartPanel.repaint();
			revalidate();
			repaint();
		}
	}

	protected JFreeChart createChart(CategoryDataset data) {
		// get a reference to the plot for further customisation...
		SpiderWebPlot plot = new SpiderWebPlot(data);
		plot.setMaxValue(BigFive.additionConst * 5);
		JFreeChart chart = new JFreeChart("Personality Mapping",
				TextTitle.DEFAULT_FONT, plot, false);

		LegendTitle legend = new LegendTitle(plot);
		legend.setPosition(RectangleEdge.BOTTOM);
		chart.addSubtitle(legend);
		return chart;
	}

	private CategoryDataset createDataset(String[] users) {
		// create the dataset...
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(BigFive.additionConst, "null", "Openness");
		dataset.addValue(BigFive.additionConst, "null", "Conscientiousness");
		dataset.addValue(BigFive.additionConst, "null", "Extraversion");
		dataset.addValue(BigFive.additionConst, "null", "Agreeableness");
		dataset.addValue(BigFive.additionConst, "null", "Neurotics");

		for (int i = 0; i < users.length; i++) {
			String user = users[i];
			Vector<Double> d = bigFive.getResults(user);
			dataset.addValue(d.get(0), user, "Openness");
			dataset.addValue(d.get(1), user, "Conscientiousness");
			dataset.addValue(d.get(2), user, "Extraversion");
			dataset.addValue(d.get(3), user, "Agreeableness");
			dataset.addValue(d.get(4), user, "Neurotics");
		}

		return dataset;
	}
}
