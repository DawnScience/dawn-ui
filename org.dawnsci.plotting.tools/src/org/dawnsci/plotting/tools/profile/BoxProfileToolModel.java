/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

public class BoxProfileToolModel extends AbstractOperationModel {
	
	private RectangularROI roi = new RectangularROI(0, 0, 10, 10, 0);
	private boolean saveY = true;
	private boolean saveX = true;
	
	public RectangularROI getRoi() {
		return roi;
	}
	public void setRoi(RectangularROI roi) {
		firePropertyChange("roi", this.roi, this.roi = roi);
	}
	public boolean isSaveY() {
		return saveY;
	}
	public void setSaveY(boolean saveY) {
		firePropertyChange("saveY", this.saveY, this.saveY = saveY);
	}
	public boolean isSaveX() {
		return saveX;
	}
	public void setSaveX(boolean saveX) {
		firePropertyChange("saveX", this.saveX, this.saveX = saveX);
	}
	

}
