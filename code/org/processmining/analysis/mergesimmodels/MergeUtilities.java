package org.processmining.analysis.mergesimmodels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;

/**
 * Utility classes used by the various merging tab UIs.
 */
public class MergeUtilities {

	/**
	 * In the case that simulation models have the same names, unique names will
	 * be generated, so that each simulation model in the list has an unique
	 * name
	 * 
	 * @param simModels
	 *            ArrayList list with simulation models
	 * @return ArrayList list with simulation models in which each model has an
	 *         unique name
	 */
	public static ArrayList<HLModel> generateUniqueNameSimModel(
			ArrayList<HLModel> simModels) {
		ArrayList<HLModel> returnModels = new ArrayList<HLModel>();
		// check whether there are some names of the simModels the same
		HashSet<String> usedNames = new HashSet<String>();
		Iterator<HLModel> simModelNames = simModels.iterator();
		while (simModelNames.hasNext()) {
			HLModel simModel = simModelNames.next();
			if (usedNames.contains(simModel.getHLProcess().getGlobalInfo()
					.getName())) {
				// find another name for this simModel
				int counter = 2;
				while (usedNames.contains(simModel.getHLProcess()
						.getGlobalInfo().getName()
						+ counter)) {
					// increment the counter
					counter++;
				}
				// unique name found, add it to the used names list
				usedNames.add(simModel.getHLProcess().getGlobalInfo().getName()
						+ counter);
				// add set it as name for the respective simulation model
				simModel.getHLProcess().getGlobalInfo().setName(
						simModel.getHLProcess().getGlobalInfo().getName()
								+ counter);
				// add it to the models that have to be returned
				returnModels.add(simModel);
			} else {
				// add to the used names list
				usedNames
						.add(simModel.getHLProcess().getGlobalInfo().getName());
				// add it to the models that have to be returned
				returnModels.add(simModel);
			}
		}
		return returnModels;
	}

	/**
	 * Retrieves the simulation models in the list which cover the given
	 * perspective
	 * 
	 * @param simModels
	 *            ArrayList list with simulation models
	 * @param p
	 *            Perspective the perspective
	 * @return ArrayList the simulation models in the list which cover the given
	 *         perspective
	 */
	public static ArrayList<HLModel> getSimModelsWithPerspective(
			ArrayList<HLModel> simModels, HLTypes.Perspective p) {
		ArrayList<HLModel> returnSimModels = new ArrayList<HLModel>();
		Iterator<HLModel> simModelsIt = simModels.iterator();
		while (simModelsIt.hasNext()) {
			HLModel simModel = simModelsIt.next();
			if (simModel.getHLProcess().getGlobalInfo().getPerspectives()
					.contains(p)) {
				returnSimModels.add(simModel);
			}
		}
		return returnSimModels;
	}

}
