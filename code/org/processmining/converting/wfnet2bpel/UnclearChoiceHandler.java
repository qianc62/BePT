package org.processmining.converting.wfnet2bpel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.framework.models.bpel.util.Pair;
import org.processmining.framework.models.petrinet.Choice;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: UnclearChoiceHandler
 * </p>
 * 
 * <p>
 * Description: Handles all choices where it is not certain if a choice is
 * explicit or implicit.
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
public class UnclearChoiceHandler extends JPanel implements ActionListener {

	private static final long serialVersionUID = 4171872528879560047L;

	private JButton ok;

	private JPanel choicePanel;

	private Map<JButton, PetriNet> choiceNets;

	private BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(1);

	private JDialog dialog;

	public UnclearChoiceHandler() {
		setLayout(new BorderLayout());
		choicePanel = new JPanel(new BorderLayout());
		add(choicePanel, BorderLayout.WEST);
		ok = new JButton("Ok");
		choicePanel.add(ok, BorderLayout.SOUTH);
		ok.addActionListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == ok) {
			try {
				ok.setEnabled(false);
				queue.put(new Object());
			} catch (InterruptedException e) {
			}
		} else {
			PetriNet wfnet = choiceNets.get(ae.getSource());
			PetriNetResult result = new PetriNetResult(wfnet);
			add(result.getVisualization(), BorderLayout.CENTER);
			validate();
			repaint();
			dialog.pack();
		}
	}

	public Map<String, Choice> handleChoices(
			Map<String, Pair<PetriNet, Choice>> unclearChoices) {
		Map<String, JRadioButton> selected = new LinkedHashMap<String, JRadioButton>();
		choiceNets = new LinkedHashMap<JButton, PetriNet>();
		choicePanel.removeAll();
		choicePanel.add(ok, BorderLayout.SOUTH);
		JPanel panel = new JPanel(new GridLayout(unclearChoices.size(), 3));
		choicePanel.add(panel, BorderLayout.CENTER);
		int i = 1;
		for (Entry entry : unclearChoices.entrySet()) {
			JButton button = new JButton(Integer.toString(i));
			button.addActionListener(this);
			choiceNets.put(button,
					((Pair<PetriNet, Choice>) entry.getValue()).first);
			panel.add(button);
			ButtonGroup group = new ButtonGroup();
			JRadioButton radio1 = new JRadioButton("explicit");
			JRadioButton radio2 = new JRadioButton("implicit");
			group.add(radio1);
			group.add(radio2);
			radio1.setSelected(true);
			selected.put((String) entry.getKey(), radio1);
			panel.add(radio1);
			panel.add(radio2);
			i++;
		}
		validate();
		repaint();

		dialog = new JDialog(MainUI.getInstance(),
				"Manual translation of components", true);
		dialog.add(this);
		dialog.pack();
		CenterOnScreen.center(dialog);
		dialog.setVisible(true);

		final Object[] obj = new Object[1];

		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					Thread.yield();
					obj[0] = queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		};

		if (obj[0] == null)
			return null;
		Map<String, Choice> choices = new LinkedHashMap<String, Choice>();
		for (String placeId : unclearChoices.keySet()) {
			if (selected.get(placeId).isSelected())
				choices.put(placeId, Choice.EXPLICIT);
			else
				choices.put(placeId, Choice.IMPLICIT);
		}

		return choices;
	}
}
