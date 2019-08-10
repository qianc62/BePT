package org.processmining.analysis.socialsuccess.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.*;
import javax.swing.event.*;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.PersonalityData;

public class IndividualsUI extends JComponent {
	private static final long serialVersionUID = -7012931195422166004L;
	private BigFive bigFive;
	private JList userList;
	private Color colorEnclosureBg = new Color(40, 40, 40);
	private Color colorNonFocus = new Color(70, 70, 70);
	private Color colorListBg = new Color(60, 60, 60);
	private Color colorListFg = new Color(180, 180, 180);
	private Color colorListSelectionBg = new Color(80, 0, 0);
	private Color colorListSelectionFg = new Color(240, 240, 240);
	private SpiderWebUI overviewUI;
	private DetailsUI spiderWebUI;

	public IndividualsUI(PersonalityData data) {
		bigFive = new BigFive(data);
		// lijst met gebruikers links.
		LogPreviewUI();
	}

	public void LogPreviewUI() {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateView();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		JPanel usersPanel = getUsersPanel();
		// assemble GUI
		this.add(usersPanel);
		FlatTabbedPane tabbedPane = new FlatTabbedPane("Individuals",
				new Color(240, 240, 240, 230), new Color(180, 180, 180, 120),
				new Color(220, 220, 220, 150));
		overviewUI = new SpiderWebUI(bigFive);
		spiderWebUI = new DetailsUI(bigFive);
		tabbedPane.addTab("Overview (graphic)", overviewUI);
		tabbedPane.addTab("Details (table)", spiderWebUI);
		this.add(tabbedPane);
	}

	protected JPanel getUsersPanel() {
		// create instances list
		userList = new JList();
		userList.setBackground(colorListBg);
		userList.setForeground(colorListFg);
		userList.setSelectionBackground(colorListSelectionBg);
		userList.setSelectionForeground(colorListSelectionFg);
		userList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		userList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				instancesSelectionChanged();
			}
		});
		JScrollPane usersScrollPane = new JScrollPane(userList);
		usersScrollPane.setOpaque(false);
		usersScrollPane.setBorder(BorderFactory.createEmptyBorder());
		usersScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		usersScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = usersScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		// assemble instances list
		JLabel usersListLabel = new JLabel("Users");
		usersListLabel.setOpaque(false);
		usersListLabel.setForeground(colorListSelectionFg);
		usersListLabel.setFont(usersListLabel.getFont().deriveFont(13f));
		usersListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		usersListLabel.setHorizontalAlignment(JLabel.CENTER);
		usersListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel usersPanel = new RoundedPanel(10, 5, 0);
		usersPanel.setBackground(colorEnclosureBg);
		usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
		usersPanel.setMaximumSize(new Dimension(180, 1000));
		usersPanel.setPreferredSize(new Dimension(180, 1000));
		usersPanel.add(usersListLabel);
		usersPanel.add(Box.createVerticalStrut(8));
		usersPanel.add(usersScrollPane);
		return usersPanel;
	}

	protected void instancesSelectionChanged() {
		int[] selectedIndices = userList.getSelectedIndices();
		String[] users = new String[selectedIndices.length];
		if (selectedIndices.length == 0) {

		} else {
			users[0] = getValue(selectedIndices[0]);
			for (int i = 0; i < selectedIndices.length; i++) {
				users[i] = getValue(selectedIndices[i]);
			}
		}
		spiderWebUI.instancesSelectionChanged(users);
		overviewUI.instancesSelectionChanged(users);
	}

	private String getValue(int index) {
		Object[] d = bigFive.getUsers().toArray();
		return (String) d[index];
	}

	protected void updateView() {
		TreeSet<String> users = bigFive.getUsers();
		if (users != null && users.size() > 0) {
			// repopulate user names list
			String[] userNames = new String[users.size()];
			Iterator<String> it = users.iterator();
			int i = 0;
			while (it.hasNext()) {
				userNames[i] = it.next();
				i++;
			}
			userList.setListData(userNames);
			// reset events list
			userList.clearSelection();
			revalidate();
			repaint();
		}
	}
}
