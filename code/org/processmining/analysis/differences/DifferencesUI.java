package org.processmining.analysis.differences;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;

import org.processmining.analysis.differences.processdifferences.ProcessAutomaton;
import org.processmining.analysis.differences.processdifferences.ProcessDifference;
import org.processmining.analysis.differences.processdifferences.ProcessDifferences;
import org.processmining.analysis.differences.relations.Relation;
import org.processmining.analysis.differences.relations.Tuple;
import org.processmining.analysis.epc.similarity.Checker;
import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.bpmn.BpmnProcessModel;
import org.processmining.framework.ui.Message;
import org.processmining.importing.pnml.PnmlImport;
import org.processmining.mining.petrinetmining.PetriNetResult;

public class DifferencesUI extends JPanel {

	// components for the left panel
	private JPanel leftPanel;
	private JTextArea jtaResult;
	private ModelGraph provBehaviour, reqBehaviour;
	private JScrollPane leftScrollPane;
	final String userDir = System.getProperty("user.dir");
	final JLabel jlbProvTransitions = new JLabel(
			"Messages in provided behaviour");
	final JLabel jlbReqTransitions = new JLabel(
			"Equivalent messages in required behaviour   ");
	final JLabel jlbMapping = new JLabel(
			"<html>Mapping of messages:<br><br></html>");
	final JLabel jlbResult = new JLabel("<html><br>Differences found:</html>");
	final JButton jbEvaluate = new JButton("   Evaluate   ");
	final JButton jbFindMatch = new JButton("   Find Match   ");
	final JLabel jlbCutOff = new JLabel("Cutoff value:");
	final JTextField txtCutOff = new JTextField("0.90");
	private Checker checker = new Checker(false);

	private ArrayList<JComboBox> cmbList = new ArrayList<JComboBox>();
	private ArrayList<JTextField> jtfList = new ArrayList<JTextField>();
	private ArrayList<ModelGraphVertex> provTransitions = new ArrayList<ModelGraphVertex>();// contains
	// only
	// visible
	// transitions
	private ArrayList<ModelGraphVertex> reqTransitions = new ArrayList<ModelGraphVertex>();// contains
	// only
	// visible
	// transitions

	// Components for the right panel
	private JScrollPane provContainer, reqContainer2, rightScrollPane;

	private boolean debugging = false;

