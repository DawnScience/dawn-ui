package org.dawnsci.plotting.tools.processing;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * 
 * @author wqk87977
 *
 */
public class ExampleProcessTool extends ImageProcessingTool {

	private static Logger logger = LoggerFactory.getLogger(ExampleProcessTool.class);

	private IAxis xPixelAxis;
	private IAxis yPixelAxis;
	private IAxis xDisplayPixelAxis;
	private IAxis yDIsplayPixelAxis;

	public ExampleProcessTool(){
		createRadioActions();
	}

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
		if (xPixelAxis==null) {
			this.xPixelAxis = plotter.getSelectedXAxis();
			xPixelAxis.setTitle("X Pixel");
		}
		
		if (yPixelAxis==null) {
			this.yPixelAxis = plotter.createAxis("Y Pixel", false, SWT.TOP);
			plotter.getSelectedYAxis().setTitle("Intensity");
		}
	}

	@Override
	protected void configureDisplayPlottingSystem(AbstractPlottingSystem plotter) {
		if (xDisplayPixelAxis==null) {
			this.xDisplayPixelAxis = plotter.getSelectedXAxis();
			xDisplayPixelAxis.setTitle("X Pixel");
		}
		
		if (yDIsplayPixelAxis==null) {
			this.yDIsplayPixelAxis = plotter.createAxis("Y Pixel", false, SWT.TOP);		
			plotter.getSelectedYAxis().setTitle("Intensity");
		}
	}

	private void createRadioActions(){
		List<Entry<String, Action>> radioActions = new ArrayList<Entry<String, Action>>();
		
		Entry<String, Action> action1 = new AbstractMap.SimpleEntry<String, Action>("Test 1 ",
			new Action("test1Action") {
				@Override
				public void run() {
					System.out.println("action1");
				}
			}
		);
		Entry<String, Action> action2 = new AbstractMap.SimpleEntry<String, Action>("Test 2",
				new Action("test2Action") {
					@Override
					public void run() {
						System.out.println("action2");
					}
				}
			);
		Entry<String, Action> action3 = new AbstractMap.SimpleEntry<String, Action>("Test 3",
				new Action("test3Action") {
					@Override
					public void run() {
						System.out.println("action3");
					}
				}
			);
		Entry<String, Action> action4 = new AbstractMap.SimpleEntry<String, Action>("Test 4",
				new Action("test4Action") {
					@Override
					public void run() {
						System.out.println("action4");
					}
				}
			);
		radioActions.add(action1);
		radioActions.add(action2);
		radioActions.add(action3);
		radioActions.add(action4);
		setRadioActions(radioActions);

		setComboActions(radioActions);
	}

	@Override
	public void createProfile(final IImageTrace  image, 
			                     IRegion      region,
			                     IROI      rbs, 
			                     boolean      tryUpdate, 
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
		
		// This is an example of profile (here zoom profile)
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if ((region.getRegionType()!=RegionType.BOX)&&(region.getRegionType()!=RegionType.PERIMETERBOX)) return;

		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;

		final int yInc = bounds.getPoint()[1]<bounds.getEndPoint()[1] ? 1 : -1;
		final int xInc = bounds.getPoint()[0]<bounds.getEndPoint()[0] ? 1 : -1;
		
		try {
			AbstractDataset slice = null;
			AbstractDataset im    = (AbstractDataset)image.getData();
			// If the region is out of the image bounds (left and top) we set the start points to 0
			if((int) bounds.getPoint()[0]<0 && (int) bounds.getPoint()[1]>=0)
				slice = im.getSlice(new int[] { (int) bounds.getPoint()[1], 0 },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else if ((int) bounds.getPoint()[1]<0 && (int) bounds.getPoint()[0]>=0)
				slice = im.getSlice(new int[] { 0, (int) bounds.getPoint()[0] },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else if((int) bounds.getPoint()[0]<0 && (int) bounds.getPoint()[1]<0)
				slice = im.getSlice(new int[] { 0, 0 },
						new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
						new int[] {yInc, xInc});
			else slice = im.getSlice(new int[] { (int) bounds.getPoint()[1], (int) bounds.getPoint()[0] },
					new int[] { (int) bounds.getEndPoint()[1], (int) bounds.getEndPoint()[0] },
					new int[] {yInc, xInc});
			
	
			slice.setName(region.getName());
			
			// Calculate axes to have real values not size
			AbstractDataset yLabels = null;
			AbstractDataset xLabels = null;
			if (image.getAxes()!=null && image.getAxes().size() > 0) {
				AbstractDataset xl = (AbstractDataset)image.getAxes().get(0);
				if (xl!=null) xLabels = getLabelsFromLabels(xl, bounds, 0);
				AbstractDataset yl = (AbstractDataset)image.getAxes().get(1);
				if (yl!=null) yLabels = getLabelsFromLabels(yl, bounds, 1);
			}
			
			if (yLabels==null) yLabels = IntegerDataset.arange(bounds.getPoint()[1], bounds.getEndPoint()[1], yInc);
			if (xLabels==null) xLabels = IntegerDataset.arange(bounds.getPoint()[0], bounds.getEndPoint()[0], xInc);
			
			final IImageTrace zoom_trace = (IImageTrace)profilePlottingSystem.updatePlot2D(slice, Arrays.asList(new IDataset[]{xLabels, yLabels}), monitor);
			registerTraces(region, Arrays.asList(new ITrace[]{zoom_trace}));
			Display.getDefault().syncExec(new Runnable()  {
				public void run() {
				     zoom_trace.setPaletteData(image.getPaletteData());
				}
			});
			
		} catch (IllegalArgumentException ne) {
			// Occurs when slice outside
			logger.trace("Slice outside bounds of image!", ne);
		} catch (Throwable ne) {
			logger.warn("Problem slicing image in "+getClass().getSimpleName(), ne);
		}

	}

	private AbstractDataset getLabelsFromLabels(AbstractDataset xl, RectangularROI bounds, int axisIndex) {
		try {
			int fromIndex = (int)bounds.getPoint()[axisIndex];
			int toIndex   = (int)bounds.getEndPoint()[axisIndex];
			int step      = toIndex>fromIndex ? 1 : -1;
			final AbstractDataset slice = xl.getSlice(new int[]{fromIndex}, new int[]{toIndex}, new int[]{step});
			return slice;
		} catch (Exception ne) {
			return null;
		}
	}

	@Override
	public void createDisplayProfile(IImageTrace image, IRegion region,
			IROI roi, boolean tryUpdate, boolean isDrag,
			IProgressMonitor monitor) {
		// This is an example of profile (here Box profile)
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final RectangularROI bounds = (RectangularROI) (roi==null ? region.getROI() : roi);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		
		AbstractDataset[] box = ROIProfile.box((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, true);
        if (box==null) return;
		//if (monitor.isCanceled()) return;
				
		final AbstractDataset x_intensity = box[0];
		x_intensity.setName("X "+region.getName());
		AbstractDataset xi = IntegerDataset.arange(x_intensity.getSize());
		final AbstractDataset x_indices = xi; // Maths.add(xi, bounds.getX()); // Real position
		x_indices.setName("X Pixel");
		
		final AbstractDataset y_intensity = box[1];
		y_intensity.setName("Y "+region.getName());
		AbstractDataset yi = IntegerDataset.arange(y_intensity.getSize());
		final AbstractDataset y_indices = yi; // Maths.add(yi, bounds.getY()); // Real position
		y_indices.setName("Y Pixel");

		//if (monitor.isCanceled()) return;
		final ILineTrace x_trace = (ILineTrace)displayPlottingSystem.getTrace("X "+region.getName());
		final ILineTrace y_trace = (ILineTrace)displayPlottingSystem.getTrace("Y "+region.getName());
		
		if (tryUpdate && x_trace!=null && y_trace!=null) {
			
			getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					displayPlottingSystem.setSelectedXAxis(xDisplayPixelAxis);
					x_trace.setData(x_indices, x_intensity);
					displayPlottingSystem.setSelectedXAxis(yDIsplayPixelAxis);
					y_trace.setData(y_indices, y_intensity);
				}
			});

			
		} else {
						
			displayPlottingSystem.setSelectedXAxis(xDisplayPixelAxis);
			Collection<ITrace> plotted = displayPlottingSystem.updatePlot1D(x_indices, Arrays.asList(new IDataset[]{x_intensity}), monitor);
			registerTraces(region, plotted);
			
			displayPlottingSystem.setSelectedXAxis(yDIsplayPixelAxis);
			plotted = displayPlottingSystem.updatePlot1D(y_indices, Arrays.asList(new IDataset[]{y_intensity}), monitor);
			registerTraces(region, plotted);
			
		}
	}
}
