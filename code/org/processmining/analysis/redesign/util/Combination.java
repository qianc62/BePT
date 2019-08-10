package org.processmining.analysis.redesign.util;

/**
 * Author: Mariska Netjes
 * (c) 2008 Technische Universiteit Eindhoven and STW
 */

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.processmining.framework.models.petrinet.PNNode;

/**
 * Generates all possible combinations for a given set of PNNodes.
 */
public class Combination {

	private static List<List<PNNode>> combinations = new ArrayList<List<PNNode>>();

	public Combination() {

	}

	/**
	 * Generates all the possible combinations for a given set of objects.
	 * 
	 * @return
	 * 
	 */
	public static List<List<PNNode>> getCombinations(List<PNNode> list) {
		/**
		 * Add this set to our combination results set
		 */
		if (list.size() != 1) {
			combinations.add(list);
		}
		/**
		 * If the object set only contains one object we stop
		 */
		if (list.size() == 1) {
			combinations.add(list);
		}
		/**
		 * Go through every object of the set of objects
		 */
		if (list.size() > 1) {
			for (int i = 0; i < list.size(); i++) {
				/**
				 * Remove the object at the current position from objects this
				 * method is called with all resulting objects
				 */
				List<PNNode> subList1 = list.subList(0, i);
				List<PNNode> subList2 = list.subList(i + 1, list.size());
				List<PNNode> subList = new ArrayList<PNNode>();
				subList.addAll(subList1);
				subList.addAll(subList2);
				if (!combinations.contains(subList)) {
					getCombinations(subList);
				}
			}
		}
		return combinations;
	}

	// =============Determination of the power set of a set of
	// strings============
	public static void main(String[] args) {

		// construct the set S = {a,b,c}
		String set[] = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k",
				"l", "m", "n", "o", "p", "q", "r" };

		// form the power set
		LinkedHashSet myPowerSet = powerset(set);

		// display the power set
		System.out.println(myPowerSet.toString());

	}

	/**
	 * Returns the power set from the given set by using a binary counter
	 * Example: S = {a,b,c} P(S) = {[], [c], [b], [b, c], [a], [a, c], [a, b],
	 * [a, b, c]}
	 * 
	 * @param set
	 *            String[]
	 * @return LinkedHashSet
	 */
	private static LinkedHashSet powerset(String[] set) {

		// create the empty power set
		LinkedHashSet power = new LinkedHashSet();

		// get the number of elements in the set
		int elements = set.length;

		// the number of members of a power set is 2^n
		int powerElements = (int) Math.pow(2, elements);

		// run a binary counter for the number of power elements
		for (int i = 0; i < powerElements; i++) {

			// convert the binary number to a string containing n digits
			String binary = intToBinary(i, elements);

			// create a new set
			LinkedHashSet innerSet = new LinkedHashSet();

			// convert each digit in the current binary number to the
			// corresponding element
			// in the given set
			for (int j = 0; j < binary.length(); j++) {
				if (binary.charAt(j) == '1')
					innerSet.add(set[j]);
			}

			// add the new set to the power set
			power.add(innerSet);

		}

		return power;
	}

	/**
	 * Converts the given integer to a String representing a binary number with
	 * the specified number of digits For example when using 4 digits the binary
	 * 1 is 0001
	 * 
	 * @param binary
	 *            int
	 * @param digits
	 *            int
	 * @return String
	 */
	private static String intToBinary(int binary, int digits) {

		String temp = Integer.toBinaryString(binary);
		int foundDigits = temp.length();
		String returner = temp;
		for (int i = foundDigits; i < digits; i++) {
			returner = "0" + returner;
		}

		return returner;
	}

}
