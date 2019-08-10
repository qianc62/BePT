package org.processmining.framework.models.bpel.util;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
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

	private int hashCode;

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
		final int PRIME = 31;
		hashCode = 1;
		hashCode = PRIME * hashCode + ((first == null) ? 0 : first.hashCode());
		hashCode = PRIME * hashCode
				+ ((fourth == null) ? 0 : fourth.hashCode());
		hashCode = PRIME * hashCode
				+ ((second == null) ? 0 : second.hashCode());
		hashCode = PRIME * hashCode + ((third == null) ? 0 : third.hashCode());
	}

	public static <First, Second, Third, Fourth> Quadruple<First, Second, Third, Fourth> create(
			First first, Second second, Third third, Fourth fourth) {
		return new Quadruple<First, Second, Third, Fourth>(first, second,
				third, fourth);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + first + "," + second + "," + third + "," + fourth + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Quadruple other = (Quadruple) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}

}
