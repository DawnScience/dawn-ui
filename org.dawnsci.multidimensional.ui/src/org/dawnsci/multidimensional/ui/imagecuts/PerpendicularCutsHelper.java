package org.dawnsci.multidimensional.ui.imagecuts;

import java.util.Collection;
import java.util.List;

import org.dawnsci.multidimensional.ui.imagecuts.CutData.CutType;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockTranslatable;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerpendicularCutsHelper {

	private static final Logger logger = LoggerFactory.getLogger(PerpendicularCutsHelper.class);
	
	private static final String X_REGION_NAME = "xPerpendicularCut";
	private static final String Y_REGION_NAME = "yPerpendicularCut";
	
	
	private IPlottingSystem<Composite> plottingSystem;
	private ITraceListener traceListener;
	private IRegionListener regionListener;
	private IROIListener roiListener;
	private CutRegionUpdateListener cutUpdateListener;
	
	private IRegion[] regions;
	private boolean inUpdate = false;
	
	public PerpendicularCutsHelper(IPlottingSystem<Composite> system) {
		this.plottingSystem = system;
		
		cutUpdateListener = new CutRegionUpdateListener() {

			@Override
			public void updateRequested(double value, double delta, CutType type) {

				IImageTrace imageTrace = getImageTrace();

				doUpdate(imageTrace, regions, value, delta, type == CutType.X);
			}
		};
	}
	
	private IImageTrace getImageTrace() {
		
		Collection<ITrace> traces = plottingSystem.getTraces(IImageTrace.class);
		
		if (traces.isEmpty()) {
			return null;
		}
		
		return (IImageTrace)traces.iterator().next();
	}
	
	
	public IRegion[] generateInitialRegions(IImageTrace imageTrace) {

		if (imageTrace == null) return null;
		
		int[] shape = imageTrace.getData().getShape();
		
		if (imageTrace.getImageServiceBean().isTransposed()) {
			
		}

		try {
			IRegion xr = plottingSystem.createRegion(X_REGION_NAME, RegionType.XAXIS);

			int xDelta = shape[1] / 10;

			XAxisBoxROI xroi = new XAxisBoxROI(shape[1] / 2 - (xDelta / 2), 0, xDelta, 1, 0);
			xr.setROI(xroi);
			xr.setUserRegion(false);

			int yDelta = shape[0] / 10;

			IRegion yr = plottingSystem.createRegion(Y_REGION_NAME, RegionType.YAXIS);
			YAxisBoxROI yroi = new YAxisBoxROI(0, shape[0] / 2 - (yDelta / 2), 1, yDelta, 0);
			yr.setROI(yroi);
			yr.setUserRegion(false);

			IRegion[] regions = new IRegion[] { xr, yr };

			plottingSystem.addRegion(xr);
			plottingSystem.addRegion(yr);
			
			if (xr instanceof ILockTranslatable) {
				((ILockTranslatable) xr).translateOnly(true);
			}
			
			if (yr instanceof ILockTranslatable) {
				((ILockTranslatable) yr).translateOnly(true);
			}

			return regions;
		} catch (Exception e) {
 			logger.error("Error generating initial regions", e);
 			plottingSystem.clearRegions();
		}

		return null;
	}
	
	public void runUpdate(IImageTrace t, IROI roi, IRegion region, PerpendicularImageCutsComposite composite) {
		if (inUpdate || t == null || regions == null) {
			return;
		}

		try {
			inUpdate = true;

			int[] shape = t.getData().getShape();

			List<IDataset> axes = t.getAxes();

			IDataset x =null;
			IDataset y = null;

			if (axes != null) {
				x = axes.get(0);
				y = axes.get(1);
			}

			RectangularROI roix = (RectangularROI)regions[0].getROI();
			RectangularROI roiy = (RectangularROI)regions[1].getROI();

			if (region != null) {
				if (region == regions[0]) {
					roix = (RectangularROI)roi;
				} else {
					roiy = (RectangularROI)roi;
				}
			}

			if (!validateROI(shape, roix, 0)) {
				regions[0].setROI(roix);	
			}

			if (!validateROI(shape, roiy, 1)) {
				regions[1].setROI(roiy);	
			}

			composite.update(t.getData(), x, y, roix,
					roiy);
		} finally {
			inUpdate = false;
		}
	}
	
	public void doUpdate(IImageTrace imageTrace, IRegion[] regions, double value, double delta, boolean isX) {

		int i = isX ? 0 : 1;

		IDataset axis = imageTrace.getAxes().get(i);
		IRegion r = regions[i];
		IROI roi = r.getROI();

		double width = delta;
		double start = value - delta / 2;

		if (axis != null) {
			double step = Math.abs(axis.getDouble(axis.getSize() - 1) - axis.getDouble(0)) / (axis.getSize() - 1);

			start = ROISliceUtils.findPositionOfClosestValueInAxis(axis, value - delta / 2);
			width = delta / step;
		}

		RectangularROI rr = (RectangularROI) roi;

		double[] p = new double[] { 0, 0 };
		double[] w = new double[] { 0, 0 };

		p[i] = start;
		w[i] = width;
		rr.setPoint(p);
		rr.setLengths(w);
		r.setROI(rr);
	}
	
	public void activate(PerpendicularImageCutsComposite composite) {
		if (traceListener == null) {

			traceListener = new ITraceListener.Stub() {

				@Override
				public void traceWillPlot(TraceWillPlotEvent evt) {

					IImageTrace t = evt.getImageTrace();

					if (regions == null) {
						regions = generateInitialRegions(t);
					}

					runUpdate(t, null, null, composite);

				}
			};

			roiListener = new IROIListener.Stub() {
				public void update(ROIEvent evt) {
					IImageTrace t = getImageTrace();
					runUpdate(t, evt.getROI(), (IRegion) evt.getSource(), composite);
				}
			};

			regionListener = new IRegionListener.Stub() {

				@Override
				public void regionsRemoved(RegionEvent evt) {
					evt.getRegions().forEach(r -> r.removeROIListener(roiListener));
					
					if (regions == null) return;
					
					if (evt.getRegions().contains(regions[0]) || evt.getRegions().contains(regions[1])) {
						regions = null;
					}

				}

				@Override
				public void regionRemoved(RegionEvent evt) {
					evt.getRegion().removeROIListener(roiListener);
				}

				@Override
				public void regionCreated(RegionEvent evt) {
					evt.getRegion().addROIListener(roiListener);

				}
			};
		}

		plottingSystem.addTraceListener(traceListener);
		plottingSystem.addRegionListener(regionListener);
		composite.addListener(cutUpdateListener);

		if (regions == null) {
			IImageTrace imageTrace = getImageTrace();
			regions = generateInitialRegions(imageTrace);
			runUpdate(imageTrace, null, null, composite);
		}
	}
	
	private boolean validateROI(int[] shape, IROI roi, int dim) {
		
		boolean valid = true;
		
		if (roi instanceof RectangularROI) {
			int other = dim == 0 ? 1 : 0;
			double point = roi.getPoint()[dim];
			double length = ((RectangularROI) roi).getLength(dim);
			if (point > shape[other] || point < 0) {
				point = shape[other]/2.0;
				double[] p = new double[2];
				p[dim] = point;
				roi.setPoint(p);
				valid = false;
			}
			
			if ((point + length) > shape[other]) {
				double[] p = new double[2];
				p[dim] = shape[other] - point;
				((RectangularROI) roi).setLengths(p);
				valid = false;
			}
		}
		
		return valid;
	}
	
	public void deactivate(PerpendicularImageCutsComposite composite) {
		plottingSystem.removeRegionListener(regionListener);
		plottingSystem.removeTraceListener(traceListener);

		if (regions != null) {
			
			regions[0].removeROIListener(roiListener);
			regions[1].removeROIListener(roiListener);
			
			plottingSystem.removeRegion(regions[0]);
			plottingSystem.removeRegion(regions[1]);
			regions = null;
		}
		
		composite.removeListener(cutUpdateListener);
	}
	
}
