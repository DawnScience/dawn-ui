
package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.DisplayLabelStrings;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;

public class GeometricParametersRepeaterTable extends Composite {

	private IPlottingSystem<Composite> plotSystem1;
	private IDataset image1;
	private Button button;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button trackerOnButton;
	private Combo backgroundCombo;
	private Combo polynomialPowerCombo;
	private Combo trackingMethodCombo;
	private Spinner boundaryBoxText;
	private SurfaceScatterPresenter ssp;
	private Boolean trackerOn = false;
	private SurfaceScatterViewStart ssvs;
	private Button useQAxis;

	public GeometricParametersRepeaterTable(Composite parent, int style, SurfaceScatterPresenter ssp,
			SurfaceScatterViewStart ssvs) {

		super(parent, style);
		this.ssp = ssp;
		this.ssvs = ssvs;

		try {
			plotSystem1 = PlottingFactory.createPlottingSystem();
			plotSystem1.setTitle("Background Subtracted Image");
		} catch (Exception e2) {

		}

		this.createContents();

	}

	public void createContents() {

		
		GeometricParametersWindows gpw= new GeometricParametersWindows(this, SWT.FILL, ssp);
		gpw.setLayout(new GridLayout());
		gpw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		
		

	}

	public Composite getComposite() {
		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem1;
	}

	public IDataset getImage() {
		return image1;
	}

