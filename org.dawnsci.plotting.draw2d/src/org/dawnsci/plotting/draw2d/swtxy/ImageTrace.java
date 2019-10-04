/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.analysis.api.roi.IPolylineROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.function.Downsample;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.DownSampleEvent;
import org.eclipse.dawnsci.plotting.api.trace.IDownSampleListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceContainer;
import org.eclipse.dawnsci.plotting.api.trace.PaletteEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.visualization.widgets.figureparts.ColorMapRamp;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.DAxis;
import org.eclipse.nebula.visualization.xygraph.figures.IAxisListener;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A trace which draws an image to the plot.
 * 
 * @author Matthew Gerring
 *
 */
public class ImageTrace extends Figure implements IImageTrace, IAxisListener, ITraceContainer, IDataListener {

	private static final Logger logger = LoggerFactory.getLogger(ImageTrace.class);

	private static final int MINIMUM_ZOOM_SIZE  = 2;
	private static final int MINIMUM_LABEL_SIZE = 10;

	private String           name;
	private String           dataName;
	private String           paletteName;
	private Axis             xAxis;
	private Axis             yAxis;
	private ColorMapRamp     intensityScale;
	private Dataset          image;
	private IDynamicShape    dynamic;
	private DownsampleType   downsampleType=DownsampleType.MAXIMUM;
	private int              currentDownSampleBin=-1;
	private int              alpha = -1;
	private List<Dataset>    axes;
	private ImageServiceBean imageServiceBean;
	private ImageScaleType dirty = null;
	/**
	 * Used to define if the zoom is at its maximum possible extend
	 */
	private boolean          isMaximumZoom;
	/**
	 * Used to define if the zoom is at an extent large enough to show a 
	 * label grid for the intensity.
	 */
	private boolean          isLabelZoom;

	/**
	 * Controls of the image should be downsampled and the ImageData recreated.
	 */
	private boolean          imageCreationAllowed = true;

	/**
	 * The parent plotting system for this image.
	 */
	private IPlottingSystem<?> plottingSystem;

	/**
	 * With origin at top-left so that x is rightwards and y is downwards, then image is not transposed
	 */
	private boolean imageTransposed = false;

	private IImageService service;

	private boolean xTicksAtEnd, yTicksAtEnd;

	private double[] globalRange;

	public ImageTrace(final String name, 
			final ColorMapRamp intensityScale) {

		this.name  = name;
		this.intensityScale = intensityScale;

		this.service = ServiceHolder.getImageService();
		this.imageServiceBean = service.createBeanFromPreferences();
		imageTransposed = imageServiceBean.isTransposed();
		setPaletteName(getPreferenceStore().getString(BasePlottingConstants.COLOUR_SCHEME));

		downsampleType = DownsampleType.forLabel(getPreferenceStore().getString(BasePlottingConstants.DOWNSAMPLE_PREF));
	}

	@Override
	public void initialize(IAxis... axes) {
		this.xAxis = axes[0] instanceof Axis ? (Axis) axes[0] : null;
		this.yAxis = axes[1] instanceof Axis ? (Axis) axes[1] : null;

		// need to set axes properly before adding axis listeners
		// override axes with image origin given untransposed image
		listenToAxisChanges = false;
		try {
			switch (getImageOrigin()) {
			case TOP_LEFT:
			default:
				xAxis.setInverted(false);
				yAxis.setInverted(true);
				break;
			case BOTTOM_LEFT:
				xAxis.setInverted(false);
				yAxis.setInverted(false);
				break;
			case BOTTOM_RIGHT:
				xAxis.setInverted(true);
				yAxis.setInverted(false);
				break;
			case TOP_RIGHT:
				xAxis.setInverted(true);
				yAxis.setInverted(true);
				break;
			}
		} finally {
			listenToAxisChanges = true;
		}

		xAxis.addListener(this);
		yAxis.addListener(this);

		xTicksAtEnd = setupTicks(xAxis);
		yTicksAtEnd = setupTicks(yAxis);

		if (xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {
			AspectAxis x = (AspectAxis) xAxis;
			AspectAxis y = (AspectAxis) yAxis;
			x.setKeepAspectWith(y);
			y.setKeepAspectWith(x);
		}
	}

	private boolean setupTicks(Axis a) {
		boolean hasTicksAtEnd = a.hasTicksAtEnds();
		if (xAxis instanceof DAxis) {
			DAxis da = (DAxis) a;
			da.setTicksAtEnds(false);
			da.setTicksIndexBased(globalRange == null);
		}
		return hasTicksAtEnd;
	}

	private void resetAxisTicks(Axis a, boolean hasTicksAtEnd) {
		if (a instanceof DAxis) {
			DAxis da = (DAxis) a;
			da.setTicksAtEnds(hasTicksAtEnd);
			da.setTicksIndexBased(false);
		}
	}

	private IPreferenceStore store;
	private IPreferenceStore getPreferenceStore() {
		if (store!=null) return store;
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IPlottingSystem.PREFERENCE_STORE);
		return store;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		if (plottingSystem!=null) plottingSystem.moveTrace(this.name, newName);
		this.name = newName;
	}

	public AspectAxis getXAxis() {
		return (AspectAxis)xAxis;
	}

	public void setXAxis(Axis xAxis) {
		this.xAxis = xAxis;
		if (xAxis instanceof DAxis) {
			((DAxis) xAxis).setTicksIndexBased(true);
		}
	}

	public AspectAxis getYAxis() {
		return (AspectAxis)yAxis;
	}

	public void setYAxis(Axis yAxis) {
		this.yAxis = yAxis;
		if (yAxis instanceof DAxis) {
			((DAxis) yAxis).setTicksIndexBased(true);
		}
	}

	public Dataset getImage() {
		return image;
	}

	public PaletteData getPaletteData() {
		if (imageServiceBean==null) return null;
		return imageServiceBean.getPalette();
	}

	public void setPaletteData(PaletteData paletteData) {
		if (paletteData==null)      return;
		if (imageServiceBean==null) return;
		imageServiceBean.setPalette(paletteData);
		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
		intensityScale.repaint();
		repaint();
		firePaletteDataListeners(paletteData);
	}

	@Override
	public String getPaletteName() {
		return paletteName;
	}

	@Override
	public void setPaletteName(String paletteName) {
		this.paletteName = paletteName;
	}

	@Override
	public void setPalette(String paletteName) {
		IPaletteService pservice = ServiceHolder.getPaletteService();
		final PaletteData paletteData = pservice.getDirectPaletteData(paletteName);
		setPaletteName(paletteName);
		setPaletteData(paletteData);

	}

	private enum ImageScaleType {
		// Going up in order of work done
		NO_REIMAGE,
		REIMAGE_ALLOWED,
		FORCE_REIMAGE, 
		REHISTOGRAM;
	}


	/**
	 * Get the current image which can be used by the composite trace.
	 * @return
	 * @throws Exception 
	 */
	protected ScaledImageData getScaledImageData() {	
		return scaledData;
	}

	private Rectangle screenRectangle;

	/**
	 * Check if requested scaling is much bigger than than the screen size and in that case do not scale
	 * Fix to http://jira.diamond.ac.uk/browse/SCI-926
	 * @param scaledWidth
	 * @param scaledHeight
	 * @return true if we will not trigger an SWT bug
	 */
	private boolean checkScalingAgainstScreenSize(int scaledWidth, int scaledHeight) {
		if (screenRectangle == null) {
			Display d = Display.getCurrent();
			if (d == null) {
				return true;
			}
			org.eclipse.swt.graphics.Rectangle rect = d.getPrimaryMonitor().getClientArea();
			screenRectangle = new Rectangle(rect.x, rect.y, rect.width, rect.height);
		}
		if (scaledWidth > screenRectangle.width * 2 || scaledHeight > screenRectangle.height * 2) {
			logger.error("Image scaling algorithm has malfunctioned and asked for an image bigger than the screen!");
			logger.debug("scaleWidth="+scaledWidth);
			logger.debug("scaleHeight="+scaledHeight);
			return false;
		}
		return true;
	}

