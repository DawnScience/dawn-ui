package org.dawnsci.plotting.tools.imagecuts;

import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.imagecuts.CutData.CutType;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROISliceUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.YAxisBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for displaying perpendicular cuts through an image for ARPES
 *
 */
public class PerpendicularImageCutsTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(PerpendicularImageCutsTool.class);

	private PerpendicularImageCutsComposite composite;
	private ITraceListener traceListener;
	private IRegionListener regionListener;
	private IROIListener roiListener;
	private CutRegionUpdateListener cutUpdateListener;

	private IRegion[] regions;

	@Override
	public void createControl(Composite parent) {

		IPlottingService service = Activator.getService(IPlottingService.class);
		try {
			composite = new PerpendicularImageCutsComposite(parent, SWT.NONE, service);
		} catch (Exception e) {
			logger.error("Could not create composite!");
			return;
		}

		cutUpdateListener = new CutRegionUpdateListener() {

			@Override
			public void updateRequested(double value, double delta, CutType type) {

				IImageTrace imageTrace = getImageTrace();

				doUpdate(imageTrace, regions, value, delta, type == CutType.X);
			}
		};
	}

	private void doUpdate(IImageTrace imageTrace, IRegion[] regions, double value, double delta, boolean isX) {

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

	@Override
	public void activate() {
		super.activate();
		IPlottingSystem<Object> plottingSystem = getPlottingSystem();
		if (traceListener == null) {

			traceListener = new ITraceListener.Stub() {

				@Override
				public void traceWillPlot(TraceWillPlotEvent evt) {

					IImageTrace t = evt.getImageTrace();

					if (regions == null) {
						generateInitialRegions(t);
					}

					runUpdate(t, null, null);

				}
			};

			roiListener = new IROIListener.Stub() {
				public void update(ROIEvent evt) {
					IImageTrace t = getImageTrace();
					runUpdate(t, evt.getROI(), (IRegion) evt.getSource());
				}
			};

			regionListener = new IRegionListener.Stub() {

				@Override
				public void regionsRemoved(RegionEvent evt) {
					evt.getRegions().forEach(r -> r.removeROIListener(roiListener));
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
			generateInitialRegions(imageTrace);
			runUpdate(imageTrace, null, null);
		}

	}

	private IRegion generateInitialRegions(IImageTrace imageTrace) {

		int[] shape = imageTrace.getData().getShape();

		IPlottingSystem<Object> plottingSystem = getPlottingSystem();

		try {
			IRegion xr = plottingSystem.createRegion("xPerpendicularCut", RegionType.XAXIS);

			int xDelta = shape[1] / 10;

			XAxisBoxROI xroi = new XAxisBoxROI(shape[1] / 2 - (xDelta / 2), 0, xDelta, 1, 0);
			xr.setROI(xroi);
			xr.setUserRegion(false);

			int yDelta = shape[0] / 10;

			IRegion yr = plottingSystem.createRegion("yPerpendicularCut", RegionType.YAXIS);
			YAxisBoxROI yroi = new YAxisBoxROI(0, shape[0] / 2 - (yDelta / 2), 1, yDelta, 0);
			yr.setROI(yroi);
			yr.setUserRegion(false);

			regions = new IRegion[] { xr, yr };

			plottingSystem.addRegion(xr);
			plottingSystem.addRegion(yr);

		} catch (Exception e) {
			logger.error("Error generating initial regions", e);
		}

		return null;
	}

	private void runUpdate(IImageTrace t, IROI roi, IRegion region) {
		if (t == null) {
			return;
		}
		Collection<IRegion> regionsx = getPlottingSystem().getRegions(RegionType.XAXIS);
		Collection<IRegion> regionsy = getPlottingSystem().getRegions(RegionType.YAXIS);

		if (regionsx.isEmpty() || regionsy.isEmpty())
			return;

		List<IDataset> axes = t.getAxes();

		composite.update(t.getData(), axes.get(0), axes.get(1), getROI(regionsx, region, roi),
				getROI(regionsy, region, roi));
	}

	private RectangularROI getROI(Collection<IRegion> regions, IRegion r, IROI roi) {

		IRegion region = regions.iterator().next();

		if (region == r) {
			return (RectangularROI) roi;
		}

		return (RectangularROI) region.getROI();
	}

	@Override
	public void deactivate() {
		if (!isActive()) {
			return;
		}
		super.deactivate();
		IPlottingSystem<Object> ps = getPlottingSystem();
		ps.removeRegionListener(regionListener);
		ps.removeTraceListener(traceListener);

		ps.removeRegion(regions[0]);
		ps.removeRegion(regions[1]);
		regions = null;
		composite.removeListener(cutUpdateListener);
	}

	@Override
	public void dispose() {
		composite.dispose();
		super.dispose();
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		composite.setFocus();
	}
}
