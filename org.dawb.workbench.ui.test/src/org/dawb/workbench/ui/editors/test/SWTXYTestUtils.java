package org.dawb.workbench.ui.editors.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;

public class SWTXYTestUtils {

	private SWTXYTestUtils() {
	}

	public static final List<IDataset> createTestArraysRandom(final int numberPlots, final int size) {
		
		final List<IDataset> ys = new ArrayList<IDataset>(numberPlots);
		for (int i = 0; i < numberPlots; i++) {
			final long[] buffer = new long[size];
			for (int j = 0; j < size; j++) buffer[j] = Math.round(Math.random()*10000);
			final IDataset ls = DatasetFactory.createFromObject(buffer);
			ls.setName("Test long set "+i);
			ys.add(ls);
		}
		return ys;
	}

	public static final List<IDataset> createTestArraysCoherent(final int numberPlots, final int size, final String name) {
		
		final List<IDataset> ys = new ArrayList<IDataset>(numberPlots);
		for (int i = 0; i < numberPlots; i++) {
			
			double rand = Math.random();
			
			final long[] buffer = new long[size];
			for (int j = 0; j < size; j++) buffer[j] = (long)Math.pow(j+rand, 2d)*i;
	
			final IDataset ls = (size>0) ? DatasetFactory.createFromObject(buffer) : DatasetFactory.zeros(new int[0], Dataset.INT64);
			if (name!=null) ls.setName(name+i);
			ys.add(ls);
		}
		
		return ys;
	}

}
