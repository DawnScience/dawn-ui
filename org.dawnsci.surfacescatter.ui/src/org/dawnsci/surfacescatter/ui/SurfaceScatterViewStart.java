package org.dawnsci.surfacescatter.ui;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.PlotSystemCompositeDataSetter;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.roi.XAxis;

import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;



public class SurfaceScatterViewStart extends Dialog {
	
	private String[] filepaths;
	private PlotSystemCompositeView customComposite;
	private MultipleOutputCurvesTableView outputCurves;
	private ArrayList<ExampleModel> models;
	private ArrayList<DataModel> dms;
	private ArrayList<GeometricParametersModel> gms;
	private SuperModel sm;
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
	
	
	public SurfaceScatterViewStart(Shell parentShell, 
			String[] filepaths,
			ArrayList<ExampleModel> models,
			ArrayList<DataModel> dms,
			ArrayList<GeometricParametersModel> gms,
			SuperModel sm,
			int numberOfImages,
			Dataset nullImage,
			SurfaceScatterPresenter ssp) {
		
		super(parentShell);
		
		this.filepaths = filepaths;
		this.models = models;
		this.dms = dms;
		this.gms = gms;
		this.sm = sm;
		this.numberOfImages = numberOfImages;
		this.nullImage = nullImage;
		this.ssp = ssp;
		this.ssvs = this;
		
		
		
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
		
		setupSash.setWeights(new int[]{50,50});

////////////////setupLeft///////////////////////////////////////////////

		
///////////////////////////Window 1  LEFT  SETUP////////////////////////////////////////////////////
		try {

			datDisplayer = new DatDisplayer(left, SWT.FILL, sm, filepaths);
			datDisplayer.setLayout(new GridLayout());
			datDisplayer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



///////////////////////////////////////////////////		

		
		
/////////////////setupRight////////////////////////////////////////

///////////////////////////Window 2 RIGHT SETUP ////////////////////////////////////////////////////
		

	    try {
			paramField = new GeometricParametersWindows(right, SWT.FILL, gms, sm);
			paramField.setLayout(new GridLayout());
			paramField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    paramField.getTabFolder().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				sm.setCorrectionSelection(paramField.getTabFolder().getSelectionIndex());
				
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
		IDataset im = PlotSystemCompositeDataSetter.imageSetter(models.get(sm.getSelection()), 0);
	    customComposite = new PlotSystemCompositeView(anaLeft, 
	    											SWT.FILL,
	    											im,
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
						sm, 
						dms, 
						filepaths, 
						models, 
						gms, 
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
	    
	    customComposite.getSlider().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				 int sliderPos = customComposite.getSlider().getSelection();
				 ssp.sliderMovemementMainImage(sliderPos, customComposite.getPlotSystem());
				 ssp.sliderZoomedArea(sliderPos, 
						 			  customComposite.getGreenRegion().getROI(), 
						 			  customComposite.getSubImagePlotSystem());
				ssvs.updateIndicators(sliderPos);
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
					int k = Integer.parseInt(customComposite.getImageNo().getText());
					ssp.sliderMovemementMainImage(k, customComposite.getPlotSystem());
					ssp.sliderZoomedArea(k, 
							  			 customComposite.getGreenRegion().getROI(), 
							  			 customComposite.getSubImagePlotSystem());
					ssp.updateSliders(ssvs.getSliderList(), k);
//					if(customComposite.getXValue().equals(ssp.getXValue(k)) == false){
//						customComposite.getXValue().setText(String.valueOf(ssp.getXValue(k)));
//					}
//					if(customComposite.getImageNo().equals(String.valueOf(k)) == false){
//						customComposite.getImageNo().setText(String.valueOf(k));
//					}
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
					double in = Double.parseDouble(customComposite.getXValue().getText());
					int k = ssp.closestImageNo(in);
//					double l = ssp.closestXValue(in);
					
					ssp.sliderMovemementMainImage(k, customComposite.getPlotSystem());
					ssp.sliderZoomedArea(k, 
							  			 customComposite.getGreenRegion().getROI(), 
							  			 customComposite.getSubImagePlotSystem());
					
					ssp.updateSliders(ssvs.getSliderList(), k);
					
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
				ssp.runTrackingJob(customComposite.getSubImagePlotSystem()
									,outputCurves);
				
				
				
				
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
															models, 
															dms, 
															sm,
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
				
				String[] cs = CurveStateIdentifier.CurveStateIdentifier1(outputCurves.getPlotSystem());
				
				ArrayList<IDataset> xArrayList = new ArrayList<>();
				ArrayList<IDataset> yArrayList = new ArrayList<>();
				ArrayList<IDataset> yArrayListFhkl = new ArrayList<>();
				ArrayList<IDataset> yArrayListError = new ArrayList<>();
				ArrayList<IDataset> yArrayListFhklError = new ArrayList<>();
				
				int f =0;
				
				for(int p = 0;p<dms.size();p++){
										
					if (dms.get(p).getyList() == null || dms.get(p).getxList() == null) {
						
					} else {
							xArrayList.add(dms.get(p).xIDataset());
							yArrayList.add(dms.get(p).yIDataset());
							yArrayListFhkl.add(dms.get(p).yIDatasetFhkl());
							yArrayListError.add(dms.get(p).yIDatasetError());
							yArrayListFhklError.add(dms.get(p).yIDatasetFhklError());
						}	
				}
				
				GeneralOverlapHandlerView goh = new GeneralOverlapHandlerView(
						getParentShell(), 
						SWT.OPEN, 
						sm,
						xArrayList,
						yArrayList,
						yArrayListError,
						yArrayListFhkl,
						yArrayListFhklError,
						datDisplayer, 
						dms,
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
				}
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
//		if(outputCurves.getRegionNo().getROI().isPlot()){
			
			RectangularROI r = new RectangularROI(ssp.getXValue(k),0.1,0,0.1,0);
			outputCurves.getRegionNo().setROI(r);
//		}
		
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
	
	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
}