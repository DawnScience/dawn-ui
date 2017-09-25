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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.MathArrays;
import org.dawnsci.surfacescatter.AnalaysisMethodologies;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.AxisEnums;
import org.dawnsci.surfacescatter.AxisEnums.yAxes;
import org.dawnsci.surfacescatter.BoxSlicerRodScanUtilsForDialog;
import org.dawnsci.surfacescatter.ClosestNoFinder;
import org.dawnsci.surfacescatter.CsdpGeneratorFromDrm;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.CurveStitchWithErrorsAndFrames;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.DummyProcessWithFrames;
import org.dawnsci.surfacescatter.FittingParameters;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.FittingParametersOutput;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.GeometricCorrectionsReflectivityMethod;
import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.InterpolationTracker;
import org.dawnsci.surfacescatter.LocationLenPtConverterUtils;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.OverlapAttenuationObject;
import org.dawnsci.surfacescatter.PlotSystem2DataSetter;
import org.dawnsci.surfacescatter.PolynomialOverlap;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum;
import org.dawnsci.surfacescatter.ProcessingMethodsEnum.ProccessingMethod;
import org.dawnsci.surfacescatter.ReflectivityAngleAliasEnum;
import org.dawnsci.surfacescatter.ReflectivityFluxCorrectionsForDialog;
import org.dawnsci.surfacescatter.ReflectivityFluxParametersAliasEnum;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.ReflectivityNormalisation;
import org.dawnsci.surfacescatter.RodObjectNexusBuilderModel;
import org.dawnsci.surfacescatter.RodObjectNexusUtils;
import org.dawnsci.surfacescatter.RodObjectNexusUtils_Development;
import org.dawnsci.surfacescatter.SXRDAngleAliasEnum;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.dawnsci.surfacescatter.SavingUtils;
import org.dawnsci.surfacescatter.SetupModel;
import org.dawnsci.surfacescatter.SplineInterpolationTracker;
import org.dawnsci.surfacescatter.TrackingMethodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory; 

public class SurfaceScatterPresenter {

	private ArrayList<FrameModel> fms;
	private GeometricParametersModel gm;
	private SetupModel stm;
	private int noImages = 0;
	private String imageName = "file_image";
	private String[] options;
	private boolean qConvert;
	private double energy;
	private Set<IPresenterStateChangeEventListener> listeners = new HashSet<>();
	private int DEBUG = 0;
	private PrintWriter writer;
	private Shell parentShell;
	private DirectoryModel drm;
	private int sliderPos = 0;
	private String saveFolder;
	private boolean errorDisplayFlag;
	private ProcessingMethodsEnum.ProccessingMethod processingMethodSelection = ProcessingMethodsEnum.ProccessingMethod.AUTOMATIC;
	private IDataHolder dh1;
	private double currentRawIntensity;
	private boolean trackWithQ = false;
	private int bB = 10; // this is a false boundaryBox, used to avoid hitting
	// edges
	private boolean updateSvs;

	public SurfaceScatterPresenter() {
		drm = new DirectoryModel();
		stm = new SetupModel();
	}

	public void surfaceScatterPresenterBuildWithFrames(String[] filepaths, 
			String xName,
			// String datFolderPath,
			MethodSetting correctionSelection) {

		updateSvs = false;
		fms = new ArrayList<FrameModel>();
		drm = new DirectoryModel();
		drm.setFms(fms);

		drm.setDatFilepaths(filepaths);

		drm.setCorrectionSelection(correctionSelection);

		ILazyDataset[] imageArray = new ILazyDataset[filepaths.length];
		////// imageArray is an array of the image ILazyDatasets

		IDataset[] xArray = new IDataset[filepaths.length];
		IDataset[] thetaArray = new IDataset[filepaths.length];
		//// xArray is an array of the l params (for a rod)
		//// thetaArray is an array of the "theta" params (for refecltivity)
		IDataset[] hArray = new IDataset[filepaths.length];
		IDataset[] kArray = new IDataset[filepaths.length];
		IDataset[] lArray = new IDataset[filepaths.length];
		
		IDataset[] internalFluxArray = new IDataset[filepaths.length];

		IDataset[] dcdThetaArray = new IDataset[filepaths.length];
		IDataset[] qdcdArray = new IDataset[filepaths.length];

		IDataset[] tifNamesArray = new IDataset[filepaths.length];
		//// tifNamesArray is an array of the tif names contained in each .dat
		//// (one dataset of tif names per dat in the array)

		TreeMap<Integer, ILazyDataset> som = new TreeMap<Integer, ILazyDataset>();
		ArrayList<Integer> imageRefList = new ArrayList<>();
		///// imageRefList is the number that the image is read in at, i.e. the
		///// nth image to be read
		ArrayList<Integer> imageNoInDatList = new ArrayList<>();
		///// imageNoInDatList is the number that the image is at in the Dat

		int imageRef = 0;
		ArrayList<Integer> imagesToFilepathRef = new ArrayList<Integer>();
		String imageFolderPath = stm.getImageFolderPath();

		////// imagesToFilepathRef: once the images have been sorted into an
		////// ascending "array", the position on that array of an image
		////// corresponds to an integer in this list, which corresponds to the
		////// position of that image's dat file in String[] filepaths

		Dataset xArrayCon = DatasetFactory.zeros(1);
		Dataset hArrayCon = DatasetFactory.zeros(1);
		Dataset kArrayCon = DatasetFactory.zeros(1);
		Dataset lArrayCon = DatasetFactory.zeros(1);
		Dataset thetaArrayCon = DatasetFactory.zeros(1);
		
//		Dataset internaFluxArrayCon = DatasetFactory.zeros(1);
		

		Dataset dcdThetaCon = DatasetFactory.zeros(1);
		Dataset qdcdCon = DatasetFactory.zeros(1);

		ILazyDataset qdcd = null;

		gm.setxName(xName);

		try {

			for (int id = 0; id < filepaths.length; id++) {

				// gm.setxNameRef(xName);

				if (imageFolderPath == null) {
					dh1 = LoaderFactory.getData(filepaths[id]);

					Path from = Paths.get(filepaths[id]);

					Charset charset = StandardCharsets.UTF_8;

					String content = new String(Files.readAllBytes(from), charset);

					String[] tifNames = StringUtils.substringsBetween(content, "/", ".tif");

					Dataset tifNamesDatasetOut = DatasetFactory.createFromObject(tifNames);

					tifNamesArray[id] = tifNamesDatasetOut;

				}

				else {

					String datName = StringUtils.substringAfterLast(filepaths[id], File.separator);

					String localFilepathCopy = StringUtils.substringBeforeLast(datName, ".dat") + "_copy";

					Path from = Paths.get(filepaths[id]);

					Path to = Paths.get(stm.getSaveFolder() + localFilepathCopy + ".dat");

					Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

					Charset charset = StandardCharsets.UTF_8;

					String content = new String(Files.readAllBytes(to), charset);

					String firstTifName = StringUtils.substringBetween(content, "/", ".tif");

					if (firstTifName.contains("/")) {
						firstTifName = StringUtils.substringAfterLast(firstTifName, "/");
					}

					String pathNameToReplace = StringUtils.substringBetween(content, "\t", "/" + firstTifName);

					if (pathNameToReplace.contains("\t")) {
						pathNameToReplace = StringUtils.substringAfterLast(pathNameToReplace, "\t");
					}

					// Dont mix linux and windows paths!

					imageFolderPath = imageFolderPath.replace("\\", "/");

					content = content.replaceAll(pathNameToReplace, imageFolderPath);

					Files.write(to, content.getBytes(charset));

					dh1 = LoaderFactory.getData(to.toString());

					////////////////// getting an array of .tifs

					String[] tifNames = StringUtils.substringsBetween(content, "/", ".tif");
					String[] tifNamesOut = new String[tifNames.length];

					for (int w = 0; w < tifNames.length; w++) {
						String t = tifNames[w];

						if (t.contains("/")) {
							t = StringUtils.substringAfterLast(t, "/");
						}

						t = imageFolderPath + "/" + t + ".tif";

						System.out.println(t);

						tifNamesOut[w] = t;
					}

					Dataset tifNamesDatasetOut = DatasetFactory.createFromObject(tifNamesOut);

					tifNamesArray[id] = tifNamesDatasetOut;

				}

				ILazyDataset ild = null;

				ild = dh1.getLazyDataset(gm.getImageName());

				if (ild == null) {
					ild = dh1.getLazyDataset("file_image");
				}

				if (ild == null) {
					ild = dh1.getLazyDataset("file");
				}

				if (ild == null) {
					imagesUnavailableWarning();
				}

				imageArray[id] = ild;
				// imageArray is an array of the images in read-in order

				int imageNoInDat = 0;
				// number of the image in the dat, i.e. the 10th image in the
				// dat

				for (int f = 0; f < (imageArray[id].getShape()[0]); f++) {

					SliceND slice2 = new SliceND(ild.getShape());
					slice2.setSlice(0, f, f + 1, 1);
					ILazyDataset nim = ild.getSliceView(slice2); // getSlice(slice2);
					som.put(imageRef, (ILazyDataset) nim);
					imageRefList.add(imageRef);
					imageNoInDatList.add(imageNoInDat);
					imagesToFilepathRef.add(id);
					imageRef++;
					imageNoInDat++;
				}

				ILazyDataset ildx = dh1.getLazyDataset(gm.getxName());
				SliceND slice1 = new SliceND(ildx.getShape());
				IDataset xdat = ildx.getSlice(slice1);
				xArray[id] = xdat;

				if (correctionSelection == MethodSetting.SXRD) {

					try {
						ILazyDataset ildl = dh1.getLazyDataset("l");
						SliceND slicel = new SliceND(ildl.getShape());
						lArray[id] = ildl.getSlice(slicel);

						ILazyDataset ildh = dh1.getLazyDataset("h");
						SliceND sliceh = new SliceND(ildl.getShape());
						hArray[id] = ildh.getSlice(sliceh);

						ILazyDataset ildk = dh1.getLazyDataset("k");
						SliceND slicek = new SliceND(ildk.getShape());
						kArray[id] = ildl.getSlice(slicek);
						
					} catch (Exception h) {

					}
				}

				if (drm.getCorrectionSelection().getCorrectionsNumber() > 0) {

					ILazyDataset ildtheta = dh1.getLazyDataset(gm.getxNameRef());

					SliceND slice2 = new SliceND(ildtheta.getShape());
					IDataset thetadat = ildtheta.getSlice(slice2);
					thetaArray[id] = thetadat;

//					dcdThetaArray[id] = dh1.getDataset(ReflectivityMetadataTitlesForDialog.getdcdtheta());
////lllll
					dcdThetaArray[id] = dh1.getDataset(ReflectivityAngleAliasEnum.THETA.getAngleAlias());
					
					qdcdArray[id] = dh1.getDataset(ReflectivityAngleAliasEnum.Q.getAngleAlias());
//							dh1.getDataset(ReflectivityMetadataTitlesForDialog.getqdcd());

					if (dcdThetaArray[id] == null) {
						try {
							dcdThetaArray[id] = dh1.getDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());
						} catch (Exception e2) {
							System.out.println("can't get dcdtheta");
						}
					} 

					if (qdcdArray[id] == null) {
						try {
							qdcdArray[id] = dh1.getDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());
						} catch (Exception e2) {
							System.out.println("can't get qdcd");
						}
					}
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
						IDataHolder dh2 = LoaderFactory.getData(filepaths[id]);
						ILazyDataset ild = dh2.getLazyDataset(gm.getImageName());
						// models.get(id).setDatImages(ild);
					}

					catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		Dataset tifNamesCon = DatasetFactory.zeros(1);

		AggregateDataset imageCon = null;

		try {
			imageCon = new AggregateDataset(false, imageArray);
		} catch (Exception j) {
			imageCon = new AggregateDataset(false, DatasetFactory.zeros(new int[] { 2, 2 }, Dataset.ARRAYFLOAT64));
		}

		try {
			xArrayCon = DatasetUtils.concatenate(xArray, 0);
			// xArrayCon is an unsorted, but concatenated DoubleDataset of l
			// values
			if (correctionSelection == MethodSetting.SXRD) {
				try {
					hArrayCon = DatasetUtils.concatenate(hArray, 0);
					kArrayCon = DatasetUtils.concatenate(kArray, 0);
					lArrayCon = DatasetUtils.concatenate(lArray, 0);
				} catch (Exception h) {
					System.out.println(h.getMessage());
				}
			}

			if (correctionSelection != MethodSetting.SXRD) {
				try {
					thetaArrayCon = DatasetUtils.concatenate(thetaArray, 0);
					xArrayCon = DatasetUtils.concatenate(xArray, 0);

					dcdThetaCon = DatasetUtils.concatenate(dcdThetaArray, 0);
					qdcdCon = DatasetUtils.concatenate(qdcdArray, 0);
				} catch (Exception g) {

				}
			}

			tifNamesCon = DatasetUtils.concatenate(tifNamesArray, 0);
			// tifNamesCon is an unsorted, but concatenated DoubleDataset of l
			// tif names
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}

		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());

