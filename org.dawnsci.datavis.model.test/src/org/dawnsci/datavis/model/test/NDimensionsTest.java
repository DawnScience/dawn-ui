package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.junit.Test;

public class NDimensionsTest {

	/**
	 * Test default slicing for options arrays,
	 * building SliceND, updating options,
	 * checking slice and options updated
	 */
	@Test
	public void test() {
		int[] shape = new int[]{9,10,11,12};
		NDimensions ndims = new NDimensions(shape, null);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		//test slice is fastest image
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 11, 12}, s.getStop());
		
		String[] optionsX = new String[]{"X"};
		ndims.setOptions(optionsX);
		
		//test slice is fastest line
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 1, 12}, s.getStop());
		
		//test dimensions correct
		String[] dimOptions = ndims.getDimensionOptions();
		assertEquals(2, dimOptions.length);
		assertEquals(optionsX[0], dimOptions[0]);
		//and descriptions either empty or optionsX[0]
		for (int i = 0; i < ndims.getRank(); i++) {
			String des = ndims.getDescription(i);
			if (des != null && !des.equals("") && !des.equals(optionsX[0])) {
				fail();
			}
		}
	}
	
	/**
	 * Test correct assignement with single dimensions
	 * in the shape
	 */
	@Test
	public void testSingleDimensions() {
		int[] shape = new int[]{1,10,1,12};
		NDimensions ndims = new NDimensions(shape,null);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		//Test fastest image ignoring dims of 1
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 10, 1, 12}, s.getStop());
		
		String[] optionsX = new String[]{"X"};
		ndims.setOptions(optionsX);
		
		//test update to 1d
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 1, 12}, s.getStop());
		
		String[] dimOptions = ndims.getDimensionOptions();
		assertEquals(2, dimOptions.length);
		assertEquals(optionsX[0], dimOptions[0]);
		for (int i = 0; i < ndims.getRank(); i++) {
			String des = ndims.getDescription(i);
			if (des != null && !des.equals("") && !des.equals(optionsX[0])) {
				fail();
			}
		}
	}
	
	@Test
	public void testSingleDimensionsGrow() {
		int[] shape = new int[]{1,1};
		NDimensions ndims = new NDimensions(shape,null);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0,}, s.getStart());
		assertArrayEquals(new int[]{1, 1}, s.getStop());
		
		ndims.updateShape(new int[]{1,3});
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0,}, s.getStart());
		assertArrayEquals(new int[]{1, 3}, s.getStop());
		
		ndims.updateShape(new int[]{1,5});
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0,}, s.getStart());
		assertArrayEquals(new int[]{1, 5}, s.getStop());
		
		ndims.updateShape(new int[]{2,5});
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0,}, s.getStart());
		assertArrayEquals(new int[]{2, 5}, s.getStop());
		
		ndims.updateShape(new int[]{5,5});
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0,}, s.getStart());
		assertArrayEquals(new int[]{5, 5}, s.getStop());
		
	}
	
	
	/**
	 * Test the slice changes correctly when updateShape is called
	 */
	@Test
	public void testUpdateShapeGrow() {
		int[] shape = new int[]{1,1,11,12};
		NDimensions ndims = new NDimensions(shape,null);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		//test fastest image sliced
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 11, 12}, s.getStop());
		
		ndims.updateShape(new int[]{1,3,11,12});
		
		//test slice changed
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 2, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{1, 3, 11, 12}, s.getStop());
		
		ndims.updateShape(new int[]{1,10,11,12});
		
		//test slice changed
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 9, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{1, 10, 11, 12}, s.getStop());
		
		ndims.updateShape(new int[]{2,10,11,12});
		
		//test slice changed
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{1, 9, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{2, 10, 11, 12}, s.getStop());
		
		ndims.updateShape(new int[]{9,10,11,12});
		
		//test slice changed
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{8, 9, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{9, 10, 11, 12}, s.getStop());
		
	}
	
	/**
	 * Test the slice changes correctly when updateShape is called
	 */
	@Test
	public void testUpdateShapeShrink() {
		int[] shape = new int[]{9,10,11,12};
		NDimensions ndims = new NDimensions(shape,null);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		ndims.setSlice(0, new Slice(4, 5));
		ndims.setSlice(1, new Slice(5, 6));
		
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{4, 5, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{5, 6, 11, 12}, s.getStop());
		
		NDimensions ndimsc = new NDimensions(ndims);
		s = ndimsc.buildSliceND();
		assertArrayEquals(new int[]{4, 5, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{5, 6, 11, 12}, s.getStop());
		
		ndimsc.updateShape(new int[] {1, 1, 11, 12});
		s = ndimsc.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 11, 12}, s.getStop());
		Slice slice = ndimsc.getSlice(0);
		assertEquals(0,slice.getStart().intValue());
		assertEquals(1,slice.getStop().intValue());
		slice = ndimsc.getSlice(1);
		assertEquals(0,slice.getStart().intValue());
		assertEquals(1,slice.getStop().intValue());
		
		s = ndims.buildSliceND();
		assertArrayEquals(new int[]{4, 5, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{5, 6, 11, 12}, s.getStop());
		
		NDimensions ndimsc2 = new NDimensions(ndims);
		ndimsc2.updateShape(new int[] {6, 6, 11, 12});
		s = ndimsc2.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{6, 6, 11, 12}, s.getStop());
		
		NDimensions ndimsc3 = new NDimensions(ndims);
		ndimsc3.updateShape(new int[] {0, 6, 11, 12});
		s = ndimsc3.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0, 0}, s.getStart());
		assertArrayEquals(new int[]{0, 6, 11, 12}, s.getStop());
		
	}
	
	/**
	 * Test correct axes added
	 */
	@Test
	public void testAxes() {
		int[] shape = new int[]{1,10,1,12};
		int[] maxshape = new int[]{-1,10,1,12};
		String name = "/entry/test/data";	
		String nameg = "/entry/test/grow";
		int[] maxg = new int[]{-1};
		String[] primary = {"zero","one","two","three"};
		Map<String, int[]> p = new HashMap<>();
		p.put(name, shape);
		for (int i = 0; i < shape.length; i++) {
			p.put(primary[i], new int[]{shape[i]});
		}
		Map<String, int[]> m = new HashMap<>();
		m.put(name, maxshape);
		m.put(nameg, maxg);
		
		NDimensions ndims = new NDimensions(shape,null);

		ndims.setUpAxes(name, p, primary, m);
		
		String[] axisOptions = ndims.getAxisOptions(0);
		
		assertTrue(Arrays.stream(axisOptions).anyMatch(s -> s.equals(nameg)));
	}

}
