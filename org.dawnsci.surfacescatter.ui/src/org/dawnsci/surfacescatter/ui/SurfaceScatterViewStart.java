package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.FittingParameters;
import org.dawnsci.surfacescatter.IntensityDisplayEnum.IntensityDisplaySetting;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.dawnsci.surfacescatter.ReflectivityFluxCorrectionsForDialog;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDNexusReader;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SurfaceScatterViewStart extends Dialog {

	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem3Composite ssps3c;
	private MultipleOutputCurvesTableView outputCurves;
	private DatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private CTabFolder folder;
	private SurfaceScatterPresenter ssp;
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
	private boolean qConvert;
	private RodAnalysisWindow raw ;
	private SetupModel stm;
	
//	private final static Logger logger = LoggerFactory.getLogger(SurfaceScatterViewStart.class);
	
	public SurfaceScatterViewStart (Shell parentShell){

		super(parentShell);

		this.ssp = new SurfaceScatterPresenter();
		
		
		this.ssp.addStateListener(new IPresenterStateChangeEventListener() {

			@Override
			public void update() {
				
				updateDisplay(null);
			}
		});
		
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
	protected Control createDialogArea(Composite parent){

		JFaceResources.getString(IDialogConstants.NO_TO_ALL_LABEL);

		container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		folder = new CTabFolder(container, SWT.BORDER | SWT.CLOSE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		RodSetupWindow rsw = new RodSetupWindow(folder,
												this,
												ssp);
		
		DatDisplayer datDisplayer = rsw.getDatDisplayer();
		
		this.experimentalSetup = rsw.getExperimentalSetup();
		this.methodSetting = rsw.getMethodSetting();
		this.parametersSetting = rsw.getParametersSetting();
		this.correctionsDropDown = rsw.getCorrectionsDropDown();
		
		paramField =rsw.getParamField();
		
		rsw.getDatDisplayer().getBuildRod().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				datDisplayer.setOption(datDisplayer.getSelectedOption());
				
			
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
				
				bglpt = ssp.getBackgroundLenPt()!=null ? ssp.getBackgroundLenPt(): new int[][] {{50, 50}, {10, 10}};
				q = ssp.getSaveFolder()!=null ? ssp.getSaveFolder(): "null";
				r =ssp.getLenPt() != null ? ssp.getLenPt() : new int[][] {{50, 50}, {10, 10}} ;
				pbolp = (ssp.getPermanentBoxOffsetLenPt() != null) ? ssp.getPermanentBoxOffsetLenPt() : new int[][]{{0,0},{0,0}};
				bolp = ssp.getBoxOffsetLenPt();
				
				
				int[] test = correctionsDropDownArray;
				int t = correctionsDropDown.getSelectionIndex();
				
				customComposite.resetCorrectionsTab();
				
				
				ssp.surfaceScatterPresenterBuildWithFrames(filepaths,
												datDisplayer.getSelectedOption(),
												datFolderPath, 
												test[t]);
				
				try{
					ssp.setLenPt(r);
					ssp.setPermanentBoxOffsetLenPt(pbolp);
					ssp.setBoxOffsetLenPt(bolp);
					ssp.setSaveFolder(q);
					ssp.setBackgroundLenPt(bglpt);
				}
				catch(Exception m){

				}
				
				if(ssp.isqConvert()){
					double  energy = Double.valueOf(paramField.getEnergy().getText());
					ssp.setEnergy(energy);
					
					ssp.setTheta(paramField.getTheta().getSelectionIndex());
					outputCurves.getqAxis().setEnabled(ssp.isqConvert());
					customComposite.getPlotSystem1CompositeView().getUseQAxis().setEnabled(ssp.isqConvert());
					
					try{
						ssp.qConversion();
					}
					catch(Exception g){
			
					}
				}
				
				folder.setSelection(1);
				
				customComposite.getSlider().setSelection(0);
				customComposite.getSlider().setMinimum(0);
				customComposite.getSlider().setMaximum(ssp.getDrm().getFms().size());
				customComposite.getSlider().setThumb(1);
				customComposite.getPlotSystem1CompositeView().checkTrackerOnButton();
				
				customComposite.resetCorrectionsTab();
				customComposite.generalUpdate();
				ssps3c.generalUpdate();
				customComposite.generalCorrectionsUpdate();
				customComposite.getPlotSystem1CompositeView().generalUpdate();
				updateIndicators(0);
				getPlotSystemCompositeView().removeBackgroundSubtractedSubImage();
				getSsps3c().isOutputCurvesVisible(false);
				customComposite.getReplay().setEnabled(false);
				
				ssps3c.isOutputCurvesVisible(false);
				ssp.setProcessingMethodSelection(ProccessingMethod.toMethodology(customComposite.getProcessingMode().getSelectionIndex()));
				
				ssps3c.getOutputCurves().getIntensity().select(IntensityDisplaySetting.toInt(ids));
				ssps3c.getOutputCurves().getOutputFormatSelection().select(SaveFormatSetting.toInt(sms));
				
				customComposite.resetCorrectionsTab();
				
				try{
					customComposite.getPlotSystem().removeTrace(customComposite.getPlotSystem().getTrace("Interpolated trajectory"));	
				}
				catch(Exception g){
					
				}
				
				ssps3c.resetCrossHairs();
				ssp.setSelection(0);
				ssp.setSliderPos(0);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {		

			}
		});

		/////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		// Tab 2 Analysis
		////////////////////////////////////////////////////// #

		raw = new RodAnalysisWindow(folder,
														ssp,
														this); 
		
		this.customComposite = raw.getCustomComposite();
		this.ssps3c = raw.getSsps3c();
		
		customComposite.getReplay().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				MovieJob mJ = new MovieJob();
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

			@Override
			public void widgetSelected(SelectionEvent e) {

				int sliderPos = customComposite.getSlider().getSelection();
				ssp.sliderMovemementMainImage(sliderPos);
				SurfaceScatterViewStart.this.updateIndicators(sliderPos);
				try{
					IDataset image = ssp.getBackgroundImage(sliderPos);
					customComposite.getSubImageBgPlotSystem().updatePlot2D(image, 
																		   null,
																		   null);
				}catch(Exception cx){
					
				}
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
					
					
					IRectangularROI greenRectangle = customComposite.getGreenRegion().getROI().getBounds();
					int[] len = greenRectangle.getIntLengths();
					int[] pt = greenRectangle.getIntPoint();

					int[][] lenPt = { len, pt };
					
					double[] bgRegionROI =ssp.regionOfInterestSetter1(lenPt);
					RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
							  bgRegionROI[1],
							  bgRegionROI[2],
							  bgRegionROI[3],
							  bgRegionROI[4]);
					
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

					IDataset image = ssp.getBackgroundImage(k);
					try{
						customComposite.getSubImageBgPlotSystem().updatePlot2D(image, 
																			   null,
																			   null);
					}
					catch(Exception h){
						
					}
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

					customComposite.getPlotSystem().repaint();
					
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
					
					try{
						IDataset image = ssp.getBackgroundImage(k);
						customComposite.getSubImageBgPlotSystem().updatePlot2D(image, 
																			   null,
																			   null);
						
					}
					catch(Exception o){
						
					}
					SurfaceScatterViewStart.this.updateIndicators(k);
					modify = true;
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});


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
			
				
				customComposite.getPlotSystem1CompositeView().setMethodologyDropDown(fp.getBgMethod());
				customComposite.getPlotSystem1CompositeView().setFitPowerDropDown(fp.getFitPower());
				customComposite.getPlotSystem1CompositeView().setTrackerTypeDropDown(fp.getTracker());
				customComposite.getPlotSystem1CompositeView().setBoundaryBox(fp.getBoundaryBox());
				
				customComposite.setRegion(fp.getLenpt());
				
				double[] bgRegionROI = ssp.regionOfInterestSetter1(fp.getLenpt());
				
				RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
						  bgRegionROI[1],
						  bgRegionROI[2],
						  bgRegionROI[3],
						  bgRegionROI[4]);
				
				customComposite.getBgRegion().setROI(bgROI);
				
				customComposite.redraw();
						
				int selection = ssp.closestImageNo(fp.getXValue());
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

		Display.getCurrent().addFilter(SWT.KeyDown, new Listener()
	    {
	        @Override
	        public void handleEvent(Event event)
	        {
	        	
				int key = event.keyCode;
				
				
				switch(key){
				case SWT.ARROW_LEFT:
					if(ssp.getSliderPos() > 0){
						int k = (ssp.getSliderPos()-1);
						ssp.sliderMovemementMainImage(k);
						customComposite.getSlider().setSelection(k);
						SurfaceScatterViewStart.this.updateIndicators(k);
					}
					break;
				case SWT.ARROW_RIGHT:
					if(ssp.getSliderPos() < (ssp.getNumberOfImages()-1)){
						int k = (ssp.getSliderPos()+1);
						ssp.sliderMovemementMainImage(k);
						customComposite.getSlider().setSelection(k);
						SurfaceScatterViewStart.this.updateIndicators(k);
					}
					break;
				case SWT.ARROW_UP:
					if(ssp.getProcessingMethodSelection() ==ProccessingMethod.MANUAL){
						SurfaceScatterViewStart.this.fireAccept();
						if(ssp.getSliderPos() < (ssp.getNumberOfImages()-1)){
							ssp.setSliderPos(ssp.getSliderPos()+1);
							
						}
					}
					break;
			
		        case SWT.ARROW_DOWN:
		        	if(ssp.getProcessingMethodSelection() ==ProccessingMethod.MANUAL){
			        	SurfaceScatterViewStart.this.fireAccept();
						if(ssp.getSliderPos() > 0){
							ssp.setSliderPos(ssp.getSliderPos()-1);
						}
						
					}
					break;
			
				}
	        }
	    });
		
		
		
		ssps3c.getOutputCurves().getqAxis().addSelectionListener(new SelectionListener() {
			
			Display display = Display.getCurrent();
	        Color red = display.getSystemColor(SWT.COLOR_RED);
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if(ssps3c.getOutputCurves().getqAxis().getSelection()){
					
					IPlottingSystem<Composite> pS = 
							ssps3c.getOutputCurves().getPlotSystem();
					
					pS.clear();
					
					ILineTrace lt1 = ssps3c.getOutputCurves().
							getPlotSystem().createLineTrace("q Axis");
					
					
					
					int state = getOutputCurves().getIntensity().getSelectionIndex();
					
					ssp.qConversion();
					
					if(state == 0){
						lt1.setData(ssp.getSplicedCurveQ(), ssp.getSplicedCurveY()); 
						
						
					}
					
					else if(state == 1){
						lt1.setData(ssp.getSplicedCurveQ(), ssp.getSplicedCurveYFhkl()); 
						
					}
					
					
					else if(state == 2){
						lt1.setData(ssp.getSplicedCurveQ(), ssp.getSplicedCurveYRaw()); 
						
					}
					
					pS.addTrace(lt1);
					pS.repaint();
					
					
					lt1.setErrorBarEnabled(ssp.getErrorFlag());
					lt1.setErrorBarColor(red);
					double start = lt1.getXData().getDouble(0);
					double end = lt1.getXData().getDouble(lt1.getXData().getShape()[0]-1);
					double range = end - start;
							
					pS.getAxes().get(0).setRange((start - 0.1*range), (end) + 0.1*range);
					
				}
				
				else if(ssps3c.getOutputCurves().getqAxis().getSelection() == false){
					
					IPlottingSystem<Composite> pS = 
							ssps3c.getOutputCurves().getPlotSystem();
					
					pS.clear();
					
					ILineTrace lt1 = ssps3c.getOutputCurves().
							getPlotSystem().createLineTrace("ly");
					
					
					int state = getOutputCurves().getIntensity().getSelectionIndex();
					
					if(state == 0){
						IDataset x = ssp.getSplicedCurveX();
						lt1.setData(x, ssp.getSplicedCurveY()); 
						
					}
					
					else if(state == 1){
						lt1.setData(ssp.getSplicedCurveX(), ssp.getSplicedCurveYFhkl()); 
						
					}
					
					else if(state == 2){
						lt1.setData(ssp.getSplicedCurveX(), ssp.getSplicedCurveYRaw()); 
						
					}
					
					pS.addTrace(lt1);
					pS.repaint();
					
					lt1.setErrorBarEnabled(ssp.getErrorFlag());
					lt1.setErrorBarColor(red);
					double start = lt1.getXData().getDouble(0);
					double end = lt1.getXData().getDouble(lt1.getXData().getShape()[0]-1);
					double range = end - start;
							
					pS.getAxes().get(0).setRange((start - 0.1*range), (end) + 0.1*range);
					
					
				}
				
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	
		
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
				&& TrackingMethodology.intToTracker1(trackerSelection) != TrackingMethodology.TrackerType1.SPLINE_INTERPOLATION
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
		
		else if((TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.INTERPOLATION 
				|| TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.SPLINE_INTERPOLATION)
						&& ssp.getTrackerOn()){
	
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
					
					int[] bB = new int[4];
					
					bB[0] = Math.abs(newOffsetLenPt[1][0]);
					bB[1] = Math.abs(newOffsetLenPt[1][1]);
					
					bB[2] = Math.abs(newOffsetLenPt[1][0] + newOffsetLenPt[0][0] - pt[0]);
					bB[3] = Math.abs(newOffsetLenPt[1][1] + newOffsetLenPt[0][1] - pt[1]);
					
					int probe = bB[0];
					
					for(int g: bB){
						if(g>probe){
							probe = g;
						}
					}
					
					ssp.setbB(probe);
					
					if(!Arrays.equals(newOffsetLenPt[0], ssp.getBoxOffsetLenPt()[0]) ||
							!Arrays.equals(newOffsetLenPt[1], ssp.getBoxOffsetLenPt()[1])){
					 
						ssp.setBoxOffsetLenPt(newOffsetLenPt);
					}
				}
				
				RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(ssp.getLenPt());
				
				customComposite.getIRegion().setROI(greenAndBg[0]);
				customComposite.getBgRegion().setROI(greenAndBg[1]);

				}
		});				
	}

	@SuppressWarnings("deprecation")
	public void interpolationTrackerBoxesAccept(){
		
		try{
 			Display display = Display.getCurrent();
	        Color cyan = display.getSystemColor(SWT.COLOR_CYAN);
	        
	        IRegion greenRegion = customComposite.getGreenRegion();
			
			ArrayList<double[][]> jk = ssp.interpolationTrackerBoxesAccept(greenRegion);
			
			try {
				
				String newRegionName = "Interpolation Region: " + ssp.getSliderPos();
				
				IRegion region =customComposite.getPlotSystem().getRegion(newRegionName);
				
				if(region == null){
					region =customComposite.getPlotSystem().createRegion(newRegionName, RegionType.BOX);
				}
				
				region.setROI(greenRegion.getROI().copy());
				ssp.addToInterpolatorRegions(region);
				region.setRegionColor(cyan);
				region.setLineWidth(10);
				region.setFill(true);
				region.setUserRegion(false);
				region.setMobile(false);
				region.toBack();
				
				customComposite.getPlotSystem().addRegion(region);
				greenRegion.toFront();

			} catch (Exception e1) {
						// TODO Auto-generated catch block
				e1.printStackTrace();
				debug("Box not accepted");
			}

			IPlottingSystem<Composite> pS = customComposite.getPlotSystem();
			
			if(jk.size() > 0){
				
				try{
					IRegion n = pS.getRegion("Interpolated trajectory");
					pS.removeRegion(n);
				}
				catch(Exception g){
					
				}
				
				IRegion trajectoryRegion = pS.createRegion("Interpolated trajectory", RegionType.POLYLINE);
				
				
				
				Dataset xData = DatasetFactory.zeros(new int[] {jk.size()}, Dataset.ARRAYFLOAT64);
				Dataset yData = DatasetFactory.zeros(new int[] {jk.size()}, Dataset.ARRAYFLOAT64);
				
				
				PolylineROI lt1 = new PolylineROI();
				
				
				
				for(int ty = 0; ty<jk.size(); ty++){
					double[][] consideredBox = jk.get(ty);
					double x = consideredBox[1][0];
					double y = consideredBox[1][1];
					xData.set(consideredBox[1][0], ty);
					yData.set(consideredBox[1][1], ty);
					
					lt1.insertPoint(x, y);;
					
				}
				
//				trajectoryRegion.roi
				trajectoryRegion.setROI(lt1);
				
				trajectoryRegion.setVisible(true);
//				lt1.setData(xData, yData);
				trajectoryRegion.setRegionColor(cyan);;
				
				
				pS.addRegion(trajectoryRegion);
				trajectoryRegion.setUserRegion(false);
				trajectoryRegion.setMobile(false);
	//			customComposite.getPlotSystem().getRegion("myRegion").toFront();
	//			customComposite.getPlotSystem().getRegion("myRegion").setActive(true);
				
//				customComposite.setRegion(ssp.getInitialLenPt());
			}
			
			
		}
		
		catch(Exception f){
			System.out.println("error at line 1069: "  + f.getMessage());
		}
	}
	
	public void interpolationTrackerBoxesReject(){
		
		if(ssp.getInterpolatorBoxes() != null){
			for(int j = 0; j<ssp.getInterpolatorBoxes().size(); j++){
				if(ssp.getInterpolatorBoxes().get(j)[2][0] == ssp.getSliderPos()){
					ssp.getInterpolatorBoxes().remove(j);
					customComposite.getPlotSystem().removeRegion(ssp.getInterpolatorRegions().get(j));
					ssp.getInterpolatorRegions().remove(j);
					customComposite.getPlotSystem().repaint(false);
				}
			}
		}
	}

	
	public void populateThetaOptionsDropDown(){
		
		Combo c = paramField.getSelectedOption();
		
		c.removeAll();
		
		for(String u : ssp.getOptions()){
			c.add(u);
		}
		
		 paramField.redraw();
	}
	
	public void fireAccept(){
		
		
		if(ssp.getProcessingMethodSelection() ==ProccessingMethod.MANUAL){
			
			customComposite.roiStandard(null);
			
			ssp.addXValuesForFireAccept();
			
			ssp.presenterDummyProcess(ssp.getSliderPos(), 
									  ssp.getImage(ssp.getSliderPos()), 
									  4,
									  null);
			
			if (getSsps3c().getOutputCurves().isVisible() != true) {
				getSsps3c().getOutputCurves().setVisible(true);
				getSsps3c().getSashForm().setWeights(new int[] { 50, 50 });
			}
	
			if (getPlotSystemCompositeView().getBackgroundSubtractedSubImage() == null) {
				getPlotSystemCompositeView().appendBackgroundSubtractedSubImage();
				getPlotSystemCompositeView().getSash().setWeights(new int[]{19, 45, 29, 7});
			}
	
			IDataset  s = ssp.getBackgroundDatArray().get(ssp.getSliderPos());
			this.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(s, null, null);
			
			ssp.stitchAndPresent1(this.getSsps3c().getOutputCurves(), ids);
	
			this.getSsps3c().generalUpdate();
			
			this.getSsps3c().getOutputCurves().getPlotSystem().repaint(false);
			customComposite.getFolder().setSelection(0);
//			analysisSash.setWeights(new int[] { 40, 60 });
//			analysisSash.redraw();
			
		}
		
	}
	
	public void fireRun(){
		
//		if(ssp.getQList()!=null){
//			if(ssp.getQList().size()!=0){
//				ssps3c.getOutputCurves().getqAxis().setEnabled(true); 
//			}
//		}
		
		if(ssp.getProcessingMethodSelection() == ProccessingMethod.AUTOMATIC){
			

			ssp.triggerBoxOffsetTransfer();
			
			if (getSsps3c().getOutputCurves().isVisible() != true) {
				getSsps3c().getOutputCurves().setVisible(true);
				getSsps3c().getSashForm().setWeights(new int[] { 50, 50 });
			}
	
			if (getPlotSystemCompositeView().getBackgroundSubtractedSubImage() == null) {
				getPlotSystemCompositeView().appendBackgroundSubtractedSubImage();
				getPlotSystemCompositeView().getSash().setWeights(new int[] { 23, 45, 25, 7 });
	
			}
			
			TrackingProgressAndAbortView tpaav 
						= new TrackingProgressAndAbortView(getParentShell(), 
														   ssp,
														   SurfaceScatterViewStart.this);
			tpaav.open();
			
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
		IDataset image = ssp.getBackgroundImage(sliderPos);
		customComposite.getSubImageBgPlotSystem().updatePlot2D(image, 
															   null,
															   null);
	}
	
	public PlotSystemCompositeView getPlotSystemCompositeView() {
		return customComposite;
	}

	public PlotSystemCompositeView getCustomComposite() {
		return customComposite;
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
		
		double x = ssp.getXValue(k);
		
//		if(outputCurves.getqAxis().getSelection()){
//			x = ssp.getQValue(k);
//		}
//		
		RectangularROI r = new RectangularROI(x, 0.1, 0, 0.1, 0);
		
		try {
			outputCurves.getRegionNo().setROI(r);
		} catch (NullPointerException f) {

		}

		modify = true;
		
		ssp.illuminateCorrectInterpolationBox(k);
		
		if((ssp.getTrackerType() == TrackerType1.INTERPOLATION 
				|| ssp.getTrackerType() == TrackerType1.SPLINE_INTERPOLATION)
				&& ssp.getInterpolatedLenPts()!= null){
			
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
				e.printStackTrace();
			
			}
		}
	}


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
						xyArrays.get(6), 
						ssp,
						SurfaceScatterViewStart.this);

				goh.open();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				

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

				if ( modify == true) {

					int xPos =0;
					
					if(qConvert){
						xPos = ssp.qPositionFinder(outputCurves.getRegionNo().getROI().getPointX());
					}
					else{
						xPos = ssp.xPositionFinder(outputCurves.getRegionNo().getROI().getPointX());
					}
										
					ssp.sliderMovemementMainImage(xPos);
					ssp.sliderZoomedArea(xPos, customComposite.getGreenRegion().getROI(),
							customComposite.getSubImagePlotSystem());
					
					customComposite.getPlotSystem().updatePlot2D(ssp.getImage(xPos),null, null);

					SurfaceScatterViewStart.this.updateIndicators(xPos);

					IDataset image = ssp.getBackgroundImage(xPos);
					customComposite.getSubImageBgPlotSystem().updatePlot2D(image, 
																		   null,
																		   null);
					
					
					double[] location = ssp.getThisLocation();
					
					int[][] lenPt = {new int[] {0,0}, 
									 new int[] {0,0}};
					
					if(location[0] == 0 &&
					   location[1] == 0 &&	
					   location[2] == 0 &&
					   location[3] == 0 &&
					   location[4] == 0 &&
					   location[5] == 0){
						
						IROI green = customComposite.getIRegion().getROI();
						
						
						IRectangularROI greenRectangle = green.getBounds();
						int[] len = greenRectangle.getIntLengths();
						int[] pt = greenRectangle.getIntPoint();

						lenPt = new int[][] { len, pt };
					}
					
					else{
						int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
						int[] pt = new int[] {(int) location[0],(int) location[1]};
						lenPt = new int[][] {len, pt};
					}
					
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
				ssp.switchFhklIntensity(pS, outputCurves.getIntensity().getSelectionIndex(),
						outputCurves.getqAxis().getSelection());
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
				int selector = outputCurves.getIntensity().getSelectionIndex();
				ssp.switchFhklIntensity(pS, 
										selector,
										outputCurves.getqAxis().getSelection());
				
				
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
		
		outputCurves.getStoreAsNexus().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE);

				if(ssp.getNexusPath()!=null){
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

				ssp.writeNexus(title);
				
				raw.getTabFolder().getTabList()[1].setEnabled(true);

				raw.getRtc().getRcm().addToCsdpList(ssp.getDrm().getCsdp());
					
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
		getSsps3c().getOutputCurves().getIntensity().select(0);;
	}
	
	public void export(){

		outputCurves.getPlotSystem().clear();
		
		ILineTrace lt1 = outputCurves.getPlotSystem().createLineTrace("Adjusted Spliced Curve");
		
		IDataset xData = ssp.getDrm().getCsdp().getSplicedCurveX();
		IDataset yData = ssp.getDrm().getCsdp().getSplicedCurveY();
		
		lt1.setData(xData, yData);
		lt1.isErrorBarEnabled();
		
		outputCurves.getPlotSystem().addTrace(lt1);
		
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

	public SurfaceScatterPresenter getSsp() {
		return ssp;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

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

	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
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

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public boolean isqConvert() {
		return qConvert;
	}

	public void setqConvert(boolean qConvert) {
		this.qConvert = qConvert;
	}
	
	public MultipleOutputCurvesTableView getOutputCurves(){
		return ssps3c.getOutputCurves();
	}

	public SetupModel getStm() {
		return stm;
	}

	public void setStm(SetupModel stm) {
		this.stm = stm;
	}
	
	public void updateDisplay(int[][] lenPt) {
		
		if(customComposite.getSlider().getSelection() != ssp.getSliderPos()){
			customComposite.getSlider().setSelection(ssp.getSliderPos());
			updateIndicators(ssp.getSliderPos());
		}
		
		RectangularROI[] bgRegionROI = new RectangularROI[2];
		
		if(lenPt == null){
			bgRegionROI = ssp.trackingRegionOfInterestSetter(ssp.getLenPt());
		}
		else{
			bgRegionROI = ssp.trackingRegionOfInterestSetter(lenPt);
		}
		customComposite.generalCorrectionsUpdate();
		
		int[] lp = bgRegionROI[0].getIntLengths();
		int[] pp = bgRegionROI[0].getIntPoint();
		
		int[] ls = bgRegionROI[1].getIntLengths();
		int[] ps = bgRegionROI[1].getIntPoint();
		
		if((Arrays.equals(lp, 
			customComposite.getIRegion().getROI().getBounds().getIntLengths()) == false) ||
			(Arrays.equals(pp, 
			customComposite.getIRegion().getROI().getBounds().getIntPoint()) == false)){
	
			customComposite.getIRegion().setROI(bgRegionROI[0]);		
		}
		
		if((Arrays.equals(ls, 
			customComposite.getBgRegion().getROI().getBounds().getIntLengths()) == false) ||
			(Arrays.equals(ps, 
			customComposite.getBgRegion().getROI().getBounds().getIntPoint()) == false)	){
	
			
			customComposite.getBgRegion().setROI(bgRegionROI[1]);
		
		}			
			
			
		customComposite.getPlotSystem().repaint();
		
		
		
		try{
			ssps3c.generalUpdate();
		}
		catch(Exception o){
			
		}
//		customComposite.generalUpdate();
	}
	
}	


	
	
	
