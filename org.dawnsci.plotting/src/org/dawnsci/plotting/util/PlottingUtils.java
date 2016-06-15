/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlottingUtils {
	private final static Logger logger = LoggerFactory.getLogger(PlottingUtils.class);

	/**
	 * Method that plots data to a LightWeight PlottingSystem
	 * @param plottingSystem
	 *             the LightWeight plotting system
	 * @param data
	 *             the data to plot
	 */
	public static void plotData(final IPlottingSystem<?> plottingSystem,
								final String plotTitle,
								final IDataset data){
		Job plotJob = new Job("Plotting data") {
			
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {

					plottingSystem.clear();
					if(data == null) return Status.CANCEL_STATUS;
					plottingSystem.updatePlot2D(data, null, monitor);
					plottingSystem.setTitle(plotTitle);
					plottingSystem.getAxes().get(0).setTitle("");
					plottingSystem.getAxes().get(1).setTitle("");
					plottingSystem.setKeepAspect(true);
					plottingSystem.setShowIntensity(false);
				} catch (Exception e) {
					logger.error("Error plotting data", e);
					return Status.CANCEL_STATUS;
				}
					return Status.OK_STATUS;
			}
		};
		plotJob.schedule();
	}

	/**
	 * Returns the bin shape given a SurfaceROI width and height
	 * 
	 * @param width
	 * @param height
	 * @param isDrag
	 * @return binShape
	 */
	public static int getBinShape(double width, double height, boolean isDrag) {
		int binShape = 1;
		if (isDrag && 
				((width > 300 && width < 900 && height > 300 && width < 900)// size above 300x300 and below 900x900
				|| (width < 300 && height > 300)					// if width below 300 but height above
				|| (width > 300 && height < 300))) {				// if width above 300 but height below
			binShape = (int)(((width + height) / 2) / 100) - 1;
		} else if (!isDrag && 
				((width > 300 && width < 900 && height > 300 && width < 900)
						|| (width < 300 && height > 300)
						|| (width > 300 && height < 300))) {
			binShape = (int)(((width + height) / 2) / 100) - 2;
		} else if (isDrag &&					// if size is bigger than 900x900 
				((width > 900 && height > 900)
				||(width > 900 && height < 900)
				||(width < 900 && height > 900))) {
			binShape = (int)(((width + height) / 2) / 100);
		} else if (!isDrag && 
				((width > 900 && height > 900)
				||(width > 900 && height < 900)
				||(width < 900 && height > 900))) {
			binShape = (int)(((width + height) / 2) / 100) - 1;
		}
		if (binShape == 0) // reset to 1 if binShape is zero
			binShape = 1;
		return binShape;
	}
}
