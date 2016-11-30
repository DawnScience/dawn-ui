package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.ClosestNoFinder;//private ArrayList<double[]> locationList; 
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.CurveStateIdentifier;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.dawnsci.surfacescatter.StitchedOutputWithErrors;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

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
	private int DEBUG = 1;
	private IRegion background;
	private IDataset tempImage;
	private IDataset subTempImage;
	private double[] tempLoc;
	private SurfaceScatterPresenter ssp;
	private PrintWriter writer;
	
	public SurfaceScatterPresenter(Shell parentShell, String[] filepaths) {

		sm = new SuperModel();
		ssp= this;
		gms = new ArrayList<GeometricParametersModel>();
		dms = new ArrayList<DataModel>();
		models = new ArrayList<ExampleModel>();
		sm.setFilepaths(filepaths);
		IDataset[] imageArray = new IDataset[filepaths.length];
		IDataset[] xArray = new IDataset[filepaths.length];
		TreeMap<Integer, Dataset> som = new TreeMap<Integer, Dataset>();
		ArrayList<Integer> imageRefList = new ArrayList<>();
		int imageRef = 0;
		ArrayList<Integer> imagesToFilepathRef = new ArrayList<Integer>();

		for (int id = 0; id < filepaths.length; id++) {
			try {
				models.add(new ExampleModel());
				dms.add(new DataModel());
				gms.add(new GeometricParametersModel());
				IDataHolder dh1 = LoaderFactory.getData(filepaths[id]);
				ILazyDataset ild = dh1.getLazyDataset(gms.get(sm.getSelection()).getImageName());
				dms.get(id).setName(StringUtils.substringAfterLast(sm.getFilepaths()[id], File.separator));
				models.get(id).setDatImages(ild);
				models.get(id).setFilepath(filepaths[id]);
				SliceND slice = new SliceND(ild.getShape());
				IDataset images = ild.getSlice(slice);
				imageArray[id] = images;

				for (int f = 0; f < (imageArray[id].getShape()[0]); f++) {

					SliceND slice2 = new SliceND(images.getShape());
					slice2.setSlice(0, f, f + 1, 1);
					IDataset nim = images.getSlice(slice2);
					nim.squeeze();
					som.put(imageRef, (Dataset) nim);
					imageRefList.add(imageRef);
					imagesToFilepathRef.add(id);
					imageRef++;
				}

				if (sm.getCorrectionSelection() == 0) {
					ILazyDataset ildx = dh1.getLazyDataset(gms.get(sm.getSelection()).getxName());
					models.get(id).setDatX(ildx);
					SliceND slice1 = new SliceND(ildx.getShape());
					IDataset xdat = ildx.getSlice(slice1);
					xArray[id] = xdat;

				} else if (sm.getCorrectionSelection() == 1) {
					ILazyDataset ildx = dh1.getLazyDataset(gms.get(sm.getSelection()).getxNameRef());
					models.get(id).setDatX(ildx);

					SliceND slice1 = new SliceND(ildx.getShape());
					IDataset xdat = ildx.getSlice(slice);
					xArray[id] = xdat;

					ILazyDataset dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getdcdtheta());
					models.get(id).setDcdtheta(dcdtheta);

					ILazyDataset qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqdcd());
					models.get(id).setQdcd(qdcd);

					if (dcdtheta == null) {
						try {
							dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());
							models.get(id).setDcdtheta(dcdtheta);
						} catch (Exception e2) {
							System.out.println("can't get dcdtheta");
						}
					} else {
					}

				} else {
				}
			}

			catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		for (GeometricParametersModel gm : gms) {

			gm.addPropertyChangeListener(new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
					for (int id = 0; id < filepaths.length; id++) {
						try {
							IDataHolder dh1 = LoaderFactory.getData(filepaths[id]);
							ILazyDataset ild = dh1.getLazyDataset(gms.get(sm.getSelection()).getImageName());
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

		updateAnalysisMethodology(0, 3, 0, "10");
		
		Dataset xArrayCon = DatasetUtils.concatenate(xArray, 0);
		Dataset imageCon = DatasetUtils.concatenate(imageArray, 0);

		int numberOfImages = xArrayCon.getSize();

		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());
		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());

		for (int sd = 0; sd < imageRefList.size(); sd++) {
			imageRefDat.set(imageRefList.get(sd), sd);
			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
		}

		Dataset xArrayConClone = xArrayCon.clone();

		DatasetUtils.sort(xArrayCon, imageRefDat);
		DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);

		Dataset[] imageSortedDat = new Dataset[imageRefList.size()];
		int[] filepathsSortedArray = new int[imageRefList.size()];
		noImages = imageRefList.size();

		for (int y = 0; y < imageRefList.size(); y++) {
			filepathsSortedArray[y] = imagesToFilepathRefDat.getInt(y);
		}

		sm.setFilepathsSortedArray(filepathsSortedArray);

		for (int rf = 0; rf < imageRefList.size(); rf++) {
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

		ssvs = new SurfaceScatterViewStart(parentShell, filepaths, models, dms, gms, sm, numberOfImages, nullImage,
				this);
		ssvs.open();

	}

	public void sliderMovemementMainImage(int sliderPos, IPlottingSystem<Composite>... pS) {

		sm.setSliderPos(sliderPos);
		Dataset image = sm.getImages()[sliderPos];

		for (IPlottingSystem<Composite> x : pS) {
			x.updatePlot2D(image, null, null);
		}
	}

	public Dataset getImage(int k) {

		Dataset image = sm.getImages()[k];
		return image;
	}

	public void regionOfInterestSetter(IROI green) {

		IRectangularROI greenRectangle = green.getBounds();
		int[] Len = greenRectangle.getIntLengths();
		int[] Pt = greenRectangle.getIntPoint();
		int[][] LenPt = { Len, Pt };

		for (ExampleModel m : models) {
			m.setBox(greenRectangle);
			m.setLenPt(LenPt);
			m.setROI(green);
		}
		
		for (DataModel dm :dms){
			dm.setInitialLenPt(LenPt);
		}
		
		
		sm.setInitialLenPt(LenPt);
	}

	public void sliderZoomedArea(int sliderPos, IROI box, IPlottingSystem<Composite>... pS) {

		Dataset image = sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);

		for (IPlottingSystem<Composite> x : pS) {
			x.updatePlot2D(subImage, null, null);
		}
	}
	
	public int closestImageNo(double in){
		int out = ClosestNoFinder.closestNoPos(in, sm.getSortedX());
		return out;
	}

	public double closestXValue(double in){
		double out = ClosestNoFinder.closestNo(in, sm.getSortedX());
		return out;
	}
	
	public double getXValue(int k){
		return sm.getSortedX().getDouble(k);
	}

	public Dataset subImage(int sliderPos, IROI box) {

		Dataset image = sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		return subImage;
	}

	public IDataset presenterDummyProcess(int selection, IDataset image, IPlottingSystem<Composite> pS,
			int trackingMarker) {

		int j = sm.getFilepathsSortedArray()[selection];

		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
		
		return DummyProcessingClass.DummyProcess(sm,
												 image, 
												 models.get(j), 
												 dms.get(j), 
												 gms.get(j), 
												 pS,
												 sm.getCorrectionSelection(), 
												 imagePosInOriginalDat[selection], 
												 trackingMarker,
												 selection);		
	}

	public void zoomAndSet() {

	}

	public void updateSliders(ArrayList<Slider> sl, int k) {

		sm.setSliderPos(k);

		for (Slider x : sl) {
			if( x.isDisposed() == false){
				try {
					x.setSelection(k);
				} catch (Error e) {
	
				}
			}
		}
	}

	public int xPositionFinder(double myNum) {

		int xPos = ClosestNoFinder.closestNoPos(myNum, sm.getSortedX());

		return xPos;
	}

	public SurfaceScatterViewStart getSsvs() {
		return ssvs;
	}

	public void updateGreenROIs(ArrayList<Slider> sl, int k) {

		sm.setSliderPos(k);

		for (Slider x : sl) {
			if (x.getSelection() != k) {
				x.setSelection(k);
			}
		}
	}

	public void updateAnalysisMethodology(int methodologySelection, int fitPowerSelection, int trackerSelection,
			String boundaryBox) {

		for (ExampleModel model : models) {
			
			if(methodologySelection !=-1){
				model.setMethodology(Methodology.values()[methodologySelection]);
		       }
			if(fitPowerSelection !=-1){
				model.setFitPower(AnalaysisMethodologies.toFitPower(fitPowerSelection));
		       }
			if(trackerSelection !=-1){
				model.setTrackerType(TrackingMethodology.intToTracker1(trackerSelection));
			}
				
			model.setBoundaryBox(Integer.parseInt(boundaryBox));
		
		}
	}

	public String[] getAnalysisSetup(int k){
		
		String[] setup = new String[4];
		
		int jok = sm.getFilepathsSortedArray()[k];
		
		setup[0] = AnalaysisMethodologies.toString(models.get(jok).getMethodology());
		setup[1] = String.valueOf(AnalaysisMethodologies.toInt(models.get(jok).getFitPower()));
		setup[2] = TrackingMethodology.toString(models.get(jok).getTrackerType());
		setup[3] = String.valueOf(models.get(jok).getBoundaryBox());
		
		return setup;
	}
	
	
	
	public int getNoImages() {
		return noImages;
	}
	
	public void genXSave(String title, String[] fr){
		
//		String[] dmnames= new String [dms.size()];
//		
//		for (int v = 0; v<dms.size(); v++){
//			dmnames[v] = dms.get(v).getName();
//		}
		
		
		
		IDataset outputDatY = DatasetFactory.ones(new int[] {1});
		
		String s = gms.get(sm.getSelection()).getSavePath();

		
		try {
			File file = new File(title);
			//file.getParentFile().mkdirs(); 
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
		writer.println("# Test file created: " + strDate);
		writer.println("# Headers: ");
		writer.println("#h	k	l	I	Ie");
		
		ILazyDataset h = SXRDGeometricCorrections.geth(models.get(sm.getSelection()));
		ILazyDataset k = SXRDGeometricCorrections.getk(models.get(sm.getSelection()));
		ILazyDataset l = SXRDGeometricCorrections.getl(models.get(sm.getSelection()));
//		
//		for (int tn = 0 ; tn< dmnames.length; tn++){
//			if (outputCurves.getPlotSystem().getTraces().iterator().next().getName().contains(dmnames[tn])){
				
		
		IDataset outputDatX = sm.xIDataset();

		if (fr[0] == "f"){
			outputDatY = sm.yIDatasetFhkl();
		}
		else{
			outputDatY = sm.yIDataset();
		}
			
		SliceND sliced = new SliceND(h.getShape());
		
		try {
			IDataset hSliced = h.getSlice(sliced);
			IDataset kSliced = k.getSlice(sliced);
			IDataset lSliced = l.getSlice(sliced);
		
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
				writer.println(hSliced.getDouble(gh) +"	"+kSliced.getDouble(gh) +"	"+lSliced.getDouble(gh) + 
						"	"+ outputDatY.getDouble(gh)+ "	"+ Math.pow(outputDatY.getDouble(gh),0.5));
			}
		}
		catch (DatasetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		writer.close();
	}	
	
	public void export(IPlottingSystem<Composite> parentPs, 
						IDataset xData,
						IDataset yData){
		
		sm.setSplicedCurveX(xData);
		sm.setSplicedCurveY(yData);
	
		parentPs.clear();

		ILineTrace lt1 = parentPs.createLineTrace("Ajusted Spliced Curve");
		lt1.setData(xData, yData);
		lt1.isErrorBarEnabled();
		
		parentPs.addTrace(lt1);
		parentPs.repaint();
	}

	public IROI getROI() {

		int jok = sm.getFilepathsSortedArray()[sm.getSliderPos()];
		ExampleModel model = models.get(jok);
		return model.getROI();
	}

	public int getSliderPos() {
		return sm.getSliderPos();
	}

	public void runReplay(IPlottingSystem<Composite> pS,
						  IPlottingSystem<Composite> subPS){
		
		MovieJob mJ = new MovieJob();
		mJ.setSuperModel(sm);
		mJ.setPS(pS);
		mJ.setSubPS(subPS);
		mJ.setTime(220);
		mJ.setSsp(this);
		mJ.setSsvs(ssvs);
		mJ.setSliders(ssvs.getSliderList());
		mJ.run();	
		
		

//		if(mJ.getState() == Job.RUNNING) {
//			mJ.cancel();
//		}
//		mJ.schedule();
//		Display.getDefault().syncExec(new Runnable() {
//			
//			 @Override
//			 public void run() {
				 	
//				for(int k = sm.getSliderPos(); k<sm.getImages().length; k++){
//						
//					tempImage = sm.getImages()[k];
//					subTempImage = sm.getBackgroundDatArray().get(k);
//					tempLoc = sm.getLocationList().get(k);
//				
//					
//					debug("Repaint k ascending 1: "  + k);
//					
//					try {
//						if (pS.getRegion("Background Region")!=null){
//							pS.removeRegion(pS.getRegion("Background Region"));
//						}
//						background = pS.createRegion("Background Region", RegionType.BOX);
//					} 
//					catch (Exception e) {
//						e.printStackTrace();
//					}
//					
//					pS.addRegion(background);
//					RectangularROI newROI = new RectangularROI(tempLoc[1],tempLoc[0],sm.getInitialLenPt()[0][0],sm.getInitialLenPt()[0][1],0);
//					background.setROI(newROI);
//					
//					Display display = Display.getCurrent();
//			        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//					background.setRegionColor(blue);
//				 
//					try {
//						Thread.sleep(220);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					pS.repaint(true);
//					
//					subPS.repaint(true);
//					
//					
//					debug("Repaint k ascending 2: "  + k);
//			 }
//			 
//			 for(int k = (sm.getSliderPos() - 1); k>=0; k--){
//						
//					tempImage = sm.getImages()[k];
//					subTempImage = sm.getBackgroundDatArray().get(k);
//					tempLoc = sm.getLocationList().get(k);
//						
//					debug("Repaint k descending 1: "  + k);
//					
//				 	pS.updatePlot2D(tempImage, null, null);
//					subPS.updatePlot2D(subTempImage, null, null);
//					
//					try {
//						if (pS.getRegion("Background Region")!=null){
//							pS.removeRegion(pS.getRegion("Background Region"));
//						}
//							
//						background = pS.createRegion("Background Region", RegionType.BOX);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					pS.addRegion(background);
//					RectangularROI newROI = new RectangularROI(tempLoc[1],tempLoc[0],sm.getInitialLenPt()[0][0],sm.getInitialLenPt()[0][1],0);
//					background.setROI(newROI);
//					
//					Display display = Display.getCurrent();
//			        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//					background.setRegionColor(blue);
//				 
//			 	
//				 	try {
//						Thread.sleep(220);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					pS.repaint(true);
//					
//					subPS.repaint(true);
//					
//					debug("Repaint k descending 2: "  + k);
//			 	}
			 }
//		});

//	}

	public void runTrackingJob(IPlottingSystem<Composite> subPS, 
							   IPlottingSystem<Composite> outputCurves,
							   IPlottingSystem<Composite> pS) {

		sm.resetAll();
		sm.setLocationList(null);
		for(DataModel md: dms){
			md.resetAll();
		}
				
				
		debug("start of ssp tracking job");

		trackingJob tj = new trackingJob();
		debug("tj invoked");
		tj.setCorrectionSelection(sm.getCorrectionSelection());
		tj.setSuperModel(sm);
		tj.setGms(gms);
		tj.setDms(dms);
		tj.setModels(models);
		tj.setPlotSystem(subPS);
		tj.setOutputCurves(outputCurves);
		tj.setTimeStep(Math.round((2 / noImages)));
		tj.setSsp(this);
		tj.runTJ1();
		
		runReplay(pS,
				  subPS);
		

	}

	public ArrayList<DataModel> getDms() {
		return dms;
	}

	public void stitchAndPresent(MultipleOutputCurvesTableView outputCurves) {

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();

		IDataset[] output = StitchedOutputWithErrors.curveStitch4(dms, sm);

		ILineTrace lt = pS.createLineTrace("progress");

		lt.setData(sm.getSplicedCurveX(), sm.getSplicedCurveY());
		lt.isErrorBarEnabled();
		
		pS.clear();
		pS.addTrace(lt);
		
		pS.repaint();
		pS.autoscaleAxes();

	}
	

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

}

