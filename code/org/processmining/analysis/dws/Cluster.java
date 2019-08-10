package org.processmining.analysis.dws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.heuristics.HNSet;
import org.processmining.framework.models.heuristics.HNSubSet;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

/**
 * Defines the data structures and the algorithms for the clustering.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

/*
 * Gestione di un cluster Al momento della creazione non viene associata la
 * HeuristicNet
 */
public class Cluster {

	private HeuristicsNet en; // contiene il modello associato al cluster
	private String name; // nome del cluster
	private LogReader log; // log su file; il suo filtro ci dice indice delle
	// tracce associate al cluster
	private ArrayList children = new ArrayList(); // eventuali sotto-cluster
	private ArrayList features = new ArrayList(); // insieme delle features
	private LogSketch ls; // mappa che associa le process instances agli ID

	private Cluster parent; // il genitore nel clustering: attualmente non �
	// usato

	/*
	 * I parametri sono passati dal costruttore solo nella radice.
	 */
	private int k = 3; // parametro da leggere: clusters su cui splittare
	private double sigma = 0.01; // parametro da leggere: soglia delle features
	private double gamma = 1; // parametro da leggere: seconda soglia sulle
	// features
	private int l = 3; // parametro da leggere: lunghezza delle features
	private int maxFeatures = 5; // parametro da leggere: numero massimo di

	// features

	/**
	 * Constructor for the root of the hierarchy.
	 * 
	 * @param log
	 *            Log file.
	 * @param name
	 *            Name of the cluster.
	 * @param k
	 *            Number of clusters to be mined.
	 * @param sigma
	 *            Threshold for the mining of the features.
	 * @param gamma
	 *            Threshold for the mining of the features.
	 * @param l
	 *            Maximum legth of each feature.
	 * @param maxFeatures
	 *            Maximum number of features.
	 */
	public Cluster(LogReader log, String name, int k, double sigma,
			double gamma, int l, int maxFeatures) {
		this.log = log;
		this.name = name;
		this.en = null;
		this.ls = new LogSketch(log);
		// LogReader original=new LogReader(log.getLogFilter(),log.getFile());
		// this.ls=new LogSketch(original);
		// fin qui modifica di gian
		this.k = k;
		this.sigma = sigma;
		this.gamma = gamma;
		this.l = l;
		this.maxFeatures = maxFeatures;
	}

	/**
	 * Constructor for the inner nodes of the hierarchy.
	 * 
	 * @param log
	 *            Log file.
	 * @param parent
	 *            Parent of the cluster in the hierarchy.
	 * @param name
	 *            Name of the cluster.
	 * @param ls
	 */
	private Cluster(LogReader log, Cluster parent, String name, LogSketch ls) {
		this.log = log;
		this.parent = parent;
		this.name = name;
		this.en = null;
		this.ls = ls;
		this.k = parent.k;
		this.sigma = parent.sigma;
		this.gamma = parent.gamma;
		this.l = parent.l;
		this.maxFeatures = parent.maxFeatures;
	}

