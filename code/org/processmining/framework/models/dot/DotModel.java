package org.processmining.framework.models.dot;

import org.processmining.framework.models.ModelGraph;
import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * Title: DotModel
 * </p>
 * 
 * <p>
 * Description: Contains a generic dot model
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
public class DotModel extends ModelGraph {

	String contents; // Contents of the dot file.

	/**
	 * Create a Dot model.
	 * 
	 * @param contents
	 *            String The contents of some dot file.
	 */
	public DotModel(String contents) {
		super("");
		this.contents = contents;
	}

	/**
	 * Write the dot model to dot. Simply copy the contents.
	 * 
	 * @param bw
	 *            Writer Writer to write to.
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		bw.write(contents);
	}
}
