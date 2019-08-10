package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.EncodeProcessInstanceToCharStream;
import org.processmining.framework.util.MultiPassExactTandemRepeats;
import org.w3c.dom.Node;

/**
 * This filter identifies presence of loops and replaces the loop construct with
 * just one occurrence (iteration) over the loop. The multi-pass strategy
 * iterates over the reduction process until no further loop is identified. This
 * processing caters only to simple loops and loops within loops. Complex
 * constructs such as parallelism/choice within loops need to be dealt
 * differently
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */

public class ExactTandemRepeatsFilter extends LogFilter {
	int encodingLength;

	public ExactTandemRepeatsFilter() {
		super(LogFilter.MODERATE, "ExactTandemRepeats Filter");
	}

	protected String getHelpForThisLogFilter() {
		return "Identifies sequence of AuditTrailEntries (of any length) that constitute a simple loop and keeps only one iteration of the loop";
	}

	/**
	 * Filters a single process instance.
	 * 
	 * @param instance
	 *            the process instance to filter
	 * @return true if the whole process instance passes the filter, false if
	 *         the process instance should be discarded.
	 */
	protected boolean doFiltering(ProcessInstance instance) {

		assert (!instance.isEmpty());

		AuditTrailEntryList entries = instance.getAuditTrailEntryList();
		if (entries.size() <= 1) {
			return true;
		}

		EncodeProcessInstanceToCharStream encodePI = new EncodeProcessInstanceToCharStream(
				instance);
		String charStream = encodePI.getCharStream();
		encodingLength = encodePI.getEncodingLength();
		int actualLength = charStream.length() / encodingLength;

		// System.out.println(instance.getName()+" @ "+encodingLength+" @
		// "+charStream);

		MultiPassExactTandemRepeats mpETR = new MultiPassExactTandemRepeats(
				charStream, encodingLength);
		String modifiedStream = mpETR.getModifiedStream();
		int modifiedLength = modifiedStream.length() / encodingLength;

		// if (actualLength != modifiedLength) {
		// System.out.println(instance.getName() + " @ " + actualLength
		// + " @ " + modifiedLength);
		// }

		boolean[] globalFlagCharStream = mpETR.getGlobalFlagCharStream();
		// System.out.println(entries.size()+" @ "+globalFlagCharStream.length);

		Iterator<AuditTrailEntry> it = entries.iterator();
		int auditTrailEntryIndex = 0;
		while (it.hasNext()) {
			it.next();
			if (!globalFlagCharStream[auditTrailEntryIndex]) {
				it.remove();
			}
			auditTrailEntryIndex++;
		}
		// System.out.println(entries.size()+" @ "+globalFlagCharStream.length+"
		// @ "+entries.size());
		return !instance.isEmpty();
	}

	@Override
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary,
				ExactTandemRepeatsFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new ExactTandemRepeatsFilter();
			}
		};
	}

	/**
	 * Method to tell whether this LogFilter changes the log or not.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method. False otherwise.
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	protected boolean thisFilterChangesLog() {
		return true;
	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {
		// do nothing
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// do nothing
	}

}