	/**
	 * Object to hold the current scaled image, the location it should be drawn
	 * and the downsampled image data which it used.
	 */
	private ScaledImageData scaledData = new ScaledImageData();

	/**
	 * number of entries in intensity scale
	 */
	final static int INTENSITY_SCALE_ENTRIES = 256;

	/**
	 * When this is called an SWT image is created
	 * and saved in the scaledData field. The image is downsampled. If rescaleAllowed
	 * is set to false, the current bin is not checked and the last scaled image
	 * is always used.
	 *  
	 * Do not synchronized this method - it can cause a race condition on linux only.
	 * 
	 * @return true if scaledImage created.
	 */
	protected boolean createScaledImage(ImageScaleType rescaleType, final IProgressMonitor monitor) {

		if (!imageCreationAllowed) return false;

		if (getXAxis() == null || getYAxis() == null) {
			return false;
		}
		boolean requireImageGeneration = scaledData.getDownsampledImageData() == null ||
				rescaleType==ImageScaleType.FORCE_REIMAGE || 
				rescaleType==ImageScaleType.REHISTOGRAM; // We know that it is needed

		// If we just changed downsample scale, we force the update.
		// This allows user resizes of the plot area to be picked up
		// and the larger data size used if it fits.
		if (!requireImageGeneration && rescaleType == ImageScaleType.REIMAGE_ALLOWED && currentDownSampleBin > 0) {
			if (getDownsampleBin() != currentDownSampleBin) {
				requireImageGeneration = true;
			}
		}

		final XYRegionGraph graph  = (XYRegionGraph)getXAxis().getParent();
		final Rectangle     bounds = graph.getRegionArea().getBounds();
		if (bounds.width < 1 || bounds.height < 1) return false;

		scaledData.setX(bounds.x);
		scaledData.setY(bounds.y);
		scaledData.setXoffset(0);
		scaledData.setYoffset(0);

		if (!imageCreationAllowed) return false;
		if (monitor!=null && monitor.isCanceled()) return false;

		if (requireImageGeneration) {
			boolean ok = createDownsampledImageData(rescaleType, monitor);
			if (!ok) return false;
		}

		if (monitor!=null && monitor.isCanceled()) return false;
		if (scaledData.getDownsampledImageData() == null) return false;

		try {
			isMaximumZoom = false;
			isLabelZoom   = false;

			scaledData.disposeImage();
			ImageData imageData = scaledData.getDownsampledImageData();
			if (imageData != null && imageData.width == bounds.width && imageData.height == bounds.height) {
				// No slice, faster
				if (monitor!=null && monitor.isCanceled()) return false;
				Image scaledImage  = new Image(Display.getDefault(), imageData);
				scaledData.setScaledImage(scaledImage);
			} else {

				if (globalRange != null) {
					return buildImageRelativeToAxes(imageData);
				}

				// slice data to get current zoom area
				Range xRange = xAxis.getRange();
				Range yRange = yAxis.getRange();

				double xMin = xRange.getLower() / currentDownSampleBin;
				double yMin = yRange.getLower() / currentDownSampleBin;
				double xMax = xRange.getUpper() / currentDownSampleBin;
				double yMax = yRange.getUpper() / currentDownSampleBin;
				int xSize = imageData.width;
				int ySize = imageData.height;

				// check as getLower and getUpper don't work as expected
				if (xMax < xMin) {
					double temp = xMax;
					xMax = xMin;
					xMin = temp;
				}
				if (yMax < yMin) {
					double temp = yMax;
					yMax = yMin;
					yMin = temp;
				}

				// factors to scale image data 
				double xScale = bounds.width / (xMax - xMin);
				double yScale = bounds.height / (yMax - yMin);

				// Deliberately get the over-sized dimensions so that the edge pixels can be smoothly panned through.
				double xPix = Math.floor(xMin);
				double yPix = Math.floor(yMin);
				int fullWidth  = (int) Math.ceil(xMax - xPix);
				int fullHeight = (int) Math.ceil(yMax - yPix);

				// Force a minimum size for (zoomed-in) pixels on the system
				if (fullWidth <= MINIMUM_ZOOM_SIZE && fullHeight <= MINIMUM_ZOOM_SIZE) {
					isMaximumZoom = true;
				}
				if (fullWidth <= MINIMUM_LABEL_SIZE && fullHeight <= MINIMUM_LABEL_SIZE) {
					isLabelZoom = true;
				}

				// These offsets are used when the scaled images is drawn to the screen.
				double xOffset;
				double yOffset;
				// Deal with the origin orientations correctly.
				ImageOrigin origin = getImageOrigin();
				if (origin.isOnLeft()) {
					xOffset = (xMin - xPix) * xScale;
				} else {
					double xPixD = xSize - xMax;
					xPix = Math.floor(xPixD);
					xOffset = (xPixD - xPix) * xScale;
				}

				if (origin.isOnTop()) {
					yOffset = (yMin - yPix) * yScale;
				} else {
					double yPixD = ySize - yMax;
					yPix = Math.floor(yPixD);
					yOffset = (yPixD - yPix) * yScale;
				}
				if (xPix < 0 || yPix < 0 || xPix+fullWidth > xSize || yPix+fullHeight > ySize) {
					logger.trace("Incorrect position calculated!");
					return false; // prevent IAE in calling getPixel
				}

				// Slice the data.
				// Pixel slice on downsampled data = fast!
				ImageData data = sliceImageData(imageData, fullWidth, fullHeight, (int) xPix, (int) yPix, ySize);

				int scaledWidth = (int) (fullWidth * xScale);
				int scaledHeight = (int) (fullHeight * yScale);

				Image scaledImage = null;
				if (data != null) {
					if (checkScalingAgainstScreenSize(scaledWidth, scaledHeight)) {
						data = data.scaledTo(scaledWidth, scaledHeight);
					} else {
						isMaximumZoom = true;
					}
					scaledImage = new Image(Display.getDefault(), data);
				}
				scaledData.setXoffset(xOffset);
				scaledData.setYoffset(yOffset);
				scaledData.setScaledImage(scaledImage);
			}

			return true;
		} catch (IllegalArgumentException ie) {
			logger.error(ie.toString());
			return false;
		} catch (java.lang.NegativeArraySizeException allowed) {
			return false;
		} catch (NullPointerException ne) {
			throw ne;
		} catch (Throwable ne) {
			logger.error("Image scale error!", ne);
			return false;
		}
	}

