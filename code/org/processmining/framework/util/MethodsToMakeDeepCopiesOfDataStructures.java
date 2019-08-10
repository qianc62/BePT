/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * <p>
 * Title: Methods to Make Deep Copies of Data Structures
 * </p>
 * <p>
 * Description: Various methods to make a deep copy data structures that are
 * used in the framework.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: TU/e
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */

public class MethodsToMakeDeepCopiesOfDataStructures {

	/**
	 * Make a deep copy of a HashSet that contains TreeSets that contain
	 * Integer.
	 * 
	 * @param originalHash
	 *            - HashSet to be copied.
	 * @return a deep copy of originalHash
	 */
	public static final HashSet cloneHashSet(HashSet originalHash) {

		HashSet clonedHashSet = null;
		TreeSet clonedTreeSet = null;

		Iterator iteratorHashSet = null;
		Iterator iteratorTreeSet = null;

		if (originalHash != null) {
			clonedHashSet = new HashSet();
			iteratorHashSet = originalHash.iterator();
			while (iteratorHashSet.hasNext()) {
				iteratorTreeSet = ((TreeSet) iteratorHashSet.next()).iterator();
				clonedTreeSet = new TreeSet();
				while (iteratorTreeSet.hasNext()) {
					clonedTreeSet.add(new Integer(((Integer) iteratorTreeSet
							.next()).intValue()));
				}
				clonedHashSet.add(clonedTreeSet);
			}
		}

		return clonedHashSet;

	}

	/**
	 * Make a deep copy of a TreeSet that contains Integer.
	 * 
	 * @param originalTreeSet
	 *            - TreeSet to be copied.
	 * @return a deep copy of originalTreeSet
	 */
	public static final TreeSet cloneTreeSet(TreeSet originalTreeSet) {

		TreeSet clonedTreeSet = null;

		Iterator iteratorTreeSet = null;
		if (originalTreeSet != null) {

			iteratorTreeSet = originalTreeSet.iterator();
			clonedTreeSet = new TreeSet();
			while (iteratorTreeSet.hasNext()) {
				clonedTreeSet.add(new Integer(
						((Integer) iteratorTreeSet.next()).intValue()));
			}
		}

		return clonedTreeSet;

	}

	public static final ArrayList cloneCollectionViaSerialization(
			Collection originalList) {

		// Serialize List into byte array
		ArrayList newlist = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(originalList);
			byte buf[] = baos.toByteArray();
			oos.close();
			//
			// deserialize byte array into ArrayList
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			ObjectInputStream ois = new ObjectInputStream(bais);
			newlist = (ArrayList) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newlist;
	}

}
