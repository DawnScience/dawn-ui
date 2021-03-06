/*
 * Copyright (c) 2012, 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;

/**
 * Class used to create nodes with for the Region Tree Editor
 * @author wqk87977
 *
 */
public class RegionEditorNodeFactory {

	public static final String ANGLE = "Angle";
	public static final String INTENSITY = "Intensity";
	public static final String SUM = "Sum";
	public static final String SYMMETRY = "Symmetry";

	/**
	 * 
	 * @param region
	 * @return a Map of key-value pairs defining a ROI
	 */
	public static Map<String, Object> getRegionNodeInfos(IROI roi) {
		if (roi == null)
			return null;
		Map<String, Object> roiInfos = new LinkedHashMap<String, Object>();
		if (roi instanceof RectangularROI) {
			roiInfos.put("X Start", ((RectangularROI)roi).getPointX());
			roiInfos.put("Y Start", ((RectangularROI)roi).getPointY());
			roiInfos.put("Width", ((RectangularROI)roi).getLengths()[0]);
			roiInfos.put("Height", ((RectangularROI)roi).getLengths()[1]);
			roiInfos.put("Angle", ((RectangularROI)roi).getAngleDegrees());
			roiInfos.put("Max Intensity", Double.NaN);
			roiInfos.put("Sum", Double.NaN);
		} else if (roi instanceof LinearROI) {
			roiInfos.put("X Start", ((LinearROI)roi).getPointX());
			roiInfos.put("Y Start", ((LinearROI)roi).getPointY());
			roiInfos.put("X End", ((LinearROI)roi).getEndPoint()[0]);
			roiInfos.put("Y End", ((LinearROI)roi).getEndPoint()[1]);
			roiInfos.put("Angle", ((LinearROI)roi).getAngleDegrees());
			roiInfos.put("Intensity", Double.NaN);
		} else if (roi instanceof CircularROI) {
			roiInfos.put("X Centre", ((CircularROI)roi).getPointX());
			roiInfos.put("Y Centre", ((CircularROI)roi).getPointY());
			roiInfos.put("Radius", ((CircularROI)roi).getRadius());
		} else if (roi instanceof SectorROI) {
			roiInfos.put("X Centre", ((SectorROI)roi).getPointX());
			roiInfos.put("Y Centre", ((SectorROI)roi).getPointY());
			roiInfos.put("Inner Radius", ((SectorROI)roi).getRadii()[0]);
			roiInfos.put("Outer Radius", ((SectorROI)roi).getRadii()[1]);
			roiInfos.put("Angle 1", ((SectorROI)roi).getAnglesDegrees()[0]);
			roiInfos.put("Angle 2", ((SectorROI)roi).getAnglesDegrees()[1]);
			roiInfos.put("Symmetry", ((SectorROI)roi).getSymmetry());
		} else if (roi instanceof RingROI) {
			roiInfos.put("X Centre", ((RingROI)roi).getPointX());
			roiInfos.put("Y Centre", ((RingROI)roi).getPointY());
			roiInfos.put("Inner Radius", ((RingROI)roi).getRadii()[0]);
			roiInfos.put("Outer Radius", ((RingROI)roi).getRadii()[1]);
		} if (roi instanceof PointROI) {
			roiInfos.put("X Start", ((PointROI)roi).getPointX());
			roiInfos.put("Y Start", ((PointROI)roi).getPointY());
			roiInfos.put("Intensity", Double.NaN);
		}
		return roiInfos;
	}
}
