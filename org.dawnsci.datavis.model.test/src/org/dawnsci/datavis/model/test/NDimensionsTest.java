package org.dawnsci.datavis.model.test;

import static org.junit.Assert.*;

import org.dawnsci.datavis.model.NDimensions;
import org.eclipse.january.dataset.SliceND;
import org.junit.Test;

public class NDimensionsTest {

	@Test
	public void test() {
		int[] shape = new int[]{9,10,11,12};
		NDimensions ndims = new NDimensions(shape);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 1, 11, 12}, s.getStop());
		
		String[] optionsX = new String[]{"X"};
		ndims.setOptions(optionsX);
		
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
	public void testSingleDimensions() {
		int[] shape = new int[]{1,10,1,12};
		NDimensions ndims = new NDimensions(shape);
		String[] optionsXY = new String[]{"X","Y"};
		ndims.setOptions(optionsXY);
		
		SliceND s = ndims.buildSliceND();
		assertArrayEquals(new int[]{0, 0, 0,0}, s.getStart());
		assertArrayEquals(new int[]{1, 10, 1, 12}, s.getStop());
		
		String[] optionsX = new String[]{"X"};
		ndims.setOptions(optionsX);
		
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

}