/////////////////////////////////////////////////////////////////////
///////////////////// Tracking Job////////////////////////////////////
/////////////////////////////////////////////////////////////////////

class trackingJob {
//extends job
	private ArrayList<DataModel> dms;
	private ArrayList<ExampleModel> models;
	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> outputCurves;
	private ArrayList<GeometricParametersModel> gms;
	private SuperModel sm;
	private int correctionSelection;
	private int noImages;
	private int timeStep;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 1;

//	public trackingJob() {
//		super("updating image...");
//	}

	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
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
		this.sm = sm;
	}

	public void setGms(ArrayList<GeometricParametersModel> gms) {
		this.gms = gms;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	public void setps(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	@SuppressWarnings("unchecked")
	
	protected  void runTJ1(){
//		ISTATUS
//		(IProgressMonitor monitor)
		
		for(ExampleModel em: models){
			em.setInput(null);
		}
		
		for(DataModel dm :dms){
			dm.resetAll();
		}
		sm.setLocationList(null);

		
		
		
		if (models.get(sm.getSelection()).getMethodology() != AnalaysisMethodologies.Methodology.TWOD_TRACKING) {

			noImages = sm.getImages().length;

			int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());

			for (DataModel dm : dms) {
				dm.resetAll();
			}
			outputCurves.clear();

			int k = 0;

			if (sm.getSliderPos() == 0) {
				for (k = 0; k < noImages; k++) {
						
					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
					
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
																		 trackingMarker, 
																		 k);

					dm.addxList(sm.getSortedX().getDouble(k));
					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					
					
//					 Display.getDefault().syncExec(new Runnable() {
//					
//					 @Override
//					 public void run() {
//					 plotSystem.clear();
//					 plotSystem.updatePlot2D(output1, null,null);
//					 plotSystem.repaint(true);
//					 outputCurves.updateCurve(dm,
//					 outputCurves.getIntensity().getSelection(), sm);
//					 }
//					 });
				}

			} else if (sm.getSliderPos() != 0) {

				//////////////////////// inside second loop
				//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////

				for (k = (sm.getSliderPos()); k >= 0; k--) {

					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
					
					// ssp.updateSliders(ssp.getSsvs().getSliderList(), k);
					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
					int trackingMarker = 1;

					IDataset j = sm.getImages()[k];
					int jok = sm.getFilepathsSortedArray()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);

					IDataset output1 = DummyProcessingClass.DummyProcess(sm, j, model, dm, gm, plotSystem,
							correctionSelection, imagePosInOriginalDat[k], trackingMarker, k);

					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
							sm.getSortedX().getDouble(k));

//					Display.getDefault().syncExec(new Runnable() {
//					
//					 public void run() {
//					 plotSystem.clear();
//					 plotSystem.updatePlot2D(output1, null,null);
//					 plotSystem.repaint(true);
//					 outputCurves.updateCurve(dm,
//					 outputCurves.getIntensity().getSelection(), sm);
//					 }
//					 });
				}

				for (k = sm.getSliderPos(); k < noImages; k++) {
					// ssp.updateSliders(ssp.getSsvs().getSliderList(), k);
					
					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
					
//					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
					int trackingMarker = 2;

					IDataset j = sm.getImages()[k];
					int jok = sm.getFilepathsSortedArray()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);

					IDataset output1 = DummyProcessingClass.DummyProcess(sm, j, model, dm, gm, plotSystem,
							correctionSelection, imagePosInOriginalDat[k], trackingMarker, k);

					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
							sm.getSortedX().getDouble(k));

