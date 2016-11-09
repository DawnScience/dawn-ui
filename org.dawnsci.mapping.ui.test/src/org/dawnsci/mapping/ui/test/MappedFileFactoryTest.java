package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map.Entry;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.MappedFileFactory;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class MappedFileFactoryTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static File file = null;
	private static File file1 = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		file1 = folder.newFile("file2.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		MapNexusFileBuilderUtils.makeGridScanWithZandSum(file1.getAbsolutePath());
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());
	}
	
	@Test
	public void loadGridScan() throws Exception{

		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(file.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadGridWithZScan() throws Exception{

		IDataHolder data = LoaderFactory.getData(file1.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(file1.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		Entry<String, MappedDataBlock> next = mdf.getDataBlockMap().entrySet().iterator().next();
		MappedDataBlock value = next.getValue();
		assertEquals(5, value.getLazy().getRank());
		
		 AbstractMapData map = mdf.getMap();
		assertEquals(3, map.getMap().getRank());
	}

}
