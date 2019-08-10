package org.processmining.analysis.sample;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;

public class SampleAnalysisPlugin {
	@Analyzer(name = "Sample Log analyzer", names = { "Log" })
	public JPanel analyze(LogReader log) {
		JPanel result = new JPanel();

		result.add(new JLabel(log.getFile().toString()));
		return result;
	}

	@Analyzer(name = "Sample Petri net analyzer", names = { "Petri net" })
	public JLabel analyze(PetriNet net) {
		return new JLabel(net.getIdentifier());
	}

	@Analyzer(name = "Sample double Petri net analyzer", names = {
			"Petri net A", "Petri net B" }, connected = false)
	public JPanel analyze(PetriNet a, PetriNet b) {
		JPanel result = new JPanel();

		result.add(new JLabel(a.getIdentifier()));
		result.add(new JLabel(b.getIdentifier()));
		return result;
	}

	@Analyzer(name = "Sample Log with Petri net analyzer", names = { "Log",
			"Petri net" })
	public JComponent analyze(LogReader log, PetriNet net) {
		JPanel result = new JPanel();

		result.add(new JLabel(log.getFile().toString()));
		result.add(new JLabel(net.getIdentifier()));
		return result;
	}

	@Analyzer(name = "Sample Disconnected Log with Petri net analyzer", names = {
			"Petri net", "Log" }, connected = false)
	public JComponent analyzeDisconnected(PetriNet net, LogReader log) {
		JPanel result = new JPanel();

		result.add(new JLabel(log.getFile().toString()));
		result.add(new JLabel(net.getIdentifier()));
		return result;
	}
}
