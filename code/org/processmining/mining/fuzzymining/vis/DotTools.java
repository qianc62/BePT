/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.mining.fuzzymining.vis;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.processmining.framework.models.DotFileWriter;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.framework.util.RuntimeUtils;

import att.grappa.Graph;
import att.grappa.Parser;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DotTools {

	protected Process process;

	public DotTools() {
		process = null;
	}

	public synchronized InputStream runDot(List<String> params, String input)
			throws IOException {
		// put input into temporary file
		File dotFile = File.createTempFile("DotTmp", "tmp");
		BufferedOutputStream tmpOs = new BufferedOutputStream(
				new FileOutputStream(dotFile));
		tmpOs.write(input.getBytes());
		tmpOs.flush();
		tmpOs.close();
		return runDot(params, dotFile);
	}

	public synchronized InputStream runDot(List<String> params, File dotFile)
			throws IOException {
		// extend params
		params = new ArrayList<String>(params);
		params.add("-q5");
		// find dot executable
		String customDot = Dot.getDotPath();
		String dotFilePath = dotFile.getAbsolutePath();
		if (RuntimeUtils.isRunningWindows()) {
			// windows dot needs quotes around input the file parameter
			dotFilePath = "\"" + dotFilePath + "\"";
		}
		Message.add("Invoking Graphviz dot: " + customDot, Message.DEBUG);
		params.add(dotFilePath);
		params.add(0, customDot);
		ProcessBuilder dot = new ProcessBuilder(params);
		process = dot.start();
		return new BufferedInputStream(process.getInputStream());
	}

	public synchronized String getDotResultString(List<String> params,
			String input) throws IOException {
		InputStream inputStream = runDot(params, input);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		String tmp = null;
		StringBuilder result = new StringBuilder();
		while ((tmp = br.readLine()) != null) {
			result.append(tmp + "\n");
		}
		return result.toString();
	}

	public synchronized Graph getGraphFromDot(DotFileWriter writer,
			List<String> params) throws Exception {
		File dotFile = File.createTempFile("ProMDotToolsTmp", ".dot");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dotFile, false));
		writer.writeToDot(bw);
		bw.close();
		Parser parser = new Parser(runDot(params, dotFile), System.err);
		parser.parse();
		dotFile.deleteOnExit();
		return parser.getGraph();
	}

	public synchronized Image renderImage(String input) throws IOException {
		List<String> params = new ArrayList<String>();
		params.add("-Tpng");
		//add by little or Lijie Wen@2015-05-22
		params.add("-Gdpi=1440");
		InputStream inputStream = runDot(params, input);
		BufferedImage image = ImageIO.read(inputStream);
		return image;
	}

	public boolean killRunningDot() {
		if (process != null) {
			process.destroy();
			return true;
		} else {
			return false;
		}
	}

}
