package org.processmining.framework.models.transitionsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiSet<E> extends ArrayList<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<E, Integer> dataMap;

	public MultiSet() {
		super();
		dataMap = new HashMap<E, Integer>();
	}

	public MultiSet(Collection<E> docs) {
		// super(docs);
		dataMap = new HashMap<E, Integer>();
		Iterator<E> it = docs.iterator();
		while (it.hasNext())
			this.add(it.next());
	}

	public boolean add(E o) {
		super.add(o);
		if (!dataMap.containsKey(o))
			dataMap.put(o, new Integer(1));
		else {
			Integer newValue = new Integer(dataMap.get(o).intValue() + 1);
			dataMap.put(o, newValue);
		}
		return true;
	}

	public boolean addAll(Collection<? extends E> o) {
		Iterator<? extends E> it = o.iterator();
		while (it.hasNext())
			this.add(it.next());
		return true;
	}

	public boolean remove(Object key) {
		if (!contains(key)) {
			return false;
		}
		int number = dataMap.get(key).intValue() - 1;
		if (number > 0) {
			dataMap.put((E) key, new Integer(number));
			this.remove(this.lastIndexOf(key));
		} else
			dataMap.remove(key);
		return true;
	}

	public Map<E, Integer> getDataMap() {
		return dataMap;
	}

	public boolean equals(Object o) {
		return dataMap.equals(((MultiSet) o).getDataMap());
	}
}