	/*
	 * Verifica la presenza di un arco nel modello
	 */
	private boolean existsEdgeInNet(int from, int to) {
		HNSet out = en.getOutputSet(from);
		for (int i = 0; i < out.size(); i++) {
			HNSubSet outSubSet = out.get(i);
			if (outSubSet.contains(to)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Per ora i path hanno lunghezza 1
	 */
	private boolean existsPathInNet(int from, int to) {
		return existsEdgeInNet(from, to);
	}

	/*
	 * Restituisce le coppie di nodi raggiungibili nel modello
	 */
	private ArrayList getTraceL2() {
		ArrayList ris = new ArrayList();
		for (int i = 0; i < en.size(); i++) {
			for (int j = 0; j < en.size(); j++) {
				if (existsPathInNet(i, j)) {
					Trace t = new Trace(new int[] { i, j }, en);
					ris.add(t);
				}
			}
		}
		return ris;
	}

	/*
	 * Filtra un lista di tracce restituendo solo quelle la cui frequenza �
	 * maggiore di sigma
	 */
	private ArrayList filterTraces(ArrayList L, double sigma) {
		double logSize = log.getLogSummary().getNumberOfProcessInstances();
		ArrayList ris = new ArrayList();
		if (L.size() == 0) {
			return ris;
		}

		Trace[] vt = new Trace[L.size()];
		int[] vo = new int[L.size()];
		Iterator it = L.listIterator();
		int i = 0;
		while (it.hasNext()) {
			Trace tL = (Trace) it.next();
			vt[i] = tL;
			i++;
		}

		Iterator logInstanceIterator = log.instanceIterator();
		while (logInstanceIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logInstanceIterator.next();
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			Trace tLog = new Trace(ates, en);
			for (i = 0; i < vt.length; i++) {
				if (tLog.contains(vt[i])) {
					int repetitions = MethodsForWorkflowLogDataStructures
							.getNumberSimilarProcessInstances(pi);
					if (repetitions == 0) {
						vo[i] += 1;
					} else {
						vo[i] += repetitions;
					}
				}
			}
		}

		for (i = 0; i < vt.length; i++) {
			/*
			 * TODO Modifica 15-3-2007 Anto =
			 */
			if (vo[i] / logSize > sigma) {
				ris.add(vt[i]);
			}
		}

		return ris;
	}

	/*
	 * Algoritmo di mining delle features
	 */

	private void mineFeatures(double sigma, double gamma, int l, int maxFeatures) {
		ArrayList L2 = filterTraces(getTraceL2(), sigma); // step 1
		int len = 3; // step 2
		ArrayList Llen = L2; // implicito nello step 2
		while (len <= l && Llen.size() > 0) { // step 3
			ArrayList Candlen = new ArrayList(); // step 4
			// estendo le sequenze di uno: creo la nuova lista di candidati
			Iterator it = Llen.iterator();
			while (it.hasNext()) {
				Trace tlen = (Trace) it.next();
				Iterator it2 = L2.iterator();
				while (it2.hasNext()) {
					Trace t2 = (Trace) it2.next();
					if (tlen.concatenable(t2)) {
						Trace tnew = new Trace(tlen, t2);
						Candlen.add(tnew);
					}
				}
			} // end while della generazione dei candidati
			Llen = filterTraces(Candlen, sigma); // step 8
			ArrayList Llen_gamma = filterTraces(Candlen, gamma); // step 9

			// costruzione insieme differenza
			ArrayList CminusL = new ArrayList();
			Iterator it3 = Candlen.iterator();
			while (it3.hasNext()) {
				Trace tCand = (Trace) it3.next();
				Iterator it4 = Llen_gamma.iterator();
				boolean trovato = false;
				while (it4.hasNext() && !trovato) {
					Trace tL = (Trace) it4.next();
					if (tCand.equals(tL)) {
						trovato = true;
					}
				}
				if (!trovato) {
					CminusL.add(tCand);
				}
			} // fine costruzione insieme differenza
			// update delle features
			Iterator it5 = CminusL.iterator();
			while (it5.hasNext()) {
				Trace tCminusL = (Trace) it5.next();
				Feature f = new Feature(tCminusL);
				features.add(f);
			} // fine update delle features
			len++;
		} // fine del loop principale
		// costruzione vettore delle features
		Feature[] lf = new Feature[features.size()];
		Iterator it = features.iterator();
		int j = 0;
		while (it.hasNext()) {
			lf[j] = (Feature) it.next();
			j++;
		}

		// questo ordinamento serve a far si che anche un rimescolamento dei log
		// produca lo stesso risultato
		Arrays.sort(lf);

		// calcolo delle occorrenze delle features
		int[] occurrences = new int[features.size()];
		int nLogs = log.getLogSummary().getNumberOfProcessInstances();
		Iterator logInstancesIterator = log.instanceIterator();
		while (logInstancesIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logInstancesIterator.next();
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			int repetitions = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			Trace tLog = new Trace(ates, en);
			for (int i = 0; i < lf.length; i++) {
				Trace t = lf[i].getBody().concat(lf[i].getHead());
				// System.out.println("Trace:"+t);
				if (tLog.contains(t)) {
					if (repetitions == 0) {
						occurrences[i] += 1;
					} else {
						occurrences[i] += repetitions;
					}
				}
			}
		} // fine calcolo occorrenze

		// selezione features massimali
		features = new ArrayList();
		int selectedFeatures = 0;
		boolean trovato = true;
		while (selectedFeatures < maxFeatures && trovato) {
			int minIndex = 0;
			int minOccurrence = nLogs + 1;
			for (int i = 0; i < lf.length; i++) {
				if (occurrences[i] < minOccurrence) {
					minOccurrence = occurrences[i];
					minIndex = i;
				}
			}
			if (minOccurrence <= nLogs) {
				boolean featureMassimale = true;
				Trace daInserire = lf[minIndex].getBody();
				Iterator itF = features.iterator();
				while (itF.hasNext() && featureMassimale) {
					Feature f = (Feature) itF.next();
					Trace t = f.getBody();
					// elimino la ridondanza a destra tipo abcd -> e
					// abcde->f
					// TODO ANTO
					if (f.ridond(lf[minIndex]))
						featureMassimale = false;

					// fine
					/*
					 * if(this.gamma>= this.sigma){ if (t.contains(daInserire)&&
					 * f.getHead().equals(lf[minIndex].getHead()))
					 * featureMassimale=false; else if (daInserire.contains(t)&&
					 * f.getHead().equals(lf[minIndex].getHead())){
					 * itF.remove(); selectedFeatures--; } } else{TODO
					 */

					if (daInserire.contains(t)
							&& f.getHead().equals(lf[minIndex].getHead()))
						featureMassimale = false;
					else if (t.contains(daInserire)
							&& f.getHead().equals(lf[minIndex].getHead())) {
						itF.remove();
						selectedFeatures--;
					}
				}
				if (featureMassimale) {
					features.add(lf[minIndex]);
					selectedFeatures++;
				}
				occurrences[minIndex] = nLogs + 1;
			} else
				trovato = false;

		} // fine selezione features massimali

		maxFeatures = selectedFeatures;
	}

	/**
	 * Implement the clustering procedure
	 * 
	 * @throws Exception
	 * 
	 */
	public void mineClusters() {

		/*
		 * anto aggiunto un controllo se features.size()==0 non deve splittare
		 */
		if (features.size() == 0) {
			return;
		}

		// crea lo spazio vettoriale
		VectorialSpace vs = new VectorialSpace();
		Iterator logInstanceIterator = log.instanceIterator();
		while (logInstanceIterator.hasNext()) {
			ProcessInstance pi = (ProcessInstance) logInstanceIterator.next();
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			Trace t = new Trace(ates, en);
			int ID = ((Integer) ls.m.get(pi.getName())).intValue();
			VectorialPoint vp = new VectorialPoint(t, features, ID);
			// System.out.println(vp);
			(vs.points).add(vp);
		}

		// esegue il clustering
		ArrayList[] logChildren = vs.kmeans(k);

		// finalizza i cluster trovati e li aggiunge come figli
		for (int i = 0; i < logChildren.length; i++) {
			if (logChildren[i].size() > 0
					&& logChildren[i].size() != vs.points.size()) {
				int[] tracesToKeepForChildren = new int[logChildren[i].size()];
				ArrayList l = new ArrayList();
				for (int j = 0; j < tracesToKeepForChildren.length; j++) {
					tracesToKeepForChildren[j] = ((Integer) logChildren[i]
							.get(j)).intValue();
					l.add(new Integer(tracesToKeepForChildren[j]));
				}
				try {
					LogReader r = LogReaderFactory.createInstance(log,
							tracesToKeepForChildren);
					Cluster c = new Cluster(r, this, name + "." + i,
							new LogSketch(r));
					children.add(c);
				} catch (Exception e) {
					// Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Mines the features for the model and the log of the cluster. The cluster
	 * has to be equipped with a HeursticsNet. If less than maxFeatures are
	 * discovered, the value maxFeatures is updated. Otherwise, the best
	 * maxFeatures features are mined.
	 */
	public void mineFeatures() {
		mineFeatures(sigma, gamma, l, maxFeatures);
	}

	public String toString() {
		return name;
	}

	/**
	 * @return The HeuristicsNet model associated with the cluster.
	 */
	public HeuristicsNet getHeuristicsNet() {
		return en;
	}

	/**
	 * @param en
	 *            The HeuristicsNet to associate with the cluster.
	 */
	public void setHeuristicsNet(HeuristicsNet en) {
		this.en = en;
	}

	/**
	 * @return The name of the cluster.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The log associated with the cluster.
	 */
	public LogReader getLog() {
		return log;
	}

	/**
	 * @return The features associated with the cluster.
	 */
	public ArrayList getFeatures() {
		return features;
	}

	/**
	 * @return The children of the cluster.
	 */
	public ArrayList getChildren() {
		return children;
	}
}
