package org.processmining.framework.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * This file constructs the SuffixTree based on Ukkonnen's online construction
 * algorithm; It also identifies the tandem repeats (or squares) using the LZ
 * decomposition proposed by Gusfield et al.
 * 
 * Further this file also contains methods to determine the maximal and
 * uper-maximal repeats based on Gusfield's algorithm.
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */
class SuffixNode {
	SuffixNode parent; // The parent of this node
	SuffixNode suffixLink; // The suffix link signifies the the link to the
	// node that forms the largest suffix of the current
	// node
	SuffixNode child; // The first child of this node
	ArrayList<SuffixNode> children; // The list of all children
	int edgeLabelStart; // Start index of the incoming edge
	int edgeLabelEnd; // End index of the incoming edge
	int pathPosition; // Index of the start position of the node's path; A

	// path of a node is a string from the root to that
	// node.
	boolean isLeftDiverse;
	boolean processed;
	String leftSymbol;

	/*
	 * Create Root Node
	 */
	public SuffixNode() {
		parent = null;
		suffixLink = null;
		child = null;
		children = new ArrayList<SuffixNode>();
		edgeLabelStart = edgeLabelEnd = 0;
		pathPosition = 0;
	}

	/*
	 * Create a new SuffixNode Input: The parent of the node, the starting and
	 * ending indices of the incoming edge to that node, the path starting
	 * position of the node.
	 */
	public SuffixNode(SuffixNode parent, int labelStart, int labelEnd,
			int position) {
		this.parent = parent;
		edgeLabelStart = labelStart;
		edgeLabelEnd = labelEnd;
		pathPosition = position;

		children = new ArrayList<SuffixNode>();
		child = null;
		suffixLink = null;
	}

	/*
	 * Add a child to this node
	 */
	public void addChildren(SuffixNode node) {
		if (!children.contains(node))
			children.add(node);
	}
}

class SuffixTreePath {
	int begin;
	int end;

	public SuffixTreePath() {
		this.begin = 0;
		this.end = 0;
	}
}

/*
 * SuffixTreePos is a combination of the source node and the position in its
 * incoming edge where suffix ends
 */
class SuffixTreePos {
	SuffixNode node;
	int edgePosition; // used for storing the index relative to a specific

	// edge where last match occurred

	public SuffixTreePos() {
		node = new SuffixNode();
		edgePosition = 0;
	}
}

// This class holds the return values of the SPA procedure

class SPAResult {
	int extension;
	boolean repeatedExtension;

	public SPAResult(int extension, boolean repeatedExtension) {
		this.extension = extension;
		this.repeatedExtension = repeatedExtension;
	}
}

// This class holds the return values of the TraceSingleEdge function
class TraceSingleEdgeResult {
	int noEdgeSymbolsFound; // This variable will hold the number of matching
	// characters found in the current edge.
	boolean searchDone;

	public TraceSingleEdgeResult(int noEdgeSymbolsFound, boolean searchDone) {
		this.noEdgeSymbolsFound = noEdgeSymbolsFound;
		this.searchDone = searchDone;
	}
}

class DescComparator implements Comparator<Object> {
	public int compare(Object o1, Object o2) {
		int result = ((Integer) o1).compareTo(((Integer) o2));
		return result * (-1);
	}
}

class DescStrComparator implements Comparator<String> {
	public int compare(String s1, String s2) {
		int result = s1.compareTo(s2);
		return result * (-1);
	}
}

class AscStrComparator implements Comparator<String> {
	public int compare(String s1, String s2) {
		return s1.compareTo(s2);
	}
}

public class UkkonenSuffixTree {
	SuffixNode root; // The root of the suffix tree
	SuffixNode suffixLess; // The node that has no suffix link yet. It will
	// have one by the end of the current phase. */

	SuffixTreePath path; //
	SuffixTreePos pos;
	String terminationSymbol;
	String sequence; // The input string for which the suffix tree is to be
	// generated
	int sequenceLength; // The length of the input string including the EOS
	int encodingLength; // The encoding length used for mapping
	int e; // The virtual end of all leaves
	int phase;
	static final boolean debug = false;
	static final String EOS = ".";
	static final String prefix = ".";
	SuffixNode[] leaves;

	ArrayList<RepeatsInfo> repeatsInfo;
	TreeSet<Integer>[] tandemPairs;
	TreeSet<String> repeatTypes;

	TreeMap<String, String> repeatPrimitiveRepeatMap;

	public UkkonenSuffixTree(String seq, int encodingLength) {
		// Logger.printCall("Entering Constructor UkkonenSuffixTree");

		// System.out.println(seq);
		this.encodingLength = encodingLength;
		terminationSymbol = EOS;

		// Adjust the sequence to start from index 1 to N rather than from 0 to
		// N;
		this.sequence = prefix;
		for (int i = 1; i < encodingLength; i++) {
			terminationSymbol += EOS;
			this.sequence += prefix;
		}

		// Copy the input string from index 1 to N; and Add the unique
		// terminating symbol at the end
		this.sequence += seq.concat(terminationSymbol);
		// Logger.println("Input Seq: " + this.sequence);
		sequenceLength = seq.length() / encodingLength + 1; // including the EOS

		pos = new SuffixTreePos();

		leaves = new SuffixNode[sequenceLength];

		createTree();
		// printTree();
		// findLeaves(root);
		// Logger.println("Leaves");
		// for (int i = 0; i < sequenceLength; i++) {
		// pos.node = leaves[i];
		// followSuffixLink();
		// Logger.println(leaves[i].pathPosition + " "
		// + leaves[i].edgeLabelStart + " " + pos.node.edgeLabelStart);
		// }

		// Logger.printReturn("Exiting Constructor UkkonenSuffixTree");
	}

	public ArrayList<RepeatsInfo> getRepeatsInfo() {
		return repeatsInfo;
	}

	public void setRepeatsInfo(ArrayList<RepeatsInfo> repeatsInfo) {
		this.repeatsInfo = repeatsInfo;
	}

	public TreeMap<String, String> getRepeatPrimitiveRepeatMap() {
		return repeatPrimitiveRepeatMap;
	}

	public void setRepeatPrimitiveRepeatMap(
			TreeMap<String, String> repeatPrimitiveRepeatMap) {
		this.repeatPrimitiveRepeatMap = repeatPrimitiveRepeatMap;
	}

	private void createTree() {
		// Logger.printCall("Entering createTree");

		int extension;
		/* Allocating the tree root node */
		root = new SuffixNode();

		// Initializing algorithm parameters
		phase = 2;
		extension = 2;

		// Allocation first node; phase 0

		root.child = new SuffixNode(root, 1, sequenceLength, 1);
		// root.child.setCV(0);
		root.addChildren(root.child);
		// leaves[0] = root.child;
		suffixLess = null;
		pos.node = root;
		pos.edgePosition = 0;
		// printTree();
		// Ukkonen's algorithm begins here

		boolean repeatedExtension = false;
		for (; phase < sequenceLength; phase++) {
			SPAResult result = SPA(phase, extension, repeatedExtension);
			// Logger.println("Phase: "+phase+" Extension Before: "+extension+"
			// Extension After: "+result.extension);
			extension = result.extension;
			repeatedExtension = result.repeatedExtension;
			// Logger.println("RepeatedExtension After Phase: " + phase + " = "
			// + repeatedExtension + " extension: " + extension);
		}

		// Logger.printReturn("Exiting createTree");
	}

