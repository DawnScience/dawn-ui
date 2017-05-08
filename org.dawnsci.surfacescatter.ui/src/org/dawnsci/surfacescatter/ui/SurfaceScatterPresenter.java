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
import org.apache.commons.math3.util.MathArrays;
import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.BoxSlicerRodScanUtilsForDialog;
import org.dawnsci.surfacescatter.ClosestNoFinder;//private ArrayList<double[]> locationList; 
import org.dawnsci.surfacescatter.CountUpToArray;
import org.dawnsci.surfacescatter.DataModel;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessingClass;
import org.dawnsci.surfacescatter.ExampleModel;
import org.dawnsci.surfacescatter.FittingParameters;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.FittingParametersOutput;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.IntensityDisplayEnum.IntensityDisplaySetting;
import org.dawnsci.surfacescatter.InterpolationTracker;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.RodObjectNexusBuilderModel;
import org.dawnsci.surfacescatter.RodObjectNexusUtils;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.dawnsci.surfacescatter.SplineInterpolationTracker;
import org.dawnsci.surfacescatter.StitchedOutputWithErrors;
import org.dawnsci.surfacescatter.SuperModel;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SurfaceScatterPresenter {

	private ArrayList<FrameModel> fms;
	private ArrayList<ExampleModel> models;
	private ArrayList<DataModel> dms;
	private GeometricParametersModel gm;
	private SuperModel sm;
	private int noImages = 0;
	private String imageName = "file_image";
	private String[] options;
	private boolean qConvert;
	private double energy;
	private Set<IPresenterStateChangeEventListener> listeners = new HashSet<>();
	private int DEBUG = 1;
	private PrintWriter writer;
	private Shell parentShell;
	private DirectoryModel drm;
	
	public void surfaceScatterPresenterBuild(String[] filepaths,
								   String xName,
								   String imageFolderPath,
								   String datFolderPath,
								   int correctionSelection) {

		sm = new SuperModel();
		dms = new ArrayList<DataModel>();
		models = new ArrayList<ExampleModel>();
		sm.setFilepaths(filepaths);
		sm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
		sm.setImageFolderPath(imageFolderPath);
		fms = new ArrayList<FrameModel>();
		drm = new DirectoryModel();
		
		ArrayList<ArrayList<Integer>> framesCorespondingToDats = new ArrayList<>();
		
		
		ILazyDataset[] imageArray = new ILazyDataset[filepaths.length];
		//////imageArray is an array of the image ILazyDatasets 
		
		IDataset[] xArray = new IDataset[filepaths.length];
		////xArray is an array of the l params (for a rod)
		
		IDataset[] tifNamesArray = new IDataset[filepaths.length];
		////tifNamesArray is an array of the tif names contained in each .dat (one dataset of tif names per dat in the array)
		IDataset[] tifPositionInDatArray = new IDataset[filepaths.length];
		////tifPositionInDatArray is an array of the positions (ints) of the tif's in each .dat (one dataset of tif positons per dat in the array)
		
		TreeMap<Integer, ILazyDataset> som = new TreeMap<Integer, ILazyDataset>();
		ArrayList<Integer> imageRefList = new ArrayList<>();
		/////imageRefList is the number that the image is read in at, i.e. the nth image to be read
		
		
		int imageRef = 0;
		ArrayList<Integer> imagesToFilepathRef = new ArrayList<Integer>();
		
		//////imagesToFilepathRef: once the images have been sorted into an ascending "array", the position on that array of an image
		//////corresponds to an integer in this list, which corresponds to the position of that image's dat file in String[] filepaths
		
		try {
		
			for (int id = 0; id < filepaths.length; id++) {
				
					models.add(new ExampleModel());
					dms.add(new DataModel());
					
					gm.setxName(xName);
					gm.setxNameRef(xName);
					
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
						
						
						//////////////////getting an array of .tifs
						
						String[] tifNames = StringUtils.substringsBetween(content, File.separator, ".tif");
						String[] tifNamesOut = new String[tifNames.length];
						int[] tifPositionsInDat = new int[tifNames.length];
						
						for(int w = 0; w<tifNames.length; w++){
							String t = tifNames[w];
							
							if(t.contains(File.separator)){
								t = StringUtils.substringAfterLast(t,File.separator);
							}
							
							t = imageFolderPath + File.separator + t +".tif";
							
							System.out.println(t);
							
							tifNamesOut[w] = t;
							tifPositionsInDat[w] = w; 
						}
						
						Dataset tifNamesDatasetOut = DatasetFactory.createFromObject(tifNamesOut);
						Dataset tifPositionsInDatOut = DatasetFactory.createFromObject(tifPositionsInDat);
						
						models.get(id).setTifNames(tifNamesDatasetOut);
						tifNamesArray[id] = tifNamesDatasetOut;
						tifPositionInDatArray[id] = tifPositionsInDatOut; 
						
						
						framesCorespondingToDats.set(id, new ArrayList<>());
						
					}
					
					ILazyDataset ild = null;
					
					ild = dh1.getLazyDataset(gm.getImageName());
					
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
					imageArray[id] = ild;
//					imageArray is an array of the images in read-in order
	
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
							ILazyDataset ildx = dh1.getLazyDataset(gm.getxName());
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
						
						ILazyDataset ildx = dh1.getLazyDataset(gm.getxNameRef());
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
		
			gm.addPropertyChangeListener(new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent evt) {
					for (int id = 0; id < filepaths.length; id++) {
						try {
							IDataHolder dh1 = LoaderFactory.getData(filepaths[id]);
							ILazyDataset ild = dh1.getLazyDataset(gm.getImageName());
							models.get(id).setDatImages(ild);
						}

						catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			});
		

		updateAnalysisMethodology(0, 1, 0, "10");
		
		Dataset xArrayCon = DatasetFactory.zeros(1);
		Dataset tifNamesCon = DatasetFactory.zeros(1);
		Dataset tifPositionsCon = DatasetFactory.zeros(1);
		
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
//			xArrayCon is an unsorted, but concatenated DoubleDataset of l values
			tifNamesCon = DatasetUtils.concatenate(tifNamesArray, 0);
//			tifNamesCon is an unsorted, but concatenated DoubleDataset of l tif names
			tifPositionsCon = DatasetUtils.concatenate(tifPositionInDatArray, 0);
//			tifPositionsCon is an unsorted, but concatenated DoubleDataset of the positions of tif names in .dats
			numberOfImages = xArrayCon.getSize();
		}
		catch(NullPointerException e){
			
		}
				
		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());
		
//		imageRefDat is a dataset, equal in length  to imageRefList (list of the integer number of the image that is read in - the nth read in, for example), 
//		and will be sorted based on the xArrayCon, which is the"l" values (for a rod)
		
		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());
		
