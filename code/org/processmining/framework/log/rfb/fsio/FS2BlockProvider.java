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
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;

import org.processmining.framework.ui.Message;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FS2BlockProvider {

	protected static Random random = new Random();
	protected static int shadowSize = 4;
	protected static int currentShadowSize = 0;
	protected static int lastRequestIndex = 0;
	protected static MappedByteBuffer[] centralMaps = null;
	protected static FS2BlockProvider[] currentMapOwners = null;

	static {
		centralMaps = new MappedByteBuffer[shadowSize];
		currentMapOwners = new FS2BlockProvider[shadowSize];
	}

	protected synchronized static MappedByteBuffer requestMap(
			FS2BlockProvider requester) throws IOException {
		synchronized (FS2BlockProvider.class) {
			// check if requested map is already in shadow
			for (int i = 0; i < currentShadowSize; i++) {
				if (currentMapOwners[i] == requester) {
					// requester found in shadow; return shadowed map
					lastRequestIndex = i;
					return centralMaps[i];
				}
			}
			// check if we can create another shadow map
			if (currentShadowSize < shadowSize) {
				// create new map in shadow in pristine place
				currentMapOwners[currentShadowSize] = requester;
				FileChannel channel = requester.getFile().getChannel();
				MappedByteBuffer map = channel.map(
						FileChannel.MapMode.READ_WRITE, 0, requester.size());
				centralMaps[currentShadowSize] = map;
				lastRequestIndex = currentShadowSize;
				currentShadowSize++;
				Message.add("NikeFS2: Populating shadow map "
						+ currentShadowSize + " (of " + shadowSize + " max.)",
						Message.DEBUG);
				return map;
			} else {
				// we need to displace one shadow to make place
				int kickIndex = random.nextInt(shadowSize - 1);
				if (kickIndex == lastRequestIndex) {
					kickIndex++;
				}
				centralMaps[kickIndex].force();
				currentMapOwners[kickIndex] = requester;
				FileChannel channel = requester.getFile().getChannel();
				MappedByteBuffer map = channel.map(
						FileChannel.MapMode.READ_WRITE, 0, requester.size());
				centralMaps[kickIndex] = map;
				lastRequestIndex = kickIndex;
				Message.add("NikeFS2: Displacing shadow map " + kickIndex,
						Message.DEBUG);
				System.gc();
				return map;
			}
		}
	}

	protected boolean mapped;
	protected RandomAccessFile file;
	protected int size;
	protected int blockSize;
	protected int numberOfBlocks;
	protected ArrayList<FS2Block> freeBlocks;

	public FS2BlockProvider(File storage, int size, int blockSize,
			boolean mapped) throws IOException {
		synchronized (this) {
			this.mapped = mapped;
			this.size = size;
			this.blockSize = blockSize;
			if (storage.exists() == false) {
				storage.createNewFile();
			}
			this.file = new RandomAccessFile(storage, "rw");
			numberOfBlocks = size / blockSize;
			freeBlocks = new ArrayList<FS2Block>();
			for (int i = 0; i < numberOfBlocks; i++) {
				FS2Block block = new FS2Block(this, i);
				freeBlocks.add(block);
			}
		}
	}

	public RandomAccessFile getFile() {
		return file;
	}

	public int size() {
		return size;
	}

	public int numberOfBlocks() {
		return numberOfBlocks;
	}

	public synchronized int numberOfFreeBlocks() {
		return freeBlocks.size();
	}

	public int blockSize() {
		return blockSize;
	}

	public synchronized FS2Block allocateBlock() {
		if (freeBlocks.size() > 0) {
			return freeBlocks.remove(0);
		} else {
			return null;
		}
	}

	public synchronized void freeBlock(FS2Block block) {
		freeBlocks.add(block);
	}

	public int getBlockOffset(int blockNumber) {
		return blockNumber * blockSize;
	}

	public synchronized int read(int blockNumber, int blockOffset, byte[] buffer)
			throws IOException {
		return read(blockNumber, blockOffset, buffer, 0, buffer.length);
	}

	public synchronized int read(int blockNumber, int blockOffset,
			byte[] buffer, int bufferOffset, int length) throws IOException {
		long pointer = getBlockOffset(blockNumber) + blockOffset;
		int readable = blockSize - blockOffset;
		int readLength = length;
		if (readable < length) {
			readLength = readable;
		}
		if (mapped == true) {
			MappedByteBuffer map = FS2BlockProvider.requestMap(this);
			map.position((int) pointer);
			map.get(buffer, bufferOffset, readLength);
			return readLength;
		} else {
			file.seek(pointer);
			return file.read(buffer, bufferOffset, readLength);
		}
	}

	public synchronized int read(int blockNumber, int blockOffset)
			throws IOException {
		long pointer = getBlockOffset(blockNumber) + blockOffset;
		if (mapped == true) {
			MappedByteBuffer map = FS2BlockProvider.requestMap(this);
			map.position((int) pointer);
			int result = map.get();
			return result + 128;
		} else {
			file.seek(pointer);
			return file.read();
		}
	}

	public synchronized void write(int blockNumber, int blockOffset,
			byte[] buffer) throws IOException {
		write(blockNumber, blockOffset, buffer, 0, buffer.length);
	}

	public synchronized void write(int blockNumber, int blockOffset,
			byte[] buffer, int bufferOffset, int length) throws IOException {
		long pointer = getBlockOffset(blockNumber) + blockOffset;
		int writable = blockSize - blockOffset;
		int writeLength = length;
		if (writable < length) {
			writeLength = writable;
		}
		if (mapped == true) {
			MappedByteBuffer map = FS2BlockProvider.requestMap(this);
			map.position((int) pointer);
			map.put(buffer, bufferOffset, writeLength);
		} else {
			file.seek(pointer);
			file.write(buffer, bufferOffset, writeLength);
		}
	}

	public synchronized void write(int blockNumber, int blockOffset, int value)
			throws IOException {
		long pointer = getBlockOffset(blockNumber) + blockOffset;
		if (mapped == true) {
			MappedByteBuffer map = FS2BlockProvider.requestMap(this);
			map.position((int) pointer);
			map.put((byte) (value - 128));
		} else {
			file.seek(pointer);
			file.write(value);
		}
	}

}
