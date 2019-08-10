package org.processmining.analysis.dws;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.analysis.AnalysisInputItem;
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.heuristics.HeuristicsNet;
import org.processmining.framework.ui.MainUI;

/**
 * Provides the graphical interface for running DWS analysis plugin.
 * 
 * @author Gianluigi Greco, Antonella Guzzo
 * @version 1.0
 */

/*
 * La classe definisce un pannello dwsp di tipi MiningInputPanel per leggere i
 * parametri di input Alla pressione del bottone jButto viene lanciato il
 * clustering
 */

public class DWSInputAnalysisGUI extends JPanel {

	private AnalysisInputPanel dwsp = null;
	private JPanel jPanel1 = null;
	private JButton jButton = null;

	// private Object[]o;
	private AnalysisPlugin plugIn;
	private HeuristicsNet net;
	private LogReader log;
	private final AnalysisInputItem[] inputs;

	int k = 2; // parametro da leggere: clusters su cui splittare
	double sigma = 0.05; // parametro da leggere: soglia delle features
	double gamma = 0.01; // parametro da leggere: seconda soglia sulle features
	int l = 5; // parametro da leggere: lunghezza delle features
	int maxFeatures = 2; // parametro da leggere: numero massimo di features

	/**
	 * This is the default constructor
	 */
	public DWSInputAnalysisGUI(AnalysisPlugin plugIn, LogReader log,
			HeuristicsNet net, AnalysisInputItem[] inputs) {
		super();
		// ProvidedObject[] po=arg0[0].getProvidedObjects();
		// this.o=po[0].getObjects();
		// o[0] � l'heuristics nets
		// o[1] � il log reader
		this.inputs = inputs;

		this.log = log;
		this.net = net;

		this.plugIn = plugIn;
		dwsp = new AnalysisInputPanel();
		initialize();
	}

	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300, 200);
		this.add(getJPanel(), BorderLayout.CENTER);
		this.add(getJPanel1(), BorderLayout.SOUTH);
	}

	private JPanel getJPanel() {
		if (dwsp == null) {
			dwsp = new AnalysisInputPanel();
			dwsp.setLayout(new BorderLayout());
		}
		return dwsp;
	}

	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getJButton(), null);
		}
		return jPanel1;
	}

	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Start Clustering");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// riempie i campi associati alla form
					k = dwsp.getK();
					sigma = dwsp.getSigma();
					gamma = dwsp.getGamma();
					l = dwsp.getFeatureLength();
					maxFeatures = dwsp.getMaxFeat();

					/*
					 * Crea la radice della gerarchia e fa il mining delle
					 * features e fa lo split dei log. Tuttavia i cluster non
					 * sono equipaggiati di features e di un loro modello.
					 */
					Cluster root = new Cluster(log, "R", k, sigma, gamma, l,
							maxFeatures);
					root.setHeuristicsNet(net);
					root.mineFeatures();
					root.mineClusters();

					// crea una nuova finestra nella quale visualizzare il
					// risultato del plugin
					MainUI.getInstance().createAnalysisResultFrame(plugIn,
							inputs, new DWSOutputGUI(root));
				}
			});
		}
		return jButton;
	}
}
