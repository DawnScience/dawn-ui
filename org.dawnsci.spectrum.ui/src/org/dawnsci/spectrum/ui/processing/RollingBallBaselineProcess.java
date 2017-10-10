/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.baseline.BaselineGeneration;

public class RollingBallBaselineProcess extends AbstractProcess {

	int width = 1;
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return rollingBallBaselineCorrection(y, width);
	}

	@Override
	protected String getAppendingName() {

		return "_rolling_baseline_"+width;
	}
	
	private  Dataset rollingBallBaselineCorrection(Dataset y, int width) {

		Dataset t1 = BaselineGeneration.rollingBallBaseline(y, width);
		return Maths.subtract(y, t1);
	}

}
