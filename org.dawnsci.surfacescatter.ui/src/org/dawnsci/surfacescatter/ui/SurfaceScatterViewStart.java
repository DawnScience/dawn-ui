package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;

import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.PlatformUI;



public class SurfaceScatterViewStart extends Dialog {
	
	private String[] filepaths;
	private PlotSystemCompositeView customComposite;
	private MultipleOutputCurvesTableView outputCurves;
	private DatDisplayer datDisplayer;
	private GeometricParametersWindows paramField;
	private CTabFolder folder;
	private SashForm right; 
	private SashForm left;
	private SashForm anaRight; 
	private SashForm anaLeft;
	private SashForm setupSash;
	private SashForm analysisSash;
	private int  numberOfImages;
	private Dataset nullImage;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private ArrayList<Slider> sliderList;
	private RegionSetterZoomedView rszv;
	private int DEBUG =1;
	private boolean modify = true;
	private  String datFolderPath;
	
	
	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	public SurfaceScatterViewStart(Shell parentShell, 
			String[] filepaths,
			int numberOfImages,
			Dataset nullImage,
			SurfaceScatterPresenter ssp,
			String datFolderPath) {
		
		super(parentShell);
		
		this.filepaths = filepaths;
		this.numberOfImages = numberOfImages;
		this.nullImage = nullImage;
		this.ssp = ssp;
		this.ssvs = this;
		this.datFolderPath= datFolderPath;
		
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		//createDialogArea(parentShell.getParent());

	}

	@Override
	protected Control createDialogArea(Composite parent) {
	
		
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		folder = new CTabFolder(container, SWT.BORDER | SWT.CLOSE);
		folder.setLayout(new GridLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		////////////////////////////////////////////////////////
		//		Tab 1 Setup
		//////////////////////////////////////////////////////////
		
		CTabItem setup = new CTabItem(folder, SWT.NONE);
		setup.setText("Setup Parameters");
		setup.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite setupComposite = new Composite(folder, SWT.NONE | SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
	    setup.setControl(setupComposite);
		
		setupSash= new SashForm(setupComposite, SWT.HORIZONTAL);
		setupSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		left = new SashForm(setupSash, SWT.VERTICAL);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		
		right = new SashForm(setupSash, SWT.VERTICAL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));
		
		setupSash.setWeights(new int[]{70,30});

////////////////setupLeft///////////////////////////////////////////////

		
///////////////////////////Window 1  LEFT  SETUP////////////////////////////////////////////////////
		try {

			datDisplayer = new DatDisplayer(left, SWT.FILL, filepaths, ssp, datFolderPath);
			datDisplayer.setLayout(new GridLayout());
			datDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
/////////////////setupRight////////////////////////////////////////

///////////////////////////Window 2 RIGHT SETUP ////////////////////////////////////////////////////
		

	    try {
			paramField = new GeometricParametersWindows(right, SWT.FILL, ssp);
			paramField.setLayout(new GridLayout());
			paramField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    paramField.getTabFolder().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.setCorrectionSelection(paramField.getTabFolder().getSelectionIndex());
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
/////////////////////////////////////////////////////////////////////////		
///////////////////////////////////////////////////////
//	    Tab 2	Analysis
	    //////////////////////////////////////////////////////#
	    
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
	    
	    anaLeft = new SashForm(analysisSash, SWT.FILL);
		anaLeft.setLayout(new GridLayout());
		anaLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
		
		anaRight = new SashForm(analysisSash, SWT.FILL);
		anaRight.setLayout(new GridLayout());
		anaRight.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));
		
		analysisSash.setWeights(new int[]{50,50});
		
//////////////////////Analysis Left//////////////////////////////
/////////////////anaLeft Window 3/////////////////////////////////		
		
		
		
	    customComposite = new PlotSystemCompositeView(anaLeft, 
	    											SWT.FILL,
	    											ssp.getImage(0),
	    											1,
	    											numberOfImages, 
	    											nullImage,
	    											ssp,
	    											this);
					
	    customComposite.setLayout(new GridLayout());
	    customComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    
	    
	    customComposite.getZoom().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				rszv = new RegionSetterZoomedView(
						getParentShell(), 
						getShellStyle(), 
						filepaths, 
						paramField, 
						ssp, 
						ssvs);
						
				
				
				rszv.open();
				
				
				
			}
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    customComposite.getReplay().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

						ssp.runReplay(customComposite.getPlotSystem(), 
									  customComposite.getFolder(),
									  customComposite.getSubImageBgPlotSystem());
			
			}
			
		
			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {	
			}
		});
	    
	    
	    customComposite.getSlider().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				 int sliderPos = customComposite.getSlider().getSelection();
				 ssp.sliderMovemementMainImage(sliderPos, customComposite.getPlotSystem());
				 ssp.sliderZoomedArea(sliderPos, 
						 			  customComposite.getGreenRegion().getROI(), 
						 			  customComposite.getSubImagePlotSystem());
				ssvs.updateIndicators(sliderPos);
				ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(),
								  sliderPos);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
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
				int[][] LenPt = new int[][] {Len,Pt};
				
				customComposite.setROITexts(LenPt);
				
				
			}
		});
	    	
	    folder.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				customComposite.getSlider().setMaximum(ssp.getNoImages());
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    customComposite.getImageNo().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if (modify == true){
					modify = false;
					
					double r = 0;
					try{
						r = Double.parseDouble(customComposite.getImageNo().getText());
					}
					catch (Exception e1){
						ssp.numberFormatWarning();
					}
					
					int k = ssp.closestImageIntegerInStack(r);
					ssp.sliderMovemementMainImage(k, customComposite.getPlotSystem());
					ssp.sliderZoomedArea(k, 
							  			 customComposite.getGreenRegion().getROI(), 
							  			 customComposite.getSubImagePlotSystem());
					ssp.updateSliders(ssvs.getSliderList(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(),
									  k);
					
					ssvs.updateIndicators(k);
					
					
					modify = true;
				}				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    customComposite.getXValue().addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if(modify == true){
					modify = false;
					
					double in = 0;
					try{
						in = Double.parseDouble(customComposite.getXValue().getText());
					}
					catch (Exception e1){
						ssp.numberFormatWarning();
					}
					
					int k = ssp.closestImageNo(in);
//					double l = ssp.closestXValue(in);
					
					ssp.sliderMovemementMainImage(k, customComposite.getPlotSystem());
					ssp.sliderZoomedArea(k, 
							  			 customComposite.getGreenRegion().getROI(), 
							  			 customComposite.getSubImagePlotSystem());
					
					ssp.updateSliders(ssvs.getSliderList(), k);
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(),
							  		  k);
					
					ssvs.updateIndicators(k);
					modify = true;
				}		
			}
			
			@Override
			public void focusGained(FocusEvent e) {
			}
		});
	    
	    customComposite.getRun().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				ssp.resetDataModels();
				ssp.runTrackingJob(customComposite.getSubImagePlotSystem(),
								   outputCurves.getPlotSystem(),
								   customComposite.getPlotSystem(),
								   customComposite.getFolder(),
								   customComposite.getSubImageBgPlotSystem());
				
				
				
				
				ssp.stitchAndPresent(outputCurves);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
