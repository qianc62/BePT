package org.processmining.converting.wfnet2bpel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.converting.wfnet2bpel.log.TranslationEditor;
import org.processmining.framework.models.bpel.BPELActivity;
import org.processmining.framework.models.bpel.util.Quintuple;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.pattern.ComponentDescription;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.mining.petrinetmining.PetriNetResult;

import att.grappa.Node;

/**
 * <p>
 * Title: ManualTranslationWizard
 * </p>
 * 
 * <p>
 * Description: Allows the user to specify a translation for a component.
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
public class ManualTranslationWizard extends JPanel implements
		ListSelectionListener, ActionListener, CaretListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4171872528879560047L;

	private JList componentList, libraryList;

	private DefaultListModel componentModel, libraryListModel;

	private List<PetriNet> components;

	private JButton back;

	public JButton forward;

	private final JLabel chooseComponentLabel, chooseTranslationLabel,
			saveComponentLabel;

	private BlockingQueue<Quintuple<PetriNet, BPELActivity, String, Integer, Map<Node, Node>>> wizardChoice = new ArrayBlockingQueue<Quintuple<PetriNet, BPELActivity, String, Integer, Map<Node, Node>>>(
			1);

	private JPanel chooseComponentPanel, saveComponentPanel, currentPanel;

	private TranslationEditor chooseTranslationPanel;

	private PetriNet wfnet;

	public BPELActivity activity;

	private JPanel south;

	private PetriNet chosenWfnet = null;

	private JTextField translationNameField;

	private int translationNameIndex;

	private JButton up;

	private JButton down;

	private JDialog dialog;

	public ManualTranslationWizard() {
		setLayout(new BorderLayout());

		chooseComponentLabel = new JLabel("Choose component");
		chooseTranslationLabel = new JLabel("Specify translation");
		saveComponentLabel = new JLabel("Save library component");

		JPanel northPanel = new JPanel(new GridLayout(1, 5));
		northPanel.add(chooseComponentLabel, BorderLayout.NORTH);
		northPanel.add(new JLabel("-->"));
		northPanel.add(chooseTranslationLabel, BorderLayout.NORTH);
		northPanel.add(new JLabel("-->"));
		northPanel.add(saveComponentLabel, BorderLayout.NORTH);
		add(northPanel, BorderLayout.NORTH);
		setLabel(chooseComponentLabel);

		setChooseComponentPanel();

		setChooseTranslationPanel();

		setSaveTranslationPanel();

		setLabel(chooseComponentLabel);

		south = new JPanel(new FlowLayout());
		back = new JButton("Previous");
		back.addActionListener(this);
		south.add(back);
		back.setEnabled(false);

		forward = new JButton("Next");
		forward.addActionListener(this);
		south.add(forward);
		forward.setEnabled(false);

		add(south, BorderLayout.SOUTH);
	}

	private void setSaveTranslationPanel() {
		saveComponentPanel = new JPanel(new GridLayout(2, 1));

		JPanel north = new JPanel(new BorderLayout());
		north.add(new JLabel("Name library component:"), BorderLayout.WEST);
		translationNameField = new JTextField();
		translationNameField.addCaretListener(this);
		north.add(translationNameField, BorderLayout.CENTER);
		saveComponentPanel.add(north);

		JPanel southPanel = new JPanel(new BorderLayout());

		JPanel southEast = new JPanel(new GridLayout(2, 1));
		up = new JButton("Up");
		up.addActionListener(this);
		southEast.add(up);
		down = new JButton("Down");
		down.addActionListener(this);
		southEast.add(down);
		southPanel.add(southEast, BorderLayout.EAST);

		JPanel southCenter = new JPanel(new BorderLayout());
		libraryListModel = new DefaultListModel();
		libraryList = new JList(libraryListModel);
		libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPanel = new JScrollPane(libraryList);
		southCenter.add(scrollPanel, BorderLayout.CENTER);
		southPanel.add(southCenter, BorderLayout.CENTER);

		saveComponentPanel.add(southPanel);
	}

	private void setChooseTranslationPanel() {
		chooseTranslationPanel = new TranslationEditor(this);
		// chooseTranslationPanel = new JPanel(new GridLayout(2, 1));
		// bpelTranslation = new JTextArea();
		// bpelTranslation.addCaretListener(this);
		// chooseTranslationPanel.add(bpelTranslation);
	}

	/**
	 * 
	 */
	private void setChooseComponentPanel() {
		chooseComponentPanel = new JPanel(new BorderLayout());

		componentModel = new DefaultListModel();
		componentList = new JList(componentModel);
		componentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		componentList.addListSelectionListener(this);

		JPanel listPanel = new JPanel(new BorderLayout());
		listPanel.add(new JLabel("Components"), BorderLayout.NORTH);
		listPanel.add(componentList, BorderLayout.CENTER);

		JScrollPane listScroller = new JScrollPane(listPanel);
		JPanel backListPanel = new JPanel(new BorderLayout());
		backListPanel.add(listScroller, BorderLayout.CENTER);
		chooseComponentPanel.add(backListPanel, BorderLayout.WEST);
	}

	@SuppressWarnings("unchecked")
	public Quintuple<PetriNet, BPELActivity, String, Integer, Map<Node, Node>> translateAComponent(
			List<PetriNet> components, List<ComponentDescription> matchingOrder) {
		this.components = components;
		componentModel.clear();
		for (PetriNet component : components) {
			componentModel.addElement(component);
		}
		for (ComponentDescription component : matchingOrder) {
			libraryListModel
					.addElement("<"
							+ (component.isPredefined() ? "PREDEFINED"
									: "USER DEFINED") + ">: "
							+ component.getName());
		}
		libraryListModel.addElement(getCurrentTranslationName());
		translationNameIndex = libraryListModel.size() - 1;
		setMainPanel(chooseComponentPanel);
		dialog = new JDialog(MainUI.getInstance(),
				"Manual translation of components", true);
		dialog.setPreferredSize(new Dimension(1024, 768));
		dialog.add(this);
		dialog.addWindowListener(this);
		dialog.pack();
		CenterOnScreen.center(dialog);
		dialog.setVisible(true);

		final Object[] obj = new Object[1];

		SwingWorker worker = new SwingWorker() {
			public Object construct() {
				try {
					Thread.yield();
					obj[0] = wizardChoice.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		worker.start();
		while (obj[0] == null)
			Thread.yield();
		return (Quintuple<PetriNet, BPELActivity, String, Integer, Map<Node, Node>>) obj[0];
	}

	private void setMainPanel(JPanel panel) {
		currentPanel = panel;
		add(panel, BorderLayout.CENTER);
		validate();
		repaint();
	}

	public void valueChanged(ListSelectionEvent arg0) {
		if (currentPanel == chooseComponentPanel) {
			int selectedIndex = 0;
			if (arg0.getSource() == componentList
					&& componentList.getSelectedIndex() >= 0) {
				selectedIndex = componentList.getSelectedIndex();
				chosenWfnet = components.get(selectedIndex);
				forward.setEnabled(true);
			}
			if (chosenWfnet != null) {
				chosenWfnet.getClusters().clear();
				PetriNetResult result = new PetriNetResult(chosenWfnet);
				chooseComponentPanel.add(result.getVisualization(),
						BorderLayout.CENTER);
				validate();
				repaint();
			}
		}
	}

	public void actionPerformed(ActionEvent ae) {
		if (currentPanel == chooseComponentPanel) {
			remove(chooseComponentPanel);
			if (ae.getSource() == forward) {
				wfnet = components.get(componentList.getSelectedIndex());
				chooseTranslationPanel.setPetriNet(wfnet);
				remove(chooseComponentPanel);
				setMainPanel(chooseTranslationPanel);
				if (chosenWfnet != null
						&& chooseTranslationPanel.getComponentCount() == 1)
					chooseTranslationPanel.add(new PetriNetResult(chosenWfnet)
							.getVisualization());
				back.setEnabled(true);
				forward.setEnabled(false);
				setLabel(chooseTranslationLabel);
			}
		} else if (currentPanel == chooseTranslationPanel) {
			remove(chooseTranslationPanel);
			if (ae.getSource() == forward) {
				setMainPanel(saveComponentPanel);
				forward.setEnabled(false);
				setLabel(saveComponentLabel);
			} else if (ae.getSource() == back) {
				setMainPanel(chooseComponentPanel);
				back.setEnabled(false);
				setLabel(chooseComponentLabel);
				forward.setEnabled(true);
			}
		} else if (currentPanel == saveComponentPanel) {
			if (ae.getSource() == forward || ae.getSource() == back) {
				remove(saveComponentPanel);
				if (ae.getSource() == forward) {
					setVisible(false);
					dialog.dispose();
					Map<Node, Node> isomorphism = new TreeMap<Node, Node>(
							new Comparator<Node>() {
								public int compare(Node arg0, Node arg1) {
									return arg0.getName().compareTo(
											arg1.getName());
								}
							});
					for (Node node : wfnet.getNodes()) {
						isomorphism.put(node, node);
					}
					try {
						wizardChoice.put(Quintuple.create(wfnet, activity,
								translationNameField.getText(),
								translationNameIndex, isomorphism));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (ae.getSource() == back) {
					setMainPanel(chooseTranslationPanel);
					setLabel(chooseTranslationLabel);
					forward.setEnabled(true);
				}
			} else if (ae.getSource() == up && translationNameIndex > 0) {
				String tmp = (String) libraryListModel
						.get(translationNameIndex - 1);
				libraryListModel.setElementAt(getCurrentTranslationName(),
						translationNameIndex - 1);
				libraryListModel.setElementAt(tmp, translationNameIndex);
				translationNameIndex--;
			} else if (ae.getSource() == down
					&& translationNameIndex < libraryListModel.size() - 1) {
				String tmp = (String) libraryListModel
						.get(translationNameIndex + 1);
				libraryListModel.setElementAt(getCurrentTranslationName(),
						translationNameIndex + 1);
				libraryListModel.setElementAt(tmp, translationNameIndex);
				translationNameIndex++;
			}
		}
	}

	private void setLabel(JLabel label) {
		Font current = label.getFont();
		Font bold = new Font(current.getName(), Font.BOLD, current.getSize());
		Font plain = new Font(current.getName(), Font.PLAIN, current.getSize());
		if (label == chooseComponentLabel)
			chooseComponentLabel.setFont(bold);
		else
			chooseComponentLabel.setFont(plain);
		if (label == chooseTranslationLabel)
			chooseTranslationLabel.setFont(bold);
		else
			chooseTranslationLabel.setFont(plain);
		if (label == saveComponentLabel)
			saveComponentLabel.setFont(bold);
		else
			saveComponentLabel.setFont(plain);
	}

	public void caretUpdate(CaretEvent arg0) {
		if (arg0.getSource() == translationNameField) {
			if (translationNameField.getText().trim().length() == 0) {
				forward.setEnabled(false);
				return;
			}
			libraryListModel.setElementAt(getCurrentTranslationName(),
					translationNameIndex);
			boolean foundOne = false;
			for (int i = 0; i < libraryListModel.size(); i++) {
				String translation = translationNameField.getText().trim();
				String libraryListName = (String) libraryListModel.get(i);
				libraryListName = libraryListName.substring(libraryListName
						.indexOf(":") + 2);
				if (foundOne && libraryListName.equals(translation)) {
					forward.setEnabled(false);
					return;
				} else if (!foundOne && libraryListName.equals(translation))
					foundOne = true;
			}

			forward.setEnabled(true);
		}
	}

	/**
	 * @return
	 */
	private String getCurrentTranslationName() {
		return "<CURRENT COMPONENT>: " + translationNameField.getText();
	}

	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		try {
			dialog.setVisible(false);
			wizardChoice.put(Quintuple.create((PetriNet) null,
					(BPELActivity) null, (String) null, 0,
					(Map<Node, Node>) null));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}

}
