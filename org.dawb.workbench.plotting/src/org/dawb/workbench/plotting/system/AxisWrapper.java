package org.dawb.workbench.plotting.system;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.IAxis;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

/**
 * Class to make a CSS Axis look like the interface used in IPlottingSystem.
 * This is close to a 1:1 mapping as the CSS interface is logical and well designed.
 * 
 * @author fcp94556
 *
 */
class AxisWrapper implements IAxis {

	private Axis wrappedAxis;

	public AxisWrapper(Axis axis) {
		this.wrappedAxis = axis;
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
}
