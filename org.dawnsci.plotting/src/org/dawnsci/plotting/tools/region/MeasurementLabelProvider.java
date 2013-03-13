package org.dawnsci.plotting.tools.region;

import java.text.DecimalFormat;

import org.dawb.common.util.number.DoubleUtils;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawnsci.plotting.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class MeasurementLabelProvider extends ColumnLabelProvider {
	
	public enum LabelType {
		ROINAME, STARTX, STARTY, ENDX, ENDY, MAX, SUM, ROITYPE, DX, DY, LENGTH, INNERRAD, OUTERRAD, ROISTRING, ACTIVE
	}

	private static final Logger logger = LoggerFactory.getLogger(MeasurementLabelProvider.class);
	
	private LabelType column;
	private AbstractRegionTableTool tool;
	private Image checkedIcon;
	private Image uncheckedIcon;
	private DecimalFormat format;

	public MeasurementLabelProvider(AbstractRegionTableTool tool, LabelType i) {
		this.column = i;
		this.tool   = tool;
		ImageDescriptor id = Activator.getImageDescriptor("icons/ticked.png");
		checkedIcon   = id.createImage();
		id = Activator.getImageDescriptor("icons/unticked.gif");
		uncheckedIcon =  id.createImage();
		this.format = new DecimalFormat("##0.00E0");
	}

	private static final String NA = "-";

	@Override
	public Image getImage(Object element){
		
		if (!(element instanceof IRegion)) return null;
		if (column==LabelType.ACTIVE){
			final IRegion region = (IRegion)element;
			return region.getROI().isPlot() && tool.getControl().isEnabled() ? checkedIcon : uncheckedIcon;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		
		if (!(element instanceof IRegion)) return null;
		final IRegion    region = (IRegion)element;

		ROIBase roi = tool.getROI(region);

		try {
			Object fobj = null;
			if (element instanceof String) return "";
			switch(column) {
			case ROINAME:
				return region.getLabel();
			case STARTX:
				if(roi instanceof LinearROI)
					fobj = ((LinearROI)roi).getPoint()[0];
				else if (roi instanceof RectangularROI)
					fobj = ((RectangularROI)roi).getPoint()[0];
				return fobj == null ? NA : DoubleUtils.formatDouble((Double)fobj, 0);
			case STARTY: // dx
				if(roi instanceof LinearROI)
					fobj = ((LinearROI)roi).getPoint()[1];
				else if (roi instanceof RectangularROI)
					fobj = ((RectangularROI)roi).getPoint()[1];
				else
					;
				return fobj == null ? NA : DoubleUtils.formatDouble((Double)fobj, 0);
			case ENDX: // dy
				if(roi instanceof LinearROI)
					fobj = ((LinearROI)roi).getEndPoint()[0];
				else if(roi instanceof RectangularROI)
					fobj = ((RectangularROI)roi).getEndPoint()[0];
				else
					;
				return fobj == null ? NA : DoubleUtils.formatDouble((Double)fobj, 0);
			case ENDY: // length
				if(roi instanceof LinearROI)
					fobj = ((LinearROI)roi).getEndPoint()[1];
				else if(roi instanceof RectangularROI)
					fobj = ((RectangularROI)roi).getEndPoint()[1];
				else
					;
				return fobj == null ? NA : DoubleUtils.formatDouble((Double)fobj, 0);
			case MAX: // max
				final double max = tool.getMaxIntensity(region);
			    if (Double.isNaN(max)) return NA;
				return DoubleUtils.formatDouble(max, 5);
			case SUM: // sum
				final double sum = tool.getSum(region);
				if(Double.isNaN(sum)) return NA;
				return DoubleUtils.formatDouble(sum, 5);
			case ROITYPE: //ROI type
				return region.getRegionType().getName();
			case DX: // dx
				if (roi instanceof LinearROI) {
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getEndPoint()[0] - lroi.getPointX();
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					fobj = rroi.getEndPoint()[0] - rroi.getPointX();
				} else {
					
				}
				return fobj == null ? NA : format.format(fobj);
			case DY: // dy
				if (roi instanceof LinearROI) {
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getEndPoint()[1] - lroi.getPointY();
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					fobj = rroi.getEndPoint()[1] - rroi.getPointY();
				} else {
					
				}
				return fobj == null ? NA : format.format(fobj);
			case LENGTH: // length
				if (roi instanceof LinearROI) {
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getLength();
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					double[] lens = rroi.getLengths();
					fobj = Math.hypot(lens[0], lens[1]);
				} else {
					
				}
				return fobj == null ? NA : format.format(fobj);
			case INNERRAD: // in rad
				if (roi instanceof SectorROI) {
					SectorROI sroi = (SectorROI) roi;
					fobj = sroi.getRadius(0);
				}
				return fobj == null ? NA : format.format(fobj);
			case OUTERRAD: // out rad
				if (roi instanceof SectorROI) {
					SectorROI sroi = (SectorROI) roi;
					fobj = sroi.getRadius(1);
				}
				return fobj == null ? NA : format.format(fobj);
			case ROISTRING: // region
				return tool.getROI(region).toString();
			default:
				return "";
			}
			
			
		} catch (Throwable ne) {
			// One must not throw RuntimeExceptions like null pointers from this
			// methd becuase the user gets an eclipse dialog confusing them with 
			// the error
			logger.error("Cannot get value in info table", ne);
			return "";
		}
	}

	public String getToolTipText(Object element) {
		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
	}

	@Override
	public void dispose(){
		super.dispose();
		checkedIcon.dispose();
		uncheckedIcon.dispose();
	}
}