	/*
	 * Performs all insertions of a single phase by calling function SEA
	 * starting from the first extension that does not already exist in the tree
	 * and ending at the first extension that already exists in the tree.
	 * 
	 * Input: 1. The phase number 2. The first extension number of this phase 3.
	 * A flag (repeatedExtension) signaling whether the extension is the first
	 * of this phase, after the last phase ended with rule 3. If so - extension
	 * will be executed again in this phase, and thus its suffix link would not
	 * be followed. 4. The global variable pos signifying the node and position
	 * in its incoming edge where extension begins
	 * 
	 * Output: 1. The extension number that was last executed on this phase.
	 * Next phase will start from it and not from 1. 2. The flag
	 * repeatedExtension (set to true if rule 3 is applied)
	 */

	private SPAResult SPA(int phase, int extension, boolean repeatedExtension) {
		// Logger.printCall("Entering SPA");
		if (debug) {
			Logger.println("Phase: " + phase + " Extension: " + extension
					+ " RepeatedExtension: " + repeatedExtension);
			Logger
					.println("Pos.Node: (" + pos.node.edgeLabelStart + ","
							+ pos.node.edgeLabelEnd + ","
							+ pos.node.pathPosition + ")");
		}
		int ruleApplied = 0; // The rule applied for this extension
		SuffixTreePath str = new SuffixTreePath();

		/* Leafs Trick: Apply implicit extensions 1 through prev_phase */
		this.e = phase + 1;

		while (extension <= phase + 1) {
			str.begin = extension;
			str.end = phase + 1;

			/* Call Single-Extension-Algorithm */
			ruleApplied = SEA(str, repeatedExtension);

			/* Check if rule 3 was applied for the current extension */
			if (ruleApplied == 3) {
				// Signaling that the next phase's first extension will not
				// follow a suffix link because same extension is applied
				repeatedExtension = true;
				break;
			}
			repeatedExtension = false;
			extension++;
		}

		// Logger.printReturn("Exiting SPA, Returning Extension: " + extension
		// + " RepeatedExtension: " + repeatedExtension);
		return new SPAResult(extension, repeatedExtension);
	}

	/*
	 * Single Extension Algorithm
	 * 
	 * Ensure that a certain extension is in the tree.
	 * 
	 * 1. Follows the current node's suffix link. 2. Check whether the rest of
	 * the extension is in the tree. 3. If it is - reports the calling function
	 * SPA of rule 3 (=> current phase is done). 4. If it's not - inserts it by
	 * applying rule 2.
	 * 
	 * Input: (Global) pos - the node and position in its incoming edge where
	 * extension begins, str - the starting and ending indices of the extension,
	 * a flag indicating whether the last phase ended by rule 3(last extension
	 * of the last phase already existed in the tree - and if so, the current
	 * phase starts at not following the suffix link of the first extension).
	 * 
	 * Output: The rule that was applied. Can be 3 (phase is done) or 2 (a new
	 * leaf was created).
	 */

	private int SEA(SuffixTreePath str, boolean afterRule3) {
		int pathPosition = str.begin;
		int ruleApplied = 0;

		SuffixNode tmp = new SuffixNode();

		if (debug) {
			printTree();
			Logger.print("Extension: " + str.begin + " Phase+1: " + str.end);
			if (!afterRule3) {
				Logger.print("  followed from (" + pos.node.edgeLabelStart
						+ "," + getNodeLabelEnd(pos.node) + " | "
						+ pos.edgePosition + ")");
			} else {
				Logger.print("  starting at (" + pos.node.edgeLabelStart + ","
						+ getNodeLabelEnd(pos.node) + " | " + pos.edgePosition
						+ ")");
			}
		}

		if (!afterRule3)
			followSuffixLink();
		// System.out.println("pos.edgepos " + pos.edgePosition);

		int noSymbolsFound = 0;
		if (pos.node == root) {
			// System.out.println("pos.node == root");
			noSymbolsFound = traceString(str, false);
			// System.out.println("pos.node.start " + pos.node.edgeLabelStart
			// + "," + pos.node.edgeLabelEnd);
			// System.out.println("noSymbolsfound : " + noSymbolsFound);
		} else {
			str.begin = str.end;
			noSymbolsFound = 0;
			if (isLastSymbolInEdge()) {
				tmp = findChild(sequence.substring(str.end * encodingLength,
						(str.end + 1) * encodingLength));
				if (tmp != null) {
					pos.node = tmp;
					pos.edgePosition = 0;
					noSymbolsFound = 1;
				}
			} else {
				int tempindex = pos.node.edgeLabelStart + pos.edgePosition + 1;
				if (sequence.substring(tempindex * encodingLength,
						(tempindex + 1) * encodingLength).equals(
						sequence.substring(str.end * encodingLength,
								(str.end + 1) * encodingLength))) {
					pos.edgePosition++;
					noSymbolsFound = 1;
					if (debug)
						Logger.println("Changing edgePosition to: "
								+ pos.edgePosition);
				}
			}
		}

		// System.out.println("str.begin: " + str.begin + " str.end: " +
		// str.end);
		if (noSymbolsFound == str.end - str.begin + 1) {
			ruleApplied = 3;
			if (suffixLess != null) {
				createSuffixLink(suffixLess, pos.node.parent);
				suffixLess = null;
			}
			if (debug)
				System.out.println(" rule 3 (" + str.begin + "," + str.end
						+ ")");
			return ruleApplied;
		}

		if (isLastSymbolInEdge() || pos.node == root) {
			if (pos.node.child != null) {
				// System.out.println("HERE");
				applyExtensionRule2(str.begin + noSymbolsFound, str.end,
						pathPosition, 0, true);
				ruleApplied = 2;
				if (suffixLess != null) {
					createSuffixLink(suffixLess, pos.node);
					suffixLess = null;
				}
			}
		} else {
			applyExtensionRule2(str.begin + noSymbolsFound, str.end,
					pathPosition, pos.edgePosition, false);
			if (suffixLess != null)
				createSuffixLink(suffixLess, pos.node);

			if (getNodeLabelLength(pos.node) == 1 && pos.node.parent == root) {
				pos.node.suffixLink = root;
				suffixLess = null;
			} else
				suffixLess = pos.node;

			ruleApplied = 2;
		}

		return ruleApplied;
	}

