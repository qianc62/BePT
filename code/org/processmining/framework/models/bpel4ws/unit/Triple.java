package org.processmining.framework.models.bpel4ws.unit;

/**
 * @author Kristian Bisgaard Lassen
 * 
 * @param <First>
 * @param <Second>
 * @param <Third>
 */
public class Triple<First, Second, Third> {

	/***/
	public final First first;

	/***/
	public final Second second;

	/**
     *
     */
	public final Third third;

	/**
	 * @param first
	 * @param second
	 * @param third
	 */
	public Triple(First first, Second second, Third third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + first + "," + second + "," + third + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Triple))
			return false;
		Triple triple = (Triple) o;
		return triple.first.equals(this.first)
				&& triple.second.equals(this.second)
				&& triple.third.equals(this.third);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int hash1 = first.hashCode();
		final int hash2 = second.hashCode() * 31;
		final int hash3 = 1000 * third.hashCode();
		return hash1 + hash2 + hash3;
	}

}
