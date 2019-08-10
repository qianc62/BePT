package org.processmining.mining.armining;

import weka.associations.Associator;
import javax.swing.JPanel;
import org.processmining.framework.log.LogReader;
import weka.core.Attribute;
import java.util.Map;
import java.util.HashMap;
import org.processmining.framework.log.ProcessInstance;
import weka.core.Instances;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogEvent;
import java.util.Iterator;
import weka.core.Instance;
import weka.core.FastVector;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.BorderLayout;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.AuditTrailEntry;
import java.io.IOException;
import java.util.ArrayList;
import org.processmining.framework.util.GUIPropertyBoolean;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.io.FileWriter;
import java.io.BufferedWriter;
import javax.swing.JOptionPane;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.log.rfb.AuditTrailEntryImpl;

/**
 * <p>
 * Title: AssociationAnalyzer
 * </p>
 * 
 * <p>
 * Description: Abstract class providing methods implemented for Apriori and
 * Pred. Apriori algorithms. This class also contains methods for creating input
 * (learning instances) in Weka format.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Shaifali Gupta (s.gupta@student.tue.nl)
 * @version 1.0
 * 
 */
public abstract class AssociationAnalyzer {

	protected LogReader theLog;
	// Weka related variables
	protected Associator myAssociator;
	protected FastVector attributeInfo;
	protected Instances data;
	protected Instance instance;

	protected GUIPropertyBoolean saveAsARFFChecked = new GUIPropertyBoolean(
			"Save intermediate input ARFF file",
			"If enabled the intermediate input ARFF file is saved to the specified location",
			false);
	protected JTextField locationARFFFile = new JTextField("");
	protected JButton saveAsARFFBrowseButton = new JButton();

	/**
	 * @returns the name of this algorithm to be displayed in Combo box
	 */
	public abstract String toString();

	/**
	 * @return the description of this algorithm
	 */
	public abstract String getDescription();

	// Reset the type of associator when the 'Start Mining' button is
	// re-pressed.
	public abstract void resetAssociator();

	/**
	 * Initializes data mining associator to be used for association analysis.
	 */
	protected abstract void initAssociator();

	/**
	 * Creates a GUI panel containing the parameters that are available for this
	 * type of association rule analyzer (i.e., the used algorithm).
	 * 
	 * @return the parameters panel to be displayed in the algorithm settings of
	 *         the association rule miner
	 */
	public abstract JPanel getParametersPanel();

	private void jbInit() throws Exception {
	}

	public abstract void applyOptionalParameters();

	public abstract ArrayList<String> getRules();

	public abstract ArrayList<String> showFreqItemSets();

	public abstract boolean getFreqItemSets();

	public abstract boolean getSaveARFFValue();

	public abstract String getlocationARFFFile();

	public abstract double getConfValue();

	public abstract double getUpperBoundMinSup();

	public abstract double getLowerBoundMinSup();

	public abstract boolean getETypeValue();

	public abstract boolean getNoNameActivity();

	public abstract boolean check(ProcessInstance pi, int ruleIndex);

	public abstract boolean checkWithEC(ProcessInstance pi, int ruleIndex);

	public abstract boolean checkFIS(ProcessInstance pi, int ruleIndex);

	public abstract boolean checkFISWithEC(ProcessInstance pi, int ruleIndex);

	/**
	 * Initializes data mining analyzer to be used for association analysis.
	 */
	public AssociationAnalyzer() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// Default option-
	// Event type information is not retained and no dummy activity is inserted

