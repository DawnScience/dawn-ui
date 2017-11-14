package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.AxisEnums.xAxes;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.CsdpFromNexusFile;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.GoodPointStripper;
import org.dawnsci.surfacescatter.ReviewCurvesModel;
import org.dawnsci.surfacescatter.SavingFormatEnum;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class ReviewTabComposite extends Composite {

	private Group methodSetting;
	private SashForm form;
	private SashForm rightForm;
	private SashForm leftForm;
	private Button clearCurves;
	private Button addCurve;
	private Button selectAll;
	private Button remove;
	private Button showErrors;
	private Button save;
	private Button saveGoodPoints;
	private Combo outputFormatSelection;
	private IPlottingSystem<Composite> plotSystemReview;
	private ReviewCurvesModel rcm;
	private String nexusFolderPath;
	private boolean errorDisplayFlag = true;
	private Combo xAxis;
	private Combo yAxis;
	private Combo rodToSave;
	private AxisEnums.xAxes xAxisSelection = xAxes.SCANNED_VARIABLE;
	private AxisEnums.yAxes yAxisSelection = yAxes.SPLICEDY;
	private Table rodDisplayTable;
	private boolean selectDeslect = true;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private boolean useGoodPointsOnly = false;
	private Button showOnlyGoodPoints;
	private IRegion imageNo;

	public ReviewTabComposite(Composite parent, int style, SurfaceScatterPresenter ssp, SurfaceScatterViewStart ssvs)
			throws Exception {
		super(parent, style);

		this.ssvs = ssvs;
		this.ssp = ssp;

		try {
			plotSystemReview = PlottingFactory.createPlottingSystem();
			plotSystemReview.setTitle("Review Plot");

		} catch (Exception e2) {
			e2.printStackTrace();
		}
		setRcm(new ReviewCurvesModel());
		this.createContents();
	}

	public void createContents() throws Exception {

		Composite setupComposite = new Composite(this, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form = new SashForm(setupComposite, SWT.FILL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form.setOrientation(SWT.HORIZONTAL);

		/////////////////////////// left
		leftForm = new SashForm(form, SWT.VERTICAL);
		leftForm.setLayout(new GridLayout());
		leftForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group rodSelector = new Group(leftForm, SWT.V_SCROLL | SWT.FILL);
		GridLayout rodSelectorLayout = new GridLayout(1, true);
		GridData rodSelectorData = new GridData((GridData.FILL_BOTH));
		rodSelector.setLayout(rodSelectorLayout);
		rodSelector.setLayoutData(rodSelectorData);
		rodSelector.setText("Rods");

		clearCurves = new Button(rodSelector, SWT.PUSH | SWT.FILL);
		clearCurves.setText("Clear Curves");
		clearCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		clearCurves.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				for (int cv = 0; cv < rodDisplayTable.getItems().length; cv++) {
					rodDisplayTable.remove(cv);
				}

				rodDisplayTable.removeAll();
				plotSystemReview.clear();
				rcm.setCsdpList(null);
				rcm.setCsdpLatest(null);

			}

		});

		addCurve = new Button(rodSelector, SWT.PUSH);
		addCurve.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addCurve.setText("Add Curve");
		addCurve.setData(new GridData(SWT.FILL));

		addCurve.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog dlg = new FileDialog(ReviewTabComposite.this.getShell(), SWT.OPEN);

				if (nexusFolderPath != null) {
					dlg.setFilterPath(nexusFolderPath);
				}

				dlg.setText("Find a Nexus File!");

				String stitle = "r";
				String path = "p";

				if (dlg.open() != null) {
					stitle = dlg.getFileName();
					path = dlg.getFilterPath();

				}

				String title = path + File.separator + stitle;

				try {
					CurveStitchDataPackage newCsdp = CsdpFromNexusFile.CsdpFromNexusFileGenerator(title);
					rcm.addToCsdpList(newCsdp);
				} catch (NullPointerException n) {
					RegionOutOfBoundsWarning r = new RegionOutOfBoundsWarning(ssvs.getShell(), 16, null);
					r.open();
				}

			}
		});

		selectAll = new Button(rodSelector, SWT.PUSH | SWT.FILL);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		selectAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem f : rodDisplayTable.getItems()) {
					f.setChecked(selectDeslect);
				}

				if (selectDeslect) {
					selectDeslect = false;
					selectAll.setText("De-Select All");
				} else {
					selectDeslect = true;
					selectAll.setText("Select All");
				}

				refreshCurvesFromTable();
			}

		});

		remove = new Button(rodSelector, SWT.PUSH | SWT.FILL);
		remove.setText("Remove Selected");
		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		remove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				for (TableItem f : rodDisplayTable.getItems()) {
					if (f.getChecked()) {
						CurveStitchDataPackage csdp = bringMeTheOneIWant(f.getText(), rcm.getCsdpList());
						rcm.getCsdpList().remove(csdp);
					}
				}

				refreshTable();
				refreshCurves();
				plotSystemReview.autoscaleAxes();
			}

		});

		rodDisplayTable = new Table(rodSelector, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		rodDisplayTable.setEnabled(true);

		GridData rodDisplayTableData = new GridData(GridData.FILL_BOTH);

		rodDisplayTable.setLayoutData(rodDisplayTableData);
		rodDisplayTable.setLayout(new GridLayout());
		rodDisplayTable.getVerticalBar().setEnabled(true);

		rodDisplayTable.getVerticalBar().setEnabled(true);
		rodDisplayTable.getVerticalBar().setIncrement(1);
		rodDisplayTable.getVerticalBar().setThumb(1);

		rodDisplayTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshCurvesFromTable();
			}

		});

		//////////////////////////////// right
		rightForm = new SashForm(form, SWT.VERTICAL);
		rightForm.setLayout(new GridLayout());
		rightForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		methodSetting = new Group(rightForm, SWT.FILL | SWT.FILL);
		GridLayout methodSettingLayout = new GridLayout(1, true);
		GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
		methodSetting.setLayout(methodSettingLayout);
		methodSetting.setLayoutData(methodSettingData);
		methodSetting.setText("Rod Display");

		Group curveSettings = new Group(rightForm, SWT.NONE);
		GridLayout curveSettingsLayout = new GridLayout(2, true);
		curveSettings.setLayout(curveSettingsLayout);

		final GridData curveSettingsData = new GridData(SWT.FILL, SWT.FILL, true, true);
		curveSettingsData.grabExcessVerticalSpace = true;
		curveSettingsData.heightHint = 100;
		curveSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		Group saveSettings = new Group(rightForm, SWT.NONE);
		GridLayout saveSettingsLayout = new GridLayout(2, true);
		saveSettings.setLayout(saveSettingsLayout);

		final GridData saveSettingsData = new GridData(SWT.FILL, SWT.FILL, true, true);
		saveSettingsData.grabExcessVerticalSpace = true;
		saveSettingsData.heightHint = 100;
		saveSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		save = new Button(saveSettings, SWT.PUSH);
		save.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		save.setData(new GridData(SWT.FILL));
		save.setText("Save Single Rod");

		save.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRod(false);

			}
		});

		saveGoodPoints = new Button(saveSettings, SWT.PUSH);
		saveGoodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		saveGoodPoints.setData(new GridData(SWT.FILL));
		saveGoodPoints.setText("Save Only Good Points");

		saveGoodPoints.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveRod(true);

			}

		});

		showOnlyGoodPoints = new Button(saveSettings, SWT.PUSH);
		showOnlyGoodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showOnlyGoodPoints.setData(new GridData(SWT.FILL));
		showOnlyGoodPoints.setText("Show Only Good Points");

		showOnlyGoodPoints.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				flipUseGoodPointsOnly();
				refreshCurvesFromTable();
			}

		});

		Button storeAsNexus = new Button(saveSettings, SWT.PUSH);
		storeAsNexus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		storeAsNexus.setData(new GridData(SWT.FILL));
		storeAsNexus.setText("Store As Nexus");

		storeAsNexus.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(ssvs.getShell(), SWT.SAVE);

				if (ssp.getNexusPath() != null) {
					fd.setFilterPath(ssp.getNexusPath());
				}

				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				ssp.setNexusPath(path);

				String title = path + File.separator + stitle + ".nxs";

				ssp.setRodName(stitle);

				ssp.writeNexus(title, 10);

			}

		});

		Group storedCurves = new Group(rightForm, SWT.NONE);
		GridLayout storedCurvesLayout = new GridLayout();
		storedCurves.setLayout(storedCurvesLayout);

		final GridData storedCurvesData = new GridData(SWT.FILL, SWT.FILL, true, true);
		storedCurvesData.grabExcessVerticalSpace = true;
		storedCurvesData.heightHint = 100;
		storedCurves.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(storedCurves, null);

		plotSystemReview.createPlotPart(storedCurves, "Stored Curves", actionBarComposite, PlotType.IMAGE, null);

		plotSystemReview.getPlotComposite().setLayoutData(storedCurvesData);

		IAxis yAxisR = plotSystemReview.getSelectedYAxis();
		if (yAxisR != null) {
			yAxisR.setLog10(true);
		}

		showErrors = new Button(methodSetting, SWT.PUSH);
		showErrors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showErrors.setData(new GridData(SWT.FILL));
		showErrors.setText("Show Errors");

		showErrors.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (errorDisplayFlag == true) {
					errorDisplayFlag = false;
				} else {
					errorDisplayFlag = true;
				}

				refreshCurves();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button logPlot = new Button(methodSetting, SWT.PUSH);
		logPlot.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		logPlot.setData(new GridData(SWT.FILL));
		logPlot.setText("Log Plot On/Off");

		InputTileGenerator tile1 = new InputTileGenerator("X Axis:", curveSettings);
		xAxis = tile1.getCombo();
		xAxis.select(0);

		xAxis.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String xSelectionT = xAxis.getText();
				xAxes xSel = AxisEnums.toXAxis(xSelectionT);

				xAxisSelection = xSel;

				refreshCurves();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		InputTileGenerator tile2 = new InputTileGenerator("Y Axis:", curveSettings);
		yAxis = tile2.getCombo();
		yAxis.select(0);

		yAxis.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String ySelectionT = yAxis.getText();
				yAxes ySel = AxisEnums.toYAxis(ySelectionT);

				yAxisSelection = ySel;

				refreshCurves();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		InputTileGenerator tile3 = new InputTileGenerator("Curve To Save:", curveSettings);
		rodToSave = tile3.getCombo();
		rodToSave.select(0);

		outputFormatSelection = new Combo(curveSettings, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);

		for (SaveFormatSetting t : SavingFormatEnum.SaveFormatSetting.values()) {
			outputFormatSelection.add(SaveFormatSetting.toString(t));
		}

		outputFormatSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputFormatSelection.select(0);

		rightForm.setWeights(new int[] { 8, 10, 8, 74 });

		rcm.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				try {
					String[] yAxes = setYAxes();
					String[] xAxes = setXAxes();

					xAxis.removeAll();
					yAxis.removeAll();
					try {
						for (String f : yAxes) {
							yAxis.add(f);
						}
						for (String g : xAxes) {
							xAxis.add(g);
						}
					} catch (Exception i) {
						System.out.println(i.getMessage());
					}

					xAxis.setText(xAxisSelection.getXAxisName());
					yAxis.setText(yAxisSelection.getYAxisName());

					refreshCurves();
					refreshTable();
					plotSystemReview.autoscaleAxes();
				} catch (Exception b) {
					System.out.println(b.getMessage());
				}
			}
		});

		plotSystemReview.setShowLegend(true);

		form.setWeights(new int[] { 25, 75 });

		try {
			imageNo = plotSystemReview.createRegion("Image", RegionType.XAXIS_LINE);
			imageNo.setShowPosition(true);
			plotSystemReview.addRegion(imageNo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Display.getCurrent().addFilter(SWT.ALL, new Listener() {
			@Override
			public void handleEvent(Event event) {

				char key = event.character;

				switch (key) {

				case 'y':
					if (plotSystemReview.getPlotType().is1D()) {
						IAxis yAxisR = plotSystemReview.getSelectedYAxis();
						if (yAxisR != null) {
							yAxisR.setLog10(!yAxisR.isLog10());
						}
					}
					break;

				case 'x':
					if (plotSystemReview.getPlotType().is1D()) {

						IAxis xAxisR = plotSystemReview.getSelectedXAxis();
						if (xAxisR != null) {
							xAxisR.setLog10(!xAxisR.isLog10());
						}
					}
					break;

				default:
					break;

				}
			}
		});

		logPlot.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				plotSystemReview.getSelectedYAxis().setLog10(!(plotSystemReview.getSelectedYAxis().isLog10()));

			}

		});

	}

	public IRegion getImageNo() {
		return imageNo;
	}

	public void setImageNo(IRegion imageNo) {
		this.imageNo = imageNo;
	}

	public Composite getComposite() {

		return this;
	}

	public Group getMethodSetting() {
		return methodSetting;
	}

	public void setMethodSetting(Group methodSetting) {
		this.methodSetting = methodSetting;
	}

	public SashForm getForm() {
		return form;
	}

	public void setForm(SashForm form) {
		this.form = form;
	}

	public SashForm getRightForm() {
		return rightForm;
	}

	public void setRightForm(SashForm form) {
		this.rightForm = form;
	}

	public Button getClearCurves() {
		return clearCurves;
	}

	public void setClearCurves(Button clearCurves) {
		this.clearCurves = clearCurves;
	}

	public Button getAddCurve() {
		return addCurve;
	}

	public void setAddCurve(Button addCurve) {
		this.addCurve = addCurve;
	}

	public Button getShowErrors() {
		return showErrors;
	}

	public void setShowErrors(Button showErrors) {
		this.showErrors = showErrors;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystemReview;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystemReview = plotSystem;
	}

	public ReviewCurvesModel getRcm() {
		return rcm;
	}

	public void setRcm(ReviewCurvesModel rcm) {
		this.rcm = rcm;
	}

	public String getNexusFolderPath() {
		return nexusFolderPath;
	}

	public void setNexusFolderPath(String nexusFolderPath) {
		this.nexusFolderPath = nexusFolderPath;
	}

	private void saveRod(boolean writeOnlyGoodPoints) {

		SaveFormatSetting sfs = SaveFormatSetting.toMethod(outputFormatSelection.getText());
		Shell shell = ssvs.getShell();

		String rodSaveName = rodToSave.getText();

		if (rodSaveName.equals("")) {
			RegionOutOfBoundsWarning no = new RegionOutOfBoundsWarning(ssvs.getShell(), 14, null);
			no.open();
			return;
		}

		CurveStitchDataPackage csdpToSave = bringMeTheOneIWant(rodSaveName, rcm.getCsdpList());

		ssp.arbitrarySavingMethod(false, writeOnlyGoodPoints, shell, sfs, csdpToSave, yAxisSelection);
	}

	public void addImageNoRegion(double j) {

		RectangularROI r = new RectangularROI(j, 0.1, 0, 0.1, 0);

		if (plotSystemReview.getRegion("Image") == null) {

			try {
				imageNo = plotSystemReview.createRegion("Image", RegionType.XAXIS_LINE);
			} catch (Exception x) {

			}

			imageNo.setShowPosition(true);
			imageNo.setROI(r);

			plotSystemReview.addRegion(imageNo);
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

	private String[] setXAxes() {

		ArrayList<CurveStitchDataPackage> csdps = rcm.getCsdpList();
		ArrayList<String> outputList = new ArrayList<String>();

		boolean isXPresent = true;
		boolean isQPresent = true;

		for (CurveStitchDataPackage csdp : csdps) {
			if (csdp.getSplicedCurveX() == null) {
				isXPresent = false;
			} else {
				for (int i = 0; i < csdp.getSplicedCurveX().getSize(); i++) {
					double d = csdp.getSplicedCurveX().getDouble(i);

					if (d == -10000000000.0) {
						isXPresent = false;
						break;
					}
				}
			}

			if (csdp.getSplicedCurveQ() == null) {
				isQPresent = false;
			} else {
				try {

					for (int i = 0; i < csdp.getSplicedCurveQ().getSize(); i++) {
						double d = csdp.getSplicedCurveQ().getDouble(i);

						if (d == -10000000000.0) {
							isQPresent = false;
							break;
						}
					}
				} catch (Exception z) {
					isQPresent = false;
				}
			}
		}

		if (isXPresent) {
			outputList.add(xAxes.SCANNED_VARIABLE.getXAxisName());
		}
		if (isQPresent) {
			outputList.add(xAxes.Q.getXAxisName());
		}

		if (outputList.size() > 0) {
			String[] output = new String[outputList.size()];

			for (int y = 0; y < outputList.size(); y++) {
				output[y] = outputList.get(y);
			}

			return output;
		}

		else {
			return null;
		}
	}

	private String[] setYAxes() {

		ArrayList<CurveStitchDataPackage> csdps = rcm.getCsdpList();
		ArrayList<String> outputList = new ArrayList<String>();

		boolean isYPresent = true;
		boolean isYRawPresent = true;
		boolean isYFhklPresent = true;

		for (CurveStitchDataPackage csdp : csdps) {
			if (csdp.getSplicedCurveY() == null || csdp.getSplicedCurveY().getSize() == 0) {
				isYPresent = false;
			} else {
				for (int i = 0; i < csdp.getSplicedCurveY().getSize(); i++) {

					try {
						double d = csdp.getSplicedCurveY().getDouble(i);

						if (d == -10000000000.0) {
							isYPresent = false;
							break;
						}
					} catch (Exception y) {
						System.out.println("error, in get Y i:  " + i);

					}
				}
			}

			if (csdp.getSplicedCurveYRaw() == null || csdp.getSplicedCurveYRaw().getSize() == 0) {
				isYRawPresent = false;
			} else {
				for (int i = 0; i < csdp.getSplicedCurveYRaw().getSize(); i++) {
					try {
						double d = csdp.getSplicedCurveYRaw().getDouble(i);

						if (d == -10000000000.0) {
							isYRawPresent = false;
							break;
						}
					} catch (Exception y) {
						System.out.println("error, in get YRaw i:  " + i);

					}
				}
			}

			if (csdp.getSplicedCurveYFhkl() == null || csdp.getSplicedCurveYFhkl().getSize() == 0) {
				isYFhklPresent = false;
			} else {
				for (int i = 0; i < csdp.getSplicedCurveYFhkl().getSize(); i++) {
					try {
						double d = csdp.getSplicedCurveYFhkl().getDouble(i);

						if (d == -10000000000.0) {
							isYFhklPresent = false;
							break;
						}
					} catch (Exception y) {
						System.out.println("error, in get YFhkl i:  " + i);

					}
				}
			}
		}

		if (isYRawPresent) {
			outputList.add(AxisEnums.toString(yAxes.SPLICEDYRAW));
		}
		if (isYPresent) {
			outputList.add(AxisEnums.toString(yAxes.SPLICEDY));
		}
		if (isYFhklPresent) {
			outputList.add(AxisEnums.toString(yAxes.SPLICEDYFHKL));
		}

		if (!outputList.isEmpty()) {
			String[] output = new String[outputList.size()];

			for (int y = 0; y < outputList.size(); y++) {
				output[y] = outputList.get(y);
			}

			return output;
		}

		else {
			return null;
		}

	}

	private void refreshTable() {

		ArrayList<String> checked = new ArrayList<>();

		if (rodDisplayTable.getItems().length > 0) {
			for (TableItem de : rodDisplayTable.getItems()) {
				if (de.getChecked()) {
					checked.add(de.getText());
				}
			}

			for (int cv = 0; cv < rodDisplayTable.getItems().length; cv++) {
				rodDisplayTable.remove(cv);
			}

			rodDisplayTable.removeAll();

		}

		if (!rcm.getCsdpList().isEmpty()) {
			for (int j = 0; j < rcm.getCsdpList().size(); j++) {

				TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
				t.setText(rcm.getCsdpList().get(j).getRodName());
				String probe = rcm.getCsdpList().get(j).getRodName();

				for (String g : checked) {
					if (probe.equals(g)) {
						t.setChecked(true);
					}
				}
			}

			String latestAddition = rcm.getCsdpLatest().getRodName();

			for (TableItem ry : rodDisplayTable.getItems()) {

				if (ry.getText().equals(latestAddition)) {
					ry.setChecked(true);
				}

			}
		}

		rodToSave.removeAll();

		for (TableItem fg : rodDisplayTable.getItems()) {
			rodToSave.add(fg.getText());
		}
	}

	private ILineTrace buildLineTrace(CurveStitchDataPackage csdp) {

		ILineTrace lt = plotSystemReview.createLineTrace(csdp.getRodName());

		IDataset x = DatasetFactory.zeros(new int[] { 2, 2 }, Dataset.ARRAYFLOAT64);
		IDataset y[] = new IDataset[2];

		if (xAxisSelection == null) {
			xAxisSelection = xAxes.SCANNED_VARIABLE;

			boolean rg = true;

			for (String h : xAxis.getItems()) {
				if (xAxisSelection.getXAxisName().equals(h)) {
					rg = false;
				}
			}

			if (rg) {
				xAxis.add(xAxisSelection.getXAxisName());
			}

		}

		if (yAxisSelection == null) {
			yAxisSelection = yAxes.SPLICEDY;

			boolean rg = true;

			for (String h : yAxis.getItems()) {
				if (AxisEnums.toString(yAxisSelection).equals(h)) {
					rg = false;
				}
			}

			if (rg) {
				yAxis.add(AxisEnums.toString(yAxisSelection));
			}

		}

		GoodPointStripper gps = new GoodPointStripper();

		x = gps.splicedXGoodPointStripper(csdp, xAxisSelection, !useGoodPointsOnly);

		y = gps.splicedYGoodPointStripper(csdp, yAxisSelection, !useGoodPointsOnly);

		y[0].setErrors(y[1]);

		lt.setData(x, y[0]);

		lt.setErrorBarEnabled(errorDisplayFlag);

		return lt;
	}

	private void refreshCurves() {

		plotSystemReview.clear();

		if (!rcm.getCsdpList().isEmpty()) {
			for (CurveStitchDataPackage csdp : rcm.getCsdpList()) {

				ILineTrace lt = buildLineTrace(csdp);

				plotSystemReview.addTrace(lt);

			}
		}
	}

	public CurveStitchDataPackage bringMeTheOneIWant(String rodName, ArrayList<CurveStitchDataPackage> csdps) {

		for (CurveStitchDataPackage csdp : csdps) {
			if (rodName.equals(csdp.getRodName())) {
				return csdp;
			}
		}

		return null;
	}

	private void refreshCurvesFromTable() {

		plotSystemReview.clear();

		for (TableItem fd : rodDisplayTable.getItems()) {
			if (fd.getChecked()) {

				CurveStitchDataPackage csdp = bringMeTheOneIWant(fd.getText(), rcm.getCsdpList());

				buildLineTrace(csdp);

				ILineTrace lt = buildLineTrace(csdp);

				plotSystemReview.addTrace(lt);
				plotSystemReview.autoscaleAxes();
			}
		}
	}

	public SashForm getLeftForm() {
		return leftForm;
	}

	public void setLeftForm(SashForm leftForm) {
		this.leftForm = leftForm;
	}

	public Combo getRodToSave() {
		return rodToSave;
	}

	public Combo getOutputFormatSelection() {
		return outputFormatSelection;
	}

	public AxisEnums.yAxes getyAxisSelection() {
		return yAxisSelection;
	}

	public void setyAxisSelection(AxisEnums.yAxes yAxisSelection) {
		this.yAxisSelection = yAxisSelection;
	}

	public void addCurrentTrace(CurveStitchDataPackage csdp) {

		rcm.removeFromCsdpList(csdp);
		rcm.addToCsdpList(csdp);

	}

	private void flipUseGoodPointsOnly() {

		useGoodPointsOnly = !useGoodPointsOnly;

		if (useGoodPointsOnly) {
			showOnlyGoodPoints.setText("Include All Points");
		} else {
			showOnlyGoodPoints.setText("Disregard Bad Points");
		}
	}

}
