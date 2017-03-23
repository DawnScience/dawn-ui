package org.dawnsci.slicing.tools.hyper;


import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;

public interface IHyperTrace extends ITrace {

	public void setData(ILazyDataset lazy, int[] order);
	
}
