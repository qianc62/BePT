/*
 * Author: R. P. Jagadeesh Chandra Bose
 * Date  : 12 July 2008
 * Purpose: This Class reads the NGrams (3 Grams) and Generates the substitution matrix as well as indel matrix
 * The format of the nGram File should be <nGram> @ <nGramCount>
 */

package org.processmining.analysis.traceclustering.charstreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.processmining.framework.ui.MainUI;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class NGram {
	private HashMap<String, Integer> allNGrams;
	int nGramCount;
	int nGramSize;
	int encodingLength;
	String prefixSuffix;

	public NGram(int n, int encodingLength, Vector<String> charStreams) {
		// Logger.printCall("In Constructor NGram");
		int noCharStreams = charStreams.size();
		int noCommands, currentCount;
		String currentStream, currentGram;

		this.encodingLength = encodingLength;
		prefixSuffix = "";
		for (int i = 0; i < encodingLength; i++)
			prefixSuffix += ".";

		if (!new File("C:\\Temp\\ProM\\NGrams\\" + n).exists()) {
			boolean success = new File("C:\\Temp\\ProM\\NGrams\\" + n).mkdirs();
			if (!success) {
				MainUI
						.getInstance()
						.showGlassDialog("File Creation Error",
								"Couldn't create a temporary directory in C:\\Temp; Can't proceed");
			}
		}

		this.nGramSize = n;
		allNGrams = new HashMap<String, Integer>();

		HashMap<String, Double> charStreamNGrams;
		double currentValue;

		for (int i = 0; i < noCharStreams; i++) {
			currentStream = prefixSuffix + charStreams.get(i) + prefixSuffix;
			// System.out.println(currentStream);
			noCommands = currentStream.length() / encodingLength;
			charStreamNGrams = new HashMap<String, Double>();
			for (int j = 0; j < noCommands - n + 1; j++) {
				currentGram = currentStream.substring(j * encodingLength,
						(j + n) * encodingLength);

				if (charStreamNGrams.containsKey(currentGram)) {
					currentValue = charStreamNGrams.get(currentGram);
					charStreamNGrams.put(currentGram, currentValue + 1);
				} else {
					charStreamNGrams.put(currentGram, 1.0);
				}

				if (allNGrams.containsKey(currentGram)) {
					currentCount = allNGrams.get(currentGram).intValue();
					allNGrams.put(currentGram, currentCount + 1);
				} else {
					allNGrams.put(currentGram, 1);
				}
			}
		}

		this.nGramCount = allNGrams.size();
		writeNGramsToFile("C:\\Temp\\ProM\\NGrams\\" + n + "\\allNGrams.out");
		System.out.println("Exiting NGram");
	}

	public int getNGramCount() {
		return this.nGramCount;
	}

	public void writeNGramsToFile(String fileName) {

		Set<String> nGramSet = allNGrams.keySet();
		Iterator<String> it = nGramSet.iterator();
		String currentGram;

		FileOutputStream out;
		PrintStream p;

		try {
			out = new FileOutputStream(fileName);
			p = new PrintStream(out);
			TablePrinter tablePrinter = new TablePrinter(p, (nGramSize + 1)
					* encodingLength, 3, 10);
			while (it.hasNext()) {
				currentGram = it.next();
				tablePrinter.print(currentGram);
				tablePrinter.print(" @ ");
				tablePrinter.print(allNGrams.get(currentGram) + "");
				tablePrinter.newLine();
			}
			p.close();
			out.close();
		} catch (IOException e) {
			System.out.println("IOException when writing NGrams to File");
			System.exit(0);
		}
	}
}
