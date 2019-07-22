/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.visualization.xygraph.figures.DAxis;
import org.eclipse.nebula.visualization.xygraph.linearscale.ITicksProvider;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * An axis which can keep aspect with another and have a maximum possible extend which cannot
 * be altered.
 * 
 * @author Matthew Gerring
 *
 */
public class AspectAxis extends DAxis implements IAxis {

	private AspectAxis relativeTo;
	private Range      maximumRange;
	private boolean    keepAspect; // This is so that the user may have images with and without aspect in the same application.
	private Dataset labelData;
	
	public AspectAxis(String title, boolean yAxis) {
		super(title, yAxis);
		keepAspect = getPreferenceStore().getBoolean(BasePlottingConstants.ASPECT);
	}
	
	public void setTitle(final String title) {
		
		final String oldName = getTitle();
        if (oldName!=null && oldName.equals(title)) return;
        
        super.setTitle(title);
	}
	
	@Override
	public void setForegroundColor(final Color color) {
		
		final Color old = super.getForegroundColor();
        super.setForegroundColor(color);
        
        if (old!=null && old.equals(color)) return;
        
	}

	
	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
		return store;
	}

	public void setKeepAspectWith(final AspectAxis axis) {
		this.relativeTo = axis;
	}

	private int precheckTickLength;

	/**
	 * @return tick length before axis has been resized after checkBounds
	 */
	public int getPrecheckTickLength() {
		return precheckTickLength;
	}

	public String getDirection() {
		return isHorizontal() ? "x" : "y";
	}

	private boolean skipCheckBounds = false;

	public void checkBounds(final boolean ignore) {
		checkBounds(ignore, false);
	}

	private double centre = Double.NaN;

	protected void checkBounds(final boolean ignore, final boolean force) {
		if (skipCheckBounds) {
			skipCheckBounds = false;
			return;
		}

		if (relativeTo == null) return;
		if (!keepAspect)        return;
		
		// We keep aspect if the other axis has a larger range than this axis.
		final double  thisRange     = getInterval(getRange());
		final double  relRange      = getInterval(relativeTo.getRange());
		final Rectangle calcBounds  = getBounds().getCopy();

		boolean check = false;
		if (!force) { // check if this axis's resolution (/pixel) is less than the other's
			check = relRange * getTickLength() > thisRange * relativeTo.getTickLength();
		}

		precheckTickLength = isHorizontal() ? calcBounds.width : calcBounds.height;
		precheckTickLength -= 4*getMargin();
		if (check || force) {
			boolean otherAxisInvalid = setRelativeAxisBounds(calcBounds, thisRange, relRange);
			if (force) { // change range instead
				double fLength = precheckTickLength + 2 * getMargin();
				double d = 1 - (isHorizontal() ? calcBounds.width : calcBounds.height) / fLength;
				if (d > 0) { // what if need to shrink???
					Range r = getRange();
					if (!Double.isFinite(centre)) {
						centre = (r.getLower() + r.getUpper()) * 0.5;
					}
					relativeTo.skipCheckBounds = true;
					zoomInOut(centre, d);
				}
			} else {
				setBounds(calcBounds);
			}
			if (!ignore && otherAxisInvalid && !force) { // force is not recursive
				relativeTo.checkBounds(false, true);
			}
		}

		// y correction for companion axis
		if (!isHorizontal() && isPrimary()) { 
			
			// We have to ensure that our own ticks have been laid out
			// because we use their size to set the location of the
			// relative to field.
			super.layoutTicks();
			
			// Make relativeTo appear near to bottom y axis
			IFigure yTicks = (IFigure)getChildren().get(1);
			Dimension yAxisSize = yTicks.getSize();
			final Rectangle relBounds = relativeTo.getBounds().getCopy();
			relBounds.y = getBounds().y + yAxisSize.height - getMargin();
			relativeTo.setBounds(relBounds);
		}
	}

	private boolean setRelativeAxisBounds(final Rectangle origBounds, final double thisRange, final double relRange) {
		int relPixels = relativeTo.getTickLength();
	
		int thisPixels = (int) Math.round(thisRange * relPixels / relRange);
		thisPixels += 2 * getMargin(); // adjust for whole axis length
	
		Rectangle bnds = getGraph().getPlotArea().getBounds();
		boolean otherAxisInvalid;
		if (isHorizontal()) {
			otherAxisInvalid = thisPixels > bnds.width;
			origBounds.width = otherAxisInvalid ? bnds.width : thisPixels;
		} else {
			otherAxisInvalid = thisPixels > bnds.height;
			origBounds.height = otherAxisInvalid ? bnds.height : thisPixels;
		}
		return otherAxisInvalid;
	}

	@Override
	public void zoomInOut(final double center, final double factor) {
		this.centre = center;
		// If we are image and it is fully zoomed, do not allow zoom in.
		final XYRegionGraph xyGraph = getGraph();
		final ImageTrace trace = xyGraph.getRegionArea().getImageTrace();
		if (trace!=null && trace.isMaximumZoom() && factor>0) return; // We cannot zoom in more.
	
		super.zoomInOut(center, factor);
	}

	/**
	 * Should be a method on Range really but 
	 * @param range
	 * @return
	 */
	private static double getInterval(Range range) {
		return Math.abs(range.getUpper() - range.getLower());
	}

	private static boolean isTopOrRightOn(Range r, int[] limits) {
		return testLimit(r.isMinBigger(), r.getUpper(), limits);
	}

	private static boolean isBottomOrLeftOn(Range r, int[] limits) {
		return testLimit(!r.isMinBigger(), r.getLower(), limits);
	}

	private static boolean testLimit(boolean first, double v, int[] limits) {
		return first ? v > limits[0] : v < limits[1];
	}

	@Override
	protected void paintClientArea(final Graphics graphics) {
		if (!isVisible())
			return;

		super.paintClientArea(graphics);

		/** HACK WARNING **/
		// If data is greater than zoom, extends give visual clue to Claire P.
		// Hack for http://jira.diamond.ac.uk/browse/DAWNSCI-552

		final XYRegionGraph graph = getGraph();
		final IImageTrace trace = graph.getRegionArea().getImageTrace();
		if (trace == null) {
			return;
		}
		final Range range = getRange();
		final int[] shape = trace.getData().getShape();
		ImageOrigin origin = trace.getImageOrigin();
		int[] limits = new int[2];
		limits[1] = shape[origin.isOnLeadingDiagonal() ^ isYAxis() ? 1 : 0];

		Rectangle bnds = graph.getRegionArea().getBounds();

		graphics.pushState();
		graphics.setBackgroundColor(ColorConstants.red);
		graphics.setAlpha(100);
		if (isYAxis()) {
			Point c = bnds.getTopLeft();
			final int x = c.x - 1;
			int y = c.y;
			if (isTopOrRightOn(range, limits)) { // Zoom at top not to edge
				final PointList triangle = new PointList();
				triangle.addPoint(x, y);
				triangle.addPoint(x + 5, y + 6);
				triangle.addPoint(x - 5, y + 6);
				graphics.fillPolygon(triangle);
			}

			if (isBottomOrLeftOn(range, limits)) { // at bottom
				final PointList triangle = new PointList();
				y += bnds.height;
				triangle.addPoint(x, y);
				triangle.addPoint(x + 5, y - 6);
				triangle.addPoint(x - 5, y - 6);
				graphics.fillPolygon(triangle);
			}
		} else {
			Point c = bnds.getBottomLeft();
			int x = c.x;
			final int y = c.y + 1;

			if (isBottomOrLeftOn(range, limits)) { // Zoom at left not to edge
				final PointList triangle = new PointList();
				triangle.addPoint(x, y);
				triangle.addPoint(x + 6, y + 5);
				triangle.addPoint(x + 6, y - 5);
				graphics.fillPolygon(triangle);
			}

			if (isTopOrRightOn(range, limits)) { // at right
				x += bnds.width;
				final PointList triangle = new PointList();
				triangle.addPoint(x, y);
				triangle.addPoint(x - 6, y + 5);
				triangle.addPoint(x - 6, y - 5);
				graphics.fillPolygon(triangle);
			}
		}

		graphics.popState();
		/** HACK OVER **/
	}

	private XYRegionGraph getGraph() {
		return (XYRegionGraph)getParent();
	}

	protected boolean panChecked(Range temp, double t1, double t2) {
		final ImageTrace trace = getGraph().getRegionArea().getImageTrace();

		// Code to stop pan outside image bounds.
		if (trace != null && !trace.hasTrueAxes()) {
			skipCheckBounds = false;
			final double d1 = t1 - t2;
			final double d2 = t2 - t1;

			final Range cur = getRange();
			final double ran = Math.max(cur.getUpper(), cur.getLower()) - Math.min(cur.getUpper(), cur.getLower());

			boolean isAxisFlipped = isYAxis() ? trace.getImageOrigin().isOnTop() : !trace.getImageOrigin().isOnLeft();
			int yIndex = trace.getImageOrigin().isOnLeadingDiagonal() ? 0 : 1;
			int index = isYAxis() ? yIndex : 1 - yIndex;
			double lower, upper;
			if (isAxisFlipped) {
				lower = temp.getUpper();
				upper = temp.getLower();
			} else {
				lower = temp.getLower();
				upper = temp.getUpper();
			}

			if ((lower-d2)<=0) {
				if (isAxisFlipped) {
					setRange(ran, 0);
				} else {
					setRange(0, ran);
				}
				return false;

			} else if ((upper+d1)>trace.getData().getShape()[index]) {
				if (isAxisFlipped) {
					setRange(trace.getData().getShape()[index], trace.getData().getShape()[index]-ran);
				} else {
					setRange(trace.getData().getShape()[index]-ran, trace.getData().getShape()[index]);
				}
				return false;
			}

		}

		skipCheckBounds = true;
		return super.panChecked(temp, t1, t2);
	}

	public boolean isKeepAspect() {
		return keepAspect;
	}

	public void setKeepAspect(boolean keepAspect) {
		this.keepAspect = keepAspect;
	}

	public Range getMaximumRange() {
		return maximumRange;
	}

	/**
	 * 
	 */
	@Override
	public void setMaximumRange(double lower, double upper) {
		setMaximumRange(new Range(lower, upper));
	}

	/**
	 * Set with lower<upper, the class will check for if the axis is in reversed mode.
	 * @param maximumRange
	 */
	public void setMaximumRange(Range maximumRange) {
		if (maximumRange==null) {
			this.maximumRange = null;
			return;
		}
		if (maximumRange.isMinBigger()) throw new RuntimeException("Maximum range must have lower less than upper. AspectAxis allows for reversed real axes in internally!");
		this.maximumRange = maximumRange;
	}

	@Override
	public void setRange(double lower, double upper) {
		final Range norm = normalize(new Range(lower, upper));
		double l = norm.getLower();
		double u = norm.getUpper();
		centre = (l + u)*0.5;
		super.setRange(l, u);
		setInverted(norm.isMinBigger());
	}

	/**
	 * @param range
	 * @return clipped range if it exceeds maximum range
	 */
	private Range normalize(Range r) {
		if (maximumRange == null) return r;

		double mu = maximumRange.getUpper();
		double ml = maximumRange.getLower();
		double u = r.getUpper();
		double l = r.getLower();
		boolean changed = false;
		boolean swap = u < l;
		if (swap) {
			double t = l;
			l = u;
			u = t;
		}
		double d = u - mu;
		if (d > 0) { // shift lower limit if upper exceeds max
			changed = true;
			l -= d;
			l = Math.max(ml, l); // ensure lower limit is okay
			u = mu;
		}
		d = ml - l;
		if (d > 0) { // do the same if lower subceeds min
			changed = true;
			u += d;
			u = Math.min(mu,  u);
			l = ml;
		}

		if (changed) {
			if (swap) {
				double t = l;
				l = u;
				u = t;
			}

			return new Range(l, u);
		}
		return r;
	}

	private static String getTitleFromLabelData(IDataset label) {
		if (label == null) {
			return null;
		}
		String title = label.getName();
		if (title == null || title.length() == 0)
			return null;
		return title;
	}

	@Override
	public void setLabelDataAndTitle(IDataset labels) {
		if (labels == null) {
			if (labelData == null) {
				return;
			}
		} else if (labels.getRank() != 1) {
			return;
		}

		String text = getTitleFromLabelData(labels);
		if (text != null) {
			setTitle(text);
		}

		if (labels != null && labels.equals(labelData)) {
			return;
		}
		setDirty(true);
		labelData = DatasetUtils.convertToDataset(labels);
	}

	/**
	 * Override to provide custom axis labels.
	 */
	@Override
	public double getLabel(double value) {
		// The value of the tick should be the pixel 
		if (labelData!=null) {
			int index = (int) Math.round(value);

			if (index >= labelData.getSize())
				return Double.NaN; // this is ignored when used in TickFactory#getTickString()

			return labelData.getDouble(index);
		}
		return super.getLabel(value);
	}

	@Override
	public boolean isLog10() {
		return super.isLogScaleEnabled();
	}

	@Override
	public void setLog10(boolean isLog10) {
		super.setLogScale(isLog10);
	}

	@Override
	public double getUpper() {
		return getRange().getUpper();
	}

	@Override
	public double getLower() {
		return getRange().getLower();
	}

	@Override
	public int getValuePosition(double value) {
		return getValuePosition(value, false);
	}

	@Override
	public double getPositionValue(int position) {
		return getPositionValue(position, false);
	}

	@Override
	public double getValueFromPosition(double position) {
		return getPositionValue(position, false);
	}

	@Override
	public double getPositionFromValue(double value) {
		return getValuePrecisePosition(value, false);
	}

	protected Collection<IAxisListener> axisListeners;

	@Override
	protected void fireRevalidated(){
		super.fireRevalidated();
		
		final AxisEvent evt = new AxisEvent(this);
		if (axisListeners!=null) for(IAxisListener listener : axisListeners)
			listener.revalidated(evt);
	}

	@Override
	protected void fireAxisRangeChanged(final Range old_range, final Range new_range){
		super.fireAxisRangeChanged(old_range, new_range);
		
		final AxisEvent evt = new AxisEvent(this, old_range.getLower(), old_range.getUpper(),
				                                  new_range.getLower(), new_range.getUpper());
		if (axisListeners!=null)  for(IAxisListener listener : axisListeners)
			listener.rangeChanged(evt);
	}

	@Override
	public void addAxisListener(IAxisListener listener) {
		if (axisListeners==null) axisListeners = new ArrayList<IAxisListener>(3);
		axisListeners.add(listener);
	}

	@Override
	public void removeAxisListener(IAxisListener listener) {
		if (axisListeners==null) return;
		axisListeners.remove(listener);
	}

	@Override
	public void setDateFormatEnabled(boolean dateEnabled) {
		super.setDateEnabled(dateEnabled);
	}

	@Override
	public boolean isDateFormatEnabled() {
		return super.isDateEnabled();
	}

	@Override
	public String toString() {
		return "(" + getTitle() + ", " + getOrientation() + ", " + getRange() + ", inverted=" + isInverted() + ")";
	}
	
	@Override
	public boolean isPrimaryAxis() {
		final XYRegionGraph xyGraph = (XYRegionGraph)getGraph();
		return xyGraph.getPrimaryXAxis()==this || xyGraph.getPrimaryYAxis()==this;
	}

	@Override
	public boolean isLabelCustomised() {
		return labelData != null;
	}
	
	// Fix to http://jira.diamond.ac.uk/browse/SCI-1444
	@Override
    protected String getAutoFormat(double min, double max) {
    	ITicksProvider ticks = getTicksProvider();
    	if (labelData!=null && ticks!=null) {
    		try {
    			if (min>=labelData.getSize()) min-=1;
    			if (max>=labelData.getSize()) max-=1;
    			min = labelData.getDouble((int)Math.round(min));
    			max = labelData.getDouble((int)Math.round(max));
    			
    			return ticks.getDefaultFormatPattern(min, max);

    		} catch (Exception ignored) {
    			// We just let the super implementation do the work instead.
    		}
    	}
    	return super.getAutoFormat(min, max);
    }

}
