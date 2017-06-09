package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessWithFrames;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.PolynomialOverlap;
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

public class TrackingHandlerWithFrames {
	
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
		
		if (fms.get(startFrame).getBackgroundMethdology() != AnalaysisMethodologies.Methodology.TWOD_TRACKING &&
				drm.isTrackerOn() != true) {

			noImages = fms.size();
				
			outputCurves.clear();			
			
			if (startFrame  == 0) {
												
					t  = new Thread(){
						@Override
						public void run(){
									
								for (int k = 0; k < noImages; k++) {
										
									if(t.isInterrupted()){
										break;
									}
									
//									debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
							
									int trackingMarker = 0;
									int imageNumber =k;
									
									FrameModel frame = fms.get(k);
									
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

									
									drm.addxList(drm.getFms().size(), k,
											frame.getScannedVariable());
									
									drm.addDmxList(frame.getDatNo(),  
												   imagePosInOriginalDat[imageNumber],
												   frame.getScannedVariable());
									
//									debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									IDataset output1 = DummyProcessWithFrames.DummyProcess(drm, 
																						   gm, 
																						   correctionSelection, 
																						   imagePosInOriginalDat[imageNumber], 
																						   trackingMarker, 
																						   imageNumber,
																						   null);
									
									if(Arrays.equals(output1.getShape(),(new int[] {2,2}))){
										ssp.boundariesWarning();

									}
								
									drm.addBackgroundDatArray(fms.size(), imageNumber, output1);
									
									drm.getOcdp().addBackgroundDatArray(fms.size(), imageNumber, output1);

									double[] tempLoc = frame.getRoiLocation();
									int[] sml =  drm.getInitialLenPt()[0];
									

									RectangularROI newROI = new RectangularROI(tempLoc[0],
																		       tempLoc[1],
																		       drm.getInitialLenPt()[0][0],
																		       drm.getInitialLenPt()[0][1],0);
									
						display.syncExec(new Runnable() {
							@Override
							public void run() {	
								try {
									updateTrackingDisplay(frame.getRawImageData().getSlice(slice).squeeze(), imageNumber);
								} catch (DatasetException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
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
			
		

			else if (startFrame != 0) {

				//////////////////////// inside second loop
				//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////

				t  = new Thread(){
					
					public void run(){
				
						for (int k = startFrame; k >= 0; k--) {

							
							int pos = k;
							if(t.isInterrupted()){
								break;
							}
							
	//						debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
							
							int trackingMarker = 1;
							
							FrameModel frame = fms.get(k);
							
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

							drm.addxList(drm.getFms().size(), k,
									frame.getScannedVariable());
							
							drm.addDmxList(frame.getDatNo(),  
									   frame.getNoInOriginalDat(),
									   frame.getScannedVariable());
							
//							debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							IDataset output1 = DummyProcessWithFrames.DummyProcess(drm, 
									   gm, 
									   correctionSelection, 
									   imagePosInOriginalDat[k], 
									   trackingMarker, 
									   k,
									   null);
							
		
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
								debug("Dummy Proccessing failure");
								ssp.boundariesWarning();
								break;
							}
							
							drm.addBackgroundDatArray(fms.size(), k, output1);
							
							double[] tempLoc = frame.getRoiLocation();
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       drm.getInitialLenPt()[0][0],
								       drm.getInitialLenPt()[0][1],0);						
							
							display.syncExec(new Runnable() {
								@Override
								public void run() {	
											
										try {
											updateTrackingDisplay(frame.getRawImageData().getSlice(slice).squeeze(), pos);
										} catch (DatasetException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											System.out.println(e.getMessage());
										}
										
										return;
									}
								});		
							
					}
					
					
					if(startFrame != noImages-1){	
						for (int k = startFrame+1; k < noImages; k++) {
							
							int pos = k;
							
							if(t.isInterrupted()){
								break;
							}
							
							int trackingMarker = 2;
		

							FrameModel frame = fms.get(k);
							
							SliceND slice = new SliceND(frame.getRawImageData().getShape());
							IDataset j = DatasetFactory.createFromObject(0);
							try {
								j = frame.getRawImageData().getSlice(slice);
							} catch (DatasetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println(e.getMessage());
							}
							
//							int jok = frame.getDatNo();
							
							drm.addxList(drm.getFms().size(), k,
									frame.getScannedVariable());
							
							drm.addDmxList(frame.getDatNo(),  
									   frame.getNoInOriginalDat(),
									   frame.getScannedVariable());
							
							debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							IDataset output1 = DummyProcessWithFrames.DummyProcess(drm, 
									   gm, 
									   correctionSelection, 
									   imagePosInOriginalDat[k], 
									   trackingMarker, 
									   k,
									   null);
							
		
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
								debug("Dummy Proccessing failure");
								ssp.boundariesWarning();
								break;
							}
//							
							drm.addBackgroundDatArray(fms.size(), k, output1);
							
							double[] tempLoc = frame.getRoiLocation();
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       drm.getInitialLenPt()[0][0],
								       drm.getInitialLenPt()[0][1],0);						
							
							display.syncExec(new Runnable() {
								@Override
								public void run() {	
											
										try {
											updateTrackingDisplay(frame.getRawImageData().getSlice(slice).squeeze(), pos);
										} catch (DatasetException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
											System.out.println(e.getMessage());
										}
										
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
			tj.setCorrectionSelection(MethodSetting.toInt(drm.getCorrectionSelection()));
			tj.setGm(gm);
			tj.setOutputCurves(outputCurves);
//			tj.setTimeStep(Math.round(2 / fms.size()));
			tj.setSsp(ssp);
			tj.setSsvs(ssvs);
			tj.setTPAAV(tpaav);
			tj.setDrm(drm);
			tj.setFms(fms);
			tj.runTJ2();
		}
		
		
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
		}
}

/////////////////////////////////////////////////////////////////////
///////////////////// Tracking Job2////////////////////////////////////
/////////////////////////////////////////////////////////////////////

class trackingJob21 {

//	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private int correctionSelection;
	private int noImages;
	private int timeStep;
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

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setSsvs (SurfaceScatterViewStart ssvs) {
		this.ssvs =  ssvs;
	}
		
	@SuppressWarnings("unchecked")
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

		int jok = fms.get(startFrame).getDatNo();

		String[] doneArray = new String[drm.getDatFilepaths().length];
		
		if (startFrame == 0) {
			
			
			t  = new Thread(){
				
				@Override
				public void run(){
			
					for (int k = 0; k < noImages; k++) {
						
						FrameModel frame= fms.get(k);
						
						if(t.isInterrupted()){
							break;
						}
						
						if (frame.getDatNo() == jok) {
							
							debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
							+ " , " + "local jok:  " + Integer.toString(jok));
							
							debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the 0 loop~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");

							int jok = drm.getFilepathsSortedArray()[k];
							int trackingMarker = 0;
							
							
							
							SliceND slice = new SliceND(frame.getRawImageData().getShape());
							IDataset j = DatasetFactory.createFromObject(0);
							try {
								j = frame.getRawImageData().getSlice(slice);
							} catch (DatasetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println(e.getMessage());
							}
												
							
							drm.addxList(drm.getFms().size(), k,
									frame.getScannedVariable());
							
							drm.addDmxList(frame.getDatNo(),  
										   imagePosInOriginalDat[k],
										   frame.getScannedVariable());
							
							
							debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
							
							debug("Tracker should fire once");
							
						
							IDataset output1 = DummyProcessWithFrames.DummyProcess(drm, 
									   gm, 
									   correctionSelection, 
									   imagePosInOriginalDat[k], 
									   trackingMarker, 
									   k,
									   null);
							
													
							
							debug("Tracker should HAVE fired once");
							
							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
								debug("Dummy Proccessing failure");
								Display d =Display.getCurrent();
								ssp.boundariesWarning("position 1, line ~1410, k: " + Integer.toString(k),d);
								break;
							}
							
							drm.addBackgroundDatArray(fms.size(), k, output1);
							
							int imageNumber =k;
							
							double[] tempLoc = frame.getRoiLocation();
							RectangularROI newROI = new RectangularROI(tempLoc[0],
								       tempLoc[1],
								       drm.getInitialLenPt()[0][0],
								       drm.getInitialLenPt()[0][1],0);
							
							display.syncExec(new Runnable() {
								@Override
								public void run() {																			
									try {
										updateTrackingDisplay(frame.getRawImageData().getSlice(slice).squeeze(), imageNumber);
									} catch (DatasetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									return;
								}
							});
						}
					}
					//////bottom of k++ loop
					doneArray[jok] = "done";
					
					while (ClosestNoFinder.full(doneArray, "done") == false) {

						debug("in the while loop");

						int nextk = ClosestNoFinder.closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()),
								drm.getSortedX(), doneArray, drm.getFilepathsSortedArray());

						int nextjok = drm.getFilepathsSortedArray()[nextk];

						debug("nextk :" + nextk);
						debug("nextjok :" + nextjok);
						debug("doneArray[nextjok]: " + doneArray[nextjok]);
						
						if (imagePosInOriginalDat[nextk] == 0) {
				
							debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
							
							for (int k = nextk; k < noImages; k++) {

								if(t.isInterrupted()){
									break;
								}
								
								FrameModel frame = fms.get(k);
								
								if (drm.getFilepathsSortedArray()[k] == nextjok) {

									debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
									
									
									int trackingMarker = 0;
									
									SliceND slice = new SliceND(frame.getRawImageData().getShape());
									IDataset j = DatasetFactory.createFromObject(0);
									try {
										j = frame.getRawImageData().getSlice(slice);
									} catch (DatasetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
									}
									
									int jokLocal = frame.getDatNo();									
									
									if(drm.getLocationList() == null && 
									   fms.get(0).getTrackingMethodology() != TrackerType1.INTERPOLATION &&
									   fms.get(0).getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
										
										if (drm.getTrackerLocationList() == null  ){
//											| sm.getTrackerLocationList().size() <= 10
											
											
											double[] test = new double[] {0,0,0,0,0,0,0,0};
											double myNum = drm.getSortedX().getDouble(k);
											double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
											int nearestCompletedDatFileNo = 0;
											
											for(int c = 0; c < drm.getSortedX().getSize(); c++){
											   FrameModel fm = fms.get(c);
											   double cdistance = fm.getScannedVariable()- myNum;
											    if((cdistance < distance) & 
											       !Arrays.equals(fm.getRoiLocation(), test) & 
											       !Arrays.equals(fm.getRoiLocation(), null)){
											        
											    	nearestCompletedDatFileNo = fm.getDatNo();
											        distance = cdistance;
											    }
											}
																		
											ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);
											ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
											
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
											
											
											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																										   lValues, 
																										   xValues, 
																										   yValues, 
																										   drm.getInitialLenPt()[0],
																										   1);
											drm.addSeedLocation(nearestCompletedDatFileNo,
													           seedLocation);
											
//											debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
									
										}
										
										else{
											double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(drm.getTrackerLocationList(), 
																																   drm.getSortedX(), 
																																   drm.getInitialLenPt()[0],
																																   k);
											drm.addSeedLocation(fms.get(k).getDatNo(),
											           			seedLocation);
										}
									}	
									
									
									else if(frame.getTrackingMethodology()== TrackerType1.INTERPOLATION ||
										    frame.getTrackingMethodology()== TrackerType1.SPLINE_INTERPOLATION){
										
										int[] len = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1])};
										int[]  pt = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1])};
										
										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
												(double) (pt[1] + len[1]) };
										
										drm.addSeedLocation(frame.getDatNo(),seedLocation);
									}
									
									drm.addxList(fms.size(), k,
											drm.getSortedX().getDouble(k));
									
									
									drm.addDmxList(frame.getDatNo(),  
											   imagePosInOriginalDat[k],
											   frame.getScannedVariable());
//									debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									IDataset output1 = 
											DummyProcessWithFrames.DummyProcess1(drm, 
																			   gm, 
																			   correctionSelection, 
																			   imagePosInOriginalDat[k], 
																			   trackingMarker, 
																			   k,
																			   drm.getSeedLocation()[frame.getDatNo()]);

									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
										debug("Dummy Proccessing failure");
										Display d =Display.getCurrent();
										ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
										
										break;
									}
									
									drm.addBackgroundDatArray(fms.size(), k, output1);
									
									int imageNumber =k;
									IDataset tempImage = j;
									
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
								doneArray[nextjok] = "done";
							}
							//////bottom of k++ loop
						}

						else if (imagePosInOriginalDat[nextk] != 0) {

							for (int k = nextk; k >= 0; k--) {

								if(t.isInterrupted()){
									break;
								}
								
								FrameModel frame = fms.get(k);
								
								if (frame.getDatNo() == nextjok) {

									debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
									
									
									int trackingMarker = 1;
									
									SliceND slice = new SliceND(frame.getRawImageData().getShape());
									IDataset j = DatasetFactory.createFromObject(0);
									try {
										j = frame.getRawImageData().getSlice(slice);
									} catch (DatasetException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										System.out.println(e.getMessage());
									}
									
									
									if(drm.getLocationList() == null && 
									   frame.getTrackingMethodology() != TrackerType1.INTERPOLATION &&
									   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
										
										
										double[] test = new double[] {0,0,0,0,0,0,0,0};
										double myNum = drm.getSortedX().getDouble(k);
										double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
										int nearestCompletedDatFileNo = 0;
										
										for(int c = 0; c < drm.getSortedX().getSize(); c++){
										   FrameModel fm = fms.get(c);
										   double cdistance = fm.getScannedVariable()- myNum;
										    if((cdistance < distance) & 
										       !Arrays.equals(fm.getRoiLocation(), test) & 
										       !Arrays.equals(fm.getRoiLocation(), null)){
										        
										    	nearestCompletedDatFileNo = fm.getDatNo();
										        distance = cdistance;
										    }
										}
										
										ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);;
										ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
										
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
										
										
										double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																									   lValues, 
																									   xValues, 
																									   yValues, 
																									   drm.getInitialLenPt()[0],
																									   1);
										drm.getSeedLocation()[frame.getDatNo()] = (seedLocation);
										
//										debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
										
									
									}
									
									else if(frame.getTrackingMethodology() == TrackerType1.INTERPOLATION ||
											frame.getTrackingMethodology() == TrackerType1.SPLINE_INTERPOLATION){
										
										int[] len = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1])};
										int[]  pt = new int[] {(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1])};
										
										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
												(double) (pt[1] + len[1]) };
										
										drm.setSeedLocation(frame.getDatNo(), seedLocation);
									}
									
									drm.addxList(drm.getNoOfImagesInDatFile(frame.getDatNo()), 
											imagePosInOriginalDat[k],
											drm.getSortedX().getDouble(k));
