package org.processmining.analysis.dws;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Implements a kmeans algorithm by projecting each log traces in a suitable
 * vectorial space of the features
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

public class VectorialSpace {

	ArrayList points = new ArrayList();

	// restituisce un array di cluster (identificativi di tracce)
	public ArrayList[] kmeans(int k) {
		if (k > points.size()) {
			k = points.size();
		}
		ArrayList[] logChildren = new ArrayList[k];
		// tracce associate ad ogni figlio
		// ogni ArrayList contiene una lista di interi
		for (int i = 0; i < k; i++) {
			logChildren[i] = new ArrayList();
		}

		VectorialPoint[] centri = new VectorialPoint[k];
		for (int i = 0; i < k; i++) {
			centri[i] = getPoint(i);
		}

		for (int h = 1; h < k; h++) { // assegno un valore al centro h-esimo
			double dCentro = getPoint(0).distance(centri[0]);
			for (int l = 1; l < h; l++) {
				double d = getPoint(0).distance(centri[l]);
				if (d < dCentro) {
					dCentro = d;
				}
			} // dCentro contiene la distanza minima dell'istanza 0 dai centri
			// processati
			int traceMax = 0;
			for (int i = 1; i < points.size(); i++) {
				double dCentroNew = getPoint(i).distance(centri[0]);
				for (int l = 1; l < h; l++) {
					double d = getPoint(i).distance(centri[l]);
					if (d < dCentroNew) {
						dCentroNew = d;
					}
				} // dCentroNew contiene la nuova distanza
				if (dCentro < dCentroNew) {
					dCentro = dCentroNew;
					traceMax = i;
				}
			} // traceMax � l'istanza piu' distante dai centri gia' processati
			centri[h] = getPoint(traceMax);
		}

		TreeSet[] rep = new TreeSet[k];
		for (int i = 0; i < k; i++) {
			rep[i] = new TreeSet();
		}
		// passaggio inverso dai centri ai cluster
		for (int i = 0; i < points.size(); i++) {
			double dMin = getPoint(i).distance(centri[0]);
			int centroMin = 0;
			for (int j = 1; j < k; j++) {
				double d = getPoint(i).distance(centri[j]);
				if (d < dMin) {
					dMin = d;
					centroMin = j;
				}
			}
			// devo aggiungere il punto i al cluster centroMin
			VectorialPoint vp = getPoint(i);
			rep[centroMin].add(vp);
			// System.out.println(vp);
			logChildren[centroMin].add(new Integer(vp.ID));
		}

		// for (int i=0;i<k;i++)
		// System.out.println(i+":"+logChildren[i].size()+":"+rep[i]);
		return logChildren;
	}

	public VectorialPoint getPoint(int index) {
		return (VectorialPoint) points.get(index);
	}

	public String toString() {
		return points.toString();
	}
}
