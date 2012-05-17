package org.dawb.workbench.plotting.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;

public class EllipseFittingTool extends AbstractToolPage {
	private final static Logger logger = LoggerFactory.getLogger(EllipseFittingTool.class);

	private Composite composite;
	private IRegionListener ellipseRegionListener;
	private ITraceListener traceListener;
	private IROIListener ellipseROIListener;

	private Text text;

	public EllipseFittingTool() {
		ellipseRegionListener = new IRegionListener.Stub() {

			@Override
			public void regionsRemoved(RegionEvent evt) {
				updateEllipses();
			}

			@Override
			public void regionRemoved(RegionEvent evt) {
				removeEllipse(evt.getRegion());
			}

			@Override
			public void regionAdded(RegionEvent evt) {
				updateEllipse(evt.getRegion());
//				if (evt.getRegion() != null && evt.getRegion().getRegionType() == RegionType.ELLIPSE) {
//					// EllipticalROI eroi = (EllipticalROI)
//					// evt.getRegion().getROI().copy();
//					// evt.getRegion().setROI(eroi);
//				}
			}
		};

		ellipseROIListener = new IROIListener.Stub() {
			@Override
			public void roiChanged(ROIEvent evt) {
				updateEllipses();
			}
		};

		try {
			setPlottingSystem(PlottingFactory.createPlottingSystem());
			traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {

					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}

				}
			};

		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}

	protected void updateEllipse(IRegion region) {
		if (region == null)
			return;

		region.addROIListener(ellipseROIListener);
		EllipticalROI eroi = (EllipticalROI) region.getROI();
		text.setText(eroi.toString());
	}

	protected void removeEllipse(IRegion region) {
		if (region == null)
			return;

		region.removeROIListener(ellipseROIListener);
		text.setText("Ellipse geometry: --");
	}

	protected void updateEllipses() {
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter == null) return;

		Collection<IRegion> regions = plotter.getRegions(RegionType.ELLIPSE);
		if (regions != null && regions.size() > 0) {
			IRegion r = null;
			Iterator<IRegion> it = regions.iterator();
			while (it.hasNext()) {
				r = it.next();
				r.addROIListener(ellipseROIListener);
			}
			EllipticalROI eroi = (EllipticalROI) r.getROI();
			text.setText("Ellipse geometry: " + eroi.toString());
//			for (IRegion r : regions) {
//				
//			}
		} else {
			text.setText("Ellipse geometry: --");
		}
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		text = new Text(composite, SWT.NONE);
		text.setText("Ellipse geometry: --");
	}

	@Override
	public void activate() {
		super.activate();
		createRegion();
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter == null) return;

		if (traceListener != null)
			plotter.addTraceListener(traceListener);
		if (ellipseRegionListener != null)
			plotter.addRegionListener(ellipseRegionListener);

		// Start with a selection of the right type
		try {
			plotter.createRegion(RegionUtils.getUniqueName("Ellipse", plotter), RegionType.ELLIPSE);
		} catch (Exception e) {
			logger.error("Cannot create region for ellipse fitting tool!");
		}
	}

	@Override
	public void deactivate() {
		IPlottingSystem plotter = getPlottingSystem();
		if (plotter != null) {
			plotter.removeTraceListener(traceListener);
			plotter.removeRegionListener(ellipseRegionListener);
		}

		super.deactivate();
	}

	private void createRegion() {
		
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
	}

}
