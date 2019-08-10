/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.converting;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PNEdge;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.models.petrinet.oWFNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.CenterOnScreen;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * <p>
 * Title: PetriNetTooWFNet
 * </p>
 * 
 * <p>
 * Description: Converts a PetriNet object into an o WFNet object (by specifying
 * the source place and the sink place). Additional source and sink places will
 * be turned into communication places. See PetriNetToWFNet for additional
 * details.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class PetriNetTooWFNet implements ConvertingPlugin {

	private Place sourcePlace = null;
	private Place sinkPlace = null;

	public PetriNetTooWFNet() {
	}

	public String getName() {
		return "Petri net to oWF net";
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:pn2owfn";
	}

	public MiningResult convert(ProvidedObject object) {
		PetriNet providedPN = null;
		LogReader log = null;

		for (int i = 0; i < object.getObjects().length; i++) {
			if (providedPN == null
					&& object.getObjects()[i] instanceof PetriNet) {
				providedPN = (PetriNet) object.getObjects()[i];
			}
			if (log == null && object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		oWFNet pn = convert(providedPN);
		pn.Test("PetriNetTooWFNet");

		return new PetriNetResult(log, pn);
	}

	public boolean accepts(ProvidedObject object) {
		for (int i = 0; i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				return true;
			}
		}
		return false;
	}

	public oWFNet convert(PetriNet source) {
		HashSet<Place> sourcePlaces = new HashSet<Place>();
		HashSet<Place> sinkPlaces = new HashSet<Place>();
		HashSet<Place> places = new HashSet<Place>(source.getPlaces());

		for (Place place : places) {
			if (place.getInEdges() == null) {
				sourcePlaces.add(place);
			}
			if (place.getOutEdges() == null) {
				sinkPlaces.add(place);
			}
		}

		if (sourcePlaces.size() == 1 && sinkPlaces.size() == 1) {
			sourcePlace = sourcePlaces.iterator().next();
			sinkPlace = sinkPlaces.iterator().next();
		} else {
			PetriNetTooWFNetUI ui = new PetriNetTooWFNetUI(this, sourcePlaces,
					sinkPlaces);
			ui.setVisible(true);
		}

		oWFNet target = new oWFNet();
		target.setSourcePlace(sourcePlace);
		target.setSinkPlace(sinkPlace);

		Iterator it = source.getTransitions().iterator();
		HashMap mapping = new HashMap();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			Transition clonedTransition = (Transition) transition.clone();
			target.addAndLinkTransition(clonedTransition);
			mapping.put(transition, clonedTransition);
		}

		it = source.getPlaces().iterator();
		while (it.hasNext()) {
			Place place = (Place) it.next();
			if ((place.getPredecessors().size() > 0 || place == sourcePlace)
					&& (place.getSuccessors().size() > 0 || place == sinkPlace)) {
				Place clonedPlace = (Place) place.clone();
				target.addAndLinkPlace(clonedPlace);
				mapping.put(place, clonedPlace);
			}
		}

		it = source.getEdges().iterator();
		while (it.hasNext()) {
			PNEdge edge = (PNEdge) it.next();
			PNEdge clonedEdge = (PNEdge) edge.clone();
			if (edge.isPT()) {
				Place p = (Place) edge.getSource();
				if (p.getPredecessors().size() > 0 || p == sourcePlace) {
					Place myPlace = (Place) mapping.get(p);
					Transition t = (Transition) edge.getDest();
					Transition myTransition = (Transition) mapping.get(t);
					target.addAndLinkEdge(clonedEdge, myPlace, myTransition);
				} else {
					// Turn additional source place into communication place.
					target.addInput(p.getIdentifier(), (Transition) mapping
							.get(edge.getDest()));
				}
			} else {
				Place p = (Place) edge.getDest();
				if (p.getSuccessors().size() > 0 || p == sinkPlace) {
					Place myPlace = (Place) mapping.get(p);
					Transition t = (Transition) edge.getSource();
					Transition myTransition = (Transition) mapping.get(t);
					target.addAndLinkEdge(clonedEdge, myTransition, myPlace);
				} else {
					// Turn additional sink place into communication place.
					target.addOutput(
							(Transition) mapping.get(edge.getSource()), p
									.getIdentifier());
				}
			}
		}

		it = source.getClusters().iterator();
		while (it.hasNext()) {
			TransitionCluster cluster = (TransitionCluster) it.next();
			TransitionCluster clonedCluster = (TransitionCluster) cluster
					.clone();
			target.addCluster(clonedCluster);
		}

		return target;
	}

	public void setSourcePlace(Place place) {
		sourcePlace = place;
	}

	public void setSinkPlace(Place place) {
		sinkPlace = place;
	}
}

class PetriNetTooWFNetUI extends JDialog implements ActionListener {
	PetriNetTooWFNet petrinettoowfnet;
	HashSet<Place> sourcePlaces;
	HashSet<Place> sinkPlaces;
	JComboBox sourceCombo;
	JComboBox sinkCombo;
	JButton doneButton;

	public PetriNetTooWFNetUI(PetriNetTooWFNet petrinettoowfnet,
			HashSet<Place> sourcePlaces, HashSet<Place> sinkPlaces) {
		super(MainUI.getInstance(), "Select options...", true);
		this.petrinettoowfnet = petrinettoowfnet;
		this.sourcePlaces = sourcePlaces;
		this.sinkPlaces = sinkPlaces;

		try {
			setUndecorated(false);
			jbInit();
			pack();
			CenterOnScreen.center(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		panel.setLayout(layout);

		sourceCombo = new JComboBox(sourcePlaces.toArray());
		Label sourceLabel = new Label("source place:");
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		layout.setConstraints(sourceLabel, constraints);
		panel.add(sourceLabel);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 0;
		layout.setConstraints(sourceCombo, constraints);
		panel.add(sourceCombo);

		sinkCombo = new JComboBox(sinkPlaces.toArray());
		Label sinkLabel = new Label("sink place:");
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 0;
		constraints.gridy = 1;
		layout.setConstraints(sinkLabel, constraints);
		panel.add(sinkLabel);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 1;
		layout.setConstraints(sinkCombo, constraints);
		panel.add(sinkCombo);

		doneButton = new JButton("Done");
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 1;
		constraints.gridy = 2;
		layout.setConstraints(doneButton, constraints);
		panel.add(doneButton);

		sourceCombo.addActionListener(this);
		sinkCombo.addActionListener(this);
		doneButton.addActionListener(this);

		this.add(panel);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			petrinettoowfnet.setSourcePlace((Place) sourceCombo
					.getSelectedItem());
			petrinettoowfnet.setSinkPlace((Place) sinkCombo.getSelectedItem());
			dispose();
		}
	}
}
