package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;

import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.dawnsci.surfacescatter.FittingParameters;
//import org.dawnsci.surfacescatter.IntensityDisplayEnum;
import org.dawnsci.surfacescatter.MethodSettingEnum;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.dawnsci.surfacescatter.ReflectivityFluxCorrectionsForDialog;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.IntensityDisplayEnum.IntensityDisplaySetting;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SurfaceScatterViewStart extends Dialog {

	private String[] filepaths;
	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem3Composite ssps3c;
	private MultipleOutputCurvesTableView outputCurves;
	private DatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private CTabFolder folder;
//	private SashForm right;
//	private SashForm left;
//	private SashForm anaRight;
//	private SashForm anaLeft;
//	private SashForm setupSash;
	private SashForm analysisSash;
	private int numberOfImages;
	private Dataset nullImage;
	private SurfaceScatterPresenter ssp;
//	private ArrayList<Slider> sliderList;
	private int DEBUG = 1;
	private boolean modify = true;
	private String datFolderPath;
	private Combo correctionsDropDown;
	private Composite container;
	private Group experimentalSetup;
	private Group methodSetting;
	private Group parametersSetting;
	private int[] correctionsDropDownArray;
	private IntensityDisplaySetting ids;
	private SaveFormatSetting sms;
	private String option;


	public CTabFolder getFolder() {
		return folder;
	}

	public void setFolder(CTabFolder folder) {
		this.folder = folder;
	}
	
	public GeometricParametersWindows getParamField(){
		return paramField;
	}

	public String getDatFolderPath() {
		return datFolderPath;
	}

	public void setDatFolderPath(String datFolderPath) {
		this.datFolderPath = datFolderPath;
	}

	public SuperSashPlotSystem3Composite getSsps3c() {
		return ssps3c;
	}

	public void setSsps3c(SuperSashPlotSystem3Composite ssps3c) {
		this.ssps3c = ssps3c;
	}

	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	public SurfaceScatterViewStart(Shell parentShell, String[] filepaths, int numberOfImages, Dataset nullImage,
			SurfaceScatterPresenter ssp, String datFolderPath) {

		super(parentShell);

		
		this.ssp = new SurfaceScatterPresenter();
		this.filepaths = filepaths;
		this.numberOfImages = numberOfImages;
		this.nullImage = nullImage;
		
		this.ssp.addStateListener(new IPresenterStateChangeEventListener() {

			@Override
			public void update() {
				
			}
		});
		
//		this.ssvs = this;
		this.datFolderPath = datFolderPath;
		this.ssp.setSsvs(this);

		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.PRIMARY_MODAL);

	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);
		c.setVisible(false);
		c.dispose();
		return c;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		JFaceResources.getString(IDialogConstants.NO_TO_ALL_LABEL);

		container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		folder = new CTabFolder(container, SWT.BORDER | SWT.CLOSE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		////////////////////////////////////////////////////////
		// Tab 1 Setup
		//////////////////////////////////////////////////////////

		CTabItem setup = new CTabItem(folder, SWT.NONE);
		setup.setText("Setup Parameters");
		setup.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite setupComposite = new Composite(folder, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setup.setControl(setupComposite);

		SashForm setupSash = new SashForm(setupComposite, SWT.FILL);
		setupSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm left = new SashForm(setupSash, SWT.VERTICAL);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm right = new SashForm(setupSash, SWT.VERTICAL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setupSash.setWeights(new int[] { 65, 35 });

		//////////////// setupLeft///////////////////////////////////////////////

		/////////////////////////// Window 1 LEFT
		/////////////////////////// SETUP////////////////////////////////////////////////////
		
		try {

			datDisplayer = new DatDisplayer(left, SWT.FILL, filepaths, ssp, datFolderPath, this);
			datDisplayer.setLayout(new GridLayout());
			datDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		///////////////// setupRight////////////////////////////////////////

		/////////////////////////// Window 2 RIGHT SETUP
		/////////////////////////// ////////////////////////////////////////////////////

		try {
			
			experimentalSetup = new Group(right, SWT.FILL);
			GridLayout experimentalSetupLayout = new GridLayout(1, true);
			GridData experimentalSetupData = new GridData(GridData.FILL_BOTH);
			experimentalSetupData.minimumWidth = 50;
			experimentalSetup.setLayout(experimentalSetupLayout);
			experimentalSetup.setLayoutData(experimentalSetupData);
			experimentalSetup.setText("Experimental Setup");
			
			methodSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout methodSettingLayout = new GridLayout(1, true);
			GridData methodSettingData = new GridData(GridData.FILL_HORIZONTAL);
			methodSettingData.minimumWidth = 50;
			methodSetting.setLayout(methodSettingLayout);
			methodSetting.setLayoutData(methodSettingData);
			methodSetting.setText("SXRD / Reflectivity");

			correctionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.FILL);
			
			 for(org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting  t: MethodSettingEnum.MethodSetting.values()){
				 correctionsDropDown.add(org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting.toString(t));
			    }

			correctionsDropDown.select(0);
			correctionsDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			parametersSetting = new Group(experimentalSetup, SWT.FILL);
			GridLayout parametersSettingLayout = new GridLayout(1, true);
			GridData parametersSettingData = new GridData(GridData.FILL_BOTH);
			parametersSettingData.minimumWidth = 50;
			parametersSetting.setLayout(parametersSettingLayout);
			parametersSetting.setLayoutData(parametersSettingData);
			parametersSetting.setText("Geometric Parameters");
			
			paramField = new GeometricParametersWindows(parametersSetting, SWT.FILL, ssp);
			paramField.setLayout(new GridLayout());
			paramField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			this.setupRightEnabled(false);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		datDisplayer.getBuildRod().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				
				datDisplayer.setOption(datDisplayer.getSelectedOption());
				
				int mr = ssps3c.getOutputCurves().getIntensity().getSelectionIndex();
				IntensityDisplaySetting ids0 = IntensityDisplaySetting.toMethod(ssps3c.getOutputCurves().getIntensity().getSelectionIndex());
				
				setIds(ids0);
				
				setSms(SaveFormatSetting.toMethod(ssps3c.getOutputCurves().getOutputFormatSelection().getSelectionIndex()));
				
				ssp.createGm();
				
				paramField.geometricParametersUpdate();
				
				try{
					for(IRegion g : ssp.getInterpolatorRegions()){
						customComposite.getPlotSystem().removeRegion(g);
						g.remove();
						
					}
				}
				catch(Exception u){
						
				}
				
				ArrayList<TableItem> checkedList = new ArrayList<>();

				for (TableItem d : datDisplayer.getRodDisplayTable().getItems()) {
					if (d.getChecked()) {
						checkedList.add(d);
					}
				}

				TableItem[] rodComponentDats = new TableItem[checkedList.size()];

				for (int g = 0; g < checkedList.size(); g++) {
					rodComponentDats[g] = checkedList.get(g);
				}

				String[] filepaths = new String[rodComponentDats.length];

				for (int f = 0; f < rodComponentDats.length; f++) {
					String filename = rodComponentDats[f].getText();
					filepaths[f] = datFolderPath + File.separator + filename;
				}
				

				ssp.resetSmOutputObjects();
				
				int[][] r = new int[][] {{50, 50}, {10, 10}};
				String q = null;
				int[][] pbolp = null;
				int[][] bolp = null;
				int[][] bglpt = null;
				
				try{
					bglpt = ssp.getBackgroundLenPt();
				}
				catch(NullPointerException f){
					
				}
				
				try{
					q = ssp.getSaveFolder();
				}
				catch(NullPointerException f){
					
				}
				try{
					r = ssp.getLenPt();
					
				}
				catch(NullPointerException f){
					
				}
				try{
					pbolp = ssp.getPermanentBoxOffsetLenPt();
				}
				catch(NullPointerException f){
					
				}
				try{
					bolp = ssp.getBoxOffsetLenPt();
							
				}
				catch(NullPointerException f){
					
				}
				
				ssp.surfaceScatterPresenterBuild(ssp.getParentShell(), filepaths, datDisplayer.getSelectedOption(),
						ssp.getImageFolderPath(), datFolderPath, correctionsDropDownArray[correctionsDropDown.getSelectionIndex()]);
				
				try{
					ssp.setLenPt(r);
				}
				catch(Exception m){

				}
				try{
					ssp.setPermanentBoxOffsetLenPt(pbolp);
				}
				catch(Exception m){

				}	
				try{
					ssp.setBoxOffsetLenPt(bolp);
				}
				catch(Exception m){

				}
				try{
					ssp.setSaveFolder(q);
				}
				catch(Exception m){

				}
				try{
					ssp.setBackgroundLenPt(bglpt);
				}
				catch(Exception m){

				}
				
				folder.setSelection(1);
		
				ssp.setSelection(0);
				ssp.setSliderPos(0);;
				customComposite.getSlider().setSelection(0);
				customComposite.getSlider().setMinimum(0);
				customComposite.getSlider().setMaximum(ssp.getNoImages());
				customComposite.getSlider().setThumb(1);
				customComposite.getPlotSystem1CompositeView().checkTrackerOnButton();
				
				customComposite.generalUpdate();
				ssps3c.generalUpdate();
				customComposite.generalCorrectionsUpdate();
				customComposite.getPlotSystem1CompositeView().generalUpdate();
				updateIndicators(0);
				getPlotSystemCompositeView().removeBackgroundSubtractedSubImage();
				getSsps3c().isOutputCurvesVisible(false);
				customComposite.getReplay().setEnabled(false);
				customComposite.resetCorrectionsTab();
				ssps3c.isOutputCurvesVisible(false);
//				customComposite.getOutputControl().setEnabled(false);
				ssp.setProcessingMethodSelection(ProccessingMethod.toMethodology(customComposite.getProcessingMode().getSelectionIndex()));
				
				ssps3c.getOutputCurves().getIntensity().select(IntensityDisplaySetting.toInt(ids));
				ssps3c.getOutputCurves().getOutputFormatSelection().select(SaveFormatSetting.toInt(sms));
				
				try{
					customComposite.getPlotSystem().removeTrace(customComposite.getPlotSystem().getTrace("Interpolated trajectory"));	
				}
				catch(Exception g){
					
				}
				
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		/////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		// Tab 2 Analysis
		////////////////////////////////////////////////////// #

		CTabItem analysis = new CTabItem(folder, SWT.NONE);
		analysis.setText("Analysis");
		analysis.setData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite analysisComposite = new Composite(folder, SWT.FILL);
		analysisComposite.setLayout(new GridLayout());
		analysisComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		analysis.setControl(analysisComposite);

		analysisSash = new SashForm(analysisComposite, SWT.FILL);
		analysisSash.setLayout(new GridLayout());
		analysisSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm anaLeft = new SashForm(analysisSash, SWT.FILL);
		anaLeft.setLayout(new GridLayout());
		anaLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));

		SashForm anaRight = new SashForm(analysisSash, SWT.FILL);
		anaRight.setLayout(new GridLayout());
		anaRight.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));

		analysisSash.setWeights(new int[] { 40, 60 });

		////////////////////// Analysis Left//////////////////////////////
		///////////////// anaLeft Window 3/////////////////////////////////

		
		Dataset noImage = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
		
		customComposite = new PlotSystemCompositeView(anaLeft, 
													  SWT.FILL, 
													  noImage, 
													  1, 
													  numberOfImages, 
													  nullImage,
													  ssp, 
													  this);

		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		customComposite.getReplay().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
