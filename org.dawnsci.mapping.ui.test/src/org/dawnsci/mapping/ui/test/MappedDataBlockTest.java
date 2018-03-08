package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.dawnsci.mapping.ui.datamodel.LiveRemoteAxes;
import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class MappedDataBlockTest {

	@ClassRule
	public static TemporaryFolder folder= new TemporaryFolder();
	
	private static MappedDataBlock gridScanBlock = null;
	private static MappedDataBlock gridScanBlock3D = null;
	private static MappedDataBlock gridScanBlockEnergy = null;
	private static File file = null;
	private static File file1 = null;
	private static File file2 = null;
	
	@BeforeClass
	public static void buildData() throws Exception {
		file = folder.newFile("file1.nxs");
		file1 = folder.newFile("file2.nxs");
		file2 = folder.newFile("file3.nxs");
		MapNexusFileBuilderUtils.makeGridScanWithSum(file.getAbsolutePath());
		IDataHolder data = LoaderFactory.getData(file.getAbsolutePath());
		ILazyDataset lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		MapScanDimensions msd = new MapScanDimensions(1, 0, 2);
		gridScanBlock = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, file.getAbsolutePath(),msd,false);
		
		MapNexusFileBuilderUtils.makeGridScanWithZandSum(file1.getAbsolutePath());
		
		data = LoaderFactory.getData(file1.getAbsolutePath());
		lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		msd = new MapScanDimensions(2, 1, 3);
		gridScanBlock3D = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset, file1.getAbsolutePath(), msd,false);
		
		MapNexusFileBuilderUtils.makeGridScanWithEnergyZ(file2.getAbsolutePath());
		
		data = LoaderFactory.getData(file2.getAbsolutePath());
		lazyDataset = data.getLazyDataset(MapNexusFileBuilderUtils.DETECTOR_PATH);
		msd = new MapScanDimensions(2, 1, 3);
		gridScanBlockEnergy = new MappedDataBlock(MapNexusFileBuilderUtils.DETECTOR_PATH,
				lazyDataset,file2.getAbsolutePath(),msd,false);
		
	}

	@Test
	public void testGetSpectrumIntInt() throws Exception {
		ILazyDataset spectrum = gridScanBlock.getSpectrum(0, 0);
		IDataset slice = spectrum.getSlice();
		slice.squeeze();
		assertEquals(2, slice.getRank());
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
	public void testGetRange() {
		double[] range = gridScanBlock.getRange();
		assertArrayEquals(new double[]{-0.5, 10.5, -0.5, 9.5}, range, 0.0001);
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
	public void testGetSpectrumIntInt3D() throws Exception {
		ILazyDataset spectrum = gridScanBlock3D.getSpectrum(0, 0);
		IDataset slice = spectrum.getSlice();
		slice.squeeze();
		assertEquals(2, slice.getRank());
		Dataset d = DatasetUtils.convertToDataset(slice);
		assertEquals(d.getElementDoubleAbs(0), 0,0);
		assertEquals(d.getElementDoubleAbs(d.getSize()-1), d.getSize()-1,0);
	}
	
	@Test
	public void testGetSpectrumIntIntEnergyZ() throws Exception {
		ILazyDataset spectrum = gridScanBlockEnergy.getSpectrum(0, 0);
		IDataset slice = spectrum.getSlice();
		slice.squeeze();
		assertEquals(1, slice.getRank());
		Dataset d = DatasetUtils.convertToDataset(slice);
		assertEquals(d.getElementDoubleAbs(0), 0,0);
		assertEquals(11*12*9, d.getElementDoubleAbs(d.getSize()-1),0);
	}
	
	@Test
	public void testLiveVersion() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(1, 0, 2);
		
		IDynamicDataset dataset = getLiveDataset();
		LiveRemoteAxes axes = getLiveAxes();
		
		AxesMetadata axm = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		axm.setAxis(0, axes.getAxes()[0]);
		axm.setAxis(1, axes.getAxes()[0]);
		dataset.addMetadata(axm);
		
		Object lock = new Object();
		
		MappedDataBlock liveBlock = new MappedDataBlock("live", dataset,"livePath", msd, true);
		liveBlock.setLock(lock);
		ILazyDataset spectrum = liveBlock.getSpectrum(1, 1);
		
		assertNotNull(spectrum);
		
		spectrum = liveBlock.getSpectrum(9, 9);
		
		assertNotNull(spectrum);
		
	}
	
	@Test
	public void testLivePointVersion() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(1, 0, 2);
		
		IDynamicDataset dataset = getLivePointDataset();
		LiveRemoteAxes axes = getLiveAxes();
		
		AxesMetadata axm = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		axm.setAxis(0, axes.getAxes()[0]);
		axm.setAxis(1, axes.getAxes()[0]);
		dataset.addMetadata(axm);
		
		MappedDataBlock liveBlock =  new MappedDataBlock("live", dataset,"livePath", msd, true);
		Object lock = new Object();
		liveBlock.setLock(lock);
		liveBlock.update();
		liveBlock.update();
		
		IDataset map = liveBlock.getMap();
		
		assertNotNull(map);
		
	}
	
	
	public static IDynamicDataset getLivePointDataset(){
		
		int[] maxShape = {-1,-1,1,1};
		
		int[] first = {1,5,1,1};
		int[] second = {2,7,1,1};
		int[] third = {7,7,1,1};
		
		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);
		
		return mock;
		
	}
	
	public static IDynamicDataset getLiveDataset(){

		int[] maxShape = {-1,-1,99,100};

		int[] first = {1,5,99,100};
		int[] second = {2,7,99,100};
		int[] third = {7,7,99,100};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static IDynamicDataset getLiveLinearDataset(){

		int[] maxShape = {-1,99,100};

		int[] first = {5,99,100};
		int[] second = {10,7,99,100};
		int[] third = {20,7,99,100};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}
	
	public static IDynamicDataset getLiveLinearMap(){

		int[] maxShape = {-1};

		int[] first = {5};
		int[] second = {10};
		int[] third = {20};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static LiveRemoteAxes getLiveLinearAxes() {

		int[] first = {5};
		int[] second = {10};
		int[] third = {20};

		IDynamicDataset x = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{20});
		IDynamicDataset y = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{20});
		((DynamicRandomLazyDataset)y).setEndNan(true);
		IDynamicDataset[] ax = new IDynamicDataset[]{y,null,null};
		String[] names = new String[]{"y",null,null};
		LiveRemoteAxes axes= new LiveRemoteAxes(ax, names, "host", 8690);
		axes.setxAxisForRemapping(x);
		axes.setxAxisForRemappingName("x");

		return axes;
	}
	
	
	
	public static IDynamicDataset getLiveMap(){

		int[] maxShape = {-1,-1};

		int[] first = {1,5};
		int[] second = {2,7};
		int[] third = {7,7};

		IDynamicDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third},maxShape);

		return mock;

	}

	public static LiveRemoteAxes getLiveAxes() {

		int[] first = {4};
		int[] second = {7};
		int[] third = {7};

		IDynamicDataset x = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{7});

		first = new int[]{1};
		second = new int[]{3};
		third = new int[]{7};
		IDynamicDataset y = new DynamicRandomLazyDataset(new int[][]{first,second,third},new int[]{7});
		((DynamicRandomLazyDataset)y).setEndNan(true);
		IDynamicDataset[] ax = new IDynamicDataset[]{(IDynamicDataset)y.getDataset(),(IDynamicDataset)x.getDataset(),null,null};
		String[] names = new String[]{"y","x",null,null};
		LiveRemoteAxes axes= new LiveRemoteAxes(ax, names, "host", 8690);

		return axes;
	}
}
