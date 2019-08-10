package org.processmining.analysis.abstractions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTextArea;

import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.abstractions.util.UkkonenSuffixTree;
import org.processmining.framework.ui.MainUI;

@SuppressWarnings("serial")
public class AbstractionResultConfigurationComponent extends SmoothPanel {
	protected static Color COLOR_FG = new Color(50, 50, 50);
	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_BG_HILIGHT = new Color(160, 160, 160);

	protected Color colorListBg = new Color(140, 140, 140);
	protected Color colorListFg = new Color(40, 40, 40);
	protected Color colorListSelectedBg = new Color(20, 20, 60);
	protected Color colorListSelectedFg = new Color(220, 40, 40);

	protected JCheckBox activeBox;

	protected JTextArea abstractionName;
	protected JList abstractionElements;
	// protected JTextArea abstractionElements;
	// protected SlickerButton editButton;
	// protected SlickerButton refreshButton;
	protected SlickerButton removeButton;
	// protected JScrollPane abstractionElementsScrollPane;
	protected ActivitySelectionPanel activitySelectionPanel;
	protected TreeSet<String> selectedActivities;
	protected Set<String> removedActivities;
	protected TreeSet<String> originalAbstractionActivities;
	boolean isModified;

	class DescendingStrLengthComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareTo(s2) * (s1.length() < s2.length() ? 1 : -1);// *
			// (-1);
		}
	}

	public Set<String> getSelectedActivities() {
		return this.activitySelectionPanel.getSelectedActivities();
	}

	public AbstractionResultConfigurationComponent(String absName,
			Set<String> absSet) {
		this.setBackground(COLOR_BG);
		this.setHighlight(COLOR_BG_HILIGHT);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		// this.setLayout(new GridLayout(0,4));
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		String[] delimiters = { "!", ",", "=", "+", "@", "~", "$", "(", ")" };

		// this.setMinimumSize(new Dimension(400, 70));
		// this.setMaximumSize(new Dimension(1000, 120));
		// this.setPreferredSize(new Dimension(390, 120));
		this.originalAbstractionActivities = new TreeSet<String>();
		originalAbstractionActivities.addAll(absSet);
		this.activeBox = new JCheckBox();
		activeBox.setEnabled(true);
		activeBox.setSelected(false);

		this.isModified = false;

		UkkonenSuffixTree st;
		String combinedActivityString = "";
		int index = 0;
		for (String activity : absSet) {
			combinedActivityString += activity.replaceAll("complete", "")
					.replaceAll("-", "")
					+ delimiters[index % 9];
			index++;
		}
		st = new UkkonenSuffixTree(1, combinedActivityString + ".");
		st.findLeftDiverseNodes();
		HashSet<String> mrSet = st.getSuperMaximalRepeats();

		TreeSet<String> filteredMRSet = new TreeSet<String>(
				new DescendingStrLengthComparator());
		for (String mr : mrSet)
			if (mr.trim().length() > 5)
				filteredMRSet.add(mr);

		HashMap<String, Integer> mrCountMap = new HashMap<String, Integer>();
		for (String mr : filteredMRSet)
			mrCountMap.put(mr, 0);

		int count;
		for (String activity : absSet) {
			for (String maxRepeat : filteredMRSet) {
				if (activity.contains(maxRepeat)) {
					count = mrCountMap.get(maxRepeat) + 1;
					mrCountMap.put(maxRepeat, count);
				}
			}
		}
		int[] countArray = new int[mrCountMap.size()];
		index = 0;
		for (String mr : mrCountMap.keySet())
			countArray[index++] = mrCountMap.get(mr);
		Arrays.sort(countArray);
		Set<String> tempAbsName = new HashSet<String>();
		for (int i = index - 1; i >= 0 && i >= index - 2; i--) {
			for (String mr : mrCountMap.keySet())
				if (mrCountMap.get(mr) == countArray[i]) {
					tempAbsName.add(mr.replaceAll("#", "").trim());
				}
		}

		if (tempAbsName.size() == 0)
			abstractionName = new JTextArea(absName);
		else
			abstractionName = new JTextArea(tempAbsName.toString());
		// abstractionName.setPreferredSize(new
		// Dimension(this.getSize().width/3,this.getSize().height));
		// abstractionName.setMaximumSize(new
		// Dimension(this.getSize().width/3,this.getSize().height));
		abstractionName.setColumns(20);
		abstractionName
				.setSize(this.getSize().width / 3, this.getSize().height);
		abstractionName.setBackground(new Color(140, 140, 140));
		abstractionName.setLineWrap(true);
		abstractionName.setWrapStyleWord(true);
		abstractionName.setForeground(new Color(0, 0, 255));

		Vector<String> a = new Vector<String>();
		a.addAll(absSet);
		abstractionElements = new JList(a);
		abstractionElements.setBackground(colorListBg);
		abstractionElements.setForeground(colorListFg);
		abstractionElements.setSelectionForeground(colorListBg);
		abstractionElements.setSelectionBackground(colorListSelectedBg);
		abstractionElements.setSelectionForeground(colorListSelectedFg);

		// abstractionElements = new JTextArea();
		// abstractionElements.setLineWrap(true);
		// abstractionElements.setWrapStyleWord(true);
		// abstractionElements.setText(absSet.toString());
		// abstractionElementsScrollPane = new JScrollPane(abstractionElements);

		this.selectedActivities = new TreeSet<String>();
		this.selectedActivities.addAll(absSet);
		activitySelectionPanel = new ActivitySelectionPanel(absSet,
				selectedActivities);

		// editButton = new SlickerButton("Edit");
		// editButton.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// activitySelectionPanel.setSelectedActivities(selectedActivities);
		// MainUI.getInstance().createFrame("Abstracted Activities",
		// activitySelectionPanel);
		// activitySelectionPanel.resizeFrame();
		// }
		//			
		// });
		//		
		// refreshButton = new SlickerButton("Refresh");
		// refreshButton.addActionListener(new ActionListener(){
		// public void actionPerformed(ActionEvent e) {
		// selectedActivities = activitySelectionPanel.getSelectedActivities();
		// Vector<String> b = new Vector<String>();
		// b.addAll(selectedActivities);
		// abstractionElements.setListData(b);
		// // abstractionElements.setText(selectedActivities.toString());
		//				
		// revalidate();
		// repaint();
		// }
		// });
		// System.out.println("Selected Activities: "+selectedActivities.size()+" @ "+selectedActivities);

		removeButton = new SlickerButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (abstractionElements.getSelectedValues().length > 0) {
					selectedActivities.clear();
					if (removedActivities == null)
						removedActivities = new TreeSet<String>();

					Vector<String> removeElementsList = new Vector<String>();
					if (removedActivities.size()
							+ abstractionElements.getSelectedValues().length == originalAbstractionActivities
							.size()) {
						MainUI
								.getInstance()
								.showGlassDialog("Invalid Selection",
										"Can't delete all elements in the abstraction with this Option");
						abstractionElements.clearSelection();
					} else {
						for (Object o : abstractionElements.getSelectedValues()) {
							removedActivities.add((String) o);
						}
						removeElementsList.addAll(removedActivities);
						// System.out.println("No. Removed Elements: "+removeElementsList.size());
						for (String s : originalAbstractionActivities)
							if (!removeElementsList.contains(s))
								selectedActivities.add(s);

						Vector<String> b = new Vector<String>();
						b.addAll(selectedActivities);
						abstractionElements.setListData(b);
						isModified = true;
					}
				}
				revalidate();
				repaint();
			}
		});
		this.add(activeBox, Component.LEFT_ALIGNMENT);
		this.add(Box.createHorizontalStrut(5));
		this.add(abstractionName, Component.LEFT_ALIGNMENT);
		this.add(Box.createHorizontalStrut(5));
		this.add(abstractionElements, Component.LEFT_ALIGNMENT);
		this.add(Box.createHorizontalStrut(5));
		this.add(removeButton);
		// this.add(editButton);
		// this.add(Box.createHorizontalStrut(5));
		// this.add(refreshButton);
	}

	public String getAbstractionName() {
		return abstractionName.getText();
	}

}
