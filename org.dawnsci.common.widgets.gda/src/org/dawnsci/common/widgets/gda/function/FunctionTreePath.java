/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreePath;

public class FunctionTreePath {

	private static final class ElementComparer implements
			IElementComparer {
		@Override
		public int hashCode(Object element) {
			return System.identityHashCode(element);
		}

		@Override
		public boolean equals(Object a, Object b) {
			return a == b;

		}
	}

	public static final FunctionTreePath ROOT = new FunctionTreePath(
			new IFunction[0]);

	private TreePath treePath;
	private IFunction[] segments;

	private static ElementComparer comparer = new ElementComparer();

	public FunctionTreePath(IFunction[] iFunctions) {
		treePath = new TreePath(iFunctions);
		segments = iFunctions;
	}

	/**
	 * Returns the element at the specified index in this path.
	 *
	 * @param index
	 *            index of element to return
	 * @return element at the specified index
	 */
	public IFunction getSegment(int index) {
		return (IFunction) treePath.getSegment(index);
	}

	/**
	 * Returns the number of elements in this path.
	 *
	 * @return the number of elements in this path
	 */
	public int getSegmentCount() {
		return treePath.getSegmentCount();
	}

	/**
	 * Returns the first element in this path, or <code>null</code> if this path
	 * has no segments.
	 *
	 * @return the first element in this path
	 */
	public IFunction getFirstSegment() {
		return (IFunction) treePath.getFirstSegment();
	}

	/**
	 * Returns the last element in this path, or <code>null</code> if this path
	 * has no segments.
	 *
	 * @return the last element in this path
	 */
	public IFunction getLastSegment() {
		return (IFunction) treePath.getLastSegment();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FunctionTreePath)) {
			return false;
		}
		FunctionTreePath otherFunctionTreePath = (FunctionTreePath)other;
		return treePath.equals(otherFunctionTreePath.treePath, comparer);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return treePath.hashCode(comparer);
	}

	/**
	 * Returns whether this path starts with the same segments as the given
	 * path, using the given comparer to compare segments.
	 *
	 * @param treePath
	 *            path to compare to
	 * @return whether the given path is a prefix of this path, or the same as
	 *         this path
	 */
	public boolean startsWith(FunctionTreePath starts) {
		return treePath.startsWith(starts.treePath, comparer);
	}

	/**
	 * Returns a copy of this tree path with one segment removed from the end,
	 * or <code>null</code> if this tree path has no segments.
	 *
	 * @return a tree path
	 */
	public FunctionTreePath getParentPath() {
		int segmentCount = getSegmentCount();
		if (segmentCount < 1) {
			return null;
		} else if (segmentCount == 1) {
			return ROOT;
		}
		IFunction[] parentSegments = new IFunction[segmentCount - 1];
		System.arraycopy(segments, 0, parentSegments, 0, segmentCount - 1);
		return new FunctionTreePath(parentSegments);
	}

	/**
	 * Returns a copy of this tree path with the given segment added at the end.
	 *
	 * @param newSegment
	 * @return a tree path
	 */
	public FunctionTreePath createChildPath(IFunction newSegment) {
		int segmentCount = getSegmentCount();
		IFunction[] childSegments = new IFunction[segmentCount + 1];
		if (segmentCount > 0) {
			System.arraycopy(segments, 0, childSegments, 0, segmentCount);
		}
		childSegments[segmentCount] = newSegment;
		return new FunctionTreePath(childSegments);
	}

	public boolean isPathIn(IOperator root2) {
		// TODO: make sure segments is a path in root2
		return true;
	}

}
