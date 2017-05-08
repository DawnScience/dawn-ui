package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;
import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.TrackerLocationInterpolation;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
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

public class TrackingHandlerWithFrames {
	
	
	
	private ArrayList<DataModel> dms;
	private ArrayList<ExampleModel> models;
	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private SuperModel sm;
	private SurfaceScatterViewStart ssvs;
	private int correctionSelection;
	private int noImages;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 0;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortView tpaav;
	private Thread t;
	private trackingJob21 tj;
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

	public void setDms(ArrayList<DataModel> dms) {
		this.dms = dms;
	}

	public void setModels(ArrayList<ExampleModel> models) {
		this.models = models;
	}

	public void setSuperModel(SuperModel sm) {
		this.sm = sm;
	}

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

//	public void setTimeStep(int timeStep) {
//		this.timeStep = timeStep;
//	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setps(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	@SuppressWarnings("unchecked")
	
	protected  void runTJ1(){

		this.sm = ssp.getSm();
		this.dms = ssp.getDms();
		this.gm = ssp.getGm();
		this.models = ssp.getModels();
		this.fms = ssp.getFms();
		this.drm = ssp.getDrm();
		
		
		sm.resetAll();
		sm.setStartFrame(sm.getSliderPos());
		
		for(ExampleModel em: models){
			em.setInput(null);
		}
		
		for(DataModel dm :dms){
			dm.resetAll();
		}
		
		sm.setLocationList(null);
		final Display display = Display.getCurrent();
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
		
		ssp.regionOfInterestSetter(ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI());
		
		if (models.get(sm.getSelection()).getMethodology() != AnalaysisMethodologies.Methodology.TWOD_TRACKING &&
				sm.getTrackerOn() != true) {

			noImages = sm.getImages().length;
				
			for (DataModel dm : dms) {
				dm.resetAll();
			}
			outputCurves.clear();
			
			if (sm.getStartFrame() == 0) {
												
					t  = new Thread(){
						@Override
						public void run(){
									
								for (int k = 0; k < noImages; k++) {
										
									if(t.isInterrupted()){
										break;
									}
									
									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
							
									int trackingMarker = 0;
									int imageNumber =k;
									
									FrameModel frame = fms.get(k);
									
//									IDataset j = ssp.getImage(imageNumber);
									
									SliceND slice = new SliceND(frame.getRawImageData().getShape());
									IDataset j = DatasetFactory.createFromObject(0);
									try {
										j = frame.getRawImageData().getSlice(slice);
									} catch (DatasetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
									}
									
									int jok = frame.getDatNo();
									DataModel dm = dms.get(jok);

									ExampleModel model = models.get(jok);

									dm.addxList(frame.getScannedVariable());
									
									sm.addxList(sm.getImages().length, k,
											frame.getScannedVariable());
									
									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																						 j,
																						 model, 
																						 dm, 
																						 gm, 
																						 correctionSelection, 
																						 imagePosInOriginalDat[imageNumber], 
																						 trackingMarker, 
																						 imageNumber);
									
									if(Arrays.equals(output1.getShape(),(new int[] {2,2}))){
										ssp.boundariesWarning();

									}
								
									sm.addBackgroundDatArray(sm.getImages().length, imageNumber, output1);
									IDataset tempImage = ssp.getImage(imageNumber);
									double[] tempLoc = sm.getLocationList().get(imageNumber);
									
									int[] sml =  sm.getInitialLenPt()[0];
									sm.setSliderPos(imageNumber);
									RectangularROI newROI = new RectangularROI(tempLoc[0],
																		       tempLoc[1],
																		       sm.getInitialLenPt()[0][0],
																		       sm.getInitialLenPt()[0][1],0);
									
						display.syncExec(new Runnable() {
							@Override
							public void run() {	
								updateTrackingDisplay(tempImage, imageNumber);
								return;
								}
							});
								
						}
								//////bottom of k++ loop
								return;
					};

				}; ////Starts  the thread
					
					t.start();			
			}
			
		

			else if (sm.getStartFrame() != 0) {

				//////////////////////// inside second loop
				//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////

				t  = new Thread(){
					
					public void run(){
				
						int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
						
						for (int k = (sm.getStartFrame()); k >= 0; k--) {
	
							if(t.isInterrupted()){
								break;
							}
							
//						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
						
						int trackingMarker = 1;
						int imageNumber =k;
						IDataset j = ssp.getImage(k);
						int jok = sm.getFilepathsSortedArray()[k];
						DataModel dm = dms.get(jok);
//						GeometricParametersModel gm = gms.get(jok);
						ExampleModel model = models.get(jok);
	
						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
								sm.getSortedX().getDouble(k));
						
						sm.addxList(sm.getImages().length, k,
								sm.getSortedX().getDouble(k));
						
						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																			 j, 
																			 model, 
																			 dm, 
																			 gm,
																			 correctionSelection, 
																			 imagePosInOriginalDat[k], 
																			 trackingMarker, 
																			 k);
	
						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
							debug("Dummy Proccessing failure");
							ssp.boundariesWarning();
							break;
						}
						
						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						
						IDataset tempImage = ssp.getImage(imageNumber);
						double[] tempLoc = sm.getLocationList().get(imageNumber);
						RectangularROI newROI = new RectangularROI(tempLoc[0],
							       tempLoc[1],
							       sm.getInitialLenPt()[0][0],
							       sm.getInitialLenPt()[0][1],0);						
						
						display.syncExec(new Runnable() {
							@Override
							public void run() {	
									
								updateTrackingDisplay(tempImage, imageNumber);
								return;
								}
							});		
						
					}
					
					
					if(sm.getStartFrame() != noImages-1){	
						for (int k = sm.getStartFrame()+1; k < noImages; k++) {
							
							if(t.isInterrupted()){
								break;
							}
							
							int trackingMarker = 2;
		
							IDataset j = ssp.getImage(k);
							int jok = sm.getFilepathsSortedArray()[k];
							DataModel dm = dms.get(jok);
//							GeometricParametersModel gm = gms.get(jok);
							ExampleModel model = models.get(jok);
		
							dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
									sm.getSortedX().getDouble(k));
							
							sm.addxList(sm.getImages().length, k,
									sm.getSortedX().getDouble(k));
							
							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																				 j, 
																				 model, 
																				 dm, 
																				 gm, 

//																				 plotSystem,
//																				 ssvsPS,
																				 correctionSelection, 
																				 imagePosInOriginalDat[k], 
																				 trackingMarker, 
																				 k);
		
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
	//							ssp.boundariesWarning();
								debug("Dummy Proccessing failure");
								break;
							}
							
							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
							