////////////////////////Analysis Right//////////////////////////////
/////////////////anaRight Window 4/////////////////////////////////		
		
	    try {
			outputCurves = new MultipleOutputCurvesTableView(anaRight, 
															SWT.FILL, 
															0);
			
			outputCurves.setLayout(new GridLayout());
			outputCurves.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    outputCurves.getOverlapZoom().addSelectionListener(new SelectionListener() {
				    	
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ArrayList<ArrayList<IDataset>> xyArrays = ssp.xyArrayPreparer();
				
				GeneralOverlapHandlerView goh = new GeneralOverlapHandlerView(
						getParentShell(), 
						SWT.OPEN, 
						xyArrays.get(0),
						xyArrays.get(1),
						xyArrays.get(2),
						xyArrays.get(3),
						xyArrays.get(4), 
						outputCurves.getPlotSystem(),
						ssp);
				
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
			
			@Override
			public void roiChanged(ROIEvent evt) {

				if(customComposite.getOutputControl().getSelection() && modify == true){
					
					int xPos = ssp.xPositionFinder(outputCurves.getRegionNo().getROI().getPointX());
					
					ssp.updateSliders(ssvs.getSliderList(), xPos);
					
					ssp.sliderMovemementMainImage(xPos, 
							                  	  customComposite.getPlotSystem());
					ssp.sliderZoomedArea(xPos, 
				 			  customComposite.getGreenRegion().getROI(), 
				 			  customComposite.getSubImagePlotSystem());
					
					ssvs.updateIndicators(xPos);
					
					ssp.bgImageUpdate(customComposite.getSubImageBgPlotSystem(),
									  xPos);
					
				}
			}
			
		});
	    
	    outputCurves.getIntensity().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
		
				IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
				Combo selector = outputCurves.getIntensity();
				ssp.switchFhklIntensity(pS, selector);
		
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
				
				String stitle = "r";
				String path = "p";
				
				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();
				
				}
				
				String title = path + File.separator + stitle;
				
				
				String[] fr = CurveStateIdentifier.CurveStateIdentifier1(outputCurves.getPlotSystem());
				
				if (outputCurves.getOutputFormatSelection().getSelectionIndex() == 0){
					ssp.genXSave(title,fr);
				}
				if (outputCurves.getOutputFormatSelection().getSelectionIndex() == 1){
					ssp.anarodSave(title,fr);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			
				
			}
		});
	    	    
////////////////////////////////////////////////////////////////////
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("SurfaceScatterDialog");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(0.6*w), (int) Math.round(0.8*h));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	
	public PlotSystemCompositeView getPlotSystemCompositeView(){
		return customComposite;
	}
	
	
	public void updateIndicators(int k){
		modify = false;
		if(customComposite.getXValue().equals(ssp.getXValue(k)) == false){
			customComposite.getXValue().setText(String.valueOf(ssp.getXValue(k)));
		}
		if(customComposite.getImageNo().equals(String.valueOf(k)) == false){
			customComposite.getImageNo().setText(String.valueOf(k));
		}	

			
		RectangularROI r = new RectangularROI(ssp.getXValue(k),0.1,0,0.1,0);
		outputCurves.getRegionNo().setROI(r);

		
		modify = true;
	}
	
	public ArrayList<Slider> getSliderList(){
		
		sliderList = null;
				
		sliderList = new ArrayList<Slider>();
		
		sliderList.add(customComposite.getSlider());
		
		if (rszv != null) {
			Slider pslider = (rszv.getSlider());		
			sliderList.add(pslider);
		}
		
		return sliderList;
	}
	
	public RegionSetterZoomedView getRszv(){
		return rszv;
	}
	
	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
}