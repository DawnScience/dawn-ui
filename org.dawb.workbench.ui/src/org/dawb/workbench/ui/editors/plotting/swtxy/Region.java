package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.draw2d.Figure;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;

/**
 * Shape used for ROIs which has bounds fixed to the graph area.
 * 
 * @author fcp94556
 *
 */
public abstract class Region {


	private RegionBean bean;
    private ISelectionProvider selectionProvider;

	public Region(String name, Trace trace) {
		super();
		this.bean = new RegionBean();
		bean.setName(name);
		bean.setTrace(trace);
	}

	public abstract void createContents(final Figure parent);

	public abstract void remove();


	public Axis getXAxis() {
		return bean.getTrace().getXAxis();
	}
	public Axis getYAxis() {
		return bean.getTrace().getYAxis();
	}	
	
	public String getName() {
		return bean.getName();
	}


	public void setName(String name) {
		bean.setName(name);
	}


	public Trace getTrace() {
		return bean.getTrace();
	}


	public void setTrace(Trace trace) {
		bean.setTrace(trace);
	}


	public XYGraph getXyGraph() {
		return bean.getXyGraph();
	}


	public void setXyGraph(XYGraph xyGraph) {
		bean.setXyGraph(xyGraph);
	}


	public boolean isFree() {
		return bean.isFree();
	}


	public void setFree(boolean free) {
		bean.setFree(free);
	}


	public Color getRegionColor() {
		return bean.getRegionColor();
	}


	public void setRegionColor(Color regionColor) {
		bean.setRegionColor(regionColor);
	}


	public boolean isShowPosition() {
		return bean.isShowPosition();
	}


	public void setShowPosition(boolean showPosition) {
		bean.setShowPosition(showPosition);
	}

	public void sync(RegionBean memento) {
		setName(memento.getName());
		setShowPosition(memento.isShowPosition());
		setFree(memento.isFree());
		setTrace(memento.getTrace());
		setXyGraph(memento.getXyGraph());
		setRegionColor(memento.getRegionColor());
	}

	public RegionBean getBean() {
		return bean;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

}
