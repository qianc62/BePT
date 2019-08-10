package org.processmining.analysis.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Auxiliary class to extract causal dependencies of ".dot" files
 * 
 * @author Ana Karla A. de Medeiros
 * 
 */

public class ExtractCausalRelations {

	HashMap<String, String> nodeToLabel = new HashMap<String, String>();
	HashMap<String, Set<String>> causeToEffects = new HashMap<String, Set<String>>();
	HashMap<String, Set<String>> effectToCauses = new HashMap<String, Set<String>>();
	HashMap<String, String> connectionToSignificance = new HashMap<String, String>();

	public ExtractCausalRelations(String inputDirectory, String inputFile,
			String outputFile) throws IOException {
		File input = new File(inputDirectory + File.separator + inputFile);
		if (input.isFile()) {
			extractNodesToLabelsMappings(input);
			extractCauseToEffectsMappings(input);
			reverseMappings();
			File output = new File(inputDirectory + File.separator + outputFile);
			createReport(causeToEffects, output + "_causeToEffects.txt");
			createReport(effectToCauses, output + "_effectsToCauses.txt");
		} else {
			System.out.println("Invalid input file! -> " + input);
		}

	}

	private void createReport(HashMap<String, Set<String>> map, String output)
			throws IOException {
		StringBuffer sb;
		FileWriter fw = new FileWriter(output);

		for (String key : map.keySet()) {
			sb = new StringBuffer();
			sb.append(key + ";" + map.get(key).size() + ";");
			for (String value : map.get(key)) {
				sb.append(value + "@@@@@");
			}
			sb.delete(sb.lastIndexOf("@@@@@"), sb.length());
			fw.write(sb.toString() + "\n");
			sb = null;
		}
		fw.close();

	}

	private void reverseMappings() {
		Set<String> causeToEffectsKeys = causeToEffects.keySet();
		for (String cause : causeToEffectsKeys) {
			Set<String> effects = causeToEffects.get(cause);
			for (String effect : effects) {
				Set<String> causes = null;
				if (effectToCauses.containsKey(effect)) {
					causes = effectToCauses.get(effect);
				} else {
					causes = new TreeSet<String>();
				}
				causes.add(cause + " "
						+ connectionToSignificance.get(cause + "->" + effect));
				effectToCauses.put(effect, causes);
			}
		}
	}

	private void extractNodesToLabelsMappings(File f) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		String line = null;
		while ((line = raf.readLine()) != null) {
			line.trim();
			if (line.indexOf("node_") >= 0 && line.indexOf("label=") >= 0) {
				if (line.indexOf("->") < 0) {
					// it is a mapping from nodes to labels
					String node = line.substring(0, line.indexOf(" "));
					String label = line.substring(line.indexOf("\"") + 1, line
							.lastIndexOf("\\"));
					nodeToLabel.put(node.trim(), label.trim());
				}
			}
		}
		raf.close();
	}

	private void extractCauseToEffectsMappings(File f) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		String line = null;
		while ((line = raf.readLine()) != null) {
			line.trim();
			if (line.indexOf("node_") >= 0 && line.indexOf("label=") >= 0) {
				if (line.indexOf("->") >= 0) {
					// it is a mapping among nodes, i.e., a causal relation
					String sourceNode = line.substring(0,
							line.indexOf("->") - 1).trim();
					String destinationNode = line.substring(
							line.indexOf("->") + 2, line.indexOf("[")).trim();
					String significance = line.substring(
							line.indexOf("\"") + 1,
							line.indexOf("\"", line.indexOf("\"") + 1) - 1)
							.trim();
					if (nodeToLabel.get(destinationNode)
							.indexOf("SYSTEM ERROR") >= 0) {
						Set destinationNodes = null;
						if (!causeToEffects.containsKey(nodeToLabel
								.get(sourceNode))) {
							destinationNodes = new TreeSet<String>();
						} else {
							destinationNodes = causeToEffects.get(nodeToLabel
									.get(sourceNode));
						}
						destinationNodes.add(nodeToLabel.get(destinationNode));
						causeToEffects.put(nodeToLabel.get(sourceNode),
								destinationNodes);
						connectionToSignificance.put(nodeToLabel
								.get(sourceNode)
								+ "->" + nodeToLabel.get(destinationNode),
								significance);
					}
				}
			}
		}
		raf.close();
	}

	public static void main(String[] args) {
		String inputDirectory = args[0];
		String inputFile = args[1];
		String outputFile = args[2];
		try {
			ExtractCausalRelations ecr = new ExtractCausalRelations(
					inputDirectory, inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