	public DifferencesUI(ModelGraph pBehaviour, ModelGraph rBehaviour) {
		// get Transitions from the provided behaviour
		try {
			if (pBehaviour == null) {
				PnmlImport p = new PnmlImport();
				this.provBehaviour = (PetriNet) ((PetriNetResult) p
						.importFile(new FileInputStream(
								"C:/Documents and Settings/RDIJKMAN/workspaceprom/Prom2/Marian/process2.pnml")))
						.getProvidedObjects()[0].getObjects()[0];
				debugging = true;
			} else {
				this.provBehaviour = pBehaviour;
			}
			// get Transitions from the required behaviour
			if (rBehaviour == null) {
				PnmlImport p = new PnmlImport();
				this.reqBehaviour = (PetriNet) ((PetriNetResult) p
						.importFile(new FileInputStream(
								"C:/Documents and Settings/RDIJKMAN/workspaceprom/Prom2/Marian/process1.pnml")))
						.getProvidedObjects()[0].getObjects()[0];
			} else {
				this.reqBehaviour = rBehaviour;
			}

			rightScrollPane = new JScrollPane(prepareRightPanel());
			leftScrollPane = new JScrollPane(prepareLeftPanel());
			leftScrollPane.setMinimumSize(new Dimension(480, 0));

			this.setLayout(new BorderLayout());
			JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			split.setOneTouchExpandable(true);
			split.setResizeWeight(0);
			split.setDividerLocation(480);
			split.add(leftScrollPane, JSplitPane.LEFT);
			split.add(rightScrollPane, JSplitPane.RIGHT);
			this.add(split, BorderLayout.CENTER);

			if (debugging) {
				findMatch();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JPanel prepareRightPanel() {
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
		split.setResizeWeight(0.5);

		provContainer = new JScrollPane(provBehaviour.getGrappaVisualization());
		reqContainer2 = new JScrollPane(reqBehaviour.getGrappaVisualization());
		split.add(provContainer, JSplitPane.TOP);
		split.add(reqContainer2, JSplitPane.BOTTOM);
		JPanel p = new JPanel(new BorderLayout());
		p.add(split, BorderLayout.CENTER);
		return p;
	}

	public JPanel prepareLeftPanel() {
		leftPanel = new JPanel(new BorderLayout());
		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);

		ArrayList<String> strProvTransitions = new ArrayList<String>();
		ArrayList<String> strReqTransitions = new ArrayList<String>();
		provTransitions.addAll(ProcessAutomaton.visibleTasks(provBehaviour));
		reqTransitions.addAll(ProcessAutomaton.visibleTasks(reqBehaviour));

		// get the names of the transitions in the provided behaviour
		for (int i = 0; i < provTransitions.size(); i++) {
			strProvTransitions.add(provTransitions.get(i).getIdentifier()
					.replace("\\n", " "));
		}

		// Get the names of the Transitions from the required behaviour
		strReqTransitions.add("No equivalent");
		for (int i = 0; i < reqTransitions.size(); i++) {
			strReqTransitions.add(reqTransitions.get(i).getIdentifier()
					.replace("\\n", " "));
		}

		// set captions
		jlbProvTransitions.setPreferredSize(new Dimension(
				(int) jlbProvTransitions.getPreferredSize().getWidth(),
				(int) jlbProvTransitions.getPreferredSize().getHeight()));
		jlbReqTransitions.setPreferredSize(new Dimension(
				(int) jlbReqTransitions.getPreferredSize().getWidth(),
				(int) jlbReqTransitions.getPreferredSize().getHeight()));
		JPanel pCaptions = new JPanel(new BorderLayout());
		pCaptions.add(jlbMapping, BorderLayout.NORTH);
		pCaptions.add(jlbProvTransitions, BorderLayout.WEST);
		pCaptions.add(jlbReqTransitions, BorderLayout.EAST);

		panel.add(pCaptions, new GridBagConstraints(0, 0, 1, 1, 1, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 10, 0));

		int i;
		for (i = 0; i < strProvTransitions.size(); i++) {
			// make a new label with the event name
			JTextField jtfProvTransitions = new JTextField(strProvTransitions
					.get(i));
			jtfProvTransitions.setFont(jtfProvTransitions.getFont().deriveFont(
					Font.PLAIN));
			jtfProvTransitions.setEditable(false);
			jtfProvTransitions.setPreferredSize(new Dimension(
					(int) jlbProvTransitions.getPreferredSize().getWidth(),
					(int) jtfProvTransitions.getPreferredSize().getHeight()));
			jtfList.add(jtfProvTransitions);
			// set the label in the textfield according the the object in the
			// log
			JComboBox jcmbReqTransitions = new JComboBox(strReqTransitions
					.toArray());
			jcmbReqTransitions.setBackground(Color.WHITE);
			jcmbReqTransitions.setPreferredSize(new Dimension(
					(int) jlbReqTransitions.getPreferredSize().getWidth(),
					(int) jlbReqTransitions.getPreferredSize().getHeight()));
			jcmbReqTransitions.setFont(jtfProvTransitions.getFont().deriveFont(
					Font.PLAIN));
			jcmbReqTransitions.add(jtfProvTransitions);
			cmbList.add(jcmbReqTransitions);
			JPanel p = new JPanel(new BorderLayout());
			p.add(jtfProvTransitions, BorderLayout.WEST);
			p.add(jcmbReqTransitions, BorderLayout.EAST);
			panel.add(p, new GridBagConstraints(0, i + 1, 1, 1, 1, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 10, 0));
		}

		jbEvaluate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				evaluate();
			}
		});

		jbFindMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findMatch();
			}
		});

		JPanel p = new JPanel(new BorderLayout(0, 5));
		p.add(jbEvaluate, BorderLayout.WEST);
		p.add(jbFindMatch, BorderLayout.EAST);
		JPanel southPanel = new JPanel(new BorderLayout(5, 0));
		southPanel.add(jlbCutOff, BorderLayout.WEST);
		southPanel.add(txtCutOff, BorderLayout.CENTER);
		p.add(southPanel, BorderLayout.SOUTH);

		panel.add(p, new GridBagConstraints(0, i + 1, 1, 1, 1, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 10, 0));

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(jlbResult, BorderLayout.WEST);
		panel.add(p1, new GridBagConstraints(0, i + 2, 1, 1, 1, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 1, 1), 10, 0));

		leftPanel.add(panel, BorderLayout.NORTH);

		jtaResult = new JTextArea(15, 20);
		jtaResult.setEditable(false);
		jtaResult.setBackground(Color.WHITE);
		jtaResult.setLineWrap(true);
		jtaResult.setWrapStyleWord(true);
		jtaResult.setBorder(javax.swing.BorderFactory.createLineBorder(
				Color.gray, 1));
		JScrollPane jspResultScroll = new JScrollPane(jtaResult,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		jbEvaluate.setAlignmentY(Component.LEFT_ALIGNMENT);

		leftPanel.add(jspResultScroll, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(panel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		leftPanel.add(scrollPane, BorderLayout.NORTH);
		this.add(leftPanel, BorderLayout.WEST);
		return leftPanel;
	}

	public void findMatch() {

		Iterator<JTextField> it1 = jtfList.iterator();
		int index = 0;
		double cutOffValue = 0.0;
		try {
			cutOffValue = Double.parseDouble(txtCutOff.getText());
		} catch (Exception e) {
		}
		while (it1.hasNext()) {
			double max = 0;
			String str1 = it1.next().getText();
			JComboBox cmb = cmbList.get(0);
			for (int i = 0; i < cmb.getItemCount(); i++) {
				String str2 = cmb.getItemAt(i).toString();
				double score = checker.syntacticEquivalenceScore(str1, str2);
				if (debugging) {
					if (str1.equals(". Produce offer")
							&& str2.equals(". Enter client and offer details")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Fiat") && str2.equals(". Fiat")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Check credit")
							&& str2.equals(". Check credit")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Make changes")
							&& str2.equals(". Make changes")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Check changes")
							&& str2.equals(". Check changes")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Print") && str2.equals(". Print")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Check complete")
							&& str2.equals(". Check complete")) {
						cmbList.get(index).setSelectedIndex(i);
					}
					if (str1.equals(". Plan appointment")
							&& str2.equals(". Notify sales")) {
						cmbList.get(index).setSelectedIndex(i);
					}
				} else if ((score > max) && (score >= cutOffValue)) {
					max = score;
					cmbList.get(index).setSelectedIndex(i);
				}
			}
			index++;
		}
	}

	public void evaluate() {
		jtaResult.setText("Processing ...");
		try {
			ProcessDifferences diffsProcessor = new ProcessDifferences(
					(ModelGraph) provBehaviour, (ModelGraph) reqBehaviour,
					fillRelation());
			jtaResult.setText("");
			Iterator<ProcessDifference> j = diffsProcessor
					.proceduralDifferences().iterator();
			if (!j.hasNext()) {
				jtaResult.append("No differences found.");
			}
			Message.add("<Differences>", Message.TEST);
			while (j.hasNext()) {
				ProcessDifference pd = j.next();

				Set<ModelGraphVertex> redTransitions = new HashSet<ModelGraphVertex>();
				if (pd.getOfTransitionsfromProvBehaviour() != null) {
					redTransitions.addAll(pd
							.getOfTransitionsfromProvBehaviour());
				}
				if (pd.getOfTransitionsfromReqBehaviour() != null) {
					redTransitions
							.addAll(pd.getOfTransitionsfromReqBehaviour());
				}

				Set<ModelGraphVertex> orangeTransitions = new HashSet<ModelGraphVertex>();
				if (pd.getinvolvedTransitionsfromProvBehaviour() != null) {
					orangeTransitions.addAll(pd
							.getinvolvedTransitionsfromProvBehaviour());
				}
				if (pd.getinvolvedTransitionsfromReqBehaviour() != null) {
					orangeTransitions.addAll(pd
							.getinvolvedTransitionsfromReqBehaviour());
				}

				jtaResult.append(pd.toString() + "\n\n");
				Message.add(pd.toString() + "#" + redTransitions.toString()
						+ orangeTransitions.toString(), Message.TEST);
			}
			Message.add("</Differences>", Message.TEST);
		} catch (Exception e) {
			e.printStackTrace();
			jtaResult.setText("something went wrong: " + e.getMessage());
		}
	}

	public Relation<ModelGraphVertex, ModelGraphVertex> fillRelation() {
		// Fill task equivalence relation
		Relation<ModelGraphVertex, ModelGraphVertex> r = new Relation<ModelGraphVertex, ModelGraphVertex>();

		Iterator<JComboBox> it = cmbList.iterator();
		int position = 0;
		while (it.hasNext()) {
			JComboBox cmb = it.next();
			if (cmb.getSelectedIndex() > 0) {
				int index = cmb.getSelectedIndex() - 1; // the first item in the
				// combobox is no
				// transition, but the
				// item "no equivalent"
				ModelGraphVertex tProv = provTransitions.get(position);
				ModelGraphVertex tReq = reqTransitions.get(index);
				Tuple<ModelGraphVertex, ModelGraphVertex> tuple = new Tuple<ModelGraphVertex, ModelGraphVertex>(
						tProv, tReq);
				r.addR(tuple);
			}
			position++;
		}
		position = 0;
		it = cmbList.iterator();
		while (it.hasNext()) {
			JComboBox cmb = it.next();
			if (cmb.getSelectedIndex() > 0) {
				int index = cmb.getSelectedIndex() - 1; // the first item in the
				// combobox is no
				// transition, but the
				// item "no equivalent"
				ModelGraphVertex tProv = provTransitions.get(position);
				ModelGraphVertex tReq = reqTransitions.get(index);
				Tuple<ModelGraphVertex, ModelGraphVertex> tuple = new Tuple<ModelGraphVertex, ModelGraphVertex>(
						tReq, tProv);
				r.addR(tuple);
			}
			position++;
		}
		return r;
	}

}
