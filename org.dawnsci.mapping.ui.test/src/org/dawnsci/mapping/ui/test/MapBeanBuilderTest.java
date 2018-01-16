package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class MapBeanBuilderTest {

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
	}
	
	
	@Test
	public void testBuildBean() throws Exception {

		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree());
		assertNull(buildBean.getBlocks().get(0).getxAxisForRemapping());
		assertTrue(buildBean.checkValid());
	}
	
	@Test
	public void testBuildBeanAxesNamesGridScan() throws Exception {

		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		assertTrue(buildBean.checkValid());
	}
	
	@Test
	public void testBuildBeanAxesNamesGridScanWithZ() throws Exception {

		IDataHolder data = LoaderFactory.getData(file1.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		assertNotNull(buildBean);
		assertTrue(buildBean.checkValid());
	}
}
