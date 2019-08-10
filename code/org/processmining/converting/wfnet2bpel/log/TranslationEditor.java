package org.processmining.converting.wfnet2bpel.log;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.input.SAXBuilder;
import org.processmining.converting.wfnet2bpel.ManualTranslationWizard;
import org.processmining.framework.models.bpel.BPEL;
import org.processmining.framework.models.bpel.BPELProcess;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.w3c.dom.Document;

/**
 * <p>
 * Title: TranslationEditor
 * </p>
 * 
 * <p>
 * Description: A simple panel that test if the BPEL that is written in it is
 * correct.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: University of Aarhus
 * </p>
 * 
 * @author Kristian Bisgaard Lassen (<a
 *         href="mailto:K.B.Lassen@daimi.au.dk">mailto
 *         :K.B.Lassen@daimi.au.dk</a>)
 * @version 1.0
 */
public class TranslationEditor extends JPanel implements CaretListener,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2159210767467399201L;

	private JTextArea bpelTranslation;

	private final ManualTranslationWizard mtw;

	private final JPanel splitPane;

	private final JEditorPane warningMessages;

	private JButton addName, addTemplate;

	private JComboBox nameBox;

	private JComboBox templateBox;

	public TranslationEditor(ManualTranslationWizard mtw) {
		super();
		setLayout(new GridLayout(2, 1));
		this.mtw = mtw;
		splitPane = new JPanel(new GridLayout(1, 2));
		add(splitPane);
		bpelTranslation = new JTextArea();
		bpelTranslation.setLineWrap(true);
		bpelTranslation.addCaretListener(this);
		nameBox = new JComboBox();
		addName = new JButton("Add placeholder");
		addName.addActionListener(this);
		templateBox = new JComboBox(new Object[] { "Flow", "Sequence",
				"Switch", "Switch case", "Switch otherwise", "Pick",
				"Pick onMessage", "Pick onAlarm", "While" });
		addTemplate = new JButton("Add template");
		addTemplate.addActionListener(this);
		JPanel suggestionPanel = new JPanel(new GridLayout(2, 1));
		JPanel panel = new JPanel(new FlowLayout());
		panel.add(nameBox);
		panel.add(addName);
		suggestionPanel.add(panel);
		panel = new JPanel(new FlowLayout());
		panel.add(templateBox);
		panel.add(addTemplate);
		suggestionPanel.add(panel);
		panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Input translation"), BorderLayout.NORTH);
		panel.add(new JScrollPane(bpelTranslation), BorderLayout.CENTER);
		panel.add(suggestionPanel, BorderLayout.SOUTH);
		splitPane.add(panel);
		warningMessages = new JEditorPane();
		warningMessages.setContentType("text/plain");
		warningMessages.setEditable(false);
		panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Warning(s)/Error(s)"), BorderLayout.NORTH);
		panel.add(new JScrollPane(warningMessages), BorderLayout.CENTER);
		splitPane.add(panel);
		splitPane.doLayout();
	}

	public void setPetriNet(PetriNet petriNet) {
		if (nameBox.getItemCount() == 0)
			for (Transition transition : petriNet.getTransitions()) {
				nameBox.addItem(transition.getIdentifier());
			}
	}

	public void caretUpdate(CaretEvent arg0) {
		SAXBuilder builder = new SAXBuilder();
		try {
			builder.build(new StringReader(bpelTranslation.getText()));
		} catch (Exception e) {
			handleException(e);
			return;
		}
		try {
			File tmpFile = File.createTempFile("bpel4wsconvserion", "bpel");
			FileOutputStream out = new FileOutputStream(tmpFile);
			out.write(("<process>" + bpelTranslation.getText() + "</process>")
					.getBytes());
			out.close();

			// Read the BPEL file as an XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			Document doc = dbf.newDocumentBuilder().parse(tmpFile);
			tmpFile.delete();
			if (doc == null || doc.getDocumentElement() == null) {
				mtw.forward.setEnabled(false);
				return;
			}

			// Hook the BPEL object to the XML document.
			BPEL model = new BPEL(doc);
			BPELProcess process = new BPELProcess(doc.getDocumentElement());
			model.setProcess(process);
			process.hookupActivities();
			mtw.activity = process.getActivity();
			if (mtw.activity == null) {
				handleException(new Exception("Illegal BPEL"));
				return;
			}
		} catch (Exception e) {
			handleException(e);
			return;
		}
		warningMessages.setText("");
		mtw.forward.setEnabled(true);
	}

	/**
	 * @param e
	 */
	private void handleException(Exception e) {
		warningMessages.setText(e.getMessage());
		mtw.forward.setEnabled(false);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == addName) {
			bpelTranslation.insert("<invoke name=\""
					+ nameBox.getSelectedItem() + "\"/>", bpelTranslation
					.getCaretPosition());
		} else if (ae.getSource() == addTemplate) {
			if (templateBox.getSelectedItem().equals("Sequence"))
				bpelTranslation.insert("<sequence></sequence>", bpelTranslation
						.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Flow"))
				bpelTranslation.insert("<flow></flow>", bpelTranslation
						.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("While"))
				bpelTranslation.insert("<while condition=\"?\"></while>",
						bpelTranslation.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Switch"))
				bpelTranslation.insert("<switch></switch>", bpelTranslation
						.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Switch case"))
				bpelTranslation.insert("<case condition=\"?\"></case>",
						bpelTranslation.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Switch case"))
				bpelTranslation.insert("<otherwise></otherwise>",
						bpelTranslation.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Pick"))
				bpelTranslation.insert("<pick></pick>", bpelTranslation
						.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Pick onMessage"))
				bpelTranslation.insert(
						"<onMessage operation=\"?\"></onMessage>",
						bpelTranslation.getCaretPosition());
			else if (templateBox.getSelectedItem().equals("Pick onAlarm"))
				bpelTranslation.insert(
						"<onAlarm for=\"?\" until=\"?\"></onAlarm>",
						bpelTranslation.getCaretPosition());
		}
		bpelTranslation.requestFocus();
	}

}
