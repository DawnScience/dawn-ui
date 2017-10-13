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
	// private int correctionSelection;
	private int noImages;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 0;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortViewImproved tpaav;
	private Thread t;
	private ArrayList<FrameModel> fms;
	private DirectoryModel drm;
	boolean start = true;

	public void setT(Thread t) {
		this.t = t;
	}

	public TrackingProgressAndAbortViewImproved getTPAAV() {
		return tpaav;
	}

	public void setTPAAV(TrackingProgressAndAbortViewImproved tpaav) {
		this.tpaav = tpaav;
	}

	public SurfaceScatterViewStart getSsvs() {
		return ssvs;
	}

	public void setProgress(ProgressBar progress) {
		this.progressBar = progress;
	}

	public void setSsvs(SurfaceScatterViewStart ssvs) {
		this.ssvs = ssvs;
	}

	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
		this.outputCurves = outputCurves;
	}

	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1() {

		this.gm = ssp.getGm();
		this.fms = ssp.getFms();
		this.drm = ssp.getDrm();

		drm.resetAll();

		int startFrame = ssp.getSliderPos();

		final Display display = Display.getCurrent();
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(drm.getFilepathsSortedArray());

		int[] len = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds()
				.getIntLengths();
		int[] pt = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds()
				.getIntPoint();

		int[][] lenPt = { len, pt };

		ssp.regionOfInterestSetter(lenPt);

		noImages = fms.size();

		ssp.regionOfInterestSetter(lenPt);

		outputCurves.clear();

		boolean[] doneArray = new boolean[drm.getDatFilepaths().length];

		drm.setDoneArray(doneArray);

		t = new Thread() {

			@Override
			public void run() {

				//////////////////////////// continuing to next
				//////////////////////////// dat////////////////////////////////////////

				while (!ClosestNoFinder.full(doneArray, "done")) {

					debug("in the while loop");

					int nextk = closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()),
							drm.getSortedX(), doneArray, drm.getFilepathsSortedArray(), fms);

					int nextjok = drm.getFilepathsSortedArray()[nextk];

					debug("nextk :" + nextk);
					debug("nextjok :" + nextjok);
					debug("doneArray[nextjok]: " + doneArray[nextjok]);

					ArrayList<FrameModel> fmal = drm.getFmsSorted().get(nextjok);

					while (isFmListScanned(fmal)) {

						int k = closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()), fmal);

						if (t.isInterrupted()) {
							break;
						}

						FrameModel frame = fmal.get(k);

						// if (frame.getDatNo() == nextjok) {

						debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  "
								+ Integer.toString(k) + " , " + "local nextjok:  " + Integer.toString(nextjok));

						int trackingMarker = 1;

						boolean seedRequired = false;

						if (start) {
							start = false;
						}

						else {
							seedRequired = doINeedASeedArray(
									// k,
									// startFrame,
									frame.getDatNo());

						}

						double myNum = frame.getScannedVariable();
						double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);

						TrackerType1 tt1 = frame.getTrackingMethodology();

						seedLocationSetter(trackingMarker, frame.getFmNo(),
								// startFrame,
								!start, frame.getDatNo(), seedRequired, tt1, myNum, distance);

						drm.addDmxList(frame.getDatNo(), frame.getNoInOriginalDat(), frame.getScannedVariable());

						drm.addxList(fms.size(), frame.getFmNo(), drm.getSortedX().getDouble(frame.getFmNo()));

						debug("value added to xList:  " + drm.getSortedX().getDouble(frame.getFmNo()) + "  k:   " + k);

						double[] gv = drm.getSeedLocation()[frame.getDatNo()];

						IDataset output1 = DummyProcessWithFrames.DummyProcess1(drm, gm,
								// correctionSelection,
								frame.getNoInOriginalDat(), trackingMarker, frame.getFmNo(), gv, ssp.getLenPt());

						if (Arrays.equals(output1.getShape(), (new int[] { 2, 2 }))) {
							Display d = Display.getCurrent();
							debug("Dummy Proccessing failure");

							display.syncExec(new Runnable() {
								@Override
								public void run() {

									ssp.boundariesWarning("position 1, line ~207", d);
									return;
								}
							});

							break;
						}

						drm.addBackgroundDatArray(fms.size(), frame.getFmNo(), output1);

						IDataset tempImage = ssp.getImage(frame.getFmNo());

						display.syncExec(new Runnable() {
							@Override
							public void run() {

								updateTrackingDisplay(tempImage, frame.getFmNo());
								return;
							}
						});
						// }

						frame.setScanned(true);
					}

					drm.getInputForEachDat()[nextjok] = null;

					// for (int k = nextk+1;
					// k < noImages;
					// k++) {
					//
					// if(t.isInterrupted()){
					// break;
					// }
					//
					// FrameModel frame = fms.get(k);
					//
					// debug("%%%%%%%%%%%%%%%%% sm.getStartFrame: " + startFrame
					// + "??????????????????" );
					// if (frame.getDatNo() == nextjok) {
					//
					// debug("l value: " +
					// Double.toString(drm.getSortedX().getDouble(k)) + " , " +
					// "local k: " + Integer.toString(k)
					// + " , " + "local nextjok: " + Integer.toString(nextjok));
					//
					//
					// int trackingMarker = 2;
					//
					// SliceND slice = new
					// SliceND(frame.getRawImageData().getShape());
					// IDataset j = DatasetFactory.createFromObject(0);
					// try {
					// j = frame.getRawImageData().getSlice(slice);
					// } catch (DatasetException e) {
					// e.printStackTrace();
					// System.out.println(e.getMessage());
					// }
					//
					// boolean seedRequired = doINeedASeedArray(k, startFrame,
					// frame.getDatNo());
					//
					// double myNum = drm.getSortedX().getDouble(k);
					// double distance = Math.abs(drm.getSortedX().getDouble(0)
					// - myNum);
					//
					// int frameDatNo = frame.getDatNo();
					//
					// TrackerType1 tt1 = frame.getTrackingMethodology();
					//
					//
					// seedLocationSetter(trackingMarker,
					// k,
					// startFrame,
					// frameDatNo,
					// seedRequired,
					// tt1,
					// myNum,
					// distance);
					// //
					//
					// drm.addDmxList(frame.getDatNo(),
					// frame.getNoInOriginalDat(),
					// frame.getScannedVariable());
					//
					//
					// IDataset output1 =
					// DummyProcessWithFrames.DummyProcess1(drm,
					// gm,
					//// correctionSelection,
					// imagePosInOriginalDat[k],
					// trackingMarker,
					// k,
					// drm.getSeedLocation()[frame.getDatNo()],
					// ssp.getLenPt());
					//
					// if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
					// Display d =Display.getCurrent();
					// debug("Dummy Proccessing failure");
					//
					// display.syncExec(new Runnable() {
					// @Override
					// public void run() {
					//
					// ssp.boundariesWarning("position 1, line ~310",d);
					// return;
					// }
					// });
					//
					//
					// break;
					// }
					//
					//
					// drm.addBackgroundDatArray(fms.size(), k, output1);
					//
					// int imageNumber =k;
					// IDataset tempImage = j;
					//
					// display.syncExec(new Runnable() {
					// @Override
					// public void run() {
					//
					// updateTrackingDisplay(tempImage, imageNumber);
					//
					// return;
					// }
					// });
					// }
					// }

					////// bottom of k++ loop
					doneArray[nextjok] = true;
				}

				try {

					CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
					csdpgfd.generateCsdpFromDrm(drm);

					CurveStitchDataPackage csdp = csdpgfd.getCsdp();
					CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);

					drm.setCsdp(csdp);

					if (drm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction
							|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
							|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
							|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling
							|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

						ReflectivityNormalisation.reflectivityNormalisation1(csdp);
						drm.setCsdp(csdp);
					}

					csdp.setRodName("Current Track");
					ssvs.getRaw().getRtc().addCurrentTrace(csdp);

					display.syncExec(new Runnable() {
						@Override
						public void run() {

							updateOutputCurve(csdp);
							return;
						}
					});

				} catch (Exception h) {
					System.out.println(h.getMessage());
				}

				return;
			}

		};

		t.start();

		ssvs.getCustomComposite().getReplay().setEnabled(true);

	}

	protected void updateTrackingDisplay(IDataset tempImage, int imageNumber) {

		ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
		ssp.sliderMovemementMainImage(imageNumber);
		ssvs.updateIndicators(imageNumber);
		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage.squeeze(), null, null);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem()
				.updatePlot2D(drm.getFms().get(imageNumber).getBackgroundSubtractedImage(), null, null);
		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
		ssvs.getSsps3c().generalUpdate();

		double[] location = ssp.getThisLocation(imageNumber);

		int[] len = new int[] { (int) (location[2] - location[0]), (int) (location[5] - location[1]) };
		int[] pt = new int[] { (int) location[0], (int) location[1] };
		int[][] lenPt = { len, pt };

		int[][] lenPt1 = LocationLenPtConverterUtils.locationToLenPtConverter(location);

		RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt1);

		ssvs.getPlotSystemCompositeView().getIRegion().setROI(greenAndBg[0]);
		ssvs.getPlotSystemCompositeView().getBgRegion().setROI(greenAndBg[1]);

		if (ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX) {
			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
		}

		ssvs.getSsps3c().generalUpdate(lenPt);

		updateOutputCurve();

		if (progressBar.isDisposed() != true) {
			progressBar.setSelection(progressBar.getSelection() + 1);

			if (progressBar.getSelection() == progressBar.getMaximum()) {
				tpaav.close();
			}
		}
	}

	protected void updateOutputCurve() {

		ssp.stitchAndPresent1(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds());
		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
		csdpgfd.generateCsdpFromDrm(drm);
		CurveStitchDataPackage csdp = csdpgfd.getCsdp();
		CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);

		drm.setCsdp(csdp);

		csdp.setRodName("Current Track");

		ssvs.getRaw().getRtc().addCurrentTrace(csdp);

		ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();

	}

	protected void updateOutputCurve(CurveStitchDataPackage csdp) {

		ssp.stitchAndPresentFromCsdp(ssvs.getSsps3c().getOutputCurves(), ssvs.getIds(), csdp);

		drm.setCsdp(csdp);

		csdp.setRodName("Current Track");

		ssvs.getRaw().getRtc().addCurrentTrace(csdp);

		ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();

	}

	private int findNearestDatNo(double distance, double myNum) {

		int nearestCompletedDatFileNo = 0;

		double[] test = new double[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] test2 = new double[] { 10, 10, 60, 10, 10, 60, 60, 60 };

		for (int c = 0; c < drm.getSortedX().getSize(); c++) {
			FrameModel fm = fms.get(c);
			double cdistance = Math.abs(fm.getScannedVariable() - myNum);
			if ((cdistance < distance) && !Arrays.equals(fm.getRoiLocation(), test)
					&& !Arrays.equals(fm.getRoiLocation(), test2) && !Arrays.equals(fm.getRoiLocation(), null)) {

				nearestCompletedDatFileNo = fm.getDatNo();
				distance = cdistance;
			}
		}

		return nearestCompletedDatFileNo;

	}

	private boolean doINeedASeedArray(
			// int k,
			// int startFrame,
			int frameDatNo) {

		// boolean seedRequired = true;
		//
		// if (k == startFrame) {
		// seedRequired = false;
		// }

		for (double[] o : drm.getLocationList().get(frameDatNo)) {
			if (!Arrays.equals(o, new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 })) {
				return false;

			}
		}

		return true;

	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

	private void seedLocationSetter(int trackingMarker, int k, boolean setStartFrame, int frameDatNo,
			boolean seedRequired, TrackerType1 tt1, double myNum, double distance) {

		if (seedRequired &&
		// tt1 != TrackerType1.INTERPOLATION &&
				tt1 != TrackerType1.SPLINE_INTERPOLATION && tt1 != TrackerType1.USE_SET_POSITIONS
				&& drm.isTrackerOn()) {

			double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(
					drm.getTrackerLocationList(), drm.getSortedX(), drm.getInitialLenPt()[0], k);

			System.out.println("k:        " + k + "      seedlocation [0]:   " + seedLocation[0]
					+ "    seedlocation [1]:   " + seedLocation[1]);

			drm.addSeedLocation(frameDatNo, seedLocation);

		}

		else if (// tt1 == TrackerType1.INTERPOLATION ||
		tt1 == TrackerType1.SPLINE_INTERPOLATION) {

			int[] len = new int[] { (int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),
					(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1]) };
			int[] pt = new int[] { (int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),
					(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1]) };

			double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
					(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
					(double) (pt[1] + len[1]) };

			debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] + " + " + "seedlocation[1] :"
					+ seedLocation[1]);

			drm.addSeedLocation(frameDatNo, seedLocation);
		}

		if (setStartFrame) {
			int[][] lenPt = ssp.getInitialLenPt();
			double[] seedLocation = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			drm.addSeedLocation(frameDatNo, seedLocation);
		}
	}

	public Thread getT() {
		return t;
	}

	private static int closestNoWithoutDone(double myNum, ArrayList<FrameModel> fms) {

		double distance = 0.0;
		int idx = 0;

		for (FrameModel f : fms) {
			if (!f.isScanned()) {
				distance = Math.abs(f.getScannedVariable() - myNum);
				idx = f.getNoInOriginalDat();
				break;
			}
		}
		
		for (int c = 0; c < fms.size(); c++) {
			double cdistance = Math.abs(fms.get(c).getScannedVariable() - myNum);
			if ((cdistance < distance) && (!fms.get(c).isScanned())) {
				idx = c;
				distance = cdistance;
			}
		}

		return idx;

	}
	