	public void createInputForWeka(LogReader log) throws IOException,
			IndexOutOfBoundsException {
		// remember all the attributes in list
		HashMap<String, Attribute> allAttributesInLog = new HashMap<String, Attribute>();

		// Retrieve all the different types of log events that have
		// occurred--------
		LogEvents allEventsInLog = log.getLogSummary().getLogEvents();
		FastVector attributeInfo = new FastVector();
		for (int i = 0; i < allEventsInLog.size(); i++) {
			LogEvent event = allEventsInLog.getEvent(i);
			// check if dummy activity is present in the log, if yes remove it.
			if ((event.getModelElementName() == "noname")
					|| (event.getEventType() == "noType")) {
				allEventsInLog.remove(event);
				continue;
			}

			// specify the attribute (values and name). Here we define the
			// attribute for Weka.
			// In case we use two values-yes and no, we decomment the following
			// two statements, and remove the next one.
			// FastVector myAttributeValues = new FastVector(2);
			// myAttributeValues.addElement("no");
			FastVector myAttributeValues = new FastVector(1);
			myAttributeValues.addElement("yes");
			String newActivityPlusType = (String) event.getModelElementName()
					.toString();

			// Define an attribute which is of type WekaAttribute.
			Attribute wekaAtt = new Attribute(newActivityPlusType,
					myAttributeValues);
			// Add this attribute to the learning problem
			attributeInfo.addElement(wekaAtt);
			allAttributesInLog.put(newActivityPlusType, wekaAtt);
		}
		// create an empty data set with attribute information
		String currentFileName = log.getFile().getShortName();
		data = new Instances(currentFileName, attributeInfo, 0);

		// Walk through the log and build learning instances
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = it.next();
			Instance instance = new Instance(attributeInfo.size());
			instance.setDataset(data);

			// initialize all attribute values for current learning instance
			// with value no
			Iterator<Map.Entry<String, Attribute>> allAtt = allAttributesInLog
					.entrySet().iterator();
			AuditTrailEntryList allAteInThisTrace = pi.getAuditTrailEntryList();
			outer: for (int i = 0; i < allAteInThisTrace.size(); i++) {
				AuditTrailEntry individualAte = allAteInThisTrace.get(i);
				// Check if dummy activity is there in a PI, remove it.
				if ((individualAte.getName() == "noname")
						|| (individualAte.getType() == "noType")) {
					allAteInThisTrace.remove(i);
					continue outer;
				}

				String nameAndType = (String) individualAte.getElement()
						.toString();
				allAtt = allAttributesInLog.entrySet().iterator();
				while (allAtt.hasNext()) {
					Map.Entry currentEntry = allAtt.next();
					String namePlusType = (String) currentEntry.getKey();
					Attribute currentAtt = (Attribute) currentEntry.getValue();
					if (nameAndType.equals(namePlusType)) {
						instance.setValue(currentAtt, "yes");
					}
				}
			}
			data.add(instance);
		}
		try {
			if (saveAsARFFChecked.getValue() != false
					&& locationARFFFile.getText() != "") {
				String myPath = locationARFFFile.getText();
				try {
					BufferedWriter outW = new BufferedWriter(new FileWriter(
							myPath));
					outW.write(data.toString());
					outW.flush();
					outW.close();
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Intermediate input ARFF file has been saved",
							"ARFF File saved", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	// Event type information is not retained and a dummy activity is inserted
	// in the log
	public void createInputForWeka3(LogReader log) throws IOException,
			IndexOutOfBoundsException {
		HashMap<String, Attribute> allAttributesInLog = new HashMap<String, Attribute>();
		LogEvent noName = new LogEvent("noname", "noType");
		LogEvents allEventsInLog = log.getLogSummary().getLogEvents();
		if (allEventsInLog.contains(noName)) {

		} else {
			allEventsInLog.add(noName);
		}
		FastVector attributeInfo = new FastVector();
		for (int i = 0; i < allEventsInLog.size(); i++) {
			LogEvent event = allEventsInLog.getEvent(i);

			FastVector myAttributeValues = new FastVector(1);
			myAttributeValues.addElement("yes");
			String newActivityPlusType = (String) event.getModelElementName()
					.toString();
			Attribute wekaAtt = new Attribute(newActivityPlusType,
					myAttributeValues);
			attributeInfo.addElement(wekaAtt);
			allAttributesInLog.put(newActivityPlusType, wekaAtt);
		}
		String currentFileName = log.getFile().getShortName();
		data = new Instances(currentFileName, attributeInfo, 0);

		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = it.next();
			Instance instance = new Instance(attributeInfo.size());
			instance.setDataset(data);

			Iterator<Map.Entry<String, Attribute>> allAtt = allAttributesInLog
					.entrySet().iterator();
			AuditTrailEntry dummy = new AuditTrailEntryImpl();
			dummy.setElement("noname");
			dummy.setType("notype");

			AuditTrailEntryList allAteInThisTrace = pi.getAuditTrailEntryList();
			allAteInThisTrace.append(dummy);

			for (int i = 0; i < allAteInThisTrace.size(); i++) {
				AuditTrailEntry individualAte = allAteInThisTrace.get(i);
				String nameAndType = (String) individualAte.getElement()
						.toString();
				allAtt = allAttributesInLog.entrySet().iterator();
				while (allAtt.hasNext()) {
					Map.Entry currentEntry = allAtt.next();
					String namePlusType = (String) currentEntry.getKey();
					Attribute currentAtt = (Attribute) currentEntry.getValue();
					if (nameAndType.equals(namePlusType)) {
						instance.setValue(currentAtt, "yes");
					}
				}
			}
			data.add(instance);
		}
		try {
			if (saveAsARFFChecked.getValue() != false
					&& locationARFFFile.getText() != "") {
				String myPath = locationARFFFile.getText();
				try {
					BufferedWriter outW = new BufferedWriter(new FileWriter(
							myPath));
					outW.write(data.toString());
					outW.flush();
					outW.close();
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Intermediate input ARFF file has been saved",
							"ARFF File saved", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	// Retain the event type information and also insert a dummy activity
	public void createInputForWeka2(LogReader log) throws IOException,
			IndexOutOfBoundsException {
		HashMap<String, Attribute> allAttributesInLog = new HashMap<String, Attribute>();

		LogEvent noName = new LogEvent("noname", "noType");
		LogEvents allEventsInLog = log.getLogSummary().getLogEvents();

		if (allEventsInLog.contains(noName)) {
		} else {
			allEventsInLog.add(noName);
		}
		FastVector attributeInfo = new FastVector();

		for (int i = 0; i < allEventsInLog.size(); i++) {
			LogEvent event = allEventsInLog.getEvent(i);

			FastVector myAttributeValues = new FastVector(1);
			myAttributeValues.addElement("yes");
			String newActivityPlusType2 = (String) event.toString();
			Attribute wekaAtt = new Attribute(newActivityPlusType2,
					myAttributeValues);
			attributeInfo.addElement(wekaAtt);
			allAttributesInLog.put(newActivityPlusType2, wekaAtt);
		}
		String currentFileName = log.getFile().getShortName();
		data = new Instances(currentFileName, attributeInfo, 0);
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = it.next();
			Instance instance = new Instance(attributeInfo.size());
			instance.setDataset(data);
			Iterator<Map.Entry<String, Attribute>> allAtt = allAttributesInLog
					.entrySet().iterator();
			AuditTrailEntry dummy = new AuditTrailEntryImpl();
			dummy.setElement("noname");
			dummy.setType("notype");

			AuditTrailEntryList allAteInThisTrace = pi.getAuditTrailEntryList();
			allAteInThisTrace.append(dummy);
			for (int i = 0; i < allAteInThisTrace.size(); i++) {
				AuditTrailEntry individualAte = allAteInThisTrace.get(i);
				String nameAndType2 = (String) individualAte.getElement()
						+ " (" + individualAte.getType() + ")";
				allAtt = allAttributesInLog.entrySet().iterator();
				while (allAtt.hasNext()) {
					Map.Entry currentEntry = allAtt.next();
					String namePlusType = (String) currentEntry.getKey();
					Attribute currentAtt = (Attribute) currentEntry.getValue();

					if (nameAndType2.equals(namePlusType)) {
						instance.setValue(currentAtt, "yes");
					}
				}
			}
			data.add(instance);
		}
		try {
			if (saveAsARFFChecked.getValue() != false
					&& locationARFFFile.getText() != "") {
				String myPath = locationARFFFile.getText();
				try {
					BufferedWriter outW = new BufferedWriter(new FileWriter(
							myPath));
					outW.write(data.toString());
					outW.flush();
					outW.close();
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Intermediate input ARFF file has been saved",
							"ARFF File saved", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	// Retain event type information and do not insert any dummy activity
	public void createInputForWeka4(LogReader log) throws IOException,
			IndexOutOfBoundsException {
		HashMap<String, Attribute> allAttributesInLog = new HashMap<String, Attribute>();
		LogEvents allEventsInLog = log.getLogSummary().getLogEvents();
		FastVector attributeInfo = new FastVector();

		for (int i = 0; i < allEventsInLog.size(); i++) {
			LogEvent event = allEventsInLog.getEvent(i);
			if ((event.getModelElementName() == "noname")
					|| (event.getEventType() == "noType")) {
				allEventsInLog.remove(event);
				continue;
			}

			FastVector myAttributeValues = new FastVector(1);
			myAttributeValues.addElement("yes");
			String newActivityPlusType2 = (String) event.toString();

			Attribute wekaAtt = new Attribute(newActivityPlusType2,
					myAttributeValues);
			attributeInfo.addElement(wekaAtt);
			allAttributesInLog.put(newActivityPlusType2, wekaAtt);
		}
		String currentFileName = log.getFile().getShortName();
		data = new Instances(currentFileName, attributeInfo, 0);
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			ProcessInstance pi = it.next();
			Instance instance = new Instance(attributeInfo.size());
			instance.setDataset(data);
			Iterator<Map.Entry<String, Attribute>> allAtt = allAttributesInLog
					.entrySet().iterator();
			AuditTrailEntryList allAteInThisTrace = pi.getAuditTrailEntryList();
			outer: for (int i = 0; i < allAteInThisTrace.size(); i++) {
				AuditTrailEntry individualAte = allAteInThisTrace.get(i);
				if ((individualAte.getName() == "noname")
						|| (individualAte.getType() == "noType")) {
					allAteInThisTrace.remove(i);
					continue outer;
				}
				String nameAndType2 = (String) individualAte.getElement()
						+ " (" + individualAte.getType() + ")";
				allAtt = allAttributesInLog.entrySet().iterator();
				while (allAtt.hasNext()) {
					Map.Entry currentEntry = allAtt.next();
					String namePlusType = (String) currentEntry.getKey();
					Attribute currentAtt = (Attribute) currentEntry.getValue();
					if (nameAndType2.equals(namePlusType)) {
						instance.setValue(currentAtt, "yes");
					}
				}
			}
			data.add(instance);
		}
		try {
			if (saveAsARFFChecked.getValue() != false
					&& locationARFFFile.getText() != "") {
				String myPath = locationARFFFile.getText();
				try {
					BufferedWriter outW = new BufferedWriter(new FileWriter(
							myPath));
					outW.write(data.toString());
					outW.flush();
					outW.close();
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Intermediate input ARFF file has been saved",
							"ARFF File saved", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	// abstract method myBuildAssociations(), this will call buildAssociations()
	// method which is the most important method from Weka.
	// This weka method executes the real algorithm, and generates rules as well
	// as FIS.
	public abstract void myBuildAssociations();

	/**
	 * Method creating an empty panel containing the given feedback message for
	 * the user.
	 * 
	 * @param message
	 *            the message to be displayed for the user
	 * @return the panel to be displayed as analysis result for the current
	 *         decision point
	 */
	public static JPanel createMessagePanel(String message) {
		JPanel messagePanel = new JPanel(new BorderLayout());
		JLabel messageLabel = new JLabel("     " + message + ".");
		messageLabel.setForeground(new Color(100, 100, 100));
		messagePanel.add(messageLabel, BorderLayout.CENTER);
		return messagePanel;
	}

	public abstract boolean isCheckBoxDummySelected();

	public abstract boolean isCheckBoxECSelected();
}
