package org.dawb.workbench.plotting.system.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.util.text.NumberUtils;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * An axis which can 
 * @author fcp94556
 *
 */
public class AspectAxis extends Axis {

	private AspectAxis relativeTo;
    private boolean    keepAspect; // This is so that the user may have images with and without aspect in the same application.
	public AspectAxis(String title, boolean yAxis) {
		super(title, yAxis);
		keepAspect = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.ASPECT);
	}

	public void setKeepAspectWith(final AspectAxis axis) {
		this.relativeTo = axis;
	}
	
	public void checkBounds() {
		
		final Rectangle bounds = getBounds();
		if (relativeTo == null) {
			super.setBounds(bounds);
			return;
		}
		
		if (!keepAspect) {
			super.setBounds(bounds);
			return;
		}
		
		// We keep aspect if the other axis has a larger range than this axis.
		final double  thisRange  = getInterval(getRange());
		final double  relRange   = getInterval(relativeTo.getRange());
		final boolean equal      = NumberUtils.equalsPercent(thisRange, relRange, 0.001);
		final boolean isOtherReallyLonger = isLonger(bounds, getGraph().getPlotArea().getBounds());
		final boolean isRelative  = equal && !isOtherReallyLonger; // The parent layouts ys second so x is the right size.
		final boolean isOtherLarger= relRange>thisRange;
		
		if (isRelative || isOtherLarger) {
			setRelativeAxisBounds(bounds, thisRange, relRange);
			return;
		}
		
		super.setBounds(bounds);
	}

	private XYGraph getGraph() {
		return (XYGraph)getParent();
	}

	/**
	 * true if with is longer in its direction in pixels than this axis. 
	 * @param aspectAxis
	 * @param relativeTo2
	 * @return
	 */
	private boolean isLonger(Rectangle compare, Rectangle otherBounds) {
		final int len1 = isYAxis() ? compare.height : compare.width;
		final int len2 = relativeTo.isYAxis() ? otherBounds.height : otherBounds.width;
		if (len1==len2) return true;
		return len2>=len1;
	}

	private void setRelativeAxisBounds (final Rectangle sugBounds, 
										final double    thisRange, 
										final double    relRange) {

		/*
		final int start  = relativeTo.getValuePosition(relativeTo.getRange().getLower(), false);
		final int end    = relativeTo.getValuePosition(relativeTo.getRange().getUpper(), false);
		final int span   = Math.max(start,  end)-Math.min(start, end); // pixels
		 */
		final Rectangle relBounds = relativeTo.getBounds();
		int      realPixels = relativeTo.isYAxis() ? relBounds.height : relBounds.width;
		realPixels-= 2*relativeTo.getMargin();
		
		final double    pixRatio  = realPixels/relRange;   // pix / unit
		int       range     = (int)Math.round(thisRange*pixRatio);    // span for thisRange of them
		range+=2*getMargin();
		
		Rectangle bounds = sugBounds.getCopy();
		if (isYAxis()) bounds.height = range; 
		else           bounds.width  = range;

		super.setBounds(bounds);
	}

	/**
	 * Should be a method on Range really but 
	 * @param range
	 * @return
	 */
	private double getInterval(Range range) {
		return Math.max(range.getLower(), range.getUpper()) - Math.min(range.getLower(), range.getUpper());
	}

	public boolean isKeepAspect() {
		return keepAspect;
	}

	public void setKeepAspect(boolean keepAspect) {
		this.keepAspect = keepAspect;
	}
}
