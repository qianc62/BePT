package org.processmining.exporting.ontologies;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.deckfour.slickerbox.components.HeaderBar;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ontology.OntologyCollection;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;

public class SaveOntologiesDialog extends JDialog {

	private static final long serialVersionUID = 6273971280201582514L;

	private boolean ok;
	private Map<OntologyModel, JTextField> textFields;
	private JFileChooser fc = new JFileChooser();
	private LogReader log;
	private HashMap<String, String> mapping;

	public SaveOntologiesDialog(LogReader log, String filename) {
		super(MainUI.getInstance(), "Save ontologies", true);
		this.log = log;

		init(log.getLogSummary().getOntologies(), filename);
		pack();
		CenterOnScreen.center(this);
	}

	public boolean showDialog() {
		ok = false;
		setVisible(true);

		if (ok) {
			save();
		}
		return ok;
	}

	private boolean save() {
		List<String> errors = new ArrayList<String>();

		mapping = new HashMap<String, String>();

		for (Map.Entry<OntologyModel, JTextField> item : textFields.entrySet()) {
			try {
				URI uri = new URI(item.getValue().getText());
				if (!uri.getScheme().equals("file")) {
					errors.add("URI must point to a file: "
							+ item.getValue().getText());
				}
				mapping.put(item.getKey().getUriInLog(), uri.toString());
			} catch (URISyntaxException e) {
				errors.add("Invalid URI: " + item.getValue().getText());
			}
		}
		if (errors.isEmpty()) {
			for (Map.Entry<OntologyModel, JTextField> item : textFields
					.entrySet()) {
				try {
					OutputStream out = new BufferedOutputStream(
							new FileOutputStream(new File(new URI(item
									.getValue().getText()))));
					String contents = item.getKey().serialize();
					out.write(contents.getBytes());
					out.close();
				} catch (FileNotFoundException e) {
					errors.add("Could not open file: "
							+ item.getValue().getText());
				} catch (URISyntaxException e) {
					errors.add("Invalid URI: " + item.getValue().getText()
							+ ": " + e.getMessage());
				} catch (IOException e) {
					errors.add(item.getValue().getText() + ": "
							+ e.getMessage());
				}
			}
		}
		if (errors.isEmpty()) {
			try {
				updateModelReferences();
			} catch (IOException e) {
				errors.add("Error while updating model references in the log: "
						+ e.getMessage());
			}
		}

		if (!errors.isEmpty()) {
			StringBuffer message = new StringBuffer(
					"One or more errors occurred, not all ontologies were saved:"
							+ System.getProperty("line.separator"));
			for (String error : errors) {
				message.append("- " + error
						+ System.getProperty("line.separator"));
			}
			JOptionPane.showMessageDialog(this, message,
					"Error while saving ontologies", JOptionPane.ERROR_MESSAGE);
		}

		return errors.isEmpty();
	}

	private void init(OntologyCollection ontologies, String filename) {
		JPanel ontologyListPanel = new JPanel(new GridBagLayout());
		int i = 0;

		textFields = new HashMap<OntologyModel, JTextField>();
		for (OntologyModel ontology : ontologies.getOntologies()) {
			if (ontology.isChanged()) {
				JPanel labelPanel = new JPanel();
				JPanel textPanel = new JPanel();

				String initialFilename = ontology.getUriInLog();
				if (filename != null) {
					File file = new File(new File(filename).getParent(),
							ontology.getShortName() + ".wsml");
					initialFilename = file.toURI().toString();
				}

				JLabel label = new JLabel(ontology.getName());
				labelPanel.add(label);

				final JTextField text = new JTextField(initialFilename, 50);
				textFields.put(ontology, text);

				JButton browse = new JButton("...");
				browse.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						if (fc.showOpenDialog(SaveOntologiesDialog.this) == JFileChooser.APPROVE_OPTION) {
							File file = fc.getSelectedFile();
							String filename = file.toURI().toString();
							text.setText(filename);
						}
					}
				});
				textPanel.add(text);
				textPanel.add(browse);