//					 Display.getDefault().syncExec(new Runnable() {
//					
//					 @Override
//					 public void run() {
//					 plotSystem.clear();
//					 plotSystem.updatePlot2D(output1, null,null);
//					 plotSystem.repaint(true);
//					 outputCurves.updateCurve(dm,
//					 outputCurves.getIntensity().getSelection(), sm);
//					
//					
//					 }
//					 });
				}

			}
		}

		else {

			trackingJob2 tj = new trackingJob2();
			debug("tj2 invoked");
			tj.setCorrectionSelection(sm.getCorrectionSelection());
			tj.setSuperModel(sm);
			tj.setGms(gms);
			tj.setDms(dms);
			tj.setModels(models);
			tj.setPlotSystem(plotSystem);
			tj.setOutputCurves(outputCurves);
			tj.setTimeStep(Math.round(2 / sm.getImages().length));
			tj.setSsp(ssp);
			tj.runTJ2();
//			tj.schedule();

		}

//		return Status.OK_STATUS;
	}
		private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
}

/////////////////////////////////////////////////////////////////////
///////////////////// Tracking Job2////////////////////////////////////
/////////////////////////////////////////////////////////////////////

class trackingJob2 {
//extends Job {

	private ArrayList<DataModel> dms;
	private ArrayList<ExampleModel> models;
	private IPlottingSystem<Composite> plotSystem;
	private IPlottingSystem<Composite> outputCurves;
	private ArrayList<GeometricParametersModel> gms;
	private SuperModel sm;
	private int correctionSelection;
	private int noImages;
	private int timeStep;
	private SurfaceScatterPresenter ssp;
	private int DEBUG = 1;

//	public trackingJob2() {
//		super("updating image...");
//	}

	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
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
		this.sm = sm;
	}

	public void setGms(ArrayList<GeometricParametersModel> gms) {
		this.gms = gms;
	}

	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
		this.plotSystem = plotSystem;
	}

	public void setTimeStep(int timeStep) {
		this.timeStep = timeStep;
	}

	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}

	@SuppressWarnings("unchecked")
