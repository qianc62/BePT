package org.processmining.framework.models.bpel4ws.type.activity;

import java.io.*;
import org.processmining.framework.models.bpel4ws.type.*;

/**
 * @author Kristian Bisgaard Lassen
 * 
 */
public abstract class Composed extends Activity {

	/**
	 * @param name
	 */
	public Composed(String name) {
		super(name);
	}

	public abstract String writeToDot(BPEL4WS model);
}
