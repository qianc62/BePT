/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.console;

import java.io.*;

import org.processmining.framework.models.petrinet.*;
import org.processmining.framework.models.petrinet.algorithms.*;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class FormatPNML {
	public static void main(String[] args) {
		InputStream in;
		BufferedWriter out;
		PetriNet net;

		if (args.length != 2) {
			System.out
					.println("Usage: java org.processmining.console.FormatPNML <inputfile> <outputfile>");
			System.exit(1);
		}

		try {
			in = new FileInputStream(args[0]);
			out = new BufferedWriter(new FileWriter(args[1], false));

			net = new PnmlReader().read(in);
			PnmlWriter.write(true, false, net, out);

			in.close();
			out.close();
		} catch (Throwable t) {
			System.out.println("Exception: " + t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}
}