	/*
	 * applyExtensionRule2 : Apply "extension rule 2" in 2 cases: 1. A new son
	 * (leaf 4) is added to a node that already has sons: (1) (1) / \ -> / | \
	 * (2) (3) (2)(3)(4)
	 * 
	 * 2. An edge is split and a new leaf (2) and an internal node (3) are
	 * added: | | (3) | / \ (1) (1) (2)
	 * 
	 * Input : pos: signifies the node 1 edgeLabelBegin: Start index of node 4's
	 * or 2's incoming edge edgeLabelEnd: End index of node 4's or 2's incoming
	 * edge pathPosition: Path start index of node 4 or 2's edgePos: Position in
	 * node 1's incoming edge where split is to be performed
	 * 
	 * Output: The newly created leaf (new_son case) or internal node (split
	 * case).
	 */

	private void applyExtensionRule2(int edgeLabelBegin, int edgeLabelEnd,
			int pathPosition, int edgePos, boolean newson) {
		// Logger.printCall("Entering applyExtensionRule2");

		SuffixNode newLeaf, newInternal;

		/* newSon */
		if (newson) {
			if (debug) {
				Logger.println("rule 2: new leaf (" + edgeLabelBegin + ","
						+ edgeLabelEnd + ")");
			}
			/* Create a new leaf (4) with the symbols of the extension */
			newLeaf = new SuffixNode(pos.node, edgeLabelBegin, edgeLabelEnd,
					pathPosition);

			/* Connect new_leaf (4) as the new son of node (1) */
			pos.node.addChildren(newLeaf);
			/* return (4) */
			// Logger.printReturn("Exiting applyExtensionRule2");
			// Logger.println("Setting Leaf: "+(pathPosition-1));
			// leaves[pathPosition-1] = newLeaf;
			// Logger.println(leaves[pathPosition-1].pathPosition+"
			// "+leaves[pathPosition-1].edgeLabelStart+"
			// "+leaves[pathPosition-1].edgeLabelEnd);
			return;
			// return newLeaf;
		}

		/*-------split-------*/
		if (debug)
			Logger.println("rule 2: split (" + edgeLabelBegin + ","
					+ edgeLabelEnd + ")");

		/* Create a new internal node (3) at the split point */
		newInternal = new SuffixNode(pos.node.parent, pos.node.edgeLabelStart,
				pos.node.edgeLabelStart + edgePos, pos.node.pathPosition);

		/*
		 * Update the node (1) incoming edge starting index (it now starts where
		 * node (3) incoming edge ends)
		 */
		pos.node.edgeLabelStart += edgePos + 1;

		/* Create a new leaf (2) with the characters of the extension */
		newLeaf = new SuffixNode(newInternal, edgeLabelBegin, edgeLabelEnd,
				pathPosition);
		// Logger.println("Setting leaf: "+(pathPosition-1));
		// leaves[pathPosition-1] = newLeaf;

		/*
		 * 1 is no longer the child of its parent. remove it from the children
		 * list;
		 */
		pos.node.parent.children.remove(pos.node);

		/* Set 3 to be a child of 1's parent */
		pos.node.parent.addChildren(newInternal);

		/*
		 * Set the first child; If 1 was the first child, then change the first
		 * child to 3
		 */
		if (newInternal.parent.child == pos.node) {
			// newInternal.parent.addChildren(newInternal);
			// newInternal.parent.children.remove(pos.node);
			newInternal.parent.child = newInternal;
		}

		/*
		 * Add 1 as a child of 3 Set 1 as the first child of 3
		 */
		newInternal.addChildren(pos.node);
		newInternal.child = pos.node;

		/*
		 * Set 1's parent to 3
		 */
		pos.node.parent = newInternal;

		/*
		 * Add 2 as a child to 3
		 */
		newInternal.addChildren(newLeaf);

		/*
		 * Set the node to explore to 3 and return 3
		 */
		pos.node = newInternal;

		// Logger.printReturn("Exiting applyExtensionRule2");
		// return newInternal;
	}

	/*
	 * followSuffixLink: Follow the suffix link of the source node
	 * 
	 * Input : pos is a combination of the source node and the position in its
	 * incoming edge where suffix ends. Output: The destination node that
	 * represents the longest suffix of node's path. Example: if node represents
	 * the path "abcde" then it returns the node that represents "bcde".
	 */
	private void followSuffixLink() {

		// Logger.printCall("Entering followSuffixLink");

		// gamma is the string between node and its father if it doesn't have a
		// suffix link
		SuffixTreePath gamma = new SuffixTreePath();
		if (pos.node == root) {
			// Logger.printReturn("Exiting followSuffixLink");
			return;
		}

		/*
		 * If node has no suffix link yet or in the middle of an edge - remember
		 * the edge between the node and its father (gamma) and follow its
		 * father's suffix link (it must have one by Ukkonen's lemma). After
		 * following, trace down gamma - it must exist in the tree (and thus can
		 * use the skip trick - see trace_string function description)
		 */

		if (pos.node.suffixLink == null || !isLastSymbolInEdge()) {
			/*
			 * If the node's father is the root, then no use following it's link
			 * (it is linked to itself). Tracing from the root (like in the
			 * naive algorithm) is required and is done by the calling function
			 * SEA upon receiving a return value of root from this function
			 */
			if (pos.node.parent == root) {
				pos.node = root;
				// Logger.printReturn("Exiting followSuffixLink");
				return;
			}

			/* Store gamma - the indices of node's incoming edge */
			gamma.begin = pos.node.edgeLabelStart;
			gamma.end = pos.node.edgeLabelStart + pos.edgePosition;

			/* Follow father's suffix link */
			pos.node = pos.node.parent.suffixLink;

			/*
			 * Down-walk gamma back to suffix_link's child; pos is updated
			 * internally in traceString
			 */
			traceString(gamma, true);
		} else {
			/* If a suffix link exists - just follow it */
			pos.node = pos.node.suffixLink;
			pos.edgePosition = getNodeLabelLength(pos.node) - 1;
		}

		// Logger.printReturn("Exiting followSuffixLink");
	}

	/*
	 * Traces for a string in the tree. This function is used in construction
	 * process only, and not for after-construction search of substrings. It is
	 * tailored to enable skipping (when we know a suffix is in the tree (when
	 * following a suffix link) we can avoid comparing all symbols of the edge
	 * by skipping its length immediately and thus save atomic operations - see
	 * Ukkonen's algorithm, skip trick). This function, in contradiction to the
	 * function traceSingleEdge, 'sees' the whole picture, meaning it searches a
	 * string in the whole tree and not just in a specific edge.
	 * 
	 * Input : The string, given in indices of the main string (str). pos.node
	 * is the node to start from pos.edgePos is the last matching position in
	 * edge
	 * 
	 * Output: number of characters found
	 */

	private int traceString(SuffixTreePath str, boolean skip) {

		// Logger.printCall("Entering traceString (" + pos.node.edgeLabelStart
		// + "," + pos.node.edgeLabelEnd + "," + pos.node.pathPosition
		// + ") (" + str.begin + "," + str.end + ") ");

		/*
		 * This variable will be true when search is done. It is a return value
		 * from function traceSingleEdge
		 */
		boolean searchDone = false;

		int noSymbolsFound = 0;

		int strBegin = str.begin;
		while (!searchDone) {
			pos.edgePosition = 0;
			TraceSingleEdgeResult result = traceSingleEdge(str, skip);
			str.begin += result.noEdgeSymbolsFound;
			noSymbolsFound += result.noEdgeSymbolsFound;
			searchDone = result.searchDone;
			// System.out.println("str.begin: " + str.begin);
		}
		// str is passed by value; so reset the value
		str.begin = strBegin;

		// Logger.printReturn("Exiting traceString");

		return noSymbolsFound;
	}

