/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.visualization.widgets.figureparts.ColorMapRamp;
import org.eclipse.nebula.visualization.xygraph.figures.Annotation;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.DAxis;
import org.eclipse.nebula.visualization.xygraph.figures.IAnnotationLabelProvider;
import org.eclipse.nebula.visualization.xygraph.figures.IAxesFactory;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Legend;
import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.linearscale.AbstractScale.LabelSide;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This is an XYGraph which supports regions of interest.
 * @author Matthew Gerring
 *
 */
public class XYRegionGraph extends XYGraph {

	private static final class XYRegionGraphAxesFactory implements IAxesFactory {
		@Override
		public Axis createXAxis() {
			AspectAxis newAxis = new AspectAxis(X_AXIS, false);
			newAxis.setTickLabelSide(LabelSide.Primary);
			return newAxis;
		}

		@Override
		public Axis createYAxis() {
			Axis newAxis = new AspectAxis(Y_AXIS, true);
			newAxis.setTickLabelSide(LabelSide.Primary);
			newAxis.setAutoScaleThreshold(0.1);
			return newAxis;
		}
	}

	public XYRegionGraph() {
		super(new XYRegionGraphAxesFactory());

		try {
			setShowLegend(getPreferenceStore().getBoolean(BasePlottingConstants.XY_SHOWLEGEND));
		} catch (NullPointerException ne) {
			setShowLegend(true);
		}
	}
	
