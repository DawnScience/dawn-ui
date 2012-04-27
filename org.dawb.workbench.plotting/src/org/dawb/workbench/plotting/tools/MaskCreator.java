package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.region.IRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * This class is taken directly out of the class of the same name in SDA
 * 
 * The intention is to make the maths available separate to the side plot system.
 * 
 * @author fcp94556
 *
 */
public class MaskCreator {

	private static final Logger logger = LoggerFactory.getLogger(MaskCreator.class);
	
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
    private int             penSize;
    private boolean         squarePen = false;
    /**
     * The booleans are false to mask and
     * true to leave that way multiply will work.
     */
    private BooleanDataset  maskDataset;
    private AbstractDataset imageDataset;

    	
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
	public boolean processBounds(Number min, Number max) {

		if (max == null && min == null) {
			maskDataset = null;
			return false;
		}
		
		createMaskIfNeeded();
		
		// Slightly wrong AbstractDataset loop, but it is fast...
		final int[] shape = imageDataset.getShape();
		for (int i = 0; i<shape[0]; ++i) {
			for (int j = 0; j<shape[1]; ++j) {
			    final float val = imageDataset.getFloat(i, j);
			    maskDataset.set(isValid(val,min,max), i, j); // false = masked
			}
		}
		
		return true;
	}
	
	/**
	 * Designed to be called after processBounds(...) has been called at least once.
	 * Deals with fact that that may leave us with no mask and will create one if needed.
	 * 
	 * @param region
	 * @return
	 */
	public boolean processRegion(IRegion region) {
		
		if (region == null)             return false;
		if (!isSupportedRegion(region)) return false;
		
		createMaskIfNeeded();
		
        //TODO !!
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
		
		return (region.getROI() instanceof RectangularROI) ||
			   (region.getROI() instanceof LinearROI)      ||
			   (region.getROI() instanceof PolygonalROI);
	}

	private void manipulate(int[] imagepos) {
		MaskMode paintMode = getMaskMode();
		int ps = getPenSize() - 1;
		int hps = ps / 2;
		boolean square = isSquarePen();
		for (int i = -hps; i <= hps; i++) {
			for (int j = -hps; j <= hps; j++) {
				if (square || Math.sqrt(i * i + j * j) <= hps) {
					toggle(paintMode, new int[] { imagepos[1] + i, imagepos[0] + j });
				}
			}
		}
	}
	
	private void toggle(MaskMode paintMode, int[] pixelpos) {
		for (int i : pixelpos) {
			if (i < 0) {
				return;
			}
		}
		try {
			if (paintMode == MaskMode.DRAW) {
				maskDataset.set(false, pixelpos);
				return;
			}
			if (paintMode == MaskMode.ERASE) {
				maskDataset.set(true, pixelpos);
				return;
			}

			maskDataset.set(!maskDataset.get(pixelpos), pixelpos);
		} catch (Exception e) {
			logger.error("Error during mask toggle!", e);
		}
	}

	private void lineBresenham(int[] from, int[] to) {
		int x0 = from[0], x1 = to[0], y0 = from[1], y1 = to[1];
		int dy = y1 - y0;
		int dx = x1 - x0;
		int stepx, stepy;

		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}
		dy <<= 1; // dy is now 2*dy
		dx <<= 1; // dx is now 2*dx

		manipulate(new int[] { x0, y0 });
		if (dx > dy) {
			int fraction = dy - (dx >> 1); // same as 2*dy - dx
			while (x0 != x1) {
				if (fraction >= 0) {
					y0 += stepy;
					fraction -= dx; // same as fraction -= 2*dx
				}
				x0 += stepx;
				fraction += dy; // same as fraction -= 2*dy
				manipulate(new int[] { x0, y0 });
			}
		} else {
			int fraction = dx - (dy >> 1);
			while (y0 != y1) {
				if (fraction >= 0) {
					x0 += stepx;
					fraction -= dy;
				}
				y0 += stepy;
				fraction += dx;
				manipulate(new int[] { x0, y0 });
			}
		}
	}

	public MaskMode getMaskMode() {
		return maskMode;
	}

	public void setMaskMode(MaskMode paintMode) {
		this.maskMode = paintMode;
	}

	public int getPenSize() {
		return penSize;
	}

	public void setPenSize(int penSize) {
		this.penSize = penSize;
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
	public void setMaskDataset(BooleanDataset maskDataset) {
		this.maskDataset = maskDataset;
		maskDataset.fill(true);
	}

	public AbstractDataset getImageDataset() {
		return imageDataset;
	}

	public void setImageDataset(AbstractDataset imageDataset) {
		this.imageDataset = imageDataset;
	}

}
