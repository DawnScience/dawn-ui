/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.draw2d.Graphics;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Paints the labels on the image trace.
 * @author Matthew Gerring
 *
 */
class IntensityLabelPainter {
	
	private static Logger logger = LoggerFactory.getLogger(IntensityLabelPainter.class);
	
	private IImageTrace image;
	private IPlottingSystem<?> system;

	IntensityLabelPainter(IPlottingSystem<?> system, IImageTrace image) {
		this.system = system;
		this.image  = image;
	}

	/**
	 * Attempts to paint the labels for intensity a the approximate centre of each pixel.
	 * Does not work if custom axes are used currently.
	 * 
	 * Assumes that the caller will do a push and pop on the graphics appropriately.
	 * 
	 * @param graphics
	 */
	public void paintIntensityLabels(Graphics graphics) {
		
		if (system==null)          return;
		if (!system.isShowValueLabels()) return;
		
		try {
			graphics.setFont(new Font(Display.getCurrent(), new FontData("Dialog", 10, SWT.NORMAL)));
			
			final IAxis xAxis = system.getSelectedXAxis();
			final IAxis yAxis = system.getSelectedYAxis();
			IDataset data = image.getData();

			// Paint labels at centre pixels
			double[] lower = new double[] {xAxis.getLower(), yAxis.getLower()};
			lower = image.getPointInImageCoordinates(lower);

			double[] upper = new double[] {xAxis.getUpper(), yAxis.getUpper()};
			upper = image.getPointInImageCoordinates(upper);

			int xLower, xUpper;
			if (xAxis.isInverted()) {
				xLower = (int) upper[0];
				xUpper = (int) Math.ceil(lower[0]);
			} else {
				xLower = (int) lower[0];
				xUpper = (int) Math.ceil(upper[0]);
			}
			int yLower, yUpper;
			if (yAxis.isInverted()) {
				yLower = (int) upper[1];
				yUpper = (int) Math.ceil(lower[1]);
			} else {
				yLower = (int) lower[1];
				yUpper = (int) Math.ceil(upper[1]);
			}

			final boolean transpose = !(image.getImageServiceBean().isTransposed() ^ image.getImageOrigin().isOnLeadingDiagonal());

			for (int y = yLower; y < yUpper; y++) {
				for (int x = xLower; x < xUpper; x++) {
					String lText = transpose ? data.getString(x, y) : data.getString(y, x);
					int lx = (int) xAxis.getPositionFromValue(x+0.5);
					int ly = (int) yAxis.getPositionFromValue(y+0.5);
					graphics.setAlpha(75);
					graphics.fillString(lText, lx, ly);
					graphics.setAlpha(255);
					graphics.drawString(lText, lx, ly);
				}
			}
		} catch (Throwable ne) {
			logger.debug("Unable to process labels!", ne);
			return;
		}
	}

}
