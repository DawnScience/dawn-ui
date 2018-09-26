package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.Slice;

public interface ISliceAssist {
	
	Slice getSlice(NDimensions ndims, int dimension);
	
	String getLabel();

}
