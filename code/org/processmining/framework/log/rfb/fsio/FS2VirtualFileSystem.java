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
package org.processmining.framework.log.rfb.fsio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.rfb.io.RandomAccessStorage;
import org.processmining.framework.log.rfb.io.StorageProvider;
import org.processmining.framework.ui.Message;

/**
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FS2VirtualFileSystem implements StorageProvider {

	/*
	 * This installs the shutdown hook, which will clear all files backing swap
	 * data from this class when the virtual machine shuts down. The shutdown
	 * hook is installed at the moment, at which this class is first loaded by
	 * the JVM.
	 */
	static {
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		Message.add(
				"NikeFS2: Shutdown hook installed for terminal cleanup tasks.",
				Message.DEBUG);
	}

	protected static FS2VirtualFileSystem instance = null;

	/**
	 * Singleton access method.
	 * 
	 * @return The singleton instance.
	 */
	public synchronized static FS2VirtualFileSystem instance() {
		if (instance == null) {
			instance = new FS2VirtualFileSystem();
		}
		return instance;
	}

	/**
	 * Default prefix for swap files
	 */
	public static final String SWAP_PREFIX = "NIKEFS2";
	/**
	 * Default suffix for swap files
	 */
	public static final String SWAP_SUFFIX = ".swap2";
	/**
	 * Default directory for storing swap files
	 */
	public static final File SWAP_DIR = new File(System
			.getProperty("java.io.tmpdir"));

	protected int blockSize = 2048;
	protected int swapFileSize = 67108864;
	protected boolean useLazyCopies = true;

	protected List<FS2BlockProvider> blockProviders;

	public FS2VirtualFileSystem() {
		blockProviders = new ArrayList<FS2BlockProvider>();
	}

	public void setUseLazyCopies(boolean useLazyCopies) {
		this.useLazyCopies = useLazyCopies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.StorageProvider#createStorage()
	 */
	public RandomAccessStorage createStorage() throws IOException {
		if (useLazyCopies == true) {
			return new FS2LazyRandomAccessStorage(this);
		} else {
			return new FS2RandomAccessStorage(this);
		}
	}

	public int blockSize() {
		return blockSize;
	}

	public synchronized FS2Block allocateBlock() throws IOException {
		// try to allocate from already created providers first
		for (FS2BlockProvider provider : blockProviders) {
			if (provider.numberOfFreeBlocks() > 0) {
				FS2Block block = provider.allocateBlock();
				if (block != null) {
					return block;
				}
			}
		}
		// force garbage collection and try again (stale files still around?)
		System.gc();
		System.runFinalization();
		Thread.yield();
		for (FS2BlockProvider provider : blockProviders) {
			if (provider.numberOfFreeBlocks() > 0) {
				FS2Block block = provider.allocateBlock();
				if (block != null) {
					return block;
				}
			}
		}
		// ok, we give up:
		// create new swap file and provider, and allocate from there
		Message.add(
				"NikeFS2: Allocating new swap file. (#"
						+ (blockProviders.size() + 1) + ": " + swapFileSize
						+ " bytes)", Message.DEBUG);
		File swapFile = File
				.createTempFile(FS2VirtualFileSystem.SWAP_PREFIX,
						FS2VirtualFileSystem.SWAP_SUFFIX,
						FS2VirtualFileSystem.SWAP_DIR);
		FS2BlockProvider addedProvider = new FS2BlockProvider(swapFile,
				swapFileSize, blockSize, true);
		blockProviders.add(addedProvider);
		return addedProvider.allocateBlock();
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
		protected String tmpFileRegEx = FS2VirtualFileSystem.SWAP_PREFIX
				+ "(.*)" + FS2VirtualFileSystem.SWAP_SUFFIX;

		/**
		 * This method is invoked in a dedicated thread by the virtual machine,
		 * before it attempts shutdown. It will attempt to delete all temporary
		 * files used by VirtualFileSystem from the file system.
		 */
		public void run() {
			System.out.println("NikeFS2: ShutdownHook invoked.. ");
			int cleaned = 0;
			File[] tmpFiles = FS2VirtualFileSystem.SWAP_DIR.listFiles();
			for (int i = 0; i < tmpFiles.length; i++) {
				if (tmpFiles[i].getName().matches(tmpFileRegEx)) {
					if (tmpFiles[i].delete() == false) {
						tmpFiles[i].deleteOnExit();
					}
					cleaned++;
				}
			}
			System.out.println("NikeFS2: cleaned " + cleaned + " stale files.");
		}
	}

}
