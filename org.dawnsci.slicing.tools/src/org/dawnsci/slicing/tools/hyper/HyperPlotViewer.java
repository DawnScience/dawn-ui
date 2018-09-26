package org.dawnsci.slicing.tools.hyper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
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

public class HyperPlotViewer extends IPlottingSystemViewer.Stub<Composite> {
	
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

	private HyperComponent hyper;
	//Can only show one trace;
	private HyperTrace trace;
	private Composite control;
	private GridData gridData;
	private ComboViewer viewer;
	private Hyper3Dmode currentMode = Hyper3Dmode.AREA_TO_LINE;
	
	@Override
	public void createControl(final Composite parent) {
		control = new Composite(parent, SWT.None);
		control.setLayout(GridLayoutFactory.fillDefaults().create());
		Composite hyperComp =new Composite(control, SWT.None);
		hyperComp.setLayout(GridLayoutFactory.fillDefaults().create());
		hyperComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hyper = new HyperComponent();
		hyper.createControl(hyperComp);
		viewer = new ComboViewer(control);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
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
	
	private void plotHyper3D(HyperTrace trace) {
		HyperDataPackage dp = buildDataPackage(trace);
		IDatasetROIReducer[] reducers = getReducers(currentMode);
		if (reducers == null) {
			return;
		}
		hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order,reducers[0],reducers[1]);
		trace.setViewer(this);
		showViewer(true);
	}
	
	@Override
	public void removeTrace(ITrace trace) {
		if (this.trace == trace){
			this.trace = null;
			hyper.clear();
		}
		
	}
	
	public void update(boolean keepRegions) {
		if (trace != null) {
			HyperDataPackage dp = buildDataPackage(trace);
			if (keepRegions){
				hyper.updateData(dp.lazyDataset, dp.axes, dp.slices, dp.order);
			} else {
				hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order);
			}
			
		}
		
	}
	
	private void showViewer(boolean show) {
		gridData.exclude = show;
		viewer.getCombo().setVisible(show);
		viewer.getCombo().pack();
	}
	
	private HyperDataPackage buildDataPackage(IHyperTrace hyperTrace) {
		ILazyDataset lazyDataset = hyperTrace.getLazyDataset();
		int[] order = hyperTrace.getOrder();
		int rank = order.length;
		
		IDataset[] daxes = new IDataset[rank];
		
		AxesMetadata md = lazyDataset.getFirstMetadata(AxesMetadata.class);
		
		if (md == null) {
			
			for (int i = 0; i < 3;i++) {
				IDataset d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
				daxes[i] = d;
			}
			
		} else {

			for (int i = 0; i < rank;i++) {
				ILazyDataset[] axis = md.getAxis(order[i]);
				IDataset d = null;
				if (axis == null || axis[0] == null) {
					d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
				} else {
					try {
						//TODO ndAxes
						d = axis[0].getSlice();
						d.squeeze();
						String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
						d.setName(name);
					} catch (DatasetException e) {
						//TODO log
						d = DatasetFactory.createRange(lazyDataset.getShape()[order[i]]);
					}
				}
				
				daxes[i] = d;
			}
			
		}
		
		return new HyperDataPackage(lazyDataset, Arrays.asList(daxes), hyperTrace.getSlice().convertToSlice(), order);
	}
	
	public Composite getControl() {
		return control;
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
	
	private class HyperDataPackage {
		
		public ILazyDataset lazyDataset;
		public List<IDataset> axes;
		public Slice[] slices;
		public int[] order;
		
		public HyperDataPackage(ILazyDataset lazy, List<IDataset> axes, Slice[] slices, int[] order) {
			this.lazyDataset = lazy;
			this.axes = axes;
			this.slices = slices;
			this.order = order;
		}
		
		
	}
}
