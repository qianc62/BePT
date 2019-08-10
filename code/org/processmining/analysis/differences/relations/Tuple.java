package org.processmining.analysis.differences.relations;

public class Tuple<T1, T2> {

	public T1 e1;
	public T2 e2;

	public Tuple(T1 e1, T2 e2) {
		this.e1 = e1;
		this.e2 = e2;
	}

	public String toString() {
		return "(" + e1.toString() + "," + e2.toString() + ")";
	}

	public boolean equals(Object arg0) {
		if (arg0 instanceof Tuple) {
			return e1.equals(((Tuple) arg0).e1) && e2.equals(((Tuple) arg0).e2);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return e1.hashCode() + e2.hashCode();
	}

}
