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

import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.log.rfb.io.RandomAccessStorage;

/**
 * Lazy implementation of the random access storage in NikeFS2: Blocks are
 * copied as late as possible (soft copies), which makes sense due to the
 * immense creation of log reader copies in ProM.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class FS2LazyRandomAccessStorage extends FS2RandomAccessStorage {

	protected FS2LazyRandomAccessStorage parent;
	protected boolean isSoftCopy = true;
	protected ArrayList<FS2LazyRandomAccessStorage> softCopies;

	/**
	 * @param virtualFileSystem
	 */
	public FS2LazyRandomAccessStorage(FS2VirtualFileSystem virtualFileSystem) {
		super(virtualFileSystem);
		synchronized (FS2RandomAccessStorage.class) {
			isSoftCopy = false;
			parent = null;
			softCopies = new ArrayList<FS2LazyRandomAccessStorage>();
		}
	}

	public FS2LazyRandomAccessStorage(FS2LazyRandomAccessStorage template) {
		super(template.vfs);
		synchronized (FS2RandomAccessStorage.class) {
			isSoftCopy = true;
			softCopies = new ArrayList<FS2LazyRandomAccessStorage>();
			size = template.size;
			pointer = template.pointer;
			blocks = template.blocks;
			parent = template;
			template.registerSoftCopy(this);
		}
	}

	public synchronized void registerSoftCopy(FS2LazyRandomAccessStorage copycat) {
		softCopies.add(copycat);
	}

	public synchronized void deregisterSoftCopy(
			FS2LazyRandomAccessStorage copycat) {
		softCopies.remove(copycat);
	}

	public synchronized void alertSoftCopies() throws IOException {
		// make a copy of the list of soft copies, as they will deregister
		// within the loop (removing themselves from our internal list)
		FS2LazyRandomAccessStorage[] copies = softCopies
				.toArray(new FS2LazyRandomAccessStorage[softCopies.size()]);
		for (FS2LazyRandomAccessStorage copy : copies) {
			copy.consolidateSoftCopy();
		}
	}

	public synchronized void consolidateSoftCopy() throws IOException {
		if (isSoftCopy == true) {
			ArrayList<FS2Block> copyBlocks = new ArrayList<FS2Block>();
			if (blocks.size() > 0) {
				// make copies of all contained blocks
				byte[] buffer = new byte[blocks.get(0).size()];
				for (FS2Block block : blocks) {
					FS2Block copyBlock = vfs.allocateBlock();
					block.read(0, buffer);
					copyBlock.write(0, buffer);
					copyBlocks.add(copyBlock);
				}
			}
			// replace blocks list
			blocks = copyBlocks;
			isSoftCopy = false;
			// deregister from template
			parent.deregisterSoftCopy(this);
			parent = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#close()
	 */
	@Override
	public synchronized void close() throws IOException {
		alertSoftCopies();
		if (parent != null) {
			parent.deregisterSoftCopy(this);
		}
		if (isSoftCopy == false) {
			// frees our rightfully owned blocks
			super.close();
		} else {
			// shared blocks must not be freed (soft copy)
			blocks = null;
			size = 0;
			pointer = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#copy()
	 */
	@Override
	public synchronized RandomAccessStorage copy() throws IOException {
		return (RandomAccessStorage) (new FS2LazyRandomAccessStorage(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#write
	 * (byte[], int, int)
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len)
			throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.write(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#write
	 * (byte[])
	 */
	@Override
	public synchronized void write(byte[] b) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#write
	 * (int)
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.write(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeBoolean
	 * (boolean)
	 */
	@Override
	public synchronized void writeBoolean(boolean v) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeBoolean(v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeByte
	 * (int)
	 */
	@Override
	public synchronized void writeByte(int b) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeByte(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeBytes
	 * (java.lang.String)
	 */
	@Override
	public synchronized void writeBytes(String str) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeBytes(str);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeChar
	 * (int)
	 */
	@Override
	public synchronized void writeChar(int c) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeChar(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeChars
	 * (java.lang.String)
	 */
	@Override
	public synchronized void writeChars(String str) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeChars(str);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeDouble
	 * (double)
	 */
	@Override
	public synchronized void writeDouble(double d) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeDouble(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeFloat
	 * (float)
	 */
	@Override
	public synchronized void writeFloat(float f) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeFloat(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeInt
	 * (int)
	 */
	@Override
	public synchronized void writeInt(int i) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeInt(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeLong
	 * (long)
	 */
	@Override
	public synchronized void writeLong(long l) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeLong(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeShort
	 * (int)
	 */
	@Override
	public synchronized void writeShort(int s) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeShort(s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.log.rfb.fsio.FS2RandomAccessStorage#writeUTF
	 * (java.lang.String)
	 */
	@Override
	public synchronized void writeUTF(String str) throws IOException {
		consolidateSoftCopy();
		alertSoftCopies();
		super.writeUTF(str);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

}