	private IPreferenceStore store;
	private boolean zoomed = false;
	private boolean autoscaled;

	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
		return store;
	}

	@Override
	protected PlotArea createPlotArea(IXYGraph xyGraph) {
		return new RegionArea(this);
	}

	public void addRegion(final IRegion region) {
		getRegionArea().addRegion(region);
	}
	public void removeRegion(final IRegion region) {
		getRegionArea().removeRegion(region);
	}
	public void renameRegion(final IRegion region, String name) {
		if (region!=null && region.getName()!=null && region.getName().equals(name)) return; // nothing to change.
		getRegionArea().renameRegion(region, name);
	}
	public void setSelectionProvider(final ISelectionProvider provider) {
		getRegionArea().setSelectionProvider(provider);
	}

	/**
	 * Create region of interest
	 * @param name
	 * @param xAxis
	 * @param yAxis
	 * @param regionType
	 * @param startingWithMouseEvent
	 * @return region
	 * @throws Exception
	 */
	public AbstractSelectionRegion<?> createRegion(String name, IAxis xAxis, IAxis yAxis, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
		return getRegionArea().createRegion(name, xAxis, yAxis, regionType, startingWithMouseEvent);
	}
	public void disposeRegion(final AbstractSelectionRegion<?> region) {
		getRegionArea().disposeRegion(region);
	}


	public ImageTrace createImageTrace(String name, ColorMapRamp intensity) {
		RegionArea ra = (RegionArea)getPlotArea();
		return ra.createImageTrace(name, intensity);
	}

	public ImageStackTrace createImageStackTrace(String name, ColorMapRamp intensity) {
		RegionArea ra = (RegionArea)getPlotArea();
		return ra.createImageStackTrace(name, intensity);
	}
	
	public void addImageTrace(final ImageTrace trace) {
		getRegionArea().addImageTrace(trace);
	}
	public void removeImageTrace(final ImageTrace trace) {
		getRegionArea().removeImageTrace(trace);
	}
	public void addVectorTrace(final VectorTrace trace) {
		getRegionArea().addVectorTrace(trace);
	}
	public void removeVectorTrace(final VectorTrace trace) {
		getRegionArea().removeVectorTrace(trace);
	}

	public boolean addRegionListener(IRegionListener l) {
		return getRegionArea().addRegionListener(l);
	}
	
	public boolean removeRegionListener(IRegionListener l) {
		return getRegionArea().removeRegionListener(l);
	}

	public IRegion getRegion(String name) {
		return getRegionArea().getRegion(name);
	}

	public void clearRegions(boolean force) {
		getRegionArea().clearRegions(force);
	}
	public List<IRegion> getRegions() {
		return getRegionArea().getRegions();
	}

	public void clearRegionTool() {
		getRegionArea().clearRegionTool();
	}

	public void clearImageTraces() {
		getRegionArea().clearImageTraces();
	}
	
	public RegionArea getRegionArea() {
		return (RegionArea)getPlotArea();
	}

	public void performAutoScale() {
		zoomed = true;
		autoscaled = true;
		Map<String, ImageTrace> traces = getRegionArea().getImageTraces();
		if (traces != null && !traces.isEmpty()) {
			
			for (ImageTrace trace : traces.values()) {
				trace.performAutoscale();
			}
			
		} else {
			
			super.performAutoScale();
		}
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		String oldTitle = getTitle();
		if (oldTitle!=null && oldTitle.equals(title)) return;
		super.setTitle(title);
	}
	
	public void setTitleColor(Color titleColor) {
    
		Color old = getTitleColor();
		if (old!=null && old.equals(titleColor)) return;
		super.setTitleColor(titleColor);
	}

	/**
	 * Call from UI thread only!
	 */
	public void clearTraces() {
		
		if (super.getLegendMap()!=null) {
			final Collection<Legend> legends = super.getLegendMap().values();
			for (Legend legend : legends) {
				legend.getTraceList().clear();
			}
			//super.getLegendMap().clear();
		}
		
		getRegionArea().clearTraces();
		for(Axis axis : getXAxisList()){
			if (axis instanceof DAxis)((DAxis)axis).clear();
		}
		for(Axis axis : getYAxisList()){
			if (axis instanceof DAxis)((DAxis)axis).clear();
		}

		revalidate();
	}

	public void setPaletteData(PaletteData data) {
		getRegionArea().setPaletteData(data);
	}

	public void setImageOrigin(ImageOrigin origin) {
		getRegionArea().setImageOrigin(origin);
	}

	public void setImageTranspose(boolean transpose) {
		getRegionArea().setImageTranspose(transpose);
	}

	@Override
	public void layout() {
		super.layout();

		for (Axis axis : getXAxisList()) {
			if (axis instanceof AspectAxis) {
				((AspectAxis) axis).checkBounds(zoomed);
			}
		}
	
		for (Axis axis : getYAxisList()) {
			if (axis instanceof AspectAxis) {
				((AspectAxis) axis).checkBounds(zoomed);
			}
		}
		zoomed = false;

		PlotArea pa = getPlotArea();
		if (pa != null && pa.isVisible()) {
			Axis x = getPrimaryXAxis();
			Axis y = getPrimaryYAxis();
			Rectangle plotAreaBound = new Rectangle(x.getBounds().x + x.getMargin(), y.getBounds().y + y.getMargin(),
					x.getTickLength(), y.getTickLength());
			pa.setBounds(plotAreaBound);
		}
	}

	public void setKeepAspect(boolean checked) {
		for (Axis axis : getXAxisList()) {
			if (axis instanceof AspectAxis) ((AspectAxis) axis).setKeepAspect(checked);
		}
		for (Axis axis : getYAxisList()) {
			if (axis instanceof AspectAxis) ((AspectAxis) axis).setKeepAspect(checked);
		}
	}

	/**
	 * Zooms about the central point a factor (-ve for out) usually +-0.1
	 */
	public void setZoomLevel(MouseEvent evt, double delta, boolean tryToUseWhitespace) {
		zoomed = true;
		for (Axis axis : getXAxisList()) {
			final double cenX = axis.getPositionValue(evt.x, false);
			axis.zoomInOut(cenX, delta);
		}
		for (Axis axis : getYAxisList()) {
			final double cenY = axis.getPositionValue(evt.y, false);
			axis.zoomInOut(cenY, delta);
		}

		RegionArea area = getRegionArea();
		ImageTrace image = area.getImageTrace();
		if (tryToUseWhitespace && image != null) {
			double[] gr = delta < 0 ? null : image.getGlobalRange();
			expandAxis((AspectAxis) getPrimaryXAxis(), gr == null ? null : Arrays.copyOfRange(gr, 0, 2));
			expandAxis((AspectAxis) getPrimaryYAxis(), gr == null ? null : Arrays.copyOfRange(gr, 2, 4));
		}
		if (delta < 0) { // don't use global range to expand after zooming in again
			autoscaled = false;
		}

		area.createPositionCursor(evt.x, evt.y);
	}

	private void expandAxis(AspectAxis axis, double[] limit) {
		int p = axis.getTickLength();
		int q = axis.getPrecheckTickLength();
		if (q > p) { // when there is room to expand
			Range r = axis.getRange();
			double l = r.getLower();
			double u = r.getUpper();
			double d = u - l;
			if (d > 0) { // shift end to maximize portion of image shown
				u = l + (d * q) / p;
			} else {
				l = u - (d * q) / p;
			}
			if (limit != null && autoscaled) {
				if (d > 0) {
					l = Math.max(l, limit[0]);
					u = Math.min(u, limit[1]);
				} else {
					l = Math.min(l, limit[1]);
					u = Math.max(u, limit[0]);
				}
			}
			axis.setRange(l, u);
		}
	}

	public void dispose() {
		removeAll();
		getRegionArea().dispose();
		clearPropertyListeners();
	}
	
	private final NumberFormat integerFormat   = NumberFormat.getIntegerInstance();
	private final NumberFormat intensityFormat = NumberFormat.getNumberInstance();
	
	public void addAnnotation(final Annotation annotation){
		
		annotation.setLabelProvider(new IAnnotationLabelProvider() {
			@Override
			public String getInfoText(double xValue, double yValue, boolean showName, boolean showSample, boolean showPosition) {
				if (getRegionArea().getImageTrace()==null) return null;
				if (annotation.getTrace()!=null) return null;
				
				final ImageTrace im = getRegionArea().getImageTrace();
				final StringBuilder buf = new StringBuilder();
				if (showName) buf.append(annotation.getName());
				if (showSample) try {
					if (annotation.getXAxis().getRange().inRange(xValue) &&
						annotation.getYAxis().getRange().inRange(yValue)) {
						
						final float value = im.getData().getFloat((int)yValue, (int)xValue);
						buf.append("\nIntensity ");
						buf.append(intensityFormat.format(value));
					}
				} catch (Exception ignored) {
					// We carry on if the pixel locations are invalid.
				}
				if (showPosition) {
					buf.append("\nLocation ");
					buf.append("(");
					buf.append(integerFormat.format(xValue));
					buf.append(" : ");
					buf.append(integerFormat.format(yValue));
					buf.append(")");
				}
				return buf.toString();
			}
		});
		super.addAnnotation(annotation);
	}

	private Collection<IPropertyChangeListener> propertyListeners;
	
	/**
	 * NOTE This listener is *not* notified once for each configuration setting made on 
	 * the configuration but once whenever the form is applied by the user (and many things
	 * are changed) 
	 * 
	 * You then have to read the property you require from the object (for instance the axis
	 * format) in case it has changed. This is not ideal, later there may be more events fired and
	 * it will be possible to check property name, for now it is always set to "Graph Configuration".
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (propertyListeners==null) propertyListeners = new HashSet<IPropertyChangeListener>(3);
		propertyListeners.add(listener);
	}
	
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (propertyListeners==null) return;
		propertyListeners.remove(listener);
	}
	
	protected void clearPropertyListeners() {
		if (propertyListeners!=null) propertyListeners.clear();
	}
	
	public void fireConfigurationPropertyChangeListeners() {
		if (propertyListeners==null) return;
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, "Graph Configuration", "Various", "Various (some new)");
		for (IPropertyChangeListener l : propertyListeners) {
			l.propertyChange(evt);
		}
	}

	public void setShowAxes(final boolean checked) {
		this.getPrimaryXAxis().setVisible(checked);
		this.getPrimaryYAxis().setVisible(checked);
	}

	/**
	 * 
	 * @param trace
	 * @param toFront - if true, move regions to front
	 */
	public void addTrace(Trace trace, Axis xAxis, Axis yAxis, boolean toFront) {
		if (trace.getTraceColor() == null) { // Cycle through default colors
			trace.setTraceColor(XYGraphMediaFactory.getInstance()
					.getColor(DEFAULT_TRACES_COLOR[plotArea.getTraceList().size() % DEFAULT_TRACES_COLOR.length]));
		}
		if (legendMap.containsKey(trace.getYAxis()))
			legendMap.get(trace.getYAxis()).addTrace(trace);
		else {
			legendMap.put(trace.getYAxis(), new Legend((IXYGraph) this));
			legendMap.get(trace.getYAxis()).addTrace(trace);
			add(legendMap.get(trace.getYAxis()));
		}

		if (xAxis == null || yAxis == null) {
			try {
				for (Axis axis : getAxisList()) {
					axis.addTrace(trace);
				}
			} catch (Throwable ne) {
				// Ignored, this is a bug fix for Dawn 1.0
				// to make the plots rescale after a plot is deleted.
			}
		} else {
			xAxis.addTrace(trace);
			yAxis.addTrace(trace);
		}

		plotArea.addTrace(trace);
		trace.setXYGraph((IXYGraph) this);
		trace.dataChanged(null);
		revalidate();
		repaint();

		getRegionArea().toFront();
	}

	
	private IAxis selectedXAxis;
	private IAxis selectedYAxis;

	public IAxis getSelectedXAxis() {
		if (selectedXAxis==null) {
			return (IAxis) getPrimaryXAxis();
		}
		return selectedXAxis;
	}

	public void setSelectedXAxis(IAxis selectedXAxis) {
		this.selectedXAxis = selectedXAxis;
	}

	public IAxis getSelectedYAxis() {
		if (selectedYAxis==null) {
			return (IAxis) getPrimaryYAxis();
		}
		return selectedYAxis;
	}

	public void setSelectedYAxis(IAxis selectedYAxis) {
		this.selectedYAxis = selectedYAxis;
	}

	@Override
	public boolean removeAxis(Axis axis) {
		// need to be done to stop inconsistency(!)
		if (axis == selectedXAxis) {
			selectedXAxis = null;
		} else if (axis == selectedYAxis) {
			selectedYAxis = null;
		}
		return super.removeAxis(axis);
	}

	private boolean isGridSnap;

	/**
	 * 
	 * @return true if regions/selections are snapped to grid
	 */
	public boolean isGridSnap() {
		return isGridSnap;
	}

	/**
	 * Sets the snap to grid option on regions/selections
	 * 
	 * @param isGridSnap
	 */
	public void setGridSnap(boolean isGridSnap) {
		this.isGridSnap = isGridSnap;
	}
}
