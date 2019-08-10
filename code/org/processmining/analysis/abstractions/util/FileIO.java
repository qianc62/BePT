package org.processmining.analysis.abstractions.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

public class FileIO {

	public FileIO() {

	}

	public HashSet<String> readFileAsSet(String inputDir, String fileName) {
		HashSet<String> stringSet = new HashSet<String>();
		BufferedReader reader;

		String currentLine;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				stringSet.add(currentLine.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return stringSet;
	}

	public ArrayList<String> readFile(String inputDir, String fileName) {
		ArrayList<String> stringList = new ArrayList<String>();
		BufferedReader reader;

		String currentLine;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				stringList.add(currentLine.trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return stringList;
	}

	public ArrayList<TreeSet<String>> readSetFromFile(String inputDir,
			String fileName) {
		ArrayList<TreeSet<String>> listSets = new ArrayList<TreeSet<String>>();
		BufferedReader reader;

		String currentLine;
		String[] currentLineSplit;
		TreeSet<String> stringSet;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLine = currentLine.replaceAll("\\[", "");
				currentLine = currentLine.replaceAll("\\]", "");
				currentLineSplit = currentLine.split(",");
				stringSet = new TreeSet<String>();
				for (String str : currentLineSplit)
					stringSet.add(str.trim());

				listSets.add(stringSet);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return listSets;
	}

	public HashMap<String, String> readMapFromFile(String inputDir,
			String fileName, String delim) {
		HashMap<String, String> map = new HashMap<String, String>();

		BufferedReader reader;

		String currentLine;
		String[] currentLineSplit;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(delim);
				if (currentLineSplit.length != 2) {
					System.out.println(currentLine);
					System.out
							.println("Something wrong in the format of the map file");
					System.exit(0);
				}

				map.put(currentLineSplit[0].trim(), currentLineSplit[1].trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return map;
	}

	public HashMap<String, Integer> readMapStringIntegerFromFile(
			String inputDir, String fileName, String delim) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader;

		String currentLine;
		String[] currentLineSplit;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(delim);
				if (currentLineSplit.length != 2) {
					System.out
							.println("Something wrong in the format of the map file");
					System.out.println(currentLine);
					System.exit(0);
				}

				map.put(currentLineSplit[0].trim(), new Integer(
						currentLineSplit[1].trim()));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return map;
	}

	public HashMap<TreeSet<String>, Integer> readMapSetIntegerFromFile(
			String inputDir, String fileName, String delim) {
		HashMap<TreeSet<String>, Integer> map = new HashMap<TreeSet<String>, Integer>();

		BufferedReader reader;

		String currentLine, currentSetLine;
		String[] currentLineSplit, currentSetLineSplit;
		TreeSet<String> setStrings;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(delim);
				if (currentLineSplit.length != 2) {
					System.out
							.println("Something wrong in the format of the map file");
					System.exit(0);
				}
				setStrings = new TreeSet<String>();
				currentSetLine = currentLineSplit[0].trim();
				currentSetLine = currentSetLine.replaceAll("\\[", "");
				currentSetLine = currentSetLine.replaceAll("\\]", "");
				currentSetLineSplit = currentSetLine.split(",");
				setStrings = new TreeSet<String>();
				for (String str : currentSetLineSplit)
					setStrings.add(str.trim());

				map.put(setStrings, new Integer(currentLineSplit[1].trim()));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return map;
	}

	public HashMap<TreeSet<String>, String> readMapSetStringFromFile(
			String inputDir, String fileName, String delim) {
		HashMap<TreeSet<String>, String> map = new HashMap<TreeSet<String>, String>();

		BufferedReader reader;

		String currentLine, currentSetLine;
		String[] currentLineSplit, currentSetLineSplit;
		TreeSet<String> setStrings;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(delim);
				if (currentLineSplit.length != 2) {
					System.out
							.println("Something wrong in the format of the map file");
					System.out.println(currentLine);
					System.exit(0);
				}
				setStrings = new TreeSet<String>();
				currentSetLine = currentLineSplit[0].trim();
				currentSetLine = currentSetLine.replaceAll("\\[", "");
				currentSetLine = currentSetLine.replaceAll("\\]", "");
				currentSetLineSplit = currentSetLine.split(",");
				setStrings = new TreeSet<String>();
				for (String str : currentSetLineSplit)
					setStrings.add(str.trim());

				map.put(setStrings, currentLineSplit[1].trim());
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return map;
	}

	public HashMap<TreeSet<String>, TreeSet<String>> readEquivalenceClassMapFromFile(
			String inputDir, String fileName, String delim) {
		HashMap<TreeSet<String>, TreeSet<String>> setEquivalenceClassMap = new HashMap<TreeSet<String>, TreeSet<String>>();

		BufferedReader reader;

		String currentLine;
		String[] currentLineSplit, currentSetLineSplit;
		String currentSetLine;
		TreeSet<String> stringKeySet, stringEquivalenceClassSet;
		try {
			reader = new BufferedReader(new FileReader(inputDir + "\\"
					+ fileName));
			while ((currentLine = reader.readLine()) != null) {
				currentLineSplit = currentLine.split(delim);
				if (currentLineSplit.length != 2) {
					System.out
							.println("Something wrong in the format of the map file");
					System.out.println(currentLine);
					System.exit(0);
				}

				currentSetLine = currentLineSplit[0];
				currentSetLine = currentSetLine.replaceAll("\\[", "");
				currentSetLine = currentSetLine.replaceAll("\\]", "");

				currentSetLineSplit = currentSetLine.split(",");
				stringKeySet = new TreeSet<String>();
				for (String str : currentSetLineSplit)
					stringKeySet.add(str.trim());

				currentSetLine = currentLineSplit[1];
				currentSetLine = currentSetLine.replaceAll("\\[", "");
				currentSetLine = currentSetLine.replaceAll("\\]", "");

				currentSetLineSplit = currentSetLine.split(",");
				stringEquivalenceClassSet = new TreeSet<String>();
				for (String str : currentSetLineSplit)
					stringEquivalenceClassSet.add(str.trim());

				setEquivalenceClassMap.put(stringKeySet,
						stringEquivalenceClassSet);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("File Not Found: " + inputDir + "\\" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception while Reading: " + inputDir + "\\"
					+ fileName);
			e.printStackTrace();
		}

		return setEquivalenceClassMap;
	}

	public <T, E> void writeToFile(String dir, String fileName, Map<T, E> map,
			String delim) {
		FileOutputStream fos;
		PrintStream ps;

		if (isDirExists(dir)) {
			try {
				fos = new FileOutputStream(dir + "\\" + fileName);
				ps = new PrintStream(fos);

				for (T t : map.keySet()) {
					ps.println(t.toString() + " " + delim + " "
							+ map.get(t).toString());
				}

				ps.close();
				fos.close();
			} catch (FileNotFoundException e) {
				System.err
						.println("File Not Found Exception while creating file: "
								+ dir + "\\" + fileName);
				System.exit(0);
			} catch (IOException e) {
				System.err.println("IO Exception while writing file: " + dir
						+ "\\" + fileName);
				System.exit(0);
			}
		} else {
			System.err.println("Can't create Directory: " + dir);
		}
	}

	public <T> void writeToFile(String dir, String fileName,
			Collection<T> collection) {
		FileOutputStream fos;
		PrintStream ps;

		if (isDirExists(dir)) {
			try {
				fos = new FileOutputStream(dir + "\\" + fileName);
				ps = new PrintStream(fos);

				for (T t : collection) {
					ps.println(t.toString());
				}

				ps.close();
				fos.close();
			} catch (FileNotFoundException e) {
				System.err
						.println("File Not Found Exception while creating file: "
								+ dir + "\\" + fileName);
				System.exit(0);
			} catch (IOException e) {
				System.err.println("IO Exception while writing file: " + dir
						+ "\\" + fileName);
				System.exit(0);
			}
		} else {
			System.err.println("Can't create Directory: " + dir);
		}
	}

	public <T> void writeToFile(String dir, String fileName, T[] arrayT) {
		FileOutputStream fos;
		PrintStream ps;

		if (isDirExists(dir)) {
			try {
				fos = new FileOutputStream(dir + "\\" + fileName);
				ps = new PrintStream(fos);

				for (T t : arrayT) {
					ps.println(t.toString());
				}

				ps.close();
				fos.close();
			} catch (FileNotFoundException e) {
				System.err
						.println("File Not Found Exception while creating file: "
								+ dir + "\\" + fileName);
				System.exit(0);
			} catch (IOException e) {
				System.err.println("IO Exception while writing file: " + dir
						+ "\\" + fileName);
				System.exit(0);
			}
		} else {
			System.err.println("Can't create Directory: " + dir);
		}
	}

	public <T> void writeToFile(String dir, String fileName, T t) {
		FileOutputStream fos;
		PrintStream ps;

		if (isDirExists(dir)) {
			try {
				fos = new FileOutputStream(dir + "\\" + fileName);
				ps = new PrintStream(fos);

				ps.println(t.toString());

				ps.close();
				fos.close();
			} catch (FileNotFoundException e) {
				System.err
						.println("File Not Found Exception while creating file: "
								+ dir + "\\" + fileName);
				System.exit(0);
			} catch (IOException e) {
				System.err.println("IO Exception while writing file: " + dir
						+ "\\" + fileName);
				System.exit(0);
			}
		} else {
			System.err.println("Can't create Directory: " + dir);
		}
	}

	public void createDir(String dir) {
		if (!(new File(dir)).exists()) {
			boolean success = new File(dir).mkdirs();
			if (!success) {
				System.out.println("Cannot create directory: " + dir);
				System.exit(0);
			}
		}
	}

	private boolean isDirExists(String dir) {
		if (!(new File(dir)).exists()) {
			return new File(dir).mkdirs();
		} else {
			return true;
		}
	}
}
