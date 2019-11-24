package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.ILazyDataset;
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

}