	private boolean buildImageRelativeToAxes(ImageData imageData) {

		// slice data to get current zoom area
		/**     
		 *      x1,y1--------------x2,y2
		 *        |                  |
		 *        |                  |
		 *        |                  |
		 *      x3,y3--------------x4,y4
		 */

		ImageData data = imageData;

		if (getAxes() == null) return false;

		Dataset xAxis = DatasetUtils.convertToDataset(getAxes().get(0));
		Dataset yAxis = DatasetUtils.convertToDataset(getAxes().get(1));

		if (xAxis == null || yAxis == null) return false;

		if (xAxis.getSize() == 1 && yAxis.getSize() == 1) return false;

		if (xAxis.getSize() == 1) {
			double step = Math.abs(yAxis.getDouble(0)-yAxis.getDouble(1));
			double d = xAxis.getDouble();
			xAxis = DatasetFactory.createFromObject(new double[]{ d, d + step});
		}

		if (yAxis.getSize() == 1) {
			double step = Math.abs(xAxis.getDouble(0)-xAxis.getDouble(1));
			double d = yAxis.getDouble();
			yAxis = DatasetFactory.createFromObject(new double[]{ d, d + step});
		}

		int[] xvalues = generateStartLengthPositionArray(xAxis,getXAxis());
		if (xvalues == null) {
			return false;
		}
		int xPix = xvalues[0];
		int width = xvalues[1];
		int xPos = xvalues[2];
		int scaledWidth = xvalues[3];

		if (width <= 0) {
			return false;
		}

		int[] yvalues = generateStartLengthPositionArray(yAxis,getYAxis());
		if (yvalues == null) {
			return false;
		}
		int yPix = yvalues[0];
		int height = yvalues[1];
		int yPos = yvalues[2];
		int scaledHeight = yvalues[3];

		if (height <= 0) {
			return false;
		}

		int ySize = imageData.height;

		try {

			data = sliceImageData(data, width, height, xPix, yPix, ySize);

			boolean proceed = checkScalingAgainstScreenSize(Math.abs(scaledWidth), Math.abs(scaledHeight));

			Image scaledImage = null;
			if (data != null) {
				if (proceed) {
					data = data.scaledTo(scaledWidth, scaledHeight);
				} else {
					isMaximumZoom = true;
				}
				scaledImage = new Image(Display.getDefault(), data);
			}


			scaledData.setX(xPos);
			scaledData.setY(yPos);
			scaledData.setXoffset(0);
			scaledData.setYoffset(0);
			scaledData.setScaledImage(scaledImage);

		} catch (IllegalArgumentException ne) {

			logger.error("Image scaling has malfunctioned");
			logger.debug("Trace name = "+getName());
			logger.debug("scaleWidth = "+scaledWidth);
			logger.debug("scaleHeight = "+scaledHeight);
			logger.debug("width = "+width);
			logger.debug("height = "+height);
			logger.debug("xPix = "+xPix);
			logger.debug("yPix = "+yPix);

			throw ne;
		}


		return true;

	}
	
	/**
	 * 
	 * @param axis
	 * @param plotAxis
	 * @return values for the start index into the data, the size of the data, the screen pixel position and the scaling
	 */
	private int[] generateStartLengthPositionArray(Dataset axis, AspectAxis plotAxis) {
		boolean dataInc = axis.getDouble(0) < axis.getDouble(-1);
		int end = axis.getSize()-1;
		
		double minData = axis.min().doubleValue();
		double maxData = axis.max().doubleValue();

		double stepData = (maxData - minData)/(end);

		//adjust axis min/max so centre of pixel is at axis value
		minData -= stepData/2;
		maxData += stepData/2;
		

		//get data coords visible on screen
		double[] visibleLimits = calculatePlotDataLimits(minData, maxData, plotAxis);
		if (visibleLimits == null) {
			return null;
		}

		double minPlot = visibleLimits[0];
		double maxPlot = visibleLimits[1];

		//get start (floored) and stop (ceiling) position in data array
		//assume linear steps
		double startIdxFrac = (minPlot - minData)/stepData;

		int startIdx = (int)Math.floor(startIdxFrac);

		double stopIdxFrac = (maxPlot -minData)/stepData;

		int stopIdx = (int)Math.ceil(stopIdxFrac);

		stopIdx = stopIdx <  end ? stopIdx : end;

		int sizeData = stopIdx - startIdx + 1;

		//get on screen pixel location to draw image
		int[] visibleLimitsPixel = getPixelDataLimits(minData + startIdx*stepData, maxData - (end-stopIdx)* stepData, plotAxis);

		//calculate scaling factor
		double pixelScale = Math.abs((visibleLimitsPixel[0]-visibleLimitsPixel[1]) / ((double)sizeData));

		int scaled = Math.max(1, (int) (sizeData*pixelScale));
		
		//if axis not increasing
		//flip start index and scaling
		if (!dataInc) {
			startIdx = end - stopIdx;
			scaled*=-1;
		}

		return new int[] {startIdx, sizeData, visibleLimitsPixel[0], scaled};
	}
		


