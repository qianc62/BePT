package org.processmining.analysis.abstractions.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

public class AbstractionSetTheory {
	SimpleDirectedGraph<TreeSet<String>, DefaultEdge> sg;
	HashMap<String, TreeSet<String>> stringAlphabetSetMap;
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> alphabetMaximalElementMap;
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> maximalElementSubsumedElementMap;
	double commonElementsThreshold, differenceElementsThreshold;
	List<TreeSet<String>> maximalElementList;
	DiffStrategy d;

	class TreeSetComparator implements Comparator<TreeSet<String>> {
		public int compare(TreeSet<String> t1, TreeSet<String> t2) {
			return compare(t1.toString(), t2.toString());
		}

		int compare(String s1, String s2) {
			return s1.equals(s2) ? 0 : (s1.length() <= s2.length() ? (s1
					.length() < s2.length() ? 1 : s2.compareTo(s1)) : -1);
		}
	}

	public static enum DiffStrategy {
		MIN_SIZE, MAX_SIZE, SUM_SIZE
	}

	public AbstractionSetTheory() {
		sg = new SimpleDirectedGraph<TreeSet<String>, DefaultEdge>(
				DefaultEdge.class);
		maximalElementSubsumedElementMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();
	}

	public List<TreeSet<String>> getMaximalElementsSubsumption(
			Set<TreeSet<String>> alphabetSet) {
		List<TreeSet<String>> maxElementList = new ArrayList<TreeSet<String>>();

		stringAlphabetSetMap = new HashMap<String, TreeSet<String>>();

		for (TreeSet<String> alphabet : alphabetSet) {
			sg.addVertex(alphabet);
			stringAlphabetSetMap.put(alphabet.toString(), alphabet);
		}

		for (TreeSet<String> alphabetI : alphabetSet) {
			for (TreeSet<String> alphabetJ : alphabetSet) {
				if (!alphabetI.equals(alphabetJ)) {
					if (alphabetJ.containsAll(alphabetI)) {
						DijkstraShortestPath<TreeSet<String>, DefaultEdge> d = new DijkstraShortestPath<TreeSet<String>, DefaultEdge>(
								sg, alphabetI, alphabetJ);
						if (d.getPathEdgeList() == null)
							sg.addEdge(alphabetI, alphabetJ);
					} else if (alphabetI.containsAll(alphabetJ)) {
						DijkstraShortestPath<TreeSet<String>, DefaultEdge> d = new DijkstraShortestPath<TreeSet<String>, DefaultEdge>(
								sg, alphabetI, alphabetJ);
						if (d.getPathEdgeList() == null)
							sg.addEdge(alphabetJ, alphabetI);
					}
				}
			}
		}

		for (TreeSet<String> alphabet : alphabetSet)
			if (sg.outDegreeOf(alphabet) == 0)
				maxElementList.add(alphabet);

		return maxElementList;
	}

	public List<TreeSet<String>> getMaximalElementsApproximateSubsumption(
			Set<TreeSet<String>> alphabetSet, double commonElementsThreshold,
			double differenceElementsThreshold, DiffStrategy d) {
		this.commonElementsThreshold = commonElementsThreshold;
		this.differenceElementsThreshold = differenceElementsThreshold;
		this.d = d;

		List<TreeSet<String>> maxElementList = new ArrayList<TreeSet<String>>();

		stringAlphabetSetMap = new HashMap<String, TreeSet<String>>();
		sg = new SimpleDirectedGraph<TreeSet<String>, DefaultEdge>(
				DefaultEdge.class);
		for (TreeSet<String> alphabet : alphabetSet) {
			sg.addVertex(alphabet);
			stringAlphabetSetMap.put(alphabet.toString(), alphabet);
		}

		TreeSet<String> tempSet;
		int noCommonElements, noDifferenceElements;

		int commonThreshold, diffThreshold;
		int minSize;

		for (TreeSet<String> alphabetI : alphabetSet) {
			for (TreeSet<String> alphabetJ : alphabetSet) {
				if (!alphabetI.equals(alphabetJ)) {
					if (alphabetJ.containsAll(alphabetI)) {
						sg.addEdge(alphabetI, alphabetJ);
					}
				}
			}
		}

		for (TreeSet<String> alphabetI : alphabetSet) {
			for (TreeSet<String> alphabetJ : alphabetSet) {
				// if(!doneSet.contains(alphabetJ))
				if (!alphabetI.equals(alphabetJ)
						&& !alphabetJ.containsAll(alphabetI)) {
					if (alphabetJ.size() >= alphabetI.size()) {

						minSize = alphabetI.size();

						commonThreshold = (int) Math.ceil(minSize
								* commonElementsThreshold);
						diffThreshold = (int) Math.ceil(minSize
								* differenceElementsThreshold);

						tempSet = new TreeSet<String>();
						tempSet.addAll(alphabetI);
						tempSet.retainAll(alphabetJ);

						noCommonElements = tempSet.size();

						tempSet = new TreeSet<String>();
						tempSet.addAll(alphabetI);
						tempSet.removeAll(alphabetJ);

						noDifferenceElements = tempSet.size();
						if (noCommonElements >= commonThreshold
								&& noDifferenceElements <= diffThreshold) {

							if (alphabetI.size() < alphabetJ.size()) {
								sg.addEdge(alphabetI, alphabetJ);
							} else {
								if (sg.inDegreeOf(alphabetJ) >= sg
										.inDegreeOf(alphabetI)) {
									sg.addEdge(alphabetI, alphabetJ);
								} else {
									sg.addEdge(alphabetJ, alphabetI);
								}
							}
						}
					}
				}

			}
			// doneSet.add(alphabetI);
		}

		for (TreeSet<String> alphabet : alphabetSet) {
			if (sg.outDegreeOf(alphabet) == 0)
				maxElementList.add(alphabet);
		}

		maximalElementList = new ArrayList<TreeSet<String>>();
		maximalElementList.addAll(maxElementList);
		return maxElementList;
	}

