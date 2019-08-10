package org.processmining.framework.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class OutputStreamWithFilename extends FileOutputStream {

	private String filename;

	public OutputStreamWithFilename(String name) throws FileNotFoundException {
		super(name);
		this.filename = name;
	}

	public String getFilename() {
		return filename;
	}
}
