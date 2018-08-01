package org.dawnsci.slicing.tools.volume;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;

/**
 * Interface for the traces for the VolumeSlicePlotViewer
 *
 */
public interface IVolumeSlicesTrace extends ITrace {
	
	public void setData(ILazyDataset data);

}