							int imageNumber =k;
							IDataset tempImage = ssp.getImage(imageNumber);
							double[] tempLoc = sm.getLocationList().get(imageNumber);
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       sm.getInitialLenPt()[0][0],
								       sm.getInitialLenPt()[0][1],0);
	
							
							display.syncExec(new Runnable() {
								@Override
								public void run() {	
										
									updateTrackingDisplay(tempImage, imageNumber);
									
									return;
									}
								});
							}
						}
					//////bottom of k++ loop
					return;
					}
					
				};
			t.start();
			}	
			
		}
		else {

			trackingJob21 tj = new trackingJob21();
			debug("tj2 invoked");
			tj.setProgress(progressBar);
			tj.setCorrectionSelection(MethodSetting.toInt(sm.getCorrectionSelection()));
			tj.setSuperModel(sm);
			tj.setGm(gm);
			tj.setDms(dms);
			tj.setModels(models);
			tj.setPlotSystem(plotSystem);
			tj.setOutputCurves(outputCurves);
			tj.setTimeStep(Math.round(2 / sm.getImages().length));
			tj.setSsp(ssp);
			tj.setSsvs(ssvs);
			tj.setTPAAV(tpaav);
			tj.runTJ2();
		}
		
		
		ssvs.getCustomComposite().getReplay().setEnabled(true);
//		ssvs.getOutputCurves().addImageNoRegion(ssp.getXValue((ssp.getNumberOfImages())/2));
	}
		private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
		
		
		public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
			
			ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
			ssp.sliderMovemementMainImage(imageNumber);
//			ssp.updateSliders(ssvs.getPlotSystemCompositeView().getSlider(), imageNumber);
			ssvs.updateIndicators(imageNumber);
			ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage, null, null);
			ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(sm.getBackgroundDatArray().get(imageNumber), null, null);
			ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
			ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
			ssvs.getSsps3c().generalUpdate();
			ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());
			
			double[] location = ssp.getLocationList().get((imageNumber));
			
			int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
			int[] pt = new int[] {(int) location[0],(int) location[1]};
			int[][] lenPt = { len, pt };
			
			RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt);
			
			ssvs.getPlotSystemCompositeView().getIRegion().setROI(greenAndBg[0]);
			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(greenAndBg[1]);
			
			if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
				ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
			}
			
			ssvs.getSsps3c().generalUpdate(ssp.getLenPt());
			
			ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();
			
			if(progressBar.isDisposed() != true){
				progressBar.setSelection(progressBar.getSelection() +1);
				
				if(progressBar.getSelection() == progressBar.getMaximum()){
					tpaav.close();
				}
			}
		}
}

