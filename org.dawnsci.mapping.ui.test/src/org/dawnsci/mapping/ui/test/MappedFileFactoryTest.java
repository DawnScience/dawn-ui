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
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.IDataset;
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
	
	private static File grid = null;
	private static File gridZ = null;
	private static File line = null;
	private static File lineZ = null;
	private static File gridDiode = null;
	private static File gridZDiode = null;
	private static File lineDiode = null;
	private static File lineZDiode = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		grid = folder.newFile("grid.nxs");
		gridZ = folder.newFile("gridZ.nxs");
		line = folder.newFile("line.nxs");
		lineZ = folder.newFile("lineZ.nxs");
		gridDiode = folder.newFile("gridDiode.nxs");
		gridZDiode = folder.newFile("gridZDiode.nxs");
		lineDiode = folder.newFile("lineDiode.nxs");
		lineZDiode = folder.newFile("lineZDiode.nxs");

		
		MapNexusFileBuilderUtils.makeGridScanWithSum(grid.getAbsolutePath());
		MapNexusFileBuilderUtils.makeGridScanWithZandSum(gridZ.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiagLineScanWithSum(line.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiagLineScanWithSumZ(lineZ.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiodeGridScan(gridDiode.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiodeGridScanEnergy(gridZDiode.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiodeLineScan(lineDiode.getAbsolutePath());
		MapNexusFileBuilderUtils.makeDiodeLineScanEnergy(lineZDiode.getAbsolutePath());
		
		
		LocalServiceManager.setLoaderService(new LoaderServiceImpl());
	}
	
	@Test
	public void loadGridScan() throws Exception{

		IDataHolder data = LoaderFactory.getData(grid.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(grid.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(grid.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadGridWithZScan() throws Exception{

		IDataHolder data = LoaderFactory.getData(gridZ.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(3, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(gridZ.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		Entry<String, MappedDataBlock> next = mdf.getDataBlockMap().entrySet().iterator().next();
		MappedDataBlock value = next.getValue();
		assertEquals(5, value.getLazy().getRank());
		
		 AbstractMapData map = mdf.getMap();
		assertEquals(2, map.getMap().getRank());
		assertEquals(3, map.getData().getRank());
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(3, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(gridZ.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadLineScan() throws Exception{

		IDataHolder data = LoaderFactory.getData(line.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(1, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(line.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		AbstractMapData map = mdf.getMap();
		assertEquals(2,map.getMap().getRank());
		assertEquals(1,map.getData().getRank());
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(1, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(line.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadLineScanZ() throws Exception{

		IDataHolder data = LoaderFactory.getData(lineZ.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(lineZ.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		assertEquals(2,mdf.getMap().getMap().getRank());
		assertEquals(2,mdf.getMap().getData().getRank());
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(lineZ.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		assertEquals(2,mdf.getMap().getMap().getRank());
		assertEquals(2,mdf.getMap().getData().getRank());
		assertTrue(mdf.getMap() instanceof ReMappedData);
	}
	
	@Test
	public void loadGridScanDiode() throws Exception {

		IDataHolder data = LoaderFactory.getData(gridDiode.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(gridDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(2, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(gridDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadGridWithZScanDiode() throws Exception{

		IDataHolder data = LoaderFactory.getData(gridZDiode.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertEquals(3, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(gridZDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		Entry<String, MappedDataBlock> next = mdf.getDataBlockMap().entrySet().iterator().next();
		MappedDataBlock value = next.getValue();
		assertEquals(3, value.getLazy().getRank());
		
		IDataset map = value.getMap();
		assertEquals(2, map.getRank());

		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertEquals(3, buildBean.getScanRank());
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(gridZDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadLineScanDiode() throws Exception{

		IDataHolder data = LoaderFactory.getData(lineDiode.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(lineDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		MappedDataBlock b = mdf.getDataBlockMap().values().iterator().next();
		assertEquals(1,b.getLazy().getRank());
		assertEquals(2,b.getMap().getRank());
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(lineDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
	}
	
	@Test
	public void loadLineScanZDiode() throws Exception{

		IDataHolder data = LoaderFactory.getData(lineZDiode.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		
		assertNotNull(buildBean);
		assertTrue(buildBean.checkValid());
		
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(lineZDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		MappedDataBlock b = mdf.getDataBlockMap().values().iterator().next();
		assertEquals(2,b.getLazy().getRank());
		assertEquals(2,b.getMap().getRank());
		
		buildBean = MapBeanBuilder.buildBean(data.getTree());
		
		assertNotNull(buildBean);
		assertTrue(buildBean.checkValid());
		
		mdf = MappedFileFactory.getMappedDataFile(lineZDiode.getAbsolutePath(), buildBean, null);
		assertNotNull(mdf);
		
		b = mdf.getDataBlockMap().values().iterator().next();
		assertEquals(2,b.getLazy().getRank());
		assertEquals(2,b.getMap().getRank());
	}

}
