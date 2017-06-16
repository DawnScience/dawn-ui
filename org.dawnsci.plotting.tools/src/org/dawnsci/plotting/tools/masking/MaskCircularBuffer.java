package org.dawnsci.plotting.tools.masking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.IntegerDataset;

public class MaskCircularBuffer {

	private IntegerDataset mask;
	private int[] shape;
	int bitMask = 1;
	boolean looped = false;
	
	public MaskCircularBuffer(int[] shape) {
		mask = DatasetFactory.zeros(IntegerDataset.class, shape);
		this.shape = shape;
	}
	
	public void maskROI(IROI roi) {
		IRectangularROI bounds = roi.getBounds();
		
		int iStart = (int)Math.floor(bounds.getPointY());
		int iStop = (int)Math.ceil(bounds.getPointY()+bounds.getLength(1));
		
		for (int i = iStart; i < iStop; i++) {
			double[] hi = roi.findHorizontalIntersections(i);
			if (hi != null) {
				boolean cutsStart = roi.containsPoint(0, i);
				boolean cutsEnd = roi.containsPoint(shape[1]-1, i);
				
				List<Integer> inters = new ArrayList<>();
				if (cutsStart) inters.add(0);
				for (double d : hi) {
					if (!inters.contains((int)d) && d > 0 && d < shape[1]-1) inters.add((int)d);
				}
				if (cutsEnd && !inters.contains(shape[1]-1)) inters.add(shape[1]-1);
				
				int[] start = new int[]{i,0};
				int[] stop = new int[]{i+1,0};
				int[] step = new int[]{1,1};
				
				while (!inters.isEmpty()) {
					
					if (inters.size() == 1) {
						start[1] = inters.get(0);
						stop[1] = start[1]+1;
						IntegerDataset data = (IntegerDataset)mask.getSliceView(start, stop, step);
						updateArray(data);
						inters.remove(0);
					} else {
						int s = inters.get(0);
						int e = inters.get(1);
						
						if (roi.containsPoint(s+(e-s)/2, i)) {
							start[1] = s;
							stop[1] = e;
							IntegerDataset data = (IntegerDataset)mask.getSliceView(start, stop, step);
							updateArray(data);
							inters.remove(0);
						} else {
							start[1] = inters.get(0);
							stop[1] = start[1]+1;
							IntegerDataset data = (IntegerDataset)mask.getSliceView(start, stop, step);
							updateArray(data);
							inters.remove(0);
						}
					}
				}
			}

		}
		
		bitMask = bitMask << 1;
		
		if (bitMask == 0) {
			looped = true;
			bitMask = 1;
		}
		
		if (looped) freeNextSlot();
		
	}
	
	public BooleanDataset getMask(){
		return Comparisons.equalTo(mask, 0);
	}
	
	public void undo(){
		bitMask = bitMask >> 1;
		IndexIterator iterator = mask.getIterator();
		
		while (iterator.hasNext()) {
			long v = mask.getElementLongAbs(iterator.index);
			mask.setAbs(iterator.index, (int)(v & ~bitMask));
		}
		
	}
	
	private void freeNextSlot(){
		
		IndexIterator iterator = mask.getIterator();
		
		int next = bitMask << 1;
		
		if (next == 0) {
			next = 1;
		}
		
		while (iterator.hasNext()) {
			long v = mask.getElementLongAbs(iterator.index);
			if (((v & bitMask) | (v & next)) != 0) {
				v = (v | (next));
			}
			v = (v & ~bitMask);
			mask.setAbs(iterator.index, (int)(v));
		}
		
	}
	
	
	public void maskThreshold(Number minNumber, Number maxNumber, Dataset data) {
		if (!Arrays.equals(mask.getShape(), data.getShape())) throw new IllegalArgumentException("must have same shape");
		
		double min = minNumber.doubleValue();
		double max = maxNumber.doubleValue();
		
		int count = 0;
		
		IndexIterator iterator = data.getIterator();
		
		while (iterator.hasNext()) {
			double element = data.getElementDoubleAbs(iterator.index);
			if (element < min || element > max) {
				long v = mask.getElementLongAbs(count);
				v= v | bitMask;
				mask.setAbs(count, (int)v);
			}
			count++;
		}
	}
	
	private void updateArray(IntegerDataset section) {
		IndexIterator iterator = section.getIterator();
		while (iterator.hasNext()) {
			long v = section.getElementLongAbs(iterator.index);
			section.setAbs(iterator.index, (int)(v | bitMask));
		}
	}
	
}
