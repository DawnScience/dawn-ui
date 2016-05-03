package org.dawnsci.plotting.javafx.trace.plane;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.ServiceLoader;
import org.dawnsci.plotting.javafx.trace.Image3DTrace;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.IPlane3DTrace;

/**
 * !!!!!!!!!<br>
 * THIS CLASS HAS NOT BEEN TESTED AND WAS MADE IN ROUGHLY 1 HOUR<br>
 * DO NOT USE UNTIL IT IS TESTED<br>
 * !!!!!!!!!<br>
 * Remove this message once tested<br>
 * @author uij85458
 *
 */
public class PlaneTrace extends Image3DTrace implements IPlane3DTrace
{
	
	private ImagePlane imagePlane;
	private ILazyDataset lazyDataset;
	
	public PlaneTrace(IPlottingSystemViewer<?> plotter, String name) {
		super(plotter, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDataset getData() {
		if(lazyDataset==null){
	        throw new IllegalArgumentException("lazyDataset was null");
	    }	
		return lazyDataset.getSlice();
	}

	@Override
	public void setData(final int[] size, final IDataset imageData, final double[] offset, final double[] planeNormal) 
	{
		final PaletteService pservice = (PaletteService) ServiceLoader.getPaletteService();
		
		lazyDataset = imageData;
		imagePlane = new ImagePlane(
				new Point2D(size[0], size[1]), 
				lazyDataset, 
				new Point3D(offset[0], offset[1], offset[2]), 
				new Point3D(planeNormal[0],planeNormal[1],planeNormal[2]),
				pservice);
		
	}
	
	public ImagePlane plane()
	{
		return imagePlane;
	}

	@Override
	public void setOpacity(double opacity) {
		// TODO Auto-generated method stub
		
	}
	

}
