package org.processmining.analysis.epc.similarity;

import java.util.*;

import org.processmining.framework.util.*;

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
public class ActivityContextFragment {

	public static final int NONE = 0;
	public static final int AND = 1;
	public static final int XOR = 2;
	public static final int OR = 3;

	private String activityName;
	private Vector<String> inputContext;
	private Vector<String> outputContext;
	private int outType;
	private int inType;

	public ActivityContextFragment(String activityName) {
		inputContext = new Vector();
		outputContext = new Vector();
		inType = NONE;
		outType = NONE;
		setActivityName(StringNormalizer.normalize(activityName));
	}

	public ActivityContextFragment(String activityName, String[] inputContext,
			int inType, String[] outputContext, int outType) {
		this.inputContext = new Vector(inputContext.length);
		this.outputContext = new Vector(outputContext.length);
		setActivityName(activityName);
		for (String s : inputContext) {
			addToInputContext(s);
		}
		for (String s : outputContext) {
			addToOutputContext(s);
		}
		setInType(inType);
		setOutType(outType);
	}

	public void setActivityName(String activityName) {
		this.activityName = StringNormalizer.normalize(activityName);
	}

	public void setInType(int inType) {
		this.inType = inType;
	}

	public void setOutType(int outType) {
		this.outType = outType;
	}

	public String getActivityName() {
		return activityName;
	}

	public void addToInputContext(String inputElement) {
		inputContext.add(StringNormalizer.normalize(inputElement));
	}

	public void removeFromInputContext(String inputElement) {
		inputContext.remove(StringNormalizer.normalize(inputElement));
	}

	public Vector<String> getInputContext() {
		return inputContext;
	}

	public void addToOutputContext(String inputElement) {
		outputContext.add(StringNormalizer.normalize(inputElement));
	}

	public void removeFromOutputContext(String inputElement) {
		outputContext.remove(StringNormalizer.normalize(inputElement));
	}

	public Vector<String> getOutputContext() {
		return outputContext;
	}

	public int getInType() {
		return inType;
	}

	public int getOutType() {
		return outType;
	}

	public String toString() {
		return "input:    " + inputContext.toString() + "\n" + "activity: "
				+ activityName + "\n" + "output:   " + outputContext.toString()
				+ "\n";
	}

}
