package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.SliceND;

public class SliceNDUtils {

	
	public static SliceND getRootSlice(SliceND first, SliceND second) {
		
		if (first.getShape().length != second.getShape().length) return null;
		
		SliceND output = new SliceND(first.getMaxShape());
		
		int[] start0 = first.getStart();
		int[] step0 = first.getStep();
		
		int[] start1 = second.getStart();
		int[] stop1 = second.getStop();
		int[] step1 = second.getStep();
		
		for (int i = 0 ; i < first.getShape().length; i++) {
			int start = start0[i] + start1[i];
			int stop = start + ((stop1[i]-start1[i])*step0[i]);
			int step = step0[i]*step1[i];
			
			output.setSlice(i,start,stop,step);
			
		}
		
		return output;
		
	}
	
}
