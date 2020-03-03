/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.roi;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;

//import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;

public enum ROIType {

	LINEAR(LinearROI.class),
	//POLYGONAL(PolygonalROI.class),   // TODO
	POINT(PointROI.class),
	RECTANGULAR(RectangularROI.class),
	PERIMETERBOX(PerimeterBoxROI.class),
	XAXIS(XAxisBoxROI.class),
	YAXIS(YAxisBoxROI.class),
	SECTOR(SectorROI.class),
	RING(RingROI.class),
	ELLIPICAL(EllipticalROI.class);
	
	private Class<? extends IROI> clazz;

	ROIType(Class<? extends IROI> clazz) {
		this.clazz = clazz;
	}

	public static String[] getTypes() {
		final ROIType[] ops = ROIType.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public static String[] getTypes(Class<? extends IROI> rClazz) {
		if (rClazz == null) {
			return getTypes();
		}
		final List<String> names = new ArrayList<>();
		for (ROIType t : ROIType.values()) {
			if (rClazz.isAssignableFrom(t.clazz)) {
				names.add(t.getName());
			}
		}
		return names.toArray(new String[names.size()]);
	}

	public String getName() {
		return clazz.getSimpleName();
	}

	public static ROIType getType(String name) {
		for (ROIType t : ROIType.values()) {
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}

	public IROI getRoi() throws InstantiationException, IllegalAccessException {
		return clazz.newInstance();
	}

	/**
	 * Get name of ROI type
	 * @param rClass RIO class
	 * @return name or null if class is not in enumeration
	 */
	public static String getName(Class<? extends IROI> rClass) { // just simple name though
		final ROIType[] ops = ROIType.values();
		for (ROIType roiType : ops) {
			if (roiType.clazz == rClass) return roiType.getName();
		}
		return null;
	}

	public static IROI createNew(String name) throws InstantiationException, IllegalAccessException {
		final ROIType roi = getType(name);
		return roi == null ? null : roi.getRoi();
	}
}
