package org.processmining.framework.models.bpel.util;

public class Quintuple<A, B, C, D, E> {

	public static <A, B, C, D, E> Quintuple<A, B, C, D, E> create(A a, B b,
			C c, D d, E e) {
		return new Quintuple<A, B, C, D, E>(a, b, c, d, e);
	}

	public final A first;

	public final B second;

	public final C third;

	public final D fourth;

	public final E fifth;

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((first == null) ? 0 : first.hashCode());
		result = PRIME * result + ((second == null) ? 0 : second.hashCode());
		result = PRIME * result + ((third == null) ? 0 : third.hashCode());
		result = PRIME * result + ((fourth == null) ? 0 : fourth.hashCode());
		result = PRIME * result + ((fifth == null) ? 0 : fifth.hashCode());
		return result;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Quintuple other = (Quintuple) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
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
		if (fourth == null) {
			if (other.fourth != null)
				return false;
		} else if (!fourth.equals(other.fourth))
			return false;
		if (fifth == null) {
			if (other.fifth != null)
				return false;
		} else if (!fifth.equals(other.fifth))
			return false;
		return true;
	}

	private Quintuple(final A a, final B b, final C c, final D d, final E e) {
		super();
		this.first = a;
		this.second = b;
		this.third = c;
		this.fourth = d;
		this.fifth = e;
	}

	@Override
	public String toString() {
		return "(" + first.toString() + "," + second.toString() + ","
				+ third.toString() + "," + fourth.toString() + ","
				+ fifth.toString() + ")";
	}

}
