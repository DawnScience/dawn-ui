package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataOptionsTest extends AbstractTestModel{

	private static DataOptions dataOptions;
	
	@BeforeClass
	public static void buildData() throws Exception {
			AbstractTestModel.buildData();
			dataOptions = loadedFile.getDataOptions().get(0);
	}
	@Test
	public void testDataOptions() {
		DataOptions op = new DataOptions("testName", loadedFile);
		assertEquals("testName", op.getName());
	}

//	@Test
//	public void testHasChildren() {
//		boolean children = dataOptions.hasChildren();
//		assertFalse(children);
//	}
//
//	@Test
//	public void testGetChildren() {
//		Object[] children = dataOptions.getChildren();
//		assertNull(children);
//	}

//	@Test
//	public void testGetName() {
//		assertEquals(NanoModelTestUtils.ROOT+Node.SEPARATOR+"dataset1",dataOptions.getName());
//	}

	@Test
	public void testGetFileName() {
		assertEquals(file.getAbsolutePath(),dataOptions.getFileName());
	}

//	@Test
//	public void testGetAllPossibleAxes() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetPrimaryAxes() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetData() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetAxes() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testGetPlottableObject() {
		assertNull(dataOptions.getPlottableObject());
	}

//	@Test
//	public void testSetPlottableObject() {
//		fail("Not yet implemented");
//	}

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
