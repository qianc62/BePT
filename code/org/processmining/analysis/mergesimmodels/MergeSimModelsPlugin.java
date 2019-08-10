package org.processmining.analysis.mergesimmodels;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.plugin.ProvidedObject;

public class MergeSimModelsPlugin implements AnalysisPlugin {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/mergesimmodels";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem("Simulation Model",
				2, Integer.MAX_VALUE) {

			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				// int counterSimModels = 0;
				boolean simModel = false;

				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HLModel) {
						// counterSimModels++;
						simModel = true;
						break;
					}
				}
				// return (counterSimModels > 1);
				return simModel;
			}
		} };
		return items;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Merge Simulation Models";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] inputs) {
		ProvidedObject[] provObjs = inputs[0].getProvidedObjects();
		// put the objects of the provided objects in arrayList o
		ArrayList o = new ArrayList();
		for (int i = 0; i < provObjs.length; i++) {
			Object[] objects = provObjs[i].getObjects();
			for (int j = 0; j < objects.length; j++) {
				o.add(objects[j]);
			}
		}
		ArrayList<HLModel> activityModels = new ArrayList<HLModel>();

		for (int i = 0; i < o.size(); i++) {
			if (o.get(i) instanceof HLModel) {
				activityModels.add((HLModel) o.get(i));
			}
		}
		boolean cancelButtonClicked = false;
		// the arrayList in which the mapped models have to be saved
		ArrayList<HLModel> mappedModels = new ArrayList<HLModel>();
		//
		ReferenceModelChooser refChooser = new ReferenceModelChooser(
				activityModels);
		if (refChooser.showModal()) {
			// get the selected reference model
			HLModel referenceModel = refChooser.getSelectedReferenceModel();
			mappedModels.add(referenceModel);
			// For each of the other activity models a mapping needs to be made
			// from the activity nodes in the
			// reference model to the highlevelactivities in the other activity
			// models.
			// Therefore, for each other activity model a screen needs to pop on
			// which the user can define the mapping
			Iterator<HLModel> actModelsIt = refChooser
					.getNotSelectedReferenceModels().iterator();
			while (actModelsIt.hasNext()) {
				HLModel activityModel = actModelsIt.next();
				// make the mapping
				// first, clone the reference model and the activity model
				HLModel clonedReferenceModel = (HLModel) referenceModel.clone();
				clonedReferenceModel.reset();
				HLModel clonedActivityModel = (HLModel) activityModel.clone();
				ReferenceModelMapping refModMapping = new ReferenceModelMapping(
						clonedReferenceModel, clonedActivityModel);
				if (refModMapping.showModal()) {
					mappedModels.add(refModMapping.getMappedModel());
				} else {
					cancelButtonClicked = true;
					break;
				}
			}
		} else {
			cancelButtonClicked = true;
		}

		if (!cancelButtonClicked) {
			return new MergeSimModelsUI(mappedModels);
		}
		return null;
	}

}
