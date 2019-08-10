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
package org.processmining.exporting.log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.processmining.exporting.ExportPlugin;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.LogException;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class MXMLibLogExport implements ExportPlugin {

	protected enum FormatType {
		MXML, MXMLGZ
	}

	protected FormatType type;

	public MXMLibLogExport() {
		this(FormatType.MXMLGZ);
	}

	public MXMLibLogExport(FormatType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.exporting.ExportPlugin#accepts(org.processmining.framework
	 * .plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		for (Object o : object.getObjects()) {
			if (o instanceof LogReader) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.exporting.ExportPlugin#export(org.processmining.framework
	 * .plugin.ProvidedObject, java.io.OutputStream)
	 */
	public void export(ProvidedObject object, OutputStream output)
			throws IOException {
		LogReader log = null;
		for (Object o : object.getObjects()) {
			if (o instanceof LogReader) {
				log = (LogReader) o;
				break;
			}
		}
		if (log == null) {
			throw new AssertionError(
					"Provided object to export does not have a log reader!");
		}
		LogPersistencyStream persistency = null;
		if (type.equals(FormatType.MXMLGZ)) {
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(output));
			persistency = new LogPersistencyStream(out, false);
		} else if (type.equals(FormatType.MXML)) {
			BufferedOutputStream out = new BufferedOutputStream(output);
			persistency = new LogPersistencyStream(out, false);
		}
		Process process = log.getProcess(0);
		ProcessInstance instance = null;
		AuditTrailEntryList ateList = null;
		try {
			// start writing log
			String name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}
			String description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}
			String source = log.getLogSummary().getSource().getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}
			persistency.startLogfile(name, description, source);
			for (int i = 0; i < log.numberOfProcesses(); i++) {
				process = log.getProcess(i);
				name = process.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcess";
				}
				description = process.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}
				persistency.startProcess(name, description, process
						.getAttributes());
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					name = instance.getName();
					if (name == null || name.length() == 0) {
						name = "UnnamedProcessInstance";
					}
					description = instance.getDescription();
					if (description == null || description.length() == 0) {
						description = name + " exported by MXMLib @ P-stable";
					}
					ateList = instance.getAuditTrailEntryList();
					persistency.startProcessInstance(name, description,
							instance.getAttributes());
					for (int k = 0; k < ateList.size(); k++) {
						persistency
								.addAuditTrailEntry(promAte2mxmlibAte(ateList
										.get(k)));
					}
					persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
		} catch (LogException e) {
			// mishap..
			e.printStackTrace();
			throw new IOException("Error when exporting!");
		}

	}

	protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
			AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.exporting.ExportPlugin#getFileExtension()
	 */
	public String getFileExtension() {
		if (type.equals(FormatType.MXMLGZ)) {
			return "mxml.gz";
		} else if (type.equals(FormatType.MXML)) {
			return "mxml";
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		if (type.equals(FormatType.MXMLGZ)) {
			return "Exports a compressed log file using the MXMLib library";
		} else if (type.equals(FormatType.MXML)) {
			return "Exports a log file using the MXMLib library";
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		if (type.equals(FormatType.MXMLGZ)) {
			return "Efficient MXML.GZ Export (MXMLib compressed)";
		} else if (type.equals(FormatType.MXML)) {
			return "Efficient MXML Export (MXMLib plain)";
		} else {
			return null;
		}
	}

}
