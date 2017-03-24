package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.BoxSlicerRodScanUtilsForDialog;
import org.dawnsci.surfacescatter.ClosestNoFinder;//private ArrayList<double[]> locationList; 
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.FittingParameters;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.FittingParametersOutput;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.InterpolationTracker;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.dawnsci.surfacescatter.StitchedOutputWithErrors;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.TrackerLocationInterpolation;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SurfaceScatterPresenter {

	
	private ArrayList<ExampleModel> models;
	private ArrayList<DataModel> dms;
	private ArrayList<GeometricParametersModel> gms;
	private SuperModel sm;
	private int noImages = 0;
//	private SurfaceScatterViewStart ssvs;
	private String imageName = "file_image";
	
	
	public ArrayList<ExampleModel> getModels() {
		return models;
	}

	public void setModels(ArrayList<ExampleModel> models) {
		this.models = models;
	}

	
	
	public ProccessingMethod getProcessingMethodSelection() {
		return sm.getProcessingMethodSelection();
	}

	public void setProcessingMethodSelection(ProcessingMethodsEnum.ProccessingMethod processingMethodSelection) {
		sm.setProcessingMethodSelection(processingMethodSelection);
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
//		ssvs.getParamField().setImageName(imageName);
	}

	private Set<IPresenterStateChangeEventListener> listeners = new HashSet<>();
	
	public void setSsvs(SurfaceScatterViewStart ssvs) {
//		this.ssvs = ssvs;
	}

	private int DEBUG = 1;
	private PrintWriter writer;
	private Shell parentShell;
	
	public Shell getParentShell() {
		return parentShell;
	}

	public void setParentShell(Shell parentShell) {
		this.parentShell = parentShell;
	}

	private IDataHolder dh1;
	
	public void setDms(ArrayList<DataModel> dms) {
		this.dms = dms;
	}

	public ArrayList<DataModel> getDms() {
		return  dms;
	}
	
	public ArrayList<GeometricParametersModel> getGms() {
		return gms;
	}

	public void setGms(ArrayList<GeometricParametersModel> gms) {
		this.gms = gms;
	}
	
	public void addStateListener(IPresenterStateChangeEventListener listener){
		listeners.add(listener);
	}
	
	private void fireStateListeners(){
		for (IPresenterStateChangeEventListener l : listeners) l.update();
	}
	
	public SurfaceScatterPresenter(){
		sm = new SuperModel();
	}
	
	
	public void surfaceScatterPresenterBuild(Shell parentShell, 
								   String[] filepaths,
								   String xName,
								   String imageFolderPath,
								   String datFolderPath,
								   int correctionSelection) {

		this.parentShell = parentShell;
		sm = new SuperModel();
		gms = new ArrayList<GeometricParametersModel>();
		dms = new ArrayList<DataModel>();
		models = new ArrayList<ExampleModel>();
		sm.setFilepaths(filepaths);
		sm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
		sm.setImageFolderPath(imageFolderPath);

		ILazyDataset[] imageArray = new ILazyDataset[filepaths.length];
		IDataset[] xArray = new IDataset[filepaths.length];
		TreeMap<Integer, ILazyDataset> som = new TreeMap<Integer, ILazyDataset>();
		ArrayList<Integer> imageRefList = new ArrayList<>();
		int imageRef = 0;
		ArrayList<Integer> imagesToFilepathRef = new ArrayList<Integer>();
		
		try {
		
			for (int id = 0; id < filepaths.length; id++) {
				
					models.add(new ExampleModel());
					dms.add(new DataModel());
					gms.add(new GeometricParametersModel());
					
					ssvs.getParamField().localGeometricParametersUpdate(gms.get(id));
					
					gms.get(id).setxName(xName);
					gms.get(id).setxNameRef(xName);
					
					if(imageFolderPath == null){
						dh1 = LoaderFactory.getData(filepaths[id]);
					}
					
					else{
						
						String datName = StringUtils.substringAfterLast(filepaths[id], File.separator);
						
						String localFilepathCopy = StringUtils.substringBeforeLast(datName, ".dat") + "_copy";	
						
						Path from = Paths.get(filepaths[id]);
						
						Path to = Paths.get(sm.getSaveFolder() + localFilepathCopy + ".dat");
						
						Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
						
						Charset charset = StandardCharsets.UTF_8;
	
						String content = new String(Files.readAllBytes(to), charset);
						
						String firstTifName = StringUtils.substringBetween(content, File.separator, ".tif");
						
						if(firstTifName.contains(File.separator)){
							firstTifName = StringUtils.substringAfterLast(firstTifName, File.separator);
						}
						
						String pathNameToReplace = StringUtils.substringBetween(content, "\t", File.separator + firstTifName);
						
						if(pathNameToReplace.contains("\t")){
							pathNameToReplace = StringUtils.substringAfterLast(pathNameToReplace,"\t");
						}
						content = content.replaceAll(pathNameToReplace, imageFolderPath);
						
						Files.write(to, content.getBytes(charset));
						
						dh1 = LoaderFactory.getData(to.toString());
						
					}
					
					ILazyDataset ild = null;
					
					
					ild = dh1.getLazyDataset(gms.get(sm.getSelection()).getImageName());
					
					if(ild == null){
						ild = dh1.getLazyDataset("file_image");
					}
					
					if(ild == null){
						ild = dh1.getLazyDataset("file");
					}
					
					if(ild == null){
						imagesUnavailableWarning();
					}
					
					
					dms.get(id).setName(StringUtils.substringAfterLast(sm.getFilepaths()[id], File.separator));
					models.get(id).setDatImages(ild);
					models.get(id).setFilepath(filepaths[id]);
					SliceND slice = new SliceND(ild.getShape());
					imageArray[id] = ild;
	
					for (int f = 0; f < (imageArray[id].getShape()[0]); f++) {
	
						SliceND slice2 = new SliceND(ild.getShape());
						slice2.setSlice(0, f, f + 1, 1);
						ILazyDataset nim = ild.getSliceView(slice2); //getSlice(slice2);
						som.put(imageRef, (ILazyDataset) nim);
						imageRefList.add(imageRef);
						imagesToFilepathRef.add(id);
						imageRef++;
					}
					
					
					if (MethodSetting.toInt(sm.getCorrectionSelection()) == 0) {
						
						try{
							ILazyDataset ildx = dh1.getLazyDataset(gms.get(sm.getSelection()).getxName());
							models.get(id).setDatX(ildx);
							SliceND slice1 = new SliceND(ildx.getShape());
							IDataset xdat = ildx.getSlice(slice1);
							xArray[id] = xdat;
						}
						
						catch(NullPointerException r){
							
						}
						
					}
					else if(MethodSetting.toInt(sm.getCorrectionSelection()) == 1||
							MethodSetting.toInt(sm.getCorrectionSelection()) == 2||	
							MethodSetting.toInt(sm.getCorrectionSelection()) == 3){
						
						ILazyDataset ildx = dh1.getLazyDataset(gms.get(sm.getSelection()).getxNameRef());
						models.get(id).setDatX(ildx);
	
						SliceND slice1 = new SliceND(ildx.getShape());
						IDataset xdat = ildx.getSlice(slice1);
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
						} 
						else {
						}
						
						if (qdcd == null) {
							try {
								qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());
								models.get(id).setQdcd(qdcd);
							} catch (Exception e2) {
								System.out.println("can't get qdcd");
							}
						} 
						
						else {
						}
					}	
					else {
					}
			}
		}

			catch (Exception e1) {
				e1.printStackTrace();
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

		updateAnalysisMethodology(0, 1, 0, "10");
		
		Dataset xArrayCon = DatasetFactory.zeros(1);
		
		AggregateDataset imageCon = null;
		
		try{
			imageCon = new AggregateDataset(false, imageArray);
		}
		catch(Exception j){
			imageCon = new AggregateDataset(false, DatasetFactory.zeros(new int[] {2, 2}, Dataset.ARRAYFLOAT64));
		}
		
		int numberOfImages = 1; 
		
		try{
			xArrayCon = DatasetUtils.concatenate(xArray, 0);
			numberOfImages = xArrayCon.getSize();
		}
		catch(NullPointerException e){
			
		}
				
		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());
		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());

		for (int sd = 0; sd < imageRefList.size(); sd++) {
			imageRefDat.set(imageRefList.get(sd), sd);
			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
		}

		Dataset xArrayConClone = xArrayCon.clone();

		try{
			DatasetUtils.sort(xArrayCon, imageRefDat);
			DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);

				
		ILazyDataset[] imageSortedDat = new ILazyDataset[imageRefList.size()];
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
		Dataset nullImage = (Dataset) imageCon.getSlice(slice2);

		sm.setNullImage((Dataset) imageCon.getSlice(slice2));

		sm.setNumberOfImages(numberOfImages);
		sm.setNullImage(nullImage);
		}
		catch(Exception e){
			
		}
	
	}
	
	public ArrayList<Double> getYList(){
		return sm.getyList();
	}
	
	public String getXName(){
		return gms.get(0).getxName();
	}
	
	public String getImageFolderPath(){
		return sm.getImageFolderPath();
		
	}
	
	public void setImageFolderPath(String ifp){
		sm.setImageFolderPath(ifp);	
	}
	
	public void setSaveFolder(String sfp){
		sm.setSaveFolder(sfp);	
	}
	
	public String getSaveFolder(){
		return sm.getSaveFolder();	
	}
	
	public MethodSetting getCorrectionSelection(){
		return sm.getCorrectionSelection();
	}
	
	public double getCurrentLorentzCorrection(){
		return sm.getCurrentLorentzCorrection();
	}
	
	public double getCurrentPolarisationCorrection(){
		return sm.getCurrentPolarisationCorrection();
	}
	
	public double getCurrentAreaCorrection(){
		return sm.getCurrentAreaCorrection();
	}
	
	public double getCurrentRawIntensity(){
		return sm.getCurrentRawIntensity();
	}
	
	public void sliderMovemementMainImage(int sliderPos) {
		if(sliderPos != sm.getSliderPos()){ 
			sm.setSliderPos(sliderPos);
		
			fireStateListeners();
		}
	}
	
	public TrackerType1 getTrackerType(){
		return models.get(0).getTrackerType();
	}
	
	public double getCurrentReflectivityFluxCorrection(){
		return sm.getCurrentReflectivityFluxCorrection();
	}
	
	public double getCurrentReflectivityAreaCorrection(){
		return sm.getCurrentReflectivityAreaCorrection();
	}

	public void setCurrentReflectivityFluxCorrection(double l){
		sm.setCurrentReflectivityFluxCorrection(l);
	}
	
	public void setCurrentReflectivityAreaCorrection(double l){
		sm.setCurrentReflectivityAreaCorrection(l);
	}
	

	
	public IDataHolder copiedDatWithCorrectedTifs(String fp, String datFolderPath) {
	
		String localFilepathCopy = StringUtils.substringBeforeLast(fp, ".dat") + "_copy";	
		
		Path from = Paths.get(datFolderPath+  File.separator + fp);
		
		Path to = Paths.get(sm.getSaveFolder() +  File.separator+ localFilepathCopy + ".dat");
		try{
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
			
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(to), charset);
			
			String firstTifName = StringUtils.substringBetween(content, File.separator, ".tif");
			
			if(firstTifName.contains(File.separator)){
				firstTifName = StringUtils.substringAfterLast(firstTifName, File.separator);
			}
			
			String pathNameToReplace = StringUtils.substringBetween(content, "\t", File.separator + firstTifName);
			
			if(pathNameToReplace.contains("\t")){
				pathNameToReplace = StringUtils.substringAfterLast(pathNameToReplace,"\t");
			}
			content = content.replaceAll(pathNameToReplace, sm.getImageFolderPath());
			
			Files.write(to, content.getBytes(charset));
			
			return dh1 = LoaderFactory.getData(to.toString());
		}
		catch(Exception n1){
			n1.printStackTrace();
			return null;
		}
	
	}
	
//	public void correctionsDisplayUpdate(){
//		
//		ssvs.getCustomComposite().generalCorrectionsUpdate();
//	}
	
	public void bgImageUpdate(IPlottingSystem<Composite> subImageBgPlotSystem,
							  int selection){
		
		if(sm.getBackgroundDatArray()!=null){
			try{
				subImageBgPlotSystem.updatePlot2D(sm.getBackgroundDatArray().get(selection),
												  null,
												  null);
			}
			catch(Exception n ){
				IDataset nullImage = DatasetFactory.zeros(new int[] {2,2});
				Maths.add(nullImage, 0.1);
				try{
					subImageBgPlotSystem.updatePlot2D(nullImage,
							  null,
							  null);
				}
				catch(Exception o){
					
				}
			}
		}
		
		else{
			
			IDataset nullImage = DatasetFactory.zeros(new int[] {2,2});
			Maths.add(nullImage, 0.1);
			try{
				subImageBgPlotSystem.updatePlot2D(nullImage, 
												  null, 
												  null);
			}
			catch(NullPointerException g){
				
			}
		}
	}
	
	
	public void addXValuesForFireAccept(){
		
		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
		
		int jok = sm.getFilepathsSortedArray()[sm.getSliderPos()];
		DataModel dm = dms.get(jok);
		ExampleModel model = models.get(jok);
		
		dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[sm.getSliderPos()],
				sm.getSortedX().getDouble(sm.getSliderPos()));
		
		sm.addxList(sm.getImages().length, sm.getSliderPos(),
				sm.getSortedX().getDouble(sm.getSliderPos()));
		
		int[] localPt = sm.getInitialLenPt()[1];
		int[] localLen = sm.getInitialLenPt()[0];

		double[] localLocation = new double[] { (double) (localPt[0]), 
												(double) (localPt[1]), 
												(double) (localPt[0] + localLen[0]),
												(double) (localPt[1]), 
												(double) (localPt[0]), 
												(double) (localPt[1] + localLen[1]), 
												(double) (localPt[0] + localLen[0]),
												(double) (localPt[1] + localLen[1]) };
		
		
		model.setTrackerCoordinates(localLocation);
		sm.addTrackerLocationList(sm.getSliderPos(), localLocation);
		
	}
	
	
	public void saveParameters(String title){
		
		ExampleModel m = models.get(sm.getSelection());
//		System.out.println(title);
		
		FittingParametersOutput.FittingParametersOutputTest(title, 
														    m.getLenPt()[1][0],
														    m.getLenPt()[1][1],
														    m.getLenPt()[0][0], 
														    m.getLenPt()[0][1], 
														    m.getMethodology(), 
														    m.getTrackerType(), 
														    m.getFitPower(), 
														    m.getBoundaryBox(), 
														    sm.getSliderPos(),
														    this.getXValue(sm.getSliderPos()), 
														    sm.getFilepaths()[sm.getFilepathsSortedArray()[sm.getSliderPos()]],
														    sm.getFilepaths());
		
	}
	
	
