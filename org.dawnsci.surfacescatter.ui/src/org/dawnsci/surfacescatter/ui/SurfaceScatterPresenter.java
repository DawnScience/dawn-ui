package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PlotSystemCompositeDataSetter;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.VerticalHorizontalSlices;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.CountUpToArray;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.widgets.Slider;

import com.sun.javafx.collections.MappingChange.Map;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import java.util.SortedMap;
import java.util.TreeMap;

public class SurfaceScatterPresenter {
	
	private String[] filepaths;
	private ArrayList<ExampleModel> models;
	private ArrayList<DataModel> dms;
	private ArrayList<GeometricParametersModel> gms;
	private SuperModel sm;
	private Slider slider;
	private Button button;
	private int noImages;
	private SurfaceScatterViewStart ssvs;
	
	
	public SurfaceScatterPresenter(Shell parentShell, String[] filepaths){
		
		sm = new SuperModel();
	    
		gms = new ArrayList<GeometricParametersModel>();
		dms = new ArrayList<DataModel>();
		models = new ArrayList<ExampleModel>();
		sm.setFilepaths(filepaths);
		IDataset[] imageArray = new IDataset[filepaths.length];
		IDataset[] xArray = new IDataset[filepaths.length];
		TreeMap<Integer, Dataset> som = new TreeMap<Integer, Dataset>();
		ArrayList<Integer> imageRefList = new  ArrayList<>();
		int imageRef =0;
		ArrayList<Integer> imagesToFilepathRef = new ArrayList<Integer>();
		
		
		for (int id = 0; id<filepaths.length; id++){ 
			try {
				models.add(new ExampleModel());
				dms.add(new DataModel());
				gms.add(new GeometricParametersModel());
				IDataHolder dh1 =LoaderFactory.getData(filepaths[id]);
				ILazyDataset ild =dh1.getLazyDataset(gms.get(sm.getSelection()).getImageName());
				dms.get(id).setName(StringUtils.substringAfterLast(sm.getFilepaths()[id], File.separator));
				models.get(id).setDatImages(ild);
				models.get(id).setFilepath(filepaths[id]);
				SliceND slice = new SliceND (ild.getShape());
				IDataset images = ild.getSlice(slice);
				imageArray[id]=images;
				
				for (int f =0; f<(imageArray[id].getShape()[0]); f++){
					
					SliceND slice2 = new SliceND(images.getShape());
					slice2.setSlice(0,f,f+1, 1);
					IDataset nim = images.getSlice(slice2);
					nim.squeeze();
					som.put(imageRef, (Dataset) nim);
					imageRefList.add(imageRef);
					imagesToFilepathRef.add(id);
					imageRef++;
				}
				
				
				
				if (sm.getCorrectionSelection() == 0){
					ILazyDataset ildx =dh1.getLazyDataset(gms.get(sm.getSelection()).getxName()); 
					models.get(id).setDatX(ildx);
					SliceND slice1 = new SliceND (ildx.getShape());
					IDataset xdat = ildx.getSlice(slice1);
					xArray[id]=xdat;
					
				}
				else if (sm.getCorrectionSelection() == 1){
					ILazyDataset ildx =dh1.getLazyDataset(gms.get(sm.getSelection()).getxNameRef()); 
					models.get(id).setDatX(ildx);
					
					SliceND slice1 = new SliceND (ildx.getShape());
					IDataset xdat = ildx.getSlice(slice);
					xArray[id]=xdat;
					
					ILazyDataset dcdtheta = dh1.getLazyDataset( ReflectivityMetadataTitlesForDialog.getdcdtheta());
					models.get(id).setDcdtheta(dcdtheta);
					
					ILazyDataset qdcd = dh1.getLazyDataset( ReflectivityMetadataTitlesForDialog.getqdcd());
					models.get(id).setQdcd(qdcd);
						
						
					if (dcdtheta == null){
						try{
					    	dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());
					    	models.get(id).setDcdtheta(dcdtheta);
						} catch (Exception e2){
							System.out.println("can't get dcdtheta");
						}
					}
					else{
					}
					
					
				}
				else{
				}	
			} 
			
			catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		for (GeometricParametersModel gm : gms){
		
			gm.addPropertyChangeListener(new PropertyChangeListener(){

				public void propertyChange(PropertyChangeEvent evt) {
					for (int id = 0; id<filepaths.length; id++){ 
						try {
							IDataHolder dh1 =LoaderFactory.getData(filepaths[id]);
							ILazyDataset ild =dh1.getLazyDataset(gms.get(sm.getSelection()).getImageName()); 
							models.get(id).setDatImages(ild);
						} 
						
						catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
		}
		
		
		
		Dataset xArrayCon = DatasetUtils.concatenate(xArray, 0);
		Dataset imageCon = DatasetUtils.concatenate(imageArray, 0);
		
		int numberOfImages = xArrayCon.getSize();
		
		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());
		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());
		
		
		for(int sd = 0; sd<imageRefList.size(); sd++){
			imageRefDat.set(imageRefList.get(sd), sd);
			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
		}
		
		Dataset xArrayConClone = xArrayCon.clone(); 
		
		DatasetUtils.sort(xArrayCon, imageRefDat);
		DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);
		
		Dataset[] imageSortedDat = new Dataset[imageRefList.size()];
		int[] filepathsSortedArray = new int[imageRefList.size()]; 
		noImages = imageRefList.size(); 
				
		for (int y = 0; y< imageRefList.size();y++){
			filepathsSortedArray[y] = imagesToFilepathRefDat.getInt(y);
		}
        
		sm.setFilepathsSortedArray(filepathsSortedArray);
		
		for(int rf = 0 ; rf < imageRefList.size(); rf++){
			int pos = imageRefDat.getInt(rf);
			imageSortedDat[rf] = som.get(pos);
		}
		
		sm.setImages(imageSortedDat);
		sm.setImageStack(imageCon);
		sm.setSortedX(xArrayCon);
		
		SliceND slice2 = new SliceND(imageCon.getShape());
		slice2.setSlice(0, 0, 1, 1);
		Dataset nullImage = imageCon.getSlice(slice2);
		
		sm.setNullImage(imageCon.getSlice(slice2));
		
		ssvs = new SurfaceScatterViewStart(parentShell, 
										   filepaths,
										   models,
										   dms,
										   gms,
										   sm,
										   numberOfImages,
										   nullImage,
										   this);
		ssvs.open();
	
		
	}
	
	
	
	public void sliderMovemementMainImage(int sliderPos, IPlottingSystem<Composite>... pS){
		
		sm.setSliderPos(sliderPos);
		Dataset image = sm.getImages()[sliderPos];
	
		for (IPlottingSystem<Composite> x :pS){
			x.updatePlot2D(image, null, null);
		}
	}
	
	public Dataset getImage(int k){
		
		Dataset image = sm.getImages()[k];
		return image;
	}
	
	public void regionOfInterestSetter(IROI green){
		
		
		IRectangularROI greenRectangle = green.getBounds();
		int[] Len = greenRectangle.getIntLengths();
		int[] Pt = greenRectangle.getIntPoint();
		int[][] LenPt = {Len,Pt};
		
		for (ExampleModel m: models){
			m.setBox(greenRectangle);
			m.setLenPt(LenPt);
			m.setROI(green);
		}
	}
	
	public void sliderZoomedArea(int sliderPos, IROI box, IPlottingSystem<Composite>... pS) {

		Dataset image = sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		
		for (IPlottingSystem<Composite> x :pS){
			x.updatePlot2D(subImage, null, null);
		}

	}
	
	public Dataset subImage (int sliderPos, IROI box) {

		Dataset image = sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		return subImage;
	}

	public IDataset presenterDummyProcess (int selection, 
										   IDataset image,
										   IPlottingSystem<Composite> pS,
										   int trackingMarker){
		
		int j = sm.getFilepathsSortedArray()[selection];
		
		return DummyProcessingClass.DummyProcess(sm, 
										  image, 
										  models.get(j),
										  dms.get(j), 
										  gms.get(j), 
										  pS, 
										  sm.getCorrectionSelection(), 
										  selection, 
										  trackingMarker);
		
	}
	
	
	public void zoomAndSet(){
		
	}
	
	public void updateSliders(ArrayList<Slider> sl, int k){
		
		sm.setSliderPos(k);
		
		for(Slider x : sl){
//			if( x.getSelection() != k){
			try{	
				x.setSelection(k);
			}
			catch(Error e){
				
			}
//			}
		}
	}
	
	public SurfaceScatterViewStart getSsvs(){
		return ssvs;
	}
	
	public void updateGreenROIs(ArrayList<Slider> sl, int k){
		
		sm.setSliderPos(k);
		
		for(Slider x : sl){
			if( x.getSelection() != k){
				x.setSelection(k);
			}
		}
	}
	
	
	public void updateAnalysisMethdodology(int methodlogySelection,
										   int fitPowerSelection,
										   int trackerSelection,
										   String boundaryBox){
	
		for (ExampleModel model: models){
			 model.setMethodology(Methodology.values()[methodlogySelection]);
			 model.setFitPower(FitPower.values()[fitPowerSelection]);
			 model.setTrackerType(TrackingMethodology.TrackerType1.values()[trackerSelection]);
			 model.setBoundaryBox(Integer.parseInt(boundaryBox));
		}
		
	}
	
	public int getNoImages(){
		return noImages;
	}
	
	public IROI getROI(){
		
		int jok = sm.getFilepathsSortedArray()[sm.getSliderPos()];
		ExampleModel model = models.get(jok);
		return model.getROI();
	}
	
	public int getSliderPos(){
		return sm.getSliderPos();
	}
	
	public void runTrackingJob(IPlottingSystem<Composite> ps,
							   MultipleOutputCurvesTable outputCurves){
		
		outputCurves.resetCurve();
		trackingJob tj = new trackingJob();
		tj.setCorrectionSelection(sm.getCorrectionSelection());
		tj.setSuperModel(sm);
		tj.setGms(gms);
		tj.setDms(dms);
		tj.setModels(models);
		tj.setPlotSystem(ps);
		tj.setOutputCurves(outputCurves);
		tj.schedule();
		tj.setTimeStep(Math.round((2/noImages)));
		tj.setSsp(this);

		
		
	}
}	
	
/////////////////////////////////////////////////////////////////////
/////////////////////Tracking Job////////////////////////////////////
/////////////////////////////////////////////////////////////////////

