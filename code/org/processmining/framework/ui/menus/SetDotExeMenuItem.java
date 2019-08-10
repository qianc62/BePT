/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 29, 2006 5:03:44 AM
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

import javax.swing.JMenuItem;

import org.processmining.framework.ui.DotExeDialog;

/**
 * A menu item for specifying a custom dot executable to be used for graph
 * layouting within ProM.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class SetDotExeMenuItem extends JMenuItem {

	public SetDotExeMenuItem() {
		super("Set custom dot executable...");
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DotExeDialog.instance().setVisible(true);
			}
		});
	}

}
