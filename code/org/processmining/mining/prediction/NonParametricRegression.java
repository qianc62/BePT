package org.processmining.mining.prediction;

import java.util.*;
import org.processmining.framework.ui.Progress;

/**
 * @author Ronald Crooy
 * 
 */
public class NonParametricRegression extends Thread {

	protected ArrayList<String> originalVarNames;
	protected ArrayList<String> bandVarNames;
	protected ArrayList<String> trainVarNames;
	protected ArrayList<String> valVarNames;

	protected ArrayList<String> toBeRemoved;
	protected SingleRegressionResult sresult;
	protected boolean busy;

	protected RConnector rconnection;

	private PredictionMinerSettingsBasedOnLogSummary localSettings;

	/**
	 * initiate variables and get Rconnection from the settings
	 * 
	 * @param settings
	 */
	public NonParametricRegression(RConnector connection) {
		localSettings = PredictionMinerSettingsBasedOnLogSummary.getInstance();

		originalVarNames = new ArrayList<String>();
		bandVarNames = new ArrayList<String>();
		trainVarNames = new ArrayList<String>();
		valVarNames = new ArrayList<String>();

		toBeRemoved = new ArrayList<String>();

		sresult = new SingleRegressionResult();

		rconnection = connection;
		// clean();
	}

	/**
	 * start bandwidth selection and test the bandwidth and the average
	 * predictor
	 */
	public void run() {
		busy = true;
		for (String name : this.toBeRemoved) {
			this.bandVarNames.remove(name);
			this.valVarNames.remove(name);
			this.trainVarNames.remove(name);
			rconnection.eval("rm(" + name + ")");
		}

		sresult.setNames(originalVarNames);

		setDataFrames(bandVarNames, "xBand");
		setDataFrames(trainVarNames, "xTrain");
		setDataFrames(valVarNames, "xVal");

		if (!localSettings.crossvalidate) {
			rconnection.eval("xVal<-xTrain");
			rconnection.eval("yVal<-yTrain");
		}

		findBandwidth();

		testValidationData((localSettings.target == 2));
		testAvgPrediction((localSettings.target == 2));

		clean();
		busy = false;
	}

	/**
	 * reset all variables and R
	 */
	private void clean() {
		rconnection.eval("rm(list=ls(all=TRUE))");
	}