/////////////////////////////////////////////////////////////////////
///////////////////// Tracking Job2////////////////////////////////////
/////////////////////////////////////////////////////////////////////

class trackingJob21 {

	private ArrayList<DataModel> dms;
	private ArrayList<ExampleModel> models;
	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private SuperModel sm;
	private int correctionSelection;
	private int noImages;
	private int timeStep;
	private SurfaceScatterPresenter ssp;
	private IPlottingSystem<Composite> ssvsPS; 
	private SurfaceScatterViewStart ssvs;
	private int DEBUG = 0;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortView tpaav;
	private Thread t;

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
	
	public SurfaceScatterViewStart getSsvs() {
		return ssvs;
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

	public void setDms(ArrayList<DataModel> dms) {
		this.dms = dms;
	}

	public void setModels(ArrayList<ExampleModel> models) {
		this.models = models;
	}

	public void setSuperModel(SuperModel sm) {
		this.sm = sm;
	}

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setSsvsPS (IPlottingSystem<Composite> ssvsPS) {
		this.ssvsPS = ssvsPS;
	}
		
	@SuppressWarnings("unchecked")
	protected void runTJ2() {

		final Display display = Display.getCurrent();
	
		debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the new tracker~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");
		sm.resetTrackers();
		sm.resetAll();
		
		for(ExampleModel m : models){
			m.setInput(null);
		}
		
		noImages = sm.getImages().length;

		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());

		for (DataModel dm : dms) {
			dm.resetAll();
		}

		ssp.regionOfInterestSetter(ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI());
		
		outputCurves.clear();

		int jok = sm.getFilepathsSortedArray()[sm.getStartFrame()];

		String[] doneArray = new String[sm.getFilepaths().length];
		
		if (sm.getStartFrame() == 0) {
			
			
			t  = new Thread(){
				
				@Override
				public void run(){
			
					for (int k = 0; k < noImages; k++) {
						
						if(t.isInterrupted()){
							break;
						}
						
						if (sm.getFilepathsSortedArray()[k] == jok) {
							
							debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
							+ " , " + "local jok:  " + Integer.toString(jok));
							
							debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the 0 loop~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");

							int jok = sm.getFilepathsSortedArray()[k];
							int trackingMarker = 0;
							IDataset j = ssp.getImage(k);
							DataModel dm = dms.get(jok);
//							GeometricParametersModel gm = gms.get(jok);
							ExampleModel model = models.get(jok);
		
							dm.addxList(sm.getSortedX().getDouble(k));
							
							sm.addxList(sm.getImages().length, k,
									sm.getSortedX().getDouble(k));
							
							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							debug("Tracker should fire once");
							
							IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																				 j, 
																				 model, 
																				 dm, 
																				 gm,
																				 correctionSelection, 
																				 imagePosInOriginalDat[k], 
																				 trackingMarker, 
																				 k);
							debug("Tracker should HAVE fired once");
							
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
								debug("Dummy Proccessing failure");
								Display d =Display.getCurrent();
								ssp.boundariesWarning("position 1, line ~1410, k: " + Integer.toString(k),d);
								break;
							}
							
							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
							
							int imageNumber =k;
							IDataset tempImage = ssp.getImage(imageNumber);
							double[] tempLoc = sm.getLocationList().get(imageNumber);
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       sm.getInitialLenPt()[0][0],
								       sm.getInitialLenPt()[0][1],0);
							
							
							
							display.syncExec(new Runnable() {
								@Override
								public void run() {																			
									updateTrackingDisplay(tempImage, imageNumber);
									
									return;
								}
							});
							
							
							
							
						}
					}
					//////bottom of k++ loop
					doneArray[jok] = "done";
					
