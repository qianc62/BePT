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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.log.rfb.io.RandomAccessStorage;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FS2RandomAccessStorage implements RandomAccessStorage {

	protected FS2VirtualFileSystem vfs;
	protected List<FS2Block> blocks;
	protected long size;
	protected long pointer;
	protected DataOutputStream dataOutputStream;
	protected DataInputStream dataInputStream;

	public FS2RandomAccessStorage(FS2VirtualFileSystem virtualFileSystem) {
		vfs = virtualFileSystem;
		size = 0;
		pointer = 0;
		blocks = new ArrayList<FS2Block>();
		dataOutputStream = new DataOutputStream(new FS2BlockOutputStream());
		dataInputStream = new DataInputStream(new FS2BlockInputStream());
	}

	protected synchronized void adjustSize() {
		if (pointer > size) {
			size = pointer;
		}
	}

	protected int translateToBlockNumber(long offset) {
		return (int) (offset / vfs.blockSize());
	}

	protected int translateToBlockOffset(long offset) {
		return (int) (offset % vfs.blockSize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#close()
	 */
	public synchronized void close() throws IOException {
		for (int i = blocks.size() - 1; i >= 0; i--) {
			blocks.remove(i).close();
		}
		size = 0;
		pointer = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#copy()
	 */
	public synchronized RandomAccessStorage copy() throws IOException {
		// NOTE: This method's implementation assumes that all blocks served
		// from the virtual file system are of equal block size!
		// If this condition does not hold, unexpected results will be
		// expected!
		FS2RandomAccessStorage clone = new FS2RandomAccessStorage(vfs);
		if (blocks.size() > 0) {
			// add copies of all contained blocks
			byte[] buffer = new byte[blocks.get(0).size()];
			for (FS2Block block : blocks) {
				FS2Block copyBlock = vfs.allocateBlock();
				block.read(0, buffer);
				copyBlock.write(0, buffer);
				clone.blocks.add(copyBlock);
			}
		}
		// adjust state
		clone.size = size;
		clone.pointer = 0;
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#getFilePointer
	 * ()
	 */
	public synchronized long getFilePointer() throws IOException {
		return pointer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.log.rfb.io.RandomAccessStorage#length()
	 */
	public synchronized long length() throws IOException {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#seek(long)
	 */
	public synchronized void seek(long pos) throws IOException {
		pointer = pos;
		adjustSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.io.RandomAccessStorage#skipBytes(int)
	 */
	public synchronized int skipBytes(int n) throws IOException {
		pointer += n;
		adjustSize();
		return n;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(int)
	 */
	public synchronized void write(int b) throws IOException {
		dataOutputStream.write(b);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[])
	 */
	public synchronized void write(byte[] b) throws IOException {
		dataOutputStream.write(b);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#write(byte[], int, int)
	 */
	public synchronized void write(byte[] b, int off, int len)
			throws IOException {
		dataOutputStream.write(b, off, len);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBoolean(boolean)
	 */
	public synchronized void writeBoolean(boolean v) throws IOException {
		dataOutputStream.writeBoolean(v);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeByte(int)
	 */
	public synchronized void writeByte(int b) throws IOException {
		dataOutputStream.writeByte(b);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeBytes(java.lang.String)
	 */
	public synchronized void writeBytes(String str) throws IOException {
		dataOutputStream.writeBytes(str);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChar(int)
	 */
	public synchronized void writeChar(int c) throws IOException {
		dataOutputStream.writeChar(c);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeChars(java.lang.String)
	 */
	public synchronized void writeChars(String str) throws IOException {
		dataOutputStream.writeChars(str);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeDouble(double)
	 */
	public synchronized void writeDouble(double d) throws IOException {
		dataOutputStream.writeDouble(d);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeFloat(float)
	 */
	public synchronized void writeFloat(float f) throws IOException {
		dataOutputStream.writeFloat(f);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeInt(int)
	 */
	public synchronized void writeInt(int i) throws IOException {
		dataOutputStream.writeInt(i);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeLong(long)
	 */
	public synchronized void writeLong(long l) throws IOException {
		dataOutputStream.writeLong(l);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeShort(int)
	 */
	public synchronized void writeShort(int s) throws IOException {
		dataOutputStream.writeShort(s);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataOutput#writeUTF(java.lang.String)
	 */
	public synchronized void writeUTF(String str) throws IOException {
		dataOutputStream.writeUTF(str);
		dataOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readBoolean()
	 */
	public synchronized boolean readBoolean() throws IOException {
		return dataInputStream.readBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readByte()
	 */
	public synchronized byte readByte() throws IOException {
		return dataInputStream.readByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readChar()
	 */
	public synchronized char readChar() throws IOException {
		return dataInputStream.readChar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readDouble()
	 */
	public synchronized double readDouble() throws IOException {
		return dataInputStream.readDouble();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFloat()
	 */
	public synchronized float readFloat() throws IOException {
		return dataInputStream.readFloat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public synchronized void readFully(byte[] b) throws IOException {
		dataInputStream.readFully(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public synchronized void readFully(byte[] b, int off, int len)
			throws IOException {
		dataInputStream.readFully(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readInt()
	 */
	public synchronized int readInt() throws IOException {
		return dataInputStream.readInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLine()
	 */
	public synchronized String readLine() throws IOException {
		return dataInputStream.readLine();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readLong()
	 */
	public synchronized long readLong() throws IOException {
		return dataInputStream.readLong();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readShort()
	 */
	public synchronized short readShort() throws IOException {
		return dataInputStream.readShort();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUTF()
	 */
	public synchronized String readUTF() throws IOException {
		return dataInputStream.readUTF();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public synchronized int readUnsignedByte() throws IOException {
		return dataInputStream.readUnsignedByte();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public synchronized int readUnsignedShort() throws IOException {
		return dataInputStream.readUnsignedShort();
	}

	/**
	 * Internal support class implementing an input stream over a list of
	 * blocks, as implemented by the enclosing class
	 * 
	 * @author Christian W. Guenther (christian@deckfour.org)
	 * 
	 */
	protected class FS2BlockInputStream extends InputStream {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read()
		 */
		@Override
		public synchronized int read() throws IOException {
			int blockNumber = translateToBlockNumber(pointer);
			if (blockNumber >= blocks.size()) {
				throw new AssertionError(
						"addressing invalid block for reading! (1)");
			}
			FS2Block block = blocks.get(blockNumber);
			int blockOffset = translateToBlockOffset(pointer);
			pointer++;
			return block.read(blockOffset);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read(byte[], int, int)
		 */
		@Override
		public synchronized int read(byte[] buffer, int offset, int length)
				throws IOException {
			int blockNumber = translateToBlockNumber(pointer);
			if (blockNumber >= blocks.size()) {
				throw new AssertionError(
						"addressing invalid block for reading! (1)");
			}
			int blockOffset = translateToBlockOffset(pointer);
			FS2Block block = blocks.get(blockNumber);
			int readBytes = block.read(blockOffset, buffer, offset, length);
			length -= readBytes;
			offset += readBytes;
			// in case not all data could be read from first block
			while (length > 0) {
				blockNumber++;
				if (blockNumber < blocks.size()) {
					block = blocks.get(blockNumber);
					int readNow = block.read(0, buffer, offset, length);
					readBytes += readNow;
					offset += readNow;
					length -= readNow;
				} else {
					break;
				}
			}
			pointer += readBytes;
			return readBytes;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#read(byte[])
		 */
		@Override
		public synchronized int read(byte[] buffer) throws IOException {
			return this.read(buffer, 0, buffer.length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#skip(long)
		 */
		@Override
		public synchronized long skip(long skip) throws IOException {
			long nPointer = pointer + skip;
			if (nPointer > size) {
				long skipped = size - pointer;
				pointer = size;
				return skipped;
			} else {
				pointer += skip;
				return skip;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.InputStream#markSupported()
		 */
		@Override
		public boolean markSupported() {
			return false;
		}

	}

	/**
	 * Internal support class implementing an output stream over a list of
	 * blocks, as implemented by the enclosing class
	 * 
	 * @author Christian W. Guenther (christian@deckfour.org)
	 * 
	 */
	protected class FS2BlockOutputStream extends OutputStream {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public synchronized void write(int value) throws IOException {
			int blockNumber = translateToBlockNumber(pointer);
			int blockOffset = translateToBlockOffset(pointer);
			// allocate blocks if necessary
			while (blockNumber >= blocks.size()) {
				blocks.add(vfs.allocateBlock());
			}
			FS2Block block = blocks.get(blockNumber);
			block.write(blockOffset, value);
			pointer++;
			adjustSize();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		@Override
		public synchronized void write(byte[] buffer, int offset, int length)
				throws IOException {
			int blockNumber = translateToBlockNumber(pointer);
			int blockOffset = translateToBlockOffset(pointer);
			// allocate blocks if necessary
			while (blockNumber >= blocks.size()) {
				blocks.add(vfs.allocateBlock());
			}
			FS2Block block = blocks.get(blockNumber);
			int bytesToWrite = block.size() - blockOffset;
			if (bytesToWrite > length) {
				bytesToWrite = length;
			}
			block.write(blockOffset, buffer, offset, bytesToWrite);
			length -= bytesToWrite;
			offset += bytesToWrite;
			int writtenBytes = bytesToWrite;
			// in case not all data could be written to the first block
			while (length > 0) {
				blockNumber++;
				// get next block to write to
				if (blockNumber >= blocks.size()) {
					// allocate new block
					blocks.add(vfs.allocateBlock());
				}
				block = blocks.get(blockNumber);
				// determine chunk size to write now
				bytesToWrite = block.size();
				if (bytesToWrite > length) {
					bytesToWrite = length;
				}
				// write
				block.write(0, buffer, offset, bytesToWrite);
				writtenBytes += bytesToWrite;
				length -= bytesToWrite;
				offset += bytesToWrite;
			}
			pointer += writtenBytes;
			adjustSize();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(byte[])
		 */
		@Override
		public synchronized void write(byte[] buffer) throws IOException {
			this.write(buffer, 0, buffer.length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#flush()
		 */
		@Override
		public void flush() throws IOException {
			// nothing to do here.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable {
			this.close();
			super.finalize();
		}

	}

}
