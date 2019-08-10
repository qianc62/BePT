/**
 * 
 */
package org.processmining.converting.erlangnet2erlang;

import org.processmining.converting.ConvertingPlugin;
import org.processmining.framework.models.erlangnet.ErlangNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.mining.MiningResult;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * @author Kristian Bisgaard Lassen
 * 
 */
public class ErlangNet2Erlang implements ConvertingPlugin {

	/**
	 * @see org.processmining.converting.ConvertingPlugin#accepts(org.processmining.framework.plugin.ProvidedObject)
	 */
	public boolean accepts(ProvidedObject original) {
		for (Object o : original.getObjects())
			if (o instanceof ErlangNet)
				return true;
		return false;
	}

	/**
	 * @see org.processmining.converting.ConvertingPlugin#convert(org.processmining.framework.plugin.ProvidedObject)
	 */
	public MiningResult convert(ProvidedObject object) {
		ErlangNet providedPN = null;
		// LogReader log = null;

		for (int i = 0; providedPN == null && i < object.getObjects().length; i++) {
			if (object.getObjects()[i] instanceof ErlangNet) {
				providedPN = (ErlangNet) object.getObjects()[i];
			}
			// if (object.getObjects()[i] instanceof LogReader) {
			// log = (LogReader) object.getObjects()[i];
			// }
		}

		if (providedPN == null) {
			return null;
		}

		ErlangWorkflowNet wfnet = new ErlangNet2ErlangWorkflowNetConverter()
				.convert(providedPN);

		return new PetriNetResult(wfnet);
	}

	/**
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/**
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Petri net to Erlang";
	}

}
