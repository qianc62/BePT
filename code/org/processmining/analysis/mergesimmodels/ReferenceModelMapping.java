package org.processmining.analysis.mergesimmodels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.GuiPropertyStringTextarea;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * Panel on which the user can indicate how a simulation model needs to be
 * mapped to the reference model. At this moment, for each vertex in the
 * reference model the user needs to choose which highlevelactivity needs to
 * mapped to that vertex.
 * 
 * @author rmans
 */
public class ReferenceModelMapping extends JDialog {

	/**
	 * the simulation model that has been choosen as reference model
	 */
	private HLModel myReferenceModel;
	/**
	 * the simulation model that needs to be mapped to the reference model
	 */
	private HLModel myActivityModel;
	/**
	 * list which contains the labels for the vertices of the reference model
	 */
	private ArrayList<ModelGraphVertex> taskLabels;
	/**
	 * list which contains the combo boxes which show the available
	 * highlevelactivities that can be choosen, for each of the vertices of the
	 * reference model
	 */
	private ArrayList<ToolTipComboBox> combosHlActs;
	/**
	 * Indicates whether the user chooses OK (ok == true) or chooses Cancel
	 * (cancel == false)
	 */
	private boolean ok = false;

	/**
	 * Basic constructor
	 * 
	 * @param referenceModel
	 *            HighLevelProcess the simulation model that has been assigned
	 *            as reference model
	 * @param activityModel
	 *            HighLevelProcess the simulation for which highlevelactivities
	 *            need to be mapped to the vertices of the reference model
	 * @throws HeadlessException
	 */
	public ReferenceModelMapping(HLModel referenceModel, HLModel activityModel)
			throws HeadlessException {
		super(
				MainUI.getInstance(),
				"Establish a mapping between the activities of the input simulation models",
				true);
		myReferenceModel = referenceModel;
		myActivityModel = activityModel;
		setUndecorated(false);
		jbInit();
		pack();
		CenterOnScreen.center(this);
	}

	/**
	 * Setting up the GUI.
	 */
	private void jbInit() {
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("   Ok   ");
		JButton cancelButton = new JButton(" Cancel ");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				// read the value from the enumeration list
				setVisible(false);
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		// the panel for choosing the reference model
		JPanel mainPanel = new JPanel();
		// add the enumeration list on the panel
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		// fill the mainPanel
		mainPanel.setLayout(new BorderLayout());
		String description = new String(
				"Please associate the activities from the reference model to the activities in the "
						+ myActivityModel.getHLProcess().getGlobalInfo()
								.getName()
						+ ". This is necessary in order to ensure that the correct simulation information is accessed for each activity. "
						+ "In the case that both models contain an activity with the same name, a mapping is automatically suggested for these two activities.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);
		mainPanel.add(helpText.getPropertyPanel(), BorderLayout.NORTH);

		GridBagLayout gb1 = new GridBagLayout();
		JPanel mappingPanel = new JPanel(gb1);

		// determine maximal length of the names of the nodes of the reference
		// model
		int ml = 0;
		JLabel lab = new JLabel("");
		lab.setFont(lab.getFont().deriveFont(Font.PLAIN));
		Iterator<ModelGraphVertex> nodes = myReferenceModel.getGraphNodes()
				.iterator();
		while (nodes.hasNext()) {
			ModelGraphVertex vertex = nodes.next();
			// check whether there exists an corresponding highlevelactivity
			if (myReferenceModel.findActivity(vertex) != null) {
				lab.setText(myReferenceModel.findActivity(vertex).getName());
				if (lab.getPreferredSize().getWidth() > ml) {
					ml = (int) lab.getPreferredSize().getWidth();
				}
			}
		}
		ml += 4;
		ml = Math.max(ml, 250);

		// the panel with explaining texts
		{
			final JLabel tasksInRefMod = new JLabel(
					"<html>Activities contained in<br>reference model:   </html>");
			tasksInRefMod.setPreferredSize(new Dimension(ml,
					(int) tasksInRefMod.getPreferredSize().getHeight() * 2));

			ToolTipComboBox newCombo = new ToolTipComboBox(myActivityModel
					.getHLProcess().getActivities().toArray());
			final JLabel hlActsInActMod = new JLabel(
					"<html>Activities contained in<br>"
							+ myActivityModel.getHLProcess().getGlobalInfo()
									.getName() + ":</html>");
			// adjust size to width of combo box, but take at least 250 width
			if (newCombo.getPreferredSize().getWidth() > 250) {
				hlActsInActMod.setPreferredSize(new Dimension((int) newCombo
						.getPreferredSize().getWidth(), (int) hlActsInActMod
						.getPreferredSize().getHeight()));
			} else {
				hlActsInActMod.setPreferredSize(new Dimension(250,
						(int) hlActsInActMod.getPreferredSize().getHeight()));
			}

			JPanel p = new JPanel(new BorderLayout());
			p.add(tasksInRefMod, BorderLayout.WEST);
			p.add(hlActsInActMod, BorderLayout.CENTER);

			mappingPanel.add(p, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 1, 1), 0, 0));
		}

