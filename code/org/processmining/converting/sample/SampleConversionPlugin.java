package org.processmining.converting.sample;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.converting.Converter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.mining.MiningResult;
import org.processmining.mining.heuristicsmining.HeuristicsMiner;

public class SampleConversionPlugin {
	@Converter(name = "Sample converter which uses the heuristics miner")
	public static MiningResult convertHN(LogReader log) {
		HeuristicsMiner miner = new HeuristicsMiner();

		miner.getOptionsPanel(log.getLogSummary());
		return miner.mine(log);
	}

	@Converter(name = "Sample petri net conversion with MiningResult")
	public static JLabel convert(PetriNet net) {
		return new JLabel(net.getIdentifier());
	}

	@Converter(name = "Sample log conversion plugin")
	public static JComponent convert(LogReader log) {
		return new JLabel(log.getFile().toString());
	}
}
