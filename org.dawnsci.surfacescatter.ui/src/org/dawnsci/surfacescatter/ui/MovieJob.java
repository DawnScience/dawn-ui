package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;

public class MovieJob {

	
	private int time = 220;
	private IDataset tempImage;
	private IDataset subTempImage;
	private IDataset subIBgTempImage;
	private double[] tempLoc;
	private DirectoryModel drm;
	private int noImages;
	private int timeStep;
	private int DEBUG = 0;
	private IPlottingSystem<Composite> pS;
	private IPlottingSystem<Composite> subIBgPS;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private int imageNumber;
	private Slider sliders;
	private TabFolder folder;
 

	
	public MovieJob() {
//		super("Playing movie...");
	}
		
	public void setTime(int time) {
		this.time = time;
	}
	
	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setSliders(Slider sliders){
		this.sliders = sliders;
	}
	
	public void setSsvs(SurfaceScatterViewStart ssvs) {
		this.ssvs = ssvs;
	}
	
//	public void setSuperModel(SuperModel sm) {
//		this.sm = sm;
//	}
	
	public void setPS(IPlottingSystem<Composite> pS) {
		this.pS = pS;
	}
	
	public void setSubIBgPS(IPlottingSystem<Composite> subIBgPS) {
		this.subIBgPS = subIBgPS;
	}
	
	public void setFolder (TabFolder folder){
		this.folder = folder;
	}
	
//	@Override
	protected void run() {
		
		this.drm = ssp.getDrm();
		
		final Display display = Display.getCurrent();
		
		Thread t  = new Thread(){
			@Override
			public void run(){
				
//				ssp.setSliderPos(0);
//				
//				int k = 0;
				
				for( int k = 0; k<drm.getFms().size(); k++){
							
					tempImage = ssp.getImage(k);
					subTempImage = drm.getBackgroundDatArray().get(k);
					tempLoc = drm.getFms().get(k).getRoiLocation();
					imageNumber =k;
					
//					int[][] tempLocLenPt = LocationLenPtConverterUtils.locationToLenPtConverter(tempLoc);
					
//					RectangularROI newROI = new RectangularROI(tempLoc[0],
//														       tempLoc[1],
//														       tempLocLenPt[0][0],
//														       tempLocLenPt[0][1],0);
						
					display.syncExec(new Runnable() {
							@Override
							public void run() {
								
								folder.setSelection(folder.getItems().length -1);
								ssp.setSliderPos(imageNumber);
								ssvs.updateIndicators(imageNumber);
								pS.updatePlot2D(tempImage, null, null);
								subIBgPS.updatePlot2D(drm.getBackgroundDatArray().get(imageNumber), null, null);
								pS.repaint(true);
								subIBgPS.repaint(true);
								ssvs.getSsps3c().generalUpdate();
//								
//								double[] location = ssp.getLocationList().get(imageNumber);
//								
//								int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
//								int[] pt = new int[] {(int) location[0],(int) location[1]};
//								int[][] lenPt = { len, pt };

								
								int[][] tempLocLenPt = LocationLenPtConverterUtils.locationToLenPtConverter(tempLoc);
								
								RectangularROI[] greenAndBg = ssp.trackingRegionOfInterestSetter(tempLocLenPt);
								
								ssvs.getCustomComposite().getIRegion().setROI(greenAndBg[0]);
								ssvs.getCustomComposite().getBgRegion().setROI(greenAndBg[1]);
								
								ssp.trackingRegionOfInterestSetter( tempLocLenPt);
								
								
								return;
							}
						});					 
						

						
						debug("Repaint k ascending: "  + k);
				 }
				
			return;	
			}
			
		};
				
		t.start();
	
	}

	private void debug (String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
	
}
