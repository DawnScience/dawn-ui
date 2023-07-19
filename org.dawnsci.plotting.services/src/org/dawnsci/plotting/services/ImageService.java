/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.services;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.NoSuchElementException;

import org.dawnsci.plotting.services.util.SWTImageUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.RGBByteDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShortDataset;
import org.eclipse.january.dataset.Stats;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * 
 
   Histogramming Explanation
   ---------------------------
   Image intensity distribution:

                ++----------------------**---------------
                +                      *+ *              
                ++                    *    *             
                |                     *    *             
                ++                    *     *            
                *                    *       *            
                +*                   *       *            
                |*                  *        *            
                +*                  *        *           
                |                  *          *         
                ++                 *          *          
                |                  *           *        
                ++                 *           *        
                |                 *            *        
                ++                *            *       
                                 *              *      
        Min Cut           Min    *              *      Max                     Max cut
 Red <- |   (min colour)  |    (color range, palette)  |      (max color)      | -> Blue
                                *                 *  
                |              *        +         *  
----------------++------------**---------+----------**----+---------------**+---------------++

 
 * @author Matthew Gerring
 *
 */
public class ImageService extends AbstractServiceFactory implements IImageService {
	
	static {
		System.out.println("Starting image service");
	}
	public ImageService() {
		// Important do nothing here, OSGI may start the service more than once.
	}
	
	/**
	 * This method is not thread safe
	 */
	public Image getImage(ImageServiceBean bean) {
		final ImageData data = getImageData(bean);
		return new Image(Display.getCurrent(), data);
	}
	
	/**
	 * getImageData(...) provides an image in a given palette data and origin.
	 * Faster than getting a resolved image
	 * 
	 * This method should be thread safe.
	 */
	public ImageData getImageData(ImageServiceBean bean) {
		ImageOrigin     origin   = bean.getOrigin();
		if (origin==null) origin = ImageOrigin.TOP_LEFT;

		// orientate the image
		Dataset oImage = DatasetUtils.convertToDataset(bean.getImage());
		if (bean.isTransposed()) {
			oImage = oImage.getTransposedView();
		}
		oImage = DatasetUtils.rotate90(oImage, origin.ordinal());
		Dataset image = oImage;

		if (image instanceof RGBByteDataset || image instanceof RGBDataset) {
			return SWTImageUtils.createImageData(image, 0, 255, null, null, null, false, false, false);
		}

		createMaxMin(bean);
		double max = getMax(bean);
		double min = getMin(bean);

		double maxCut = getMaxCut(bean);
		double minCut = getMinCut(bean);

		if (oImage.isComplex()) { // handle complex datasets by creating RGB dataset
			Dataset hue = Maths.angle(oImage, true);
			Dataset lImage = DatasetUtils.convertToDataset(getImageLoggedData(bean));
			if (bean.isTransposed()) {
				lImage = lImage.getTransposedView();
			}
			Dataset value = DatasetUtils.rotate90(lImage, origin.ordinal());
			double maxmax = Math.max(Math.abs(max), Math.abs(min));
			if (max - min > Math.ulp(maxmax)) {
				value.isubtract(min);
				value.imultiply(1./(max - min));
			} else if (maxmax > 0) {
				value.imultiply(1./maxmax);
			}
			Maths.clip(value, value, 0, 1);
			image = RGBByteDataset.createFromHSV(hue, null, value);
			return SWTImageUtils.createImageData(image, 0, 255, null, null, null, false, false, false);
		}

		// now deal with the log if needed
		if (bean.isLogColorScale()) {
			Dataset lImage = DatasetUtils.convertToDataset(getImageLoggedData(bean));
			if (bean.isTransposed()) {
				lImage = lImage.getTransposedView();
			}

			image = DatasetUtils.rotate90(lImage, origin.ordinal());
			max = Math.log10(max);
			// note createMaxMin() -> getFastStatistics() -> getImageLogged() which ensures min >= 0 
			min = Math.log10(min);
			maxCut = Math.log10(maxCut);
			// no guarantees for minCut though
			minCut = minCut <= 0 ? Double.NEGATIVE_INFINITY : Math.log10(minCut);
		}

		if (bean.getFunctionObject()!=null && bean.getFunctionObject() instanceof FunctionContainer) {
			final FunctionContainer fc = (FunctionContainer)bean.getFunctionObject();
			// TODO This does not support masking or cut bounds for zingers and dead pixels.
			return SWTImageUtils.createImageData(image, min, max, fc.getRedFunc(), 
																  fc.getGreenFunc(), 
																  fc.getBlueFunc(), 
																  fc.isInverseRed(), 
																  fc.isInverseGreen(), 
																  fc.isInverseBlue());
		}

		return SWTImageUtils.createImageData(min, max, minCut, maxCut, image, bean);
	}
	