//									
//									debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
									
									drm.addDmxList(frame.getDatNo(),  
											   imagePosInOriginalDat[k],
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
										debug("Dummy Proccessing failure");
										Display d =Display.getCurrent();
										ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
										
										break;
									}
									
									
									drm.addBackgroundDatArray(fms.size(), k, output1);
									
									
									int imageNumber =k;
									IDataset tempImage = j;
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

							
							drm.getInputForEachDat()[nextjok]=(null);
							
							if(nextk != noImages-1){
								for (int k = nextk+1; k < noImages; k++) {
	
									if(t.isInterrupted()){
										break;
									}
									
									
									FrameModel frame= fms.get(k);
									
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

										if(drm.getLocationList().get(jok) == null && 
										   frame.getTrackingMethodology()!= TrackerType1.INTERPOLATION &&
										   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
											
											
											double[] test = new double[] {0,0,0,0,0,0,0,0};
											double myNum = drm.getSortedX().getDouble(k);
											double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
											int nearestCompletedDatFileNo = 0;
											
											for(int c = 0; c < drm.getSortedX().getSize(); c++){
											   FrameModel fm = fms.get(c);
											   double cdistance = fm.getScannedVariable()- myNum;
											    if((cdistance < distance) & 
											       !Arrays.equals(fm.getRoiLocation(), test) & 
											       !Arrays.equals(fm.getRoiLocation(), null)){
											        
											    	nearestCompletedDatFileNo = fm.getDatNo();
											        distance = cdistance;
											    }
											}
											
											ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);;
											ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
											
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
											
											
											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																										   lValues, 
																										   xValues, 
																										   yValues, 
																										   drm.getInitialLenPt()[0],
																										   1);
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
										
										drm.addxList(fms.size(), 
													 k,
													 drm.getSortedX().getDouble(k));
										
//										debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
										
										IDataset output1 = 
												DummyProcessWithFrames.DummyProcess1(drm, 
																				   gm, 
																				   correctionSelection, 
																				   imagePosInOriginalDat[k], 
																				   trackingMarker, 
																				   k,
																				   drm.getSeedLocation()[frame.getDatNo()]);
	
										if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
											debug("Dummy Proccessing failure");
											Display d =Display.getCurrent();
											ssp.boundariesWarning("position 1, line ~2369, k: " + Integer.toString(k),d);
											
											break;
										}
										
										
										drm.addBackgroundDatArray(fms.size(), k, output1);
										
										int imageNumber =k;
										IDataset tempImage = j;
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
			
			
		
			for (int k = startFrame; k >= 0; k--) {

				if(t.isInterrupted()){
					break;
				}
				
				FrameModel frame = fms.get(k);
				
				if (frame.getDatNo() == jok) {
					
					debug("switched to k--");
					debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  startFrame + "??????????????????" );
					debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
					+ " , " + "local jok:  " + Integer.toString(jok));
					
					int trackingMarker = 1;
					
					SliceND slice = new SliceND(frame.getRawImageData().getShape());
					IDataset j = DatasetFactory.createFromObject(0);
					try {
						j = frame.getRawImageData().getSlice(slice);
					} catch (DatasetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(e.getMessage());
					}
					
					
					int a = frame.getDatNo();
					int b = drm.getNoOfImagesInDatFile(a);
					int c = frame.getNoInOriginalDat();
					double d = drm.getSortedX().getDouble(k);
					
					drm.addDmxList(a, c, d);
					
					drm.addxList(fms.size(), k,
							drm.getSortedX().getDouble(k));
					