//	@SuppressWarnings("unchecked")
//	public int loadParameters(String title,
//							   PlotSystemCompositeView pscv,
//							   PlotSystem1CompositeView ps1cv
//							   ){
//		
//		FittingParameters fp = FittingParametersInputReader.reader(title);
//		
//		for( ExampleModel m : models){
//		
//			m.setLenPt(fp.getLenpt());
//			m.setTrackerType(fp.getTracker());
//			m.setFitPower(fp.getFitPower());
//			m.setBoundaryBox(fp.getBoundaryBox());
//			m.setMethodology(fp.getBgMethod());
//		}
//		
//		sm.setInitialLenPt(fp.getLenpt());
//		int selection = this.closestImageNo(fp.getXValue());
//		
//		sm.setInitialLenPt(fp.getLenpt());
//		sm.setSliderPos(this.closestImageNo(fp.getXValue()));
//		
//		ps1cv.setMethodologyDropDown(fp.getBgMethod());
//		ps1cv.setFitPowerDropDown(fp.getFitPower());
//		ps1cv.setTrackerTypeDropDown(fp.getTracker());
//		ps1cv.setBoundaryBox(fp.getBoundaryBox());
//		
//		
//		
//		pscv.setRegion(fp.getLenpt());
//		pscv.redraw();
//		
//		RectangularROI loadedROI = new RectangularROI(fp.getLenpt()[1][0],
//													  fp.getLenpt()[1][1],
//													  fp.getLenpt()[0][0],
//													  fp.getLenpt()[0][1],
//													  0);
//		
//		this.updateSliders(ssvs.getSliderList(),selection);
//		
//		this.sliderMovemementMainImage(selection);
//		
//		this.regionOfInterestSetter(loadedROI);
//		
//		ssvs.updateIndicators(selection);
//		
//		
//		return this.closestImageNo(fp.getXValue());
//		
//	}
	
	
	@SuppressWarnings("unchecked")
	public FittingParameters loadParameters(String title
							   ){
		
		FittingParameters fp = FittingParametersInputReader.reader(title);
		
		for( ExampleModel m : models){
		
			m.setLenPt(fp.getLenpt());
			m.setTrackerType(fp.getTracker());
			m.setFitPower(fp.getFitPower());
			m.setBoundaryBox(fp.getBoundaryBox());
			m.setMethodology(fp.getBgMethod());
		}
		
		sm.setInitialLenPt(fp.getLenpt());
		int selection = this.closestImageNo(fp.getXValue());
		
		sm.setInitialLenPt(fp.getLenpt());
		sm.setSliderPos(this.closestImageNo(fp.getXValue()));
		
		this.sliderMovemementMainImage(selection);
		
		
		
		return fp;
		
		
//		ps1cv.setMethodologyDropDown(fp.getBgMethod());
//		ps1cv.setFitPowerDropDown(fp.getFitPower());
//		ps1cv.setTrackerTypeDropDown(fp.getTracker());
//		ps1cv.setBoundaryBox(fp.getBoundaryBox());
//		
//		
//		
//		pscv.setRegion(fp.getLenpt());
//		pscv.redraw();
		
		
		
//		this.updateSliders(ssvs.getSliderList(),selection);
		
		
		
//		ssvs.updateIndicators(selection);
		
		
//		return this.closestImageNo(fp.getXValue());
		
	}
	
	public Dataset getImage(int k) {
		if(sm != null){
			if(sm.getImages() != null){
		
			
				ILazyDataset image = sm.getImages()[k];
				SliceND slice = new SliceND(image.getShape());
				
					try {
						Dataset f = (Dataset) image.getSlice(slice);
						f.squeeze();
						return f;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						return DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
					}
			}
			else{
				return DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
			}
		}
		return DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
	}
	
	
	public RectangularROI regionOfInterestSetter(IROI green) {

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
		
		try{
			fireStateListeners();;
		}
		catch(NullPointerException f){
			
		}
		
		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(LenPt, 
															   models.get(0).getBoundaryBox(), 
															   models.get(0).getMethodology());
	
		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
												  bgRegionROI[1],
												  bgRegionROI[2],
												  bgRegionROI[3],
												  bgRegionROI[4]);
		
		
		
		return bgROI;
//		try{
//			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgROI);
//		}
//		catch(Exception f){
//			
//		}	
	}
	
//	public void regionOfInterestSetter() {
//
//		IROI green = ssvs.getPlotSystemCompositeView().getPlotSystem().
//				getRegion("myRegion").getROI();
//		
//		IRectangularROI greenRectangle = green.getBounds();
//		int[] Len = greenRectangle.getIntLengths();
//		int[] Pt = greenRectangle.getIntPoint();
//		int[][] LenPt = { Len, Pt };
//
//		for (ExampleModel m : models) {
//			m.setBox(greenRectangle);
//			m.setLenPt(LenPt);
//			m.setROI(green);
//		}
//
//		for (DataModel dm :dms){
//			dm.setInitialLenPt(LenPt);
//		}
//		
//		int[][] ilpt = sm.getInitialLenPt();
//		
//		if(LenPt[0][0] != ilpt[0][0] ||
//		   LenPt[0][1] != ilpt[0][1] ||	
//		   LenPt[1][0] != ilpt[1][0] ||
//		   LenPt[1][1] != ilpt[1][1]){
//			
//			sm.setInitialLenPt(LenPt);
//		
//			try{
//				fireStateListeners();;
//			}
//			catch(NullPointerException f){
//				
//			};
//		}
//		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(LenPt, 
//				   models.get(0).getBoundaryBox(), 
//				   models.get(0).getMethodology());
//
//		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
//												  bgRegionROI[1],
//												  bgRegionROI[2],
//												  bgRegionROI[3],
//												  bgRegionROI[4]);
//		try{
//			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgROI);
//		}
//		catch(Exception f){
//			debug("couldn't get the gold backgrounds");
//		}
//	}
//	
	
	
	
	
	
//	public void trackingRegionOfInterestSetter(int k){
//	
//		double[] loc= sm.getLocationList().get(k);
//		
//		trackingRegionOfInterestSetter(loc, k);
//		
//	}
	
	public ArrayList<double[]> getLocationList(){
		return sm.getLocationList();
	}
	
	public void backgroundBoxesManager(IRegion r1, IRegion r2, Button centreButton){
		
		Display display = Display.getCurrent();
        Color magenta = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
        Color red = display.getSystemColor(SWT.COLOR_RED);

		if (models.get(0).getMethodology() == Methodology.SECOND_BACKGROUND_BOX ||
			models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){


//			IRegion r1 = ssvs.getPlotSystemCompositeView().getBgRegion();
			r1.setVisible(false);
			
//			IRegion r2 = ssvs.getPlotSystemCompositeView().getSecondBgRegion();
			r2.setVisible(true);
			r2.setUserRegion(true);
			r2.setLineWidth(1);
			r2.setMobile(true);
			r2.setFill(true);
			r2.setLineWidth(3);
			
			if (sm.getBackgroundLenPt()!=null){
			
				int[][] redLenPt = sm.getBackgroundLenPt();
				int[] redLen = redLenPt[0];
				int[] redPt = redLenPt[1];
				
				
				RectangularROI startROI = new RectangularROI(redPt[0],
															 redPt[1],
															 redLen[0],
															 redLen[1],
															 0);
			
				r2.setROI(startROI);
				
			}
			
			
			
			r2.setRegionColor(magenta);		
			
			centreButton.setEnabled(true);
			
			if (models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
					
				if (sm.getBoxOffsetLenPt()!=null){
						
						int[][] newOffsetLenPt =sm.getBoxOffsetLenPt();
						int[] len = sm.getInitialLenPt()[0]; 
						int[] pt = sm.getInitialLenPt()[1];
						
						int[] offsetLen = newOffsetLenPt[0];
						int[] offsetPt = newOffsetLenPt[1];
						
						int pt0 = pt[0] + offsetPt[0];
						int pt1 = pt[1] + offsetPt[1];
						
						int len0 = len[0] + offsetLen[0];
						int len1 = len[1] + offsetLen[1];
						
						sm.setBackgroundLenPt(new int[][] {{pt0,pt1},{len0,len1}});
					}
					
					
				else{
						
						int[] len = sm.getInitialLenPt()[0]; 
						int[] pt = sm.getInitialLenPt()[1];
						
						int pt0 = pt[0] + 25;
						int pt1 = pt[1] + 25;
						
						int len0 = len[0] + 0;
						int len1 = len[1] + 0;
					
						IRectangularROI newROI = new RectangularROI(pt0,pt1,len0,len1,0);
					
						r2.setROI(newROI);
						
						sm.setBackgroundLenPt(new int[][] {{pt0,pt1},{len0,len1}});
					}
					
					r2.setRegionColor(red);
					
				}
		}
			else{
				
				r1.setVisible(true);
				r2.setVisible(false);
				
				r2.setRegionColor(magenta);
				r2.setUserRegion(false);
				r2.setLineWidth(1);
				r2.setMobile(false);
				r2.setFill(false);
				r2.setLineWidth(0);
				
				centreButton.setEnabled(false);
				
				
//				this.regionOfInterestSetter();
				
			}
		
	}
	
	public void addToInterpolatorRegions(IRegion region){
		sm.addToInterpolatorRegions(region);
	}
	
	public ArrayList<double[][]> interpolationTrackerBoxesAccept(IRegion r2){
//		
//		Display display = Display.getCurrent();
//		Color blue = display.getSystemColor(SWT.COLOR_CYAN);
//		
//		IRegion r2 = ssvs.getPlotSystemCompositeView().getGreenRegion();
//				
		double[][] box = new double[3][];
				
		double[] lengths = new double[] {(double) r2.getROI().getBounds().getIntLengths()[0], (double) r2.getROI().getBounds().getIntLengths()[1]};
		double[] pts = new double[] {(double) r2.getROI().getBounds().getIntPoint()[0], r2.getROI().getBounds().getIntPoint()[1]};
		double[] xdata = new double[]{(double) sm.getSliderPos(), (double) sm.getSortedX().getDouble(sm.getSliderPos())};
				
		box[0] = lengths;
		box[1] = pts;
		box[2] = xdata;
				
		sm.addToInterpolatorBoxes(box);
				
//		try {
//			IRegion region =ssvs.getPlotSystemCompositeView().getPlotSystem().createRegion(("Interpolation Region: " + sm.getSliderPos()), RegionType.BOX);
//			region.setROI(r2.getROI());
//			sm.addToInterpolatorRegions(region);
//			region.setRegionColor(blue);
//			region.setLineWidth(5);
//			region.setFill(true);
//			region.setUserRegion(false);
//			region.setMobile(false);
//			ssvs.getPlotSystemCompositeView().getPlotSystem().addRegion(region);
//
//		} catch (Exception e1) {
//					// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		if(sm.getInterpolatorBoxes().size() > 1){
			
			ArrayList<double[][]> interpolatedLenPts = InterpolationTracker.interpolatedTrackerLenPtArray(sm.getInterpolatorBoxes(), 
										  sm.getSortedX());
			sm.setInterpolatedLenPts(interpolatedLenPts);
				
			return interpolatedLenPts;
		}
		
		return null;
	}
	
	public ArrayList<double[][]> getInterpolatorBoxes(){
		return sm.getInterpolatorBoxes();
	}
	
	public void interpolationTrackerBoxesReject(){
		
						
//		double u =(double) sm.getSliderPos();
//				
//		if(sm.getInterpolatorBoxes() != null){
//			for(int j = 0; j<sm.getInterpolatorBoxes().size(); j++){
//				if(sm.getInterpolatorBoxes().get(j)[2][0] == u){
//					sm.getInterpolatorBoxes().remove(j);
//					ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(sm.getInterpolatorRegions().get(j));
//					sm.getInterpolatorRegions().remove(j);
//					ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(false);
//				}
//			}
//		}
		
	}
	
	public void illuminateCorrectInterpolationBox(){
		
		if(getTrackerType() == TrackerType1.INTERPOLATION && sm.getInterpolatorRegions()!= null){
			
			double u =(double) sm.getSliderPos();
			
			for(int j =0; j<sm.getInterpolatorBoxes().size(); j++){
				if(sm.getInterpolatorBoxes().get(j)[2][0] == u){
					sm.getInterpolatorRegions().get(j).setFill(true);
				}
				else{
					sm.getInterpolatorRegions().get(j).setFill(false);
				}
			}
		}
	}
	
	public ArrayList<double[][]> getInterpolatedLenPts(){
		return sm.getInterpolatedLenPts();
	}
	
	
	public void illuminateCorrectInterpolationBox(int k){
		
		if(getTrackerType() == TrackerType1.INTERPOLATION && sm.getInterpolatorRegions()!= null){
			
			double u =(double) k;
			
			for(int j =0; j<sm.getInterpolatorRegions().size(); j++){
				if(sm.getInterpolatorBoxes().get(j)[2][0] == u){
					sm.getInterpolatorRegions().get(j).setFill(true);
				}
				else{
					sm.getInterpolatorRegions().get(j).setFill(false);
				}
			}
		}
	}
	
	
	
	public AnalaysisMethodologies.Methodology getBackgroundSubtraction(){
		return models.get(0).getMethodology();
	}
	
	
	public void triggerBoxOffsetTransfer(){
		
		if(models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
			try{
				sm.setPermanentBoxOffsetLenPt(sm.getBoxOffsetLenPt());
			}
			catch(Exception j){
				
			}
		}
		
		if(models.get(0).getMethodology() == Methodology.SECOND_BACKGROUND_BOX){
			try{
				sm.setPermanentBackgroundLenPt(sm.getBackgroundLenPt());
			}
			catch(Exception j){
				
			}
		}
	}
	
