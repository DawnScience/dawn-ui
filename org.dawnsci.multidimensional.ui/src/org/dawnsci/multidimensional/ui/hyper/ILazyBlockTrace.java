package org.dawnsci.multidimensional.ui.hyper;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

/**
 * Interface to represent a trace containing an
 * ND lazy dataset that may be sliced and permuted
 * 
 */
public interface ILazyBlockTrace extends ITrace {

	public void setData(ILazyDataset lazy, int[] order, SliceND slice);
	
	public ILazyDataset getLazyDataset();
	
	public int[] getOrder();
	
	public SliceND getSlice();
	
}
