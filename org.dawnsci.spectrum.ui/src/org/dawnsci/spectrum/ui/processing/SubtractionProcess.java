/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class SubtractionProcess extends AbstractCacheProcess{
	
	double scale = 1;
	
	public SubtractionProcess(IContain1DData subtrahend) {
		super(subtrahend);
	}
	
	public void setScale(double scale) {
		this.scale = scale;
	}
	
	public double getScale() {
		return scale;
	}
	

	@Override
	protected Dataset process(final Dataset x, final Dataset y) {
		Dataset y1 = DatasetUtils.convertToDataset(cachedData.getyDatasets().get(0));
		Dataset s = Maths.multiply(y1, scale);
		s = Maths.subtract(y, s);
		return s;
	}
	
	public IContain1DData getSubtrahend() {
		return cachedData;
	}

	@Override
	protected String getAppendingName() {
		return "-"+oCachedData.getName();
	}

}
