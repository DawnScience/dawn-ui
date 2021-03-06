package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.CsdpGeneratorFromDrm;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.GoodPointStripper;
import org.dawnsci.surfacescatter.OverlapFinder;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.dawnsci.surfacescatter.ScannedVariableName;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
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
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

public class OverlapCurves extends Composite {

	private Slider slider;
	private IPlottingSystem<Composite> plotSystem;
	private IRegion[] overlapRegionArray;
	private IRegion[] nonOverlapRegionArray;
	private Button errors;
	private ArrayList<ILineTrace> ltList;
	private Combo intensitySelect;
	private Combo xAxisSelect;
	private GeneralOverlapHandlerView gohv;
	private IRegion imageNo;
	private SurfaceScatterPresenter ssp;
	private Combo activeCurveCombo;
	private Button disregardPoint;
	private Text yValueText;
	private Text frameNoText;
	private CurveStitchDataPackage csdp;
	private Color red;

	public OverlapCurves(Composite parent, int style, ArrayList<IDataset> arrayILDy, ArrayList<IDataset> arrayILDx,
			String title, OverlapUIModel model, GeneralOverlapHandlerView gohv, SurfaceScatterPresenter ssp) {

		super(parent, style);

		new Label(this, SWT.NONE).setText(title);

		this.gohv = gohv;
		this.ssp = ssp;

		CsdpGeneratorFromDrm d = new CsdpGeneratorFromDrm();

		this.csdp = d.generateCsdpFromDrm(ssp.getDrm());

		overlapRegionArray = new IRegion[arrayILDy.size() - 1];
		nonOverlapRegionArray = new IRegion[arrayILDy.size() - 1];

		try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		this.createContents(arrayILDy, arrayILDx, model);
	}

	public void createContents(ArrayList<IDataset> arrayILDy, ArrayList<IDataset> arrayILDx, OverlapUIModel model) {

		model.getROIList().clear();

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		setLayout(gridLayout);

		Composite setupComposite = new Composite(this, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm form = new SashForm(setupComposite, SWT.FILL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		form.setOrientation(SWT.VERTICAL);

		Composite controls = new Composite(form, SWT.NULL);
		GridLayout controlsSelectionLayout = new GridLayout(3, true);
		GridData controlsSelectionData = new GridData(SWT.FILL, SWT.NULL, true, false);
		controls.setLayoutData(controlsSelectionData);
		controls.setLayout(controlsSelectionLayout);

		InputTileGenerator xAxisValue = new InputTileGenerator("X Axis: ", controls);

		xAxisSelect = xAxisValue.getCombo();

		for (AxisEnums.xAxes t : AxisEnums.xAxes.values()) {

			if (t != AxisEnums.xAxes.Q) {
				xAxisSelect.add(t.getXAxisName(), t.getXAxisNumber());
			}

			else if (t == AxisEnums.xAxes.Q && csdp.getqIDataset() != null) {
				xAxisSelect.add(t.getXAxisName(), t.getXAxisNumber());
			}
		}

		xAxisSelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		xAxisSelect.select(0);

		xAxisSelect.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				model.setxAxis(AxisEnums.toXAxis(xAxisSelect.getText()));

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		InputTileGenerator displayValue = new InputTileGenerator("Y Axis: ", controls);

		intensitySelect = displayValue.getCombo();

		for (AxisEnums.yAxes t : AxisEnums.yAxes.values()) {
			intensitySelect.add(t.getYAxisName(), t.getYAxisNumber());
		}

		intensitySelect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		intensitySelect.select(0);

		intensitySelect.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				model.setyAxis(AxisEnums.toYAxis(intensitySelect.getText()));

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		InputTileGenerator activeCurve = new InputTileGenerator("activeCurve: ", controls);

		for (String datName : ssp.getDatFilepaths()) {
			activeCurve.getCombo().add(datName);
		}

		activeCurveCombo = activeCurve.getCombo();
		activeCurveCombo.select(0);

		activeCurveCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int ui = activeCurveCombo.getSelectionIndex();

				int rg = ssp.getDrm().getDmxList().get(ui).size() / 2;
				int rf = ssp.getDrm().getDmxList().get(ui).size();

				slider.setMinimum(0);
				slider.setMaximum(rf);
				slider.setIncrement(1);
				slider.setThumb(1);

				slider.setSelection(ui);

				double hj = ssp.xValueFromDat(ui, rg);
				moveImageNoRegion(hj);

			}

		});

