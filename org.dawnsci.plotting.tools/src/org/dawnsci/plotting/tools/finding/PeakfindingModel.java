/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingData;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

/**
 * TODO: just use peak opp
 * 
 * @author Dean P. Ottewell
 *
 */
public class PeakfindingModel extends AbstractOperationModel {

	private static final Logger logger = LoggerFactory.getLogger(PeakFindingManager.class);
	
	List<IdentifiedPeak> peaksId = null;
	
	IDataset xdata;
	
	IDataset ydata;
	
	private Class<? extends IOptimizer> optimizerClass;
	
	//NEED TO LOAD THE PEAK FINDER SELECTED
	
	private static IPeakFindingService peakFindServ = (IPeakFindingService) Activator
			.getService(IPeakFindingService.class);

	private String peakFinderID;
	
	private Double searchScaleIntensity;
	

	private IPeakFindingData peakFindData; 

	
	public static IPeakFindingService getPeakFindServ() {
		return peakFindServ;
	}
	
	public IPeakFindingData getPeakFindData() {
		return peakFindData;
	}
	
	public void setPeakFindData(IPeakFindingData peakFindData) {
		this.peakFindData = peakFindData;
	}
	
	
	

	
	/*TMP PLACEMENT HACK*/
	/**
	 * Assumes peakpos are those represented in yData passed into. 
	 * xData and yData must be same size
	 * 
	 * @param peakpos
	 * @param xData
	 * @param yData 
	 * @return every peak pos inside @peakpos cast to identified Peak
	 */
//	List<IdentifiedPeak> convertIntoPeaks(Map<Integer, Double> peakpos, Dataset xData, Dataset yData){
//		
//		if(xData.getSize() != yData.getSize())
//			logger.error("Signal data must be matching size");
//		
//		List<IdentifiedPeak> peaksID = new ArrayList<IdentifiedPeak>();
//		for (Map.Entry<Integer, Double> peak : peakpos.entrySet()) {
//			peaksID.add(generateIdentifedPeak(peak.getKey(),xData,yData));
//		}
//		return peaksID;
//	
//	
//	}
}