//	public void trackingRegionOfInterestSetter(double[] location) {
//
//		int[] len = new int[] {(int) (location[2]-location[0]),(int) (location[5]-location[1])};
//		int[] pt = new int[] {(int) location[0],(int) location[1]};
//		int[][] lenPt = { len, pt };
//		
//		RectangularROI newGreenROI = new RectangularROI(pt[0],
//				pt[1],
//				len[0],
//				len[1],
//				0);
//
//		ssvs.getPlotSystemCompositeView().getIRegion().setROI(newGreenROI);	
//		
//		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 
//				   models.get(0).getBoundaryBox(), 
//				   models.get(0).getMethodology());
//
//		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
//												  bgRegionROI[1],
//												  bgRegionROI[2],
//												  bgRegionROI[3],
//												  bgRegionROI[4]);
//
//		try{
//			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgROI);
//		}
//		catch(Exception f){
//			
//		}
//		
//		if(models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
//		
//			int[] offsetLen = sm.getPermanentBoxOffsetLenPt()[0];
//			int[] offsetPt = sm.getPermanentBoxOffsetLenPt()[1];
//			
//			int pt0 = pt[0] + offsetPt[0];
//			int pt1 = pt[1] + offsetPt[1];
//			
//			
//			int len0 = len[0] + offsetLen[0];
//			int len1 = len[1] + offsetLen[1];
//			
//			RectangularROI offsetBgROI = new RectangularROI(pt0,
//					  pt1,
//					  len0,
//					  len1,
//					  0);
//			
//			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(offsetBgROI);
//		}
//		
//		if(models.get(0).getMethodology() == Methodology.SECOND_BACKGROUND_BOX){
//			
//			int[] bgLen = sm.getPermanentBackgroundLenPt()[0];
//			int[] bgPt = sm.getPermanentBackgroundLenPt()[1];
//			
//			int pt0 = bgPt[0];
//			int pt1 = bgPt[1];
//			
//			
//			int len0 = bgLen[0];
//			int len1 = bgLen[1];
//			
//			RectangularROI bgROI1 = new RectangularROI(pt0,
//					  pt1,
//					  len0,
//					  len1,
//					  0);
//			
//			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(bgROI1);
//		}
//
//		try{
//			ssvs.getSsps3c().generalUpdate(lenPt);
//		}
//		catch(NullPointerException f){
//			
//		}
//			
//	}
	
	public RectangularROI[] trackingRegionOfInterestSetter(int[][] lenPt) {

		int[] len = lenPt[0];
		int[] pt = lenPt[1];
		RectangularROI newGreenROI = new RectangularROI(pt[0],
				pt[1],
				len[0],
				len[1],
				0);

//		ssvs.getPlotSystemCompositeView().getIRegion().setROI(newGreenROI);	
		
		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 
				   models.get(0).getBoundaryBox(), 
				   models.get(0).getMethodology());

		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
												  bgRegionROI[1],
												  bgRegionROI[2],
												  bgRegionROI[3],
												  bgRegionROI[4]);

		return new RectangularROI[] {newGreenROI, bgROI};
		
		
//		try{
//			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgROI);
//		}
//		catch(Exception f){
//			
//		}
		
//		if(models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
//		
//			int[] offsetLen = sm.getPermanentBoxOffsetLenPt()[0];
//			int[] offsetPt = sm.getPermanentBoxOffsetLenPt()[1];
//			
//			int pt0 = pt[0] + offsetPt[0];
//			int pt1 = pt[1] + offsetPt[1];
//			
//			
//			int len0 = len[0] + offsetLen[0];
//			int len1 = len[1] + offsetLen[1];
//			
//			RectangularROI offsetBgROI = new RectangularROI(pt0,
//					  pt1,
//					  len0,
//					  len1,
//					  0);
//			
////			ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(offsetBgROI);
//		}

//		try{
//			ssvs.getSsps3c().generalUpdate(lenPt);
//		}
//		catch(NullPointerException f){
//			
//		}
			
		
	}
	
	public Methodology getMethodology(){
		return models.get(0).getMethodology();
	}
	
	
	public RectangularROI  generateOffsetBgROI(int[][] lenPt){
		
		int[] len = lenPt[0];
		int[] pt = lenPt[1];
		
		int[] offsetLen = sm.getPermanentBoxOffsetLenPt()[0];
		int[] offsetPt = sm.getPermanentBoxOffsetLenPt()[1];
		
		int pt0 = pt[0] + offsetPt[0];
		int pt1 = pt[1] + offsetPt[1];
		
		
		int len0 = len[0] + offsetLen[0];
		int len1 = len[1] + offsetLen[1];
		
		RectangularROI offsetBgROI = new RectangularROI(pt0,
				  pt1,
				  len0,
				  len1,
				  0);
		
		return offsetBgROI;
		
//		ssvs.getPlotSystemCompositeView().getSecondBgRegion().setROI(offsetBgROI);
		
	}
	
	
	public static ILazyDataset concatenate(final ILazyDataset[] as, final int axis) {
		if (as == null || as.length == 0) {
			throw new IllegalArgumentException("No datasets given");
		}
		ILazyDataset a = as[0];
		if (as.length == 1) {			
			return a.clone();
		}
		int[] ashape = a.getShape();
		int at = DTypeUtils.getDType(a);
		int anum = as.length;
		int isize = a.getElementsPerItem();

		int i = 1;
		for (; i < anum; i++) {
			if (at != DTypeUtils.getDType(as[i])) {
//				utilsLogger.error("Datasets are not of same type");
				break;
			}
			if (!ShapeUtils.areShapesCompatible(ashape, as[i].getShape(), axis)) {
//				utilsLogger.error("Datasets' shapes are not equal");
				break;
			}
			final int is = as[i].getElementsPerItem();
			if (isize < is)
				isize = is;
		}
		if (i < anum) {
//			utilsLogger.error("Dataset are not compatible");
			throw new IllegalArgumentException("Datasets are not compatible");
		}

		for (i = 1; i < anum; i++) {
			ashape[axis] += as[i].getShape()[axis];
		}

		ILazyDataset result = DatasetFactory.zeros(isize, ashape, at);

		int[] start = new int[ashape.length];
		int[] stop = ashape;
		stop[axis] = 0;
		for (i = 0; i < anum; i++) {
			ILazyDataset b = as[i];
			int[] bshape = b.getShape();
			stop[axis] += bshape[axis];
			((Dataset) result).setSlice(b, start, stop, null);
			start[axis] += bshape[axis];
		}

		return result;
	}
	
	public RectangularROI[] regionOfInterestSetter(int[][] LenPt) {

		
		RectangularROI green = new RectangularROI(LenPt[1][0],
												  LenPt[1][1],
												  LenPt[0][0],
												  LenPt[0][1],
												  0);

		for (ExampleModel m : models) {
		
			m.setLenPt(LenPt);
			m.setROI(green);
		}
		
		for (DataModel dm :dms){
			dm.setInitialLenPt(LenPt);
		}
		
		if(sm.getInitialLenPt().equals(LenPt) == false){
			sm.setInitialLenPt(LenPt);
		}
		
		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(LenPt, 
				   models.get(0).getBoundaryBox(), 
				   models.get(0).getMethodology());

		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
											      bgRegionROI[1],
											      bgRegionROI[2],
											      bgRegionROI[3],
											      bgRegionROI[4]);
		
		return new RectangularROI[] {green, bgROI};
//		try{
//			ssvs.getPlotSystemCompositeView().getGreenRegion().setROI(green);
//			ssvs.getPlotSystemCompositeView().getBgRegion().setROI(bgROI);
//		}
//		catch(Exception f){
//			
//		}		
	}
	
	public int[][] getLenPt(){
		if (sm != null){
			return sm.getInitialLenPt();
		}
		else{
			return new int[][] {{0,0},{0,0}};
		}
		
	}
	public void setLenPt(int[][] LenPt){
		sm.setInitialLenPt(LenPt);
		fireStateListeners();
	}

	public void sliderZoomedArea(int sliderPos, IROI box, IPlottingSystem<Composite>... pS) {

		Dataset image = this.getImage(sliderPos);
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);


	}
	
//	public void resetCorrectionsSelection(){
//		
//		int  correctionSelection =0;
//		
//		
//		try{
//			correctionSelection = ssvs.getCorrectionsDropDownArray()[ssvs.getCorrectionSelection().getSelectionIndex()];
//			
//		}
//		catch(ArrayIndexOutOfBoundsException e1){
//			correctionSelection = 0;
//
//		}
//		
//		sm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
//	}
//	
	
	public void resetCorrectionsSelection(int  correctionSelection){
		
		sm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
	}
	
	
	
	public int closestImageNo(double in){
		int out = ClosestNoFinder.closestNoPos(in, sm.getSortedX());
		return out;
	}

	public double closestXValue(double in){
		double out = ClosestNoFinder.closestNo(in, sm.getSortedX());
		return out;
	}
	
	public int closestImageIntegerInStack(double in){
		int l = sm.getImages().length;
		int out = ClosestNoFinder.closestIntegerInStack(in, l);
		return out;
	}
	
	public IDataset getTemporaryBackground(){
		return sm.getTemporaryBackgroundHolder();
	}
	
	public ArrayList<IDataset> getBackgroundDatArray(){
		try{
			return sm.getBackgroundDatArray();
		}
		catch(Exception j){
			return null;
		}
	}
	public double getXValue(int k){
		
		if(sm != null){
			if(sm.getSortedX() != null){
				return sm.getSortedX().getDouble(k);
			}
			else{
				return 0;
			}
		}
		else{
			return 0;
		}
	}
	
	public double getXValue(double r){
		
		int k = (int) Math.round(r);
		
		if(sm != null){
			if(sm.getSortedX() != null){
				return sm.getSortedX().getDouble(k);
			}
			else{
				return 0;
			}
		}
		else{
			return 0;
		}
	}
	
	public Dataset subImage(int sliderPos, IROI box) {

		Dataset image = this.getImage(sliderPos);  // sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		return subImage;
	}

	public IDataset presenterDummyProcess(int selection, 
										  IDataset image, 
										  IPlottingSystem<Composite> pS,
										  int trackingMarker) {

		int j = sm.getFilepathsSortedArray()[selection];

		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
		try{
			IDataset output = DummyProcessingClass.DummyProcess(sm,
													 image, 
													 models.get(j), 
													 dms.get(j), 
													 gms.get(j), 
													 pS,
//													 ssvs.getPlotSystemCompositeView().getPlotSystem(),
													 MethodSetting.toInt(sm.getCorrectionSelection()), 
													 imagePosInOriginalDat[selection], 
													 trackingMarker,
													 selection);	
			
			
			sm.addBackgroundDatArray(sm.getImages().length, selection, output);
			
			return output;
			
		}
		catch(IllegalArgumentException s){
			
			 correctionMethodsWarning();
			
			return null;
		}
	}

	public void geometricParametersUpdate(
										  String fluxPath,
										  double beamHeight,
										  double footprint,
										  double angularFudgeFactor,
										  boolean beamCorrection,
										  double beamInPlane,
										  double beamOutPlane,
										  double covar,
										  double detectorSlits,
										  double inPlaneSlits,
										  double inplanePolarisation,
										  double outPlaneSlits,
										  double outplanePolarisation,
										  double scalingFactor,
										  double reflectivityA,
										  double sampleSize,
										  double normalisationFactor,
										  boolean specular,
										  String imageName
										  ){
											
		for(GeometricParametersModel gm: gms){
			gm.setFluxPath(fluxPath);
			gm.setBeamHeight(beamHeight);
			gm.setFootprint(footprint);
			gm.setAngularFudgeFactor(angularFudgeFactor);
			gm.setBeamCorrection(beamCorrection);
			gm.setBeamInPlane(beamInPlane);
			gm.setBeamOutPlane(beamOutPlane);
			gm.setCovar(covar);
			gm.setDetectorSlits(detectorSlits);
			gm.setInPlaneSlits(inPlaneSlits);
			gm.setInplanePolarisation(inplanePolarisation);
			gm.setOutPlaneSlits(outPlaneSlits);
			gm.setOutplanePolarisation(outplanePolarisation);
			gm.setScalingFactor(scalingFactor);
			gm.setReflectivityA(reflectivityA);
			gm.setSampleSize(sampleSize);
			gm.setNormalisationFactor(normalisationFactor);
			gm.setSpecular(specular);
			gm.setImageName(imageName);
		}
		
	}
	
	public ArrayList<ArrayList<IDataset>> xyArrayPreparer(){
		
		ArrayList<ArrayList<IDataset>> output = new ArrayList<>();
		
		ArrayList<IDataset> xArrayList = new ArrayList<>();
		ArrayList<IDataset> yArrayList = new ArrayList<>();
		ArrayList<IDataset> yArrayListFhkl = new ArrayList<>();
		ArrayList<IDataset> yArrayListError = new ArrayList<>();
		ArrayList<IDataset> yArrayListFhklError = new ArrayList<>();
		ArrayList<IDataset> yArrayListRaw= new ArrayList<>();
		ArrayList<IDataset> yArrayListRawError = new ArrayList<>();
		
		for(int p = 0;p<dms.size();p++){
								
			if (dms.get(p).getyList() == null || dms.get(p).getxList() == null) {
				
			} 
			
			else {
					xArrayList.add(dms.get(p).xIDataset());
					yArrayList.add(dms.get(p).yIDataset());
					yArrayListError.add(dms.get(p).yIDatasetError());
					yArrayListFhkl.add(dms.get(p).yIDatasetFhkl());
					yArrayListFhklError.add(dms.get(p).yIDatasetFhklError());
					yArrayListRaw.add(dms.get(p).yRawIDataset());
					yArrayListRawError.add(dms.get(p).yRawIDatasetError());
				}	
		}
		
		output.add(0, xArrayList);
		output.add(1, yArrayList);
		output.add(2, yArrayListFhkl);
		output.add(3, yArrayListError);
		output.add(4, yArrayListFhklError);
		output.add(5, yArrayListRaw);
		output.add(6, yArrayListRawError);
		
		return output;
	}
	
	
	public int getNumberOfImages(){
		try{
			return sm.getNumberOfImages();
		}
		catch (Exception h){
			return 0;
		}
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

//	public SurfaceScatterViewStart getSsvs() {
//		return ssvs;
//	}

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
			
			double r = 0;
			try{
				r = Double.parseDouble(boundaryBox);
			}
			catch (Exception e1){
				this.numberFormatWarning();
			}
			
			model.setBoundaryBox((int) Math.round(r));
		}
		
