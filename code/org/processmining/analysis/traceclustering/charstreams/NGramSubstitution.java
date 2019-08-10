/*
 * Author: R. P. Jagadeesh Chandra Bose
 * Date  : 12 July 2008
 * Purpose: This Class reads the NGrams (3 Grams) and Generates the substitution matrix as well as indel matrix
 * The format of the nGram File should be <nGram> @ <nGramCount>
 */

package org.processmining.analysis.traceclustering.charstreams;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * @author R. P. Jagadeesh Chandra Bose
 * 
 */
public class NGramSubstitution {
	private int encodingLength;
	private Vector<String> symbols; // Store the symbols used in encoding for
	// the commands
	private Vector<String> commands; // The commands in the user profiles
	private String prefixSuffix;
	private int n;
	private Vector<String> grams;
	private Vector<Integer> gramCount;
	private HashSet<String>[] contexts;
	private HashMap<String, Integer> scoringMatrix;
	private HashMap<String, Integer> insLeftGivenRight;
	private HashMap<String, Integer> insRightGivenLeft;
	private int[][] countRightGivenLeft;
	private int[][] countLeftGivenRight;
	private int[][] subsCount;
	private float[] probSymbol;
	private float[][] normSubCount;

	private int noCommands;

	/*
	 * This constructor takes the command level abstraction as an argument and
	 * accordingly reads the appropriate file
	 */
	public NGramSubstitution(int nGramSize, int encodingLength,
			String nGramDir, String nGramFile,
			HashMap<String, String> itemCharMap) {
		System.out.println("In Constructor NGramSubstitution");

		this.encodingLength = encodingLength;
		prefixSuffix = "";
		for (int i = 0; i < encodingLength; i++) {
			prefixSuffix += ".";
		}
		commands = new Vector<String>();
		symbols = new Vector<String>();

		Iterator<String> it = itemCharMap.keySet().iterator();
		String currCommand;
		while (it.hasNext()) {
			currCommand = it.next();
			commands.add(currCommand);
			symbols.add(itemCharMap.get(currCommand));
		}
		// readCommandCharMapSymbols(map);
		// Logger.println("Encoding Length: "+encodingLength);
		//		
		noCommands = this.commands.size();
		System.out.println("No. of Commands: " + noCommands);

		initialize();
		// n = Settings.getGramSize();
		n = nGramSize;

		readNGrams(nGramDir, nGramFile);
		getSymbolContexts();
		computeSubstitutionScores();
		computeIndelScores();

		// for(int i = 0; i < noCommands; i++)
		// Logger.println(commands.get(i)+"\t"+symbols.get(i));

		// Logger.printReturn("Exiting Constructor NGramSubstitution");
	}

	/*
	 * This function reads a file that contains information about the comamnds
	 * and their corresponding char encoding The format of the file should be
	 * <command> @ <encoding> The parameter specifies the level of abstraction 0
	 * implies that the commands are encoded as is 1 implies the first level of
	 * command group abstraction 2 implies the second level of command group
	 * abstraction
	 */

	private void initialize() {
		int noSymbols = symbols.size();

		grams = new Vector<String>();
		gramCount = new Vector<Integer>();

		countLeftGivenRight = new int[noSymbols][noSymbols];
		countRightGivenLeft = new int[noSymbols][noSymbols];
		subsCount = new int[noSymbols][noSymbols];
		normSubCount = new float[noSymbols][noSymbols];
		probSymbol = new float[noSymbols];
		contexts = new HashSet[noSymbols];
		scoringMatrix = new HashMap<String, Integer>();
		insLeftGivenRight = new HashMap<String, Integer>();
		insRightGivenLeft = new HashMap<String, Integer>();

		for (int i = 0; i < noSymbols; i++) {
			subsCount[i][i] = 0;
			countLeftGivenRight[i][i] = 0;
			countRightGivenLeft[i][i] = 0;
			normSubCount[i][i] = 0;
			probSymbol[i] = 0;
			for (int j = i + 1; j < noSymbols; j++) {
				subsCount[i][j] = subsCount[j][i] = 0;
				normSubCount[i][j] = normSubCount[j][i] = 0;
				countLeftGivenRight[i][j] = countLeftGivenRight[j][i] = 0;
				countRightGivenLeft[i][j] = countRightGivenLeft[j][i] = 0;
			}
		}
	}