		taskLabels = new ArrayList<ModelGraphVertex>();
		combosHlActs = new ArrayList<ToolTipComboBox>();
		// for each node in the reference model that refers to an activity,
		// create a textfield with the
		// identifier of that node and next to that a combo box for which
		// highlevelactivity of the activity model
		// needs to be mapped to that task.
		int i = 0;
		Iterator<ModelGraphVertex> nodesIt = myReferenceModel.getGraphNodes()
				.iterator();
		while (nodesIt.hasNext()) {
			ModelGraphVertex vertex = nodesIt.next();
			// check whether there exists a corresponding highlevelactivity
			if (myReferenceModel.findActivity(vertex) != null) {
				// make a new label with the vertex identifier
				final JTextField label = new JTextField(vertex.toString());
				label.setFont(label.getFont().deriveFont(Font.PLAIN));
				label.setEditable(false);
				label.setPreferredSize(new Dimension(ml, (int) label
						.getPreferredSize().getHeight()));
				// add the vertex to taskLabels list
				taskLabels.add(vertex);

				// make a new combo-list
				// already suppose a mapping, based on the name of the vertex
				// and the name of the
				// highlevelactivity
				HLActivity[] activities = myActivityModel.getHLProcess()
						.getActivities().toArray(
								new HLActivity[myActivityModel.getHLProcess()
										.getActivities().size()]);
				boolean found = false;
				for (int j = 0; j < activities.length; j++) {
					// if the name of the highlevelactivity and the identifier
					// of the vertex is exactly the same, then swap
					// with the first position in the array
					// remove the \\n from vertex name
					String strVertex = vertex.getIdentifier().replace("\\n",
							" ");
					if (activities[j].getName().equals(strVertex)) {
						HLActivity h = activities[j];
						activities[j] = activities[0];
						activities[0] = h;
						found = true;
					}
				}
				// TODO: temporary solution to also allow non-mappings
				// -> check how to do it in a type-safe way and avoid
				// re-ordering hack
				ArrayList activitiesPlusNone = new ArrayList();
				if (found == true) {
					for (int k = 0; k < activities.length; k++) {
						activitiesPlusNone.add(activities[k]);
					}
					activitiesPlusNone.add("None                     ");
				} else {
					activitiesPlusNone.add("None                     ");
					for (int k = 0; k < activities.length; k++) {
						activitiesPlusNone.add(activities[k]);
					}
				}
				ToolTipComboBox newCombo = new ToolTipComboBox(
						activitiesPlusNone.toArray());

				if (newCombo.getPreferredSize().getWidth() < 250) {
					newCombo.setPreferredSize(new Dimension(250, (int) newCombo
							.getPreferredSize().getHeight()));
				}

				newCombo.setFont(newCombo.getFont().deriveFont(Font.PLAIN));
				newCombo.setBorder(null);
				// add the combobox to the combosHlActs list
				combosHlActs.add(newCombo);

				// create the panel for the textfield and the combobox
				JPanel p = new JPanel(new BorderLayout());
				// p.setBackground(Color.BLUE);
				p.add(label, BorderLayout.WEST);
				p.add(newCombo, BorderLayout.CENTER);

				mappingPanel.add(p, new GridBagConstraints(0, i + 1, 1, 1, 0.0,
						0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(1, 1, 1, 1), 0, 0));
				i++;
			}
		}