//					debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);


					IDataset output1 = DummyProcessWithFrames.DummyProcess(drm, 
																		   gm, 
																		   correctionSelection, 
																		   imagePosInOriginalDat[k], 
																		   trackingMarker, 
																		   k,
																		   null);
					
					

					if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
						debug("Dummy Proccessing failure");

						break;	
					}
					
					drm.addBackgroundDatArray(fms.size(), k, output1);
					
					int imageNumber =k;
					IDataset tempImage = j;
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

			debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  startFrame + "??????????????????" );
			
			drm.getInputForEachDat()[jok]=(null);
			
			if(startFrame != noImages-1){
				
				
				for (int k = startFrame; k < noImages; k++) {
					
					if(t.isInterrupted()){
						break;
					}
	
					FrameModel frame = fms.get(k);
					
					if (frame.getDatNo() == jok) {
	
						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  startFrame + "??????????????????" );
						
						
						debug("switched to k++");
						debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local jok:  " + Integer.toString(jok));
						
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
					
						int a = frame.getDatNo();
						int b = frame.getNoInOriginalDat();
						double c = frame.getScannedVariable();
					
						drm.addDmxList(a,  
								   b,
								   c);

						drm.addxList(fms.size(), k,
								drm.getSortedX().getDouble(k));
						
//						debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						IDataset output1 = 
								DummyProcessWithFrames.DummyProcess0(drm, 
															   		  gm,
															   		  correctionSelection, 
															   		  imagePosInOriginalDat[k], 
															   		  trackingMarker, 
															   		  k);
	
						
	
						if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
							Display di =Display.getCurrent();
							debug("Dummy Proccessing failure");
							
							System.out.println("problem");
							ssp.boundariesWarning("position 1, line ~1955, k: " + Integer.toString(k),di);
						
							break;	
						}
						
						drm.addBackgroundDatArray(fms.size(), k, output1);
						
						
						int imageNumber =k;
						IDataset tempImage = j;
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
			}
		//////bottom of k++ loop

		doneArray[jok] = "done";

		//////////////////////////// continuing to next
		//////////////////////////// dat////////////////////////////////////////

		while (ClosestNoFinder.full(doneArray, "done") == false) {

			debug("in the while loop");

			int nextk = ClosestNoFinder.closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()),
															 drm.getSortedX(), 
															 doneArray, 
															 drm.getFilepathsSortedArray());

			int nextjok = drm.getFilepathsSortedArray()[nextk];

			debug("nextk :" + nextk);
			debug("nextjok :" + nextjok);
			debug("doneArray[nextjok]: " + doneArray[nextjok]);
			
			if (imagePosInOriginalDat[nextk] == 0) {
	
				debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
				
				for (int k = nextk; k < noImages; k++) {

					if(t.isInterrupted()){
						break;
					}
					
					FrameModel frame = fms.get(k);
					
					if (frame.getDatNo() == nextjok) {

						debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						int trackingMarker = 0;
						
						SliceND slice = new SliceND(frame.getRawImageData().getShape());
						IDataset j = DatasetFactory.createFromObject(0);
						try {
							j = frame.getRawImageData().getSlice(slice);
						} catch (DatasetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println(e.getMessage());
						}
						
						int jokLocal = frame.getDatNo();
						
						if(drm.getLocationList().get(frame.getDatNo()) == null && 
						   frame.getTrackingMethodology() != TrackerType1.INTERPOLATION &&
						   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
							
							if (drm.getTrackerLocationList() == null ){
								
//								| sm.getTrackerLocationList().size() <= 10 
								double[] test = new double[] {0,0,0,0,0,0,0,0};
								double myNum = drm.getSortedX().getDouble(k);
								double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
								int nearestCompletedDatFileNo = 0;
								
								for(int c = 0; c < drm.getSortedX().getSize(); c++){
								   FrameModel fm = fms.get(c);
								   double cdistance = fm.getScannedVariable()- myNum;
								    if((cdistance < distance) & 
								       !Arrays.equals(fm.getRoiLocation(), test) & 
								       !Arrays.equals(fm.getRoiLocation(), null) &
								       doneArray[fm.getDatNo()] != null){
								        
								    	nearestCompletedDatFileNo = fm.getDatNo();
								        distance = cdistance;
								    }
								}
								ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);;
								ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
								
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
								
								
								
								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																							   lValues, 
																							   xValues, 
																							   yValues, 
																							   drm.getInitialLenPt()[0],
																							   1);
								drm.addSeedLocation(frame.getDatNo(),seedLocation);
								
								debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ 1456   seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
						
							}
							else{
								double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(drm.getTrackerLocationList(), 
																							   drm.getSortedX(), 
																							   drm.getInitialLenPt()[0],
																							   k);
								drm.addSeedLocation(frame.getDatNo(),seedLocation);
							}
						}	
						
						
						else if(frame.getTrackingMethodology()== TrackerType1.INTERPOLATION ||
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
					
						
						drm.addxList(fms.size(), k,
								drm.getSortedX().getDouble(k));
						
//						debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						double[] gV=  drm.getSeedLocation()[frame.getDatNo()];
						
						IDataset output1 = 
								DummyProcessWithFrames.DummyProcess1(drm, 
																   gm, 
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   gV);

						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
							Display d =Display.getCurrent();
							debug("Dummy Proccessing failure");
							ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
							
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
					doneArray[nextjok] = "done";
				}
			}

			else if (imagePosInOriginalDat[nextk] != 0) {

				debug("%%%%%%%%%%%%%%%%% switched the dat file");
				debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  nextk + "??????????????????" );
				
				
