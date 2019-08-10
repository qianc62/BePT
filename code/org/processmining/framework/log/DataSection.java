package org.processmining.framework.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataSection implements Map<String, String> {

	private static final long serialVersionUID = 8597757086649376814L;
	private static final List<String> EMPTY_LIST = Collections
			.unmodifiableList(new ArrayList<String>(0));

	private Map<String, DataAttribute> attributes = new HashMap<String, DataAttribute>();

	public DataSection() {
		super();
	}

	public DataSection(DataSection data) {
		attributes = new HashMap<String, DataAttribute>(data.attributes);
	}

	public void clear() {
		attributes.clear();
	}

	public void put(DataAttribute attr) {
		attributes.put(attr.getName(), attr);
	}

	public List<String> getModelReferences(String key) {
		DataAttribute attr = attributes.get(key);
		return attr == null ? EMPTY_LIST : attr.getModelReferences();
	}

	public String remove(Object key) {
		DataAttribute attribute = attributes.remove(key);
		return attribute.getValue();
	}

	public Object clone() {
		return new DataSection(this);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("DataSection <<<\n");
		for (String key : keySet()) {
			result.append("  " + key + ":" + get(key) + ", "
					+ attributes.get(key) + "\n");
		}
		result.append(">>>\n");
		return result.toString();
	}

	public static DataSection fromMap(Map<String, String> map) {
		DataSection data = new DataSection();

		for (Map.Entry<String, String> item : map.entrySet()) {
			data.put(item.getKey(), item.getValue());
		}
		return data;
	}

	public void setModelReferences(String key, List<String> modelRefs) {
		DataAttribute attr = attributes.get(key);
		if (attr != null) {
			attr.setModelReferences(modelRefs);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return attributes.containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		for (DataAttribute attr : attributes.values()) {
			if (attr.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		HashMap<String, String> interimMap = new HashMap<String, String>();
		for (String key : attributes.keySet()) {
			interimMap.put(key, attributes.get(key).getValue());
		}
		return interimMap.entrySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public String get(Object key) {
		DataAttribute attr = attributes.get(key);
		if (attr != null) {
			return attr.getValue();
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		return attributes.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public String put(String key, String value) {
		if (attributes.containsKey(key)) {
			String rem = attributes.get(key).getValue();
			attributes.get(key).setValue(value);
			return rem;
		} else {
			DataAttribute attr = new DataAttribute(key, value);
			attributes.put(key, attr);
			return value;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends String> map) {
		for (String key : map.keySet()) {
			put(key, map.get(key));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return attributes.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection<String> values() {
		List<String> values = new ArrayList<String>();
		for (String key : attributes.keySet()) {
			values.add(attributes.get(key).getValue());
		}
		return values;
	}
}
