package org.processmining.analysis.socialsuccess.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import corejava.PrintfFormat;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.ui.summary.SummaryUI;

public class DetailsUI extends JComponent {
	private static final long serialVersionUID = -7012931195422166004L;
	private BigFive bigFive;
	protected JTextPane detailPane = null;
	protected SummaryUI parent = null;

	public DetailsUI(BigFive bf) {
		bigFive = bf;

		LogPreviewUI();
	}

	public void LogPreviewUI() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.decode("#888888"));
		this.detailPane = new JTextPane();
		this.detailPane.setContentType("text/html");
		this.detailPane.setBorder(BorderFactory.createEmptyBorder());
		this.detailPane.setEditable(false);
		this.detailPane.setCaretPosition(0);
		// intialise the pane
		instancesSelectionChanged(new String[0]);

		JScrollPane scrollPane = new JScrollPane(this.detailPane);
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

		// assemble GUI
	}

	protected void instancesSelectionChanged(String[] users) {
		PrintfFormat pf = new PrintfFormat("%.1f");
		String text = "<html><body bgcolor=\"#888888\" text=\"#333333\">";
		if (users.length == 0) {
			text += "<div bgcolor=\"#AAAAAA\"><br/><br/><center><b>Select at least a single user to view details!</b></center><br/><br/></div>";
		} else {
			text += "<table bgcolor=\"#AAAAAA\"><tr><th>User</th><th>Openness</th><th>Conscienstiousness</th>"
					+ "<th>Extraversion</th><th>Agreeableness</th><th>Neurotics</th></tr>";
			for (int i = 0; i < users.length; i++) {
				text += "<tr><td>" + users[i] + "</td>";
				Vector<Double> sc = bigFive.getResults(users[i]);
				for (int j = 0; j < sc.size(); j++) {
					text += "<td>"
							+ pf
									.sprintf(((sc.get(j) - BigFive.additionConst) / (BigFive.additionConst * 4.)) * 100)
							+ " % </td>";
				}
				text += "</tr>";
			}
			text += "</table>";
		}

		// Ook details weergeven
		if (users.length == 1 && !users[0].startsWith("Cluster")) {
			text += "<br/><br/>" + this.getDetails(users[0]);
		}

		text += "</body></html>";
		detailPane.setText(text);
	}

	private String getDetails(String user) {
		String text = "";
		Double[][] s = bigFive.getDetailedResults(user);
		text += "<table align=\"left\" bgcolor=\"#AAAAAA\">";
		PrintfFormat pf = new PrintfFormat("%.1f");
		for (int i = 0; i < s.length; i++) {
			text += "<tr>";
			switch (i) {
			case 0:
				text += "<td><b>Openness</b></td><td>Hava A Rich Vocabulary</td>"
						+ "<td>Full of Ideas</td><td>Excellent Ideas</td><th/><th/>";
				break;
			case 1:
				text += "<td><b>Conscientiousness</b></td><td>Order And Regularity</td>"
						+ "<td>Pay Attentions to Details</td><td>Tidy Up</td><th/><th/>";
				break;
			case 2:
				text += "<td><b>Extraversion</b></td><td>Moderating Groups</td>"
						+ "<td>Public Person</td><td>Start Conversations</td><th/><th/>";
				break;
			case 3:
				text += "<td><b>Agreeableness</b></td><td>Interested in Others</td>"
						+ "<td>On Good Terms</td><td>Show Gratitude</td><th/><th/>";
				break;
			case 4:
				text += "<td><b>Neuroticism</b></td><td>Irritated Easily</td>"
						+ "<td>Overwhelmed by Emotions</td><th/><th/><th/>";
				break;
			}
			text += "</tr><tr><td></td>";
			for (int j = 0; j < s[i].length; j++) {
				if (s[i][j] != null)
					text += "<td>"
							+ pf
									.sprintf(((s[i][j]) / (4 * BigFive.additionConst)) * 100)
							+ " % </td>";
				else
					text += "<td>&nbsp;</td>";
			}
			text += "</tr><tr><td colspan=\"6\" bgcolor=\"#888888\"></td></tr>";
		}
		text += "</table>";
		return text;
	}

}
