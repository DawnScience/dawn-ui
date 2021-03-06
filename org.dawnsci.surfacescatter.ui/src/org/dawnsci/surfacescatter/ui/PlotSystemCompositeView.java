package org.dawnsci.surfacescatter.ui;

import java.util.Arrays;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.DisplayLabelStrings;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class PlotSystemCompositeView extends Composite {

	private Slider slider;
	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> subImageBgPlotSystem;
	private PlotSystem1CompositeView customComposite1;
	private IDataset image;
	private IRegion region;
	private IRegion bgRegion;
	private IRegion secondBgRegion;
	private Button run;
	private Combo processingMode;;
	private int numberOfImages;
	private Dataset nullImage;
	private SurfaceScatterPresenter ssp;
	private Text xValue;
	private Text imageNumber;
	private Button replay;
	private Button go;
	private TabFolder folder;
	private Text xCoord;
	private Text xLen;
	private Text yCoord;
	private Text yLen;
	private Text lorentz;
	private Text polarisation;
	private Text areaCorrection;
	private Text rawIntensity;
	private Text reflectivityAreaCorr;
	private Text reflectivityFluxCorr;
	private SashForm form;
	private TabItem subBgI;
	private TabItem correctionsTab;
	private GeometricParametersRepeaterTable gprt;
	private RodComponentsDisplay rcd;
	private Button centreSecondBgRegion;
	private Button disregardFrame;
	private Button includeAllFrames;
	private Button increment;
	private Button decrement;
	private Composite manualControls;
	private SurfaceScatterViewStart ssvs;
	private Button accept;
	private Button acceptBack;
	private Label tile1Label;

	public PlotSystemCompositeView(Composite parent, int style, IDataset image, int numberOfImages, Dataset nullImage,
			SurfaceScatterPresenter ssp, SurfaceScatterViewStart ssvs) {
		super(parent, style);
		this.numberOfImages = numberOfImages;
		this.nullImage = nullImage;
		this.ssp = ssp;
		this.ssvs = ssvs;

		try {
			plotSystem = PlottingFactory.createPlottingSystem();
			plotSystem.setTitle("Raw Image");

		} catch (Exception e2) {
			e2.printStackTrace();
		}

		this.createContents(image);
	}

	public void createContents(IDataset image) {

		this.image = image;

		ssp.addStateListener(new IPresenterStateChangeEventListener() {

			@Override
			public void update() {
				generalUpdate();

			}
		});

		Display display = Display.getCurrent();
		Color gold = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
		Color transparent = display.getSystemColor(SWT.COLOR_TRANSPARENT);

		form = new SashForm(this, SWT.VERTICAL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ScrolledComposite sc2 = new ScrolledComposite(form, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Composite topControlsComposite = new Composite(sc2, SWT.NULL);
		GridLayout topControlsCompositeLayout = new GridLayout(3, true);
		GridData topControlsCompositeData = new GridData(GridData.FILL_BOTH);
		topControlsComposite.setLayout(topControlsCompositeLayout);
		topControlsComposite.setLayoutData(topControlsCompositeData);

		slider = new Slider(topControlsComposite, SWT.HORIZONTAL);

		slider.setMinimum(0);
		slider.setMaximum(numberOfImages);
		slider.setIncrement(1);
		slider.setThumb(1);

		final GridData gdFirstField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		slider.setLayoutData(gdFirstField);

		go = new Button(topControlsComposite, SWT.PUSH | SWT.FILL);
		go.setText("Go");
		go.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		replay = new Button(topControlsComposite, SWT.PUSH | SWT.FILL);
		replay.setText("Replay");
		replay.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		replay.setEnabled(false);

		InputTileGenerator tile1 = new InputTileGenerator("X Variable:",
				String.valueOf(ssp.getXValue(slider.getSelection())), topControlsComposite);

		tile1Label = tile1.getLabel();
		xValue = tile1.getText();
		InputTileGenerator tile2 = new InputTileGenerator("Image No.:", String.valueOf(slider.getSelection()),
				topControlsComposite);
		imageNumber = tile2.getText();
		InputTileGenerator tile3 = new InputTileGenerator("ROI x coord:", String.valueOf(ssp.getLenPt()[1][0]),
				topControlsComposite);
		xCoord = tile3.getText();
		InputTileGenerator tile4 = new InputTileGenerator("x Len:", String.valueOf(ssp.getLenPt()[0][0]),
				topControlsComposite);
		xLen = tile4.getText();
		InputTileGenerator tile5 = new InputTileGenerator("ROI y coord:", String.valueOf(ssp.getLenPt()[1][1]),
				topControlsComposite);
		yCoord = tile5.getText();
		InputTileGenerator tile6 = new InputTileGenerator("y len:", String.valueOf(ssp.getLenPt()[0][1]),
				topControlsComposite);
		yLen = tile6.getText();

		sc2.setContent(topControlsComposite);
		//
		sc2.setExpandHorizontal(true);
		sc2.setExpandVertical(true);

		sc2.setMinSize(topControlsComposite.computeSize(topControlsComposite.getSize().x, SWT.DEFAULT));

		// sc1.setAlwaysShowScrollBars(true);
		sc2.pack();

		Group images = new Group(form, SWT.NONE);
		GridLayout imagesLayout = new GridLayout(1, true);
		images.setLayout(imagesLayout);
		GridData imagesData = new GridData(SWT.FILL, SWT.FILL, true, true);
		imagesData.grabExcessVerticalSpace = true;

		images.setLayoutData(imagesData);

		ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(images, null);

		plotSystem.createPlotPart(images, "Raw Image", actionBarComposite, PlotType.IMAGE, null);

		plotSystem.getPlotComposite().setLayoutData(imagesData);

		plotSystem.createPlot2D(nullImage, null, null);

		try {
			region = plotSystem.createRegion("myRegion", RegionType.BOX);
			bgRegion = plotSystem.createRegion("bgRegion", RegionType.BOX);
			secondBgRegion = plotSystem.createRegion("Background Region", RegionType.BOX);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Group centringButtons = new Group(images, SWT.NONE);
		GridLayout centringButtonsLayout = new GridLayout(4, true);
		centringButtons.setLayout(centringButtonsLayout);
		GridData centringButtonsData = new GridData(SWT.FILL, SWT.NULL, true, false);
		centringButtons.setLayoutData(centringButtonsData);

		Button centreRegion = new Button(centringButtons, SWT.PUSH | SWT.FILL);
		centreRegion.setText("Centre Region");
		centreRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		centreRegion.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRegion(region);

			}

		});

		centreSecondBgRegion = new Button(centringButtons, SWT.PUSH | SWT.FILL);
		centreSecondBgRegion.setText("Centre Background Region");
		centreSecondBgRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		centreSecondBgRegion.setEnabled(false);

		centreSecondBgRegion.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRegion(secondBgRegion);

			}

		});

		disregardFrame = new Button(centringButtons, SWT.PUSH | SWT.FILL);
		disregardFrame.setText("Disregard Frame");
		disregardFrame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		disregardFrame.setEnabled(true);

		disregardFrame.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.flipGoodPoint(slider.getSelection());
				if (ssp.getDrm().getFms().get(ssp.getSliderPos()).isGoodPoint()) {
					disregardFrame.setText("Disregard Frame");

				} else {
					disregardFrame.setText("Include Frame");
					includeAllFrames.setEnabled(true);
				}
				if (ssp.areAllPointsGood()) {
					includeAllFrames.setEnabled(false);
				}
			}

		});

		includeAllFrames = new Button(centringButtons, SWT.PUSH | SWT.FILL);
		includeAllFrames.setText("Include All Frames");
		includeAllFrames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		includeAllFrames.setEnabled(false);

		includeAllFrames.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (FrameModel fm : ssp.getDrm().getFms()) {
					fm.setGoodPoint(true);
				}
				disregardFrame.setText("Disregard Frame");
				includeAllFrames.setEnabled(false);
			}

		});

		folder = new TabFolder(form, SWT.BORDER | SWT.CLOSE);
		folder.setLayout(new GridLayout());

		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		////////////////////////////////////////////////////////
		// Tab 1 Setup
		//////////////////////////////////////////////////////////

		TabItem subI = new TabItem(folder, SWT.NONE);
		subI.setText("Background Options");
		subI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		ScrolledComposite sc1 = new ScrolledComposite(folder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		Composite subIComposite = new Composite(sc1, SWT.NONE | SWT.FILL);
		subIComposite.setLayout(new GridLayout());
		subIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);

		customComposite1 = new PlotSystem1CompositeView(subIComposite, SWT.NONE, 0, ssp, ssvs);
		customComposite1.setLayout(new GridLayout());
		customComposite1.setLayoutData(ld2);

		sc1.setContent(subIComposite);
		sc1.setExpandHorizontal(true);
		sc1.setExpandVertical(true);

		int x = subIComposite.computeSize(subIComposite.getSize().x, SWT.DEFAULT).x;
		int y = subIComposite.computeSize(subIComposite.getSize().y, SWT.DEFAULT).y;

		sc1.setMinSize(x, y);

		sc1.pack();

		subI.setControl(sc1);

		plotSystem.addRegion(region);
		RectangularROI startROI = new RectangularROI(100, 100, 50, 50, 0);
		region.setROI(startROI);

		RectangularROI bgStartROI = new RectangularROI(90, 90, 70, 70, 0);
		bgRegion.setROI(bgStartROI);
		bgRegion.setRegionColor(gold);
		bgRegion.setUserRegion(false);
		bgRegion.setLineWidth(3);
		bgRegion.setMobile(false);
		plotSystem.addRegion(bgRegion);

		RectangularROI secondBgStartROI = new RectangularROI(10, 10, 20, 20, 0);
		secondBgRegion.setROI(secondBgStartROI);
		secondBgRegion.setRegionColor(transparent);
		secondBgRegion.setUserRegion(true);
		secondBgRegion.setLineWidth(3);
		secondBgRegion.setMobile(true);

		getSecondBgRegion().setVisible(false);
		secondBgRegion.setFill(false);
		plotSystem.addRegion(secondBgRegion);
		secondBgRegion.setVisible(false);

		////////////////////////////////////////////////////////
		// Tab 2 Setup
		//////////////////////////////////////////////////////////

		correctionsTab = new TabItem(folder, SWT.NONE);
		correctionsTab.setText("Raw Output and Corrections");
		correctionsTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite correctionsTabComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		correctionsTabComposite.setLayout(new GridLayout());
		correctionsTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		correctionsTab.setControl(correctionsTabComposite);

		if (ssp.getCorrectionSelection() == MethodSetting.SXRD) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Lorentz Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			lorentz = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Polarisation Correction:",
					String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			polarisation = subTile2.getText();
			InputTileGenerator subTile3 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentAreaCorrection()), corrections);
			setAreaCorrection(subTile3.getText());

		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Flux Correction:",
					String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			reflectivityFluxCorr = subTile2.getText();
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
		}

		////////////////////////////////////////////////////////
		// Tab 3 Setup
		//////////////////////////////////////////////////////////

		TabItem geometricParametersRepeaterTab = new TabItem(folder, SWT.NONE);
		geometricParametersRepeaterTab.setText("Geometric Parameters");
		geometricParametersRepeaterTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite geometricParametersRepeaterTabComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		geometricParametersRepeaterTabComposite.setLayout(new GridLayout());
		geometricParametersRepeaterTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		geometricParametersRepeaterTab.setControl(geometricParametersRepeaterTabComposite);

		gprt = new GeometricParametersRepeaterTable(geometricParametersRepeaterTabComposite, SWT.NONE, ssp, ssvs);

		GridData ld3 = new GridData(SWT.FILL, SWT.FILL, true, true);

		gprt.setLayout(new GridLayout());
		gprt.setLayoutData(ld3);

		////////////////////////////////////////////////////////
		// Tab 4 Setup
		//////////////////////////////////////////////////////////

		TabItem rodComponentsDisplayTab = new TabItem(folder, SWT.NONE);
		rodComponentsDisplayTab.setText("Rod Component Files");
		rodComponentsDisplayTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite rodComponentsDisplayComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		rodComponentsDisplayComposite.setLayout(new GridLayout());
		rodComponentsDisplayComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		rodComponentsDisplayTab.setControl(rodComponentsDisplayComposite);

		rcd = new RodComponentsDisplay(rodComponentsDisplayComposite, SWT.NONE, ssp);

		GridData ld4 = new GridData(SWT.FILL, SWT.FILL, true, true);

		rcd.setLayout(new GridLayout());
		rcd.setLayoutData(ld4);

		folder.pack();

		region.addROIListener(new IROIListener() {

			@Override
			public void roiDragged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				roiStandard(evt);
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				roiStandard(evt);
			}

			public void roiStandard(ROIEvent evt) {

				PlotSystemCompositeView.this.roiStandard(evt);
			}
		});

		slider.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ssp.sliderMovemementMainImage(slider.getSelection());

			}

		});

		Group processing = new Group(form, SWT.NONE);
		GridLayout processingLayout = new GridLayout(2, true);
		processing.setLayout(processingLayout);
		GridData processingData = new GridData(SWT.FILL, SWT.NULL, true, false);
		processing.setLayoutData(processingData);

		InputTileGenerator tile0 = new InputTileGenerator("Processing Method:", processing);
		processingMode = tile0.getCombo();

		for (ProccessingMethod i : ProccessingMethod.values()) {
			processingMode.add(ProccessingMethod.toString(i));
		}

		manualControls = new Group(processing, SWT.NONE);

		changeProcessingMode();

		addChangeProcessingMethodListeners();

		form.setWeights(new int[] { 12, 54, 27, 7 });
	}

	public GeometricParametersRepeaterTable getGprt() {
		return gprt;
	}

	public RodComponentsDisplay getRcd() {
		return rcd;
	}

	public void addChangeProcessingMethodListeners() {

		processingMode.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.setProcessingMethodSelection(ProccessingMethod.toMethodology(processingMode.getSelectionIndex()));
				changeProcessingMode();
			}

		});

	}

	public void roiStandard(ROIEvent evt) {

		int[] pt = new int[2];
		int[] len = new int[2];

		RectangularROI[] bgRegionROI = new RectangularROI[2];

		IRectangularROI greenRectangle = region.getROI().getBounds();
		int[] glen = greenRectangle.getIntLengths();
		int[] gpt = greenRectangle.getIntPoint();

		if (evt != null) {
			pt = evt.getROI().getBounds().getIntPoint();
			len = evt.getROI().getBounds().getIntLengths();
		}

		else {
			len = glen;
			pt = gpt;
		}

		int[][] lenPt = { len, pt };

		if (len[0] < 1 || len[1] < 1) {

			if (len[0] < 1) {
				len[0] = 2;
			}

			if (len[1] < 1) {
				len[1] = 2;
			}

			ssvs.updateDisplay(new int[][] { len, pt });

		}

		bgRegionROI = ssp.trackingRegionOfInterestSetter(lenPt);

		int[] ly = bgRegionROI[1].getIntLengths();
		int[] py = bgRegionROI[1].getIntPoint();

		IRectangularROI yellowRectangle = bgRegion.getROI().getBounds();
		int[] ylen = yellowRectangle.getIntLengths();
		int[] ypt = yellowRectangle.getIntPoint();

		int[] gl = bgRegionROI[0].getIntLengths();
		int[] gp = bgRegionROI[0].getIntPoint();

		if (!Arrays.equals(ypt, py) || !Arrays.equals(ylen, ly)) {

			bgRegion.setROI(bgRegionROI[1]);
			plotSystem.repaint();
		}

		if (!Arrays.equals(gpt, gp) || !Arrays.equals(glen, gl) && gpt[0] > 0 && gpt[1] > 0) {

			region.setROI(bgRegionROI[0]);
			plotSystem.repaint();

		}
		try {
			ssvs.probeSecondBackgroundRegion();
		} catch (Exception g) {
			System.out.println(g.getMessage());
		}

	}

	public void changeProcessingMode() {

		for (Control i : manualControls.getChildren()) {
			i.dispose();
		}

		if (ssp.getProcessingMethodSelection() == ProccessingMethod.AUTOMATIC) {

			processingMode.select(0);

			GridLayout manualControlsLayout = new GridLayout(1, true);
			manualControls.setLayout(manualControlsLayout);
			GridData manualControlsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			manualControls.setLayoutData(manualControlsData);

			run = new Button(manualControls, SWT.PUSH);
			run.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			run.setText("Run");
			addRunListener();

		}

		if (ssp.getProcessingMethodSelection() == ProccessingMethod.MANUAL) {

			GridLayout manualControlsLayout = new GridLayout(4, true);
			manualControls.setLayout(manualControlsLayout);
			GridData manualControlsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			manualControls.setLayoutData(manualControlsData);

			decrement = new Button(manualControls, SWT.PUSH);
			decrement.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			decrement.setText("<");

			acceptBack = new Button(manualControls, SWT.PUSH);
			acceptBack.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			acceptBack.setText("< Accept");

			accept = new Button(manualControls, SWT.PUSH);
			accept.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			accept.setText("Accept >");

			increment = new Button(manualControls, SWT.PUSH);
			increment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			increment.setText(">");

			addManualListeners();

		}

		form.setWeights(new int[] { 19, 45, 29, 7 });

		manualControls.layout(true, true);
		manualControls.redraw();
	}

	public void addRunListener() {

		run.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireRun();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void addManualListeners() {

		acceptBack.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireAccept();
				ssp.setSliderPos(slider.getSelection() - 1);
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		accept.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ssvs.fireAccept();
				ssp.setSliderPos(slider.getSelection() + 1);
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		increment.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				slider.setSelection(slider.getSelection() + 1);
				ssp.setSliderPos(slider.getSelection());
				generalUpdate();
				ssvs.sliderMovementGeneralUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		decrement.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				slider.setSelection(slider.getSelection() - 1);
				ssp.setSliderPos(slider.getSelection());
				generalUpdate();

				ssvs.sliderMovementGeneralUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
	}

	public Combo getProcessingMode() {
		return processingMode;
	}

	public void setProcessingMode(Combo processingMode) {
		this.processingMode = processingMode;
	}

	public int getSliderPos() {
		int sliderPos = slider.getSelection();
		return sliderPos;
	}

	public Composite getComposite() {

		return this;
	}

	public IPlottingSystem<Composite> getPlotSystem() {
		return plotSystem;
	}

	public IDataset getImage() {
		return image;
	}

	public PlotSystem1CompositeView getPlotSystem1CompositeView() {
		return customComposite1;
	}

	public Slider getSlider() {
		return slider;
	}

	public IPlottingSystem<Composite> getSubImagePlotSystem() {
		return customComposite1.getPlotSystem();
	}

	public IPlottingSystem<Composite> getSubImageBgPlotSystem() {
		return subImageBgPlotSystem;
	}

	public void updateImage(IDataset image) {
		plotSystem.updatePlot2D(image, null, null);
	}

	public IRegion getIRegion() {
		return region;
	}

	public IRegion getBgRegion() {
		return bgRegion;
	}

	public IRegion getSecondBgRegion() {
		return secondBgRegion;
	}

	public void setRegion(int[][] lenpt) {

		boolean areLengthsEqual = false;
		boolean arePtsEqual = false;

		int[] lengths = region.getROI().getBounds().getIntLengths();
		int[] points = region.getROI().getBounds().getIntPoint();

		if (lengths[0] == (lenpt[0][0]) && lengths[1] == (lenpt[0][1])) {
			areLengthsEqual = true;
		}

		if (points[0] == (lenpt[1][0]) && points[1] == (lenpt[1][1])) {
			arePtsEqual = true;
		}

		if (areLengthsEqual == false || arePtsEqual == false) {
			RectangularROI newROI = new RectangularROI(lenpt[1][0], lenpt[1][1], lenpt[0][0], lenpt[0][1], 0);

			if (Arrays.equals(region.getROI().getBounds().getIntLengths(), lenpt[0])
					&& Arrays.equals(region.getROI().getBounds().getIntPoint(), lenpt[1])) {

			} else {
				region.setROI(newROI);
			}
		}
	}

	public IRegion getGreenRegion() {
		return region;
	}

	public void resetRegion(IRegion re) {

		int[] ad = ssp.getImage(ssp.getSliderPos()).getShape();

		RectangularROI newROI = new RectangularROI((int) Math.round(ad[1] * 3 / 8), (int) Math.round(ad[0] * 3 / 8),
				(int) Math.round(ad[1] * 0.25), (int) Math.round(ad[0] * 0.25), 0);
		re.setROI(newROI);
		re.toFront();
		re.setVisible(true);
		generalUpdate();

	}

	public Button getRun() {
		return run;
	}

	public Button getReplay() {
		return replay;
	}

	public Text getXValue() {
		return xValue;
	}

	public Text getImageNo() {
		return imageNumber;
	}

	public TabFolder getFolder() {
		return folder;
	}

	public Text getXCoord() {
		return xCoord;
	}

	public Text getXLen() {
		return xLen;
	}

	public Text getYCoord() {
		return yCoord;
	}

	public Text getYLen() {
		return yLen;
	}

	public Text[] getROITexts() {

		Text[] texts = new Text[4];
		texts[0] = xCoord;
		texts[1] = xLen;
		texts[2] = yCoord;
		texts[3] = yLen;

		return texts;
	}

	public void setROITexts(String[] values) {

		xCoord.setText(values[0]);
		xLen.setText(values[1]);
		yCoord.setText(values[2]);
		yLen.setText(values[3]);

	}

	public void resetCorrectionsTab() {

		correctionsTab.dispose();

		correctionsTab = new TabItem(folder, SWT.NONE);
		correctionsTab.setText("Raw Output and Corrections");
		correctionsTab.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite correctionsTabComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		correctionsTabComposite.setLayout(new GridLayout());
		correctionsTabComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		correctionsTab.setControl(correctionsTabComposite);

		if (ssp.getCorrectionSelection() == MethodSetting.SXRD) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Lorentz Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			lorentz = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Polarisation Correction:",
					String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			polarisation = subTile2.getText();
			InputTileGenerator subTile3 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentAreaCorrection()), corrections);
			setAreaCorrection(subTile3.getText());

		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
			InputTileGenerator subTile2 = new InputTileGenerator("Flux Correction:",
					String.valueOf(ssp.getCurrentPolarisationCorrection()), corrections);
			reflectivityFluxCorr = subTile2.getText();
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
			InputTileGenerator subTile1 = new InputTileGenerator("Area Correction:",
					String.valueOf(ssp.getCurrentLorentzCorrection()), corrections);
			reflectivityAreaCorr = subTile1.getText();
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction) {

			Group corrections = new Group(correctionsTabComposite, SWT.NONE);
			GridLayout correctionsLayout = new GridLayout(2, true);
			corrections.setLayout(correctionsLayout);
			GridData correctionsData = new GridData(SWT.FILL, SWT.NULL, true, false);
			corrections.setLayoutData(correctionsData);

			InputTileGenerator subTile0 = new InputTileGenerator("Raw Intensity:",
					String.valueOf(ssp.getCurrentRawIntensity()), corrections);
			rawIntensity = subTile0.getText();
		}

		folder.pack();
		form.setWeights(new int[] { 19, 45, 29, 7 });

	}

	public void setROITexts(int[][] LenPt) {

		String[] values = new String[4];

		values[0] = String.valueOf(LenPt[1][0]);
		values[1] = String.valueOf(LenPt[0][0]);
		values[2] = String.valueOf(LenPt[1][1]);
		values[3] = String.valueOf(LenPt[0][1]);

		setROITexts(values);

	}

	public void generalCorrectionsUpdate() {

		if (ssp.getCorrectionSelection() == MethodSetting.SXRD) {

			lorentz.setText(String.valueOf(ssp.getCurrentLorentzCorrection()));
			polarisation.setText(String.valueOf(ssp.getCurrentPolarisationCorrection()));

			double raw = ssp.getCurrentRawIntensity();
			try {
				rawIntensity.setText(String.valueOf(raw));
				areaCorrection.setText(String.valueOf(ssp.getCurrentAreaCorrection()));
			} catch (NullPointerException np) {

			}
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

			rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));

			double f = ssp.getCurrentReflectivityFluxCorrection();

			String fluxCorr = String.valueOf(f);
			try {
				reflectivityFluxCorr.setText(fluxCorr);
				reflectivityAreaCorr.setText(String.valueOf(ssp.getCurrentReflectivityAreaCorrection()));
			} catch (NullPointerException np) {

			}
		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {
			try {
				rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));
				reflectivityAreaCorr.setText(String.valueOf(ssp.getCurrentReflectivityAreaCorrection()));

			} catch (NullPointerException np) {

			}

		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction) {
			try {
				rawIntensity.setText(String.valueOf(ssp.getCurrentRawIntensity()));
			} catch (NullPointerException np) {

			}

		}

		this.update();

	}

	public void generalCorrectionsSet(double lorentzCorrection, double polarisationCorrection, double rawIntensityValue,
			double areaCorrectionValue) {

		lorentz.setText(String.valueOf(lorentzCorrection));
		polarisation.setText(String.valueOf(polarisationCorrection));
		rawIntensity.setText(String.valueOf(rawIntensityValue));
		areaCorrection.setText(String.valueOf(areaCorrectionValue));

	}

	public void generalReflectivityCorrectionsSet(double areaCorrection, double rawIntensityValue,
			double fluxCorrection) {

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

			rawIntensity.setText(String.valueOf(rawIntensityValue));
			reflectivityFluxCorr.setText(String.valueOf(fluxCorrection));
			reflectivityAreaCorr.setText(String.valueOf(areaCorrection));

		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
				|| ssp.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

			rawIntensity.setText(String.valueOf(rawIntensityValue));
			reflectivityAreaCorr.setText(String.valueOf(areaCorrection));

		}

		if (ssp.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction) {

			rawIntensity.setText(String.valueOf(rawIntensityValue));
		}

		this.update();

	}

	public void generalUpdate() {

		plotSystem.updatePlot2D(ssp.getImage(ssp.getSliderPos()), null, null);
		setRegion(ssp.getLenPt());
		plotSystem.repaint();

		generalCorrectionsUpdate();

	}

	public TabItem getBackgroundSubtractedSubImage() {
		return subBgI;
	}

	public void appendBackgroundSubtractedSubImage() {

		boolean alreadyGotOne = false;

		for (TabItem a : folder.getItems()) {
			if (a.getText().equals(DisplayLabelStrings.getBgSubtractedImage())) {
				subBgI = a;
				alreadyGotOne = true;
				break;
			}
		}

		if (!alreadyGotOne) {
			subBgI = new TabItem(folder, SWT.NONE);
			subBgI.setText(DisplayLabelStrings.getBgSubtractedImage());
			subBgI.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}

		try {
			folder.pack();
		} catch (Exception f) {

		}

		Composite subBgIComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		subBgIComposite.setLayout(new GridLayout());
		subBgIComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		subBgI.setControl(subBgIComposite);

		try {
			subImageBgPlotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		subImageBgPlotSystem.createPlotPart(subBgIComposite, "Region of interest", null, PlotType.IMAGE, null);
		subImageBgPlotSystem.getPlotComposite().setLayoutData(new GridData(GridData.FILL_BOTH));
		subImageBgPlotSystem.createPlot2D(nullImage, null, null);

		folder.setSelection(subBgI);

		folder.pack();

	}

	public void removeBackgroundSubtractedSubImage() {

		for (TabItem a : folder.getItems()) {
			if (a.getText().equals(DisplayLabelStrings.getBgSubtractedImage())) {
				a.dispose();
				break;
			}
		}

		folder.pack();
		subBgI = null;
		form.setWeights(new int[] { 19, 45, 29, 7 });
		folder.setSelection(0);
	}

	public Button getGo() {
		return go;
	}

	public Button getCentreSecondBgRegion() {
		return centreSecondBgRegion;
	}

	public SashForm getSash() {
		return form;
	}

	public Text getAreaCorrection() {
		return areaCorrection;
	}

	public void setAreaCorrection(Text areaCorrection) {
		this.areaCorrection = areaCorrection;
	}

	public Text getLorentz() {
		return lorentz;
	}

	public void setLorentz(Text lorentz) {
		this.lorentz = lorentz;
	}

	public Text getPolarisation() {
		return polarisation;
	}

	public void setPolarisation(Text polarisation) {
		this.polarisation = polarisation;
	}

	public Text getRawIntensity() {
		return rawIntensity;
	}

	public void setRawIntensity(Text rawIntensity) {
		this.rawIntensity = rawIntensity;
	}

	public Button getIncrement() {
		return increment;
	}

	public void setIncrement(Button increment) {
		this.increment = increment;
	}

	public Button getDecrement() {
		return decrement;
	}

	public void setDecrement(Button decrement) {
		this.decrement = decrement;
	}

	public Button getAccept() {
		return accept;
	}

	public void setAccept(Button accept) {
		this.accept = accept;
	}

	public Button getAcceptBack() {
		return acceptBack;
	}

	public void setAcceptBack(Button acceptBack) {
		this.acceptBack = acceptBack;
	}

	public Button getDisregardFrame() {
		return disregardFrame;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
		gprt.setSsp(ssp);
		rcd.setSsp(ssp);
	}

	public void setXValueLabel(String in) {

		tile1Label.setText(in);

	}

}
