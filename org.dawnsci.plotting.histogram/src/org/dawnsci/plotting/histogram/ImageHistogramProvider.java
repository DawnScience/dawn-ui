package org.dawnsci.plotting.histogram;

import java.util.List;

import org.dawnsci.plotting.histogram.ui.HistogramViewer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;

public class ImageHistogramProvider implements IHistogramProvider {

	private static final int MAX_BINS = 2048;

	private IPaletteTrace image;
	private IPaletteListener imageListener = new ImagePaletteListener();

	protected HistogramViewer histogramViewer;

	private IDataset imageDataset;
	private ImageServiceBean bean;
	private PaletteData paletteData;

	/**
	 * Calculated histogram, index 0 for Y values, 1 for X values
	 */
	private IDataset[] histogramValues;

	public ImageHistogramProvider() {

	}

	private void resetImage(){
		this.image = null;
		this.imageDataset = null;
		this.bean = null;
		this.paletteData = null;
	}

	private void setImage(IPaletteTrace image){
		//TODO: connect and disconect listeners, etc...
		this.image = image;
		this.imageDataset = getImageData(image);
		this.bean = image.getImageServiceBean();
		this.paletteData = image.getPaletteData();
	}

	/**
	 * Given an image, extract the image data. If no image data is found, return
	 * some default dummy data
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

	public double getMaximumRange() {
		double max = bean.getMaximumCutBound().getBound().doubleValue();
		if (Double.isInfinite(max))
			max = imageDataset.max(true).doubleValue();
		return max;
	}

	public double getMininumRange() {
		double rMin = bean.getMinimumCutBound().getBound().doubleValue();
		if (Double.isInfinite(rMin))
			rMin = imageDataset.min(true).doubleValue();
		return rMin;
	}

	/**
	 * This implementation masks the inconsistencies of the imagebean
	 * i.e. sometimes it can be in an intermittent state, where min is set but not max.
	 * We default null values to 0.
	 */
	@Override
	public double getMax() {
		double maxValue = doubleValue(bean.getMax());
		double minValue = doubleValue(bean.getMin());
		if (maxValue < minValue){
			maxValue = minValue;
		}

		return maxValue;
	}



	/**
	 * This implementation masks the inconsistencies of the imagebean
	 * i.e. sometimes it can be in an intermittent state, where min is set but not max.
	 * We default null values to 0.
	 */
	@Override
	public double getMin() {
		double minValue = doubleValue(bean.getMin());
		double maxValue = doubleValue(bean.getMax());
		if (minValue > maxValue){
			minValue = maxValue;
		}

		return minValue;
	}

	/**
	 * Utility for obtaining min and max with defaults
	 *
	 * @return value as double or  0 as default if no value is available
	 */
	private double doubleValue(Number n) {
		if (n == null)
			return 0;
		return n.doubleValue();
	}


	/**
	 * This will take an image, and pull out all the parameters required to
	 * calculate the histogram
	 *
	 * @return Calculated histogram, index 0 for Y values, 1 for X values
	 */
	private IDataset[] generateHistogramData(IDataset imageDataset, int numBins) {
		double rangeMax = getMaximumRange();
		double rangeMin = getMininumRange();

		Histogram hist = new Histogram(numBins, rangeMin, rangeMax, true);
		List<? extends Dataset> histogram_values = hist.value(imageDataset);

		Dataset histogramX = histogram_values.get(1).getSliceView(
				new Slice(numBins));
		histogramX.setName("Intensity");

		Dataset histogramY = histogram_values.get(0);
		histogramY = Maths.log10((Maths.add(histogramY, 1.0)));
		histogramY.setName("Histogram");

		return new IDataset[] { histogramY, histogramX };
	}

	@Override
	public IHistogramDatasets getDatasets() {
		Assert.isNotNull(image, "This provider must have an image set when get datasets is called");

		//getmin and max - validate values and swap
		//validate these are good numbers
		double histoMin = getMin();
		double histoMax = getMax();


		IDataset[] histogramData = generateHistogramData(imageDataset,
				getNumberOfBins());
		final IDataset histogramY = histogramData[0];
		final IDataset histogramX = histogramData[1];

		// now build the RGB Lines ( All the -3's here are to avoid the
		// min/max/NAN colours)
		PaletteData paletteData = image.getPaletteData();
		final DoubleDataset R = new DoubleDataset(paletteData.colors.length - 3);
		final DoubleDataset G = new DoubleDataset(paletteData.colors.length - 3);
		final DoubleDataset B = new DoubleDataset(paletteData.colors.length - 3);
		final DoubleDataset RGBX_orig_calc = new DoubleDataset(
				paletteData.colors.length - 3);
		R.setName("red");
		G.setName("green");
		B.setName("blue");
		RGBX_orig_calc.setName("Axis (orig calc)");

		double scale = ((histogramY.max(true).doubleValue()) / 256.0);
		if (scale <= 0)
			scale = 1.0 / 256.0;

		// palleteData.colors = new RGB[256];
		for (int i = 0; i < paletteData.colors.length - 3; i++) {
			R.set(paletteData.colors[i].red * scale, i);
			G.set(paletteData.colors[i].green * scale, i);
			B.set(paletteData.colors[i].blue * scale, i);

			// why not length -3???
			RGBX_orig_calc
					.set(histoMin
							+ (i * ((histoMax - histoMin) / paletteData.colors.length)),
							i);
		}

		final Dataset RGBX = DatasetUtils.linSpace(histoMin, histoMax,
				paletteData.colors.length - 3, Dataset.FLOAT64);
		RGBX.setName("Axis");

		return new IHistogramDatasets() {

			@Override
			public IDataset getY() {
				return histogramY;
			}

			@Override
			public IDataset getX() {
				return histogramX;
			}

			@Override
			public IDataset getRGBX() {
				return RGBX;
			}

			@Override
			public IDataset getR() {
				return R;
			}

			@Override
			public IDataset getG() {
				return G;
			}

			@Override
			public IDataset getB() {
				return B;
			}
		};
	}


	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMax(double max) {
		image.setMax(max);

	}

	@Override
	public void setMin(double min) {
		image.setMin(min);
	}

	private final class ImagePaletteListener extends IPaletteListener.Stub{
		@Override
		public void minChanged(PaletteEvent event) {
			histogramViewer.refresh();
		}

		@Override
		public void maxChanged(PaletteEvent event) {
			histogramViewer.refresh();
		}
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.histogramViewer = (HistogramViewer) viewer;

		if (newInput != oldInput){
			//remove listeners on old input
			if (oldInput != null){
				IPaletteTrace oldImage = (IPaletteTrace) oldInput;
				oldImage.removePaletteListener(imageListener);
			}
			// reset cached input

			//setImage
			if (newInput instanceof IPaletteTrace){
				IPaletteTrace image = (IPaletteTrace) newInput;
				setImage(image);
				image.addPaletteListener(imageListener);
				// set listeners
			}

		}
	}
}
