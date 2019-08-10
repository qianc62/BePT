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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.processmining.exporting.ExportPlugin;
import org.processmining.exporting.ontologies.SaveOntologiesDialog;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.OutputStreamWithFilename;

public class SAMXMLLogExport implements ExportPlugin {

	public SAMXMLLogExport() {
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();
		boolean logr = false;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				logr = true;
			}
		}
		return logr;
	}

	public String getFileExtension() {
		return "MXML";
	}

	public synchronized void export(final ProvidedObject object,
			final OutputStream output) throws IOException {
		LogReader log = null;
		Object[] o = object.getObjects();

		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof LogReader) {
				log = (LogReader) o[i];
				break;
			}
		}
		if (log == null) {
			throw new IOException(
					"Internal error: no log found in provided objects");
		}

		String filename = null;
		if (output instanceof OutputStreamWithFilename) {
			filename = ((OutputStreamWithFilename) output).getFilename();
		}
		if (saveOntologies(log, filename)) {
			export(log, output);
		} else {
			throw new IOException("Export cancelled.");
		}
	}

	public void export(final LogReader log, final OutputStream output)
			throws IOException {
		SwingWorker w = new SwingWorker() {
			public Object construct() {

				Boolean result = null;

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

					Progress p = new Progress("Writing process instance:", 0,
							log.getLogSummary().getNumberOfProcessInstances());
					synchronized (log) {
						writeHeader(output, log);
					}
					// Now write all process instances.
					int j = 0;
					synchronized (log) {
						Iterator logIt = log.instanceIterator();
						while ((logIt.hasNext()) && (!p.isCanceled())) {
							ProcessInstance pi = (ProcessInstance) logIt.next();

							if (j % 20 == 0) {
								p.setNote(pi.getName());
								p.setProgress(j);
							}
							j++;

							writeProcessInstance(pi, output);
						}
					}

					synchronized (log) {
						writeTail(output);
					}
					result = new Boolean(p.isCanceled());
					p.close();
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
			throw new IOException("export interrupted");
		}
	}

	private boolean saveOntologies(LogReader log, String logFilename) {
		OntologyCollection ontologies = log.getLogSummary().getOntologies();
		List<OntologyModel> changed = new ArrayList<OntologyModel>();

		for (OntologyModel ontology : ontologies.getOntologies()) {
			if (ontology.isChanged()) {
				changed.add(ontology);
			}
		}
		if (changed.isEmpty()) {
			return true;
		}
		return (new SaveOntologiesDialog(log, logFilename)).showDialog();
	}

	public void writeTail(OutputStream output) {
		writeLn("\t", "</Process>", output);
		writeLn("", "</WorkflowLog>", output);

	}

	private String getModelReferences(List<String> uris) {
		StringBuffer result = new StringBuffer();
		if (uris != null && uris.size() > 0) {
			result.append(" modelReference=\"");
			for (String uri : uris) {
				result.append(convertLine(uri));
				result.append(" ");
			}
			result.append("\" ");
		}
		return result.toString();
	}

	public void writeHeader(OutputStream output, LogReader log) {
		writeLn("", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", output);
		writeLn(
				"",
				"<WorkflowLog "
						+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"WorkflowLog.xsd\" \n"
						+ "\tdescription=\"Exported by ProM framework from "
						+ convertLine(log.getLogSummary().getWorkflowLog()
								.getDescription())
						+ "\" "
						+ getModelReferences(log.getLogSummary()
								.getWorkflowLog().getModelReferences()) + ">",
				output);

		// First write data section of log
		DataSection data = log.getLogSummary().getWorkflowLog().getData();
		if (!data.keySet().isEmpty()) {
			writeLn("\t", "<Data>", output);
			Iterator it = data.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (key.equals("program")) {
					continue;
				}
				String value = (String) data.get(key);
				String refs = getModelReferences(data.getModelReferences(key));
				writeLn("\t\t", "<Attribute name=\"" + convertLine(key) + "\""
						+ refs + ">" + convertLine(value) + "</Attribute>",
						output);
			}
			writeLn("\t", "</Data>", output);
		}

		// Write the source section
		writeLn("\t", "<Source "
				+ "program=\""
				+ convertLine(log.getLogSummary().getSource().getData().get(
						"program"))
				+ "\" "
				+ getModelReferences(log.getLogSummary().getSource()
						.getModelReferences()) + ">", output);
		data = log.getLogSummary().getSource().getData();
		writeDataSection("\t\t", data, output);
		writeLn("\t", "</Source>", output);

		// Now, write the process section, for one process
		writeLn("\t", "<Process "
				+ "id=\""
				+ convertLine(log.getLogSummary().getProcesses()[0].getName())
				+ "\" "
				+ "description=\""
				+ convertLine(log.getLogSummary().getProcesses()[0]
						.getDescription()) + "\" "
				+ getModelReferences(log.getProcess(0).getModelReferences())
				+ ">", output);
		data = log.getLogSummary().getProcesses()[0].getData();
		writeDataSection("\t\t", data, output);
	}

	public void writeProcessInstance(ProcessInstance pi, OutputStream output) {
		writeLn("\t\t", "<ProcessInstance " + "id=\""
				+ convertLine(pi.getName()) + "\" " + "description=\""
				+ convertLine(pi.getDescription()) + "\" "
				+ getModelReferences(pi.getModelReferences()) + ">", output);
		DataSection data = pi.getDataAttributes();
		writeDataSection("\t\t\t", data, output);

		Iterator ates = pi.getAuditTrailEntryList().iterator();
		while (ates.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ates.next();
			writeLn("\t\t\t", "<AuditTrailEntry>", output);
			data = ate.getDataAttributes();
			writeDataSection("\t\t\t\t", data, output);

			writeLn("\t\t\t\t",
					"<WorkflowModelElement"
							+ getModelReferences(ate
									.getElementModelReferences()) + ">"
							+ convertLine(ate.getElement())
							+ "</WorkflowModelElement>", output);

			if (ate.getType().startsWith("unknown")) {
				writeLn("\t\t\t\t", "<EventType "
						+ getModelReferences(ate.getTypeModelReferences())
						+ "unknowntype=\""
						+ convertLine(ate.getType()
								.replaceFirst("unknown:", ""))
						+ "\">unknown</EventType>", output);
			} else {
				writeLn("\t\t\t\t", "<EventType"
						+ getModelReferences(ate.getTypeModelReferences())
						+ ">" + convertLine(ate.getType()) + "</EventType>",
						output);
			}

			if (ate.getTimestamp() != null) {
				Date d = ate.getTimestamp();

				SimpleDateFormat dateParser = new SimpleDateFormat("Z");
				String timezone = dateParser.format(d);
				timezone = timezone.substring(0, 3) + ":"
						+ timezone.substring(3, 5);

				dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
				String s = dateParser.format(d);
				s += timezone;

				writeLn("\t\t\t\t", "<Timestamp>" + s + "</Timestamp>", output);
			}

			if (ate.getOriginator() != null && ate.getOriginator().length() > 0) {
				writeLn("\t\t\t\t",
						"<Originator"
								+ getModelReferences(ate
										.getOriginatorModelReferences()) + ">"
								+ convertLine(ate.getOriginator())
								+ "</Originator>", output);
			}

			writeLn("\t\t\t", "</AuditTrailEntry>", output);
		}

		writeLn("\t\t", "</ProcessInstance>", output);

	}

	private void writeDataSection(String indent, DataSection data,
			OutputStream out) {
		if (!data.keySet().isEmpty()) {
			writeLn(indent, "<Data>", out);
			Iterator it = data.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = (String) data.get(key);
				String refs = getModelReferences(data.getModelReferences(key));
				writeLn(indent, "\t<Attribute " + "name=\"" + convertLine(key)
						+ "\" " + refs + ">" + convertLine(value)
						+ "</Attribute>", out);
			}
			writeLn(indent, "</Data>", out);
		}

	}

	private static void writeLn(String indent, String s, OutputStream out) {
		try {
			if (s.length() != 0) {
				out.write(indent.getBytes());
				out.write(s.getBytes());
			}
			out.write("\n".getBytes());
		} catch (IOException ex) {
			Message.add("Error while writing to file", Message.ERROR);
		}
	}

	public String getName() {
		return "SA-MXML log file";
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: SA-MXML log export</b>"
				+ "<p>This Plug-in allows the user to export logs in the SA-MXML format. "
				+ "This format is used by ProM itself and this plugin thus allows the user "
				+ "to apply sets of filters to a log once and store the results for future use. "
				+ "For more information about MXML, see "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"mining_meta_model", "this paper");
	}

	private static String convertLine(String line) {
		if (line == null) {
			return "";
		}
		return line.replaceAll("&", "&amp;").replaceAll("\'", "&apos;")
				.replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(
						">", "&gt;");
	}

}
