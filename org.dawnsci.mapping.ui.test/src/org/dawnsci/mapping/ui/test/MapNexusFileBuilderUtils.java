package org.dawnsci.mapping.ui.test;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusScanFile;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusFileBuilder;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;

public class MapNexusFileBuilderUtils {

	public static final String DETECTOR = "detector";
	public static final String SUM = "sum";
	public static final int SMALLEST=10;
	public static final String ENTRY1 = "entry1";
	public static final String DATA = "data";
	public static final String DETECTOR_PATH = Node.SEPARATOR + ENTRY1 + Node.SEPARATOR + DETECTOR + Node.SEPARATOR + DATA;
	public static final String SUM_PATH = Node.SEPARATOR + ENTRY1 + Node.SEPARATOR + SUM + Node.SEPARATOR + DATA;
	
	public static void makeGridScanWithSum(String path) throws NexusException {
		ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode("entry1", entry);

		NXdata makeNXData = makeNXData(4, 2);
		entry.addGroupNode(DETECTOR, makeNXData);
		NXdata sum = makeNXData(2, 0);
		entry.addGroupNode(SUM, sum);
		
		
		try (NexusScanFile file = fileBuilder.createFile(false)) {
		}

	}
	
	public static void makeGridScanWithEnergyZ(String path) throws NexusException {
		ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder(path);
		NXroot nXroot = fileBuilder.getNXroot();
		NXentry entry = NexusNodeFactory.createNXentry();
		nXroot.addGroupNode("entry1", entry);

		NXdata makeNXData = makeNXData(3, 0);
		entry.addGroupNode(DETECTOR, makeNXData);
		
		
		try (NexusScanFile file = fileBuilder.createFile(false)) {
		}

	}
	
	private static NXdata makeNXData(int rank, int detectorRank) {
		
		NXdata nxData = NexusNodeFactory.createNXdata();
		
		int[] shape = new int[rank];
		Arrays.fill(shape, 100);
		
		for (int i = 0 ; i < rank - detectorRank; i++) {
			shape[i] = SMALLEST+i;
		}
		int size = 1;
		for (int i = 0 ; i < shape.length; i++) {
			size *= shape[i];
		}
		
		DoubleDataset data = DatasetFactory.createRange(size);
		data.setName(DATA);
		data.setShape(shape);
		nxData.setDataset("data", data);
		nxData.setAttributeSignal("data");
		
		String[] axes = new String[shape.length];
		for (int i = 0 ; i < rank; i++) {
			if (i >= rank - detectorRank) {
				axes[i] = ".";
			} else {
				DoubleDataset ax = DatasetFactory.createRange(shape[i]);
				axes[i] = "axis_"+i;
				nxData.setDataset("axis_"+i, ax);
				nxData.setAttribute(null, "axis_"+i + "_indices", new int[]{i});
			}
		}
		
		nxData.setAttribute(null, "axes", axes);
		
		return nxData;
		
	}

}
