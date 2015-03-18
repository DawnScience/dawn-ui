/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import org.dawnsci.common.widgets.tree.LabelNode;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * This class is a custom LabelNode to hold data about Region nodes in 
 * the tree viewer Region Editor
 *
 * @author wqk87977
 *
 */
public class RegionEditorNode extends LabelNode {

	private boolean isVisible;
	private boolean isActive;
	private boolean isMobile;
	private boolean angleInRadian = false;
	private IRegion region;
	private IPlottingSystem plottingSystem;

	
	public RegionEditorNode(IPlottingSystem plottingSystem, IRegion region, LabelNode parent) {
		this.plottingSystem = plottingSystem;
		this.region = region;
		setLabel(region.getName());
		setParent(parent);
		if (getParent() != null)
			((LabelNode) getParent()).addChild(this);
	}

	public void setName(String value) {
		if (region.getName().equals(value))
			return;

		try {
			plottingSystem.renameRegion(region, value);
			setLabel(value);
			setTooltip(value);
		} catch (Exception e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Region Exists", 
					"The region '"+value+"' already exists.\n\n"+
			        "Please choose a unique name for regions.");
		}
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

	public IRegion getRegion() {
		return region;
	}
}
