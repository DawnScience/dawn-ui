package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlottableObject;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;


public class LoadedFileTest  extends AbstractTestModel {
	
	@Test
	public void testLoadedFile() throws Exception {
		assertEquals(file.getAbsolutePath(), loadedFile.getFilePath());
	}

	@Test
	public void testGetDataOptions() {
		List<DataOptions> dataOptions = loadedFile.getDataOptions();
		//single element dataset not included
		assertEquals(nameShapeMap.size()-2, dataOptions.size());
	}

	@Test
	public void testGetName() {
		assertEquals(file.getName(), loadedFile.getName());
	}

	@Test
	public void testGetLongName() {
		assertEquals(file.getAbsolutePath(), loadedFile.getFilePath());
	}

	@Test
	public void testGetLazyDataset() {
		Entry<String, int[]> datasetEntry = nameShapeMap.entrySet().iterator().next();
		ILazyDataset lazyDataset = loadedFile.getLazyDataset(NanoModelTestUtils.ROOT + Node.SEPARATOR + datasetEntry.getKey());
		assertArrayEquals(datasetEntry.getValue(), lazyDataset.getShape());
		
	}

	@Test
	public void testGetDataShapes() {
		Map<String, int[]> dataShapes = loadedFile.getDataShapes();
		assertEquals(nameShapeMap.size(), dataShapes.size());
		Entry<String, int[]> datasetEntry = nameShapeMap.entrySet().iterator().next();
		assertArrayEquals(datasetEntry.getValue(), dataShapes.get(NanoModelTestUtils.ROOT + Node.SEPARATOR +datasetEntry.getKey()));
	}

	@Test
	public void testIsSelected() {
		assertFalse(loadedFile.isSelected());
	}

	@Test
	public void testSetSelected() {
		assertFalse(loadedFile.isSelected());
		loadedFile.setSelected(true);
		assertTrue(loadedFile.isSelected());
		loadedFile.setSelected(false);
		assertFalse(loadedFile.isSelected());
	}

	@Test
	public void testGetChecked() {
		DataOptions dataOptions = loadedFile.getDataOptions().get(0);
		dataOptions.setSelected(true);
		List<DataOptions> checked = loadedFile.getChecked();
		assertEquals(1, checked.size());
		assertEquals(dataOptions, checked.get(0));
	}
	
