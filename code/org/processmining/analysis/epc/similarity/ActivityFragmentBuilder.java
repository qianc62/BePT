package org.processmining.analysis.epc.similarity;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

import org.processmining.analysis.*;
import org.processmining.analysis.causality.*;
import org.processmining.exporting.causality.*;
import org.processmining.framework.models.*;
import org.processmining.framework.models.causality.*;
import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.ui.*;
import org.processmining.framework.util.*;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class ActivityFragmentBuilder implements AnalysisPlugin {

	public ActivityFragmentBuilder() {
	}

	public String getName() {
		return "EPC Similarity Calculator";
	}

	public String getHtmlDescription() {
		return "Takes two collections of EPC hierarchies and compares the EPCs in these collections";
	}

	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem base = new AnalysisInputItem("Comparison Base", 1,
				1000) {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof ConfigurableEPC) {
						return true;
					}
					if (o[i] instanceof EPCHierarchy) {
						return true;
					}
				}
				return false;
			}
		};
		AnalysisInputItem comp = new AnalysisInputItem(
				"Compare to (optional; if left empty then the base is compared with itself)",
				0, 1000) {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof ConfigurableEPC) {
						return true;
					}
					if (o[i] instanceof EPCHierarchy) {
						return true;
					}
				}
				return false;
			}
		};

		return new AnalysisInputItem[] { base, comp };
	}

	private ArrayList<ConfigurableEPC> baseEpcs = new ArrayList();
	private ArrayList<ConfigurableEPC> compareToEpcs = new ArrayList();
	private ArrayList<String> basePaths = new ArrayList();
	private ArrayList<String> compareToPaths = new ArrayList();
	private Similarities similarities;

	public ArrayList<ConfigurableEPC> getCompareToEpcs() {
		return compareToEpcs;
	}

	public void buildList(AnalysisInputItem item,
			ArrayList<ConfigurableEPC> list) {
		for (ProvidedObject obj : item.getProvidedObjects()) {
			for (Object o : obj.getObjects()) {
				if (o instanceof ConfigurableEPC) {
					((ConfigurableEPC) o).object = StringNormalizer
							.normalize(getPath((ConfigurableEPC) o, null));
					addConditionalToList((ConfigurableEPC) o, list);
				} else if (o instanceof EPCHierarchy) {
					EPCHierarchy hierarchy = (EPCHierarchy) o;
					for (Object o2 : hierarchy.getAllObjects()) {
						if (o2 instanceof ConfigurableEPC) {
							((ConfigurableEPC) o2).object = StringNormalizer
									.normalize(getPath((ConfigurableEPC) o2,
											hierarchy));
							addConditionalToList((ConfigurableEPC) o2, list);
						}
					}
				}
			}
		}
	}

	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {

		AnalysisInputItem base = analysisInputItemArray[0];
		AnalysisInputItem compareTo = analysisInputItemArray[1];
		comps = 0;
		compareToEpcs = new ArrayList();
		baseEpcs = new ArrayList();
		compareToPaths = new ArrayList();
		basePaths = new ArrayList();

		buildList(base, baseEpcs);
		buildList(compareTo, compareToEpcs);
		if (compareToEpcs.size() == 0) {
			// no epcs to compare to, either not provided, or just an empty set.
			for (int i = 0; i < baseEpcs.size(); i++) {
				compareToEpcs.add(baseEpcs.get(i));
			}
		}

		if (baseEpcs.size() == 0 || compareToEpcs.size() == 0) {
			Message
					.add(
							"At least one syntactically valid EPC is necessary for comparison.",
							Message.ERROR);
			return null;
		}

		Message.add("Starting comparison of " + baseEpcs.size()
				+ " base epcs with " + compareToEpcs.size() + " epcs.");

		Progress progress = new Progress("Comparing EPCs", 0, baseEpcs.size()
				* compareToEpcs.size() + 2);
		progress.setNote("Sorting EPCs on size.");
		progress.setProgress(1);

		Comparator comp = new Comparator() {
			public int compare(Object o1, Object o2) {
				if (o1 == o2) {
					return 0;
				}
				if (o1 == null) {
					return -1;
				}
				if (o2 == null) {
					return -1;
				}
				ConfigurableEPC epc1 = (ConfigurableEPC) o1;
				ConfigurableEPC epc2 = (ConfigurableEPC) o2;
				return -(epc1.getFunctions().size() * epc1.getEdges().size() - epc2
						.getFunctions().size()
						* epc2.getEdges().size());
			}

			public boolean equals(Object obj) {
				return false;
			}
		};
		Collections.sort(baseEpcs, comp);
		Collections.sort(compareToEpcs, comp);

		for (int i = 0; i < baseEpcs.size(); i++) {
			basePaths.add(i, (String) baseEpcs.get(i).object);
		}
		for (int i = 0; i < compareToEpcs.size(); i++) {
			compareToPaths.add(i, (String) compareToEpcs.get(i).object);
		}

		progress
				.setNote("Initializing Checker (might take some time, but happens only once)");
		progress.setProgress(2);

		try {
			// give ProM time to show the progress bar
			Thread.currentThread().sleep(10);
		} catch (InterruptedException ex) {
			// progress not shown, big deal.
		}

		FootprintSimilarityResultUI ui = null;

		// Start calculating the similarities in a multi-threaded fashion,
		// i.e. first, all footprints are built in separate threads.
		// each footprint that finishes is then compared to the already finished
		// ones, untill all are finished.
		// The last footprint that is built is compared to all others and
		// the comparison is done.

		SimilarityOptions options = new SimilarityOptions();
		SimilarityOptionDialog dialog = new SimilarityOptionDialog(options);
		replacementList = new ArrayList();

		if (dialog.showSettings()) {

			FootprintSimilarityResult result = new FootprintSimilarityResult(
					basePaths, baseEpcs, compareToPaths, compareToEpcs, options
							.getFootprintFolder());

			String title = options.toString();
			similarities = new Similarities(title, baseEpcs.size(),
					compareToEpcs.size());
			result.setSimilarities(similarities);

			fillSimilarities(result, progress, options);
			ui = new FootprintSimilarityResultUI(result);
		} else {
			Message.add("Comparison cancelled by user.");
		}

		for (int[] x : replacementList) {
			similarities.set(x[0], x[1], similarities.get(x[2], x[3]));
		}

		progress.close();
		Message.add("Performed " + comps + " comparisons with these options: "
				+ options.toString());

		return ui;
	}

	private ArrayList<int[]> replacementList;

	public void addToInvalidEntryReplaceList(int baseIndex, int compareToIndex,
			int copyFromBaseIndex, int copyFromCompareToIndex) {
		synchronized (replacementList) {
			replacementList.add(new int[] { baseIndex, compareToIndex,
					copyFromBaseIndex, copyFromCompareToIndex });
		}
	}

	public Similarities getSimilarities() {
		return similarities;
	}

	private int lastBaseStarted = -1;

	protected synchronized int getNextBase() {
		int b = ++lastBaseStarted;
		if (b < baseEpcs.size()) {
			return b;
		} else {
			return -1;
		}
	}

	protected synchronized int getNextCompareTo(boolean[] done) {
		int j = -1;
		synchronized (compWorking) {
			synchronized (compReady) {
				do {
					j++;
				} while ((compWorking[j] && !compReady[j]) || done[j]);
				// j is the first index of which the footprint is ready,
				// or on which no thread is working yet (owner.getCompWorking(j)
				// == false) and
				// that I have not done yet.
				setCompFootprintWorking(j);
			}
		}
		return j;
	}

	private StopWatch stopwatch;

	public void fillSimilarities(FootprintSimilarityResult result,
			Progress progress, SimilarityOptions options) {

		// we have all EPCs
		// now, start comparing all pairs of EPCs

		baseReady = new boolean[baseEpcs.size()];
		Arrays.fill(baseReady, false);
		compReady = new boolean[compareToEpcs.size()];
		Arrays.fill(baseReady, false);
		compWorking = new boolean[compareToEpcs.size()];
		Arrays.fill(baseReady, false);
		progress.setNote("Pairwise comparing footprints");

		lastBaseStarted = -1;
		comps = 0;
		messagePoint = 10;
		stopwatch = new StopWatch();
		stopwatch.start();

		FootprintBuilderThread[] threads = new FootprintBuilderThread[options
				.getParallelThreads()];
		for (int i = 0; i < options.getParallelThreads(); i++) {
			SimilarityCalculator simCalc;
			if (!progress.isCanceled()) {
				simCalc = new SimilarityCalculator(options
						.getFunctionThreshold(), options
						.getFunctionSyntaxWeight(), options
						.getFunctionSemanticWeight(), options
						.getFunctionStructureWeight());
				simCalc
						.setContextSimilarityCalculator(new SimilarityCalculator(
								options.getEventThreshold(), options
										.getEventSyntaxWeight(), options
										.getEventSemanticWeight(), 0));

				threads[i] = new FootprintBuilderThread(result, progress, this,
						options.getRemoveInitialFinalNodes(), options
								.getUseSimilarityValues(), simCalc);
				// once started, this thread will start producing footprints
				// and comparing them with already produced footprints,
				// untill all footprints have been dealt with.
				threads[i].start();
			}
		}
		for (int i = 0; i < options.getParallelThreads(); i++) {
			try {
				// Join all the threads started.
				threads[i].join();
			} catch (InterruptedException ex1) {
				// Builder thread interrupted. No big deal.
			}
		}
	}

	private boolean[] baseReady;

	protected final void setBaseFootprintReady(int i) {
		synchronized (baseReady) {
			baseReady[i] = true;
		}
	}

	private boolean[] compWorking;
	private boolean[] compReady;

	protected final void setCompFootprintWorking(int i) {
		synchronized (compWorking) {
			compWorking[i] = true;
		}
	}

	protected final void setCompFootprintReady(int i) {
		synchronized (compReady) {
			compReady[i] = true;
			compWorking[i] = false;
		}
	}

	protected final boolean getCompWorking(int i) {
		return compWorking[i];
	}

	protected final boolean getCompReady(int i) {
		return compReady[i];
	}

	private String getPath(ModelGraph epc, EPCHierarchy hierarchy) {
		String s = epc.getIdentifier();
		if (hierarchy != null && hierarchy.getParent(epc) != null) {
			s = getPath(hierarchy.getParent(epc), hierarchy) + "." + s;
		}
		return s;
	}

	private String getPath(Object o, EPCHierarchy hierarchy) {
		String s = o.toString();
		if (hierarchy.getParent(o) != null) {
			return getPath(hierarchy.getParent(o), hierarchy) + "." + s;
		} else {
			return s;
		}
	}

	protected String getBaseEPCIdentifier(int i) {
		return baseEpcs.get(i).getIdentifier();
	}

	protected String getCompareToEPCIdentifier(int i) {
		return compareToEpcs.get(i).getIdentifier();
	}

	protected String getBasePath(int i) {
		return basePaths.get(i);
	}

	protected String getCompareToPath(int i) {
		return compareToPaths.get(i);
	}

	protected int comps;
	private int messagePoint;

	protected final Similarity compare(int baseEpc, int compEpc,
			CausalFootprint cfp1, CausalFootprint cfp2,
			List<ActivityContextFragment> frags1,
			List<ActivityContextFragment> frags2, Progress progress,
			boolean removeInitialAndFinalNodes, boolean useSimilarityValues,
			SimilarityCalculator simCalc) {
		// Get the best possible mapping from functions to functions
		comps++;

		if ((comps % messagePoint) == 0) {
			stopwatch.stop();
			long timeSoFar = stopwatch.getDuration();
			long expectedRemaining = Math
					.round(((double) timeSoFar / (double) comps)
							* (((baseEpcs.size() * (compareToEpcs.size()))) - comps));
			// Message.add("Expected time remaining: " +
			// StopWatch.formatDuration(expectedRemaining));
			progress
					.setNote("Remaining: "
							+ StopWatch
									.formatDurationNoMilliSeconds(expectedRemaining));
		}
		int[] funcMapping;
		double[] similarities;
		// synchronized (simCalc) {
		try {
			// Similarities is an Array of doubles.
			// index[0..frags2.size()-1] are the similarities between
			// frags1.get(0),frags2.get(0..frags2.size()).
			// To get the similarity between two fragments frags1.get(i) and
			// frags2.get(j), use:
			// similarities[i*frags2.size()+j];
			similarities = simCalc.getSimilarityMatrix(frags1, frags2);

			// calculate best mapping, using the threshold in > fashion, i.e. a
			// threshold of 0.0
			// states that no mapping will be made between elements with
			// similarity 0.0
			funcMapping = simCalc.getBestPossibleMapping(similarities, frags1,
					frags2);
		} catch (Exception ex) {
			Message.add(ex.getMessage(), Message.ERROR);
			return null;
		}
		// }

		int noMaps = 0;
		HashMap<LogEventProvider, LogEventProvider> mapping = new HashMap();
		HashMap<Entry<LogEventProvider, LogEventProvider>, Double> similarityMap = new HashMap();
		for (int i = 0; i < baseEpcs.get(baseEpc).getFunctions().size(); i++) {
			EPCFunction f1 = (EPCFunction) baseEpcs.get(baseEpc).getFunctions()
					.get(i);
			Entry entry;
			if (funcMapping[i] == -1) {
				mapping.put(f1, CausalFootprintSimilarityResult.NOMAP);
				noMaps++;
			} else {
				mapping.put(f1, (EPCFunction) compareToEpcs.get(compEpc)
						.getFunctions().get(funcMapping[i]));
				if (useSimilarityValues) {
					similarityMap.put(new SimpleEntry(f1, mapping.get(f1)),
							similarities[i * frags2.size() + funcMapping[i]]);
				}
			}
		}
		Similarity sim;
		if (noMaps == mapping.keySet().size()) {
			// no mapping made
			// If the sets of functions of EPCs are already disjoint, then
			// the EPCs always have similarity 0.0
			sim = new Similarity();
			sim.similarity = 0.0;
			sim.nameA = cfp1.getIdentifier();
			sim.nameB = cfp2.getIdentifier();
		} else {

			CausalFootprintSimilarityResult footPrintSimilarity = new CausalFootprintSimilarityResult(
					cfp1, cfp2, mapping);
			sim = footPrintSimilarity.calculateSimilarity(progress,
					removeInitialAndFinalNodes, similarityMap);
			footPrintSimilarity = null;
		}

		return sim;
	}

	// private void addConditionalToList(ConfigurableEPC epc, String label,
	// ArrayList<ConfigurableEPC> epcs,
	// ArrayList<String> paths) {
	private void addConditionalToList(ConfigurableEPC epc,
			ArrayList<ConfigurableEPC> epcs) {
		boolean startEvent = false;
		boolean endEvent = false;
		Iterator<EPCEvent> it = epc.getEvents().iterator();
		while (!(startEvent && endEvent) && it.hasNext()) {
			EPCEvent e = it.next();
			startEvent |= e.inDegree() == 0;
			endEvent |= e.outDegree() == 0;
		}
		if (epc.getFunctions().size() > 0 && startEvent && endEvent
				&& epc.isValidEPC().length() == 0) {
			epcs.add(epc);
			// paths.add(label);
		} else {
			return;
		}
	}

	protected final List<ActivityContextFragment> getBaseFragments(int epc) {
		return getFragments(baseEpcs.get(epc));
	}

	protected final List<ActivityContextFragment> getCompareToFragments(int epc) {
		return getFragments(compareToEpcs.get(epc));
	}

	private List<ActivityContextFragment> getFragments(ConfigurableEPC epc) {
		ArrayList<ActivityContextFragment> fragments = new ArrayList<ActivityContextFragment>();

		for (EPCFunction function : (ArrayList<EPCFunction>) epc.getFunctions()) {
			ActivityContextFragment fragment = new ActivityContextFragment(
					function.getIdentifier());

			ArrayList<EPCEvent> pre = (ArrayList<EPCEvent>) epc
					.getPreceedingEvents(function);
			for (EPCEvent event : pre) {
				fragment.addToInputContext(event.getIdentifier());
			}

			ArrayList<EPCEvent> post = (ArrayList<EPCEvent>) epc
					.getSucceedingEvents(function);
			for (EPCEvent event : post) {
				fragment.addToOutputContext(event.getIdentifier());
			}
			fragment.setInType(ActivityContextFragment.OR);
			// functions to calculate type of input in EPCs are not available
			// yet.

			fragments.add(fragment);
		}

		return fragments;
	}
}

