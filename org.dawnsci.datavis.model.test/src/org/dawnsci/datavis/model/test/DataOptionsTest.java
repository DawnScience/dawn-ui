package org.dawnsci.datavis.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsUtils;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.LazyDataset;
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
		assertNotEquals(0, axisOptions.length);

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

	@Test
	public void testAxisIndexMapping() {
		int[] aShape = {12, 100, 1600};
		int[] dShape = {12, 100, 1600, 1600};

		assertArrayEquals(new int[] {0, 1, 2}, DataOptionsUtils.mapAxisToDataShape(2, aShape, dShape));
		assertArrayEquals(new int[] {0, 1, 3}, DataOptionsUtils.mapAxisToDataShape(3, aShape, dShape));
	}

	@Test
	public void testMappedAxis() {
		ILazyDataset lazy = LazyDataset.createLazyDataset(DatasetFactory.zeros(7, 15, 23));
		ILazyDataset mapped = DataOptionsUtils.getMappedAxis(4, lazy, new int[] {3, 0, 1});
		assertArrayEquals(new int[] {15, 23, 1, 7}, mapped.getShape());

		mapped = DataOptionsUtils.getMappedAxis(4, lazy, new int[] {2, 0, 1});
		assertArrayEquals(new int[] {15, 23, 7, 1}, mapped.getShape());
	}
}
