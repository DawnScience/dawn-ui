package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;

public class PlotShapeUtils {

	public static int getPlottableRank(ILazyDataset lz) {
		int[] shape = lz.getShape();
		int[] max = shape;
		if (lz instanceof IDynamicDataset) {
			max = ((IDynamicDataset) lz).getMaxShape();
		}
		if (shape.length != 1 || shape[0] != 1 || max[0] == 1) {
			shape = ShapeUtils.squeezeShape(shape, false);
		} 
		
		return shape.length;
	}
	
}