					while (ClosestNoFinder.full(doneArray, "done") == false) {

						debug("in the while loop");

						int nextk = ClosestNoFinder.closestNoWithoutDone(sm.getSortedX().getDouble(sm.getSliderPos()),
								sm.getSortedX(), doneArray, sm.getFilepathsSortedArray());

						int nextjok = sm.getFilepathsSortedArray()[nextk];

						debug("nextk :" + nextk);
						debug("nextjok :" + nextjok);
						debug("doneArray[nextjok]: " + doneArray[nextjok]);
						
						if (imagePosInOriginalDat[nextk] == 0) {
				
							debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
							
							for (int k = nextk; k < noImages; k++) {

								if(t.isInterrupted()){
									break;
								}
								
								if (sm.getFilepathsSortedArray()[k] == nextjok) {
//									ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());


									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
									
									
									int trackingMarker = 0;
									IDataset j = ssp.getImage(k);
									int jokLocal = sm.getFilepathsSortedArray()[k];
									DataModel dm = dms.get(jokLocal);
//									GeometricParametersModel gm = gms.get(jokLocal);
									ExampleModel model = models.get(jokLocal);
									
									
									if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
										
										if (sm.getTrackerLocationList() == null  ){
//											| sm.getTrackerLocationList().size() <= 10
											int seedIndex = 
													ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																						  sm.getSortedX(), 
																						  sm.getLocationList());
					
											int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
											
											
											ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
											ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
											
											Dataset yValues = DatasetFactory.zeros(seedList.size());
											Dataset xValues = DatasetFactory.zeros(seedList.size());
											Dataset lValues = DatasetFactory.zeros(seedList.size());
											
											for(int op = 0; op<seedList.size(); op++){
												
												double x = seedList.get(op)[1];
												double y = seedList.get(op)[0];
												double l = lList.get(op);
												
												xValues.set(x, op);
												yValues.set(y, op);
												lValues.set(l, op);
						
											}
											
											
											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																										   lValues, 
																										   xValues, 
																										   yValues, 
																										   sm.getInitialLenPt()[0],
																										   1);
											dm.setSeedLocation(seedLocation);
											
//											debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
									
										}
										else{
											double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(sm.getTrackerLocationList(), 
																										   sm.getSortedX(), 
																										   sm.getInitialLenPt()[0],
																										   k);
											dm.setSeedLocation(seedLocation);
										}
									}	
									
									
									else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
										
										int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
										int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
										
										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
												(double) (pt[1] + len[1]) };
										