	/*
	 * This function reads all nGrams (provided as a file) and builds the
	 * insLeftGivenRight and insRightGivenLeft matrices The format of the nGram
	 * file should be <nGram> @ <nGramCount>
	 */
	private void readNGrams(String nGramDir, String nGramFile) {
		BufferedReader reader;

		// String nGramDir = Settings.getUserProfileCharStreamInputDirectory()
		// + "\\" + map;
		// String nGramFile = Settings.getAllNGramsFileName();

		try {
			reader = new BufferedReader(new FileReader(nGramDir + "\\"
					+ nGramFile));
			String currentLine, currentGram;
			String[] currentLineSplit;
			String[] gramSymbols = new String[n];
			int currentGramCount;
			int leftGivenRightIndexI, leftGivenRightIndexJ;
			int rightGivenLeftIndexI, rightGivenLeftIndexJ;

			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(" @ ");
				currentGram = currentLineSplit[0].trim();
				currentGramCount = new Integer(currentLineSplit[1].trim())
						.intValue();

				grams.add(currentGram);
				gramCount.add(currentGramCount);

				for (int i = 0; i < n; i++) {
					gramSymbols[i] = currentGram.substring(i * encodingLength,
							(i + 1) * encodingLength);
				}

				if (!gramSymbols[2].equals(prefixSuffix)) {
					leftGivenRightIndexI = symbols.indexOf(gramSymbols[1]); // hard
					// coded
					// for
					// 3
					leftGivenRightIndexJ = symbols.indexOf(gramSymbols[2]);
					countLeftGivenRight[leftGivenRightIndexI][leftGivenRightIndexJ] += currentGramCount;
				}
				if (!gramSymbols[0].equals(prefixSuffix)) {
					rightGivenLeftIndexI = symbols.indexOf(gramSymbols[0]);
					rightGivenLeftIndexJ = symbols.indexOf(gramSymbols[1]);
					countRightGivenLeft[rightGivenLeftIndexI][rightGivenLeftIndexJ] += currentGramCount;
				}

			}

			reader.close();
		} catch (IOException e) {
			System.out.println("IOException while reading nGrams file "
					+ nGramDir + "\\" + nGramFile);
			System.exit(0);
		}
	}

	private void getSymbolContexts() {
		// Logger.printCall("Entering getContexts");

		int noNGrams = grams.size();
		int noSymbols = symbols.size();
		String currentGram, currentContext;
		String[] gramSymbols = new String[n];

		for (int i = 0; i < noSymbols; i++) {
			contexts[i] = new HashSet<String>();
		}

		for (int i = 0; i < noNGrams; i++) {
			currentGram = grams.get(i);
			for (int j = 0; j < n; j++) {
				gramSymbols[j] = currentGram.substring(j * encodingLength,
						(j + 1) * encodingLength);
			}

			currentContext = gramSymbols[0].concat(gramSymbols[2]); // Hardcoded
			// for 3
			// grams;
			contexts[symbols.indexOf(gramSymbols[1])].add(currentContext);
		}

		// Logger.printReturn("Exiting getContexts");
	}

	private Vector<String> getAllNGramsGivenContext(String context) {
		int noNGrams = grams.size();
		String currentGram, currentContext;
		Vector<String> allGramsGivenContext = new Vector<String>();

		for (int i = 0; i < noNGrams; i++) {
			currentGram = grams.get(i);
			currentContext = currentGram.substring(0 * encodingLength,
					1 * encodingLength)
					+ currentGram.substring(2 * encodingLength,
							3 * encodingLength);
			if (currentContext.equals(context)) {
				allGramsGivenContext.add(currentGram);
			}
		}

		return allGramsGivenContext;
	}

	private void computeIndelScores() {
		// Logger.printCall("Entering computeIndelScores");

		int noSymbols = symbols.size();
		long[] normSymbolScore = new long[noSymbols];
		for (int i = 0; i < noSymbols; i++) {
			normSymbolScore[i] = 0;
			for (int j = 0; j < noSymbols; j++) {
				normSymbolScore[i] += countRightGivenLeft[j][i];
			}
		}

		int indelScore;
		float numer;
		for (int i = 0; i < noSymbols; i++) {
			for (int j = 0; j < noSymbols; j++) {
				if (countRightGivenLeft[j][i] > 0 && probSymbol[i] > 0
						&& probSymbol[j] > 0) {
					numer = countRightGivenLeft[j][i]
							/ ((float) normSymbolScore[i]);
					indelScore = Math.round(new Double(Math.log(numer
							/ (probSymbol[i] * probSymbol[j]))
							/ Math.log(2.0)).floatValue() * 2) / 10;
					// Logger.println(symbols.get(j)+"@"+symbols.get(i)+" "+indelScore);
					insRightGivenLeft.put(
							symbols.get(j) + "@" + symbols.get(i), indelScore);
				}
			}
		}
		printScoringMatrixToFile(insRightGivenLeft,
				"indelRightGivenLeft0.2.txt");
		printScoringMatrixSymbolToFile(insRightGivenLeft,
				"indelSymbolRightGivenLeft0.2.txt");

		for (int i = 0; i < noSymbols; i++) {
			normSymbolScore[i] = 0;
			for (int j = 0; j < noSymbols; j++) {
				normSymbolScore[i] += countLeftGivenRight[i][j];
			}
		}

		for (int i = 0; i < noSymbols; i++) {
			for (int j = 0; j < noSymbols; j++) {
				if (countLeftGivenRight[i][j] > 0 && probSymbol[i] > 0
						&& probSymbol[j] > 0) {
					numer = countLeftGivenRight[i][j]
							/ ((float) normSymbolScore[i]);
					indelScore = Math.round(new Double(Math.log(numer
							/ (probSymbol[i] * probSymbol[j]))
							/ Math.log(2.0)).floatValue() * 2) / 10;
					insLeftGivenRight.put(
							symbols.get(i) + "@" + symbols.get(j), indelScore);
				}
			}
		}
		printScoringMatrixToFile(insLeftGivenRight,
				"indelLeftGivenRight0.2.txt");
		printScoringMatrixSymbolToFile(insLeftGivenRight,
				"indelSymbolLeftGivenRight0.2.txt");

		// Logger.printReturn("Exiting computeIndelScores");
	}

	private void computeSubstitutionScores() {
		// Logger.printCall("Entering computeSubstitutionScores");

		int noSymbols = symbols.size();
		int noGramsGivenContext;
		String currentContext, subSymbol, currentGram, currentSymbolContextGram;
		Vector<String> allNGramsGivenContext;
		Iterator<String> it;
		int iCount, jCount;
		for (int i = 0; i < noSymbols; i++) {
			it = contexts[i].iterator();
			while (it.hasNext()) {
				currentContext = it.next();
				currentSymbolContextGram = currentContext.substring(
						0 * encodingLength, 1 * encodingLength)
						+ symbols.get(i)
						+ currentContext.substring(1 * encodingLength,
								2 * encodingLength);

				iCount = gramCount.get(grams.indexOf(currentSymbolContextGram));

				allNGramsGivenContext = getAllNGramsGivenContext(currentContext);
				noGramsGivenContext = allNGramsGivenContext.size();
				for (int j = 0; j < noGramsGivenContext; j++) {
					currentGram = allNGramsGivenContext.get(j);
					subSymbol = currentGram.substring(1 * encodingLength,
							2 * encodingLength);

					jCount = gramCount.get(grams.indexOf(currentGram));
					if (symbols.indexOf(subSymbol) == i) {
						subsCount[i][i] += (iCount * (iCount - 1)) / 2;
					} else {
						subsCount[i][symbols.indexOf(subSymbol)] += iCount
								* jCount;
					}
				}
			}
		}

		long normCoeff = 0;
		for (int i = 0; i < noSymbols; i++)
			for (int j = 0; j <= i; j++)
				normCoeff += subsCount[i][j];

		for (int i = 0; i < noSymbols; i++) {
			for (int j = 0; j < noSymbols; j++) {
				normSubCount[i][j] = (subsCount[i][j]) / ((float) normCoeff);
			}
		}

		// Compute the probability of each symbol
		float sum = 0;
		for (int i = 0; i < noSymbols; i++) {
			probSymbol[i] = normSubCount[i][i];
			for (int j = 0; j < noSymbols; j++) {
				if (i != j)
					probSymbol[i] += normSubCount[i][j] / 2.0;
			}
			sum += probSymbol[i];
		}

		// Verify whether the probabilities sum to 1.0
		// Logger.println("Sum of Probabilities: " + sum);

		// for (int i = 0; i < noSymbols; i++) {
		// Logger.println(symbols.get(i) + " " + probSymbol[i]);
		// }

		int temp;
		for (int i = 0; i < noSymbols; i++) {
			if (normSubCount[i][i] > 0 && probSymbol[i] > 0) {
				scoringMatrix.put(symbols.get(i) + "@" + symbols.get(i), Math
						.round(new Double((Math.log(normSubCount[i][i]
								/ (probSymbol[i] * probSymbol[i])))
								/ Math.log(2.0)).floatValue() * 2));
			}
			for (int j = 0; j < noSymbols; j++) {
				if (i != j) {
					if (normSubCount[i][j] > 0 && probSymbol[i] > 0
							&& probSymbol[j] > 0) {

						temp = Math.round(new Double((Math
								.log(normSubCount[i][j]
										/ (2 * probSymbol[i] * probSymbol[j])))
								/ Math.log(2.0)).floatValue() * 2);
						// if(temp == 0)
						// System.out.println(symbols.get(i) + "@" +
						// symbols.get(j)+" "+2*probSymbol[i]*probSymbol[j]+" "+normSubCount[i][j]);
						scoringMatrix.put(
								symbols.get(i) + "@" + symbols.get(j), temp);
					}
				}
			}
		}

		printScoringMatrixToFile(scoringMatrix, "subsScore.txt");
		printScoringMatrixSymbolToFile(scoringMatrix, "subsSymbolScore.txt");

		// Logger.printReturn("Exiting computeSubstitutionScores");
	}

	private void printScoringMatrixSymbolToFile(
			HashMap<String, Integer> scoreMap, String fileName) {
		// Logger.printCall("Entering printScoringMatrixSymbolToFile");

		try {
			// FileOutputStream out = new
			// FileOutputStream(charStreamInputDir+"\\"+map+"\\"+fileName);
			FileOutputStream out = new FileOutputStream(
					"C:\\Temp\\ProM\\NGrams\\3\\" + fileName);
			PrintStream p = new PrintStream(out);

			TablePrinter tablePrinter = new TablePrinter(p,
					2 * encodingLength + 1, 2, 5);

			Set<String> keySet = scoreMap.keySet();
			Iterator<String> it = keySet.iterator();
			String currentKey;
			while (it.hasNext()) {
				currentKey = it.next();
				tablePrinter.print(currentKey);
				tablePrinter.print(":");
				tablePrinter.print(scoreMap.get(currentKey) + "");
				tablePrinter.newLine();
			}
			p.close();
			out.close();
		} catch (IOException e) {
			System.out
					.println("IOException when writing symbol score matrix to file: "
							+ fileName);
		}
		// Logger.printReturn("Entering printScoringMatrixSymbolToFile");
	}

	private void printScoringMatrixToFile(HashMap<String, Integer> scoreMap,
			String fileName) {
		// Logger.printCall("Entering printScoringMatrixToFile");

		try {
			// FileOutputStream out = new
			// FileOutputStream(charStreamInputDir+"\\"+map+"\\"+fileName);
			FileOutputStream out = new FileOutputStream(
					"C:\\Temp\\ProM\\NGrams\\3\\" + fileName);
			PrintStream p = new PrintStream(out);

			TablePrinter tablePrinter = new TablePrinter(p, 100 + 2, 100 + 2, 6);

			String currentPair;
			String[] commandPair;
			Set<String> keySet = scoreMap.keySet();
			Iterator<String> it = keySet.iterator();
			while (it.hasNext()) {
				currentPair = it.next();
				commandPair = currentPair.split("@");
				tablePrinter.print(commands
						.get(symbols.indexOf(commandPair[0])));
				tablePrinter.print(commands
						.get(symbols.indexOf(commandPair[1])));
				tablePrinter.print(scoreMap.get(currentPair) + "");
				tablePrinter.newLine();
				// Logger.println(commands.get(symbols.indexOf(commandPair[0]))
				// + "," + commands.get(symbols.indexOf(commandPair[1]))
				// + "," + scoringMatrix.get(currentPair));
			}
			p.close();
			out.close();
		} catch (IOException e) {
			System.out
					.println("IOException while writing scoring matrix to File");
			System.exit(0);
		}
		// Logger.printReturn("Exiting printScoringMatrix");
	}

	public HashMap<String, Integer> getScoringMatrix() {
		return scoringMatrix;
	}

	public HashMap<String, Integer> getInsLeftGivenRight() {
		return insLeftGivenRight;
	}

	public HashMap<String, Integer> getInsRightGivenLeft() {
		return insRightGivenLeft;
	}
}
