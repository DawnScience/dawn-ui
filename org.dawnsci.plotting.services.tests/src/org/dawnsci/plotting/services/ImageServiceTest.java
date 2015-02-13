package org.dawnsci.plotting.services;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.junit.Test;

public class ImageServiceTest {

	@Test
	public void testLogOffset() {

		ImageServiceBean imageServiceBean = new ImageServiceBean();
		Dataset image = AbstractDataset.arange(10, AbstractDataset.FLOAT64);
		imageServiceBean.setImage(image);
		imageServiceBean.setLogColorScale(true);
		assertEquals(-1, imageServiceBean.getLogOffset(), 0.0);

	}

	@Test
	public void testPlot() {
		
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		Dataset image = AbstractDataset.arange(10, AbstractDataset.FLOAT64);
		imageServiceBean.setImage(image);
		ImageService imageService = new ImageService();
		
		double epsilon = 0.01;
		
		
		double expectedResultNoLogging[] = {0.0,
				1.0,
				2.0,
				3.0,
				4.0,
				5.0,
				6.0,
				7.0,
				8.0,
				9.0
		};
		
		double expectedResultWithLogging[] = {Math.log10(1.0), 
				Math.log10(2.0),
				Math.log10(3.0),
				Math.log10(4.0),
				Math.log10(5.0),
				Math.log10(6.0),
				Math.log10(7.0),
				Math.log10(8.0),
				Math.log10(9.0),
				Math.log10(10.0),
		};

		
		// Off by default
		double[] imageVals = (double[])image.getBuffer();
		assertArrayEquals(expectedResultNoLogging, imageVals, epsilon);
		
		Dataset initialResult = imageService.getImageLoggedData(imageServiceBean);
		double actualInitialResult[] = (double[])initialResult.getBuffer();
		assertArrayEquals(expectedResultNoLogging, actualInitialResult, epsilon);

		
		// Check on
		imageServiceBean.setLogColorScale(true);
		
		double[] imageVals1 = (double[])image.getBuffer();
		assertArrayEquals(expectedResultNoLogging, imageVals1, epsilon);
		
		assertEquals(-1, imageServiceBean.getLogOffset(), 0.0);
		Dataset resultLoggingOn = imageService.getImageLoggedData(imageServiceBean);
		double actualResultWithLogging[] = (double[])resultLoggingOn.getBuffer();
		assertArrayEquals(expectedResultWithLogging, actualResultWithLogging, epsilon);
		
		
		// Check off
		imageServiceBean.setLogColorScale(false);
		
		double[] imageVals2 = (double[])image.getBuffer();
		assertArrayEquals(expectedResultNoLogging, imageVals2, epsilon);
		
		assertEquals(0.0, imageServiceBean.getLogOffset(), 0.0);
		Dataset resultLoggingOff = imageService.getImageLoggedData(imageServiceBean);
		double actualResultNoLogging[] = (double[])resultLoggingOff.getBuffer();
		assertArrayEquals(expectedResultNoLogging, actualResultNoLogging, epsilon);
	

	}
}
