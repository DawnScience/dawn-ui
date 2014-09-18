package org.dawnsci.plotting.expression;

import org.eclipse.dawnsci.analysis.api.io.ILazyLoader;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;

class ExpressionLazyDataset extends LazyDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4008063310659517138L;

	public ExpressionLazyDataset(String name, int dtype, int[] shape, ILazyLoader loader) {
		super(name, dtype, shape, loader);
	}
	
	public void setShapeSilently(final int[] shape) {
		try {
			setShape(shape);
		} catch (IllegalArgumentException e) {
			size = Integer.MAX_VALUE; // this indicates that the entire dataset cannot be read in! 
		}
	}

}
