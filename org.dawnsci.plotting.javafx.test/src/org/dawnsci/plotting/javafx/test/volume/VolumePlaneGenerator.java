package org.dawnsci.plotting.javafx.test.volume;

import java.awt.image.BufferedImage;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class VolumePlaneGenerator {
	private double max;
	private double min;
	private ILazyDataset lazySlice;

	private BufferedImage[] xyPlanes;
	private BufferedImage[] zyPlanes;
	private BufferedImage[] zxPlanes;

	private double resolution;

	public VolumePlaneGenerator(double resolution, ILazyDataset dataset) throws DatasetException {
		this.max = dataset.getSlice().max(true, true).doubleValue();
		this.min = dataset.getSlice().min(true, true).doubleValue();

		this.resolution = resolution;

		int[] step = new int[] { (int) (dataset.getShape()[0] / (dataset.getShape()[0] * this.resolution) + 0.5f),
				(int) (dataset.getShape()[1] / (dataset.getShape()[1] * this.resolution) + 0.5f),
				(int) (dataset.getShape()[2] / (dataset.getShape()[2] * this.resolution) + 0.5f) };

		lazySlice = dataset.getSliceView(new int[] { 0, 0, 0 }, dataset.getShape(), step);
	}

	private BufferedImage[] createPlaneArray(double transparency, ILazyDataset lazySlice) throws DatasetException {
		if (transparency > 1 || transparency < 0)
			throw new IllegalArgumentException("transparency must be between 0 and 1");

		int opacity = (int) (255 * transparency);

		BufferedImage[] outputBIArray = new BufferedImage[lazySlice.getShape()[2]];

		for (int z = 0; z < lazySlice.getShape()[2]; z++) {
			IDataset slice = lazySlice.getSlice(new int[] { 0, 0, z },
					new int[] { lazySlice.getShape()[0], lazySlice.getShape()[1], z + 1 }, new int[] { 1, 1, 1 });

			BufferedImage bi = new BufferedImage(slice.getShape()[0], slice.getShape()[1], BufferedImage.TYPE_INT_ARGB);

			for (int y = 0; y < slice.getShape()[1]; y++) {
				for (int x = 0; x < slice.getShape()[0]; x++) {
					int value = (int) (((slice.getInt(x, y, 0) / max) * opacity) + 0.5f);

					int argb = value;
					argb = (argb << 8) + 255;
					argb = (argb << 8) + 0;
					argb = (argb << 8) + 0;

					bi.setRGB(x, y, argb);

				}
			}

			outputBIArray[z] = bi;
		}

		return outputBIArray;
	}

	public void createImagePlanes() throws DatasetException {
		xyPlanes = createPlaneArray(resolution, lazySlice.getSlice());
		zyPlanes = createPlaneArray(resolution, lazySlice.getTransposedView(1, 2, 0).getSlice());
		zxPlanes = createPlaneArray(resolution, lazySlice.getTransposedView(2, 0, 1).getSlice());
	}

	public BufferedImage[] getPlane_XY() {
		return xyPlanes;
	}

	public BufferedImage[] getPlane_ZY() {
		return zyPlanes;
	}

	public BufferedImage[] getPlane_ZX() {
		return zxPlanes;
	}
}