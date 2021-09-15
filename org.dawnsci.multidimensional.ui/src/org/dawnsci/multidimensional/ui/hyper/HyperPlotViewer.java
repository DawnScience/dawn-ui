package org.dawnsci.multidimensional.ui.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class HyperPlotViewer extends AbstractHyperPlotViewer {
	
	private enum Hyper3Dmode {
		AREA_TO_LINE("Average Regions") ,IMAGE_LINE_TO_IMAGE("Line Profile - as image"),LINE_TO_LINE("Line Profile - as lines");
		
		private String description;
		
		private Hyper3Dmode(String description) {
			this.description = description;
		}
		
		@Override
		public String toString() {
			return description;
		}
		
	}

	private ComboViewer viewer;
	private Hyper3Dmode currentMode = Hyper3Dmode.AREA_TO_LINE;
	
	@Override
	public void createControl(final Composite parent) {
		innerCreateControl(parent, GridLayoutFactory.fillDefaults().create());
		createHyperComponent();
		viewer = new ComboViewer(control);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		viewer.getControl().setLayoutData(gridData);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(new Object[] {Hyper3Dmode.AREA_TO_LINE,Hyper3Dmode.IMAGE_LINE_TO_IMAGE,Hyper3Dmode.LINE_TO_LINE});
		viewer.getCombo().select(0);
		currentMode = Hyper3Dmode.AREA_TO_LINE;
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Hyper3Dmode mode = (Hyper3Dmode)((IStructuredSelection)event.getSelection()).getFirstElement();
				if (mode == currentMode) return;
				currentMode = mode;
				if (trace == null || !(trace instanceof HyperTrace)) return;

				plotHyper3D(trace);

			}
		});

		showViewer(false);
		
	}
	
	private IDatasetROIReducer[] getReducers(Hyper3Dmode mode) {
		
		switch (mode) {
		case AREA_TO_LINE:
			return new IDatasetROIReducer[] {new TraceReducer(), new ImageTrapeziumBaselineReducer()};
		case IMAGE_LINE_TO_IMAGE:
			return new IDatasetROIReducer[] {new ArpesMainImageReducer(), new ArpesSideImageReducer()};
		case LINE_TO_LINE:
			return new IDatasetROIReducer[] {new TraceLineReducer(), new ImageTrapeziumBaselineReducer()};
		}
		
		return null;
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		
		if (trace instanceof Hyper4DTrace) {
			Hyper4DTrace h = (Hyper4DTrace)trace;
			this.trace = h;
			
			HyperDataPackage dp = buildDataPackage(h);
			hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order, new Hyper4DMapReducer(), new Hyper4DImageReducer());
			((Hyper4DTrace) trace).setViewer(this);
			showViewer(false);
			return true;
		}
		
		if (trace instanceof HyperTrace) {
			HyperTrace h = (HyperTrace)trace;
			this.trace = h;
			
			plotHyper3D(h);
			
			return true;
		}
		return false;
	}
	
	private void plotHyper3D(BaseHyperTrace trace) {
		HyperDataPackage dp = buildDataPackage(trace);
		IDatasetROIReducer[] reducers = getReducers(currentMode);
		if (reducers == null) {
			return;
		}
		hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order,reducers[0],reducers[1]);
		trace.setViewer(this);
		showViewer(true);
	}
	
	private void showViewer(boolean show) {
		viewer.getCombo().setEnabled(show);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public  <U extends ITrace> U createTrace(String name, Class<? extends ITrace> clazz) {
		
		if (clazz == IHyper4DTrace.class) {
			HyperTrace hyperTrace = new Hyper4DTrace();
			hyperTrace.setName(name);
			return (U)hyperTrace;
		}
		
		if (clazz == IHyperTrace.class) {
			HyperTrace hyperTrace = new HyperTrace();
			hyperTrace.setName(name);
			return (U)hyperTrace;
		}
		return null;
	}
	
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		
		if (IHyper4DTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		
		if (IHyperTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
	
	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(IHyperTrace.class);
		return l;
	}
	

}
