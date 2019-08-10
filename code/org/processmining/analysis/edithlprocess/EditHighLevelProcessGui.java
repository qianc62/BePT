package org.processmining.analysis.edithlprocess;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.gui.HLProcessGui;
import org.processmining.framework.models.hlprocess.hlmodel.HLModel;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.MiningResult;

public class EditHighLevelProcessGui extends JPanel implements Provider,
		MiningResult {

	protected HLProcessGui processGui;
	protected HLModel hlProcessImpl;
	protected HLProcess hlProcess;

	/**
	 * Creates a high-level process GUI based on a HLModel plus its HLProcess.
	 * <p>
	 * A generic visualization will be provided to highlight nodes in the
	 * process model that are associated to the high level information.
	 * 
	 * @param process
	 *            the HLModel of whicht the high-level information should be
	 *            made editable
	 */
	public EditHighLevelProcessGui(HLModel process) {
		hlProcessImpl = process;
		hlProcess = process.getHLProcess();
		processGui = new HLProcessGui(process);
		jbInit();
	}

	/**
	 * Creates a high-level process GUI based on an HLProcess only. <br>
	 * This means that there is no HLModel available that links high-level
	 * process information to certain nodes in the process model.
	 * 
	 * @param hlprocess
	 *            the HLProcess to be made editable
	 */
	public EditHighLevelProcessGui(HLProcess hlprocess) {
		hlProcess = hlprocess;
		processGui = new HLProcessGui(hlprocess);
		jbInit();
	}

	protected void jbInit() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(processGui.getPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (hlProcessImpl != null) {
			return new ProvidedObject[] {
					new ProvidedObject("High-level Process",
							new Object[] { hlProcessImpl }),
					new ProvidedObject("Process Information",
							new Object[] { hlProcess }) };
		} else {
			return new ProvidedObject[] { new ProvidedObject(
					"Process Information", new Object[] { hlProcess }) };
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		// no log - only implementing interface
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		// window content is the high level process view itself
		return this;
	}

}
