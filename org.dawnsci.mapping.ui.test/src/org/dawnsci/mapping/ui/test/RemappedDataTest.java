package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import org.dawnsci.mapping.ui.datamodel.LiveRemoteAxes;
import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.junit.Test;

public class RemappedDataTest {

	@Test
	public void testLiveVersion() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(0, 0, 1);
		
		IDynamicDataset dataset = MappedDataBlockTest.getLiveLinearDataset();
		LiveRemoteAxes axes = MappedDataBlockTest.getLiveLinearAxes();
		
		MappedDataBlock liveBlock = new MappedDataBlock("live", dataset,"livePath", msd, true);
		
		ReMappedData md = new ReMappedData("map", MappedDataBlockTest.getLiveLinearMap(), liveBlock, "livePath", true);
		
		md.update();
		
		IDataset map = md.getMap();
		
		md.update();
		
		map = md.getMap();
		
		assertNotNull(map);
		
		AxesMetadata meta = map.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] ax = meta.getAxes();
		IDataset y = ax[0].getSlice();
		IDataset x = ax[1].getSlice();
		
		assertEquals(map.getShape()[0],y.getShape()[0]);
		assertEquals(map.getShape()[1],x.getShape()[1]);
		
		assertTrue(Comparisons.allTrue(Comparisons.isFinite(y)));
	}

}
