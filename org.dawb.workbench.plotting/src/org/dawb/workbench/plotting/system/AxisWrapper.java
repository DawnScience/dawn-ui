package org.dawb.workbench.plotting.system;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.IAxisListener;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.ui.plot.IAxis;
import org.dawb.common.ui.plot.axis.CoordinateSystemEvent;
import org.dawb.common.ui.plot.axis.ICoordinateSystemListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Class to make a CSS Axis look like the interface used in IPlottingSystem.
 * This is close to a 1:1 mapping as the CSS interface is logical and well designed.
 * 
 * @author fcp94556
 *
 */
class AxisWrapper implements IAxis, IAxisListener{

	private Axis wrappedAxis;

	public AxisWrapper(Axis axis) {
		this.wrappedAxis = axis;
		wrappedAxis.addListener(this);
	}
	
	public void dispose() {
		wrappedAxis.removeListener(this);
	}

	@Override
	public String getTitle() {
		return wrappedAxis.getTitle();
	}

	@Override
	public void setTitle(String title) {
		wrappedAxis.setTitle(title);
	}

	@Override
	public void setTitleFont(Font titleFont) {
		wrappedAxis.setTitleFont(titleFont);
	}

	@Override
	public Font getTitleFont() {
		return wrappedAxis.getTitleFont();
	}

	@Override
	public boolean isLog10() {
		return wrappedAxis.isLogScaleEnabled();
	}

	@Override
	public void setLog10(boolean isLog10) {
		wrappedAxis.setLogScale(isLog10);
	}

	@Override
	public void setForegroundColor(Color color) {
		wrappedAxis.setForegroundColor(color);
	}

	@Override
	public Color getForegroundColor() {
		return wrappedAxis.getForegroundColor();
	}

	@Override
	public void setBackgroundColor(Color color) {
		wrappedAxis.setBackgroundColor(color);
	}

	@Override
	public Color getBackgroundColor() {
		return wrappedAxis.getBackgroundColor();
	}

	@Override
	public boolean isShowMajorGrid() {
		return wrappedAxis.isShowMajorGrid();
	}

	@Override
	public void setShowMajorGrid(boolean showMajorGrid) {
		wrappedAxis.setShowMajorGrid(showMajorGrid);
	}

	@Override
	public boolean isShowMinorGrid() {
		return wrappedAxis.isShowMinorGrid();
	}

	@Override
	public void setShowMinorGrid(boolean showMinorGrid) {
		wrappedAxis.setShowMinorGrid(showMinorGrid);
	}

	@Override
	public Color getMajorGridColor() {
		return wrappedAxis.getMajorGridColor();
	}

	@Override
	public void setMajorGridColor(Color majorGridColor) {
		wrappedAxis.setMajorGridColor(majorGridColor);
	}

	@Override
	public Color getMinorGridColor() {
		return wrappedAxis.getMinorGridColor();
	}

	@Override
	public void setMinorGridColor(Color minorGridColor) {
		wrappedAxis.setMinorGridColor(minorGridColor);
	}

	@Override
	public String getFormatPattern() {
		return wrappedAxis.getFormatPattern();
	}

	@Override
	public void setFormatPattern(String formatPattern) {
		wrappedAxis.setFormatPattern(formatPattern);
	}

	@Override
	public boolean isYAxis() {
		return wrappedAxis.isYAxis();
	}

	@Override
	public void setYAxis(boolean isYAxis) {
		wrappedAxis.setYAxis(isYAxis);
	}

	@Override
	public void setVisible(boolean b) {
		wrappedAxis.setVisible(b);
	}

	@Override
	public boolean isVisible() {
		return wrappedAxis.isVisible();
	}

	/**
	 * We break encapsulation in this package only.
	 * @return
	 */
	final Axis getWrappedAxis() {
		return wrappedAxis;
	}
	
	/**
	 * returns the upper bound in the units of the axis (not pixels)
	 */
	public double getUpper() {
		return wrappedAxis.getRange().getUpper();
	}
	
	/**
	 * returns the lower bound in the units of the axis (not pixels)
	 */
	public double getLower() {
		return wrappedAxis.getRange().getLower();
	}
	
	public void setRange(double start, double end) {
		wrappedAxis.setRange(start, end);
	}
	
	/**
	 * The position in pixels of a given value.
	 * @param value
	 * @return
	 */
	public int getValuePosition(double value) {
		return wrappedAxis.getValuePosition(value, false);
	}
	
	/**
	 * The value for a position in pixels.
	 * @param value
	 * @return
	 */
	public double getPositionValue(int position) {
		return wrappedAxis.getPositionValue(position, false);
	}

	private Collection<ICoordinateSystemListener> coordinateListeners;
	@Override
	public void addCoordinateSystemListener(ICoordinateSystemListener l) {
		if (coordinateListeners==null) coordinateListeners = new HashSet<ICoordinateSystemListener>();
		coordinateListeners.add(l);
	}

	@Override
	public void removeCoordinateSystemListener(ICoordinateSystemListener l) {
		if (coordinateListeners==null) return;
		coordinateListeners.remove(l);
	}

	@Override
	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
		fireCoordinateSystemListeners();
	}

	@Override
	public void axisRevalidated(Axis axis) {
		fireCoordinateSystemListeners();
	}

	private void fireCoordinateSystemListeners() {
		if (coordinateListeners==null) return;
		final CoordinateSystemEvent evt = new CoordinateSystemEvent(this);
		for (ICoordinateSystemListener l : coordinateListeners) {
			l.coordinatesChanged(evt);
		}
	}


}
