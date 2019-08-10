package org.processmining.analysis.mergesimmodels;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * In this panel, the user can select one of the simulation models, that needs
 * to be the reference model.
 * 
 * @author rmans
 */
public class ReferenceModelChooser extends JDialog {

	/**
	 * Indicates whether the user chooses OK (ok == true) or chooses Cancel
	 * (cancel == false)
	 */
	private boolean ok = false;

	/** the simulation models */
	private List<HLModel> myActivityModels;

	/**
	 * combo box showing the names of the simulation models that can be selected
	 * as reference model
	 */
	private GUIPropertyListEnumeration myPosRefModels;

	/** the simulation model that is selected as reference model */
	private HLModel mySelectedActivityModel;

	/**
	 * Basic constructor
	 * 
	 * @param activityModels
	 *            List simulation models that may be selected as a reference
	 *            model
	 * @throws HeadlessException
	 */
	public ReferenceModelChooser(List<HLModel> activityModels)
			throws HeadlessException {
		super(MainUI.getInstance(), "Choose reference model", true);
		myActivityModels = activityModels;
		setUndecorated(false);
		jbInit();
		pack();
		CenterOnScreen.center(this);
	}

	/**
	 * Setting up the GUI interface
	 */
	private void jbInit() {
		// create the enumeration list for the possible reference models
		ArrayList<String> values = new ArrayList<String>();
		Iterator<HLModel> actModels = myActivityModels.iterator();
		while (actModels.hasNext()) {
			HLModel actMod = actModels.next();
			String name = actMod.getHLProcess().getGlobalInfo().getName();
			values.add(name);
		}
		myPosRefModels = new GUIPropertyListEnumeration("", "",
				myActivityModels, null, 300);

		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton("   Ok   ");
		JButton cancelButton = new JButton(" Cancel ");

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				// read the value from the enumeration list
				mySelectedActivityModel = (HLModel) myPosRefModels.getValue();
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
		JPanel refModChooserPanel = new JPanel();
		// add the enumeration list on the panel
		refModChooserPanel.add(myPosRefModels.getPropertyPanel());

		String description = new String(
				"Please choose one of the input simulation models as the reference model. "
						+ "The chosen reference model will serve as a template for the output model "
						+ "(that is, if it is based on, for example, a Petri-net process model the result will also be a Petri-net based simulation model).");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(helpText.getPropertyPanel(), BorderLayout.NORTH);
		getContentPane().add(refModChooserPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
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
	 * Get the simulation model that has been assigned as reference model
	 * 
	 * @return HighLevelProcess the simulation model that is assigned as
	 *         reference model
	 */
	public HLModel getSelectedReferenceModel() {
		return mySelectedActivityModel;
	}

	/**
	 * Retrieves the highlevelprocesses that are not selected. In the case that
	 * the choosen reference model appears twice or more in the list, the first
	 * occurrence will be left out.
	 * 
	 * @return List the highlevelprocesses that are not selected.
	 */
	public List<HLModel> getNotSelectedReferenceModels() {
		ArrayList<HLModel> returnNotSelectedReferenceModels = new ArrayList<HLModel>();
		Iterator<HLModel> it = myActivityModels.iterator();
		boolean once = false;
		while (it.hasNext()) {
			HLModel proc = it.next();
			if (mySelectedActivityModel == proc) {
				if (!once) {
					once = true;
				} else {
					returnNotSelectedReferenceModels.add(proc);
				}
			} else {
				returnNotSelectedReferenceModels.add(proc);
			}
		}
		return returnNotSelectedReferenceModels;
	}

}