	private double getMax(ImageServiceBean bean) {
		if (bean.getMaximumCutBound()==null || bean.getMaximumCutBound().getBound()==null) {
			return bean.getMax().doubleValue();
		}
		return Math.min(bean.getMax().doubleValue(), bean.getMaximumCutBound().getBound().doubleValue());
	}
	
	private double getMin(ImageServiceBean bean) {
		if (bean.getMinimumCutBound()==null || bean.getMinimumCutBound().getBound()==null) {
			return bean.getMin().doubleValue();
		}
		return Math.max(bean.getMin().doubleValue(), bean.getMinimumCutBound().getBound().doubleValue());
	}
	
	private double getMaxCut(ImageServiceBean bean) {
		if (bean.getMaximumCutBound()==null || bean.getMaximumCutBound().getBound()==null) {
			return Double.POSITIVE_INFINITY;
		}
		return bean.getMaximumCutBound().getBound().doubleValue();
	}
	
	private double getMinCut(ImageServiceBean bean) {
		if (bean.getMinimumCutBound()==null || bean.getMinimumCutBound().getBound()==null) {
			return Double.NEGATIVE_INFINITY;
		}
		return bean.getMinimumCutBound().getBound().doubleValue();
	}

	private void createMaxMin(ImageServiceBean bean) {
		
		double[] stats  = null;
		if (bean.getMin()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
			bean.setMin(stats[0]);
		}
		
		if (bean.getMax()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
			bean.setMax(stats[1]);
		}
	}

	/**
	 * Get the logged image value and cache the result.
	 * 
	 * @param bean
	 * @return a dataset that can be absolute, if complex, and also be logged according to bean
	 * Package private for testing
	 */
	/* package */ Dataset getImageLoggedData(ImageServiceBean bean) {
		Dataset ret = DatasetUtils.convertToDataset(bean.getImageValue());
		if (ret == null) {
			ret = getImageLoggedDataCalc(bean);
			bean.setImageValue(ret);
		}
		return ret;
	}
	/**
	 * Get the logged image value.
	 * 
	 * @param bean
	 * @return a dataset that can be absolute, if complex, and also be logged according to bean
	 * Package private for testing
	 */
	/* package */ Dataset getImageLoggedDataCalc(ImageServiceBean bean) {
		Dataset ret = DatasetUtils.convertToDataset(bean.getImage());

		if (ret.isComplex()) {
			ret = Maths.abs(ret);
		}
		if (bean.isLogColorScale()) {
			double offset = bean.getLogOffset();
			if (Double.isFinite(offset)) {
				ret = Maths.subtract(ret, offset);
			}
			ret = Maths.log10(ret);
		}
		return ret;
	}