	/*
	 * traceSingleEdge : Traces for a string in a given node's OUTcoming edge.
	 * It searches only in the given edge and not other ones. Search stops when
	 * either whole string was found in the given edge, a part of the string was
	 * found but the edge ended (and the next edge must be searched too -
	 * performed by function traceString) or one non-matching character was
	 * found.
	 * 
	 * Input : The string to be searched, given in indices of the main string
	 * (in str). pos.node is the node to start from pos.edgePos holds the last
	 * matching position in edge
	 * 
	 * Output: (global) the node where tracing has stopped and the edge position
	 * where last match occurred; the string position where last match occurred,
	 * number of characters found, a flag for signaling whether search is done,
	 * and a flag to signal whether search stopped at a last character of an
	 * edge.
	 */
	private TraceSingleEdgeResult traceSingleEdge(SuffixTreePath str,
			boolean skip) {
		// Logger.printCall("Entering traceSingleEdge");

		SuffixNode contNode;
		int length, strlen;

		/* Set default return values */
		int noEdgeSymbolsFound = 0;
		boolean searchDone = true;
		pos.edgePosition = 0;

		/*
		 * Search for the first character of the string in the outcoming edge of
		 * node
		 */

		contNode = findChild(this.sequence.substring(
				str.begin * encodingLength, (str.begin + 1) * encodingLength));

		if (contNode == null) {
			/* Search is done, string not found */

			pos.edgePosition = getNodeLabelLength(pos.node) - 1;
			noEdgeSymbolsFound = 0;
			if (debug) {
				Logger.println("String not found");
				Logger.println("pos.edgePos: " + pos.edgePosition);
			}
			// Logger.printReturn("Exiting traceSingleEdge");
			return new TraceSingleEdgeResult(noEdgeSymbolsFound, searchDone);
		}

		/* Found first character - prepare for continuing the search */
		pos.node = contNode;
		length = getNodeLabelLength(pos.node);
		strlen = str.end - str.begin + 1;

		/* Compare edge length and string length. */
		/*
		 * If edge is shorter then the string being searched and skipping is
		 * enabled - skip edge
		 */
		int edgePos;
		if (skip) {
			if (length <= strlen) {
				noEdgeSymbolsFound = length;
				pos.edgePosition = length - 1;
				if (length < strlen)
					searchDone = false;
			} else {
				noEdgeSymbolsFound = strlen;
				pos.edgePosition = strlen - 1;
			}
			// Logger.printReturn("Exiting traceSingleEdge");
			return new TraceSingleEdgeResult(noEdgeSymbolsFound, searchDone);
		} else {
			/* Find minimum out of edge length and string length, and scan it */
			if (strlen < length) {
				length = strlen;
			}

			pos.edgePosition = 1;
			for (edgePos = 1, noEdgeSymbolsFound = 1; edgePos < length; edgePos++, noEdgeSymbolsFound++) {
				/*
				 * Compare current characters of the string and the edge. If
				 * equal - continue
				 */
				if (!sequence.substring(
						(pos.node.edgeLabelStart + edgePos) * encodingLength,
						(pos.node.edgeLabelStart + edgePos + 1)
								* encodingLength).equals(
						sequence.substring((str.begin + edgePos)
								* encodingLength, (str.begin + edgePos + 1)
								* encodingLength))) {
					edgePos--;
					pos.edgePosition = edgePos;
					// Logger.printReturn("Exiting traceSingleEdge");
					return new TraceSingleEdgeResult(noEdgeSymbolsFound,
							searchDone);
				}
			}
		}

		/* The loop has advanced edgePosition one too much */
		pos.edgePosition = edgePos;
		pos.edgePosition--;
		if (noEdgeSymbolsFound < strlen) {
			/* Search is not done yet */
			searchDone = false;
		}

		// Logger.printReturn("Exiting traceSingleEdge");
		return new TraceSingleEdgeResult(noEdgeSymbolsFound, searchDone);
	}

	/*
	 * findChild : Finds the child of node that starts with a certain symbol.
	 * 
	 * Input :The node to start searching from and the symbol to be searched in
	 * the sons.
	 * 
	 * Output: The child node if it exists, null if no such child.
	 */
	private SuffixNode findChild(String symbol) {

		if (debug)
			Logger.println("Entering findChild looking for symbol: " + symbol);

		int i;
		for (i = 0; i < pos.node.children.size(); i++) {
			// Get the first symbol on the edge connecting this node and its
			// child; Check if it equals the symbol we are looking for; If yes,
			// we are done- found the child that we are interested in
			if (sequence.substring(
					pos.node.children.get(i).edgeLabelStart * encodingLength,
					(pos.node.children.get(i).edgeLabelStart + 1)
							* encodingLength).equals(symbol)) {
				break;
			}
		}
		// Have we found the child?
		if (i < pos.node.children.size()) {
			// Logger.println("Child Found");
			pos.node = pos.node.children.get(i);
			return pos.node;
		} else {
			return null;
		}
	}

	/*
	 * This function prints the tree. It simply starts the recursive function
	 * printNode with depth 0 (the root).
	 */
	private void printTree() {
		Logger.println("Root");
		printNode(root, 0);
	}

	/*
	 * Prints a subtree under a node of a certain tree-depth.
	 * 
	 * Input : The node that is the root of the subtree, and the depth of that
	 * node. The depth is used for printing the branches that are coming from
	 * higher nodes and only then the node itself is printed. This gives the
	 * effect of a tree on screen. In each recursive call, the depth is
	 * increased.
	 */
	private void printNode(SuffixNode node, int depth) {
		int d = depth;
		int start = node.edgeLabelStart;
		int end = getNodeLabelEnd(node);

		if (depth > 0) {
			/* Print the branches coming from higher nodes */
			while (d > 1) {
				System.out.print("|");
				d--;
			}
			System.out.print("+");
			/* Print the node itself */
			while (start <= end) {
				Logger.print(sequence.substring(start * encodingLength,
						(start + 1) * encodingLength));
				start++;
			}
			Logger.print("(" + node.pathPosition + "- " + node.edgeLabelStart
					+ "," + end + ")");
			// if(node.suffixLink != null){
			// Logger.print("("+node.suffixLink.edgeLabelStart+"-"+node.suffixLink.pathPosition
			// +" )");
			// }
			if (debug)
				Logger.print("\t\t\t (" + node.edgeLabelStart + "," + end
						+ " |" + node.pathPosition + ")");

			System.out.println();
		}

		/* Recursive call for all node's children */
		for (int i = 0; i < node.children.size(); i++) {
			printNode(node.children.get(i), depth + 1);
		}
	}

