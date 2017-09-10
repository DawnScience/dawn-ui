package org.dawnsci.dedi.ui.views.plot.plotters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.vecmath.Vector2d;

import org.dawnsci.dedi.configuration.BeamlineConfiguration;
import org.dawnsci.dedi.configuration.calculations.BeamlineConfigurationUtil;
import org.dawnsci.dedi.configuration.calculations.results.controllers.AbstractResultsController;
import org.dawnsci.dedi.configuration.calculations.scattering.D;
import org.dawnsci.dedi.configuration.calculations.scattering.Q;
import org.dawnsci.dedi.ui.views.plot.DefaultBeamlineConfigurationPlot;
import org.dawnsci.dedi.ui.widgets.plotting.Legend;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;


public abstract class AbstractBeamlineConfigurationPlotter implements IBeamlineConfigurationPlotter {
	private DefaultBeamlineConfigurationPlot context;
	protected BeamlineConfiguration beamlineConfiguration; 
	private AbstractResultsController resultsController;
	private IPlottingSystem<Composite> system;
	private Legend legend;
	
	private static final String DETECTOR_REGION = "Detector";
	private static final String CAMERA_TUBE_REGION = "Camera Tube";
	private static final String BEAMSTOP_REGION = "Beamstop";
	private static final String CLEARANCE_REGION = "Clearance";
	private static final String MASK_TRACE = "Mask";
	
	private List<IRegion> calibrantRingRegions = new ArrayList<>();
    private CalibrantSpacing selectedCalibrant;
	