	/**
	 * Fast statistics as a rough guide - this is faster than Dataset.getMin()
	 * and getMax() which may cache but slows the opening of images too much.
	 * The return array[2] was added in "Updated for Diffraction Tool." commit,
	 * but no trace of such usage. However it should not be removed, because
	 * it is useful as return array[3].
	 * 
	 * @param bean
	 * @return [0] = min [1] = max(=mean*constant) [2] = ???
	 */
	public double[] getFastStatistics(ImageServiceBean bean) {
		
		Dataset image = getImageLoggedData(bean);

		if (bean.getHistogramType() == HistoType.OUTLIER_VALUES) {
			double[] ret = null;
			
			Dataset mask = bean.getMask() == null ? null : DatasetUtils.convertToDataset(bean.getMask());
			
			try {
			    double[] stats = Stats.outlierValues(image,mask,true, bean.getLo(), bean.getHi(), -1);
			    ret = new double[]{stats[0], stats[1], -1};
			} catch (IllegalArgumentException iae) {
				bean.setLo(10);
				bean.setHi(90);
			    double[] stats = Stats.outlierValues(image,mask,true, bean.getLo(), bean.getHi(), -1);
			    ret = new double[]{stats[0], stats[1], -1};
			} catch (NoSuchElementException e) {
				//data all NaN
				ret = new double[] {Double.NaN, Double.NaN,-1};
			}

			if (bean.isLogColorScale() && ret != null) {
				ret = new double[] { Math.pow(10, ret[0]), Math.pow(10, ret[1]), -1 };
			}

			sanitise_stats(ret, InterfaceUtils.isInteger(image.getClass()));

			return ret;
		}
		
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double sum = 0.0;
		int size = 0;
		
		BooleanDataset mask = bean.getMask() == null ? null : DatasetUtils.cast(BooleanDataset.class, bean.getMask());

	    // Big loop warning:
	    final IndexIterator it = image.getIterator();
	    final IndexIterator mit = mask == null ? null : mask.getIterator();
		while (it.hasNext()) {
			
			final double val = image.getElementDoubleAbs(it.index);
			if (mit != null && mit.hasNext()) {
				if (!mask.getElementBooleanAbs(mit.index)) {
					continue; // Masked!
			    }
			}

			if (Double.isNaN(val))      continue;
			if (!bean.isInBounds(val))  continue;

			sum += val;
			if (val < min) min = val;
			if (val > max) max = val;
			size++;
		}
		
		double retMax = Double.NaN;
		double retExtra = Double.NaN;
		
		if (bean.getHistogramType() == HistoType.FULL_RANGE) {
			retMax = max;
			
		} else if (bean.getHistogramType()==HistoType.MEDIAN) { 
			
			double median = Double.NaN;
			try {
				median = ((Number)Stats.median(image)).doubleValue(); // SLOW
			} catch (Exception ne) {
				median = ((Number)Stats.median(image.cast(ShortDataset.class))).doubleValue();// SLOWER
			}
			retMax = 2 * median;
			retExtra=median;
			
		} else { // Use mean based histo
			double mean = sum / size;
			retMax = (Math.E)*mean; // Not statistical, E seems to be better than 3...
			retExtra=mean;

		}
		
		if (retMax > max) retMax = max;
		
		if (bean.isLogColorScale()) {
			return new double[]{Math.pow(10, min), Math.pow(10, retMax), Math.pow(10, retExtra)};
		}
		
		double[] ret = new double[]{min, retMax, retExtra, max};
		
		sanitise_stats(ret, InterfaceUtils.isInteger(image.getClass()));

		return ret;
	}
	
	private void sanitise_stats(double[] output, boolean isInteger) {
		double a = output[0];
		if (a == output[1]) {
			if (isInteger) {
				output[0] = a - 1;
				output[1] = a + 1;
			} else if (a != 0) {
				output[0] = Math.nextDown(output[0]);
				output[1] = Math.nextUp(output[1]);
			} else { // special case as Double.MIN_VALUE causes problems in later use
				output[0] = -Math.pow(2, -52);
				output[1] = -output[0];
			}
		}
	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		
		if (serviceInterface==IImageService.class) {
			return new ImageService();
		} 
		return null;
	}
	