	/*
	 * getNodeLabelEnd: Return the end index of the incoming edge to that node.
	 * 
	 * This function is needed because for leaves the end index is not relevant,
	 * instead we must look at the variable "e" (the global virtual end of all
	 * leaves). Never refer directly to a leaf's end-index.
	 * 
	 * Input : The node its end index we need.
	 * 
	 * Output: The end index of that node (meaning the end index of the node's
	 * incoming edge).
	 */
	private int getNodeLabelEnd(SuffixNode node) {

		// If it's a leaf - return e
		if (node.child == null)
			return this.e;
		// If it's not a leaf - return its real end
		return node.edgeLabelEnd;
	}

	/*
	 * getNodeLabelLength: returns the length of the incoming edge to that node.
	 * Uses getNodeLabelEnd
	 * 
	 * Input : The node its length we need.
	 * 
	 * Output: The length of that node.
	 */
	private int getNodeLabelLength(SuffixNode node) {
		return getNodeLabelEnd(node) - node.edgeLabelStart + 1;
	}

	/*
	 * isLastSymbolInEdge: Returns true if edgePosition is the last position in
	 * node's incoming edge.
	 */
	private boolean isLastSymbolInEdge() {
		if (pos.edgePosition == getNodeLabelLength(pos.node) - 1)
			return true;
		return false;
	}

	/*
	 * createSuffixLink : Creates a suffix link between node and the node 'link'
	 * which represents its largest suffix. The function could be avoided but is
	 * needed to monitor the creation of suffix links when debugging or changing
	 * the tree.
	 * 
	 * Input : The node to link from, the node to link to.
	 */
	private void createSuffixLink(SuffixNode node, SuffixNode link) {
		node.suffixLink = link;
	}

	protected int search(String searchString) {
		int k, j = 0, nodeLabelEnd;

		int searchStringLength = searchString.length() / encodingLength;
		pos.node = findChild(searchString.substring(0, encodingLength));
		if (pos.node == null)
			return -1;

		/*
		 * Scan nodes down from the root until a leaf is reached or the
		 * substring is found
		 */
		while (pos.node != null) {
			k = pos.node.edgeLabelStart;
			nodeLabelEnd = getNodeLabelEnd(pos.node);

			/*
			 * Scan a single edge - compare each character with the searched
			 * string
			 */
			while (j < searchStringLength
					&& k <= nodeLabelEnd
					&& sequence.substring(k * encodingLength, (k + 1)
							* encodingLength) == searchString.substring(j
							* encodingLength, (j + 1) * encodingLength)) {
				j++;
				k++;
			}

			if (j == searchStringLength) {
				return pos.node.pathPosition;
			} else if (k > nodeLabelEnd) {
				pos.node = findChild(searchString.substring(j * encodingLength,
						(j + 1) * encodingLength));
			} else {
				return -1;
			}
		}

		return -1;
	}

