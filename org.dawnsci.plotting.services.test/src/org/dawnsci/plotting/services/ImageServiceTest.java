package org.dawnsci.plotting.services;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Maths;
import org.junit.Test;

public class ImageServiceTest {

	final static double EPSILON = 0.01;

	/**
	 * Regression tests http://jira.diamond.ac.uk/browse/DAWNSCI-5819
	 */
	@Test
	public void testLogOffset() {

		// Test dataset of 10 doubles [0.0,..9.0]
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		Dataset image = DatasetFactory.createRange(10);
		imageServiceBean.setImage(image);
		imageServiceBean.setLogColorScale(true);

		// Offset should be dataset min -(dataset range * 1e-6)
		assertEquals(-9e-6, imageServiceBean.getLogOffset(), 0.0);

		image = Maths.subtract(image, -20);
		imageServiceBean.setImage(image);
		// Offset should be dataset min -(dataset range * 1e-6)
		assertEquals(20, imageServiceBean.getLogOffset(), 1);
	}

	/**
	 * Regression tests http://jira.diamond.ac.uk/browse/DAWNSCI-5819
	 */
	@Test
	public void testPlot() {

		// Test dataset of 4 doubles [0.0,..3.0]
		Dataset image = DatasetFactory.createRange(4);
		ImageService imageService = new ImageService();

		// The expected results, to check against
		double offset = 3e-6;
		double delta = 1 - offset;
		double[] expectedResultNoLogging = { 0.0, 1.0, 2.0, 3.0 };
		double[] expectedResultWithLogging = { Math.log10(1.0 - delta),
				Math.log10(2.0 - delta), Math.log10(3.0  - delta), Math.log10(4.0 - delta) };

		Map<Boolean, double[]> expectedResults = new HashMap<Boolean, double[]>();
		expectedResults.put(false, expectedResultNoLogging);
		expectedResults.put(true, expectedResultWithLogging);

		// Checking the initial state - off by default
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		imageServiceBean.setImage(image);
		double[] imageVals = (double[]) image.getBuffer();
		assertArrayEquals(expectedResultNoLogging, imageVals, EPSILON);

		Dataset initialResult = imageService
				.getImageLoggedData(imageServiceBean);
		double actualInitialResult[] = (double[]) initialResult.getBuffer();
		assertArrayEquals(expectedResultNoLogging, actualInitialResult, EPSILON);

		// Loop for toggling on/off the logging
		for (int i = 0; i < 10; i++) {

			// We need a new bean per iteration
			ImageServiceBean imageServiceBean1 = new ImageServiceBean();
			imageServiceBean1.setImage(image);

			// Toggle logging on/off
			boolean toggle = i % 2 == 0;
			imageServiceBean1.setImage(image);
			imageServiceBean1.setLogColorScale(toggle);

			// We expect the offset to be -1 if logging on, else 0
			assertEquals(toggle ? -offset : 0, imageServiceBean1.getLogOffset(), 0.0);
			Dataset resultD = imageService
					.getImageLoggedData(imageServiceBean1);
			double result[] = (double[]) resultD.getBuffer();

			// Check the result against our expected result array
			assertArrayEquals(expectedResults.get(toggle), result, EPSILON);
		}
	}
}
