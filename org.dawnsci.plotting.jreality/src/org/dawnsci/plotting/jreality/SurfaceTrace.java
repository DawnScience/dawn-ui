package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ISurfaceTrace;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.services.ImageServiceBean;
import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * A class for holding surface trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 * @author fcp94556
 *
 */
public class SurfaceTrace implements ISurfaceTrace{

	private String                 name;
	private AbstractDataset        data;
	private List<AbstractDataset>  axes;
	private List<String>           axesNames;
	private JRealityPlotViewer     plotter;
	private boolean                active;
	private PaletteData            palette;
	private AbstractPlottingSystem plottingSystem;
	private ROIBase                window;
	
	public SurfaceTrace(JRealityPlotViewer plotter, String name) {
		this.plotter = plotter;
		this.name    = name;
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
			plotter.plot(getData(), createAxisValues(), plotter.getWindow(getWindow()), PlottingMode.SURF2D);
			
			if (plottingSystem!=null) {
				plottingSystem.fireTraceUpdated(new TraceEvent(this));
			}
		}
	}

	public String getName() {
		return name;
	}

	public List<AbstractDataset> getAxes() {
		return axes;
	}

	public AbstractDataset getData() {
		return data;
	}

	public boolean isActive() {
		return active;
	}

	protected final void setActive(boolean active) {
		this.active = active;
		if (active) {
			if (plottingSystem!=null) plottingSystem.fireTraceAdded(new TraceEvent(this));
		}
	}

	protected List<AxisValues> createAxisValues() {
		
		final AxisValues xAxis = new AxisValues(getLabel(0), axes!=null?axes.get(0):null);
		final AxisValues yAxis = new AxisValues(getLabel(1), axes!=null?axes.get(1):null);
		final AxisValues zAxis = new AxisValues(getLabel(2), axes!=null?axes.get(2):null);
		return Arrays.asList(xAxis, yAxis, zAxis);
	}

	private String getLabel(int i) {
		String label = axesNames!=null ? axesNames.get(i) : null;
		if  (label==null) label = (axes!=null && axes.get(i)!=null) ? axes.get(i).getName() : null;
		return label;
	}

	public List<String> getAxesNames() {
		return axesNames;
	}

	public void setAxesNames(List<String> axesNames) {
		this.axesNames = axesNames;
	}

	private Object userObject;

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * True if visible
	 * @return
	 */
	public boolean isVisible() {
		return isActive();
	}

	/**
	 * True if visible
	 * @return
	 */
	public void setVisible(boolean isVisible) {
		// TODO FIXME What to do to make plots visible/invisible?
	}

	private boolean isUserTrace=true;

	public boolean isUserTrace() {
		return isUserTrace;
	}

	public void setUserTrace(boolean isUserTrace) {
		this.isUserTrace = isUserTrace;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(AbstractPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
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

	@Override
	public ROIBase getWindow() {
		return window;
	}

	@Override
	public void setWindow(ROIBase window) {
		this.window = window;
		if (plotter!=null && this.isActive()) plotter.setWindow(window);
	}
	
}
