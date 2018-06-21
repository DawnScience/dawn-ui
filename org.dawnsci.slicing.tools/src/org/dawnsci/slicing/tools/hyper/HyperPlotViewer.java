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
import org.eclipse.swt.widgets.Composite;

public class HyperPlotViewer extends IPlottingSystemViewer.Stub<Composite> {

	private HyperComponent hyper;
	//Can only show one trace;
	private HyperTrace trace;
	
	public void createControl(final Composite parent) {
		hyper = new HyperComponent();
		hyper.createControl(parent);
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		
		if (trace instanceof Hyper4DTrace) {
			Hyper4DTrace h = (Hyper4DTrace)trace;
			this.trace = h;
			
			HyperDataPackage dp = buildDataPackage(h);
			hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order, new Hyper4DMapReducer(), new Hyper4DImageReducer());
			((Hyper4DTrace) trace).setViewer(this);
			return true;
		}
		
		if (trace instanceof HyperTrace) {
			HyperTrace h = (HyperTrace)trace;
			this.trace = h;
			
			HyperDataPackage dp = buildDataPackage(h);
			
			hyper.setData(dp.lazyDataset, dp.axes, dp.slices, dp.order);
			((HyperTrace) trace).setViewer(this);
			return true;
		}
		return false;
	}
	
	@Override
	public void removeTrace(ITrace trace) {
		if (this.trace == trace){
			trace = null;
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
		if (hyper == null) return null;
		return (Composite)hyper.getControl();
	}
	
	
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
