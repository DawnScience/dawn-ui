package org.dawb.workbench.plotting.system.swtxy;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.IAxisListener;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.dawb.common.services.IImageService;
import org.dawb.common.services.IImageService.ImageServiceBean;
import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.PaletteListener;
import org.dawb.common.util.text.NumberUtils;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.function.Downsample;
import uk.ac.diamond.scisoft.analysis.dataset.function.DownsampleMode;

/**
 * A trace which draws an image to the plot.
 * 
 * @author fcp94556
 *
 */
public class ImageTrace extends Figure implements IImageTrace, IAxisListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ImageTrace.class);

	private String         name;
	private Axis           xAxis;
	private Axis           yAxis;
	private AbstractDataset  image;
	private PaletteData    paletteData;
	private ImageOrigin    imageOrigin;
    private Number         min, max;
	private DownsampleType downsampleType=DownsampleType.MEAN;
	private int            currentDownSampleBin=-1;
	private List<AbstractDataset> axes;
	
	private static Map<IImageTrace.ImageOrigin, IImageService.ImageOrigin> imageOriginaMap;
	static {
		imageOriginaMap = new HashMap<IImageTrace.ImageOrigin, IImageService.ImageOrigin>();
		imageOriginaMap.put(IImageTrace.ImageOrigin.TOP_LEFT,     IImageService.ImageOrigin.TOP_LEFT);
		imageOriginaMap.put(IImageTrace.ImageOrigin.TOP_RIGHT,    IImageService.ImageOrigin.TOP_RIGHT);
		imageOriginaMap.put(IImageTrace.ImageOrigin.BOTTOM_LEFT,  IImageService.ImageOrigin.BOTTOM_LEFT);
		imageOriginaMap.put(IImageTrace.ImageOrigin.BOTTOM_RIGHT, IImageService.ImageOrigin.BOTTOM_RIGHT);
	}
	
	public ImageTrace(final String name, 
			          final Axis xAxis, 
			          final Axis yAxis) {
		
		this.name  = name;
		this.xAxis = xAxis;		
		this.yAxis = yAxis;

		this.paletteData = PaletteFactory.getPalette(Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.P_PALETTE));	
		this.imageOrigin = IImageTrace.ImageOrigin.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.ORIGIN_PREF));
	    		
		xAxis.addListener(this);
		yAxis.addListener(this);
		
		if (xAxis instanceof AspectAxis && yAxis instanceof AspectAxis) {
			
			AspectAxis x = (AspectAxis)xAxis;
			AspectAxis y = (AspectAxis)yAxis;
			x.setKeepAspectWith(y);
			y.setKeepAspectWith(x);		
		}
				
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Axis getxAxis() {
		return xAxis;
	}

	public void setxAxis(Axis xAxis) {
		this.xAxis = xAxis;
	}

	public Axis getyAxis() {
		return yAxis;
	}

	public void setyAxis(Axis yAxis) {
		this.yAxis = yAxis;
	}

	public AbstractDataset getImage() {
		return image;
	}

	public PaletteData getPaletteData() {
		return paletteData;
	}

	public void setPaletteData(PaletteData paletteData) {
		this.paletteData = paletteData;
		createScaledImage(ImageScaleType.FORCE_RESCALE, null);
		repaint();
		firePaletteDataListeners(paletteData);
	}


	private enum ImageScaleType {
		NO_RESCALE,
		RESCALE_ALLOWED,
		FORCE_RESCALE, 
		REHISTOGRAM;
	}
	private Image            scaledImage;
	private ImageData        imageData;
	private ImageServiceBean lastImageServiceBean;
	private boolean          generatingImage = false;
	/**
	 * When this is called the SWT image is created
	 * and saved in the swtImage field. The image is downsampled. If rescaleAllowed
	 * is set to false, the current bin is not checked and the last scaled image
	 * is always used.
	 *  
	 * Do not synchronized this method - it can cause a race condition on linux only.
	 * 
	 * @return true if scaledImage created.
	 */
	private boolean createScaledImage(ImageScaleType rescaleType, final IProgressMonitor monitor) {
			
		
		if (generatingImage) return false;

		boolean requireImageGeneration = imageData==null || 
				                         rescaleType==ImageScaleType.FORCE_RESCALE || 
				                         rescaleType==ImageScaleType.REHISTOGRAM; // We know that it is needed
		
		// If we just changed downsample scale, we force the update.
	    // This allows user resizes of the plot area to be picked up
		// and the larger data size used if it fits.
        if (!requireImageGeneration && rescaleType==ImageScaleType.RESCALE_ALLOWED && currentDownSampleBin>0) {
        	if (getDownsampleBin()!=currentDownSampleBin) {
        		requireImageGeneration = true;
        	}
        }

		final XYRegionGraph graph  = (XYRegionGraph)getxAxis().getParent();
		final Rectangle     rbounds = graph.getRegionArea().getBounds();
		if (rbounds.width<1 || rbounds.height<1) return false;

		if (generatingImage) return false;
		if (monitor!=null && monitor.isCanceled()) return false;

		if (requireImageGeneration) {
			try {
				generatingImage = true;
				AbstractDataset reducedFullImage = getDownsampled(image);

				final IImageService service = (IImageService)PlatformUI.getWorkbench().getService(IImageService.class);

				final ImageServiceBean bean =  new IImageService.ImageServiceBean();
				bean.setImage(reducedFullImage);
				bean.setOrigin(imageOriginaMap.get(getImageOrigin()));
				bean.setPalette(getPaletteData());
				bean.setMonitor(monitor);
				
				if (rescaleType==ImageScaleType.REHISTOGRAM) { // Avoids changing colouring to 
					// max and min of new selection.
					AbstractDataset slice = slice(getAxisBounds());
					float[] fa = getFastStatistics(slice);
					setMin(fa[0]);
					setMax(fa[1]);
				}

				if (getMin()!=null) bean.setMin(getMin());
				if (getMax()!=null) bean.setMax(getMax());
				
				this.imageData   = service.getImageData(bean);
				this.lastImageServiceBean = bean;
				
			} catch (Exception e) {
				logger.error("Cannot create image from data!", e);
			} finally {
				generatingImage = false;
			}
			
		}
		
		if (monitor!=null && monitor.isCanceled()) return false;
		
		try {
			
			if (imageData.width==bounds.width && imageData.height==bounds.height) { 
				// No slice, faster
				if (monitor!=null && monitor.isCanceled()) return false;
				this.scaledImage  = new Image(Display.getDefault(), imageData);

			} else {
				// slice data to get current zoom area
				/**     
				 *      x1,y1--------------x2,y2
				 *        |                  |
				 *        |                  |
				 *        |                  |
				 *      x3,y3--------------x4,y4
				 */
				ImageData data = imageData;
				RESCALE: if (!getAxisBounds().equals(getImageBounds())) {
					final double x1Rat = getXRatio(getX(true));
					final double y1Rat = getYRatio(getY(true));
					final double x4Rat = getXRatio(getX(false));
					final double y4Rat = getYRatio(getY(false));
					
					// If scales are not requiring a slice, break
					if (x1Rat==0d && y1Rat==0d && x4Rat==1d && y4Rat==1d) {
						break RESCALE;
					}
				
					int x1 = (int)Math.round(imageData.width*x1Rat);
					int y1 = (int)Math.round(imageData.height*y1Rat);
					int x4 = (int)Math.round(imageData.width*x4Rat);
					int y4 = (int)Math.round(imageData.height*y4Rat);
					
	
					// Pixel slice on downsampled data = fast!
					final int size   = (x4-x1)*(y4-y1);
					final byte[] pixels = new byte[size];
					final int wid    = (x4-x1);
					for (int y = 0; y < (y4-y1); y++) {
						imageData.getPixels(x1, y1+y, wid, pixels, wid*y);
					}
					
					data = new ImageData((x4-x1), (y4-y1), 8, getPaletteData(), 1, pixels);
				}
				data = data.scaledTo(rbounds.width, rbounds.height);
				this.scaledImage = new Image(Display.getDefault(), data);
			}
			
			return true;
			
		} catch (Throwable ne) {
			logger.error("Image scale error!", ne);
			return false;
		}

	}

	private double getX(boolean isTopLeft) {
		
		double xCoord = isTopLeft ? getxAxis().getRange().getLower() : getxAxis().getRange().getUpper();
		switch (getImageOrigin()) {
		case TOP_LEFT:
			return xCoord;
		case TOP_RIGHT:
			return image.getShape()[0]-xCoord;
		case BOTTOM_RIGHT:
			return image.getShape()[1]-xCoord;
		case BOTTOM_LEFT:
			return xCoord;
		}
		return 0d;
	}

	private double getY(boolean isTopLeft) {
		
		double yCoord = isTopLeft ? getyAxis().getRange().getUpper() : getyAxis().getRange().getLower();
		switch (getImageOrigin()) {
		case TOP_LEFT:
			return yCoord;
		case TOP_RIGHT:
			return yCoord;
		case BOTTOM_RIGHT:
			return image.getShape()[0]-yCoord;
		case BOTTOM_LEFT:
			return image.getShape()[1]-yCoord;
		}
		return 0d;
	}

	private Map<Integer, Reference<Object>> binCache;
	
	private AbstractDataset getDownsampled(AbstractDataset image) {
		
		// Down sample, no point histogramming the whole thing
        final int bin = getDownsampleBin();
        this.currentDownSampleBin = bin;
		if (bin==1) {
	        logger.trace("No downsample bin (or bin=1)");
			return image; // nothing to downsample
		}
		
		if (binCache!=null && binCache.containsKey(bin) && binCache.get(bin).get()!=null) {
	        logger.trace("Downsample bin used, "+bin);
			return (AbstractDataset)binCache.get(bin).get();
		}
		
		final Downsample downSampler = new Downsample(getDownsampleTypeDiamond(), new int[]{bin,bin});
		List<AbstractDataset>   sets = downSampler.value(image);
		final AbstractDataset set = sets.get(0);
		
		if (binCache==null) binCache = new HashMap<Integer,Reference<Object>>(3);
		binCache.put(bin, new SoftReference<Object>(set));
        logger.trace("Downsample bin created, "+bin);
      
		return set;
	}

	/**
	 * Returns the bin for downsampling, either 1,2,4 or 8 currently.
	 * This gives a pixel count of 1,4,16 or 64.
	 * @param slice
	 * @param bounds
	 * @return
	 */
	private int getDownsampleBin() {
		
		final XYRegionGraph graph      = (XYRegionGraph)getxAxis().getParent();
		final Rectangle     realBounds = graph.getRegionArea().getBounds();
		
		double rwidth  = getSpan(getxAxis());
		double rheight = getSpan(getyAxis());
 
		int iwidth  = realBounds.width;
		int iheight = realBounds.height;

		if (iwidth>(rwidth/2d) || iheight>(rheight/2d)) {
			return 1;
		}

		if (iwidth>(rwidth/4d) || iheight>(rheight/4d)) {
			return 2;
		}

		if (iwidth>(rwidth/8d) || iheight>(rheight/8d)) {
			return 4;
		}
		return 8;
	}

	private double getSpan(Axis axis) {
		final Range range = axis.getRange();
		return Math.max(range.getUpper(),range.getLower()) - Math.min(range.getUpper(), range.getLower());
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		
		super.paintFigure(graphics);

		/**
		 * This is not actually needed except that when there
		 * are a number of opens of an image, e.g. when moving
		 * around an h5 gallery with arrow keys, it looks smooth 
		 * with this in.
		 */
		if (scaledImage==null) {
			boolean imageReady = createScaledImage(ImageScaleType.NO_RESCALE, null);
			if (!imageReady) return;
		}

		graphics.pushState();	
		final XYRegionGraph graph  = (XYRegionGraph)xAxis.getParent();
		final Point         loc    = graph.getRegionArea().getLocation();
		
		graphics.drawImage(scaledImage, loc.x, loc.y);
  	    
		graphics.popState();
	}

	public void remove() {
		
		if (binCache!=null)         binCache.clear();
		if (scaledImage!=null)      scaledImage.dispose();
		if (paletteListeners!=null) paletteListeners.clear();
		paletteListeners = null;
        clearAspect(xAxis);
        clearAspect(yAxis);
		getParent().remove(this);
		xAxis.removeListener(this);
		yAxis.removeListener(this);
		axisRedrawActive = false;
		lastImageServiceBean = null;
	}

	private void clearAspect(Axis axis) {
        if (axis instanceof AspectAxis ) {			
			AspectAxis aaxis = (AspectAxis)axis;
			aaxis.setKeepAspectWith(null);
			aaxis.setMaximumRange(null);
		}
	}

	@Override
	public AbstractDataset getData() {
		return image;
	}

	@Override
	public AbstractDataset slice(RegionBounds bounds) {
		
		final AbstractDataset data = getData();
		
		// Check that a slice needed, this speeds up the initial show of the image.
		final RegionBounds imageBounds = getImageBounds(); 
		if (imageBounds!=null && imageBounds.equals(bounds))  {
			return image;
		}
		
		int[] xRange = getRange(bounds, 0, false);
		int[] yRange = getRange(bounds, 1, false);		
				
		try {
			return data.getSlice(new int[]{xRange[0],yRange[0]}, 
					             new int[]{xRange[1],yRange[1]}, 
					             new int[]{1,     1});
			
		}catch (IllegalArgumentException iae) {
			logger.error("Cannot slice image", iae);
			return getData();
		}
	}

	private int[] getRange(RegionBounds bounds, int index, boolean inverted) {
		
		if (bounds.isCircle()) bounds = bounds.getOuterRectangle();
		final int side = image.getShape()[index];
		int start = (int)Math.round(bounds.getP1()[index]);
		if (inverted) start = side-start;
		
		int stop  = (int)Math.round(bounds.getP2()[index]);
		if (inverted) stop = side-stop;

		if (start>stop) {
			start = (int)Math.round(bounds.getP2()[index]);
			if (inverted) start = side-start;
			
			stop  = (int)Math.round(bounds.getP1()[index]);
			if (inverted) stop = side-stop;
		}
		
		return new int[]{start, stop};
	}

	private boolean axisRedrawActive = true;

	@Override
	public void axisRangeChanged(Axis axis, Range old_range, Range new_range) {
		//createScaledImage(true, null);
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
		createScaledImage(ImageScaleType.RESCALE_ALLOWED, null);
	}


	
	private void setAxisRedrawActive(boolean b) {
		this.axisRedrawActive = b;
	}


	private double getXRatio(double x) {
		
		if (getImageOrigin()==ImageOrigin.TOP_LEFT || getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
			return  x / image.getShape()[1];
		} else {
			return  x / image.getShape()[0];
		}
	}
	private double getYRatio(double y) {
		
	    if (getImageOrigin()==ImageOrigin.TOP_LEFT || getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
			return  y / image.getShape()[0];
		} else {
			return  y / image.getShape()[1];
		}
	}
	


	public void performAutoscale() {
		switch(getImageOrigin()) {
		case TOP_LEFT:
			xAxis.setRange(0, image.getShape()[1]);
			yAxis.setRange(image.getShape()[0], 0);	
			break;
			
		case BOTTOM_LEFT:
			xAxis.setRange(0, image.getShape()[0]);
			yAxis.setRange(0, image.getShape()[1]);		
			break;

		case BOTTOM_RIGHT:
			xAxis.setRange(image.getShape()[1], 0);
			yAxis.setRange(0, image.getShape()[0]);		
			break;

		case TOP_RIGHT:
			xAxis.setRange(image.getShape()[0], 0);
			yAxis.setRange(image.getShape()[1], 0);		
			break;
		
		}
	}
	
	private RegionBounds getImageBounds() {
		switch(getImageOrigin()) {
		case TOP_LEFT:
			return new RegionBounds(new double[]{0, image.getShape()[0]},
					                new double[]{image.getShape()[1], 0});	
						
		case BOTTOM_LEFT:
			return new RegionBounds(new double[]{0, 0},
	                                new double[]{image.getShape()[0], image.getShape()[1]});	

		case BOTTOM_RIGHT:
			return new RegionBounds(new double[]{image.getShape()[1], 0},
                                    new double[]{0, image.getShape()[0]});	

		case TOP_RIGHT:
			return new RegionBounds(new double[]{image.getShape()[0], image.getShape()[1]},
                                    new double[]{0,0});	
		
		}
		return null;
	}
	
	private RegionBounds getAxisBounds() {
		double [] p1 = new double[]{getxAxis().getRange().getLower(), getyAxis().getRange().getLower()};
		double [] p2 = new double[]{getxAxis().getRange().getUpper(), getyAxis().getRange().getUpper()};
		return new RegionBounds(p1, p2);
	}

	public void setImageOrigin(ImageOrigin imageOrigin) {
		this.imageOrigin = imageOrigin;
		createAxisBounds();
		performAutoscale();
		createScaledImage(ImageScaleType.FORCE_RESCALE, null);
		repaint();
	}

	/**
	 * Creates new axis bounds, updates the label data set
	 */
	private void createAxisBounds() {
		if (getImageOrigin()==ImageOrigin.TOP_LEFT || getImageOrigin()==ImageOrigin.BOTTOM_RIGHT) {
			setupAxis(getxAxis(), new Range(0,image.getShape()[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
			setupAxis(getyAxis(), new Range(0,image.getShape()[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
		} else {
			setupAxis(getxAxis(), new Range(0,image.getShape()[0]), axes!=null&&axes.size()>1 ? axes.get(1) : null);
			setupAxis(getyAxis(), new Range(0,image.getShape()[1]), axes!=null&&axes.size()>0 ? axes.get(0) : null);
		}
	}
	
	private void setupAxis(Axis axis, Range bounds, AbstractDataset labels) {
		((AspectAxis)axis).setMaximumRange(bounds);
		((AspectAxis)axis).setLabelData(labels);
	}

	@Override
	public ImageOrigin getImageOrigin() {
		return imageOrigin;
	}
	
	@Override
	public void setData(final AbstractDataset image, List<AbstractDataset> axes) {
		// The image is drawn low y to the top left but the axes are low y to the bottom right
		// We do not currently reflect it as it takes too long. Instead in the slice
		// method, we allow for the fact that the dataset is in a different orientation to 
		// what is plotted.
		this.image = image;
		
		final float[] fa = getFastStatistics(image);
		setMin(fa[0]);
		setMax(fa[1]);
		this.axes  = axes;
		this.lastImageServiceBean = null;
		
		createAxisBounds();

 		try {
			setAxisRedrawActive(false);
			performAutoscale();
		} finally {
			setAxisRedrawActive(true);
		}
       
	}

	public Number getMin() {
		return min;
	}

	public void setMin(Number min) {
		this.min = min;
		fireMinDataListeners();
	}

	public Number getMax() {
		return max;
	}

	public void setMax(Number max) {
		this.max = max;
		fireMaxDataListeners();
	}

	@Override
	public ImageServiceBean getImageServiceBean() {
		return lastImageServiceBean;
	}
	
	@Override
	public Number getCalculatedMax() {
		return getMax()!=null ? getMax() : getImageServiceBean().getMax();
	}
	@Override
	public Number getCalculatedMin() {
		return getMin()!=null ? getMin() : getImageServiceBean().getMin();
	}

	private Collection<PaletteListener> paletteListeners;

	@Override
	public void addPaletteListener(PaletteListener pl) {
		if (paletteListeners==null) paletteListeners = new HashSet<PaletteListener>(11);
		paletteListeners.add(pl);
	}

	@Override
	public void removePaletteListener(PaletteListener pl) {
		if (paletteListeners==null) return;
		paletteListeners.remove(pl);
	}
	
	
	private void firePaletteDataListeners(PaletteData paletteData) {
		if (paletteListeners==null) return;
		final PaletteEvent evt = new PaletteEvent(this, paletteData);
		for (PaletteListener pl : paletteListeners) pl.paletteChanged(evt);
	}
	private void fireMinDataListeners() {
		if (paletteListeners==null) return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (PaletteListener pl : paletteListeners) pl.minChanged(evt);
	}
	private void fireMaxDataListeners() {
		if (paletteListeners==null) return;
		final PaletteEvent evt = new PaletteEvent(this, getPaletteData());
		for (PaletteListener pl : paletteListeners) pl.maxChanged(evt);
	}

	@Override
	public DownsampleType getDownsampleType() {
		return downsampleType;
	}
	
	@Override
	public void setDownsampleType(DownsampleType type) {
		this.downsampleType = type;
		createScaledImage(ImageScaleType.FORCE_RESCALE, null);
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
		setMax(null); // I think...
		setMin(null);
		createScaledImage(ImageScaleType.REHISTOGRAM, null);
		// Max and min changed in all likely-hood
		fireMaxDataListeners();
		fireMinDataListeners();
		repaint();
	}
	
	/**
	 * Fast statistcs as a rough guide - this is faster than AbstractDataset.getMin()
	 * and getMax() which may cache but slows the opening of images too much.
	 * 
	 * @param bean
	 * @return [0] = min [1] = max
	 */
	private static float[] getFastStatistics(AbstractDataset image) {
		
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float sum = 0.0f;
		final int size = image.getSize();
		
		for (int index = 0; index<size; ++index) {
				
			final float val = (float)image.getElementDoubleAbs(index);
			sum += val;
			if (val < min) min = val;
			if (val > max) max = val;
			
		}
		float mean = sum / (image.getShape()[0] * image.getShape()[1]);
	
		float retMin = min;
		float retMax = 3*mean;
		if (retMax > max)	retMax = max;
		
		return new float[]{retMin, retMax};

	}

}
