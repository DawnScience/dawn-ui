package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class MappedDataBlockTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static MappedDataBlock gridScanBlock = null;
	private static File file = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		
		gridScanBlock = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, 1, 0, file.getAbsolutePath());
	}

	@Test
	public void testToString() {
		gridScanBlock.toString();
	}

	@Test
	public void testHasChildren() {
		assertFalse(gridScanBlock.hasChildren());
	}

	@Test
	public void testGetChildren() {
		assertNull(gridScanBlock.getChildren());
	}

	@Test
	public void testGetSpectrumIntInt() throws Exception {
		ILazyDataset spectrum = gridScanBlock.getSpectrum(0, 0);
		IDataset slice = spectrum.getSlice();
		Dataset d = DatasetUtils.convertToDataset(slice);
		assertEquals(d.getElementDoubleAbs(0), 0,0);
		assertEquals(d.getElementDoubleAbs(d.getSize()-1), d.getSize()-1,0);
	}

	@Test
	public void testGetDataDimensions() {
		int[] dd = gridScanBlock.getDataDimensions();
		assertArrayEquals(new int[]{2, 3}, dd);
	}

	@Test
	public void testGetXAxis() {
		ILazyDataset[] xAxis = gridScanBlock.getXAxis();
		assertNotNull(xAxis);
		assertNotNull(xAxis[0]);
	}

	@Test
	public void testGetYAxis() {
		ILazyDataset[] yAxis = gridScanBlock.getYAxis();
		assertNotNull(yAxis);
		assertNotNull(yAxis[0]);
	}

	@Test
	public void testIsRemappingRequired() {
		assertFalse(gridScanBlock.isRemappingRequired());
	}

	@Test
	public void testGetRange() {
		double[] range = gridScanBlock.getRange();
		assertArrayEquals(new double[]{-0.5, 10.5, -0.5, 9.5}, range, 1);
	}

	@Test
	public void testGetPath() {
		assertEquals(file.getAbsolutePath(),gridScanBlock.getPath());
	}

	@Test
	public void testGetLazy() {
		ILazyDataset lazy = gridScanBlock.getLazy();
		assertNotNull(lazy);
		assertArrayEquals(new int[] {MapNexusFileBuilderUtils.SMALLEST,MapNexusFileBuilderUtils.SMALLEST+1,100,100},lazy.getShape());
	}

	@Test
	public void testGetLongName() {
		assertEquals(file.getAbsolutePath() +" : "+MapNexusFileBuilderUtils.DETECTOR_PATH, gridScanBlock.getLongName());;
	}

	@Test
	public void testIsTransposed() {
		assertFalse(gridScanBlock.isTransposed());
	}

	@Test
	public void testGetyDim() {
		assertEquals(0,gridScanBlock.getyDim());
	}

	@Test
	public void testGetxDim() {
		assertEquals(1,gridScanBlock.getxDim());
	}

	@Test
	public void testGetySize() {
		assertEquals(MapNexusFileBuilderUtils.SMALLEST,gridScanBlock.getySize());
	}

	@Test
	public void testGetxSize() {
		assertEquals(MapNexusFileBuilderUtils.SMALLEST+1,gridScanBlock.getxSize());
	}

	@Test
	public void testIsLive() {
		assertFalse(gridScanBlock.isLive());
	}

}
