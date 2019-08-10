package org.processmining.analysis.socialsuccess.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.slickerbox.components.FlatTabbedPane;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.ui.SlickerScrollBarUI;
import org.processmining.analysis.socialsuccess.BigFive;
import org.processmining.analysis.socialsuccess.PersonalityData;

public class ClustersUI extends JPanel {
	private static final long serialVersionUID = 6086465489593102426L;
	private BigFive bigFive;
	private JList usersList;
	private ArrayList<String> userNameList;
	private JList clusterList;
	private Color colorEnclosureBg = new Color(40, 40, 40);
	private Color colorNonFocus = new Color(70, 70, 70);
	private Color colorListBg = new Color(60, 60, 60);
	private Color colorListFg = new Color(180, 180, 180);
	private Color colorListSelectionBg = new Color(80, 0, 0);
	private Color colorListSelectionFg = new Color(240, 240, 240);
	private SpiderWebUI overviewUI;
	private DetailsUI spiderWebUI;
	private ArrayList<TreeMap<String, Vector<Double>>> clusters;

	public ClustersUI(PersonalityData data) {
		bigFive = new BigFive(data);
		LogPreviewUI();
	}

	public void LogPreviewUI() {
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent arg0) {
				updateClusters();
			}

			public void ancestorMoved(AncestorEvent arg0) { /* ignore */
			}

			public void ancestorRemoved(AncestorEvent arg0) { /* ignore */
			}
		});
		// assemble GUI
		this.add(getClusterPanel());
		this.add(getUserPanel());
		FlatTabbedPane tabbedPane = new FlatTabbedPane("Groups", new Color(240,
				240, 240, 230), new Color(180, 180, 180, 120), new Color(220,
				220, 220, 150));
		overviewUI = new SpiderWebUI(bigFive);
		spiderWebUI = new DetailsUI(bigFive);
		tabbedPane.addTab("Overview (graphic)", overviewUI);
		tabbedPane.addTab("Details (table)", spiderWebUI);
		this.add(tabbedPane);
	}

	private JPanel getClusterPanel() {
		// create cluster list
		clusterList = new JList();
		clusterList.setBackground(colorListBg);
		clusterList.setForeground(colorListFg);
		clusterList.setSelectionBackground(colorListSelectionBg);
		clusterList.setSelectionForeground(colorListSelectionFg);
		clusterList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clusterList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				clusterSelectionChanged();
			}
		});
		JScrollPane clustersScrollPane = new JScrollPane(clusterList);
		clustersScrollPane.setOpaque(false);
		clustersScrollPane.setBorder(BorderFactory.createEmptyBorder());
		clustersScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		clustersScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollBar vBar = clustersScrollPane.getVerticalScrollBar();
		vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0),
				new Color(160, 160, 160), colorNonFocus, 4, 12));
		vBar.setOpaque(false);
		// assemble instances list
		JLabel clustersListLabel = new JLabel("Clusters");
		clustersListLabel.setOpaque(false);
		clustersListLabel.setForeground(colorListSelectionFg);
		clustersListLabel.setFont(clustersListLabel.getFont().deriveFont(13f));
		clustersListLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		clustersListLabel.setHorizontalAlignment(JLabel.CENTER);
		clustersListLabel.setHorizontalTextPosition(JLabel.CENTER);
		RoundedPanel clusterPanel = new RoundedPanel(10, 5, 0);
		clusterPanel.setBackground(colorEnclosureBg);
		clusterPanel.setLayout(new BoxLayout(clusterPanel, BoxLayout.Y_AXIS));
		clusterPanel.setMaximumSize(new Dimension(180, 1000));
		clusterPanel.setPreferredSize(new Dimension(180, 1000));
		clusterPanel.add(clustersListLabel);
		clusterPanel.add(Box.createVerticalStrut(8));
		clusterPanel.add(clustersScrollPane);
		return clusterPanel;
	}

	private JPanel getUserPanel() {
		// create cluster list
		usersList = new JList();
		usersList.setBackground(colorListBg);
		usersList.setForeground(colorListFg);
		usersList.setSelectionBackground(colorListSelectionBg);
		usersList.setSelectionForeground(colorListSelectionFg);
		usersList
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		usersList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				userSelectionChanged();
			}
		});
		JScrollPane usersScrollPane = new JScrollPane(usersList);
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
		RoundedPanel userPanel = new RoundedPanel(10, 5, 0);
		userPanel.setBackground(colorEnclosureBg);
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
		userPanel.setMaximumSize(new Dimension(180, 1000));
		userPanel.setPreferredSize(new Dimension(180, 1000));
		userPanel.add(usersListLabel);
		userPanel.add(Box.createVerticalStrut(8));
		userPanel.add(usersScrollPane);
		return userPanel;
	}

	protected void clusterSelectionChanged() {
		int[] selectedIndices = clusterList.getSelectedIndices();

		if (clusters != null && clusters.size() > 0) {
			// repopulate user names list
			userNameList = new ArrayList<String>();
			ArrayList<String> list = new ArrayList<String>();

			// misschien het / de gekozen clusters bovenaan

			for (int i = 0; i < clusters.size(); i++) {
				Iterator<String> it = clusters.get(i).keySet().iterator();
				userNameList.add("Cluster " + i);
				list.add("Cluster " + i);
				boolean isSelected = false;
				for (int j = 0; j < selectedIndices.length; j++)
					if (selectedIndices[j] == i)
						isSelected = true;

				while (it.hasNext()) {
					String user = it.next();
					if (!user.equals("_reserved")) {
						userNameList.add(user);
						if (isSelected)
							list.add("+ " + user);
						else
							list.add("- " + user);
					}
				}
			}
			usersList.setListData(list.toArray());
			// reset events list
			usersList.clearSelection();
			userSelectionChanged();
			revalidate();
			repaint();
		}
	}

	private void userSelectionChanged() {
		int i = 0;
		int[] selectedIndices = usersList.getSelectedIndices();
		int[] selectedClusters = clusterList.getSelectedIndices();
		String[] users = new String[selectedIndices.length
				+ selectedClusters.length];
		if (selectedClusters.length == 0) {

		} else {
			for (i = 0; i < selectedClusters.length; i++) {
				users[i] = "Cluster " + selectedClusters[i];
			}
		}
		if (selectedIndices.length == 0) {

		} else {
			// ga verder waar de vorige loop was
			for (int j = 0; j < selectedIndices.length; j++) {
				users[i] = userNameList.get(selectedIndices[j]);
				i++;
			}
		}
		spiderWebUI.instancesSelectionChanged(users);
		overviewUI.instancesSelectionChanged(users);
	}

	protected void updateClusters() {
		// save the clusters
		clusters = bigFive.getClusters();
		if (clusters != null && clusters.size() > 0) {
			// repopulate user names list
			String[] clusteNames = new String[clusters.size()];

			for (int i = 0; i < clusters.size(); i++) {
				// -1 since every user has a mean point
				clusteNames[i] = "Cluster " + i + " (# "
						+ (clusters.get(i).size() - 1) + ")";
			}
			clusterList.setListData(clusteNames);
			// reset events list
			clusterList.clearSelection();
			revalidate();
			repaint();
		}
	}

}
