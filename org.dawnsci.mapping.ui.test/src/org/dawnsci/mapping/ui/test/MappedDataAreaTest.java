package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.MappedDataArea;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.MappedFileFactory;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.LoaderServiceImpl;

public class MappedDataAreaTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static File grid = null;

	@BeforeClass
	public static void buildData() throws Exception {
		grid = folder.newFile("grid.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(grid.getAbsolutePath());
	}
	
	
	@Test
	public void test() throws Exception {
		IDataHolder data = LoaderFactory.getData(grid.getAbsolutePath());
		MappedDataFileBean buildBean = MapBeanBuilder.buildBean(data.getTree(), MapNexusFileBuilderUtils.STAGE_X,
				MapNexusFileBuilderUtils.STAGE_Y);
		LoaderServiceImpl loaderServiceImpl = new LoaderServiceImpl();
		MappedDataFile mdf = MappedFileFactory.getMappedDataFile(grid.getAbsolutePath(), buildBean, null, loaderServiceImpl.getData(grid.getAbsolutePath(), null));
		MappedDataArea mda = new MappedDataArea();
		mda.addMappedDataFile(mdf);
		
		assertTrue(mda.contains(grid.getAbsolutePath()));
		assertTrue(mda.isInRange(mdf));
		mda.removeFile(grid.getAbsolutePath());
		assertTrue(mda.count() == 0);
		
	}
}
