package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.GoodPointStripper;
import org.dawnsci.surfacescatter.OverlapAttenuationObject;
import org.dawnsci.surfacescatter.OverlapDataModel;
import org.dawnsci.surfacescatter.OverlapDisplayObjects;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.dawnsci.surfacescatter.ReflectivityNormalisation;
import org.dawnsci.surfacescatter.ScannedVariableName;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class StitchedOverlapCurves extends Composite {

	private IPlottingSystem<Composite> plotSystem;
	private ILineTrace lt1;
	private SurfaceScatterPresenter ssp;
	private CurveStitchDataPackage csdp;
	private double[][] maxMinArray;
	private ArrayList<OverlapAttenuationObject> oAos;
	private ArrayList<OverlapDataModel> odms;
	private ArrayList<OverlapDisplayObjects> odos;
	private boolean modify = true;
	private TableViewer viewer;
	private OverlapDisplayObjects odo;
	private ArrayList<IDataset> xArrayList;
	private boolean useGoodPointsOnly = false;
	private Button showOnlyGoodPoints;
	private Button export;
	private Slider slider;
	private IRegion imageNo;
	private GeneralOverlapHandlerView gohv;

	public StitchedOverlapCurves(Composite parent, int style, ArrayList<IDataset> xArrayList,
			ArrayList<IDataset> yArrayList, ArrayList<IDataset> yArrayListError, ArrayList<IDataset> yArrayListFhkl,
			ArrayList<IDataset> yArrayListFhklError, ArrayList<IDataset> yArrayListRaw,
			ArrayList<IDataset> yArrayListRawError, String title, OverlapUIModel model, SurfaceScatterPresenter ssp,
			GeneralOverlapHandlerView gohv) {

		super(parent, style);

		new Label(this, SWT.NONE).setText(title);
		this.xArrayList = xArrayList;
		this.gohv = gohv;

		try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		this.ssp = ssp;

		this.createContents(yArrayList, yArrayListError, yArrayListFhkl, yArrayListFhklError, yArrayListRaw,
				yArrayListRawError, title, model);

	}

	public void createContents(ArrayList<IDataset> yArrayList, ArrayList<IDataset> yArrayListError,
			ArrayList<IDataset> yArrayListFhkl, ArrayList<IDataset> yArrayListFhklError,
			ArrayList<IDataset> yArrayListRaw, ArrayList<IDataset> yArrayListRawError, String filepaths,
			OverlapUIModel model) {

		csdp = ssp.curveStitchingOutput(null, false, null);

		Composite setupComposite = new Composite(this, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm form = new SashForm(setupComposite, SWT.FILL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form.setOrientation(SWT.VERTICAL);

		
		slider = new Slider(form, SWT.HORIZONTAL);
		slider.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		slider.setMinimum(0);
		slider.setMaximum(csdp.getSplicedCurveX().getSize());
		slider.setIncrement(1);
		slider.setThumb(1);
		
		Composite saveSettings = new Composite(form, SWT.NONE);
		GridLayout saveSettingsLayout = new GridLayout(3, true);
		saveSettings.setLayout(saveSettingsLayout);

		final GridData saveSettingsData = new GridData(SWT.FILL, SWT.FILL, true, true);
		saveSettingsData.grabExcessVerticalSpace = true;
		saveSettingsData.heightHint = 100;
		saveSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		showOnlyGoodPoints = new Button(saveSettings, SWT.PUSH);
		showOnlyGoodPoints.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showOnlyGoodPoints.setData(new GridData(SWT.FILL));
		showOnlyGoodPoints.setText("Show Only Good Points");

		showOnlyGoodPoints.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				flipUseGoodPointsOnly();
				refreshCurvesFromTable(model.getxAxis(), model.getyAxis());
			}
		});

		export = new Button(saveSettings, SWT.PUSH);
		export.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		export.setText("Export Curve");
		export.setSize(export.computeSize(100, 20, true));

		Button normalise = new Button(saveSettings, SWT.PUSH);
		normalise.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		normalise.setText("Normalise To Point");
		normalise.setSize(export.computeSize(100, 20, true));

		normalise.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				plotSystem.clear();

				AxisEnums.yAxes k = model.getyAxis();

				double normalisationPoint = normalisationValue(k);

				ReflectivityNormalisation.reflectivityNormalisationToAPoint(csdp, k, normalisationPoint);

				lt1 = plotSystem.createLineTrace("Concatenated Curve Test");

				IDataset x = getXIDataset(model.getxAxis());
				IDataset y = getYIDataset(model.getyAxis());

				lt1.setData(x, y);

				plotSystem.addTrace(lt1);
				plotSystem.repaint();
				lt1.setErrorBarEnabled(gohv.isErrorFlag());
			}
		});

	

		slider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				double xsl = (double) slider.getSelection();

				double xval = ssp.getFms().get((int) xsl).getScannedVariable();
				moveImageNoRegion(xval);

			}
		});

		/////////////////////////// TOP

		SashForm topForm = new SashForm(form, SWT.VERTICAL);
		topForm.setLayout(new GridLayout());
		topForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite stitchedCurves = new Composite(topForm, SWT.FILL | SWT.FILL);
		GridLayout stitchedCurvesLayout = new GridLayout(1, true);
		GridData stitchedCurvesData = new GridData(GridData.FILL_HORIZONTAL);
		stitchedCurves.setLayout(stitchedCurvesLayout);
		stitchedCurves.setLayoutData(stitchedCurvesData);
		
		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(stitchedCurves, null);

		final GridData gdSecondField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdSecondField.grabExcessVerticalSpace = true;
		gdSecondField.grabExcessVerticalSpace = true;

		plotSystem.createPlotPart(stitchedCurves, "Stitched Curves", actionBarComposite, PlotType.IMAGE, null);

		lt1 = plotSystem.createLineTrace("Concatenated Curve Test");

		IDataset x = getXIDataset(model.getxAxis());
		IDataset y = getYIDataset(model.getyAxis());

		lt1.setData(x, y);

		plotSystem.addTrace(lt1);
		plotSystem.repaint();

		maxMinArray = AttenuationCorrectedOutput.maxMinArrayGenerator(xArrayList, model);

		model.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				int g = slider.getSelection();

				maxMinArray = AttenuationCorrectedOutput.maxMinArrayGenerator(xArrayList, model);

				csdp = ssp.curveStitchingOutputFourier(maxMinArray, false, null);

				plotSystem.clear();

				resetAttenuationFactors(model.getxAxis(), model.getyAxis(), xArrayList, true);

				slider.setMinimum(0);
				slider.setMaximum(csdp.getSplicedCurveX().getSize());
				slider.setIncrement(1);
				slider.setThumb(1);
				slider.setSelection(g);

				buildLineTrace(model.getyAxis(), model.getxAxis());
				
				useGoodPointsOnly = model.isUseNegativevalues();
				checkGoodPointsLegend();
				

			}
		});

		plotSystem.getPlotComposite().setLayoutData(gdSecondField);
		
		plotSystem.getAxes().get(0).setTitle(ScannedVariableName.SCANNED_VARIABLE_NAME.getName());

		///////////// BOTTOM

		SashForm bottomForm = new SashForm(form, SWT.VERTICAL);
		bottomForm.setLayout(new GridLayout());
		bottomForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite overlapSelector = new Composite(bottomForm, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		GridLayout overlapSelectorLayout = new GridLayout(1, true);
		GridData overlapSelectorData = new GridData(GridData.FILL_BOTH);
		overlapSelector.setLayout(overlapSelectorLayout);
		overlapSelector.setLayoutData(overlapSelectorData);

		odms = csdp.getOverlapDataModels();

		odos = new ArrayList<OverlapDisplayObjects>();

		oAos = new ArrayList<OverlapAttenuationObject>();

		viewer = buildTable1(model.getxAxis(), model.getyAxis(), overlapSelector, xArrayList);

		viewer.getTable().setLayout(overlapSelectorLayout);
		viewer.getTable().setLayoutData(overlapSelectorData);

		viewer.getTable().pack();
		viewer.getTable().forceFocus();

		Composite bottomButtons = new Composite(bottomForm, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		GridLayout bottomButtonsLayout = new GridLayout(2, true);
		GridData bottomButtonsData = new GridData(GridData.FILL_BOTH);
		bottomButtons.setLayout(bottomButtonsLayout);
		bottomButtons.setLayoutData(bottomButtonsData);
		
		Button resetAll = new Button(bottomButtons, SWT.PUSH);
		resetAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resetAll.setData(new GridData(SWT.FILL));
		resetAll.setText("Reset All");

		resetAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (OverlapDisplayObjects odo : odos) {
					odo.setModified(false);
				}
				for (OverlapAttenuationObject oAo : oAos) {
					oAo.setModified(false);
				}

				resetAttenuationFactors(model.getxAxis(), model.getyAxis(), xArrayList, true);

			}
		});

		Button go = new Button(bottomButtons, SWT.PUSH);
		go.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		go.setData(new GridData(SWT.FILL));

		go.setText("Go");

		go.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateAttenuationFactors(model.getxAxis(), model.getyAxis(), xArrayList);

			}
		});

		RectangularROI r = new RectangularROI(csdp.getSplicedCurveX().getDouble(1), 0.1, 0, 0.1, 0);

		try {
			imageNo = plotSystem.createRegion("Image", RegionType.XAXIS_LINE);
			imageNo.setShowPosition(true);
			plotSystem.addRegion(imageNo);
			imageNo.setROI(r);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (imageNo != null) {
			imageNo.addROIListener(new IROIListener() {

				@Override
				public void roiSelected(ROIEvent evt) {
				}

				@Override
				public void roiDragged(ROIEvent evt) {
				}

				@Override
				public void roiChanged(ROIEvent evt) {

					int xPos = ssp.xPositionFinder(imageNo.getROI().getPointX());

					double yValue;

					AxisEnums.yAxes yA = model.getyAxis();

					switch (yA) {
					case SPLICEDYFHKL:
						yValue = ssp.getFms().get(xPos).getUnspliced_Fhkl_Intensity();
						break;
					case SPLICEDY:
						yValue = ssp.getFms().get(xPos).getUnspliced_Corrected_Intensity();
						break;
					case SPLICEDYRAW:
						yValue = ssp.getFms().get(xPos).getUnspliced_Raw_Intensity();
						break;
					default:
						// defensive only
					}
				}
			});
		}

//		int h = bottomForm.getChildren().length;
//	
		bottomForm.setWeights(new int[] {80,20});
		
		form.setWeights(new int[] { 5, 7, 73, 20 });

		IAxis yAxisR = plotSystem.getSelectedYAxis();

		yAxisR.setLog10(!yAxisR.isLog10());

		viewer.getTable().forceFocus();
		viewer.getTable().setFocus();

		this.getShell().addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				viewer.getTable().forceFocus();
				viewer.getTable().setFocus();
				viewer.getTable().forceFocus();

			}

			@Override
			public void controlMoved(ControlEvent e) {
				viewer.getTable().forceFocus();
				viewer.getTable().setFocus();
				viewer.getTable().forceFocus();
			}
		});
	}

	public Composite getComposite() {
		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem;
	}

	public ILineTrace getLineTrace1() {
		return lt1;
	}

	public CurveStitchDataPackage getCsdp() {
		return csdp;
	}

	public void setCsdp(CurveStitchDataPackage csdp) {
		this.csdp = csdp;
	}

	private void updateAttenuationFactors(AxisEnums.xAxes x, AxisEnums.yAxes y, ArrayList<IDataset> xArrayList) {

		for (OverlapDisplayObjects odo : odos) {
			if (odo.isModified()) {
				OverlapAttenuationObject oAo = odo.getOAo();
				oAos.set(odo.getOdoNumber(), oAo);
			}
		}

		csdp = ssp.curveStitchingOutput(maxMinArray, true, oAos);
		odms = csdp.getOverlapDataModels();

		for (int i = 0; i < odos.size(); i++) {

			OverlapDisplayObjects odo = odos.get(i);
			OverlapDataModel odm = odms.get(i);
			OverlapAttenuationObject oAo = oAos.get(i);

			if (odo.isModified()) {
				settingOdoFromOdm(odm, odo, oAo);
				odo.setModified(true);
				oAos.set(odo.getOdoNumber(), odo.getOAo());

			} else {
				settingOdoFromOdm(odm, odo, oAo);
				oAos.set(odo.getOdoNumber(), odo.getOAo());

			}
		}

		getTheRightCurve(y, x);

		plotSystem.clearTraces();
		plotSystem.addTrace(lt1);
		plotSystem.repaint();
	}

	private void resetAttenuationFactors(AxisEnums.xAxes x, AxisEnums.yAxes y, ArrayList<IDataset> xArrayList,
			boolean globalReset) {

		if (globalReset) {
			csdp = ssp.curveStitchingOutput(maxMinArray, true, null);
		} else {
			csdp = ssp.curveStitchingOutput(maxMinArray, true, oAos);
		}

		buildLineTrace(y, x);

		odms = csdp.getOverlapDataModels();

		for (int i = 0; i < odos.size(); i++) {

			OverlapDisplayObjects odo = odos.get(i);
			OverlapDataModel odm = odms.get(i);

			OverlapAttenuationObject oAo = odo.getOAo();

			if (!globalReset) {

				if (odo.isButtonPushed()) {

					settingOdoFromOdm(odm, odo, oAo);
					oAo = odo.getOAo();
				}
			} else {
				settingOdoFromOdm(odm, odo, oAo);
				oAo = odo.getOAo();
			}
			oAos.set(odo.getOdoNumber(), oAo);
		}

	}

	private void settingOdoFromOdm(OverlapDataModel odm, OverlapDisplayObjects odo, OverlapAttenuationObject oAo) {

		double l = odm.getAttenuationFactor();
		odo.getTextCorrected().setText(String.valueOf(l));
		odo.setTextCorrectedContent(l);

		double m = odm.getAttenuationFactorRaw();
		odo.getTextRaw().setText(String.valueOf(m));
		odo.setTextRawContent(m);

		double n = odm.getAttenuationFactorFhkl();
		odo.getTextFhkl().setText(String.valueOf(n));
		odo.setTextFhklContent(n);

		oAo.setModified(false);
		odo.setModified(false);
		odo.setButtonPushed(false);

	}

	private TableViewer buildTable1(AxisEnums.xAxes x, AxisEnums.yAxes y, Composite overlapSelector,
			ArrayList<IDataset> xArrayList) {

		ArrayList<OverlapAttenuationObject> oAos1 = new ArrayList<>();

		viewer = new TableViewer(overlapSelector,
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		for (int i = 0; i < odms.size(); i++) {

			OverlapDataModel odm = odms.get(i);

			OverlapDisplayObjects odol = new OverlapDisplayObjects();

			odol.generateFromOdmAndTable(odm, i);

			odos.add(odol);

			oAos1.add(odol.getOAo());
			try {
				if (oAos.size() >= (i - 1) && oAos.get(i) != null) {

					oAos1.get(i).setModified(oAos.get(i).isModified());

				}
			} catch (Exception h) {

			}

			odol.addPropertyChangeListener(new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (modify) {
						OverlapDisplayObjects odom = (OverlapDisplayObjects) evt.getSource();
						odom.setModified(true);

						if (odom.isButtonPushed()) {
							odom.setModified(false);
							try {
								oAos.get(odom.getOdoNumber()).setModified(false);
							} catch (Exception m) {

							}
						}

						resetAttenuationFactors(x, y, xArrayList, false);

					}
				}
			});

			oAos = oAos1;
		}

		// create the columns
		createColumns(viewer);

		// make lines and header visible
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.pack();

		viewer.setContentProvider(new ArrayContentProvider());

		// get the content for the viewer, setInput will call getElements in the
		viewer.setInput(odos);

		return viewer;
	}

	private void createColumns(final TableViewer viewer) {

		String[] titles = { "Overlap: ", "Corrected Attenuation: ", "Raw Attenuation: ", "Fhkl Attenuation: ",
				"Local Reset: " };

		int[] bounds = { 100, 100, 100, 100, 50 };

		// first column is for the overlap name
		TableViewerColumn col = createTableViewerColumn(viewer, titles[0], bounds[0]);

		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				odo = (OverlapDisplayObjects) element;
				return odo.getLabel();
			}
		});

		// second column is for the corrected data attenuation factor
		col = createTableViewerColumn(viewer, titles[1], bounds[1]);

		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {

				TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Text attenuationCorrected = new Text(item.getParent(), SWT.BORDER);

				odo.buildTextCorrected(attenuationCorrected);

				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(odo.getTextCorrected(), item, cell.getColumnIndex());
				editor.layout();

			}
		});

		// third column is for the raw data attenuation factor
		col = createTableViewerColumn(viewer, titles[2], bounds[2]);

		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {

				TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Text attenuationRaw = new Text(item.getParent(), SWT.BORDER);

				odo.buildTextRaw(attenuationRaw);

				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(odo.getTextRaw(), item, cell.getColumnIndex());
				editor.layout();

			}
		});

		// fourth column is for the Fhkl data attenuation factor
		col = createTableViewerColumn(viewer, titles[3], bounds[3]);
		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {

				TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Text attenuationFhkl = new Text(item.getParent(), SWT.BORDER);

				odo.buildTextFhkl(attenuationFhkl);

				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(odo.getTextFhkl(), item, cell.getColumnIndex());
				editor.layout();

			}
		});

		// fifth column is for the reset button
		col = createTableViewerColumn(viewer, titles[4], bounds[4]);
		col.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public void update(ViewerCell cell) {

				TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Button resetOverlap = new Button(item.getParent(), SWT.PUSH);
				resetOverlap.setText("Reset Overlap");
				odo.addResetListener(resetOverlap);

				editor.grabHorizontal = true;
				editor.grabVertical = true;
				editor.setEditor(odo.getResetOverlap(), item, cell.getColumnIndex());
				editor.layout();

			}
		});

	}

	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound) {

		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public void changeCurves(AxisEnums.yAxes y, AxisEnums.xAxes x) {

		Display display = Display.getCurrent();

		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		Color green = display.getSystemColor(SWT.COLOR_GREEN);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);

		plotSystem.clear();

		IDataset xD = getXIDataset(x);
		IDataset yD = getYIDataset(y);

		switch (y) {
		case SPLICEDY:
			lt1 = plotSystem.createLineTrace("Spliced Corrected Curve");
			lt1.setTraceColor(blue);
			break;
		case SPLICEDYFHKL:
			lt1 = plotSystem.createLineTrace("Spliced Fhkl Curve");
			lt1.setTraceColor(green);
			break;
		case SPLICEDYRAW:
			lt1 = plotSystem.createLineTrace("Spliced Raw Curve");
			lt1.setTraceColor(black);
			break;
		default:
			// purely defensive

		}

		lt1.setData(xD, yD);
		plotSystem.addTrace(lt1);

	}

	private void getTheRightCurve(AxisEnums.yAxes y, AxisEnums.xAxes x) {

		IDataset xD = getXIDataset(x);
		IDataset yD = getYIDataset(y);

		switch (y) {
		case SPLICEDY:
			lt1 = plotSystem.createLineTrace("Spliced Corrected Curve");
			break;
		case SPLICEDYFHKL:
			lt1 = plotSystem.createLineTrace("Spliced Fhkl Curve");
			break;
		case SPLICEDYRAW:
			lt1 = plotSystem.createLineTrace("Spliced Raw Curve");
			break;
		default:
			// purely defensive
		}

		lt1.setData(xD, yD);

	}

	public void resetAll(AxisEnums.xAxes x, AxisEnums.yAxes y) {
		resetAll(x, y, true);
	}

	public void resetAll(AxisEnums.xAxes x, AxisEnums.yAxes y, boolean global) {

		for (OverlapDisplayObjects odo : odos) {
			odo.setModified(false);
		}
		for (OverlapAttenuationObject oAo : oAos) {
			oAo.setModified(false);
		}

		resetAttenuationFactors(x, y, xArrayList, global);
	}

	private void flipUseGoodPointsOnly() {

		useGoodPointsOnly = !useGoodPointsOnly;
		checkGoodPointsLegend();
	}
	
	private void checkGoodPointsLegend() {
		
		if (!useGoodPointsOnly) {
			showOnlyGoodPoints.setText("Display All Points");
		} else {
			showOnlyGoodPoints.setText("Show Only Good Points");
		}
		
	}

	private void buildLineTrace(AxisEnums.yAxes yA, AxisEnums.xAxes xA) {

		plotSystem.clear();

		lt1 = plotSystem.createLineTrace(csdp.getRodName());

		GoodPointStripper gps = new GoodPointStripper();

		IDataset x = gps.splicedXGoodPointStripper(csdp, xA, useGoodPointsOnly);

		IDataset[] y = gps.splicedYGoodPointStripper(csdp, yA, useGoodPointsOnly);

		y[0].setErrors(y[1]);

		lt1.setData(x, y[0]);

		lt1.setErrorBarEnabled(gohv.isErrorFlag());

		plotSystem.addTrace(lt1);
	}

	public Button getExport() {
		return export;
	}

	private void refreshCurvesFromTable(AxisEnums.xAxes x, AxisEnums.yAxes y) {

		plotSystem.clear();

		buildLineTrace(y, x);

		plotSystem.autoscaleAxes();
	}

	public void addImageNoRegion(double j) {

		RectangularROI r = new RectangularROI(j, 0.1, 0, 0.1, 0);

		if (plotSystem.getRegion("Image") == null) {

			try {
				imageNo = plotSystem.createRegion("Image", RegionType.XAXIS_LINE);
			} catch (Exception x) {

			}

			imageNo.setShowPosition(true);
			imageNo.setROI(r);

			plotSystem.addRegion(imageNo);
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

	private IDataset getYIDataset(AxisEnums.yAxes y) {

		switch (y) {
		case SPLICEDY:
			return csdp.getSplicedCurveY();
		case SPLICEDYRAW:
			return csdp.getSplicedCurveYRaw();
		case SPLICEDYFHKL:
			return csdp.getSplicedCurveYFhkl();
		default:
			//
		}

		return csdp.getSplicedCurveY();
	}

	private IDataset getXIDataset(AxisEnums.xAxes x) {

		switch (x) {
		case SCANNED_VARIABLE:
			return csdp.getSplicedCurveX();
		case Q:
			return csdp.getSplicedCurveQ();
		default:
			//
		}

		return csdp.getSplicedCurveX();
	}

	private double normalisationValue(AxisEnums.yAxes k) {

		int xPos = ssp.xPositionFinder(imageNo.getROI().getPointX());

		double yValue = 1;

		switch (k) {
		case SPLICEDYFHKL:
			yValue = csdp.getSplicedCurveYFhkl().getDouble(xPos);
			break;

		case SPLICEDY:
			yValue = csdp.getSplicedCurveY().getDouble(xPos);
			break;

		case SPLICEDYRAW:
			yValue = csdp.getSplicedCurveYRaw().getDouble(xPos);
			break;

		default:
			// defensive only
		}

		return yValue;

	}
}