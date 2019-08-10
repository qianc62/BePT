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

package org.processmining.exporting.instances;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.epcpack.InstanceEPC;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.mining.instancemining.InstanceEPCBuilder;

public class PPMInstancesExport implements ExportPlugin {
	private LogReader log = null;
	private ArrayList instances = null;
	private String procTypeGroup = null;
	private String procType = null;
	private InstanceEPCBuilder ieb = null;
	private boolean theExporterIsBusy = false;

	public PPMInstancesExport() {
	}

	public String getName() {
		return "PPM Instances";
	}

	public String getFileExtension() {
		return "xml";
	}

	public void export(final ProvidedObject object, final OutputStream output)
			throws IOException {
		if (theExporterIsBusy) {
			return;
		}
		theExporterIsBusy = true;

		SwingWorker w = new SwingWorker() {
			public Object construct() {
				Boolean result;

				Object[] o = object.getObjects();

				ieb = null;
				log = null;
				for (int i = 0; (ieb == null) || (log == null); i++) {
					if (o[i] instanceof InstanceEPCBuilder) {
						ieb = (InstanceEPCBuilder) o[i];
					}
					if (o[i] instanceof LogReader) {
						log = (LogReader) o[i];
					}
				}

				// From the log, we need to get all instances given by indices.
				// So if indices= {0,1} we need the first two log traces.

				PPMInstancesExportUI ui = new PPMInstancesExportUI(MainUI
						.getInstance(), "Specify type and typegroup");
				if (!ui.showModal()) {
					return null;
				}
				procTypeGroup = ui.getProcessTypeGroup();
				procType = ui.getProcessType();

				Progress p = new Progress("Writing process instance:", 0, log
						.getLogSummary().getNumberOfProcessInstances());

				// We have a processTypeGroup and ProcessType
				// Write the header to the stream.
				writeLn("<?xml version=\"1.0\"?>", output);
				writeLn("<!DOCTYPE graphlist SYSTEM \"graph.dtd\">", output);
				writeLn("<graphlist>", output);

				int j = 0;
				synchronized (log) {
					Iterator logIt = log.instanceIterator();
					while ((logIt.hasNext()) && (!p.isCanceled())) {
						ProcessInstance pi = (ProcessInstance) logIt.next();

						// Message.add("Writing instance:"
						// +pi.getName()+" ATE's: "+pi.getAuditTrailEntries().size());

						p.setNote(pi.getName());
						p.setProgress(j++);
						try {
							writeInstance(output, pi);
						} catch (IOException ex) {
						}

					}
				}
				writeLn("</graphlist>", output);

				result = new Boolean(p.isCanceled());
				p.close();
				return result;
			}

			public void finished() {
				theExporterIsBusy = false;
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

	private void writeLn(String s, OutputStream out) {
		try {
			if (s.length() != 0) {
				out.write(s.getBytes());
			}
			out.write("\n".getBytes());
		} catch (IOException ex) {
		}
	}

	private void writeInstance(OutputStream out, ProcessInstance pi)
			throws IOException {

		InstanceEPC epc = (InstanceEPC) ieb.build(pi);

		writeLn("<graph id=\"" + procType + epc.getIdentifier()
				+ "\" xml:lang=\"en\">", out);
		writeLn("   <attribute type=\"AT_ID\">" + procType
				+ epc.getIdentifier() + "</attribute>", out);
		writeLn("   <attribute type=\"AT_EPK_KEY\">" + procType
				+ epc.getIdentifier() + "</attribute>", out);
		writeLn("   <attribute type=\"AT_PROCTYPEGROUP\">" + procTypeGroup
				+ "</attribute>", out);
		writeLn("   <attribute type=\"AT_PROCTYPE\">" + procType
				+ "</attribute>", out);

		Map data = pi.getAttributes();
		Iterator dataKeys = data.keySet().iterator();
		while (dataKeys.hasNext()) {
			Object key = dataKeys.next();
			String s = (String) key;
			writeLn("   <attribute type=\"AT_" + key + "\">"
					+ (String) data.get(key) + "</attribute>", out);
		}

		// Now write the real information for this instance
		epc.writePPMImport(out);

		writeLn("</graph>", out);

		epc = null;
	}

	public boolean accepts(ProvidedObject object) {
		Object[] o = object.getObjects();
		boolean iepcb = false;
		boolean logr = false;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof InstanceEPCBuilder) {
				iepcb = true;
			}
			if (o[i] instanceof LogReader) {
				logr = true;
			}
		}
		return iepcb && logr;
	}

	private ProcessInstance loadProcessInstance(String name) {
		// simply walk through the log and find the right instance
		Iterator logIt = log.instanceIterator();
		while (logIt.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logIt.next();

			if (name.equals(pi.getName())) {
				return pi;
			}
		}
		return null;
	}

	public String getHtmlDescription() {
		return "<p> <b>Plug-in: PPM instances export</b>"
				+ "<p>This Plug-in allows the user export a collection of instance EPCs to "
				+ "<a href=\"http://www2.ids-scheer.com/international/english/products/49643\">Aris PPM</a>  "
				+ "for analysis purposes."
				+ "<p> Each case is exported as an instance EPC, i.e. an a-cyclic EPC without choices."
				+ "<p> For more information about the way that instances are built, consider "
				+ org.processmining.framework.util.Constants.get_BVD_URLString(
						"making_instance_nets", "this page") + ". ";
	}
}