	private Map<Integer, Dataset> maskCache;
	private static final int MAX_CACHE_SIZE = 10;

    
	public AbstractBeamlineConfigurationPlotter(DefaultBeamlineConfigurationPlot context){
		this.context = context;
		this.beamlineConfiguration = context.getBeamlineConfiguration();
		this.system = context.getSystem();
		this.legend = context.getLegend();
		this.resultsController = context.getResultsController();
		
		createMaskCache();
	}
	
	
	@SuppressWarnings("serial")
	private void createMaskCache() {
		maskCache = new LinkedHashMap<Integer, Dataset>(MAX_CACHE_SIZE+1, 0.75F, true){
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<Integer, Dataset> eldest) {
				return size() > MAX_CACHE_SIZE;
			}
		};
	}
	
	
	@Override
	public void createPlot(){
		this.selectedCalibrant = context.getSelectedCalibrant();
		
		if(beamlineConfiguration.getDetector() != null && context.isDetectorPlot()) 
			createDetectorRegion();
		else removeRegion(DETECTOR_REGION);
		
		if(beamlineConfiguration.getDetector() != null && beamlineConfiguration.getCameraTube() != null && context.isCameraTubePlot()) 
			createCameraTubeRegion();
		else removeRegion(CAMERA_TUBE_REGION);
		
		if(beamlineConfiguration.getBeamstop() != null && beamlineConfiguration.getDetector() != null && context.isBeamstopPlot()) 
			createBeamstopRegion();
		else removeRegions(new String[]{BEAMSTOP_REGION, CLEARANCE_REGION});
		
		if(beamlineConfiguration.getBeamstop() != null && beamlineConfiguration.getDetector() != null && 
		   beamlineConfiguration.getAngle() != null && context.isRayPlot()) 
			createRay();
		else removeRegions(new String[] {"Ray1", "Ray2", "Ray3", "Ray4"});
		
		if(beamlineConfiguration.getWavelength() != null && beamlineConfiguration.getCameraLength() != null && 
		   selectedCalibrant != null && context.isCalibrantPlot())
			createCalibrantRings();
		else removeRegions(calibrantRingRegions);
		
		if(context.isMaskPlot()) createMask();
		else removeTrace(MASK_TRACE);
		
		createEmptyTrace();
		rescalePlot();
	}
	
	
	private void createDetectorRegion(){
		removeRegion(DETECTOR_REGION); 
		
		IRegion detectorRegion;
		try {
			detectorRegion = system.createRegion(DETECTOR_REGION, IRegion.RegionType.BOX);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		IROI detectorROI = new RectangularROI(getDetectorTopLeftX(), getDetectorTopLeftY(), getDetectorWidth(), getDetectorHeight(), 0);
		addRegion(detectorRegion, detectorROI, legend.getColour("Detector"));
	}
	
	
	private void createBeamstopRegion(){
		removeRegions(new String[]{BEAMSTOP_REGION, CLEARANCE_REGION});
		
		IRegion beamstopRegion;
		IRegion clearanceRegion;
		
		try {
			beamstopRegion = system.createRegion(BEAMSTOP_REGION, IRegion.RegionType.ELLIPSE);
			clearanceRegion = system.createRegion(CLEARANCE_REGION, IRegion.RegionType.ELLIPSE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		double clearanceMajor = getClearanceMajor();
		double clearanceMinor = getClearanceMinor();
		double beamstopMajor = getBeamstopMajor();
		double beamstopMinor = getBeamstopMinor();
		double beamstopCentreX = getBeamstopCentreX();
		double beamstopCentreY = getBeamstopCentreY();
		
		IROI clearanceROI = new EllipticalROI(clearanceMajor + beamstopMajor, clearanceMinor + beamstopMinor, 0,
				                         beamstopCentreX, beamstopCentreY);
		IROI beamstopROI = new EllipticalROI(beamstopMajor, beamstopMinor, 0, beamstopCentreX, beamstopCentreY);
		
		addRegion(clearanceRegion, clearanceROI, legend.getColour("Clearance"));
		addRegion(beamstopRegion, beamstopROI,legend.getColour("Beamstop"));
	}
	
	
	private void createCameraTubeRegion(){
		removeRegion(CAMERA_TUBE_REGION);
		
		IRegion cameraTubeRegion;
		try {
			cameraTubeRegion = system.createRegion(CAMERA_TUBE_REGION, IRegion.RegionType.ELLIPSE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		IROI cameraTubeROI = new EllipticalROI(getCameraTubeMajor(),getCameraTubeMinor(), 0, 
										  getCameraTubeCentreX(), getCameraTubeCentreY());
		
		cameraTubeRegion.setAlpha(50);
		addRegion(cameraTubeRegion, cameraTubeROI, legend.getColour("Camera tube"));
	}
	
	
	private void createRay() {
		removeRegions(new String[] {"Ray1", "Ray2", "Ray3", "Ray4"});
		
		IRegion visibleRangeRegion1;
		IRegion visibleRangeRegion2;
		IRegion inaccessibleRangeRegion;
		IRegion requestedRangeRegion;
		
		Vector2d visibleRangeStartPoint = resultsController.getVisibleRangeStartPoint();
		Vector2d visibleRangeEndPoint = resultsController.getVisibleRangeEndPoint();
		Vector2d requestedRangeStartPoint = resultsController.getRequestedRangeStartPoint();
		Vector2d requestedRangeEndPoint = resultsController.getRequestedRangeEndPoint();
		
		if(visibleRangeStartPoint == null || visibleRangeEndPoint == null) return;
		
		try {
			visibleRangeRegion1 = system.createRegion("Ray1", IRegion.RegionType.LINE);
			visibleRangeRegion2 = system.createRegion("Ray2", IRegion.RegionType.LINE);
			inaccessibleRangeRegion = system.createRegion("Ray3", IRegion.RegionType.LINE);
			requestedRangeRegion = system.createRegion("Ray4", IRegion.RegionType.LINE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		IROI inaccessibleRangeROI = new LinearROI(new double[] {getBeamstopCentreX(), 
				                                           getBeamstopCentreY()},
											 new double[] {getVisibleRangeStartPointX(), 
											 		       getVisibleRangeStartPointY()});
		
		addRegion(inaccessibleRangeRegion, inaccessibleRangeROI, Display.getDefault().getSystemColor(SWT.COLOR_RED));
		
		if(!resultsController.getIsSatisfied() || requestedRangeStartPoint == null || requestedRangeEndPoint == null){	
			IROI visibleRangeROI1 = new LinearROI(new double[] {getVisibleRangeStartPointX(), 
														   getVisibleRangeStartPointY()}, 
					                         new double[] {getVisibleRangeEndPointX(), 
					                        		       getVisibleRangeEndPointY()});
	
			addRegion(visibleRangeRegion1, visibleRangeROI1, new Color(Display.getDefault(), 205, 133, 63));
		} else {
			IROI visibleRangeROI1 = new LinearROI(new double[] {getVisibleRangeStartPointX(), 
										                   getVisibleRangeStartPointY()}, 
										      new double[] {getRequestedRangeStartPointX(), 
										    		        getRequestedRangeStartPointY()});
			
			IROI visibleRangeROI2 = new LinearROI(new double[] {getRequestedRangeEndPointX(), 
														   getRequestedRangeEndPointY()}, 
										      new double[] {getVisibleRangeEndPointX(), 
										    		        getVisibleRangeEndPointY()});
			
			IROI requestedRangeROI = new LinearROI(new double[] {getRequestedRangeStartPointX(),	
															getRequestedRangeStartPointY()}, 
					                          new double[] {getRequestedRangeEndPointX(),
					                        		        getRequestedRangeEndPointY()});
			
			addRegion(visibleRangeRegion1, visibleRangeROI1, new Color(Display.getDefault(), 205, 133, 63));
			addRegion(visibleRangeRegion2, visibleRangeROI2, new Color(Display.getDefault(), 205, 133, 63));
			addRegion(requestedRangeRegion, requestedRangeROI, Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
		}
	}
	
	
	private void createCalibrantRings(){
	   removeRegions(calibrantRingRegions);
	   calibrantRingRegions = new ArrayList<>();
	   
	   List<HKL> hkls = selectedCalibrant.getHKLs();
	   
	   String ringName = "Ring";
	   for(int i = 0; i < hkls.size(); i++){
		   IRegion ringRegion = null;
		   try {
				ringRegion = system.createRegion(ringName + i, IRegion.RegionType.ELLIPSE);
				calibrantRingRegions.add(ringRegion);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		   
		   Q q = new D(hkls.get(i).getD().to(D.BASE_UNIT)).toQ();
		   IROI ringROI;
		   try {
			   ringROI = new EllipticalROI(getCalibrantRingMajor(q), getCalibrantRingMinor(q), 0, getBeamstopCentreX(), getBeamstopCentreY());
		   } catch(IllegalArgumentException e) {
			   continue;
		   }
				  
		   ringRegion.setFill(false);
		   addRegion(ringRegion, ringROI, Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
	   }
	}
	
	
	@SuppressWarnings("deprecation")
	private void createMask(){
		removeTrace(MASK_TRACE);
		
		DiffractionDetector detector = beamlineConfiguration.getDetector();
		
		if(detector.getNumberOfHorizontalModules() == 0 || detector.getNumberOfVerticalModules() == 0 ||
		   (detector.getXGap() == 0 && detector.getYGap() == 0)) return;
		
		int detectorWidth = detector.getNumberOfPixelsX();  // Number of columns
		int detectorHeight = detector.getNumberOfPixelsY(); // Number of rows
		
		int numberOfHorizontalModules = detector.getNumberOfHorizontalModules();
		int numberOfVerticalModules = detector.getNumberOfVerticalModules();
		
		int gapWidth = detector.getXGap();
		int gapHeight = detector.getYGap();
		
		int moduleWidth = (detectorWidth - (numberOfHorizontalModules-1)*gapWidth)/
							numberOfHorizontalModules;
		int moduleHeight = (detectorHeight - (numberOfVerticalModules-1)*gapHeight)/
							numberOfVerticalModules;
		
		List<Integer> missingModules = detector.getMissingModules();
		
		Dataset mask = maskCache.get(Objects.hash(detectorWidth, detectorHeight, gapWidth, gapHeight,
													numberOfHorizontalModules, numberOfVerticalModules, missingModules));
		
		if(mask == null){
			mask = DatasetFactory.ones(new int[]{detectorHeight, detectorWidth}, Dataset.BOOL);
			
			for(int i = moduleWidth; i < detectorWidth; i += moduleWidth + gapWidth)
				mask.setSlice(false, null, new Slice(i , i+gapWidth));
			
			for(int i = moduleHeight; i < detectorHeight; i += moduleHeight + gapHeight)
				mask.setSlice(false, new Slice(i, i + gapHeight));
			
			if(missingModules != null){
				for(Integer index : missingModules){
					int x = index % numberOfHorizontalModules;
					int y = index/numberOfHorizontalModules;
					int rowSliceStart = y*(moduleHeight + gapHeight);
					int colSliceStart = x*(moduleWidth + gapWidth);
					mask.setSlice(false, new Slice(rowSliceStart, rowSliceStart + moduleHeight),
							             new Slice(colSliceStart, colSliceStart + moduleWidth));
				}
			}
			
			maskCache.put(Objects.hash(detectorWidth, detectorHeight, gapWidth, gapHeight,
										numberOfHorizontalModules, numberOfVerticalModules, missingModules), mask);
		}
		
		Dataset xAxis = DatasetFactory.createRange(getDetectorTopLeftX(), getDetectorTopLeftX() + getHorizontalLengthFromPixels(detectorWidth), getHorizontalLengthFromPixels(1), Dataset.FLOAT64);
		Dataset yAxis = DatasetFactory.createRange(getDetectorTopLeftY(),getDetectorTopLeftY() + getVerticalLengthFromPixels(detectorHeight), getVerticalLengthFromPixels(1), Dataset.FLOAT64);
				
		final IImageTrace image = system.createImageTrace(MASK_TRACE);
		image.setDownsampleType(DownsampleType.POINT);
		image.setRescaleHistogram(false);
		image.setData(mask, Arrays.asList(xAxis, yAxis), false);
		image.setGlobalRange(getGlobalRange());  
		system.addTrace(image);
	}
	
	
	private void createEmptyTrace(){
		removeTrace("Dot");
		final IImageTrace image = system.createImageTrace("Dot");
		image.setRescaleHistogram(false);
		image.setData(DatasetFactory.createFromObject((boolean[][]) new boolean[1][1]), 
				      Arrays.asList(DatasetFactory.createFromObject((boolean[]) new boolean[1]),
				    		  		DatasetFactory.createFromObject((boolean[]) new boolean[1])), false);
		image.setGlobalRange(getGlobalRange());
		image.setAlpha(0);
		system.addTrace(image);
	}
	
	
	private double[] getGlobalRange(){
		IAxis systemYAxis = system.getAxes().get(1);
		IAxis systemXAxis = system.getAxes().get(0);
		
		return new double[]{systemXAxis.getLower(), systemXAxis.getUpper(), 
                systemYAxis.getUpper(), systemYAxis.getLower()}; // Assuming y axis is inverted.
	}
	
	
	private void addRegion(IRegion region, IROI roi, Color colour){
		region.setROI(roi);
		region.setMobile(false);
		region.setActive(false);
		region.setUserRegion(false);
		region.setRegionColor(colour);
		system.addRegion(region);
	}
	
	
	private void rescalePlot(){
		if(system.isRescale()){
			IAxis yAxis = system.getAxes().get(1);
			yAxis.setInverted(true);
			IAxis xAxis = system.getAxes().get(0);
			
			double maxX = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;
			
			// The regions to take into account when scaling the plot
			List<IRegion> regions = Arrays.asList(system.getRegion(DETECTOR_REGION), 
					                              system.getRegion(CAMERA_TUBE_REGION));
			
			for(IRegion region : regions){
				if(region == null) continue;
				IROI roi = region.getROI();
				if(roi != null){
					IRectangularROI bounds = roi.getBounds();
					maxX = Math.max(maxX, bounds.getPointX() + bounds.getLength(0));
					maxY = Math.max(maxY, bounds.getPointY() + bounds.getLength(1));
					minX = Math.min(minX,  bounds.getPointX());
					minY = Math.min(minY, bounds.getPointY());
				}
			}
			
			if(maxX == Double.MIN_VALUE || minX == Double.MAX_VALUE || maxY == Double.MIN_VALUE || minY == Double.MAX_VALUE){
				maxX = 100; minX = -100; maxY = 100; minY = -100;
			}
				
			
			double length = Math.max(maxX-minX, maxY-minY);
			yAxis.setRange(minY + length, minY);
			xAxis.setRange(minX, minX + length);
		}
	}


	protected abstract double getDetectorTopLeftX();
	
	protected abstract double getDetectorTopLeftY();
	
	protected abstract double getHorizontalLengthFromMM(double lengthMM);
	
	protected abstract double getHorizontalLengthFromPixels(double lengthPixels);

	protected abstract double getVerticalLengthFromMM(double lengthMM);
	
	protected abstract double getVerticalLengthFromPixels(double lengthPixels);

	
	private double getDetectorWidth(){
		return getHorizontalLengthFromMM(beamlineConfiguration.getDetectorWidthMM());
	}
	
	
	private double getDetectorHeight(){
		return getVerticalLengthFromMM(beamlineConfiguration.getDetectorHeightMM());
	}
	
	
	private double getClearanceMajor(){
		return getHorizontalLengthFromPixels(beamlineConfiguration.getClearance());
	}
	
	
	private double getClearanceMinor(){
		return getVerticalLengthFromPixels(beamlineConfiguration.getClearance());
	}
	
	
	private double getBeamstopMajor(){
		return getHorizontalLengthFromMM(beamlineConfiguration.getBeamstop().getRadiusMM());
	}
	
	
	private double getBeamstopMinor(){
		return getVerticalLengthFromMM(beamlineConfiguration.getBeamstop().getRadiusMM());
	}
	
	
	private double getBeamstopCentreX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(beamlineConfiguration.getBeamstopXCentreMM());
	}
	
	
	private double getBeamstopCentreY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(beamlineConfiguration.getBeamstopYCentreMM());
	}
	
	
	private double getCameraTubeMajor(){
		return getHorizontalLengthFromMM(beamlineConfiguration.getCameraTube().getRadiusMM());
	}
	
	
	private double getCameraTubeMinor(){
		return getVerticalLengthFromMM(beamlineConfiguration.getCameraTube().getRadiusMM());
	}
	
	
	private double getCameraTubeCentreX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(beamlineConfiguration.getCameraTubeXCentreMM());
	}
	
	private double getCameraTubeCentreY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(beamlineConfiguration.getCameraTubeYCentreMM());
	}
	
	
	private double getVisibleRangeStartPointX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(resultsController.getVisibleRangeStartPoint().x);
	}
	
	
	private double getVisibleRangeStartPointY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(resultsController.getVisibleRangeStartPoint().y);
	}
	
	
	private double getVisibleRangeEndPointX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(resultsController.getVisibleRangeEndPoint().x);
	}
	
	
	private double getVisibleRangeEndPointY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(resultsController.getVisibleRangeEndPoint().y);
	}
	
	
	private double getRequestedRangeStartPointX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(resultsController.getRequestedRangeStartPoint().x);
	}
	
	
	private double getRequestedRangeStartPointY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(resultsController.getRequestedRangeStartPoint().y);
	}
	
	
	private double getRequestedRangeEndPointX(){
		return getDetectorTopLeftX() + getHorizontalLengthFromMM(resultsController.getRequestedRangeEndPoint().x);
	}
	
	
	private double getRequestedRangeEndPointY(){
		return getDetectorTopLeftY() + getVerticalLengthFromMM(resultsController.getRequestedRangeEndPoint().y);
	}
	
	
	private double getCalibrantRingMajor(Q q){
		return getHorizontalLengthFromMM(1.0e3*BeamlineConfigurationUtil.calculateDistanceFromQValue(q.getValue().to(Q.BASE_UNIT).getEstimatedValue(), 
                beamlineConfiguration.getCameraLength(), beamlineConfiguration.getWavelength())); 
	}
	
	
	private double getCalibrantRingMinor(Q q){
		return getVerticalLengthFromMM(1.0e3*BeamlineConfigurationUtil.calculateDistanceFromQValue(q.getValue().to(Q.BASE_UNIT).getEstimatedValue(), 
                beamlineConfiguration.getCameraLength(), beamlineConfiguration.getWavelength()));
	}
	
	
	private void removeRegion(String name){
		IRegion region = system.getRegion(name);
		if(region != null) system.removeRegion(region);
	}
	
	
	private void removeTrace(String name){
		ITrace trace = system.getTrace(name);
		if(trace != null) system.removeTrace(trace);
	}
	
	
	private void removeRegions(String[] names){
		for(int i = 0; i < names.length; i++) removeRegion(names[i]);
	}
	
	
	private void removeRegions(List<IRegion> regions){
		for(IRegion region : regions)
			if(region != null) system.removeRegion(region);
	}
}

