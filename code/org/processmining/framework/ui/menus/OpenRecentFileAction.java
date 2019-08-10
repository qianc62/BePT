package org.processmining.framework.ui.menus;

import java.awt.event.ActionEvent;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.plugin.DoNotCreateNewInstance;
import org.processmining.framework.plugin.Plugin;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.actions.CatchOutOfMemoryAction;
import org.processmining.importing.ImportPlugin;
import org.processmining.importing.ImportPluginCollection;

public class OpenRecentFileAction extends CatchOutOfMemoryAction {

	private static final long serialVersionUID = 7284362331132178747L;

	private String longFile;
	private String algorithmName;
	private LogReader log;

	public OpenRecentFileAction(String longFileName, String label,
			String algorithm, MDIDesktopPane desktop, LogReader log) {
		super(label, desktop);
		putValue(SHORT_DESCRIPTION, longFileName);

		this.longFile = longFileName;
		this.algorithmName = algorithm;
		this.log = log;

	}

	public OpenRecentFileAction(String longFileName, String shortFileName,
			String algorithm, MDIDesktopPane desktop) {
		super(shortFileName + " - " + algorithm, desktop);
		putValue(SHORT_DESCRIPTION, longFileName);

		this.longFile = longFileName;
		this.algorithmName = algorithm;
	}

	public void execute(ActionEvent e) {
		Plugin algorithm = null;
		try {
			if (algorithmName.equals(UISettings.LOGTYPE)) {
				LogFile logFile = LogFile.getInstance(longFile);
				MainUI.getInstance().addAction("import : MXML Log File",
						LogStateMachine.START, null);
				MainUI.getInstance().createOpenLogFrame(logFile);
			} else {
				algorithm = ImportPluginCollection.getInstance().get(
						algorithmName);
				if (!(algorithm instanceof DoNotCreateNewInstance)) {
					algorithm = (ImportPlugin) algorithm.getClass()
							.newInstance();
				}
				MainUI.getInstance().importFromFile((ImportPlugin) algorithm,
						longFile, log);
			}
			UISettings.getInstance().addRecentFile(longFile, algorithmName);
		} catch (InstantiationException ex) {
			Message
					.add(
							"Could not create a new instance of the selected algorithm!",
							Message.ERROR);
			Message.add(ex.getMessage(), Message.ERROR);
		} catch (IllegalAccessException ex) {
			Message
					.add(
							"Could not create a new instance of the selected algorithm!",
							Message.ERROR);
			Message.add(ex.getMessage(), Message.ERROR);
		}
	}

	public void handleOutOfMem() {

	}
}
