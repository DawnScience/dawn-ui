package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class LoadedFileTest  extends AbstractTestModel {
	
	@Test
	public void testLoadedFile() throws Exception {
		assertEquals(file.getAbsolutePath(), loadedFile.getLongName());
	}

	@Test
	public void testHasChildren() {
		assertTrue(loadedFile.hasChildren());
	}

	@Test
	public void testGetChildren() {
		assertNotNull(loadedFile.getChildren());
	}

	@Test
	public void testGetDataOptions() {
		List<DataOptions> dataOptions = loadedFile.getDataOptions();
		//single element dataset not included
		assertEquals(nameShapeMap.size()-1, dataOptions.size());
	}

	@Test
	public void testGetName() {
		assertEquals(file.getName(), loadedFile.getName());
	}

	@Test
	public void testGetLongName() {
		assertEquals(file.getAbsolutePath(), loadedFile.getLongName());
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

}