//	@Override
	protected void runTJ2() {
//IStatus
//		IProgressMonitor monitor
		
		debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the new tracker~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");

		noImages = sm.getImages().length;

		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());

		for (DataModel dm : dms) {
			dm.resetAll();
		}

		outputCurves.clear();

		int jok = sm.getFilepathsSortedArray()[sm.getSliderPos()];

		String[] doneArray = new String[sm.getFilepaths().length];

		int k = 0;

		if (sm.getSliderPos() == 0) {
			for (k = 0; k < noImages; k++) {
				if (sm.getFilepathsSortedArray()[k] == jok) {
					
					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
					+ " , " + "local jok:  " + Integer.toString(jok));
					
					debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the 0 loop~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");
					ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());

					int trackingMarker = 0;
					IDataset j = sm.getImages()[k];
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
																		 trackingMarker, 
																		 k);

					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					dm.addxList(sm.getSortedX().getDouble(k));
					
//					Display.getDefault().syncExec(new Runnable() {
//						
//						 @Override
//						 public void run() {
//						 plotSystem.clear();
//						 plotSystem.updatePlot2D(output1, null,null);
//						 plotSystem.repaint(true);
//						 outputCurves.updateCurve(dm,
//						 outputCurves.getIntensity().getSelection(), sm);
//						
//						
//						 }
//					 });
					
				}
			}
			doneArray[jok] = "done";

		}

		//////////////////////// inside second loop
		//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////

		else if (sm.getSliderPos() != 0) {

			for (k = (sm.getSliderPos()); k >= 0; k--) {

				if (sm.getFilepathsSortedArray()[k] == jok) {


					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
					+ " , " + "local jok:  " + Integer.toString(jok));
					
					int trackingMarker = 1;
					IDataset j = sm.getImages()[k];
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
																		 trackingMarker, 
																		 k);

					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
							sm.getSortedX().getDouble(k));
				}
			}

			for (k = sm.getSliderPos(); k < noImages; k++) {

				if (sm.getFilepathsSortedArray()[k] == jok) {

					

					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
					+ " , " + "local jok:  " + Integer.toString(jok));
					
					int trackingMarker = 2;
					IDataset j = sm.getImages()[k];
					DataModel dm = dms.get(jok);
					GeometricParametersModel gm = gms.get(jok);
					ExampleModel model = models.get(jok);

					IDataset output1 = 
							DummyProcessingClass.DummyProcess(sm, 
															  j, 
															  model, 
															  dm,
													   		  gm, 
													   		  plotSystem,
													   		  correctionSelection, 
													   		  imagePosInOriginalDat[k], 
													   		  trackingMarker, 
													   		  k);

					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
							sm.getSortedX().getDouble(k));
					
					
