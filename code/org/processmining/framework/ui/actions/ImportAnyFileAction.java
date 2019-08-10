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

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.OpenMXMLLogDialog;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.ImportPluginCollection;
import org.processmining.importing.LogReaderConnectionImportPlugin;
import org.processmining.mining.MiningResult;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ImportAnyFileAction extends CatchOutOfMemoryAction {

	protected boolean useJFileChooser = false;

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_open.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public ImportAnyFileAction(MDIDesktopPane desktop, boolean useJFileChooser) {
		super("Open supported file...", ImportAnyFileAction.getIcon(), desktop);
		this.useJFileChooser = useJFileChooser;
		putValue(SHORT_DESCRIPTION, "Open any type of file supported by ProM  "
				+ getShortcut());
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
	}

	public String getShortcut() {
		// get shortcut according to platform
		String shortCut = null;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			shortCut = "Command-O";
		} else {
			shortCut = "Ctrl+O";
		}
		return shortCut;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.actions.CatchOutOfMemoryAction#execute
	 * (java.awt.event.ActionEvent)
	 */
	@Override
	protected void execute(ActionEvent e) {
		if (this.useJFileChooser == true) {
			executeWithJFileChooser();
		} else {
			executeWithAWTFileChooser();
		}
	}

	protected void executeWithAWTFileChooser() {
		FileDialog dialog = new FileDialog(MainUI.getInstance(),
				"Open file...", FileDialog.LOAD);
		dialog.setFilenameFilter(new ImportAnyFilenameFilter());
		dialog.setDirectory((new File(UISettings.getInstance()
				.getLastOpenedImportFile())).getParent());
		dialog.setFile((new File(UISettings.getInstance()
				.getLastOpenedImportFile())).getName());
		dialog.setVisible(true);
		if (dialog.getFile() != null) {
			File file = new File(dialog.getDirectory() + File.separator
					+ dialog.getFile());
			handleOpeningFile(file);
		}
	}

	protected void executeWithJFileChooser() {
		JFileChooser dialog = new JFileChooser(new File(UISettings
				.getInstance().getLastOpenedImportFile()));
		dialog.setFileFilter(new ImportAnyFilenameFilter());
		dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		dialog.setMultiSelectionEnabled(true);
		int returnVal = dialog.showOpenDialog(MainUI.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			for (File file : dialog.getSelectedFiles()) {
				UISettings.getInstance().setLastOpenedImportFile(
						file.getAbsolutePath());
				List<ImportPlugin> plugins = getCompatiblePlugins(file);
				ImportPlugin plugin = plugins.get(0);
				if (plugins.size() > 1) {
					// multiple plugins can handle this file; let the user pick
					String[] pluginNames = new String[plugins.size()];
					for (int i = 0; i < pluginNames.length; i++) {
						pluginNames[i] = plugins.get(i).getName();
					}
					String picked = (String) JOptionPane.showInputDialog(MainUI
							.getInstance(), "The file " + file.getName()
							+ " can be \n"
							+ "loaded by a number of different import \n"
							+ "plugins.\n"
							+ "Please choose the appropriate import plugin:",
							"Please choose type of file...",
							JOptionPane.QUESTION_MESSAGE, null, pluginNames,
							pluginNames[0]);
					for (ImportPlugin plg : plugins) {
						if (plg.getName().equals(picked)) {
							plugin = plg;
							break;
						}
					}
				}
				LogReader connectLog = null;
				if (plugin instanceof LogReaderConnectionImportPlugin) {
					// pick log reader to connect to
					connectLog = getLogToConnect();
				}
				if (plugin instanceof MxmlImportPlugin) {
					LogFile logFile = null;
					if (file.getName().toLowerCase().endsWith(".zip")) {
						OpenMXMLLogDialog logDialog = new OpenMXMLLogDialog(
								file.getAbsolutePath());
						logDialog.setChosenZipFile(file.getAbsolutePath(), "");
						logDialog.showDialog(true);
						logFile = logDialog.getFile();
					} else {
						logFile = LogFile.getInstance(file.getAbsolutePath());
					}
					MainUI.getInstance().createOpenLogFrame(logFile);
					UISettings.getInstance().addRecentFile(logFile.toString(),
							null);
					UISettings.getInstance().setLastOpenedLogFile(
							logFile.toString());
				} else {
					UISettings.getInstance().addRecentFile(
							file.getAbsolutePath(), plugin.getName());
					MainUI.getInstance().importFromFile(plugin,
							file.getAbsolutePath(), connectLog);
				}
			}
		}
	}

	protected void handleOpeningFile(File file) {
		UISettings.getInstance()
				.setLastOpenedImportFile(file.getAbsolutePath());
		List<ImportPlugin> plugins = getCompatiblePlugins(file);
		ImportPlugin plugin = plugins.get(0);
		if (plugins.size() > 1) {
			// multiple plugins can handle this file; let the user pick
			String[] pluginNames = new String[plugins.size()];
			for (int i = 0; i < pluginNames.length; i++) {
				pluginNames[i] = plugins.get(i).getName();
			}
			String picked = (String) JOptionPane.showInputDialog(MainUI
					.getInstance(),
					"The file you have chosen can be loaded by a\n"
							+ "number of different import plugins.\n"
							+ "Please choose the appropriate import plugin:",
					"Please choose type of file...",
					JOptionPane.QUESTION_MESSAGE, null, pluginNames,
					pluginNames[0]);
			for (ImportPlugin plg : plugins) {
				if (plg.getName().equals(picked)) {
					plugin = plg;
					break;
				}
			}
		}
		LogReader connectLog = null;
		if (plugin instanceof LogReaderConnectionImportPlugin) {
			// pick log reader to connect to
			connectLog = getLogToConnect();
		}
		if (plugin instanceof MxmlImportPlugin) {
			LogFile logFile = null;
			if (file.getName().toLowerCase().endsWith(".zip")) {
				OpenMXMLLogDialog logDialog = new OpenMXMLLogDialog(file
						.getAbsolutePath());
				logDialog.setChosenZipFile(file.getAbsolutePath(), "");
				logDialog.showDialog(true);
				logFile = logDialog.getFile();
			} else {
				logFile = LogFile.getInstance(file.getAbsolutePath());
			}
			MainUI.getInstance().createOpenLogFrame(logFile);
			UISettings.getInstance().addRecentFile(logFile.toString(), null);
			UISettings.getInstance().setLastOpenedLogFile(logFile.toString());
		} else {
			UISettings.getInstance().addRecentFile(file.getAbsolutePath(),
					plugin.getName());
			MainUI.getInstance().importFromFile(plugin, file.getAbsolutePath(),
					connectLog);
		}
	}

	protected LogReader getLogToConnect() {
		HashMap<String, LogReader> logReaders = new HashMap<String, LogReader>();
		JInternalFrame[] frames = MainUI.getInstance().getDesktop()
				.getAllFrames();
		for (JInternalFrame frame : frames) {
			if (frame instanceof Provider) {
				ProvidedObject[] providedObjects = ((Provider) frame)
						.getProvidedObjects();
				for (ProvidedObject providedObject : providedObjects) {
					for (Object object : providedObject.getObjects()) {
						if (object instanceof LogReader) {
							logReaders.put("Open with log: " + frame.getTitle()
									+ " - " + providedObject.getName(),
									(LogReader) object);
						}
					}
				}
			}
		}
		if (logReaders.size() == 0) {
			return null;
		}
		String[] keys = new String[logReaders.size() + 1];
		keys[0] = "Open without log file";
		int index = 1;
		for (String key : logReaders.keySet()) {
			keys[index] = key;
			index++;
		}
		String picked = (String) JOptionPane.showInputDialog(MainUI
				.getInstance(), "The file you have chosen can optionally be\n"
				+ "connected to a log, if you want to.\n"
				+ "Please pick a log to connect the file to:",
				"Connect file to log...", JOptionPane.QUESTION_MESSAGE, null,
				keys, keys[0]);
		if (picked.equals(keys[0])) {
			return null;
		} else {
			return logReaders.get(picked);
		}
	}

	protected List<ImportPlugin> getCompatiblePlugins(File file) {
		ArrayList<ImportPlugin> plugins = new ArrayList<ImportPlugin>();
		MxmlImportPlugin mxmlPlugin = new MxmlImportPlugin();
		if (mxmlPlugin.getFileFilter().accept(file)) {
			plugins.add(mxmlPlugin);
		}
		ImportPluginCollection importPlugins = ImportPluginCollection
				.getInstance();
		for (Plugin plugin : importPlugins.getPlugins()) {
			ImportPlugin importPlugin = (ImportPlugin) plugin;
			if (importPlugin.getFileFilter().accept(file)) {
				plugins.add(importPlugin);
			}
		}
		return plugins;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.ui.actions.CatchOutOfMemoryAction#handleOutOfMem
	 * ()
	 */
	@Override
	protected void handleOutOfMem() {
		// ignore this one for now
	}

	protected class MxmlImportPlugin implements ImportPlugin {

		protected MxmlFilenameFilter filter = new MxmlFilenameFilter();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.importing.ImportPlugin#getFileFilter()
		 */
		public FileFilter getFileFilter() {
			return new FileFilter() {

				@Override
				public boolean accept(File file) {
					return filter.accept(file.getParentFile(), file.getName());
				}

				@Override
				public String getDescription() {
					return "Accepts MXML log files";
				}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.processmining.importing.ImportPlugin#importFile(java.io.InputStream
		 * )
		 */
		public MiningResult importFile(InputStream input) throws IOException {
			// ignore
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
		 */
		public String getHtmlDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.processmining.framework.plugin.Plugin#getName()
		 */
		public String getName() {
			return "MXML Log reader";
		}

	}

}