		// imageRefDat is a dataset, equal to imageRefList (list of the integer
		// number of the image that is read in - the nth read in, for example),
		// and will be sorted based on the xArrayCon, which is the"l" values
		// (for a rod)

		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());

		// imagesToFilepathRefDat is a dataset, equal to imagesToFilepathRef
		// (list of the integer number of the dat (in String[] filepaths) of the
		// image that is read in at that point- the nth read in, for example),
		// and will be sorted based on the xArrayCon, which is the"l" values
		// (for a rod)

		for (int sd = 0; sd < imageRefList.size(); sd++) {
			imageRefDat.set(imageRefList.get(sd), sd);
			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
		}

		if (gm.isUseNegativeQ()) {

			thetaArrayCon = Maths.multiply(thetaArrayCon, -1);
			xArrayCon = Maths.multiply(xArrayCon, -1);

			int[] localShape = imageRefDat.getShape();

			SliceND pr = new SliceND(localShape);

			pr.flip();

			int[] localShape2 = imagesToFilepathRefDat.getShape();

			SliceND pr2 = new SliceND(localShape2);

			pr2.flip();

			imageRefDat = imageRefDat.getSlice(pr);
			imagesToFilepathRefDat = imagesToFilepathRefDat.getSlice(pr2);
		}

		Dataset xArrayConClone = xArrayCon.clone();

		DoubleDataset xArrayConCloneDouble = (DoubleDataset) xArrayConClone.clone();

		Dataset xArrayConCloneForh = xArrayCon.clone();
