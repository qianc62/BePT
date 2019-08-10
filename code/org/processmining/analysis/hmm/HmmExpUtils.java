package org.processmining.analysis.hmm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Message;
import org.processmining.importing.pnml.PnmlImport;
import org.processmining.mining.petrinetmining.PetriNetResult;

/**
 * Utility methods for the HMM experiments.
 */
public class HmmExpUtils {

	public static String noiseLogFolder = "NoisyLogs";
	public static String noiseHmmFolder = "RefHmms";
	public static String noiseEvalFolder = "Evaluation";

	public static String inputModelFolder = "InputModels";

	public static void cleanup() {
		HmmExpUtils.cleanup(HmmExpUtils.noiseLogFolder);
		HmmExpUtils.cleanup(HmmExpUtils.noiseHmmFolder);
		HmmExpUtils.cleanup(HmmExpUtils.noiseEvalFolder);
		new File(HmmExpUtils.noiseLogFolder).mkdir();
		new File(HmmExpUtils.noiseHmmFolder).mkdir();
		new File(HmmExpUtils.noiseEvalFolder).mkdir();
	}

	/**
	 * Reads a number of Petri net models (expected to be in PNML format) from
	 * the input model folder.
	 * 
	 * @return a map containing the name of the input model file plus the Petri
	 *         net object for each of the models
	 */
	public static Map<String, PetriNet> readInputModels() {
		HashMap<String, PetriNet> map = new HashMap<String, PetriNet>();
		File dir = new File(inputModelFolder);
		File[] files = dir.listFiles();
		try {
			for (File file : files) {
				String fileName = file.getName();
				if (fileName.matches(".*pnml")) {
					// fileName = fileName.replaceAll(".pnml", "");
					FileInputStream stream = new FileInputStream(file);
					PnmlImport importplugin = new PnmlImport();
					PetriNetResult result = (PetriNetResult) importplugin
							.importFile(stream);
					PetriNet pn = result.getPetriNet();
					map.put(fileName, pn);
				}
			}
		} catch (Exception ex) {
			Message.add(
					"Problem encountered during cleanup of previous files.",
					Message.ERROR);
			ex.printStackTrace();
		}
		return map;
	}

	/**
	 * Deletes potential previous files and sub directories in relevant
	 * directory.
	 * 
	 * @param folder
	 *            the directory to be cleaned up
	 */
	public static void cleanup(String folder) {
		File dir = new File(folder);
		File[] files = dir.listFiles();
		try {
			for (File file : files) {
				cleanupSubfolders(file);
			}
		} catch (Exception ex) {
			Message.add(
					"Problem encountered during cleanup of previous files.",
					Message.ERROR);
			ex.printStackTrace();
		}
	}

	private static void cleanupSubfolders(File file) {
		if (file.isDirectory() == true) {
			File[] subsFiles = file.listFiles();
			for (File subFile : subsFiles) {
				cleanupSubfolders(subFile);
			}
		}
		file.delete();
	}

	/**
	 * Creates a new file and returns the writer object for writing to that
	 * file.
	 * 
	 * @param dir
	 *            the directory the file should be created in
	 * @param name
	 *            the name of the file to be created
	 * @param suffix
	 *            the suffix of the file to be created
	 * @return the writer for the file dir/name.suffix
	 */
	public static BufferedWriter createWriter(String dir, String name,
			String suffix) {
		try {
			File file = new File(dir + "/" + name + "." + suffix);
			return new BufferedWriter(new FileWriter(file));
		} catch (IOException ex) {
			ex.printStackTrace();
			Message.add("Could not create file " + dir + "/" + name + "."
					+ suffix, Message.ERROR);
			return null;
		}
	}

	/**
	 * Finishes and closes the file for the given writer.
	 * 
	 * @param writer
	 *            the handle pointing to the file to be closed
	 */
	public static void finishFile(BufferedWriter writer) {
		try {
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			Message.add("Could not finish file", Message.ERROR);
		}
	}

}