	private ImageData sliceImageData(ImageData imageData, int width, int height, int xPix, int yPix, int ySize) {
		ImageData data;
		if (imageData.depth <= 8) {
			// NOTE Assumes 8-bit images
			final int size   = width*height;
			final byte[] pixels = new byte[size];
			for (int y = 0; y < height && (yPix+y)<ySize ; y++) {
				imageData.getPixels(xPix, yPix+y, width, pixels, width*y);
			}
			data = new ImageData(width, height, imageData.depth, getPaletteData(), 1, pixels);
			if (imageData.alphaData != null) {
				final byte[] alphas = new byte[size];
				for (int y = 0; y < height && (yPix+y)<ySize ; y++) {
					imageData.getAlphas(xPix, yPix+y, width, alphas, width*y);
				}
				data.alphaData = alphas;
			}
		} else {
			// NOTE Assumes 24 Bit Images
			final int[] pixels = new int[width];
			data = new ImageData(width, height, imageData.depth, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));
			for (int y = 0; y < height; y++) {					
				imageData.getPixels(xPix, y+yPix, width, pixels, 0);
				data.setPixels(0, y, width, pixels, 0);
			}
			if (imageData.alphaData != null) {
				final byte[] alphas = new byte[width];
				for (int y = 0; y < height; y++) {					
					imageData.getAlphas(xPix, y+yPix, width, alphas, 0);
					data.setAlphas(0, y, width, alphas, 0);
				}
			}
		}
		data.alpha = imageData.alpha;
		return data;
	}

	public void setGlobalRange(double[] globalRange) {
		this.globalRange = globalRange;
		if (xAxis instanceof DAxis) {
			((DAxis) xAxis).setTicksIndexBased(false);
			((DAxis) xAxis).setTicksAtEnds(false);
		}
		if (yAxis instanceof DAxis) {
			((DAxis) yAxis).setTicksIndexBased(false);
			((DAxis) yAxis).setTicksAtEnds(false);
		}
		updateImageDirty(ImageScaleType.FORCE_REIMAGE);
	}

	public double[] getGlobalRange() {
		return globalRange;
	}

	private final int getPositionInAxis(double val, IDataset axis) {
		if (axis.getSize() == 1) return 0;
		Dataset a = DatasetUtils.convertToDataset(axis.clone());
		return Maths.abs(a.isubtract(val)).minPos()[0];
	}

	private final int[] getPixelDataLimits(double min, double max, IAxis axis) {
		int v1 = (int) Math.round(axis.getPositionFromValue(min));
		int v2 = (int) Math.round(axis.getPositionFromValue(max));

		return new int[]{v1, v2};
	}
	
	private final double[] calculatePlotDataLimits(double minData, double maxData, IAxis axis) {
		double min = axis.getLower();
		double max = axis.getUpper();

		// Make sure we have the min and max right
		if (max < min) {
			double temp = max;
			max = min;
			min = temp;
		}

		if (minData > max || maxData < min) { // not visible
			return null;
		}

		// Bind the extent of the images to the actual data
		min = Math.max(minData, min);
		max = Math.min(maxData, max);

		return new double[]{min, max};
	}

	private boolean createDownsampledImageData(ImageScaleType rescaleType, IProgressMonitor monitor) {

		try {
			imageCreationAllowed = false;
			if (image==null) return false;

			IDataset reducedFullImage = getDownsampled(image);

			imageServiceBean.setImage(reducedFullImage);
			imageServiceBean.setMonitor(monitor);
			if (fullMask!=null) {
				// For masks, we preserve the min (the falses) to avoid losing fine lines
				// which are masked.
				imageServiceBean.setMask(getDownsampled(fullMask, DownsampleMode.MINIMUM));
			} else {
				imageServiceBean.setMask(null); // Ensure we lose the mask!
			}

			if (rescaleType==ImageScaleType.REHISTOGRAM) { // Avoids changing colouring to 
				// max and min of new selection.

				Range xRange = getXAxis().getRange();
				Range yRange = getYAxis().getRange();

				if (globalRange != null) {
					xRange = new Range(getPositionInAxis(xRange.getLower(), getAxes().get(0)), getPositionInAxis(xRange.getUpper(), getAxes().get(0)));
					yRange = new Range(getPositionInAxis(yRange.getLower(), getAxes().get(1)), getPositionInAxis(yRange.getUpper(), getAxes().get(1)));
				}


				Dataset  slice     = slice(yRange, xRange, getData());
				ImageServiceBean histoBean = imageServiceBean.clone();
				histoBean.setImage(slice);
				if (fullMask!=null) histoBean.setMask(slice(getYAxis().getRange(), getXAxis().getRange(), fullMask));
				double[] fa = service.getFastStatistics(histoBean);
				setMin(fa[0]);
				setMax(fa[1]);

			}

			this.imageServiceBean.setAlpha(getAlpha());
			ImageData imageData   = service.getImageData(imageServiceBean);
			scaledData.setDownsampledImageData(imageData);

			try {
				ImageServiceBean intensityScaleBean = imageServiceBean.clone();
				intensityScaleBean.setOrigin(ImageOrigin.TOP_LEFT);
				// We send the image drawn with the same palette to the 
				// intensityScale
				// TODO FIXME This will not work in log mode
				if (reducedFullImage instanceof RGBDataset) return true;
				final DoubleDataset dds = DatasetFactory.zeros(DoubleDataset.class, INTENSITY_SCALE_ENTRIES, 1);
				double max = getMax().doubleValue();
				double inc = (max - getMin().doubleValue())/INTENSITY_SCALE_ENTRIES;
				for (int i = 0; i < INTENSITY_SCALE_ENTRIES; i++) {
					dds.set(max - (i*inc), i, 0);
				}
				intensityScaleBean.setImage(dds);
				intensityScaleBean.setMask(null);
				intensityScale.setImageData(service.getImageData(intensityScaleBean));
				intensityScale.setLog10(getImageServiceBean().isLogColorScale());
			} catch (Throwable ne) {
				logger.warn("Cannot update intensity!");
			}

		} catch (Exception e) {
			logger.error("Cannot create image from data!", e);
		} finally {
			imageCreationAllowed = true;
		}
		return true;
	}

	private static final int[] getBounds(Range xr, Range yr) {
		return new int[] {(int) Math.floor(xr.getLower()), (int) Math.floor(yr.getLower()),
				(int) Math.ceil(xr.getUpper()), (int) Math.ceil(yr.getUpper())};
	}

	private Map<Integer, Reference<Dataset>> mipMap;
	private Map<Integer, Reference<Dataset>> maskMap;
	private Collection<IDownSampleListener> downsampleListeners;

	private IDataset getDownsampled(Dataset image) {

		return getDownsampled(image, getDownsampleTypeDiamond());
	}

	/**
	 * Uses caches based on bin, not DownsampleMode.
	 * @param image
	 * @param mode
	 * @return
	 */
	private Dataset getDownsampled(Dataset image, DownsampleMode mode) {

		// Down sample, no point histogramming the whole thing
		final int bin = getDownsampleBin();

		boolean newBin = false;
		if (currentDownSampleBin!=bin) newBin = true;

		try {
			this.currentDownSampleBin = bin;
			if (bin==1) {
				logger.trace("No downsample bin (or bin=1)");
				return image; // nothing to downsample
			}

			if (image.getDType()!=Dataset.BOOL) {
				if (mipMap!=null && mipMap.containsKey(bin) && mipMap.get(bin).get()!=null) {
					logger.trace("Downsample bin used, "+bin);
					return mipMap.get(bin).get();
				}
			} else {
				if (maskMap!=null && maskMap.containsKey(bin) && maskMap.get(bin).get()!=null) {
					logger.trace("Downsample mask bin used, "+bin);
					return maskMap.get(bin).get();
				}
			}

			final Downsample downSampler = new Downsample(mode, new int[]{bin,bin});
			List<? extends IDataset>   sets = downSampler.value(image);
			final Dataset set = DatasetUtils.convertToDataset(sets.get(0));

			if (image.getDType()!=Dataset.BOOL) {
				if (mipMap==null) mipMap = new HashMap<Integer,Reference<Dataset>>(3);
				mipMap.put(bin, new SoftReference<Dataset>(set));
				logger.trace("Downsample bin created, "+bin);
			} else {
				if (maskMap==null) maskMap = new HashMap<Integer,Reference<Dataset>>(3);
				maskMap.put(bin, new SoftReference<Dataset>(set));
				logger.trace("Downsample mask bin created, "+bin);
			}

			return set;

		} finally {
			if (newBin) { // We fire a downsample event.
				fireDownsampleListeners(new DownSampleEvent(this, bin));
			}
		}
	}

	protected void fireDownsampleListeners(DownSampleEvent evt) {
		if (downsampleListeners==null) return;
		for (IDownSampleListener l : downsampleListeners) l.downSampleChanged(evt);
	}

	@Override
	public int getBin() {
		return currentDownSampleBin;
	}

	/**
	 * Add listener to be notifed if the dawnsampling changes.
	 * @param l
	 */
	@Override
	public void addDownsampleListener(IDownSampleListener l) {
		if (downsampleListeners==null) downsampleListeners = new HashSet<IDownSampleListener>(7);
		downsampleListeners.add(l);
	}

	/**
	 * Remove listener so that it is not notified.
	 * @param l
	 */
	@Override
	public void removeDownsampleListener(IDownSampleListener l) {
		if (downsampleListeners==null) return;
		downsampleListeners.remove(l);
	}

	@Override
	public IDataset getDownsampled() {
		return getDownsampled(getImage());
	}

	public IDataset getDownsampledMask() {
		if (getMask()==null) return null;
		return getDownsampled(getMask(), DownsampleMode.MINIMUM);
	}

	/**
	 * Returns the bin for downsampling, either 1,2,4 or 8 currently.
	 * This gives a pixel count of 1,4,16 or 64 for the bin. If 1 no
	 * binning at all is done and no downsampling is being done, getDownsampled()
	 * will return the Dataset ok even if bin is one (no downsampling).
	 * 
	 * @param slice
	 * @param bounds
	 * @return
	 */
	public int getDownsampleBin() {

		if (globalRange != null) {
			return 1;
		}

		final XYRegionGraph graph      = (XYRegionGraph)getXAxis().getParent();
		final Rectangle     realBounds = graph.getRegionArea().getBounds();

		double rwidth  = getSpan(getXAxis());
		double rheight = getSpan(getYAxis());

		int iwidth  = realBounds.width;
		int iheight = realBounds.height;

		int max = 1024;
		int ret = -1;
		for (int i = 2 ; i <= max; i *= 2) {
			if (iwidth>(rwidth/i) || iheight>(rheight/i)) {
				ret = i/2;
				break;
			}
		}
		// We make sure that the bin is no smaller than 1/64 of the shape
		int dataSide  = Math.max(image.getShapeRef()[0], image.getShapeRef()[1]);
		double sixtyF = dataSide/64;
		if (ret>sixtyF) ret = (int)sixtyF; // No need to round, int portion accurate enough
		if (ret<1)      ret = 1;

		return ret;
	}

	private double getSpan(Axis axis) {
		final Range range = axis.getRange();
		return Math.max(range.getUpper(),range.getLower()) - Math.min(range.getUpper(), range.getLower());
	}

	private boolean lastAspectRatio = true;
	private IntensityLabelPainter intensityLabelPainter;
	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);

		/**
		 * This is not actually needed except that when there
		 * are a number of opens of an image, e.g. when moving
		 * around an h5 gallery with arrow keys, it looks smooth 
		 * with this in.
		 */
		if (scaledData.getScaledImage()==null || !isKeepAspectRatio() || lastAspectRatio!=isKeepAspectRatio()) {
			boolean imageReady = createScaledImage(ImageScaleType.NO_REIMAGE, null);
			if (!imageReady && dirty == null) {
				return;
			}
			lastAspectRatio = isKeepAspectRatio();
		}

		if (dirty != null) {
			boolean imageReady = createScaledImage(dirty, null);
			dirty = null;
			if (!imageReady) {
				return;
			}
		}

		graphics.pushState();

		// Offsets and scaled image are calculated in the createScaledImage method.
		if (scaledData.getScaledImage()!=null) {
			graphics.drawImage(scaledData.getScaledImage(), scaledData.getXPosition(), scaledData.getYPosition());
		}

		if (isLabelZoom && plottingSystem.isShowValueLabels() && scaledData!=null) {
			if (intensityLabelPainter==null) intensityLabelPainter = new IntensityLabelPainter(plottingSystem, this);
			intensityLabelPainter.paintIntensityLabels(graphics);
		}

		graphics.popState();
	}

	private boolean isKeepAspectRatio() {
		return getXAxis().isKeepAspect() && getYAxis().isKeepAspect();
	}

	public void sleep() {
		if (mipMap!=null)           mipMap.clear();
		if (maskMap!=null)          maskMap.clear();
		scaledData.disposeImage();
	}
	public void remove() {
		if (mipMap!=null)           mipMap.clear();
		if (maskMap!=null)          maskMap.clear();
		scaledData.disposeImage();

		if (paletteListeners!=null) paletteListeners.clear();
		paletteListeners = null;
		if (downsampleListeners!=null) downsampleListeners.clear();
		downsampleListeners = null;

		if (getParent()!=null) getParent().remove(this);
		xAxis.removeListener(this);
		yAxis.removeListener(this);
		xAxis.setInverted(false); // by default, origin is at top left so reset for XY plots
		yAxis.setInverted(false);

		axisRedrawActive = false;
		if (imageServiceBean!=null) imageServiceBean.dispose();

		this.imageServiceBean = null;
		this.service          = null;
		this.intensityScale   = null;

		if (dynamic != null) {
			dynamic.removeDataListener(this);
			dynamic = null;
		}

		this.image            = null;
		this.rgbDataset       = null;
		this.fullMask         = null;
	}

	@Override
	public void dispose() {
		remove();
		resetAxes();
	}

	@Override
	public Dataset getData() {
		return image;
	}

	@Override
	public IDataset getRGBData() {
		return rgbDataset;
	}


	/**
	 * Create a slice of data from given ranges
	 * @param xr
	 * @param yr
	 * @return
	 */
	private final Dataset slice(Range xr, Range yr, final Dataset data) {

		// Check that a slice needed, this speeds up the initial show of the image.
		final int[] shape = getImageShape(data);
		final int[] imageRanges = getImageBounds(shape, getImageOrigin());
		final int[] bounds = getBounds(xr, yr);
		if (imageRanges!=null && Arrays.equals(imageRanges, bounds)) {
			return data;
		}

		int[] xRange = getRange(bounds, shape[0], 0, false);
		int[] yRange = getRange(bounds, shape[1], 1, false);

		try {
			return data.getSliceView(new int[]{xRange[0],yRange[0]}, new int[]{xRange[1],yRange[1]}, null);

		} catch (IllegalArgumentException iae) {
			logger.error("Cannot slice image", iae);
			return data;
		}
	}

	private static final int[] getRange(int[] bounds, int side, int index, boolean inverted) {
		int start = bounds[index];
		if (inverted) start = side-start;

		int stop  = bounds[2+index];
		if (inverted) stop = side-stop;

		if (start>stop) {
			start = bounds[2+index];
			if (inverted) start = side-start;

			stop  = bounds[index];
			if (inverted) stop = side-stop;
		}

		return new int[]{start, stop};
	}

	private boolean axisRedrawActive = true;

	private boolean listenToAxisChanges = true;

	@Override
	public void axisRangeChanged(Axis axis, Range oldRange, Range newRange) {
		if (listenToAxisChanges) {
			if (oldRange.isMinBigger() ^ newRange.isMinBigger()) {
				imageTransposed = !imageTransposed;
				if (imageServiceBean != null) {
					imageServiceBean.setTransposed(imageTransposed);
				}
				flipImageOriginFromAxes(axis.isHorizontal());
			}
		}
		updateImageDirty(ImageScaleType.REIMAGE_ALLOWED);
	}

	private void flipImageOriginFromAxes(boolean isHorizontal) {
		ImageOrigin imageOrigin = getImageOrigin();
		switch (imageOrigin) {
		case TOP_LEFT:
		default:
			imageOrigin = isHorizontal ? ImageOrigin.TOP_RIGHT : ImageOrigin.BOTTOM_LEFT;
			break;
		case BOTTOM_LEFT:
			imageOrigin = isHorizontal ? ImageOrigin.BOTTOM_RIGHT : ImageOrigin.TOP_LEFT;
			break;
		case BOTTOM_RIGHT:
			imageOrigin = isHorizontal ? ImageOrigin.BOTTOM_LEFT: ImageOrigin.TOP_RIGHT;
			break;
		case TOP_RIGHT:
			imageOrigin = isHorizontal ? ImageOrigin.TOP_LEFT : ImageOrigin.BOTTOM_RIGHT;
			break;
		}

		imageServiceBean.setOrigin(imageOrigin);
	}

	/**
	 * We do a bit here to ensure that 
	 * not too many calls to createScaledImage(...) are made.
	 */
	@Override
	public void axisRevalidated(Axis axis) {
		if (axis.isYAxis()) updateAxisRange(axis);
	}

	private void updateAxisRange(Axis axis) {
		if (!axisRedrawActive) return;
		updateImageDirty(ImageScaleType.REIMAGE_ALLOWED);
	}

	private void updateImageDirty(ImageScaleType type) {

		if (dirty == null) dirty = type;

		if (dirty.ordinal() < type.ordinal()) {
			dirty = type;
		}

	}

	private void setAxisRedrawActive(boolean b) {
		this.axisRedrawActive = b;
	}

	// does the transpose if necessary
	private int[] getImageShape(Dataset image) {
		int[] shape = image.getShapeRef();
		if (imageTransposed) {
			return new int[] { shape[1], shape[0] };
		}
		return shape;
	}
	/*
	 * T=false                                 T=true                            
	 *                                                                           
	 * TL                TR                    TL                TR              
	 *  x = 0, shape[1]   x = shape[0], 0       x = 0, shape[1]   x = shape[1], 0
	 *  y = shape[0], 0   y = shape[1], 0       y = shape[0], 0   y = shape[0], 0
	 *                                                                           
	 * BL                BR                    BL                BR              
	 *  x = 0, shape[0]   x = shape[1], 0       x = 0, shape[1]   x = shape[0], 0
	 *  y = 0, shape[1]   y = 0, shape[0]       y = 0, shape[0]   y = 0, shape[1]
	 */

	public void performAutoscale() {
		if (globalRange != null) {
			xAxis.setRange(globalRange[0], globalRange[1]);
			yAxis.setRange(globalRange[3], globalRange[2]);
			return;
		}

		int[] shape = getImageShape(image);
		switch (getImageOrigin()) {
		case TOP_LEFT:
		default:
			xAxis.setRange(0, shape[1]);
			yAxis.setRange(shape[0], 0);
			break;
		case BOTTOM_LEFT:
			xAxis.setRange(0, shape[0]);
			yAxis.setRange(0, shape[1]);
			break;
		case BOTTOM_RIGHT:
			xAxis.setRange(shape[1], 0);
			yAxis.setRange(0, shape[0]);
			break;
		case TOP_RIGHT:
			xAxis.setRange(shape[0], 0);
			yAxis.setRange(shape[1], 0);
			break;
		}
	}

	private static final int[] getImageBounds(int[] shape, ImageOrigin origin) {
		switch (origin) {
		case TOP_LEFT:
			return new int[] {0, shape[0], shape[1], 0};
		case BOTTOM_LEFT:
			return new int[] {0, 0, shape[0], shape[1]};
		case BOTTOM_RIGHT:
			return new int[] {shape[1], 0, 0, shape[0]};
		case TOP_RIGHT:
			return new int[] {shape[0], shape[1], 0, 0};
		}
		return null;
	}

	/**
	 * Rotates the image so that its origin is in given corner
	 */
	public void setImageOrigin(ImageOrigin imageOrigin) {
		if (getImageOrigin() == imageOrigin) {
			return;
		}
		logger.trace("Image origin changed to: " + imageOrigin.getLabel());
		if (imageServiceBean != null) {
			imageServiceBean.setOrigin(imageOrigin);
		}
		updateAxesAndImage();
	}

	private void updateAxesAndImage() {
		if (this.mipMap != null) {
			mipMap.clear();
		}
		createAxisBounds();
		listenToAxisChanges = false;
		try {
			performAutoscale();
		} finally {
			listenToAxisChanges = true;
		}
		createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
		layout();
		fireImageOriginListeners();
	}

	/**
	 * Creates new axis bounds, updates the label data set
	 */
	private void createAxisBounds() {

		if (globalRange != null) {
			((AspectAxis)getXAxis()).setTitle(axes.get(0).getName());
			((AspectAxis)getYAxis()).setTitle(axes.get(1).getName());
			return;
		}

		final int[] shape = image.getShapeRef();
		boolean leading = getImageOrigin().isOnLeadingDiagonal();
		boolean flip = !(imageTransposed ^ leading);

		if (flip) {
			setupAxis(getXAxis(), new Range(0,shape[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
			setupAxis(getYAxis(), new Range(0,shape[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
		} else {
			setupAxis(getXAxis(), new Range(0,shape[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
			setupAxis(getYAxis(), new Range(0,shape[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
		}
	}

	private void setupAxis(Axis axis, Range bounds, Dataset labels) {
		((AspectAxis) axis).setMaximumRange(bounds);
		((AspectAxis) axis).setLabelDataAndTitle(labels);
	}

	@Override
	public ImageOrigin getImageOrigin() {
		if (imageServiceBean==null) return ImageOrigin.TOP_LEFT;
		return imageServiceBean.getOrigin();
	}

	public void setImageTransposed(boolean imageTransposed) {
		if (this.imageTransposed == imageTransposed) {
			return;
		}

		logger.trace("Image transpose changed to: " + imageTransposed);
		this.imageTransposed = imageTransposed;
		if (imageServiceBean != null) {
			imageServiceBean.setTransposed(imageTransposed);
		}

		updateAxesAndImage();
	}

	private boolean rescaleHistogram = true;

	public boolean isRescaleHistogram() {
		return rescaleHistogram;
	}

	@Override
	public void setRescaleHistogram(boolean rescaleHistogram) {
		this.rescaleHistogram = rescaleHistogram;
		fireSetRescaleListeners();
	}

	private RGBDataset rgbDataset;

	@Override
	public boolean setData(ILazyDataset im, List<? extends IDataset> axes, boolean performAuto) {
		if (im instanceof IDynamicShape) {
			return internalSetDynamicData((IDynamicShape) im);
		}
		return setDataInternal(im, axes, performAuto);
	}

	@Override
	public void setDynamicData(IDynamicShape dynamic) {
		internalSetDynamicData(dynamic);
	}

	private boolean internalSetDynamicData(IDynamicShape dynamic) {
		this.dynamic = dynamic;
		ILazyDataset lazy = dynamic instanceof ILazyDataset ? (ILazyDataset) dynamic : dynamic.getDataset(); 
		boolean flag = setDataInternal(lazy, null, false);
		dynamic.addDataListener(this);
		return flag;
	}

	/**
	 * Called when the internal data of image has changed.
	 */
	@Override
	public void dataChangePerformed(final DataEvent evt) {
		if (dynamic == null) {
			return;
		}

		try {
			final IDataset slice = DatasetUtils.sliceAndConvertLazyDataset(dynamic.getDataset());
			if (Display.getDefault().getThread()==Thread.currentThread()) {
				setDataInternal(slice, axes, plottingSystem.isRescale());
				updateImageDirty(ImageScaleType.FORCE_REIMAGE);
				repaint();
			} else {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						setDataInternal(slice, axes, plottingSystem.isRescale());
						updateImageDirty(ImageScaleType.FORCE_REIMAGE);
						repaint();
					}
				});
			}
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}
	}

	private boolean setDataInternal(ILazyDataset lazy, List<? extends IDataset> axes, boolean performAuto) {

		Dataset im = null;
		try {
			//don't take view of Dataset, could lose metadata listeners
			im = (lazy instanceof Dataset) ? (Dataset)lazy : DatasetUtils.sliceAndConvertLazyDataset(lazy);
		} catch (DatasetException e) {
		}
		if (im == null) {
			return false;
		}

		// We are just assigning the data before the image is live.
		if (getParent()==null && !performAuto && globalRange == null) {
			this.image = im;
			internalSetAxes(axes);
			// is this enough?
			if (imageServiceBean==null){ 
				imageServiceBean = new ImageServiceBean();
			}

			imageServiceBean.setImage(im);

			imageServiceBean.setLogColorScale(getPreferenceStore().getBoolean(PlottingConstants.CM_LOGSCALE));
			//shouldn't rehistogram here, may not be UI thread
			//			if(imageServiceBean.isLogColorScale())
			//				rehistogram();

			return false;
		}

		if (getPreferenceStore().getBoolean(PlottingConstants.IGNORE_RGB) && im instanceof RGBDataset) {
			RGBDataset rgb = (RGBDataset) im;
			im = rgb.createGreyDataset(DoubleDataset.class);
			rgbDataset = rgb;
		} else {
			rgbDataset = null;
		}
		if (plottingSystem!=null) try {
			final TraceWillPlotEvent evt = new TraceWillPlotEvent(this, false);
			evt.setImageData(im, axes);
			evt.setNewImageDataSet(false);
			plottingSystem.fireWillPlot(evt);
			if (!evt.doit) return false;
			if (evt.isNewImageDataSet()) {
				im = DatasetUtils.convertToDataset(evt.getImage());
				axes = evt.getAxes();
			}
		} catch (Throwable ignored) {
			// We allow things to proceed without a warning.
			//			ignored.printStackTrace();
		}

		// The image is drawn low y to the top left but the axes are low y to the bottom right
		// We do not currently reflect it as it takes too long. Instead in the slice
		// method, we allow for the fact that the dataset is in a different orientation to 
		// what is plotted.
		if (image==null) return false;
		this.image = im;
		if (this.mipMap!=null)  mipMap.clear();
		scaledData.disposeImage();

		if (imageServiceBean==null) imageServiceBean = new ImageServiceBean();
		imageServiceBean.setImage(im);

		if (service==null) service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);
		if (rescaleHistogram) {
			final double[] fa = service.getFastStatistics(imageServiceBean);
			setMin(fa[0]);
			setMax(fa[1]);
		}

		setAxes(axes, performAuto);

		if (plottingSystem!=null) {
			try {
				if (plottingSystem.getTraces().contains(this)) {
					plottingSystem.fireTraceUpdated(new TraceEvent(this));
				}
			} catch (Throwable ignored) {
				// We allow things to proceed without a warning.
			}

			try {
				if (getPreferenceStore().getBoolean(PlottingConstants.SHOW_INTENSITY)) {
					plottingSystem.setShowIntensity(!(im instanceof RGBDataset));
				}
			} catch (Exception ne) { // Not the end of the world if this fails!
				logger.warn("Could not set whether to show intensity scale", ne);
			}
		}

		return true;
	}

	private void internalSetAxes(List<? extends IDataset> axes) {
		if (this.axes != axes) {
			if (axes == null) {
				this.axes = null;
			} else {
				this.axes = new ArrayList<Dataset>(axes.size());
				for (IDataset i : axes) {
					this.axes.add(DatasetUtils.convertToDataset(i));
				}
			}
		}
	}

	@Override
	public void setAxes(List<? extends IDataset> axes, boolean performAuto) {
		internalSetAxes(axes);

		if (getXAxis() == null || getYAxis() == null) {
			return;
		}

		createAxisBounds();

		if (axes==null) {
			getXAxis().setTitle("");
			getYAxis().setTitle("");
		} else if (axes.size() == 0 || axes.get(0) == null) {
			getXAxis().setTitle("");
		} else if (axes.size() < 2 || axes.get(1) == null) {
			getYAxis().setTitle("");
		}

		if (globalRange != null && xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {

			AspectAxis ax = (AspectAxis)xAxis;
			AspectAxis ay = (AspectAxis)yAxis;

			if (ax.isKeepAspect() && ay.isKeepAspect()) {
				ax.setKeepAspectWith(ay);
				ay.setKeepAspectWith(ax);
			}
		}

		if (performAuto) {
			try {
				setAxisRedrawActive(false);
				performAutoscale();
			} finally {
				setAxisRedrawActive(true);
			}
		} else {
			//			createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
			updateImageDirty(ImageScaleType.FORCE_REIMAGE);
			repaint();
		}
	}


	public Number getMin() {
		return imageServiceBean.getMin();
	}

	public void setMin(Number min) {
		if (imageServiceBean==null) return;

		imageServiceBean.setMin(min);
		try {
			intensityScale.setMin(min.doubleValue());
		} catch (Exception e) {
			logger.error("Cannot set scale of intensity!",e);
		}
		fireMinDataListeners();

	}

	public Number getMax() {
		return imageServiceBean.getMax();
	}

	public void setMax(Number max) {

		if (imageServiceBean==null) return;
		imageServiceBean.setMax(max);
		try {
			intensityScale.setMax(max.doubleValue());
		} catch (Exception e) {
			logger.error("Cannot set scale of intensity!",e);
		}
		fireMaxDataListeners();

	}

	@Override
	public ImageServiceBean getImageServiceBean() {
		return imageServiceBean;
	}

	private Collection<IPaletteListener> paletteListeners;


	@Override
	public void addPaletteListener(IPaletteListener pl) {
		if (paletteListeners==null) paletteListeners = new HashSet<IPaletteListener>(11);
		paletteListeners.add(pl);
	}

	@Override
	public void removePaletteListener(IPaletteListener pl) {
		if (paletteListeners==null) return;
		paletteListeners.remove(pl);
	}


	private void firePaletteDataListeners(PaletteData paletteData) {
		if (paletteListeners==null) return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData()); // Important do not let Mark get at it :)
		for (IPaletteListener pl : paletteListeners) pl.paletteChanged(evt);
	}
	private void fireMinDataListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.minChanged(evt);
	}
	private void fireMaxDataListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.maxChanged(evt);
	}
	private void fireMaxCutListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.maxCutChanged(evt);
	}
	private void fireMinCutListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.minCutChanged(evt);
	}
	private void fireNanBoundsListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.nanBoundsChanged(evt);
	}
	private void fireMaskListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.maskChanged(evt);
	}
	private void fireImageOriginListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.imageOriginChanged(evt);
	}

	private void fireSetRescaleListeners() {
		if (paletteListeners==null) return;
		if (!imageCreationAllowed)  return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (IPaletteListener pl : paletteListeners) pl.rescaleHistogramChanged(evt);
	}

	@Override
	public DownsampleType getDownsampleType() {
		return downsampleType;
	}

	@Override
	public void setDownsampleType(DownsampleType type) {

		if (this.mipMap!=null)  mipMap.clear();
		if (this.maskMap!=null) maskMap.clear();
		this.downsampleType = type;
		updateImageDirty(ImageScaleType.FORCE_REIMAGE);
		getPreferenceStore().setValue(BasePlottingConstants.DOWNSAMPLE_PREF, type.getLabel());
		repaint();
	}

	private DownsampleMode getDownsampleTypeDiamond() {
		switch(getDownsampleType()) {
		case MEAN:
			return DownsampleMode.MEAN;
		case MAXIMUM:
			return DownsampleMode.MAXIMUM;
		case MINIMUM:
			return DownsampleMode.MINIMUM;
		case POINT:
			return DownsampleMode.POINT;
		}
		return DownsampleMode.MEAN;
	}

	@Override
	public void rehistogram() {
		if (imageServiceBean==null) return;
		imageServiceBean.setMax(null);
		imageServiceBean.setMin(null);
		createScaledImage(ImageScaleType.REHISTOGRAM, null);
		// Max and min changed in all likelihood
		fireMaxDataListeners();
		fireMinDataListeners();
		repaint();
	}

	public void remask() {
		if (imageServiceBean==null) return;

		createScaledImage(rescaleHistogram ? ImageScaleType.REHISTOGRAM : ImageScaleType.FORCE_REIMAGE, null);

		// Max and min changed in all likely-hood
		fireMaskListeners();
		repaint();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IDataset> getAxes() {
		List<? extends IDataset> t = axes;
		return (List<IDataset>) t;
	}

	/**
	 * return the HistoType being used
	 * @return
	 */
	@Override
	public HistoType getHistoType() {
		if (imageServiceBean==null) return null;
		return imageServiceBean.getHistogramType();
	}

	/**
	 * Sets the histo type.
	 */
	@Override
	public boolean setHistoType(HistoType type) {

		if (imageServiceBean==null) return false;
		HistoType orig = imageServiceBean.getHistogramType();
		if (orig == type) { // do nothing (especially if min/max are changed)
			return true;
		}

		imageServiceBean.setHistogramType(type);
		getPreferenceStore().setValue(BasePlottingConstants.HISTO_PREF, type.getLabel());
		//		boolean histoOk = createScaledImage(ImageScaleType.REHISTOGRAM, null);
		updateImageDirty(ImageScaleType.REHISTOGRAM);
		repaint();

		return true;
	}

	@Override
	public ITrace getTrace() {
		return this;
	}

	@Override
	public void setTrace(ITrace trace) {
		// Does nothing, you cannot change the trace, this is the trace.
	}

	public void setImageUpdateActive(boolean active) {
		this.imageCreationAllowed = active;
		if (active) {
			createScaledImage(ImageScaleType.FORCE_REIMAGE, null);
			repaint();
		}
		firePaletteDataListeners(getPaletteData());
	}

	@Override
	public HistogramBound getMinCut() {
		return imageServiceBean.getMinimumCutBound();
	}

	@Override
	public void setMinCut(HistogramBound bound) {

		storeBound(bound, BasePlottingConstants.MIN_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setMinimumCutBound(bound);
		fireMinCutListeners();

	}

	private void storeBound(HistogramBound bound, String prop) {
		if (bound!=null) {
			getPreferenceStore().setValue(prop, bound.toString());
		} else {
			getPreferenceStore().setValue(prop, "");
		}
	}

	@Override
	public HistogramBound getMaxCut() {
		return imageServiceBean.getMaximumCutBound();
	}

	@Override
	public void setMaxCut(HistogramBound bound) {

		storeBound(bound, BasePlottingConstants.MAX_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setMaximumCutBound(bound);
		fireMaxCutListeners();
	}

	@Override
	public HistogramBound getNanBound() {
		return imageServiceBean.getNanBound();
	}

	@Override
	public void setNanBound(HistogramBound bound) {
		storeBound(bound, BasePlottingConstants.NAN_CUT);
		if (imageServiceBean==null) return;
		imageServiceBean.setNanBound(bound);
		fireNanBoundsListeners();
	}

	private Dataset fullMask;
	/**
	 * The masking dataset of there is one, normally null.
	 * @return
	 */
	public Dataset getMask() {
		return fullMask;
	}

	/**
	 * 
	 * @param bd
	 */
	public void setMask(IDataset mask) {
		Dataset dMask = DatasetUtils.convertToDataset(mask);
		if (dMask != null) {
			dMask = DatasetUtils.rotate90(dMask, -getImageOrigin().ordinal());
		}
		if (dMask != null && image != null) {
			int[] iShape = image.getShapeRef();
			int[] mShape = dMask.getShapeRef();
			if (!ShapeUtils.areShapesCompatible(iShape, mShape)) {
				BooleanDataset maskDataset = DatasetFactory.zeros(BooleanDataset.class, iShape);
				maskDataset.setName("mask");
				maskDataset.fill(true);

				final int yMin = Math.min(iShape[0], mShape[0]);
				final int xMin = Math.min(iShape[1], mShape[1]);
				for (int y = 0; y < yMin; ++y) {
					for (int x = 0; x < xMin; ++x) {
						try {
							// We only add the falses
							if (!dMask.getBoolean(y, x)) {
								maskDataset.set(Boolean.FALSE, y, x);
							}
						} catch (Throwable ignored) {
							continue;
						}
					}
				}

				dMask = maskDataset;
			}
		}
		if (maskMap!=null) maskMap.clear();
		fullMask = dMask;
		remask();
	}

	private boolean userTrace = true;
	@Override
	public boolean isUserTrace() {
		return userTrace;
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		this.userTrace = isUserTrace;
	}

	public boolean isMaximumZoom() {
		return isMaximumZoom;
	}

	private Object userObject;

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	/**
	 * If the axis data set has been set, this method will return 
	 * a selection region in the coordinates of the axes labels rather
	 * than the indices.
	 * 
	 * Ellipse and Sector rois are not currently supported.
	 * 
	 * @return ROI in label coordinates. This roi is not that useful after it
	 *         is created. The data processing needs rois with indices.
	 */
	@Override
	public IROI getRegionInAxisCoordinates(final IROI roi) throws Exception {

		if (!TraceUtils.isCustomAxes(this)) return roi;

		final Dataset xl = axes.get(0); // May be null
		final Dataset yl = axes.get(1); // May be null

		if (roi instanceof LinearROI) {
			double[] sp = ((LinearROI)roi).getPoint();
			double[] ep = ((LinearROI)roi).getEndPoint();
			TraceUtils.transform(xl,0,sp,ep);
			TraceUtils.transform(yl,1,sp,ep);
			return new LinearROI(sp, ep);

		} else if (roi instanceof IPolylineROI) {
			IPolylineROI proi = (IPolylineROI)roi;
			final PolylineROI ret = (proi instanceof PolygonalROI) ? new PolygonalROI() : new PolylineROI();
			for (IROI pointROI : proi) {
				double[] dp = pointROI.getPointRef();
				TraceUtils.transform(xl,0,dp);
				TraceUtils.transform(yl,1,dp);
				ret.insertPoint(dp);
			}

		} else if (roi instanceof PointROI) {
			double[] dp = roi.getPointRef();
			TraceUtils.transform(xl,0,dp);
			TraceUtils.transform(yl,1,dp);
			return new PointROI(dp);

		} else if (roi instanceof RectangularROI) {
			RectangularROI rroi = (RectangularROI)roi;
			double[] sp=roi.getPoint();
			double[] ep=rroi.getEndPoint();
			TraceUtils.transform(xl,0,sp,ep);
			TraceUtils.transform(yl,1,sp,ep);

			return new RectangularROI(sp[0], sp[1], ep[0]-sp[0], sp[1]-ep[1], rroi.getAngle());

		} else {
			throw new Exception("Unsupported roi "+roi.getClass());
		}

		return roi;
	}

	@Override
	public double[] getPointInAxisCoordinates(final double[] point) throws Exception {
		if (axes == null || axes.size() == 0 || image == null)
			return point;

		final double[] ret = point.clone();
		final int[] shape = image.getShapeRef();

		final Dataset xl = axes.get(0); // May be null
		if (TraceUtils.isAxisCustom(xl, shape[1])) {
			TraceUtils.transform(xl, 0, ret);
		}

		if (axes.size() < 2) {
			return ret;
		}

		final Dataset yl = axes.get(1); // May be null
		if (TraceUtils.isAxisCustom(yl, shape[0])) {
			TraceUtils.transform(yl, 1, ret);
		}

		return ret;
	}

	@Override
	public double[] getPointInImageCoordinates(final double[] axisLocation) throws Exception {
		if (axes == null || axes.size() == 0 || image == null)
			return axisLocation;

		final double[] ret = axisLocation.clone();
		final int[] shape = image.getShapeRef();

		final Dataset xl = DatasetUtils.convertToDataset(axes.get(0)); // May be null
		if (TraceUtils.isAxisCustom(xl, shape[1])) {
			double x = axisLocation[0];
			if (Double.isNaN(x)) {
				ret[0] = Double.NaN;
			} else {
				List<Double> c = DatasetUtils.crossings(xl, x);
				ret[0] = c.size() > 0 ? c.get(0) : Double.NaN;
			}
		}

		if (axes.size() < 2)
			return ret;

		final Dataset yl = DatasetUtils.convertToDataset(axes.get(1)); // May be null
		if (TraceUtils.isAxisCustom(yl, shape[0])) {
			double y = axisLocation[1];
			if (Double.isNaN(y)) {
				ret[1] = Double.NaN;
			} else {
				List<Double> c = DatasetUtils.crossings(yl, y);
				ret[1] = c.size() > 0 ? c.get(0) : Double.NaN;
			}
		}

		return ret;
	}

	public IPlottingSystem<?> getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(IPlottingSystem<?> plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	@Override
	public boolean isActive() {
		return getParent()!=null;
	}

	@Override
	public List<String> getAxesNames() {
		return Arrays.asList(xAxis.getTitle(), yAxis.getTitle());
	}

	@Override
	public boolean is3DTrace() {
		return false;
	}

	@Override
	public int getRank() {
		return 2;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
		rehistogram();
	}

	@Override
	public boolean hasTrueAxes(){
		return globalRange != null;
	}

	@Override
	public void axisForegroundColorChanged(Axis axis, Color oldColor, Color newColor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void axisTitleChanged(Axis axis, String oldTitle, String newTitle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void axisAutoScaleChanged(Axis axis, boolean oldAutoScale, boolean newAutoScale) {
		// TODO Auto-generated method stub

	}

	@Override
	public void axisLogScaleChanged(Axis axis, boolean old, boolean logScale) {
		// TODO Auto-generated method stub

	}

	private void clearAspect(Axis axis) {
		if (axis instanceof AspectAxis) {
			AspectAxis aaxis = (AspectAxis) axis;
			aaxis.setKeepAspectWith(null);
			aaxis.setMaximumRange(null);
		}
	}

	/**
	 * Reset axes back to not keep aspect ratio and original ticks settings
	 */
	public void resetAxes() {
		clearAspect(xAxis);
		clearAspect(yAxis);

		resetAxisTicks(xAxis, xTicksAtEnd);
		resetAxisTicks(yAxis, yTicksAtEnd);
	}
}
