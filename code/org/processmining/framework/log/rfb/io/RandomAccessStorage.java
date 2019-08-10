/**
 * Project: ProM
 * File: RandomAccessStorage.java
 * Author: Christian W. Guenther (christian@deckfour.org)
 * Created: May 12, 2006, 11:55:34 PM
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This interface specifies a random acess data storage container, pretty much
 * the same as <code>java.util.RandomAccessFile</code>.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public abstract interface RandomAccessStorage extends DataOutput, DataInput {

	/**
	 * Closes the data storage container. After this method has been invoked, no
	 * further access to the represented instance is allowed.
	 * 
	 * @throws IOException
	 */
	public abstract void close() throws IOException;

	/**
	 * Returns the current file pointer of the storage container. A file pointer
	 * is the offset in bytes, from the beginning of the sequential byte
	 * storage, at which the next read or write operation would occur.
	 * 
	 * @return Offset in bytes from beginning of storage.
	 * @throws IOException
	 */
	public abstract long getFilePointer() throws IOException;

	/**
	 * Returns the length, or size, in number of bytes currently used by this
	 * instance.
	 * 
	 * @return Number of bytes currently allocated.
	 * @throws IOException
	 */
	public abstract long length() throws IOException;

	/**
	 * Repositions the offset, or file pointer, at which the next read or write
	 * operation will occur.
	 * 
	 * @param pos
	 *            The offset in bytes, at which the next operation will occur.
	 * @throws IOException
	 */
	public abstract void seek(long pos) throws IOException;

	/**
	 * Moves the offset, or file pointer, a specified number of bytes towards
	 * the end of the storage container.
	 */
	public abstract int skipBytes(int n) throws IOException;

	/**
	 * Creates a clone, or copy, of this storage, having the exact same contents
	 * and the file pointer reset to zero.
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract RandomAccessStorage copy() throws IOException;

}
