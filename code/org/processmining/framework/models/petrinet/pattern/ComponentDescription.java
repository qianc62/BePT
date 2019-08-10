package org.processmining.framework.models.petrinet.pattern;

public class ComponentDescription {

	private final boolean isPredefined;

	private String name;

	private Double cost;

	public ComponentDescription(final boolean isPredefined, final String name,
			final Double cost) {
		super();
		this.isPredefined = isPredefined;
		this.name = name;
		this.cost = cost;
	}

	/**
	 * @return the cost
	 */
	public Double getCost() {
		return cost;
	}

	/**
	 * @return the isPredefined
	 */
	public boolean isPredefined() {
		return isPredefined;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(Double cost) {
		this.cost = cost;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = null;
		if (isPredefined)
			result = "Predefined: ";
		else
			result = "User-defined: ";
		result += name;
		if (cost != null)
			result += " (" + cost.toString() + ")";
		return result;
	}

}
