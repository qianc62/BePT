package org.processmining.framework.log.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.w3c.dom.Node;

/**
 * This Logfilter obfuscates the Model element names of the audit trail entries
 * in the log. The generated names will be of the same character length as the
 * original names, and the same character set will be used for the generation of
 * the new names. <br>
 * It is therefore not a very secure obfuscation method, but comes in handy if
 * task names that are unreadable anyway needs to be scrambled because of
 * non-disclosure agreements in the context of a paper.
 * 
 * @author anne
 */
public class ObfuscationFilter extends LogFilter {

	/**
	 * Instantiates a LogFilter with a pointer to the lower level filter. When
	 * filtering, the <code>filter(ProcessInstance pi)</code> method should
	 * first call <code>filter.filter(pi)</code>. If that returns false, no
	 * further filtering is necessary.
	 * 
	 * @param lowLevelFilter
	 *            the filter that should be checked first, to see if the
	 *            instance can be discared anyway. Note that this filter can be
	 *            null.
	 */
	public ObfuscationFilter() {
		super(LogFilter.MODERATE, "Obfuscation filter");
	}

	protected String getHelpForThisLogFilter() {
		return "This Logfilter obfuscates the Model element names of the audit trail entries "
				+ "in the log. The generated names will be of the same character length as the "
				+ "original names, and the same character set will be used for the generation "
				+ "of the new names (according to the well-known Caesar algorithm). "
				+ "<br> "
				+ "It is therefore not a very secure obfuscation method, but comes in handy "
				+ "if task names that are unreadable anyway needs to be scrambled because of "
				+ "non-disclosure agreements in the context of a paper.";
	}

	/**
	 * Method to tell whether this LogFilter changes the log or not.
	 * 
	 * @return boolean True if this LogFilter changes the process instance in
	 *         the <code>filter()</code> method. False otherwise.
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	public boolean thisFilterChangesLog() {
		return true;
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
		// This is handled by the filter method of LogFilter and does not belong
		// here
		// if ((filter != null) && !filter.filter(instance) ||
		// instance.isEmpty()) {
		// return false;
		// }
		assert (!instance.isEmpty());

		try {

			AuditTrailEntryList entries = instance.getAuditTrailEntryList();
			int index = 0;
			AuditTrailEntry currentATE;
			AuditTrailEntry copiedATE;
			HashMap<String, String> mapping = new HashMap<String, String>();

			// TODO: make an option panel for this filter (offer to store
			// mapping information optionally there)
			// open file to store mapping
			// JFileChooser saveDialog = new JFileChooser();
			// saveDialog.setSelectedFile(new
			// File("Mapping_ObfuscationLogFilter_" + instance.getName() +
			// ".txt"));
			// if(saveDialog.showSaveDialog(MainUI.getInstance())==JFileChooser.APPROVE_OPTION)
			// {
			// File outFile = saveDialog.getSelectedFile();
			try {
				/*
				 * Caesar algorithm after
				 * http://homepages.gold.ac.uk/rachel/Caesar.java.
				 */
				int key = 22; // I have chosen 22 to be my key value but you can
				// change this to any value between 0 and 25

				// BufferedWriter outWriter = new BufferedWriter(new
				// FileWriter(outFile));
				// outWriter.write("% Ofuscation of event names by Caesar algorithm after http://homepages.gold.ac.uk/rachel/Caesar.java \n"
				// +
				// "% Offset: " + key +
				// " (can be anything between 0 and 25) \n\n");

				// walk through the process instance and perform the actual
				// filtering task
				while (index < entries.size()) {
					// get ate at current index position
					currentATE = entries.get(index);
					copiedATE = (AuditTrailEntry) currentATE.clone();
					String s = currentATE.getElement().toString();
					StringBuffer newWfme = new StringBuffer();

					for (int i = 0; i < s.length(); i++) // we look at the
					// plaintext one
					// character at a
					// time
					{
						int current = s.charAt(i); // current holds the
						// character as an int value
						// - the ASCII value
						if (current > 64 && current < 91) // if the character is
						// an upper case
						// letter
						{
							current = current + key; // add the key value onto
							// the ASCII value
							if (current > 90)
								current = current - 26; // if we've come off the
							// end of the alphabet
							// then we loop
						} // back around by subtracting 26.
						else if (current > 96 && current < 123) // if the
						// character is
						// a lower case
						// letter
						{
							current = current + key;
							if (current > 122)
								current = current - 26;
						}
						// now we print out the encrypted character or if the
						// character
						// was not a letter then we print the original
						// character.
						newWfme.append((char) current);
					}

					// change the name of the workflow model element
					copiedATE.setElement(newWfme.toString());
					// replace to make change persistent
					entries.replace(copiedATE, index);
					// move to next ate
					index++;

					// write mapping
					mapping.put(s, newWfme.toString());
				}

				// TODO: make an option panel for this filter (offer to store
				// mapping information optionally there)
				// write mapping file
				/*
				 * Iterator<String> it= mapping.keySet().iterator(); while
				 * (it.hasNext()) { String clear = it.next(); String obfuscated
				 * = mapping.get(clear); outWriter.write(clear + " --> " +
				 * obfuscated + "\n"); } // finish the writing
				 * outWriter.flush(); outWriter.close();
				 */

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// }
			// filtering successful
			return true;
		} catch (Exception ex) {
			Message.add("An error occurred during filtering process instance "
					+ instance.getName());
			ex.printStackTrace();
			// ignore the instance as an error occurred
			return false;
		}
	}

	/**
	 * Returns a Panel for the setting of parameters.
	 * 
	 * @param summary
	 *            A LogSummary to be used for setting parameters.
	 * @return JPanel
	 * @todo Implement this org.processmining.framework.log.LogFilter method
	 */
	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, ObfuscationFilter.this) {

			protected boolean getAllParametersSet() {
				return true;
			}

			protected JPanel getPanel() {
				return null;
			}

			public LogFilter getNewLogFilter() {
				return new ObfuscationFilter();
			}
		};
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {
		// do nothing!
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
		// do nothing!
	}
}
