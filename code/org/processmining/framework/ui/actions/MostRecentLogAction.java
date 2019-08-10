package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.menus.OpenRecentFileAction;
import org.processmining.framework.util.RuntimeUtils;

public class MostRecentLogAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = 1546546968613376485L;

	public static Icon getIcon() {
		String customIconPath = UISettings.getInstance()
				.getPreferredIconTheme()
				+ "/toolbar_open_mru.png";
		if ((new File(customIconPath).exists())) {
			return new ImageIcon(customIconPath);
		} else {
			return Utils.getStandardIcon("general/Open24");
		}
	}

	public MostRecentLogAction(MDIDesktopPane desktop) {
		super("Open most recently opened log file again (if possible)",
				MostRecentLogAction.getIcon(), desktop);
		putValue(SHORT_DESCRIPTION, "Open most recently opened log  "
				+ getShortcut());
	}

	public String getShortcut() {
		// get shortcut according to platform
		String shortCut = null;
		if (RuntimeUtils.isRunningMacOsX() == true) {
			shortCut = "Command-R";
		} else {
			shortCut = "Ctrl+R";
		}
		return shortCut;
	}

	public void execute(ActionEvent e) {
		String lastUsed = UISettings.getInstance().getLastOpenedLogFile();

		if (lastUsed != null && lastUsed.length() > 0) {
			new OpenRecentFileAction(lastUsed, (new File(lastUsed)).getName(),
					UISettings.LOGTYPE, desktop).execute(null);
		} else {
			JOptionPane
					.showMessageDialog(
							MainUI.getInstance(),
							"There is no recently used log file yet.\nPlease open a log file using the 'Open MXML Log file' in the 'File' menu first.");
		}
	}

	public void handleOutOfMem() {
		Message.add("Out of memory while analyzing");
	}
}
