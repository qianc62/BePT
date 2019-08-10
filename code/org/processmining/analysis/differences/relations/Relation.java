package org.processmining.analysis.differences.relations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Relation<T1, T2> {
	// Attributes for the relation
	private Set<Tuple<T1, T2>> s = new HashSet<Tuple<T1, T2>>();
	private Set<T1> dom = new HashSet<T1>();
	private Set<T2> ran = new HashSet<T2>();

	// Attributes for the adjacency matrix
	private int adjmat[][];
	private Map<T1, Integer> elementToInt;
	private Map<Integer, T1> intToElement;
	private boolean adjmatComputed = false;
	private boolean tcloscomputed = false;
	private boolean rcloscomputed = false;
	private boolean scloscomputed = false;

	public Relation() {
		s = new HashSet<Tuple<T1, T2>>();
		dom = new HashSet<T1>();
		ran = new HashSet<T2>();

		adjmat = new int[0][0];
		elementToInt = new HashMap<T1, Integer>();
		intToElement = new HashMap<Integer, T1>();
		adjmatComputed = false;
		tcloscomputed = false;
		rcloscomputed = false;
		scloscomputed = false;
	}

	public Set<T1> getDom() {
		return dom;
	}

	public void setDom(Set<T1> dom) {
		adjmatComputed = false;
		tcloscomputed = false;
		rcloscomputed = false;
		scloscomputed = false;
		this.dom = dom;
	}

	public Set<T2> getRan() {
		return ran;
	}

	public void setRan(Set<T2> ran) {
		adjmatComputed = false;
		tcloscomputed = false;
		rcloscomputed = false;
		scloscomputed = false;
		this.ran = ran;
	}

	public Set<Tuple<T1, T2>> getS() {
		return s;
	}

	public void setS(Set<Tuple<T1, T2>> s) {
		adjmatComputed = false;
		tcloscomputed = false;
		rcloscomputed = false;
		scloscomputed = false;
		this.s = s;
	}

	public void addR(Tuple<T1, T2> r) {
		adjmatComputed = false;
		tcloscomputed = false;
		rcloscomputed = false;
		scloscomputed = false;

		dom.add(r.e1);
		ran.add(r.e2);
		s.add(r);
	}

	/**
	 * Computes the transitive closure of the adjacency matrix using Warshall's
	 * algorithm Pre: getDom() == getRan()
	 */
	public void tclos() {
		tclos(true);
	}

	private void tclos(boolean putback) {
		if (!adjmatComputed) {
			computeAdjMat();
		}
		int max = elementToInt.size();
		for (int k = 0; k < max; k++)
			for (int i = 0; i < max; i++)
				if (adjmat[i][k] == 1)
					for (int j = 0; j < max; j++)
						if (adjmat[k][j] == 1)
							adjmat[i][j] = 1;

		tcloscomputed = true;
		if (putback) {
			putback();
		}
	}

	/**
	 * Computes the reflexive closure of the adjacency matrix Pre: getDom() ==
	 * getRan()
	 */
	public void rclos() {
		rclos(true);
	}

	private void rclos(boolean putback) {
		if (!adjmatComputed) {
			computeAdjMat();
		}
		int max = elementToInt.size();
		for (int i = 0; i < max; i++) {
			adjmat[i][i] = 1;
		}
		rcloscomputed = true;
		if (putback) {
			putback();
		}
	}

	/**
	 * Computes the symmetric closure of the adjacency matrix Pre: getDom() ==
	 * getRan()
	 */
	public void sclos() {
		sclos(true);
	}

	private void sclos(boolean putback) {
		if (!adjmatComputed) {
			computeAdjMat();
		}
		int max = elementToInt.size();
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max; j++) {
				if (adjmat[i][j] == 1) {
					adjmat[j][i] = 1;
				}
			}
		}
		scloscomputed = true;
		if (putback) {
			putback();
		}
	}

	/**
	 * Computes the reflexive, symmetric and transitive closure of the relation
	 * More efficient than computing the three successively Pre: getDom() ==
	 * getRan()
	 */
	public void clos() {
		if (!adjmatComputed) {
			computeAdjMat();
		}
		rclos(false);
		sclos(false);
		tclos(true);
	}

	/**
	 * Puts the adjacency matrix back into the relation Pre: getDom() ==
	 * getRan()
	 */
	private void putback() {
		int max = elementToInt.size();
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max; j++) {
				if (adjmat[i][j] == 1) {
					s.add(new Tuple<T1, T2>(intToElement.get(i),
							(T2) intToElement.get(j)));
				}
			}
		}
	}

	/**
	 * Computes the adjacency matrix Pre: getDom() == getRan()
	 */
	private void computeAdjMat() {
		Set domran = new HashSet();
		domran.addAll(getDom());
		domran.addAll(getRan());
		setDom(domran);
		setRan(domran);

		elementToInt = new HashMap<T1, Integer>();
		intToElement = new HashMap<Integer, T1>();
		int c = 0;
		for (Iterator<T1> i = dom.iterator(); i.hasNext();) {
			T1 element = i.next();
			elementToInt.put(element, c);
			intToElement.put(c, element);
			c++;
		}
		int max = c;

		/*
		 * adjacency matrix, such that: adjmat[i,j] == 1 iff (elementToInt-1(i),
		 * elementToInt-1(j)) in s
		 */
		adjmat = new int[max][max];
		for (Iterator<Tuple<T1, T2>> i = s.iterator(); i.hasNext();) {
			Tuple<T1, T2> t = i.next();
			int from = elementToInt.get(t.e1);
			int to = elementToInt.get((T1) t.e2);
			adjmat[from][to] = 1;
		}

		adjmatComputed = true;
	}

	/**
	 * Returns the equivalence class of an element Pre: getDom() == getRan() and
	 * the relation is an equivalence relation
	 * 
	 * @param e
	 *            the element to compute the equivalence class for
	 * @return the equivalence class
	 */
	public Set<T1> eqClass(T1 e) {
		if (!adjmatComputed) {
			computeAdjMat();
		}

		Set<T1> result = new HashSet<T1>();
		int i = elementToInt.get(e);
		if (i != -1) {
			int max = elementToInt.size();
			for (int j = 0; j < max; j++) {
				if (adjmat[i][j] == 1) {
					result.add(intToElement.get(j));
				}
			}
		}
		return result;
	}

	public Set<T1> reachableFrom(T1 o) {
		if (!(tcloscomputed && rcloscomputed)) {
			rclos(false);
			tclos(false);
		}
		Set<T1> result = new HashSet<T1>();
		int fromInt = elementToInt.get(o);
		for (int j = 0; j < intToElement.size(); j++) {
			if (adjmat[fromInt][j] == 1) {
				result.add(intToElement.get(j));
			}
		}
		return result;
	}

	public boolean path(T1 from, T1 to) {
		if (!(tcloscomputed && rcloscomputed)) {
			rclos(false);
			tclos(false);
		}
		int fromInt = elementToInt.get(from);
		int toInt = elementToInt.get(to);
		return (adjmat[fromInt][toInt] == 1);
	}

	public String printAdjMat() {
		String result = "\t";
		int max = elementToInt.size();

		for (int i = 0; i < max; i++) {
			result += intToElement.get(i) + "\t";
		}
		result += "\n";

		for (int i = 0; i < max; i++) {
			result += intToElement.get(i) + ":\t";
			for (int j = 0; j < max; j++) {
				result += adjmat[i][j] + "\t";
			}
			result += "\n";
		}
		return result;
	}
}
