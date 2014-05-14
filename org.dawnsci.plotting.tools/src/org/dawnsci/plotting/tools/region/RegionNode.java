/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.plotting.api.region.IRegion;

/**
 * This class is a custom LabelNode to hold data about Region nodes in 
 * the tree viewer Region Editor
 *
 * @author wqk87977
 *
 */
public class RegionNode extends LabelNode {

	private boolean isVisible;
	private boolean isActive;
	private boolean isMobile;
	private boolean angleInRadian = false;
	private IRegion region;

	public RegionNode() {
		super(null, null);
	}

	public RegionNode(RegionNode parent) {
		super(null, parent);
	}

	public RegionNode(String label) {
		super(label, null);
	}

	public RegionNode(IRegion region, LabelNode parent) {
		this.region = region;
		setLabel(region.getName());
		setParent(parent);
		if (getParent() != null)
			((LabelNode) getParent()).addChild(this);
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		region.setVisible(isVisible);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
		region.setActive(isActive);
	}

	public boolean isMobile() {
		return isMobile;
	}

	public void setMobile(boolean isMobile) {
		this.isMobile = isMobile;
		region.setMobile(isMobile);
	}

	public boolean isAngleInRadian() {
		return angleInRadian;
	}

	public void setAngleInRadian(boolean angleInRadian) {
		this.angleInRadian = angleInRadian;
	}

}
