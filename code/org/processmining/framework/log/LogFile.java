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

package org.processmining.framework.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JProgressBar;

import org.processmining.framework.log.rfb.MonitorInputStream;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.log.rfb.LogData;
import java.io.FileOutputStream;

/**
 * Encapsulates a log file.
 * <p>
 * Use the getInstance method to obtain an instance.
 * <p>
 * Currently, normal files are supported by giving the file name. Also, files
 * inside zip files are supported by giving the resource string
 * zip://zip_file_name#zip_file_entry_name.
 * 
 * @author Peter van den Brand
 * @version 1.0
 */

abstract public class LogFile {

	/**
	 * If not <code>null</code>, this progress bar will be used to report
	 * progress feedback information while loading the log file.
	 */
	protected JProgressBar progressBar = null;

	protected LogFile() {
	}

	/**
	 * Returns a new InputStream for reading from this LogFile. Note that the
	 * returned InputStream has to be a newly created InputStream that will
	 * start reading at the beginning of the log.
	 * 
	 * @return a new InputStream for reading from this LogFile.
	 * @throws IOException
	 *             in case the file could not be opened
	 */
	abstract public InputStream getInputStream() throws IOException;

	/**
	 * Returns a short descriptive name of the file. This is typically only the
	 * filename without the path information.
	 * 
	 * @return a short descriptive name of the file.
	 */
	abstract public String getShortName();

	/**
	 * Returns the full filename including path info of the log file. Important:
	 * <code>LogFile.getInstance(thisLogFile.toString())</code> should give a
	 * duplicate of the <code>thisLogFile</code> object.
	 * 
	 * @return the full filename including path info of the log file
	 */
	abstract public String toString();

	/**
	 * Sets the progress bar to which feedback while loading the log will be
	 * transmitted. May be voided by providing <code>null</code> (default).
	 * 
	 * @param aProgressBar
	 */
	public void setProgressBar(JProgressBar aProgressBar) {
		progressBar = aProgressBar;
	}

	/**
	 * Create a LogFile instance for the given resource. Currently, normal files
	 * are supported by giving the file name. Also, files inside zip files are
	 * supported by giving the resource string
	 * zip://zip_file_name#zip_file_entry_name.
	 * 
	 * @param resource
	 *            the log file to open
	 * @return a LogFile instance for the given resource
	 */
	public static LogFile getInstance(String resource) {
		if (resource.startsWith("zip://")) {
			return new ZipLogFile(resource);
		} else if (resource.endsWith(GZipLogFile.FILE_EXTENSION)) {
			return new GZipLogFile(resource);
		} else {
			return new FileLogFile(resource);
		}
	}

	/**
	 * This method instantiates a file and a LogFile object that contains an
	 * empty log.
	 * 
	 * @param filename
	 *            String the filename of the file that should be created
	 * @return LogFile the LogFile object referring to a file with an empty log
	 * @throws IOException
	 *             if something goes wrong with the physical file.
	 */
	public static LogFile instantiateEmptyLogFile(String filename)
			throws IOException {
		// Write an empty log file.
		if (filename != null && !filename.equals("")) {
			File tmpFile = new File(filename);

			FileOutputStream stream = new FileOutputStream(tmpFile);
			stream
					.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
							.getBytes());
			stream
					.write("<WorkflowLog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://is.tm.tue.nl/research/processmining/WorkflowLog.xsd\">\n"
							.getBytes());
			stream.write("<Process id=\"new_process\">\n".getBytes());
			stream.write("<Data>\n".getBytes());
			stream
					.write(("<Attribute name=\""
							+ LogData.RETAIN_PROCESS_IF_EMPTY + "\">true</Attribute>\n")
							.getBytes());
			stream.write("</Data>\n".getBytes());
			stream.write("</Process>\n".getBytes());
			stream.write("</WorkflowLog>\n".getBytes());
			stream.flush();
			stream.close();

			return LogFile.getInstance(filename);
		}
		return null;

	}

}

class FileLogFile extends LogFile {

	private String filename;

	protected FileLogFile(String filename) {
		this.filename = filename;
	}

	public InputStream getInputStream() throws IOException {
		return new MonitorInputStream(new FileInputStream(filename), (new File(
				filename)).length(), progressBar);
	}

	public String getShortName() {
		return (new File(filename)).getName();
	}

	public String toString() {
		return filename;
	}
}

class ZipLogFile extends LogFile {

	private String filename;
	private String entry;

	protected ZipLogFile(String filename) {
		filename = filename.substring("zip://".length());
		this.filename = filename.substring(0, filename.indexOf('#'));
		this.entry = filename.substring(filename.indexOf('#') + 1);
	}

	public InputStream getInputStream() throws IOException {
		if (filename.startsWith(UISettings.getInstance()
				.getExecutionLogFileName())) {
			throw new IOException("Cannot open my own execution log file:"
					+ filename);
		}

		ZipFile zip = new ZipFile(filename);
		ZipEntry zipEntry = zip.getEntry(entry);

		if (zipEntry == null) {
			throw new IOException("Could not open zip file: " + entry
					+ " in the file " + filename);
		} else {
			return new MonitorInputStream(zip.getInputStream(zipEntry),
					zipEntry.getSize(), progressBar);
		}
	}

	public String getShortName() {
		return entry;
	}

	public String toString() {
		return "zip://" + filename + "#" + entry;
	}
}

/**
 * Implements a log file wrapper for single, GZIP-compressed MXML log files.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
class GZipLogFile extends LogFile {

	public static final String FILE_EXTENSION = ".gz";
	protected String fileName = null;

	protected GZipLogFile(String aFileName) {
		fileName = aFileName;
	}

	public InputStream getInputStream() throws IOException {
		return new GZIPInputStream(new MonitorInputStream(new FileInputStream(
				fileName), (new File(fileName)).length(), progressBar));
	}

	public String getShortName() {
		return (new File(fileName)).getName();
	}

	public String toString() {
		return fileName;
	}
}
