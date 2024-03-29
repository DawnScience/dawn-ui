/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.services.util;

import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ITransferFunction;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

/**
 * Helper methods to convert to SWT images from datasets
 */
public class SWTImageUtils {

	private static ImageData createImageFromRGBADataset(RGBDataset rgbdata, long minv, long maxv)
	{
		ImageData img;
		final IndexIterator iter = rgbdata.getIterator(true);
		final int[] pos = iter.getPos();
		final int[] shape = rgbdata.getShape();
		final int height = shape[0];
		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
		long delta = maxv - minv;
		short off = (short) minv;

		short[] data = rgbdata.getData();
		if (delta < 32) { // 555
			img = new ImageData(width, height, 16, new PaletteData(0x7c00, 0x03e0, 0x001f));
			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0x1f) << 10) | (((data[n + 1] - off) & 0x1f) << 5) | ((data[n + 2] - off)& 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 64) { // 565
			img = new ImageData(width, height, 16, new PaletteData(0xf800, 0x07e0, 0x001f));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> 1) & 0x1f) << 10) | (((data[n + 1] - off) & 0x3f) << 5) | (((data[n + 2] - off) >> 1) & 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 256) { // 888
			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0xff) << 16) | (((data[n + 1] - off) & 0xff) << 8) | ((data[n + 2] - off) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else {
			int shift = 0;
			while (delta >= 256) {
				shift++;
				delta >>= 1;
			}

			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> shift) & 0xff) << 16) | ((((data[n + 1] - off) >> shift) & 0xff) << 8) | (((data[n + 2] - off) >> shift) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		}
		return img;
	}
	
	private static ImageData createImageFromRGBADataset(RGBByteDataset rgbdata, long minv, long maxv)
	{
		ImageData img;
		final IndexIterator iter = rgbdata.getIterator(true);
		final int[] pos = iter.getPos();
		final int[] shape = rgbdata.getShape();
		final int height = shape[0];
		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
		long delta = maxv - minv;
		short off = (short) minv;

		byte[] data = rgbdata.getData();
		if (delta < 256 && rgbdata.getStrides() == null) { // can use data directly
			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff), 3*width, rgbdata.getData());
		} else if (delta < 32) { // 555
			img = new ImageData(width, height, 16, new PaletteData(0x7c00, 0x03e0, 0x001f));
			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0x1f) << 10) | (((data[n + 1] - off) & 0x1f) << 5) | ((data[n + 2] - off)& 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 64) { // 565
			img = new ImageData(width, height, 16, new PaletteData(0xf800, 0x07e0, 0x001f));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> 1) & 0x1f) << 10) | (((data[n + 1] - off) & 0x3f) << 5) | (((data[n + 2] - off) >> 1) & 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 256) { // 888
			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0xff) << 16) | (((data[n + 1] - off) & 0xff) << 8) | ((data[n + 2] - off) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else {
			int shift = 0;
			while (delta >= 256) {
				shift++;
				delta >>= 1;
			}

			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> shift) & 0xff) << 16) | ((((data[n + 1] - off) >> shift) & 0xff) << 8) | (((data[n + 2] - off) >> shift) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		}
		return img;
	}

	static private ImageData createImageFromDataset(Dataset a,
													double minv,
													double maxv,
													ITransferFunction redFunc,
													ITransferFunction greenFunc,
													ITransferFunction blueFunc,
													boolean inverseRed,
													boolean inverseGreen,
													boolean inverseBlue)  {
		final int[] shape = a.getShape();
		final int height = shape[0];
		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
		ImageData img;
		final IndexIterator iter = a.getIterator(true);
		final int[] pos = iter.getPos();
		img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));
		double delta = maxv - minv;
		while (iter.hasNext()) {
			double value = (a.getElementDoubleAbs(iter.index) - minv)/delta;
			final int red = (inverseRed ? (255-redFunc.mapToByte(value)) : redFunc.mapToByte(value));
			final int green = (inverseGreen ? (255-greenFunc.mapToByte(value)) : greenFunc.mapToByte(value));
			final int blue = (inverseBlue ? (255-blueFunc.mapToByte(value)) : blueFunc.mapToByte(value));
			final int rgb = (red << 16) | green << 8 | blue; 		
			img.setPixel(pos[1], pos[0],rgb); 
		}
		return img;
	}

	static private ImageData createImageFromDataset(Dataset a, PaletteData paletteData) throws Exception {
		final IImageService iservice = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
		ImageServiceBean ibean = new ImageServiceBean();
		ibean.setImage(a);
		ibean.setPalette(paletteData);
		return iservice.getImageData(ibean);
	}

	/**
	 * Create SWT ImageData from a dataset
	 * <p>
	 * The input dataset can be a RGB dataset in which case the mapping functions
	 * and inversion flags are ignored.
	 * @param a dataset
	 * @param max maximum value of dataset
	 * @param redFunc
	 * @param greenFunc
	 * @param blueFunc
	 * @param inverseRed
	 * @param inverseGreen
	 * @param inverseBlue
	 * @return an ImageData object for SWT
	 */
	static public ImageData createImageData(Dataset a, Number max,
											ITransferFunction redFunc,
											ITransferFunction greenFunc,
											ITransferFunction blueFunc,
											boolean inverseRed,
											boolean inverseGreen,
											boolean inverseBlue) {
		return createImageData(a, 0, max, redFunc, greenFunc, blueFunc, inverseRed, inverseGreen, inverseBlue);
	}

	/**
	 * Create SWT ImageData from a dataset
	 * <p>
	 * The input dataset can be a RGB dataset in which case the mapping functions
	 * and inversion flags are ignored.
	 * @param a dataset
	 * @param min minimum value of dataset
	 * @param max maximum value of dataset
	 * @param redFunc
	 * @param greenFunc
	 * @param blueFunc
	 * @param inverseRed
	 * @param inverseGreen
	 * @param inverseBlue
	 * @return an ImageData object for SWT
	 */
	static public ImageData createImageData(Dataset a, Number min, Number max,
											ITransferFunction redFunc,
											ITransferFunction greenFunc,
											ITransferFunction blueFunc,
											boolean inverseRed,
											boolean inverseGreen,
											boolean inverseBlue) {
		ImageData img;

		if (a instanceof RGBByteDataset) {
			img = createImageFromRGBADataset((RGBByteDataset) a, min.longValue(), max.longValue());
		} else if (a instanceof RGBDataset) {
			img = createImageFromRGBADataset((RGBDataset) a, min.longValue(), max.longValue());
		} else {
			img = createImageFromDataset(a, min.doubleValue(), max.doubleValue(),redFunc,greenFunc,blueFunc,
										 inverseRed,inverseGreen,inverseBlue);
		}

		return img;
	}

	/**
	 * Create SWT ImageData from a dataset given a palette data
	 * <p>
	 * The input dataset can be a RGB dataset in which case the mapping functions
	 * and inversion flags are ignored.
	 * @param a dataset
	 * @param min minimum value of dataset
	 * @param max maximum value of dataset
	 * @param paletteData
	 * @return an ImageData object for SWT
	 * @throws Exception
	 */
	static public ImageData createImageData(Dataset a, Number min, Number max,
											PaletteData paletteData) throws Exception {
		ImageData img;
		if (a instanceof RGBByteDataset) {
			img = createImageFromRGBADataset((RGBByteDataset) a, min.longValue(), max.longValue());
		} else if (a instanceof RGBDataset) {
			img = createImageFromRGBADataset((RGBDataset) a, min.longValue(), max.longValue());
		} else {
			img = createImageFromDataset(a, paletteData);
		}
		return img;
	}

	private static int shift(int value, int shift) {
		return shift < 0 ? value >>> -shift : value << shift;
	}

	/**
	 * Create RGB dataset from an SWT image
	 * @param image
	 * @return a RGB dataset
	 */
	public static RGBDataset createRGBDataset(final ImageData image) {
		final int[] data = new int[image.width];
		final RGBDataset rgb = DatasetFactory.zeros(RGBDataset.class, image.height, image.width);
		final short[] p = new short[3];
		final PaletteData palette = image.palette;
		if (palette.isDirect) {
			for (int i = 0; i < image.height; i++) {
				image.getPixels(0, i, image.width, data, 0);
				for (int j = 0; j < image.width; j++) {
					int value = data[j];
					p[0] = (short) shift(value & palette.redMask, palette.redShift);
					p[1] = (short) shift(value & palette.greenMask, palette.greenShift);
					p[2] = (short) shift(value & palette.blueMask, palette.blueShift);
					rgb.setItem(p, i, j);
				}
			}
		} else {
			final RGB[] table = palette.getRGBs();
			for (int i = 0; i < image.height; i++) {
				image.getPixels(0, i, image.width, data, 0);
				for (int j = 0; j < image.width; j++) {
					RGB value = table[data[j]];
					p[0] = (short) value.red;
					p[1] = (short) value.green;
					p[2] = (short) value.blue;
					rgb.setItem(p, i, j);
				}
			}
		}

		return rgb;
	}

	private static boolean isRGBCompatible(ImageData image) {
		if (image.depth == 24 && image.bytesPerLine % 3 == 0) { // three components
			PaletteData p = image.palette;
			return p.redShift == 16 && p.greenShift == 8 && p.blueShift == 0; // so RGB
		}
		return false;
	}

	/**
	 * Create RGB dataset from an SWT image
	 * @param image
	 * @return a RGB dataset
	 */
	public static RGBByteDataset createRGBByteDataset(final ImageData image) {
		final int[] data = new int[image.width];
		RGBByteDataset rgb;
		final byte[] p = new byte[3];
		final PaletteData palette = image.palette;
		if (palette.isDirect) {
			if (isRGBCompatible(image)) {
				int pad = (image.bytesPerLine - 3 * image.width) / 3;
				rgb = DatasetFactory.createFromObject(RGBByteDataset.class, image.data, image.height, image.width + pad);
				if (pad > 0) { // crop end of rows
					rgb = (RGBByteDataset) rgb.getSliceView(null, new Slice(image.width), null);
				}
			} else {
				rgb = DatasetFactory.zeros(RGBByteDataset.class, image.height, image.width);
				for (int i = 0; i < image.height; i++) {
					image.getPixels(0, i, image.width, data, 0);
					for (int j = 0; j < image.width; j++) {
						int value = data[j];
						p[0] = (byte) shift(value & palette.redMask, palette.redShift);
						p[1] = (byte) shift(value & palette.greenMask, palette.greenShift);
						p[2] = (byte) shift(value & palette.blueMask, palette.blueShift);
						rgb.setItem(p, i, j);
					}
				}
			}
		} else {
			rgb = DatasetFactory.zeros(RGBByteDataset.class, image.height, image.width);
			final RGB[] table = palette.getRGBs();
			for (int i = 0; i < image.height; i++) {
				image.getPixels(0, i, image.width, data, 0);
				for (int j = 0; j < image.width; j++) {
					RGB value = table[data[j]];
					p[0] = (byte) value.red;
					p[1] = (byte) value.green;
					p[2] = (byte) value.blue;
					rgb.setItem(p, i, j);
				}
			}
		}

		return rgb;
	}

	public static ImageData createImageData(double min, double max, double minCut, double maxCut, Dataset image, ImageServiceBean bean) {
		int depth = bean.getDepth();
		if (depth > 8) { // Depth > 8 will not work properly at the moment.
			throw new RuntimeException(SWTImageUtils.class.getSimpleName() + " only supports 8-bit images unless a FunctionContainer has been set!");
			//if (depth == 16) palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
			//if (depth == 24) palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			//if (depth == 32) palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
		}
		int size = 1 << depth;

		final int[] shape = image.getShape();
		if (bean.isCancelled()) return null;

		int len = image.getSize();
		if (len == 0) return null;
	
		// The last three indices of the palette are always taken up with bound colours
		// We *DO NOT* copy the palette here so up to 3 of the original
		// colours can be changed. Instead whenever a palette is given to an
		// ImageService bean it should be original.
		PaletteData palette = bean.getPalette();
		if (palette == null) {
			try {
				final IPaletteService service = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
				palette = service.getDirectPaletteData("Gray Scale");
				bean.setPalette(palette);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// We have three special values, those which are greater than the max cut,
		// less than the min cut and the NaN number. For these we use special pixel
		// values in the palette as defined by the cut bound if it is set.
		// Also, any of these can be transparent; in which case, they are mapped to one pixel value
		int transIndex = -1;
		HistogramBound bound;
		bound = bean.getNanBound();
		final byte nanByte;
		size--;
		assignColour(bound, palette, size);
		nanByte = (byte) (size & 0xFF);
		if (!bound.hasColor()) {
			transIndex = size;
		}

		bound = bean.getMaximumCutBound();
		final byte maxByte;
		if (bound.hasColor()) {
			size--;
			assignColour(bound, palette, size);
			maxByte = (byte) (size & 0xFF);
		} else {
			if (transIndex < 0) {
				transIndex = --size;
				assignColour(bound, palette, size);
			}
			maxByte = (byte) (transIndex & 0xFF);
		}

		bound = bean.getMinimumCutBound();
		final byte minByte;
		if (bound.hasColor()) {
			size--;
			assignColour(bound, palette, size);
			minByte = (byte) (size & 0xFF);
		} else {
			if (transIndex < 0) {
				transIndex = --size;
				assignColour(bound, palette, size);
			}
			minByte = (byte) (transIndex & 0xFF);
		}

		if (transIndex < 0) { //
			transIndex = nanByte < 0 ? 256 + nanByte : nanByte;
		}
		final byte transByte = (byte) (transIndex & 0xFF);

		double scale;
		double maxPixel;
		if (max > min) {
			// 1 less than size and then 1 for each bound colour is lost.
			scale = (size - 1) / (max - min);
			maxPixel = max - min;
		} else if (max == min) {
			scale = 1;
			maxPixel = max;
		} else {
			scale = 1;
			maxPixel = 0xFF;
		}
		if (bean.isCancelled()) return null;
		
		Dataset mask = DatasetUtils.convertToDataset(bean.getMask());
		if (mask != null) {
			mask = DatasetUtils.rotate90(mask, bean.getOrigin().ordinal());
		}
		ImageData imageData = null;

		int alpha = bean.getAlpha();
		// We use a byte array directly as this is faster than using setPixel(...)
		// on image data. Set pixel does extra floating point operations. The downside
		// is that by doing this we certainly have to have 8 bit as getPixelColorIndex(...)
		// forces the use of on byte.
		final byte[] scaledImageAsByte = new byte[len];

		int index = 0;
		for (int i = 0; i<shape[0]; ++i) {
			if (bean.isCancelled()) return null;
			for (int j = 0; j<shape[1]; ++j) {
				
				// This saves a value lookup when the pixel is certainly masked.
				scaledImageAsByte[index] = mask==null || mask.getBoolean(i,j)
								? getPixelColorIndex(image.getDouble(i,j), min, max, scale, maxPixel, minCut, maxCut, minByte, maxByte, nanByte)
								: transByte;
				++index;
			}
		}

		boolean mixAlphas = palette.getRGB(transIndex) == null && alpha >= 0;
		
		//Windows does not accept a null RGB value in the palette
		if (palette.getRGB(transIndex) == null) palette.colors[transIndex] = new RGB(0,0,0);
		
		imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
		
		if (mixAlphas) { // set NaN pixels transparent
			final byte[] scaledAlphaAsByte = new byte[len];
			int k = 0;
			for (int i = 0; i<shape[0]; ++i) {
				if (bean.isCancelled()) return null;
				for (int j = 0; j<shape[1]; ++j) {
					scaledAlphaAsByte[k] = (byte) (scaledImageAsByte[k] == transByte ? 0 : alpha);
					k++;
				}
			}
			imageData.alphaData = scaledAlphaAsByte;
		} else {
			imageData.alpha = alpha;
		}
		return imageData;
	}

	private static void assignColour(HistogramBound bound, PaletteData palette, int index) {
		if (bound == null) {
			return;
		}
	
		int[] colour = bound.getColor();
		palette.colors[index] = colour == null ? null : new RGB(colour[0], colour[1], colour[2]);
	}

	/**
	 * private finals inline well by the compiler.
	 * @param val
	 * @param min
	 * @param max
	 * @param scale
	 * @param maxPixel
	 * @param scaledImageAsByte
	 */
	private final static byte getPixelColorIndex(final double  val, 
												 final double  min, 
												 final double  max, 
												 final double  scale, 
												 final double  maxPixel,
												 final double  minCut,
												 final double  maxCut,
												 final byte minByte,
												 final byte maxByte,
												 final byte nanByte) {
	
		// Deal with bounds
		if (Double.isNaN(val)) return nanByte;
	
		if (val<=minCut) return minByte;
		if (val>=maxCut) return maxByte;
	
		// If the pixel is within the bounds
		double scaled_pixel;
		if (val < min) {
			scaled_pixel = 0;
		} else if (val >= max) {
			scaled_pixel = maxPixel;
		} else {
			scaled_pixel = val - min;
		}
		scaled_pixel = scaled_pixel * scale;
	
		return (byte) (0x000000FF & ((int) scaled_pixel));
	}

}
