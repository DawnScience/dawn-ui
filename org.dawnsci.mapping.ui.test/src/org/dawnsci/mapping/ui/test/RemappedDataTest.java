package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.dawnsci.mapping.ui.datamodel.LiveRemoteAxes;
import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.ReMappedData;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.junit.Test;

public class RemappedDataTest {

	@Test
	public void testLiveVersion() throws Exception{
		
		MapScanDimensions msd =new MapScanDimensions(0, 0, 1);
		
		IDynamicDataset dataset = MappedDataBlockTest.getLiveLinearDataset();
		LiveRemoteAxes axes = MappedDataBlockTest.getLiveLinearAxes();
		AxesMetadata axm = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		axm.setAxis(0, axes.getAxes()[0]);
		axm.addAxis(0, axes.getxAxisForRemapping());
		dataset.addMetadata(axm);
		Object lock = new Object();
		MappedDataBlock liveBlock = new MappedDataBlock("live", dataset,"livePath", msd, true);
		liveBlock.setLock(lock);
		ReMappedData md = new ReMappedData("map", MappedDataBlockTest.getLiveLinearMap(), liveBlock, "livePath", true);
		md.setLock(lock);
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
