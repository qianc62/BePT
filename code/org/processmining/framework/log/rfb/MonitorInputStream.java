/**
 * 
 */
package org.processmining.framework.log.rfb;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JProgressBar;

/**
 * This class proxies for an InputStream, in knowledge of its source's total
 * size. Equipped with a progress bar, it notifies it to reflect the progress of
 * reading data from the proxied stream.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class MonitorInputStream extends InputStream {

	protected InputStream parent;
	protected JProgressBar progressBar = null;
	protected long size;
	protected long pointer;
	protected long stepSize;
	protected long nextStep;

	/**
	 * Creates a new monitor input stream
	 */
	public MonitorInputStream(InputStream aParent, long aSize,
			JProgressBar aProgressBar) {
		parent = aParent;
		size = aSize;
		pointer = 0;
		stepSize = size / 1000;
		if (stepSize <= 0) {
			stepSize = 1;
		}
		nextStep = stepSize;
		progressBar = aProgressBar;
		if (progressBar != null) {
			progressBar.setMinimum(0);
			progressBar.setMaximum(1000);
		}
	}

	/**
	 * Adjusts the number of read bytes
	 * 
	 * @param bytes
	 */
	protected void incrementRead(long bytes) {
		pointer += bytes;
		if (pointer >= nextStep) {
			// System.out.println("read " + pointer + " / " + size + " bytes.");
			nextStep += stepSize;
			if (progressBar != null) {
				progressBar.setValue((int) (pointer / stepSize));
				if (pointer >= size) {
					progressBar.setIndeterminate(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		incrementRead(1);
		return parent.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return parent.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		parent.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int arg0) {
		parent.mark(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return parent.markSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		int read = parent.read(arg0, arg1, arg2);
		incrementRead(read);
		return read;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws IOException {
		int read = parent.read(arg0);
		incrementRead(read);
		return read;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		parent.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long arg0) throws IOException {
		long read = parent.skip(arg0);
		incrementRead(read);
		return read;
	}

}
