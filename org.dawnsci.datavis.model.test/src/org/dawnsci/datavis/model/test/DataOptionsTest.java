package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataOptionsTest extends AbstractTestModel{

	private static DataOptions dataOptions;
	
	@BeforeClass
	public static void buildData() throws Exception {
			AbstractTestModel.buildData();
			dataOptions = loadedFile.getDataOptions().get(0);
	}
	
	/**
	 * Check changing axes in NDimensions propagates to DataOptions
	 */
	@Test
	public void testSettingAxes() {
		DataOptions op = new DataOptions("/entry/dataset2", loadedFile);
		
		NDimensions nd = op.buildNDimensions();
		
		String[] axisOptions = nd.getAxisOptions(0);
		
		assertNotNull(axisOptions);
		assertTrue(axisOptions.length != 0);
		
		nd.setAxis(0, axisOptions[1]);
		AxesMetadata m = op.getLazyDataset().getFirstMetadata(AxesMetadata.class);
		
		assertTrue(axisOptions[1].contains(m.getAxis(0)[0].getName()));
		
		
	}
	
	@Test
	public void testDataOptions() {
		DataOptions op = new DataOptions("testName", loadedFile);
		assertEquals("testName", op.getName());
	}

	@Test
	public void testGetFileName() {
		assertEquals(file.getAbsolutePath(),dataOptions.getFilePath());
	}

	@Test
	public void testGetPlottableObject() {
		assertNull(dataOptions.getPlottableObject());
	}

	@Test
	public void testIsSelected() {
		assertFalse(dataOptions.isSelected());
	}

	@Test
	public void testSetSelected() {
		assertFalse(dataOptions.isSelected());
		dataOptions.setSelected(true);
		assertTrue(dataOptions.isSelected());
		dataOptions.setSelected(false);
		assertFalse(dataOptions.isSelected());
	}

}