//				startFrame = nextk;
				for (int k = (nextk); k >= 0; k--) {

					if(t.isInterrupted()){
						break;
					}
					
					FrameModel frame = fms.get(k);
					
					if (frame.getDatNo() == nextjok) {

						debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						debug("%%%%%%%%%%%%%%%%% switched the dat file");
						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  + nextjok + "??????????????????" );
						
						
						int trackingMarker = 1;
						
						
						ArrayList<Double[]> zero = new ArrayList<>();
						
//						zero.add(new Double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0});
						
						Double[] io = new Double[] {0.0,
													0.0,
													0.0,
													0.0,
													0.0,
													0.0,
													0.0,
													0.0};
						
						ArrayList<double[]> re = (ArrayList<double[]>) drm.getLocationList().get(frame.getDatNo()).clone();
						
						double[] lc = re.get(0);
						
						double ry = lc[0];
						int w = re.size();
						
						ArrayList<double[]> removeR = new ArrayList<>();
						
						for(double[] iu : re){
							
							double g = 0.0;
							for(int by = 0; by<iu.length; by++){
								g =g+Double.compare(iu[by], 0.0);
							
							}
							if(g==0.0){
								removeR.add(iu);
							}							
						}
						
						for(double[] df : removeR){
							re.remove(df);
						}
						
						if(re.size() == 0 && 
						   frame.getTrackingMethodology() != TrackerType1.INTERPOLATION &&
						   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION) {
							
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
						
						drm.addDmxList(frame.getDatNo(),  
								   	   frame.getNoInOriginalDat(),
								   	   frame.getScannedVariable());
					
						
						drm.addxList(fms.size(), k,
									 drm.getSortedX().getDouble(k));
						
						debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
						
						double[] gv =  drm.getSeedLocation()[frame.getDatNo()];
						
						int[][] gvLenPt = LocationLenPtConverterUtils.locationToLenPtConverter(gv);
						
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

							
							if(drm.getLocationList().get(jok)  == null && 
							   frame.getTrackingMethodology()!= TrackerType1.INTERPOLATION &&
							   frame.getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION){
								
								double[] test = new double[] {0,0,0,0,0,0,0,0};
								double myNum = drm.getSortedX().getDouble(k);
								double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
								int nearestCompletedDatFileNo = 0;
								
								for(int c = 0; c < drm.getSortedX().getSize(); c++){
								   FrameModel fm = fms.get(c);
								   double cdistance = fm.getScannedVariable()- myNum;
								    if((cdistance < distance) & 
								       !Arrays.equals(fm.getRoiLocation(), test) & 
								       !Arrays.equals(fm.getRoiLocation(), null)){
								        
								    	nearestCompletedDatFileNo = fm.getDatNo();
								        distance = cdistance;
								    }
								}
								
								ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);;
								ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);
								
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
								
								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k),
																							   lValues, 
																							   xValues, 
																							   yValues, 
																							   drm.getInitialLenPt()[0],
																							   1);
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
			
							
//							debug("value added to xList:  "   + drm.getSortedX().getDouble(k)  + "  k:   " + k);
							
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
		ssvs.updateIndicators(imageNumber);
		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage.squeeze(), null, null);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(drm.getFms().get(imageNumber).getBackgroundSubtractedImage(), null, null);
		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
		ssvs.getSsps3c().generalUpdate();
		ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());

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
//			System.out.println("progress bar start:  " +progressBar.getSelection());
			progressBar.setSelection(progressBar.getSelection() +1);
//			System.out.println("progress bar incremented:  " +progressBar.getSelection());
//			System.out.println("progress bar max:  " +progressBar.getMaximum());
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
	
	

