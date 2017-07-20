package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.CsdpGeneratorFromDrm;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.CurveStitchWithErrorsAndFrames;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessWithFrames;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.ReflectivityNormalisation;
import org.dawnsci.surfacescatter.TrackerLocationInterpolation;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class TrackingHandlerWithFramesImproved {
	
	private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private SurfaceScatterViewStart ssvs;
	private int correctionSelection;
	private int noImages;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 0;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortView tpaav;
	private Thread t;
	private trackingJob21Improved tj;
	private ArrayList<FrameModel> fms;
	private DirectoryModel drm;

	public Thread getT() {
		if(tj != null){
			t = tj.getT();
		}
		
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public TrackingProgressAndAbortView getTPAAV() {
		return tpaav;
	}

	public void setTPAAV(TrackingProgressAndAbortView tpaav) {
		this.tpaav = tpaav;
	}

	public SurfaceScatterViewStart getSsvs() {
		return ssvs;
	}
	
	public void setProgress(ProgressBar progress){
		this.progressBar = progress;
	}

	public void setSsvs(SurfaceScatterViewStart ssvs) {
		this.ssvs = ssvs;
	}
	
	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
		this.outputCurves = outputCurves;
	}

	public void setCorrectionSelection(int cS) {
		this.correctionSelection = cS;
	}

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}


	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	

	@SuppressWarnings("unchecked")
	
	protected  void runTJ1(){

		this.gm = ssp.getGm();
		this.fms = ssp.getFms();
		this.drm = ssp.getDrm();
		
		drm.resetAll();

		int startFrame = ssp.getSliderPos();
		
		final Display display = Display.getCurrent();
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(drm.getFilepathsSortedArray());
		
		int[] len = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds().getIntLengths();
		int[] pt = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds().getIntPoint();

		int[][] lenPt = { len, pt };
		
		ssp.regionOfInterestSetter(lenPt);
		


			trackingJob21Improved tj = new trackingJob21Improved();
			debug("tj2 invoked");
			tj.setProgress(progressBar);
			tj.setCorrectionSelection(MethodSetting.toInt(drm.getCorrectionSelection()));
			tj.setGm(gm);
			tj.setOutputCurves(outputCurves);
			tj.setSsp(ssp);
			tj.setSsvs(ssvs);
			tj.setTPAAV(tpaav);
			tj.setDrm(drm);
			tj.setFms(fms);
			tj.runTJ2();
//		}
		
		ssvs.getCustomComposite().getReplay().setEnabled(true);

	}
		private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
		
		
		public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
			
			ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
			ssp.sliderMovemementMainImage(imageNumber);
			ssvs.updateIndicators(imageNumber);
			ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage, null, null);
			
			ArrayList<IDataset>  kl =new ArrayList<>();
			
			try{
				kl = drm.getBackgroundDatArray();
				ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(kl.get(imageNumber), null, null);
			}
			catch(Exception t){
				System.out.println(t.getMessage());
			}
			
			ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
			ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
			ssvs.getSsps3c().generalUpdate();
			ssp.stitchAndPresentWithFrames(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());
			
			double[] location = ssp.getThisLocation(imageNumber);
			
			int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
			int[] pt = new int[] {(int) location[0],(int) location[1]};
			int[][] lenPt = { len, pt };
			
			RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt);
			
			ssvs.getPlotSystemCompositeView().getIRegion().setROI(greenAndBg[0]);
			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(greenAndBg[1]);
			
			if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
				ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(ssp.generateOffsetBgROI(lenPt));
			}
			
			ssvs.getSsps3c().generalUpdate(lenPt);
			
			ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();
			
			if(progressBar.isDisposed() != true){
				progressBar.setSelection(progressBar.getSelection() +1);
				
				if(progressBar.getSelection() == progressBar.getMaximum()){
					tpaav.close();
				}
			}
			

			CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
			
			csdpgfd.generateCsdpFromDrm(drm);
			
			CurveStitchDataPackage csdp = csdpgfd.getCsdp();
			
			csdp.setRodName("Current Track");
			
			CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);
			
			ssvs.getRaw().getRtc().addCurrentTrace(csdp);
			
			
		}
}

/////////////////////////////////////////////////////////////////////
///////////////////// Tracking Job2////////////////////////////////////
/////////////////////////////////////////////////////////////////////

class trackingJob21Improved {

	
	private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private int correctionSelection;
	private int noImages;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private int DEBUG =0;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortView tpaav;
	private Thread t;
	private ArrayList<FrameModel> fms;
	
	public ArrayList<FrameModel> getFms() {
		return fms;
	}

