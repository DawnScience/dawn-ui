package org.dawnsci.plotting.jreality;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.dawb.common.services.IPaletteService;
import org.dawnsci.plotting.api.histogram.HistogramBound;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.dawnsci.plotting.api.trace.IPaletteListener;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.api.trace.PaletteEvent;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
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

	private static Logger logger = LoggerFactory.getLogger(SurfaceTrace.class);

	private AbstractDataset        data;
	private boolean rescaleHistogram = true;
	private ImageServiceBean imageServiceBean;
	private IImageService service;
	private boolean          imageCreationAllowed = true;

	public SurfaceTrace(JRealityPlotViewer plotter, String name) {
		super(plotter, name);
		this.imageServiceBean = new ImageServiceBean();
		try {
			final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
			final String scheme = getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME);
			imageServiceBean.setPalette(pservice.getPaletteData(scheme));	
		} catch (Exception e) {
			logger.error("Cannot create palette!", e);
		}	
		imageServiceBean.setOrigin(ImageOrigin.forLabel(getPreferenceStore().getString(BasePlottingConstants.ORIGIN_PREF)));
		imageServiceBean.setHistogramType(HistoType.forLabel(getPreferenceStore().getString(BasePlottingConstants.HISTO_PREF)));
		imageServiceBean.setMinimumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MIN_CUT)));
		imageServiceBean.setMaximumCutBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.MAX_CUT)));
		imageServiceBean.setNanBound(HistogramBound.fromString(getPreferenceStore().getString(BasePlottingConstants.NAN_CUT)));
		imageServiceBean.setLo(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_LO));
		imageServiceBean.setHi(getPreferenceStore().getDouble(BasePlottingConstants.HISTO_HI));		

		this.service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);

	}

	@Override
	public PaletteData getPaletteData() {
		if (imageServiceBean==null) return null;
		return imageServiceBean.getPalette();
	}

	private IPreferenceStore store;

	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		return store;
	}

	/**
	 * This function updates the color mapping with a ColorMappingUpdate object
	 * @param update
	 */
	@Override
	public void setPaletteData(PaletteData palette){
		if (isActive()) {
			if (imageServiceBean==null) return;
			imageServiceBean.setPalette(palette);
			ColourImageData imageData = createImageData();
			plotter.handleColourCast(imageData, getMin().doubleValue(), getMax().doubleValue());
			firePaletteDataListeners(palette);
		}
	}

	protected ColourImageData createImageData() {
		ColourImageData imageData = new ColourImageData(256,1);
		int lastValue=0;
		for (int i = 0; i < imageData.getWidth(); i++){
			int value =  ((255&0xff) << 24)+((getPaletteData().colors[i].red&0xff) << 16)+((getPaletteData().colors[i].green&0xff) << 8)+(getPaletteData().colors[i].blue&0xff);
			if(i==252)
				lastValue = value;
			else if(i==253||i==254||i==255)
				imageData.set(lastValue, i);
			else if(i>=0&&i<252)
				imageData.set(value, i);
		}
		return imageData;
	}

	@Override
	public Number getMin() {
		return imageServiceBean.getMin();
	}

	@Override
	public Number getMax() {
		return imageServiceBean.getMax();
	}

	@Override
	public HistogramBound getNanBound() {
		return imageServiceBean.getNanBound();
	}

	@Override
	public void setNanBound(HistogramBound bound) {
		storeBound(bound, BasePlottingConstants.NAN_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setNanBound(bound);
		fireNanBoundsListeners();
	}

	@SuppressWarnings("unchecked")
	public void setData(final IDataset data, List<? extends IDataset> axes) {

		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
		imageServiceBean.setImage(data);
		
		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
		if (rescaleHistogram) {
			final float[] fa = service.getFastStatistics(imageServiceBean);
			setMin(fa[0]);
			setMax(fa[1]);
		}

		if (axes!=null && axes.size()==2) {
			axes = Arrays.asList(axes.get(0), axes.get(1), null);
		}
		
		this.data = (AbstractDataset)data;
		this.axes = (List<IDataset>) axes;
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

	@Override
	public ImageServiceBean getImageServiceBean() {
		return imageServiceBean;
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
	public void setWindow(IROI window) {
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
		if (plotter!=null && this.isActive()) plotter.setSurfaceWindow(this.window);
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

	public void dispose() {
		try {
			plotter.removeSurfaceTrace(this);
			super.dispose();
		} catch (Throwable ignored) {
			// It's disposed anyway
		}
	}

	@Override
	public HistogramBound getMinCut() {
		return imageServiceBean.getMinimumCutBound();
	}

	@Override
	public HistogramBound getMaxCut() {
		return imageServiceBean.getMaximumCutBound();
	}

	@Override
	public void setMinCut(HistogramBound bound) {
		storeBound(bound, BasePlottingConstants.MIN_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setMinimumCutBound(bound);
		fireMinCutListeners();
	}

	@Override
	public void setMaxCut(HistogramBound bound) {
		storeBound(bound, BasePlottingConstants.MAX_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setMaximumCutBound(bound);
		fireMaxCutListeners();
	}

	private void storeBound(HistogramBound bound, String prop) {
		if (bound!=null) {
			getPreferenceStore().setValue(prop, bound.toString());
		} else {
			getPreferenceStore().setValue(prop, "");
		}
	}

	@Override
	public void setMin(Number min) {
		if (imageServiceBean==null) return;
		imageServiceBean.setMin(min);
		fireMinDataListeners();
	}

	@Override
	public void setMax(Number max) {
		if (imageServiceBean==null) return;
		imageServiceBean.setMax(max);
		fireMaxDataListeners();
	}

	@Override
	public void setRescaleHistogram(boolean rescaleHistogram) {
		this.rescaleHistogram = rescaleHistogram;
	}

	@Override
	public boolean isRescaleHistogram() {
		return rescaleHistogram;
	}

	private void fireMaxCutListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.maxCutChanged(evt);
	}

	private void fireMinCutListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.minCutChanged(evt);
	}

	private void fireMaxDataListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.maxChanged(evt);
	}

	private void fireMinDataListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.minChanged(evt);
	}

	private void fireNanBoundsListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.nanBoundsChanged(evt);
	}
}
