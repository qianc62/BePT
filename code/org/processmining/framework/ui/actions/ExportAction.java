/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.framework.util.OutputStreamWithFilename;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author christian
 * 
 */
public class ExportAction extends AbstractAction {

	private static final long serialVersionUID = 926879139843390373L;

	private ExportPlugin algorithm;
	private ProvidedObject object;

	public ExportAction(ExportPlugin algorithm, ProvidedObject object) {
		super(RuntimeUtils.stripHtmlForOsx("<html>" /*
													 * + object.getName() +
													 * " to<br>&nbsp;&nbsp;&nbsp;"
													 */+ algorithm.getName()
				+ "</html>"));
		this.algorithm = algorithm;
		this.object = object;
	}

	public ExportPlugin getPlugin() {
		return algorithm;
	}

	public String toString() {
		return this.algorithm.getName();
	}

	public void actionPerformed(ActionEvent e) {
		final String filename = Utils.saveFileDialog(MainUI.getInstance()
				.getDesktop(), new GenericFileFilter(algorithm
				.getFileExtension()));

		if (filename != null && !filename.equals("")) {

			SwingWorker closer = new SwingWorker() {
				public Object construct() {
					OutputStreamWithFilename out = null;

					try {
						out = new OutputStreamWithFilename(filename);
						try {
							if (algorithm instanceof DoNotCreateNewInstance) {
								algorithm.export(object, out);
							} else {
								((ExportPlugin) algorithm.getClass()
										.newInstance()).export(object, out);
							}
						} catch (IllegalAccessException ex2) {
							Message.add("No new instantiation of "
									+ algorithm.getName()
									+ " could be made, using"
									+ " old instance instead", Message.ERROR);
							algorithm.export(object, out);
						} catch (InstantiationException ex2) {
							Message.add("No new instantiation of "
									+ algorithm.getName()
									+ " could be made, using"
									+ " old instance instead", Message.ERROR);
							algorithm.export(object, out);
						}
					} catch (IOException ex) {
						return ex;
					} catch (OutOfMemoryError err) {
						return err;
					} finally {
						try {
							if (out != null) {
								out.close();
							}
						} catch (IOException ex1) {
							return ex1;
						}
					}
					return null;
				}

				public void finished() {
					if (get() != null && get() instanceof Exception) {
						Message.add(filename + ": "
								+ ((Exception) get()).getMessage());
						MainUI.getInstance().addAction(algorithm,
								LogStateMachine.ATE_ABORT,
								new Object[] { object, filename, get() });
					} else {
						Message.add(filename + " exported.");
						MainUI.getInstance().addAction(algorithm,
								LogStateMachine.COMPLETE,
								new Object[] { object, filename });
					}
				}
			};
			closer.start();

			Message.add("Starting export to file: " + filename);

		}
	}
}
