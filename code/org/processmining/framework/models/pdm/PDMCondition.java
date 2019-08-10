package org.processmining.framework.models.pdm;

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
public class PDMCondition {

	private PDMDataElement data;
	private String value;
	private String id;

	public PDMCondition() {
	}

	public PDMCondition(String id, PDMDataElement data, String value) {
		this.id = id;
		this.data = data;
		this.value = value;
	}

	public PDMDataElement getDataElement() {
		return data;
	}

	public String getValue() {
		return value;
	}

	public String writeCondition() {
		String result = new String();
		result = data.getID() + " = " + value;
		return result;
	}

	public String getID() {
		return id;
	}

}