	class trackingJob extends Job {

		private ArrayList<DataModel> dms;
		private ArrayList<ExampleModel> models;
		private IPlottingSystem<Composite> plotSystem;
		private MultipleOutputCurvesTable outputCurves;
		private ArrayList<GeometricParametersModel> gms;
		private SuperModel sm;
		private int correctionSelection;
		private int noImages;
		private int timeStep;
		private SurfaceScatterPresenter ssp;
		
		
		public trackingJob() {
			super("updating image...");
		}
		
		public void setOutputCurves(MultipleOutputCurvesTable outputCurves) {
			this.outputCurves = outputCurves;
		}
		
		public void setCorrectionSelection(int cS) {
			this.correctionSelection = cS;
		}
		
		public void setDms(ArrayList<DataModel> dms) {
			this.dms = dms;
		}
		
		public void setModels(ArrayList<ExampleModel> models) {
			this.models = models;
		}
		
		public void setSuperModel(SuperModel sm) {
			this.sm= sm;
		}
		
		public void setGms(ArrayList<GeometricParametersModel> gms) {
			this.gms = gms;
		}	
		
		public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
			this.plotSystem = plotSystem;
		}
		
		public void setTimeStep (int timeStep){
			this.timeStep = timeStep;
		}
		
		public void setSsp(SurfaceScatterPresenter ssp){
			this.ssp = ssp;
		}
		
