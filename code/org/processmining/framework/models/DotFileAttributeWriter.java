package org.processmining.framework.models;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * Title: DotFileAttributeWriter
 * </p>
 * 
 * <p>
 * Description: Write object attributes to a Dot file
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
 * 
 *          Code rating: Red
 * 
 *          Review rating: Red
 */
public interface DotFileAttributeWriter {
	public void writePreambleAttributes(Writer bw) throws IOException;

	public void writeVertexAttributes(Writer bw, ModelGraphVertex vertex)
			throws IOException;

	public void writeEdgeAttributes(Writer bw, ModelGraphEdge edge)
			throws IOException;

	public void writeClusterAttributes(Writer bw, ModelGraphCluster cluster)
			throws IOException;
}
