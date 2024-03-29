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
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.BooleanDataset;
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
	
	private static final String INNER_RANGE = "Inner Visible Range";
	private static final String OUTER_RANGE = "Outer Visible Range";
	private static final String INACCESSIBLE_RANGE = "Inaccessible Range";
	private static final String REQUESTED_RANGE = "Requested Range";
	private static final String VISIBLE_RANGE = "Visible Range";

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
	public void createPlot() {
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
		
		if(beamlineConfiguration.getWavelength() != null && beamlineConfiguration.getCameraLength() != null && 
		   selectedCalibrant != null && context.isCalibrantPlot())
			createCalibrantRings();
		else removeRegions(calibrantRingRegions);
		
		if(context.isMaskPlot()) createMask();
		else removeTrace(MASK_TRACE);

		if(beamlineConfiguration.getBeamstop() != null && beamlineConfiguration.getDetector() != null && 
				   beamlineConfiguration.getAngle() != null && context.isRayPlot()) 
					createRay();
		else {
			removeTrace(INNER_RANGE);
			removeTrace(OUTER_RANGE);
			removeTrace(INACCESSIBLE_RANGE);
			removeTrace(REQUESTED_RANGE);
			removeTrace(VISIBLE_RANGE);
		};
		
		createEmptyTrace();
		rescalePlot();
	}

	private void createDetectorRegion() {
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
		int lineWidth = 6;
		removeTrace(INNER_RANGE);
		removeTrace(OUTER_RANGE);
		removeTrace(INACCESSIBLE_RANGE);
		removeTrace(REQUESTED_RANGE);
		removeTrace(VISIBLE_RANGE);
		
		ILineTrace visibleRange1;
		ILineTrace visibleRange2;
		ILineTrace inaccessibleRange;
		ILineTrace requestedRange;
		
		Vector2d visibleRangeStartPoint = resultsController.getVisibleRangeStartPoint();
		Vector2d visibleRangeEndPoint = resultsController.getVisibleRangeEndPoint();
		Vector2d requestedRangeStartPoint = resultsController.getRequestedRangeStartPoint();
		Vector2d requestedRangeEndPoint = resultsController.getRequestedRangeEndPoint();
		
		if(visibleRangeStartPoint == null || visibleRangeEndPoint == null) return;
		
		try {
			visibleRange1 = system.createLineTrace(INNER_RANGE);
			visibleRange2 = system.createLineTrace(OUTER_RANGE);
			inaccessibleRange = system.createLineTrace(INACCESSIBLE_RANGE);
			requestedRange = system.createLineTrace(REQUESTED_RANGE);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		inaccessibleRange.setData(createCoordinatePair(getBeamstopCentreX(), getVisibleRangeStartPointX()), 
										createCoordinatePair(getBeamstopCentreY(), getVisibleRangeStartPointY()));
		
		inaccessibleRange.setLineWidth(lineWidth);
		inaccessibleRange.setTraceColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		system.addTrace(inaccessibleRange);
		
		
		if(!resultsController.getIsSatisfied() || requestedRangeStartPoint == null || requestedRangeEndPoint == null){
			visibleRange1.setData(createCoordinatePair(getVisibleRangeStartPointX(), getVisibleRangeEndPointX()), 
								  createCoordinatePair(getVisibleRangeStartPointY(), getVisibleRangeEndPointY()));
			
			visibleRange1.setLineWidth(lineWidth);
			visibleRange1.setName(VISIBLE_RANGE);
			visibleRange1.setTraceColor(new Color(Display.getDefault(), 205, 133, 63));
			system.addTrace(visibleRange1);
			
		} else {
			visibleRange1.setData(createCoordinatePair(getVisibleRangeStartPointX(), getRequestedRangeStartPointX()), 
					  createCoordinatePair(getVisibleRangeStartPointY(), getRequestedRangeStartPointY()));
			
			visibleRange1.setLineWidth(lineWidth);
			visibleRange1.setTraceColor(new Color(Display.getDefault(), 205, 133, 63));
			system.addTrace(visibleRange1);
			
			visibleRange2.setData(createCoordinatePair(getRequestedRangeEndPointX(), getVisibleRangeEndPointX()), 
								  createCoordinatePair(getRequestedRangeEndPointY(), getVisibleRangeEndPointY()));
			
			visibleRange2.setLineWidth(lineWidth);
			visibleRange2.setTraceColor(new Color(Display.getDefault(), 205, 133, 63));
			system.addTrace(visibleRange2);
			
			requestedRange.setData(createCoordinatePair(getRequestedRangeStartPointX(), getRequestedRangeEndPointX()), 
								   createCoordinatePair(getRequestedRangeStartPointY(), getRequestedRangeEndPointY()));
			
			requestedRange.setLineWidth(lineWidth);
			requestedRange.setTraceColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
			system.addTrace(requestedRange);
		}
	}
	
	
	private Dataset createCoordinatePair(double pointA, double pointB) {
		return DatasetFactory.createFromObject(new double[] {pointA, pointB});
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
			maskCache.clear();
			mask = DatasetFactory.ones(BooleanDataset.class, detectorHeight, detectorWidth);
			
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
		
		Dataset xAxis = DatasetFactory.createRange(getDetectorTopLeftX(), getDetectorTopLeftX() + getHorizontalLengthFromPixels(detectorWidth), getHorizontalLengthFromPixels(1));
		Dataset yAxis = DatasetFactory.createRange(getDetectorTopLeftY(),getDetectorTopLeftY() + getVerticalLengthFromPixels(detectorHeight), getVerticalLengthFromPixels(1));
				
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
			
			double maxX = Double.NEGATIVE_INFINITY;
			double minX = Double.POSITIVE_INFINITY;
			double maxY = Double.NEGATIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY;
			
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
			
			if (Double.isInfinite(maxX) || Double.isInfinite(minX) || Double.isInfinite(maxY) || Double.isInfinite(minY)){
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

	private double getCalibrantRingMajor(Q q) {
		return getHorizontalLengthFromMM(1.0e3 * BeamlineConfigurationUtil.calculateDistanceFromQValue(
				q.getValue().to(Q.BASE_UNIT).getValue().doubleValue(), beamlineConfiguration.getCameraLength(),
				beamlineConfiguration.getWavelength()));
	}

	private double getCalibrantRingMinor(Q q) {
		return getVerticalLengthFromMM(1.0e3 * BeamlineConfigurationUtil.calculateDistanceFromQValue(
				q.getValue().to(Q.BASE_UNIT).getValue().doubleValue(), beamlineConfiguration.getCameraLength(),
				beamlineConfiguration.getWavelength()));
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

