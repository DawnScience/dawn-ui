package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Arrays;
import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CsdpGeneratorFromDrm;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.CurveStitchWithErrorsAndFrames;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessWithFrames;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.ReflectivityNormalisation;
import org.dawnsci.surfacescatter.TrackerLocationInterpolation;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Display;

public class TrackingCore {

	public TrackingCore(boolean[] doneArray, SurfaceScatterPresenter ssp, Thread t, SurfaceScatterViewStart ssvs,
			boolean showtrack, TrackingHandlerWithFramesImproved thwfi, Display display) {

		DirectoryModel drm = ssp.getDrm();
		ArrayList<FrameModel> fms = ssp.getFms();
		GeometricParametersModel gm = ssp.getGm();

		boolean start = true;

		while (!ClosestNoFinder.full(doneArray, "done")) {

			int nextk = closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()), drm.getSortedX(),
					doneArray, drm.getFilepathsSortedArray(), fms);

			int nextjok = drm.getFilepathsSortedArray()[nextk];

			ArrayList<FrameModel> fmal = drm.getFmsSorted().get(nextjok);

			while (isFmListScanned(fmal)) {

				int k = closestNoWithoutDone(drm.getSortedX().getDouble(ssp.getSliderPos()), fmal);

				if (t != null) {
					if (t.isInterrupted()) {
						break;
					}
				}

				FrameModel frame = fmal.get(k);

				int trackingMarker = 1;

				boolean seedRequired = false;

				if (start) {
					start = false;
				}

				else {
					seedRequired = doINeedASeedArray(frame.getDatNo(), drm);

				}

				TrackerType1 tt1 = frame.getTrackingMethodology();

				seedLocationSetter(ssp, frame.getFmNo(), !start, frame.getDatNo(), seedRequired, tt1);

				drm.addDmxList(frame.getDatNo(), frame.getNoInOriginalDat(), frame.getScannedVariable());

				drm.addxList(fms.size(), frame.getFmNo(), drm.getSortedX().getDouble(frame.getFmNo()));

				double[] gv = drm.getSeedLocation()[frame.getDatNo()];

				IDataset output1 = DummyProcessWithFrames.DummyProcess1(drm, gm, frame.getNoInOriginalDat(),
						trackingMarker, frame.getFmNo(), gv, ssp.getLenPt());

				if (Arrays.equals(output1.getShape(), (new int[] { 2, 2 }))) {

					break;
				}

				drm.addBackgroundDatArray(fms.size(), frame.getFmNo(), output1);

				if (showtrack) {
					IDataset tempImage = ssp.getImage(frame.getFmNo());

					display.syncExec(new Runnable() {
						@Override
						public void run() {

							thwfi.updateTrackingDisplay(tempImage, frame.getFmNo());
							return;
						}
					});
				}

				frame.setScanned(true);
			}

			drm.getInputForEachDat()[nextjok] = null;

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

			if (showtrack) {
				ssvs.getRaw().getRtc().addCurrentTrace(csdp);

				display.syncExec(new Runnable() {
					@Override
					public void run() {

						thwfi.updateOutputCurve(csdp);
						return;
					}
				});
			}

		} catch (Exception h) {
			System.out.println(h.getMessage());
		}

		return;
	}

	private void seedLocationSetter(SurfaceScatterPresenter ssp, int k, boolean setStartFrame, int frameDatNo,
			boolean seedRequired, TrackerType1 tt1) {

		DirectoryModel drm = ssp.getDrm();

		if (seedRequired && tt1 != TrackerType1.SPLINE_INTERPOLATION && tt1 != TrackerType1.USE_SET_POSITIONS
				&& drm.isTrackerOn()) {

			double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(
					drm.getTrackerLocationList(), drm.getSortedX(), drm.getInitialLenPt()[0], k);

			drm.addSeedLocation(frameDatNo, seedLocation);

		}

		else if (tt1 == TrackerType1.SPLINE_INTERPOLATION) {

			int[] len = new int[] { (int) Math.round(drm.getInterpolatedLenPts().get(k)[0][0]),
					(int) Math.round(drm.getInterpolatedLenPts().get(k)[0][1]) };
			int[] pt = new int[] { (int) Math.round(drm.getInterpolatedLenPts().get(k)[1][0]),
					(int) Math.round(drm.getInterpolatedLenPts().get(k)[1][1]) };

			double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
					(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
					(double) (pt[1] + len[1]) };

			drm.addSeedLocation(frameDatNo, seedLocation);
		}

		if (setStartFrame) {
			int[][] lenPt = ssp.getInitialLenPt();
			double[] seedLocation = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			drm.addSeedLocation(frameDatNo, seedLocation);
		}
	}

	private boolean doINeedASeedArray(int frameDatNo, DirectoryModel drm) {

		for (double[] o : drm.getLocationList().get(frameDatNo)) {
			if (!Arrays.equals(o, new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 })) {
				return false;

			}
		}

		return true;

	}

	private static boolean isFmListScanned(ArrayList<FrameModel> fmal) {

		for (FrameModel f : fmal) {
			if (!f.isScanned()) {
				return true;
			}
		}

		return false;
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

	private static int closestNoWithoutDone(double myNum, Dataset numbers, boolean[] doneArray,
			int[] filepathSortedArray, ArrayList<FrameModel> fms) {

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

}