//		if(TrackingMethodology.intToTracker1(trackerSelection) != TrackerType1.INTERPOLATION 
//				&& sm.getInterpolatorRegions() != null){
//			
//			for(IRegion g : sm.getInterpolatorRegions()){
//				ssvs.getPlotSystemCompositeView().getPlotSystem().removeRegion(g);
//				g.remove();
//				
//			}
//			try{
//				ssvs.getPlotSystemCompositeView().getPlotSystem().removeTrace(ssvs.getPlotSystemCompositeView().getPlotSystem().getTrace("Interpolated trajectory"));	
//			}
//			catch(Exception g){
//				
//			}
//			sm.setInterpolatorRegions(null);
//			
//			if(sm.getInterpolatorBoxes() != null){
//				sm.setInterpolatorBoxes(null);
//			}
//			
//			ssvs.getPlotSystemCompositeView().getPlotSystem1CompositeView().getAcceptLocation().setEnabled(false);
//			ssvs.getPlotSystemCompositeView().getPlotSystem1CompositeView().getRejectLocation().setEnabled(false);
//		}
//		
//		else if(TrackingMethodology.intToTracker1(trackerSelection) == TrackerType1.INTERPOLATION &&
//				sm.getTrackerOn()){
//	
//			ssvs.getPlotSystemCompositeView().getPlotSystem1CompositeView().getAcceptLocation().setEnabled(true);
//			ssvs.getPlotSystemCompositeView().getPlotSystem1CompositeView().getRejectLocation().setEnabled(true);
//		}
	}
	
	public ArrayList<IRegion> getInterpolatorRegions(){
		return sm.getInterpolatorRegions();
	}
	
	public void setInterpolatorBoxes(ArrayList<double[][]> boxes){
		sm.setInterpolatorBoxes(boxes);
	}
	
	public void setInterpolatorRegions(ArrayList<IRegion> boxes){
		sm.setInterpolatorRegions(boxes);
	}

	public String[] getAnalysisSetup(int k){
		
		String[] setup = new String[4];
		
		try{
				
			int jok = sm.getFilepathsSortedArray()[k];
			
			setup[0] = AnalaysisMethodologies.toString(models.get(jok).getMethodology());
			setup[1] = String.valueOf(AnalaysisMethodologies.toInt(models.get(jok).getFitPower()));
			setup[2] = TrackingMethodology.toString(models.get(jok).getTrackerType());
			setup[3] = String.valueOf(models.get(jok).getBoundaryBox());
			
		}
		
		catch(NullPointerException s){
			
			setup[0] = AnalaysisMethodologies.toString(Methodology.TWOD);
			setup[1] = String.valueOf(1);
			setup[2] = TrackingMethodology.toString(TrackingMethodology.TrackerType1.TLD);
			setup[3] = String.valueOf(10);
		}
		
		return setup;
		
	}
	
	public int getNoImages() {
		return noImages;
	}
	
	public void genXSave(String title, String[] fr){
	
		try {
			File file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    writer.println("#Output file created: " + strDate);
	    
	    if (sm.getCorrectionSelection() == MethodSetting.SXRD){
	    

		    IDataset[] hArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] kArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] lArray = new IDataset[sm.getFilepaths().length];
		    
		    for (int id = 0; id < sm.getFilepaths().length; id++) {
		    
		    	ILazyDataset h = SXRDGeometricCorrections.geth(models.get(id));
				ILazyDataset k = SXRDGeometricCorrections.getk(models.get(id));
				ILazyDataset l = SXRDGeometricCorrections.getl(models.get(id));
				
				hArray[id] = (IDataset) h;
				kArray[id] = (IDataset) k;
				lArray[id] = (IDataset) l;
				
		    }
		    
		    Dataset hArrayCon = DatasetUtils.concatenate(hArray, 0);
		    Dataset kArrayCon = DatasetUtils.concatenate(kArray, 0);
		    Dataset lArrayCon = DatasetUtils.concatenate(lArray, 0);	
				
		    hArrayCon.sort(0);
		    kArrayCon.sort(0);
		    lArrayCon.sort(0);
		    
			
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(hArrayCon.getDouble(gh) +"	"+ kArrayCon.getDouble(gh) +"	"+lArrayCon.getDouble(gh) + 
							"	"+ sm.getSplicedCurveYFhkl().getDouble(gh)+ "	"+ sm.getSplicedCurveY().getError(gh));
			}
	    }
		
		else{
		    
			
		    if (models.get(0).getQdcdDat() != null){
		    	 
		    	IDataset[] qdcd = new IDataset[sm.getFilepaths().length];
			    Dataset qdcdArrayCon = DatasetFactory.zeros(new int[] {1});
				
		    	
  		   	 	 for (int id = 0; id < sm.getFilepaths().length; id++) {
				    
		   	 		IDataset qdcdDat = (IDataset) models.get(id).getQdcdDat();
						
		   	 		qdcd[id] = (IDataset) qdcdDat;
					
		   	 		qdcdArrayCon = DatasetUtils.concatenate(qdcd, 0);
				 
				    qdcdArrayCon.sort(0);
				
		   	 	 }
			
			   	 writer.println("# Test file created: " + strDate);
				 writer.println("# Headers: ");
				 writer.println("#qdcd	I	Ie");
			
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
							sm.getSplicedCurveY().getError(gh));
				 }
			
			
		    }
		    
		    else{
		    	writer.println("#"+gms.get(0).getxName()+"	I	Ie");
				
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
							sm.getSplicedCurveY().getError(gh));
				 }
			
		    }
		    
		 	
	    }	
		writer.close();
	}	
	
	public void setTrackerOn(Boolean trackerOn){
		sm.setTrackerOn(trackerOn);
	}
	
	public Boolean getTrackerOn (){
		try{
			return sm.getTrackerOn();
		}
		catch(NullPointerException g){
			return false;
		}
	}
	
	public ArrayList<GeometricParametersModel> getGeometricParamtersModels(){
		return gms;
	}
	
	
	public void anarodSave(String title, String[] fr){

		try {
			File file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
	    if (sm.getCorrectionSelection() == MethodSetting.SXRD){
		    
		    IDataset[] hArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] kArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] lArray = new IDataset[sm.getFilepaths().length];
		    
		    for (int id = 0; id < sm.getFilepaths().length; id++) {
		    
		    	ILazyDataset h = SXRDGeometricCorrections.geth(models.get(id));
				ILazyDataset k = SXRDGeometricCorrections.getk(models.get(id));
				ILazyDataset l = SXRDGeometricCorrections.getl(models.get(id));
				
				hArray[id] = (IDataset) h;
				kArray[id] = (IDataset) k;
				lArray[id] = (IDataset) l;
				
		    }
		    
		    Dataset hArrayCon = DatasetUtils.concatenate(hArray, 0);
		    Dataset kArrayCon = DatasetUtils.concatenate(kArray, 0);
		    Dataset lArrayCon = DatasetUtils.concatenate(lArray, 0);	
				
		    hArrayCon.sort(0);
		    kArrayCon.sort(0);
		    lArrayCon.sort(0);
		    
			writer.println("# Test file created: " + strDate);
			writer.println("# Headers: ");
			writer.println("#h	k	l	F	Fe");
	
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(hArrayCon.getDouble(gh) +"	"+ kArrayCon.getDouble(gh) +"	"+lArrayCon.getDouble(gh) + 
							"	"+ sm.getSplicedCurveYFhkl().getDouble(gh)+ "	"+ sm.getSplicedCurveYFhkl().getError(gh));
			}
	    }
	    
	    else{
		    
			
		    if (models.get(0).getQdcdDat() != null){
		    	 
		    	IDataset[] qdcd = new IDataset[sm.getFilepaths().length];
			    Dataset qdcdArrayCon = DatasetFactory.zeros(new int[] {1});
				
		    	
  		   	 	 for (int id = 0; id < sm.getFilepaths().length; id++) {
				    
		   	 		IDataset qdcdDat = (IDataset) models.get(id).getQdcdDat();
						
		   	 		qdcd[id] = (IDataset) qdcdDat;
					
		   	 		qdcdArrayCon = DatasetUtils.concatenate(qdcd, 0);
				 
				    qdcdArrayCon.sort(0);
				
		   	 	 }
			
			   	 writer.println("# Test file created: " + strDate);
				 writer.println("# Headers: ");
				 writer.println("#qdcd	I	Ie");
			
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
						writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
					    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
					    sm.getSplicedCurveY().getError(gh));
				}
			
			
		    }
		    
		    else{
		    	writer.println("#"+gms.get(0).getxName()+"	I	Ie");
				
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
							sm.getSplicedCurveY().getError(gh));
				 }
			
		    }
	    	
	    }
	    
		writer.close();
	}	
	
	public void intSave(String title, String[] fr){
		
		File file =null;
		
		try {
			file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		int index  = title.lastIndexOf(".");
		
		if(index != -1){
			String ext = title.substring(0,index);
			file.renameTo(new File(ext + ".int"));
		}
		else{
			file.renameTo(new File(title + ".int"));
		}
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
	    if(sm.getCorrectionSelection() == MethodSetting.SXRD){
	    
		    IDataset[] hArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] kArray = new IDataset[sm.getFilepaths().length];
		    IDataset[] lArray = new IDataset[sm.getFilepaths().length];
		    
		    for (int id = 0; id < sm.getFilepaths().length; id++) {
		    
		    	ILazyDataset h = SXRDGeometricCorrections.geth(models.get(id));
				ILazyDataset k = SXRDGeometricCorrections.getk(models.get(id));
				ILazyDataset l = SXRDGeometricCorrections.getl(models.get(id));
				
				hArray[id] = (IDataset) h;
				kArray[id] = (IDataset) k;
				lArray[id] = (IDataset) l;
				
		    }
		    
		    Dataset hArrayCon = DatasetUtils.concatenate(hArray, 0);
		    Dataset kArrayCon = DatasetUtils.concatenate(kArray, 0);
		    Dataset lArrayCon = DatasetUtils.concatenate(lArray, 0);	
				
		    hArrayCon.sort(0);
		    kArrayCon.sort(0);
		    lArrayCon.sort(0);
		    
			writer.println("# Test file created: " + strDate);
			writer.println("# Headers: ");
			writer.println("#h	k	l	F	Fe	lorentz	correction 	polarisation correction		area correction");
	
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(hArrayCon.getDouble(gh) +"	"+ kArrayCon.getDouble(gh) +"	"+lArrayCon.getDouble(gh) + 
							"	"+ sm.getSplicedCurveYFhkl().getDouble(gh)+ "	"+ sm.getSplicedCurveYFhkl().getError(gh) +"	"
							+ sm.getLorentzCorrection().get(gh)+"	" + sm.getPolarisation().get(gh)+"	" + sm.getAreaCorrection().get(gh));
			}
	    }
	    else{
	    	
	    	 writer.println("# Test file created: " + strDate);
			 writer.println("# Headers: ");

		    if (models.get(0).getQdcdDat() != null){
		    	 
		    	IDataset[] qdcd = new IDataset[sm.getFilepaths().length];
			    Dataset qdcdArrayCon = DatasetFactory.zeros(new int[] {1});
				
		    	
  		   	 	 for (int id = 0; id < sm.getFilepaths().length; id++) {
				    
		   	 		IDataset qdcdDat = (IDataset) models.get(id).getQdcdDat();
						
		   	 		qdcd[id] = (IDataset) qdcdDat;
					
		   	 		qdcdArrayCon = DatasetUtils.concatenate(qdcd, 0);
				 
				    qdcdArrayCon.sort(0);
				
		   	 	 }
			
			   	
				 writer.println("#qdcd	I	Ie	Area Correction	Flux Correction");
				 
				 if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh)+ "	"+ 
						    sm.getReflectivityAreaCorrection().get(gh)+ "	"+
						    sm.getReflectivityFluxCorrection().get(gh));
					}
				 }
				 
				 if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh)+ "	"+ 
						    sm.getReflectivityAreaCorrection().get(gh));
					}
				 }
				 
				 if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh));
					 }
				 }
			
		    }
		    		    
		    else{
		    	writer.println("#"+gms.get(0).getxName()+"	I	Ie	Area Correction	Flux Correction");
				
		    	if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
						 	writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh)+ "	"+ 
						    sm.getReflectivityAreaCorrection().get(gh)+ "	"+
						    sm.getReflectivityFluxCorrection().get(gh));
					}
				 }
				 
				 if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
						 	writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh)+ "	"+ 
						    sm.getReflectivityAreaCorrection().get(gh));
					}
				 }
				 
				 if(sm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction){

					 for(int gh = 0 ; gh<sm.getImages().length; gh++){
						 	writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
						    sm.getSplicedCurveY().getError(gh));
					 }
			
		    }
	    	
		   }	    
	    }
		writer.close();
	}	
	
	public void writeOutReflectivityDat(){
		
		IDataset[] qdcd = new IDataset[sm.getFilepaths().length];
			
    	 for (int id = 0; id < sm.getFilepaths().length; id++) {
		    
    		IDataset qdcdDat = (IDataset) models.get(id).getQdcdDat();
					
		    qdcd[id] = (IDataset) qdcdDat;
				
		 }
		    
		 Dataset qdcdArrayCon = DatasetUtils.concatenate(qdcd, 0);
		 
		 qdcdArrayCon.sort(0);
		 
		 SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
		 Date now = new Date();
		 String strDate = sdfDate.format(now);
		    
		 writer.println("# Test file created: " + strDate);
		 writer.println("# Headers: ");
		 writer.println("#qdcd	I	Ie");
	
		 for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(qdcdArrayCon.getDouble(gh) +"	"+ 
				    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
				    sm.getSplicedCurveY().getError(gh));
		}	
	}
	
	public void simpleXYYeSave(String title, String[] fr, int state){
		
		File file =null;
		
		try {
			file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		int index  = title.lastIndexOf(".");
		if(index != -1){
			String ext = title.substring(0,index);
			file.renameTo(new File(ext + ".int"));
		}
		else{
			file.renameTo(new File(title + ".int"));
		}
			
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    
		writer.println("# Test file created: " + strDate);
		writer.println("# Headers: ");
		writer.println("#X	Y	Ye");
		
		if(state == 1){
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(sm.getSplicedCurveX().getDouble(gh) + 
							"	"+ sm.getSplicedCurveYFhkl().getDouble(gh)+ 
							"	"+ sm.getSplicedCurveYFhkl().getError(gh));
			}
		}
		
		if(state == 0){
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(sm.getSplicedCurveX().getDouble(gh) + 
							"	"+ sm.getSplicedCurveY().getDouble(gh)+ 
							"	"+ sm.getSplicedCurveY().getError(gh));
			}
		}

		if(state == 2){
			for(int gh = 0 ; gh<sm.getImages().length; gh++){
					writer.println(sm.getSplicedCurveX().getDouble(gh) + 
							"	"+ sm.getSplicedCurveYRaw().getDouble(gh)+ 
							"	"+ sm.getSplicedCurveYRaw().getError(gh));
			}
		}
		
		writer.close();
	}	
	
	
	
	
