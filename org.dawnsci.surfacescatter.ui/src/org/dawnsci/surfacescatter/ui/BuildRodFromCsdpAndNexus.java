package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.DirectoryModel;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.FrameModel;
import org.dawnsci.surfacescatter.NeXusStructureStrings;
import org.dawnsci.surfacescatter.OverviewNexusObjectBuilderEnum;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;

public class BuildRodFromCsdpAndNexus {

	public BuildRodFromCsdpAndNexus(String inFile, CurveStitchDataPackage csdp) {

		SurfaceScatterPresenter sspi = new SurfaceScatterPresenter();
		sspi.createGm();

		ArrayList<FrameModel> fms = new ArrayList<>();

		NexusFile file = new NexusFileFactoryHDF5().newNexusFile(inFile);

		FittingParametersInputReader.anglesAliasReaderFromNexus(file);

		FittingParametersInputReader.geometricalParametersReaderFromNexus(file, sspi.getGm(), sspi.getDrm());

		try {
			file.openToRead();
		} catch (NexusException e) {
			e.printStackTrace();
		}

		String path = "/" + NeXusStructureStrings.getEntry() + "/" + NeXusStructureStrings.getOverviewOfFrames();
		try {
			GroupNode point = file.getGroup(path, false);

			Attribute boundaryBoxAttribute = point.getAttribute(path + NeXusStructureStrings.getBoundaryboxArray()[1]);
			IntegerDataset boundaryBox0 = DatasetUtils.cast(IntegerDataset.class, boundaryBoxAttribute.getValue());

			for (int i = 0; i < boundaryBox0.getSize(); i++) {
				String framePath = "/" + NeXusStructureStrings.getEntry() + "/" + NeXusStructureStrings.getPoint() + i;
				GroupNode framePoint = file.getGroup(framePath, false);
				FrameModel fm = frameModelBuilder(framePoint);
				fms.add(fm);

			}

		} catch (NexusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sspi.setFms(fms);

		DirectoryModel drm = directoryModelBuilder(fms, file);
		drm.setCsdp(csdp);

		sspi.setDrm(drm);

	}

	private FrameModel frameModelBuilder(GroupNode g) {

		FrameModel fm = new FrameModel();

		for (OverviewNexusObjectBuilderEnum oe : OverviewNexusObjectBuilderEnum.values()) {
			try {
				oe.frameModelPopulateFromGroupNodeMethod(g, fm);
			} catch (Exception j) {
				System.out.println(j.getMessage());
			}

		}

		Attribute rawImageAt = g.getAttribute(NeXusStructureStrings.getRawImage());
		Dataset rawImage = (Dataset) rawImageAt.getValue();

		fm.setRawImageData(rawImage);

		Attribute backgroundSubtractedImageAt = g.getAttribute(NeXusStructureStrings.getBackgroundSubtractedImage());
		Dataset backgroundSubtractedImage = (Dataset) backgroundSubtractedImageAt.getValue();

		fm.setBackgroundSubtractedImage(backgroundSubtractedImage);

		return fm;

	}

	private DirectoryModel directoryModelBuilder(ArrayList<FrameModel> fms, NexusFile file) {

		DirectoryModel drm = new DirectoryModel();

		FrameModel fm0 = fms.get(0);

		drm.setCorrectionSelection(fm0.getCorrectionSelection());

		int[] datfileNo = new int[fms.size()];
		int[] noInDatFileNo = new int[fms.size()];

		for (int i = 0; i < fms.size(); i++) {
			datfileNo[i] = fms.get(i).getDatNo();
			noInDatFileNo[i] = fms.get(i).getNoInOriginalDat();
		}

		makeSortedNestedArrayLists(fms, drm);

		makeSortedLists(fms, drm);

		return drm;

	}

	private int numberOfUniqueNumbers(int[] in) {

		ArrayList<Integer> probeList = new ArrayList<>();

		probeList.add(in[0]);

		for (int i : in) {
			boolean add = true;

			for (int j : probeList) {
				if (i == j) {
					add = false;
					break;
				}
			}

			if (add) {
				probeList.add(i);
			}
		}

		return probeList.size();
	}

	private int getMaxNo(int[] in) {

		int probe = in[0];

		for (int i : in) {
			if (i > probe) {
				probe = i;
			}
		}

		return probe;
	}

	private int[] numberOfFramesInEachDat(int noDats, int[] inDatNos, int[] inNoInDat) {

		int[] noInEachDat = new int[noDats];

		for (int u = 0; u < noDats; u++) {
			noInEachDat[u] = 0;
		}

		for (int g = 0; g < inDatNos.length; g++) {
			int localDatNo = inDatNos[g];
			if (inNoInDat[g] > noInEachDat[localDatNo]) {
				noInEachDat[localDatNo] = inNoInDat[g];
			}
		}

		return noInEachDat;

	}

	private void makeSortedNestedArrayLists(ArrayList<FrameModel> fms, DirectoryModel drm) {

		ArrayList<ArrayList<FrameModel>> fmsSorted = new ArrayList<>();
		ArrayList<ArrayList<Integer>> framesCorrespondingToDats = new ArrayList<>();
		ArrayList<ArrayList<Double>> dmxList = new ArrayList<>();
		ArrayList<ArrayList<Double>> dmqList = new ArrayList<>();
		ArrayList<ArrayList<double[]>> locationList = new ArrayList<>();

		int[] datfileNo = new int[fms.size()];
		int[] noInDatFileNo = new int[fms.size()];

		for (int i = 0; i < fms.size(); i++) {
			datfileNo[i] = fms.get(i).getDatNo();
			noInDatFileNo[i] = fms.get(i).getNoInOriginalDat();
		}

		int noOfDats = numberOfUniqueNumbers(datfileNo);

		int[] noFramesInEachDat = numberOfFramesInEachDat(noOfDats, datfileNo, noInDatFileNo);

		for (int y = 0; y < noOfDats; y++) {

			fmsSorted.add(new ArrayList<>());
			framesCorrespondingToDats.add(new ArrayList<>());
			dmxList.add(new ArrayList<>());
			dmqList.add(new ArrayList<>());
			locationList.add(new ArrayList<>());

			for (int j = 0; j < noFramesInEachDat[y]; j++) {
				fmsSorted.get(y).add(new FrameModel());
				framesCorrespondingToDats.get(y).add(0);
				dmxList.get(y).add(0.0);
				dmqList.get(y).add(0.0);
				locationList.get(y).add(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0,0.0});
			}
		}

		for (FrameModel fm : fms) {
			fmsSorted.get(fm.getDatNo()).set(fm.getNoInOriginalDat(), fm);
			framesCorrespondingToDats.get(fm.getDatNo()).set(fm.getNoInOriginalDat(), fm.getFmNo());
			dmxList.get(fm.getDatNo()).add(fm.getScannedVariable());
			dmqList.get(fm.getDatNo()).add(fm.getQ());
			locationList.get(fm.getDatNo()).add(fm.getRoiLocation());
		}

		drm.setFramesCorespondingToDats(framesCorrespondingToDats);
		drm.setFmsSorted(fmsSorted);
		drm.setDmxList(dmxList);
		drm.setDmqList(dmqList);
		drm.setLocationList(locationList);
	}

