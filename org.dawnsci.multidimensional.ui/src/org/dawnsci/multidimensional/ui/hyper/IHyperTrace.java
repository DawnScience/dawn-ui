package org.dawnsci.multidimensional.ui.hyper;


import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public interface IHyperTrace extends ITrace {

	public void setData(ILazyDataset lazy, int[] order, SliceND slice);
	
	public ILazyDataset getLazyDataset();
	
	public int[] getOrder();
	
	public SliceND getSlice();
}