//		Dataset xArrayConCloneFork = xArrayCon.clone();
//		Dataset xArrayConCloneForl = xArrayCon.clone();

		try {
			DatasetUtils.sort(xArrayCon, imageRefDat);
			// so now we have the image number in imageArray (imageRefDat)
			// sorted by "l" value xArrayCon
			DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);
			// so now we have the dat number in filepaths
			// (imagesToFilepathRefDat) sorted by "l" value xArrayCon

			try {
				if (correctionSelection == MethodSetting.SXRD) {
					

					Dataset[] sortThese = new Dataset[]{hArrayCon, kArrayCon,  lArrayCon};
					SurfaceScatterPresenterUtilities.sortDatasets(xArrayConCloneForh, sortThese);
//					
//					DatasetUtils.sort(xArrayConCloneForh, hArrayCon);
//					DatasetUtils.sort(xArrayConCloneFork, kArrayCon);
//					DatasetUtils.sort(xArrayConCloneForl, lArrayCon);
				} else {
					
					Dataset[] sortThese = new Dataset[]{thetaArrayCon, qdcdCon, dcdThetaCon};
					SurfaceScatterPresenterUtilities.sortDatasets(xArrayConCloneForh, sortThese);
//					DatasetUtils.sort(xArrayConCloneForh, thetaArrayCon);
//					DatasetUtils.sort(xArrayConCloneFork, qdcdCon);
//					DatasetUtils.sort(xArrayConCloneForl, dcdThetaCon);
				}
			}

			catch (Exception h) {
				System.out.println(h.getMessage());
			}

			Dataset sortedTifNamesCon = sortStrings(xArrayConCloneDouble, tifNamesCon);
			// so now we have the tif names sorted by "l" value xArrayCon

			ILazyDataset[] imageSortedDat = new ILazyDataset[imageRefList.size()];
			// imageSortedDat this is the array of sorted images - sorted
			// according to "l"

			int[] filepathsSortedArray = new int[imageRefList.size()];
			// filepathsSortedArray is the .dat positions in filepaths - sorted
			// by images "l" values
			noImages = imageRefList.size();

			String[] datNamesInOrder = new String[imageRefList.size()];

			drm.setCorrectionSelection(correctionSelection);

			for (int f = 0; f < imageRefList.size(); f++) {

				datNamesInOrder[f] = filepaths[imagesToFilepathRefDat.getInt(f)];
				filepathsSortedArray[f] = imagesToFilepathRefDat.getInt(f);
				int pos = imageRefDat.getInt(f);
				imageSortedDat[f] = som.get(pos);

				FrameModel fm = new FrameModel();
				fms.add(fm);

				fm.setScannedVariable(xArrayCon.getDouble(f));
				fm.setRawImageData(imageSortedDat[f]);
				fm.setTifFilePath(sortedTifNamesCon.getString(f));
				fm.setDatFilePath(datNamesInOrder[f]);
				fm.setBackgroundMethodology(Methodology.TWOD);
				fm.setFitPower(FitPower.ONE);
				fm.setTrackingMethodology(TrackerType1.TLD);
				fm.setNoInOriginalDat(imageNoInDatList.get(pos));
				fm.setDatNo(imagesToFilepathRefDat.getInt(f));
				fm.setImageNumber(f);
				fm.setCorrectionSelection(correctionSelection);

				if (correctionSelection == MethodSetting.SXRD) {

					double polarisation = SXRDGeometricCorrections
							.polarisation(datNamesInOrder[f], gm.getInplanePolarisation(), gm.getOutplanePolarisation())
							.getDouble(imageNoInDatList.get(pos));

					fm.setPolarisationCorrection(polarisation);

					double lorentz = SXRDGeometricCorrections.lorentz(datNamesInOrder[f])
							.getDouble(imageNoInDatList.get(pos));

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
																			 .getDouble(imageNoInDatList.get(pos));

					fm.setAreaCorrection(areaCorrection);

					fm.setH(hArrayCon.getDouble(f));
					fm.setK(kArrayCon.getDouble(f));
					fm.setL(lArrayCon.getDouble(f));

				}

				else {

					drm.setSortedTheta(thetaArrayCon);

				}

				try {

					double angularFudgeFactor = gm.getAngularFudgeFactor();
					double beamHeight = gm.getBeamHeight();
					double footprint = gm.getFootprint();

					try{
						fm.setQdcd(qdcdCon.getDouble(f));
					}
					catch(IndexOutOfBoundsException e){
						
					}
					if (correctionSelection == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
							|| correctionSelection == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile) {

						double reflectivityAreaCorrection = GeometricCorrectionsReflectivityMethod
								.reflectivityCorrectionsBatchGaussianPofile(dcdThetaCon, fm.getNoInOriginalDat(),
										angularFudgeFactor, beamHeight, footprint);

						fm.setReflectivityAreaCorrection(reflectivityAreaCorrection);

					}

					else if (correctionSelection == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling
							|| correctionSelection == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

						double reflectivityAreaCorrection = GeometricCorrectionsReflectivityMethod
								.reflectivityCorrectionsBatchSimpleScaling(dcdThetaCon, fm.getNoInOriginalDat(),
										beamHeight, footprint);

						fm.setReflectivityAreaCorrection(reflectivityAreaCorrection);

					}
				} catch (Exception h) {
					System.out.println("problem in ref area corr:   " + h.getMessage());
					fluxCallibrationWarning();
					break;
				}

				try {
					if (correctionSelection == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
							|| correctionSelection == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {

						String externalFlux = gm.getFluxPath();

						if (externalFlux.isEmpty()) {
							externalFlux = null;
						}

						double reflectivityFluxCorrection = 0;
						
						if(gm.isUseInternalFlux()){

							reflectivityFluxCorrection = ReflectivityFluxCorrectionsForDialog
									.reflectivityFluxCorrectionsDouble(// fm.getDatFilePath(),
											xArrayCon.getDouble(f), filepaths);
							
						}
						else{
						
							reflectivityFluxCorrection = ReflectivityFluxCorrectionsForDialog
									.reflectivityFluxCorrectionsDouble(// fm.getDatFilePath(),
											qdcdCon.getDouble(f), externalFlux);
						}
						fm.setReflectivityFluxCorrection(reflectivityFluxCorrection);

						if (Double.isInfinite(reflectivityFluxCorrection)) {
							fluxCallibrationWarning();
							break;
						}
					}

				} catch (Exception i) {
					System.out.println("problem doing flux corr");
					fluxCallibrationWarning();
					break;

				}

				fm.setScannedVariable(xArrayCon.getDouble(f));

			}

			drm.setFilepathsSortedArray(filepathsSortedArray);
			drm.setImageNoInDatList(imageNoInDatList);

			drm.setSortedX(xArrayCon);

			SliceND slice2 = new SliceND(imageCon.getShape());
			slice2.setSlice(0, 0, 1, 1);

			updateAnalysisMethodology(0, 1, 0, "10");

		}

		catch (Exception e) {
			System.out.println(e.getMessage());
		}

		updateSvs = true;
	}

	public DirectoryModel getDrm() {
		return drm;
	}

	public void setDrm(DirectoryModel drm) {
		this.drm = drm;
	}

	public ArrayList<Double> getYList() {
		return drm.getOcdp().getyList();
	}

	public String getXName() {
		return gm.getxName();
	}

	public String getImageFolderPath() {
		return stm.getImageFolderPath();

	}

	public void setImageFolderPath(String ifp) {

		if (stm == null) {
			stm = new SetupModel();
		}

		stm.setImageFolderPath(ifp);
	}

	public void setSaveFolder(String sfp) {
		saveFolder = sfp;
	}

	public String getSaveFolder() {
		return saveFolder;
	}

	public MethodSetting getCorrectionSelection() {
		return drm.getCorrectionSelection();
	}

	public double getCurrentLorentzCorrection() {
		try {
			FrameModel f = fms.get(sliderPos);
			return f.getLorentzianCorrection();
		} catch (Exception i) {
			return 0.0;
		}

	}

	public double getCurrentPolarisationCorrection() {

		try {
			FrameModel f = fms.get(sliderPos);

			return f.getPolarisationCorrection();
		} catch (Exception i) {
			return 0.0;
		}

	}

	public double getCurrentAreaCorrection() {

		try {
			FrameModel f = fms.get(sliderPos);

			return f.getAreaCorrection();

		} catch (Exception i) {
			return 0.0;
		}

	}

	public double getCurrentRawIntensity() {

		return currentRawIntensity;
	}

	public void sliderMovemementMainImage(int sliderPos) {
		if (sliderPos != this.sliderPos) {
			this.sliderPos = (sliderPos);
			fireStateListeners();
		}
	}

	public TrackerType1 getTrackerType() {

		try {
			FrameModel f = fms.get(sliderPos);
			return f.getTrackingMethodology();
		} catch (Exception g) {
			return TrackerType1.TLD;
		}

	}

	public double getCurrentReflectivityFluxCorrection() {
		try {
			FrameModel f = fms.get(sliderPos);
			return f.getReflectivityFluxCorrection();
		} catch (Exception i) {
			return 0.0;
		}
	}

	public double getCurrentReflectivityAreaCorrection() {
		try {
			FrameModel f = fms.get(sliderPos);
			return f.getReflectivityAreaCorrection();
		} catch (Exception i) {
			return 0.0;
		}
	}

	public void setCurrentReflectivityFluxCorrection(double l) {
		try {
			FrameModel f = fms.get(sliderPos);
			f.setReflectivityFluxCorrection(l);
		} catch (Exception i) {
		}

	}

	public void setCurrentReflectivityAreaCorrection(double l) {
		try {
			FrameModel f = fms.get(sliderPos);
			f.setReflectivityAreaCorrection(l);
		} catch (Exception i) {
		}

	}

	public IDataHolder copiedDatWithCorrectedTifs(String fp, String datFolderPath) {

		String localFilepathCopy = StringUtils.substringBeforeLast(fp, ".dat") + "_copy";

		Path from = Paths.get(datFolderPath + File.separator + fp);

		Path to = Paths.get(saveFolder + File.separator + localFilepathCopy + ".dat");
		try {
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(to), charset);

			String firstTifName = StringUtils.substringBetween(content, "/", ".tif");

			if (firstTifName.contains("/")) {
				firstTifName = StringUtils.substringAfterLast(firstTifName, "/");
			}

			String pathNameToReplace = StringUtils.substringBetween(content, "\t", "/" + firstTifName);

			if (pathNameToReplace.contains("\t")) {
				pathNameToReplace = StringUtils.substringAfterLast(pathNameToReplace, "\t");
			}

			String imFolPath = stm.getImageFolderPath();
			// Dont mix linux and windows paths!

			imFolPath = imFolPath.replace("\\", "/");

			content = content.replaceAll(pathNameToReplace, imFolPath);

			Files.write(to, content.getBytes(charset));

			return dh1 = LoaderFactory.getData(to.toString());
		} catch (Exception n1) {
			return null;
		}

	}

	public IDataset getBackgroundImage(int selection) {

		if (drm.getFms().get(selection).getBackgroundSubtractedImage() != null) {

			return drm.getFms().get(selection).getBackgroundSubtractedImage().squeeze();
		}

		else {

			IDataset nullImage = DatasetFactory.zeros(new int[] { 2, 2 });
			Maths.add(nullImage, 0.1);

			return nullImage;
		}

	}

	public void addXValuesForFireAccept() {

		FrameModel f = fms.get(sliderPos);

		int jok = f.getDatNo();

		drm.addDmxList(jok, f.getNoInOriginalDat(), drm.getSortedX().getDouble(sliderPos));

		drm.addxList(fms.size(), sliderPos, drm.getSortedX().getDouble(sliderPos));

		int[] localPt = drm.getInitialLenPt()[1];
		int[] localLen = drm.getInitialLenPt()[0];

		double[] localLocation = new double[] { (double) (localPt[0]), (double) (localPt[1]),
				(double) (localPt[0] + localLen[0]), (double) (localPt[1]), (double) (localPt[0]),
				(double) (localPt[1] + localLen[1]), (double) (localPt[0] + localLen[0]),
				(double) (localPt[1] + localLen[1]) };

		drm.setTrackerCoordinates(localLocation);
		drm.addTrackerLocationList(sliderPos, localLocation);

		if (qConvert) {
			qConversion();
		}

	}

	public void saveParameters(String title) {

		FrameModel m = fms.get(sliderPos);

		int[][] lenPt = LocationLenPtConverterUtils.locationToLenPtConverter(m.getRoiLocation());

		FittingParametersOutput.FittingParametersOutputTest(title, lenPt[1][0], lenPt[1][1], lenPt[0][0], lenPt[0][1],
				m.getBackgroundMethdology(), m.getTrackingMethodology(), m.getFitPower(), m.getBoundaryBox(), sliderPos,
				this.getXValue(sliderPos), drm.getDatFilepaths()[drm.getFilepathsSortedArray()[sliderPos]],
				drm.getDatFilepaths());

	}

	@SuppressWarnings("unchecked")
	public FittingParameters loadParameters(String title) {

		String fileType = StringUtils.substringAfterLast(title, ".");

		FittingParameters fp = new FittingParameters();

		if (!fileType.equals("nxs")) {

			fp = FittingParametersInputReader.reader(title);

			for (FrameModel m : fms) {

				double[] location = LocationLenPtConverterUtils.lenPtToLocationConverter(fp.getLenpt());

				m.setRoiLocation(location);
				m.setTrackingMethodology(fp.getTracker());
				m.setFitPower(fp.getFitPower());
				m.setBoundaryBox(fp.getBoundaryBox());
				m.setBackgroundMethodology(fp.getBgMethod());
			}
		}

		else {

			try {
				dh1 = LoaderFactory.getData(title);
			} catch (Exception e) {

			}

			Tree tree = dh1.getTree();

			for (int n = 0; n < fms.size(); n++) {

				FrameModel m = fms.get(n);

				FittingParametersInputReader.readerFromNexus(tree, n, m);
			}

			fp = FittingParametersInputReader.fittingParametersFromFrameModel(fms.get(0));

			FittingParametersInputReader.geometricalParametersReaderFromNexus(tree, gm);

		}

		drm.setInitialLenPt(fp.getLenpt());

		try {
			sliderPos = (this.closestImageNo(fp.getXValue()));
		} catch (Exception h) {
			sliderPos = 0;
		}

		this.sliderMovemementMainImage(sliderPos);

		return fp;

	}

	public IDataset getImage(int k) {

		try {
			FrameModel frame = fms.get(k);

			SliceND slice = new SliceND(frame.getRawImageData().getShape());
			IDataset j = DatasetFactory.createFromObject(0);

			j = frame.getRawImageData().getSlice(slice).squeeze();
			return (IDataset) j;

		} catch (Exception e) {

			return DatasetFactory.zeros(new int[] { 2, 2 }, Dataset.ARRAYFLOAT64);
		}

	}

	public double[] regionOfInterestSetter1(int[][] lenPt) {

		if ((Arrays.equals(lenPt[0], getLenPt()[0]) == false) || (Arrays.equals(lenPt[1], getLenPt()[1]) == false)) {

			drm.setInitialLenPt(lenPt);

			try {
				fireStateListeners();
			} catch (Exception f) {

			}

		}

		int boundaryBox = 10;
		Methodology meth = Methodology.TWOD;

		try {
			boundaryBox = drm.getFms().get(sliderPos).getBoundaryBox();
			meth = drm.getFms().get(sliderPos).getBackgroundMethdology();
		} catch (Exception n) {

		}

		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, boundaryBox, meth);

		return bgRegionROI;

	}

	public ArrayList<ArrayList<double[]>> getLocationList() {
		return drm.getLocationList();
	}

	public double[] getThisLocation() {

		FrameModel f = fms.get(sliderPos);

		return f.getRoiLocation();

	}

	public double[] getThisLocation(int k) {

		FrameModel f = fms.get(k);

		return f.getRoiLocation();

	}

	public void backgroundBoxesManager(IRegion r1, IRegion r2, Button centreButton) {

		Display display = Display.getCurrent();
		Color magenta = display.getSystemColor(SWT.COLOR_DARK_MAGENTA);
		Color red = display.getSystemColor(SWT.COLOR_RED);

		FrameModel f = fms.get(sliderPos);

		if (f.getBackgroundMethdology() == Methodology.SECOND_BACKGROUND_BOX
				|| f.getBackgroundMethdology() == Methodology.OVERLAPPING_BACKGROUND_BOX) {

			r1.setVisible(false);
			r2.setVisible(true);
			r2.setUserRegion(true);
			r2.setLineWidth(1);
			r2.setMobile(true);
			r2.setFill(true);
			r2.setLineWidth(3);

			if (drm.getBackgroundLenPt() != null) {

				int[][] redLenPt = drm.getBackgroundLenPt();
				int[] redLen = redLenPt[0];
				int[] redPt = redLenPt[1];

				RectangularROI startROI = new RectangularROI(redPt[0], redPt[1], redLen[0], redLen[1], 0);

				r2.setROI(startROI);

			}

			r2.setRegionColor(magenta);

			centreButton.setEnabled(true);

			if (f.getBackgroundMethdology() == Methodology.OVERLAPPING_BACKGROUND_BOX) {

				if (drm.getBoxOffsetLenPt() != null) {

					int[][] newOffsetLenPt = drm.getBoxOffsetLenPt();
					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];

					int[] offsetLen = newOffsetLenPt[0];
					int[] offsetPt = newOffsetLenPt[1];

					int pt0 = pt[0] + offsetPt[0];
					int pt1 = pt[1] + offsetPt[1];

					int len0 = len[0] + offsetLen[0];
					int len1 = len[1] + offsetLen[1];

					drm.setBackgroundLenPt(new int[][] { { len0, len1 }, { pt0, pt1 } });
				}

				else {

					int[] len = drm.getInitialLenPt()[0];
					int[] pt = drm.getInitialLenPt()[1];

					int pt0 = pt[0] + 25;
					int pt1 = pt[1] + 25;

					int len0 = len[0] + 0;
					int len1 = len[1] + 0;

					IRectangularROI newROI = new RectangularROI(pt0, pt1, len0, len1, 0);

					r2.setROI(newROI);

					drm.setBackgroundLenPt(new int[][] { { pt0, pt1 }, { len0, len1 } });
				}

				r2.setRegionColor(red);
			}
		}

		else {

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

	public void addToInterpolatorRegions(IRegion region) {
		drm.addToInterpolatorRegions(region);
	}

	@SuppressWarnings("incomplete-switch")
	public ArrayList<double[][]> interpolationTrackerBoxesAccept(IRegion r2) {

		double[][] box = new double[3][];

		IRectangularROI r2ROI = r2.getROI().copy().getBounds();

		double[] lengths = new double[] { (double) r2ROI.getIntLengths()[0], (double) r2ROI.getIntLengths()[1] };
		double[] pts = new double[] { (double) r2ROI.getIntPoint()[0], r2ROI.getIntPoint()[1] };
		double[] xdata = new double[] { (double) sliderPos, (double) drm.getSortedX().getDouble(sliderPos) };

		box[0] = lengths;
		box[1] = pts;
		box[2] = xdata;

		drm.addToInterpolatorBoxes(box);

		ArrayList<double[][]> interpolatedLenPts = new ArrayList<>();

		Dataset xDataset = DatasetFactory.createFromObject(0);

		if (trackWithQ) {
			xDataset = drm.getSortedQ();
		}

		else {
			xDataset = drm.getSortedX();
		}

		if (drm.getInterpolatorBoxes().size() > 1) {

			interpolatedLenPts = InterpolationTracker.interpolatedTrackerLenPtArray(drm.getInterpolatorBoxes(),
					xDataset);

			if (drm.getInterpolatorBoxes().size() > 2 && getTrackerType() == TrackerType1.SPLINE_INTERPOLATION) {

				SplineInterpolationTracker split = new SplineInterpolationTracker();

				interpolatedLenPts = split.interpolatedTrackerLenPtArray1(drm.getInterpolatorBoxes(), xDataset);

			}

			int[] g = fms.get(0).getRawImageData().squeezeEnds().getShape();

			double[][] q = new double[][] { { (double) 0.0, (double) 0.0 }, { (double) 0.0, (double) 0.0 } };

			for (int r = 0; r < interpolatedLenPts.size(); r++) {

				double[][] p = interpolatedLenPts.get(r);

				if ((p[0][0] + p[1][0] + bB + 2) < g[1] && (p[0][1] + p[1][1] + bB + 2) < g[0]) {

					q = p;
				}

				if ((p[0][0] + p[1][0] + bB + 2) > g[1] || (p[0][1] + p[1][1] + bB + 2) > g[0]) {

					interpolatedLenPts.set(r, q);
				}
			}

			drm.setInterpolatedLenPts(interpolatedLenPts);

		}

		return interpolatedLenPts;
	}

	public ArrayList<double[][]> getInterpolatorBoxes() {
		return drm.getInterpolatorBoxes();
	}

	public void illuminateCorrectInterpolationBox() {

		if ((// getTrackerType() == TrackerType1.INTERPOLATION
				// ||
				getTrackerType() == TrackerType1.SPLINE_INTERPOLATION) && drm.getInterpolatorRegions() != null) {

			double u = (double) sliderPos;

			for (int j = 0; j < drm.getInterpolatorBoxes().size(); j++) {
				if (drm.getInterpolatorBoxes().get(j)[2][0] == u) {
					drm.getInterpolatorRegions().get(j).setFill(true);
				} else {
					drm.getInterpolatorRegions().get(j).setFill(false);
				}
			}
		}
	}

	public ArrayList<double[][]> getInterpolatedLenPts() {
		return drm.getInterpolatedLenPts();
	}

	public void illuminateCorrectInterpolationBox(int k) {

		if ((// getTrackerType() == TrackerType1.INTERPOLATION
				// ||
				getTrackerType() == TrackerType1.SPLINE_INTERPOLATION) && drm.getInterpolatorRegions() != null) {

			double u = (double) k;

			for (int j = 0; j < drm.getInterpolatorRegions().size(); j++) {
				if (drm.getInterpolatorBoxes().get(j)[2][0] == u) {
					drm.getInterpolatorRegions().get(j).setFill(true);
				} else {
					drm.getInterpolatorRegions().get(j).setFill(false);
				}
			}
		}
	}

	public AnalaysisMethodologies.Methodology getBackgroundSubtraction() {
		return fms.get(sliderPos).getBackgroundMethdology();
	}

	public void triggerBoxOffsetTransfer() {

		if (fms.get(sliderPos).getBackgroundMethdology() == Methodology.OVERLAPPING_BACKGROUND_BOX) {
			try {
				int[][] inspect = drm.getBoxOffsetLenPt();
				drm.setPermanentBoxOffsetLenPt(inspect);
			} catch (Exception j) {

			}
		}

		if (fms.get(sliderPos).getBackgroundMethdology() == Methodology.SECOND_BACKGROUND_BOX) {
			try {
				drm.setPermanentBackgroundLenPt(drm.getBackgroundLenPt());
			} catch (Exception j) {

			}
		}
	}

	public RectangularROI[] trackingRegionOfInterestSetter(int[][] lenPt) {

		int[] len = lenPt[0];
		int[] pt = lenPt[1];
		RectangularROI newGreenROI = new RectangularROI(pt[0], pt[1], len[0], len[1], 0);

		double[] bgRegionROI = new double[] { 0, 0, 0, 0, 0 };

		try {
			FrameModel f = fms.get(sliderPos);

			bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, f.getBoundaryBox(),
					f.getBackgroundMethdology());

		} catch (Exception n) {
			bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt, 10, Methodology.TWOD);

		}
		RectangularROI bgROI = new RectangularROI(bgRegionROI[0], bgRegionROI[1], bgRegionROI[2], bgRegionROI[3],
				bgRegionROI[4]);

		if (Arrays.equals(drm.getInitialLenPt()[0], lenPt[0]) == false
				|| Arrays.equals(drm.getInitialLenPt()[1], lenPt[1]) == false) {

			setLenPt(sliderPos, lenPt);
		}

		return new RectangularROI[] { newGreenROI, bgROI };

	}

	public Methodology getMethodology() {

		return fms.get(sliderPos).getBackgroundMethdology();

	}

	public RectangularROI generateOffsetBgROI(int[][] lenPt) {

		int[] len = lenPt[0];
		int[] pt = lenPt[1];

		int[] offsetLen = drm.getPermanentBoxOffsetLenPt()[0];
		int[] offsetPt = drm.getPermanentBoxOffsetLenPt()[1];

		int pt0 = pt[0] + offsetPt[0];
		int pt1 = pt[1] + offsetPt[1];

		int len0 = len[0] + offsetLen[0];
		int len1 = len[1] + offsetLen[1];

		RectangularROI offsetBgROI = new RectangularROI(pt0, pt1, len0, len1, 0);

		return offsetBgROI;

	}

	public void setInterpolatedLenPts(ArrayList<double[][]> intepolatedLenPts) {
		drm.setInterpolatedLenPts(intepolatedLenPts);
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
				// utilsLogger.error("Datasets are not of same type");
				break;
			}
			if (!ShapeUtils.areShapesCompatible(ashape, as[i].getShape(), axis)) {
				// utilsLogger.error("Datasets' shapes are not equal");
				break;
			}
			final int is = as[i].getElementsPerItem();
			if (isize < is)
				isize = is;
		}
		if (i < anum) {
			// utilsLogger.error("Dataset are not compatible");
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

		RectangularROI green = new RectangularROI(lenPt[1][0], lenPt[1][1], lenPt[0][0], lenPt[0][1], 0);

		if (Arrays.equals(drm.getInitialLenPt()[0], lenPt[0]) == false
				|| Arrays.equals(drm.getInitialLenPt()[1], lenPt[1]) == false) {

			drm.setInitialLenPt(lenPt);
		} // drm.setInitialLenPt(lenPt);

		double[] bgRegionROI = BoxSlicerRodScanUtilsForDialog.backgroundBoxForDisplay(lenPt,
				fms.get(sliderPos).getBoundaryBox(), fms.get(sliderPos).getBackgroundMethdology());

		RectangularROI bgROI = new RectangularROI(bgRegionROI[0], bgRegionROI[1], bgRegionROI[2], bgRegionROI[3],
				bgRegionROI[4]);

		return new RectangularROI[] { green, bgROI };

	}

	public int[][] getLenPt() {
		if (drm != null) {
			return drm.getInitialLenPt();
		} else {
			return new int[][] { { 0, 0 }, { 0, 0 } };
		}

	}

	public void setLenPt(int[][] lenPt) {

		if (!Arrays.equals(lenPt[0], getLenPt()[0]) || !Arrays.equals(lenPt[1], getLenPt()[1])) {

			drm.setInitialLenPt(lenPt);

			try {
				fireStateListeners();
			} catch (Exception f) {
			}
		}
	}

	public void setLenPt(int sliderpos, int[][] lenPt) {

		if (!Arrays.equals(lenPt[0], getLenPt()[0]) || !Arrays.equals(lenPt[1], getLenPt()[1])) {

			drm.setInitialLenPt(sliderpos, lenPt);

			try {
				fireStateListeners();
			} catch (Exception f) {
			}
		}
	}

	public void sliderZoomedArea(int sliderPos, IROI box, IPlottingSystem<Composite>... pS) {
		IDataset image = this.getImage(sliderPos);
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
	}

	public void resetCorrectionsSelection(int correctionSelection) {
		drm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
	}

	public int closestImageNo(double in) {
		int out = ClosestNoFinder.closestNoPos(in, drm.getSortedX());
		return out;
	}

	public double closestXValue(double in) {
		double out = ClosestNoFinder.closestNo(in, drm.getSortedX());
		return out;
	}

	public int closestImageIntegerInStack(double in) {
		int l = fms.size();
		int out = ClosestNoFinder.closestIntegerInStack(in, l);
		return out;
	}

	public IDataset getTemporaryBackground() {
		return drm.getTemporaryBackgroundHolder();
	}

	public ArrayList<IDataset> getBackgroundDatArray() {
		try {
			return drm.getBackgroundDatArray();
		} catch (Exception j) {
			return null;
		}
	}

	public double getXValue(int k) {

		if (drm != null) {
			if (drm.getSortedX() != null) {
				return drm.getSortedX().getDouble(k);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public double getXValue(double r) {

		int k = (int) Math.round(r);

		if (drm != null) {
			if (drm.getSortedX() != null) {
				return drm.getSortedX().getDouble(k);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public Dataset subImage(int sliderPos, IROI box) {
		IDataset image = this.getImage(sliderPos); // sm.getImages()[sliderPos];
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		return subImage;
	}

	public Dataset subImage(IDataset image, IROI box) {
		Dataset subImage = (Dataset) PlotSystem2DataSetter.PlotSystem2DataSetter1(box, image);
		return subImage;
	}

	public IDataset presenterDummyProcess(int selection, IDataset image, int trackingMarker, int[][] lenPt) {

		int j = fms.get(selection).getNoInOriginalDat();

		try {

			double[] lj = null;

			if (lenPt != null) {
				lj = LocationLenPtConverterUtils.lenPtToLocationConverter(lenPt);
			}

			IDataset output = DummyProcessWithFrames.DummyProcess(drm, gm,
					MethodSetting.toInt(drm.getCorrectionSelection()), j, trackingMarker, selection, lj, getLenPt());

			drm.addBackgroundDatArray(fms.size(), selection, output);

			return output;

		} catch (IllegalArgumentException s) {

			correctionMethodsWarning();

			return null;
		}
	}

	public void geometricParametersUpdate(
			// String fluxPath,
			double beamHeight, double footprint, double angularFudgeFactor, boolean beamCorrection, double beamInPlane,
			double beamOutPlane, double covar, double detectorSlits, double inPlaneSlits, double inplanePolarisation,
			double outPlaneSlits, double outplanePolarisation, double scalingFactor, double reflectivityA,
			double sampleSize, double normalisationFactor, double energy1, boolean specular, String imageName,
			String xNameRef, boolean useNegativeQ) {

		// gm.setFluxPath(fluxPath);
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
		gm.setxNameRef(xNameRef);
		gm.setEnergy(energy1);
		gm.setUseNegativeQ(useNegativeQ);

	}

	public ArrayList<ArrayList<IDataset>> xyArrayPreparer() {

		ArrayList<ArrayList<IDataset>> output = new ArrayList<>();

		ArrayList<IDataset> xArrayList = new ArrayList<>();
		ArrayList<IDataset> yArrayList = new ArrayList<>();
		ArrayList<IDataset> yArrayListFhkl = new ArrayList<>();
		ArrayList<IDataset> yArrayListError = new ArrayList<>();
		ArrayList<IDataset> yArrayListFhklError = new ArrayList<>();
		ArrayList<IDataset> yArrayListRaw = new ArrayList<>();
		ArrayList<IDataset> yArrayListRawError = new ArrayList<>();

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();
		csdpgfd.generateCsdpFromDrm(drm);
		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		xArrayList = csdpgfd.convert(csdp.getxIDataset());
		yArrayList = csdpgfd.convert(csdp.getyIDataset());
		yArrayListError = csdpgfd.convert(csdp.getyIDatasetError());
		yArrayListFhkl = csdpgfd.convert(csdp.getyIDatasetFhkl());
		yArrayListFhklError = csdpgfd.convert(csdp.getyIDatasetFhklError());
		yArrayListRaw = csdpgfd.convert(csdp.getyRawIDataset());
		yArrayListRawError = csdpgfd.convert(csdp.getyRawIDatasetError());

		output.add(0, xArrayList);
		output.add(1, yArrayList);
		output.add(2, yArrayListFhkl);
		output.add(3, yArrayListError);
		output.add(4, yArrayListFhklError);
		output.add(5, yArrayListRaw);
		output.add(6, yArrayListRawError);

		try {
			qConversion();
		} catch (Exception d) {
		}

		return output;
	}

	public int getNumberOfImages() {
		try {
			return fms.size();
		} catch (Exception h) {
			return 0;
		}
	}

	public int xPositionFinder(double myNum) {

		return ClosestNoFinder.closestNoPos(myNum, drm.getSortedX());

	}

	public int xPositionFinderInDat(int i, double myNum) {

		Dataset xIn = DatasetFactory.createFromList(drm.getDmxList().get(i));
		int d = ClosestNoFinder.closestNoPos(myNum, xIn);
		return d;

	}

	public int absoluteFrameNumberUsingXValueFromSpecifiedDatNo(int i, double myNum) {

		int d = xPositionFinderInDat(i, myNum);

		int e = 0;

		for (FrameModel fm : drm.getFms()) {
			if (fm.getDatNo() == i && fm.getNoInOriginalDat() == d) {
				e = fm.getImageNumber();
			}
		}

		return e;
	}

	public double xValueFinderInDat(int i, double myNum) {

		Dataset xIn = DatasetFactory.createFromList(drm.getDmxList().get(i));
		double f = ClosestNoFinder.closestNo(myNum, xIn);
		return f;

	}

	public double xValueFromDat(int i, double myNum) {

		Dataset xIn = DatasetFactory.createFromList(drm.getDmxList().get(i));
		double f = xIn.getDouble((int) myNum);
		return f;

	}

	public int qPositionFinder(double myNum) {

		int qPos = ClosestNoFinder.closestNoPos(myNum, drm.getSortedQ());

		return qPos;
	}

	public void updateAnalysisMethodology(int methodologySelection, int fitPowerSelection, int trackerSelection,
			String boundaryBox) {

		bB = Integer.valueOf(boundaryBox);

		for (FrameModel fm : fms) {

			if (methodologySelection != -1) {
				fm.setBackgroundMethodology(Methodology.values()[methodologySelection]);
			}
			if (fitPowerSelection != -1) {
				fm.setFitPower(AnalaysisMethodologies.toFitPower(fitPowerSelection));
			}
			if (trackerSelection != -1) {
				fm.setTrackingMethodology(TrackingMethodology.intToTracker1(trackerSelection));
			}

			double r = 0;

			try {
				r = Double.parseDouble(boundaryBox);
			} catch (Exception e1) {
				this.numberFormatWarning();
			}

			fm.setBoundaryBox((int) Math.round(r));
		}

		fireStateListeners();
	}

	public ArrayList<IRegion> getInterpolatorRegions() {
		return drm.getInterpolatorRegions();
	}

	public void setInterpolatorBoxes(ArrayList<double[][]> boxes) {
		drm.setInterpolatorBoxes(boxes);
	}

	public void setInterpolatorRegions(ArrayList<IRegion> boxes) {
		drm.setInterpolatorRegions(boxes);
	}

	public String[] getAnalysisSetup(int k) {

		String[] setup = new String[4];

		try {
			FrameModel f = fms.get(k);
			int jok = f.getDatNo();

			setup[0] = AnalaysisMethodologies.toString(f.getBackgroundMethdology());
			setup[1] = String.valueOf(AnalaysisMethodologies.toInt(f.getFitPower()));
			setup[2] = TrackingMethodology.toString(f.getTrackingMethodology());
			setup[3] = String.valueOf(f.getBoundaryBox());

		}

		catch (NullPointerException s) {

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

	public void writeNexus(String nexusFilePath) {

		RodObjectNexusBuilderModel rnbm = new RodObjectNexusBuilderModel(fms, nexusFilePath, gm, drm);

		RodObjectNexusUtils_Development.RodObjectNexusUtils(rnbm);

	}

	public IDataset getSplicedCurveX() {
		return drm.getCsdp().getSplicedCurveX();
	}

	public IDataset getSplicedCurveY() {
		return drm.getCsdp().getSplicedCurveY();
	}

	public IDataset getSplicedCurveYFhkl() {
		return drm.getCsdp().getSplicedCurveYFhkl();
	}

	public IDataset getSplicedCurveYRaw() {
		return drm.getCsdp().getSplicedCurveYRaw();
	}

	public void setTrackerOn(Boolean trackerOn) {
		drm.setTrackerOn(trackerOn);
	}

	public Boolean getTrackerOn() {
		try {
			return drm.isTrackerOn();
		} catch (NullPointerException g) {
			return false;
		}
	}

	public void anarodSave(String title) {

		try {
			File file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);

		if (drm.getCorrectionSelection() == MethodSetting.SXRD) {

			writer.println("# Test file created: " + strDate);
			writer.println("# Headers: ");
			writer.println("#h	k	l	F	Fe");

			for (int gh = 0; gh < fms.size(); gh++) {
				FrameModel f = fms.get(gh);

				writer.println(f.getH() + "	" + f.getK() + "	" + f.getL() + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getError(gh));
			}
		}

		else {

			if (Double.isFinite(fms.get(0).getQ())) {

				writer.println("# Test file created: " + strDate);
				writer.println("# Headers: ");
				writer.println("#qdcd	I	Ie");

				for (int gh = 0; gh < fms.size(); gh++) {

					writer.println(fms.get(gh).getQ() + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
							+ drm.getCsdp().getSplicedCurveY().getError(gh));
				}

			}
			//
			else {
				writer.println("#" + gm.getxName() + "	I	Ie");

				for (int gh = 0; gh < fms.size(); gh++) {

					writer.println(drm.getxList().get(gh) + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh)
							+ "	" + drm.getCsdp().getSplicedCurveY().getError(gh));
				}

			}
			//
		}

		writer.close();
	}

	public void intSave(String title) {

		File file = null;

		try {
			file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int index = title.lastIndexOf(".");

		if (index != -1) {
			String ext = title.substring(0, index);
			file.renameTo(new File(ext + ".int"));
		} else {
			file.renameTo(new File(title + ".int"));
		}

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);

		if (drm.getCorrectionSelection() == MethodSetting.SXRD) {

			writer.println("# Test file created: " + strDate);
			writer.println("# Headers: ");
			writer.println("#h	k	l	F	Fe	lorentz	correction 	polarisation correction		area correction");

			for (int gh = 0; gh < fms.size(); gh++) {
				FrameModel f = fms.get(gh);

				writer.println(f.getH() + "	" + f.getK() + "	" + f.getL() + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getError(gh) + "	" + f.getLorentzianCorrection() + "	"
						+ f.getPolarisationCorrection() + "	" + f.getAreaCorrection());
			}
		} else {

			writer.println("# Test file created: " + strDate);
			writer.println("# Headers: ");

			if (Double.isFinite(fms.get(0).getQdcd())) {

				writer.println("#qdcd	I	Ie	Area Correction	Flux Correction");

				if (drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
						|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling) {
					//
					for (int gh = 0; gh < fms.size(); gh++) {

						FrameModel fm = fms.get(gh);

						writer.println(fm.getQdcd() + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
								+ drm.getCsdp().getSplicedCurveY().getError(gh) + "	"
								+ fm.getReflectivityAreaCorrection() + "	" + fm.getReflectivityFluxCorrection());
					}
				}

				if (drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
						|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

					for (int gh = 0; gh < fms.size(); gh++) {

						FrameModel fm = fms.get(gh);

						writer.println(fm.getQdcd() + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
								+ drm.getCsdp().getSplicedCurveY().getError(gh) + "	"
								+ fm.getReflectivityAreaCorrection());
					}
				}

				if (drm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction) {

					for (int gh = 0; gh < fms.size(); gh++) {

						FrameModel fm = fms.get(gh);

						writer.println(fm.getQdcd() + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
								+ drm.getCsdp().getSplicedCurveY().getError(gh));
					}
				}

			}
		}

		writer.close();
	}

	public void writeOutReflectivityDat() {
		//
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);

		writer.println("# Test file created: " + strDate);
		writer.println("# Headers: ");
		writer.println("#qdcd	I	Ie");

		for (int gh = 0; gh < fms.size(); gh++) {

			FrameModel fm = fms.get(gh);

			writer.println(fm.getQdcd() + "	" + drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
					+ drm.getCsdp().getSplicedCurveY().getError(gh));
		}
	}

	public void simpleXYYeSave(String title, int state) {

		File file = null;

		try {
			file = new File(title);
			file.createNewFile();
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int index = title.lastIndexOf(".");
		if (index != -1) {
			String ext = title.substring(0, index);
			file.renameTo(new File(ext + ".int"));
		} else {
			file.renameTo(new File(title + ".int"));
		}

		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);

		writer.println("# Test file created: " + strDate);
		writer.println("# Headers: ");
		writer.println("#X	Y	Ye");

		if (state == 1) {
			for (int gh = 0; gh < fms.size(); gh++) {
				writer.println(drm.getCsdp().getSplicedCurveX().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYFhkl().getError(gh));
			}
		}

		if (state == 0) {
			for (int gh = 0; gh < fms.size(); gh++) {
				writer.println(drm.getCsdp().getSplicedCurveX().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveY().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveY().getError(gh));
			}
		}

		if (state == 2) {
			for (int gh = 0; gh < fms.size(); gh++) {
				writer.println(drm.getCsdp().getSplicedCurveX().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYRaw().getDouble(gh) + "	"
						+ drm.getCsdp().getSplicedCurveYRaw().getError(gh));
			}
		}

		writer.close();
	}

	public void setSplicedCurveX(IDataset xData) {
		drm.getCsdp().setSplicedCurveX(xData);
	}

	//
	public void setSplicedCurveY(IDataset yData) {
		drm.getCsdp().setSplicedCurveY(yData);
	}

	public IROI getROI() {

		double[] l = fms.get(sliderPos).getRoiLocation();

		int[][] lenpt = LocationLenPtConverterUtils.locationToLenPtConverter(l);

		int[] len = lenpt[0];
		int[] pt = lenpt[1];

		RectangularROI bgROI = new RectangularROI(pt[0], pt[1], len[0], len[1], 0);

		return bgROI;
	}

	public int[][] getPermanentBoxOffsetLenPt() {
		return drm.getPermanentBoxOffsetLenPt();
	}

	public void setPermanentBoxOffsetLenPt(int[][] l) {
		drm.setPermanentBoxOffsetLenPt(l);
	}

	public int[][] getBoxOffsetLenPt() {
		return drm.getBoxOffsetLenPt();
	}

	public void setBoxOffsetLenPt(int[][] l) {
		drm.setBoxOffsetLenPt(l);
	}

	public int getSliderPos() {

		return sliderPos;
	}

	public void boundariesWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 0, null);
		roobw.open();
	}

	public void fluxCallibrationWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 6, null);
		roobw.open();
	}

	public void outOfMemoryWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 5, null);
		roobw.open();
	}

	public void numberFormatWarning(String note) {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 1, note);
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

	public void boundariesWarning(String note, Display d) {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 0, note);
		roobw.open();

	}

	public void numberFormatWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 1, null);
		roobw.open();

		roobw.getOverride().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return;

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return;
	}

	public void correctionMethodsWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 2, null);

		roobw.open();

		roobw.getOverride().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return;

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return;
	}

	public void imagesUnavailableWarning() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 3, null);
		roobw.open();

		roobw.getOverride().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return;

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return;
	}

	public void dialogToChangeImageName() {
		RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(parentShell, 3, null);
		roobw.open();

		roobw.getOverride().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				roobw.close();
				return;

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return;
	}

	public void dialogToChangeImageFolder(Boolean t, DatDisplayer dd) {

		ImageFolderChangeDialog ifcd = new ImageFolderChangeDialog(parentShell, this, t, dd);
		ifcd.open();

		return;
	}

	public IDataset[] curveStitchingOutput() {

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();

		IDataset[] output = CurveStitchWithErrorsAndFrames.curveStitch4(csdpgfd.generateCsdpFromDrm(drm), null);

		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		try {
			qConversion();
		} catch (Exception d) {
		}

		drm.setCsdp(csdp);

		return output;
	}

	public CurveStitchDataPackage curveStitchingOutput(double[][] mm, boolean accept,
			ArrayList<OverlapAttenuationObject> oAos) {

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();

		csdpgfd.generateCsdpFromDrm(drm);

		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		CurveStitchWithErrorsAndFrames.curveStitch4(csdp, mm, oAos);

		if (drm.getCorrectionSelection() == MethodSetting.Reflectivity_NO_Correction
				|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile
				|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile
				|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling
				|| drm.getCorrectionSelection() == MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling) {

			ReflectivityNormalisation.reflectivityNormalisation1(csdp);

		}

		if (accept) {
			drm.setCsdp(csdp);
		}
		try {
			qConversion();
		} catch (Exception d) {
		}

		return csdp;
	}

	public void switchFhklIntensity(IPlottingSystem<Composite> pS, String selectorName, boolean qAxis) {

		AxisEnums.yAxes selector = AxisEnums.yAxes.SPLICEDY;

		for (AxisEnums.yAxes yA : AxisEnums.yAxes.values()) {
			if (yA.getYAxisName().equals(selectorName)) {
				selector = yA;
			}
		}

		pS.clear();

		ILineTrace lt = pS.createLineTrace("Corrected Intensity Curve");

		pS.addTrace(lt);

		Display display = Display.getCurrent();

		IDataset x = DatasetFactory.zeros(new int[] { 2, 2 }, Dataset.ARRAYFLOAT64);

		if (qAxis) {
			x = getSplicedCurveQ();
		} else {
			x = getSplicedCurveX();
		}

		switch (selector) {
		case SPLICEDY:

			lt.setData(x, drm.getCsdp().getSplicedCurveY());

			Color blue = display.getSystemColor(SWT.COLOR_BLUE);

			lt.setTraceColor(blue);
			break;

		case SPLICEDYFHKL:

			lt.setName("Fhkl Curve");

			lt.setData(x, drm.getCsdp().getSplicedCurveYFhkl());

			Color green = display.getSystemColor(SWT.COLOR_GREEN);

			lt.setTraceColor(green);
			break;

		case SPLICEDYRAW:

			lt.setName("Raw Intensity Curve");

			lt.setData(x, drm.getCsdp().getSplicedCurveYRaw());

			Color black = display.getSystemColor(SWT.COLOR_BLACK);

			lt.setTraceColor(black);
			break;

		default:
			//
		}

		lt.setErrorBarEnabled(errorDisplayFlag);

		Color red = display.getSystemColor(SWT.COLOR_RED);

		lt.setErrorBarColor(red);

		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

	}

	public void switchFhklIntensityUnStitchedCurves(IPlottingSystem<Composite> pS, int selector, boolean qAxis) {

		pS.clear();

		ILineTrace lt = pS.createLineTrace("Corrected Intensity Curve");

		Display display = Display.getCurrent();

		IDataset x = DatasetFactory.zeros(new int[] { 2, 2 }, Dataset.ARRAYFLOAT64);

		if (qAxis) {
			x = getSplicedCurveQ();
		} else {
			x = getSplicedCurveX();
		}

		if (selector == 0) {

			lt.setData(x, drm.getCsdp().getSplicedCurveY());

			Color blue = display.getSystemColor(SWT.COLOR_BLUE);

			lt.setTraceColor(blue);
		}

		if (selector == 1) {

			lt.setName("Fhkl Curve");

			lt.setData(x, drm.getCsdp().getSplicedCurveYFhkl());

			Color green = display.getSystemColor(SWT.COLOR_GREEN);

			lt.setTraceColor(green);
		}

		if (selector == 2) {

			lt.setName("Raw Intensity Curve");

			lt.setData(x, drm.getCsdp().getSplicedCurveYRaw());

			Color black = display.getSystemColor(SWT.COLOR_BLACK);

			lt.setTraceColor(black);
		}

		lt.setErrorBarEnabled(errorDisplayFlag);

		Color red = display.getSystemColor(SWT.COLOR_RED);

		lt.setErrorBarColor(red);

		pS.addTrace(lt);
		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

	}

	public void setCorrectionSelection(int correctionSelection) {
		drm.setCorrectionSelection(MethodSetting.toMethod(correctionSelection));
	}

	public void setRodName(String in) {
		drm.setRodName(in);
	}

	public String getRodName() {
		return drm.getRodName();
	}

	public void setSelection(int selection) {
		sliderPos = selection;
		// sm.setSelection(selection);
	}

	public void setSliderPos(int selection) {
		// sm.setSliderPos(selection);
		sliderPos = selection;
		fireStateListeners();
	}

	public int[][] getBackgroundLenPt() {
		return drm.getBackgroundLenPt();
	}

	public void setBackgroundLenPt(int[][] l) {
		drm.setBackgroundLenPt(l);
	}

	public int[][] getInitialLenPt() {
		return drm.getInitialLenPt();
	}

	public void resetTrackers() {
		drm.resetTrackers();
	}

	public IDataset returnNullImage() {

		IDataset output = this.getImage(0);

		return output;

	}

	public void resetSmOutputObjects() {
		drm.resetAll();
	}

	public double[] trackerInterpolationInterpolator1(int k) {

		double[] test = new double[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		double myNum = drm.getSortedX().getDouble(k);
		double distance = Math.abs(drm.getSortedX().getDouble(0) - myNum);
		int nearestCompletedDatFileNo = 0;

		for (int c = 0; c < drm.getSortedX().getSize(); c++) {
			FrameModel fm = fms.get(c);
			double cdistance = fm.getScannedVariable() - myNum;
			if ((cdistance < distance) & !Arrays.equals(fm.getRoiLocation(), test)
					& !Arrays.equals(fm.getRoiLocation(), null)) {

				nearestCompletedDatFileNo = fm.getDatNo();
				distance = cdistance;
			}
		}

		ArrayList<double[]> seedList = drm.getLocationList().get(nearestCompletedDatFileNo);
		ArrayList<Double> lList = drm.getDmxList().get(nearestCompletedDatFileNo);

		Dataset yValues = DatasetFactory.zeros(seedList.size());
		Dataset xValues = DatasetFactory.zeros(seedList.size());
		Dataset lValues = DatasetFactory.zeros(seedList.size());

		for (int op = 0; op < seedList.size(); op++) {

			double x = seedList.get(op)[1];
			double y = seedList.get(op)[0];
			double l = lList.get(op);

			xValues.set(x, op);
			yValues.set(y, op);
			lValues.set(l, op);

		}

		double[] seedLocation = PolynomialOverlap.extrapolatedLocation(drm.getSortedX().getDouble(k), lValues, xValues,
				yValues, drm.getInitialLenPt()[0], 1);

		debug("!!!!!!!!!!!!!!!     }}}}}{{{{{{{{crude  seedlocation for Tracker[0] : " + seedLocation[0] + " + "
				+ "seedlocation[1] :" + seedLocation[1]);

		return seedLocation;
	}

	public IDataset returnSubNullImage() {

		RectangularROI startROI = new RectangularROI(100, 100, 50, 50, 0);
		IROI box = startROI.getBounds().bounds(startROI);
		IDataset subImage = PlotSystem2DataSetter.PlotSystem2DataSetter1(box, this.returnNullImage());

		return subImage;
	}

	public IDataset getSplicedCurveQ() {
		return drm.getSplicedCurveQ();
	}

	public void qConversion() {
		drm.qConversion(gm.getEnergy(), gm.getTheta());
	}

	public void setNexusPath(String np) {

		if (stm == null) {
			stm = new SetupModel();
		}
		stm.setNexusPath(np);
	}

	public String getNexusPath() {
		return stm.getNexusPath();
	}

	public void stitchAndPresent(MultipleOutputCurvesTableView outputCurves) {

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();

		IDataset[] output = CurveStitchWithErrorsAndFrames.curveStitch4(csdpgfd.generateCsdpFromDrm(drm), null);

		ILineTrace lt = pS.createLineTrace("progress");

		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		drm.setCsdp(csdp);

		try {
			qConversion();
		} catch (Exception d) {
		}

		lt.setData(drm.getCsdp().getSplicedCurveX(), drm.getCsdp().getSplicedCurveY());

		pS.clear();
		pS.addTrace(lt);

		pS.repaint();
		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

		lt.setErrorBarEnabled(false);

	}

	public void stitchAndPresentWithFrames(MultipleOutputCurvesTableView outputCurves, AxisEnums.yAxes ids) {

		Display display = Display.getCurrent();

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();

		IDataset[] output = CurveStitchWithErrorsAndFrames.curveStitch4(csdpgfd.generateCsdpFromDrm(drm), null);

		ILineTrace lt = pS.createLineTrace("progress");

		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		IDataset X = DatasetFactory.createFromObject(csdpgfd.getCsdp().getSplicedCurveX());

		if (outputCurves.getqAxis().getSelection()) {

			qConversion();
			X = csdp.getSplicedCurveQ();
		}

		else {
			X = csdp.getSplicedCurveX();
		}

		if (ids == null) {

			lt.setData(X, csdp.getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);
		}

		else if (ids == yAxes.SPLICEDY) {

			lt.setData(X, csdp.getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);

		} else if (ids == yAxes.SPLICEDYFHKL) {

			lt.setData(X, csdp.getSplicedCurveYFhkl());
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
			lt.setTraceColor(green);
		} else if (ids == yAxes.SPLICEDYRAW) {

			lt.setData(X, csdp.getSplicedCurveYRaw());
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			lt.setTraceColor(black);

		}

		pS.clear();
		pS.addTrace(lt);

		pS.repaint();
		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

		lt.setErrorBarEnabled(false);

		drm.setCsdp(csdp);

	}

	public void stitchAndPresent1(MultipleOutputCurvesTableView outputCurves, AxisEnums.yAxes ids) {

		Display display = Display.getCurrent();

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();

		CsdpGeneratorFromDrm csdpgfd = new CsdpGeneratorFromDrm();

		csdpgfd.generateCsdpFromDrm(drm);

		CurveStitchDataPackage csdp = csdpgfd.getCsdp();

		IDataset[] output1 = CurveStitchWithErrorsAndFrames.curveStitch4(csdp, null);

		drm.setCsdp(csdp);

		ILineTrace lt = pS.createLineTrace("progress");

		IDataset X = DatasetFactory.createFromObject(csdp.getSplicedCurveX());

		if (outputCurves.getqAxis().getSelection()) {

			qConversion();
			X = drm.getCsdp().getSplicedCurveQ();
		}

		else {
			X = csdp.getSplicedCurveX();
		}

		if (ids == null) {

			lt.setData(X, drm.getCsdp().getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);
		}

		else if (ids == yAxes.SPLICEDY) {

			lt.setData(X, drm.getCsdp().getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);

		} else if (ids == yAxes.SPLICEDYFHKL) {

			lt.setData(X, drm.getCsdp().getSplicedCurveYFhkl());
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
			lt.setTraceColor(green);
		} else if (ids == yAxes.SPLICEDYRAW) {

			lt.setData(X, drm.getCsdp().getSplicedCurveYRaw());
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			lt.setTraceColor(black);

		}

		pS.clear();
		pS.addTrace(lt);

		pS.repaint();
		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

		lt.setErrorBarEnabled(false);

	}

	public void stitchAndPresentFromCsdp(MultipleOutputCurvesTableView outputCurves, AxisEnums.yAxes ids,
			CurveStitchDataPackage csdp) {

		Display display = Display.getCurrent();

		outputCurves.resetCurve();

		IPlottingSystem<Composite> pS = outputCurves.getPlotSystem();

		drm.setCsdp(csdp);

		ILineTrace lt = pS.createLineTrace("progress");

		IDataset X = DatasetFactory.createFromObject(csdp.getSplicedCurveX());

		if (outputCurves.getqAxis().getSelection()) {

			qConversion();
			X = drm.getCsdp().getSplicedCurveQ();
		}

		else {
			X = csdp.getSplicedCurveX();
		}

		if (ids == null) {

			lt.setData(X, drm.getCsdp().getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);
		}

		else if (ids == yAxes.SPLICEDY) {

			lt.setData(X, drm.getCsdp().getSplicedCurveY());
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			lt.setTraceColor(blue);

		} else if (ids == yAxes.SPLICEDYFHKL) {

			lt.setData(X, drm.getCsdp().getSplicedCurveYFhkl());
			Color green = display.getSystemColor(SWT.COLOR_GREEN);
			lt.setTraceColor(green);
		} else if (ids == yAxes.SPLICEDYRAW) {

			lt.setData(X, drm.getCsdp().getSplicedCurveYRaw());
			Color black = display.getSystemColor(SWT.COLOR_BLACK);
			lt.setTraceColor(black);

		}

		pS.clear();
		pS.addTrace(lt);

		pS.repaint();
		pS.autoscaleAxes();

		double start = lt.getXData().getDouble(0);
		double end = lt.getXData().getDouble(lt.getXData().getShape()[0] - 1);
		double range = end - start;

		pS.getAxes().get(0).setRange((start - 0.1 * range), (end) + 0.1 * range);

		lt.setErrorBarEnabled(false);

	}

	public void switchErrorDisplay() {
		if (errorDisplayFlag == true) {
			errorDisplayFlag = (false);
		} else {
			errorDisplayFlag = (true);
		}
	}

	public boolean getErrorFlag() {
		return errorDisplayFlag;
	}

	public void setErrorFlag(boolean n) {
		errorDisplayFlag = n;
	}

	public void geometricParametersWindowPopulate() {

	}

	private void debug(String output) {
		if (DEBUG == 1) {
			System.out.println(output);
		}
	}

	public GeometricParametersModel getGm() {
		if (gm == null) {
			gm = new GeometricParametersModel();
		}

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
		gm.setEnergy(energy);
	}

	public void setTheta(int theta) {

		gm.setTheta(theta);
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

	public ProccessingMethod getProcessingMethodSelection() {
		return processingMethodSelection;
	}

	public void setProcessingMethodSelection(ProcessingMethodsEnum.ProccessingMethod processingMethodSelection) {

		this.processingMethodSelection = (processingMethodSelection);

		for (FrameModel f : fms) {
			f.setProcessingMethodSelection(processingMethodSelection);
		}
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

	public void addStateListener(IPresenterStateChangeEventListener listener) {
		listeners.add(listener);
	}

	private void fireStateListeners() {
		for (IPresenterStateChangeEventListener l : listeners)
			l.update();
	}

	public void createGm() {
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

		double[] positionsInB = new double[l];

		for (int r = 0; r < l; r++) {
			positionsInB[r] = r;
		}

		MathArrays.sortInPlace(s.getData(), positionsInB);

		String[] sortedB = new String[l];

		for (int r = 0; r < l; r++) {
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

	public SetupModel getStm() {
		return stm;
	}

	public void setStm(SetupModel stm) {
		this.stm = stm;
	}

	public boolean isErrorDisplayFlag() {
		return errorDisplayFlag;
	}

	public void setErrorDisplayFlag(boolean errorDisplayFlag) {
		this.errorDisplayFlag = errorDisplayFlag;
	}

	public void setCurrentRawIntensity(double currentRawIntensity) {
		this.currentRawIntensity = currentRawIntensity;
	}

	public boolean isTrackWithQ() {
		return trackWithQ;
	}

	public void setTrackWithQ(boolean trackWithQ) {
		if (trackWithQ) {
			qConversion();
		}
		this.trackWithQ = trackWithQ;
	}

	public int getbB() {
		return bB;
	}

	public void setbB(int bB) {
		this.bB = bB;
	}

	public String[] getDatFilepaths() {
		return drm.getDatFilepaths();
	}

	public double getUnsplicedCorrectedIntensityFromFm(int datNo, int pos) {

		return drm.getOcdp().getyListForEachDat().get(datNo).get(pos);

	}

	public double getUnsplicedRawIntensityFromFm(int datNo, int pos) {

		return drm.getOcdp().getyListRawIntensityErrorForEachDat().get(datNo).get(pos);

	}

	public double getUnsplicedFhklIntensityFromFm(int datNo, int pos) {
		return drm.getOcdp().getyListFhklErrorForEachDat().get(datNo).get(pos);

	}

	public void setGoodPoint(int i, boolean u) {
		drm.getFms().get(i).setGoodPoint(u);
	}

	public boolean isGoodPoint(int i) {
		return drm.getFms().get(i).isGoodPoint();
	}

	public void allGoodPoints() {
		for (FrameModel fm : drm.getFms()) {
			fm.setGoodPoint(true);
		}
	}

	public void flipGoodPoint(int i) {
		if (drm.getFms().get(i).isGoodPoint()) {
			drm.getFms().get(i).setGoodPoint(false);
		} else {
			drm.getFms().get(i).setGoodPoint(true);
		}
	}

	public boolean areAllPointsGood() {
		boolean output = true;

		for (FrameModel fm : fms) {
			if (!fm.isGoodPoint()) {
				output = false;
			}
		}

		return output;
	}

	public void writeOutAngleAliases(EnumMap<SXRDAngleAliasEnum, String> sXRDMap,
			EnumMap<ReflectivityAngleAliasEnum, String> reflectivityMap,
			EnumMap<ReflectivityFluxParametersAliasEnum, String> reflectivityFluxMap) {

		gm.setsXRDMap(sXRDMap);
		gm.setReflectivityFluxMap(reflectivityFluxMap);
		gm.setReflectivityFluxMap(reflectivityFluxMap);

	}

	public void writeFluxFilePathToGeometricModel(String f, boolean useInternalFlux) {
		gm.setFluxPath(f);
		gm.setUseInternalFlux(useInternalFlux);
	}

	public void arbitrarySavingMethod(boolean useQ, boolean writeOnlyGoodPoints, Shell shell, SaveFormatSetting sfs,
			String rodSaveName, CurveStitchDataPackage csdpToSave, AxisEnums.yAxes yAxis
			// AxisEnums.xAxes xAxis
			) {

		FileDialog fd = new FileDialog(shell, SWT.SAVE);

		if (this.getSaveFolder() != null) {
			fd.setFilterPath(this.getSaveFolder());
		}

		String stitle = "r";
		String path = "p";

		if (fd.open() != null) {
			stitle = fd.getFileName();
			path = fd.getFilterPath();

		}

		if (this.getSaveFolder() == null) {
			this.setSaveFolder(path);
			;
		}

		String title = path + File.separator + stitle;

		SavingUtils su = new SavingUtils();

		int saveIntensityState = yAxis.getYAxisNumber();

		if (sfs == SaveFormatSetting.GenX) {
			su.genXSave(writeOnlyGoodPoints, title, csdpToSave, this.getDrm(), this.getDrm().getFms(), this.getGm());
		}
		if (sfs == SaveFormatSetting.Anarod) {
			su.anarodSave(writeOnlyGoodPoints, title, csdpToSave, this.getDrm(), this.getDrm().getFms(), this.getGm());
		}
		if (sfs == SaveFormatSetting.int_format) {
			su.intSave(writeOnlyGoodPoints, title, csdpToSave, this.getDrm(), this.getDrm().getFms(), this.getGm());
		}
		if (sfs == SaveFormatSetting.ASCII) {
			su.simpleXYYeSave(useQ, writeOnlyGoodPoints, title, saveIntensityState, csdpToSave, this.getDrm().getFms());
		}

	}

	public void disregardNegativeIntensities() {

		CurveStitchDataPackage csdp = drm.getCsdp();

		int noDats = csdp.getyIDataset().length;

		for (int r = 0; r < noDats; r++) {
			int datLength = csdp.getyIDataset()[r].getSize();

			for (int s = 0; s < datLength; s++) {
				if (csdp.getyIDataset()[r].getDouble(s) < 0) {
					getFrameModel(r, s).setGoodPoint(false);
					;
				}
			}
		}
	}

	public boolean isUpdateSvs() {
		return updateSvs;
	}

	public void setUpdateSvs(boolean updateSvs) {
		this.updateSvs = updateSvs;
	}

	public FrameModel getFrameModel(int datNo, int noInOriginalDat) {

		for (FrameModel fm : fms) {
			if (fm.getDatNo() == datNo && fm.getNoInOriginalDat() == noInOriginalDat) {

				return fm;
			}
		}

		return null;
	}

}
