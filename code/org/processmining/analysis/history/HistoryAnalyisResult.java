package org.processmining.analysis.history;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;

/**
 * This result frame constitutes only a temporary solution and mainly serves as
 * a provider object for the history-enhanced execution log, which can be either
 * exported or directly used as input for further, history-based analysis.
 * 
 * @author Anne Rozinat
 */
public class HistoryAnalyisResult extends JPanel implements Provider {

	final HistoryAnalysisPlugin myAlgorithm;
	final AnalysisInputItem[] myInput;
	final LogReader myLog;

	private JButton jbHelp = null;

	public HistoryAnalyisResult(HistoryAnalysisPlugin algorithm,
			AnalysisInputItem[] input, LogReader log) {
		myAlgorithm = algorithm;
		myInput = input;
		myLog = log;

		// build GUI
		jbInit();
	}

	/**
	 * Build the Conformance Analysis Settings GUI, that is to let people
	 * specify which kind of conformance analysis they want.
	 */
	private void jbInit() {
		// this.setLayout(new BorderLayout());
		this
				.add(new JLabel(
						"<html><br>The given execution log has been enhanced with counting measures for each log event in the "
								+ "data section of the audit trail entry. <br>"
								+ "You can find possible analysis actions for the history-enhanced log in the menu.</html>"));
		jbHelp = new JButton("Help");
		this.add(jbHelp);
		jbHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
	}

	/**
	 * Specifies provided objects of the analysis that can be further used to,
	 * e.g., export an item.
	 * 
	 * @return the provided objects offered by the plugin that is currently
	 *         visible
	 */
	public ProvidedObject[] getProvidedObjects() {
		try {
			ArrayList<ProvidedObject> objects = new ArrayList<ProvidedObject>();
			// offer input log reader
			objects.add(new ProvidedObject("History-enhanced Log",
					new Object[] { myLog }));
			int numberOfProvidedObjects = objects.size();
			Iterator<ProvidedObject> it = objects.iterator();
			ProvidedObject[] result = new ProvidedObject[numberOfProvidedObjects];
			for (int i = 0; i < numberOfProvidedObjects; i++) {
				result[i] = it.next();
			}
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