	public void setFms(ArrayList<FrameModel> fms) {
		this.fms = fms;
	}

	public DirectoryModel getDrm() {
		return drm;
	}

	public void setDrm(DirectoryModel drm) {
		this.drm = drm;
	}

	private DirectoryModel drm;

	public Thread getT() {
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public TrackingProgressAndAbortView getTPAAV() {
		return tpaav;
	}

	public void setTPAAV(TrackingProgressAndAbortView tpaav) {
		this.tpaav = tpaav;
	}

	public void setProgress(ProgressBar progress){
		this.progressBar = progress;
	}

	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
		this.outputCurves = outputCurves;
	}

	public void setCorrectionSelection(int cS) {
		this.correctionSelection = cS;
	}

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	
	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setSsvs (SurfaceScatterViewStart ssvs) {
		this.ssvs =  ssvs;
	}
		
	protected void runTJ2() {

		final Display display = Display.getCurrent();
	
		debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the new tracker~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");
		drm.resetAll();
		
		noImages = fms.size();

		int startFrame = ssp.getSliderPos();
		
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(drm.getFilepathsSortedArray());

		int[][] lenPt = ssp.getInitialLenPt();
		
		ssp.regionOfInterestSetter(lenPt);
		
		outputCurves.clear();

		String[] doneArray = new String[drm.getDatFilepaths().length];
		
		drm.setDoneArray(doneArray);

		t  = new Thread(){
				
			@Override
			public void run(){
			

		//////////////////////////// continuing to next
		//////////////////////////// dat////////////////////////////////////////

				while (!ClosestNoFinder.full(doneArray, "done")) {
		
					debug("in the while loop");
		
					int nextk = ClosestNoFinder.closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()),
																	 drm.getSortedX(), 
																	 doneArray, 
																	 drm.getFilepathsSortedArray());
		
					int nextjok = drm.getFilepathsSortedArray()[nextk];
		
					debug("nextk :" + nextk);
					debug("nextjok :" + nextjok);
					debug("doneArray[nextjok]: " + doneArray[nextjok]);
					
					for (int k = (nextk); k >= 0; k--) {
		
							if(t.isInterrupted()){
								break;
							}
							
							FrameModel frame = fms.get(k);
							
							if (frame.getDatNo() == nextjok) {
		
								debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
								+ " , " + "local nextjok:  " + Integer.toString(nextjok));
										
								int trackingMarker = 1;
								
								boolean seedRequired = true;
								
								if(k==startFrame){
									seedRequired = false;
								}
								
								for(double[] o : drm.getLocationList().get(frame.getDatNo())){
									if(!Arrays.equals(o, new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0})){
										seedRequired = false;
									}	
								}
								
								if(seedRequired && 
								   frame.getTrackingMethodology() != TrackerType1.INTERPOLATION &&
								   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION &&
								   frame.getTrackingMethodology() != TrackerType1.USE_SET_POSITIONS &&
								   drm.isTrackerOn()) {
									
									double[] test = new double[] {0,0,0,0,0,0,0,0};
									double[] test2 = new double[] {10,10,60,10,10,60,60,60};
									double myNum = drm.getSortedX().getDouble(k);
									double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
									int nearestCompletedDatFileNo = 0;
									
									for(int c = 0; c < drm.getSortedX().getSize(); c++){
									   FrameModel fm = fms.get(c);
									   double cdistance =  Math.abs(fm.getScannedVariable()- myNum);
									    if((cdistance < distance) & 
									       !Arrays.equals(fm.getRoiLocation(), test) & 
									       !Arrays.equals(fm.getRoiLocation(), test2) &
									       !Arrays.equals(fm.getRoiLocation(), null)){
									        
									    	nearestCompletedDatFileNo = fm.getDatNo();
									        distance = cdistance;
									    }
									}
			
									ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);;
									ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
									
									double[] seedLocation =null;
									Dataset yValues = DatasetFactory.zeros(new int[] {1});
									Dataset xValues = DatasetFactory.zeros(new int[] {1});
									Dataset lValues = DatasetFactory.zeros(new int[] {1});
									
									try{
										yValues = DatasetFactory.zeros(seedList.size());
										xValues = DatasetFactory.zeros(seedList.size());
										lValues = DatasetFactory.zeros(seedList.size());
									}
									
									catch(Exception r){
										
										boolean f = true;
										
										while(f){
											
											for(ArrayList<double[]> kr : drm.getLocationList()){
												if(kr != null){
													
													seedList = kr;
													
													yValues = DatasetFactory.zeros(seedList.size());
													xValues = DatasetFactory.zeros(seedList.size());
													lValues = DatasetFactory.zeros(seedList.size());
													
													f = false;
												}
											}
										}
									}
									
									for(int op = 0; op<seedList.size(); op++){
											
											double x = seedList.get(op)[1];
											double y = seedList.get(op)[0];
											double l = lList.get(op);
											
											if(x!=0.0 && y!=0.0){
												xValues.set(x, op);
												yValues.set(y, op);
												lValues.set(l, op);
											}
					
									}
										
									seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																									   lValues, 
																									   xValues, 
																									   yValues, 
																									   drm.getInitialLenPt()[0],
																									   1);
									drm.addSeedLocation(frame.getDatNo(),seedLocation);
										
									debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
									
								}	
								
