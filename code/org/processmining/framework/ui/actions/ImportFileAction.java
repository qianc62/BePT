/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.ui.actions;

import java.awt.event.ActionEvent;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.importing.ImportPlugin;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class ImportFileAction extends CatchOutOfMemoryAction {

	private ImportPlugin algorithm;
	private LogReader log;

	public ImportFileAction(ImportPlugin algorithm, MDIDesktopPane desktop) {
		this(algorithm, null, desktop, "Open " + algorithm.getName());
	}

	public ImportFileAction(ImportPlugin algorithm, LogReader connectToLog,
			MDIDesktopPane desktop, String label) {
		super(label, desktop);
		this.algorithm = algorithm;
		this.log = connectToLog;
	}

	public void execute(ActionEvent e) {
		String filename = Utils.openImportFileDialog(MainUI.getInstance(),
				algorithm.getFileFilter());
		if (filename.equals("")) {
			return;
		}

		UISettings.getInstance().addRecentFile(filename, algorithm.getName());
		UISettings.getInstance().setLastOpenedImportFile(filename);

		MainUI.getInstance().importFromFile(algorithm, filename, log);
	}

	public void handleOutOfMem() {
		Message.add("File to big for import: Out Of Memory");
	}
}
