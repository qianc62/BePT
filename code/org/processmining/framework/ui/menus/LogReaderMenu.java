/**
 * Project: ProM HPLR
 * File: LogReaderMenu.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 11, 2006, 3:04:10 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.classic.LogReaderClassic;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.log.rfb.fsio.FS2VirtualFileSystem;
import org.processmining.framework.log.rfb.io.CachedRandomAccessFileProvider;
import org.processmining.framework.log.rfb.io.StorageProvider;
import org.processmining.framework.log.rfb.io.VirtualFileSystem;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

/**
 * Implementation of a menu for selecting and switching the currently used
 * LogReader (and backend) implementation in a user-friendly manner.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class LogReaderMenu extends JMenu implements ActionListener {

	/**
	 * not used at the moment; implement properly if you intend to!
	 */
	private static final long serialVersionUID = -5773480325235293142L;

	public static final String ACTION_CLASSIC = "LOGREADER_CLASSIC";
	public static final String ACTION_BUFFERED_CACHEDFILES = "LOGREADER_BUFFERED_CACHEDFILES";
	public static final String ACTION_BUFFERED_NIKEFS = "LOGREADER_BUFFERED_NIKEFS";
	public static final String ACTION_BUFFERED_NIKEFS2_AGGRESSIVE = "LOGREADER_BUFFERED_NIKEFS2_AGGRESSIVE";
	public static final String ACTION_BUFFERED_NIKEFS2_CONSERVATIVE = "LOGREADER_BUFFERED_NIKEFS2_CONSERVATIVE";

	protected JRadioButtonMenuItem classicReader = null;
	protected JRadioButtonMenuItem rfbReader = null;
	protected JRadioButtonMenuItem vfsReader = null;
	protected JRadioButtonMenuItem nfs2AggressiveReader = null;
	protected JRadioButtonMenuItem nfs2ConservativeReader = null;

	public static StorageProvider storageProvider = new CachedRandomAccessFileProvider();

	protected boolean showWarning = true;

	/**
	 * Creates a new log reader menu instance.
	 */
	public LogReaderMenu() {
		super("Log reader implementation");
		ButtonGroup bGroup = new ButtonGroup();
		nfs2AggressiveReader = new JRadioButtonMenuItem(
				"NikeFS2 Aggressive Optimization", false);
		nfs2AggressiveReader
				.setToolTipText("Aggressively optimized virtual file system fixed-size block allocation; true random access; read-write persistency");
		nfs2AggressiveReader
				.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE);
		nfs2AggressiveReader.addActionListener(this);
		bGroup.add(nfs2AggressiveReader);
		this.add(nfs2AggressiveReader);
		nfs2ConservativeReader = new JRadioButtonMenuItem(
				"NikeFS2 Conservative Optimization", false);
		nfs2ConservativeReader
				.setToolTipText("Conservatively optimized virtual file system fixed-size block allocation; true random access; read-write persistency");
		nfs2ConservativeReader
				.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_CONSERVATIVE);
		nfs2ConservativeReader.addActionListener(this);
		bGroup.add(nfs2ConservativeReader);
		this.add(nfs2ConservativeReader);
		vfsReader = new JRadioButtonMenuItem("Buffered log reader (NikeFS)",
				false);
		vfsReader
				.setToolTipText("Virtual file system block allocation; true random access; read-write persistency");
		vfsReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS);
		vfsReader.addActionListener(this);
		bGroup.add(vfsReader);
		this.add(vfsReader);
		rfbReader = new JRadioButtonMenuItem(
				"Buffered log reader (dedicated files)", false);
		rfbReader
				.setToolTipText("OS-level file allocation; true random access; read-write persistency");
		rfbReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_CACHEDFILES);
		rfbReader.addActionListener(this);
		bGroup.add(rfbReader);
		this.add(rfbReader);
		classicReader = new JRadioButtonMenuItem(
				"Classic log reader (heap-based)", true);
		classicReader
				.setToolTipText("Heap space allocation; no random access; read-only");
		classicReader.setActionCommand(LogReaderMenu.ACTION_CLASSIC);
		classicReader.addActionListener(this);
		bGroup.add(classicReader);
		this.add(classicReader);
		restore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		try {
			if (command.equals(LogReaderMenu.ACTION_CLASSIC)) {
				LogReaderFactory.setLogReaderClass(LogReaderClassic.class);
				Message.add("Switching log reader implementation to classic.",
						Message.NORMAL);
				UISettings.getInstance().setPreferredLogReader(command);
				showWarning();
			} else if (command
					.equals(LogReaderMenu.ACTION_BUFFERED_CACHEDFILES)) {
				LogReaderFactory.setLogReaderClass(BufferedLogReader.class);
				LogReaderMenu.storageProvider = new CachedRandomAccessFileProvider();
				Message
						.add(
								"Switching log reader implementation to file-buffered (true random access).",
								Message.NORMAL);
				UISettings.getInstance().setPreferredLogReader(command);
				showWarning();
			} else if (command.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS)) {
				LogReaderFactory.setLogReaderClass(BufferedLogReader.class);
				LogReaderMenu.storageProvider = VirtualFileSystem.instance();
				Message
						.add(
								"Switching log reader implementation to virtual file system-buffered (true random access).",
								Message.NORMAL);
				UISettings.getInstance().setPreferredLogReader(command);
				showWarning();
			} else if (command
					.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE)) {
				LogReaderFactory.setLogReaderClass(BufferedLogReader.class);
				LogReaderMenu.storageProvider = FS2VirtualFileSystem.instance();
				FS2VirtualFileSystem.instance().setUseLazyCopies(true);
				Message
						.add(
								"Switching log reader implementation to NIKEFS2, aggressive optimization.",
								Message.NORMAL);
				UISettings.getInstance().setPreferredLogReader(command);
				showWarning();
			} else if (command
					.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_CONSERVATIVE)) {
				LogReaderFactory.setLogReaderClass(BufferedLogReader.class);
				LogReaderMenu.storageProvider = FS2VirtualFileSystem.instance();
				FS2VirtualFileSystem.instance().setUseLazyCopies(false);
				Message
						.add(
								"Switching log reader implementation to NIKEFS2, conservative optimization.",
								Message.NORMAL);
				UISettings.getInstance().setPreferredLogReader(command);
				showWarning();
			}
		} catch (Exception ex) {
			Message
					.add(
							"Encountered a problem when trying to switch log reader"
									+ " implementation. Please check STDOUT for stack trace!",
							Message.ERROR);
			ex.printStackTrace();
		}
	}

	protected void showWarning() {
		if (showWarning == true) {
			JOptionPane
					.showMessageDialog(
							null,
							"You have switched the log reader implementation.\n\n"
									+ "Make sure to close all open windows in the framework\n"
									+ "now (i.e., Logs, Mining settings and results, etc.)!\n\n"
									+ "ProM will remember the log reader you selected, and\n"
									+ "restore your settings on the next startup.",
							"Log reader implementation switched!",
							JOptionPane.WARNING_MESSAGE);
		}
	}

	protected void restore() {
		showWarning = false;
		String prefReader = UISettings.getInstance().getPreferredLogReader();
		if (prefReader.equals(LogReaderMenu.ACTION_CLASSIC)) {
			classicReader.setSelected(true);
			actionPerformed(new ActionEvent(this, 0,
					LogReaderMenu.ACTION_CLASSIC));
		} else if (prefReader.equals(LogReaderMenu.ACTION_BUFFERED_CACHEDFILES)) {
			rfbReader.setSelected(true);
			actionPerformed(new ActionEvent(this, 0,
					LogReaderMenu.ACTION_BUFFERED_CACHEDFILES));
		} else if (prefReader.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS)) {
			vfsReader.setSelected(true);
			actionPerformed(new ActionEvent(this, 0,
					LogReaderMenu.ACTION_BUFFERED_NIKEFS));
		} else if (prefReader
				.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE)) {
			nfs2AggressiveReader.setSelected(true);
			actionPerformed(new ActionEvent(this, 0,
					LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE));
		} else if (prefReader
				.equals(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_CONSERVATIVE)) {
			nfs2ConservativeReader.setSelected(true);
			actionPerformed(new ActionEvent(this, 0,
					LogReaderMenu.ACTION_BUFFERED_NIKEFS2_CONSERVATIVE));
		} else {
			Message
					.add(
							"Could not restore preferred log reader - identification unknown!",
							Message.WARNING);
		}
		showWarning = true;
	}

}
