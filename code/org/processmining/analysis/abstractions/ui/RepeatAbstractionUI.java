package org.processmining.analysis.abstractions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.analysis.abstractions.util.ActivitySet;
import org.processmining.analysis.abstractions.util.UkkonenSuffixTree;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.ProgressPanel;

@SuppressWarnings("serial")
public class RepeatAbstractionUI extends JPanel {

	class DescendingStrLengthComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.equals(s2) ? 0 : (s1.length() <= s2.length() ? (s1
					.length() < s2.length() ? 1 : s2.compareTo(s1)) : -1);
			// return s1.compareTo(s2) * (s1.length() < s2.length() ? 1 : -1);//
			// *
			// (-1);
		}
	}

	static protected LogReader log;

	protected HeaderBar header;
	protected JComponent view;
	protected ProgressPanel progress;

	protected int encodingLength;
	protected HashMap<String, String> activityCharMap;
	protected HashMap<String, String> charActivityMap;

	static protected ArrayList<String> charStreamsList;

	protected HashMap<TreeSet<String>, TreeSet<String>> nearSuperMaximalRepeatAlphabetRepeatMap;
	protected HashMap<TreeSet<String>, Integer> nearSuperMaximalRepeatAlphabetCountMap;

	public RepeatAbstractionUI(LogReader log) {
		RepeatAbstractionUI.log = log;

		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(60, 60, 60));

		header = new HeaderBar("Repeat Abstractions");
		this.add(header, BorderLayout.NORTH);

		progress = new ProgressPanel("Repeat Abstractions");
		startAbstraction();
	}

	protected void startAbstraction() {
		progress.setNote("Encoding Activities");
		this.add(progress.getPanel(), BorderLayout.CENTER);
		this.view = progress.getPanel();

		HashSet<String> activitySet = new HashSet<String>();
		;
		try {
			for (int i = 0; i < log.numberOfInstances(); i++) {
				AuditTrailEntryList ateList = log.getInstance(i)
						.getAuditTrailEntryList();
				for (int a = 0; a < ateList.size(); a++) {
					activitySet.add(encodeActivity(ateList.get(a)));
				}
				activitySet.add("ProcessInstance" + i);
			}
			ActivitySet a = new ActivitySet(activitySet);
			a.encodeActivitySet();
			encodingLength = a.getEncodingLength();
			activityCharMap = a.getActivityCharMap();
			charActivityMap = a.getCharActivityMap();

			// FileIO io = new FileIO();
			// io.writeToFile("D:\\JC", "ActivityCharMapRepeatAbstraction.txt",
			// activityCharMap, "^");

		} catch (IndexOutOfBoundsException e) {
			// System.out.println("Index Out of Bounds Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		} catch (IOException e) {
			// System.out.println("IO Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		}

		progress.setNote("Converting to CharStreams");
		charStreamsList = new ArrayList<String>();
		String currentCharStream;
		HashSet<String> charStreamsSet = new HashSet<String>();
		try {
			for (int i = 0; i < log.numberOfInstances(); i++) {
				progress.setNote("Converting to CharStreams Process Instance "
						+ (i + 1));
				currentCharStream = "";
				AuditTrailEntryList ateList = log.getInstance(i)
						.getAuditTrailEntryList();
				for (int a = 0; a < ateList.size(); a++) {
					currentCharStream += activityCharMap
							.get(encodeActivity(ateList.get(a)));
				}
				charStreamsList.add(currentCharStream);
				charStreamsSet.add(currentCharStream);
			}
		} catch (IndexOutOfBoundsException e) {
			// System.out.println("Index Out of Bounds Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		} catch (IOException e) {
			// System.out.println("IO Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		}

		/**
		 * Find all superMaximalRepeats; Super maximal repeats are good enough
		 * because we use Subsumption property for abstraction
		 */

		// String combinedCharStream = "";
		int charStreamIndex = 0;
		// for(String charStream : charStreamsSet){
		// combinedCharStream += charStream +
		// activityCharMap.get("ProcessInstance"+charStreamIndex);
		// charStreamIndex++;
		// }

		StringBuilder combinedCharStreamBuilder = new StringBuilder();
		for (String charStream : charStreamsSet) {
			combinedCharStreamBuilder.append(charStream
					+ activityCharMap.get("ProcessInstance" + charStreamIndex));
			charStreamIndex++;
		}

		// UkkonenSuffixTree st = new
		// UkkonenSuffixTree(encodingLength,combinedCharStream);
		UkkonenSuffixTree st = new UkkonenSuffixTree(encodingLength,
				combinedCharStreamBuilder.toString());
		st.findLeftDiverseNodes();
		HashSet<String> nearSuperMaximalRepeatSet = st
				.getNearSuperMaximalRepeats();
		// HashSet<String> superMaximalRepeatSet = st.getSuperMaximalRepeats();

		// st = null;
		// combinedCharStream = null;
		/*
		 * HashSet<String> mrIsolationSet = new HashSet<String>();
		 * mrIsolationSet.addAll(nearSuperMaximalRepeatSet);
		 * mrIsolationSet.removeAll(superMaximalRepeatSet);
		 * 
		 * superMaximalRepeatSet = null;
		 */
		combinedCharStreamBuilder = null;
		/**
		 * Get the repeat alphabet map
		 */
		// HashMap<String, TreeSet<String>> repeatRepeatAlphabetMap = new
		// HashMap<String, TreeSet<String>>();
		nearSuperMaximalRepeatAlphabetRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		TreeSet<String> nearSuperMaximalRepeatAlphabetRepeatSet;
		TreeSet<String> repeatAlphabet;
		int nearSuperMaximalRepeatLength;
		for (String nearSuperMaximalRepeat : nearSuperMaximalRepeatSet) {
			nearSuperMaximalRepeatLength = nearSuperMaximalRepeat.length()
					/ encodingLength;
			repeatAlphabet = new TreeSet<String>();
			for (int i = 0; i < nearSuperMaximalRepeatLength; i++)
				repeatAlphabet.add(nearSuperMaximalRepeat.substring(i
						* encodingLength, (i + 1) * encodingLength));

			// if(!repeatRepeatAlphabetMap.containsKey(nearSuperMaximalRepeat))
			// repeatRepeatAlphabetMap.put(nearSuperMaximalRepeat,
			// repeatAlphabet);

			if (nearSuperMaximalRepeatAlphabetRepeatMap
					.containsKey(repeatAlphabet)) {
				nearSuperMaximalRepeatAlphabetRepeatSet = nearSuperMaximalRepeatAlphabetRepeatMap
						.get(repeatAlphabet);
			} else {
				nearSuperMaximalRepeatAlphabetRepeatSet = new TreeSet<String>(
						new DescendingStrLengthComparator());
			}

			nearSuperMaximalRepeatAlphabetRepeatSet.add(nearSuperMaximalRepeat);
			nearSuperMaximalRepeatAlphabetRepeatMap.put(repeatAlphabet,
					nearSuperMaximalRepeatAlphabetRepeatSet);
		}

		// System.out.println("No. Near Super Maximals: "+nearSuperMaximalRepeatSet.size());
		// System.out.println("No. NSM Alphabets: "+nearSuperMaximalRepeatAlphabetRepeatMap.size());
		// System.out.println("No. MR in Isolation: "+mrIsolationSet.size());
		// System.out.println("RepeatRepeatAlphabetSize: "+repeatRepeatAlphabetMap.size());

		/**
		 * Get the count of repeat alphabets
		 */
		nearSuperMaximalRepeatAlphabetCountMap = new HashMap<TreeSet<String>, Integer>();
		int count;
		charStreamIndex = 0;
		// combinedCharStream = "";
		// for(String charStream : charStreamsList){
		// combinedCharStream +=
		// charStream+activityCharMap.get("ProcessInstance"+charStreamIndex);
		// charStreamIndex++;
		// }
		// st = new UkkonenSuffixTree(encodingLength,combinedCharStream);
		// st.findLeftDiverseNodes();
		// combinedCharStream = null;

		for (TreeSet<String> nsmra : nearSuperMaximalRepeatAlphabetRepeatMap
				.keySet()) {
			nearSuperMaximalRepeatAlphabetRepeatSet = nearSuperMaximalRepeatAlphabetRepeatMap
					.get(nsmra);
			count = 0;
			for (String nsmr : nearSuperMaximalRepeatAlphabetRepeatSet)
				count += st.noMatches(nsmr);

			nearSuperMaximalRepeatAlphabetCountMap.put(nsmra, count);
		}
		st = null;

		// FileIO io = new FileIO();
		// io.writeToFile("D:\\JC", "rra.txt", repeatRepeatAlphabetMap,"^");
		// io.writeToFile("D:\\JC", "nsmRAMap.txt",
		// nearSuperMaximalRepeatAlphabetRepeatMap,"^");
		// io.writeToFile("D:\\JC", "nsmRACountMap.txt",
		// nearSuperMaximalRepeatAlphabetCountMap, "^");

		progress.setNote("Done");
		progress.close();
		view = new RepeatAbstractionResUI(encodingLength,
				nearSuperMaximalRepeatAlphabetRepeatMap,
				nearSuperMaximalRepeatAlphabetCountMap, charActivityMap);
		add(view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	public static String encodeActivity(AuditTrailEntry ate) {
		return ate.getElement() + "--" + ate.getType();
	}
}
