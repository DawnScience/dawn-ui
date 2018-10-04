package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ShapeUtils;

public class PlotShapeUtils {

	private PlotShapeUtils() {}
	
	public static int getPlottableRank(ILazyDataset lz, boolean mayGrow) {
		int[] shape = lz.getShape();
		int[] max = shape;
		
		//if a live scan, use maxshape as single dimensions may grow
		if (lz instanceof IDynamicDataset && mayGrow) {
			max = ((IDynamicDataset) lz).getMaxShape();
		}
		
		shape = ShapeUtils.squeezeShape(max, false);
		
		return shape.length;
	}
	
}
