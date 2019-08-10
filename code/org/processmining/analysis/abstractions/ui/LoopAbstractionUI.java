package org.processmining.analysis.abstractions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.analysis.abstractions.util.ActivitySet;
import org.processmining.analysis.abstractions.util.UkkonenSuffixTree;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.ProgressPanel;

@SuppressWarnings("serial")
public class LoopAbstractionUI extends JPanel {
	static protected LogReader log;

	protected JComponent view;
	protected RoundedPanel confPanel;
	protected ProgressPanel progress;

	protected HeaderBar header;

	protected JButton startButton;

	protected int encodingLength;
	protected HashMap<String, String> activityCharMap;
	protected HashMap<String, String> charActivityMap;
	protected HashMap<TreeSet<String>, TreeSet<String>> allTandemRepeatAlphabetRepeatMap;
	protected HashMap<TreeSet<String>, Integer> alphabetCountMap;

	static protected ArrayList<String> charStreamsList;

	public LoopAbstractionUI(LogReader log) {
		LoopAbstractionUI.log = log;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(60, 60, 60));

		header = new HeaderBar("Loop Abstractions");
		this.add(header, BorderLayout.NORTH);

		progress = new ProgressPanel("Loop Abstractions");
		startAbstraction();
	}

	public JPanel packLeftAligned(JComponent component, int distFromLeft) {
		Dimension compPref = component.getPreferredSize();
		Dimension compMin = component.getMinimumSize();
		JPanel packed = new JPanel();
		packed.setPreferredSize(compPref);
		packed.setMaximumSize(new Dimension(2000, compPref.height));
		packed.setMinimumSize(compMin);
		packed.setOpaque(false);
		packed.setBorder(BorderFactory.createEmptyBorder());
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(distFromLeft));
		packed.add(component);
		packed.add(Box.createHorizontalGlue());
		return packed;
	}

	protected void startAbstraction() {
		progress.setNote("Encoding Activities");
		this.add(progress.getPanel(), BorderLayout.CENTER);
		this.view = progress.getPanel();
		this.revalidate();
		this.repaint();

		HashSet<String> activitySet = new HashSet<String>();
		;
		try {
			for (int i = 0; i < log.numberOfInstances(); i++) {
				AuditTrailEntryList ateList = log.getInstance(i)
						.getAuditTrailEntryList();
				for (int a = 0; a < ateList.size(); a++) {
					activitySet.add(encodeActivity(ateList.get(a)));
				}
			}
			ActivitySet a = new ActivitySet(activitySet);
			a.encodeActivitySet();
			encodingLength = a.getEncodingLength();
			activityCharMap = a.getActivityCharMap();
			charActivityMap = a.getCharActivityMap();
			// FileIO io = new FileIO();
			// io.writeToFile("D:\\JC", "ActivityCharMap.txt", activityCharMap,
			// "^");
		} catch (IndexOutOfBoundsException e) {
			// System.out.println("Index Out of Bounds Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		} catch (IOException e) {
			// System.out.println("IO Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		}

		progress.setNote("Converting to CharStreams");
		charStreamsList = new ArrayList<String>();
		String currentCharStream;
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
			}
		} catch (IndexOutOfBoundsException e) {
			// System.out.println("Index Out of Bounds Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		} catch (IOException e) {
			// System.out.println("IO Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		}

		// System.out.println("No. CharStreams: "+charStreamsList.size());

		/**
		 * Find all the tandem repeats here for each stream and aggregate the
		 * results of all streams;
		 */

		allTandemRepeatAlphabetRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		alphabetCountMap = new HashMap<TreeSet<String>, Integer>();

		TreeSet<String> tandemRepeatAlphabetRepeatSet, currentStreamTandemRepeatAlphabetSet;
		int count = 0;
		HashMap<TreeSet<String>, TreeSet<String>> currentStreamTandemRepeatAlphabetRepeatMap;
		UkkonenSuffixTree st;
		for (String charStream : charStreamsList) {
			if (charStream.length() / encodingLength > 1) {
				st = new UkkonenSuffixTree(encodingLength, charStream);
				st.LZDecomposition();
				currentStreamTandemRepeatAlphabetRepeatMap = st
						.getPrimitiveTandemRepeats();
				for (TreeSet<String> repeatAlphabet : currentStreamTandemRepeatAlphabetRepeatMap
						.keySet()) {
					currentStreamTandemRepeatAlphabetSet = currentStreamTandemRepeatAlphabetRepeatMap
							.get(repeatAlphabet);

					if (allTandemRepeatAlphabetRepeatMap
							.containsKey(repeatAlphabet))
						tandemRepeatAlphabetRepeatSet = allTandemRepeatAlphabetRepeatMap
								.get(repeatAlphabet);
					else
						tandemRepeatAlphabetRepeatSet = new TreeSet<String>();

					tandemRepeatAlphabetRepeatSet
							.addAll(currentStreamTandemRepeatAlphabetSet);
					allTandemRepeatAlphabetRepeatMap.put(repeatAlphabet,
							tandemRepeatAlphabetRepeatSet);

					/**
					 * Get the number of occurrences of the repeats in the
					 * alphabet in this charStream
					 */
					count = 0;
					for (String tr : currentStreamTandemRepeatAlphabetSet)
						count += st.noMatches(tr);

					if (alphabetCountMap.containsKey(repeatAlphabet)) {
						count += alphabetCountMap.get(repeatAlphabet);
					}
					alphabetCountMap.put(repeatAlphabet, count);
				}

				currentStreamTandemRepeatAlphabetRepeatMap = st
						.getComplexAlphabetTandemRepeatMap();

				for (TreeSet<String> repeatAlphabet : currentStreamTandemRepeatAlphabetRepeatMap
						.keySet()) {
					if (allTandemRepeatAlphabetRepeatMap
							.containsKey(repeatAlphabet))
						tandemRepeatAlphabetRepeatSet = allTandemRepeatAlphabetRepeatMap
								.get(repeatAlphabet);
					else
						tandemRepeatAlphabetRepeatSet = new TreeSet<String>();

					currentStreamTandemRepeatAlphabetSet = currentStreamTandemRepeatAlphabetRepeatMap
							.get(repeatAlphabet);
					tandemRepeatAlphabetRepeatSet
							.addAll(currentStreamTandemRepeatAlphabetSet);
					allTandemRepeatAlphabetRepeatMap.put(repeatAlphabet,
							tandemRepeatAlphabetRepeatSet);

					count = 0;
					for (String tr : currentStreamTandemRepeatAlphabetSet)
						count += st.noMatches(tr);

					if (alphabetCountMap.containsKey(repeatAlphabet)) {
						count += alphabetCountMap.get(repeatAlphabet);
					}
					alphabetCountMap.put(repeatAlphabet, count);
				}
			}
		}
		progress.setNote("Done");
		progress.close();
		// charStreamsList = null;
		// System.out.println("No. TandemRepeat Alphabets: "+allTandemRepeatAlphabetRepeatMap.size());
		// FileIO io = new FileIO();
		// io.writeToFile("D:\\JC", "TRAlphabetRepeatMap.txt",
		// allTandemRepeatAlphabetRepeatMap, "^");
		// view = new
		// LoopAbstractionResultUI(encodingLength,allTandemRepeatAlphabetRepeatMap,
		// alphabetCountMap, charActivityMap, activityCharMap);
		view = new LoopAbstractionResUI(encodingLength,
				allTandemRepeatAlphabetRepeatMap, alphabetCountMap,
				charActivityMap);
		add(view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	public static String encodeActivity(AuditTrailEntry ate) {
		return ate.getElement() + "--" + ate.getType();
	}
}
