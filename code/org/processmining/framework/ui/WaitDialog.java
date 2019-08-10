/**
 * Project: ProM
 * File: WaitDialog.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Aug 25, 2006, 7:24:59 PM
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
package org.processmining.framework.ui;

import java.awt.Frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * A simple dialog with title and message, indicating to the user that he has to
 * wait for a certain action to finish before continuation.
 * <p>
 * Features an indeterminate progress bar, as we all like 'em :)
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class WaitDialog extends JDialog {

	protected String title = null;
	protected String message = null;

	/**
	 * Creates a new wait dialog with the specified information
	 * 
	 * @param owner
	 *            Parent frame of the new dialog
	 * @param title
	 *            Title of the dialog
	 * @param message
	 *            Message to be displayed within the dialog.
	 */
	public WaitDialog(Frame owner, String title, String message) {
		super(owner, title, false);
		this.title = title;
		this.message = message;
		this.setSize(300, 150);
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(Box.createVerticalGlue());
		JLabel messageLabel = new JLabel(message);
		messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		contentPanel.add(messageLabel);
		contentPanel.add(Box.createVerticalStrut(10));
		JProgressBar waitBar = new JProgressBar();
		waitBar.setIndeterminate(true);
		waitBar.setAlignmentX(JProgressBar.CENTER_ALIGNMENT);
		JPanel waitBarPanel = new JPanel();
		waitBarPanel.setOpaque(false);
		waitBarPanel.setLayout(new BoxLayout(waitBarPanel, BoxLayout.X_AXIS));
		waitBarPanel.add(Box.createHorizontalGlue());
		waitBarPanel.add(waitBar);
		waitBarPanel.add(Box.createHorizontalGlue());
		contentPanel.add(waitBarPanel);
		contentPanel.add(Box.createVerticalGlue());
		this.add(contentPanel);
		this.setLocationRelativeTo(owner);
	}
}
