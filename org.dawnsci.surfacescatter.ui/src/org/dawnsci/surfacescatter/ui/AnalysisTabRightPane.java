package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.VerticalHorizontalSlices;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class AnalysisTabRightPane extends Dialog {

	
	private SuperSashPlotSystem3Composite customComposite2;
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


	public AnalysisTabRightPane(Shell parentShell, 
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
		
		try {
			customComposite2 = new SuperSashPlotSystem3Composite(container, SWT.NONE, ssvs, ssp);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		IDataset k = ssp.returnSubNullImage();
		customComposite2.setData(k);
		customComposite2.setLayout(new GridLayout());
		customComposite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	

		
	

		
//		customComposite1.getSaveButton().addSelectionListener(new SelectionListener() {
//		
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//			
//				FileDialog fd = new FileDialog(getParentShell(), SWT.SAVE); 
//				
//				String stitle = "r";
//				String path = "p";
//				
//				if (fd.open() != null) {
//					stitle = fd.getFileName();
//					path = fd.getFilterPath();
//				}
//			
//				String title = path + File.separator + stitle + ".txt";
//				
//				debug(title);
//				
//				ssp.saveParameters(title);
//			
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			// TODO Auto-generated method stub
//			
//			}
//		});	    
//		
//		customComposite1.getLoadButton().addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN); 
//				
//				String stitle = "r";
//				String path = "p";
//				
//				if (fd.open() != null) {
//					stitle = fd.getFileName();
//					path = fd.getFilterPath();
//				}
//				
//				String title = path + File.separator + stitle;
//				
//				int selection = (int) ssp.loadParameters(title, 
//												         customComposite,
//												         customComposite1,
//												         customComposite2);
//							
//				generalUpdate();
//				
//				
//			}
			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
//		
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

//		customComposite2.getBackgroundButton().addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				generalUpdate();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//
//			}
//		});

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
			
			int[] len = ssp.getLenPt()[0];
			int[] pt = ssp.getLenPt()[1];
			
			int selection = ssp.getSliderPos();
						
//			ssp.sliderMovemementMainImage(selection, 
//					customComposite.getPlotSystem(), 
//					ssvs.getPlotSystemCompositeView().getPlotSystem());
//			
//			ssp.sliderZoomedArea(selection, 
//					customComposite.getGreenRegion().getROI(), 
//					customComposite2.getPlotSystem2());
			
			IRectangularROI greenRegion = new RectangularROI(pt[0], pt[1], len[0], len[1], 0);
			
			Dataset subImage =ssp.subImage(selection,greenRegion);
			
			ILineTrace lt1 = VerticalHorizontalSlices.horizontalslice(
							customComposite2.getRegions()[0].getROI().getBounds(),
							customComposite2.getPlotSystem1(), 
							subImage, 
							greenRegion);
			
			customComposite2.getPlotSystem1().addTrace(lt1);
			
			ILineTrace lt2 = VerticalHorizontalSlices.verticalslice(
							customComposite2.getRegions()[1].getROI().getBounds(), 
							subImage,
							customComposite2.getPlotSystem3(), 
							greenRegion);
			
			customComposite2.getPlotSystem3().addTrace(lt2);	
			
			
			
		
				
				IDataset output = ssp.presenterDummyProcess(selection,
															ssp.getImage(selection),
															3);
		
				ILineTrace lt3 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(), 
						ssp.getTemporaryBackground(),
						greenRegion);
				
				ILineTrace lt4 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						customComposite2.getRegions()[1].getROI().getBounds(),
						customComposite2.getPlotSystem3(),
						ssp.getTemporaryBackground(),
						greenRegion);
				
				
				ILineTrace lt5 = VerticalHorizontalSlices.horizontalsliceBackgroundSubtracted(
						customComposite2.getRegions()[0].getROI().getBounds(),
						customComposite2.getPlotSystem1(), 
						output,
						greenRegion);
				
				ILineTrace lt6 = VerticalHorizontalSlices.verticalsliceBackgroundSubtracted(
						customComposite2.getRegions()[1].getROI().getBounds(),
						customComposite2.getPlotSystem3(),
						output,
						greenRegion);
					
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
		
	
//	public void rOIUpdate(){
//		
//		int[][] LenPt = new int[2][2];
//		
//		modify = false;
//		ssvs.setModify(false);
//		
//		double do0 = 0;
//		try{
//			do0 = Double.parseDouble(customComposite.getXCoord().getText());
//		}
//		catch (Exception e1){
//			ssp.numberFormatWarning();
//		}
//		
//		long f =  ((int) Math.round(do0));
//		
//		int w =  ((int) Math.round(do0));
//		
//		LenPt[1][0] = w;
//		
//		
//		double do1 = 0;
//		try{
//			do1 = Double.parseDouble(customComposite.getXLen().getText());
//		}
//		catch (Exception e1){
//			ssp.numberFormatWarning();
//		}
//		
//		LenPt[0][0] = (int) Math.round(do1);
//		
//		double do2 = 0;
//		try{
//			do2 = Double.parseDouble(customComposite.getYCoord().getText());
//		}
//		catch (Exception e1){
//			ssp.numberFormatWarning();
//		}
//		
//		LenPt[1][1] = (int) Math.round(do2);
//		
//		
//		double do3 = 0;
//		try{
//			do3 = Double.parseDouble(customComposite.getYLen().getText());
//		}
//		catch (Exception e1){
//			ssp.numberFormatWarning();
//		}
//		
//		LenPt[0][1] = (int) Math.round(do3);
//		
//		
//		
//		ssp.regionOfInterestSetter(LenPt);
//		
//		ssvs.getPlotSystemCompositeView().setRegion(LenPt);
//		
//		customComposite.setRegion(LenPt);
//		
//		modify = true;
//		ssvs.setModify(true);
//		
//		
//	}
//	
//	public void roiReset(int[][] LenPt){
//		
//		modify = false;
//		customComposite.setRegion(LenPt);		
//		
//		String[] values = new String[4];
//		
//		values[0] = String.valueOf(LenPt[1][0]);
//		values[1] = String.valueOf(LenPt[0][0]);
//		values[2] = String.valueOf(LenPt[1][1]);
//		values[3] = String.valueOf(LenPt[0][1]);
//		
//		customComposite.setROITexts(values);
//		
//		modify = true;
//		
//	}
//	
//	
////	public Slider getSlider(){
////		
////		return customComposite.getSlider();
////	}
//
//	public void dummyProcessTrigger(){
//		
//		int y = customComposite.getSliderPos();
//		
//		ssp.presenterDummyProcess(y, 
//								  ssp.getImage(y), 
//								  customComposite.getPlotSystem(),
//								  0);
//	}
//	

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
	
}

