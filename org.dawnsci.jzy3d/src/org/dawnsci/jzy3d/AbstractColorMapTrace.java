package org.dawnsci.jzy3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.AbstractColorMap;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractColorMapTrace implements IPaletteTrace {

	protected ImageServiceBean bean;
	private PaletteData paletteData;
	private String paletteName;
	
	private String name;
	private String dataName;
	private List<String> axesNames;
	
	private boolean isVisible = true;
	private boolean isUserTrace = true;
	
	private boolean rescaleHistogram = true;
	
	private Object userObject;
	
	private Set<IPaletteListener> paletteListeners;
	
	protected AbstractColorMapTrace(String palette) {
		setPalette(palette);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}



	@Override
	public List<String> getAxesNames() {
		return axesNames;
	}


	@Override
	public void setRescaleHistogram(boolean rescaleHistogram) {
		this.rescaleHistogram = rescaleHistogram;

	}

	@Override
	public boolean isRescaleHistogram() {
		return rescaleHistogram;
	}

	@Override
	public String getDataName() {
		return dataName;
	}

	@Override
	public void setDataName(String name) {
		this.dataName = name;

	}


	@Override
	public boolean isVisible() {
		return isVisible;
	}

	@Override
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;

	}

	@Override
	public boolean isUserTrace() {
		return isUserTrace;
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		this.isUserTrace = isUserTrace;

	}

	@Override
	public Object getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;

	}

	@Override
	public boolean is3DTrace() {
		return false;
	}

	@Override
	public void dispose() {

	}

	@Override
	public int getRank() {
		return 2;
	}
	
	@Override
	public PaletteData getPaletteData() {
		return paletteData;
	}

	@Override
	public void setPaletteData(PaletteData paletteData) {
		if (paletteData == null) return;
		this.paletteData = paletteData; 
		if (bean == null) return;
		AbstractColorMap map = new AbstractColorMap() {
			
			@Override
			public Color getColor(double arg0, double arg1, double z, double zMin, double zMax) {
				double rel_value = 0;
		        
		        if( z < zMin )
		            rel_value = 0;
		        else if( z > zMax )
		            rel_value = 1;
		        else
		            rel_value = ( z - zMin ) / ( zMax - zMin );
		        

		        
				RGB rgb = paletteData.getRGB((int)(rel_value*255));
				return new Color((float)(rgb.red/255.), (float)(rgb.green/255.), (float)(rgb.blue/255.), 1);
			}
		};
		
		float bmin = bean.getMin().floatValue();
		float bmax = bean.getMax().floatValue();
		
		ColorMapper colorMapper = new ColorMapper(map, bmin, bmax, new Color(1, 1, 1, .5f));
		
		setColorMap(colorMapper);
		
		firePaletteDataListeners(paletteData);
		
	}
	
	protected abstract void setColorMap(ColorMapper mapper);

	@Override
	public String getPaletteName() {
		return paletteName;
	}

	@Override
	public void setPaletteName(String paletteName) {
		this.paletteName = paletteName;
		
	}

	@Override
	public void setPalette(String paletteName) {
		final IPaletteService paletteService = ServiceProvider.getService(IPaletteService.class);
		final PaletteData p = paletteService.getDirectPaletteData(paletteName);
        setPaletteName(paletteName);
        setPaletteData(p);
		
	}

	@Override
	public ImageServiceBean getImageServiceBean() {
		return bean;
	}

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
	public Number getMin() {
		return bean.getMin();
	}

	@Override
	public Number getMax() {
		return bean.getMax();
	}

	@Override
	public HistogramBound getNanBound() {
		return bean.getNanBound();
	}

	@Override
	public HistogramBound getMinCut() {
		return bean.getMinimumCutBound();
	}

	@Override
	public HistogramBound getMaxCut() {
		return bean.getMaximumCutBound();
	}

	@Override
	public void setNanBound(HistogramBound bound) {
		bean.setNanBound(bound);
		
	}

	@Override
	public void setMinCut(HistogramBound bound) {
		bean.setMinimumCutBound(bound);
		
	}

	@Override
	public void setMaxCut(HistogramBound bound) {
		bean.setMinimumCutBound(bound);
		
	}

	@Override
	public void setMin(Number min) {
		if (bean==null) return;
		bean.setMin(min);
		
		
	}

	@Override
	public void setMax(Number max) {
		if (bean==null) return;
		bean.setMax(max);
		
	}

}
