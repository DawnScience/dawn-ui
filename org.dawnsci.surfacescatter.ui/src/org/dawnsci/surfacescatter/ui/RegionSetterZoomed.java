package org.dawnsci.surfacescatter.ui;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.ui.GeometricParametersWindows;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PlotSystemCompositeDataSetter;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.VerticalHorizontalSlices;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class RegionSetterZoomed extends Dialog {
	
	private String[] filepaths;
	private PlotSystemComposite customComposite;
	private SuperSashPlotSystem2Composite customComposite2;
	private ArrayList<ExampleModel> models;
	private ArrayList<GeometricParametersModel> gms;
	private ArrayList<DataModel> dms;
	private SuperModel sm;
	private Shell parentShell;
	private SashForm right; 
	private SashForm left;
	private GeometricParametersWindows paramField;
	private PlotSystem1Composite customComposite1;
	
	public RegionSetterZoomed(Shell parentShell, int style, 
			SuperModel sm, ArrayList<DataModel> dms, 
			String[] filepaths, ArrayList<ExampleModel> models,
			ArrayList<GeometricParametersModel> gms,
			GeometricParametersWindows paramField) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.parentShell = parentShell;
		this.sm = sm;
		this.dms = dms;
		this.filepaths = filepaths;
		this.models = models;
		this.gms = gms;
		this.paramField = paramField;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite container = (Composite) super.createDialogArea(parent);
		
		SashForm sashForm= new SashForm(container, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		left = new SashForm(sashForm, SWT.VERTICAL);
		
		
		right = new SashForm(sashForm, SWT.VERTICAL);
		
		sashForm.setWeights(new int[]{50,50});
		
		/////////////////Left SashForm///////////////////////////////////////////////////
		Group topImage = new Group(left, SWT.NONE);
		topImage.setText("Top Image");
		GridLayout topImageLayout = new GridLayout();
		topImage.setLayout(topImageLayout);
		GridData topImageData= new GridData(SWT.FILL, SWT.FILL, true, true);
		topImage.setLayoutData(topImageData);
		
		GridData ld1 = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		//////////////////////////Window 2////////////////////////////////////////////////////
				
		customComposite = new PlotSystemComposite(topImage, SWT.NONE, models, sm,
		PlotSystemCompositeDataSetter.imageSetter(models.get(sm.getSelection()), 0), 0);
			
		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(ld1);
	
		//////////////////////////////////////////////////////////

		
		Group mainImage = new Group(left, SWT.NONE);
		mainImage.setText("Main Image");
		GridLayout mainImageLayout = new GridLayout();
		mainImage.setLayout(mainImageLayout);
		GridData mainImageData= new GridData(SWT.FILL, SWT.FILL, true, true);
		mainImage.setLayoutData(mainImageData);
		
		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		
		///////////////////////////Window 3////////////////////////////////////////////////////	    
			    
		customComposite1 = new PlotSystem1Composite(mainImage, 
			SWT.NONE, models, dms, sm, gms.get(sm.getSelection()), 
			customComposite, 0, 0);
		customComposite1.setLayout(new GridLayout());
		customComposite1.setLayoutData(ld2);
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		left.setWeights(new int[] {72,28});
		///////////////////////////////////////////////////////////////////////////////
		/////////////////Right sashform////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////
	
		///////////////////////////Window 6////////////////////////////////////////////////////
		try {
			customComposite2 = new SuperSashPlotSystem2Composite(right, SWT.NONE);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		IDataset k = PlotSystem2DataSetter.PlotSystem2DataSetter1(models.get(sm.getSelection()));
		customComposite2.setData(k);
		models.get(sm.getSelection()).setCurrentImage(k);
		customComposite2.setLayout(new GridLayout());
		customComposite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//////////////////////////////////////////////////////////////////////////////////
		
/////////////////////////////////////////////////////////////////////
/////////////////////////Resized Green ROI///////////////////////// 	    
///////////////////////////////////////////////////////////////////

		customComposite.getGreenRegion().addROIListener(new IROIListener() {
		
			@Override
			public void roiSelected(ROIEvent evt) {
				roiModStandard(evt);
			}
			
			@Override
			public void roiDragged(ROIEvent evt) {
				roiModStandard(evt);
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				roiModStandard(evt);
			}
			
			public void roiModStandard(ROIEvent evt) {
			
			customComposite2.updateImage(PlotSystem2DataSetter.PlotSystem2DataSetter1(models.get(sm.getSelection())));
			
			customComposite2.getPlotSystem1().clear();
			customComposite2.getPlotSystem1().addTrace(VerticalHorizontalSlices.horizontalslice(customComposite2.getRegions()[0].getROI().getBounds(),
			customComposite2.getPlotSystem1(),
			customComposite2.getImage()));
			customComposite2.getPlotSystem1().repaint();
			customComposite2.getPlotSystem1().autoscaleAxes();
			
			customComposite2.getPlotSystem3().clear();
			customComposite2.getPlotSystem3().addTrace(VerticalHorizontalSlices.verticalslice(
			customComposite2.getRegions()[1].getROI().getBounds(),
			customComposite2.getImage(),
			customComposite2.getPlotSystem3()));
			customComposite2.getPlotSystem3().repaint();
			customComposite2.getPlotSystem3().autoscaleAxes();
			
			
			if (customComposite2.getBackgroundButton().getSelection()){
			
			
				SliceND slice = new SliceND(models.get(sm.getSelection()).getDatImages().getShape());
				int sel = models.get(sm.getSelection()).getImageNumber();
				slice.setSlice(0, sel, sel+1, 1);
				IDataset j = null;
				try {
					j = models.get(sm.getSelection()).getDatImages().getSlice(slice);
				} catch (Exception e1){
					e1.printStackTrace();
				}
				
				j.squeeze();
				
				IDataset background = DummyProcessingClass.DummyProcess(sm, j, models.get(sm.getSelection()),
				dms.get(sm.getSelection()), gms.get(sm.getSelection()), 
				customComposite.getPlotSystem(), paramField.getTabFolder().getSelectionIndex(), sel, 0);
				dms.get(sm.getSelection()).setSlicerBackground(background);
				
				ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(customComposite2.getRegions()[0].getROI().getBounds(),
				customComposite2.getPlotSystem1(),
				customComposite2.getImage());
				ILineTrace lt2 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
				customComposite2.getRegions()[0].getROI().getBounds(),
				customComposite2.getPlotSystem1()
				, background);
				
				IDataset backSubTop = Maths.subtract(lt1.getYData(), lt2.getYData());
				IDataset xTop = lt1.getXData();
				ILineTrace lt12BackSub = customComposite2.getPlotSystem1().createLineTrace("background slice");
				lt12BackSub.setData( xTop, backSubTop);
				
				
				ILineTrace lt3 = VerticalHorizontalSlices.verticalslice(
				customComposite2.getRegions()[1].getROI().getBounds(),
				customComposite2.getImage(),
				customComposite2.getPlotSystem3());
				ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
				customComposite2.getRegions()[1].getROI().getBounds(),
				customComposite2.getPlotSystem3(),
				background);
				
				IDataset backSubSide = Maths.subtract(lt3.getYData(), lt4.getYData());
				IDataset xSide = lt3.getYData();
				ILineTrace lt34BackSub = customComposite2.getPlotSystem3().createLineTrace("background slice");
				lt34BackSub.setData(backSubSide,xSide);
				
				customComposite2.getPlotSystem1().clear();
				customComposite2.getPlotSystem1().addTrace(lt1);
				customComposite2.getPlotSystem1().addTrace(lt2);
				customComposite2.getPlotSystem1().addTrace(lt12BackSub);
				customComposite2.getPlotSystem1().repaint();
				customComposite2.getPlotSystem1().autoscaleAxes();
				
				customComposite2.getPlotSystem3().clear();
				customComposite2.getPlotSystem3().addTrace(lt3);
				customComposite2.getPlotSystem3().addTrace(lt4);
				customComposite2.getPlotSystem3().addTrace(lt34BackSub);
				customComposite2.getPlotSystem3().repaint();
				customComposite2.getPlotSystem3().autoscaleAxes();
				}
			}
		});
		

//////////////////////////////////////////////////////////////////////////////////

		
		
	/////////////////////////////////////////////////////////////////////////////////
	/////////////////Slider///////////////////////////
	////////////////////////////////////////////////////
	customComposite.getSlider().addSelectionListener(new SelectionListener() {
	
		public void widgetSelected(SelectionEvent e) {
		
			int selection = customComposite.getSlider().getSelection();
			models.get(sm.getSelection()).setSliderPos(selection);
			
			if (customComposite.getOutputControl().getSelection() == false){
				IDataset jk = PlotSystemCompositeDataSetter.imageSetter(models.get(sm.getSelection()), selection);
				customComposite.updateImage(jk);
				
				customComposite2.updateImage(PlotSystem2DataSetter.PlotSystem2DataSetter1(models.get(sm.getSelection())));
				
				customComposite2.getPlotSystem1().clear();
				customComposite2.getPlotSystem1().addTrace(VerticalHorizontalSlices.horizontalslice(
				customComposite2.getRegions()[0].getROI().getBounds(),
				customComposite2.getPlotSystem1(),
				customComposite2.getImage()));
				customComposite2.getPlotSystem1().repaint();
				customComposite2.getPlotSystem1().autoscaleAxes();
				
				customComposite2.getPlotSystem3().clear();
				customComposite2.getPlotSystem3().addTrace(VerticalHorizontalSlices.verticalslice(
				customComposite2.getRegions()[1].getROI().getBounds(),
				customComposite2.getImage(),
				customComposite2.getPlotSystem3()));
				customComposite2.getPlotSystem3().repaint();
				customComposite2.getPlotSystem3().autoscaleAxes();
				
				
				if(customComposite2.getBackgroundButton().getSelection()){
					CrossHairsBackGround chb = new CrossHairsBackGround();
					chb.setCustomComposite(customComposite);
					chb.setCustomComposite2(customComposite2);
					chb.setGeometricParametersWindows(paramField);
					chb.setDms(dms);
					chb.setGms(gms);
					chb.setModels(models);
					chb.setSm(sm);
					chb.CrossHairsBackground();
				}
			}
			
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
		}
	});
	

///////////////////////////////////////////////////////////////////////////////////	  
////////////////////////////////////////////////////////////////////////////////////
///////////////////////Background slice viewer//////////////////////////////////////
/////////////////Keywords: cross hairs viewer, image examiner/////////////////////////////
////////////////////////////////////////////////////////////////////////
	CrossHairsBackGround chb =new CrossHairsBackGround();
	
		customComposite2.getRegions()[0].addROIListener(new IROIListener(){
		
			@Override
			public void roiSelected(ROIEvent evt) {
				// TODO Auto-generated method stub
				roiStandard1(evt);
			}
		
			@Override
			public void roiDragged(ROIEvent evt) {
				// TODO Auto-generated method stub
				roiStandard1(evt);
			}
		
			@Override
			public void roiChanged(ROIEvent evt) {
				roiStandard1(evt);
		
			}
		
			public void roiStandard1(ROIEvent evt){	
		
				ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(),
						customComposite2.getImage());
		
			
				customComposite2.getPlotSystem1().clear();
				customComposite2.getPlotSystem1().addTrace(lt1);
				customComposite2.getPlotSystem1().repaint();
				customComposite2.getPlotSystem1().autoscaleAxes();
				
				if(customComposite2.getBackgroundButton().getSelection()){
					chb.setCustomComposite(customComposite);
					chb.setCustomComposite2(customComposite2);
					chb.setGeometricParametersWindows(paramField);
					chb.setDms(dms);
					chb.setGms(gms);
					chb.setModels(models);
					chb.setSm(sm);
					chb.CrossHairsBackground();
				}
			}
			
		});