class FootprintBuilderThread extends org.processmining.framework.ui.SwingWorker {

	private FootprintSimilarityResult result;
	private Progress progress;
	private ActivityFragmentBuilder owner;
	private boolean removeInitialAndFinalNodes;
	private final boolean useSimilarityValues;
	private final SimilarityCalculator simCalc;

	public FootprintBuilderThread(FootprintSimilarityResult result,
			Progress progress, ActivityFragmentBuilder owner,
			boolean removeInitialAndFinalNodes, boolean useSimilarityValues,
			SimilarityCalculator simCalc) {
		this.simCalc = simCalc;
		this.result = result;
		this.progress = progress;
		this.owner = owner;
		this.removeInitialAndFinalNodes = removeInitialAndFinalNodes;
		this.useSimilarityValues = useSimilarityValues;
	}

	public Object construct() {
		int builder = owner.getNextBase();
		while (builder != -1) {
			// System.out.println("Memory used before starting comparison:
			// "+(java.lang.Runtime.getRuntime().totalMemory()-
			// java.lang.Runtime.getRuntime().freeMemory()));
			buildNextFootprintAndCompare(builder);

			builder = owner.getNextBase();
		}
		return null;
	}

	private void buildNextFootprintAndCompare(int baseIndex) {
		CausalFootprint footprint = result
				.getBaseFootPrint(baseIndex, progress);
		owner.setBaseFootprintReady(baseIndex);
		if (progress.isCanceled()) {
			return;
		}
		// existing[j] = true implies that epcs[j] is converted already and
		// hence
		// can be loaded from file.
		int totalDone = 0;
		boolean[] done = new boolean[owner.getCompareToEpcs().size()];
		Arrays.fill(done, false);
		CausalFootprint compFootprint;
		Similarity sim;

		while (totalDone != done.length && !progress.isCanceled()) {
			// for (int j = 0; !progress.isCanceled() && j <
			// owner.getCompareToEpcs().size(); j++) {
			// compute the similarities between baseIndex and j;
			int j = owner.getNextCompareTo(done);
			try {
				done[j] = true;
				totalDone++;
				compFootprint = result.getCompareToFootPrint(j, progress);
				owner.setCompFootprintReady(j);

				// System.out.println("Memory used after loading 2 footprints:
				// "+(java.lang.Runtime.getRuntime().totalMemory()-
				// java.lang.Runtime.getRuntime().freeMemory()));
				// Check if similarity has already been determined
				double similarity = result.getSimilarity(owner
						.getCompareToPath(j), owner.getBasePath(baseIndex));
				if (similarity < 0) {
					// similarity has not been determined yet.
					owner.getSimilarities().set(baseIndex, j,
							Similarities.BEING_PROCESSED);
					sim = owner.compare(baseIndex, j, footprint, compFootprint,
							owner.getBaseFragments(baseIndex), owner
									.getCompareToFragments(j), progress,
							removeInitialAndFinalNodes, useSimilarityValues,
							simCalc);
					compFootprint = null;
					owner.getSimilarities().set(baseIndex, j, sim.similarity);
					// Message.add("Comparison: "+ owner.comps+
					// ", base "+ baseIndex +
					// " to "+j+" = "+ sim.similarity,
					// Message.DEBUG);
				} else {
					// we already compared these two, or they are currently
					// being compared
					int copyFromCompareToIndex = result.getCompareToIndex(owner
							.getBasePath(baseIndex));
					int copyFromBaseIndex = result.getBaseIndex(owner
							.getCompareToPath(j));

					if (similarity != Similarities.BEING_PROCESSED) {
						owner.getSimilarities().set(baseIndex, j, similarity);
					} else {
						owner.addToInvalidEntryReplaceList(baseIndex, j,
								copyFromBaseIndex, copyFromCompareToIndex);
					}
				}
			} catch (OutOfMemoryError ex) {
				owner.getSimilarities().set(baseIndex, j, Similarities.INVALID);
				Message.add("Out of memory while comparing: "
						+ owner.getBaseEPCIdentifier(baseIndex) + " with "
						+ owner.getCompareToEPCIdentifier(j), Message.ERROR);
			}
			progress.inc();
		}
		return;
	}
}

class SimpleEntry<K, V> implements Map.Entry<K, V> {

	private final K key;
	private final V value;

	SimpleEntry(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public int hashCode() {
		return (getKey() == null ? 0 : getKey().hashCode())
				^ (getValue() == null ? 0 : getValue().hashCode());

	}

	public boolean equals(Object o) {
		if (!(o instanceof Map.Entry)) {
			return false;
		}
		Map.Entry<?, ?> e2 = (Map.Entry) o;
		return (getKey() == null ? e2.getKey() == null : getKey().equals(
				e2.getKey()))
				&& (getValue() == null ? e2.getValue() == null : getValue()
						.equals(e2.getValue()));

	}
}