	public HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> findMaximalElementSubsumedElementMap(
			Set<TreeSet<String>> alphabetSet,
			List<TreeSet<String>> maxElementList) {
		HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> maxElementSubsumedElementMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();
		TreeSet<TreeSet<String>> maximalElementSubsumedElementSet, alphabetMaximalElementSet;
		alphabetMaximalElementMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();

		for (TreeSet<String> maxElement : maxElementList) {
			maximalElementSubsumedElementSet = new TreeSet<TreeSet<String>>(
					new TreeSetComparator());
			for (TreeSet<String> alphabet : alphabetSet) {
				if (!maxElement.equals(alphabet)) {
					if (sg.containsEdge(alphabet, maxElement)) {
						maximalElementSubsumedElementSet.add(alphabet);
						if (alphabetMaximalElementMap.containsKey(alphabet)) {
							alphabetMaximalElementSet = alphabetMaximalElementMap
									.get(alphabet);
						} else {
							alphabetMaximalElementSet = new TreeSet<TreeSet<String>>(
									new TreeSetComparator());
						}
						alphabetMaximalElementSet.add(maxElement);
						alphabetMaximalElementMap.put(alphabet,
								alphabetMaximalElementSet);
					}
				} else {
					alphabetMaximalElementSet = new TreeSet<TreeSet<String>>(
							new TreeSetComparator());
					alphabetMaximalElementSet.add(maxElement);
					alphabetMaximalElementMap.put(alphabet,
							alphabetMaximalElementSet);
					maximalElementSubsumedElementSet.add(maxElement);
				}
			}
			maxElementSubsumedElementMap.put(maxElement,
					maximalElementSubsumedElementSet);
			maximalElementSubsumedElementMap.put(maxElement,
					maximalElementSubsumedElementSet);
		}

		HashSet<TreeSet<String>> tempSet = new HashSet<TreeSet<String>>();
		tempSet.addAll(alphabetSet);
		tempSet.removeAll(alphabetMaximalElementMap.keySet());
		// System.out.println("TempSet: "+tempSet.size());
		if (tempSet.size() > 0) {
			AbstractionSetTheory b = new AbstractionSetTheory();
			List<TreeSet<String>> bMax = b
					.getMaximalElementsApproximateSubsumption(tempSet,
							commonElementsThreshold,
							differenceElementsThreshold, d);

			// System.out.println("no. Max : "+bMax.size());
			// System.out.println(bMax);
			HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> bMaxSubMap = b
					.findMaximalElementSubsumedElementMap(tempSet, bMax);

			bMaxSubMap = b.getMaximalElementSubsumedElementMap();
			for (TreeSet<String> bm : bMaxSubMap.keySet()) {
				if (maximalElementSubsumedElementMap.containsKey(bm)) {
					maximalElementSubsumedElementSet = maximalElementSubsumedElementMap
							.get(bm);
				} else {
					maximalElementSubsumedElementSet = new TreeSet<TreeSet<String>>(
							new TreeSetComparator());
				}
				maximalElementSubsumedElementSet.addAll(bMaxSubMap.get(bm));
				maximalElementSubsumedElementMap.put(bm,
						maximalElementSubsumedElementSet);
			}

			HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> bAMEMap = b
					.getAlphabetMaximalElementMap();
			for (TreeSet<String> alp : bAMEMap.keySet()) {
				if (alphabetMaximalElementMap.containsKey(alp)) {
					alphabetMaximalElementSet = alphabetMaximalElementMap
							.get(alp);
				} else {
					alphabetMaximalElementSet = new TreeSet<TreeSet<String>>(
							new TreeSetComparator());
				}
				alphabetMaximalElementSet.addAll(bAMEMap.get(alp));
				alphabetMaximalElementMap.put(alp, alphabetMaximalElementSet);
			}
			maximalElementList.addAll(b.getMaximalElementList());
		}

		return maxElementSubsumedElementMap;
	}

	public HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> getMaximalElementSubsumedElementMap() {
		return maximalElementSubsumedElementMap;
	}

	public List<TreeSet<String>> getMaximalElementList() {
		return maximalElementList;
	}

	public HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> getAlphabetMaximalElementMap() {
		return alphabetMaximalElementMap;
	}

	public HashMap<Integer, TreeSet<TreeSet<String>>> getSizeAlphabetSetMap(
			Collection<TreeSet<String>> alphabetSet) {
		HashMap<Integer, TreeSet<TreeSet<String>>> sizeAlphabetSetMap = new HashMap<Integer, TreeSet<TreeSet<String>>>();
		int size;
		TreeSet<TreeSet<String>> sizeAlphabetSet;
		for (TreeSet<String> maximalElement : alphabetSet) {
			size = maximalElement.size();
			if (sizeAlphabetSetMap.containsKey(size))
				sizeAlphabetSet = sizeAlphabetSetMap.get(size);
			else
				sizeAlphabetSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
			sizeAlphabetSet.add(maximalElement);
			sizeAlphabetSetMap.put(size, sizeAlphabetSet);
		}
		return sizeAlphabetSetMap;
	}
}
