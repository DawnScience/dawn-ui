package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.CsdpGeneratorFromDrm;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.CurveStitchWithErrorsAndFrames;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class TrackingHandlerWithFramesImproved {

	private IPlottingSystem<Composite> outputCurves;
	private SurfaceScatterViewStart ssvs;
	private SurfaceScatterPresenter ssp;
	private ProgressBar progressBar;
	private TrackingProgressAndAbortViewImproved tpaav;
	private Thread t;
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

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
		this.drm = ssp.getDrm();
	}

	protected void runTJ1() {

		ssp.getDrm().resetAll();

		final Display display = Display.getCurrent();

		int[] len = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds()
				.getIntLengths();
		int[] pt = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion").getROI().getBounds()
				.getIntPoint();

		int[][] lenPt = { len, pt };

		ssp.regionOfInterestSetter(lenPt);

		outputCurves.clear();

		boolean[] doneArray = new boolean[drm.getDatFilepaths().length];

		drm.setDoneArray(doneArray);

		t = new Thread() {

			@Override
			public void run() {

				new TrackingCore(doneArray, ssp, t, ssvs, true, TrackingHandlerWithFramesImproved.this, display, false);

				return;

			}
		};

		t.start();

		ssvs.getCustomComposite().getReplay().setEnabled(true);

	}

	protected void updateTrackingDisplay(IDataset tempImage, int imageNumber) {

		double[] location = ssp.getThisLocation(imageNumber);

		if (location[0] > tempImage.getShape()[1] || location[2] > tempImage.getShape()[1]) {
			throw new java.lang.IndexOutOfBoundsException();
		}

		if (location[1] > tempImage.getShape()[0] || location[7] > tempImage.getShape()[0]) {
			throw new java.lang.IndexOutOfBoundsException();
		}

		for (double i : location) {
			if (i < 0) {
				throw new java.lang.IndexOutOfBoundsException();
			}
		}

		ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
		ssp.sliderMovemementMainImage(imageNumber);
		ssvs.updateIndicators(imageNumber);
		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage.squeeze(), null, null);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem()
				.updatePlot2D(drm.getFms().get(imageNumber).getBackgroundSubtractedImage(), null, null);
		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
		ssvs.getSsps3c().generalUpdate();

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

		try {
			updateOutputCurve();
		} catch (Exception g) {
			throw new java.lang.IndexOutOfBoundsException();
		}

		if (!progressBar.isDisposed()) {
			progressBar.setSelection(progressBar.getSelection() + 1);

			if (progressBar.getSelection() == progressBar.getMaximum()) {
				tpaav.close();
			}
		}
	}

	public void kill() {
		tpaav.close();
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

	public Thread getT() {
		return t;
	}

}
