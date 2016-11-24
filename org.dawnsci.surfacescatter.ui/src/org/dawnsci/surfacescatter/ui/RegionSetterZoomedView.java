package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PlotSystemCompositeDataSetter;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.VerticalHorizontalSlices;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
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
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.PlatformUI;

public class RegionSetterZoomedView extends Dialog {

	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem2Composite customComposite2;
	private ArrayList<ExampleModel> models;
	private ArrayList<GeometricParametersModel> gms;
	private ArrayList<DataModel> dms;
	private SuperModel sm;
	private SashForm right;
	private SashForm left;
	private PlotSystem1CompositeView customComposite1;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;

	public RegionSetterZoomedView(Shell parentShell, 
			int style, 
			SuperModel sm, 
			ArrayList<DataModel> dms,
			String[] filepaths, 
			ArrayList<ExampleModel> models, 
			ArrayList<GeometricParametersModel> gms,
			GeometricParametersWindows paramField, 
			SurfaceScatterPresenter ssp,
			SurfaceScatterViewStart ssvs) {
		
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.sm = sm;
		this.dms = dms;
		this.models = models;
		this.gms = gms;
		this.ssp = ssp;
		this.ssvs = ssvs;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		final Composite container = (Composite) super.createDialogArea(parent);

		SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		left = new SashForm(sashForm, SWT.VERTICAL);

		right = new SashForm(sashForm, SWT.VERTICAL);

		sashForm.setWeights(new int[] { 50, 50 });

		///////////////// Left
		///////////////// SashForm///////////////////////////////////////////////////
		Group topImage = new Group(left, SWT.NONE);
//		topImage.setText("Top Image");
		GridLayout topImageLayout = new GridLayout();
		topImage.setLayout(topImageLayout);
		GridData topImageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		topImage.setLayoutData(topImageData);

		GridData ld1 = new GridData(SWT.FILL, SWT.FILL, true, true);

		////////////////////////// Window
		////////////////////////// 2////////////////////////////////////////////////////

		customComposite = new PlotSystemCompositeView(topImage, 
				SWT.NONE,
				PlotSystemCompositeDataSetter.imageSetter(models.get(sm.getSelection()), 0),
				0,
				ssp.getNoImages(),
				(Dataset) PlotSystemCompositeDataSetter.imageSetter(models.get(sm.getSelection()), 0),
				ssp,
				ssvs);

		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(ld1);

		//////////////////////////////////////////////////////////

		Group mainImage = new Group(left, SWT.FILL);
//		mainImage.setText("Main Image");
		GridLayout mainImageLayout = new GridLayout();
		mainImage.setLayout(mainImageLayout);
		GridData mainImageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		mainImage.setLayoutData(mainImageData);

		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);

		/////////////////////////// Window
		/////////////////////////// 3////////////////////////////////////////////////////

		customComposite1 = new PlotSystem1CompositeView(mainImage, 
				SWT.NONE, 
				models, 
				dms, 
				sm, 
				gms.get(sm.getSelection()),
				customComposite, 
				0, 
				0, 
				ssp);
		customComposite1.setLayout(new GridLayout());
		customComposite1.setLayoutData(ld2);
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		left.setWeights(new int[] { 85, 15 });
		///////////////////////////////////////////////////////////////////////////////
		///////////////// Right
		/////////////////////////////////////////////////////////////////////////////// sashform////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////

		/////////////////////////// Window
		/////////////////////////// 6////////////////////////////////////////////////////
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
		///////////////////////// Resized Green ROI/////////////////////////
		///////////////////////////////////////////////////////////////////

