package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.ProgressBar;

public class BatchTracking {

	// private IPlottingSystem<Composite> outputCurves;
	private GeometricParametersModel gm;
	private MethodSetting correctionSelection;
	private int noImages;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 0;

	// private ProgressBar progressBar;
	// private TrackingProgressAndAbortViewImproved tpaav;

	private ArrayList<FrameModel> fms;
	private DirectoryModel drm;
	private String savePath;


	public void setGm(GeometricParametersModel gms) {
		this.gm = gms;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	protected void runTJ1(int[][] lenPt, String savepath1) {

		this.gm = ssp.getGm();
		this.fms = ssp.getFms();
		this.drm = ssp.getDrm();
		this.savePath = savepath1;

		drm.resetAll();

		int startFrame = ssp.getSliderPos();

		final Display display = Display.getCurrent();
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(drm.getFilepathsSortedArray());

		ssp.regionOfInterestSetter(lenPt);

		noImages = fms.size();

		ssp.regionOfInterestSetter(lenPt);

		// outputCurves.clear();

		String[] doneArray = new String[drm.getDatFilepaths().length];

		drm.setDoneArray(doneArray);

		//////////////////////////// continuing to next
		//////////////////////////// dat////////////////////////////////////////

		System.out.println("////////////@@@@@@@@@@@~~~~~~~~~~starting tracking thread");

		while (!ClosestNoFinder.full(doneArray, "done")) {

			int nextk = ClosestNoFinder.closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()),
					drm.getSortedX(), doneArray, drm.getFilepathsSortedArray());

			int nextjok = drm.getFilepathsSortedArray()[nextk];

			debug("nextk :" + nextk);
			debug("nextjok :" + nextjok);
			debug("doneArray[nextjok]: " + doneArray[nextjok]);

			for (int k = (nextk); k >= 0; k--) {

				FrameModel frame = fms.get(k);

				if (frame.getDatNo() == nextjok) {

					debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  "
							+ Integer.toString(k) + " , " + "local nextjok:  " + Integer.toString(nextjok));

					int trackingMarker = 1;

					boolean seedRequired = doINeedASeedArray(k, startFrame, frame.getDatNo());

					double myNum = drm.getSortedX().getDouble(k);
					double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);

					int frameDatNo = frame.getDatNo();

					TrackerType1 tt1 = frame.getTrackingMethodology();

					seedLocationSetter(trackingMarker, k, startFrame, frameDatNo, seedRequired, tt1, myNum, distance);

					drm.addDmxList(frame.getDatNo(), frame.getNoInOriginalDat(), frame.getScannedVariable());

					drm.addxList(fms.size(), k, drm.getSortedX().getDouble(k));

					debug("value added to xList:  " + drm.getSortedX().getDouble(k) + "  k:   " + k);

					double[] gv = drm.getSeedLocation()[frame.getDatNo()];

					IDataset output1 = DummyProcessWithFrames.DummyProcess1(drm, gm,
							// correctionSelection,
							imagePosInOriginalDat[k], trackingMarker, k, gv, ssp.getLenPt());

					if (Arrays.equals(output1.getShape(), (new int[] { 2, 2 }))) {
						Display d = Display.getCurrent();
						debug("Dummy Proccessing failure");

						break;
					}

					drm.addBackgroundDatArray(fms.size(), k, output1);

					int imageNumber = k;
					IDataset tempImage = ssp.getImage(imageNumber);

				}
			}

			drm.getInputForEachDat()[nextjok] = null;

			for (int k = nextk + 1; k < noImages; k++) {

				

				FrameModel frame = fms.get(k);

				debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  " + startFrame + "??????????????????");
				if (frame.getDatNo() == nextjok) {

					debug("l value: " + Double.toString(drm.getSortedX().getDouble(k)) + " , " + "local k:  "
							+ Integer.toString(k) + " , " + "local nextjok:  " + Integer.toString(nextjok));

					int trackingMarker = 2;

					SliceND slice = new SliceND(frame.getRawImageData().getShape());
					IDataset j = DatasetFactory.createFromObject(0);
					try {
						j = frame.getRawImageData().getSlice(slice);
					} catch (DatasetException e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
					}

					boolean seedRequired = doINeedASeedArray(k, startFrame, frame.getDatNo());

					double myNum = drm.getSortedX().getDouble(k);
					double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);

					int frameDatNo = frame.getDatNo();

					TrackerType1 tt1 = frame.getTrackingMethodology();

					seedLocationSetter(trackingMarker, k, startFrame, frameDatNo, seedRequired, tt1, myNum, distance);
					//

					drm.addDmxList(frame.getDatNo(), frame.getNoInOriginalDat(), frame.getScannedVariable());

					IDataset output1 = DummyProcessWithFrames.DummyProcess1(drm, gm,
							// correctionSelection,
							imagePosInOriginalDat[k], trackingMarker, k, drm.getSeedLocation()[frame.getDatNo()],
							ssp.getLenPt());

					drm.addBackgroundDatArray(fms.size(), k, output1);

					int imageNumber = k;
					IDataset tempImage = j;

				}
			}

			////// bottom of k++ loop
			doneArray[nextjok] = "done";
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
		} catch (Exception h) {
			System.out.println(h.getMessage());
		}

		
		ssp.writeNexus(savePath);
		
		return;

	}

	// protected void updateOutputCurve(){
	//
	// CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
	// csdpgfd.generateCsdpFromDrm(drm);
	// CurveStitchDataPackage csdp = csdpgfd.getCsdp();
	// CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);
	//
	// drm.setCsdp(csdp);
	//
	// csdp.setRodName("Current Track");
	//
	//
	// if(progressBar.isDisposed() != true){
	// progressBar.setSelection(progressBar.getSelection() +1);
	//
	// if(progressBar.getSelection() == progressBar.getMaximum()){
	// tpaav.close();
	// }
	// }
	//
	// }
	//
	// protected void updateOutputCurve(CurveStitchDataPackage csdp){
	//
	//
	// drm.setCsdp(csdp);
	//
	// csdp.setRodName("Current Track");
	//
	// }
	//
	//
	//
	// private int findNearestDatNo(double distance,
	// double myNum){
	//
	// int nearestCompletedDatFileNo = 0;
	//
	// double[] test = new double[] {0,0,0,0,0,0,0,0};
	// double[] test2 = new double[] {10,10,60,10,10,60,60,60};
	//
	// for(int c = 0; c < drm.getSortedX().getSize(); c++){
	// FrameModel fm = fms.get(c);
	// double cdistance = Math.abs(fm.getScannedVariable()- myNum);
	// if((cdistance < distance) &&
	// !Arrays.equals(fm.getRoiLocation(), test) &&
	// !Arrays.equals(fm.getRoiLocation(), test2) &&
	// !Arrays.equals(fm.getRoiLocation(), null)){
	//
	// nearestCompletedDatFileNo = fm.getDatNo();
	// distance = cdistance;
	// }
	// }
	//
	// return nearestCompletedDatFileNo;
	//
	// }

	private boolean doINeedASeedArray(int k, int startFrame, int frameDatNo) {

		boolean seedRequired = true;

		if (k == startFrame) {
			seedRequired = false;
		}

		for (double[] o : drm.getLocationList().get(frameDatNo)) {
			if (!Arrays.equals(o, new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 })) {
				seedRequired = false;
			}
		}

		return seedRequired;

	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

	private void seedLocationSetter(int trackingMarker, int k, int startFrame, int frameDatNo, boolean seedRequired,
			TrackerType1 tt1, double myNum, double distance) {

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

		if (k == startFrame) {
			int[][] lenPt = ssp.getInitialLenPt();
			double[] seedLocation = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			drm.addSeedLocation(frameDatNo, seedLocation);
		}
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

}