	/**
	 * This function calculates the bandwidth, all data is expected to be
	 * imported at this point.
	 */
	private void findBandwidth() {
		Rresult result = null;
		String set = null;
		if (!(localSettings.useCompleteTrainingForBandwidth)) {
			set = "Band";
		} else {
			set = "Train";
		}
		result = rconnection.eval("bw<-npregbw(xdat=x" + set + ",ydat=y" + set
				+ "," + "tol=" + localSettings.tol + "," + "ftol="
				+ localSettings.tol + "," + "ckertype='"
				+ localSettings.getcker() + "'" + "," + "okertype='"
				+ localSettings.getoker() + "'" + "," + "ukertype='"
				+ localSettings.getuker() + "'" + ")");
		result = rconnection.eval("bw$bw");
		try {
			sresult.setPrediction(result.asDoubleArray(), 6);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * this function must be called AFTER bandwidth is set or calculated, the
	 * bandwidth will be tested and results will be stored
	 * 
	 * @param factor
	 */
	private void testValidationData(boolean factor) {
		Rresult result = null;
		rconnection
				.eval("bw2<-npregbw(bws=bw$bw,bandwidth.compute=FALSE,xdat=xTrain,ydat=yTrain"
						+ ","
						+ "ckertype='"
						+ localSettings.getcker()
						+ "'"
						+ ","
						+ "okertype='"
						+ localSettings.getoker()
						+ "'"
						+ ","
						+ "ukertype='"
						+ localSettings.getuker()
						+ "'"
						+ ")");
		// if (localSettings.crossvalidate){
		rconnection
				.eval("results<-npreg(exdat=xVal,eydat=yVal,txdat=xTrain,tydat=yTrain,bws=bw2)");
		rconnection.eval("estimations<-results$mean");
		rconnection
				.eval("for(i in 1:length(estimations)){ if (is.nan(estimations[i])){estimations[i]<-0;}}");
		rconnection.eval("abserror<-as.numeric(estimations)-as.numeric(yVal)");
		// }
		// else{
		// rconnection.eval("results<-npreg(txdat=xTrain,tydat=yTrain,bws=bw2)");
		// rconnection.eval("estimations<-results$mean");
		// rconnection.eval("for(i in 1:length(estimations)){ if (is.nan(estimations[i])){estimations[i]<-0;}}");
		// rconnection.eval("abserror<-as.numeric(estimations)-as.numeric(yTrain)");
		// }
		try {
			if (factor) {
				rconnection
						.eval("estimations<-round(as.numeric(results$mean))");
				rconnection.eval("yVal2<-as.numeric(yVal)-1");
				rconnection.eval("abserror<-estimations-yVal2");
				result = rconnection.eval("estimations");
				sresult.setPrediction(result.asDoubleArray(), 1);
				result = rconnection.eval("results$MSE");
				sresult.setResult(result.asDouble(), 1);
				result = rconnection.eval("results$MAPE");
				sresult.setResult(result.asDouble(), 2);
				result = rconnection.eval("results$MAE");
				sresult.setResult(result.asDouble(), 3);
				result = rconnection.eval("abserror");
				sresult.setPrediction(result.asDoubleArray(), 4);
				result = rconnection.eval("yVal2");
			} else {
				result = rconnection.eval("results$MSE");
				sresult.setResult(result.asDouble(), 1);
				result = rconnection.eval("results$MAPE");
				sresult.setResult(result.asDouble(), 2);
				result = rconnection.eval("results$MAE");
				sresult.setResult(result.asDouble(), 3);
				result = rconnection.eval("estimations");
				sresult.setPrediction(result.asDoubleArray(), 1);
				result = rconnection.eval("abserror");
				sresult.setPrediction(result.asDoubleArray(), 4);
				result = rconnection.eval("yVal");
			}
			sresult.setPrediction(result.asDoubleArray(), 3);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public double regressOnce(double[] bandwidth) {
		rconnection.assign("bw", bandwidth);
		rconnection
				.eval("bw2<-npregbw(bws=bw,bandwidth.compute=FALSE,xdat=xTrain,ydat=yTrain"
						+ ","
						+ "ckertype='"
						+ localSettings.getcker()
						+ "'"
						+ ","
						+ "okertype='"
						+ localSettings.getoker()
						+ "'"
						+ ","
						+ "ukertype='"
						+ localSettings.getuker()
						+ "'"
						+ ")");
		rconnection
				.eval("results<-npreg(exdat=xVal,txdat=xTrain,tydat=yTrain,bws=bw2)");
		Rresult result = rconnection.eval("estimations<-results$mean");
		double estimation = 0.0;
		try {
			estimation = result.asDouble();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return estimation;
	}

	/**
	 * this function needs all variables to be set, it will calculate the
	 * performance for an average based estimation
	 * 
	 * @param factor
	 */
	private void testAvgPrediction(boolean factor) {
		Rresult reresult = null;
		if (factor) {
			rconnection.eval("avgval=round(mean(as.numeric(yTrain)))");
			rconnection.eval("avgEstimations<-1:length(yVal)");
			rconnection.eval("avgEstimations<-avgEstimations*0+avgval");
			rconnection.eval("absAvgEstError=as.numeric(yVal)-avgEstimations");
			rconnection
					.eval("for(i in 1:length(absAvgEstError)){ absAvgEstError[i]<-abs(absAvgEstError[i])}");
		} else {
			rconnection.eval("avgdur=mean(durations)");
			rconnection.eval("avgEstimations=avgdur-timesofar");
			rconnection
					.eval("for(i in 1:length(avgEstimations)){ avgEstimations[i]<-max(avgEstimations[i],0)}");
			rconnection.eval("absAvgEstError=avgEstimations-yVal");
		}
		try {
			if (factor) {
				reresult = rconnection.eval("avgEstimations-1");
			} else {
				reresult = rconnection.eval("avgEstimations");
			}

			sresult.setPrediction(reresult.asDoubleArray(), 2);
			reresult = rconnection.eval("absAvgEstError");
			sresult.setPrediction(reresult.asDoubleArray(), 5);
			reresult = rconnection.eval("mean(absAvgEstError^2)");
			sresult.setResult(reresult.asDouble(), 4);
			if (factor) {
				reresult = rconnection
						.eval("mean(abs(absAvgEstError))/mean(round(as.numeric(yVal)))");
			} else {
				reresult = rconnection
						.eval("mean(abs(absAvgEstError))/mean(yVal)");
			}
			sresult.setResult(reresult.asDouble(), 5);
			reresult = rconnection.eval("mean(abs(absAvgEstError))");
			sresult.setResult(reresult.asDouble(), 6);
		} catch (Exception e) {
			System.out.println(e);
		}
		// debugging code below
		/*
		 * if (true) {
		 * System.out.println("Now the console is yours ... have fun");
		 * re.startMainLoop(); } else { re.end(); System.out.println("end"); }
		 */
	}

	/**
	 * this is a private helper function that uses a list of strings(names of
	 * lists) and a name to create a dataframe under the name 'framename' with
	 * the given variables as tables
	 * 
	 * @param names
	 * @param framename
	 */
	public void setDataFrames(ArrayList<String> names, String framename) {
		String name = null;
		String command = null;
		if (names.size() > 0) {
			Iterator<String> nameit = names.iterator();
			name = nameit.next();
			command = framename + "<-data.frame(" + name;
			while (nameit.hasNext()) {
				name = nameit.next();
				command += ',' + name;
			}
			command += ')';

			rconnection.eval(command, false);
		}
	}

	public void importNewVariable(Double[] values, String name, String postfix,
			ArrayList<String> place) {
		double[] temp = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			temp[i] = values[i];
		}
		importNewVariable(temp, name, postfix, place);
	}

	public void importNewVariable(double[] values, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, values);
		double variance = 1.0;
		try {
			variance = rconnection.eval("var(" + name + postfix + ")")
					.asDouble();
		} catch (Exception e) {
			System.out.println(e);
		}
		Boolean removeconstant = false;
		removeconstant = !(variance > 0);
		// if all of the bandwidth must be used, ignore constants in the band
		// set. it will not be used
		if (localSettings.useCompleteTrainingForBandwidth
				&& postfix.equals("_duration_band")) {
			removeconstant = false;
		}

		if (!removeconstant) {

			if (!(place == null)) {
				place.add(name + postfix);
				if (!this.originalVarNames.contains(name + "_duration")) {
					this.originalVarNames.add(name + "_duration");
				}
			}
			// System.out.println(name+" : "+ variance);
		} else {
			this.toBeRemoved.add(name + "_duration_val");
			this.toBeRemoved.add(name + "_duration_train");
			this.toBeRemoved.add(name + "_duration_band");
			System.out.println(name + "_duration removed, variance of " + name
					+ postfix + " is : " + variance);
			// System.out.println("removing "+name);
			// rconnection.eval("rm("+name+")");

		}

	}

	public void importNewVariable(double value, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, value);
		if (!(place == null)) {
			place.add(name + postfix);
			if (!this.originalVarNames.contains(name + "_duration")) {
				this.originalVarNames.add(name + "_duration");
			}
		}
	}

	public void importNewVariable(String[] values, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, values);
		rconnection.eval(name + postfix + "<-factor(" + name + postfix + ")",
				false);
		if (!(place == null)) {
			place.add(name + postfix);
			if (!this.originalVarNames.contains(name + "_attribute")) {
				this.originalVarNames.add(name + "_attribute");
			}
		}
	}

	public void importNewVariable(String value, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, value);
		rconnection.eval(name + postfix + "<-factor(" + name + postfix + ")",
				false);
		if (!(place == null)) {
			place.add(name + postfix);
			if (!this.originalVarNames.contains(name + "_attribute")) {
				this.originalVarNames.add(name + "_attribute");
			}
		}
	}

	public void importNewVariable(Integer[] values, String name,
			String postfix, ArrayList<String> place) {
		int[] temp = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			temp[i] = values[i];
		}
		importNewVariable(temp, name, postfix, place);
	}

	public void importNewVariable(int[] values, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, values);
		rconnection.eval(name + postfix + "<-ordered(" + name + postfix + ")",
				false);
		if (!(place == null)) {
			place.add(name + postfix);
			if (!this.originalVarNames.contains(name + "_occurrence")) {
				this.originalVarNames.add(name + "_occurrence");
			}
		}
	}

	public void importNewVariable(int value, String name, String postfix,
			ArrayList<String> place) {
		rconnection.assign(name + postfix, value);
		rconnection.eval(name + postfix + "<-ordered(" + name + postfix + ")",
				false);
		if (!(place == null)) {
			place.add(name + postfix);
			if (!this.originalVarNames.contains(name + "_occurrence")) {
				this.originalVarNames.add(name + "_occurrence");
			}
		}
	}
}
