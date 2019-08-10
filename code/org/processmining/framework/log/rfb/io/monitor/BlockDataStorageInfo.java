/**
 * Project: ProM Framework
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: Oct 14, 2006 10:51:22 PM
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
package org.processmining.framework.log.rfb.io.monitor;

import java.io.File;

/**
 * Stores information about the internal state of one block data storage
 * instance.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class BlockDataStorageInfo {

	protected File file;
	protected int partitionLevel;
	protected int numberOfBlocks;
	protected int blockSize;
	protected int[] blockFillSizes;

	public BlockDataStorageInfo(File aFile, int aPartitionLevel,
			int aNumberOfBlocks, int aBlockSize, int aBlockFillSizes[]) {
		file = aFile;
		partitionLevel = aPartitionLevel;
		numberOfBlocks = aNumberOfBlocks;
		blockSize = aBlockSize;
		blockFillSizes = aBlockFillSizes;
	}

	public File getFile() {
		return file;
	}

	public int getPartitionLevel() {
		return partitionLevel;
	}

	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getBlockFillSize(int index) {
		return blockFillSizes[index];
	}

	public float getBlockFillRatio(int index) {
		return ((float) blockFillSizes[index] / (float) blockSize);
	}

	public int[] getBlockFillSizes() {
		return blockFillSizes;
	}

	public int getNumberOfFreeBlocks() {
		int numberOfFreeBlocks = 0;
		for (int i = 0; i < blockFillSizes.length; i++) {
			if (blockFillSizes[i] == 0) {
				numberOfFreeBlocks++;
			}
		}
		return numberOfFreeBlocks;
	}
}