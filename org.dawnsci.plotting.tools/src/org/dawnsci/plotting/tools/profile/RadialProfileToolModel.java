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
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;

public class RadialProfileToolModel extends AbstractOperationModel {

	private SectorROI roi = new SectorROI();
	
	public SectorROI getRoi() {
		return roi;
	}
	public void setRoi(SectorROI roi) {
		firePropertyChange("roi", this.roi, this.roi = roi);
	}
	
}