//		imagesToFilepathRefDat is a dataset, equal in length  to imagesToFilepathRef (list of the integer number of the dat (in String[] filepaths) of the image that is read in at that point- the nth read in, for example), 
//		and will be sorted based on the xArrayCon, which is the"l" values (for a rod)
		
		Dataset imagesPositionsInDat = DatasetFactory.ones(imageRefList.size());
		
//		imagesPositionsInDat is a dataset, equal in length to imagesToFilepathRef (list of the integer number of the position of a tif file in the originating .dat, currently in the order in which they were read in), 
//		and will be sorted based on the xArrayCon, which is the"l" values (for a rod)
		
		
		for (int sd = 0; sd < imageRefList.size(); sd++) {
			imageRefDat.set(imageRefList.get(sd), sd);
			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
			imagesPositionsInDat.set(tifPositionsCon.getInt(sd), sd);
		}

		sm.setSortedDatIntsInOrderDataset(imagesToFilepathRefDat);
		
		Dataset xArrayConClone = xArrayCon.clone();
   
		DoubleDataset xArrayConCloneDouble = (DoubleDataset) xArrayConClone.clone();
		
		try{
			DatasetUtils.sort(xArrayCon, imageRefDat);
//			so now we have the image number in imageArray (imageRefDat) sorted by "l" value xArrayCon
			DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);
