package org.dawnsci.multidimensional.ui.imagecompare;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.ILazyDataset;

public interface IImageCompareTrace extends ITrace {
	
	void setImages(List<ILazyDataset> images);
	
	List<ILazyDataset> getImages();

}
