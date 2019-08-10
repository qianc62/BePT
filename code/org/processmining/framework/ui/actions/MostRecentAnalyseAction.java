package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.AnalysisPluginCollection;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.menus.AnalysisAction;
import org.processmining.framework.util.RuntimeUtils;

public class MostRecentAnalyseAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = 1546546968613376485L;

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_analyse_mru.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Find24");
		}
	}

	public MostRecentAnalyseAction(MDIDesktopPane desktop) {
		super("Execute most recently used analysus action again (if possible)",
				MostRecentAnalyseAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Execute most recently used analysis  "
				+ getShortcut());
	}

	public String getShortcut() {
		// get shortcut according to platform
		String shortCut = null;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			shortCut = "Command-T";
		} else {
			shortCut = "Ctrl+T";
		}
		return shortCut;
	}

	public void execute(ActionEvent e) {
		String lastUsed = UISettings.getInstance().getLastUsedAnalysis();

		if (lastUsed == null || lastUsed.length() == 0) {
			JOptionPane
					.showMessageDialog(
							MainUI.getInstance(),
							"There is no recently used analysis plugin yet.\nPlease select a plugin from the 'Analysis' menu first.");
			return;
		}

		ProvidedObject[] objects = MainUI.getInstance().getProvidedObjects();
		AnalysisPluginCollection collection = AnalysisPluginCollection
				.getInstance();

		if (objects.length == 0) {
			JOptionPane
					.showMessageDialog(MainUI.getInstance(),
							"Please select a window which provides an object to analyze.");
			return;
		}

		for (int i = 0; i < collection.size(); i++) {
			AnalysisPlugin algorithm = (AnalysisPlugin) collection.get(i);

			if (algorithm != null && algorithm.getName().equals(lastUsed)) {
				AnalysisInputItem[] items = algorithm.getInputItems();

				if (items.length == 1
						&& (items[0].getMinimum() == 1 || items[0].getMaximum() == 1)) {
					for (int j = 0; j < objects.length; j++) {
						ProvidedObject object = objects[j];

						if (object != null && items[0].accepts(object)) {
							new AnalysisAction(algorithm, object)
									.actionPerformed(null);
							return;
						}
					}
				} else {
					JOptionPane
							.showMessageDialog(
									MainUI.getInstance(),
									"The analysis plugin '"
											+ lastUsed
											+ "' needs multiple objects.\nPlease use the 'More analysis...' option in the 'Analysis' menu to run it again.");
					return;
				}
			}
		}
		JOptionPane
				.showMessageDialog(
						MainUI.getInstance(),
						"The analysis plugin '"
								+ lastUsed
								+ "' cannot be run on the objects provided by the window that currently has the focus.");
	}

	public void handleOutOfMem() {
		Message.add("Out of memory while analyzing");
	}
}