//	private static int closestNoWithoutDone(double myNum, ArrayList<FrameModel> fms) {
//
//		double distance = 0.0;
//		int idx = 0;
//
//		for (FrameModel f : fms) {
//			if (!f.isScanned()) {
//				distance = Math.abs(f.getScannedVariable() - myNum);
//				idx = f.getNoInOriginalDat();
//				break;
//			}
//		}
//		
//		for (int c = 0; c < fms.size(); c++) {
//			double cdistance = Math.abs(fms.get(c).getScannedVariable() - myNum);
//			if ((cdistance < distance) && (!fms.get(c).isScanned())) {
//				idx = c;
//				distance = cdistance;
//			}
//		}
//
//		return idx;
//
//	}
	
	private static int closestNoWithoutDone(double myNum, Dataset numbers, boolean[] doneArray,
			int[] filepathSortedArray, ArrayList<FrameModel> fms) {

//		String test = "done";
		double distance = Math.abs(numbers.getDouble(0) - myNum);
		int idx = 0;

		
		
		for (FrameModel f : fms) {
			if (!f.isScanned() && !(doneArray[f.getDatNo()])) {
				distance = Math.abs(f.getScannedVariable() - myNum);
				idx = f.getFmNo();
				break;
			}
		}
		
		for (int c = 0; c < numbers.getSize(); c++) {
			double cdistance = Math.abs(numbers.getDouble(c) - myNum);
			if ((cdistance < distance) & !doneArray[filepathSortedArray[c]]) {
				idx = c;
				distance = cdistance;
			}
		}
		return idx;

	}

	private static boolean isFmListScanned(ArrayList<FrameModel> fmal) {

		for (FrameModel f : fmal) {
			if (!f.isScanned()) {
				return true;
			}
		}

		return false;
	}

}