//
//				ssp.runReplay(customComposite.getPlotSystem(), customComposite.getFolder(),
//						customComposite.getSubImageBgPlotSystem());
//				
				MovieJob mJ = new MovieJob();
//				mJ.setSuperModel(sm);
				mJ.setPS(customComposite.getPlotSystem());
				mJ.setTime(220);
				mJ.setSsp(ssp);
				mJ.setSsvs(SurfaceScatterViewStart.this);
				mJ.setSliders(customComposite.getSlider());
				mJ.setFolder(customComposite.getFolder());
				mJ.setSubIBgPS(customComposite.getSubImageBgPlotSystem());
				mJ.run();
				

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		customComposite.getSlider().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {

				int sliderPos = customComposite.getSlider().getSelection();
				ssp.sliderMovemementMainImage(sliderPos);
				SurfaceScatterViewStart.this.updateIndicators(sliderPos);
				ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), sliderPos);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		customComposite.getGreenRegion().addROIListener(new IROIListener() {

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

			@SuppressWarnings("unchecked")
			public void roiStandard(ROIEvent evt) {

				IRegion region = customComposite.getIRegion();

				int[] Len = region.getROI().getBounds().getIntLengths();
				int[] Pt = region.getROI().getBounds().getIntPoint();
				int[][] LenPt = new int[][] { Len, Pt };

				customComposite.setROITexts(LenPt);
			}
		});

		folder.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (ssp.getNoImages() != 0) {
					customComposite.getSlider().setMaximum(ssp.getNoImages());
					customComposite.getPlotSystem1CompositeView().generalUpdate();
					paramField.geometricParametersUpdate();
					RectangularROI bgROI =ssp.regionOfInterestSetter(customComposite.getGreenRegion().getROI());
					customComposite.getBgRegion().setROI(bgROI);

					if (ssp.getYList() == (null)) {
						try {
							getPlotSystemCompositeView().removeBackgroundSubtractedSubImage();
							getSsps3c().isOutputCurvesVisible(false);
							customComposite.getReplay().setEnabled(false);
						} catch (Exception n) {

						}
					}

					ssps3c.resetVerticalAndHorizontalSlices();
					customComposite.getPlotSystem1CompositeView().checkTrackerOnButton();
				} else {
					folder.setSelection(0);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		addSecondBgRegionListeners(customComposite.getSecondBgRegion());

		customComposite.getGo().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {

					modify = false;

					double r = 0;
					try {
						r = Double.parseDouble(customComposite.getImageNo().getText());
					} catch (Exception e1) {
						ssp.numberFormatWarning();
					}

					int k = ssp.closestImageIntegerInStack(r);
					ssp.sliderMovemementMainImage(k);
					ssp.sliderZoomedArea(k, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());
					ssp.updateSliders(customComposite.getSlider(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), k);

					SurfaceScatterViewStart.this.updateIndicators(k);

					modify = true;

					int pt0 = (int) Math.round(Double.valueOf(customComposite.getXCoord().getText()));
					int pt1 = (int) Math.round(Double.valueOf(customComposite.getYCoord().getText()));

					int len0 = (int) Math.round(Double.valueOf(customComposite.getXLen().getText()));
					int len1 = (int) Math.round(Double.valueOf(customComposite.getYLen().getText()));

					int[][] newLenPt = new int[][] { { len0, len1 }, { pt0, pt1 } };

					RectangularROI[] greenBgRoi = ssp.regionOfInterestSetter(newLenPt);
					
					customComposite.getGreenRegion().setROI(greenBgRoi[0]);
					customComposite.getBgRegion().setROI(greenBgRoi[1]);
					
					ssp.backgroundBoxesManager(customComposite.getBgRegion(), 
											   customComposite.getSecondBgRegion(),
											   customComposite.getCentreSecondBgRegion());
					
					ssps3c.generalUpdate();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		customComposite.getXValue().addFocusListener(new FocusListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void focusLost(FocusEvent e) {
				if (modify == true) {
					modify = false;

					double in = 0;
					
					try {
						in = Double.parseDouble(customComposite.getXValue().getText());
					} catch (Exception e1) {
						ssp.numberFormatWarning();
					}

					int k = ssp.closestImageNo(in);

					ssp.sliderMovemementMainImage(k);
					ssp.sliderZoomedArea(k, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());

					ssp.updateSliders(customComposite.getSlider(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), k);

					SurfaceScatterViewStart.this.updateIndicators(k);
					modify = true;
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});

		//////////////////////// Analysis Right//////////////////////////////
		///////////////// anaRight Window 4/////////////////////////////////
		try {
			ssps3c = new SuperSashPlotSystem3Composite(anaRight, SWT.FILL, SurfaceScatterViewStart.this, ssp);

			ssps3c.setLayout(new GridLayout());
			ssps3c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			ssps3c.setSsp(ssp);

		} catch (Exception d) {

		}

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				event.doit = false;
			}
		});

		customComposite.getPlotSystem1CompositeView().getSaveButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				if(ssp.getSaveFolder()!=null){
					fd.setFilterPath(ssp.getSaveFolder());
				}
				
				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;
				
				if(ssp.getSaveFolder()==null){
					ssp.setSaveFolder(path);;
				}
				
				ssp.saveParameters(title);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		customComposite.getPlotSystem1CompositeView().getLoadButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN);

				if(ssp.getSaveFolder() !=null){
					fd.setFilterPath(ssp.getSaveFolder());;
				}
				
				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}

				String title = path + File.separator + stitle;

				FittingParameters fp = ssp.loadParameters(title); 
			
				RectangularROI loadedROI = new RectangularROI(fp.getLenpt()[1][0],
						  fp.getLenpt()[1][1],
						  fp.getLenpt()[0][0],
						  fp.getLenpt()[0][1],
						  0);
				
				
				customComposite.getPlotSystem1CompositeView().setMethodologyDropDown(fp.getBgMethod());
				customComposite.getPlotSystem1CompositeView().setFitPowerDropDown(fp.getFitPower());
				customComposite.getPlotSystem1CompositeView().setTrackerTypeDropDown(fp.getTracker());
				customComposite.getPlotSystem1CompositeView().setBoundaryBox(fp.getBoundaryBox());
				
				customComposite.setRegion(fp.getLenpt());
				RectangularROI bgROI = ssp.regionOfInterestSetter(loadedROI);
				customComposite.getBgRegion().setROI(bgROI);
				
				customComposite.redraw();
						
				int selection = ssp.closestImageNo(fp.getXValue());
				ssp.updateSliders(customComposite.getSlider(),selection);
				SurfaceScatterViewStart.this.updateIndicators(selection);
				
				
				ssps3c.generalUpdate();
				customComposite.getPlotSystem1CompositeView().generalUpdate();
				RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(ssp.getLenPt());
				
				customComposite.getIRegion().setROI(greenAndBg[0]);
				customComposite.getBgRegion().setROI(greenAndBg[1]);
				
				
				if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
					customComposite.getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
				}
				
				getSsps3c().generalUpdate(ssp.getLenPt());
				customComposite.generalCorrectionsUpdate();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {


			}
		});

		////////////////////////////////////////////////////////////////////
		customComposite.getSecondBgRegion().setVisible(false);
		customComposite.getSecondBgRegion().repaint();
		appendListenersToOutputCurves();

		datDisplayer.redrawDatDisplayerFolderView();

		return container;
	}
	
	
	public void updateAnalysisMethodology(int methodologySelection, 
										  int fitPowerSelection, 
										  int trackerSelection,
										  String boundaryBox){
		
		
		ssp.updateAnalysisMethodology(methodologySelection, 
									  fitPowerSelection, 
									  trackerSelection, 
									  boundaryBox);
		
		
		if(TrackingMethodology.intToTracker1(trackerSelection) != TrackerType1.INTERPOLATION 
				&& ssp.getInterpolatorRegions() != null){
			
			for(IRegion g : ssp.getInterpolatorRegions()){
				customComposite.getPlotSystem().removeRegion(g);
				g.remove();
				
			}
			try{
				customComposite.getPlotSystem().removeTrace(customComposite.getPlotSystem().getTrace("Interpolated trajectory"));	
			}
			catch(Exception g){
				
			}
			
			try{
				customComposite.getPlotSystem().removeRegion(customComposite.getPlotSystem().getRegion("Gray Region"));
			}
			catch(Exception u){
				
			}
			
			ssp.setInterpolatorRegions(null);
			ssp.setInterpolatedLenPts(null);
			
			if(ssp.getInterpolatorBoxes() != null){
				ssp.setInterpolatorBoxes(null);
			}
			
			customComposite.getPlotSystem1CompositeView().getAcceptLocation().setEnabled(false);
			customComposite.getPlotSystem1CompositeView().getRejectLocation().setEnabled(false);
		}
		
		else if(TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.INTERPOLATION &&
				ssp.getTrackerOn()){
	
			customComposite.getPlotSystem1CompositeView().getAcceptLocation().setEnabled(true);
			customComposite.getPlotSystem1CompositeView().getRejectLocation().setEnabled(true);
		}
		
		
	}
	
	
	public void addSecondBgRegionListeners(IRegion r2){
				
		r2.addROIListener(new IROIListener() {
			
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
				
				int[] len = ssp.getInitialLenPt()[0]; 
				int[] pt = ssp.getInitialLenPt()[1];
				int[][] lenpt = {len, pt};
				
				IRectangularROI bounds = r2.getROI().getBounds();
				int[] redLen = bounds.getIntLengths();
				int[] redPt = bounds.getIntPoint();
				int[][] redLenPt = {redLen, redPt};
				
				ssp.setBackgroundLenPt(redLenPt);
				
				if (ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
					
					int [][] newOffsetLenPt = new int[2][2];
					
					newOffsetLenPt[0][0]  =  -len[0] + redLen[0];
					newOffsetLenPt[0][1]  =  -len[1] + redLen[1];
					
					
					newOffsetLenPt[1][0]  = -pt[0] + redPt[0];
					newOffsetLenPt[1][1]  = -pt[1] + redPt[1];
					
					 
					ssp.setBoxOffsetLenPt(newOffsetLenPt);
				}
				
				RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(ssp.getLenPt());
				
				customComposite.getIRegion().setROI(greenAndBg[0]);
				customComposite.getBgRegion().setROI(greenAndBg[1]);
				
				
				if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
					customComposite.getSecondBgRegion().setROI(ssp.generateOffsetBgROI(ssp.getLenPt()));
				}
				
				getSsps3c().generalUpdate(ssp.getLenPt());
				getSsps3c().generalUpdate();
				
			}
		});				
	}

	public void interpolationTrackerBoxesAccept(){
		
		try{
			Display display = Display.getCurrent();
	        Color cyan = display.getSystemColor(SWT.COLOR_CYAN);
	        
			
			ArrayList<double[][]> jk = ssp.interpolationTrackerBoxesAccept(customComposite.getGreenRegion());
			
			try {
				IRegion region =customComposite.getPlotSystem().createRegion(("Interpolation Region: " + ssp.getSliderPos()), RegionType.BOX);
				region.setROI(customComposite.getGreenRegion().getROI());
				ssp.addToInterpolatorRegions(region);
				region.setRegionColor(cyan);
				region.setLineWidth(10);
				region.setFill(true);
				region.setUserRegion(false);
				region.setMobile(false);
				customComposite.getPlotSystem().addRegion(region);

			} catch (Exception e1) {
						// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			
			IPlottingSystem<Composite> pS = customComposite.getPlotSystem();
			try{
				pS.removeTrace(pS.getTrace("Interpolated trajectory"));	
			}
			catch(Exception g){
				
			}
			ILineTrace lt1 = pS.createLineTrace("Interpolated trajectory");
			
			Dataset xData = DatasetFactory.zeros(new int[] {jk.size()}, Dataset.ARRAYFLOAT64);
			Dataset yData = DatasetFactory.zeros(new int[] {jk.size()}, Dataset.ARRAYFLOAT64);
			
			for(int ty = 0; ty<jk.size(); ty++){
				double[][] consideredBox = jk.get(ty);
				
				xData.set(consideredBox[1][0], ty);
				yData.set(consideredBox[1][1], ty);
			}
			
			lt1.setData(xData, yData);
			lt1.setTraceColor(cyan);
			pS.addTrace(lt1);
		}
		catch(Exception f){
			
		}
	}
	
	
	public void fireAccept(){
		
		ssp.addXValuesForFireAccept();
		
		ssp.presenterDummyProcess(ssp.getSliderPos(), 
								  ssp.getImage(ssp.getSliderPos()), 
								  customComposite.getPlotSystem(), 
								  4);
		
		if (getSsps3c().getOutputCurves().isVisible() != true) {
			getSsps3c().getOutputCurves().setVisible(true);
			getSsps3c().getSashForm().setWeights(new int[] { 50, 50 });
			getSsps3c().getLeft().setWeights(new int[] { 50, 50 });
			getSsps3c().getRight().setWeights(new int[] { 50, 50 });
		}

		if (getPlotSystemCompositeView().getBackgroundSubtractedSubImage() == null) {
			getPlotSystemCompositeView().appendBackgroundSubtractedSubImage();
			getPlotSystemCompositeView().getSash().setWeights(new int[]{19, 45, 29, 7});
		}


		IDataset  s = ssp.getBackgroundDatArray().get(ssp.getSliderPos());
		this.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(s, null, null);
		this.getSsps3c().generalUpdate();
		ssp.stitchAndPresent1(this.getSsps3c().getOutputCurves(), ids);

		this.getSsps3c().getOutputCurves().getPlotSystem().repaint(false);
		customComposite.getFolder().setSelection(0);
		analysisSash.setWeights(new int[] { 40, 60 });
		analysisSash.redraw();
		
		
		
	}
	
	public void fireRun(){
		

		if(ssp.getProcessingMethodSelection() == ProccessingMethod.AUTOMATIC){
			
			ssp.setStartFrame(customComposite.getSlider().getSelection());
			ssp.resetDataModels();
			ssp.triggerBoxOffsetTransfer();
			
			if (getSsps3c().getOutputCurves().isVisible() != true) {
				getSsps3c().getOutputCurves().setVisible(true);
				getSsps3c().getSashForm().setWeights(new int[] { 50, 50 });
				getSsps3c().getLeft().setWeights(new int[] { 50, 50 });
				getSsps3c().getRight().setWeights(new int[] { 50, 50 });
			}
	
			if (getPlotSystemCompositeView().getBackgroundSubtractedSubImage() == null) {
				getPlotSystemCompositeView().appendBackgroundSubtractedSubImage();
				getPlotSystemCompositeView().getSash().setWeights(new int[] { 23, 45, 25, 7 });
	
			}
			
			analysisSash.setWeights(new int[] { 40, 60 });
			analysisSash.redraw();
			
			TrackingProgressAndAbortView tpaav 
						= new TrackingProgressAndAbortView(getParentShell(), 
														   ssp.getNumberOfImages(),
														   ssp,
//														   customComposite.getSubImagePlotSystem(),
//														   getSsps3c().getOutputCurves().getPlotSystem(),
//														   customComposite.getPlotSystem(),
//														   customComposite.getFolder(),
//														   customComposite.getSubImageBgPlotSystem(),
														   SurfaceScatterViewStart.this);
			tpaav.open();
			
//			customComposite.getOutputControl().setEnabled(true);
//			customComposite.getOutputControl().setSelection(true);
			
		}
		
	}
	

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Rod Analysis (RodAn)");

	}
	

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(1*w), (int) Math.round(1*h));
	}
	

	@Override
	protected boolean isResizable() {
		return true;
	}

	public void sliderMovementGeneralUpdate(){
		int sliderPos = customComposite.getSlider().getSelection();
		ssp.sliderMovemementMainImage(sliderPos);
		SurfaceScatterViewStart.this.updateIndicators(sliderPos);
		ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), sliderPos);
		ssps3c.generalUpdate();
	}
	
	
	
	
	public PlotSystemCompositeView getPlotSystemCompositeView() {
		return customComposite;
	}

	public PlotSystemCompositeView getCustomComposite() {
		return customComposite;
	}

	public MultipleOutputCurvesTableView getOutputCurves() {
		return outputCurves;
	}

	public void updateIndicators(int k) {
		
		try{
			customComposite.getPlotSystem().removeRegion(customComposite.getPlotSystem().getRegion("Gray Region"));
		}
		catch(Exception u){
			
		}
		
		Display display = Display.getCurrent();
        Color gray= display.getSystemColor(SWT.COLOR_DARK_GRAY);
		
		modify = false;
		if (customComposite.getXValue().equals(ssp.getXValue(k)) == false) {
			customComposite.getXValue().setText(String.valueOf(ssp.getXValue(k)));
		}
		if (customComposite.getImageNo().equals(String.valueOf(k)) == false) {
			customComposite.getImageNo().setText(String.valueOf(k));
		}

		RectangularROI r = new RectangularROI(ssp.getXValue(k), 0.1, 0, 0.1, 0);
		try {
			outputCurves.getRegionNo().setROI(r);
		} catch (NullPointerException f) {

		}

		modify = true;
		
		ssp.illuminateCorrectInterpolationBox(k);
		
		if(ssp.getTrackerType() == TrackerType1.INTERPOLATION && ssp.getInterpolatedLenPts()!= null){
			
			double[][] lf =ssp.getInterpolatedLenPts().get(ssp.getSliderPos());
			
			RectangularROI grayROI = new RectangularROI(lf[1][0],
					  lf[1][1],
					  lf[0][0],
					  lf[0][1],
					  0);
			
			try {
				IRegion grayRegion = customComposite.getPlotSystem().createRegion("Gray Region", RegionType.BOX);
				grayRegion.setROI(grayROI);
				grayRegion.setRegionColor(gray);
				grayRegion.setLineWidth(10);
				grayRegion.setFill(true);
				grayRegion.setUserRegion(false);
				grayRegion.setMobile(false);
				customComposite.getPlotSystem().addRegion(grayRegion);				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
		}
	}

//	public ArrayList<Slider> getSliderList() {
//
//		sliderList = null;
//
//		sliderList = new ArrayList<Slider>();
//
//		sliderList.add(customComposite.getSlider());
//
//		return sliderList;
//	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
	
	public void resetSXRDReflectivityCombo(int[] input){
		
		correctionsDropDown.removeAll();
		
		for(int t = 0;t< input.length;t++){
			 
			correctionsDropDown.add(MethodSetting.toString(MethodSetting.toMethod(input[t])));
		
			 if(input[t] == 0){
				 paramField.getFolder().setSelection(0);
			 }
			
			 if(input[t] == 1){
				 paramField.getFolder().setSelection(1);
			 }
			 
			 if(input[t] == 2){
				 paramField.getFolder().setSelection(2);
			 }
			 
		}

		correctionsDropDown.select(0);
		
		setCorrectionsDropDownArray(input);
		
		
		
	}	

	public Combo getCorrectionSelection() {
		return correctionsDropDown;
	}

	public DatDisplayer getDatDisplayer() {
		return datDisplayer;
	}
	
	public void resetIntensityCombo(){
		SurfaceScatterViewStart.this.getSsps3c().getOutputCurves().getIntensity().select(0);
	}
	
	public void setupRightEnabled(boolean enabled){
		
		experimentalSetup.setEnabled(enabled);
		
		for (Control r: experimentalSetup.getChildren()){
			r.setEnabled(enabled);
		}
		
		for (Control r: methodSetting.getChildren()){
			r.setEnabled(enabled);
		}
		
		paramField.setEnabled(enabled);
		parametersSetting.setEnabled(enabled);
		
	}
	
	public void appendListenersToOutputCurves() {

		outputCurves = ssps3c.getOutputCurves();

		outputCurves.getOverlapZoom().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<ArrayList<IDataset>> xyArrays = ssp.xyArrayPreparer();

				GeneralOverlapHandlerView goh = new GeneralOverlapHandlerView(
						getParentShell(), SWT.OPEN,
						xyArrays.get(0), xyArrays.get(1), 
						xyArrays.get(2), xyArrays.get(3), 
						xyArrays.get(4), xyArrays.get(5),
						xyArrays.get(6), outputCurves.getPlotSystem(), 
						ssp,
						SurfaceScatterViewStart.this);

				goh.open();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		outputCurves.getRegionNo().addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
			}

			@Override
			public void roiDragged(ROIEvent evt) {
			}

			@SuppressWarnings("unchecked")
			@Override
			public void roiChanged(ROIEvent evt) {

//				customComposite.getOutputControl().getSelection() && 
				if ( modify == true) {

					int xPos = ssp.xPositionFinder(outputCurves.getRegionNo().getROI().getPointX());

					ssp.updateSliders(customComposite.getSlider(), xPos);

					ssp.sliderMovemementMainImage(xPos);
					ssp.sliderZoomedArea(xPos, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());
					
					customComposite.getPlotSystem().updatePlot2D(ssp.getImage(xPos),null, null);

					SurfaceScatterViewStart.this.updateIndicators(xPos);

					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(), xPos);
					
					double[] location = ssp.getLocationList().get(xPos);
					
					int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
					int[] pt = new int[] {(int) location[0],(int) location[1]};
					int[][] lenPt = { len, pt };

					RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(lenPt);
					
					customComposite.getIRegion().setROI(greenAndBg[0]);
					customComposite.getBgRegion().setROI(greenAndBg[1]);
					
					
					if(ssp.getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
						customComposite.getSecondBgRegion().setROI(ssp.generateOffsetBgROI(lenPt));
					}
					
					getSsps3c().generalUpdate(lenPt);
					
				}
			}

		});
			

		outputCurves.getIntensity().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
				ssp.switchFhklIntensity(pS, outputCurves.getIntensity());
				ids = IntensityDisplaySetting.toMethod(ssps3c.getOutputCurves().getIntensity().getSelectionIndex());

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		outputCurves.getErrorsButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ssp.switchErrorDisplay();

				IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
				Combo selector = outputCurves.getIntensity();
				ssp.switchFhklIntensity(pS, selector);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		

		outputCurves.getSave().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				if(ssp.getSaveFolder()!=null){
					fd.setFilterPath(ssp.getSaveFolder());
				}
				
				String stitle = "r";
				String path = "p";

				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();

				}
				
				if(ssp.getSaveFolder()==null){
					ssp.setSaveFolder(path);;
				}
				
				String title = path + File.separator + stitle;

				if (sms == SaveFormatSetting.GenX) {
					ssp.genXSave(title);
				}
				if (sms == SaveFormatSetting.Anarod) {
					ssp.anarodSave(title);
				}
				if (sms == SaveFormatSetting.int_format) {
					ssp.intSave(title);
				}
				if (sms == SaveFormatSetting.ASCII) {
					ssp.simpleXYYeSave(title, getOutputCurves().getIntensity().getSelectionIndex());
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	public int[] getCorrectionsDropDownArray() {
		return correctionsDropDownArray;
	}

	public void setCorrectionsDropDownArray(int[] correctionsDropDownArray) {
		this.correctionsDropDownArray = correctionsDropDownArray;
	}
	
	public void export(IPlottingSystem<Composite> parentPs, 
			IDataset xData,
			IDataset yData){

		ssp.setSplicedCurveX(xData);
		ssp.setSplicedCurveY(yData);
		
		parentPs.clear();
		
		ILineTrace lt1 = parentPs.createLineTrace("Adjusted Spliced Curve");
		lt1.setData(xData, yData);
		lt1.isErrorBarEnabled();
		
		parentPs.addTrace(lt1);
		//parentPs.repaint();
		getSsps3c().getOutputCurves().getIntensity().select(0);;
	}
		
	
	
	
	public void checkForFlux(String filepath){ 
		try{
			IDataHolder dh1 =null;
			
			try {
				dh1 = LoaderFactory.getData(filepath);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	
			ILazyDataset qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqdcd());
			
			if (qdcd == null) {
				try {
					qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());
		
				} catch (Exception e2) {
	//				System.out.println("can't get qdcd");
				}
			} 
			
			else {
			}
			
			SliceND sl = new SliceND(qdcd.getShape());
			Dataset QdcdDat = null;
			
			try {
				QdcdDat = (Dataset) qdcd.getSlice(sl);
				
			} catch (DatasetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		
			double ref = 
					ReflectivityFluxCorrectionsForDialog.reflectivityFluxCorrectionsDouble(SurfaceScatterViewStart.this.getParamField().getFluxPath().getText(), 
																				 	   	   QdcdDat.getDouble(0), 
																				 	   	   filepath);
			
		}
		catch(Exception h){
			RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(getShell(),4,null);
			roobw.open();
		}
	}

	public SaveFormatSetting getSms() {
		return sms;
	}

	public void setSms(SaveFormatSetting sms) {
		this.sms = sms;
	}

	public IntensityDisplaySetting getIds() {
		return ids;
	}

	public void setIds(IntensityDisplaySetting ids) {
		this.ids = ids;
	}
}