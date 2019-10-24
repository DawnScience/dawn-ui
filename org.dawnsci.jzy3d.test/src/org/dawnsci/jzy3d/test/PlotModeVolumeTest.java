package org.dawnsci.jzy3d.test;

import static org.junit.Assert.*;

import org.dawnsci.jzy3d.plotmodes.PlotModeVolume;
import org.junit.Test;

public class PlotModeVolumeTest {

	@Test
	public void testGetPermutationArray() {
		TPlotModeVolume pmv = new TPlotModeVolume();
		
		String[] dims = {"Z","Y","X"};
		int[] perm = {0,1,2};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"Z","X","Y"};
		perm = new int[] {0,2,1};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		
		dims = new String[]{"Y","Z","X"};
		perm = new int[] {1,0,2};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"X","Y","Z"};
		perm = new int[] {2,1,0};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"Y","X","Z"};
		perm = new int[] {2,0,1};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"X","Z","Y"};
		perm = new int[] {1,2,0};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		//4D
		
		dims = new String[]{"","Z","Y","X"};
		perm = new int[] {0,1,2,3};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"Z","","Y","X"};
		perm = new int[] {0,1,2,3};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
		
		dims = new String[]{"Y","X","","Z"};
		perm = new int[] {3,0,2,1};
		
		assertArrayEquals(perm, pmv.getPermutationArray(dims));
	}
	
	private class TPlotModeVolume extends PlotModeVolume {
		
		public int[] getPermutationArray(Object[] options) {
			return super.getPermutationArray(options);
		}
		
		
	}
}
