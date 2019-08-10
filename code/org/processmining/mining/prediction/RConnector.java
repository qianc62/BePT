package org.processmining.mining.prediction;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.WaitDialog; //import org.rosuda.JRI.*;
import org.rosuda.JRclient.*;
import org.processmining.framework.ui.Progress;
import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;

/**
 * this class connects to one of 2 R connections
 * 
 * @author Ronald Crooy
 * 
 */
public class RConnector {
	private Rengine Rlocal;
	protected Rconnection Rserve;
	protected Boolean succes;
	protected Boolean useLocalR;
	protected String hostname;
	protected Integer port;
	public String name;

	private RSession sess;// used to detach a remote r connection

	/**
	 * shutdown hook for clean cancellations
	 * 
	 * @author Ronald crooy
	 * 
	 */
	class ShutdownThread extends Thread {
		private RConnector conn;

		ShutdownThread(RConnector conn) {
			this.conn = conn;
		}

		public void run() {
			try {
				if (this.conn.useLocalR) {
					// this.conn.Rlocal.end();
				} else {
					this.conn.Rserve.close();
				}
			} catch (Exception e) {
				// log the exception
			}
		}
	}

	public RConnector() {
		useLocalR = false;
		sess = null;
		port = null;
		hostname = null;
		succes = false;
		Rlocal = null;
		Rserve = null;
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
	}

	/**
	 * evaluates the R code in the correct R-engine
	 */
	public Rresult eval(String rcode) {
		// System.out.println(this.name+" : "+rcode);
		return eval(rcode, true, true);
	}

	/**
	 * evaluates the R code in the correct R-engine if waitforresult is set to
	 * false, the evalution will not return anyting.
	 */
	public Rresult eval(String rcode, Boolean waitforresult) {
		return eval(rcode, waitforresult, waitforresult);
	}

	/**
	 * evaluates the R code in the correct R-engine if blocking is set to false,
	 * eval will return null while busy if convert is set to true the resulting
	 * Rresult will contain native representation of the contents, otherwise an
	 * empty Rresult will be returned. NOTE that using multiple separate
	 * non-blocking evals is very likely a BAD thing!!
	 */
	private Rresult eval(String rcode, Boolean blocking, Boolean convert) {
		// debugging
		// System.out.println(this.name+" : "+rcode);
		Rresult result = null;
		if (useLocalR) {
			if (blocking) {
				result = new Rresult(Rlocal.eval(rcode, convert));
			} else {
				Rlocal.idleEval(rcode, convert);
			}
		} else {
			if (blocking) {
				try {
					org.rosuda.JRclient.REXP temp = Rserve.eval(rcode);
					result = new Rresult(temp.getType(), temp.getContent());
				} catch (Exception e) {
					// connection error
					System.out.println(this.name + " had problems with: "
							+ rcode + "; it resulted in :" + e.getMessage());

				}
			} else {
				try {
					Rserve.voidEval(rcode);
				} catch (Exception e) {
					// connection error
					System.out.println(this.name + " had problems with: "
							+ rcode + "; it resulted in :" + e.getMessage());
				}
			}
		}
		return result;
	}

