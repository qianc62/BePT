package org.processmining.framework.models;

import java.util.Collection;
import java.util.HashSet;

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
public class Region extends HashSet {

	private HashSet input = new HashSet();
	private HashSet output = new HashSet();

	public Region(Region r1, Region r2) {
		super(r1.size() + r2.size());
		addAll(r1);
		addAll(r2);
		input.addAll(r1.getInput());
		input.addAll(r2.getInput());
		output.addAll(r1.getOutput());
		output.addAll(r2.getOutput());
	}

	public Region(HashSet input, HashSet output) {
		super();
		this.input.addAll(input);
		this.output.addAll(output);
	}

	public Region(HashSet input, HashSet output, int size) {
		super(size);
		this.input.addAll(input);
		this.output.addAll(output);
	}

	public Region(HashSet input, HashSet output, Collection c) {
		super(c);
		this.input.addAll(input);
		this.output.addAll(output);
	}

	public HashSet getInput() {
		return input;
	}

	public HashSet getOutput() {
		return output;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || !(o instanceof Region)) {
			return false;
		}
		Region r = (Region) o;
		return (getInput().equals(r.getInput()) ? getOutput().equals(
				r.getOutput()) : false);
	}

}
