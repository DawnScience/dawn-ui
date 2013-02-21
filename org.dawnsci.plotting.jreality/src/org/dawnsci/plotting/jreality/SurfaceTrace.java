package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.dawb.common.services.ImageServiceBean;
import org.dawb.common.ui.plot.roi.data.SurfacePlotROI;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ISurfaceTrace;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * A class for holding surface trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 * @author fcp94556
 *
 */
public class SurfaceTrace extends PlotterTrace implements ISurfaceTrace{

	private AbstractDataset        data;
	private PaletteData            palette;
	
	public SurfaceTrace(JRealityPlotViewer plotter, String name) {
		super(plotter, name);
	}
	
	public PaletteData getPaletteData() {
		return palette;
	}

	/**
	 * This function updates the color mapping with a ColorMappingUpdate object
	 * @param update
	 */
	public void setPaletteData(PaletteData palette){
		this.palette = palette;
		if (isActive()) {
			ColourImageData imageData = createImageData();
			plotter.handleColourCast(imageData, data.min().doubleValue(), data.max().doubleValue());
			firePaletteDataListeners(palette);
		}
	}
	
	protected ColourImageData createImageData() {
		ColourImageData imageData = new ColourImageData(256,1);
		int lastValue=0;
		for (int i = 0; i < imageData.getWidth(); i++){
			int value =  ((255&0xff) << 24)+((palette.colors[i].red&0xff) << 16)+((palette.colors[i].green&0xff) << 8)+(palette.colors[i].blue&0xff);
			if(i==252)
				lastValue = value;
			else if(i==253||i==254||i==255)
				imageData.set(lastValue, i);
			else if(i>=0&&i<252)
				imageData.set(value, i);
		}
		
		return imageData;
	}

	public void setData(final AbstractDataset data, List<AbstractDataset> axes) {
		
		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.data = data;
		this.axes = axes;
		if (isActive()) {
			plotter.updatePlot(createAxisValues(), plotter.getWindow(getWindow()), PlottingMode.SURF2D, getData());
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}
	}

	public AbstractDataset getData() {
		return data;
	}
	
	private ImageServiceBean serviceBean;
	@Override
	public ImageServiceBean getImageServiceBean() {
		if (serviceBean==null) {
			serviceBean = new ImageServiceBean();
		}
		serviceBean.setPalette(getPaletteData());
		return serviceBean;
	}

	private Collection<IPaletteListener> paletteListeners;

	@Override
	public void addPaletteListener(IPaletteListener pl) {
		if (paletteListeners==null) paletteListeners = new HashSet<IPaletteListener>(11);
		paletteListeners.add(pl);
	}

	@Override
	public void removePaletteListener(IPaletteListener pl) {
		if (paletteListeners==null) return;
		paletteListeners.remove(pl);
	}
	
	
	private void firePaletteDataListeners(PaletteData paletteData) {
		if (paletteListeners==null) return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData()); // Important do not let Mark get at it :)
		for (IPaletteListener pl : paletteListeners) pl.paletteChanged(evt);
	}

	/**
	 * Also ignores data windows outside the data size.
	 */
	@Override
	public void setWindow(ROIBase window) {
		if (window instanceof RectangularROI && getData()!=null) {
			RectangularROI rroi = (RectangularROI)window;
			int[]       start = rroi.getIntPoint();
			final int[] lens  = rroi.getIntLengths();
			int[]       end   = new int[]{start[0]+lens[0], start[1]+lens[1]};
			
			// Ensure shape not outside
			start = normalize(start, getData().getShape()[1], getData().getShape()[0]);
			end   = normalize(end,   getData().getShape()[1], getData().getShape()[0]);
			
			window = new SurfacePlotROI(start[0], start[1], end[0], end[1], 0,0,0,0);
		}
			
		this.window = window;
		if (plotter!=null && this.isActive()) plotter.setSurfaceWindow(window);
	}

	private int[] normalize(int[] point, int maxX, int maxY) {
		if (point[0]<0) point[0]=0;
		if (point[0]>=maxX) point[0]=maxX-1;
		
		if (point[1]<0) point[1]=0;
		if (point[1]>=maxY) point[1]=maxY-1;
		return point;
	}
	
	@Override
	public boolean is3DTrace() {
		return true;
	}

}
