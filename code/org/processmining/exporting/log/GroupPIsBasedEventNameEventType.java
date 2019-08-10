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

package org.processmining.exporting.log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.log.util.FilterPerWorkflowModelElementAndEvent;
import org.processmining.exporting.log.util.MyProcessInstance;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class GroupPIsBasedEventNameEventType implements ExportPlugin {

	SAMXMLLogExport xmlLogExport = new SAMXMLLogExport();

	public GroupPIsBasedEventNameEventType() {

	}

	public String getName() {
		return "Grouped MXML log (same sequences)";
	}

	public String getHtmlDescription() {
		return "<p> This export plug-in groups traces that express the same sequence "
				+ "of tasks. For instance, if a log contains the four traces (i) \"a,b,c,b,c,d\", "
				+ "(ii) \"a,b,c,b,c,b,c,b,c,b,c,d\", (iii) \"a,f,g,h,d\" and (iv) \"a,b,c,b,c,d\", "
				+ "the traces (i) and (iv) are going to "
				+ "be grouped because they express the same sequence of events.";
	}

	public boolean accepts(ProvidedObject object) {
		return xmlLogExport.accepts(object);
	}

	public String getFileExtension() {
		return xmlLogExport.getFileExtension();
	}

	public synchronized void export(final ProvidedObject object,
			final OutputStream output) throws IOException {

		SwingWorker w = new SwingWorker() {
			public Object construct() {

				Boolean result = null;

				// retrieving the log...
				Object[] o = object.getObjects();
				LogReader log = null;
				for (int i = 0; (log == null); i++) {
					if (o[i] instanceof LogReader) {
						log = (LogReader) o[i];
					}
				}

				if (log.numberOfProcesses() <= 0) {
					JOptionPane
							.showMessageDialog(
									MainUI.getInstance(),
									"No selected process for this log or log is empty!\nThe export will be interrupted!",
									"Error in export plug-in '" + getName()
											+ "'", JOptionPane.ERROR_MESSAGE);
					result = new Boolean(true);
				} else if (log.getProcess(0).size() <= 0) {
					JOptionPane
							.showMessageDialog(
									MainUI.getInstance(),
									"The selected process does not contain process instances!\nThe export will be interrupted!",
									"Error in export plug-in '" + getName()
											+ "'", JOptionPane.ERROR_MESSAGE);
					result = new Boolean(true);
				} else {

					// for testing purposes (begin)
					int numberTraceInLogBeforeGrouping = log
							.numberOfInstances();
					// for testing purposes (end)

					// grouping the process instances...
					Iterator iteratorPIs = filterDuplicateATEs(log);

					Progress p = new Progress("Writing process instance:", 0,
							log.getLogSummary().getNumberOfProcessInstances());

					xmlLogExport.writeHeader(output, log);
					// Now write all process instances.
					int j = 0;

					// for testing purposes (begin)
					int numberUniqueTraceInLogAfterGrouping = 0;
					ArrayList listOfFrequencyOfTraces = new ArrayList();
					// for testing purposes (end)

					while ((iteratorPIs.hasNext()) && (!p.isCanceled())) {
						ProcessInstance pi = ((MyProcessInstance) iteratorPIs
								.next()).getPI();

						p.setNote(pi.getName());
						p.setProgress(j++);
						xmlLogExport.writeProcessInstance(pi, output);

						// for testing purposes (begin)
						numberUniqueTraceInLogAfterGrouping++;
						listOfFrequencyOfTraces.add(new Integer(
								MethodsForWorkflowLogDataStructures
										.getNumberSimilarProcessInstances(pi)));
						// for testing purposes (end)

					}

					xmlLogExport.writeTail(output);
					result = new Boolean(p.isCanceled());
					p.close();

					// for testing purposes (begin)
					Integer[] orderedElementsInList = new Integer[listOfFrequencyOfTraces
							.size()];
					orderedElementsInList = (Integer[]) listOfFrequencyOfTraces
							.toArray(orderedElementsInList);
					Arrays.sort(orderedElementsInList);
					StringBuffer stringOrderedElementsInList = new StringBuffer();
					stringOrderedElementsInList.append("[");
					int totalNumTracesGroupedLog = 0;
					for (int i = 0; i < orderedElementsInList.length; i++) {
						stringOrderedElementsInList.append(" ").append(
								orderedElementsInList[i]);
						totalNumTracesGroupedLog += orderedElementsInList[i];
					}
					stringOrderedElementsInList.append(" ]");

					Message
							.add(
									"<GroupLog(sameSequences)  numberTraceInLogBeforeGrouping=\""
											+ numberTraceInLogBeforeGrouping
											+ "\" numberUniqueTraceInLogAfterGrouping=\""
											+ numberUniqueTraceInLogAfterGrouping
											+ "\" totalNumberTraceInLogAfterGrouping=\""
											+ totalNumTracesGroupedLog
											+ "\" frequenciesOfTracesInTheLog=\""
											+ stringOrderedElementsInList
													.toString() + "\">",
									Message.TEST);
					// for testing purposes (end)
				}
				return result;
			}

		};
		w.start();
		try {
			w.join();
		} catch (InterruptedException ex) {
		}
		if (w.get() == null || ((Boolean) w.get()).booleanValue() == true) {
			throw new IOException("export interrupted.");
		}

	}

	private Iterator filterDuplicateATEs(LogReader logReader) {

		Hashtable filteredProcessInstances = new Hashtable();

		ProcessInstance pi = null;
		MyProcessInstance myPi = null;
		MyProcessInstance aux = null;

		Iterator logReaderIterator = logReader.instanceIterator();
		while (logReaderIterator.hasNext()) {

			pi = (ProcessInstance) logReaderIterator.next();

			myPi = new FilterPerWorkflowModelElementAndEvent(pi,
					MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(pi));

			// adding the retrieved process instance to filteredProcessInstances
			if (filteredProcessInstances.contains(myPi)) {
				aux = (MyProcessInstance) filteredProcessInstances.get(myPi);
				aux
						.increaseNumberSimilarPIs(MethodsForWorkflowLogDataStructures
								.getNumberSimilarProcessInstances(pi));
				aux.addGroupedPiIdentifier(myPi.getPI().getName());

			} else {
				filteredProcessInstances.put(myPi, myPi);
			}
			myPi = null;
		}

		return filteredProcessInstances.values().iterator();
	}

}