		Composite includeControls = new Composite(controls, SWT.NULL);
		GridLayout includeControlsLayout = new GridLayout(2, true);
		GridData includeControlsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		includeControls.setLayoutData(includeControlsData);
		includeControls.setLayout(includeControlsLayout);

		disregardPoint = new Button(includeControls, SWT.PUSH);
		disregardPoint.setText("Disregard Point");
		disregardPoint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		disregardPoint.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				double xValue = OverlapCurves.this.imageNo.getROI().getPointX();
				int datNo = activeCurveCombo.getSelectionIndex();

				int frameNo = ssp.absoluteFrameNumberUsingXValueFromSpecifiedDatNo(datNo, xValue);

				if (ssp.isGoodPoint(frameNo)) {
					ssp.setGoodPoint(frameNo, false);
				} else {
					ssp.setGoodPoint(frameNo, true);
				}

				if (ssp.isGoodPoint(frameNo)) {
					disregardPoint.setText("Disregard Point");
				} else {
					disregardPoint.setText("Include Point");
				}

				addCurves();

				model.poke();

				gohv.getStitchedCurves().resetAll(model.getxAxis(), model.getyAxis(), true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Button includeAll = new Button(includeControls, SWT.PUSH);
		includeAll.setText("Include All");
		includeAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		includeAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.allGoodPoints();

				disregardPoint.setText("Disregard Point");
				addCurves();
				gohv.getStitchedCurves().resetAll(model.getxAxis(), model.getyAxis(), true);

				model.setUseNegativevalues(true);
				model.poke();
			}
		});

		InputTileGenerator scannedPoint = new InputTileGenerator("X Value:  ", "0", controls);
		frameNoText = scannedPoint.getText();

		InputTileGenerator yPoint = new InputTileGenerator("Y Value:  ", "0", controls);
		yValueText = yPoint.getText();

		// Composite normGroup = new Composite(form, SWT.NONE);
		// GridLayout normGroupLayout = new GridLayout(1, true);
		// normGroup.setLayout(normGroupLayout);
		// GridData normGroupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		// normGroupData.grabExcessVerticalSpace = true;
		// normGroupData.heightHint = 100;
		// normGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |
		// GridData.FILL_VERTICAL));

		slider = new Slider(controls, SWT.HORIZONTAL);

		int rf = ssp.getDrm().getDmxList().get(0).size();

		slider.setMinimum(0);
		slider.setMaximum(rf);
		slider.setIncrement(1);
		slider.setThumb(1);
		slider.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		errors = new Button(controls, SWT.PUSH);
		errors.setText("Errors");
		errors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button removeNegativeIntensities = new Button(controls, SWT.PUSH);
		removeNegativeIntensities.setText("Remove Negative Intensities");
		removeNegativeIntensities.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		removeNegativeIntensities.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.disregardNegativeIntensities();
				model.poke();
				model.setUseNegativevalues(false);
				addCurves();
			}

		});

		Composite unstitchedCurves = new Composite(form, SWT.FILL | SWT.FILL);
		GridLayout stitchedCurvesLayout = new GridLayout(1, true);
		GridData stitchedCurvesData = new GridData(GridData.FILL_HORIZONTAL);
		unstitchedCurves.setLayout(stitchedCurvesLayout);
		unstitchedCurves.setLayoutData(stitchedCurvesData);
		// unstitchedCurves.setText("Unstitched Curves");

		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(unstitchedCurves, null);

		final GridData gdSecondField = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdSecondField.grabExcessVerticalSpace = true;
		gdSecondField.grabExcessVerticalSpace = true;

		plotSystem.createPlotPart(unstitchedCurves, "Seperate Data Curves", actionBarComposite, PlotType.IMAGE, null);

		addCurves();

		plotSystem.getPlotComposite().setLayoutData(gdSecondField);

		plotSystem.getAxes().get(0).setTitle(ScannedVariableName.SCANNED_VARIABLE_NAME.getName());

		ArrayList<IRectangularROI> roiList = new ArrayList<IRectangularROI>();
		IRectangularROI nullROI = null;

		for (int yr = 0; yr < overlapRegionArray.length; yr++) {
			roiList.add(nullROI);
		}

		model.setROIList(roiList);

		String root = "RegionNo:";
		int k = 0;

		double[][] overlap = OverlapFinder.overlapFinderOperation(arrayILDx);
		double[][] nonOverlap = OverlapFinder.nonOverlapFinderOperation(arrayILDx);

		Display display = Display.getCurrent();
		red = display.getSystemColor(SWT.COLOR_RED);

		for (k = 0; k < (model.getROIList().size()); k++) {

			if (overlap[k][1] < 999999) {

				roiList.set(k, new RectangularROI(overlap[k][1], 0.1, overlap[k][0] - overlap[k][1], 0.1, 0));

				String regionName = root + Integer.toString(k);

				try {
					overlapRegionArray[k] = plotSystem.createRegion(regionName, RegionType.XAXIS);
					overlapRegionArray[k].setROI(roiList.get(k));
					plotSystem.addRegion(overlapRegionArray[k]);

				} catch (Exception e1) {
					e1.printStackTrace();
				}

				model.setROIList(roiList);

				int ktemp = k;
				((IRegion) overlapRegionArray[k]).addROIListener(new IROIListener() {

					@Override
					public void roiDragged(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);
					}

					@Override
					public void roiChanged(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);

					}

					@Override
					public void roiSelected(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);
					}

				});
			}

			else {
				String regionName = root + Integer.toString(k);

				RectangularROI redROI = new RectangularROI(nonOverlap[k][1], 0.1, nonOverlap[k][0] - nonOverlap[k][1],
						0.1, 0);

				try {
					IRegion redRegion = plotSystem.createRegion(regionName, RegionType.XAXIS);
					redRegion.setROI(redROI);
					plotSystem.addRegion(redRegion);
					redRegion.setRegionColor(red);
					redRegion.setMobile(false);

					nonOverlapRegionArray[k] = redRegion;

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		double f = (double) (ssp.getDrm().getDmxList().get(activeCurveCombo.getSelectionIndex()).size() / 2);
		addImageNoRegion(f);
		imageNoAddListener();

		slider.setSelection((int) f);

		slider.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				double xsl = (double) slider.getSelection();
				int datNo = activeCurveCombo.getSelectionIndex();

				double xval = ssp.xValueFromDat(datNo, xsl);
				moveImageNoRegion(xval);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Composite resetGroup = new Composite(form, SWT.FILL | SWT.FILL);
		GridLayout resetGroupLayout = new GridLayout(1, true);
		GridData resetGroupData = new GridData(GridData.FILL_HORIZONTAL);
		resetGroup.setLayout(resetGroupLayout);
		resetGroup.setLayoutData(resetGroupData);

		Button resetButton = new Button(resetGroup, SWT.PUSH);
		resetButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resetButton.setText("Reset Overlaps");

		//////////////////////////////////////////////////////////////////////////////////

		resetButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				buildAndSetOverlapRegions(model, arrayILDx, arrayILDy);

				gohv.getStitchedCurves().resetAll(model.getxAxis(), model.getyAxis());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		form.setWeights(new int[] { 20, 67, 4 });

		IAxis yAxisR = plotSystem.getSelectedYAxis();

		yAxisR.setLog10(!yAxisR.isLog10());

	}

	public Combo getxAxisSelect() {
		return xAxisSelect;
	}

	public void setxAxisSelect(Combo xAxisSelect) {
		this.xAxisSelect = xAxisSelect;
	}

	public Composite getComposite() {

		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem;
	}

	public Button getErrorsButton() {
		return errors;
	}

	public ArrayList<ILineTrace> getILineTraceList() {
		return ltList;
	}

	public Combo getIntensity() {
		return intensitySelect;
	}

	private void addCurves() {

		CsdpGeneratorFromDrm csdpg = new CsdpGeneratorFromDrm();
		csdp = csdpg.generateCsdpFromDrm(ssp.getDrm());

		for (ITrace it : plotSystem.getTraces()) {
			plotSystem.removeTrace(it);
		}

		IDataset i = null;
		IDataset j = null;
		IDataset l = null;

		ltList = new ArrayList<ILineTrace>();

		GoodPointStripper gps = new GoodPointStripper();

		AxisEnums.yAxes yM = AxisEnums.toYAxis(intensitySelect.getText());
		AxisEnums.xAxes xM = AxisEnums.toXAxis(xAxisSelect.getText());

		IDataset coordinateDatasets[][] = gps.goodPointStripper(csdp, yM, xM);

		for (int r = 0; r < coordinateDatasets.length; r++) {
			if (coordinateDatasets[r] != null) {
				j = coordinateDatasets[r][0];
				i = coordinateDatasets[r][1];
				l = coordinateDatasets[r][2];

				i.setErrors(l);

				ILineTrace lt = plotSystem.createLineTrace(
						Double.toString(j.getDouble(0)) + "-" + Double.toString(j.getDouble(j.getSize() - 1)));
				lt.setData(j, i);

				ltList.add(lt);

				plotSystem.addTrace(lt);
				plotSystem.repaint();

				lt.setErrorBarEnabled(gohv.isErrorFlag());
			}
		}

	}

	private void imageNoAddListener() {
		imageNo.addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
			}

			@Override
			public void roiDragged(ROIEvent evt) {
			}

			@Override
			public void roiChanged(ROIEvent evt) {

				int xPos = 0;

				AxisEnums.xAxes xM = AxisEnums.toXAxis(xAxisSelect.getText());

				switch (xM) {
				case SCANNED_VARIABLE:
					xPos = ssp.xPositionFinder(imageNo.getROI().getPointX());
					break;
				case Q:
					xPos = ssp.qPositionFinder(imageNo.getROI().getPointX());
				}

				int datNo = activeCurveCombo.getSelectionIndex();
				double xPoint = imageNo.getROI().getPointX();

				int xPosInDat = ssp.xPositionFinderInDat(datNo, xPoint);
				double xX = ssp.xValueFromDat(datNo, xPosInDat);

				frameNoText.setText(String.valueOf(xX));

				double yValue = 0;

				String k = intensitySelect.getText();

				AxisEnums.yAxes yM = AxisEnums.toYAxis(k);

				switch (yM) {
				case SPLICEDYFHKL:
					yValue = ssp.getUnsplicedFhklIntensityFromFm(activeCurveCombo.getSelectionIndex(), xPosInDat);

					break;
				case SPLICEDY:
					yValue = ssp.getUnsplicedCorrectedIntensityFromFm(activeCurveCombo.getSelectionIndex(), xPosInDat);
					break;
				case SPLICEDYRAW:
					yValue = ssp.getUnsplicedRawIntensityFromFm(activeCurveCombo.getSelectionIndex(), xPosInDat);
					break;
				default:
					// defensive only
				}

				yValueText.setText(String.valueOf(yValue));

				if (ssp.isGoodPoint(xPos)) {
					disregardPoint.setText("Disregard Point");
				} else {
					disregardPoint.setText("Include Point");
				}

				int frameNo = ssp.absoluteFrameNumberUsingXValueFromSpecifiedDatNo(datNo, xPoint);

				if (ssp.isGoodPoint(frameNo)) {
					disregardPoint.setText("Disregard Point");
				} else {
					disregardPoint.setText("Include Point");
				}
			}
		});

	}

	private void buildAndSetOverlapRegions(OverlapUIModel model, ArrayList<IDataset> arrayILDx,
			ArrayList<IDataset> arrayILDy) {

		IROI imN = imageNo.getROI().copy();

		plotSystem.clearRegions();

		try {
			imageNo = plotSystem.createRegion("Point Selector", RegionType.XAXIS_LINE);
		} catch (Exception e1) {

		}

		imageNo.setROI(imN);

		imageNo.setShowPosition(true);
		plotSystem.addRegion(imageNo);

		imageNo.toFront();

		imageNoAddListener();

		ArrayList<IRectangularROI> roiList = new ArrayList<IRectangularROI>();
		IRectangularROI nullROI = null;
		overlapRegionArray = new IRegion[arrayILDy.size() - 1];

		for (int yr = 0; yr < overlapRegionArray.length; yr++) {
			roiList.add(nullROI);
		}

		String root = "RegionNo:";
		int k = 0;

		double[][] overlap = OverlapFinder.overlapFinderOperation(arrayILDx);
		double[][] nonOverlap = OverlapFinder.nonOverlapFinderOperation(arrayILDx);

		for (k = 0; k < (model.getROIList().size()); k++) {

			if (overlap[k][1] < 999999) {

				roiList.set(k, new RectangularROI(overlap[k][1], 0.1, overlap[k][0] - overlap[k][1], 0.1, 0));

				String regionName = root + Integer.toString(k);

				try {
					overlapRegionArray[k] = plotSystem.createRegion(regionName, RegionType.XAXIS);
					overlapRegionArray[k].setROI(roiList.get(k));
					plotSystem.addRegion(overlapRegionArray[k]);

				} catch (Exception e1) {
					e1.printStackTrace();
				}

				model.setROIList(roiList);

				int ktemp = k;
				((IRegion) overlapRegionArray[k]).addROIListener(new IROIListener() {

					@Override
					public void roiDragged(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);
					}

					@Override
					public void roiChanged(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);

					}

					@Override
					public void roiSelected(ROIEvent evt) {
						model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(), ktemp);
					}

				});
			}

			else {
				String regionName = root + Integer.toString(k);

				RectangularROI redROI = new RectangularROI(nonOverlap[k][1], 0.1, nonOverlap[k][0] - nonOverlap[k][1],
						0.1, 0);

				try {
					IRegion redRegion = plotSystem.createRegion(regionName, RegionType.XAXIS);
					redRegion.setROI(redROI);
					plotSystem.addRegion(redRegion);
					redRegion.setRegionColor(red);
					redRegion.setMobile(false);

					nonOverlapRegionArray[k] = redRegion;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		//
		// for (k = 0; k < (roiList.size()); k++) {
		//
		// if (overlap[k][1] < 999999) {
		//
		// if (DEBUG == 1) {
		// System.out.println("k in overlapCurves: " + k);
		// System.out.println("overlap[k][1]: " + overlap[k][1]);
		// System.out.println("overlap[k][0]: " + overlap[k][0]);
		// System.out.println("roiList.size() : " + roiList.size());
		// }
		//
		// roiList.set(k, new RectangularROI(overlap[k][1], 0.1, overlap[k][0] -
		// overlap[k][1], 0.1, 0));
		//
		// String regionName = root + Integer.toString(k);
		//
		// try {
		// overlapRegionArray[k] = plotSystem.createRegion(regionName,
		// RegionType.XAXIS);
		// overlapRegionArray[k].setROI(roiList.get(k));
		// overlapRegionArray[k].toBack();
		// plotSystem.addRegion(overlapRegionArray[k]);
		//
		// } catch (Exception e1) {
		// e1.printStackTrace();
		// }
		//
		// int ktemp = k;
		// ((IRegion) overlapRegionArray[k]).addROIListener(new IROIListener() {
		//
		// @Override
		// public void roiDragged(ROIEvent evt) {
		// model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(),
		// ktemp);
		// }
		//
		// @Override
		// public void roiChanged(ROIEvent evt) {
		// model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(),
		// ktemp);
		//
		// }
		//
		// @Override
		// public void roiSelected(ROIEvent evt) {
		// model.setROIListElementEst(overlapRegionArray[ktemp].getROI().getBounds(),
		// ktemp);
		// }
		//
		// });
		// }
		// }

		model.setROIList(roiList);
		// model.setROIListElementEst(overlapRegionArray[1].getROI().getBounds(), 1);
		model.poke();
	}

	public void changeCurves(AxisEnums.xAxes x, AxisEnums.yAxes y, CurveStitchDataPackage csdp) {

		addCurves();
	}

	public IRegion getImageNo() {
		return imageNo;
	}

	public void setImageNo(IRegion imageNo) {
		this.imageNo = imageNo;
	}

	public void addImageNoRegion(double j) {

		RectangularROI r = new RectangularROI(j, 0.1, 0, 0.1, 0);

		if (plotSystem.getRegion("Image") == null) {

			try {
				imageNo = plotSystem.createRegion("Image", RegionType.XAXIS_LINE);
			} catch (Exception x) {

			}

			imageNo.toFront();
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
}