	private void makeSortedLists(ArrayList<FrameModel> fms, DirectoryModel drm) {

		ArrayList<Double> qList = new ArrayList<>();
		ArrayList<Double> xList = new ArrayList<>();
		ArrayList<IDataset> backgroundDatArray= new ArrayList<>();
		ArrayList<Integer> imageNoInDatList= new ArrayList<>();

		
		int[] filePathsSortedArray = new int[fms.size()];
		String[] datFilepaths = new String[fms.size()];
		
		
		for (FrameModel fm : fms) {

			qList.add(fm.getQ());
			xList.add(fm.getScannedVariable());
			backgroundDatArray.add(fm.getBackgroundSubtractedImage());
			imageNoInDatList.add(fm.getImageNumber());
			filePathsSortedArray[fm.getFmNo()] = fm.getDatNo();
			datFilepaths[fm.getFmNo()] = fm.getDatFilePath();
		}

		drm.setqList(qList);
		drm.setxList(xList);
		
		Dataset qDat = DatasetFactory.createFromList(qList);
		Dataset xDat = DatasetFactory.createFromList(xList);
		
		drm.setSortedX(xDat);
		drm.setSortedQ(qDat);
		
		drm.setBackgroundDatArray(backgroundDatArray);
		drm.setImageNoInDatList(imageNoInDatList);
		drm.setFilepathsSortedArray(filePathsSortedArray);
		drm.setDatFilepaths(datFilepaths);
		

	}
}
