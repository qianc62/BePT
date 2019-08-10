package org.processmining.framework.models.bpel4ws.unit;

/**
 * @author Kristian Bisgaard Lassen
 * 
 * @param <First>
 * @param <Second>
 */
public class Pair<First, Second> {
	/***/
	public final First first;

	/***/
	public final Second second;

	/**
	 * @param first
	 * @param second
	 */
	public Pair(First first, Second second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + first + "," + second + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Pair))
			return false;
		Pair pair = (Pair) o;
		return pair.first.equals(this.first) && pair.second.equals(this.second);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int hash1 = first.hashCode();
		final int hash2 = second.hashCode() * 31;
		return hash1 + hash2;
	}

}