		customComposite.getGreenRegion().addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				generalUpdate();
			}


		});

		//////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////////////////////////////////////
		///////////////// Slider///////////////////////////
		////////////////////////////////////////////////////
		customComposite.getSlider().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e) {

//				if (customComposite.getOutputControl().getSelection() == false) {
				generalUpdate();
//				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		///////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////
		/////////////////////// Background slice
		/////////////////////////////////////////////////////////////////////////////////// viewer//////////////////////////////////////
		///////////////// Keywords: cross hairs viewer, image
		/////////////////////////////////////////////////////////////////////////////////// examiner/////////////////////////////
		////////////////////////////////////////////////////////////////////////
		

		customComposite2.getRegions()[0].addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				generalUpdate();

			}


		});
		
		
		///////////////////////////////////////////////////////////////////////////////
		customComposite2.getRegions()[1].addROIListener(new IROIListener() {

			@Override
			public void roiSelected(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				generalUpdate();
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				generalUpdate();

			}


		});

		/////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////// Background Curves in cross sections
		///////////////////////////////////////////////////////////////////////////////////////// Plotsystem2////////
		/////////////////////////////////////////// keywords: background
		///////////////////////////////////////////////////////////////////////////////////////// display//////////////////////////////////////////////

		customComposite2.getBackgroundButton().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				generalUpdate();
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

		return new Point((int) Math.round(0.6 * w), (int) Math.round(0.8 * h));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public void generalUpdate(){
		
		customComposite2.getPlotSystem1().clear();
		customComposite2.getPlotSystem3().clear();
		
		
		ssp.regionOfInterestSetter(customComposite.getGreenRegion().getROI());
		
		IRectangularROI greenRectangle = customComposite.getGreenRegion().getROI().getBounds();
		int[] Len = greenRectangle.getIntLengths();
		int[] Pt = greenRectangle.getIntPoint();
		int[][] LenPt = {Len,Pt};
		
		ssvs.getPlotSystemCompositeView().setRegion(LenPt);
		
		int selection = customComposite.getSlider().getSelection();
		
		ssp.updateSliders(ssvs.getSliderList(), selection);
		
		ssp.sliderMovemementMainImage(selection, 
				customComposite.getPlotSystem(), 
				ssvs.getPlotSystemCompositeView().getPlotSystem());
		
		ssp.sliderZoomedArea(selection, 
				customComposite.getGreenRegion().getROI(), 
				customComposite2.getPlotSystem2());
				
		Dataset subImage =ssp.subImage(selection,customComposite.getGreenRegion().getROI());
		
		ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(), 
						subImage, 
						customComposite.getGreenRegion().getROI());
		
		customComposite2.getPlotSystem1().addTrace(lt1);
		
		ILineTrace lt2 = VerticalHorizontalSlices.verticalslice(
						customComposite2.getRegions()[1].getROI().getBounds(), 
						subImage,
						customComposite2.getPlotSystem3(), 
						customComposite.getGreenRegion().getROI());
		
		customComposite2.getPlotSystem3().addTrace(lt2);	
		
		if (customComposite2.getBackgroundButton().getSelection()){
			
			IDataset background = ssp.presenterDummyProcess(selection,
					ssp.getImage(selection),
					customComposite.getPlotSystem(),
					0);
	
			ILineTrace lt3 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
					customComposite2.getRegions()[0].getROI().getBounds(),
					customComposite2.getPlotSystem1(), 
					background,
					customComposite.getGreenRegion().getROI());
			
			ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
					customComposite2.getRegions()[1].getROI().getBounds(),
					customComposite2.getPlotSystem3(),
					background,
					customComposite.getGreenRegion().getROI());
		
			IDataset backSubTop = Maths.subtract(lt1.getYData(), lt3.getYData());
			IDataset xTop = lt1.getXData();
			ILineTrace lt13BackSub = customComposite2.getPlotSystem1().createLineTrace("background slice");
			lt13BackSub.setData( xTop, backSubTop);
			
			customComposite2.getPlotSystem1().addTrace(lt3);
			customComposite2.getPlotSystem1().addTrace(lt13BackSub);
			
			IDataset backSubSide = Maths.subtract(lt2.getXData(), lt4.getXData());
			IDataset xSide = lt2.getYData();
			ILineTrace lt24BackSub = customComposite2.getPlotSystem3().createLineTrace("background slice");
			lt24BackSub.setData(backSubSide,xSide);
			
			customComposite2.getPlotSystem3().addTrace(lt4);
			customComposite2.getPlotSystem3().addTrace(lt24BackSub);
			
		}
		
		customComposite2.getPlotSystem1().clearAnnotations();
		customComposite2.getPlotSystem3().clearAnnotations();
		
		
		
		customComposite2.getPlotSystem1().autoscaleAxes();
		customComposite2.getPlotSystem3().autoscaleAxes();

		customComposite2.getPlotSystem1().repaint();
		customComposite2.getPlotSystem3().repaint();
		
	}	
	
	public Slider getSlider(){
		
		return customComposite.getSlider();
	}

}