///////////////////////////////////////////////////////////////////////////////
		customComposite2.getRegions()[1].addROIListener(new IROIListener(){
		
		
			@Override
			public void roiSelected(ROIEvent evt) {
			// TODO Auto-generated method stub
			roiStandard2(evt);
			}
		
			@Override
			public void roiDragged(ROIEvent evt) {
			// TODO Auto-generated method stub
			roiStandard2(evt);
			}
		
			@Override
			public void roiChanged(ROIEvent evt) {
			roiStandard2(evt);
			
			}
		
			public void roiStandard2(ROIEvent evt){	
		
			ILineTrace lt3 = VerticalHorizontalSlices.verticalslice(
				customComposite2.getRegions()[1].getROI().getBounds(),
				customComposite2.getImage(),
				customComposite2.getPlotSystem3());
		
		
		
			customComposite2.getPlotSystem3().clear();
			customComposite2.getPlotSystem3().addTrace(lt3);
			customComposite2.getPlotSystem3().repaint();
			customComposite2.getPlotSystem3().autoscaleAxes();
			
			if(customComposite2.getBackgroundButton().getSelection()){
				chb.setCustomComposite(customComposite);
				chb.setCustomComposite2(customComposite2);
				chb.setGeometricParametersWindows(paramField);
				chb.setDms(dms);
				chb.setGms(gms);
				chb.setModels(models);
				chb.setSm(sm);
				chb.CrossHairsBackground();
			}
			
		
			}
		
		});
		
