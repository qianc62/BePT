package org.processmining.framework.models.bpel4ws.unit;

/**
 * @author Kristian Bisgaard Lassen
 * 
 * @param <First>
 * @param <Second>
 * @param <Third>
 * @param <Fourth>
 */
public class Quadruple<First, Second, Third, Fourth> {

	/***/
	public final First first;

	/***/
	public final Second second;

	/**
     *
     */
	public final Third third;

	/**
     *
     */
	public final Fourth fourth;

	/**
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
	 */
	public Quadruple(First first, Second second, Third third, Fourth fourth) {
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + first + "," + second + "," + third + "," + fourth + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Quadruple))
			return false;
		Quadruple quadruple = (Quadruple) o;
		return quadruple.first.equals(this.first)
				&& quadruple.second.equals(this.second)
				&& quadruple.third.equals(this.third)
				&& quadruple.fourth.equals(this.fourth);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int hash1 = first.hashCode();
		final int hash2 = second.hashCode() * 31;
		final int hash3 = 1000 * third.hashCode();
		final int hash4 = 97 * third.hashCode();
		return hash1 + hash2 + hash3 + hash4;
	}

}