	/**
	 * assigns the array of strings in 'value' to 'name'
	 * 
	 * @param name
	 * @param value
	 */
	public void assign(String name, String[] value) {
		if (useLocalR) {
			Rlocal.assign(name, value);
		} else {
			try {
				Rserve.assign(name, new org.rosuda.JRclient.REXP(value));
			} catch (RSrvException e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	public void assign(String name, String value) {
		String[] temp = new String[1];
		temp[0] = value;
		if (useLocalR) {
			Rlocal.assign(name, value);
		} else {
			try {
				Rserve.assign(name, value);
			} catch (Exception e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	public void assign(String name, int[] value) {
		if (useLocalR) {
			Rlocal.assign(name, value);
		} else {
			try {
				Rserve.assign(name, value);
			} catch (Exception e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	public void assign(String name, int value) {
		int[] temp = new int[1];
		temp[0] = value;
		if (useLocalR) {
			Rlocal.assign(name, temp);
		} else {
			try {
				Rserve.assign(name, temp);
			} catch (Exception e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	public void assign(String name, double[] value) {
		if (useLocalR) {
			Rlocal.assign(name, value);
		} else {
			try {
				Rserve.assign(name, value);
			} catch (Exception e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	public void assign(String name, double value) {
		double[] temp = new double[1];
		temp[0] = value;
		if (useLocalR) {
			Rlocal.assign(name, temp);
		} else {
			try {
				Rserve.assign(name, temp);
			} catch (Exception e) {
				// what to do now
				System.out.println("assignment error for variable'" + name
						+ "' " + e);
			}
		}
	}

	/**
	 * check if R can be loaded locally
	 * 
	 * @return
	 */
	public Boolean testLocalR() {
		// Map<String, String> env = System.getenv();
		// for (String envName : env.keySet()) {
		// System.out.format("%s=%s%n", envName, env.get(envName));
		// }
		Boolean result = true;
		try {
			System.loadLibrary("jri");
			if (!Rengine.versionCheck()) {
				System.err
						.println("** Version mismatch - Java files don't match library version.");
				return false;
			}
			String[] args = { "" };
			Rlocal = Rengine.getMainEngine();

			if (Rlocal == null) {
				Rlocal = new Rengine(args, false, null);
				if (!Rlocal.waitForR()) {
					System.err.println("cannot load R");
					// Message.add("Cannot load R");
					return false;
				}
			}
			// Rengine.DEBUG=1;
			if (Rlocal.eval("require(\"np\", quietly=TRUE)").asBool().isFALSE()) {
				JOptionPane
						.showMessageDialog(
								null,
								"the extra package NP needs to be installed, this will be done automatically"
										+ ", however you must select a mirror to download the packages from");
				Rlocal
						.eval("if (require(\"np\", quietly=TRUE)){ library(np)}else{install.packages(\"np\")}");
			}
		} catch (UnsatisfiedLinkError ule) {
			result = false;
			System.err.println("unable to load jri");
		}
		succes = result;
		if (succes) {
			this.useLocalR = true;
			Message.add("connected to local R");
		}
		return result;
	}

	/**
	 * check if a connection to the specified RServer can be made
	 * 
	 * @return
	 */
	public Boolean testRconnection(String host, Integer port) {
		Boolean result = true;
		String username = null;
		String password = null;
		try {
			Rserve = new Rconnection(host, port);
			if (Rserve.needLogin()) {
				username = JOptionPane.showInputDialog(null,
						"R server needs login, please enter username");
				password = JOptionPane.showInputDialog(null,
						"Now please enter password");
				Rserve.login(username, password);
			}
			Rserve.eval("library(np)");
		} catch (RSrvException e) {
			result = false;
		}
		if (result) {
			Message.add("connected to Rserve on " + host);
			this.hostname = host;
			this.port = port;
			this.useLocalR = false;
		}
		succes = result;
		return result;
	}

}

/**
 * this class is a wrapper for the 2 equal but differently located REXP classes
 * 
 * @author Ronald Crooy
 * 
 */
class Rresult extends org.rosuda.JRclient.REXP {

	/**
	 * @author Ronald Crooy always use constructors for this value
	 * @param value
	 */
	public Rresult(org.rosuda.JRI.REXP value) {
		super(value.getType(), value.getContent());
	}

	public Rresult() {
		super();
	}

	public Rresult(int type, Object content) {
		super(type, content);
	}
	// public Rresult(int type, Object content){
	// super(type,content);
	// }
}
/*
 * class JRIMainLoopCallBacks implements RMainLoopCallbacks {
 * 
 * 
 * public void rWriteConsole(Rengine re, String text, int oType) {
 * System.out.print(text); }
 * 
 * public void rBusy(Rengine re, int which) {
 * System.out.println("rBusy("+which+")"); }
 * 
 * public String rReadConsole(Rengine re, String prompt, int addToHistory) {
 * System.out.print(prompt); try { BufferedReader br=new BufferedReader(new
 * InputStreamReader(System.in)); String s=br.readLine(); return
 * (s==null||s.length()==0)?s:s+"\n"; } catch (Exception e) {
 * System.out.println("jriReadConsole exception: "+e.getMessage()); } return
 * null; }
 * 
 * public void rShowMessage(Rengine re, String message) {
 * System.out.println("rShowMessage \""+message+"\""); }
 * 
 * public String rChooseFile(Rengine re, int newFile) { FileDialog fd = new
 * FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file",
 * (newFile==0)?FileDialog.LOAD:FileDialog.SAVE); fd.show();
 * 
 * String res=null; if (fd.getDirectory()!=null) res=fd.getDirectory(); if
 * (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile()); return
 * res; }
 * 
 * public void rFlushConsole (Rengine re) { }
 * 
 * public void rLoadHistory (Rengine re, String filename) { }
 * 
 * public void rSaveHistory (Rengine re, String filename) { } }*
 */