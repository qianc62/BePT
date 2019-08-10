/**
 *
 */
package org.processmining.framework.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author christian
 * 
 */
public class Service {

	protected ServerSocket serverSocket;
	protected int port;
	protected ServiceHandler handler;
	protected boolean running;
	protected Thread serverThread;

	public Service(int aPort, ServiceHandler aHandler) {
		port = aPort;
		handler = aHandler;
		serverSocket = null;
		running = false;
		serverThread = null;
	}

	public void start() throws IOException {
		running = true;
		serverSocket = new ServerSocket(port);
		serverThread = new Thread() {
			public void run() {
				while (running == true) {
					try {
						// wait for and handle incoming connections
						Socket clientSocket = serverSocket.accept();
						handleConnection(clientSocket);
					} catch (IOException e) {
						// No connections were made yet, and this.stop() was
						// called.

						// System.err.println("Fatal error in server thread on port "
						// + port);
						// System.err.println("Accept failed with IO Exception:");
						// e.printStackTrace();
						this.interrupt();
						return;
					}
				}
			}
		};
		serverThread.start();
	}

	public void stop() {
		if (serverThread != null) {
			if (serverThread.isAlive()) {
				try {
					serverSocket.close();
				} catch (IOException ex) {
					// No connections were made yet, so the serverthread was
					// blocked in the accept() method.
				}
				running = false;
			}
		}
	}

	protected void handleConnection(final Socket socket) throws IOException {
		ConnectionHandlerThread handlerThread = new ConnectionHandlerThread(
				socket);
		handlerThread.start();
	}

	protected class ConnectionHandlerThread extends Thread {

		protected Socket clientSocket;

		public ConnectionHandlerThread(Socket aSocket) {
			this.clientSocket = aSocket;
		}

		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				PrintWriter out = new PrintWriter(clientSocket
						.getOutputStream(), true);
				handler.handleRequest(in, out);
			} catch (IOException e) {
				// abort and give up
				System.err.println("Fatal error handling connection,");
				System.err.println("failed with IO Exception:");
				e.printStackTrace();
			} finally {
				// clean up connection
				try {
					if (clientSocket != null) {
						clientSocket.close();
					}
				} catch (IOException ie) { /* this one's forsaken.. */
				}
			}
		}
	}

}
