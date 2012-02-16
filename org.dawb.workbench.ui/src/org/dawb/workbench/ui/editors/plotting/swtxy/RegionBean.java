package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.io.Serializable;

import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.eclipse.swt.graphics.Color;

public class RegionBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3501897005952664393L;
	
	
	protected String name;
	protected Trace  trace;
	protected XYGraph xyGraph;
	protected boolean free = true;
	protected Color  regionColor;
	protected boolean showPosition;

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

	public XYGraph getXyGraph() {
		return xyGraph;
	}

	public void setXyGraph(XYGraph xyGraph) {
		this.xyGraph = xyGraph;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public Color getRegionColor() {
		return regionColor;
	}

	public void setRegionColor(Color regionColour) {
		this.regionColor = regionColour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (free ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((regionColor == null) ? 0 : regionColor.hashCode());
		result = prime * result + (showPosition ? 1231 : 1237);
		result = prime * result + ((trace == null) ? 0 : trace.hashCode());
		result = prime * result + ((xyGraph == null) ? 0 : xyGraph.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegionBean other = (RegionBean) obj;
		if (free != other.free)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (regionColor == null) {
			if (other.regionColor != null)
				return false;
		} else if (!regionColor.equals(other.regionColor))
			return false;
		if (showPosition != other.showPosition)
			return false;
		if (trace == null) {
			if (other.trace != null)
				return false;
		} else if (!trace.equals(other.trace))
			return false;
		if (xyGraph == null) {
			if (other.xyGraph != null)
				return false;
		} else if (!xyGraph.equals(other.xyGraph))
			return false;
		return true;
	}

	public boolean isShowPosition() {
		return showPosition;
	}

	public void setShowPosition(boolean showPosition) {
		this.showPosition = showPosition;
	}

	public void sync(RegionBean bean) {
		setName(bean.getName());
		setShowPosition(bean.isShowPosition());
		setFree(bean.isFree());
		setTrace(bean.getTrace());
		setXyGraph(bean.getXyGraph());
		setRegionColor(bean.getRegionColor());
	}
	

}
