/*
 * Author: R. P. Jagadeesh Chandra Bose
 * Date: 22 Dec 2008
 * Version: 1.0
 * 
 * This file implements the suffix tree generation in linear time and space
 * The suffix tree is constructed based on Ukkonen's algorithm
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

package org.processmining.analysis.abstractions.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SuffixNode {
	/**
	 * The parent node of this node
	 */
	SuffixNode parent; // The parent of this node

	/**
	 * The suffix link signifies the link to the node that forms the largest
	 * suffix of the current node
	 */
	SuffixNode suffixLink; // 

	/**
	 * The first child of this node
	 */
	SuffixNode firstChild;

	/**
	 * The list of all children of this node
	 */
	ArrayList<SuffixNode> childrenList;

	/**
	 * The starting index (in the input string) of the incoming edge
	 */
	int edgeLabelStart;

	/**
	 * The end index (in the input string)of the incoming edge
	 */
	int edgeLabelEnd;

	/**
	 * The index of the start position (in the input string) of the node's path.
	 * The path of the node is the string from the root of the tree to the node
	 */
	int pathPosition;

	/**
	 * flag signifying whether the node is left diverse. left diverse is
	 * important in determining the repeats
	 */
	boolean isLeftDiverse;

	/**
	 * flag to signify whether the node is already processed. useful in reducing
	 * the time in calculating repeats
	 */
	boolean isProcessed;

	/**
	 * The left symbol of the path from the root to the node. The left symbol
	 * property is used in determining the left diversity of the node
	 */
	String leftSymbol;

	/**
	 * Root Node
	 */

	public SuffixNode() {
		this.parent = null;
		this.suffixLink = null;
		this.firstChild = null;
		this.childrenList = new ArrayList<SuffixNode>();
		this.edgeLabelStart = 0;
		this.edgeLabelEnd = 0;
		this.pathPosition = 0;
	}

	/**
	 * 
	 * @param parent
	 *            : the parent node of the node
	 * @param edgeLabelStart
	 *            : the starting index of the incoming edge to the node
	 * @param edgeLabelEnd
	 *            : the ending index of the incoming edge to the node
	 * @param pathPosition
	 *            : the starting index of the path to this node
	 */
	public SuffixNode(SuffixNode parent, int edgeLabelStart, int edgeLabelEnd,
			int pathPosition) {
		this.parent = parent;
		this.edgeLabelStart = edgeLabelStart;
		this.edgeLabelEnd = edgeLabelEnd;
		this.pathPosition = pathPosition;

		this.suffixLink = null;
		this.firstChild = null;
		this.childrenList = new ArrayList<SuffixNode>();
	}

	public void addChild(SuffixNode node) {
		if (!childrenList.contains(node))
			childrenList.add(node);
	}

	public SuffixNode getFirstChild() {
		return firstChild;
	}

	public void setFirstChild(SuffixNode firstChild) {
		this.firstChild = firstChild;
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

/**
 * 
 * @author R. P. Jagadeesh Chandra Bose
 * 
 *         SuffixTreePos is a combination of the source node and the position in
 *         its incoming edge where suffix ends
 */

class SuffixTreePos {
	/**
	 * The source node
	 */
	SuffixNode node;

	/**
	 * The position in the edge where the last match occurred
	 */
	int edgePosition;

	public SuffixTreePos() {
		node = new SuffixNode();
		edgePosition = 0;
	}
}

/**
 * @author R.P. Jagadeesh Chandra Bose Class to hold the return values of the
 *         SPA procedure
 * 
 */
class SPAResult {
	int extension;
	boolean repeatedExtension;

	public SPAResult(int extension, boolean repeatedExtension) {
		this.extension = extension;
		this.repeatedExtension = repeatedExtension;
	}
}

/**
 * @author R. P. Jagadeesh Chandra Bose Class to hold the return value of the
 *         traceSingleEdge procedure
 * 
 */
class TraceSingleEdgeResult {
	/**
	 * number of matching symbols found on the edge
	 */
	int noEdgeSymbolsFound;
	/**
	 * flag denoting the end of search
	 */
	boolean searchDone;

	public TraceSingleEdgeResult(int noEdgeSymbolsFound, boolean searchDone) {
		this.noEdgeSymbolsFound = noEdgeSymbolsFound;
		this.searchDone = searchDone;
	}
}

class DescendingIntComparator implements Comparator<Integer> {
	public int compare(Integer i1, Integer i2) {
		int result = i1.compareTo(i2);
		return result * (-1);
	}
}

class DescendingStrCompartor implements Comparator<String> {
	public int compare(String s1, String s2) {
		return s1.compareTo(s2) * (-1);
	}
}

class DescendingStrLengthComparator implements Comparator<String> {
	public int compare(String s1, String s2) {
		return s1.compareTo(s2) * (s1.length() < s2.length() ? 1 : -1);// *
		// (-1);
	}
}

public class UkkonenSuffixTree {
	/**
	 * The root of the tree
	 */
	SuffixNode root;

	/**
	 * The node that doesn't have a suffix link yet. It will have one by the end
	 * of the current phase
	 */
	SuffixNode suffixLess;

	SuffixTreePath path;
	static SuffixTreePos pos;

	/**
	 * The input string for which the tree has to be constructed
	 */
	String sequence;
	/**
	 * The length of the input sequence
	 */
	int sequenceLength;

	/**
	 * The encoding length of the sequence
	 */
	int encodingLength;

	/**
	 * The virtual end of all leaves
	 */
	int e;

	/**
	 * The phase number
	 */
	int phase;

	/**
	 * The eos symbol
	 */
	static final String EOS = ".";

	String terminationSymbol;
	/**
	 * The prefix symbol;
	 */
	static final String prefix = ".";

	TreeSet<Integer>[] tandemPairs;
	HashSet<String> complexTandemRepeats;
	HashMap<TreeSet<String>, TreeSet<String>> complexAlphabetTandemRepeatMap;

	public UkkonenSuffixTree(int encodingLength, String sequence) {
		this.encodingLength = encodingLength;

		/**
		 * The termination symbol for the input string
		 */
		terminationSymbol = EOS;

		/**
		 * Adjust the sequence to start from index 1 rather than from index 0
		 */
		this.sequence = prefix;
		for (int i = 1; i < encodingLength; i++) {
			terminationSymbol += EOS;
			this.sequence += prefix;
		}

		/**
		 * Copy the input sequence from index 1 to N; Add the termination symbol
		 * at the end
		 */
		// this.sequence = sequence;
		this.sequence += sequence.concat(terminationSymbol);

		/**
		 * Compute the sequence length = length of i/p string sequence, + 1 for
		 * the termination symbol
		 */
		this.sequenceLength = sequence.length() / encodingLength + 1;

		pos = new SuffixTreePos();

		createTree();
	}

	private void createTree() {
		this.root = new SuffixNode();

		int extension;

		/* Initialize the algorithm parameters */
		phase = 2;
		extension = 2;

		/* Allocation of first child to the root; phase 0 */
		root.setFirstChild(new SuffixNode(root, 1, sequenceLength, 1));
		root.addChild(root.firstChild);
		this.e = 2;
		suffixLess = null;

		pos.node = root;
		pos.edgePosition = 0;

		/**
		 * Ukkonen's Algorithm begins here
		 */

		boolean repeatedExtension = false;
		for (; phase < sequenceLength; phase++) {
			SPAResult spaResult = SPA(phase, extension, repeatedExtension);
			extension = spaResult.extension;
			repeatedExtension = spaResult.repeatedExtension;
		}
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
		/* The rule applied */
		int ruleApplied = 0;

		SuffixTreePath streePath = new SuffixTreePath();

		/* Leaf's trick; Apply implicit extensions 1 through previous phase */
		this.e = phase + 1;

		while (extension <= phase + 1) {
			streePath.begin = extension;
			streePath.end = phase + 1;

			/* Call Single Extension Algorithm */
			ruleApplied = SEA(streePath, repeatedExtension);

			/* Check if rule 3 is applied for the current extension */
			if (ruleApplied == 3) {
				/*
				 * Signal that the next phase's first extension will not follow
				 * a suffix link because same extension is applied
				 */
				repeatedExtension = true;
				break;
			}

			repeatedExtension = false;
			extension++;
		}
		return new SPAResult(extension, repeatedExtension);
	}

	/**
	 * Single Extension Algorithm
	 * 
	 * Ensure that a certain extension is in the tree.
	 * 
	 * 1. Follows the current node's suffix link. 2. Check whether the rest of
	 * the extension is in the tree. 3. If it is - reports the calling function
	 * SPA of rule 3 (=> current phase is done). 4. If it's not - inserts it by
	 * applying rule 2.
	 * 
	 * Input: 1. (Global variable) pos - the node and position in its incoming
	 * edge where extension begins, 2. streePath - the starting and ending
	 * indices of the extension, 3. boolean, afterRule3, a flag indicating
	 * whether the last phase ended by rule 3(last extension of the last phase
	 * already existed in the tree - and if so, the current phase starts at not
	 * following the suffix link of the first extension).
	 * 
	 * Output: 1. The rule that was applied. Can be 3 (phase is done) or 2 (a
	 * new leaf was created).
	 */
	private int SEA(SuffixTreePath streePath, boolean afterRule3) {
		int ruleApplied = 0;

		int pathPosition = streePath.begin;

		SuffixNode tempNode = new SuffixNode();

		if (!afterRule3) {
			followSuffixLink();
		}

		int noSymbolsFound = 0;
		if (pos.node == root) {
			noSymbolsFound = traceString(streePath, false);
		} else {
			streePath.begin = streePath.end;
			noSymbolsFound = 0;
			if (isLastSymbolInEdge()) {
				tempNode = findChild(sequence.substring(streePath.end
						* encodingLength, (streePath.end + 1) * encodingLength));
				if (tempNode != null) {
					pos.node = tempNode;
					pos.edgePosition = 0;
					noSymbolsFound = 1;
				}
			} else {
				int tempIndex = pos.node.edgeLabelStart + pos.edgePosition + 1;
				String str1 = sequence.substring(tempIndex * encodingLength,
						(tempIndex + 1) * encodingLength);
				String str2 = sequence.substring(
						streePath.end * encodingLength, (streePath.end + 1)
								* encodingLength);
				if (str1.equals(str2)) {
					pos.edgePosition++;
					noSymbolsFound = 1;
				}
			}
		}

		if (noSymbolsFound == streePath.end - streePath.begin + 1) {
			ruleApplied = 3;
			if (suffixLess != null) {
				createSuffixLink(suffixLess, pos.node.parent);
				suffixLess = null;
			}

			return ruleApplied;
		}

		if (isLastSymbolInEdge() || pos.node == root) {
			if (pos.node.firstChild != null) {
				applyExtensionRule2(streePath.begin + noSymbolsFound,
						streePath.end, pathPosition, 0, true);
				ruleApplied = 2;
				if (suffixLess != null) {
					createSuffixLink(suffixLess, pos.node);
					suffixLess = null;
				}
			}
		} else {
			applyExtensionRule2(streePath.begin + noSymbolsFound,
					streePath.end, pathPosition, pos.edgePosition, false);

			if (suffixLess != null)
				createSuffixLink(suffixLess, pos.node);

			if (getNodeLabelLength(pos.node) == 1 && pos.node.parent == root) {
				pos.node.suffixLink = root;
				suffixLess = null;
			} else {
				suffixLess = pos.node;
			}

			ruleApplied = 2;
		}

		return ruleApplied;
	}

	/**
	 * applyExtensionRule2 : Apply "extension rule 2" in 2 cases: 1. A new son
	 * (leaf 4) is added to a node that already has sons: (1) (1) / \ -> / | \
	 * (2) (3) (2)(3)(4)
	 * 
	 * 2. An edge connecting to (1) is split and a new leaf (2) and an internal
	 * node (3) are added: | | | (3) | -> / \ (1) (1) (2)
	 * 
	 * 
	 * Input : pos: signifies the node 1 edgeLabelBegin: Start index of node 4's
	 * or 2's incoming edge edgeLabelEnd: End index of node 4's or 2's incoming
	 * edge pathPosition: Path start index of node 4 or 2's edgePos: Position in
	 * node 1's incoming edge where split is to be performed
	 * 
	 * Output: The newly created leaf (new_son case) or internal node (split
	 * case).
	 * 
	 */
	private void applyExtensionRule2(int edgeLabelBegin, int edgeLabelEnd,
			int pathPosition, int edgePos, boolean newson) {

		/* newSon */
		if (newson) {
			/* Create a new leaf (4) with the symbols of the extension */
			SuffixNode newLeaf = new SuffixNode(pos.node, edgeLabelBegin,
					edgeLabelEnd, pathPosition);

			/* Connect new_leaf (4) as the new son of node (1) */
			pos.node.addChild(newLeaf);

			return;
		}

		/* split case */

		/* Create a new internal node (3) at the split point */
		SuffixNode newInternal = new SuffixNode(pos.node.parent,
				pos.node.edgeLabelStart, pos.node.edgeLabelStart + edgePos,
				pos.node.pathPosition);

		/*
		 * Update the node (1) incoming edge starting index (it now starts where
		 * node (3) incoming edge ends)
		 */
		pos.node.edgeLabelStart += edgePos + 1;

		/* Create a new leaf (2) with the characters of the extension */
		SuffixNode newLeaf = new SuffixNode(newInternal, edgeLabelBegin,
				edgeLabelEnd, pathPosition);

		/*
		 * 1 is no longer the child of its parent. remove it from the children
		 * list;
		 */

		pos.node.parent.childrenList.remove(pos.node);

		/* Set 3 to be a child of 1's parent */
		pos.node.parent.addChild(newInternal);

		/*
		 * Set the first child; If 1 was the first child, then change the first
		 * child to 3
		 */
		if (newInternal.parent.firstChild == pos.node) {
			newInternal.parent.firstChild = newInternal;
		}

		/*
		 * Add 1 as a child of 3 Set 1 as the first child of 3
		 */
		newInternal.setFirstChild(pos.node);
		newInternal.addChild(pos.node);

		/*
		 * Set 1's parent to 3
		 */
		pos.node.parent = newInternal;

		/*
		 * Add 2 as a child to 3
		 */
		newInternal.addChild(newLeaf);

		/*
		 * Set the node to explore to 3
		 */
		pos.node = newInternal;
	}

	private void followSuffixLink() {
		// gamma is the string between node and its father if it doesn't have a
		// suffix link
		SuffixTreePath gamma = new SuffixTreePath();
		if (pos.node == root) {
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

	private int traceString(SuffixTreePath streePath, boolean skip) {
		/*
		 * This variable will be true when search is done. It is a return value
		 * from function traceSingleEdge
		 */
		boolean isSearchDone = false;

		int noSymbolsFound = 0;

		int streeBegin = streePath.begin;

		while (!isSearchDone) {
			pos.edgePosition = 0;
			TraceSingleEdgeResult result = traceSingleEdge(streePath, skip);
			streePath.begin += result.noEdgeSymbolsFound;
			noSymbolsFound += result.noEdgeSymbolsFound;
			isSearchDone = result.searchDone;
		}

		// streePath is passed by value; so reset the value
		streePath.begin = streeBegin;

		return noSymbolsFound;
	}

	private int traceString(SuffixTreePath streePath, String searchString,
			boolean skip) {
		/*
		 * This variable will be true when search is done. It is a return value
		 * from function traceSingleEdge
		 */
		boolean isSearchDone = false;

		int noSymbolsFound = 0;

		int streeBegin = streePath.begin;

		while (!isSearchDone) {
			pos.edgePosition = 0;
			TraceSingleEdgeResult result = traceSingleEdge(streePath,
					searchString, skip);
			// System.out.println("no. EdgeSymbolsFound: "+result.noEdgeSymbolsFound);
			streePath.begin += result.noEdgeSymbolsFound;
			noSymbolsFound += result.noEdgeSymbolsFound;
			isSearchDone = result.searchDone;
		}

		// streePath is passed by value; so reset the value
		streePath.begin = streeBegin;

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
	 * (in streePath). pos.node is the node to start from pos.edgePos holds the
	 * last matching position in edge
	 * 
	 * Output: (global) the node where tracing has stopped and the edge position
	 * where last match occurred; the string position where last match occurred,
	 * number of characters found, a flag for signaling whether search is done,
	 * and a flag to signal whether search stopped at a last character of an
	 * edge.
	 */
	private TraceSingleEdgeResult traceSingleEdge(SuffixTreePath streePath,
			boolean skip) {

		/* Set default return values */
		int noEdgeSymbolsFound = 0;
		boolean isSearchDone = true;
		pos.edgePosition = 0;

		/*
		 * Search for the first character of the string in the outcoming edge of
		 * node
		 */

		SuffixNode contNode = findChild(this.sequence.substring(streePath.begin
				* encodingLength, (streePath.begin + 1) * encodingLength));

		if (contNode == null) {
			/* Search is done, string not found */

			pos.edgePosition = getNodeLabelLength(pos.node) - 1;
			noEdgeSymbolsFound = 0;

			return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
		}

		/* Found first character - prepare for continuing the search */
		pos.node = contNode;
		int nodeLabelLength = getNodeLabelLength(pos.node);
		int streePathLength = streePath.end - streePath.begin + 1;

		/* Compare edge length and string length. */
		/**
		 * If edge is shorter then the string being searched and skipping is
		 * enabled - skip edge
		 */

		int edgePos;
		if (skip) {
			if (nodeLabelLength <= streePathLength) {
				noEdgeSymbolsFound = nodeLabelLength;
				pos.edgePosition = nodeLabelLength - 1;
				if (nodeLabelLength < streePathLength)
					isSearchDone = false;
			} else {
				noEdgeSymbolsFound = streePathLength;
				pos.edgePosition = streePathLength - 1;
			}
			return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
		} else {
			/* Find minimum out of edge length and string length, and scan it */
			if (streePathLength < nodeLabelLength) {
				nodeLabelLength = streePathLength;
			}

			pos.edgePosition = 1;
			for (edgePos = 1, noEdgeSymbolsFound = 1; edgePos < nodeLabelLength; edgePos++, noEdgeSymbolsFound++) {
				/*
				 * Compare current characters of the string and the edge. If
				 * equal - continue
				 */
				if (!sequence.substring(
						(pos.node.edgeLabelStart + edgePos) * encodingLength,
						(pos.node.edgeLabelStart + edgePos + 1)
								* encodingLength).equals(
						sequence.substring((streePath.begin + edgePos)
								* encodingLength,
								(streePath.begin + edgePos + 1)
										* encodingLength))) {
					edgePos--;
					pos.edgePosition = edgePos;
					// Logger.printReturn("Exiting traceSingleEdge");
					return new TraceSingleEdgeResult(noEdgeSymbolsFound,
							isSearchDone);
				}
			}
		}

		/* The loop has advanced edgePosition one too much */
		pos.edgePosition = edgePos;
		pos.edgePosition--;
		if (noEdgeSymbolsFound < streePathLength) {
			isSearchDone = false;
		}

		return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
	}

	private TraceSingleEdgeResult traceSingleEdge(SuffixTreePath streePath,
			String searchString, boolean skip) {

		/* Set default return values */
		int noEdgeSymbolsFound = 0;
		boolean isSearchDone = true;
		pos.edgePosition = 0;

		/*
		 * Search for the first character of the string in the outcoming edge of
		 * node
		 */

		SuffixNode contNode = findChild(searchString.substring(streePath.begin
				* encodingLength, (streePath.begin + 1) * encodingLength));

		if (contNode == null) {
			/* Search is done, string not found */

			pos.edgePosition = getNodeLabelLength(pos.node) - 1;
			noEdgeSymbolsFound = 0;

			return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
		}

		/* Found first character - prepare for continuing the search */
		pos.node = contNode;
		int nodeLabelLength = getNodeLabelLength(pos.node);
		int streePathLength = streePath.end - streePath.begin + 1;
		// System.out.println("streePathLength: "+streePathLength+" nodeLabelLength; "+nodeLabelLength);
		/* Compare edge length and string length. */
		/**
		 * If edge is shorter then the string being searched and skipping is
		 * enabled - skip edge
		 */

		int edgePos;
		if (skip) {
			if (nodeLabelLength <= streePathLength) {
				noEdgeSymbolsFound = nodeLabelLength;
				pos.edgePosition = nodeLabelLength - 1;
				if (nodeLabelLength < streePathLength)
					isSearchDone = false;
			} else {
				noEdgeSymbolsFound = streePathLength;
				pos.edgePosition = streePathLength - 1;
			}
			return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
		} else {
			/* Find minimum out of edge length and string length, and scan it */
			if (streePathLength < nodeLabelLength) {
				nodeLabelLength = streePathLength;
			}
			// System.out.println("nodeLabelLength: "+nodeLabelLength);
			pos.edgePosition = 1;
			for (edgePos = 1, noEdgeSymbolsFound = 1; edgePos < nodeLabelLength; edgePos++, noEdgeSymbolsFound++) {
				/*
				 * Compare current characters of the string and the edge. If
				 * equal - continue
				 */
				if (!sequence.substring(
						(pos.node.edgeLabelStart + edgePos) * encodingLength,
						(pos.node.edgeLabelStart + edgePos + 1)
								* encodingLength).equals(
						searchString.substring((streePath.begin + edgePos)
								* encodingLength,
								(streePath.begin + edgePos + 1)
										* encodingLength))) {
					edgePos--;
					pos.edgePosition = edgePos;
					// Logger.printReturn("Exiting traceSingleEdge");
					return new TraceSingleEdgeResult(noEdgeSymbolsFound,
							isSearchDone);
				}
			}
		}

		/* The loop has advanced edgePosition one too much */
		pos.edgePosition = edgePos;
		pos.edgePosition--;
		if (noEdgeSymbolsFound < streePathLength) {
			isSearchDone = false;
		}

		return new TraceSingleEdgeResult(noEdgeSymbolsFound, isSearchDone);
	}

	/**
	 * findChild : Finds the child of node that starts with a certain symbol.
	 * 
	 * Input :The node to start searching from and the symbol to be searched in
	 * the sons.
	 * 
	 * Output: The child node if it exists, null if no such child.
	 */
	private SuffixNode findChild(String symbol) {
		int i;

		int noChildren = pos.node.childrenList.size();

		for (i = 0; i < noChildren; i++) {
			if (sequence.substring(
					pos.node.childrenList.get(i).edgeLabelStart
							* encodingLength,
					(pos.node.childrenList.get(i).edgeLabelStart + 1)
							* encodingLength).equals(symbol)) {
				break;
			}
		}
		/* Have we found the child */
		if (i < noChildren) {
			pos.node = pos.node.childrenList.get(i);
			return pos.node;
		} else {
			return null;
		}

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
		if (node.firstChild == null)
			return this.e;
		// If it's not a leaf - return its real end
		return node.edgeLabelEnd;
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

	/*
	 * This function prints the tree. It simply starts the recursive function
	 * printNode with depth 0 (the root).
	 */
	public void printTree() {
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
				System.out.print(sequence.substring(start * encodingLength,
						(start + 1) * encodingLength));
				start++;
			}

			System.out.print("(" + node.isLeftDiverse + "  " + node.leftSymbol
					+ "  " + node.pathPosition + " : " + node.edgeLabelStart
					+ "," + end + ")");
			System.out.println();
		}

		/* Recursive call for all node's children */
		if (node.childrenList != null) {
			for (SuffixNode child : node.childrenList)
				printNode(child, depth + 1);
		}
	}

	public ArrayList<SuffixNode> getLeaves(SuffixNode node)
			throws StackOverflowError {
		ArrayList<SuffixNode> leaves = new ArrayList<SuffixNode>();
		try {

			if (node.firstChild == null) {
				leaves.add(node);
			}
			// System.out.println("No. Children: "+node.childrenList.size());
			for (SuffixNode child : node.childrenList)
				leaves.addAll(getLeaves(child));
		} catch (StackOverflowError e) {
			throw new StackOverflowError();
		}
		return leaves;
	}

	public int noMatches(String searchString) throws StackOverflowError {
		int noMatches = 0;
		SuffixTreePath streePath = new SuffixTreePath();

		pos.node = root;

		streePath.begin = 0;
		streePath.end = searchString.length() / encodingLength - 1;
		int searchStringLength = searchString.length() / encodingLength;

		noMatches = traceString(streePath, searchString, true);

		try {
			if (noMatches == searchStringLength) {
				return getLeaves(pos.node).size();
			}
		} catch (StackOverflowError e) {
			throw new StackOverflowError();
		}
		return 0;
	}

	public int[] getMatches(String searchString) {

		SuffixTreePath streePath = new SuffixTreePath();

		pos.node = root;

		streePath.begin = 0;
		streePath.end = searchString.length() / encodingLength - 1;
		int searchStringLength = searchString.length() / encodingLength;

		int noMatches = traceString(streePath, searchString, true);

		if (noMatches == searchStringLength) {
			ArrayList<SuffixNode> leaves = getLeaves(pos.node);
			int noLeaves = leaves.size();
			int[] matchingPos = new int[noLeaves];
			int i = 0;
			for (SuffixNode leafNode : leaves)
				matchingPos[i++] = leafNode.pathPosition;
			leaves = null;
			return matchingPos;
		} else {
			return null;
		}
	}

	/**
	 * Assess the left diversity (property) of nodes
	 * 
	 * A node v is called left diverse if at least two leaves in v's subtree
	 * have different left symbols; So, check for the leftSymbolSet to be of at
	 * least size 2; If the node qualifies to be leftDiverse, then set all its
	 * ancestors also as leftDiverse;
	 * 
	 */

	public void findLeftDiverseNodes() {
		ArrayList<SuffixNode> leaves = getLeaves(root);
		// System.out.println("No. Leaf Nodes in the Tree: " + leaves.size());
		/**
		 * Identify the leftSymbol of each leaf node and set the leftDiversity
		 * property of each leaf to false
		 */
		for (SuffixNode leafNode : leaves) {
			leafNode.leftSymbol = sequence.substring(
					(leafNode.pathPosition - 1) * encodingLength,
					leafNode.pathPosition * encodingLength);
			leafNode.isLeftDiverse = false;
			leafNode.isProcessed = true;
		}

		/**
		 * Bottom up traversal of each leaf Determine the leftDiversity of each
		 * internal node and propagate it to the ancestors
		 */
		SuffixNode parentNode;
		ArrayList<SuffixNode> leavesParentNode;
		HashSet<String> leftSymbolSet;
		for (SuffixNode leafNode : leaves) {
			parentNode = leafNode.parent;
			while (parentNode != null && !parentNode.isProcessed) {
				/* Get the leaves of this parent node */
				leavesParentNode = getLeaves(parentNode);

				leftSymbolSet = new HashSet<String>();

				for (SuffixNode currentLeafParentNode : leavesParentNode)
					leftSymbolSet.add(currentLeafParentNode.leftSymbol);

				if (leftSymbolSet.size() > 1) {
					parentNode.isLeftDiverse = true;
					setAncestorsLeftDiverse(parentNode);
					parentNode.isProcessed = true;
				}
				// Free Memory
				leavesParentNode = null;
				leftSymbolSet = null;
				parentNode = parentNode.parent;
			}
		}
	}

	/**
	 * Sets the leftDiversity property of the ancestors of the given node to be
	 * true
	 */
	private void setAncestorsLeftDiverse(SuffixNode node) {
		if (node != root && !node.parent.isProcessed) {
			node.parent.isLeftDiverse = true;
			setAncestorsLeftDiverse(node.parent);
			node.parent.isProcessed = true;
		}
	}

	/**
	 * @return the list of all leftDiverse nodes under the given node;
	 */
	public ArrayList<SuffixNode> getLeftDiverseNodes(SuffixNode node) {
		ArrayList<SuffixNode> leftDiverseNodeList = new ArrayList<SuffixNode>();
		if (node.isLeftDiverse)
			leftDiverseNodeList.add(node);
		for (SuffixNode child : node.childrenList)
			leftDiverseNodeList.addAll(getLeftDiverseNodes(child));

		return leftDiverseNodeList;
	}

	/**
	 * Get the maximal repeat strings (not necessarily tandem) in the input
	 * string for which the suffix tree is constructed
	 * 
	 * The algorithm is of Gusfield's;
	 * 
	 * 
	 * Determine the maximal repeats; A string alpha labeling a path to a node v
	 * of T is a maximal repeat iff v is leftDiverse Retrieve all
	 * leftDiverseNodes and print the paths leading to that node from the root
	 * The path information is stored in the class attributes pathPosition and
	 * edgeLabelEnd;
	 * 
	 * Returns a HashSet of maximal Repeats;
	 * 
	 * Assumes that the parent method calling this would have invoked
	 * findLeftDiverseNodes();
	 */

	public HashSet<String> getMaximalRepeats() {
		HashSet<String> maximalRepeats = new HashSet<String>();

		ArrayList<SuffixNode> leftDiverseNodeList = getLeftDiverseNodes(root);
		int noLeftDiverseNodes = leftDiverseNodeList.size();

		if (noLeftDiverseNodes == 0) {
			System.out
					.println("Looks like findLeftDiverseNodes() is not invoked");
			return null;
		}
		System.out.println("No. Left Diverse Nodes: " + noLeftDiverseNodes);
		for (SuffixNode leftDiverseNode : leftDiverseNodeList) {
			maximalRepeats.add(sequence.substring(leftDiverseNode.pathPosition
					* encodingLength, (leftDiverseNode.edgeLabelEnd + 1)
					* encodingLength));
		}

		return maximalRepeats;
	}

	public HashSet<String> getFilteredMaximalRepeats() {
		HashSet<String> maximalRepeats = getMaximalRepeats();
		HashSet<String> filteredMaximalRepeats = new HashSet<String>();

		String[] repeatSplit;
		String splitPattern = "\\$";
		for (int i = 1; i < encodingLength; i++)
			splitPattern += "\\$";
		for (String repeat : maximalRepeats) {
			if (repeat.length() >= encodingLength) {
				repeatSplit = repeat.split(splitPattern);
				for (String currentSplitRepeat : repeatSplit) {
					if (currentSplitRepeat.length() >= encodingLength)
						filteredMaximalRepeats.add(currentSplitRepeat);
				}
			}
		}

		return filteredMaximalRepeats;
	}

	/**
	 * Find SuperMaximal Repeats; A left diverse internal node v represents a
	 * super maximal repeat alpha if and only if all of v's children are leaves,
	 * and each has a distinct left character
	 * 
	 * Assumes that the parent method calling this would have already invoked
	 * findLeftDiverseNodes();
	 * 
	 */
	public HashSet<String> getSuperMaximalRepeats() {
		HashSet<String> superMaximalRepeats = new HashSet<String>();

		ArrayList<SuffixNode> leftDiverseNodeList = getLeftDiverseNodes(root);
		int noLeftDiverseNodes = leftDiverseNodeList.size();

		if (noLeftDiverseNodes == 0) {
			System.out
					.println("Looks like findLeftDiverseNodes() is not invoked");
			return null;
		}

		boolean isAllChildrenLeaves;
		HashSet<String> leftSymbolSet;
		for (SuffixNode leftDiverseNode : leftDiverseNodeList) {
			isAllChildrenLeaves = true;
			leftSymbolSet = new HashSet<String>();
			for (SuffixNode child : leftDiverseNode.childrenList) {
				if (child.firstChild != null) {
					isAllChildrenLeaves = false;
					break;
				} else {
					leftSymbolSet.add(child.leftSymbol);
				}
			}
			// Check for the distinct left character
			if (isAllChildrenLeaves
					&& leftSymbolSet.size() == leftDiverseNode.childrenList
							.size()) {
				superMaximalRepeats.add(sequence.substring(
						leftDiverseNode.pathPosition * encodingLength,
						(leftDiverseNode.edgeLabelEnd + 1) * encodingLength));
			}
		}

		return superMaximalRepeats;
	}

	public HashSet<String> getFilteredSuperMaximalRepeats() {
		HashSet<String> filteredSuperMaximalRepeats = new HashSet<String>();

		HashSet<String> superMaximalRepeats = getSuperMaximalRepeats();
		String[] repeatSplit;
		String splitPattern = "\\$";
		for (int i = 1; i < encodingLength; i++)
			splitPattern += "\\$";
		for (String repeat : superMaximalRepeats) {
			if (repeat.length() >= encodingLength) {
				repeatSplit = repeat.split(splitPattern);
				for (String currentSplitRepeat : repeatSplit) {
					if (currentSplitRepeat.length() >= encodingLength)
						filteredSuperMaximalRepeats.add(currentSplitRepeat);
				}
			}
		}

		return filteredSuperMaximalRepeats;
	}

	/**
	 * Find NearSuperMaximal Repeats; A left diverse internal node v represents
	 * a near super maximal repeat alpha if and only if one of v's children is a
	 * leaf and its left character is the left character of no other leaf below
	 * v
	 * 
	 * Assumes that the parent method calling this would have already invoked
	 * findLeftDiverseNodes();
	 * 
	 */
	public HashSet<String> getNearSuperMaximalRepeats() {
		HashSet<String> nearSuperMaximalRepeats = new HashSet<String>();

		ArrayList<SuffixNode> leftDiverseNodeList = getLeftDiverseNodes(root);
		int noLeftDiverseNodes = leftDiverseNodeList.size();

		if (noLeftDiverseNodes == 0) {
			System.out
					.println("Looks like findLeftDiverseNodes() is not invoked");
			return null;
		}

		ArrayList<SuffixNode> leavesList;
		ArrayList<SuffixNode> childrenThatAreLeaves;
		HashMap<String, Integer> leftSymbolCountMap;
		int count;
		for (SuffixNode leftDiverseNode : leftDiverseNodeList) {

			childrenThatAreLeaves = new ArrayList<SuffixNode>();
			leftSymbolCountMap = new HashMap<String, Integer>();
			for (SuffixNode child : leftDiverseNode.childrenList) {
				if (child.firstChild == null) {
					childrenThatAreLeaves.add(child);

					count = 1;
					if (leftSymbolCountMap.containsKey(child.leftSymbol)) {
						count = leftSymbolCountMap.get(child.leftSymbol);
						count++;
					}
					leftSymbolCountMap.put(child.leftSymbol, count);
				} else {
					leavesList = getLeaves(child);
					for (SuffixNode leaf : leavesList) {
						count = 1;
						if (leftSymbolCountMap.containsKey(leaf.leftSymbol)) {
							count = leftSymbolCountMap.get(leaf.leftSymbol);
							count++;
						}
						leftSymbolCountMap.put(leaf.leftSymbol, count);
					}
				}

			}
			// Check for the criteria
			boolean isNearSuperMaximal = false;
			if (childrenThatAreLeaves.size() > 0) {
				for (SuffixNode child : childrenThatAreLeaves) {
					if (leftSymbolCountMap.get(child.leftSymbol) == 1) {
						isNearSuperMaximal = true;
						break;
					}
				}
			}
			if (isNearSuperMaximal) {
				nearSuperMaximalRepeats.add(sequence.substring(
						leftDiverseNode.pathPosition * encodingLength,
						(leftDiverseNode.edgeLabelEnd + 1) * encodingLength));
			}
		}

		return nearSuperMaximalRepeats;
	}

	public HashSet<String> getFilteredNearSuperMaximalRepeats() {
		HashSet<String> filteredNearSuperMaximalRepeats = new HashSet<String>();

		HashSet<String> nearSuperMaximalRepeats = getNearSuperMaximalRepeats();
		String[] repeatSplit;
		String splitPattern = "\\$";
		for (int i = 1; i < encodingLength; i++)
			splitPattern += "\\$";
		for (String repeat : nearSuperMaximalRepeats) {
			if (repeat.length() >= encodingLength) {
				repeatSplit = repeat.split(splitPattern);
				for (String currentSplitRepeat : repeatSplit) {
					if (currentSplitRepeat.length() > encodingLength)
						filteredNearSuperMaximalRepeats.add(currentSplitRepeat);
				}
			}
		}

		return filteredNearSuperMaximalRepeats;
	}

	@SuppressWarnings("unchecked")
	public void LZDecomposition() {

		int[] s = new int[sequenceLength];
		int[] l = new int[sequenceLength];

		tandemPairs = new TreeSet[sequenceLength];
		for (int i = 0; i < sequenceLength; i++) {
			tandemPairs[i] = new TreeSet<Integer>();
		}

		s[0] = 0;
		l[0] = 0;

		String currentSymbol;
		int j;
		for (int i = 2; i <= sequenceLength; i++) {
			j = i - 1;

			currentSymbol = this.sequence.substring(i * encodingLength, (i + 1)
					* encodingLength);

			pos.node = root;
			SuffixNode childNode = findChild(currentSymbol);
			if (childNode == null) {
				System.out.println("Something terribly Wrong; Current Symbol "
						+ currentSymbol + " not found");
				System.exit(0);
			}

			int noMatches = childNode.edgeLabelEnd - childNode.edgeLabelStart
					+ 1;
			if (i != childNode.pathPosition) {
				l[j] = noMatches;
				s[j] = childNode.pathPosition;
			} else {
				l[j] = 0;
				s[j] = 0;
				continue;
			}

			int prevPathPos = childNode.pathPosition;
			String nextSymbol;

			i += noMatches - 1;

			while (++i < sequenceLength) {
				nextSymbol = sequence.substring(i * encodingLength, (i + 1)
						* encodingLength);

				pos.node = childNode;
				childNode = findChild(nextSymbol);
				if (childNode == null) {
					break;
				}

				if (childNode.edgeLabelStart < i) {
					i += childNode.edgeLabelEnd - childNode.edgeLabelStart;
					noMatches += childNode.edgeLabelEnd
							- childNode.edgeLabelStart + 1;
					prevPathPos = childNode.pathPosition;
				} else {
					break;
				}
			}

			s[j] = prevPathPos;
			l[j] = noMatches;
			i = j + 1;
		}

		// Vector of Blocks
		ArrayList<Integer> I = new ArrayList<Integer>();

		I.add(1);
		j = 0;

		while (I.get(j) <= sequenceLength - 1) {
			I.add(I.get(j) + Math.max(1, l[I.get(j) - 1]));
			j++;
		}

		int noBlocks = I.size() - 1;
		ArrayList<String> blocks = new ArrayList<String>();
		int[] h = new int[noBlocks + 1];
		for (int i = 0; i < noBlocks; i++) {
			blocks.add(sequence.substring(I.get(i) * encodingLength, I
					.get(i + 1)
					* encodingLength));
			h[i] = I.get(i);
		}

		blocks.add(terminationSymbol);
		h[noBlocks] = I.get(noBlocks);
		noBlocks++; // consider the termination symbol block

		int currentBlockStart, nextBlockStart, currentBlockLength, nextBlockLength;
		String currentBlock, nextBlock;
		for (int i = 0; i < noBlocks - 1; i++) {
			currentBlock = blocks.get(i);
			currentBlockLength = currentBlock.length() / encodingLength;
			currentBlockStart = h[i];

			nextBlock = blocks.get(i + 1);
			nextBlockLength = nextBlock.length() / encodingLength;
			nextBlockStart = h[i + 1];
			processBlockAlgorithm1A(currentBlockLength, nextBlockStart);
			processBlockAlgorithm1B(currentBlockLength, nextBlockLength,
					currentBlockStart, nextBlockStart);
		}

		// TreeSet<String> tandemRepeatSet = getTandemRepeats();
		// for(String tandemRepeat: tandemRepeatSet)
		// System.out.println(tandemRepeat);
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

	public TreeSet<String> getTandemRepeats() {
		TreeSet<String> tandemRepeatSet = new TreeSet<String>();
		String tandemRepeat;
		for (int i = 0; i < sequenceLength; i++) {
			if (tandemPairs[i].size() > 0) {
				for (Integer loc : tandemPairs[i]) {
					tandemRepeat = sequence.substring((i + 1) * encodingLength,
							(i + 1 + loc) * encodingLength);
					tandemRepeatSet.add(tandemRepeat);
				}
			}
		}

		return tandemRepeatSet;
	}

	/*
	 * private String getPrimitiveRepeat(String tandemRepeat){ String
	 * primitiveRepeat = ""; TreeSet<String> tandemRepeatAlphabet = new
	 * TreeSet<String>(); TreeSet<String> alphabetTandemRepeatSet,
	 * alphabetPrimitiveRepeatSet; boolean isComplex = false;
	 * 
	 * int tandemRepeatLength = tandemRepeat.length()/encodingLength;
	 * 
	 * 
	 * for(int i = 0; i < tandemRepeatLength; i++){
	 * tandemRepeatAlphabet.add(tandemRepeat.substring(i*encodingLength,
	 * (i+1)*encodingLength)); }
	 * 
	 * if(alphabetTandemRepeatMap.containsKey(tandemRepeatAlphabet)){
	 * alphabetTandemRepeatSet =
	 * alphabetPrimitiveRepeatMap.get(tandemRepeatAlphabet); }else{
	 * alphabetTandemRepeatSet = new TreeSet<String>(); }
	 * alphabetTandemRepeatSet.add(tandemRepeat);
	 * alphabetTandemRepeatMap.put(tandemRepeatAlphabet,
	 * alphabetTandemRepeatSet);
	 *//**
	 * Simple Cases
	 */
	/*
	 * if(tandemRepeatAlphabet.size() == 1){ primitiveRepeat =
	 * tandemRepeat.substring(0, encodingLength); }else if(tandemRepeatLength ==
	 * tandemRepeatAlphabet.size()){ primitiveRepeat = tandemRepeat; }else{
	 * isComplex = true; complexTandemRepeats.add(tandemRepeat); }
	 * 
	 * if(!isComplex){
	 * if(alphabetPrimitiveRepeatMap.containsKey(tandemRepeatAlphabet)){
	 * alphabetPrimitiveRepeatSet =
	 * alphabetPrimitiveRepeatMap.get(tandemRepeatAlphabet); }else{
	 * alphabetPrimitiveRepeatSet = new TreeSet<String>(); }
	 * 
	 * alphabetPrimitiveRepeatSet.add(primitiveRepeat);
	 * alphabetPrimitiveRepeatMap
	 * .put(tandemRepeatAlphabet,alphabetPrimitiveRepeatSet);
	 * 
	 * }
	 * 
	 * return primitiveRepeat; }
	 */

	public HashMap<TreeSet<String>, TreeSet<String>> getPrimitiveTandemRepeats() {
		TreeSet<String> tandemRepeatSet = getTandemRepeats();
		// System.out.println(tandemRepeatSet.size());
		// for(String tr: tandemRepeatSet)
		// System.out.println(tr);
		HashSet<String> primitiveRepeatSet = new HashSet<String>();

		ArrayList<String> tandemRepeatArrayList = new ArrayList<String>();

		HashMap<String, TreeSet<String>> tandemRepeatAlphabetMap = new HashMap<String, TreeSet<String>>();
		HashMap<TreeSet<String>, TreeSet<String>> alphabetTandemRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		HashMap<TreeSet<String>, TreeSet<String>> alphabetPrimitiveRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();

		complexTandemRepeats = new HashSet<String>();

		int tandemRepeatLength;
		String tandemRepeat;
		TreeSet<String> tandemRepeatAlphabetSet, alphabetTandemRepeatSet, alphabetPrimitiveRepeatSet;
		for (String tandemRepeatPair : tandemRepeatSet) {
			tandemRepeatLength = tandemRepeatPair.length() / encodingLength;
			tandemRepeatLength /= 2; // The division by 2 is to consider only
			// the repeat alpha

			tandemRepeat = tandemRepeatPair.substring(0, tandemRepeatLength
					* encodingLength);
			tandemRepeatArrayList.add(tandemRepeat);

			tandemRepeatAlphabetSet = new TreeSet<String>();
			for (int i = 0; i < tandemRepeatLength; i++) {
				tandemRepeatAlphabetSet.add(tandemRepeat.substring(i
						* encodingLength, (i + 1) * encodingLength));
			}

			tandemRepeatAlphabetMap.put(tandemRepeat, tandemRepeatAlphabetSet);

			if (alphabetTandemRepeatMap.containsKey(tandemRepeatAlphabetSet)) {
				alphabetTandemRepeatSet = alphabetTandemRepeatMap
						.get(tandemRepeatAlphabetSet);
			} else {
				alphabetTandemRepeatSet = new TreeSet<String>();
			}
			alphabetTandemRepeatSet.add(tandemRepeat);
			alphabetTandemRepeatMap.put(tandemRepeatAlphabetSet,
					alphabetTandemRepeatSet);
		}

		/**
		 * Free Memory
		 */
		tandemRepeatSet = null;

		int noTandemRepeats = tandemRepeatArrayList.size();
		boolean[] isTandemRepeatProcessed = new boolean[noTandemRepeats];

		for (int i = 0; i < noTandemRepeats; i++) {
			isTandemRepeatProcessed[i] = false;
		}

		int index, noProcessed = 0;
		boolean isComplex, isNegligable;
		Pattern prPattern;
		Matcher prMatcher;
		String primitiveRepeat;
		for (String tr : tandemRepeatArrayList) {
			index = tandemRepeatArrayList.indexOf(tr);
			tandemRepeatLength = tr.length() / encodingLength;
			if (!isTandemRepeatProcessed[index]) {
				isComplex = false;
				isNegligable = false;
				primitiveRepeat = "";
				tandemRepeatAlphabetSet = tandemRepeatAlphabetMap.get(tr);

				/**
				 * Simple Cases 1. When the tandem repeat is made up of only one
				 * symbol i.e., the alphabet size is 1 2. When the tandem
				 * repeat, alpha is itself a primitive repeat i.e., the size of
				 * alpha is the same as the size of the alphabet
				 */

				if (tandemRepeatAlphabetSet.size() == 1) {
					primitiveRepeat = tr.substring(0, encodingLength);
					primitiveRepeatSet.add(primitiveRepeat);
					isTandemRepeatProcessed[index] = true;
					noProcessed++;
				} else if (tandemRepeatAlphabetSet.size() == tandemRepeatLength) {
					primitiveRepeat = tr;
					primitiveRepeatSet.add(tr);
				} else if (tandemRepeatAlphabetSet.size() == tandemRepeatLength - 1) {
					primitiveRepeat = tr;
					primitiveRepeatSet.add(tr);
				}/*
				 * else if(tandemRepeatLength/tandemRepeatAlphabetSet.size() >
				 * 25){
				 *//**
				 * Get the contribution (frequency of each symbol) If the
				 * contribution of an alphabet is just 1 or a few (which is
				 * negligable to that of the total length), then split the tr
				 * using that negligable symbol
				 */
				/*
				 * 
				 * // System.out.println("Negligable Case"); //
				 * System.out.println(tr); isNegligable = true;
				 * UkkonenSuffixTree st = new
				 * UkkonenSuffixTree(encodingLength,tr); st.LZDecomposition();
				 * HashMap<TreeSet<String>,TreeSet<String>> tprAM
				 * =st.getPrimitiveTandemRepeats(); for(TreeSet<String> prA :
				 * tprAM.keySet()){ primitiveRepeatSet.addAll(tprAM.get(prA));
				 * if(alphabetPrimitiveRepeatMap.containsKey(prA))
				 * alphabetPrimitiveRepeatSet =
				 * alphabetPrimitiveRepeatMap.get(prA); else
				 * alphabetPrimitiveRepeatSet = new TreeSet<String>();
				 * alphabetPrimitiveRepeatSet.addAll(tprAM.get(prA));
				 * alphabetPrimitiveRepeatMap.put(prA,
				 * alphabetPrimitiveRepeatSet); }
				 * 
				 * if(st.getComplexTandemRepeats().size() > 0)
				 * complexTandemRepeats.addAll(st.getComplexTandemRepeats()); }
				 */else {
					/**
					 * Check if this tr is in itself a tandem repeat
					 * 
					 */
					// System.out.println("T: "+tr+" @ "+tandemRepeatAlphabetSet.size());
					boolean found = false;
					UkkonenSuffixTree st = new UkkonenSuffixTree(
							encodingLength, tr);
					st.LZDecomposition();
					Set<String> tempPRSet = st.getTandemRepeats();
					// System.out.println(st.getPrimitiveTandemRepeats());
					// System.out.println(tempPRSet);
					for (String tempPR : tempPRSet)
						if (tr.equals(tempPR)) {
							// System.out.println("HHH: "+tr);
							found = true;
							break;
						}

					if (found) {
						// System.out.println("Adding Complex: "+tr.substring(0*encodingLength,tandemRepeatLength*encodingLength/2));
						complexTandemRepeats.add(tr.substring(
								0 * encodingLength, tandemRepeatLength
										* encodingLength / 2));
					} else {
						// System.out.println("Adding Complex: "+tr);
						complexTandemRepeats.add(tr);
					}

					isComplex = true;
				}

				if (!isComplex && !isNegligable) {
					if (alphabetPrimitiveRepeatMap
							.containsKey(tandemRepeatAlphabetSet)) {
						alphabetPrimitiveRepeatSet = alphabetPrimitiveRepeatMap
								.get(tandemRepeatAlphabetSet);
					} else {
						alphabetPrimitiveRepeatSet = new TreeSet<String>();
					}
					alphabetPrimitiveRepeatSet.add(primitiveRepeat);
					alphabetPrimitiveRepeatMap.put(tandemRepeatAlphabetSet,
							alphabetPrimitiveRepeatSet);

					/**
					 * Applicable for both the simple cases 1. and 2. Since the
					 * tandem repeat list is sorted; the smallest tandem repeat
					 * pair would be alpha^2; However, there can be similar
					 * tandem repeats of the type alpha^(2*n); Set the primitive
					 * repeat type to all such tandem repeats a. Get all tandem
					 * repeats that share the same alphabet b. Verify if the
					 * tandem repeat is of type alpha^(2*n). There can be
					 * instances where two tandem repeats share the same
					 * alphabet but alpha's are different
					 */

					alphabetTandemRepeatSet = alphabetTandemRepeatMap
							.get(tandemRepeatAlphabetSet);
					prPattern = Pattern.compile("(" + primitiveRepeat + ")+");

					for (String similarTandemRepeat : alphabetTandemRepeatSet) {
						// System.out.println(primitiveRepeat+"  STR: "+similarTandemRepeat);
						prMatcher = prPattern.matcher(similarTandemRepeat);
						if (prMatcher.replaceAll("").equals("")) {
							index = tandemRepeatArrayList
									.indexOf(similarTandemRepeat);
							isTandemRepeatProcessed[index] = true;
							noProcessed++;

							// System.out.println(similarTandemRepeat+" @ "+primitiveRepeat);
						}
					}
				}
			}
		}

		/**
		 * By now all simple cases would have been handled; Let us process
		 * complex repeats now It is important to process all simple cases
		 * before we proceed to tackle complex cases
		 */
		String prRegEx;
		Iterator<String> it;
		TreeSet<String> toConsiderPrimitiveRepeatSet;
		HashSet<String> processedComplexTandemRepeats = new HashSet<String>();
		HashSet<String> newComplexTandemRepeats = new HashSet<String>();
		int trLength;
		for (String tr : complexTandemRepeats) {
			// System.out.println(tandemRepeatAlphabetMap.size());
			if (tandemRepeatAlphabetMap.containsKey(tr))
				tandemRepeatAlphabetSet = tandemRepeatAlphabetMap.get(tr);
			else {
				tandemRepeatAlphabetSet = new TreeSet<String>();
				trLength = tr.length() / encodingLength;
				for (int jj = 0; jj < trLength; jj++)
					tandemRepeatAlphabetSet.add(tr.substring(jj
							* encodingLength, (jj + 1) * encodingLength));
				tandemRepeatAlphabetMap.put(tr, tandemRepeatAlphabetSet);
			}
			// System.out.println(tr+" @ "+tandemRepeatAlphabetSet);
			/**
			 * A Complex Case of a Tandem Repeat Many Scenarios exist 1. Check
			 * whether this tandem repeat is a combination of other primitive
			 * repeats a.Check if there exists primitive repeats with same
			 * alphabet as that of this tandem repeat Then it might be the case
			 * that this tandem repeat is a combination of the other primitive
			 * repeats (resulting out of variation in ordering - parallelism
			 * within loops)
			 * 
			 * e.g., d5i4o4p1o4p1d5i4 is the current tandem repeat that is
			 * complex there exist primitive repeats d5i4o4p1 and o4p1d5i4
			 * sharing the same alphabet
			 * 
			 * 2. The exact alphabet doesn't exist; Check whether the complex
			 * tandem repeat is a combination of primitive repeats involving the
			 * subset alphabets
			 * 
			 * e.g., o4p1o4p1d5i4d5i4
			 */

			toConsiderPrimitiveRepeatSet = new TreeSet<String>(
					new DescendingStrCompartor());
			for (TreeSet<String> alphabetSet : alphabetPrimitiveRepeatMap
					.keySet()) {

				if (tandemRepeatAlphabetSet.containsAll(alphabetSet)) {
					for (String ptr : alphabetPrimitiveRepeatMap
							.get(alphabetSet))
						if (tr.contains(ptr))
							toConsiderPrimitiveRepeatSet.add(ptr);
				}
			}

			prRegEx = "(";
			it = toConsiderPrimitiveRepeatSet.iterator();
			while (it.hasNext()) {
				prRegEx += "(" + it.next() + ")";
				if (it.hasNext())
					prRegEx += "|";
			}
			prRegEx += ")+";

			prPattern = Pattern.compile(prRegEx);
			prMatcher = prPattern.matcher(tr);
			// System.out.println(tr+" @ "+prRegEx);
			if (prMatcher.replaceAll("").equals("")) {
				// System.out.println("Solved: "+tr);
				processedComplexTandemRepeats.add(tr);
			} else {
				/**
				 * Alphabet Matching: But a new Pattern exists Check if the
				 * remaining substring in the tandem repeat is a permutation of
				 * the alphabet if not a permutation,then it can be a case that
				 * the remaining substring consists of a subset of the alphabet
				 * 
				 * e.g., ctr: d5i4o4p1p1o4d5i4 now p1o4d5i4 doesn't exist as a
				 * pr; but the alphabet is the same; may be in this log it is
				 * not manifested
				 * 
				 * Write these as complex tandem repeats (unprocessed); If there
				 * exist another permutation in any other log, we can process
				 * them separately
				 */

				/**
				 * Also, there can be a tandem Repeat within this
				 */
				// System.out.println("In Else: "+tr);
				UkkonenSuffixTree st = new UkkonenSuffixTree(encodingLength, tr);
				st.LZDecomposition();
				HashMap<TreeSet<String>, TreeSet<String>> tempMap = st
						.getPrimitiveTandemRepeats();
				TreeSet<String> aptrs;

				for (TreeSet<String> as : tempMap.keySet()) {
					if (alphabetPrimitiveRepeatMap.containsKey(as)) {
						aptrs = alphabetPrimitiveRepeatMap.get(as);
					} else {
						aptrs = new TreeSet<String>();
					}
					aptrs.addAll(tempMap.get(as));
					alphabetPrimitiveRepeatMap.put(as, aptrs);
				}

				if (tempMap.size() > 0) {
					processedComplexTandemRepeats.add(tr);
				}
				/**
				 * All of new complex tandem repeats need not be complex
				 * q9O0O2A0O1G6c1w6b3F6a3y4K4a3K4q9O0O2A0O1G6c1w6b3F6a3y4K4a3K4
				 */

				HashSet<String> comTR = st.getComplexTandemRepeats();
				HashSet<String> tempProcessed = new HashSet<String>();

				for (String cTr : comTR) {
					Pattern pt = Pattern.compile("(" + cTr + "){2,}");
					Matcher mc = pt.matcher(tr);
					if (mc.find()) {
						processedComplexTandemRepeats.add(tr);
						tempProcessed.add(tr);
					}
				}
				comTR.removeAll(tempProcessed);
				newComplexTandemRepeats.removeAll(tempProcessed);
				newComplexTandemRepeats.addAll(comTR);
			}

		}

		complexTandemRepeats.removeAll(processedComplexTandemRepeats);
		complexTandemRepeats.addAll(newComplexTandemRepeats);

		/**
		 * Populate complexAlphabetTandemRepeatMap
		 */
		complexAlphabetTandemRepeatMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		int complexTRLength;
		for (String complexTR : complexTandemRepeats) {
			if (tandemRepeatAlphabetMap.containsKey(complexTR))
				tandemRepeatAlphabetSet = tandemRepeatAlphabetMap
						.get(complexTR);
			else {
				tandemRepeatAlphabetSet = new TreeSet<String>();
				complexTRLength = complexTR.length() / encodingLength;
				for (int jj = 0; jj < complexTRLength; jj++)
					tandemRepeatAlphabetSet.add(complexTR.substring(jj
							* encodingLength, (jj + 1) * encodingLength));
				tandemRepeatAlphabetMap.put(complexTR, tandemRepeatAlphabetSet);
			}
			if (complexAlphabetTandemRepeatMap
					.containsKey(tandemRepeatAlphabetSet)) {
				tandemRepeatSet = complexAlphabetTandemRepeatMap
						.get(tandemRepeatAlphabetSet);
			} else {
				tandemRepeatSet = new TreeSet<String>();
			}
			tandemRepeatSet.add(complexTR);
			complexAlphabetTandemRepeatMap.put(tandemRepeatAlphabetSet,
					tandemRepeatSet);
		}

		// Free Memory
		processedComplexTandemRepeats = null;
		newComplexTandemRepeats = null;
		tandemRepeatAlphabetMap = null;
		alphabetTandemRepeatMap = null;

		// System.out.println("Primitive Repeat Set");
		// for(TreeSet<String> alphabetSet :
		// alphabetPrimitiveRepeatMap.keySet()){
		// System.out.println(alphabetSet+" @ "+alphabetPrimitiveRepeatMap.get(alphabetSet));
		// }
		//	
		// System.out.println("Complex Tandem Repeats");
		// for(String complexTandemRepeat : complexTandemRepeats)
		// System.out.println(complexTandemRepeat);

		return alphabetPrimitiveRepeatMap;
	}

	public HashSet<String> getComplexTandemRepeats() {
		return complexTandemRepeats;
	}

	public HashMap<TreeSet<String>, TreeSet<String>> getComplexAlphabetTandemRepeatMap() {
		return complexAlphabetTandemRepeatMap;
	}

	public <T> void printArray(T[] arrayT) {
		for (T t : arrayT)
			System.out.print(t + " ");
		System.out.println();
	}
}