	// @SuppressWarnings("unchecked")
	public void LZDecomposition() {

		// Logger.printCall("Entering LZDecomposition");

		// printTree();
		int[] s = new int[sequenceLength];
		int[] l = new int[sequenceLength];

		tandemPairs = new TreeSet[sequenceLength];
		for (int i = 0; i < sequenceLength; i++)
			tandemPairs[i] = new TreeSet<Integer>(new DescComparator());

		String currentSymbol;
		s[0] = 0;
		l[0] = 0;
		int j;
		for (int i = 2; i <= sequenceLength; i++) {
			j = i - 1;

			currentSymbol = this.sequence.substring(i * encodingLength, (i + 1)
					* encodingLength);
			// if(j == 5){
			// Logger.println("currentSymbol: "+currentSymbol);
			// }
			pos.node = root;
			SuffixNode childNode = findChild(currentSymbol);
			if (childNode == null) {
				Logger.println("Something terribly Wrong; Current Symbol `"
						+ currentSymbol + "' not found");
				// break;
				System.exit(0);
			}

			// int noMatches = 1;
			int noMatches = childNode.edgeLabelEnd - childNode.edgeLabelStart
					+ 1;
			if (i != childNode.pathPosition) {
				// Logger.println("IF: "+noMatches);
				l[j] = noMatches;
				s[j] = childNode.pathPosition;
			} else {
				l[j] = 0;
				s[j] = 0;
				// Logger.println("Continue");
				continue;
			}
			int prevPathPos = childNode.pathPosition;
			String nextSymbol;
			// Logger.println("Index: "+i+" CurrentSymbol: "+currentSymbol);
			// added new the next line; delete if needed
			i += noMatches - 1;
			while (++i < sequenceLength) {
				nextSymbol = sequence.substring(i * encodingLength, (i + 1)
						* encodingLength);
				// Logger.print(nextSymbol+" ");
				pos.node = childNode;
				childNode = findChild(nextSymbol);
				if (childNode == null) {
					break;
				}
				// Logger.print("i: "+i+" childNodeStart:
				// "+childNode.edgeLabelStart+" childNodeEnd:
				// "+childNode.edgeLabelEnd);
				if (childNode.edgeLabelStart < i) {
					i += childNode.edgeLabelEnd - childNode.edgeLabelStart;
					noMatches += childNode.edgeLabelEnd
							- childNode.edgeLabelStart + 1;
					prevPathPos = childNode.pathPosition;
				} else {
					break;
				}
			}
			// Logger.println("");
			s[j] = prevPathPos;
			l[j] = noMatches;
			i = j + 1;
		}

		// for (int i = 1; i < sequenceLength; i++)
		// Logger.print(sequence.substring(i * encodingLength, (i + 1)
		// * encodingLength)
		// + " ");
		// Logger.println("");
		// for (int i = 1; i < sequenceLength; i++)
		// Logger.print(s[i - 1] + " ");
		// Logger.println("");
		// for (int i = 1; i < sequenceLength; i++)
		// Logger.print(l[i - 1] + " ");
		// Logger.println("");

		// Vector of Blocks
		ArrayList<Integer> I = new ArrayList<Integer>();

		I.add(1);
		j = 0;
		// Logger.print(" ");

		while (I.get(j) <= sequenceLength - 1) {
			I.add(I.get(j) + Math.max(1, l[I.get(j) - 1]));
			// Logger.print(I.get(j + 1) + " ");
			j++;
		}
		// Logger.println("");
		int noBlocks = I.size() - 1;
		Vector<String> blocks = new Vector<String>();
		int[] h = new int[noBlocks + 1];
		for (int i = 0; i < noBlocks; i++) {
			blocks.add(sequence.substring(I.get(i) * encodingLength, I
					.get(i + 1)
					* encodingLength));
			h[i] = I.get(i);
			// Logger.println("Block " + (i + 1)+ ": " +
			// sequence.substring(I.get(i) * encodingLength, I.get(i + 1)*
			// encodingLength));
		}
		blocks.add(terminationSymbol);
		h[noBlocks] = I.get(noBlocks);
		noBlocks++; // consider the termination symbol block
		// findRepetitions(blocks);

		// for(int i = 0; i < noBlocks; i++){
		// System.out.println(blocks.get(i));
		// }

		int currentBlockStart, nextBlockStart, currentBlockLength, nextBlockLength;
		String currentBlock, nextBlock;
		for (int i = 0; i < noBlocks - 1; i++) {
			currentBlock = blocks.get(i);
			currentBlockLength = currentBlock.length() / encodingLength;
			currentBlockStart = h[i];

			nextBlock = blocks.get(i + 1);
			nextBlockLength = nextBlock.length() / encodingLength;
			nextBlockStart = h[i + 1];
			// Logger.println("Block "
			// + (i + 1)
			// + ": "
			// + sequence.substring(I.get(i) * encodingLength, I
			// .get(i + 1)
			// * encodingLength) + " " + currentBlockStart);
			processBlockAlgorithm1A(currentBlockLength, nextBlockStart);
			processBlockAlgorithm1B(currentBlockLength, nextBlockLength,
					currentBlockStart, nextBlockStart);
		}

		// Find the repeat types from the tandem pairs identified
		repeatTypes = findRepeatTypes();
		// System.out.println("Tandem Pairs");
		Iterator<String> it;
		// it = repeatTypes.iterator();
		// while(it.hasNext())
		// System.out.println(it.next());

		// Find the primitive repeats from the repeat types

		repeatPrimitiveRepeatMap = findPrimitiveRepeats(repeatTypes);

		// System.out.println("Primitive Repeats");
		// it = repeatPrimitiveRepeatMap.keySet().iterator();
		// String currentRepeat;
		// while(it.hasNext()){
		// currentRepeat = it.next();
		// System.out.println(currentRepeat+" @
		// "+repeatPrimitiveRepeatMap.get(currentRepeat));
		// }

		// if(repeatPrimitiveRepeatMap.size() != repeatTypes.size()){
		// System.out.println("Something Missing");
		// }

		// TreeMap<Integer, String> repeatAtPos = findRepeatsAtPos(repeatTypes);

		TreeMap<Integer, String> repeatAtPos = findRepeatsAtPos(new TreeSet<String>(
				repeatPrimitiveRepeatMap.keySet()));
		// System.out.println("RepeatAtPos: "+repeatAtPos.size());
		int[] repeatStart, repeatEnd;
		String[] repeat;

		int noPosWithRepeats = repeatAtPos.size();
		repeatStart = new int[noPosWithRepeats];
		repeatEnd = new int[noPosWithRepeats];
		repeat = new String[noPosWithRepeats];

		// Logger.println("Repeats Satisfying at Pos");
		Iterator<Integer> intIter = repeatAtPos.keySet().iterator();
		Integer currentKey;
		int i = 0;
		while (intIter.hasNext()) {
			currentKey = intIter.next();
			repeatStart[i] = currentKey;
			repeat[i] = repeatAtPos.get(currentKey).toString();
			repeatEnd[i] = currentKey + (repeat[i].length() / encodingLength)
					- 1;
			i++;
		}

		boolean[] flag = new boolean[noPosWithRepeats];
		for (i = 0; i < noPosWithRepeats; i++)
			flag[i] = true;
		// Process the identified repeat locations in the sequence to remove
		// subsumption and overlaps
		int activeRepeatStart, activeRepeatEnd, activeRepeatLength;
		String activeRepeat;

		// Find complete subsumption and flag them
		for (i = 0; i < noPosWithRepeats; i++) {
			activeRepeatStart = repeatStart[i];
			activeRepeatEnd = repeatEnd[i];

			j = i + 1;
			while (j < noPosWithRepeats) {
				// check for subsumption;
				if (repeatStart[j] > activeRepeatStart
						&& repeatEnd[j] <= activeRepeatEnd) {
					flag[j] = false;
					j++;
				} else {
					break;
				}
			}
			i = j - 1;
		}

		// Process Repeats Further for Overlap
		for (i = 0; i < noPosWithRepeats - 1; i++) {
			if (flag[i] == true) {
				// Logger.println("HERE: "+repeatStart[i]+" "+repeatEnd[i]+"
				// "+repeat[i]);
				// check if something is sandwiched between two repeats; then
				// ignore the middle one
				// i.e., if a2,b2 is between a1,b1 and a3,b3-> then ignore a2,b2

				activeRepeatStart = repeatStart[i];
				activeRepeatEnd = repeatEnd[i];
				activeRepeat = repeat[i];
				activeRepeatLength = activeRepeat.length();

				j = i + 1;
				while (j < noPosWithRepeats) {
					if (flag[j] == false) {
						j++;
						continue;
					}
					if (repeatStart[j] == activeRepeatEnd) {
						// Logger.println("R: " + repeatStart[j] + " "
						// + activeRepeatEnd);
						int k = j + 1;
						// get the next valid repeat info
						while (k < noPosWithRepeats && flag[k] == false)
							k++;
						if (k == noPosWithRepeats) {
							flag[j] = false;
							break;
						}
						if (k < noPosWithRepeats
								&& repeatEnd[j] <= repeatEnd[k]) {
							// a2,b2 is inbetween a1,b1 and a3,b3
							flag[j] = false;
							j = k;
						} else {
							break;
						}
					} else if (repeatStart[j] < activeRepeatEnd) {
						// If the subsequent repeat length is more than the
						// current one and if there is a overlap, ignore current
						// one
						if (repeat[j].length() > activeRepeatLength) {
							flag[i] = false;
							// the activeRepeat no longer holds true; so break
							// away
							break;
						} else {
							// both the repeats might be of the same length or
							// the second one might be shorter
							flag[j] = false;
							j++;
						}
					} else {
						break;
					}
				}
				i = j - 1;
			}
		}

		repeatsInfo = new ArrayList<RepeatsInfo>();
		for (i = 0; i < noPosWithRepeats; i++)
			if (flag[i] == true) {
				repeatsInfo.add(new RepeatsInfo(repeatStart[i], repeatEnd[i],
						repeat[i]));
				// Logger.println(repeatStart[i] + " " + repeatEnd[i] + " "
				// + repeat[i]);
			}

	}

	private void processBlockAlgorithm1A(int currentBlockLength, int h1) {
		int q, k1, k2;
		for (int k = 1; k <= currentBlockLength; k++) {
			q = h1 - k;
			// Compute the longest common extension in the forward direction
			// from positions h1 and q
			// k1 stores the length of that extension
			k1 = 0;
			while ((h1 + k1 + 1 < sequenceLength)
					&& sequence.substring((q + k1) * encodingLength,
							(q + k1 + 1) * encodingLength).equals(
							sequence.substring((h1 + k1) * encodingLength, (h1
									+ k1 + 1)
									* encodingLength)))
				k1++;

			// Compute the longest common extension in the backward direction
			// from positions h1-1 and q-1. k2 denote the length of that
			// extension
			k2 = 0;
			while ((q - k2 - 1) > 0
					&& sequence.substring((q - 1 - k2) * encodingLength,
							(q - k2) * encodingLength).equals(
							sequence.substring((h1 - k2 - 1) * encodingLength,
									(h1 - k2) * encodingLength)))
				k2++;

			if (k1 + k2 >= k && k1 > 0) {
				int maxVal = max(q - k2, q - k + 1);
				tandemPairs[maxVal - 1].add(2 * k);
				// Logger.println("(1A). Tandem Repeat Pair: "+maxVal+","+2*k);
			} else if (k1 + k2 >= k && k1 == 0) {
				int maxVal = max(q - k2, q - k);
				tandemPairs[maxVal - 1].add(2 * k);
				// Logger.println("(1A). Tandem Repeat Pair: "+maxVal+","+2*k);
			}
		}
	}

