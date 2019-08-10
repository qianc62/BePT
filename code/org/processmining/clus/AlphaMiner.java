package org.processmining.clus;

import org.processmining.framework.log.LogFile;
import org.processmining.framework.ui.OpenLogSettings;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.ui.slicker.logdialog.SlickerOpenLogSettings;
import org.processmining.mining.petrinetmining.AlphaProcessMiner;
import org.processmining.mining.petrinetmining.PetriNetResult;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.exporting.petrinet.PnmlExport;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.processmining.framework.ui.Message;
import org.processmining.framework.plugin.ProvidedObject;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AlphaMiner {

	private String inputFile = null, outputFile = null;
	private LogReader logReader = null;
	private PetriNet petriNet = null;

	/**
	 * Create an Alpha Miner, read the arguments.
	 * 
	 * @param args
	 *            String[] The arguments, format: -i inputfile [-o output file]
	 */
	public AlphaMiner(String[] args) {
		int argMode = 0;
		for (int i = 0; i < args.length; i++) {
			if (argMode == 0) {
				if (args[i].contentEquals("-i")) {
					argMode = 1;
				} else if (args[i].contentEquals("-o")) {
					argMode = 2;
				}
			} else if (argMode == 1) {
				inputFile = new String(args[i]);
				argMode = 0;
			} else if (argMode == 2) {
				outputFile = new String(args[i]);
				argMode = 0;
			}
		}
	}

	public AlphaMiner(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		startMining();
	}

	public void startMining() {
		new MiningThread().start();
	}

	public class MiningThread extends Thread {
		public void run() {
			ExecutorService pool = Executors.newFixedThreadPool(1);
			Future<String> result = pool.submit(new OpenLog());
			if (result.equals("true")) {
				pool.submit(new Mine());
			}
			if (result.equals("true")) {
				Export();
			}
		}
	}

	public class OpenLog implements Callable<String> {

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			openLog();
			return "true";
		}

	}

	public class Mine implements Callable<String> {

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			mine();
			return "true";
		}

	}

	/**
	 * Open the log, given the input file specified.
	 */
	public void openLog() {
		if (inputFile != null) {
			// Open the log.
			LogFile logFile = LogFile.getInstance(inputFile);
			final OpenLogSettings settings = new SlickerOpenLogSettings(logFile);
			System.out.println("hello");
			try {
				logReader = LogReaderFactory.createInstance(settings
						.getLogFilter(), logFile);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else {
			System.err.println("No input file found.");
		}
	}

	/**
	 * Mine the log for a Petri net.
	 */
	public void mine() {
		if (logReader != null) {
			// Mine the log for a Petri net.
			AlphaProcessMiner miningPlugin = new AlphaProcessMiner();
			PetriNetResult result = (PetriNetResult) miningPlugin
					.mine(logReader);
			petriNet = result.getPetriNet();
		} else {
			System.err.println("No log reader could be constructed.");
		}
	}

	/**
	 * Export the mined Petri net to a PNML file.
	 */
	public void Export() {
		if (petriNet != null) {
			// Export the Petri net as PNML.
			PnmlExport exportPlugin = new PnmlExport();
			Object[] objects = new Object[] { petriNet };
			ProvidedObject object = new ProvidedObject("temp", objects);
			FileOutputStream outputStream = null;
			try {
				if (outputFile != null) {
					outputStream = new FileOutputStream(outputFile);
				}
				// If no output file specified, write to System.out
				// However, some other thing smay get written to System.out as
				// well :-(.
				exportPlugin.export(object,
						(outputStream != null ? outputStream : System.out));
				System.exit(0);
			} catch (Exception e) {
				System.err.println("Unable to write to file: " + e.toString());
			}
		} else {
			System.err.println("No Petri net could be constructed.");
		}
	}

	/**
	 * Main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		AlphaMiner alphaMiner = new AlphaMiner(
				"insuranceClaimHandlingExample.mxml",
				"insuranceClaimHandlingExample.pnml");
		/*
		 * try{ alphaMiner.OpenLog(); alphaMiner.Mine(); }catch(Exception ex){
		 * ex.printStackTrace(); System.exit(0); } System.out.println("hello");
		 * alphaMiner.Export();
		 */
	}
}
