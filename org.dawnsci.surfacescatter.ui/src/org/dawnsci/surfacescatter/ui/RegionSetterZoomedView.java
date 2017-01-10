package org.dawnsci.surfacescatter.ui;

import java.io.File;
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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class RegionSetterZoomedView extends Dialog {

	private PlotSystemCompositeView customComposite;
	private SuperSashPlotSystem2Composite customComposite2;
	private SashForm right;
	private SashForm left;
	private PlotSystem1CompositeView customComposite1;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private boolean modify = true;
	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}


	private int DEBUG = 1;


	public RegionSetterZoomedView(Shell parentShell, 
			int style, 
			String[] filepaths, 
			GeometricParametersWindows paramField, 
			SurfaceScatterPresenter ssp,
			SurfaceScatterViewStart ssvs) {
		
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
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
		GridLayout topImageLayout = new GridLayout();
		topImage.setLayout(topImageLayout);
		GridData topImageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		topImage.setLayoutData(topImageData);

		GridData ld1 = new GridData(SWT.FILL, SWT.FILL, true, true);

		////////////////////////// Window
		////////////////////////// 2////////////////////////////////////////////////////

		customComposite = new PlotSystemCompositeView(topImage, 
				SWT.NONE,
				ssp.returnNullImage(),
				0,
				ssp.getNoImages(),
				(Dataset) ssp.returnSubNullImage(),
				ssp,
				ssvs);

		customComposite.setLayout(new GridLayout());
		customComposite.setLayoutData(ld1);

		//////////////////////////////////////////////////////////

		Group mainImage = new Group(left, SWT.FILL);
		GridLayout mainImageLayout = new GridLayout();
		mainImage.setLayout(mainImageLayout);
		GridData mainImageData = new GridData(SWT.FILL, SWT.FILL, true, true);
		mainImage.setLayoutData(mainImageData);

		GridData ld2 = new GridData(SWT.FILL, SWT.FILL, true, true);

		/////////////////////////// Window
		/////////////////////////// 3////////////////////////////////////////////////////

		customComposite1 = new PlotSystem1CompositeView(mainImage, 
				SWT.NONE, 
				customComposite, 
				0, 
				0, 
				ssp,
				this);
		
		customComposite1.setLayout(new GridLayout());
		customComposite1.setLayoutData(ld2);
		
		//////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////
		left.setWeights(new int[] { 75, 25 });
		///////////////////////////////////////////////////////////////////////////////
		///////////////// Right sashform////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////

		/////////////////////////// Window 6////////////////////////////////////////////////////
		try {
			customComposite2 = new SuperSashPlotSystem2Composite(right, SWT.NONE);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		IDataset k = ssp.returnSubNullImage();
		customComposite2.setData(k);
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
						ssp.updateSliders(ssvs.getSliderList(), k);
						if(customComposite.getXValue().equals(ssp.getXValue(k)) == false){
							customComposite.getXValue().setText(String.valueOf(ssp.getXValue(k)));
						}
						if(customComposite.getImageNo().equals(String.valueOf(k)) == false){
							customComposite.getImageNo().setText(String.valueOf(k));
						}	
						generalUpdate();
						modify = true;
					}				
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					
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
						double l = ssp.closestXValue(in);
						
						ssp.sliderMovemementMainImage(k, customComposite.getPlotSystem());
//						ssp.sliderZoomedArea(k, 
//								  			 customComposite.getGreenRegion().getROI(), 
//								  			 customComposite.getSubImagePlotSystem());
						
						ssp.updateSliders(ssvs.getSliderList(), k);
						if(customComposite.getXValue().equals(String.valueOf(l)) == false){
							customComposite.getXValue().setText(String.valueOf(l));
						}
						if(customComposite.getImageNo().equals(String.valueOf(k)) == false){
							customComposite.getImageNo().setText(String.valueOf(k));
						}
						
						generalUpdate();
						modify = true;
					}		
				}
				
				@Override
				public void focusGained(FocusEvent e) {
				}
		});		
		
		
		/////////////////////////////////////////////////////////////////////////////////
		///////////////// Slider///////////////////////////
		////////////////////////////////////////////////////
		customComposite.getSlider().addSelectionListener(new SelectionListener() {

			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent e) {

//				if (customComposite.getOutputControl().getSelection() == false) {
				if(modify == true){
					modify = false;
					int sliderPos = customComposite.getSlider().getSelection();
					
					if(customComposite.getXValue().equals(ssp.getXValue(sliderPos)) == false){
						customComposite.getXValue().setText(String.valueOf(ssp.getXValue(sliderPos)));
					}
					if(customComposite.getImageNo().equals(String.valueOf(sliderPos)) == false){
						customComposite.getImageNo().setText(String.valueOf(sliderPos));
					}	
					
					ssvs.updateIndicators(sliderPos);
					
					modify = true;
				}
				
				generalUpdate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		
/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////ROI Indicators////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
		for(Text t : customComposite.getROITexts()){
			
			t.addFocusListener(new FocusListener() {
	
				@Override
				public void focusLost(FocusEvent e) {
					rOIUpdate();
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub		
				}
			});
			
		}
////////////////////////////////Save Fit Parameters////////////////////////////////////////
		
		customComposite1.getSaveButton().addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE); 
				
				String stitle = "r";
				String path = "p";
				
				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();
				}
			
				String title = path + File.separator + stitle + ".txt";
				
				debug(title);
				
				ssp.saveParameters(title);
			
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
			}
		});	    
		
		customComposite1.getLoadButton().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN); 
				
				String stitle = "r";
				String path = "p";
				
				if (fd.open() != null) {
					stitle = fd.getFileName();
					path = fd.getFilterPath();
				}
				
				String title = path + File.separator + stitle;
				
				int selection = (int) ssp.loadParameters(title, 
												         customComposite,
												         customComposite1,
												         customComposite2);
							
				generalUpdate();
				
				
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
		
		if(modify){
			customComposite2.getPlotSystem1().clear();
			customComposite2.getPlotSystem3().clear();
			
			ssp.regionOfInterestSetter(customComposite.getGreenRegion().getROI());
			
			IRectangularROI greenRectangle = customComposite.getGreenRegion().getROI().getBounds();
			int[] Len = greenRectangle.getIntLengths();
			int[] Pt = greenRectangle.getIntPoint();
			int[][] LenPt = {Len,Pt};
			
			ssvs.getPlotSystemCompositeView().setRegion(LenPt);
			customComposite.setROITexts(LenPt);
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
				
				IDataset output = ssp.presenterDummyProcess(selection,
						ssp.getImage(selection),
						customComposite.getPlotSystem(),
						3);
		
				ILineTrace lt3 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(), 
						ssp.getTemporaryBackground(),
						customComposite.getGreenRegion().getROI());
				
				ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						customComposite2.getRegions()[1].getROI().getBounds(),
						customComposite2.getPlotSystem3(),
						ssp.getTemporaryBackground(),
						customComposite.getGreenRegion().getROI());
				
				
				ILineTrace lt5 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(), 
						output,
						customComposite.getGreenRegion().getROI());
				
				ILineTrace lt6 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						customComposite2.getRegions()[1].getROI().getBounds(),
						customComposite2.getPlotSystem3(),
						output,
						customComposite.getGreenRegion().getROI());
					
				lt3.setName("background Slice");
				lt4.setName("background Slice");
				
				customComposite2.getPlotSystem1().addTrace(lt3);
				customComposite2.getPlotSystem3().addTrace(lt4);
				customComposite2.getPlotSystem1().addTrace(lt5);
				customComposite2.getPlotSystem3().addTrace(lt6);
				
			}
			
			customComposite2.getPlotSystem1().clearAnnotations();
			customComposite2.getPlotSystem3().clearAnnotations();
			
			customComposite2.getPlotSystem1().autoscaleAxes();
			customComposite2.getPlotSystem3().autoscaleAxes();
	
			customComposite2.getPlotSystem1().repaint();
			customComposite2.getPlotSystem3().repaint();
		}
	}	
	
	public void rOIUpdate(){
		
		int[][] LenPt = new int[2][2];
		
		modify = false;
		ssvs.setModify(false);
		
		double do0 = 0;
		try{
			do0 = Double.parseDouble(customComposite.getXCoord().getText());
		}
		catch (Exception e1){
			ssp.numberFormatWarning();
		}
		
		long f =  ((int) Math.round(do0));
		
		int w =  ((int) Math.round(do0));
		
		LenPt[1][0] = w;
		
		
		double do1 = 0;
		try{
			do1 = Double.parseDouble(customComposite.getXLen().getText());
		}
		catch (Exception e1){
			ssp.numberFormatWarning();
		}
		
		LenPt[0][0] = (int) Math.round(do1);
		
		double do2 = 0;
		try{
			do2 = Double.parseDouble(customComposite.getYCoord().getText());
		}
		catch (Exception e1){
			ssp.numberFormatWarning();
		}
		
		LenPt[1][1] = (int) Math.round(do2);
		
		
		double do3 = 0;
		try{
			do3 = Double.parseDouble(customComposite.getYLen().getText());
		}
		catch (Exception e1){
			ssp.numberFormatWarning();
		}
		
		LenPt[0][1] = (int) Math.round(do3);
		
		
		
		ssp.regionOfInterestSetter(LenPt);
		
		ssvs.getPlotSystemCompositeView().setRegion(LenPt);
		
		customComposite.setRegion(LenPt);
		
		modify = true;
		ssvs.setModify(true);
		
		
	}
	
	public void roiReset(int[][] LenPt){
		
		modify = false;
		customComposite.setRegion(LenPt);		
		
		String[] values = new String[4];
		
		values[0] = String.valueOf(LenPt[1][0]);
		values[1] = String.valueOf(LenPt[0][0]);
		values[2] = String.valueOf(LenPt[1][1]);
		values[3] = String.valueOf(LenPt[0][1]);
		
		customComposite.setROITexts(values);
		
		modify = true;
		
	}
	
	
	public Slider getSlider(){
		
		return customComposite.getSlider();
	}

	public void dummyProcessTrigger(){
		
		int y = customComposite.getSliderPos();
		
		ssp.presenterDummyProcess(y, 
								  ssp.getImage(y), 
								  customComposite.getPlotSystem(),
								  0);
	}
	

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
	
}