	private void processBlockAlgorithm1B(int currentBlockLength,
			int nextBlockLength, int h, int h1) {
		int q, k1, k2;
		for (int k = 1; k <= currentBlockLength + nextBlockLength; k++) {
			q = h + k;

			// compute the longest common extension from positions h & q. k1
			// denote the length of that extension
			k1 = 0;
			while ((q + k1) < sequenceLength
					&& sequence.substring((h + k1) * encodingLength,
							(h + k1 + 1) * encodingLength).equals(
							sequence.substring((q + k1) * encodingLength, (q
									+ k1 + 1)
									* encodingLength)))
				k1++;

			// compute the longest common extension in the backward direction
			// from positions h-1 and q-1; let k2 denote the length of that
			// extension
			k2 = 0;
			while ((h - k2 - 1) > 0
					&& sequence.substring((h - k2 - 1) * encodingLength,
							(h - k2) * encodingLength).equals(
							sequence.substring((q - 1 - k2) * encodingLength,
									(q - k2) * encodingLength)))
				k2++;

			if (k1 + k2 >= k && k1 > 0 && k2 > 0
					&& (max(h - k2, h - k + 1) + k) <= h1) {
				int maxVal = max(h - k2, h - k + 1);
				tandemPairs[maxVal - 1].add(2 * k);
				// Logger.println("(1B). Tandem Repeat Pair:
				// "+max(h-k2,h-k+1)+","+2*k);
			}
		}
	}

	int max(int a, int b) {
		return a > b ? a : b;
	}

	private TreeSet<String> findRepeatTypes() {
		// Logger.printCall("Entering findRepeatTypes");

		TreeSet<String> repeatTypes = new TreeSet<String>(
				new AscStrComparator());
		String tandemRepeat;
		for (int i = 0; i < sequenceLength; i++) {
			if (tandemPairs[i].size() > 0) {
				// Logger.print((i + 1) + ": " + tandemPairs[i].toString());
				Iterator<Integer> it = tandemPairs[i].iterator();
				while (it.hasNext()) {
					tandemRepeat = sequence.substring((i + 1) * encodingLength,
							(i + 1 + it.next()) * encodingLength);
					repeatTypes.add(tandemRepeat);
				}
			}
		}

		// Logger.printReturn("Exiting findRepeatTypes");
		return repeatTypes;
	}

	private TreeMap<String, String> findPrimitiveRepeats(
			TreeSet<String> repeatTypes) {
		// Logger.println("Finding Primitive Repeats");
		TreeMap<String, String> repeatPrimitiveRepeatMap = new TreeMap<String, String>();

		HashMap<HashSet<String>, HashSet<String>> repeatAlphabetEquivalenceClasses;
		repeatAlphabetEquivalenceClasses = new HashMap<HashSet<String>, HashSet<String>>();
		HashSet<String> currentEquivalenceClasses;
		TreeSet<String> fullyContainedPairs;
		String[] fullyContainedPairsArray;
		Iterator<String> it3, it4;
		String tempPair, tempstr;

		String currentTandemRepeatPair, currentTandemRepeat;
		int currentTandemRepeatLength;
		HashSet<String> currentTandemRepeatAlphabet;
		Iterator<String> it = repeatTypes.iterator();
		boolean found = false;
		while (it.hasNext()) {
			currentTandemRepeatPair = it.next();
			currentTandemRepeat = currentTandemRepeatPair.substring(0,
					currentTandemRepeatPair.length() / 2);
			currentTandemRepeatLength = currentTandemRepeat.length()
					/ encodingLength;

			currentTandemRepeatAlphabet = new HashSet<String>();

			for (int i = 0; i < currentTandemRepeatLength; i++) {
				currentTandemRepeatAlphabet.add(currentTandemRepeat.substring(i
						* encodingLength, (i + 1) * encodingLength));
			}

			if (repeatAlphabetEquivalenceClasses
					.containsKey(currentTandemRepeatAlphabet)) {
				currentEquivalenceClasses = repeatAlphabetEquivalenceClasses
						.get(currentTandemRepeatAlphabet);
			} else {
				currentEquivalenceClasses = new HashSet<String>();
			}
			currentEquivalenceClasses.add(currentTandemRepeatPair);
			repeatAlphabetEquivalenceClasses.put(currentTandemRepeatAlphabet,
					currentEquivalenceClasses);

			if (currentTandemRepeatAlphabet.size() == 1) {
				// Simple case; The tandem repeat comprises of only one symbol;
				// The primitive repeat should be the symbol itself
				repeatPrimitiveRepeatMap.put(currentTandemRepeatPair,
						currentTandemRepeat.substring(0, 1 * encodingLength));
			} else if (currentTandemRepeatLength == currentTandemRepeatAlphabet
					.size()) {
				// The number of alphabets in the repeat string is the same as
				// the repeat; no minimization possible or
				// The currentTandemRepeat String is not a tandem repeat in
				// itself
				repeatPrimitiveRepeatMap.put(currentTandemRepeatPair,
						currentTandemRepeat);
			} else {
				if (repeatPrimitiveRepeatMap.containsKey(currentTandemRepeat)) {
					repeatPrimitiveRepeatMap.put(currentTandemRepeatPair,
							repeatPrimitiveRepeatMap.get(currentTandemRepeat));
					// Remove this tandem pair from the equivalence class
					// currentEquivalenceClasses =
					// repeatAlphabetEquivalenceClasses.get(currentTandemRepeatAlphabet);
					// currentEquivalenceClasses.remove(currentTandemRepeatPair);
					// repeatAlphabetEquivalenceClasses.put(currentTandemRepeatAlphabet,
					// currentEquivalenceClasses);
				} else {
					// by now all tandem repeat pairs less than this pair length
					// would have been processed;
					// so get all tandem repeat pairs fully contained in this
					// pair and which start with the same element from the
					// repeat alphabet equivalence classes
					currentEquivalenceClasses = repeatAlphabetEquivalenceClasses
							.get(currentTandemRepeatAlphabet);

					fullyContainedPairs = new TreeSet<String>(
							new AscStrComparator());
					it3 = currentEquivalenceClasses.iterator();
					while (it3.hasNext()) {
						tempPair = it3.next();
						if (!currentTandemRepeatPair.equals(tempPair)
								&& currentTandemRepeatPair.contains(tempPair)
								&& currentTandemRepeatPair.substring(0,
										encodingLength).equals(
										tempPair.substring(0, encodingLength)))
							fullyContainedPairs.add(tempPair);
					}

					if (fullyContainedPairs.size() > 1) {
						// System.out.println("F: " + currentTandemRepeatPair+ "
						// @ " + fullyContainedPairs);
						found = false;
						// Check if the current pair is a sum of pairs of other
						// pairs
						// only the first two entries should be sufficient (need
						// to be validated)
						fullyContainedPairsArray = fullyContainedPairs
								.toArray(new String[fullyContainedPairs.size()]);
						for (int i = 0; i < fullyContainedPairsArray.length - 1; i++) {
							for (int j = i; j < fullyContainedPairsArray.length; j++) {
								if (fullyContainedPairsArray[i].length()
										+ fullyContainedPairsArray[j].length() == currentTandemRepeatPair
										.length()) {
									repeatPrimitiveRepeatMap
											.put(
													currentTandemRepeatPair,
													repeatPrimitiveRepeatMap
															.get(fullyContainedPairsArray[i]));
									found = true;
									break;
								}
							}
							if (found == true)
								break;
						}

						if (found == false) {
							System.out.println("SOMETHING WRONG");
						}
						// it3 = fullyContainedPairs.iterator();
						// tempPair1 = it3.next(); tempPair2 = it3.next();
						// if(currentTandemRepeatPair.length() ==
						// tempPair1.length()+tempPair2.length()){
						// repeatPrimitiveRepeatMap.put(currentTandemRepeatPair,repeatPrimitiveRepeatMap.get(tempPair1));
						// }else{
						// System.out.println(currentTandemRepeatPair.length()+"
						// "+tempPair1.length()+" "+tempPair2.length());
						// System.out.println("SOMETHING WRONG");
						// }

					} else {
						// Check if this is an exceptional case because of
						// one/two symbols etc
						if (currentTandemRepeatLength
								/ currentTandemRepeatAlphabet.size() > 10) {
							// System.out.println("EXCEPTIONAL CASE:
							// "+currentTandemRepeat);
							HashMap<String, String> exceptionalMap = findPrimitiveRepeatsExceptionalCase(
									currentTandemRepeat,
									currentTandemRepeatAlphabet);
							it4 = exceptionalMap.keySet().iterator();
							while (it4.hasNext()) {
								tempstr = it4.next();
								if (!repeatPrimitiveRepeatMap.keySet()
										.contains(tempstr)) {
									repeatPrimitiveRepeatMap.put(tempstr,
											exceptionalMap.get(tempstr));
								}
							}
						} else {
							// System.out.println("HERE: "+currentTandemRepeat);
							repeatPrimitiveRepeatMap.put(
									currentTandemRepeatPair,
									currentTandemRepeat);
						}
					}

					fullyContainedPairs = null;
				}
			}

			// System.out.println(currentTandemRepeat+" @
			// "+currentTandemRepeatAlphabet);

			currentTandemRepeatAlphabet = null;
		}

		// System.out.println("RepeatAlphabet Equivalence Classes");
		// Iterator<HashSet<String>> it2 =
		// repeatAlphabetEquivalenceClasses.keySet().iterator();
		// while(it2.hasNext()){
		// currentTandemRepeatAlphabet = it2.next();
		// System.out.println(currentTandemRepeatAlphabet+" @
		// "+repeatAlphabetEquivalenceClasses.get(currentTandemRepeatAlphabet));
		// }
		return repeatPrimitiveRepeatMap;
	}

