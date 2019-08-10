/*
 * Created on May 23, 2005
 *
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * (c) 2005 Technische Universiteit Eindhoven, Christian W. Guenther
 * all rights reserved
 * 
 * LICENSE WARNING:
 * This code has been created within the realm of an STW project.
 * The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear.
 * Before this code can be released in any form, be it binary or source 
 * code, this issue has to be clarified with the STW.
 * Please do not add this file to any build or source export transferred
 * to anybody outside the TM.IS group.
 */
package org.processmining.mining.dmcscanning;

import javax.swing.JComponent;

import org.processmining.framework.log.LogReader;
import org.processmining.mining.MiningResult;
import org.processmining.mining.dmcscanning.ui.DmcResultView;

/**
 * Custom class used for storing and visualizing the results of DMC scanning
 * (incl. derived ADMC and MDMC sets).
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class DmcScanningResult implements MiningResult {

	protected LogReader reader = null;
	protected DmcSet dmcSet = null;
	protected AdmcSet admcSet = null;
	protected MdmcSet mdmcSet = null;

	/**
	 * constructor
	 * 
	 * @param aReader
	 *            the LogReader instance used for mining
	 * @param dmc
	 *            the set of initially scanned DMCs
	 * @param admc
	 *            the set of aggregated DMCs, ADMC
	 * @param mdmc
	 *            the set of minimal DMCs, MDMC
	 */
	public DmcScanningResult(LogReader aReader, DmcSet dmc, AdmcSet admc,
			MdmcSet mdmc) {
		reader = aReader;
		dmcSet = dmc;
		admcSet = admc;
		mdmcSet = mdmc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getVisualization()
	 */
	public JComponent getVisualization() {
		return new DmcResultView(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.mining.MiningResult#getLogReader()
	 */
	public LogReader getLogReader() {
		return reader;
	}

	/**
	 * @return Returns the AdmcSet.
	 */
	public AdmcSet getAdmcSet() {
		return admcSet;
	}

	/**
	 * @return Returns the DmcSet.
	 */
	public DmcSet getDmcSet() {
		return dmcSet;
	}

	/**
	 * @return Returns the MdmcSet.
	 */
	public MdmcSet getMdmcSet() {
		return mdmcSet;
	}
}
