package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public abstract class RegionShape extends Shape {
	
	protected String name;
	protected Trace  trace;
	protected XYGraph xyGraph;
	protected boolean free = true;

	public RegionShape(String name, Trace trace) {
		super();
		this.name  = name;
		this.trace = trace;
	}

	@Override
	protected void fillShape(Graphics graphics) {
		
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public void setxyGraph(XYGraph xyGraph) {
		this.xyGraph = xyGraph;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public Axis getXAxis() {
		return trace.getXAxis();
	}
	public Axis getYAxis() {
		return trace.getYAxis();
	}

	private Color regionColor;
	public void setRegionColor(Color color) {
		this.regionColor = color;
	}
	public Color getRegionColor(){
		return regionColor;
	}
  
	private boolean showPosition = true;
	private Rectangle fixedBounds;

	public boolean isShowPosition() {
		return showPosition;
	}

	public void setShowPosition(boolean showPosition) {
		this.showPosition = showPosition;
	}

	public void setBounds(final Rectangle bounds) {
		super.setBounds(bounds);
		this.fixedBounds = bounds;
	}

	protected void primTranslate(int dx, int dy) {
        super.primTranslate(dx, dy);
		this.bounds = this.bounds.getUnion(fixedBounds);
	}

}
