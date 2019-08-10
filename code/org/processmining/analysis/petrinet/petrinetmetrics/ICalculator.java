package org.processmining.analysis.petrinet.petrinetmetrics;

/**
 * @author Daniel Teixeira and Jo�o Sobrinho
 */
public interface ICalculator {
	public String Calculate();

	public String VerifyBasicRequirements();

	public String getType();

	public String getName();
}
