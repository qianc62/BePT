package org.processmining.analysis.abstractions.ui;

import java.awt.Color;
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
public class RepeatAbstractionResultConfigurationComponent extends SmoothPanel {

	class DescendingStrLengthComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.compareTo(s2) * (s1.length() < s2.length() ? 1 : -1);
		}
	}

	protected static Color COLOR_FG = new Color(50, 50, 50);
	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_BG_HILIGHT = new Color(160, 160, 160);

	protected Color colorListBg = new Color(140, 140, 140);
	protected Color colorListFg = new Color(40, 40, 40);
	protected Color colorListSelectedBg = new Color(20, 20, 60);
	protected Color colorListSelectedFg = new Color(220, 40, 40);

	protected JCheckBox activeBox;
	protected JTextArea abstractionName;
	protected JList abstractionActivitiesList;

	protected ActivitySelectionPanel activitySelectionPanel;

	protected SlickerButton removeButton;
	protected TreeSet<String> selectedActivities;
	protected Set<String> removedActivities;
	protected TreeSet<String> originalAbstractionActivities;
	HashSet<String> decodedActivitySet;
	boolean isModified;

	public RepeatAbstractionResultConfigurationComponent(String absName,
			Set<String> alphabet) {
		this.setBackground(COLOR_BG);
		this.setHighlight(COLOR_BG_HILIGHT);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		this.activeBox = new JCheckBox();
		activeBox.setEnabled(true);
		activeBox.setSelected(false);

		this.isModified = false;

		String[] delimiters = { "!", ",", "=", "+", "@", "~", "$", "(", ")" };

		this.originalAbstractionActivities = new TreeSet<String>();
		originalAbstractionActivities.addAll(alphabet);

		decodedActivitySet = new HashSet<String>();
		for (String symbol : alphabet)
			decodedActivitySet.add(RepeatAbstractionResUI.charActivityMap
					.get(symbol));

		String combinedActivityString = "";
		int index = 0;

		for (String decodedActivity : decodedActivitySet) {
			combinedActivityString += decodedActivity
					.replaceAll("complete", "").replaceAll("-", "")
					+ delimiters[index % 9];
			index++;
		}

		UkkonenSuffixTree st = new UkkonenSuffixTree(1, combinedActivityString
				+ ".");
		st.findLeftDiverseNodes();
		HashSet<String> smrSet = st.getSuperMaximalRepeats();
		st = null;

		TreeSet<String> filteredSMRSet = new TreeSet<String>(
				new DescendingStrLengthComparator());
		for (String mr : smrSet)
			if (mr.trim().length() > 5)
				filteredSMRSet.add(mr);

		HashMap<String, Integer> smrCountMap = new HashMap<String, Integer>();
		for (String mr : filteredSMRSet)
			smrCountMap.put(mr, 0);

		int count;
		for (String activity : decodedActivitySet) {
			for (String maxRepeat : filteredSMRSet) {
				if (activity.contains(maxRepeat)) {
					count = smrCountMap.get(maxRepeat) + 1;
					smrCountMap.put(maxRepeat, count);
				}
			}
		}

		int[] countArray = new int[smrCountMap.size()];
		index = 0;
		for (String smr : smrCountMap.keySet())
			countArray[index++] = smrCountMap.get(smr);
		Arrays.sort(countArray);

		Set<String> tempAbsName = new HashSet<String>();
		for (int i = index - 1; i >= 0 && i >= index - 2; i--) {
			for (String smr : smrCountMap.keySet())
				if (smrCountMap.get(smr) == countArray[i]) {
					tempAbsName.add(smr.replaceAll("#", "").trim());
				}
		}

		if (tempAbsName.size() == 0)
			abstractionName = new JTextArea(absName);
		else
			abstractionName = new JTextArea(tempAbsName.toString());

		abstractionName.setBackground(new Color(140, 140, 140));
		abstractionName.setLineWrap(true);
		abstractionName.setWrapStyleWord(true);
		abstractionName.setForeground(new Color(0, 0, 255));

		Vector<String> a = new Vector<String>();
		a.addAll(decodedActivitySet);

		abstractionActivitiesList = new JList(a);
		abstractionActivitiesList.setBackground(colorListBg);
		abstractionActivitiesList.setForeground(colorListFg);
		abstractionActivitiesList.setSelectionForeground(colorListBg);
		abstractionActivitiesList.setSelectionBackground(colorListSelectedBg);
		abstractionActivitiesList.setSelectionForeground(colorListSelectedFg);

		this.selectedActivities = new TreeSet<String>();
		this.selectedActivities.addAll(alphabet);
		activitySelectionPanel = new ActivitySelectionPanel(alphabet,
				selectedActivities);

		removeButton = new SlickerButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (abstractionActivitiesList.getSelectedValues().length > 0) {
					selectedActivities.clear();
					if (removedActivities == null)
						removedActivities = new TreeSet<String>();

					Vector<String> removeElementsList = new Vector<String>();
					if (removedActivities.size()
							+ abstractionActivitiesList.getSelectedValues().length == originalAbstractionActivities
							.size()) {
						MainUI
								.getInstance()
								.showGlassDialog("Invalid Selection",
										"Can't delete all elements in the abstraction with this Option");
						abstractionActivitiesList.clearSelection();
					} else {
						for (Object o : abstractionActivitiesList
								.getSelectedValues()) {
							removedActivities
									.add(RepeatAbstractionResUI.activityCharMap
											.get((String) o));
						}
						removeElementsList.addAll(removedActivities);
						// System.out.println("No. Removed Elements: "+removeElementsList.size());
						HashSet<String> decodedSelectedActivitySet = new HashSet<String>();
						for (String symbol : originalAbstractionActivities)
							if (!removeElementsList.contains(symbol)) {
								selectedActivities.add(symbol);
								decodedSelectedActivitySet
										.add(RepeatAbstractionResUI.charActivityMap
												.get(symbol));
							}

						Vector<String> b = new Vector<String>();
						b.addAll(decodedSelectedActivitySet);

						abstractionActivitiesList.setListData(b);
						isModified = true;
					}
				}
				revalidate();
				repaint();
			}
		});
		this.add(activeBox);
		this.add(Box.createHorizontalStrut(5));
		this.add(abstractionName);
		this.add(Box.createHorizontalStrut(5));
		this.add(abstractionActivitiesList);
		this.add(Box.createHorizontalStrut(5));
		this.add(removeButton);
	}

	public String getAbstractionName() {
		// return abstractionName.getText();
		return "("
				+ abstractionName.getText().replaceAll("\\[", "").replaceAll(
						"\\]", "") + ")";
	}

	public Set<String> getSelectedActivities() {
		return this.activitySelectionPanel.getSelectedActivities();
	}

}