/////////////////////////////////////////////////////////////////////////////////////////	    
//////////////////////////////////Background Curves in cross sections Plotsystem2////////
///////////////////////////////////////////keywords: background display//////////////////////////////////////////////
		
		customComposite2.getBackgroundButton().addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
				CrossHairsBackGround chb = new CrossHairsBackGround();
				chb.setCustomComposite(customComposite);
				chb.setCustomComposite2(customComposite2);
				chb.setGeometricParametersWindows(paramField);
				chb.setDms(dms);
				chb.setGms(gms);					
				chb.setModels(models);
				chb.setSm(sm);
				chb.CrossHairsBackground();
			}
		
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
		
			}
		});

		
				
///////////////////////////////////////////////////////////////////////////////////////	    
			    
	    return container;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ExampleDialog");
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

}

	class CrossHairsBackGround{
		
		private SuperSashPlotSystem2Composite customComposite2;
		private ArrayList<ExampleModel> models;
		private ArrayList<GeometricParametersModel> gms;
		private ArrayList<DataModel> dms;
		private SuperModel sm;
		private PlotSystemComposite customComposite;
		private GeometricParametersWindows paramField;
		
		public void setCustomComposite2(SuperSashPlotSystem2Composite customComposite2){
			this.customComposite2 = customComposite2;
		}
		
		public void setModels (ArrayList<ExampleModel> models){
			this.models =models;
		}
		
		public void setDms (ArrayList<DataModel> dms){
			this.dms = dms;
		}
	
		public void setGms (ArrayList<GeometricParametersModel> gms){
			this.gms = gms;
		}
		
		public void setSm (SuperModel sm){
			this.sm = sm;
		}
		
		public void setCustomComposite(PlotSystemComposite customComposite){
			this.customComposite = customComposite;
		}
		
		public void setGeometricParametersWindows (GeometricParametersWindows paramField){
			this.paramField =paramField;
		}
		
		public void CrossHairsBackground(){
			SliceND slice = new SliceND(models.get(sm.getSelection()).getDatImages().getShape());
			int selection = models.get(sm.getSelection()).getImageNumber();
			slice.setSlice(0, selection, selection+1, 1);
			IDataset j = null;
			
			try {
				j = models.get(sm.getSelection()).getDatImages().getSlice(slice);
			} catch (Exception e1){
				e1.printStackTrace();
			}
			
			j.squeeze();
			
			IDataset background = DummyProcessingClass.DummyProcess(sm, j, models.get(sm.getSelection()),
			dms.get(sm.getSelection()), gms.get(sm.getSelection()), 
			customComposite.getPlotSystem(), paramField.getTabFolder().getSelectionIndex(), selection, 0);
			dms.get(sm.getSelection()).setSlicerBackground(background);
			
			ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
			customComposite2.getRegions()[0].getROI().getBounds(),
			customComposite2.getPlotSystem1(),
			customComposite2.getImage());
			ILineTrace lt2 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
					customComposite2.getRegions()[0].getROI().getBounds(),
					customComposite2.getPlotSystem1()
					, background);
			
			IDataset backSubTop = Maths.subtract(lt1.getYData(), lt2.getYData());
			IDataset xTop = lt1.getXData();
			ILineTrace lt12BackSub = customComposite2.getPlotSystem1().createLineTrace("background slice");
			lt12BackSub.setData( xTop, backSubTop);
			
			
			ILineTrace lt3 = VerticalHorizontalSlices.verticalslice(
			customComposite2.getRegions()[1].getROI().getBounds(),
			customComposite2.getImage(),
			customComposite2.getPlotSystem3());
			ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
					customComposite2.getRegions()[1].getROI().getBounds(),
					customComposite2.getPlotSystem3()
					,background);
			
			IDataset backSubSide = Maths.subtract(lt3.getXData(), lt4.getXData());
			IDataset xSide = lt3.getYData();
			ILineTrace lt34BackSub = customComposite2.getPlotSystem3().createLineTrace("background slice");
			lt34BackSub.setData(backSubSide,xSide);
			
			customComposite2.getPlotSystem1().clear();
			customComposite2.getPlotSystem1().addTrace(lt1);
			customComposite2.getPlotSystem1().addTrace(lt2);
			customComposite2.getPlotSystem1().addTrace(lt12BackSub);
			customComposite2.getPlotSystem1().repaint();
			customComposite2.getPlotSystem1().autoscaleAxes();
			customComposite2.getPlotSystem1().clearAnnotations();
			
			customComposite2.getPlotSystem3().clear();
			customComposite2.getPlotSystem3().addTrace(lt3);
			customComposite2.getPlotSystem3().addTrace(lt4);
			customComposite2.getPlotSystem3().addTrace(lt34BackSub);
			customComposite2.getPlotSystem3().repaint();
			customComposite2.getPlotSystem3().autoscaleAxes();
			customComposite2.getPlotSystem3().clearAnnotations();
	
	}
		
		
	}




