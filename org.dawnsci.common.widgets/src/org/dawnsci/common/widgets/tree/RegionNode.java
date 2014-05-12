/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

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

	public RegionNode() {
		super(null, null);
	}

	public RegionNode(RegionNode parent) {
		super(null, parent);
	}

	public RegionNode(String label) {
		super(label, null);
	}

	public RegionNode(String label, LabelNode parent) {
		setLabel(label);
		setParent(parent);
		if (getParent() != null)
			((LabelNode) getParent()).addChild(this);
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isMobile() {
		return isMobile;
	}

	public void setMobile(boolean isMobile) {
		this.isMobile = isMobile;
	}

	public boolean isAngleInRadian() {
		return angleInRadian;
	}

	public void setAngleInRadian(boolean angleInRadian) {
		this.angleInRadian = angleInRadian;
	}

}
