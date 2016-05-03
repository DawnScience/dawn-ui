package org.dawnsci.plotting.javafx.trace.spline;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.ServiceLoader;
import org.dawnsci.plotting.javafx.trace.Image3DTrace;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ILine3DTrace;

/**
 * !!!!!!!!!<br>
 * THIS CLASS HAS NOT BEEN TESTED AND WAS MADE IN ROUGHLY 1 HOUR<br>
 * DO NOT USE UNTIL IT IS TESTED<br>
 * !!!!!!!!!<br>
 * Remove this message once tested<br>
 * @author uij85458
 *
 */
public class LineTrace extends Image3DTrace implements ILine3DTrace
{
	private ILazyDataset lazyDataset;
	private LineGroup line;
	
	public LineTrace(IPlottingSystemViewer<?> plotter, String name) {
		super(plotter, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setData(final IDataset points) 
	{
		final PaletteService pservice = (PaletteService) ServiceLoader.getPaletteService();
		
		lazyDataset = points;
		line = new LineGroup(points);
		
	}
	

	@Override
	public IDataset getData() {
		if(lazyDataset==null){
	        throw new IllegalArgumentException("lazyDataset was null");
	    }	
		return lazyDataset.getSlice();
	}
	
}