	public int[] getMethodology() {

		int[] returns = new int[3];

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				returns[0] = backgroundCombo.getSelectionIndex();
				returns[1] = polynomialPowerCombo.getSelectionIndex();
				try {
					returns[2] = Integer.parseInt(boundaryBoxText.getText());
				} catch (Exception e) {
					returns[2] = 10;
				}
			}
		});

		return returns;
	}

	public void generalUpdate() {

		int methodologySelection = backgroundCombo.getSelectionIndex();
		int fitPowerSelection = polynomialPowerCombo.getSelectionIndex();
		int trackerSelection = trackingMethodCombo.getSelectionIndex();

		String boundaryBox = String.valueOf(boundaryBoxText.getSelection());

		ssvs.updateAnalysisMethodology(methodologySelection, fitPowerSelection, trackerSelection, boundaryBox);

		ssp.backgroundBoxesManager(ssvs.getPlotSystemCompositeView().getBgRegion(),
				ssvs.getPlotSystemCompositeView().getSecondBgRegion(),
				ssvs.getPlotSystemCompositeView().getCentreSecondBgRegion());

		IRectangularROI greenRectangle = ssvs.getPlotSystemCompositeView().getPlotSystem().getRegion("myRegion")
				.getROI().getBounds();
		int[] len = greenRectangle.getIntLengths();
		int[] pt = greenRectangle.getIntPoint();

		int[][] lenPt = { len, pt };

		RectangularROI[] bgRegionROI = ssp.trackingRegionOfInterestSetter(lenPt);

		ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgRegionROI[1]);

		try {
			IRegion u = ssvs.getPlotSystemCompositeView().getPlotSystem()
					.getRegion(DisplayLabelStrings.getInterpolatedTrajectory());
			ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(u);

			for (IRegion g : ssp.getInterpolatorRegions()) {
				ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(g);
				g.remove();

			}

		} catch (Exception k) {

		}

		if (ssp.getDrm().getFms().get(ssp.getSliderPos()).getTrackingMethodology() != TrackerType1.SPLINE_INTERPOLATION
				|| !ssp.getTrackerOn()) {

			button4.setEnabled(false);
			button5.setEnabled(false);

		}

		if (ssp.getDrm().getFms().get(ssp.getSliderPos()).getTrackingMethodology() != TrackerType1.USE_SET_POSITIONS
				|| !ssp.getTrackerOn()) {

			button4.setEnabled(false);
			button5.setEnabled(false);

			try {

				IRegion s = ssvs.getPlotSystemCompositeView().getPlotSystem()
						.getRegion(DisplayLabelStrings.getSetTrajectory());
				ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(s);

				for (IRegion g : ssp.getInterpolatorRegions()) {
					ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(g);
					g.remove();

				}

			} catch (Exception k) {

			}
		}

		if (ssp.getTrackerOn()) {

			if (ssp.getTrackerType() == TrackerType1.SPLINE_INTERPOLATION) {
				button4.setEnabled(true);
				button5.setEnabled(true);
			}

			if (ssp.getTrackerType() == TrackerType1.USE_SET_POSITIONS) {
				button4.setEnabled(false);
				button5.setEnabled(false);

				ssvs.useSetPositionTracker();

			}

			trackerOnButton.setText(DisplayLabelStrings.getTurnTrackerOff());

			ssp.setTrackerOn(trackerOn);
		}

		if (trackerOn) {
			trackerOnButton.setText(DisplayLabelStrings.getTurnTrackerOff());
		} else {
			trackerOnButton.setText(DisplayLabelStrings.getTurnTrackerOn());
			ssp.resetInterpolatorMethods();
			try {
				for (IRegion g : ssp.getInterpolatorRegions()) {
					ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(g);
					g.remove();

				}
			} catch (NullPointerException h) {

			}

			IRegion gr = ssvs.getPlotSystemCompositeView().getPlotSystem()
					.getRegion(DisplayLabelStrings.getGrayRegion());
			ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(gr);
		}
		try {
			ssvs.sliderMovementGeneralUpdate();
		} catch (NullPointerException g) {

		}

		if (AnalaysisMethodologies.toMethodology(methodologySelection) != Methodology.TWOD
				&& AnalaysisMethodologies.toMethodology(methodologySelection) != Methodology.X
				&& AnalaysisMethodologies.toMethodology(methodologySelection) != Methodology.Y) {

			boundaryBoxText.setEnabled(false);

		}

		else {
			boundaryBoxText.setEnabled(true);

		}
		
		backgroundCombo.setEnabled(true);

	}

	public Combo[] getCombos() {
		return new Combo[] { backgroundCombo, polynomialPowerCombo, trackingMethodCombo };
	}

	public void setMethodologyDropDown(String in) {
		Methodology in1 = AnalaysisMethodologies.toMethodology(in);
		setMethodologyDropDown(in1);
	}

	public void setMethodologyDropDown(Methodology in) {
		for (int i = 0; i < AnalaysisMethodologies.Methodology.values().length; i++) {
			if (in == AnalaysisMethodologies.Methodology.values()[i]) {
				backgroundCombo.select(i);
			}
		}
	}

	public void setFitPowerDropDown(FitPower m) {
		for (int i = 0; i < AnalaysisMethodologies.FitPower.values().length; i++) {
			if (m == AnalaysisMethodologies.FitPower.values()[i]) {
				polynomialPowerCombo.select(i);
			}
		}
	}

	public void setFitPowerDropDown(int in) {
		FitPower in1 = AnalaysisMethodologies.toFitPower(in);
		setFitPowerDropDown(in1);
	}

	public void setFitPowerDropDown1(String in) {
		FitPower m = AnalaysisMethodologies.toFitPower(Integer.parseInt(in));
		setFitPowerDropDown(m);
	}

	public void setTrackerTypeDropDown(TrackerType1 m) {
		for (int i = 0; i < TrackingMethodology.TrackerType1.values().length; i++) {
			if (m == TrackingMethodology.TrackerType1.values()[i]) {
				trackingMethodCombo.select(i);
			}
		}
	}

	public TrackerType1 getTrackerTypeDropDown() {
		return TrackingMethodology.toTracker1(trackingMethodCombo.getText());
	}

	public void setTrackerTypeDropDown(String in) {
		TrackerType1 m = TrackingMethodology.toTracker1(in);
		setTrackerTypeDropDown(m);
	}

	public void setBoundaryBox(int in) {
		boundaryBoxText.setSelection(in);
	}

	public void setBoundaryBox(String in) {
		boundaryBoxText.setSelection(Integer.valueOf(in));
	}

	public Button getRunButton() {
		return button1;
	}

	public Button getSaveButton() {
		return button2;
	}

	public Button getLoadButton() {
		return button3;
	}

	public Button getProceedButton() {
		return button;
	}

	public Button getTrackerOnButton() {
		return trackerOnButton;
	}

	public void checkTrackerOnButton() {

		if (!ssp.getTrackerOn()) {
			trackerOnButton.setText(DisplayLabelStrings.getTurnTrackerOn());
			button4.setEnabled(false);
			button5.setEnabled(false);
		} else {
			trackerOnButton.setText(DisplayLabelStrings.getTurnTrackerOn());
		}

	}

	public Button getAcceptLocation() {
		return button4;
	}

	public void setAcceptLocation(Button button4) {
		this.button4 = button4;
	}

	public Button getRejectLocation() {
		return button5;
	}

	public void setRejectLocation(Button button5) {
		this.button5 = button5;
	}

	public Button getUseQAxis() {
		return useQAxis;
	}

	public void setUseQAxis(Button useQAxis) {
		this.useQAxis = useQAxis;
	}

	class operationJob extends Job {

		private IDataset input;

		public operationJob() {
			super("updating image...");
		}

		public void setData(IDataset input) {
			this.input = input;
		}

		protected IStatus run(IProgressMonitor monitor) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					plotSystem1.clear();
					plotSystem1.updatePlot2D(input, null, monitor);
					plotSystem1.repaint(true);
				}

			});

			return Status.OK_STATUS;
		}
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
}
