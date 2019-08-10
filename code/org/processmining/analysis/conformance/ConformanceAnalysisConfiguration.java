/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.conformance;

import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisConfiguration;
import org.processmining.framework.models.petrinet.algorithms.logReplay.AnalysisMethodEnum;

/**
 * Creates a configuration object that contains all the analysis categories and
 * options that are available for the ConformanceAnalysisPlugin.
 * 
 * @author arozinat
 */
public class ConformanceAnalysisConfiguration extends AnalysisConfiguration {

	public ConformanceAnalysisConfiguration() {
		// f
		AnalysisConfiguration f_option = new AnalysisConfiguration();
		f_option.setName("f");
		f_option
				.setToolTip("Degree of fit based on missing and remaining tokens in the model during log replay");
		f_option
				.setDescription("The token-based <b>fitness</b> metric <i>f</i> relates the amount of missing tokens during log replay with the amount of consumed ones and "
						+ "the amount of remaining tokens with the produced ones. If the log could be replayed correctly, that is, there were no tokens missing nor remaining, it evaluates to 1.");
		f_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		// degree or successful execution
		AnalysisConfiguration fractionSE_option = new AnalysisConfiguration();
		fractionSE_option.setName("pSE");
		fractionSE_option
				.setToolTip("The fraction of successfully executed process instances");
		fractionSE_option
				.setDescription("The <b>successful execution</b> metric <i>p<sub>SE</sub></i> determines the fraction of successfully executed process instances (taking the number of occurrences per trace into account).");
		fractionSE_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		// degree of proper completion
		AnalysisConfiguration fractionPC_option = new AnalysisConfiguration();
		fractionPC_option.setName("pPC");
		fractionPC_option
				.setToolTip("The fraction of properly completed process instances");
		fractionPC_option
				.setDescription("The <b>proper completion</b> metric <i>p<sub>PC</sub></i> determines the fraction of properly completed process instances (taking the number of occurrences per trace into account).");
		fractionPC_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		// // build fitness section
		AnalysisConfiguration fitnessOptions = new AnalysisConfiguration();
		fitnessOptions.setName("Fitness");
		fitnessOptions.setToolTip("Fitness Analysis");
		fitnessOptions
				.setDescription("Fitness evaluates whether the observed process <i>complies with</i> the control flow specified by the process. "
						+ "One way to investigate the fitness is to replay the log in the Petri net. The log replay is carried out in a non-blocking way, i.e., if there are tokens missing "
						+ "to fire the transition in question they are created artificially and replay proceeds. While doing so, diagnostic data is collected and can be accessed afterwards.");
		fitnessOptions.addChildConfiguration(f_option);
		fitnessOptions.addChildConfiguration(fractionSE_option);
		fitnessOptions.addChildConfiguration(fractionPC_option);
		// indicate the type of analysis method that is needed
		// TODO - check whether this cannot already implicitly be determined
		fitnessOptions.addRequestedMethod(AnalysisMethodEnum.LOG_REPLAY);

		// saB
		AnalysisConfiguration aB_option = new AnalysisConfiguration();
		aB_option.setSelected(false);
		aB_option.setName("saB");
		aB_option
				.setToolTip("Simple behavioral appropriateness based on the mean number of enabled transitions");
		aB_option
				.setDescription("The <b>simple behavioral appropriateness</b> metric <i>sa<sub>B</sub></i> is based on the mean number of enabled transitions during log replay "
						+ "(the greater the value the less behavior is allowed by the process model and the more precisely the behavior observed in the log is captured). "
						+ "Note that this metric should only be used as a comparative means for models without alternative duplicate tasks. "
						+ "Note further that in order to determine the mean number of enabled tasks in the presence of invisible tasks requires to build the state space "
						+ "from the current marking after each replay step. Since this may greatly decrease the performance of the computational process, you might want to swich this feature off.");
		aB_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		// aaB
		AnalysisConfiguration aaB_option = new AnalysisConfiguration();
		aaB_option.setName("aaB");
		aaB_option
				.setToolTip("Advanced behavioral appropriateness based on activity relations that were not observed i the log");
		aaB_option
				.setDescription("The <b>advanced behavioral appropriateness</b> metric <i>aa<sub>B</sub></i> is based on successorship relations among activities with respect the event relations observed  in the log "
						+ "(the greater the value the more precisely the behavior observed in the log is captured).");
		aaB_option.setNewAnalysisMethod(AnalysisMethodEnum.LOG_REPLAY);
		aaB_option.setNewAnalysisMethod(AnalysisMethodEnum.STATE_SPACE);
		// // build behavioral appropriateness section
		AnalysisConfiguration behAppropOptions = new AnalysisConfiguration();
		behAppropOptions.setName("Precision");
		behAppropOptions.setToolTip("Behavioral Appropriateness Analysis");
		behAppropOptions
				.setDescription("Precision, or Behavioral Appropriateness, evaluates <i>how precisely</i> the model describes the observed process.");
		behAppropOptions.addChildConfiguration(aB_option);
		behAppropOptions.addChildConfiguration(aaB_option);
		// indicate the type of analysis method that is needed
		// TODO - check whether this cannot already implicitly be determined
		behAppropOptions.addRequestedMethod(AnalysisMethodEnum.LOG_REPLAY);
		behAppropOptions.addRequestedMethod(AnalysisMethodEnum.STATE_SPACE); // for
		// improved
		// metric!

		// saS
		AnalysisConfiguration aS_option = new AnalysisConfiguration();
		aS_option.setSelected(false);
		aS_option.setName("saS");
		aS_option
				.setToolTip("Simple structural appropriateness based on the size of the process model");
		aS_option
				.setDescription("The <b>simple structural appropriateness</b> metric <i>sa<sub>S</sub></i> is a simple metric based on the graph size of the model "
						+ "(the greater the value the more compact is the model). "
						+ "Note that this metric should only be used as a comparative means for models allowing for the same amount of behavior.");
		aS_option.setNewAnalysisMethod(AnalysisMethodEnum.STRUCTURAL);
		// / aaS
		AnalysisConfiguration aaS_option = new AnalysisConfiguration();
		aaS_option.setName("aaS");
		aaS_option
				.setToolTip("Advanced structural appropriateness based on the punishement of redundant invisible and alternative duplicate tasks.");
		aaS_option
				.setDescription("The <b>advanced structural appropriateness</b> metric <i>aa<sub>S</sub></i> is based on the detection of redundant invisible tasks (simply superfluous) "
						+ "and alternative duplicate tasks (list alternative behavior rather than expressing it in a meaningful way).");
		aaS_option.setNewAnalysisMethod(AnalysisMethodEnum.STATE_SPACE);
		aaS_option.setNewAnalysisMethod(AnalysisMethodEnum.STRUCTURAL);
		// // build structural appropriatness section
		AnalysisConfiguration structAppropOptions = new AnalysisConfiguration();
		structAppropOptions.setName("Structure");
		structAppropOptions.setToolTip("Structural Appropriateness Analysis");
		structAppropOptions
				.setDescription("Structural Appropriateness evaluates whether the model describes the observed process in a <i>structurally suitable</i> way.");
		structAppropOptions.addChildConfiguration(aS_option);
		structAppropOptions.addChildConfiguration(aaS_option);
		// indicate the type of analysis method that is needed
		// TODO - check whether this cannot already implicitly be determined
		structAppropOptions.addRequestedMethod(AnalysisMethodEnum.STRUCTURAL);
		structAppropOptions.addRequestedMethod(AnalysisMethodEnum.STATE_SPACE); // for
		// improved
		// metric!

		// // add to root element
		this.addChildConfiguration(fitnessOptions);
		this.addChildConfiguration(behAppropOptions);
		this.addChildConfiguration(structAppropOptions);
	}
}
