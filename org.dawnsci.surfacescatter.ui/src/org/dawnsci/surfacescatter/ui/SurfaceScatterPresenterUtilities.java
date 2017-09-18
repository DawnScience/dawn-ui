//package org.dawnsci.surfacescatter.ui;
//
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//import java.io.File;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//
//import org.apache.commons.lang.StringUtils;
//import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
//import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
//import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
//import org.eclipse.dawnsci.analysis.api.tree.DataNode;
//import org.eclipse.dawnsci.nexus.NexusFile;
//import org.eclipse.january.dataset.AggregateDataset;
//import org.eclipse.january.dataset.Dataset;
//import org.eclipse.january.dataset.DatasetFactory;
//import org.eclipse.january.dataset.DatasetUtils;
//import org.eclipse.january.dataset.DoubleDataset;
//import org.eclipse.january.dataset.IDataset;
//import org.eclipse.january.dataset.ILazyDataset;
//import org.eclipse.january.dataset.SliceND;
//
//import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
//import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
//
//public class SurfaceScatterPresenterUtilities {
//
//	private IDataHolder dh1;
//	
//	
//	public void dataLoaderFromDat(String[] filepaths,
//								  String imageFolderPath,
//								  String gmImageName){
//		
//		IDataset[] tifNamesArray = new IDataset[filepaths.length];
//		ILazyDataset[] imageArray = new ILazyDataset[filepaths.length];
//		
//		for (int id = 0; id < filepaths.length; id++) {
//
//			if(imageFolderPath == null){
//				dh1 = LoaderFactory.getData(filepaths[id]);
//
//				Path from = Paths.get(filepaths[id]);
//
//				Charset charset = StandardCharsets.UTF_8;
//
//				String content = new String(Files.readAllBytes(from), charset);
//
//				String[] tifNames = StringUtils.substringsBetween(content, "/", ".tif");
//
//				Dataset tifNamesDatasetOut = DatasetFactory.createFromObject(tifNames);
//
//				tifNamesArray[id] = tifNamesDatasetOut;
//
//				ILazyDataset ild = dh1.getLazyDataset(gmImageName);
//
//				if(ild == null){
//					ild = dh1.getLazyDataset("file_image");
//				}
//
//				if(ild == null){
//					ild = dh1.getLazyDataset("file");
//				}
//
//				if(ild == null){
////					ssp.imagesUnavailableWarning();
//				}
//
//				imageArray[id] = ild;
//
//			}
//
//			else{
//
//				String datName = StringUtils.substringAfterLast(filepaths[id], File.separator);
//
//				Path from = Paths.get(filepaths[id]);
//
//				Charset charset = StandardCharsets.UTF_8;
//
//				String content = new String(Files.readAllBytes(from), charset);
//
//				String firstTifName = StringUtils.substringBetween(content, "/", ".tif");
//
//				if(firstTifName.contains("/")){
//					firstTifName = StringUtils.substringAfterLast(firstTifName, "/");
//				}
//
//				String pathNameToReplace = StringUtils.substringBetween(content, "\t", "/" + firstTifName);
//
//				if(pathNameToReplace.contains("\t")){
//					pathNameToReplace = StringUtils.substringAfterLast(pathNameToReplace,"\t");
//				}
//
//				//Dont mix linux and windows paths!
//
//				imageFolderPath = imageFolderPath.replace("\\", "/");
//
//				content = content.replaceAll(pathNameToReplace, imageFolderPath);
//
//				//////////////////getting an array of .tifs
//
//				String[] tifNames = StringUtils.substringsBetween(content, "/", ".tif");
//				String[] tifNamesOut = new String[tifNames.length];
//
//				for(int w = 0; w<tifNames.length; w++){
//					String t = tifNames[w];
//
//					if(t.contains("/")){
//						t = StringUtils.substringAfterLast(t,"/");
//					}
//
//					t = imageFolderPath + "/" + t +".tif";
//
//					System.out.println(t);
//
//					tifNamesOut[w] = t;
//				}
//
//
//				Dataset tifNamesDatasetOut = DatasetFactory.createFromObject(tifNamesOut);
//
//				ILazyDataset[] localTifArray = new Dataset[tifNamesOut.length]; 
//				
//				tifNamesArray[id] = tifNamesDatasetOut;
//				
//				
//				for(int y =0; y<tifNamesOut.length; y++){	
//					dh1 = (IDataHolder) new TIFFImageLoader(tifNamesOut[y]);
//					localTifArray[y] =  dh1.getLazyDataset(0);
//				}
//				
//				
//				ILazyDataset localImagesDataset = new AggregateDataset(false, localTifArray);
//				
//				imageArray[id] = localImagesDataset;
//			}
//
//			
//			
//			
//			
//			//imageArray is an array of the images in read-in order
//
//			int imageNoInDat = 0;
//			//number of the image in the dat, i.e. the 10th image in the dat
//
//			for (int f = 0; f < (imageArray[id].getShape()[0]); f++) {
//
//				SliceND slice2 = new SliceND(ild.getShape());
//				slice2.setSlice(0, f, f + 1, 1);
//				ILazyDataset nim = ild.getSliceView(slice2); //getSlice(slice2);
//				som.put(imageRef, (ILazyDataset) nim);
//				imageRefList.add(imageRef);
//				imageNoInDatList.add(imageNoInDat);
//				imagesToFilepathRef.add(id);
//				imageRef++;
//				imageNoInDat++;
//			}
//
//			if (MethodSetting.toInt(drm.getCorrectionSelection()) == 0) {
//
//				try{
//					ILazyDataset ildx = dh1.getLazyDataset(gm.getxName());
//					SliceND slice1 = new SliceND(ildx.getShape());
//					IDataset xdat = ildx.getSlice(slice1);
//					xArray[id] = xdat;
//
//					if(MethodSetting.toMethod(correctionSelection) == MethodSetting.SXRD){
//						try{
//							ILazyDataset ildl = dh1.getLazyDataset("l");
//							SliceND slicel = new SliceND(ildl.getShape());
//							lArray[id] = ildl.getSlice(slicel);
//
//							ILazyDataset ildh = dh1.getLazyDataset("h");
//							SliceND sliceh = new SliceND(ildl.getShape());
//							hArray[id] = ildh.getSlice(sliceh);
//
//							ILazyDataset ildk = dh1.getLazyDataset("k");
//							SliceND slicek = new SliceND(ildk.getShape());
//							kArray[id] = ildl.getSlice(slicek);
//						}
//						catch(Exception h){
//
//						}
//					}
//				}
//
//				catch(NullPointerException r){
//
//				}
//
//			}
//
//			else if(MethodSetting.toInt(drm.getCorrectionSelection()) == 1||
//					MethodSetting.toInt(drm.getCorrectionSelection()) == 2||	
//					MethodSetting.toInt(drm.getCorrectionSelection()) == 3){
//
//				ILazyDataset ildx = dh1.getLazyDataset(gm.getxName());
//				ILazyDataset ildtheta = dh1.getLazyDataset(gm.getxNameRef());
//
//
//				SliceND slice1 = new SliceND(ildx.getShape());
//				IDataset xdat = ildx.getSlice(slice1);
//				xArray[id] = xdat;
//
//				SliceND slice2 = new SliceND(ildtheta.getShape());
//				IDataset thetadat = ildtheta.getSlice(slice2);
//				thetaArray[id] = thetadat;
//
//				dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getdcdtheta());
//
//				qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqdcd());
//
//				if (dcdtheta == null) {
//					try {
//						dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());
//					} catch (Exception e2) {
//						System.out.println("can't get dcdtheta");
//					}
//				} 
//				else {
//				}
//
//				if (qdcd == null) {
//					try {
//						qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());
//					} catch (Exception e2) {
//						System.out.println("can't get qdcd");
//					}
//				} 
//
//
//				//xArrayCon is an unsorted, but concatenated DoubleDataset of l values
//				if(MethodSetting.toMethod(correctionSelection) != MethodSetting.SXRD){
//					try{
//						thetaArrayCon = DatasetUtils.concatenate(thetaArray, 0);
//						xArrayCon = DatasetUtils.concatenate(xArray, 0);
//
//
//					}
//					catch(Exception g){
//
//					}
//				}
//
//
//			}
//		}
//
//
//
//
//		gm.addPropertyChangeListener(new PropertyChangeListener() {
//
//			public void propertyChange(PropertyChangeEvent evt) {
//				for (int id = 0; id < filepaths.length; id++) {
//					try {
//						IDataHolder dh1 = LoaderFactory.getData(filepaths[id]);
//						ILazyDataset ild = dh1.getLazyDataset(gm.getImageName());
//						//						models.get(id).setDatImages(ild);
//					}
//
//					catch (Exception e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				}
//			}
//		});
//
//
//
//		Dataset tifNamesCon = DatasetFactory.zeros(1);
//
//		AggregateDataset imageCon = null;
//
//		try{
//			imageCon = new AggregateDataset(false, imageArray);
//		}
//		catch(Exception j){
//			imageCon = new AggregateDataset(false, DatasetFactory.zeros(new int[] {2, 2}, Dataset.ARRAYFLOAT64));
//		}
//
//
//		try{
//			xArrayCon = DatasetUtils.concatenate(xArray, 0);
//			//xArrayCon is an unsorted, but concatenated DoubleDataset of l values
//			if(MethodSetting.toMethod(correctionSelection) == MethodSetting.SXRD){
//				try{
//					hArrayCon = DatasetUtils.concatenate(hArray, 0);
//					kArrayCon = DatasetUtils.concatenate(kArray, 0);
//					lArrayCon = DatasetUtils.concatenate(lArray, 0);
//				}
//				catch(Exception h){
//
//				}
//			}
//
//
//			tifNamesCon = DatasetUtils.concatenate(tifNamesArray, 0);
//			//tifNamesCon is an unsorted, but concatenated DoubleDataset of l tif names
//		}
//		catch(NullPointerException e){
//
//		}
//
//		Dataset imageRefDat = DatasetFactory.ones(imageRefList.size());
//
//		//imageRefDat is a dataset, equal to imageRefList (list of the integer number of the image that is read in - the nth read in, for example), 
//		//and will be sorted based on the xArrayCon, which is the"l" values (for a rod)
//
//		Dataset imagesToFilepathRefDat = DatasetFactory.ones(imageRefList.size());
//
//		//imagesToFilepathRefDat is a dataset, equal to imagesToFilepathRef (list of the integer number of the dat (in String[] filepaths) of the image that is read in at that point- the nth read in, for example), 
//		//and will be sorted based on the xArrayCon, which is the"l" values (for a rod)
//
//		for (int sd = 0; sd < imageRefList.size(); sd++) {
//			imageRefDat.set(imageRefList.get(sd), sd);
//			imagesToFilepathRefDat.set(imagesToFilepathRef.get(sd), sd);
//		}
//
//		//sm.setSortedDatIntsInOrderDataset(imagesToFilepathRefDat);
//
//		Dataset xArrayConClone = xArrayCon.clone();
//
//		DoubleDataset xArrayConCloneDouble = (DoubleDataset) xArrayConClone.clone();
//
//		Dataset xArrayConCloneForh = xArrayCon.clone();
//		Dataset xArrayConCloneFork = xArrayCon.clone();
//		Dataset xArrayConCloneForl = xArrayCon.clone();
//
//		try{
//			DatasetUtils.sort(xArrayCon, imageRefDat);
//			//so now we have the image number in imageArray (imageRefDat) sorted by "l" value xArrayCon
//			DatasetUtils.sort(xArrayConClone, imagesToFilepathRefDat);
//			//so now we have the dat number in filepaths (imagesToFilepathRefDat) sorted by "l" value xArrayCon
//
//			if(MethodSetting.toMethod(correctionSelection) == MethodSetting.SXRD){
//				try{
//					DatasetUtils.sort(xArrayConCloneForh, hArrayCon);
//					DatasetUtils.sort(xArrayConCloneFork, kArrayCon);
//					DatasetUtils.sort(xArrayConCloneForl, lArrayCon);
//				}
//				catch(Exception h){
//
//				}
//			}
//			else{
//				DatasetUtils.sort(xArrayConCloneForh, thetaArrayCon);
//			}
//
//
//			Dataset sortedTifNamesCon = this.sortStrings(xArrayConCloneDouble, tifNamesCon);
//			//so now we have the tif names sorted by "l" value xArrayCon
//
//			ILazyDataset[] imageSortedDat = new ILazyDataset[imageRefList.size()];
//			//		imageSortedDat this is the array of sorted images - sorted according to "l"
//
//			int[] filepathsSortedArray = new int[imageRefList.size()];
//
//
//
//		}
//
//	
//	
//}
