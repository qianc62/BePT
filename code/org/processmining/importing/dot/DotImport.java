package org.processmining.importing.dot;

import org.processmining.importing.ImportPlugin;
import java.io.InputStream;
import java.io.IOException;
import org.processmining.framework.ui.filters.GenericFileFilter;
import org.processmining.framework.models.dot.DotModel;
import org.processmining.mining.dot.DotResult;

/**
 * <p>
 * Title: Dot import plug-in
 * </p>
 * 
 * <p>
 * Description: Imports a dot file into a Dot model.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class DotImport implements ImportPlugin {

	/**
	 * Create the Dot import plug-in.
	 */
	public DotImport() {
	}

	/**
	 * Menu item.
	 * 
	 * @return String
	 */
	public String getName() {
		return "DOT file";
	}

	/**
	 * Extension to filter on.
	 * 
	 * @return FileFilter
	 */
	public javax.swing.filechooser.FileFilter getFileFilter() {
		return new GenericFileFilter("dot");
	}

	/**
	 * Import a Dot model from a given stream.
	 * 
	 * @param input
	 *            InputStream The given stream.
	 * @return DotResult The resulting DotResult, containing the Dot model.
	 * @throws IOException
	 */
	public DotResult importFile(InputStream input) throws IOException {
		DotModel model = new DotModel(read(input));
		return new DotResult(model);
	}

	/**
	 * Read a string from a stream. Nothing fancy.
	 * 
	 * @param input
	 *            InputStream
	 * @return String
	 * @throws IOException
	 */
	public String read(InputStream input) throws IOException {
		StringBuffer buffer = new StringBuffer();
		int c;
		while ((c = input.read()) != -1) {
			buffer.append((char) c);
		}
		return buffer.toString();
	}

	/**
	 * Help page.
	 * 
	 * @return String
	 */
	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:import:owfn";
	}

}