//	public void export(IPlottingSystem<Composite> parentPs, 
//						IDataset xData,
//						IDataset yData){
//		
//		sm.setSplicedCurveX(xData);
//		sm.setSplicedCurveY(yData);
//	
//		parentPs.clear();
//
//		ILineTrace lt1 = parentPs.createLineTrace("Adjusted Spliced Curve");
//		lt1.setData(xData, yData);
//		lt1.isErrorBarEnabled();
//		
//		parentPs.addTrace(lt1);
////		parentPs.repaint();
//		ssvs.getSsps3c().getOutputCurves().getIntensity().select(0);;
//	}

	
	public void setSplicedCurveX(IDataset xData){
		sm.setSplicedCurveX(xData);
	}
	
	public void setSplicedCurveY(IDataset yData){
		sm.setSplicedCurveY(yData);
	}
	
	
	public IROI getROI() {

		int jok = sm.getFilepathsSortedArray()[sm.getSliderPos()];
		ExampleModel model = models.get(jok);
		return model.getROI();
	}
	
	public int[][] getPermanentBoxOffsetLenPt() {
		return sm.getPermanentBoxOffsetLenPt();
	}

	public void setPermanentBoxOffsetLenPt(int[][] l) {
		sm.setPermanentBoxOffsetLenPt(l);
	}
	
	public int[][] getBoxOffsetLenPt() {
		return sm.getBoxOffsetLenPt();
	}

	public void setBoxOffsetLenPt(int[][] l) {
		sm.setBoxOffsetLenPt(l);
	}
	
	public int getSliderPos() {
		return sm.getSliderPos();
	}
	
	public void boundariesWarning(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,0, null);
		roobw.open();
	}
	
	public void outOfMemoryWarning(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,5, null);
		roobw.open();
	}
	
	public void numberFormatWarning(String note){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,1,note);
		roobw.open();
		
		
		roobw.getOverride().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return; 
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
	}
	
	public void boundariesWarning(String note, Display d){
//		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,0, note);
//		d.asyncExec(new Runnable() {
//			@Override
//			public void run() {	
//				roobw.open();
		return;
//			}
//		});
	}
	
	public void numberFormatWarning(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,1,null);
		roobw.open();
		
		roobw.getOverride().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return; 
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
		
		
		return;
	}
	
	public void correctionMethodsWarning(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,2,null);

		roobw.open();
		
		roobw.getOverride().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return; 
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
//		ssvs.getFolder().setSelection(0);
		
		return;
	}
	
	public void imagesUnavailableWarning(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,3,null);
		roobw.open();
		
		roobw.getOverride().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return; 
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
//		ssvs.getFolder().setSelection(0);
		
		return;
	}
	
	
	public void dialogToChangeImageName(){
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell,3,null);
		roobw.open();
		
		roobw.getOverride().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return; 
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		}); 
		
//		ssvs.getFolder().setSelection(0);
		
		return;
	}
	
	public void dialogToChangeImageFolder(Boolean t, DatDisplayer dd){
		
		ImageFolderChangeDialog ifcd = new ImageFolderChangeDialog(parentShell,
																   this, 
																   t, 
																   dd);
		ifcd.open();
		
		
		
		return;
	}
	
	
	
	public IDataset[] curveStitchingOutput (IPlottingSystem<Composite> plotSystem, 
									   ArrayList<IDataset> xArrayList,
									   ArrayList<IDataset> yArrayList,
									   ArrayList<IDataset> yArrayListError,
									   ArrayList<IDataset> yArrayListFhkl,
									   ArrayList<IDataset> yArrayListFhklError,
									   ArrayList<IDataset> yArrayListRaw,
									   ArrayList<IDataset> yArrayListRawError,
									   OverlapUIModel model ){
		
		IDataset[] attenuatedDatasets = 
				StitchedOutputWithErrors.curveStitch(plotSystem, 
												     xArrayList,
												     yArrayList,
												     yArrayListError,
												     yArrayListFhkl,
												     yArrayListFhklError, 
												     yArrayListRaw,
												     yArrayListRawError, 
												     dms,
												     sm,
												     model);
		
		return attenuatedDatasets;
	}
	
	
	public IDataset[] curveStitchingOutput (){

		IDataset[] attenuatedDatasets = 
						StitchedOutputWithErrors.curveStitch4(dms,
															 sm);
	
		return attenuatedDatasets;
	}

	public void switchFhklIntensity(IPlottingSystem<Composite> pS, Combo selector){
		
		pS.clear();
		
		ILineTrace lt = 
				pS.createLineTrace("Corrected Intensity Curve");
		
		Display display = Display.getCurrent();
		
		if(selector.getSelectionIndex() ==0){
					
			lt.setData(sm.getSortedX(),sm.getSplicedCurveY());
		
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			
			lt.setTraceColor(blue);
		}

		if(selector.getSelectionIndex() ==1){
			
			lt.setName("Fhkl Curve");
			
			lt.setData(sm.getSortedX(),sm.getSplicedCurveYFhkl());
			
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
		
			lt.setTraceColor(green);
		}
		
		if(selector.getSelectionIndex() ==2){
			
			lt.setName("Raw Intensity Curve");
			
			lt.setData(sm.getSortedX(),sm.getSplicedCurveYRaw());
			
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
		
			lt.setTraceColor(black);
		}
		
		lt.setErrorBarEnabled(sm.isErrorDisplayFlag());
		
		Color red = display.getSystemColor(SWT.COLOR_RED);
		
		lt.setErrorBarColor(red);
	
		pS.addTrace(lt);
		pS.autoscaleAxes();
		
		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0]-1);
		double range = end - start;
				
		pS.getAxes().get(0).setRange((start - 0.1*range), (end) + 0.1*range);
		
//		pS.repaint();	
		
	}
	
	public void setCorrectionSelection(int correctionSelection){
		sm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
	}
	
	public void setSelection (int selection){
		sm.setSelection(selection);
	}
	
	public void setSliderPos (int selection){
		sm.setSliderPos(selection);
	}
	
	public int[][] getBackgroundLenPt(){
		return sm.getBackgroundLenPt();
	}
	
	public void setBackgroundLenPt(int[][] l){
		sm.setBackgroundLenPt(l);
	}
	
	public int[][] getInitialLenPt(){
		return sm.getInitialLenPt();
	}
		
