package org.dawnsci.surfacescatter.ui;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.OutputException;
import org.dawnsci.surfacescatter.SavingFormatEnum;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class MultipleOutputCurvesTableView extends Composite {

	private IPlottingSystem<Composite> plotSystem4;
	private IRegion imageNo;
	private ILineTrace lt;
	private Button sc;
	private Button save;
	private Combo intensitySelect;
	private Combo outputFormatSelection;
	private Group overlapSelection;
	private Button errors;
	private Button overlapZoom;
	private Button qAxis;
	private Button storeAsNexus;
	private IRegion marker;
	private SurfaceScatterPresenter ssp;

	public MultipleOutputCurvesTableView(Composite parent, int style, int extra, SurfaceScatterPresenter ssp) {

		super(parent, style);

		this.ssp = ssp;

		try {
			plotSystem4 = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		this.createContents();

	}

	public void createContents() {

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		overlapSelection = new Group(sashForm, SWT.NULL);
		GridLayout overlapSelectionLayout = new GridLayout(4, true);
		GridData overlapSelectionData = new GridData(SWT.FILL);
		overlapSelectionData.minimumWidth = 50;
		overlapSelection.setLayout(overlapSelectionLayout);
		overlapSelection.setLayoutData(overlapSelectionData);

		errors = new Button(overlapSelection, SWT.PUSH | SWT.FILL);
		errors.setText("Errors");
		errors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		intensitySelect = new Combo(overlapSelection, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);

		for (AxisEnums.yAxes t : AxisEnums.yAxes.values()) {
			intensitySelect.add(t.getYAxisName(), t.getYAxisNumber());
		}

		intensitySelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		intensitySelect.select(0);

		outputFormatSelection = new Combo(overlapSelection, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);

		for (SaveFormatSetting t : SavingFormatEnum.SaveFormatSetting.values()) {
			outputFormatSelection.add(SaveFormatSetting.toString(t));
		}

		outputFormatSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputFormatSelection.select(0);

		save = new Button(overlapSelection, SWT.PUSH | SWT.FILL);
		save.setText("Save Spliced");
		save.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRod(false);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		setLayout(gridLayout);

		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(sashForm, null);

		final GridData gdSecondField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdSecondField.grabExcessVerticalSpace = true;
		gdSecondField.grabExcessVerticalSpace = true;
		gdSecondField.heightHint = 100;

		plotSystem4.createPlotPart(sashForm, "Progress Plot", actionBarComposite, PlotType.IMAGE, null);

		lt = plotSystem4.createLineTrace("Blank Curve");
		IDataset backup = DatasetFactory.createRange(0, 200, 1, Dataset.FLOAT64);
		lt.setData(backup, backup);

		plotSystem4.addTrace(lt);
		try {
			imageNo = plotSystem4.createRegion("Image", RegionType.XAXIS_LINE);
			imageNo.setShowPosition(true);
			plotSystem4.addRegion(imageNo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		plotSystem4.getPlotComposite().setLayoutData(gdSecondField);

		Group extraButtons = new Group(sashForm, SWT.NULL);
		GridLayout extraButtonsLayout = new GridLayout(3, true);
		GridData extraButtonsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		extraButtons.setLayout(extraButtonsLayout);
		extraButtons.setLayoutData(extraButtonsData);

		overlapZoom = new Button(extraButtons, SWT.PUSH);
		overlapZoom.setText("Overlap Zoom");
		overlapZoom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		qAxis = new Button(extraButtons, SWT.CHECK);
		qAxis.setText("q Axis");
		qAxis.setEnabled(false);
		qAxis.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		storeAsNexus = new Button(extraButtons, SWT.PUSH);
		storeAsNexus.setText("Store As Nexus");
		storeAsNexus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		sashForm.setWeights(new int[] { 10, 7, 75, 8 });
	}

	public Button getqAxis() {
		return qAxis;
	}

	public void setqAxis(Button qAxis) {
		this.qAxis = qAxis;
	}

	public Composite getComposite() {

		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem4;
	}

	public IRegion getRegionNo() {
		return imageNo;
	}

	public void resetCurve() {

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				plotSystem4.clear();
				lt = plotSystem4.createLineTrace("Output Curve");
				IDataset backup = DatasetFactory.createRange(0, 200, 1, Dataset.FLOAT64);
				lt.setData(backup, backup);
				plotSystem4.addTrace(lt);
				plotSystem4.repaint();
			}
		});

	}

	public void addToDatSelector() {
		if (this.getSc() == null) {
			sc = new Button(overlapSelection, SWT.CHECK);
			sc.setText("Spliced Curve");
			sc.setSelection(true);
			overlapSelection.layout(true, true);
			overlapSelection.setSize(overlapSelection.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			this.redraw();
			this.update();
		}
	}

	public Button getSc() {
		return sc;
	}

	public Group getOverlapSelectionGroup() {
		return overlapSelection;
	}

	public Combo getIntensity() {
		return intensitySelect;
	}

	public Combo getOutputFormatSelection() {
		return outputFormatSelection;
	}

	public Button getOverlapZoom() {
		return overlapZoom;
	}

	public Button getSave() {
		return save;
	}

	public Button getErrorsButton() {
		return errors;
	}

	public IRegion getMarker() {
		return marker;
	}

	public void addImageNoRegion(double j) {

		RectangularROI r = new RectangularROI(j, 0.1, 0, 0.1, 0);

		if (plotSystem4.getRegion("Image") == null) {

			try {
				imageNo = plotSystem4.createRegion("Image", RegionType.XAXIS_LINE);
			} catch (Exception x) {
				System.out.println(x.getMessage());
			}

			imageNo.setShowPosition(true);
			imageNo.setROI(r);

			plotSystem4.addRegion(imageNo);
			imageNo.setShowPosition(true);
		}

		else {
			moveImageNoRegion(j);
		}
	}

	public void moveImageNoRegion(double j) {

		RectangularROI r = new RectangularROI(j, 0.1, 0, 0.1, 0);
		imageNo.setROI(r);
	}

	public Button getStoreAsNexus() {
		return storeAsNexus;
	}

	public void setStoreAsNexus(Button storeAsNexus) {
		this.storeAsNexus = storeAsNexus;
	}

	private void saveRod(boolean writeOnlyGoodPoints) {

		SaveFormatSetting sfs = SaveFormatSetting.toMethod(outputFormatSelection.getText());
		Shell shell = this.getShell();

		AxisEnums.yAxes yAxisSelection = AxisEnums.yAxes.SPLICEDY;

		for (AxisEnums.yAxes t : AxisEnums.yAxes.values()) {
			if (intensitySelect.getText().equals(t.getYAxisName())) {
				yAxisSelection = t;
			}
		}

		CurveStitchDataPackage csdpToSave = ssp.getDrm().getCsdp();

		boolean useQ = qAxis.getSelection();
		try {
			ssp.arbitrarySavingMethod(useQ, writeOnlyGoodPoints, shell, sfs, csdpToSave, yAxisSelection);
		} catch (OutputException o) {
			ssp.outputErrorWarning(o.getMessage());
		}
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	public void changeXaxisLabel(String in) {

		plotSystem4.getAxes().get(0).setTitle(in);

	}
}
