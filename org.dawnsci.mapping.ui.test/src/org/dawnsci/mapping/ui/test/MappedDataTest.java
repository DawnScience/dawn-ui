package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.LiveRemoteAxes;
import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.MetadataType;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class MappedDataTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static MappedDataBlock gridScanBlock = null;
	private static MappedData gridScanMap = null;
	private static File file = null;
	private static File fileRemap = null;
	private static File fileEnergy = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		
		MapScanDimensions msd = new MapScanDimensions(1, 0, 2);
		gridScanBlock = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, file.getAbsolutePath(),msd,null,false);
		
		ILazyDataset sum = data.getLazyDataset(MapNexusFileBuilderUtils.SUM_PATH);
		
		gridScanMap = new MappedData(MapNexusFileBuilderUtils.SUM_PATH, sum.getSlice(), gridScanBlock,file.getAbsolutePath(),false);
		
		fileRemap = folder.newFile("file2.nxs");
		MapNexusFileBuilderUtils.makeDiagLineScanWithSum(fileRemap.getAbsolutePath());
		
		fileEnergy = folder.newFile("file3.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithZandSum(fileEnergy.getAbsolutePath());
		
	}
	
	@Test
	public void testGetSpectrum() {
		IDataset spectrum = gridScanMap.getSpectrum(0, 0);
		Dataset d = DatasetUtils.convertToDataset(spectrum);
		assertEquals(d.getElementDoubleAbs(0), 0,0);
		assertEquals(d.getElementDoubleAbs(d.getSize()-1), d.getSize()-1,0);
	}

	@Ignore
	@Test
	public void testMakeNewMapWithParent() {
		DoubleDataset rand = Random.rand(gridScanMap.getMap().getShape());
		AxesMetadata ax = gridScanMap.getMap().getFirstMetadata(AxesMetadata.class);
		MetadataType clone = ax.clone();
		rand.setMetadata(clone);
		MappedData map = gridScanMap.makeNewMapWithParent("random", Random.rand(gridScanMap.getMap().getShape()));
		assertEquals(gridScanBlock, map.getParent());
	}

	@Test
	public void testIsLive() {
		assertFalse(gridScanMap.isLive());
	}

	@Test
	public void testGetData() {
		IDataset d = gridScanMap.getMap();
		assertNotNull(d);
		assertArrayEquals(new int[] {10, 11}, d.getShape());
	}

	@Test
	public void testHasChildren() {
		assertFalse(gridScanMap.hasChildren());
	}

	@Test
	public void testGetChildren() {
		assertNull(gridScanMap.getChildren());
	}

	@Test
	public void testGetTransparency() {
		int t = gridScanMap.getTransparency();
		assertEquals(255, t);
	}

	@Test
	public void testSetTransparency() {
		gridScanMap.setTransparency(10);
		int t = gridScanMap.getTransparency();
		assertEquals(10, t);
		gridScanMap.setTransparency(-1);
		t = gridScanMap.getTransparency();
		assertEquals(-1, t);
	}

	@Test
	public void testGetParent() {
		assertEquals(gridScanBlock, gridScanMap.getParent());
	}


	@Test
	public void testGetRange() {
		double[] range = gridScanMap.getRange();
		assertArrayEquals(new double[]{-0.5, 10.5, -0.5, 9.5}, range, 1);
	}

	@Test
	public void testGetLongName() {
		assertEquals(file.getAbsolutePath() +" : "+MapNexusFileBuilderUtils.SUM_PATH, gridScanMap.getLongName());
	}
	
	@Test
	public void testLiveVersion() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(1, 0, 2);
		
		IDynamicDataset dataset = MapTestUtils.getLiveDataset();
		LiveRemoteAxes axes = MapTestUtils.getLiveAxes();
		
		AxesMetadata axm = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		axm.setAxis(0, axes.getAxes()[0]);
		axm.setAxis(1, axes.getAxes()[1]);
		dataset.addMetadata(axm);
		Object lock = new Object();
		MappedDataBlock liveBlock =  new MappedDataBlock("live", dataset,"livePath", msd,null, true);
		liveBlock.setLock(lock);
		IDynamicDataset liveMap = MapTestUtils.getLiveMap();
		MappedData md = new MappedData("map", liveMap, liveBlock, "livePath", true);
		md.setLock(lock);
		md.update();
		
		IDataset map = md.getMap();
		
		MapTestUtils.enableIncrement(dataset,liveMap);
		md.update();
		
		map = md.getMap();
		
		assertNotNull(map);
		assertArrayEquals(new int[] {2,7}, map.getShape());
		
		AxesMetadata meta = map.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] ax = meta.getAxes();
		IDataset y = ax[0].getSlice();
		IDataset x = ax[1].getSlice();
		
		assertEquals(map.getShape()[0],y.getShape()[0]);
		assertEquals(map.getShape()[1],x.getShape()[1]);
		
		assertTrue(Comparisons.allTrue(Comparisons.isFinite(y)));
		
		MapTestUtils.enableIncrement(dataset,liveMap);
        md.update();
		
		map = md.getMap();
		
		assertNotNull(map);
		//only 6 since axes end is nan
		assertArrayEquals(new int[] {6,7}, map.getShape());
		
		meta = map.getFirstMetadata(AxesMetadata.class);
		ax = meta.getAxes();
		y = ax[0].getSlice();
		x = ax[1].getSlice();
		
		assertEquals(map.getShape()[0],y.getShape()[0]);
		assertEquals(map.getShape()[1],x.getShape()[1]);
		
		assertTrue(Comparisons.allTrue(Comparisons.isFinite(y)));
	}

	
	@Test
	public void testLiveVersionFastIsY() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(0, 1, 2);
		
		IDynamicDataset dataset = MapTestUtils.getLiveDataset();
		LiveRemoteAxes axes = MapTestUtils.getLiveAxes();
		
		AxesMetadata axm = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		axm.setAxis(0, axes.getAxes()[0]);
		axm.setAxis(1, axes.getAxes()[1]);
		dataset.addMetadata(axm);
		Object lock = new Object();
		MappedDataBlock liveBlock =  new MappedDataBlock("live", dataset,"livePath", msd,null, true);
		liveBlock.setLock(lock);
		IDynamicDataset liveMap = MapTestUtils.getLiveMap();
		MappedData md = new MappedData("map", liveMap, liveBlock, "livePath", true);
		md.setLock(lock);
		md.update();
		
		IDataset map = md.getMap();
		
		MapTestUtils.enableIncrement(dataset,liveMap);
		md.update();
		
		map = md.getMap();
		
		assertNotNull(map);
		assertArrayEquals(new int[] {7,2}, map.getShape());
		
		long size = ShapeUtils.calcLongSize(map.getShape());
		Dataset s = DatasetFactory.createRange(size);
		s.setShape(new int[] {map.getShape()[1],map.getShape()[0]});
		s = s.transpose();
		
		assertArrayEquals(((DoubleDataset)s).getData(), ((DoubleDataset)map).getData(),0);
		
		MapTestUtils.enableIncrement(dataset,liveMap);
		md.update();
		map = md.getMap();
		
		assertNotNull(map);
		//only 6 since axes end is nan
		assertArrayEquals(new int[] {7,6}, map.getShape());
		
		size = ShapeUtils.calcLongSize(map.getShape());
		s = DatasetFactory.createRange(size);
		s.setShape(new int[] {map.getShape()[1],map.getShape()[0]});
		s = s.transpose();
		
		assertArrayEquals(((DoubleDataset)s).getData(), ((DoubleDataset)map).getData(),0);
		
		AxesMetadata meta = map.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] ax = meta.getAxes();
		IDataset y = ax[0].getSlice();
		IDataset x = ax[1].getSlice();
		
		assertEquals(map.getShape()[0],y.getShape()[0]);
		assertEquals(map.getShape()[1],x.getShape()[1]);
		
		assertTrue(Comparisons.allTrue(Comparisons.isFinite(y)));
	}
	
	@Test
	public void testWithZ() throws Exception {
		IDataHolder data = LoaderFactory.getData(fileEnergy.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		
		MapScanDimensions msd = new MapScanDimensions(2, 1, 3);
		MappedDataBlock b = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, file.getAbsolutePath(),msd,null,false);
		
		ILazyDataset sum = data.getLazyDataset(MapNexusFileBuilderUtils.SUM_PATH);
		
		MappedData m = new MappedData(MapNexusFileBuilderUtils.SUM_PATH, sum.getSlice(), b,fileEnergy.getAbsolutePath(),false);
		
		IDataset spectrum = m.getSpectrum(2, 3);
		
		assertEquals(5, spectrum.getRank());
		
		assertNotNull(spectrum);
		
		IDataset map = m.getMap();
		
		assertArrayEquals(new int[] {11, 12}, map.getShape());
		
		IDataset map2 = m.getMapForDims(0, 1);
		
		assertArrayEquals(new int[] {10, 11}, map2.getShape());
		
	}
	
	@Test
	public void testFastIsY() throws Exception {
		
		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		
		MapScanDimensions msd = new MapScanDimensions(0, 1, 2);
		MappedDataBlock b = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, file.getAbsolutePath(),msd,null,false);
		
		ILazyDataset sum = data.getLazyDataset(MapNexusFileBuilderUtils.SUM_PATH);
		
		MappedData m = new MappedData(MapNexusFileBuilderUtils.SUM_PATH, sum.getSlice(), b,fileEnergy.getAbsolutePath(),false);
		
		IDataset map = m.getMap();

		assertArrayEquals(new int[] {11, 10}, map.getShape());
		
	}


}
