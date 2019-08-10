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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.log.SAMXMLLogExport;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.ui.MDIDesktopPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.ui.Utils;
import org.processmining.framework.ui.filters.GenericFileFilter;

import com.holub.tools.Archive;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class ExportHistoryAction extends CatchOutOfMemoryAction {

	public void execute(ActionEvent e) {
		final String filename = Utils.saveFileDialog(desktop,
				new GenericFileFilter(".mxml.zip"));
		executeWithFile(e, filename);
	}

	public void executeWithFile(ActionEvent e, String filename) {
		final String fn = UISettings.getInstance().getExecutionLogFileName();
		final ExportPlugin algorithm = new SAMXMLLogExport();

		MainUI.getInstance().addAction(algorithm, LogStateMachine.START,
				new Object[] {});

		File f = new File(fn + ".zip");
		if (f.exists()) {

			try {

				File outputFile = new File(filename);
				FileOutputStream out = new FileOutputStream(outputFile);
				FileInputStream in = new FileInputStream(f);
				int c;
				while ((c = in.read()) != -1) {
					out.write(c);
				}
				in.close();
				out.close();
				in = null;
				out = null;

				// Add the finishing tags to the copy of the log
				Archive zipArchive = new Archive(filename);
				OutputStream executionLog = zipArchive.output_stream_for(fn
						+ " content.xml", true);
				executionLog.write("</Process>".getBytes());
				executionLog.write("</WorkflowLog>".getBytes());
				executionLog.flush();
				executionLog.close();
				zipArchive.close();

			} catch (FileNotFoundException ex) {
				Message.add("Could not create copy of execution log: "
						+ ex.getMessage(), Message.ERROR);
			} catch (IOException ex) {
				Message.add("Could not copy execution log for export: "
						+ ex.getMessage(), Message.ERROR);
			} catch (InterruptedException ex) {
				Message.add("Could not copy execution log for export: "
						+ ex.getMessage(), Message.ERROR);
			} finally {
				Message.add("Finished writing history.");
			}
		}
	}

	public void handleOutOfMem() {
	}

	public ExportHistoryAction(String s, MDIDesktopPane desktop) {
		super(s, desktop);
	}

}
