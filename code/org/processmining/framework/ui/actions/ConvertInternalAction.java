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

import javax.swing.AbstractAction;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.mining.MiningResult;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.StopWatch;

/**
 * @author christian
 * 
 */
public class ConvertInternalAction extends AbstractAction {

	private static final long serialVersionUID = 106316877533872580L;

	private ConvertingPlugin algorithm;
	private ProvidedObject object;

	public ConvertInternalAction(ConvertingPlugin algorithm,
			ProvidedObject object) {
		super(RuntimeUtils.stripHtmlForOsx("<html>" /*
													 * + object.getName() +
													 * " using<br>&nbsp;&nbsp;&nbsp;"
													 */+ algorithm.getName()
				+ "</html>"));
		this.algorithm = algorithm;
		this.object = object;
	}

	public ConvertingPlugin getPlugin() {
		return algorithm;
	}

	public String toString() {
		return this.algorithm.getName();
	}

	public void actionPerformed(ActionEvent e) {

		MainUI.getInstance().addAction(algorithm, LogStateMachine.START,
				new Object[] { object });

		SwingWorker worker = new SwingWorker() {
			MiningResult result;
			StopWatch timer = new StopWatch();

			public Object construct() {
				Message.add("Start conversion.");
				timer.start();
				try {
					if (algorithm instanceof DoNotCreateNewInstance) {
						result = algorithm.convert(object);
					} else {
						result = ((ConvertingPlugin) algorithm.getClass()
								.newInstance()).convert(object);
					}
				} catch (IllegalAccessException ex) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					result = algorithm.convert(object);
				} catch (InstantiationException ex) {
					Message.add("No new instantiation of "
							+ algorithm.getName() + " could be made, using"
							+ " old instance instead", Message.ERROR);
					result = algorithm.convert(object);
				}
				return result;
			}

			public void finished() {
				timer.stop();
				Message.add("Conversion duration: " + timer.formatDuration());
				MainUI.getInstance().addAction(
						algorithm,
						LogStateMachine.COMPLETE,
						(result instanceof Provider) ? ((Provider) result)
								.getProvidedObjects() : null);

				MainUI.getInstance().createConversionResultFrame(algorithm,
						result);
			}
		};
		worker.start();
	}
}