		@Override
		protected IStatus run (IProgressMonitor monitor) {
		
			noImages = sm.getImages().length;
			
			int[] imagePosInOriginalDat= CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());

			for(DataModel dm:dms){
				dm.resetAll();
			}
			outputCurves.resetCurve();
						
			int k =0;
			
			if (sm.getSliderPos() == 0){ 
				for ( k = 0; k<noImages; k++){
		
					//System.out.println("k value :   " + k);
//					ssp.updateSliders(ssp.getSsvs().getSliderList(), k);
					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
					
					int trackingMarker = 0;
					IDataset j = sm.getImages()[k];
					int jok = sm.getFilepathsSortedArray()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);
					

				
					IDataset output1 = DummyProcessingClass.DummyProcess(sm,
																		 j,
																		 model,
																		 dm, 
																		 gm, 
																		 plotSystem,
																		 correctionSelection, 
																		 imagePosInOriginalDat[k], 
																		 trackingMarker);
					
					dm.addxList(sm.getSortedX().getDouble(k));
					
					
					
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							plotSystem.clear();
							plotSystem.updatePlot2D(output1, null,monitor);
							plotSystem.repaint(true);
							outputCurves.updateCurve(dm, outputCurves.getIntensity().getSelection(), sm);
						}
					});
				}
		
			} else if (sm.getSliderPos() != 0){
		
			
			////////////////////////inside second loop scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////
			
			
				for (k = (sm.getSliderPos()); k >= 0; k-- ){
				
//					ssp.updateSliders(ssp.getSsvs().getSliderList(), k);
					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
					int trackingMarker = 1;
					
					IDataset j = sm.getImages()[k];
					int jok = sm.getFilepathsSortedArray()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);
					
					
				
			
					
				
					IDataset output1 = DummyProcessingClass.DummyProcess(sm,
																		 j, 
																		 model,
																		 dm, 
																		 gm, 
																		 plotSystem,
																		 correctionSelection, 
																		 imagePosInOriginalDat[k], 
																		 trackingMarker);
					
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k], sm.getSortedX().getDouble(k));
					
					
					Display.getDefault().syncExec(new Runnable() {
				
						@Override
						public void run() {
							plotSystem.clear();
							plotSystem.updatePlot2D(output1, null,monitor);
							plotSystem.repaint(true);
							outputCurves.updateCurve(dm, outputCurves.getIntensity().getSelection(), sm);
						}
					});
				}
			
				for ( k = sm.getSliderPos(); k<noImages; k++){
//					ssp.updateSliders(ssp.getSsvs().getSliderList(), k);
					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
					int trackingMarker = 2;
					
					IDataset j = sm.getImages()[k];
					int jok = sm.getFilepathsSortedArray()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);
					
					
					IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
																		 j, 
																		 model,
																		 dm, 
																		 gm, 
																		 plotSystem,
																		 correctionSelection, 
																		 imagePosInOriginalDat[k], 
																		 trackingMarker);
					
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k], sm.getSortedX().getDouble(k));
					
				
					Display.getDefault().syncExec(new Runnable() {
				
						@Override
						public void run() {
						plotSystem.clear();
						plotSystem.updatePlot2D(output1, null,monitor);
						plotSystem.repaint(true);
						outputCurves.updateCurve(dm, outputCurves.getIntensity().getSelection(), sm);
				
						
						}
					});
				}	
		}
		
		
		try {
			Thread.sleep(timeStep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return Status.OK_STATUS;
	
	
	
	}
		
}

