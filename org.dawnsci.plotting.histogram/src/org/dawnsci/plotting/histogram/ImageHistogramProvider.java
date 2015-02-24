package org.dawnsci.plotting.histogram;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;

public class ImageHistogramProvider implements IHistogramProvider {

	private static final int MAX_BINS = 2048;

	private IPaletteTrace image;
	private IDataset imageDataset;
	private ImageServiceBean bean;

	public ImageHistogramProvider(IPaletteTrace image) {
		this.image = image;
		this.imageDataset = getImageData(image);
		this.bean = image.getImageServiceBean();
	}

	/**
	 * Given an image, extract the image data. If no image data is found, return some default dummy data
	 * 
	 * @param image
	 *            IPaletteTrace image
	 * @return actual 2-D data of the image
	 */
	/* protected */IDataset getImageData(IPaletteTrace image) {
		IDataset im = (Dataset) image.getImageServiceBean().getImageValue();
		if (im == null)
			im = (IDataset) image.getImageServiceBean().getImage();
		if (im == null)
			im = (IDataset) image.getData();
		// this line below looks suspect, could lead to old data being used if
		// lifecycle issues
		// if (im==null && imageDataset!=null) im = imageDataset;
		if (im == null)
			im = new DoubleDataset(new double[] { 0, 1, 2, 3 }, 2, 2);
		return im;
	}

	@Override
	public int getNumberOfBins() {
		if (((Dataset) imageDataset).hasFloatingPointElements()) {
			return MAX_BINS;
		} else {
			// set the number of points to the range
			int numBins = (Integer) imageDataset.max(true).intValue()
					- imageDataset.min(true).intValue();
			if (numBins > MAX_BINS)
				numBins = MAX_BINS;
			return numBins;
		}
	}

	@Override
	public double getMaximumRange() {
		return bean.getMaximumCutBound().getBound().doubleValue();
	}

	@Override
	public double getMininumRange() {
		return bean.getMinimumCutBound().getBound().doubleValue();
	}

	@Override
	public double getMax() {
		// TODO: null checks
		return bean.getMax().doubleValue();
	}

	@Override
	public double getMin() {
		// TODO: null checks
		return bean.getMin().doubleValue();
	}

	@Override
	public IDataset getXDataset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset getYDataset() {
		// TODO Auto-generated method stub
		return null;
	}

}
