package org.dawnsci.mapping.ui.test;

import static org.junit.Assert.*;

import org.dawnsci.mapping.ui.datamodel.MapScanDimensions;
import org.eclipse.january.dataset.SliceND;
import org.junit.Test;

public class MapScanDimensionsTest {

	@Test
	public void testVanillaMapScan() {
		int x = 1;
		int y = 0;
		int rank = 2;
		MapScanDimensions msd = new MapScanDimensions(x, y, rank);
		
		assertEquals(x, msd.getxDim());
		assertEquals(y, msd.getyDim());
		assertEquals(rank, msd.getScanRank());
		assertNull(msd.getNonXYScanDimensions());
		assertFalse(msd.isRemappingRequired());
		assertFalse(msd.isTransposed());
		assertTrue(msd.isMapDimension(x));
		assertTrue(msd.isMapDimension(y));
		assertFalse(msd.isMapDimension(3));
		
		assertArrayEquals(new int[]{2}, msd.getDataDimensions(3));
		assertArrayEquals(new int[]{2,3}, msd.getDataDimensions(4));
		SliceND slice = msd.getSlice(0, 0, new int[]{10,10,100,100});
		assertArrayEquals(new int[]{0,0,0,0},slice.getStart());
		assertArrayEquals(new int[]{1,1,100,100},slice.getStop());
		
	}
	
	@Test
	public void testRemapScan() {
		int x = 0;
		int y = 0;
		int rank = 1;
		MapScanDimensions msd = new MapScanDimensions(x, y, rank);
		
		assertEquals(x, msd.getxDim());
		assertEquals(y, msd.getyDim());
		assertEquals(rank, msd.getScanRank());
		assertNull(msd.getNonXYScanDimensions());
		assertTrue(msd.isRemappingRequired());
		assertFalse(msd.isTransposed());
		assertTrue(msd.isMapDimension(x));
		assertTrue(msd.isMapDimension(y));
		assertFalse(msd.isMapDimension(3));
		
		assertArrayEquals(new int[]{1}, msd.getDataDimensions(2));
		assertArrayEquals(new int[]{1,2}, msd.getDataDimensions(3));
		msd.getSlice(0, 0, new int[]{10,100,100});
		
		SliceND slice = msd.getSlice(0, 0, new int[]{10,100,100});
		assertArrayEquals(new int[]{0,0,0},slice.getStart());
		assertArrayEquals(new int[]{1,100,100},slice.getStop());
		
	}
	
	@Test
	public void test3DMapScan() {
		int x = 2;
		int y = 1;
		int rank = 3;
		MapScanDimensions msd = new MapScanDimensions(x, y, rank);
		
		assertEquals(x, msd.getxDim());
		assertEquals(y, msd.getyDim());
		assertEquals(rank, msd.getScanRank());
		assertArrayEquals(new int[]{0},msd.getNonXYScanDimensions());
		assertFalse(msd.isRemappingRequired());
		assertFalse(msd.isTransposed());
		assertTrue(msd.isMapDimension(x));
		assertTrue(msd.isMapDimension(y));
		assertFalse(msd.isMapDimension(3));
		
		assertArrayEquals(new int[]{3}, msd.getDataDimensions(4));
		assertArrayEquals(new int[]{3,4}, msd.getDataDimensions(5));
		SliceND slice = msd.getSlice(0, 0, new int[]{10,10,10,100,100});
		assertArrayEquals(new int[]{0,0,0,0,0},slice.getStart());
		assertArrayEquals(new int[]{1,1,1,100,100},slice.getStop());
		msd.updateNonXYScanSlice(0, 1);
		slice = msd.getSlice(0, 0, new int[]{10,10,10,100,100});
		assertArrayEquals(new int[]{1,0,0,0,0},slice.getStart());
		assertArrayEquals(new int[]{2,1,1,100,100},slice.getStop());
		
		int x0 = 0;
		
		msd.changeXandYdims(x0, y);
		
		assertEquals(x0, msd.getxDim());
		assertEquals(y, msd.getyDim());
		assertEquals(rank, msd.getScanRank());
		assertArrayEquals(new int[]{x},msd.getNonXYScanDimensions());
		assertFalse(msd.isRemappingRequired());
		assertTrue(msd.isTransposed());
		assertTrue(msd.isMapDimension(x0));
		assertTrue(msd.isMapDimension(y));
		assertFalse(msd.isMapDimension(x));
		
		assertArrayEquals(new int[]{3}, msd.getDataDimensions(4));
		assertArrayEquals(new int[]{3,4}, msd.getDataDimensions(5));
		slice = msd.getSlice(0, 0, new int[]{10,10,10,100,100});
		assertArrayEquals(new int[]{0,0,0,0,0},slice.getStart());
		assertArrayEquals(new int[]{1,1,1,100,100},slice.getStop());
		msd.updateNonXYScanSlice(0, 1);
		slice = msd.getSlice(0, 0, new int[]{10,10,10,100,100});
		assertArrayEquals(new int[]{0,0,1,0,0},slice.getStart());
		assertArrayEquals(new int[]{1,1,2,100,100},slice.getStop());
		
	}

}
