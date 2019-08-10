/**
 * 
 */
package org.processmining.framework.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author christian
 * 
 */
public interface ServiceHandler {

	public void handleRequest(BufferedReader in, PrintWriter out)
			throws IOException;

}
