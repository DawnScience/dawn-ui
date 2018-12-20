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

public class ZoomProfileToolModel extends AbstractOperationModel {

	private RectangularROI region = new RectangularROI(0,0,10,10, 0);

	public RectangularROI getRegion() {
		return region;
	}

	public void setRegion(RectangularROI region) {
		firePropertyChange("region", this.region, this.region = region);
	}
	
	
}
