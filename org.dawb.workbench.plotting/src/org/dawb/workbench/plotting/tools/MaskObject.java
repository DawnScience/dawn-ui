package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.image.ShapeType;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;

/**
 * This class is taken directly out of the class of the same name in SDA
 * 
 * The intention is to make the maths available separate to the side plot system.
 * 
 * @author fcp94556
 *
 */
public class MaskObject {

	private static final Logger logger = LoggerFactory.getLogger(MaskObject.class);
	
	public enum MaskMode {
		/**
		 * Draw the mask, ie do the mask
		 */
		DRAW, 
		
		/**
		 * Toggle the masked state
		 */
		TOGGLE, 
		
		/**
		 * Remove the mask at the location.
		 */
		ERASE
	}

	private MaskMode        maskMode;
    private int             lineWidth;
    private boolean         squarePen = false;
    /**
     * The booleans are false to mask and
     * true to leave that way multiply will work.
     */
    private BooleanDataset  maskDataset;
    private AbstractDataset imageDataset;
    

    /**
     * Designed to copy data in an incoming mask onto this one as best as possible.
     * @param savedMask
     */
	public void process(BooleanDataset savedMask) {
		createMaskIfNeeded();
		
		final int[] shape = savedMask.getShape();
		for (int y = 0; y<shape[0]; ++y) {
			for (int x = 0; x<shape[1]; ++x) {
		        try {
		        	// We only add the falses
		        	// 
		        	if (!savedMask.getBoolean(y,x)) {
		        		this.maskDataset.set(Boolean.FALSE, y,x);
		        	}
		        } catch (Throwable ignored) {
		        	continue;
		        }
			}
		}
	}
	
	/**
	 * Attempts to process a pen shape at a given draw2d location.
	 * 
	 * Translate from click location using 'system' parameter.
	 * 
	 * @param loc
	 * @param system
	 */
	public void process(final Point loc, final IPlottingSystem system) {
				
		ShapeType penShape = ShapeType.valueOf(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.MASK_PEN_SHAPE));
        if (penShape==ShapeType.NONE) return;
        
		createMaskIfNeeded();
       
		boolean maskOut       = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.MASK_PEN_MASKOUT);
        final int     penSize = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.MASK_PEN_SIZE);
        final int     rad     = (int)Math.ceil(penSize/2f);

        final IAxis xAxis = system.getSelectedXAxis();
        final IAxis yAxis = system.getSelectedYAxis();


        final List<int[]> span = new ArrayList<int[]>(2);
        Display.getDefault().syncExec(new Runnable() {
        	public void run() {
        		span.add(new int[]{(int)xAxis.getPositionValue(loc.x-rad), (int)yAxis.getPositionValue(loc.y-rad)});
        		span.add(new int[]{(int)xAxis.getPositionValue(loc.x+rad), (int)yAxis.getPositionValue(loc.y+rad)});
        	}
        });
        int[] start = span.get(0);
        int[] end   = span.get(1);

        Boolean mv = maskOut ? Boolean.FALSE : Boolean.TRUE;
        for (int y = start[1]; y<=end[1]; ++y) {
        	for (int x = start[0]; x<=end[0]; ++x) {
        		if (penShape==ShapeType.SQUARE) {
        			maskDataset.set(mv, y, x);

        		} else if (penShape==ShapeType.CIRCLE) {
        			// Check inside circle

        		}
        	}
        }
	}
	
	/**
	 * Designed to be called after processBounds(...) has been called at least once.
	 * Deals with fact that that may leave us with no mask and will create one if needed.
	 * 
	 * @param region
	 * @return
	 */
	public boolean process(IRegion region) {
        return process(null, null, Arrays.asList(new IRegion[]{region}));
	}

	/**
	 * Processes the while of the dataset and sets those values in bounds 
	 * to be false and those outside to be true in the mask.
	 * 
	 * Nullifies the mask if max and min are null.
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public boolean process(final Number min, final Number max, Collection<IRegion> regions) {

		boolean tmp = true;
		if (max == null && min == null) {
			tmp = false;
		}
		final boolean requireMinMax = tmp;
		
		createMaskIfNeeded();
		
		// Remove invalid regions first to make processing faster.
		final List<IRegion> validRegions = regions!=null?new ArrayList<IRegion>(regions.size()):null;
		if (validRegions!=null) for (IRegion region : regions) {
			if (region == null)             continue;
			if (!isSupportedRegion(region)) continue;
			if (!region.isMaskRegion())     continue;
			validRegions.add(region);
		}
		
		// Slightly wrong AbstractDataset loop, but it is faster...
		final int[] shape = imageDataset.getShape();
		if (validRegions==null && requireMinMax) {
			for (int y = 0; y<shape[0]; ++y) {
				for (int x = 0; x<shape[1]; ++x) {
					final float val = imageDataset.getFloat(y, x);
					boolean isValid = isValid(val,min,max);
					if (!isValid) maskDataset.set(Boolean.FALSE, y, x); // false = masked
				}
			}
		} else if (validRegions!=null){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					for (int y = 0; y<shape[0]; ++y) {
						for (int x = 0; x<shape[1]; ++x) {
							if (requireMinMax) {
								final float val = imageDataset.getFloat(y, x);
								boolean isValid = isValid(val,min,max);
								if (!isValid) maskDataset.set(Boolean.FALSE, y, x); // false = masked
							}
							if (validRegions!=null) for (IRegion region : validRegions) {
								try {
									if (region.containsPoint(x, y)) {
										maskDataset.set(Boolean.FALSE, y, x);
									}
								} catch (Throwable ne) {
									logger.trace("Cannot process point "+(new Point(x,y)), ne);
									continue;
								}
							}
						}
					}

				}
			});
			    
		}
		
		return true;
	}
	
	private void createMaskIfNeeded() {
		if (maskDataset == null || !maskDataset.isCompatibleWith(imageDataset)) {
			maskDataset = new BooleanDataset(imageDataset.getShape());
			maskDataset.setName("mask");
			maskDataset.setExtendible(false);
			maskDataset.fill(true);
		}		
	}

	private static final boolean isValid(float val, Number min, Number max) {
		if (min!=null && val<=min.floatValue()) return false;
		if (max!=null && val>=max.floatValue()) return false;
		return true;
	}

	/**
	 * TODO Add more than just line, free and box - ring would be useful!
	 * @param region
	 * @return
	 */
	public boolean isSupportedRegion(IRegion region) {
		
		if (!region.isVisible())    return false;
		if (!region.isUserRegion()) return false;
		if (region.getROI()==null)  return false;
		
		return true;
	}

	public MaskMode getMaskMode() {
		return maskMode;
	}

	public void setMaskMode(MaskMode paintMode) {
		this.maskMode = paintMode;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int penSize) {
		this.lineWidth = penSize;
	}

	public boolean isSquarePen() {
		return squarePen;
	}

	public void setSquarePen(boolean squarePen) {
		this.squarePen = squarePen;
	}

	public BooleanDataset getMaskDataset() {
		return maskDataset;
	}

	/**
	 * The booleans get filled true when this is set.
	 * @param maskDataset
	 */
	public void setMaskDataset(BooleanDataset maskDataset, boolean requireFill) {
		this.maskDataset = maskDataset;
		if (maskDataset!=null && requireFill) maskDataset.fill(true);
	}

	public AbstractDataset getImageDataset() {
		return imageDataset;
	}

	public void setImageDataset(AbstractDataset imageDataset) {
		this.imageDataset = imageDataset;
	}

	public void reset() {
		this.maskDataset = null;
	}
}