										dm.setSeedLocation(seedLocation);
									}
									
									dm.addxList(sm.getSortedX().getDouble(k));
									
									sm.addxList(sm.getImages().length, k,
											sm.getSortedX().getDouble(k));
									
									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									IDataset output1 = 
											DummyProcessingClass.DummyProcess1(sm, 
																			   j, 
																			   model, 
																			   dm, 
																			   gm, 
																			   plotSystem,
																			   ssvsPS,
																			   correctionSelection, 
																			   imagePosInOriginalDat[k], 
																			   trackingMarker, 
																			   k,
																			   dm.getSeedLocation());

									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
										debug("Dummy Proccessing failure");
										Display d =Display.getCurrent();
										ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
										
										break;
									}
									
									sm.addBackgroundDatArray(sm.getImages().length, k, output1);
									
									
									int imageNumber =k;
									IDataset tempImage = ssp.getImage(imageNumber);
									double[] tempLoc = sm.getLocationList().get(imageNumber);
									RectangularROI newROI = new RectangularROI(tempLoc[0],
										       tempLoc[1],
										       sm.getInitialLenPt()[0][0],
										       sm.getInitialLenPt()[0][1],0);
									
									display.syncExec(new Runnable() {
										@Override
										public void run() {	
									
											updateTrackingDisplay(tempImage, imageNumber);
											
											return;
										}
										});
									

								}
								doneArray[nextjok] = "done";
							}
							//////bottom of k++ loop
						}

						else if (imagePosInOriginalDat[nextk] != 0) {

							for (int k = (sm.getStartFrame()); k >= 0; k--) {

								if(t.isInterrupted()){
									break;
								}
								
								if (sm.getFilepathsSortedArray()[k] == nextjok) {

									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
									
									
									int trackingMarker = 1;
									IDataset j = ssp.getImage(k);
									DataModel dm = dms.get(nextjok);
//									GeometricParametersModel gm = gms.get(nextjok);
									ExampleModel model = models.get(nextjok);
									
									if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
										
										int seedIndex = 
												ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																					  sm.getSortedX(), 
																					  sm.getLocationList());
				
										int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
										
										
										ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
										ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
										
										Dataset yValues = DatasetFactory.zeros(seedList.size());
										Dataset xValues = DatasetFactory.zeros(seedList.size());
										Dataset lValues = DatasetFactory.zeros(seedList.size());
										
										for(int op = 0; op<seedList.size(); op++){
											
											double x = seedList.get(op)[1];
											double y = seedList.get(op)[0];
											double l = lList.get(op);
											
											xValues.set(x, op);
											yValues.set(y, op);
											lValues.set(l, op);
					
										}
										
										double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																									   lValues, 
																									   xValues, 
																									   yValues, 
																									   sm.getInitialLenPt()[0],
																									   1);
										dm.setSeedLocation(seedLocation);
										
//										debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
										
									
									}
									
									else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
										
										int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
										int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
										
										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
												(double) (pt[1] + len[1]) };
										
										dm.setSeedLocation(seedLocation);
									}
									
									dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
											sm.getSortedX().getDouble(k));
									
									
									sm.addxList(sm.getImages().length, k,
											sm.getSortedX().getDouble(k));
									
									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									IDataset output1 = 
											DummyProcessingClass.DummyProcess1(sm, 
																			   j, 
																			   model, 
																			   dm, 
																			   gm, 
																			   plotSystem,
																			   ssvsPS,
																			   correctionSelection, 
																			   imagePosInOriginalDat[k], 
																			   trackingMarker, 
																			   k,
																			   dm.getSeedLocation());

									
									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
										debug("Dummy Proccessing failure");
										Display d =Display.getCurrent();
										ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
										
										break;
									}
									
									
									sm.addBackgroundDatArray(sm.getImages().length, k, output1);
									
									
									int imageNumber =k;
									IDataset tempImage = ssp.getImage(imageNumber);
									double[] tempLoc = sm.getLocationList().get(imageNumber);
									RectangularROI newROI = new RectangularROI(tempLoc[0],
										       tempLoc[1],
										       sm.getInitialLenPt()[0][0],
										       sm.getInitialLenPt()[0][1],0);
									
									display.syncExec(new Runnable() {
										@Override
										public void run() {	
										
											updateTrackingDisplay(tempImage, imageNumber);
											
											return;
										}
										});
								}
							}

							
							models.get(nextjok).setInput(null);
							
							if(sm.getStartFrame() != noImages-1){
								for (int k = sm.getStartFrame()+1; k < noImages; k++) {
	
									if(t.isInterrupted()){
										break;
									}
									
									if (sm.getFilepathsSortedArray()[k] == nextjok) {
	
										debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
										+ " , " + "local nextjok:  " + Integer.toString(nextjok));
										
										
										int trackingMarker = 2;
										IDataset j = ssp.getImage(k);
										DataModel dm = dms.get(nextjok);
//										GeometricParametersModel gm = gms.get(nextjok);
										ExampleModel model = models.get(nextjok);
	
										
										if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
											
											int seedIndex = 
													ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																						  sm.getSortedX(), 
																						  sm.getLocationList());
					
											int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
											
											
											ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
											ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
											
											Dataset yValues = DatasetFactory.zeros(seedList.size());
											Dataset xValues = DatasetFactory.zeros(seedList.size());
											Dataset lValues = DatasetFactory.zeros(seedList.size());
											
											for(int op = 0; op<seedList.size(); op++){
												
												double x = seedList.get(op)[1];
												double y = seedList.get(op)[0];
												double l = lList.get(op);
												
												xValues.set(x, op);
												yValues.set(y, op);
												lValues.set(l, op);
						
											}
											
											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																										   lValues, 
																										   xValues, 
																										   yValues, 
																										   sm.getInitialLenPt()[0],
																										   1);
											dm.setSeedLocation(seedLocation);
										
										}	
										
										else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
											
											int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
											int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
											
											double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
													(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
													(double) (pt[1] + len[1]) };
											
											dm.setSeedLocation(seedLocation);
										}
										
										
										dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
												sm.getSortedX().getDouble(k));
										
										sm.addxList(sm.getImages().length, k,
												sm.getSortedX().getDouble(k));
										
										debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
										
										IDataset output1 = 
												DummyProcessingClass.DummyProcess1(sm, 
																				   j, 
																				   model, 
																				   dm, 
																				   gm, 
																				   plotSystem,
																				   ssvsPS,
																				   correctionSelection, 
																				   imagePosInOriginalDat[k], 
																				   trackingMarker, 
																				   k,
																				   dm.getSeedLocation());
	
										if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
											debug("Dummy Proccessing failure");
											Display d =Display.getCurrent();
											ssp.boundariesWarning("position 1, line ~2369, k: " + Integer.toString(k),d);
											
											break;
										}
										
										
										sm.addBackgroundDatArray(sm.getImages().length, k, output1);
										
										int imageNumber =k;
										IDataset tempImage = ssp.getImage(imageNumber);
										double[] tempLoc = sm.getLocationList().get(imageNumber);
										RectangularROI newROI = new RectangularROI(tempLoc[0],
											       tempLoc[1],
											       sm.getInitialLenPt()[0][0],
											       sm.getInitialLenPt()[0][1],0);
										
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
					}
					
					return;
				}
			};
			t.start();
		}

		//////////////////////// inside second loop
		//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////

		else {

			t  = new Thread(){
				
				@Override
				public void run(){
			
			
		
			for (int k = (sm.getStartFrame()); k >= 0; k--) {

				if(t.isInterrupted()){
					break;
				}
				
				if (sm.getFilepathsSortedArray()[k] == jok) {
					
					debug("switched to k--");
					debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
					+ " , " + "local jok:  " + Integer.toString(jok));
					
					int trackingMarker = 1;
					IDataset j = ssp.getImage(k);
					DataModel dm = dms.get(jok);
//					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);

					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
							sm.getSortedX().getDouble(k));
					
					
					sm.addxList(sm.getImages().length, k,
							sm.getSortedX().getDouble(k));
					
					debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
					
					IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																		 j, 
																		 model, 
																		 dm, 
																		 gm,
																		 correctionSelection, 
																		 imagePosInOriginalDat[k], 
																		 trackingMarker, 
																		 k);

					if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
						debug("Dummy Proccessing failure");

						break;	
					}
					
					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					
					int imageNumber =k;
					IDataset tempImage = ssp.getImage(imageNumber);
					double[] tempLoc = sm.getLocationList().get(imageNumber);
					RectangularROI newROI = new RectangularROI(tempLoc[0],
						       tempLoc[1],
						       sm.getInitialLenPt()[0][0],
						       sm.getInitialLenPt()[0][1],0);
					
					display.syncExec(new Runnable() {
						@Override
						public void run() {	
								
		
							updateTrackingDisplay(tempImage, imageNumber);
							return;
							}
						});
				}
			}

			debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
			
			models.get(jok).setInput(null);
			
			if(sm.getStartFrame() != noImages-1){
				
				
				
				for (int k = sm.getStartFrame(); k < noImages; k++) {
					
					if(t.isInterrupted()){
						break;
					}
	
					if (sm.getFilepathsSortedArray()[k] == jok) {
	
						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
						
						
						debug("switched to k++");
						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local jok:  " + Integer.toString(jok));
						
						int trackingMarker = 2;
						IDataset j = ssp.getImage(k);
						DataModel dm = dms.get(jok);
//						GeometricParametersModel gm = gms.get(jok);
						ExampleModel model = models.get(jok);
	
						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
								sm.getSortedX().getDouble(k));
						
						
						sm.addxList(sm.getImages().length, k,
								sm.getSortedX().getDouble(k));
						
						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess0(sm, 
																  j, 
																  model, 
																  dm,
														   		  gm, 
														   		  plotSystem,
														   		  ssvsPS,
														   		  correctionSelection, 
														   		  imagePosInOriginalDat[k], 
														   		  trackingMarker, 
														   		  k);
	
						
	
						if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
							Display d =Display.getCurrent();
							debug("Dummy Proccessing failure");
							ssp.boundariesWarning("position 1, line ~1955, k: " + Integer.toString(k),d);
						
							break;	
						}
						
						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						
						
						int imageNumber =k;
						IDataset tempImage = ssp.getImage(imageNumber);
						double[] tempLoc = sm.getLocationList().get(imageNumber);
						RectangularROI newROI = new RectangularROI(tempLoc[0],
							       tempLoc[1],
							       sm.getInitialLenPt()[0][0],
							       sm.getInitialLenPt()[0][1],0);
						
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

		doneArray[jok] = "done";

		//////////////////////////// continuing to next
		//////////////////////////// dat////////////////////////////////////////

		while (ClosestNoFinder.full(doneArray, "done") == false) {

			debug("in the while loop");

			int nextk = ClosestNoFinder.closestNoWithoutDone(sm.getSortedX().getDouble(sm.getSliderPos()),
					sm.getSortedX(), doneArray, sm.getFilepathsSortedArray());

			int nextjok = sm.getFilepathsSortedArray()[nextk];

			debug("nextk :" + nextk);
			debug("nextjok :" + nextjok);
			debug("doneArray[nextjok]: " + doneArray[nextjok]);
			
			if (imagePosInOriginalDat[nextk] == 0) {
	
				debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
				
				for (int k = nextk; k < noImages; k++) {

					if(t.isInterrupted()){
						break;
					}
					
					if (sm.getFilepathsSortedArray()[k] == nextjok) {

						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						int trackingMarker = 0;
						IDataset j = ssp.getImage(k);
						int jokLocal = sm.getFilepathsSortedArray()[k];
						DataModel dm = dms.get(jokLocal);
//						GeometricParametersModel gm = gms.get(jokLocal);
						ExampleModel model = models.get(jokLocal);
						
						
						if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
							
							if (sm.getTrackerLocationList() == null ){
								
//								| sm.getTrackerLocationList().size() <= 10 
								int seedIndex = 
										ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																			  sm.getSortedX(), 
																			  sm.getLocationList());
		
								int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
								
								
								ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
								ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
								
								Dataset yValues = DatasetFactory.zeros(seedList.size());
								Dataset xValues = DatasetFactory.zeros(seedList.size());
								Dataset lValues = DatasetFactory.zeros(seedList.size());
								
								for(int op = 0; op<seedList.size(); op++){
									
									double x = seedList.get(op)[1];
									double y = seedList.get(op)[0];
									double l = lList.get(op);
									
									xValues.set(x, op);
									yValues.set(y, op);
									lValues.set(l, op);
			
								}
								
								
								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																							   lValues, 
																							   xValues, 
																							   yValues, 
																							   sm.getInitialLenPt()[0],
																							   1);
								dm.setSeedLocation(seedLocation);
								
								debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
						
							}
							else{
								double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(sm.getTrackerLocationList(), 
																							   sm.getSortedX(), 
																							   sm.getInitialLenPt()[0],
																							   k);
								dm.setSeedLocation(seedLocation);
							}
						}	
						
						
						else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
							
							int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
							int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
							
							double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
									(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
									(double) (pt[1] + len[1]) };
							
							dm.setSeedLocation(seedLocation);
						}
						
						dm.addxList(sm.getSortedX().getDouble(k));
						
						sm.addxList(sm.getImages().length, k,
								sm.getSortedX().getDouble(k));
						
						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess1(sm, 
																   j, 
																   model, 
																   dm, 
																   gm, 
																   plotSystem,
																   ssvsPS,
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   dm.getSeedLocation());

						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
							Display d =Display.getCurrent();
							debug("Dummy Proccessing failure");
							ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
							
							break;
						}
						
						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						
						
						int imageNumber =k;
						IDataset tempImage = ssp.getImage(imageNumber);
						double[] tempLoc = sm.getLocationList().get(imageNumber);
						RectangularROI newROI = new RectangularROI(tempLoc[0],
							       tempLoc[1],
							       sm.getInitialLenPt()[0][0],
							       sm.getInitialLenPt()[0][1],0);
						
						
						display.syncExec(new Runnable() {
							@Override
							public void run() {	

								updateTrackingDisplay(tempImage, imageNumber);
								return;
							}
						});
						

					}
					doneArray[nextjok] = "done";
				}
			}

			else if (imagePosInOriginalDat[nextk] != 0) {

				debug("%%%%%%%%%%%%%%%%% switched the dat file");
				debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
				
				for (int k = (sm.getStartFrame()); k >= 0; k--) {

					if(t.isInterrupted()){
						break;
					}
					
					if (sm.getFilepathsSortedArray()[k] == nextjok) {

						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						debug("%%%%%%%%%%%%%%%%% switched the dat file");
						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
						
						
						int trackingMarker = 1;
						IDataset j = ssp.getImage(k);
						DataModel dm = dms.get(nextjok);
//						GeometricParametersModel gm = gms.get(nextjok);
						ExampleModel model = models.get(nextjok);
						
						if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
							
							int seedIndex = 
									ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																		  sm.getSortedX(), 
																		  sm.getLocationList());
	
							int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
							
							
							ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
							ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
							
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
									
									for(DataModel kr : dms){
										if(kr.getLocationList() != null){
											
											seedList = kr.getLocationList();
											
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
									
									xValues.set(x, op);
									yValues.set(y, op);
									lValues.set(l, op);
			
							}
								
							seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																							   lValues, 
																							   xValues, 
																							   yValues, 
																							   sm.getInitialLenPt()[0],
																							   1);
							dm.setSeedLocation(seedLocation);
								
							debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
							
						}	
						
						else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
							
							int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
							int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
							
							double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
									(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
									(double) (pt[1] + len[1]) };
							
							dm.setSeedLocation(seedLocation);
						}
						
						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
								sm.getSortedX().getDouble(k));
						
						
						sm.addxList(sm.getImages().length, k,
								sm.getSortedX().getDouble(k));
						
						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess1(sm, 
																   j, 
																   model, 
																   dm, 
																   gm, 
																   plotSystem,
																   ssvsPS,
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   dm.getSeedLocation());

						
						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
							Display d =Display.getCurrent();
							debug("Dummy Proccessing failure");
							ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
							
							break;
						}
						
						
						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						
						
						int imageNumber =k;
						IDataset tempImage = ssp.getImage(imageNumber);
						double[] tempLoc = sm.getLocationList().get(imageNumber);
						RectangularROI newROI = new RectangularROI(tempLoc[0],
							       tempLoc[1],
							       sm.getInitialLenPt()[0][0],
							       sm.getInitialLenPt()[0][1],0);
						
						display.syncExec(new Runnable() {
							@Override
							public void run() {	
								
								updateTrackingDisplay(tempImage, imageNumber);
								return;
							}
							});
						

					}
				}
				models.get(nextjok).setInput(null);
				if(sm.getStartFrame() != noImages-1){
					for (int k = sm.getStartFrame()+1; k < noImages; k++) {
						
						if(t.isInterrupted()){
							break;
						}
						
						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
						if (sm.getFilepathsSortedArray()[k] == nextjok) {
	
							debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
							+ " , " + "local nextjok:  " + Integer.toString(nextjok));
							
							
							int trackingMarker = 2;
							IDataset j = ssp.getImage(k);
							DataModel dm = dms.get(nextjok);
//							GeometricParametersModel gm = gms.get(nextjok);
							ExampleModel model = models.get(nextjok);
	
							
							if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
								
								int seedIndex = 
										ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																			  sm.getSortedX(), 
																			  sm.getLocationList());
		
								int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
								
								
								ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
								ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
								
								Dataset yValues = DatasetFactory.zeros(seedList.size());
								Dataset xValues = DatasetFactory.zeros(seedList.size());
								Dataset lValues = DatasetFactory.zeros(seedList.size());
								
								for(int op = 0; op<seedList.size(); op++){
									
									double x = seedList.get(op)[1];
									double y = seedList.get(op)[0];
									double l = lList.get(op);
									
									xValues.set(x, op);
									yValues.set(y, op);
									lValues.set(l, op);
			
								}
								
								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																							   lValues, 
																							   xValues, 
																							   yValues, 
																							   sm.getInitialLenPt()[0],
																							   1);
								dm.setSeedLocation(seedLocation);
							
							}	
							
							else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
								
								int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
								int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
								
								double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
										(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
										(double) (pt[1] + len[1]) };
								
								dm.setSeedLocation(seedLocation);
							}
							
							
							dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
									sm.getSortedX().getDouble(k));
							
							sm.addxList(sm.getImages().length, k,
									sm.getSortedX().getDouble(k));
							
							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							IDataset output1 = 
									DummyProcessingClass.DummyProcess1(sm, 
																	   j, 
																	   model, 
																	   dm, 
																	   gm, 
																	   plotSystem,
																	   ssvsPS,
																	   correctionSelection, 
																	   imagePosInOriginalDat[k], 
																	   trackingMarker, 
																	   k,
																	   dm.getSeedLocation());
	
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
								Display d =Display.getCurrent();
								debug("Dummy Proccessing failure");
								ssp.boundariesWarning(Integer.toString(k),d);
								
								break;
							}
							
							
							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
							
							int imageNumber =k;
							IDataset tempImage = ssp.getImage(imageNumber);
							double[] tempLoc = sm.getLocationList().get(imageNumber);
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       sm.getInitialLenPt()[0][0],
								       sm.getInitialLenPt()[0][1],0);
							
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
		}
		return;
				}
				
		};
		t.start();
		}
		
		ssvs.getCustomComposite().getReplay().setEnabled(true);
	}
	
	public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
		

		ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
		ssp.sliderMovemementMainImage(imageNumber);
//		ssp.updateSliders(ssvs.getPlotSystemCompositeView().getSlider(), imageNumber);
		ssvs.updateIndicators(imageNumber);
		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage, null, null);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(sm.getBackgroundDatArray().get(imageNumber), null, null);
		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
		ssvs.getSsps3c().generalUpdate();
		ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());

		double[] location = ssp.getLocationList().get((imageNumber));
		
		int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
		int[] pt = new int[] {(int) location[0],(int) location[1]};
		int[][] lenPt = { len, pt };
		
		
		RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt);
		
		ssvs.getPlotSystemCompositeView().getIRegion().setROI(greenAndBg[0]);
		ssvs.getPlotSystemCompositeView().getBgRegion().setROI(greenAndBg[1]);
		
		
		if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
		}
		
		ssvs.getSsps3c().generalUpdate(ssp.getLenPt());

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
	
	