//					Display.getDefault().syncExec(new Runnable() {
//						
//						 public void run() {
//						 plotSystem.clear();
//						 plotSystem.updatePlot2D(output1, null,null);
//						 plotSystem.repaint(true);
//						 outputCurves.updateCurve(dm,
//						 outputCurves.getIntensity().getSelection(), sm);
//						
//						
//						 }
//					 });
				}
			}
		}

		doneArray[jok] = "done";

		//////////////////////////// continuing to next
		//////////////////////////// dat////////////////////////////////////////

		while (ClosestNoFinder.full(doneArray, "done") == false) {

			debug("in the while loop");

			int nextk = ClosestNoFinder.closestNoWithoutDone(sm.getSortedX().getDouble(sm.getSliderPos()),
					sm.getSortedX(), doneArray, sm.getFilepathsSortedArray());

			int nextjok = sm.getFilepathsSortedArray()[nextk];

			debug("nextk :" + nextk);
			debug("nextjok :" + nextjok);
			debug("doneArray[nextjok]: " + doneArray[nextjok]);
			
			if (imagePosInOriginalDat[nextk] == 0) {
	
				debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
				
				for (k = nextk; k < noImages; k++) {

					if (sm.getFilepathsSortedArray()[k] == nextjok) {
						ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());


						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						int trackingMarker = 0;
						IDataset j = sm.getImages()[k];
						int jokLocal = sm.getFilepathsSortedArray()[k];
						DataModel dm = dms.get(jokLocal);
						GeometricParametersModel gm = gms.get(jokLocal);
						ExampleModel model = models.get(jokLocal);
						
						
						if(dm.getLocationList() == null){
							
							int seedIndex = 
									ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																		  sm.getSortedX(), 
																		  sm.getLocationList());
	
							int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
							
							
							ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
							ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
							
							Dataset yValues = DatasetFactory.zeros(seedList.size());
							Dataset xValues = DatasetFactory.zeros(seedList.size());
							Dataset lValues = DatasetFactory.zeros(seedList.size());
							
							for(int op = 0; op<seedList.size(); op++){
								
								double x = seedList.get(op)[1];
								double y = seedList.get(op)[0];
								double l = lList.get(op);
								
								xValues.set(x, op);
								yValues.set(y, op);
								lValues.set(l, op);
		
							}
							
							double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																						   lValues, 
																						   xValues, 
																						   yValues, 
																						   sm.getInitialLenPt()[0],
																						   1);
							dm.setSeedLocation(seedLocation);
							
							debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
						
						}	
						
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess1(sm, 
																   j, 
																   model, 
																   dm, 
																   gm, 
																   plotSystem,
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   dm.getSeedLocation());

						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						dm.addxList(sm.getSortedX().getDouble(k));
						
//						Display.getDefault().syncExec(new Runnable() {
//							
//							 @Override
//							 public void run() {
//							 plotSystem.clear();
//							 plotSystem.updatePlot2D(output1, null,null);
//							 plotSystem.repaint(true);
//							 outputCurves.updateCurve(dm,
//							 outputCurves.getIntensity().getSelection(), sm);
//							
//							
//							 }
//						 });

					}
					doneArray[nextjok] = "done";
				}
			}

			else if (imagePosInOriginalDat[nextk] != 0) {

				for (k = (sm.getSliderPos()); k >= 0; k--) {

					if (sm.getFilepathsSortedArray()[k] == nextjok) {

						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						int trackingMarker = 1;
						IDataset j = sm.getImages()[k];
						DataModel dm = dms.get(nextjok);
						GeometricParametersModel gm = gms.get(nextjok);
						ExampleModel model = models.get(nextjok);
						
						if(dm.getLocationList() == null){
							
							int seedIndex = 
									ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																		  sm.getSortedX(), 
																		  sm.getLocationList());
	
							int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
							
							
							ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
							ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
							
							Dataset yValues = DatasetFactory.zeros(seedList.size());
							Dataset xValues = DatasetFactory.zeros(seedList.size());
							Dataset lValues = DatasetFactory.zeros(seedList.size());
							
							for(int op = 0; op<seedList.size(); op++){
								
								double x = seedList.get(op)[1];
								double y = seedList.get(op)[0];
								double l = lList.get(op);
								
								xValues.set(x, op);
								yValues.set(y, op);
								lValues.set(l, op);
		
							}
							
							double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																						   lValues, 
																						   xValues, 
																						   yValues, 
																						   sm.getInitialLenPt()[0],
																						   1);
							dm.setSeedLocation(seedLocation);
							
							debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
							
						
						}	
						
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess1(sm, 
																   j, 
																   model, 
																   dm, 
																   gm, 
																   plotSystem,
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   dm.getSeedLocation());

						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
								sm.getSortedX().getDouble(k));
						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						
