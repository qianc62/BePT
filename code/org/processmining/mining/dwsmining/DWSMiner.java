/*
 * Created on 19-set-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.processmining.mining.dwsmining;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import org.processmining.analysis.dws.Cluster;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.PluginDocumentationLoader;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;
import org.processmining.mining.heuristicsmining.HeuristicsMiner;
import org.processmining.mining.heuristicsmining.HeuristicsNetResult;

/**
 * This is the main class implementing the DWS (Disjunctive Workflow Schema)
 * mining plugin.
 * 
 * @author Gianluigi Greco, Antonella Guzzo, Luigi Pontieri
 * @version 1.0
 * @see org.processmining.framework.models.heuristics.HeuristicsNet
 */

public class DWSMiner implements MiningPlugin {

	JPanel ep;
	MiningInputPanel dwsp;
	int maxSplits = 5; // parametro da passare
	int k = 3; // parametro da leggere: clusters su cui splittare
	double sigma = 0.01; // parametro da leggere: soglia delle features
	double gamma = 1; // parametro da leggere: seconda soglia sulle features
	int l = 3; // parametro da leggere: lunghezza delle features
	int maxFeatures = 5; // parametro da leggere: numero massimo di features

	private MiningPlugin mp;
	private HeuristicsMiner em;

	public DWSMiner() {
		mp = new HeuristicsMiner();
		em = (HeuristicsMiner) mp;
	}

	public String getName() {
		return "DWS mining plugin";
	}

	public MiningResult mine(LogReader log) {
		// lettura dei parametri
		maxSplits = dwsp.getSplit();
		k = dwsp.getK();
		sigma = dwsp.getSigma();
		gamma = dwsp.getGamma();
		l = dwsp.getFeatureLength();
		maxFeatures = dwsp.getMaxFeat();

		// scelta algoritmo di mining
		// MiningPlugin mp=new HeuristicsMiner();
		// HeuristicsMiner em=(HeuristicsMiner)mp;
		// ep=em.getOptionsPanel(log.getLogSummary());

		// Faccio il mining della radice e dei figli del primo livello
		Cluster root = new Cluster(log, "R", k, sigma, gamma, l, maxFeatures);
		HeuristicsNetResult er = (HeuristicsNetResult) mp.mine(root.getLog());
		HeuristicsNet en = er.getHeuriticsNet();
		root.setHeuristicsNet(en);
		root.mineFeatures();
		root.mineClusters();
		for (int i = 0; i < root.getChildren().size(); i++) {
			Cluster c = (Cluster) root.getChildren().get(i);
			er = (HeuristicsNetResult) mp.mine(c.getLog());
			en = er.getHeuriticsNet();
			c.setHeuristicsNet(en);
			c.mineFeatures();
		}
		ArrayList frontiera = root.getChildren();
		int splits = 1;
		boolean noRefinement = false;
		while (splits < maxSplits && !noRefinement) {
			double bestFit = 0;
			Cluster bestCluster = null;
			ArrayList newFrontiera = new ArrayList();
			Iterator it = frontiera.iterator();
			while (it.hasNext()) {
				Cluster c = (Cluster) it.next();
				double cfit = c.getHeuristicsNet().getFitness();
				if (cfit > bestFit) {
					if (bestCluster != null)
						newFrontiera.add(bestCluster);
					bestFit = cfit;
					bestCluster = c;
				} else
					newFrontiera.add(c);
			}// bestCluster � definito solo se bestFit>0
			if (bestFit > 0) {
				bestCluster.mineClusters();
				for (int i = 0; i < bestCluster.getChildren().size(); i++) {
					Cluster c = (Cluster) bestCluster.getChildren().get(i);
					er = (HeuristicsNetResult) mp.mine(c.getLog());
					en = er.getHeuriticsNet();
					c.setHeuristicsNet(en);
					c.mineFeatures();
					newFrontiera.add(c);
				}
				splits++;
			} else
				noRefinement = true;
			frontiera = newFrontiera;
		}

		// visualizzazione risultato
		DWSResult d = new DWSResult();
		d.setRoot(root);
		return d;
	}

	public String getHtmlDescription() {
		return PluginDocumentationLoader.load(this);
	}

	public JPanel getOptionsPanel(LogSummary arg) {
		// HeuristicsMinerParameters emp=new HeuristicsMinerParameters();
		// ep=new HeuristicsMinerGUI(arg,emp);
		ep = em.getOptionsPanel(arg);
		dwsp = new MiningInputPanel();
		return new DWSInputMinerGUI(ep, dwsp);
	}

}