	@Test
	public void mixedSignalTags() {
		
		String signal = "thesignal";
		String notthesignal = "notthesignal";
		String data = "data";
		
		DataNode sn = TreeFactory.createDataNode(0);
		DoubleDataset dss = DatasetFactory.zeros(new int[] {3});
		dss.setName(signal);
		sn.setDataset(dss);
		
		DataNode nsn = TreeFactory.createDataNode(0);
		DoubleDataset dns = DatasetFactory.zeros(new int[] {5});
		dns.setName(notthesignal);
		nsn.setDataset(dns);
		nsn.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, 1));
		
		NXdata nx = NexusNodeFactory.createNXdata();
		nx.setAttributeSignal(signal);
		nx.addDataNode(signal, sn);
		nx.addDataNode(notthesignal, nsn);
		nx.setAttributeAxes(".");
		
		NXentry en = NexusNodeFactory.createNXentry();
		en.addGroupNode(data, nx);
		
		Tree t = TreeFactory.createTreeFile(0, "/tmp/nofile.nxs");
		t.setGroupNode(en);
		
		DataHolder dh = new DataHolder();
		dh.setTree(t);
		
		HDF5Loader.updateDataHolder(dh, true);
		
		LoadedFile f = new LoadedFile(dh);
		
		DataOptions signalDO = f.getDataOption(Tree.ROOT + data + Node.SEPARATOR + signal);
		
		ILazyDataset lzs = signalDO.getLazyDataset();
		assertEquals(3, lzs.getSize());
		
		DataOptions nsignalDO = f.getDataOption(Tree.ROOT + data + Node.SEPARATOR + notthesignal);
		
		ILazyDataset lzns = nsignalDO.getLazyDataset();
		assertEquals(5, lzns.getSize());
		
	}
	
	@Test
	public void pre2014SignalTags() throws MetadataException {
		
		String signal = "thesignal";
		String notthesignal = "notthesignal";
		String axis = "axis";
		String data = "data";
		
		DataNode sn = TreeFactory.createDataNode(0);
		DoubleDataset dss = DatasetFactory.zeros(new int[] {3});
		dss.setName(signal);
		sn.setDataset(dss);
		sn.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, 1));
		
		DataNode an = TreeFactory.createDataNode(0);
		DoubleDataset dsa = DatasetFactory.zeros(new int[] {3});
		dsa.setName(axis);
		an.setDataset(dsa);
		an.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXIS, "1"));
		
		DataNode nsn = TreeFactory.createDataNode(0);
		DoubleDataset dns = DatasetFactory.zeros(new int[] {5});
		dns.setName(notthesignal);
		nsn.setDataset(dns);

		
		NXdata nx = NexusNodeFactory.createNXdata();
		nx.addDataNode(signal, sn);
		nx.addDataNode(notthesignal, nsn);
		nx.addDataNode(axis, an);

		NXentry en = NexusNodeFactory.createNXentry();
		en.addGroupNode(data, nx);
		
		GroupNode root = TreeFactory.createGroupNode(0);
		root.addGroupNode("entry", en);
		
		Tree t = TreeFactory.createTreeFile(0, "/tmp/nofile.nxs");
		t.setGroupNode(root);
		
		DataHolder dh = new DataHolder();
		dh.setTree(t);
		
		HDF5Loader.updateDataHolder(dh, true);
		
		LoadedFile f = new LoadedFile(dh);
		
		DataOptions signalDO = f.getDataOption(Tree.ROOT +"entry"+ Node.SEPARATOR + data + Node.SEPARATOR + signal);
		
		ILazyDataset lzs = signalDO.getLazyDataset();
		assertEquals(3, lzs.getSize());
		
		List<AxesMetadata> metadata = lzs.getMetadata(AxesMetadata.class);
		assertNotNull(metadata);
		assertFalse(metadata.isEmpty());

		AxesMetadata m = metadata.get(0);
		ILazyDataset[] lax = m.getAxis(0);
		assertTrue(lax[0].getName().endsWith(axis));
		
	}
	
	
	@Test
	public void testNexusTags() throws MetadataException {
		testTagging(true);

	}
	
	@Test
	public void testNoNexusTags() throws MetadataException {
		testTagging(false);

	}
	
	private void testTagging(boolean tag) throws MetadataException {
		
		String readback = "readback";
		String other = "other";
		String signal = "data";
		String uncert = NexusConstants.DATA_ERRORS;
		String data = "nexusdata";
		String entry = "entry";
		String set = readback + NexusConstants.DATA_AXESSET_SUFFIX;
		String nxreadset = Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + set;
		String nxread = Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + readback;
		String nxother = Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + other;
		String nxuncert = Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + uncert;
		
		NXentry en = NexusNodeFactory.createNXentry();
		NXdata nx = NexusNodeFactory.createNXdata();
		
		en.addGroupNode(data, nx);
		
		//signal dataset
		DataNode dn = TreeFactory.createDataNode(0);
		DoubleDataset dds = DatasetFactory.zeros(new int[] {4,5});
		dds.setName(signal);
		dn.setDataset(dds);
		nx.addDataNode(signal, dn);
		
		//uncert dataset
		dn = TreeFactory.createDataNode(0);
		dds = DatasetFactory.zeros(new int[] {4,5});
		dds.setName(uncert);
		dn.setDataset(dds);
		nx.addDataNode(uncert, dn);
		
		//other dataset in node
		//suitable axis but not tagged
		dn = TreeFactory.createDataNode(0);
		dds = DatasetFactory.zeros(new int[] {5});
		dds.setName(other);
		dn.setDataset(dds);
		nx.addDataNode(other, dn);
		
		//set axis
		dn = TreeFactory.createDataNode(0);
		dds = DatasetFactory.zeros(new int[] {4});
		dds.setName(set);
		dn.setDataset(dds);
		nx.addDataNode(set, dn);
		
		//readback axis
		dn = TreeFactory.createDataNode(0);
		dds = DatasetFactory.zeros(new int[] {4,5});
		dds.setName(readback);
		dn.setDataset(dds);
		nx.addDataNode(readback, dn);
		
		
		if (tag) {
			nx.setAttributeSignal(signal);
			nx.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, new String[] {set, NexusConstants.DATA_AXESEMPTY}));
			nx.addAttribute(TreeFactory.createAttribute(set + NexusConstants.DATA_INDICES_SUFFIX, 0));
			nx.addAttribute(TreeFactory.createAttribute(readback + NexusConstants.DATA_INDICES_SUFFIX, new int[] {0,1}));
		}
		
		Tree t = TreeFactory.createTreeFile(0, "/tmp/nofilenexus.nxs");
		GroupNode root = TreeFactory.createGroupNode(0);
		root.addGroupNode(entry, en);
		t.setGroupNode(root);

		DataHolder dh = new DataHolder();
		dh.setTree(t);
		
		HDF5Loader.updateDataHolder(dh, true);
		
		LoadedFile f = new LoadedFile(dh);
		
		DataOptions signalDO = f.getDataOption(Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + signal);
		
		ILazyDataset lz = signalDO.getLazyDataset();
		
		List<AxesMetadata> metadata = lz.getMetadata(AxesMetadata.class);
		
		if (tag) {
			assertNotNull(metadata);
			assertFalse(metadata.isEmpty());

			AxesMetadata m = metadata.get(0);
			ILazyDataset[] axis = m.getAxis(0);
			assertTrue(axis[0].getName().endsWith(set));
			axis = m.getAxis(1);
			assertNull(axis);

			NDimensions ndims = signalDO.buildNDimensions();
			//axes tagged, _indices, indices then all other in alphabetical order
			String[] axop0 = ndims.getAxisOptions(0);
			String[] expected0 = {nxreadset, nxread, NDimensions.INDICES, nxuncert};
			assertArrayEquals(axop0, expected0);

			String[] axop1 = ndims.getAxisOptions(1);
			String[] expected1 = {NDimensions.INDICES, nxread, nxuncert, nxother};
			assertArrayEquals(axop1, expected1);
			
			ILazyDataset errors = lz.getErrors();
			assertNotNull(errors);
			
		} else {
			assertNull(metadata);
			NDimensions ndims = signalDO.buildNDimensions();

			String[] axop0 = ndims.getAxisOptions(0);
			//only important indices first
			assertEquals(axop0[0], NDimensions.INDICES);

			String[] axop1 = ndims.getAxisOptions(1);
			assertEquals(axop1[0], NDimensions.INDICES);
			
			ILazyDataset errors = lz.getErrors();
			assertNull(errors);
		}
	}
	
	@Test
	public void testAxesNotNexus() throws MetadataException {
		
		DataHolder dh = new DataHolder();
		
		for (int i = 0; i < 4 ; i++) {
			String name = "Column_" + i;
			DoubleDataset d = DatasetFactory.ones(new int[] {100});
			d.setName(name);
			dh.addDataset(name, d);
		}
		
		dh.setFilePath("/notafile.dat");
		
		LoadedFile f = new LoadedFile(dh);
		List<DataOptions> dol = f.getDataOptions();
		assertEquals(4, dol.size());
		DataOptions dataOptions = dol.get(0);
		NDimensions nd = dataOptions.buildNDimensions();
		//probably ok null here for now
		PlottableObject po = new PlottableObject(null, nd);
		dataOptions.setPlottableObject(po);
		Map<String, int[]> allPossibleAxes = dataOptions.getAllPossibleAxes();
		assertEquals(3, allPossibleAxes.size());
		String[] axisOptions = dataOptions.getPlottableObject().getNDimensions().getAxisOptions(0);
		//3 + indices
		assertEquals(4, axisOptions.length);
		
		//This should do nothing
		f.setOnlySignals(true);
		dol = f.getDataOptions();
		assertEquals(4, dol.size());
		dataOptions = dol.get(0);
        allPossibleAxes = dataOptions.getAllPossibleAxes();
		assertEquals(3, allPossibleAxes.size());
		axisOptions = dataOptions.getPlottableObject().getNDimensions().getAxisOptions(0);
		assertEquals(4, axisOptions.length);
		
	}
	
	@Test
	public void testNexusAuxSignals() throws MetadataException {
		
		String signal = "data";
		String auxSignal1 = "aux_signal1";
		String signalErrors = NexusConstants.DATA_UNCERTAINTIES;
		String auxSignal1Errors = "aux_signal1" + NexusConstants.DATA_ERRORS_SUFFIX;
		String auxSignal2 = "aux_signal2";
		String notAuxSignal = "not_aux_signal";
		String data = "nexusdata";
		String set = "readback" + NexusConstants.DATA_AXESSET_SUFFIX;
		String entry = "entry";
		
		NXentry en = NexusNodeFactory.createNXentry();
		NXdata nx = NexusNodeFactory.createNXdata();
		
		en.addGroupNode(data, nx);
		
		//signal datasets
		makeDataNode(signal,nx);
		makeDataNode(auxSignal1,nx);
		makeDataNode(auxSignal2,nx);
		makeDataNode(notAuxSignal,nx);
		makeDataNode(signalErrors,nx);
		makeDataNode(auxSignal1Errors,nx);
		
		//set axis
		DataNode dn = TreeFactory.createDataNode(0);
		DoubleDataset dds = DatasetFactory.zeros(new int[] {4});
		dds.setName(set);
		dn.setDataset(dds);
		nx.addDataNode(set, dn);
		
		nx.setAttributeSignal(signal);
		nx.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, new String[] {set, NexusConstants.DATA_AXESEMPTY}));
		nx.addAttribute(TreeFactory.createAttribute(set + NexusConstants.DATA_INDICES_SUFFIX, 0));
		nx.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AUX_SIGNALS, new String[] {auxSignal1, auxSignal2}));
		
		Tree t = TreeFactory.createTreeFile(0, "/tmp/aux_signals_nexus.nxs");
		GroupNode root = TreeFactory.createGroupNode(0);
		root.addGroupNode(entry, en);
		t.setGroupNode(root);

		DataHolder dh = new DataHolder();
		dh.setTree(t);
		
		HDF5Loader.updateDataHolder(dh, true);
		
		LoadedFile f = new LoadedFile(dh);
		
		List<DataOptions> dataOptions = f.getDataOptions(false);
		
		assertEquals(7, dataOptions.size());
		
		dataOptions = f.getDataOptions(true);
		
		assertEquals(3, dataOptions.size());
		
		DataOptions signalDO = f.getDataOption(Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + signal);
		
		ILazyDataset lz = signalDO.getLazyDataset();
		
		assertNotNull(lz.getErrors());
		
		DataOptions auxSignal1DO = f.getDataOption(Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + auxSignal1);
		
		lz = auxSignal1DO.getLazyDataset();
		
		assertNotNull(lz.getErrors());
		
		DataOptions auxSignal2DO = f.getDataOption(Tree.ROOT + entry + Node.SEPARATOR + data + Node.SEPARATOR + auxSignal2);
		
		lz = auxSignal2DO.getLazyDataset();
		
		assertNull(lz.getErrors());
		
	}
	
	private void makeDataNode(String name, GroupNode parent) {
		DataNode dn = TreeFactory.createDataNode(0);
		DoubleDataset dds = DatasetFactory.zeros(new int[] {4,5});
		dds.setName(name);
		dn.setDataset(dds);
		parent.addDataNode(name, dn);
	}

}
