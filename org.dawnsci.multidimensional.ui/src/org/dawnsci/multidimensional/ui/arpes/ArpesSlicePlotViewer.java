package org.dawnsci.multidimensional.ui.arpes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.multidimensional.ui.Activator;
import org.dawnsci.multidimensional.ui.hyper.AbstractHyperPlotViewer;
import org.dawnsci.multidimensional.ui.hyper.ArpesSideImageReducer;
import org.dawnsci.multidimensional.ui.hyper.ArpesXImageReducer;
import org.dawnsci.multidimensional.ui.hyper.BaseHyperTrace;
import org.dawnsci.multidimensional.ui.hyper.IDatasetROIReducer;
import org.dawnsci.multidimensional.ui.imagecuts.AdditionalCutDimension;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularCutsHelper;
import org.dawnsci.multidimensional.ui.imagecuts.PerpendicularImageCutsComposite;
import org.dawnsci.plotting.system.LightWeightPlotViewer;
import org.eclipse.dawnsci.plotting.api.IPlotActionSystem;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.region.ILockTranslatable;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class ArpesSlicePlotViewer extends AbstractHyperPlotViewer {
	private static final String MAIN_TITLE = "Constant Energy Map";
	private static final String SIDE_TITLE = "Detector Image";

	private PerpendicularImageCutsComposite cutComposite;
	private PerpendicularCutsHelper helper;
	private IRegion[] cachedSideRegions;
	public ArpesSlicePlotViewer() {

	}

	@Override
	protected void innerCreateControl(Composite parent, Layout layout) {
		control = new SashForm(parent, SWT.None);
		control.setLayout(layout);
	}

	@Override
	public void createControl(final Composite parent) {
		innerCreateControl(parent, GridLayoutFactory.fillDefaults().numColumns(2).create());
		createHyperComponent();
		hyper.setInvertYAxis(false);
		try {
			cutComposite = new PerpendicularImageCutsComposite(control, SWT.None,
					Activator.getService(IPlottingService.class), true);
			cutComposite.setLayoutData(GridDataFactory.fillDefaults().grab(false, true).create());
			this.helper = new PerpendicularCutsHelper(hyper.getSideSystem());
			if (control instanceof SashForm) {
				((SashForm) control).setWeights(new int[] { 2, 1 });
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		clearActions(hyper.getMainSystem());
		clearActions(hyper.getSideSystem());
	}

	private void clearActions(IPlottingSystem<?> system) {
		IPlotActionSystem plotActionSystem = system.getPlotActionSystem();
		plotActionSystem.remove(BasePlottingConstants.REMOVE_REGION);
		plotActionSystem.remove(BasePlottingConstants.SNAP_TO_GRID);
		plotActionSystem.remove(system.getPlotName() + "/org.dawnsci.plotting.system.preference.undoUndo");
		plotActionSystem.remove(system.getPlotName() + "/org.dawnsci.plotting.system.preference.undoRedo");
		plotActionSystem.remove("org.dawb.workbench.fullscreen");
		plotActionSystem.remove("org.dawb.workbench.plotting.rescale");

	}

	@Override
	public boolean addTrace(ITrace trace) {

		if (trace instanceof BaseHyperTrace) {

			helper.setCachedRegions(cachedSideRegions);
			helper.activate(this.cutComposite);
			hyper.clear();
			setTitles();

			BaseHyperTrace h = (BaseHyperTrace) trace;
			this.trace = h;

			plotHyper3D(h);

			return true;
		}
		return false;
	}

	private void plotHyper3D(BaseHyperTrace trace) {
		HyperDataPackage dp = buildDataPackage(trace);

		ArpesSideImageReducer arpesSideImageReducer = new ArpesSideImageReducer();

		IDatasetROIReducer[] reducers = new IDatasetROIReducer[] { new ArpesXImageReducer(), arpesSideImageReducer };

		final IDataset ax0 = dp.axes.get(0);

		IRegionListener l = new IRegionListener.Stub() {

			@Override
			public void regionAdded(RegionEvent evt) {
				evt.toString();
				Object source = evt.getSource();
				if (source instanceof IRegion) {

					if (source instanceof ILockTranslatable) {
						((ILockTranslatable) source).translateOnly(true);
					}

					AdditionalCutDimension d = new AdditionalCutDimension((IRegion) source, ax0);
					helper.setAdditionalCutDimension(d);
				}

			}
		};
		hyper.setExternalListeners(null, null, l, null);
		hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order, reducers[0], reducers[1]);
		trace.setViewer(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends ITrace> U createTrace(String name, Class<U> clazz) {

		if (clazz == IArpesSliceTrace.class) {
			ArpesSliceTrace hyperTrace = new ArpesSliceTrace();
			hyperTrace.setName(name);
			return (U) hyperTrace;
		}
		return null;
	}

	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		if (IArpesSliceTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}

	@Override
	public void removeTrace(ITrace trace) {
		Collection<IRegion> cachedSideRegionsCollection = hyper.getSideSystem().getRegions();
		if (cachedSideRegionsCollection != null) {
			cachedSideRegions = cachedSideRegionsCollection.toArray(IRegion[]::new);
		}
		super.removeTrace(trace);
	}

	@Override
	public Image getImage(Rectangle size) {
		IImageTrace tr = (IImageTrace) hyper.getMainSystem().getTraces().stream().findFirst().orElse(null);
		if ((tr!=null) && (tr.getImage() instanceof Image result)) {
			return result;
		}
		return null;
	}

	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(IArpesSliceTrace.class);
		return l;
	}

	public void setMainSystemUseAspectFromLabel(boolean useAspectFromLabel) {
		if (hyper.getMainSystem().getActiveViewer() instanceof LightWeightPlotViewer<?> lwp) {
			lwp.setUseAspectFromLabel(useAspectFromLabel);
		}
	}

	public void setSideSystemUseAspectFromLabel(boolean useAspectFromLabel) {
		if (hyper.getSideSystem().getActiveViewer() instanceof LightWeightPlotViewer<?> lwp) {
			lwp.setUseAspectFromLabel(useAspectFromLabel);
		}
	}

	private void setTitles() {
		hyper.getMainSystem().setTitle(MAIN_TITLE);
		hyper.getSideSystem().setTitle(SIDE_TITLE);
	}
}
