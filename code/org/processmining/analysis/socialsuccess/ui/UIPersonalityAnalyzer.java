package org.processmining.analysis.socialsuccess.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.deckfour.slickerbox.components.GradientPanel;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.IconVerticalTabbedPane;
import org.processmining.analysis.socialsuccess.PersonalityData;
import org.processmining.analysis.socialsuccess.ui.summary.SummaryUI;

public class UIPersonalityAnalyzer extends JPanel {

	private static final long serialVersionUID = -3375004086008517837L;
	public static Color colorEnclosureBg = new Color(40, 40, 40);
	public static Color colorNonFocus = new Color(70, 70, 70);
	public static Color colorListBg = new Color(60, 60, 60);
	public static Color colorListFg = new Color(180, 180, 180);
	public static Color colorListSelectionBg = new Color(80, 0, 0);
	public static Color colorListSelectionFg = new Color(240, 240, 240);

	// icons
	private Image summaryIcon = (new ImageIcon("images/slicker/summary48-2.png"))
			.getImage();
	private Image clusterIcon = (new ImageIcon("images/slicker/inspector48.png"))
			.getImage();
	private Image individualIcon = (new ImageIcon(
			"images/slicker/summary48-3.png")).getImage();

	// UI elements
	protected HeaderBar header;
	protected PersonalityData data;

	public UIPersonalityAnalyzer(PersonalityData personalityData) {
		super();
		this.data = personalityData;
		this.initialize();
	}

	/**
	 * loads the log and initializes this component
	 */
	protected void initialize() {
		header = new HeaderBar("Personality Analyzer");
		header.setHeight(40);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(header, BorderLayout.NORTH);
		this.setBackground(colorEnclosureBg);
		GradientPanel backPanel = new GradientPanel(new Color(130, 130, 130),
				colorEnclosureBg);
		backPanel.setLayout(new BorderLayout());
		IconVerticalTabbedPane iconTabs = new IconVerticalTabbedPane(new Color(
				230, 230, 230, 210), new Color(20, 20, 20, 160));
		iconTabs.addTab("Summary", summaryIcon, new SummaryUI(this.data));
		iconTabs.addTab("Individuals", individualIcon, new IndividualsUI(
				this.data));
		iconTabs.addTab("Groups", clusterIcon, new ClustersUI(this.data));
		backPanel.add(iconTabs, BorderLayout.CENTER);
		this.add(backPanel, BorderLayout.CENTER);
		this.setVisible(true);
		revalidate();
		repaint();
	}
}
