/**
 * Project: ProM
 * File: VirtualFileSystem.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 15, 2006, 9:30:21 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 *    - Redistributions of source code must retain the above copyright 
 *      notice, this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in 
 *      the documentation and/or other materials provided with the 
 *      distribution.
 *    - Neither the name of the Eindhoven Technical University nor the 
 *      names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written 
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *	Contact:
 *		TU Eindhoven
 *		Department of Technology Management
 *		Subdepartment of Information Systems
 *		Postbus 513
 *		5600 MB Eindhoven
 *		The Netherlands
 */
package org.processmining.framework.log.rfb.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.log.rfb.io.monitor.BlockDataStorageInfo;
import org.processmining.framework.ui.Message;

/**
 * This class implements a virtual file system, from which new files, as random
 * access storage containers, can be obtained. These files are only possible to
 * grow, i.e. cannot be compacted other than copying them to a newly allocated
 * file.
 * <p>
 * The virtual file system creates and manages blocks and their size
 * dynamically, however during the runtime of the virtual machine the amount of
 * memory used by the virtual file system does not decrease, i.e. there is no
 * garbage collection.
 * <p>
 * Virtual file systems are usually used as singleton, as the file system itself
 * is an abstraction and does not group logical entities of a specific class.
 * However, if you intend to use several, separated virtual file systems,
 * adequate constructors are provided.
 * <p>
 * The virtual file system makes a best effort, to delete the backing swap files
 * from disk at the moment of garbage collection or, again, at virtual machine
 * shutdown.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class VirtualFileSystem implements StorageProvider {

	/*
	 * This installs the shutdown hook, which will clear all files backing swap
	 * data from this class when the virtual machine shuts down. The shutdown
	 * hook is installed at the moment, at which this class is first loaded by
	 * the JVM.
	 */
	static {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		Message.add(
				"NikeFS: Shutdown hook installed for terminal cleanup tasks.",
				Message.DEBUG);
	}

	/**
	 * 32 Megabytes default swap file size
	 */
	public static final int DEFAULT_SWAP_FILE_SIZE = 33554432;
	/**
	 * Default prefix for swap files
	 */
	public static final String SWAP_PREFIX = "NIKEFS";
	/**
	 * Default suffix for swap files
	 */
	public static final String SWAP_SUFFIX = ".swap";
	/**
	 * Singleton instance
	 */
	protected static VirtualFileSystem instance = null;

	/**
	 * Singleton access method.
	 * 
	 * @return The singleton instance.
	 */
	public synchronized static VirtualFileSystem instance() {
		if (instance == null) {
			instance = new VirtualFileSystem();
		}
		return instance;
	}

	/**
	 * Size of a swap file.
	 */
	protected int swapFileSize = 0;
	/**
	 * Location where to keep swap files.
	 */
	protected File swapDir = null;
	/**
	 * List of allocated swap files.
	 */
	protected ArrayList<BlockDataStorage> swapFiles = null;

	/**
	 * Creates a new virtual file system with default settings.
	 */
	public VirtualFileSystem() {
		this(VirtualFileSystem.DEFAULT_SWAP_FILE_SIZE, new File(System
				.getProperty("java.io.tmpdir")));
	}

	/**
	 * Creates a customized instance of a virtual file system.
	 * 
	 * @param aSwapFileSize
	 *            Size of individual swap files in bytes.
	 * @param aSwapDirectory
	 *            Directory where to create and keep the swap files.
	 */
	public VirtualFileSystem(int aSwapFileSize, File aSwapDirectory) {
		synchronized (this) {
			swapFileSize = aSwapFileSize;
			swapDir = aSwapDirectory;
			swapFiles = new ArrayList<BlockDataStorage>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.StorageProvider#createStorage()
	 */
	public synchronized RandomAccessStorage createStorage() {
		return new VirtFsRandomAccessStorage(this);
	}

	/**
	 * Returns an array of current swap files used by this virtual file system
	 * 
	 * @return Array of currently used swap files
	 */
	public BlockDataStorage[] getSwapFiles() {
		return swapFiles.toArray(new BlockDataStorage[swapFiles.size()]);
	}

	/**
	 * Returns an array of information containers about each swap file currently
	 * used by this virtual file system
	 * 
	 * @return array of information containers, one for each swap file used
	 */
	public BlockDataStorageInfo[] getSwapFileInfos() {
		BlockDataStorageInfo[] infos = new BlockDataStorageInfo[swapFiles
				.size()];
		for (int i = 0; i < swapFiles.size(); i++) {
			infos[i] = swapFiles.get(i).getInfo();
		}
		return infos;
	}

	/**
	 * Allocates a new block from the first free swap file. The size of the
	 * returned block is undefined, but can be inquired from the returned
	 * instance. If no block can be allocated from an existing swap file
	 * storage, a new one is created.
	 * <p>
	 * This method is for use by file metaphors using the virtual file system as
	 * storage implementation. It is not intended to allocate blocks, if you
	 * intend to use them at a higher level of abstraction (byte buffers,
	 * files,...)
	 * 
	 * @param minSize
	 *            The minimum size of the returned storage block. Can be
	 *            <code>0</code> or negative, in this case the size of the
	 *            returned block is not checked.
	 * @return A newly allocated block.
	 * @throws IOException
	 */
	public synchronized StorageBlock allocateBlock(int minSize)
			throws IOException {
		StorageBlock block = null;
		for (BlockDataStorage bds : swapFiles) {
			// check each swap file, whether a block can be allocated.
			// if so, check size. if fit, return it immediately.
			block = bds.allocateBlock();
			if (block != null) {
				if (block.getMaxSize() >= minSize) {
					return block;
				} else {
					block.close();
				}
			}
		}
		// Could not allocate from current swap: add new swap file
		File swapFile = File.createTempFile(VirtualFileSystem.SWAP_PREFIX,
				VirtualFileSystem.SWAP_SUFFIX, swapDir);
		// the size of the swap file must be a multiple of the swap file size
		// that
		// is larger than the requested number of bytes
		int newSwapFileSize = swapFileSize;
		while (newSwapFileSize < minSize) {
			newSwapFileSize *= 2;
		}
		// create new swap file and add to list, return newly allocated block
		// from it
		BlockDataStorage swap = new BlockDataStorage(swapFile, newSwapFileSize);
		swapFiles.add(swap);
		Message.add("NikeFS: Allocating new swap file. (#" + (swapFiles.size())
				+ ": " + newSwapFileSize + " bytes)", Message.DEBUG);
		return swap.allocateBlock();
	}

	/**
	 * Remove swap files when this instance is garbage collected.
	 */
	protected void finalize() throws Throwable {
		File file = null;
		for (BlockDataStorage bds : swapFiles) {
			bds.close();
			file = bds.getFile();
			System.out
					.println("NikeFS: Removing swap file of garbage-collected storage ("
							+ file.getName() + ")");
			if (file.delete() == false) {
				file.deleteOnExit();
			}
		}
	}

	/*------------------------------------------------------------------------------
	 * Shutdown hook thread class implementation below:
	 */

	/**
	 * This class implements a shutdown hook, which will clear all swap files
	 * from the file system at the moment, at which the JVM shuts down.
	 * 
	 * @author Christian W. Guenther (christian at deckfour dot org)
	 */
	protected static class ShutdownHook extends Thread {

		/**
		 * Regular expression to match temporary file names.
		 */
		protected String tmpFileRegEx = VirtualFileSystem.SWAP_PREFIX + "(.*)"
				+ VirtualFileSystem.SWAP_SUFFIX;

		/**
		 * This method is invoked in a dedicated thread by the virtual machine,
		 * before it attempts shutdown. It will attempt to delete all temporary
		 * files used by VirtualFileSystem from the file system.
		 */
		public void run() {
			System.out.println("NikeFS: ShutdownHook invoked.. ");
			int cleaned = 0;
			File[] tmpFiles = (new File(System.getProperty("java.io.tmpdir")))
					.listFiles();
			for (int i = 0; i < tmpFiles.length; i++) {
				if (tmpFiles[i].getName().matches(tmpFileRegEx)) {
					if (tmpFiles[i].delete() == false) {
						tmpFiles[i].deleteOnExit();
					}
					cleaned++;
				}
			}
			System.out.println("NikeFS: cleaned " + cleaned + " stale files.");
		}
	}
}
