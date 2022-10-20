package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.junit.Test;

public class MockDatasetConnectorTest {

	@Test
	public void test() throws Exception {
		int[] maxShape = {-1,-1,99,100};
		
		int[] first = {1,5,99,100};
		int[] second = {2,7,99,100};
		int[] third = {7,7,99,100};
		
		DynamicRandomLazyDataset mock = new DynamicRandomLazyDataset(new int[][]{first,second,third}, maxShape);
		IDynamicDataset dataset = (IDynamicDataset)mock.getDataset();
		
		assertArrayEquals(first, dataset.getShape());
		
		IDataset slice = dataset.getSlice();
		
		assertArrayEquals(first, slice.getShape());
		
		MapTestUtils.enableIncrement(dataset);
		dataset.refreshShape();
		
		assertArrayEquals(second, dataset.getShape());
		
		slice = dataset.getSlice();
		
		assertArrayEquals(second, slice.getShape());
		
		MapTestUtils.enableIncrement(dataset);
		dataset.refreshShape();
		
		assertArrayEquals(third, dataset.getShape());
		
		slice = dataset.getSlice();
		
		assertArrayEquals(third, slice.getShape());
		
		dataset.refreshShape();
		
		assertArrayEquals(third, dataset.getShape());
		
		slice = dataset.getSlice();
		
		assertArrayEquals(third, slice.getShape());
		
	}
}
