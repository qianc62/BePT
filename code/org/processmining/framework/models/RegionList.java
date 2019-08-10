package org.processmining.framework.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.processmining.framework.log.LogEvent;

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
public class RegionList extends ArrayList<Region> {

	public void addAndRemoveLarger(Region r) {
		removeLarger(r);
		add(r);
	}

	public void removeComplements() {
		for (int i = 0; i < size(); i++) {
			Region r = get(i);
			removeComplement(r, i);
		}
	}

	public boolean removeComplement(Region r) {
		Iterator<Region> it = iterator();
		while (it.hasNext()) {
			Region r2 = it.next();
			if (r.getInput().equals(r2.getInput())
					&& r.getOutput().equals(r2.getOutput())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	private boolean removeComplement(Region r, int i) {
		for (int j = i + 1; j < size(); j++) {
			Region r2 = get(j);
			if (r.getInput().equals(r2.getInput())
					&& r.getOutput().equals(r2.getOutput())) {
				remove(j);
				return true;
			}
		}
		return false;
	}

	public void retainMinimal() {
		for (int i = 0; i < size(); i++) {
			Region r = get(i);
			if (r.getInput().isEmpty() || r.getOutput().isEmpty()) {
				if (removeSimilar(r)) {
					i = 0;
				}
			} else if (removeLarger(r)) {
				i = 0;
			}
		}
	}

	private boolean removeLarger(Region r) {
		boolean result = false;
		Iterator<Region> it = iterator();
		while (it.hasNext()) {
			Region reg = it.next();
			if ((reg != r) && (!r.isEmpty()) && (reg.containsAll(r))) {
				it.remove();
				result = true;
			}
		}
		return result;
	}

	private boolean removeSimilar(Region r) {
		boolean result = false;
		Iterator<Region> it = iterator();
		while (it.hasNext()) {
			Region reg = it.next();
			if ((reg != r) && reg.equals(r)) {
				it.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * getPreRegions
	 * 
	 * @param obj
	 *            LogEvent
	 * @return HashSet
	 */
	public ArrayList getPreRegions(LogEvent obj) {
		ArrayList result = new ArrayList();
		Iterator<Region> it = this.iterator();
		while (it.hasNext()) {
			Region r = it.next();
			if (r.getOutput().contains(obj)) {
				result.add(r);
			}
		}
		return result;
	}

	/**
	 * getPreRegions
	 * 
	 * @param obj
	 *            LogEvent
	 * @return HashSet
	 */
	public ArrayList getPostRegions(LogEvent obj) {
		ArrayList result = new ArrayList();
		Iterator<Region> it = this.iterator();
		while (it.hasNext()) {
			Region r = it.next();
			if (r.getInput().contains(obj)) {
				result.add(r);
			}
		}
		return result;
	}

	/**
	 * removeEmpty
	 */
	public void removeEmpty() {
		Iterator<Region> it = this.iterator();
		while (it.hasNext()) {
			Region r = it.next();
			if (r.getInput().isEmpty() && r.getOutput().isEmpty()) {
				it.remove();
			}
		}
	}

	public void removeRegionsWithEmptyInput(Collection objects) {
		Iterator<Region> it = this.iterator();
		while (it.hasNext()) {
			Region r = it.next();
			if (r.getInput().isEmpty() && !objects.containsAll(r.getOutput())) {
				it.remove();
			}
		}
	}

	public void removeRegionsWithEmptyOutput(Collection objects) {
		Iterator<Region> it = this.iterator();
		while (it.hasNext()) {
			Region r = it.next();
			if (r.getOutput().isEmpty() && !objects.containsAll(r.getInput())) {
				it.remove();
			}
		}
	}

}
