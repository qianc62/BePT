package org.processmining.analysis.socialsuccess.ui.summary;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.ui.SwingWorker;

public class UIInputSummary extends JPanel {

	private static final long serialVersionUID = -5205187497296143377L;
	protected JTextPane summaryPane = null;
	protected SummaryUI parent = null;

	public UIInputSummary(SummaryUI summaryUI) {
		super();
		this.parent = summaryUI;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.decode("#888888"));
		this.summaryPane = new JTextPane();
		this.summaryPane.setBorder(BorderFactory.createEmptyBorder());
		this.summaryPane.setContentType("text/html");
		// pre-populate the text pane with some teaser message
		this.summaryPane
				.setText("<html><body bgcolor=\"#888888\" text=\"#333333\">"
						+ "<br><br><br><br><br><center><font face=\"helvetica,arial,sans-serif\" size=\"4\">"
						+ "Please wait while the summary is created...</font></center></body></html>");
		this.summaryPane.setEditable(false);
		this.summaryPane.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(this.summaryPane);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		JScrollBar vBar = scrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(20, 20, 20), new Color(60, 60, 60), 4, 12));
		vBar.setOpaque(false);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		RoundedPanel scrollEnclosure = new RoundedPanel(10, 0, 0);
		scrollEnclosure.setBackground(Color.decode("#888888"));
		scrollEnclosure.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollEnclosure.setLayout(new BorderLayout());
		scrollEnclosure.add(scrollPane, BorderLayout.CENTER);
		this.add(scrollEnclosure);
		checkRecompileSummary();
	}

	public LogFilter getLogFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void checkRecompileSummary() {
		// create summary HTML asynchronously (can take a while with large logs)
		SwingWorker worker = new SwingWorker() {
			protected String htmlSummary = null;

			public Object construct() {
				Thread.yield();
				htmlSummary = getSummaryText();
				return null;
			}

			public void finished() {
				if (htmlSummary != null) {
					summaryPane.setText(htmlSummary);
					summaryPane.setCaretPosition(0);
				}
				repaint();
			}

		};
		worker.start();
	}

	public String getSummaryText() {
		return parent.getInputSummary().toString();
	}

}
