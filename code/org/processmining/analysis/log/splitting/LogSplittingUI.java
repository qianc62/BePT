package org.processmining.analysis.log.splitting;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.AuditTrailEntryListImpl;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.log.rfb.ProcessInstanceImpl;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.ToolTipComboBox;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.LogException;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

public class LogSplittingUI extends JPanel implements Provider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LogReader log;
	private JPanel eventSelectionPanel;
	private ToolTipComboBox conceptsToolTipComboBox;
	private ToolTipComboBox ontologiesToolTipComboBox;
	private JCheckBox superConcepts;
	private JCheckBox subConcepts;
	private LogReader providedLog;
	private JSpinner timeIntervalSpinner;
	private SpinnerNumberModel timeIntervalSpinnerNumberModel;
	private JCheckBox considerTimeWhenFilteringCheckBox;
	private JRadioButton ontologyButton;
	private JRadioButton labelButton;
	private JPanel ontologyOptionsSubPanel;
	private JPanel labelOptionsSubPanel;
	private ToolTipComboBox labelsToolTipComboBox;
	private OntologyCollection ontologyCollection;

	public LogSplittingUI(LogReader log) {
		this.log = log;
		this.ontologyCollection = new OntologyCollection(log.getLogSummary());
		this.providedLog = log;
		try {
			jbInit();
			considerTimeWhenFilteringCheckBox.doClick();
			ontologyButton.doClick();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() {

		// creating the necessary panels for the time options
		considerTimeWhenFilteringCheckBox = new JCheckBox(
				"Use Time Interval during Log Splitting");
		JLabel timeInterval = new JLabel(" Time Interval (in seconds) ");
		timeIntervalSpinnerNumberModel = new SpinnerNumberModel(100.0, 0.0,
				Integer.MAX_VALUE, 10.0);
		timeIntervalSpinner = new JSpinner(timeIntervalSpinnerNumberModel);

		JPanel timeIntervalSetupSubPanel = new JPanel();
		timeIntervalSetupSubPanel.add(timeInterval);
		timeIntervalSetupSubPanel.add(timeIntervalSpinner);

		JPanel timeOptionsSetupPanel = new JPanel();
		timeOptionsSetupPanel.setBorder(new TitledBorder("Time Options"));
		timeOptionsSetupPanel.add(considerTimeWhenFilteringCheckBox);
		timeOptionsSetupPanel.add(timeIntervalSetupSubPanel);

		considerTimeWhenFilteringCheckBox
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						timeIntervalSpinner
								.setEnabled(considerTimeWhenFilteringCheckBox
										.isSelected());
					}
				});

		// creating event selection panel
		// users can split the log based on a given event label or based
		// on concepts to which events link to
		eventSelectionPanel = new JPanel();
		eventSelectionPanel.setBorder(new TitledBorder(
				"Event Selection Options"));

		// creating tool tip for event labels
		String[] labels = log.getLogSummary().getModelElements();
		labelsToolTipComboBox = new ToolTipComboBox(labels);
		labelsToolTipComboBox.setSelectedIndex(0);

		// creating tool tip for ontologies
		List<OntologyModel> ontologies = log.getLogSummary().getOntologies()
				.getOntologies();
		Vector<OntologyInCombo> shortNameOntologies = new Vector<OntologyInCombo>();
		for (OntologyModel ontology : ontologies) {
			shortNameOntologies.add(new OntologyInCombo(ontology));
		}
		ontologiesToolTipComboBox = new ToolTipComboBox(shortNameOntologies);
		ontologiesToolTipComboBox.setSelectedIndex(0);

		// creating tool tip for concepts
		conceptsToolTipComboBox = createConceptsComboBox((OntologyInCombo) ontologiesToolTipComboBox
				.getSelectedItem());

		// create check boxes for sub and super concepts
		superConcepts = new JCheckBox("Include super concepts");
		subConcepts = new JCheckBox("Include sub concepts");

		// creating the radio button to select whether to use labels or concepts
		// during the split

		labelButton = new JRadioButton("Split by label:");
		ontologyButton = new JRadioButton("Split by concept:");
		ButtonGroup labelOrConceptRadioButtonGroup = new ButtonGroup();
		labelOrConceptRadioButtonGroup.add(labelButton);
		labelOrConceptRadioButtonGroup.add(ontologyButton);

		// creating panel to group all label-related options
		labelOptionsSubPanel = new JPanel();
		labelOptionsSubPanel.add(labelButton);
		labelOptionsSubPanel.add(labelsToolTipComboBox);

		// creating panel to group all the ontology-related options
		ontologyOptionsSubPanel = new JPanel();

		// adding action listeners to both label and ontology buttons
		ontologyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ontologiesToolTipComboBox.setEnabled(ontologyButton
						.isSelected());
				conceptsToolTipComboBox.setEnabled(ontologyButton.isSelected());
				superConcepts.setEnabled(ontologyButton.isSelected());
				subConcepts.setEnabled(ontologyButton.isSelected());
				labelsToolTipComboBox.setEnabled(!ontologyButton.isSelected());
			}
		});

		labelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ontologiesToolTipComboBox.setEnabled(!labelButton.isSelected());
				conceptsToolTipComboBox.setEnabled(!labelButton.isSelected());
				superConcepts.setEnabled(!labelButton.isSelected());
				subConcepts.setEnabled(!labelButton.isSelected());
				labelsToolTipComboBox.setEnabled(labelButton.isSelected());
			}
		});

		// adding ontology and concept tool tip boxes to the ontology panel
		addElementsToEventSelectionPanel();

		// updating the concepts when user selects another ontology
		ontologiesToolTipComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conceptsToolTipComboBox = createConceptsComboBox((OntologyInCombo) ontologiesToolTipComboBox
						.getSelectedItem());
				addElementsToEventSelectionPanel();
			}
		});

		// creating panel for inputParameters
		JPanel inputParametersPanel = new JPanel(new BorderLayout());
		inputParametersPanel.add(timeOptionsSetupPanel, BorderLayout.NORTH);
		inputParametersPanel.add(eventSelectionPanel, BorderLayout.CENTER);

		// creating the "split log" button
		JPanel splitLogPanel = new JPanel();
		JButton splitLog = new JButton("Split Log");
		splitLogPanel.add(splitLog);

		splitLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					providedLog = splitLog();
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Log has been sucessfully split!",
							"Log Splitting Result",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(MainUI.getInstance(),
							"Log could not be split!", "Log Splitting Result",
							JOptionPane.ERROR_MESSAGE);
					exc.printStackTrace();
					providedLog = null;
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.add(inputParametersPanel, BorderLayout.CENTER);
		this.add(splitLogPanel, BorderLayout.SOUTH);
	}

	private void addElementsToEventSelectionPanel() {

		ontologyOptionsSubPanel.removeAll();
		ontologyOptionsSubPanel.add(ontologyButton);
		ontologyOptionsSubPanel.add(ontologiesToolTipComboBox);
		ontologyOptionsSubPanel.add(conceptsToolTipComboBox);
		ontologyOptionsSubPanel.add(superConcepts);
		ontologyOptionsSubPanel.add(subConcepts);

		eventSelectionPanel.removeAll();
		eventSelectionPanel.setLayout(new BorderLayout());
		eventSelectionPanel.add(labelOptionsSubPanel, BorderLayout.NORTH);
		eventSelectionPanel.add(ontologyOptionsSubPanel, BorderLayout.CENTER);
		eventSelectionPanel.repaint();
		eventSelectionPanel.validate();
	}

	private LogReader splitLog() throws IOException {

		LogReader splittedLog = null;

		// creating the new log
		File outputFile = File.createTempFile("LogSplittingTemp", ".mxml.gz");
		FileOutputStream output = new FileOutputStream(outputFile);
		BufferedOutputStream out = new BufferedOutputStream(
				new GZIPOutputStream(output));
		LogPersistencyStream persistency = new LogPersistencyStream(out, false);

		Message.add(this.getName() + ">>>   Starting to split the log...");

		// initializing the log
		Process process = log.getProcess(0);
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
		try {
			persistency.startSAMXMLLogfile(name, description, source);

			// retrieving the processes in the log (start)
			for (int processIndex = 0; processIndex < log.numberOfProcesses(); processIndex++) {
				// writing the process to the log (start)
				process = log.getProcess(processIndex);
				persistency.startProcess(process.getName(), process
						.getDescription(), toListURI(process
						.getModelReferences()), process.getAttributes());
				// writing the process instances for this process (start)
				for (int processInstanceIndex = 0; processInstanceIndex < process
						.size(); processInstanceIndex++) {

					ProcessInstance originalPI = process
							.getInstance(processInstanceIndex);

					Message.add(this.getName()
							+ ">>>   Splitting process instance number "
							+ (processInstanceIndex + 1) + "  out of "
							+ process.size() + "...");

					// splitting the original process instances based on the
					// input parameters (start)
					List<ProcessInstance> splittedPI = splitProcessInstance(originalPI);
					for (ProcessInstance newPI : splittedPI) {
						persistency.startProcessInstance(newPI.getName(),
								(newPI.getDescription() == null ? "" : newPI
										.getDescription()), toListURI(newPI
										.getModelReferences()), newPI
										.getAttributes());
						for (int indexATE = 0; indexATE < newPI
								.getAuditTrailEntryList().size(); indexATE++) {
							persistency
									.addAuditTrailEntry(promATE2mxmlibATE(newPI
											.getAuditTrailEntryList().get(
													indexATE)));
						}
						persistency.endProcessInstance();
					}
					// splitting the original process instances based on the
					// input parameters (end)
				}
				// writing the process instances for this process (end)
				persistency.endProcess();
				// writing the process to the log (end)
			}
			// retrieving the processes in the log (end)

			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			splittedLog = BufferedLogReader.createInstance(
					new DefaultLogFilter(DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (LogException exc) {
			exc.printStackTrace();
			outputFile.deleteOnExit();
			splittedLog = null;

		} catch (Exception exc) {
			exc.printStackTrace();
			splittedLog = null;
		}

		return splittedLog;
	}

	private org.processmining.lib.mxml.AuditTrailEntry promATE2mxmlibATE(
			AuditTrailEntry promATE) {

		org.processmining.lib.mxml.AuditTrailEntry mxmlibATE = new org.processmining.lib.mxml.AuditTrailEntry();

		// converting the workflow model element and its semantic references
		mxmlibATE.setWorkflowModelElement(promATE.getElement());
		mxmlibATE.setWorkflowModelElementModelReferences(toListURI(promATE
				.getElementModelReferences()));

		// converting the event type element and its semantic references
		mxmlibATE.setEventType(EventType.getType(promATE.getType()));
		mxmlibATE.setEventTypeModelReferences(toListURI(promATE
				.getTypeModelReferences()));

		// converting the originator element and its semantic references
		mxmlibATE.setOriginator(promATE.getOriginator());
		mxmlibATE.setOriginatorModelReferences(toListURI(promATE
				.getOriginatorModelReferences()));

		if (promATE.getTimestamp() != null) {
			mxmlibATE.setTimestamp(promATE.getTimestamp());
		}
		mxmlibATE.setAttributes(promATE.getAttributes());

		return mxmlibATE;
	}

	private AuditTrailEntryList reduceATEBasedOnTimeInterval(
			AuditTrailEntryList ateList) {

		try {
			long timeInterval = timeIntervalSpinnerNumberModel.getNumber()
					.longValue() * 1000; // because the time is in seconds
			long timeCurrentATE = ateList.get(ateList.size() - 1)
					.getTimestamp().getTime();
			long timeFirstATE = 0;
			try {
				timeFirstATE = ateList.get(0).getTimestamp().getTime();
			} catch (NullPointerException e) {
				timeFirstATE = 0;
			}
			while ((timeCurrentATE - timeFirstATE) > timeInterval) {
				ateList.remove(0);
				timeFirstATE = ateList.get(0).getTimestamp().getTime();
			}
		} catch (IOException ioe) {
			// couldn't check for the time
		}

		return ateList;

	}

	private List<ProcessInstance> splitProcessInstance(
			ProcessInstance originalPI) {

		List<ProcessInstance> resultingSplittedPIs = new LinkedList<ProcessInstance>();
		Set<ConceptModel> setOfSplitConcepts = new TreeSet<ConceptModel>();

		// set-up when filtering is based on concepts/ontologies
		if (ontologyButton.isSelected()) {
			ConceptModel selectedConcept = ((ConceptInCombo) conceptsToolTipComboBox
					.getSelectedItem()).get();
			// identifying the concepts to be used as split points
			setOfSplitConcepts.add(selectedConcept);
			if (subConcepts.isSelected()) {
				// also include the subconcepts
				setOfSplitConcepts.addAll(selectedConcept.getOntology()
						.getSubConcepts(selectedConcept));
			}
			if (superConcepts.isSelected()) {
				// also include the superconcepts
				setOfSplitConcepts.addAll(selectedConcept.getOntology()
						.getSuperConcepts(selectedConcept));
			}
		}

		// performing actual process instance splitting
		int indexNewPI = 0;
		AuditTrailEntryList newATElist = null;
		try {
			newATElist = new AuditTrailEntryListImpl();
			boolean foundAteWithDesiredConcept = false;
			long timeAteWithDesiredConcept = 0;
			for (AuditTrailEntry ate : originalPI.getListOfATEs()) {
				if (!foundAteWithDesiredConcept) {
					newATElist.append(ate);
				} else {
					if (ate.getTimestamp() != null
							&& ate.getTimestamp().getTime() == timeAteWithDesiredConcept) {
						newATElist.append(ate);
					} else {
						// new ATE does not have the same time of current one
						// create process instance and restart the variable
						// newATElist
						foundAteWithDesiredConcept = false;

						if (considerTimeWhenFilteringCheckBox.isSelected()) {
							newATElist = reduceATEBasedOnTimeInterval(newATElist);
						}

						ProcessInstance newProcessInstance = new ProcessInstanceImpl(
								originalPI.getProcess(), newATElist, originalPI
										.getModelReferences());
						newProcessInstance.setName(originalPI.getName().trim()
								+ "_" + indexNewPI++);
						resultingSplittedPIs.add(newProcessInstance);
						try {
							newATElist = new AuditTrailEntryListImpl();
							newATElist.append(ate);
						} catch (IOException exc) {
							exc.printStackTrace();
							// Couldn't create a new AuditTrailEntryListImpl
							break;
						}
					}
				}

				if ((labelButton.isSelected() && (((String) labelsToolTipComboBox
						.getSelectedItem()).compareTo(ate.getElement()) == 0))
						|| (ontologyButton.isSelected() && containsConcept(
								setOfSplitConcepts,
								extractAllModelReferencesInsideATE(ate)))) {
					foundAteWithDesiredConcept = true;
					timeAteWithDesiredConcept = ate.getTimestamp().getTime();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Couldn't create a new AuditTrailEntryListImpl
		}

		return resultingSplittedPIs;
	}

	private List<String> extractAllModelReferencesInsideATE(AuditTrailEntry ate) {
		Set<String> allModelReferencesInATE = new TreeSet<String>();
		List<String> result = new LinkedList<String>();

		allModelReferencesInATE.addAll(ate.getModelReferences());
		allModelReferencesInATE.addAll(ate.getElementModelReferences());
		allModelReferencesInATE.addAll(ate.getOriginatorModelReferences());
		allModelReferencesInATE.addAll(ate.getTypeModelReferences());

		result.addAll(allModelReferencesInATE);
		return result;
	}

	private List<URI> toListURI(List<String> modelReferences) {
		LinkedList<URI> list = new LinkedList<URI>();
		for (String modelReference : modelReferences) {
			try {
				list.add(new URI(modelReference));
			} catch (URISyntaxException e) {
				e.printStackTrace();

			}
		}
		return list;
	}

	private boolean containsConcept(Set<ConceptModel> listOfConcepts,
			List<String> ateModelReferences) {

		for (String modelReference : ateModelReferences) {
			for (ConceptModel concept : listOfConcepts) {
				ConceptModel modelReferenceMappedToLoadedOntology = ontologyCollection
						.findConceptByUriInLog(modelReference);
				if (modelReferenceMappedToLoadedOntology != null) {
					modelReference = modelReferenceMappedToLoadedOntology
							.getName();
				}
				if (modelReference.compareTo(concept.getName()) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	private ToolTipComboBox createConceptsComboBox(
			OntologyInCombo selectedOntology) {
		Collection<ConceptModel> conceptsForThisOntology = selectedOntology
				.get().getConcepts();
		Vector<ConceptInCombo> shortNameConcepts = new Vector<ConceptInCombo>();
		for (ConceptModel concept : conceptsForThisOntology) {
			shortNameConcepts.add(new ConceptInCombo(concept));
		}
		return new ToolTipComboBox(shortNameConcepts);
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[] { new ProvidedObject(
				"Splitted Log", new Object[] { providedLog }) };
		return objects;
	}

}

class OntologyInCombo {
	private OntologyModel ontology;

	public OntologyInCombo(OntologyModel ontology) {
		this.ontology = ontology;
	}

	public OntologyModel get() {
		return ontology;
	}

	public String toString() {
		return ontology.getShortName();
	}
}

class ConceptInCombo {
	private ConceptModel concept;

	public ConceptInCombo(ConceptModel concept) {
		this.concept = concept;
	}

	public ConceptModel get() {
		return concept;
	}

	public String toString() {
		return concept.getShortName();
	}
}
