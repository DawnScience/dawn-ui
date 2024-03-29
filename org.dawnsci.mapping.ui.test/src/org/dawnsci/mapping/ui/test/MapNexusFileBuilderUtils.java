package org.dawnsci.mapping.ui.test;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFile;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusFileBuilder;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class MapNexusFileBuilderUtils {

	public static final String DETECTOR = "detector";
	public static final String STAGE_X = "stage_x_set";
	public static final String STAGE_Y = "stage_y_set";
	public static final String STAGE_Z = "stage_z_set";
	public static final String TEMPERATURE = "temperature";
	public static final String ENERGY = "energy";
	public static final String SUM = "sum";
	public static final int SMALLEST=10;
	public static final String ENTRY1 = "entry1";
	public static final String DATA = "data";
	public static final String DETECTOR_PATH = Node.SEPARATOR + ENTRY1 + Node.SEPARATOR + DETECTOR + Node.SEPARATOR + DATA;
	public static final String SUM_PATH = Node.SEPARATOR + ENTRY1 + Node.SEPARATOR + SUM + Node.SEPARATOR + DATA;
	
	public static final String[] FASTEST_AXES = new String[] {STAGE_X,STAGE_Y,STAGE_Z,TEMPERATURE};
	
	public static void setUpServices() {
		ServiceProvider.setService(INexusFileFactory.class, new NexusFileFactoryHDF5());
	}
	
	public static void tearDownServices() {
		ServiceProvider.reset();
	}
	
	public static void makeGridScanWithSum(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);

		NXdata makeNXData = makeNXData(4, 2, false);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNXData(2, 0, false);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeGridScanWithZandSum(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);

		NXdata makeNXData = makeNXData(5, 2, false);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNXData(3, 0, false);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeGridScanWithEnergyZ(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode("entry1", entry);

		NXdata makeNXData = makeNXData(3, 0, true);
		entry.addGroupNode(DETECTOR, makeNXData);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}

	}
	
	public static void makeDiagLineScanWithSum(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);

		NXdata makeNXData = makeNonGridNXData(3, 2, false);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNonGridNXData(1, 0, false);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makePointScanWithSum(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);

		NXdata makeNXData = makeNonGridNXData(3, 2, false, 1);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNonGridNXData(1, 0, false, 1);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeDiagLineScanWithSumZ(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);

		NXdata makeNXData = makeNonGridNXData(4, 2, false);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNonGridNXData(2, 0, false);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeDiodeGridScan(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNXData(2, 0, false);
		entry.addGroupNode(DETECTOR, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeMultipleDiodeGridScan(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNXData(2, 0, false);
		entry.addGroupNode(DETECTOR, sum);
		NXdata sum2 = makeNXData(2, 0, false);
		entry.addGroupNode(DETECTOR+"_1", sum);
		entry.addGroupNode(DETECTOR+"_2", sum2);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeDiodeGridScanEnergy(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNXData(3, 0, true);
		entry.addGroupNode(DETECTOR, sum);
		
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeDiodeLineScan(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNonGridNXData(1, 0, false);
		entry.addGroupNode(DETECTOR, sum);
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makeDiodeLineScanEnergy(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNonGridNXData(2, 0, true);
		entry.addGroupNode(DETECTOR, sum);
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	public static void makePixelImageStack(String path) throws NexusException {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode(ENTRY1, entry);
		NXdata sum = makeNXData(3, 2, true);
		entry.addGroupNode(DETECTOR, sum);
		
		try (NexusBuilderFile file = fileBuilder.createFile(false)) {
		}
	}
	
	private static NXdata makeNXData(int rank, int detectorRank, boolean isEnergy) {
		return makeNXData(rank, detectorRank, isEnergy, SMALLEST);
	}
	
	private static NXdata makeNonGridNXData(int rank, int detectorRank, boolean isEnergy) {
		return makeNonGridNXData(rank, detectorRank, isEnergy, SMALLEST);
	}
	
	private static NXdata makeNXData(int rank, int detectorRank, boolean isEnergy, int smallest) {
		
		int scanRank = rank - detectorRank;
		
		if (scanRank > 3) throw new IllegalArgumentException("Max scan rank is 4");
		
		NXdata nxData = NexusNodeFactory.createNXdata();
		
		int[] shape = new int[rank];
		Arrays.fill(shape, 100);
		
		for (int i = 0 ; i < rank - detectorRank; i++) {
			shape[i] = smallest+i;
		}
		int size = 1;
		for (int i = 0 ; i < shape.length; i++) {
			size *= shape[i];
		}
		
		DoubleDataset data = DatasetFactory.createRange(size);
		data.setName(DATA);
		data.setShape(shape);
		nxData.setDataset(DATA, data);
		nxData.setAttributeSignal(DATA);
		
		String[] axes = new String[shape.length];
		for (int i = 0 ; i < rank; i++) {
			if (i >= scanRank) {
				axes[i] = ".";
			} else {
				String axis = FASTEST_AXES[scanRank-1-i];
				if (isEnergy) {
					 axis = i == 0 ? ENERGY : FASTEST_AXES[scanRank-1-i];
				}
				DoubleDataset ax = DatasetFactory.createRange(shape[i]);
				axes[i] = axis;
				nxData.setDataset(axis, ax);
				nxData.setAttribute(null, axis + "_indices", i);
				
				nxData.setDataset(axis.substring(0,axis.length()-4), ax);
				nxData.setAttribute(null, axis.substring(0,axis.length()-4) + "_indices", i);
			}
		}
		
		nxData.setAttribute(null, "axes", axes);
		
		return nxData;
		
	}
	
	private static NXdata makeNonGridNXData(int rank, int detectorRank, boolean isEnergy, int smallest) {
		
		int scanRank = rank - detectorRank;
		
		if (scanRank > 3) throw new IllegalArgumentException("Max scan rank is 3");
		
		NXdata nxData = NexusNodeFactory.createNXdata();
		
		int[] shape = new int[rank];
		Arrays.fill(shape, 100);
		
		for (int i = 0 ; i < rank - detectorRank; i++) {
			shape[i] = smallest+i;
		}
		int size = 1;
		for (int i = 0 ; i < shape.length; i++) {
			size *= shape[i];
		}
		
		DoubleDataset data = DatasetFactory.createRange(size);
		data.setName(DATA);
		data.setShape(shape);
		nxData.setDataset(DATA, data);
		nxData.setAttributeSignal(DATA);
		
		String[] axes = new String[shape.length];
		for (int i = 0 ; i < rank; i++) {
			if (i >= scanRank) {
				axes[i] = ".";
			} else {
				if (i == scanRank-1) {
					axes[i] = ".";
					DoubleDataset ax = DatasetFactory.createRange(shape[i]);
					String axis = FASTEST_AXES[1];
					nxData.setDataset(axis, ax);
					nxData.setAttribute(null, axis + "_indices", i);
					axis = FASTEST_AXES[0];
					nxData.setDataset(axis, ax);
					nxData.setAttribute(null, axis + "_indices", i);
				} else {
					String axis = FASTEST_AXES[scanRank-i];
					if (isEnergy) {
						 axis = i == 0 ? ENERGY : FASTEST_AXES[scanRank-i];
					}
					DoubleDataset ax = DatasetFactory.createRange(shape[i]);
					axes[i] = axis;
					nxData.setDataset(axis, ax);
					nxData.setAttribute(null, axis + "_indices", i);
				}
				
				
			}
		}
		
		nxData.setAttribute(null, "axes", axes);
		
		return nxData;
		
	}

}
