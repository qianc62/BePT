package org.processmining.framework.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * This class defines a logging framework.
 * 
 * @author jcbose (R. P. Jagadeesh Chandra 'JC' Bose)
 */

public class Logger {

	static PrintStream outFile = null;
	static String logDir = "C:\\Temp\\";
	static String fileName = "Log.txt";

	static int callDepth = 0;
	static String prefix = "";

	// Start a Log file that logs all input and output
	// The file is saved to the c:\temp directory

	public static void startLog() {
		try {
			outFile = new PrintStream(new FileOutputStream(logDir + fileName));
			outFile.println("----- Log file " + fileName + " started "
					+ new Date());
			System.out.println("----- Writing output to the file: " + fileName
					+ ". ");
		} catch (IOException e) {
			System.out.println("IOException in startLog opening " + fileName
					+ "\n" + e);
		}
	}

	public static void stopLog() {
		outFile.println("----- Log file " + fileName + " closed " + new Date());
		outFile.close();
		outFile = null;
	}

	public static void println(String s) {
		if (outFile != null)
			outFile.println(prefix + s);
		System.out.println(prefix + s);
	}

	public static void print(String s) {
		if (outFile != null)
			outFile.print(s);
		System.out.print(s);
		System.out.flush();
	}

	public static void printCall(String s) {
		println(s);
		callDepth++;
		prefix = getPrefix(callDepth);
	}

	static String getPrefix(int i) {
		if (i > 0)
			return ("| " + getPrefix(i - 1));
		else
			return ("");
	}

	public static void printReturn(String s) {
		callDepth--;
		prefix = getPrefix(callDepth);
		println(s);
	}

	public static PrintStream getPrintStream() {
		return outFile;
	}
}
