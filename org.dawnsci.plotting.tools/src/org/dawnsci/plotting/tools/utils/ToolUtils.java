/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.utils;

import org.dawb.common.ui.plot.roi.data.LinearROIData;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for tools
 * @author wqk87977
 *
 */
public class ToolUtils {

	private static Logger logger = LoggerFactory.getLogger(ToolUtils.class);

	/**
	 * 
	 * @param im
	 * @param bounds
	 * @return a Slice of image given a ROI and taking into account out of bounds cases
	 */
	public static IDataset getClippedSlice(IDataset im, RectangularROI bounds) {
		final int yInc = bounds.getPoint()[1] < bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0] < bounds.getEndPoint()[0] ? 1 : -1;
		try {
			IDataset slice = null;
			double xStart = bounds.getPoint()[0];
			double yStart = bounds.getPoint()[1];
			double xEnd = bounds.getEndPoint()[0];
			double yEnd = bounds.getEndPoint()[1];
			// shape[0] = y, shape[1] = x
			int[] shape = im.getShape();
			// Check for most of the possible cases where a region might be out of bounds
			if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[0] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { shape[1], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart < 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, 0 },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if (xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { (int) yEnd, (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart < 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { 0, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd <= shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { (int) yEnd, shape[1] },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd <= shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], (int) xEnd },
						new int[] {yInc, xInc});
			else if(xStart >= 0 && yStart >= 0 && xEnd > shape[1] && yEnd > shape[0])
				slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
						new int[] { shape[0], shape[1] },
						new int[] {yInc, xInc});
			else slice = im.getSlice(new int[] { (int) yStart, (int) xStart },
					new int[] { (int) yEnd, (int) xEnd },
					new int[] {yInc, xInc});
			return slice;
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger .trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image", ne);
		}
		return null;
	}

	/**
	 * 
	 * @param rectangleSlice
	 * @return the max intensity (pixel) of the rectangle slice
	 */
	public static double getRectangleMaxIntensity(IDataset rectangleSlice) {
		if (rectangleSlice == null)
			return Double.NaN;
		int[] maxPos = rectangleSlice.maxPos();
		return rectangleSlice.getDouble(maxPos[0], maxPos[1]);
	}

	/**
	 * 
	 * @param im
	 * @param lroi
	 * @return the line intensity of the linearROI given an image dataset
	 */
	public static double getLineIntensity(IDataset im, LinearROI lroi) {
		LinearROIData lrd = new LinearROIData(lroi, (Dataset) im, 1d);
		try {
			double max2 = lrd.getProfileData().length > 1
					&& lrd.getProfileData()[1] != null
					? lrd.getProfileData()[1].max().doubleValue()
					: -Double.MAX_VALUE;
			return Math.max(lrd.getProfileData()[0].max().doubleValue(), max2);
		} catch (Throwable ne) {
			return Double.NaN;
		}
	}

	/**
	 * 
	 * @param rectangleSlice
	 * @return the sum of intensities of the rectangle slice
	 */
	public static double getRectangleSum(IDataset rectangleSlice) {
		if (rectangleSlice == null)
			return Double.NaN;
		return (Double)((Dataset)rectangleSlice).sum(true);
	}

	/**
	 * 
	 * @param format
	 * @return the increment value given a String format
	 */
	public static double getDecimal(String format) {
		int decimal = format.split("\\.").length > 1 ? format.split("\\.")[1].length() : 0;
		double increment = 1;
		for (int i = 0; i < decimal; i++) {
			increment = (double) increment / 10;
		}
		return increment;
	}
}
