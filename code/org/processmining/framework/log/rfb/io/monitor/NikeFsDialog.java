/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 15, 2006 3:32:58 AM
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
package org.processmining.framework.log.rfb.io.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.log.rfb.io.VirtualFileSystem;
import org.processmining.framework.util.RuntimeUtils;

/**
 * Dialog for monitoring the application, most specifically the NikeFS log
 * reader / virtual file system subsystem.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class NikeFsDialog extends JDialog {

	public static Color dialogBgColor = new Color(80, 80, 70);

	protected BlockDataStorageInfo info[];
	protected JScrollPane scrollPane;
	protected JPanel swapFilePanel;
	protected NikeFsDialogUpdateThread updateThread;

	protected JLabel labelSwapMemory;
	protected JLabel labelHeapMemory;
	protected JComboBox updateComboBox;
	protected JComboBox displayComboBox;

	protected int swapPanelSize;
	protected int updateDelay;

	protected NumberFormat numberFormat;

	/**
	 * @param arg0
	 * @throws HeadlessException
	 */
	public NikeFsDialog(Frame owner) throws HeadlessException {
		super(owner, "NikeFS Monitoring", false);
		this.setSize(800, 400);
		this.setLayout(new BorderLayout());
		JPanel upperPanel = new JPanel();
		upperPanel.setBackground(new Color(150, 150, 140));
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.X_AXIS));
		JPanel imLabelPanel = new JPanel();
		imLabelPanel.setLayout(new BoxLayout(imLabelPanel, BoxLayout.Y_AXIS));
		imLabelPanel.setBackground(new Color(150, 150, 140));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
		labelPanel.setBackground(new Color(150, 150, 140));
		Font font = getFont().deriveFont(12.0f);
		JLabel imLabelSwap = new JLabel("NikeFS Swap memory usage:");
		imLabelSwap.setFont(font);
		imLabelSwap.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		imLabelSwap.setAlignmentX(RIGHT_ALIGNMENT);
		JLabel imLabelMemory = new JLabel("System memory usage:");
		imLabelMemory.setFont(font);
		imLabelMemory.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		imLabelMemory.setAlignmentX(RIGHT_ALIGNMENT);
		JLabel imLabelProcessors = new JLabel("Available processors:");
		imLabelProcessors.setFont(font);
		imLabelProcessors.setBorder(BorderFactory.createEmptyBorder(4, 10, 4,
				10));
		imLabelProcessors.setAlignmentX(RIGHT_ALIGNMENT);
		labelSwapMemory = new JLabel("retrieving information...");
		labelSwapMemory.setFont(font);
		labelSwapMemory
				.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		labelHeapMemory = new JLabel("retrieving information...");
		labelHeapMemory.setFont(font);
		labelHeapMemory
				.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		JLabel labelProcessors = new JLabel(Integer.toString(Runtime
				.getRuntime().availableProcessors()));
		labelProcessors.setFont(font);
		labelProcessors
				.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		String updateItems[] = { "2", "3", "5", "10", "30" };
		updateComboBox = new JComboBox(updateItems);
		updateComboBox.setActionCommand("UPDATE_FREQUENCY");
		updateComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("UPDATE_FREQUENCY")) {
					updateDelay = Integer.parseInt((String) updateComboBox
							.getSelectedItem()) * 1000;
					updateThread.interrupt();
				}
			}
		});
		String displaySizeItems[] = { "128", "256", "512" };
		displayComboBox = new JComboBox(displaySizeItems);
		displayComboBox.setActionCommand("UPDATE_DISPLAYSIZE");
		displayComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("UPDATE_DISPLAYSIZE")) {
					swapPanelSize = Integer.parseInt((String) displayComboBox
							.getSelectedItem());
					updateThread.interrupt();
				}
			}
		});
		if (RuntimeUtils.isRunningMacOsX()) {
			updateComboBox.setBackground(new Color(150, 150, 140));
			displayComboBox.setBackground(new Color(150, 150, 140));
		}
		JLabel imLabelUpdate = new JLabel("Update interval (in seconds):");
		imLabelUpdate.setFont(font);
		JLabel imLabelDisplaySize = new JLabel("Display size of swap panels:");
		imLabelDisplaySize.setFont(font);
		JPanel settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
		settingsPanel.setBackground(new Color(150, 150, 140));
		settingsPanel
				.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
		settingsPanel.add(imLabelUpdate);
		settingsPanel.add(this.updateComboBox);
		settingsPanel.add(Box.createHorizontalStrut(20));
		settingsPanel.add(imLabelDisplaySize);
		settingsPanel.add(this.displayComboBox);
		settingsPanel.add(Box.createHorizontalGlue());
		this.add(settingsPanel, BorderLayout.SOUTH);
		upperPanel.add(Box.createHorizontalStrut(20));
		imLabelPanel.add(Box.createVerticalStrut(10));
		imLabelPanel.add(imLabelProcessors);
		imLabelPanel.add(imLabelMemory);
		imLabelPanel.add(imLabelSwap);
		imLabelPanel.add(Box.createVerticalStrut(10));
		upperPanel.add(imLabelPanel);
		upperPanel.add(Box.createHorizontalStrut(10));
		labelPanel.add(Box.createVerticalStrut(10));
		labelPanel.add(labelProcessors);
		labelPanel.add(labelHeapMemory);
		labelPanel.add(labelSwapMemory);
		labelPanel.add(Box.createVerticalStrut(10));
		upperPanel.add(labelPanel);
		upperPanel.add(Box.createHorizontalGlue());
		this.add(upperPanel, BorderLayout.NORTH);
		swapPanelSize = 128;
		updateDelay = 3000;
		swapFilePanel = new JPanel();
		swapFilePanel.setBackground(dialogBgColor);
		swapFilePanel.setLayout(new BoxLayout(swapFilePanel, BoxLayout.X_AXIS));
		swapFilePanel
				.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		scrollPane = new JScrollPane(swapFilePanel);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane, BorderLayout.CENTER);
		numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setMinimumFractionDigits(2);
		updateThread = new NikeFsDialogUpdateThread(this);
		updateThread.start();
		setVisible(false);
	}

	public void dispose() {
		updateThread.setRunning(false);
		super.dispose();
	}

	public void setVisible(boolean isVisible) {
		updateThread.setRunning(isVisible);
		super.setVisible(isVisible);
	}

	protected String formatMemory(long memory) {
		if (memory > 1048576) {
			return numberFormat.format((double) memory / (double) 1048576)
					+ " MBytes";
		} else if (memory > 1024) {
			return numberFormat.format((double) memory / (double) 1024)
					+ " kBytes";
		} else {
			return memory + " Bytes";
		}
	}

	protected void updateDisplay() {
		info = VirtualFileSystem.instance().getSwapFileInfos();
		// update general information
		long maxMem = Runtime.getRuntime().maxMemory();
		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		long swapMem = info.length * VirtualFileSystem.DEFAULT_SWAP_FILE_SIZE;
		String memText = formatMemory(totalMem - freeMem) + " used, "
				+ formatMemory(totalMem) + " allocated, "
				+ formatMemory(maxMem) + " available";
		this.labelHeapMemory.setText(memText);
		String swapText = formatMemory(swapMem) + " allocated in "
				+ info.length + " files ("
				+ formatMemory(VirtualFileSystem.DEFAULT_SWAP_FILE_SIZE)
				+ " per file)";
		this.labelSwapMemory.setText(swapText);
		// update swap file information
		JPanel tmpPanel = new JPanel();
		tmpPanel.setBackground(dialogBgColor);
		tmpPanel.setLayout(new BoxLayout(tmpPanel, BoxLayout.X_AXIS));
		tmpPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		BlockDataStorageInfoPanel panel;
		tmpPanel.add(Box.createHorizontalStrut(5));
		for (int i = 0; i < info.length; i++) {
			panel = new BlockDataStorageInfoPanel(info[i], swapPanelSize);
			tmpPanel.add(panel);
			tmpPanel.add(Box.createHorizontalStrut(5));
		}
		swapFilePanel = tmpPanel;
		int scroll = scrollPane.getHorizontalScrollBar().getValue();
		scrollPane.getViewport().setView(swapFilePanel);
		scrollPane.getHorizontalScrollBar().setValue(scroll);
		scrollPane.repaint();
	}

	protected class NikeFsDialogUpdateThread extends Thread {

		protected NikeFsDialog dialog;
		protected boolean isRunning;

		public NikeFsDialogUpdateThread(NikeFsDialog theDialog) {
			isRunning = true;
			dialog = theDialog;
		}

		protected synchronized void waitIfNotRunning() {
			while (isRunning == false) {
				try {
					wait();
				} catch (InterruptedException e) {
					// no biggie...
					// (settings changed)
				}
			}
		}

		public synchronized void setRunning(boolean running) {
			isRunning = running;
			notify();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while (true) {
				dialog.updateDisplay();
				this.waitIfNotRunning();
				// wait for specified interval
				try {
					Thread.sleep(updateDelay);
				} catch (InterruptedException e) {
					// no biggie...
					// (settings changed)
				}
			}
		}

	}

}