				ontologyListPanel.add(labelPanel, new GridBagConstraints(0,
						i * 2, 1, 1, 1.0, 1.0, GridBagConstraints.LINE_START,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				ontologyListPanel.add(textPanel, new GridBagConstraints(0,
						i * 2 + 1, 1, 1, 1.0, 1.0,
						GridBagConstraints.LINE_START,
						GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
						0, 0));
				i++;
			}
		}

		HeaderBar header = new HeaderBar("Save changed or new ontologies");
		header.setHeight(40);

		JButton okButton = new JButton("   Ok   ");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (save()) {
					ok = true;
					setVisible(false);
				}
			}
		});
		JButton cancel = new JButton("Cancel export");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		JButton setAllDirsButton = new JButton(
				"Choose a directory for all ontologies at once...");
		setAllDirsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAllDirectories();
			}
		});

		JPanel okCancel = new JPanel();
		okCancel.add(okButton);
		okCancel.add(cancel);

		JPanel buttons = new JPanel(new BorderLayout());
		JPanel setAllDirs = new JPanel();
		setAllDirs.add(setAllDirsButton);
		buttons.add(setAllDirs, BorderLayout.WEST);
		buttons.add(new JPanel(), BorderLayout.CENTER);
		buttons.add(okCancel, BorderLayout.EAST);

		this.setLayout(new BorderLayout());
		this.add(header, BorderLayout.NORTH);
		this.add(ontologyListPanel, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
	}

	protected void updateModelReferences() throws IOException {
		for (ProcessInstance pi : log.getInstances()) {
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();

			for (int index = 0; index < ates.size(); index++) {
				AuditTrailEntry ate = ates.get(index);

				translate(ate.getDataAttributes());
				ate.setElementModelReferences(translate(ate
						.getElementModelReferences()));
				ate.setTypeModelReferences(translate(ate
						.getTypeModelReferences()));
				ate.setOriginatorModelReferences(translate(ate
						.getOriginatorModelReferences()));

				// we need to replace the ATE so the BufferedLogReader knows it
				// has changed
				pi.getAuditTrailEntryList().replace(ate, index);
			}
			translate(pi.getDataAttributes());
			pi.setModelReferences(translate(pi.getModelReferences()));
		}
		for (int index = 0; index < log.numberOfProcesses(); index++) {
			translate(log.getProcess(index).getDataAttributes());
			log.getProcess(index).setModelReferences(
					translate(log.getProcess(index).getModelReferences()));
		}
		translate(log.getLogSummary().getWorkflowLog().getData());
		log.getLogSummary().getWorkflowLog().setModelReferences(
				translate(log.getLogSummary().getWorkflowLog()
						.getModelReferences()));

		translate(log.getLogSummary().getSource().getData());
		log.getLogSummary().getSource()
				.setModelReferences(
						translate(log.getLogSummary().getSource()
								.getModelReferences()));
	}

	private void translate(DataSection dataAttributes) {
		for (String name : dataAttributes.keySet()) {
			dataAttributes.setModelReferences(name, translate(dataAttributes
					.getModelReferences(name)));
		}
	}

	private List<String> translate(List<String> modelReferences) {
		Set<String> result = new HashSet<String>(modelReferences.size());

		for (String uri : modelReferences) {
			String ontology = OntologyModel.getOntologyPart(uri);
			String concept = OntologyModel.getConceptPart(uri);
			String translated = mapping.get(ontology);

			if (translated == null) {
				translated = ontology;
			}
			if (translated.length() > 0) {
				result.add(translated + OntologyModel.ONTOLOGY_SEPARATOR
						+ concept);
			}
		}
		List<String> asList = new ArrayList<String>();
		asList.addAll(result);
		return asList;
	}

	protected void setAllDirectories() {
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			for (Map.Entry<OntologyModel, JTextField> item : textFields
					.entrySet()) {
				File file = new File(fc.getSelectedFile(), item.getKey()
						.getShortName()
						+ ".wsml");
				item.getValue().setText(file.toURI().toString());
			}
		}
	}
}
