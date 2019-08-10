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

package org.processmining.mining.snamining.miningoperation;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.mining.snamining.SocialNetworkOptions;
import org.processmining.mining.snamining.miningoperation.handover.Handover_CCCDCM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_CCCDIM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_CCIDCM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_CCIDIM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_ICCDCM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_ICCDIM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_ICIDCM;
import org.processmining.mining.snamining.miningoperation.handover.Handover_ICIDIM;
import org.processmining.mining.snamining.miningoperation.reassignment.Reassignment_CM;
import org.processmining.mining.snamining.miningoperation.reassignment.Reassignment_IM;
import org.processmining.mining.snamining.miningoperation.similartask.Similartask_CC;
import org.processmining.mining.snamining.miningoperation.similartask.Similartask_ED;
import org.processmining.mining.snamining.miningoperation.similartask.Similartask_HD;
import org.processmining.mining.snamining.miningoperation.similartask.Similartask_SC;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_CCCDCM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_CCCDIM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_CCIDCM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_CCIDIM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_ICCDCM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_ICCDIM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_ICIDCM;
import org.processmining.mining.snamining.miningoperation.subcontract.Subcontract_ICIDIM;
import org.processmining.mining.snamining.miningoperation.workingtogether.Workingtogether_DWC;
import org.processmining.mining.snamining.miningoperation.workingtogether.Workingtogether_DWTC;
import org.processmining.mining.snamining.miningoperation.workingtogether.Workingtogether_SAR;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class OperationFactory {
	public OperationFactory() {
	}

	public static BasicOperation getOperation(int indexType,
			LogSummary summary, LogReader log) {
		BasicOperation object = null;
		log.reset();
		switch (indexType) {
		// SUBCONTRACTING //
		case (SocialNetworkOptions.SUBCONTRACTING
				+ SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Subcontract_ICCDCM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING + SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION):
			object = new Subcontract_ICCDIM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Subcontract_ICIDCM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING):
			object = new Subcontract_ICIDIM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING
				+ SocialNetworkOptions.CONSIDER_CAUSALITY
				+ SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Subcontract_CCCDCM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING
				+ SocialNetworkOptions.CONSIDER_CAUSALITY + SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION):
			object = new Subcontract_CCCDIM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING
				+ SocialNetworkOptions.CONSIDER_CAUSALITY + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Subcontract_CCIDCM(summary, log);
			break;
		case (SocialNetworkOptions.SUBCONTRACTING + SocialNetworkOptions.CONSIDER_CAUSALITY):
			object = new Subcontract_CCIDIM(summary, log);
			break;
		// HANDOVER_OF_WORK
		case (SocialNetworkOptions.HANDOVER_OF_WORK
				+ SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Handover_ICCDCM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK + SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION):
			object = new Handover_ICCDIM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Handover_ICIDCM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK):
			object = new Handover_ICIDIM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK
				+ SocialNetworkOptions.CONSIDER_CAUSALITY
				+ SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Handover_CCCDCM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK
				+ SocialNetworkOptions.CONSIDER_CAUSALITY + SocialNetworkOptions.CONSIDER_DIRECT_SUCCESSION):
			object = new Handover_CCCDIM(summary, log);
			break;

		case (SocialNetworkOptions.HANDOVER_OF_WORK
				+ SocialNetworkOptions.CONSIDER_CAUSALITY + SocialNetworkOptions.CONSIDER_MULTIPLE_TRANSFERS):
			object = new Handover_CCIDCM(summary, log);
			break;
		case (SocialNetworkOptions.HANDOVER_OF_WORK + SocialNetworkOptions.CONSIDER_CAUSALITY):
			object = new Handover_CCIDIM(summary, log);
			break;
		// WORKING_TOGETHER
		case (SocialNetworkOptions.WORKING_TOGETHER + SocialNetworkOptions.SIMULTANEOUS_APPEARANCE_RATIO):
			object = new Workingtogether_SAR(summary, log);
			break;
		case (SocialNetworkOptions.WORKING_TOGETHER + SocialNetworkOptions.DISTANCE_WITHOUT_CAUSALITY):
			object = new Workingtogether_DWC(summary, log);
			break;
		case (SocialNetworkOptions.WORKING_TOGETHER + SocialNetworkOptions.DISTANCE_WITH_CAUSALITY):
			object = new Workingtogether_DWTC(summary, log);
			break;
		// SIMILAR_TASK
		case (SocialNetworkOptions.SIMILAR_TASK + SocialNetworkOptions.EUCLIDIAN_DISTANCE):
			object = new Similartask_ED(summary, log);
			break;
		case (SocialNetworkOptions.SIMILAR_TASK + SocialNetworkOptions.CORRELATION_COEFFICIENT):
			object = new Similartask_CC(summary, log);
			break;
		case (SocialNetworkOptions.SIMILAR_TASK + SocialNetworkOptions.SIMILARITY_COEFFICIENT):
			object = new Similartask_SC(summary, log);
			break;
		case (SocialNetworkOptions.SIMILAR_TASK + SocialNetworkOptions.HAMMING_DISTANCE):
			object = new Similartask_HD(summary, log);
			break;
		// REASSIGNMENT
		case (SocialNetworkOptions.REASSIGNMENT + SocialNetworkOptions.MULTIPLE_REASSIGNMENT):
			object = new Reassignment_CM(summary, log);
			break;
		case (SocialNetworkOptions.REASSIGNMENT):
			object = new Reassignment_IM(summary, log);
			break;
		}
		return object;
	}
}
