/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.io.Serializable;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.swt.graphics.Color;

public class RegionBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3501897005952664393L;
	
	protected ICoordinateSystem    coordinateSystem;
	protected IXYGraph xyGraph;
	protected String  name;
	protected PlotType plotType;
	protected Color   regionColor;
	protected boolean showPosition=false;
	protected int     alpha=80;

	/**
	 * Controls whether the mapping region is shown in the plot<br>
	 * Default value must match default value of <code>RegistryToggleState</code>
	 * for command id <code>uk.ac.diamond.daq.mapping.ui.command.showHideMappingRegion</code>
	 * in <code>plugin.xml</code>
	 */
	protected boolean visible=true;

	/**
	 * Controls whether the mapping region fill is shown in the plot<br>
	 * Default value must match default value of <code>RegistryToggleState</code>
	 * for command id <code>uk.ac.diamond.daq.mapping.ui.command.toggleFillMappingRegion</code>
	 * in <code>plugin.xml</code>
	 */
	protected boolean fill=true;

	protected boolean mobile=true;
	protected boolean showLabel=false;	
	public void sync(RegionBean bean) {
		setName(bean.getName());
		setPlotType(bean.getPlotType());
		setShowPosition(bean.isShowPosition());
		setXyGraph(bean.getXyGraph());
		setRegionColor(bean.getRegionColor());
		setAlpha(bean.getAlpha());
		setVisible(bean.isVisible());
		setMobile(bean.isMobile());
		setFill(bean.isFill());
		setShowLabel(bean.isShowLabel());
	}

	public ICoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}

	public void setCoordinateSystem(ICoordinateSystem coordinateSystem) {
		this.coordinateSystem = coordinateSystem;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlotType getPlotType() {
		return plotType;
	}

	public void setPlotType(PlotType type) {
		this.plotType = type;
	}

	public IXYGraph getXyGraph() {
		return xyGraph;
	}

	public void setXyGraph(IXYGraph xyGraph) {
		this.xyGraph = xyGraph;
	}

	public Color getRegionColor() {
		return regionColor;
	}

	public void setRegionColor(Color regionColour) {
		this.regionColor = regionColour;
	}

	public boolean isShowPosition() {
		return showPosition;
	}

	public void setShowPosition(boolean showPosition) {
		this.showPosition = showPosition;
	}

	public int getAlpha() {
		return alpha;
	}

	/**
	 * 0-255
	 * @param alpha
	 */
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + alpha;
		result = prime
				* result
				+ ((coordinateSystem == null) ? 0 : coordinateSystem.hashCode());
		result = prime * result + (mobile ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((regionColor == null) ? 0 : regionColor.hashCode());
		result = prime * result + (showLabel ? 1231 : 1237);
		result = prime * result + (showPosition ? 1231 : 1237);
		result = prime * result + (visible ? 1231 : 1237);
		result = prime * result + (fill ? 1231 : 1237);
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
		if (alpha != other.alpha)
			return false;
		if (coordinateSystem == null) {
			if (other.coordinateSystem != null)
				return false;
		} else if (!coordinateSystem.equals(other.coordinateSystem))
			return false;
		if (mobile != other.mobile)
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
		if (showLabel != other.showLabel)
			return false;
		if (showPosition != other.showPosition)
			return false;
		if (visible != other.visible)
			return false;
		if (fill != other.fill)
			return false;
		if (xyGraph == null) {
			if (other.xyGraph != null)
				return false;
		} else if (!xyGraph.equals(other.xyGraph))
			return false;
		return true;
	}


	public boolean isShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}
}