		JScrollPane scrollPane = new JScrollPane(mappingPanel);
		scrollPane.setBorder(null);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Makes this dialog visible and will return as soon as it is set invisible
	 * again.
	 * 
	 * @return <code>true</code> if the ok button is clicked, <code>false</code>
	 *         if the cancel button is clicked.
	 */
	public boolean showModal() {
		setVisible(true);
		return ok;
	}

	/**
	 * Returns the reference model for which a mapping is made from the provided
	 * simulation model to the reference model itself. So, the user has
	 * indicated for each of the vertices of the reference model, which
	 * highlevelactivity of the provided simulation model has to be assigned to
	 * that vertex. Furthermore, the resources, groups and data attributes of
	 * the provided simulation model and that exist on process level are copied
	 * to the reference model (also on process level). The data, probability,
	 * and frequency dependencies are also copied, based on equivalence of their
	 * node identifiers, from the provided simulation model to the reference
	 * model.
	 * 
	 * @return HighLevelProcess
	 */
	public HLModel getMappedModel() {
		// first make sure that the selected highlevelactivity for each task
		// node in
		// the reference model is also mapped to that task node
		int counter = 0;
		Iterator<ModelGraphVertex> taskLabelsIt = taskLabels.iterator();
		while (taskLabelsIt.hasNext()) {
			ModelGraphVertex task = taskLabelsIt.next();
			// get the corresponding combobox
			ToolTipComboBox combo = combosHlActs.get(counter);
			// get the selected highlevelactivity and assign it to this task
			if (combo.getSelectedItem() instanceof HLActivity) {
				HLActivity selectedActivity = (HLActivity) combo
						.getSelectedItem();
				myReferenceModel.setActivity(task, selectedActivity);
			}
			counter++;
		}
		// furthermore, copy process information from the activity model to the
		// reference model
		// data attributes
		Iterator<HLAttribute> dataAttribs = myActivityModel.getHLProcess()
				.getAttributes().iterator();
		while (dataAttribs.hasNext()) {
			HLAttribute dataAttrib = dataAttribs.next();
			HLAttribute clonedAtt = (HLAttribute) dataAttrib.clone();
			myReferenceModel.getHLProcess().addOrReplace(clonedAtt);
		}
		// resources
		Iterator<HLResource> resources = myActivityModel.getHLProcess()
				.getResources().iterator();
		while (resources.hasNext()) {
			HLResource resource = resources.next();
			HLResource clonedResource = (HLResource) resource.clone();
			myReferenceModel.getHLProcess().addOrReplace(clonedResource);
		}
		// groups
		Iterator<HLGroup> groups = myActivityModel.getHLProcess().getGroups()
				.iterator();
		while (groups.hasNext()) {
			HLGroup group = groups.next();
			HLGroup clonedGroup = (HLGroup) group.clone();
			myReferenceModel.getHLProcess().addOrReplace(clonedGroup);
		}
		// perspectives
		Iterator<HLTypes.Perspective> perspectives = myActivityModel
				.getHLProcess().getGlobalInfo().getPerspectives().iterator();
		while (perspectives.hasNext()) {
			HLTypes.Perspective perspective = perspectives.next();
			myReferenceModel.getHLProcess().getGlobalInfo().addPerspective(
					perspective);
		}
		// case generation scheme
		myReferenceModel.getHLProcess().getGlobalInfo()
				.setCaseGenerationScheme(
						myActivityModel.getHLProcess().getGlobalInfo()
								.getCaseGenerationScheme());
		myReferenceModel.getHLProcess().getGlobalInfo().setTimeUnit(
				myActivityModel.getHLProcess().getGlobalInfo().getTimeUnit());

		// choices
		Iterator<HLChoice> choicesAct = myActivityModel.getHLProcess()
				.getChoices().iterator();
		while (choicesAct.hasNext()) {
			HLChoice choiceAct = choicesAct.next();
			ModelGraphVertex choiceNode = myActivityModel
					.findModelGraphVertexForChoice(choiceAct.getID());
			myReferenceModel.setChoice(choiceNode, choiceAct);
		}

		// TODO: check whether above really replaces what was below, and remove

		// // choices
		// Iterator<HLChoice> choicesRef =
		// myReferenceModel.getHLProcess().getChoices().iterator();
		// while (choicesRef.hasNext()) {
		// HLChoice choiceRef = choicesRef.next();
		// Iterator<HLChoice> choicesAct =
		// myActivityModel.getChoices().iterator();
		// while (choicesAct.hasNext()) {
		// HLChoice choiceAct = choicesAct.next();
		// // check whether the source node is the same (based on the
		// identifier)
		// if
		// (choiceRef.getChoiceNode().getIdentifier().equals(choiceAct.getChoiceNode().getIdentifier()))
		// {
		//					
		//					
		//					
		// // data dependencies
		// Iterator<HighLevelDataDependency> dataDepsRef =
		// choiceRef.getDataDependencies().iterator();
		// while (dataDepsRef.hasNext()) {
		// HighLevelDataDependency dataDepRef = dataDepsRef.next();
		// // find the corresponding data dependency for the activity model
		// Iterator<HighLevelDataDependency> dataDepsAct =
		// choiceAct.getDataDependencies().iterator();
		// while (dataDepsAct.hasNext()) {
		// HighLevelDataDependency dataDepAct = dataDepsAct.next();
		// if
		// (dataDepRef.getTargetNode().getIdentifier().equals(dataDepAct.getTargetNode().getIdentifier()))
		// {
		// dataDepRef.setExpression(dataDepAct.getExpression());
		// break;
		// }
		// }
		// }
		// // probability dependencies
		// Iterator<HighLevelProbabilityDependency> probDepsRef =
		// choiceRef.getProbabilityDependencies().iterator();
		// while (probDepsRef.hasNext()) {
		// HighLevelProbabilityDependency probDepRef = probDepsRef.next();
		// // find the corresponding probability dependency for the activity
		// model
		// Iterator<HighLevelProbabilityDependency> probDepsAct =
		// choiceAct.getProbabilityDependencies().iterator();
		// while (probDepsAct.hasNext()) {
		// HighLevelProbabilityDependency probDepAct = probDepsAct.next();
		// if
		// (probDepRef.getTargetNode().getIdentifier().equals(probDepAct.getTargetNode().getIdentifier()))
		// {
		// probDepRef.setProbability(probDepAct.getProbability());
		// break;
		// }
		// }
		// }
		// // frequency dependencies
		// ArrayList<HLActivity> freqDepsToCopy = new ArrayList<HLActivity>();
		// Iterator<HLActivity> freqDepsRef =
		// choiceRef.getFrequencyDependencies().iterator();
		// while (freqDepsRef.hasNext()) {
		// HLActivity freqDepRef = freqDepsRef.next();
		// // find the corresponding frequency dependency for the activity model
		// // find the corresponding highlevelactivity with the same name
		// Iterator<HLActivity> activities =
		// myActivityModel.getHLProcess().getActivities().iterator();
		// while (activities.hasNext()) {
		// HLActivity activity = activities.next();
		// if (freqDepRef.getName().equals(activity.getName())) {
		// freqDepsToCopy.add(activity);
		// }
		// }
		// }
		// choiceRef.removeAllFrequencyDependencies();
		// Iterator<HLActivity> it = freqDepsToCopy.iterator();
		// while (it.hasNext()) {
		// HLActivity act = it.next();
		// choiceRef.addFrequencyDependency(act);
		// }
		// }
		// }
		// }
		// copy the name
		myReferenceModel.getHLProcess().getGlobalInfo().setName(
				myActivityModel.getHLProcess().getGlobalInfo().getName());

		return myReferenceModel;
	}

}
