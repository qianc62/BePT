package org.processmining.converting.wfnet2bpel;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningResult;

/**
 * <p>
 * Title: WorkflowNet2BPEL4WS
 * </p>
 * 
 * <p>
 * Description: Conversion plug-in to convert at Petri net into a BPEL
 * specification.
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
public class WorkflowNet2BPEL4WS implements ConvertingPlugin {

	/**
	 * @see org.processmining.exporting.ExportPlugin#accepts(org.processmining.framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject object) {
		for (Object o : object.getObjects())
			if (o instanceof PetriNet)
				return true;
		return false;
	}

	/**
	 * @see org.processmining.exporting.ExportPlugin#export(org.processmining.framework.plugin.ProvidedObject,
	 *      java.io.OutputStream)
	 */
	public MiningResult convert(ProvidedObject object) {
		PetriNet providedPN = null;
		LogReader log = null;

		for (int i = 0; providedPN == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof PetriNet) {
				providedPN = (PetriNet) object.getObjects()[i];
			}
			if (object.getObjects()[i] instanceof LogReader) {
				log = (LogReader) object.getObjects()[i];
			}
		}

		if (providedPN == null) {
			return null;
		}

		// if (false) {
		// batchTest();
		// }
		return new Converter().convert(log, providedPN);
		// BPEL model = new Converter().convert(log,providedPN);
		// if (model != null)
		// Message.add(model.getProcess().toString());
		//
		// return new BPELResult(log, model);
	}

	// private void batchTest() {
	// String path =
	// "/Users/kbl/PhD/Implementationer/WorkflowNet to BPEL4WS/BPM course (CPN)";
	// List<Pair<String, PetriNet>> nets = new ArrayList<Pair<String,
	// PetriNet>>();
	// getTPNFiles(path, nets);
	// Converter converter1 = new Converter();
	// Converter converter2 = new Converter();
	// converter1.askForComponent = false;
	// for (Pair<String, PetriNet> petriNet : nets) {
	// converter1.count = 0;
	// converter1.path = petriNet.first.substring(0,
	// petriNet.first.lastIndexOf(".")) + ".C";
	// System.err.println(petriNet.first);
	// converter1.convert(petriNet.second);
	// System.err.println("and again....");
	// converter2.convert(petriNet.second);
	// }
	// }
	//
	// private void getTPNFiles(String path, List<Pair<String, PetriNet>> nets)
	// {
	// for (File file : new File(path).listFiles(new FileFilter() {
	// public boolean accept(File file) {
	// return file.getName().endsWith(".tpn");
	// }
	// })) {
	// try {
	// nets.add(Pair.create(file.getAbsolutePath(), new TpnImport()
	// .importFile(new FileInputStream(file)).getPetriNet()));
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// for (File file : new File(path).listFiles())
	// if (file.isDirectory())
	// getTPNFiles(file.getAbsolutePath(), nets);
	// }

	/**
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "BPEL 1.1";
	}

	/**
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/downloads/prom/WFnet2BPEL.pdf";
	}

}
