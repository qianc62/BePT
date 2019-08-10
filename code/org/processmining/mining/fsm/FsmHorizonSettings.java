package org.processmining.mining.fsm;

import java.util.HashSet;
import java.util.Collection;

/**
 * <p>
 * Title: FsmHorizonSettings
 * </p>
 * 
 * <p>
 * Description: Settings for a single horizon
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public class FsmHorizonSettings {

	// Supported abstractions
	public static final int SEQ = 1; // Sequence
	public static final int BAG = 2; // Bag or multiset
	public static final int SET = 3; // Set

	// Whether these settings have been selected for use or not.
	private boolean use;
	// Maximal number of steps to take. If -1, take all steps.
	private int horizon;
	// Abstraction, see above.
	private int abstraction;
	// Filter.
	private HashSet<String> filter;
	private String[] filterArray;
	// Maximal number of filtered steps to take.
	private int filteredHorizon;

	/**
	 * Default settings.
	 */
	public FsmHorizonSettings() {
		this.use = false;
		this.horizon = -1;
		this.abstraction = SET;
		this.filter = new HashSet<String>();
		this.filterArray = new String[1];
		this.filteredHorizon = 1;
	}

	/**
	 * Gets the label for the givenabstraction.
	 * 
	 * @param mode
	 *            int abstraction.
	 * @return String label.
	 */
	public String getLabel(int mode) {
		String s = "";
		if (mode == SEQ) {
			s = "Sequence";
		} else if (mode == BAG) {
			s = "Bag";
		} else if (mode == SET) {
			s = "Set";
		}
		return s;
	}

	/**
	 * Returns whether these settings are selected for use.
	 * 
	 * @return boolean
	 */
	public boolean getUse() {
		return use;
	}

	/**
	 * Sets whether these settings should be selected for use.
	 * 
	 * @param newUse
	 *            boolean new value.
	 * @return boolean old value.
	 */
	public boolean setUse(boolean newUse) {
		boolean oldUse = use;
		use = newUse;
		return oldUse;
	}

	/**
	 * Returns the number of steps to take.
	 * 
	 * @return int
	 */
	public int getHorizon() {
		return horizon;
	}

	/**
	 * Sets the number of steps to take.
	 * 
	 * @param newHorizon
	 *            int new value.
	 * @return int old value.
	 */
	public int setHorizon(int newHorizon) {
		int oldHorizon = horizon;
		horizon = newHorizon;
		return oldHorizon;
	}

	/**
	 * Returns the abstraction.
	 * 
	 * @return int
	 */
	public int getAbstraction() {
		return abstraction;
	}

	/**
	 * Sets the abstraction.
	 * 
	 * @param newAbstraction
	 *            int new value.
	 * @return int old value.
	 */
	public int setAbstraction(int newAbstraction) {
		int oldAbstraction = abstraction;
		abstraction = newAbstraction;
		return oldAbstraction;
	}

	/**
	 * Returns the filter.
	 * 
	 * @return Collection
	 */
	public Collection<String> getFilter() {
		return filter;
	}

	public String[] getFilterArray() {
		return filterArray;
	}

	/**
	 * Adds all elements from the given array to the filter.
	 * 
	 * @param objects
	 *            Object[]
	 */
	public void addAllToFilter(Object[] objects) {
		filterArray = new String[objects.length];
		for (int i = 0; i < objects.length; i++) {
			filterArray[i] = objects[i].toString();
		}
		for (Object object : objects) {
			filter.add(object.toString());
		}
	}

	/**
	 * Clears the filter.
	 */
	public void clearFilter() {
		filter.clear();
	}

	/**
	 * Gets the number of filtered steps to take.
	 * 
	 * @return int
	 */
	public int getFilteredHorizon() {
		return filteredHorizon;
	}

	/**
	 * Sets the number of filtered steps to take.
	 * 
	 * @param newFilteredHorizon
	 *            int new value.
	 * @return int old value.
	 */
	public int setFilteredHorizon(int newFilteredHorizon) {
		int oldFilteredHorizon = filteredHorizon;
		filteredHorizon = newFilteredHorizon;
		return oldFilteredHorizon;
	}
}