	private HashMap<String, String> findPrimitiveRepeatsExceptionalCase(
			String currentTandemRepeat,
			HashSet<String> currentTandemRepeatAlphabet) {
		HashMap<String, String> repeatPrimitiveRepeatMap = new HashMap<String, String>();
		Iterator<String> it = currentTandemRepeatAlphabet.iterator();
		String currentAlphabet;
		String[] repeatSplit;
		// System.out.println(currentTandemRepeat);
		int currentRepeatLength = currentTandemRepeat.length() / encodingLength, currentSplitLength;
		HashSet<String> currentSplitAlphabet;
		while (it.hasNext()) {
			currentAlphabet = it.next();
			repeatSplit = currentTandemRepeat.split(currentAlphabet,
					currentRepeatLength);
			if (repeatSplit.length <= 3) {
				for (int i = 0; i < repeatSplit.length; i++) {
					currentSplitAlphabet = new HashSet<String>();
					currentSplitLength = repeatSplit[i].length()
							/ encodingLength;
					for (int j = 0; j < currentSplitLength; j++)
						currentSplitAlphabet.add(repeatSplit[i].substring(j
								* encodingLength, (j + 1) * encodingLength));

					if (currentSplitAlphabet.size() == 1) {
						// Todo
						// Actually should check for the tandem array (even
						// count) here; Ignoring for the time being
						if (currentSplitLength % 2 == 0)
							repeatPrimitiveRepeatMap.put(repeatSplit[i],
									currentSplitAlphabet.iterator().next());
						else {
							repeatPrimitiveRepeatMap.put(repeatSplit[i]
									.substring(0, (currentSplitLength - 1)
											* encodingLength),
									currentSplitAlphabet.iterator().next());
						}
					}
				}
				// System.out.println("MINIMIZATION: "+currentAlphabet+"
				// "+repeatSplit.length+" "+repeatSplit[0]);
			} else if (currentTandemRepeatAlphabet.size() == 2
					&& repeatSplit.length == currentRepeatLength / 2) {
				repeatPrimitiveRepeatMap.put(currentTandemRepeat,
						currentAlphabet + it.next());
			}
		}

		return repeatPrimitiveRepeatMap;
	}

	private TreeMap<Integer, String> findRepeatsAtPos(
			TreeSet<String> repeatTypes) {
		// Logger.printCall("Entering findRepeatsAtPos");

		TreeMap<Integer, String> repeatAtPos = new TreeMap<Integer, String>();

		Iterator<String> it = repeatTypes.iterator();
		String currentRepeat, tempString;
		int repeatIndex = -1;
		// Logger.println("Repeat Types");
		while (it.hasNext()) {
			currentRepeat = it.next();
			if (currentRepeat.length() >= 2 * encodingLength) {
				for (int i = 0; i < sequenceLength; i++) {
					repeatIndex = sequence.indexOf(currentRepeat, (i + 1)
							* encodingLength);
					if (repeatIndex == -1)
						break;
					else
						repeatIndex /= encodingLength;
					// Logger.println(currentRepeat+" "+repeatIndex);

					if (repeatAtPos.containsKey(repeatIndex)) {
						tempString = repeatAtPos.get(repeatIndex);
						if (currentRepeat.length() > tempString.length())
							repeatAtPos.put(repeatIndex, currentRepeat);
					} else {
						repeatAtPos.put(repeatIndex, currentRepeat);
					}
				}
			}
			// Logger.println(currentRepeat+" "+repeatIndex);
		}

		// Logger.printReturn("Exiting findRepeatsAtPos");

		return repeatAtPos;
	}

	public TreeSet<String> getRepeatTypes() {
		return repeatTypes;
	}

}