//			so now we have the dat number in filepaths (imagesToFilepathRefDat) sorted by "l" value xArrayCon
			Dataset sortedTifNamesCon = this.sortStrings(xArrayConCloneDouble, tifNamesCon);
//			so now we have the tif names sorted by "l" value xArrayCon
			DatasetUtils.sort(xArrayConClone, imagesPositionsInDat);
//			so now we have the tif positions in their originating .dat files sorted by "l" value xArrayCon
			
			ILazyDataset[] imageSortedDat = new ILazyDataset[imageRefList.size()];
//			imageSortedDat this is the array of sorted images - sorted according to "l"
			
			int[] filepathsSortedArray = new int[imageRefList.size()];
//			filepathsSortedArray is the .dat positions in filepaths  - sorted by images "l" values
			noImages = imageRefList.size();
	
	
			String[] datNamesInOrder = new String[imageRefList.size()];
			
			for(int f = 0; f<imageRefList.size(); f++ ){
				datNamesInOrder[f] = filepaths[imagesToFilepathRefDat.getInt(f)];
				filepathsSortedArray[f] = imagesToFilepathRefDat.getInt(f);
				int pos = imageRefDat.getInt(f);
				imageSortedDat[f] = som.get(pos);
				
				FrameModel fm = new FrameModel();
				fms.add(fm);
				
				fm.setRawImageData(imageSortedDat[f]);
				fm.setTifFilePath( sortedTifNamesCon.getString(f));
				fm.setDatFilePath(datNamesInOrder[f]);
				fm.setDatNo(imagesToFilepathRefDat.getInt(f));
				fm.setBackgroundMethdology(Methodology.TWOD);
				fm.setFitPower(FitPower.ONE);
				fm.setTrackingMethodolgy(TrackerType1.TLD);
				
				double polarisation = SXRDGeometricCorrections.polarisation(datNamesInOrder[f], 
																			gm.getInplanePolarisation(), 
																			gm.getOutplanePolarisation())
																			.getDouble(imagesPositionsInDat.getInt(f));
				fm.setPolarisationCorrection(polarisation);
				
				double lorentz = SXRDGeometricCorrections.lorentz(datNamesInOrder[f])
														 .getDouble(imagesPositionsInDat.getInt(f));

				fm.setLorentzianCorrection(lorentz);
				
				double areaCorrection = SXRDGeometricCorrections.areacor(datNamesInOrder[f], 
																		 gm.getBeamCorrection(), 
																		 gm.getSpecular(),  
																		 gm.getSampleSize(), 
																		 gm.getOutPlaneSlits(), 
																		 gm.getInPlaneSlits(), 
																		 gm.getBeamInPlane(), 
																		 gm.getBeamOutPlane(), 
																		 gm.getDetectorSlits())
																		 .getDouble(imagesPositionsInDat.getInt(f));
				
				fm.setAreaCorrection(areaCorrection);
				fm.setScannedVariable(xArrayCon.getDouble(f));
				
				
			}
			
			
			for(int r = 0; r< fms.size(); r++){
				
				framesCorespondingToDats.get(fms.get(r).getDatNo()).add((Integer)r);
				
			}
			
			drm.setFramesCorespondingToDats(framesCorespondingToDats);
			
			Dataset sortedDatNamesInOrderDataset = DatasetFactory.createFromObject(datNamesInOrder);
			sm.setSortedDatNamesInOrderDataset(sortedDatNamesInOrderDataset);			
	
			sm.setFilepathsSortedArray(filepathsSortedArray);
	
			sm.setImages(imageSortedDat);
			sm.setImageStack(imageCon);
			sm.setSortedX(xArrayCon);
			sm.setSortedTifFiles(sortedTifNamesCon);
	
			SliceND slice2 = new SliceND(imageCon.getShape());
			slice2.setSlice(0, 0, 1, 1);
			Dataset nullImage = (Dataset) imageCon.getSlice(slice2);
	
			sm.setNullImage((Dataset) imageCon.getSlice(slice2));
	
			sm.setNumberOfImages(numberOfImages);
			sm.setNullImage(nullImage);
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public DirectoryModel getDrm() {
		return drm;
	}

	public void setDrm(DirectoryModel drm) {
		this.drm = drm;
	}

	public ArrayList<Double> getYList(){
		return sm.getyList();
	}
	
	public String getXName(){
		return gm.getxName();
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
		
		if(qConvert){
			qConversion();
		}
		
	}
	
	
	public void saveParameters(String title){
		
		ExampleModel m = models.get(sm.getSelection());
		
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
	
	
	public double[] regionOfInterestSetter(IROI green) {

		IRectangularROI greenRectangle = green.getBounds();
		int[] len = greenRectangle.getIntLengths();
		int[] pt = greenRectangle.getIntPoint();

		int[][] lenPt = { len, pt };

		for (ExampleModel m : models) {
			m.setBox(greenRectangle);
			m.setLenPt(lenPt);
			m.setROI(green);
		}
		
		for (DataModel dm :dms){
			dm.setInitialLenPt(lenPt);
		}
		
		int [][] test = getLenPt();
		
		if((Arrays.equals(lenPt[0], getLenPt()[0]) == false) ||
		   (Arrays.equals(lenPt[1], getLenPt()[1]) == false)){
			
			sm.setInitialLenPt(lenPt);
			
			try{
				fireStateListeners();
			}
			catch(Exception f){
				
			}
			
		}
		
		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 
															   models.get(0).getBoundaryBox(), 
															   models.get(0).getMethodology());
	
		
		
		return bgRegionROI;
	
	}
	

	
	public ArrayList<double[]> getLocationList(){
		return sm.getLocationList();
	}
	
	public void backgroundBoxesManager(IRegion r1, IRegion r2, Button centreButton){
		
		Display display = Display.getCurrent();
        Color magenta = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
        Color red = display.getSystemColor(SWT.COLOR_RED);

		if (models.get(0).getMethodology() == Methodology.SECOND_BACKGROUND_BOX ||
			models.get(0).getMethodology() == Methodology.OVERLAPPING_BACKGROUND_BOX){

			r1.setVisible(false);
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
				
		}
	}
	
	public void addToInterpolatorRegions(IRegion region){
		sm.addToInterpolatorRegions(region);
	}
	
	@SuppressWarnings("incomplete-switch")
	public ArrayList<double[][]> interpolationTrackerBoxesAccept(IRegion r2){
				
		double[][] box = new double[3][];
				
		double[] lengths = new double[] {(double) r2.getROI().getBounds().getIntLengths()[0], (double) r2.getROI().getBounds().getIntLengths()[1]};
		double[] pts = new double[] {(double) r2.getROI().getBounds().getIntPoint()[0], r2.getROI().getBounds().getIntPoint()[1]};
		double[] xdata = new double[]{(double) sm.getSliderPos(), (double) sm.getSortedX().getDouble(sm.getSliderPos())};
				
		box[0] = lengths;
		box[1] = pts;
		box[2] = xdata;
				
		sm.addToInterpolatorBoxes(box);
		
		ArrayList<double[][]> interpolatedLenPts = new ArrayList<>();
		

		if(sm.getInterpolatorBoxes().size() > 2 
				&& getTrackerType() == TrackerType1.SPLINE_INTERPOLATION ){
			
					SplineInterpolationTracker split = new SplineInterpolationTracker();
					
					interpolatedLenPts = split.interpolatedTrackerLenPtArray1(sm.getInterpolatorBoxes(),
																			  sm.getSortedX());
					
					sm.setInterpolatedLenPts(interpolatedLenPts);

					return interpolatedLenPts;
			}

		if(sm.getInterpolatorBoxes().size() > 1){
				
					interpolatedLenPts = InterpolationTracker.interpolatedTrackerLenPtArray(sm.getInterpolatorBoxes(), 
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
		
		if((getTrackerType() == TrackerType1.INTERPOLATION 
				|| getTrackerType() == TrackerType1.SPLINE_INTERPOLATION)
				&& sm.getInterpolatorRegions()!= null){
			
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
		
		if((getTrackerType() == TrackerType1.INTERPOLATION 
				|| getTrackerType() == TrackerType1.SPLINE_INTERPOLATION)
				&& sm.getInterpolatorRegions()!= null){
			
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
	
	public RectangularROI[] trackingRegionOfInterestSetter(int[][] lenPt) {

		int[] len = lenPt[0];
		int[] pt = lenPt[1];
		RectangularROI newGreenROI = new RectangularROI(pt[0],
				pt[1],
				len[0],
				len[1],
				0);

		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 
				   models.get(0).getBoundaryBox(), 
				   models.get(0).getMethodology());

		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
												  bgRegionROI[1],
												  bgRegionROI[2],
												  bgRegionROI[3],
												  bgRegionROI[4]);

		return new RectangularROI[] {newGreenROI, bgROI};
		
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
		
	}
	
	public void setInterpolatedLenPts(ArrayList<double[][]> intepolatedLenPts){
		sm.setInterpolatedLenPts(intepolatedLenPts);
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
	
	public RectangularROI[] regionOfInterestSetter(int[][] lenPt) {

		
		RectangularROI green = new RectangularROI(lenPt[1][0],
												  lenPt[1][1],
												  lenPt[0][0],
												  lenPt[0][1],
												  0);

		for (ExampleModel m : models) {
			m.setLenPt(lenPt);
			m.setROI(green);
		}
		
		for (DataModel dm :dms){
			dm.setInitialLenPt(lenPt);
		}
		
		if(Arrays.equals(sm.getInitialLenPt()[0],lenPt[0]) == false ||
		   Arrays.equals(sm.getInitialLenPt()[1],lenPt[1]) == false){
			
			sm.setInitialLenPt(lenPt);
		}
		
		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 
				   models.get(0).getBoundaryBox(), 
				   models.get(0).getMethodology());

		RectangularROI bgROI = new RectangularROI(bgRegionROI[0],
											      bgRegionROI[1],
											      bgRegionROI[2],
											      bgRegionROI[3],
											      bgRegionROI[4]);
		
		return new RectangularROI[] {green, bgROI};
	
	}
	
	public int[][] getLenPt(){
		if (sm != null){
			return sm.getInitialLenPt();
		}
		else{
			return new int[][] {{0,0},{0,0}};
		}
		
	}
	
	public void setLenPt(int[][] lenPt){
		
		if((Arrays.equals(lenPt[0], getLenPt()[0]) == false) ||
		   (Arrays.equals(lenPt[1], getLenPt()[1]) == false)){
					
			sm.setInitialLenPt(lenPt);
					
			try{
				fireStateListeners();
			}
			catch(Exception f){			
			}			
		}
	}

	public void sliderZoomedArea(int sliderPos, IROI box, IPlottingSystem<Composite>... pS) {
		Dataset image = this.getImage(sliderPos);
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
	}
	
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
	
	public double getQValue(int k){
		
		if(sm != null){
			if(sm.getSortedQ() != null){
				return sm.getSortedQ().getDouble(k);
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
										  int trackingMarker) {

		int j = sm.getFilepathsSortedArray()[selection];

		int[] imagePosInOriginalDat = CountUpToArray.CountUpToArray1(sm.getFilepathsSortedArray());
		try{
			IDataset output = DummyProcessingClass.DummyProcess(sm,
													 image, 
													 models.get(j), 
													 dms.get(j), 
													 gm, 
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

	public int xPositionFinder(double myNum) {

		int xPos = ClosestNoFinder.closestNoPos(myNum, sm.getSortedX());

		return xPos;
	}
	
	public int qPositionFinder(double myNum) {

		int qPos = ClosestNoFinder.closestNoPos(myNum, sm.getSortedQ());

		return qPos;
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
		
		fireStateListeners();
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
	
	public void writeNexus(String nexusFilePath){
		RodObjectNexusBuilderModel rnbm = new RodObjectNexusBuilderModel();
		rnbm.setSm(sm);
		rnbm.setDms(dms);
		rnbm.setGm(gm);
		rnbm.setModels(models);
		rnbm.setFilepath(nexusFilePath);
		
		RodObjectNexusUtils ronu = new RodObjectNexusUtils(rnbm);
		
	}
	
	
	
	
	
	
	
	public void genXSave(String title){
	
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
		    	writer.println("#"+gm.getxName()+"	I	Ie");
				
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
							sm.getSplicedCurveY().getError(gh));
				 }
			
		    }
		    
		 	
	    }	
		writer.close();
	}	
	
	public IDataset getSplicedCurveX(){
		return sm.getSplicedCurveX();
	}
	
	public IDataset getSplicedCurveY(){
		return sm.getSplicedCurveY();
	}
	
	public IDataset getSplicedCurveYFhkl(){
		return sm.getSplicedCurveYFhkl();
	}
	
	public IDataset getSplicedCurveYRaw(){
		return sm.getSplicedCurveYRaw();
	}

	
	public IDataset getSplicedCurveQ(){
		return sm.getSplicedCurveQ();
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

	public void anarodSave(String title){

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
		    	writer.println("#"+gm.getxName()+"	I	Ie");
				
				 for(int gh = 0 ; gh<sm.getImages().length; gh++){
							writer.println(sm.getxList().get(gh) +"	"+ 
						    sm.getSplicedCurveY().getDouble(gh)+ "	"+ 
							sm.getSplicedCurveY().getError(gh));
				 }
			
		    }
	    	
	    }
	    
		writer.close();
	}	
	
	public void intSave(String title){
		
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
		    	writer.println("#"+gm.getxName()+"	I	Ie	Area Correction	Flux Correction");
				
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
	
	public void simpleXYYeSave(String title, int state){
		
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
	
	public void setSplicedCurveX(IDataset xData){
		sm.setSplicedCurveX(xData);
	}
	
	public ArrayList<Double> getQList(){
		return sm.getqList();
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

	public void switchFhklIntensity(IPlottingSystem<Composite> pS, 
									int selector,
									boolean qAxis){
		
		pS.clear();
		
		ILineTrace lt = 
				pS.createLineTrace("Corrected Intensity Curve");
		
		Display display = Display.getCurrent();
		
		IDataset x = DatasetFactory.zeros(new int[] {2,2}, Dataset.ARRAYFLOAT64);
		
		if(qAxis){
			x = getSplicedCurveQ();
		}
		else{
			x = getSplicedCurveX();
		}
		
		if(selector ==0){
					
			lt.setData(x,sm.getSplicedCurveY());
		
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			
			lt.setTraceColor(blue);
		}

		if(selector ==1){
			
			lt.setName("Fhkl Curve");
			
			lt.setData(x,sm.getSplicedCurveYFhkl());
			
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
		
			lt.setTraceColor(green);
		}
		
		if(selector ==2){
			
			lt.setName("Raw Intensity Curve");
			
			lt.setData(x,sm.getSplicedCurveYRaw());
			
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
	
	
	public void qConversion(){
		sm.qConversion();
	}
	
	public void setNexusPath(String np){
		sm.setNexusPath(np);
	}
	
	public String getNexusPath(){
		return sm.getNexusPath();
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
	
	public void stitchAndPresent1(MultipleOutputCurvesTableView outputCurves,
								  IntensityDisplaySetting ids) {

		Display display = Display.getCurrent();
		
		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();
		
		IDataset[] output = StitchedOutputWithErrors.curveStitch4(dms, sm);

		ILineTrace lt = pS.createLineTrace("progress");

		IDataset X = DatasetFactory.createFromObject(sm.getSplicedCurveX());
		
		if(outputCurves.getqAxis().getSelection()){
			
			qConversion();
			X = sm.getSplicedCurveQ();
		}
		
		else{
			X = sm.getSplicedCurveX();
		}
		
		
		if(ids == null){

			lt.setData(X, sm.getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);
		}
		
		else if(ids == IntensityDisplaySetting.Corrected_Intensity){

			lt.setData(X, sm.getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);
			
		}
		else if(ids == IntensityDisplaySetting.Fhkl){

			lt.setData(X, sm.getSplicedCurveYFhkl());
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
			lt.setTraceColor(green);
		}
		else if(ids == IntensityDisplaySetting.Raw_Intensity){

			lt.setData(X, sm.getSplicedCurveYRaw());
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			lt.setTraceColor(black);
			
		}
		
		
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
	
	public boolean getErrorFlag(){
		return sm.isErrorDisplayFlag();
	}
	
	public void setErrorFlag(boolean n){
		 sm.setErrorDisplayFlag(n);
	}
	
	public void geometricParametersWindowPopulate(){
		
	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

	public GeometricParametersModel getGm() {
		return gm;
	}

	public void setGm(GeometricParametersModel gm) {
		this.gm = gm;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
		sm.setEnergy(energy);
	}
	
	
	public int getTheta() {
		return sm.getTheta();
	}

	public void setTheta(int theta) {
	
		sm.setTheta(theta);
	}

	public boolean isqConvert() {
		return qConvert;
	}

	public void setqConvert(boolean qConvert) {
		this.qConvert = qConvert;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

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
	}
	
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
	
	public void addStateListener(IPresenterStateChangeEventListener listener){
		listeners.add(listener);
	}
	
	private void fireStateListeners(){
		for (IPresenterStateChangeEventListener l : listeners) l.update();
	}
	
	public SurfaceScatterPresenter(){
		sm = new SuperModel();
	}
	
	public void createGm(){
		gm = new GeometricParametersModel();
	}
	
	
	public static Dataset sortStrings(DoubleDataset a, Dataset b) {
		if (!DTypeUtils.isDTypeNumerical(a.getDType())) {
			throw new UnsupportedOperationException("Sorting non-numerical datasets not supported yet");
		}

		// gather all datasets as double dataset copies
		
		DoubleDataset s = (DoubleDataset) DatasetFactory.createFromObject(a);
				
		int l = b == null ? 0 : b.getSize();
		Dataset[] t = new Dataset[l];
		int n = 0;
		for (int i = 0; i < l; i++) {
			if (b.getObject(i) != null) {

				t[i] = DatasetFactory.createFromObject(b.getObject(i));
				n++;
			}
		}

		double[] positionsInB  = new double[l]; 
		
		for (int r = 0; r<l; r++){
			positionsInB[r] = r;
		}
		
//		String[][] y = new String[n][];
//		for (int i = 0, j = 0; i < l; i++) {
//			if (t[i] != null) {
//				y[j++] = t[i].getObject();
//			}
//		}

		MathArrays.sortInPlace(s.getData(), positionsInB);

		String[] sortedB  = new String[l]; 
		
		for (int r = 0; r<l; r++){
			sortedB[r] = b.getString((int) positionsInB[r]);
		}
		
		Dataset outputB = DatasetFactory.createFromObject(sortedB);
		
		a.setSlice(s);
		
		return outputB;
		
	}
	
	public ArrayList<FrameModel> getFms() {
		return fms;
	}

	public void setFms(ArrayList<FrameModel> fms) {
		this.fms = fms;
	}

	

}