//	public void addSecondBgRegionListeners(IRegion r2){
//		
////		IRegion r2 = ssvs.getPlotSystemCompositeView().getSecondBgRegion();
////		
//		r2.addROIListener(new IROIListener() {
//			
//			@Override
//			public void roiDragged(ROIEvent evt) {
//				roiStandard(evt);
//			}
//	
//			@Override
//			public void roiChanged(ROIEvent evt) {
//				roiStandard(evt);
//			}
//	
//			@Override
//			public void roiSelected(ROIEvent evt) {
//				roiStandard(evt);
//			}
//			
//			public void roiStandard(ROIEvent evt) {
//				
//				int[] len = sm.getInitialLenPt()[0]; 
//				int[] pt = sm.getInitialLenPt()[1];
//				int[][] lenpt = {len, pt};
//				
//				IRectangularROI bounds = r2.getROI().getBounds();
//				int[] redLen = bounds.getIntLengths();
//				int[] redPt = bounds.getIntPoint();
//				int[][] redLenPt = {redLen, redPt};
//				
//				sm.setBackgroundLenPt(redLenPt);
//				
//				if (models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){
//					
//					int [][] newOffsetLenPt = new int[2][2];
//					
//					newOffsetLenPt[0][0]  =  -len[0] + redLen[0];
//					newOffsetLenPt[0][1]  =  -len[1] + redLen[1];
//					
//					
//					newOffsetLenPt[1][0]  = -pt[0] + redPt[0];
//					newOffsetLenPt[1][1]  = -pt[1] + redPt[1];
//					
//					 
//					sm.setBoxOffsetLenPt(newOffsetLenPt);
//				}
//				
//				regionOfInterestSetter();
//				ssvs.getSsps3c().generalUpdate();
//				
//			}
//		});				
//	}
//	
	
	
	public void runReplay(IPlottingSystem<Composite> pS,
						  TabFolder folder,
						  IPlottingSystem<Composite> subIBgPS){
		
		MovieJob mJ = new MovieJob();
		mJ.setSuperModel(sm);
		mJ.setPS(pS);
		mJ.setTime(220);
		mJ.setSsp(this);
		mJ.setSsvs(ssvs);
		mJ.setSliders(ssvs.getSliderList());
		mJ.setFolder(folder);
		mJ.setSubIBgPS(subIBgPS);
		mJ.run();	
		
	}
	
	public void setStartFrame(int f){
		sm.setStartFrame(f);
	}
	
	public int getStartFrame(){
		return sm.getStartFrame();
	}
	
	
	public void resetDataModels(){
		for(DataModel dm:dms){
			dm.resetAll();
		}
	}
	
	public void resetTrackers(){
		sm.resetTrackers();
	}
	
	public IDataset returnNullImage(){
		
		IDataset output = this.getImage(0);
		
		return output;
		
	}
	
	public void resetSmOutputObjects(){
		sm.resetAll();
	}
	
	public double[] trackerInterpolationInterpolator1(int k){
		
			
			
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
			
			
			debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{crude  seedlocation for Tracker[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
			
			
		
		return seedLocation;
		}

	
	public IDataset returnSubNullImage(){
		
		RectangularROI startROI = new RectangularROI(100,100,50,50,0);
		IROI box = startROI.getBounds().bounds(startROI);
		IDataset subImage = PlotSystem2DataSetter.PlotSystem2DataSetter1(box, this.returnNullImage());
		
		return subImage;
	}

	
	public SuperModel getSm(){
		return sm;
	}

	public void runTrackingJob(IPlottingSystem<Composite> subPS, 
							   IPlottingSystem<Composite> outputCurves,
							   IPlottingSystem<Composite> pS,
							   TabFolder folder,
							   IPlottingSystem<Composite> subIBgPS,
							   ProgressBar progress,
							   TrackingProgressAndAbortView tpaav) {

//		sm.resetAll();
//		sm.setLocationList(null);
//		
//		for(DataModel md: dms){
//			md.resetAll();
//		}
//		this.backgroundBoxesManager();
//	
//		sm.setStartFrame(sm.getSliderPos());
		
		trackingJob tj = new trackingJob();
		debug("tj invoked");
		tj.setProgress(progress);
		tj.setSsvs(ssvs);
		tj.setCorrectionSelection(MethodSetting.toInt(sm.getCorrectionSelection()));
		tj.setSuperModel(sm);
		tj.setGms(gms);
		tj.setDms(dms);
		tj.setSsvsPS(ssvs.getPlotSystemCompositeView().getPlotSystem());
		tj.setModels(models);
		tj.setPlotSystem(subPS);
		tj.setOutputCurves(outputCurves);
		tj.setTimeStep(Math.round((2 / noImages)));
		tj.setSsp(this);
		tj.setTPAAV(tpaav);
		tj.runTJ1();
		

		
		return;
		
	}

	
	public void stitchAndPresent(MultipleOutputCurvesTableView outputCurves) {

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
		
		IDataset[] output = StitchedOutputWithErrors.curveStitch4(dms, sm);

		ILineTrace lt = pS.createLineTrace("progress");

		lt.setData(sm.getSplicedCurveX(), sm.getSplicedCurveY());
	
		pS.clear();
		pS.addTrace(lt);
		
		pS.repaint();
		pS.autoscaleAxes();
		
		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0]-1);
		double range = end - start;
				
		pS.getAxes().get(0).setRange((start - 0.1*range), (end) + 0.1*range);
		
		lt.setErrorBarEnabled(false);
		
	}
	
	public void switchErrorDisplay(){
		if (sm.isErrorDisplayFlag() ==true){
			sm.setErrorDisplayFlag(false);
		}
		else{
			sm.setErrorDisplayFlag(true);
		}
	}
	

	
	public void geometricParametersWindowPopulate(){
		
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

//class trackingJob {
//extends job
//	private ArrayList<DataModel> dms;
//	private ArrayList<ExampleModel> models;
//	private IPlottingSystem<Composite> plotSystem;
//	private IPlottingSystem<Composite> outputCurves;
//	private ArrayList<GeometricParametersModel> gms;
//	private SuperModel sm;
//	private SurfaceScatterViewStart ssvs;
//	private int correctionSelection;
//	private int noImages;
//	private int timeStep;
//	private int imageNumber;
//	private SurfaceScatterPresenter ssp;
//	private IPlottingSystem<Composite> ssvsPS;
//	private int DEBUG = 0;
//	private ProgressBar progressBar;
//	private TrackingProgressAndAbortView tpaav;
//
//	public TrackingProgressAndAbortView getTPAAV() {
//		return tpaav;
//	}
//
//	public void setTPAAV(TrackingProgressAndAbortView tpaav) {
//		this.tpaav = tpaav;
//	}
//
//	public SurfaceScatterViewStart getSsvs() {
//		return ssvs;
//	}
//	
//	public void setProgress(ProgressBar progress){
//		this.progressBar = progress;
//	}
//
//	public void setSsvs(SurfaceScatterViewStart ssvs) {
//		this.ssvs = ssvs;
//	}
//	
//	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
//		this.outputCurves = outputCurves;
//	}
//
//	public void setCorrectionSelection(int cS) {
//		this.correctionSelection = cS;
//	}
//
//	public void setDms(ArrayList<DataModel> dms) {
//		this.dms = dms;
//	}
//
//	public void setModels(ArrayList<ExampleModel> models) {
//		this.models = models;
//	}
//
//	public void setSuperModel(SuperModel sm) {
//		this.sm = sm;
//	}
//
//	public void setGms(ArrayList<GeometricParametersModel> gms) {
//		this.gms = gms;
//	}
//
//	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
//		this.plotSystem = plotSystem;
//	}
//
//	public void setTimeStep(int timeStep) {
//		this.timeStep = timeStep;
//	}
//
//	public void setSsp(SurfaceScatterPresenter ssp) {
//		this.ssp = ssp;
//	}
//
//	public void setSsvsPS(IPlottingSystem<Composite> ssvsPS) {
//		this.ssvsPS = ssvsPS;
//	}
//	
//	public void setps(IPlottingSystem<Composite> plotSystem) {
//		this.plotSystem = plotSystem;
//	}
//
//	@SuppressWarnings("unchecked")
//	
//	protected  void runTJ1(){
//
//		
//		for(ExampleModel em: models){
//			em.setInput(null);
//		}
//		
//		for(DataModel dm :dms){
//			dm.resetAll();
//		}
//		
//		sm.setLocationList(null);
//		final Display display = Display.getCurrent();
//		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
//		
//		ssp.regionOfInterestSetter();
//		
//		if (models.get(sm.getSelection()).getMethodology() != AnalaysisMethodologies.Methodology.TWOD_TRACKING &&
//				sm.getTrackerOn() != true) {
//
//			noImages = sm.getImages().length;
//				
//			for (DataModel dm : dms) {
//				dm.resetAll();
//			}
//			outputCurves.clear();
//
//			int k = 0;
//
//			if (sm.getStartFrame() == 0) {
//												
//					Thread t  = new Thread(){
//						@Override
//						public void run(){
//									
//								for (int k = 0; k < noImages; k++) {
//										
//									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
//							
//									int trackingMarker = 0;
//									int imageNumber =k;
//									IDataset j = ssp.getImage(imageNumber);
//									int jok = sm.getFilepathsSortedArray()[imageNumber];
//									DataModel dm = dms.get(jok);
//									GeometricParametersModel gm = gms.get(jok);
//									ExampleModel model = models.get(jok);
//
//									dm.addxList(sm.getSortedX().getDouble(imageNumber));
//									
//									sm.addxList(sm.getImages().length, k,
//											sm.getSortedX().getDouble(k));
//									
//									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//									
//									IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
//																						 j,
//																						 model, 
//																						 dm, 
//																						 gm, 
//																						 plotSystem,
////																						 ssvsPS,
//																						 correctionSelection, 
//																						 imagePosInOriginalDat[imageNumber], 
//																						 trackingMarker, 
//																						 imageNumber);
//									
//									if(Arrays.equals(output1.getShape(),(new int[] {2,2}))){
//										ssp.boundariesWarning();
//
//									}
//								
//									sm.addBackgroundDatArray(sm.getImages().length, imageNumber, output1);
//									IDataset tempImage = ssp.getImage(imageNumber);
//									double[] tempLoc = sm.getLocationList().get(imageNumber);
//									double[] tl = tempLoc;
//									int[] sml =  sm.getInitialLenPt()[0];
//									sm.setSliderPos(imageNumber);
//									RectangularROI newROI = new RectangularROI(tempLoc[0],
//																		       tempLoc[1],
//																		       sm.getInitialLenPt()[0][0],
//																		       sm.getInitialLenPt()[0][1],0);
//									
//						display.syncExec(new Runnable() {
//							@Override
//							public void run() {	
//								updateTrackingDisplay(tempImage, imageNumber);
//								return;
//								}
//							});
//								
//						}
//								//////bottom of k++ loop
//								return;
//					};
//
//				}; ////Starts  the thread
//					
//					t.start();			
//			}
//			
//		
//
//			else if (sm.getStartFrame() != 0) {
//
//				//////////////////////// inside second loop
//				//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////
//
//				Thread t  = new Thread(){
//					
//					public void run(){
//				
//						int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
//						
//						for (int k = (sm.getStartFrame()); k >= 0; k--) {
//	
////						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k));
//						
//						int trackingMarker = 1;
//						int imageNumber =k;
//						IDataset j = ssp.getImage(k);
//						int jok = sm.getFilepathsSortedArray()[k];
//						DataModel dm = dms.get(jok);
//						GeometricParametersModel gm = gms.get(jok);
//						ExampleModel model = models.get(jok);
//	
//						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//								sm.getSortedX().getDouble(k));
//						
//						sm.addxList(sm.getImages().length, k,
//								sm.getSortedX().getDouble(k));
//						
//						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//						
//						IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
//																			 j, 
//																			 model, 
//																			 dm, 
//																			 gm, 
//																			 plotSystem,
////																			 ssvsPS,
//																			 correctionSelection, 
//																			 imagePosInOriginalDat[k], 
//																			 trackingMarker, 
//																			 k);
//	
//						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//							debug("Dummy Proccessing failure");
//							ssp.boundariesWarning();
//							break;
//						}
//						
//						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//						
//						IDataset tempImage = ssp.getImage(imageNumber);
//						double[] tempLoc = sm.getLocationList().get(imageNumber);
//						RectangularROI newROI = new RectangularROI(tempLoc[0],
//							       tempLoc[1],
//							       sm.getInitialLenPt()[0][0],
//							       sm.getInitialLenPt()[0][1],0);						
//						
//						display.syncExec(new Runnable() {
//							@Override
//							public void run() {	
//									
//								updateTrackingDisplay(tempImage, imageNumber);
//								return;
//								}
//							});		
//						
//					}
//					
//					
//					if(sm.getStartFrame() != noImages-1){	
//						for (int k = sm.getStartFrame()+1; k < noImages; k++) {
//							
//							
//							int trackingMarker = 2;
//		
//							IDataset j = ssp.getImage(k);
//							int jok = sm.getFilepathsSortedArray()[k];
//							DataModel dm = dms.get(jok);
//							GeometricParametersModel gm = gms.get(jok);
//							ExampleModel model = models.get(jok);
//		
//							dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//									sm.getSortedX().getDouble(k));
//							
//							sm.addxList(sm.getImages().length, k,
//									sm.getSortedX().getDouble(k));
//							
//							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//							
//							IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
//																				 j, 
//																				 model, 
//																				 dm, 
//																				 gm, 
//																				 plotSystem,
////																				 ssvsPS,
//																				 correctionSelection, 
//																				 imagePosInOriginalDat[k], 
//																				 trackingMarker, 
//																				 k);
//		
//							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//	//							ssp.boundariesWarning();
//								debug("Dummy Proccessing failure");
//								break;
//							}
//							
//							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//							
//							int imageNumber =k;
//							IDataset tempImage = ssp.getImage(imageNumber);
//							double[] tempLoc = sm.getLocationList().get(imageNumber);
//							RectangularROI newROI = new RectangularROI(tempLoc[0],
//								       tempLoc[1],
//								       sm.getInitialLenPt()[0][0],
//								       sm.getInitialLenPt()[0][1],0);
//	
//							
//							display.syncExec(new Runnable() {
//								@Override
//								public void run() {	
//										
//									updateTrackingDisplay(tempImage, imageNumber);
//									
//									return;
//									}
//								});
//							}
//						}
//					//////bottom of k++ loop
//					return;
//					}
//					
//				};
//			t.start();
//			}	
//			
//		}
//		else {
//
//			trackingJob2 tj = new trackingJob2();
//			debug("tj2 invoked");
//			tj.setProgress(progressBar);
//			tj.setCorrectionSelection(MethodSetting.toInt(sm.getCorrectionSelection()));
//			tj.setSuperModel(sm);
//			tj.setGms(gms);
//			tj.setDms(dms);
//			tj.setModels(models);
//			tj.setPlotSystem(plotSystem);
//			tj.setOutputCurves(outputCurves);
//			tj.setTimeStep(Math.round(2 / sm.getImages().length));
//			tj.setSsp(ssp);
//			tj.setSsvs(ssvs);
//			tj.setTPAAV(tpaav);
//			tj.runTJ2();
//		}
//		
//		
//		ssvs.getCustomComposite().getReplay().setEnabled(true);
////		ssvs.getOutputCurves().addImageNoRegion(ssp.getXValue((ssp.getNumberOfImages())/2));
//	}
//		private void debug(String output) {
//		if (DEBUG == 1) {
//			System.out.println(output);
//		}
//	}
//		
//		
//		public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
//			
//			
//			ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
//			ssp.updateSliders(ssvs.getSliderList(), imageNumber);
//			ssvs.updateIndicators(imageNumber);
//			ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage, null, null);
//			ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(sm.getBackgroundDatArray().get(imageNumber), null, null);
//			ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
//			ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
//			ssvs.getSsps3c().generalUpdate();
//			ssp.stitchAndPresent(ssvs.getSsps3c().getOutputCurves());
//			ssp.trackingRegionOfInterestSetter(sm.getLocationList().get(imageNumber), imageNumber);
//			ssvs.getSsps3c().getOutputCurves().getIntensity().select(0);
//			ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();
//			if(progressBar.isDisposed() != true){
//				progressBar.setSelection(progressBar.getSelection() +1);
//	//			System.out.println(progressBar.getSelection());
//				
//				if(progressBar.getSelection() == progressBar.getMaximum()){
//	//				System.out.println(progressBar.getSelection() +" in the break");
//					tpaav.close();
//				}
//			}
//		}
//}
//
///////////////////////////////////////////////////////////////////////
/////////////////////// Tracking Job2////////////////////////////////////
///////////////////////////////////////////////////////////////////////
//
//class trackingJob2 {
//
//	private ArrayList<DataModel> dms;
//	private ArrayList<ExampleModel> models;
//	private IPlottingSystem<Composite> plotSystem;
//	private IPlottingSystem<Composite> outputCurves;
//	private ArrayList<GeometricParametersModel> gms;
//	private SuperModel sm;
//	private int correctionSelection;
//	private int noImages;
//	private int timeStep;
//	private SurfaceScatterPresenter ssp;
//	private IPlottingSystem<Composite> ssvsPS; 
//	private SurfaceScatterViewStart ssvs;
//	private int DEBUG = 0;
//	private ProgressBar progressBar;
//	private TrackingProgressAndAbortView tpaav;
//
//	public TrackingProgressAndAbortView getTPAAV() {
//		return tpaav;
//	}
//
//	public void setTPAAV(TrackingProgressAndAbortView tpaav) {
//		this.tpaav = tpaav;
//	}
//
//	public void setProgress(ProgressBar progress){
//		this.progressBar = progress;
//	}
//	
//	public SurfaceScatterViewStart getSsvs() {
//		return ssvs;
//	}
//
//	public void setSsvs(SurfaceScatterViewStart ssvs) {
//		this.ssvs = ssvs;
//	}
//
//	public void setOutputCurves(IPlottingSystem<Composite> outputCurves) {
//		this.outputCurves = outputCurves;
//	}
//
//	public void setCorrectionSelection(int cS) {
//		this.correctionSelection = cS;
//	}
//
//	public void setDms(ArrayList<DataModel> dms) {
//		this.dms = dms;
//	}
//
//	public void setModels(ArrayList<ExampleModel> models) {
//		this.models = models;
//	}
//
//	public void setSuperModel(SuperModel sm) {
//		this.sm = sm;
//	}
//
//	public void setGms(ArrayList<GeometricParametersModel> gms) {
//		this.gms = gms;
//	}
//
//	public void setPlotSystem(IPlottingSystem<Composite> plotSystem) {
//		this.plotSystem = plotSystem;
//	}
//
//	public void setTimeStep(int timeStep) {
//		this.timeStep = timeStep;
//	}
//
//	public void setSsp(SurfaceScatterPresenter ssp) {
//		this.ssp = ssp;
//	}
//	
//	public void setSsvsPS (IPlottingSystem<Composite> ssvsPS) {
//		this.ssvsPS = ssvsPS;
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	protected void runTJ2() {
//
//		final Display display = Display.getCurrent();
//	
//		debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the new tracker~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");
//		sm.resetTrackers();
//		sm.resetAll();
//		
//		for(ExampleModel m : models){
//			m.setInput(null);
//		}
//		
//		noImages = sm.getImages().length;
//
//		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
//
//		for (DataModel dm : dms) {
//			dm.resetAll();
//		}
//
//		ssp.regionOfInterestSetter();
//		outputCurves.clear();
//
//		int jok = sm.getFilepathsSortedArray()[sm.getStartFrame()];
//
//		String[] doneArray = new String[sm.getFilepaths().length];
//		
//		if (sm.getStartFrame() == 0) {
//			
//			
//			Thread t  = new Thread(){
//				
//				@Override
//				public void run(){
//			
//					for (int k = 0; k < noImages; k++) {
//						if (sm.getFilepathsSortedArray()[k] == jok) {
//							
//							debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//							+ " , " + "local jok:  " + Integer.toString(jok));
//							
//							debug("@@@@@@@@@@@~~~~~~~~~~~~~~~in the 0 loop~~~~~~~~~~~~~~~~~~@@@@@@@@@@@@@@");
//
//							int jok = sm.getFilepathsSortedArray()[k];
//							int trackingMarker = 0;
//							IDataset j = ssp.getImage(k);
//							DataModel dm = dms.get(jok);
//							GeometricParametersModel gm = gms.get(jok);
//							ExampleModel model = models.get(jok);
//		
//							dm.addxList(sm.getSortedX().getDouble(k));
//							
//							sm.addxList(sm.getImages().length, k,
//									sm.getSortedX().getDouble(k));
//							
//							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//							
//							debug("Tracker should fire once");
//							
//							IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
//																				 j, 
//																				 model, 
//																				 dm, 
//																				 gm, 
//																				 plotSystem,
////																				 ssvsPS,
//																				 correctionSelection, 
//																				 imagePosInOriginalDat[k], 
//																				 trackingMarker, 
//																				 k);
//							debug("Tracker should HAVE fired once");
//							
//							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//								debug("Dummy Proccessing failure");
//								Display d =Display.getCurrent();
//								ssp.boundariesWarning("position 1, line ~1410, k: " + Integer.toString(k),d);
//								break;
//							}
//							
//							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//							
//							int imageNumber =k;
//							IDataset tempImage = ssp.getImage(imageNumber);
//							double[] tempLoc = sm.getLocationList().get(imageNumber);
//							RectangularROI newROI = new RectangularROI(tempLoc[0],
//								       tempLoc[1],
//								       sm.getInitialLenPt()[0][0],
//								       sm.getInitialLenPt()[0][1],0);
//							
//							
//							
//							display.syncExec(new Runnable() {
//								@Override
//								public void run() {																			
//									updateTrackingDisplay(tempImage, imageNumber);
//									
//									return;
//									}
//								});
//							
//							
//							
//							
//						}
//					}
//					//////bottom of k++ loop
//					doneArray[jok] = "done";
//					
//					while (ClosestNoFinder.full(doneArray, "done") == false) {
//
//						debug("in the while loop");
//
//						int nextk = ClosestNoFinder.closestNoWithoutDone(sm.getSortedX().getDouble(sm.getSliderPos()),
//								sm.getSortedX(), doneArray, sm.getFilepathsSortedArray());
//
//						int nextjok = sm.getFilepathsSortedArray()[nextk];
//
//						debug("nextk :" + nextk);
//						debug("nextjok :" + nextjok);
//						debug("doneArray[nextjok]: " + doneArray[nextjok]);
//						
//						if (imagePosInOriginalDat[nextk] == 0) {
//				
//							debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
//							
//							for (int k = nextk; k < noImages; k++) {
//
//								if (sm.getFilepathsSortedArray()[k] == nextjok) {
////									ssp.sliderMovemementMainImage(k, ssp.getSsvs().getPlotSystemCompositeView().getPlotSystem());
//
//
//									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//									
//									
//									int trackingMarker = 0;
//									IDataset j = ssp.getImage(k);
//									int jokLocal = sm.getFilepathsSortedArray()[k];
//									DataModel dm = dms.get(jokLocal);
//									GeometricParametersModel gm = gms.get(jokLocal);
//									ExampleModel model = models.get(jokLocal);
//									
//									
//									if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//										
//										if (sm.getTrackerLocationList() == null | sm.getTrackerLocationList().size() <= 10 ){
//											int seedIndex = 
//													ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																						  sm.getSortedX(), 
//																						  sm.getLocationList());
//					
//											int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//											
//											
//											ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//											ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//											
//											Dataset yValues = DatasetFactory.zeros(seedList.size());
//											Dataset xValues = DatasetFactory.zeros(seedList.size());
//											Dataset lValues = DatasetFactory.zeros(seedList.size());
//											
//											for(int op = 0; op<seedList.size(); op++){
//												
//												double x = seedList.get(op)[1];
//												double y = seedList.get(op)[0];
//												double l = lList.get(op);
//												
//												xValues.set(x, op);
//												yValues.set(y, op);
//												lValues.set(l, op);
//						
//											}
//											
//											
//											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																										   lValues, 
//																										   xValues, 
//																										   yValues, 
//																										   sm.getInitialLenPt()[0],
//																										   1);
//											dm.setSeedLocation(seedLocation);
//											
////											debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//									
//										}
//										else{
//											double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(sm.getTrackerLocationList(), 
//																										   sm.getSortedX(), 
//																										   sm.getInitialLenPt()[0],
//																										   k);
//											dm.setSeedLocation(seedLocation);
//										}
//									}	
//									
//									
//									else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//										
//										int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//										int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//										
//										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//												(double) (pt[1] + len[1]) };
//										
//										dm.setSeedLocation(seedLocation);
//									}
//									
//									dm.addxList(sm.getSortedX().getDouble(k));
//									
//									sm.addxList(sm.getImages().length, k,
//											sm.getSortedX().getDouble(k));
//									
//									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//									
//									IDataset output1 = 
//											DummyProcessingClass.DummyProcess1(sm, 
//																			   j, 
//																			   model, 
//																			   dm, 
//																			   gm, 
//																			   plotSystem,
//																			   ssvsPS,
//																			   correctionSelection, 
//																			   imagePosInOriginalDat[k], 
//																			   trackingMarker, 
//																			   k,
//																			   dm.getSeedLocation());
//
//									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//										debug("Dummy Proccessing failure");
//										Display d =Display.getCurrent();
//										ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
//										
//										break;
//									}
//									
//									sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//									
//									
//									int imageNumber =k;
//									IDataset tempImage = ssp.getImage(imageNumber);
//									double[] tempLoc = sm.getLocationList().get(imageNumber);
//									RectangularROI newROI = new RectangularROI(tempLoc[0],
//										       tempLoc[1],
//										       sm.getInitialLenPt()[0][0],
//										       sm.getInitialLenPt()[0][1],0);
//									
//									display.syncExec(new Runnable() {
//										@Override
//										public void run() {	
//									
//											updateTrackingDisplay(tempImage, imageNumber);
//											
//											return;
//										}
//										});
//									
//
//								}
//								doneArray[nextjok] = "done";
//							}
//							//////bottom of k++ loop
//						}
//
//						else if (imagePosInOriginalDat[nextk] != 0) {
//
//							for (int k = (sm.getStartFrame()); k >= 0; k--) {
//
//								if (sm.getFilepathsSortedArray()[k] == nextjok) {
//
//									debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//									+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//									
//									
//									int trackingMarker = 1;
//									IDataset j = ssp.getImage(k);
//									DataModel dm = dms.get(nextjok);
//									GeometricParametersModel gm = gms.get(nextjok);
//									ExampleModel model = models.get(nextjok);
//									
//									if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//										
//										int seedIndex = 
//												ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																					  sm.getSortedX(), 
//																					  sm.getLocationList());
//				
//										int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//										
//										
//										ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//										ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//										
//										Dataset yValues = DatasetFactory.zeros(seedList.size());
//										Dataset xValues = DatasetFactory.zeros(seedList.size());
//										Dataset lValues = DatasetFactory.zeros(seedList.size());
//										
//										for(int op = 0; op<seedList.size(); op++){
//											
//											double x = seedList.get(op)[1];
//											double y = seedList.get(op)[0];
//											double l = lList.get(op);
//											
//											xValues.set(x, op);
//											yValues.set(y, op);
//											lValues.set(l, op);
//					
//										}
//										
//										double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																									   lValues, 
//																									   xValues, 
//																									   yValues, 
//																									   sm.getInitialLenPt()[0],
//																									   1);
//										dm.setSeedLocation(seedLocation);
//										
////										debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//										
//									
//									}
//									
//									else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//										
//										int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//										int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//										
//										double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//												(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//												(double) (pt[1] + len[1]) };
//										
//										dm.setSeedLocation(seedLocation);
//									}
//									
//									dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//											sm.getSortedX().getDouble(k));
//									
//									
//									sm.addxList(sm.getImages().length, k,
//											sm.getSortedX().getDouble(k));
//									
//									debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//									
//									IDataset output1 = 
//											DummyProcessingClass.DummyProcess1(sm, 
//																			   j, 
//																			   model, 
//																			   dm, 
//																			   gm, 
//																			   plotSystem,
//																			   ssvsPS,
//																			   correctionSelection, 
//																			   imagePosInOriginalDat[k], 
//																			   trackingMarker, 
//																			   k,
//																			   dm.getSeedLocation());
//
//									
//									if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//										debug("Dummy Proccessing failure");
//										Display d =Display.getCurrent();
//										ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
//										
//										break;
//									}
//									
//									
//									sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//									
//									
//									int imageNumber =k;
//									IDataset tempImage = ssp.getImage(imageNumber);
//									double[] tempLoc = sm.getLocationList().get(imageNumber);
//									RectangularROI newROI = new RectangularROI(tempLoc[0],
//										       tempLoc[1],
//										       sm.getInitialLenPt()[0][0],
//										       sm.getInitialLenPt()[0][1],0);
//									
//									display.syncExec(new Runnable() {
//										@Override
//										public void run() {	
//										
//											updateTrackingDisplay(tempImage, imageNumber);
//											
//											return;
//										}
//										});
//								}
//							}
//
//							
//							models.get(nextjok).setInput(null);
//							
//							if(sm.getStartFrame() != noImages-1){
//								for (int k = sm.getStartFrame()+1; k < noImages; k++) {
//	
//									if (sm.getFilepathsSortedArray()[k] == nextjok) {
//	
//										debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//										+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//										
//										
//										int trackingMarker = 2;
//										IDataset j = ssp.getImage(k);
//										DataModel dm = dms.get(nextjok);
//										GeometricParametersModel gm = gms.get(nextjok);
//										ExampleModel model = models.get(nextjok);
//	
//										
//										if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//											
//											int seedIndex = 
//													ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																						  sm.getSortedX(), 
//																						  sm.getLocationList());
//					
//											int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//											
//											
//											ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//											ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//											
//											Dataset yValues = DatasetFactory.zeros(seedList.size());
//											Dataset xValues = DatasetFactory.zeros(seedList.size());
//											Dataset lValues = DatasetFactory.zeros(seedList.size());
//											
//											for(int op = 0; op<seedList.size(); op++){
//												
//												double x = seedList.get(op)[1];
//												double y = seedList.get(op)[0];
//												double l = lList.get(op);
//												
//												xValues.set(x, op);
//												yValues.set(y, op);
//												lValues.set(l, op);
//						
//											}
//											
//											double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																										   lValues, 
//																										   xValues, 
//																										   yValues, 
//																										   sm.getInitialLenPt()[0],
//																										   1);
//											dm.setSeedLocation(seedLocation);
//										
//										}	
//										
//										else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//											
//											int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//											int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//											
//											double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//													(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//													(double) (pt[1] + len[1]) };
//											
//											dm.setSeedLocation(seedLocation);
//										}
//										
//										
//										dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//												sm.getSortedX().getDouble(k));
//										
//										sm.addxList(sm.getImages().length, k,
//												sm.getSortedX().getDouble(k));
//										
//										debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//										
//										IDataset output1 = 
//												DummyProcessingClass.DummyProcess1(sm, 
//																				   j, 
//																				   model, 
//																				   dm, 
//																				   gm, 
//																				   plotSystem,
//																				   ssvsPS,
//																				   correctionSelection, 
//																				   imagePosInOriginalDat[k], 
//																				   trackingMarker, 
//																				   k,
//																				   dm.getSeedLocation());
//	
//										if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//											debug("Dummy Proccessing failure");
//											Display d =Display.getCurrent();
//											ssp.boundariesWarning("position 1, line ~2369, k: " + Integer.toString(k),d);
//											
//											break;
//										}
//										
//										
//										sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//										
//										int imageNumber =k;
//										IDataset tempImage = ssp.getImage(imageNumber);
//										double[] tempLoc = sm.getLocationList().get(imageNumber);
//										RectangularROI newROI = new RectangularROI(tempLoc[0],
//											       tempLoc[1],
//											       sm.getInitialLenPt()[0][0],
//											       sm.getInitialLenPt()[0][1],0);
//										
//										display.syncExec(new Runnable() {
//											@Override
//											public void run() {	
//													
//												updateTrackingDisplay(tempImage, imageNumber);
//												
//												return;
//											}
//											});
//	
//									}
//								}
//							}
//							//////bottom of k++ loop
//							doneArray[nextjok] = "done";
//						}
//					}
//					
//					return;
//				}
//			};
//			t.start();
//		}
//
//		//////////////////////// inside second loop
//		//////////////////////// scenario@@@@@@@@@@@@@@@@@@@@@@@@@@@@///////////
//
//		else {
//
//			Thread t  = new Thread(){
//				
//				@Override
//				public void run(){
//			
//			
//		
//			for (int k = (sm.getStartFrame()); k >= 0; k--) {
//
//				if (sm.getFilepathsSortedArray()[k] == jok) {
//					
//					debug("switched to k--");
//					debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//					debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//					+ " , " + "local jok:  " + Integer.toString(jok));
//					
//					int trackingMarker = 1;
//					IDataset j = ssp.getImage(k);
//					DataModel dm = dms.get(jok);
//					GeometricParametersModel gm = gms.get(jok);
//					ExampleModel model = models.get(jok);
//
//					dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//							sm.getSortedX().getDouble(k));
//					
//					
//					sm.addxList(sm.getImages().length, k,
//							sm.getSortedX().getDouble(k));
//					
//					debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//					
//					IDataset output1 = DummyProcessingClass.DummyProcess(sm, 
//																		 j, 
//																		 model, 
//																		 dm, 
//																		 gm, 
//																		 plotSystem,
////																		 ssvsPS,
//																		 correctionSelection, 
//																		 imagePosInOriginalDat[k], 
//																		 trackingMarker, 
//																		 k);
//
//					if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
//						debug("Dummy Proccessing failure");
//
//						break;	
//					}
//					
//					sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//					
//					int imageNumber =k;
//					IDataset tempImage = ssp.getImage(imageNumber);
//					double[] tempLoc = sm.getLocationList().get(imageNumber);
//					RectangularROI newROI = new RectangularROI(tempLoc[0],
//						       tempLoc[1],
//						       sm.getInitialLenPt()[0][0],
//						       sm.getInitialLenPt()[0][1],0);
//					
//					display.syncExec(new Runnable() {
//						@Override
//						public void run() {	
//								
//		
//							updateTrackingDisplay(tempImage, imageNumber);
//							return;
//							}
//						});
//				}
//			}
//
//			debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//			
//			models.get(jok).setInput(null);
//			
//			if(sm.getStartFrame() != noImages-1){
//				for (int k = sm.getStartFrame(); k < noImages; k++) {
//	
//					if (sm.getFilepathsSortedArray()[k] == jok) {
//	
//						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//						
//						
//						debug("switched to k++");
//						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//						+ " , " + "local jok:  " + Integer.toString(jok));
//						
//						int trackingMarker = 2;
//						IDataset j = ssp.getImage(k);
//						DataModel dm = dms.get(jok);
//						GeometricParametersModel gm = gms.get(jok);
//						ExampleModel model = models.get(jok);
//	
//						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//								sm.getSortedX().getDouble(k));
//						
//						
//						sm.addxList(sm.getImages().length, k,
//								sm.getSortedX().getDouble(k));
//						
//						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//						
//						IDataset output1 = 
//								DummyProcessingClass.DummyProcess0(sm, 
//																  j, 
//																  model, 
//																  dm,
//														   		  gm, 
//														   		  plotSystem,
//														   		  ssvsPS,
//														   		  correctionSelection, 
//														   		  imagePosInOriginalDat[k], 
//														   		  trackingMarker, 
//														   		  k);
//	
//						
//	
//						if(Arrays.equals(output1.getShape(),(new int[] {2,2}) )){
//							Display d =Display.getCurrent();
//							debug("Dummy Proccessing failure");
//							ssp.boundariesWarning("position 1, line ~1955, k: " + Integer.toString(k),d);
//						
//							break;	
//						}
//						
//						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//						
//						
//						int imageNumber =k;
//						IDataset tempImage = ssp.getImage(imageNumber);
//						double[] tempLoc = sm.getLocationList().get(imageNumber);
//						RectangularROI newROI = new RectangularROI(tempLoc[0],
//							       tempLoc[1],
//							       sm.getInitialLenPt()[0][0],
//							       sm.getInitialLenPt()[0][1],0);
//						
//						display.syncExec(new Runnable() {
//							@Override
//							public void run() {	
//									
//								updateTrackingDisplay(tempImage, imageNumber);
//								
//								return;
//							}
//							});
//						
//						
//					}
//				}
//			}
//		//////bottom of k++ loop
//
//		doneArray[jok] = "done";
//
//		//////////////////////////// continuing to next
//		//////////////////////////// dat////////////////////////////////////////
//
//		while (ClosestNoFinder.full(doneArray, "done") == false) {
//
//			debug("in the while loop");
//
//			int nextk = ClosestNoFinder.closestNoWithoutDone(sm.getSortedX().getDouble(sm.getSliderPos()),
//					sm.getSortedX(), doneArray, sm.getFilepathsSortedArray());
//
//			int nextjok = sm.getFilepathsSortedArray()[nextk];
//
//			debug("nextk :" + nextk);
//			debug("nextjok :" + nextjok);
//			debug("doneArray[nextjok]: " + doneArray[nextjok]);
//			
//			if (imagePosInOriginalDat[nextk] == 0) {
//	
//				debug("In the while loop for imagePosInOriginalDat[nextk] == 0");
//				
//				for (int k = nextk; k < noImages; k++) {
//
//					if (sm.getFilepathsSortedArray()[k] == nextjok) {
//
//						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//						
//						
//						int trackingMarker = 0;
//						IDataset j = ssp.getImage(k);
//						int jokLocal = sm.getFilepathsSortedArray()[k];
//						DataModel dm = dms.get(jokLocal);
//						GeometricParametersModel gm = gms.get(jokLocal);
//						ExampleModel model = models.get(jokLocal);
//						
//						
//						if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//							
//							if (sm.getTrackerLocationList() == null | sm.getTrackerLocationList().size() <= 10 ){
//								int seedIndex = 
//										ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																			  sm.getSortedX(), 
//																			  sm.getLocationList());
//		
//								int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//								
//								
//								ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//								ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//								
//								Dataset yValues = DatasetFactory.zeros(seedList.size());
//								Dataset xValues = DatasetFactory.zeros(seedList.size());
//								Dataset lValues = DatasetFactory.zeros(seedList.size());
//								
//								for(int op = 0; op<seedList.size(); op++){
//									
//									double x = seedList.get(op)[1];
//									double y = seedList.get(op)[0];
//									double l = lList.get(op);
//									
//									xValues.set(x, op);
//									yValues.set(y, op);
//									lValues.set(l, op);
//			
//								}
//								
//								
//								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																							   lValues, 
//																							   xValues, 
//																							   yValues, 
//																							   sm.getInitialLenPt()[0],
//																							   1);
//								dm.setSeedLocation(seedLocation);
//								
//								debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//						
//							}
//							else{
//								double[] seedLocation = TrackerLocationInterpolation.trackerInterpolationInterpolator0(sm.getTrackerLocationList(), 
//																							   sm.getSortedX(), 
//																							   sm.getInitialLenPt()[0],
//																							   k);
//								dm.setSeedLocation(seedLocation);
//							}
//						}	
//						
//						
//						else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//							
//							int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//							int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//							
//							double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//									(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//									(double) (pt[1] + len[1]) };
//							
//							dm.setSeedLocation(seedLocation);
//						}
//						
//						dm.addxList(sm.getSortedX().getDouble(k));
//						
//						sm.addxList(sm.getImages().length, k,
//								sm.getSortedX().getDouble(k));
//						
//						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//						
//						IDataset output1 = 
//								DummyProcessingClass.DummyProcess1(sm, 
//																   j, 
//																   model, 
//																   dm, 
//																   gm, 
//																   plotSystem,
//																   ssvsPS,
//																   correctionSelection, 
//																   imagePosInOriginalDat[k], 
//																   trackingMarker, 
//																   k,
//																   dm.getSeedLocation());
//
//						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//							Display d =Display.getCurrent();
//							debug("Dummy Proccessing failure");
//							ssp.boundariesWarning("position 1, line ~2115, k: " + Integer.toString(k),d);
//							
//							break;
//						}
//						
//						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//						
//						
//						int imageNumber =k;
//						IDataset tempImage = ssp.getImage(imageNumber);
//						double[] tempLoc = sm.getLocationList().get(imageNumber);
//						RectangularROI newROI = new RectangularROI(tempLoc[0],
//							       tempLoc[1],
//							       sm.getInitialLenPt()[0][0],
//							       sm.getInitialLenPt()[0][1],0);
//						
//						
//						display.syncExec(new Runnable() {
//							@Override
//							public void run() {	
//
//								updateTrackingDisplay(tempImage, imageNumber);
//								return;
//							}
//						});
//						
//
//					}
//					doneArray[nextjok] = "done";
//				}
//			}
//
//			else if (imagePosInOriginalDat[nextk] != 0) {
//
//				debug("%%%%%%%%%%%%%%%%% switched the dat file");
//				debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//				
//				for (int k = (sm.getStartFrame()); k >= 0; k--) {
//
//					if (sm.getFilepathsSortedArray()[k] == nextjok) {
//
//						debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//						+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//						
//						
//						debug("%%%%%%%%%%%%%%%%% switched the dat file");
//						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//						
//						
//						int trackingMarker = 1;
//						IDataset j = ssp.getImage(k);
//						DataModel dm = dms.get(nextjok);
//						GeometricParametersModel gm = gms.get(nextjok);
//						ExampleModel model = models.get(nextjok);
//						
//						if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//							
//							int seedIndex = 
//									ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																		  sm.getSortedX(), 
//																		  sm.getLocationList());
//	
//							int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//							
//							
//							ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//							ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//							
//							double[] seedLocation =null;
//							Dataset yValues = DatasetFactory.zeros(new int[] {1});
//							Dataset xValues = DatasetFactory.zeros(new int[] {1});
//							Dataset lValues = DatasetFactory.zeros(new int[] {1});
//							
//							try{
//								yValues = DatasetFactory.zeros(seedList.size());
//								xValues = DatasetFactory.zeros(seedList.size());
//								lValues = DatasetFactory.zeros(seedList.size());
//							}
//							
//							catch(Exception r){
//								
//								boolean f = true;
//								
//								while(f){
//									
//									for(DataModel kr : dms){
//										if(kr.getLocationList() != null){
//											
//											seedList = kr.getLocationList();
//											
//											yValues = DatasetFactory.zeros(seedList.size());
//											xValues = DatasetFactory.zeros(seedList.size());
//											lValues = DatasetFactory.zeros(seedList.size());
//											
//											f = false;
//										}
//									}
//								}
//							}
//							
//							for(int op = 0; op<seedList.size(); op++){
//									
//									double x = seedList.get(op)[1];
//									double y = seedList.get(op)[0];
//									double l = lList.get(op);
//									
//									xValues.set(x, op);
//									yValues.set(y, op);
//									lValues.set(l, op);
//			
//							}
//								
//							seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																							   lValues, 
//																							   xValues, 
//																							   yValues, 
//																							   sm.getInitialLenPt()[0],
//																							   1);
//							dm.setSeedLocation(seedLocation);
//								
//							debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{ seedlocation[0] : " + seedLocation[0] +" + " + "seedlocation[1] :" + seedLocation[1]);
//							
//						}	
//						
//						else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//							
//							int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//							int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//							
//							double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//									(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//									(double) (pt[1] + len[1]) };
//							
//							dm.setSeedLocation(seedLocation);
//						}
//						
//						dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//								sm.getSortedX().getDouble(k));
//						
//						
//						sm.addxList(sm.getImages().length, k,
//								sm.getSortedX().getDouble(k));
//						
//						debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//						
//						IDataset output1 = 
//								DummyProcessingClass.DummyProcess1(sm, 
//																   j, 
//																   model, 
//																   dm, 
//																   gm, 
//																   plotSystem,
//																   ssvsPS,
//																   correctionSelection, 
//																   imagePosInOriginalDat[k], 
//																   trackingMarker, 
//																   k,
//																   dm.getSeedLocation());
//
//						
//						if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//							Display d =Display.getCurrent();
//							debug("Dummy Proccessing failure");
//							ssp.boundariesWarning("position 1, line ~2245, k: " + Integer.toString(k),d);
//							
//							break;
//						}
//						
//						
//						sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//						
//						
//						int imageNumber =k;
//						IDataset tempImage = ssp.getImage(imageNumber);
//						double[] tempLoc = sm.getLocationList().get(imageNumber);
//						RectangularROI newROI = new RectangularROI(tempLoc[0],
//							       tempLoc[1],
//							       sm.getInitialLenPt()[0][0],
//							       sm.getInitialLenPt()[0][1],0);
//						
//						display.syncExec(new Runnable() {
//							@Override
//							public void run() {	
//								
//								updateTrackingDisplay(tempImage, imageNumber);
//								return;
//							}
//							});
//						
//
//					}
//				}
//				models.get(nextjok).setInput(null);
//				if(sm.getStartFrame() != noImages-1){
//					for (int k = sm.getStartFrame()+1; k < noImages; k++) {
//						debug("%%%%%%%%%%%%%%%%% sm.getStartFrame:  "  +  sm.getStartFrame() + "??????????????????" );
//						if (sm.getFilepathsSortedArray()[k] == nextjok) {
//	
//							debug("l value: " + Double.toString(sm.getSortedX().getDouble(k)) + " , " + "local k:  " + Integer.toString(k)
//							+ " , " + "local nextjok:  " + Integer.toString(nextjok));
//							
//							
//							int trackingMarker = 2;
//							IDataset j = ssp.getImage(k);
//							DataModel dm = dms.get(nextjok);
//							GeometricParametersModel gm = gms.get(nextjok);
//							ExampleModel model = models.get(nextjok);
//	
//							
//							if(dm.getLocationList() == null && models.get(0).getTrackerType() != TrackerType1.INTERPOLATION){
//								
//								int seedIndex = 
//										ClosestNoFinder.closestNoWithLocation(sm.getSortedX().getDouble(k),
//																			  sm.getSortedX(), 
//																			  sm.getLocationList());
//		
//								int nearestCompletedDatFileNo = sm.getFilepathsSortedArray()[seedIndex];
//								
//								
//								ArrayList<double[]> seedList = dms.get(nearestCompletedDatFileNo).getLocationList();
//								ArrayList<Double> lList = dms.get(nearestCompletedDatFileNo).getxList();
//								
//								Dataset yValues = DatasetFactory.zeros(seedList.size());
//								Dataset xValues = DatasetFactory.zeros(seedList.size());
//								Dataset lValues = DatasetFactory.zeros(seedList.size());
//								
//								for(int op = 0; op<seedList.size(); op++){
//									
//									double x = seedList.get(op)[1];
//									double y = seedList.get(op)[0];
//									double l = lList.get(op);
//									
//									xValues.set(x, op);
//									yValues.set(y, op);
//									lValues.set(l, op);
//			
//								}
//								
//								double[] seedLocation = PolynomialOverlap.extrapolatedLocation(sm.getSortedX().getDouble(k),
//																							   lValues, 
//																							   xValues, 
//																							   yValues, 
//																							   sm.getInitialLenPt()[0],
//																							   1);
//								dm.setSeedLocation(seedLocation);
//							
//							}	
//							
//							else if(models.get(0).getTrackerType() == TrackerType1.INTERPOLATION){
//								
//								int[] len = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[0][1])};
//								int[]  pt = new int[] {(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][0]),(int) Math.round(sm.getInterpolatedLenPts().get(k)[1][1])};
//								
//								double[] seedLocation = new double[] { (double) pt[0], (double) pt[1], (double) (pt[0] + len[0]),
//										(double) (pt[1]), (double) pt[0], (double) pt[1] + len[1], (double) (pt[0] + len[0]),
//										(double) (pt[1] + len[1]) };
//								
//								dm.setSeedLocation(seedLocation);
//							}
//							
//							
//							dm.addxList(model.getDatImages().getShape()[0], imagePosInOriginalDat[k],
//									sm.getSortedX().getDouble(k));
//							
//							sm.addxList(sm.getImages().length, k,
//									sm.getSortedX().getDouble(k));
//							
//							debug("value added to xList:  "   + sm.getSortedX().getDouble(k)  + "  k:   " + k);
//							
//							IDataset output1 = 
//									DummyProcessingClass.DummyProcess1(sm, 
//																	   j, 
//																	   model, 
//																	   dm, 
//																	   gm, 
//																	   plotSystem,
//																	   ssvsPS,
//																	   correctionSelection, 
//																	   imagePosInOriginalDat[k], 
//																	   trackingMarker, 
//																	   k,
//																	   dm.getSeedLocation());
//	
//							if(Arrays.equals(output1.getShape(), (new int[] {2,2}))){
//								Display d =Display.getCurrent();
//								debug("Dummy Proccessing failure");
//								ssp.boundariesWarning(Integer.toString(k),d);
//								
//								break;
//							}
//							
//							
//							sm.addBackgroundDatArray(sm.getImages().length, k, output1);
//							
//							int imageNumber =k;
//							IDataset tempImage = ssp.getImage(imageNumber);
//							double[] tempLoc = sm.getLocationList().get(imageNumber);
//							RectangularROI newROI = new RectangularROI(tempLoc[0],
//								       tempLoc[1],
//								       sm.getInitialLenPt()[0][0],
//								       sm.getInitialLenPt()[0][1],0);
//							
//							display.syncExec(new Runnable() {
//								@Override
//								public void run() {	
//										
//									updateTrackingDisplay(tempImage, imageNumber);
//									
//									return;
//								}
//								});
//	
//						}
//					}
//				}
//				//////bottom of k++ loop	
//				doneArray[nextjok] = "done";
//			}
//		}
//		return;
//				}
//				
//		};
//		t.start();
//		}
//		
//		ssvs.getCustomComposite().getReplay().setEnabled(true);
//	}
//	
//	public void updateTrackingDisplay(IDataset tempImage, int imageNumber){
//		
//		ssp.updateSliders(ssvs.getSliderList(), imageNumber);
//		ssvs.getPlotSystemCompositeView().getFolder().setSelection(2);
//		ssp.updateSliders(ssvs.getSliderList(), imageNumber);
//		ssvs.updateIndicators(imageNumber);
//		ssvs.getPlotSystemCompositeView().getPlotSystem().updatePlot2D(tempImage, null, null);
//		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().updatePlot2D(sm.getBackgroundDatArray().get(imageNumber), null, null);
//		ssvs.getPlotSystemCompositeView().getPlotSystem().repaint(true);
//		ssvs.getPlotSystemCompositeView().getSubImageBgPlotSystem().repaint(true);
//		ssvs.getSsps3c().generalUpdate();
//		ssp.stitchAndPresent(ssvs.getSsps3c().getOutputCurves());
//		ssp.trackingRegionOfInterestSetter(sm.getLocationList().get(imageNumber), imageNumber);
//		ssvs.getSsps3c().getOutputCurves().getIntensity().select(0);
//		ssvs.getSsps3c().getOutputCurves().getIntensity().redraw();
////		System.out.println(progressBar.getSelection());
//		
//		if(progressBar.isDisposed() != true){
//			progressBar.setSelection(progressBar.getSelection() +1);
////			System.out.println(progressBar.getSelection());
//			
//			if(progressBar.getSelection() == progressBar.getMaximum()){
////				System.out.println(progressBar.getSelection() +" in the break");
//				tpaav.close();
//			}
//		}
//		
//	}
//
//	private void debug (String output) {
//		if (DEBUG == 1) {
//			System.out.println(output);
//		}
//	}
//}

///////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////Movie Job/////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////

class MovieJob {

	private int time = 220;
//	private IRegion background;
	private IDataset tempImage;
	private IDataset subTempImage;
	private IDataset subIBgTempImage;
	private double[] tempLoc;
	private SuperModel sm;
	private int noImages;
	private int timeStep;
	private int DEBUG = 0;
	private IPlottingSystem<Composite> pS;
	private IPlottingSystem<Composite> subIBgPS;
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private int imageNumber;
	private ArrayList<Slider> sliders;
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
	
	public void setSubIBgPS(IPlottingSystem<Composite> subIBgPS) {
		this.subIBgPS = subIBgPS;
	}
	
	public void setFolder (TabFolder folder){
		this.folder = folder;
	}
	
//	@Override
	protected void run() {
		

		
		final Display display = Display.getCurrent();
		
		Thread t  = new Thread(){
			@Override
			public void run(){
				
				sm.setSliderPos(0);
				
				int k = 0;
				
				for( k = 0; k<sm.getImages().length; k++){
							
					tempImage = ssp.getImage(k);
					subTempImage = sm.getBackgroundDatArray().get(k);
					tempLoc = sm.getLocationList().get(k);
					imageNumber =k;
					sm.setSliderPos(k);
					RectangularROI newROI = new RectangularROI(tempLoc[0],
														       tempLoc[1],
														       sm.getInitialLenPt()[0][0],
														       sm.getInitialLenPt()[0][1],0);
						
					display.syncExec(new Runnable() {
							@Override
							public void run() {
								
								folder.setSelection(folder.getItems().length -1);
								ssp.updateSliders(ssvs.getSliderList(), imageNumber);
								ssvs.updateIndicators(imageNumber);
								pS.updatePlot2D(tempImage, null, null);
								subIBgPS.updatePlot2D(sm.getBackgroundDatArray().get(imageNumber), null, null);
								pS.repaint(true);
								subIBgPS.repaint(true);
								ssvs.getSsps3c().generalUpdate();
								ssp.trackingRegionOfInterestSetter(imageNumber);
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
