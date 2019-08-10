/*
 * Author: R. P. Jagadeesh Chandra Bose
 * Date  : 4-5 August 2008
 * This file implements the suffix tree generation in linear time and space
 * The suffix tree is constructed based on Ukkonen's algorithm
 * 
 * The general outline of the algorithm is as follows:
 * 
 * n = length of the string.
   CreateTree:
	   Calls n times to SPA (Single Phase Algorithm). SPA:  
	      Increase the variable e (virtual end of all leaves).
	   Calls SEA (Single Extension Algorithm) starting with the first extension that
	   does not already exist in the tree and ending at the first extension that
	   already exists. SEA :  
	      Follow suffix link.
	      Check if current suffix exists in the tree.
	      If it does not - apply rule 2 and then create a new suffix link.
	      apply_rule_2:  
	         Create a new leaf and maybe a new internal node as well.
	         create_node:  
	            Create a new node or a leaf.
   
  Rules 1 and 3 are implicit and thus are not implemented. Only rule 2 is
  "real".
 */

package org.processmining.analysis.traceclustering.charstreams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
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
	TreeMap<String, String> repeatPrimitiveRepeatMap;

	TreeSet<Integer>[] tandemPairs;

	public TreeMap<String, String> getRepeatPrimitiveRepeatMap() {
		return repeatPrimitiveRepeatMap;
	}

	public UkkonenSuffixTree(String seq, int encodingLength) {
		// Logger.printCall("Entering Constructor UkkonenSuffixTree");

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
		sequenceLength = seq.length() / encodingLength + 1; // including the EOS

		pos = new SuffixTreePos();

		leaves = new SuffixNode[sequenceLength];

		createTree();

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

		// Ukkonen's algorithm begins here

		boolean repeatedExtension = false;
		for (; phase < sequenceLength; phase++) {
			SPAResult result = SPA(phase, extension, repeatedExtension);

			extension = result.extension;
			repeatedExtension = result.repeatedExtension;

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

		if (!afterRule3)
			followSuffixLink();
		// System.out.println("pos.edgepos " + pos.edgePosition);

		int noSymbolsFound = 0;
		if (pos.node == root) {
			noSymbolsFound = traceString(str, false);
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
				}
			}
		}

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
			/* Create a new leaf (4) with the symbols of the extension */
			newLeaf = new SuffixNode(pos.node, edgeLabelBegin, edgeLabelEnd,
					pathPosition);

			/* Connect new_leaf (4) as the new son of node (1) */
			pos.node.addChildren(newLeaf);
			/* return (4) */
			return;
			// return newLeaf;
		}

		/*-------split-------*/

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
			// if (debug) {
			// Logger.println("String not found");
			// Logger.println("pos.edgePos: " + pos.edgePosition);
			// }
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

		// if (debug)
		// Logger.println("Entering findChild looking for symbol: " + symbol);

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

	@SuppressWarnings("unchecked")
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
				System.out.println("Something terribly Wrong; Current Symbol `"
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
		TreeSet<String> repeatTypes = findRepeatTypes();

		// Find the primitive repeats from the repeat types
		repeatPrimitiveRepeatMap = findPrimitiveRepeats(repeatTypes);

		TreeMap<Integer, String> repeatAtPos = findRepeatsAtPos(repeatTypes);

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

		// Logger.println("Processing Repeats");
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
			}

		// Logger.printReturn("Exiting LZDecomposition");
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
			} else if (k1 + k2 >= k && k1 == 0) {
				int maxVal = max(q - k2, q - k);
				tandemPairs[maxVal - 1].add(2 * k);
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
			}
		}
	}

	int max(int a, int b) {
		return a > b ? a : b;
	}

	private TreeSet<String> findRepeatTypes() {
		// Logger.printCall("Entering findRepeatTypes");

		TreeSet<String> repeatTypes = new TreeSet<String>(
				new DescStrComparator());
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
		// Logger.printCall("Entering findPrimitiveRepeats");

		HashSet<String> repeatAlphabet;
		HashMap<String, HashSet<String>> repeatAlphabetMap;
		TreeMap<String, String> repeatPrimitiveRepeatMap;

		repeatAlphabetMap = new HashMap<String, HashSet<String>>();
		repeatPrimitiveRepeatMap = new TreeMap<String, String>();

		String currentRepeat;
		int currentRepeatLength;
		Iterator<String> it = repeatTypes.iterator();
		while (it.hasNext()) {
			currentRepeat = it.next();
			// The division by 2 is to consider the repeat itself than the pair;
			// Since the second half is also the same, we need not process that
			currentRepeatLength = (currentRepeat.length() / encodingLength) / 2;

			repeatAlphabet = new HashSet<String>();
			for (int i = 0; i < currentRepeatLength; i++) {
				repeatAlphabet.add(currentRepeat.substring(i * encodingLength,
						(i + 1) * encodingLength));
			}
			repeatAlphabetMap.put(currentRepeat, repeatAlphabet);
			if (repeatAlphabet.size() == 1) {
				// The repeat pair is made up of just one alphabet; so the
				// primitive repeat is just `aa' where `a' is the alphabet
				repeatPrimitiveRepeatMap.put(currentRepeat, currentRepeat
						.substring(0, 2 * encodingLength));
			} else if (currentRepeatLength == repeatAlphabet.size()) {
				// No minimization possible; Already a primitive repeat
				repeatPrimitiveRepeatMap.put(currentRepeat, currentRepeat
						.substring(0, currentRepeatLength * encodingLength));
			} else if (currentRepeatLength % repeatAlphabet.size() != 0) {
				// No minimization possible; since the repeat length is not an
				// integral multiple of the alphabet size;
				// for example, if the repeat length is 5 and it comprises of 2
				// or 3 alphabets; nothing can be done; On the otherhand if the
				// repeat length is 9 and it comprises of 3 alphabets; possibly
				// we can minimize it
				repeatPrimitiveRepeatMap.put(currentRepeat, currentRepeat
						.substring(0, currentRepeatLength * encodingLength));
			} else {
				repeatPrimitiveRepeatMap.put(currentRepeat, currentRepeat
						.substring(0, currentRepeatLength * encodingLength));
			}
		}

		// Logger.println("Primitive Repeats");
		String activeRepeat, currentPrimitiveRepeat;
		boolean found;
		it = repeatPrimitiveRepeatMap.keySet().iterator();
		while (it.hasNext()) {
			activeRepeat = it.next();

			currentRepeat = activeRepeat;
			currentPrimitiveRepeat = repeatPrimitiveRepeatMap
					.get(currentRepeat);
			found = false;
			if (repeatPrimitiveRepeatMap.containsKey(currentPrimitiveRepeat)) {
				// There exists a shorter primitive repeat for the current one;
				currentRepeat = currentPrimitiveRepeat;
				currentPrimitiveRepeat = repeatPrimitiveRepeatMap
						.get(currentRepeat);
				found = true;
			}
			if (found == true)
				repeatPrimitiveRepeatMap.put(activeRepeat,
						currentPrimitiveRepeat);
		}

		// Further processing of primitive repeats is required
		// For example if there is a tandem repeat of 30 length involving only 2
		// alphabets
		// then the repeat would be of 15 length; in the above logic, 15%2 != 0
		// and the
		// entire 15 length string would have been placed as a primitive repeat;
		// however it would
		// have been a tandem array with alpha^k
		// Process only odd lengthed primitive repeat

		// simple logic as follows:
		// get all primitive repeats involving the same alphabet that are of
		// lengths less than the current primitive repeat
		// check if the current primitive repeat is a multiple of the shorter
		// primitive; then replace the current one with the shorter one

		Collection<String> primitiveRepeats = repeatPrimitiveRepeatMap.values();
		HashMap<String, HashSet<String>> primitiveRepeatAlphabetMap = new HashMap<String, HashSet<String>>();
		it = primitiveRepeats.iterator();
		String currPrimitiveRepeat;
		int currPrimitiveRepeatLength;
		while (it.hasNext()) {
			currPrimitiveRepeat = it.next();
			currPrimitiveRepeatLength = currPrimitiveRepeat.length()
					/ encodingLength;
			repeatAlphabet = new HashSet<String>();
			for (int i = 0; i < currPrimitiveRepeatLength; i++) {
				repeatAlphabet.add(currPrimitiveRepeat.substring(i
						* encodingLength, (i + 1) * encodingLength));
			}

			primitiveRepeatAlphabetMap.put(currPrimitiveRepeat, repeatAlphabet);
		}
		Iterator<String> it1;

		it = repeatPrimitiveRepeatMap.keySet().iterator();

		String currRepeat, tempPrimitiveRepeat, minPrimitiveRepeat;
		HashSet<String> activeRepeatAlphabet, tempAlphabet;
		String tempStr;

		while (it.hasNext()) {
			currRepeat = it.next();
			currPrimitiveRepeat = repeatPrimitiveRepeatMap.get(currRepeat);
			currPrimitiveRepeatLength = currPrimitiveRepeat.length()
					/ encodingLength;
			if (currPrimitiveRepeatLength % 2 != 0) {
				// odd primitive repeat
				// get the repeat alphabet
				activeRepeatAlphabet = primitiveRepeatAlphabetMap
						.get(currPrimitiveRepeat);

				// get the shortest primitive repeat with the same repeat
				// alphabet
				minPrimitiveRepeat = currPrimitiveRepeat;
				it1 = primitiveRepeatAlphabetMap.keySet().iterator();
				while (it1.hasNext()) {
					tempPrimitiveRepeat = it1.next();
					// check if currPrimitiveRepeat and tempPrimitiveRepeat
					// start with the same char
					if (!currPrimitiveRepeat.substring(0, encodingLength)
							.equals(
									tempPrimitiveRepeat.substring(0,
											encodingLength)))
						continue;
					if (!primitiveRepeatAlphabetMap
							.containsKey(tempPrimitiveRepeat)) {
						// Logger.println("PR: "+tempPrimitiveRepeat);
						System.exit(0);
					}
					tempAlphabet = primitiveRepeatAlphabetMap
							.get(tempPrimitiveRepeat);
					if (tempAlphabet.size() == activeRepeatAlphabet.size()
							&& tempAlphabet.containsAll(activeRepeatAlphabet)) {
						if (tempPrimitiveRepeat.length() < minPrimitiveRepeat
								.length())
							minPrimitiveRepeat = tempPrimitiveRepeat;
					}
				}

				// check if the curr primitive repeat is a multiple occurrence
				// of minPR
				tempStr = "";
				int noOccurrences = currPrimitiveRepeat.length()
						/ minPrimitiveRepeat.length();
				for (int i = 0; i < noOccurrences; i++)
					tempStr += minPrimitiveRepeat;
				if (currPrimitiveRepeat.equals(tempStr)) {
					repeatPrimitiveRepeatMap
							.put(currRepeat, minPrimitiveRepeat);
				}
			}
		}

		// Logger.printReturn("Exiting findPrimitiveRepeats");

		return repeatPrimitiveRepeatMap;
	}

	private TreeMap<Integer, String> findRepeatsAtPos(
			TreeSet<String> repeatTypes) {
		// Logger.printCall("Entering findRepeatsAtPos");

		TreeMap<Integer, String> repeatAtPos = new TreeMap<Integer, String>();

		Iterator<String> it = repeatTypes.iterator();
		String currentRepeat, tempString;
		int repeatIndex = -1;

		while (it.hasNext()) {
			currentRepeat = it.next();
			if (currentRepeat.length() > 2 * encodingLength) {
				for (int i = 0; i < sequenceLength; i++) {
					repeatIndex = sequence.indexOf(currentRepeat, (i + 1)
							* encodingLength);
					if (repeatIndex == -1)
						break;
					else
						repeatIndex /= encodingLength;

					if (repeatAtPos.containsKey(repeatIndex)) {
						tempString = repeatAtPos.get(repeatIndex);
						if (currentRepeat.length() > tempString.length())
							repeatAtPos.put(repeatIndex, currentRepeat);
					} else {
						repeatAtPos.put(repeatIndex, currentRepeat);
					}
				}
			}

		}

		// Logger.printReturn("Exiting findRepeatsAtPos");

		return repeatAtPos;
	}

	public void findLeftDiverseNodes() {
		// Get all the leaf nodes of the suffixTree;
		ArrayList<SuffixNode> leafNodes, nodeLeaves;
		leafNodes = getLeaves(root);

		int noLeaves = leafNodes.size();
		// Logger.println("No. Leaves: " + noLeaves);

		// Identify the leftCharacter of each leaf node and set the leftDiverse
		// value of each leaf to false
		SuffixNode node;
		for (int i = 0; i < noLeaves; i++) {
			node = leafNodes.get(i);
			node.isLeftDiverse = false;
			node.leftSymbol = sequence.substring((node.pathPosition - 1)
					* encodingLength, node.pathPosition * encodingLength);
			node.processed = true;
		}

		/*
		 * Bottom up traversal of each leaf Determine the leftDiversity of each
		 * internal node and propage it to the ancestors
		 */
		SuffixNode currLeafNode, parentNode;
		HashSet<String> leftSymbolSet;
		for (int i = 0; i < noLeaves; i++) {
			currLeafNode = leafNodes.get(i);
			parentNode = currLeafNode.parent;

			// Get the leaves of this parent node
			nodeLeaves = getLeaves(parentNode);

			// Get the left character symbol set of all leaves under this node;
			leftSymbolSet = new HashSet<String>();
			for (int j = 0; j < nodeLeaves.size(); j++) {
				leftSymbolSet.add(nodeLeaves.get(j).leftSymbol);
			}

			/*
			 * A node v is called left diverse if at least two leaves in v's
			 * subtree have different left symbols; So, check for the
			 * leftSymbolSet to be of at least size 2; If the node qualifies to
			 * be leftDiverse, then set all its ancestors also as leftDiverse;
			 */

			if (leftSymbolSet.size() > 1) {
				parentNode.isLeftDiverse = true;
				setAncestorsLeftDiverse(parentNode);
				parentNode.processed = true;
			}
		}
	}

	/*
	 * This module determines all maximal repeats (not necessarily tandem) in
	 * the sequence The algorithm is of Gusfield's; Finding leftDiverseNodes
	 * Returns a HashSet of maximal Repeats;
	 * 
	 * Assumes that the parent method calling this would have invoked
	 * findLeftDiverseNodes();
	 */
	public HashSet<String> getMaximalRepeats() {

		HashSet<String> maximalRepeats = new HashSet<String>();

		/*
		 * Determine the maximal repeats; A string alpha labeling a path to a
		 * node v of T is a maximal repeat iff v is leftDiverse Retrieve all
		 * leftDiverseNodes and print the paths leading to that node from the
		 * root The path information is stored in the class attributes
		 * pathPosition and edgeLabelEnd;
		 */

		ArrayList<SuffixNode> leftDiverseNodes = getLeftDiverseNodes(root);
		int noLeftDiverseNodes = leftDiverseNodes.size();

		if (noLeftDiverseNodes == 0) {
			// System.out.println("Looks like findLeftDiverseNodes() is not invoked");
			return null;
		}

		SuffixNode node;
		for (int i = 0; i < noLeftDiverseNodes; i++) {
			node = leftDiverseNodes.get(i);
			// Logger.println(node.pathPosition+" "+node.edgeLabelEnd);
			if (node.pathPosition != 0)
				maximalRepeats.add(sequence.substring(node.pathPosition
						* encodingLength, (node.edgeLabelEnd + 1)
						* encodingLength));
		}

		return maximalRepeats;
	}

	/*
	 * Find SuperMaximal Repeats; A left diverse internal node v represents a
	 * super maximal repeat alpha if and only if all of v's children are leaves,
	 * and each has a distinct left character
	 * 
	 * Assumes that the parent method calling this would have already invoked
	 * findLeftDiverseNodes();
	 */

	public HashSet<String> getSuperMaximalRepeats() {
		HashSet<String> superMaximalRepeats = new HashSet<String>();

		ArrayList<SuffixNode> leftDiverseNodes = getLeftDiverseNodes(root);
		int noLeftDiverseNodes = leftDiverseNodes.size();

		if (noLeftDiverseNodes == 0) {
			// System.out.println("Looks like findLeftDiverseNodes() is not invoked");
			return null;
		}

		int noChildren, j;
		SuffixNode node, child;
		HashSet<String> leftSymbolSet;

		for (int i = 0; i < noLeftDiverseNodes; i++) {
			node = leftDiverseNodes.get(i);
			noChildren = node.children.size();
			for (j = 0; j < noChildren; j++) {
				child = node.children.get(j);
				if (child.children.size() > 0) {
					// There exists a child of this node that is not a leaf; So
					// doesn't qualify for super maximal repeat
					break;
				}
			}
			if (j == noChildren) {
				/*
				 * Check for the left character set condition; Children are the
				 * leaves; so just iterate to check the left character
				 */
				leftSymbolSet = new HashSet<String>();
				for (int k = 0; k < noChildren; k++) {
					leftSymbolSet.add(node.children.get(k).leftSymbol);
				}
				if (leftSymbolSet.size() == noChildren) {
					// Signifies that the left character of each leaf is
					// different
					superMaximalRepeats.add(sequence.substring(
							node.pathPosition * encodingLength,
							(node.edgeLabelEnd + 1) * encodingLength));
				}
			}
		}

		return superMaximalRepeats;
	}

	/*
	 * This method determines the set of leaves of any given node; Returns the
	 * set of leaves as an array list
	 */
	private ArrayList<SuffixNode> getLeaves(SuffixNode node) {
		ArrayList<SuffixNode> nodeLeaves = new ArrayList<SuffixNode>();
		if (node.child == null)
			nodeLeaves.add(node);
		for (int i = 0; i < node.children.size(); i++)
			nodeLeaves.addAll(getLeaves(node.children.get(i)));

		return nodeLeaves;
	}

	/*
	 * This method returns the list of leftDiverse nodes under a given node;
	 */
	private ArrayList<SuffixNode> getLeftDiverseNodes(SuffixNode node) {
		ArrayList<SuffixNode> leftDiverse = new ArrayList<SuffixNode>();
		if (node.isLeftDiverse)
			leftDiverse.add(node);
		for (int i = 0; i < node.children.size(); i++)
			leftDiverse.addAll(getLeftDiverseNodes(node.children.get(i)));
		return leftDiverse;
	}

	/*
	 * This method sets the leftDiversity of all ancestors of a given node
	 */

	private void setAncestorsLeftDiverse(SuffixNode node) {
		if (node != root && !node.parent.processed) {
			node.parent.isLeftDiverse = true;
			setAncestorsLeftDiverse(node.parent);
			node.parent.processed = true;
		}
	}

	public ArrayList<RepeatsInfo> getRepeatsInfo() {
		return repeatsInfo;
	}
}
