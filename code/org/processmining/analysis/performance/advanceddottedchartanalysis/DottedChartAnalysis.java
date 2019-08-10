package org.processmining.analysis.performance.advanceddottedchartanalysis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.processmining.analysis.performance.advanceddottedchartanalysis.model.DottedChartModel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.DottedChartPanel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.DottedChartOptionPanel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.OverviewPanel;
import org.processmining.analysis.performance.advanceddottedchartanalysis.ui.SettingPanel;

import org.processmining.framework.log.LogReader;

/**
 * Dotted chart analysis
 * 
 * @author Minseok Song
 */
public class DottedChartAnalysis extends JPanel implements ActionListener {

	private static final long serialVersionUID = -2720292789575028504L;

	// final attributes
	final LogReader inputLog;
	private DottedChartModel dcModel;

	// GUI attributes
	private JPanel centerPanel = new JPanel();
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JSplitPane sp = new JSplitPane();
	private JSplitPane split = new JSplitPane();
	private JSplitPane spom = new JSplitPane();
	private OverviewPanel ovPanel = null;
	private DottedChartPanel dottedChartPanel = null;
	private DottedChartOptionPanel dottedChartOptionPanel = null;
	private JScrollPane chartPane;
	private JScrollPane metricsPane;
	private SettingPanel settingPanel;

	public DottedChartAnalysis(LogReader log) {
		inputLog = log;
		dcModel = new DottedChartModel(inputLog);
		analyse();
	}

	/**
	 * Creates thread in which the log relations are mined out of the used log
	 */
	public void analyse() {
		jbInit();
	}

	/**
	 * Actually builds the GUI
	 */
	private void jbInit() {

		int height = this.getHeight() - this.getInsets().bottom
				- this.getInsets().top - 50;
		int width = this.getWidth() - this.getInsets().left
				- this.getInsets().right - 200;

		// initialize
		dottedChartOptionPanel = new DottedChartOptionPanel(this);
		dottedChartPanel = new DottedChartPanel(this);
		dottedChartOptionPanel.registerGUIListener(dottedChartPanel);
		settingPanel = new SettingPanel(this);

		// initialize settings of taskMap
		chartPane = new JScrollPane(dottedChartPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chartPane.setAutoscrolls(true);

		// repaint after moving scrollbar
		AdjustmentListener adjListener = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				reDrawBoxOnOverview();
				dottedChartPanel.updateUI();
				dottedChartPanel.repaint();
			}
		};
		chartPane.getHorizontalScrollBar().addAdjustmentListener(adjListener);
		chartPane.getVerticalScrollBar().addAdjustmentListener(adjListener);
		chartPane.setPreferredSize(new Dimension(width - 200, height - 10));

		// initialize overview Panel;
		ovPanel = new OverviewPanel(this);
		ovPanel.setPreferredSize(new Dimension(200, 130));

		// initialize MetricsPanel
		metricsPane = new JScrollPane(dcModel
				.getStatisticsPanel(dottedChartOptionPanel));
		metricsPane.setPreferredSize(new Dimension(200, height - 10));

		// initialize Panel including overview and metrics Panel
		spom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ovPanel, metricsPane);
		spom.setOneTouchExpandable(false);
		spom.setDividerLocation(131);
		spom.setDividerSize(3);

		// initialize main split panel
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPane, spom);
		split.setDividerLocation(split.getSize().width
				- split.getInsets().right - split.getDividerSize() - 200);

		split.setOneTouchExpandable(true);
		split.setResizeWeight(1.0);
		split.setDividerSize(3);
		split.setOneTouchExpandable(true);

		// Add dotted Panel and Setting Panel
		tabbedPane.add("Dotted Chart", split);
		tabbedPane.add("Settings", settingPanel);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				switch (tabbedPane.getSelectedIndex()) {
				case 0:
					// getOverviewPanel().setDrawBox(true);
					// dottedChartPanel.changeWidthSort();
					break;
				case 1:
					settingPanel.initSettingPanel();
					break;
				default:
					break;
				}
			}
		});

		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(tabbedPane, BorderLayout.CENTER);
		centerPanel.setMinimumSize(new Dimension(width, height));
		centerPanel.setAlignmentX(LEFT_ALIGNMENT);
		sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dottedChartOptionPanel
				.getOptionPanel(), centerPanel);// westPanel, centerPanel);
		sp.setDividerLocation(165);
		sp.setDividerSize(3);
		sp.setOneTouchExpandable(true);
		this.setLayout(new BorderLayout());
		this.add(sp, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	// public methods for attributes
	public DottedChartModel getDottedChartModel() {
		return dcModel;
	}

	public DottedChartPanel getDottedChartPanel() {
		return dottedChartPanel;
	}

	public SettingPanel getSettingPanel() {
		return settingPanel;
	}

	public DottedChartOptionPanel getDottedChartOptionPanel() {
		return dottedChartOptionPanel;
	}

	public LogReader getLogReader() {
		return inputLog;
	}

	// methods for dealing with zoom
	public Dimension getViewportSize() {
		return chartPane.getViewport().getSize();
	}

	public int getVerticalPosition() {
		return chartPane.getViewport().getViewPosition().y;
	}

	public int getHorizontalPosition() {
		return chartPane.getViewport().getViewPosition().x;
	}

	public JScrollPane getScrollPane() {
		return chartPane;
	}

	public void setScrollBarPosition(Point aP) {
		chartPane.getViewport().validate();
		if (aP.x < 0)
			aP.x = 0;
		if (aP.y < 0)
			aP.y = 0;
		if ((aP.x + chartPane.getViewport().getSize().getWidth()) > dottedChartPanel
				.getWidth())
			aP.x = (int) (dottedChartPanel.getWidth() - chartPane.getViewport()
					.getSize().getWidth());
		if ((aP.y + chartPane.getViewport().getSize().getHeight()) > dottedChartPanel
				.getHeight())
			aP.y = (int) (dottedChartPanel.getHeight() - chartPane
					.getViewport().getSize().getHeight());
		chartPane.getViewport().setViewPosition(aP);
	}

	public void actionPerformed(ActionEvent e) {
		dcModel.updateStatisticsPanel(dottedChartOptionPanel);
		ovPanel.setDrawBox(true);
		ovPanel.repaint();
		ovPanel.revalidate();
	}

	public void reDrawBoxOnOverview() {
		ovPanel.repaint();
		ovPanel.revalidate();
	}
}
