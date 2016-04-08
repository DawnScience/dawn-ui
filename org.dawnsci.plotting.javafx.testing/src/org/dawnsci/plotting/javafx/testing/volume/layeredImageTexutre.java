package org.dawnsci.plotting.javafx.testing.volume;

import org.dawnsci.plotting.javafx.volume.layeredImageTexture;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.junit.Test;

public class layeredImageTexutre {

	@Test
	public void generatetextureDimensions()
	{
		System.out.println("running");
		ILazyDataset lazyDataset =  Random.lazyRand(new int[]{111,222,333});
		
//		layeredImageTexture test = new layeredImageTexture(lazyDataset);
		
		
	}
	
}