//						Display.getDefault().syncExec(new Runnable() {
//							
//							 @Override
//							 public void run() {
//							 plotSystem.clear();
//							 plotSystem.updatePlot2D(output1, null,null);
//							 plotSystem.repaint(true);
//							 outputCurves.updateCurve(dm,
//							 outputCurves.getIntensity().getSelection(), sm);
//							
//							
//							 }
//						 });
					}
				}

				for (k = sm.getSliderPos(); k < noImages; k++) {

					if (sm.getFilepathsSortedArray()[k] == nextjok) {

						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
						
						
						int trackingMarker = 2;
						IDataset j = sm.getImages()[k];
						DataModel dm = dms.get(nextjok);
						GeometricParametersModel gm = gms.get(nextjok);
						ExampleModel model = models.get(nextjok);

						
						if(dm.getLocationList() == null){
							
							int seedIndex = 
									ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
																		  sm.getSortedX(), 
																		  sm.getLocationList());
	
							int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
							
							
							ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
							ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
							
							Dataset yValues = DatasetFactory.zeros(seedList.size());
							Dataset xValues = DatasetFactory.zeros(seedList.size());
							Dataset lValues = DatasetFactory.zeros(seedList.size());
							
							for(int op = 0; op<seedList.size(); op++){
								
								double x = seedList.get(op)[1];
								double y = seedList.get(op)[0];
								double l = lList.get(op);
								
								xValues.set(x, op);
								yValues.set(y, op);
								lValues.set(l, op);
		
							}
							
							double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
																						   lValues, 
																						   xValues, 
																						   yValues, 
																						   sm.getInitialLenPt()[0],
																						   1);
							dm.setSeedLocation(seedLocation);
						
						}	
						
						
						IDataset output1 = 
								DummyProcessingClass.DummyProcess1(sm, 
																   j, 
																   model, 
																   dm, 
																   gm, 
																   plotSystem,
																   correctionSelection, 
																   imagePosInOriginalDat[k], 
																   trackingMarker, 
																   k,
																   dm.getSeedLocation());

						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
								sm.getSortedX().getDouble(k));
						
