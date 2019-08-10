package org.processmining.analysis.hmm;

/**
 * Configuration class holding the parameters of the experiment to be performed.
 */
public class HmmExpConfiguration {

	public enum BaseLine {
		MODEL, LOG
	}

	private static HmmExpConfiguration singleton = new HmmExpConfiguration();
	private int noiseLevels = 20; // between 1 and 100 (additional log without
	// noise is implied)
	private int traces = 100; // number of traces to be generated per noise
	// level (at least one)
	private int traceLength = 100; // maximum number of events per trace
	private BaseLine referencePoint; // whether to use model-based or log-based
	// Hmm for noise generation
	private boolean replicate = true;
	private int replications = 50; // number of replications per noise level

	private HmmExpConfiguration() {
	}

	public static HmmExpConfiguration getInstance() {
		return singleton;
	}

	public int getNoiseLevels() {
		return noiseLevels;
	}

	public void setNoiseLevels(int noiseLevels) {
		this.noiseLevels = noiseLevels;
	}

	public int getTraces() {
		return traces;
	}

	public void setTraces(int traces) {
		this.traces = traces;
	}

	public int getTraceLength() {
		return traceLength;
	}

	public void setTraceLength(int traceLength) {
		this.traceLength = traceLength;
	}

	public BaseLine getReferencePoint() {
		return referencePoint;
	}

	public void setReferencePoint(BaseLine referencePoint) {
		this.referencePoint = referencePoint;
	}

	public boolean isReplicate() {
		return replicate;
	}

	public void setReplicate(boolean replicate) {
		this.replicate = replicate;
	}

	public int getReplications() {
		return replications;
	}

	public void setReplications(int replications) {
		this.replications = replications;
	}
}