								else if(frame.getTrackingMethodology() == TrackerType1.INTERPOLATION ||
									    frame.getTrackingMethodology() == TrackerType1.SPLINE_INTERPOLATION){
									
									int[] len = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1])};
									int[]  pt = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1])};
									
									double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
											(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
											(double) (pt[1] + len[1]) };
									
									debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
									
									drm.addSeedLocation(frame.getDatNo(),seedLocation);
								}
								
								
								if(k == startFrame){
									int[][] lenPt = ssp.getInitialLenPt();
									double[] seedLocation = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
									drm.addSeedLocation(frame.getDatNo(),seedLocation);
								}
								
								drm.addDmxList(frame.getDatNo(),  
										   	   frame.getNoInOriginalDat(),
										   	   frame.getScannedVariable());
							
								
								drm.addxList(fms.size(), k,
											 drm.getSortedX().getDouble(k));
								
								debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
								
								double[] gv =  drm.getSeedLocation()[frame.getDatNo()];
								int[][] gvLenPt  =new int[2][];
								
								if (frame.getTrackingMethodology() != TrackerType1.USE_SET_POSITIONS){
								
									gvLenPt = LocationLenPtConverterUtils.locationToLenPtConverter(gv);
								}
								else{
									gvLenPt =  LocationLenPtConverterUtils.locationToLenPtConverter(frame.getRoiLocation());
								}
								int[] g = fms.get(0).getRawImageData().squeezeEnds().getShape();
									
								if((gvLenPt[0][0] + gvLenPt[1][0])>g[0]){
									
										int x = (gvLenPt[0][0] + gvLenPt[1][0])-g[0];
										
										gvLenPt[1][0] -= x+1;
								}
								
								if((gvLenPt[0][1] + gvLenPt[1][1])>g[1]){
										int y = (gvLenPt[0][0] + gvLenPt[1][0])-g[0];
										
										gvLenPt[1][1] -= y+1;
								}
								
								gv = LocationLenPtConverterUtils.lenPtToLocationConverter(gvLenPt);
								
								IDataset output1 = 
										DummyProcessWithFrames.DummyProcess1(drm, 
																			 gm, 
																			 correctionSelection, 
																			 imagePosInOriginalDat[k], 
																			 trackingMarker, 
																			 k,
																			 gv);
								
								if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
									Display d =Display.getCurrent();
									debug("Dummy Proccessing failure");
									ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
									
									break;
								}
								
								
								drm.addBackgroundDatArray(fms.size(), k, output1);
								
								
								int imageNumber =k;
								IDataset tempImage = ssp.getImage(imageNumber);
								double[] tempLoc = drm.getLocationList().get(frame.getDatNo()).get(frame.getNoInOriginalDat());
								RectangularROI newROI = new RectangularROI(tempLoc[0],
									       tempLoc[1],
									       drm.getInitialLenPt()[0][0],
									       drm.getInitialLenPt()[0][1],0);
								
								display.syncExec(new Runnable() {
									@Override
									public void run() {	
										
										updateTrackingDisplay(tempImage, imageNumber);
										return;
									}
								});
								
							}
						}
						
						drm.getInputForEachDat()[nextjok] = null;
						
						if(nextjok != noImages-1){
							for (int k = nextjok+1; 
							     k < noImages; 
							     k++) {
								
								if(k ==28){
									
									System.out.println("break");
									
								}
								
								if(t.isInterrupted()){
									break;
								}
								
								FrameModel frame = fms.get(k);
								
								debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  startFrame + "??????????????????" );
								if (frame.getDatNo() == nextjok) {
			
									debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
									
									
									int trackingMarker = 2;
									
									SliceND slice = new SliceND(frame.getRawImageData().getShape());
									IDataset j = DatasetFactory.createFromObject(0);
									try {
										j = frame.getRawImageData().getSlice(slice);
									} catch (DatasetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
									}
		
									
									
									boolean seedRequired = true;
									
									for(double[] o : drm.getLocationList().get(frame.getDatNo())){
										if(!Arrays.equals(o, new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0})){
											seedRequired = false;
										}
										
									}
									
									if(seedRequired && 
									   frame.getTrackingMethodology()!= TrackerType1.INTERPOLATION &&
									   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION &&
									   frame.getTrackingMethodology()!= TrackerType1.USE_SET_POSITIONS
									   ){
										
										double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(drm.getTrackerLocationList(), 
																										   drm.getSortedX(), 
																										   drm.getInitialLenPt()[0],
																										   k);
										drm.addSeedLocation(frame.getDatNo(),seedLocation);
									}
								
										
									
									else if(frame.getTrackingMethodology() == TrackerType1.INTERPOLATION ||
											frame.getTrackingMethodology() == TrackerType1.SPLINE_INTERPOLATION){
										
										int[] len = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1])};
										int[]  pt = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1])};
										
										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
												(double) (pt[1] + len[1]) };
										
										drm.addSeedLocation(frame.getDatNo(),seedLocation);
									}
									
									
									drm.addDmxList(frame.getDatNo(),  
											   frame.getNoInOriginalDat(),
											   frame.getScannedVariable());
					
								
									IDataset output1 = 
											DummyProcessWithFrames.DummyProcess1(drm, 
																			   gm, 
																			   correctionSelection, 
																			   imagePosInOriginalDat[k], 
																			   trackingMarker, 
																			   k,
																			   drm.getSeedLocation()[frame.getDatNo()]);
			
									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
										Display d =Display.getCurrent();
										debug("Dummy Proccessing failure");
										ssp.boundariesWarning(Integer.toString(k),d);
										
										break;
									}
									
									
									drm.addBackgroundDatArray(fms.size(), k, output1);
									
									int imageNumber =k;
									IDataset tempImage = j;
		
									display.syncExec(new Runnable() {
										@Override
										public void run() {	
												
											updateTrackingDisplay(tempImage, imageNumber);
											
											return;
										}
									});
			
								}
							}
						}
						//////bottom of k++ loop	
						doneArray[nextjok] = "done";
					}
				
				if(drm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction ||
						   
					drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction ||
					drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){
							
					ReflectivityNormalisation.ReflectivityNormalisation1(drm.getCsdp());
							
					display.syncExec(new Runnable() {
							@Override
							public void run() {	
											
									try {
										ssvs.getSsps3c().generalUpdate(lenPt);
										ssvs.getSsps3c().getOutputCurves().getPlotSystem().repaint();
										ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());
											
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
									}
										
									return;
								}
						});	
				}
				
				return;
		}
				
		};
		t.start();
		
	
		
		
		
		
		ssvs.getCustomComposite().getReplay().setEnabled(true);
	}
	
	public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
		

		ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
		ssp.sliderMovemementMainImage(imageNumber);
		ssvs.updateIndicators(imageNumber);
		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage.squeeze(), null, null);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(drm.getFms().get(imageNumber).getBackgroundSubtractedImage(), null, null);
		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
		ssvs.getSsps3c().generalUpdate();
		
		try{
			if(drm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction ||
					   drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction ||
					   drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){
						
						ReflectivityNormalisation.ReflectivityNormalisation1(drm.getCsdp());	
			}
		}
		catch(Exception h){
			
		}
		
		ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());
		
		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
		
		csdpgfd.generateCsdpFromDrm(drm);
		
		CurveStitchDataPackage csdp = csdpgfd.getCsdp();
		
		csdp.setRodName("Current Track");
		
		CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);
		
		ssvs.getRaw().getRtc().addCurrentTrace(csdp);
		
		double[] location = ssp.getThisLocation(imageNumber);
		
		int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
		int[] pt = new int[] {(int) location[0],(int) location[1]};
		int[][] lenPt = { len, pt };
		
		int[][] lenPt1= LocationLenPtConverterUtils.locationToLenPtConverter(location);
		
		RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt1);
		
		ssvs.getPlotSystemCompositeView().getIRegion().setROI(greenAndBg[0]);
		ssvs.getPlotSystemCompositeView().getBgRegion().setROI(greenAndBg[1]);
		
		
		if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
		}
		
		ssvs.getSsps3c().generalUpdate(lenPt);

		ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();

		
		if(progressBar.isDisposed() != true){
			progressBar.setSelection(progressBar.getSelection() +1);

			if(progressBar.getSelection() == progressBar.getMaximum()){
				tpaav.close();
			}
		}	
	}

	private void debug (String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
}
	
	