//						Display.getDefault().syncExec(new Runnable() {
//							
//							 @Override
//							 public void run() {
//							 plotSystem.clear();
//							 plotSystem.updatePlot2D(output1, null,null);
//							 plotSystem.repaint(true);
//							 outputCurves.updateCurve(dm,
//							 outputCurves.getIntensity().getSelection(), sm);
//							
//							
//							 }
//						 });
					}
				}
				doneArray[nextjok] = "done";
			}
		}

		try {
			Thread.sleep(2000*timeStep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	

	private void debug (String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}
}




class MovieJob {
//extends Job {

	private int time = 220;
	private IRegion background;
	private IDataset tempImage;
	private IDataset subTempImage;
	private double[] tempLoc;
	private IPlottingSystem<Composite> plotSystem;
	private SuperModel sm;
	private int correctionSelection;
	private int noImages;
	private int timeStep;
	private int DEBUG = 1;
	private IPlottingSystem<Composite> pS;
	private IPlottingSystem<Composite> subPS;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private int imageNumber;
	private ArrayList<Slider> sliders;

	
	public MovieJob() {
//		super("Playing movie...");
	}
		
	public void setTime(int time) {
		this.time = time;
	}
	
	public void setSsp(SurfaceScatterPresenter ssp) {
		this.ssp = ssp;
	}
	
	public void setSliders(ArrayList<Slider> sliders){
		this.sliders = sliders;
	}
	
	public void setSsvs(SurfaceScatterViewStart ssvs) {
		this.ssvs = ssvs;
	}
	
	public void setSuperModel(SuperModel sm) {
		this.sm = sm;
	}
	
	public void setPS(IPlottingSystem<Composite> pS) {
		this.pS = pS;
	}
	
	public void setSubPS(IPlottingSystem<Composite> subPS) {
		this.subPS = subPS;
	}
	
//	@Override
	protected void run() {
	
//		for( IDataset t: outputDatArray){
//			java.util.Date date = new java.util.Date();
//			System.out.println(date);
//			System.out.println("start:" + date);
//			System.out.println("sum: " + (Double) DatasetUtils.cast(DoubleDataset.class, t).sum());
//			//outputMovie.clear();
//			outputMovie.getPlotSystem().updatePlot2D(t, null, monitor);
//			outputMovie.getPlotSystem().repaint(true);
//			//outputMovie.repaint();
//			try {
//				TimeUnit.MILLISECONDS.sleep(time);
//			} catch (InterruptedException e) {
//			
//				e.printStackTrace();
//			}
//			date = new java.util.Date();
//			System.out.println("stop:" + date);
//			}
		

		try {
			if (pS.getRegion("Background Region")!=null){
				pS.removeRegion(pS.getRegion("Background Region"));
			}
			background = pS.createRegion("Background Region", RegionType.BOX);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		final Display display = Display.getCurrent();
        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		
		background.setRegionColor(blue);
		pS.addRegion(background);
			
		
		Thread t  = new Thread(){
			@Override
			public void run(){
				
				sm.setSliderPos(0);
//				ssp.updateSliders(sliders, 0);
				
				
					for( int k = sm.getSliderPos(); k<sm.getImages().length; k++){
							
						tempImage = sm.getImages()[k];
						subTempImage = sm.getBackgroundDatArray().get(k);
						tempLoc = sm.getLocationList().get(k);
						imageNumber =k;
						double[] tl = tempLoc;
						int[] sml =  sm.getInitialLenPt()[0];
					
						RectangularROI newROI = new RectangularROI(tempLoc[0],tempLoc[1],sm.getInitialLenPt()[0][0],sm.getInitialLenPt()[0][1],0);
						
						
						

						display.syncExec(new Runnable() {
							@Override
							public void run() {
								ssp.updateSliders(ssvs.getSliderList(), imageNumber);
								ssvs.updateIndicators(imageNumber);
								background.setROI(newROI);
								pS.updatePlot2D(tempImage, null, null);
								subPS.updatePlot2D(subTempImage, null, null);
								pS.repaint(true);
								subPS.repaint(true);
							}
						});					 
						try {
							sleep(220);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						debug("Repaint k ascending: "  + k);
				 }
				 
				 for( int k = sm.getSliderPos() - 1; k>=0; k--){
							
						tempImage = sm.getImages()[k];
						subTempImage = sm.getBackgroundDatArray().get(k);
						tempLoc = sm.getLocationList().get(k);
							
			
					 	pS.updatePlot2D(tempImage, null, null);
						subPS.updatePlot2D(subTempImage, null, null);
						
						try {
							if (pS.getRegion("Background Region")!=null){
								pS.removeRegion(pS.getRegion("Background Region"));
							}
								
							background = pS.createRegion("Background Region", RegionType.BOX);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						pS.addRegion(background);
						RectangularROI newROI = new RectangularROI(tempLoc[1],tempLoc[0],sm.getInitialLenPt()[0][0],sm.getInitialLenPt()[0][1],0);
						background.setROI(newROI);
						
						Display display = Display.getCurrent();
				        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
						background.setRegionColor(blue);
					 
				 	
					 	try {
							sleep(220);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						pS.repaint(true);
						subPS.repaint(true);
						debug("Repaint k descending: "  + k);
				 	}
				 }
			};
			t.start();

				 }
			
				
		
//		return Status.OK_STATUS;
	
	private void debug (String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

}