	public static final class SDAFunctionBean {
		
	}

	
	/**
	 * Converts an SWT ImageData to an AWT BufferedImage.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	@Override
	public BufferedImage getBufferedImage(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
					palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					bufferedImage.setRGB(x, y, rgb.red << 16 | rgb.green << 8
							| rgb.blue);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	@Override
	public ImageServiceBean createBeanFromPreferences() {
		
		ImageServiceBean imageServiceBean = new ImageServiceBean();
		
		if (Platform.getPreferencesService() != null) { // Normally
			IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
			imageServiceBean.setOrigin(ImageOrigin.forLabel(store.getString(BasePlottingConstants.ORIGIN_PREF)));
			imageServiceBean.setTransposed(store.getBoolean(BasePlottingConstants.TRANSPOSE_PREF));
			imageServiceBean.setHistogramType(HistoType.forLabel(store.getString(BasePlottingConstants.HISTO_PREF)));
			imageServiceBean.setMinimumCutBound(HistogramBound.fromString(store.getString(BasePlottingConstants.MIN_CUT)));
			imageServiceBean.setMaximumCutBound(HistogramBound.fromString(store.getString(BasePlottingConstants.MAX_CUT)));
			imageServiceBean.setNanBound(HistogramBound.fromString(store.getString(BasePlottingConstants.NAN_CUT)));
			imageServiceBean.setLo(store.getDouble(BasePlottingConstants.HISTO_LO));
			imageServiceBean.setHi(store.getDouble(BasePlottingConstants.HISTO_HI));
			
			try {
				IPaletteService pservice = ServiceLoader.getPaletteService();
				if (pservice !=null) {
					final String scheme = store.getString(BasePlottingConstants.COLOUR_SCHEME);
						
					if (store.getBoolean(BasePlottingConstants.USE_PALETTE_FUNCTIONS)) {
						FunctionContainer container = pservice.getFunctionContainer(scheme);
						if (container!=null) {
							imageServiceBean.setFunctionObject(container);
						} else {
							imageServiceBean.setPalette(pservice.getDirectPaletteData(scheme));
						}
					} else {
						// if 8-bit, set direct palette, otherwise set palette functions.
						PaletteData pd;
						try {
							pd = pservice.getDirectPaletteData(scheme);
						} catch(final IllegalArgumentException e) { //scheme does not exist
							final String defaultScheme = pservice.getColorSchemes().iterator().next();
							pd = pservice.getDirectPaletteData(defaultScheme);
							store.setValue(BasePlottingConstants.COLOUR_SCHEME, defaultScheme);
						}
						imageServiceBean.setPalette(pd);
					}
	
				}
			} catch (Exception e) {
				// Ignored
			}
			
		} else { // Hard code something
			
			imageServiceBean.setOrigin(ImageOrigin.TOP_LEFT);
			imageServiceBean.setHistogramType(HistoType.OUTLIER_VALUES);
			imageServiceBean.setMinimumCutBound(HistogramBound.DEFAULT_MINIMUM);
			imageServiceBean.setMaximumCutBound(HistogramBound.DEFAULT_MAXIMUM);
			imageServiceBean.setNanBound(HistogramBound.DEFAULT_NAN);
			imageServiceBean.setLo(00.01);
			imageServiceBean.setHi(99.99);
			imageServiceBean.setPalette(makeJetPalette());
		}
	
		return imageServiceBean;
	}

	
	private static PaletteData makeJetPalette() {
		RGB jet[] = new RGB[256];
		
		int nb = 256;

		for (int i = 0; i < nb; i++) {
			
			double value = (double)i/(double)255;

			double outBlue = 0;
			if (value <= 0.1) {outBlue  =  5*value + 0.5;}
			if (value > 0.1 && value <= 1.0/3.0 ) {outBlue  =  1;}
			if (value >1.0/3.0 && value <= 1.0/2.0) {outBlue  =  -6*value +3;}
			
			double outGreen = 0;
			if (value > 1.0/3.0 && value < 2.0/3.0  ) {outGreen = 1;}
			if (value <= 1.0/3.0 && value >= 1.0/8.0) {outGreen = 24.0/5*value - 0.6;}
			if (value >= 2.0/3.0 && value <= 7.0/8.0) {outGreen = -24.0/5*value + 4.2;}
			
			double outRed = 0;
			if (value >= 0.9) {outRed = -5*value +5.5;}
			if (value > 2.0/3.0 && value <= 0.9 ) {outRed = 1;}
			if (value >=1.0/2.0 && value <= 2.0/3.0 ) {outRed = 6*value -3;}
			
			jet[i] = new RGB((int)(outRed*255),
					(int)(outGreen*255),
					(int)(outBlue*255));

		}
		return new PaletteData(jet);
	}
}
