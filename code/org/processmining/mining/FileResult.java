/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.mining;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.Message;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class FileResult extends Writer implements MiningResult {

	private StringBuffer buffer = new StringBuffer();
	private String extension;

	public FileResult(String extension) {
		this.extension = extension;
	}

	public LogReader getLogReader() {
		return null;
	}

	public JComponent getVisualization() {
		return null;
	}

	public String getExtension() {
		return extension;
	}

	public void saveResult(OutputStream out) {
		try {
			out.write(buffer.toString().getBytes());
		} catch (IOException ex) {
			Message.add(
					"Could not write results to output: " + ex.getMessage(),
					Message.ERROR);
		}
	}

	public void close() {
	}

	public void flush() {
	}

	public void write(char[] cbuf) {
		buffer.append(cbuf);
	}

	public void write(char[] cbuf, int off, int len) {
		buffer.append(cbuf, off, len);
	}

	public void write(int c) {
		buffer.append(c);
	}

	public void write(String str) {
		buffer.append(str);
	}

	public void write(String str, int off, int len) {
		buffer.append(str.substring(off, off + len));
	}

	public void writeln(String str) {
		buffer.append(str + "\n");
	}
}
