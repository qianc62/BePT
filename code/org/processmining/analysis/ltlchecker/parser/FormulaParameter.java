package org.processmining.analysis.ltlchecker.parser;

public class FormulaParameter {
	private Attribute param;
	private String defaultValue;
	
	public FormulaParameter(Attribute param) { 
		this.param = param; 
		this.defaultValue = null;
	}
	
	public FormulaParameter(Attribute param, String defaultValue) { 
		this.param = param; 
		this.defaultValue = defaultValue; 
	}
	
	public Attribute getParam() { 
		return param; 
	}
	
	public String getDefaultValue() { 
		return defaultValue; 
	}
	
	public void setDefaultValue(String value) { 
		defaultValue = value; 
	}
	
	public boolean hasDefaultValue() {
		return defaultValue != null;
	}
}

