package org.dawnsci.datavis.model.test.fileconfig;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.fileconfig.NexusFileConfiguration;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.NexusHDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

public class NexusFileConfigurationTest {

	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testConfigurePost2014NXS() throws IOException, NexusException, ScanFileHolderException {
		File file = folder.newFile("post2014.nxs");
		
		try (NexusFile nxsFile = new NexusFileHDF5(file.getAbsolutePath())) {
			nxsFile.createAndOpenToWrite();
			GroupNode rootNode = TreeFactory.createGroupNode(0);
			rootNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ROOT));
			DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date currentDate = new Date();
			rootNode.addAttribute(TreeFactory.createAttribute("file_name", file.getAbsolutePath()));
			rootNode.addAttribute(TreeFactory.createAttribute("file_time", dateFormatter.format(currentDate)));
			rootNode.addAttribute(TreeFactory.createAttribute("default", "entry1"));
			
			GroupNode entryNode = TreeFactory.createGroupNode(0);
			entryNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			entryNode.addAttribute(TreeFactory.createAttribute("default", "data"));
			
			GroupNode dataNode = TreeFactory.createGroupNode(0);
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, NexusConstants.DATA_DATA));
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXES, new String[]{"axisX", "axisY"}));
			
			DoubleDataset data = DatasetFactory.createRange(DoubleDataset.class, 100);
			data.setShape(10, 10);
			DoubleDataset dataXY = DatasetFactory.createRange(DoubleDataset.class, 10);
			
			dataNode.addAttribute(TreeFactory.createAttribute("axisX" + NexusConstants.DATA_INDICES_SUFFIX, Long.valueOf(0)));
			dataNode.addDataNode("axisX", NexusTreeUtils.createDataNode("axisX", dataXY, null));
			dataNode.addAttribute(TreeFactory.createAttribute("axisY" + NexusConstants.DATA_INDICES_SUFFIX, Long.valueOf(1)));
			dataNode.addDataNode("axisY", NexusTreeUtils.createDataNode("axisY", dataXY, null));
			
			dataNode.addDataNode("data", NexusTreeUtils.createDataNode(NexusConstants.DATA_DATA, data, null));
			
			nxsFile.addNode("/", rootNode);
			nxsFile.addNode("/entry1", entryNode);
			nxsFile.addNode("/entry1/" + "data", dataNode);
		}
	
		NexusHDF5Loader loader = new NexusHDF5Loader();
		loader.setFile(file.getAbsolutePath());
		DataHolder dh = loader.loadFile();
		
		NexusFileConfiguration conf = new NexusFileConfiguration();
		LoadedFile f = new LoadedFile(dh);
		assertTrue(conf.configure(f));
		
		DataOptions dataOption = f.getDataOption("/entry1/data/data");
		assertNotNull(dataOption);
		assertTrue(dataOption.isSelected());
		
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testConfigurePre2014NXS() throws IOException, NexusException, ScanFileHolderException {
		// nexus layout based on old I20 file...
		
		File file = folder.newFile("pre2014.nxs");
		
		try (NexusFile nxsFile = new NexusFileHDF5(file.getAbsolutePath())) {
			nxsFile.createAndOpenToWrite();
			GroupNode rootNode = TreeFactory.createGroupNode(0);
			rootNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ROOT));
			
			GroupNode entryNode = TreeFactory.createGroupNode(0);
			entryNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			
			GroupNode dataNode = TreeFactory.createGroupNode(0);
			dataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.DATA));
			
			DoubleDataset data = DatasetFactory.createRange(DoubleDataset.class, 100);
			data.setShape(10, 10);
			DoubleDataset dataXY = DatasetFactory.createRange(DoubleDataset.class, 10);
		
			DataNode axisXNode = NexusTreeUtils.createDataNode("axisX", dataXY, null);
			axisXNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXIS, 1));
			axisXNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_PRIMARY, 1));
			dataNode.addDataNode("axisX", axisXNode);
			DataNode axisYNode = NexusTreeUtils.createDataNode("axisY", dataXY, null);
			axisYNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_AXIS, 2));
			axisYNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_PRIMARY, 1));
			dataNode.addDataNode("axisY", axisYNode);
			
			DataNode dataDataNode = NexusTreeUtils.createDataNode(NexusConstants.DATA_DATA, data, null);
			dataDataNode.addAttribute(TreeFactory.createAttribute(NexusConstants.DATA_SIGNAL, 1));
			dataNode.addDataNode("data", dataDataNode);
			
			nxsFile.addNode("/", rootNode);
			nxsFile.addNode("/entry1", entryNode);
			nxsFile.addNode("/entry1/" + "data", dataNode);
		}
	
		NexusHDF5Loader loader = new NexusHDF5Loader();
		loader.setFile(file.getAbsolutePath());
		DataHolder dh = loader.loadFile();
		
		NexusFileConfiguration conf = new NexusFileConfiguration();
		LoadedFile f = new LoadedFile(dh);
		assertTrue(conf.configure(f));
		
		DataOptions dataOption = f.getDataOption("/entry1/data/data");
		assertNotNull(dataOption);
		assertTrue(dataOption.isSelected());
	}

}
