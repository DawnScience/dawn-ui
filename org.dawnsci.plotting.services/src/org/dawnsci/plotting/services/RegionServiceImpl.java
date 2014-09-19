/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalFitROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.FreeDrawROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PerimeterBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionService;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;

public class RegionServiceImpl implements IRegionService {

	static {
		System.out.println("Starting region service");
	}
	public RegionServiceImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	private static Map<Object,Object> roiMap;
	
	private synchronized static Map<Object,Object> getRoiMap() {
		if (roiMap!=null) return roiMap;

		roiMap = new HashMap<Object,Object>(7);
		roiMap.put(RegionType.LINE,          LinearROI.class);
		roiMap.put(RegionType.POLYLINE,      PolylineROI.class);
		roiMap.put(RegionType.POLYGON,       PolygonalROI.class);
		roiMap.put(RegionType.BOX,           RectangularROI.class);
		roiMap.put(RegionType.PERIMETERBOX,  PerimeterBoxROI.class);
		roiMap.put(RegionType.GRID,          GridROI.class);
		roiMap.put(RegionType.CIRCLE,        CircularROI.class);
		roiMap.put(RegionType.CIRCLEFIT,     CircularFitROI.class);
		roiMap.put(RegionType.SECTOR,        SectorROI.class);
		roiMap.put(RegionType.POINT,         PointROI.class);
		roiMap.put(RegionType.ELLIPSE,       EllipticalROI.class);
		roiMap.put(RegionType.ELLIPSEFIT,    EllipticalFitROI.class);
		roiMap.put(RegionType.RING,          RingROI.class);
		roiMap.put(RegionType.XAXIS,         XAxisBoxROI.class);
		roiMap.put(RegionType.YAXIS,         YAxisBoxROI.class);
		roiMap.put(RegionType.XAXIS_LINE,    XAxisLineBoxROI.class);
		roiMap.put(RegionType.YAXIS_LINE,    YAxisLineBoxROI.class);
		roiMap.put(RegionType.FREE_DRAW,     FreeDrawROI.class);

		// Goes both ways.
		for (Object key : new HashSet<Object>(roiMap.keySet())) {
			roiMap.put(roiMap.get(key), key);
		}

		return roiMap;
	}
	
	public final RegionType forROI(IROI iroi) {
		return (RegionType)getRoiMap().get(iroi.getClass());
	}
	public RegionType getRegion(Class<? extends IROI> clazz) {
		return (RegionType)getRoiMap().get(clazz);
	}
	
	/**
	 * Method attempts to make the best IRegion it
	 * can for the ROI.
	 * 
	 * @param plottingSystem
	 * @param roi
	 * @param roiName
	 * @return
	 */
	public IRegion createRegion( final IPlottingSystem plottingSystem,
							     final IROI            roi, 
								 final String          roiName) throws Exception {

		IRegion region = plottingSystem.getRegion(roiName);
		if (region != null && region.isVisible()) {
			region.setROI(roi);
			return region;
		} 
		
		RegionType type = null;
		if (roi instanceof LinearROI) {
			type = RegionType.LINE;
			
		} else if (roi instanceof RectangularROI) {
			if (roi instanceof PerimeterBoxROI) {
				type = RegionType.PERIMETERBOX;
			} else if (roi instanceof XAxisBoxROI){
				type = RegionType.XAXIS;
			} else if (roi instanceof YAxisBoxROI){
				type = RegionType.YAXIS;
			} else {
				type = RegionType.BOX;
			}
		
		} else if (roi instanceof RingROI) {
			if (roi instanceof SectorROI) {
				type = RegionType.SECTOR;
			} else {
				type = RegionType.RING;
			}
		} else if (roi instanceof CircularROI) {
			type = RegionType.CIRCLE;
			
		} else if (roi instanceof CircularFitROI) {
			type = RegionType.CIRCLEFIT;
			
		} else if (roi instanceof EllipticalROI) {
			type = RegionType.ELLIPSE;
			
		} else if (roi instanceof EllipticalFitROI) {
			type = RegionType.ELLIPSEFIT;
			
		} else if (roi instanceof PointROI) {
			type = RegionType.POINT;

		}
		
		if (type==null) return null;
		
		IRegion newRegion = plottingSystem.createRegion(roiName, type);
		newRegion.setROI(roi);
		plottingSystem.addRegion(newRegion);

		return newRegion;

	}
}
