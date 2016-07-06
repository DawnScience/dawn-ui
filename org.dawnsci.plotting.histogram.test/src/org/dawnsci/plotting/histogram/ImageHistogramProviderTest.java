package org.dawnsci.plotting.histogram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.january.dataset.IDataset;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class ImageHistogramProviderTest {

	private ImageHistogramProvider provider;
	private IPaletteTrace image;

	@Before
	public void setUp() throws Exception {
		image = mock(IPaletteTrace.class);
		provider = new ImageHistogramProvider();
	}

	@Test
	public void testGetImageData() {
		// create normal data set
		// get image data, make sure it is not logged
	}
	
	// create complex data set
	// get image data, make sure it is abs... 
	
	

	@Test
	public void testGetNumberOfBins() {
		//mock.getImageData(return=float data set)
		IDataset dataset = null; // floating point dataset
		//doReturn(dataset).when(image).getSomething();
		assertEquals(2048, provider.getNumberOfBins());

		//mock.getImageData(return=integer data set, 1-5)
		assertEquals(5, provider.getNumberOfBins());

		//mock.getImageData(return=integer data set, 1-4096)
		assertEquals(2048, provider.getNumberOfBins());
	}

	@Test
	public void testGetMaximumRange() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMininumRange() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMin() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetXDataset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetYDataset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRDataset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetGDataset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBDataset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRGBDataset() {
		fail("Not yet implemented");
	}

}
