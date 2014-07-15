package org.dawnsci.plotting.jreality;

import java.util.Collection;
import java.util.HashSet;

import org.dawnsci.plotting.jreality.data.ColourImageData;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.IImage3DTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for holding surface trace and multi 2d trace data.
 * 
 * We may need to abstract some parts to a general 3D trace as more options are supported.
 * 
 *
 */
abstract public class Image3DTrace extends PlotterTrace implements IImage3DTrace{

	private static Logger logger = LoggerFactory.getLogger(Image3DTrace.class);

	private static final int WIDTH = 256;

	protected boolean rescaleHistogram = true;
	protected ImageServiceBean imageServiceBean;
	protected IImageService service;
	private boolean          imageCreationAllowed = true;
	private String paletteName;
	protected PlottingMode plotType;

	public Image3DTrace(JRealityPlotViewer plotter, String name) {
		super(plotter, name);
		this.imageServiceBean = new ImageServiceBean();
		try {
			final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
			final String scheme = getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME);
			imageServiceBean.setPalette(pservice.getDirectPaletteData(scheme));	
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

	@Override
	public String getPaletteName() {
		return paletteName;
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

	@Override
	public void setPaletteName(String paletteName){
		this.paletteName = paletteName;
	}

	protected ColourImageData createImageData() {

		PaletteData     pd        = getPaletteData();
		ColourImageData imageData = new ColourImageData(WIDTH,1);
		int lastValue=0;
		for (int i = 0; i < WIDTH; i++){
			int value =  ((255&0xff) << 24) +
					     ((pd.colors[i].red&0xff)   << 16)+
					     ((pd.colors[i].green&0xff) << 8)+
					     (pd.colors[i].blue&0xff);
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

	@Override
	public boolean is3DTrace() {
		return true;
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
	@Override
	public int getRank() {
		return 2;
	}
}
