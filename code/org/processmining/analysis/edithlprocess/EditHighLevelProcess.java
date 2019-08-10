package org.processmining.analysis.edithlprocess;

import javax.swing.JComponent;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.hlprocess.hlmodel.HLYAWL;
import org.processmining.framework.models.hlprocess.hlmodel.HLProtos;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.yawl.YAWLModel;
import org.processmining.framework.models.protos.ProtosModel;
import org.processmining.framework.models.protos.ProtosSubprocess;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * Displays the high level information for the provided process in a generic
 * way.
 * 
 * @author Anne Rozinat
 */
public class EditHighLevelProcess implements AnalysisPlugin {

	/**
	 * Specify the name of the plugin.
	 * 
	 * @return The name of the plugin.
	 */
	public String getName() {
		return "View/Edit High Level Process";
	}

	/**
	 * Provide user documentation for the plugin.
	 * 
	 * @return The Html body of the documentation page.
	 */
	public String getHtmlDescription() {
		return "http://prom.win.tue.nl/research/wiki/online/edithlprocess";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.analysis.AnalysisPlugin#getInputItems()
	 */
	public AnalysisInputItem[] getInputItems() {
		AnalysisInputItem[] items = { new AnalysisInputItem(
				"High Level Process") {
			public boolean accepts(ProvidedObject object) {
				Object[] o = object.getObjects();
				for (int i = 0; i < o.length; i++) {
					if (o[i] instanceof HLModel || o[i] instanceof HLProcess
							|| o[i] instanceof PetriNet
							|| o[i] instanceof YAWLModel ||
							// added by Mariska Netjes
							o[i] instanceof ProtosModel) {
						return true;
					}
				}
				return false;
			}
		} };
		return items;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.AnalysisPlugin#analyse(org.processmining.analysis
	 * .AnalysisInputItem[])
	 */
	public JComponent analyse(AnalysisInputItem[] analysisInputItemArray) {
		AnalysisInputItem PNLog = analysisInputItemArray[0];
		Object[] o = PNLog.getProvidedObjects()[0].getObjects();
		EditHighLevelProcessGui hlProcessGui = null;
		for (int i = 0; i < o.length; i++) {
			if (o[i] instanceof HLModel) {
				hlProcessGui = new EditHighLevelProcessGui((HLModel) o[i]);
			} else if (o[i] instanceof HLProcess) {
				hlProcessGui = new EditHighLevelProcessGui((HLProcess) o[i]);
				// for convenience: allow to create high-level versions of plain
				// control-flow
				// models without the need to call one of the analysis plugins
				// that creates one
			} else if (o[i] instanceof PetriNet) {
				hlProcessGui = new EditHighLevelProcessGui(new HLPetriNet(
						(PetriNet) o[i]));
			} else if (o[i] instanceof YAWLModel) {
				hlProcessGui = new EditHighLevelProcessGui(new HLYAWL(
						(YAWLModel) o[i]));
				// added by Mariska Netjes
			} else if (o[i] instanceof ProtosModel) {
				hlProcessGui = new EditHighLevelProcessGui(new HLProtos(
						(ProtosModel) o[i]));
			}
		}
		return hlProcessGui;
	}

}
