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
package org.processmining.framework.ui.slicker.launch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.converting.ConvertingPlugin;
import org.processmining.converting.ConvertingPluginCollection;
import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.ExportPluginCollection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.actions.ConvertInternalAction;
import org.processmining.framework.ui.actions.ExportAction;
import org.processmining.framework.ui.actions.MineAction;
import org.processmining.framework.ui.menus.AnalysisAction;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningPluginCollection;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class LaunchActionList {

	public enum PluginType {
		MINING, ANALYSIS, CONVERSION, EXPORT;
	}

	protected static final MiningPluginCollection colMining = MiningPluginCollection
			.getInstance();
	protected static final AnalysisPluginCollection colAnalysis = AnalysisPluginCollection
			.getInstance();
	protected static final ConvertingPluginCollection colConverting = ConvertingPluginCollection
			.getInstance();
	protected static final ExportPluginCollection colExport = ExportPluginCollection
			.getInstance();

	protected ArrayList<AbstractAction> actions;
	protected ArrayList<PluginType> pluginTypes;
	protected ArrayList<String> names;

	protected ArrayList<ActionListener> listeners;

	protected boolean isActive = true;

	public LaunchActionList(final ProvidedObject object) {
		actions = new ArrayList<AbstractAction>();
		pluginTypes = new ArrayList<PluginType>();
		names = new ArrayList<String>();
		listeners = new ArrayList<ActionListener>();
		Thread gatherThread = new Thread() {
			public void run() {
				updateData(object);
			}
		};
		gatherThread.run();
	}

	public void initialize(final ProvidedObject object) {
		actions = new ArrayList<AbstractAction>();
		pluginTypes = new ArrayList<PluginType>();
		names = new ArrayList<String>();
		listeners = new ArrayList<ActionListener>();
		Thread gatherThread = new Thread() {
			public void run() {
				updateData(object);
			}
		};
		gatherThread.run();
	}

	protected void updateData(ProvidedObject object) {
		// get all matching mining actions
		synchronized (this) {
			for (Object obj : object.getObjects()) {
				if (obj instanceof LogReader) {
					for (Plugin miner : colMining.getPlugins()) {
						if (miner == null) {
							continue;
						}
						actions
								.add(new MineAction((MiningPlugin) miner,
										object));
						pluginTypes.add(PluginType.MINING);
						names.add(miner.getName().toLowerCase());
					}
				}
				if (isActive == false) {
					return;
				} // abort if indicated
			}
		}
		notifyListeners();
		synchronized (this) {
			// get all matching analysis actions
			for (int i = 0; i < colAnalysis.size(); i++) {
				AnalysisPlugin algorithm = (AnalysisPlugin) colAnalysis.get(i);
				if (algorithm == null) {
					continue;
				}
				AnalysisInputItem[] items = algorithm.getInputItems();
				// if the analysisInputItem requires exactly one object and
				// this item accepts our object, add an action
				if (items.length == 1 && items[0].getMaximum() == 1) {
					if (items[0].accepts(object)) {
						actions.add(new AnalysisAction(algorithm, object));
						pluginTypes.add(PluginType.ANALYSIS);
						names.add(algorithm.getName().toLowerCase());
					}
				}
				if (isActive == false) {
					return;
				} // abort if indicated
			}
		}
		notifyListeners();
		synchronized (this) {
			// get all matching conversion actions
			for (int i = 0; i < colConverting.size(); i++) {
				ConvertingPlugin algorithm = (ConvertingPlugin) colConverting
						.get(i);
				if (algorithm == null) {
					continue;
				}
				if (algorithm.accepts(object)) {
					actions.add(new ConvertInternalAction(algorithm, object));
					pluginTypes.add(PluginType.CONVERSION);
					names.add(algorithm.getName().toLowerCase());
				}
				if (isActive == false) {
					return;
				} // abort if indicated
			}
		}
		notifyListeners();
		synchronized (this) {
			// get all matching export actions
			for (int i = 0; i < colExport.size(); i++) {
				ExportPlugin algorithm = (ExportPlugin) colExport.get(i);
				if (algorithm == null) {
					continue;
				}
				if (algorithm.accepts(object)) {
					actions.add(new ExportAction(algorithm, object));
					pluginTypes.add(PluginType.EXPORT);
					names.add(algorithm.getName().toLowerCase());
				}
				if (isActive == false) {
					return;
				} // abort if indicated
			}
		}
		notifyListeners();
	}

	public synchronized void addUpdateListener(ActionListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeUpdateListener(ActionListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners() {
		ActionEvent evt = new ActionEvent(this, 1, "update");
		for (ActionListener listener : listeners) {
			listener.actionPerformed(evt);
		}
	}

	public void cancel() {
		isActive = false;
	}

	public synchronized List<AbstractAction> getAllActions() {
		return new ArrayList<AbstractAction>(actions);
	}

	public synchronized List<AbstractAction> filter(ActionFilter filter) {
		ArrayList<AbstractAction> result = new ArrayList<AbstractAction>();
		for (int i = 0; i < pluginTypes.size(); i++) {
			PluginType type = pluginTypes.get(i);
			// ensure first that the plugin type is valid
			if (type == PluginType.MINING) {
				if (filter.useMining() == false) {
					continue;
				}
			} else if (type == PluginType.ANALYSIS) {
				if (filter.useAnalysis() == false) {
					continue;
				}
			} else if (type == PluginType.CONVERSION) {
				if (filter.useConversion() == false) {
					continue;
				}
			} else if (type == PluginType.EXPORT) {
				if (filter.useExport() == false) {
					continue;
				}
			}
			// type must be valid; check name
			if (filter.filter(names.get(i)) == true) {
				// okay, add to result set
				result.add(actions.get(i));
			}
		}
		return result;
	}

}
