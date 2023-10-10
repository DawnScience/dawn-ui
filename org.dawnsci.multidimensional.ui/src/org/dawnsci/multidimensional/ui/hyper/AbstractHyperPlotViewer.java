package org.dawnsci.multidimensional.ui.hyper;

import java.util.Arrays;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class AbstractHyperPlotViewer extends IPlottingSystemViewer.Stub<Composite>  {
	
	
	protected BaseHyperTrace trace;
	protected HyperComponent hyper;
	protected Composite control;

	
	protected void innerCreateControl(Composite parent, Layout layout) {
		control = new Composite(parent, SWT.None);
		control.setLayout(layout);
	}
	
	protected void createHyperComponent() {
		Composite hyperComp =new Composite(control, SWT.None);
		hyperComp.setLayout(GridLayoutFactory.fillDefaults().create());
		hyperComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hyper = new HyperComponent();
		hyper.createControl(hyperComp);
	}
	
	protected HyperDataPackage buildDataPackage(ILazyBlockTrace hyperTrace) {
		ILazyDataset lazyDataset = hyperTrace.getLazyDataset();
		int[] order = hyperTrace.getOrder();
		int rank = order.length;
		
		IDataset[] daxes = new IDataset[rank];
		
		AxesMetadata md = lazyDataset.getFirstMetadata(AxesMetadata.class);
		int[] shape = lazyDataset.getShape();
		if (md == null) {
			
			for (int i = 0; i < rank; i++) {
				IDataset d = DatasetFactory.createRange(shape[order[i]]);
				daxes[i] = d;
			}
			
		} else {

			for (int i = 0; i < rank;i++) {
				ILazyDataset[] axis = md.getAxis(order[i]);
				IDataset d = null;
				if (axis == null || axis[0] == null) {
					d = DatasetFactory.createRange(shape[order[i]]);
				} else {
					try {
						//TODO ndAxes
						d = axis[0].getSlice();
						d.squeeze();
						String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
						d.setName(name);
					} catch (DatasetException e) {
						//TODO log
						d = DatasetFactory.createRange(shape[order[i]]);
					}
				}
				
				daxes[i] = d;
			}
			
		}
		
		return new HyperDataPackage(lazyDataset, Arrays.asList(daxes), hyperTrace.getSlice().convertToSlice(), order);
	}
	
	@Override
	public Composite getControl() {
		return control;
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
	
	@Override
	public void removeTrace(ITrace trace) {
		if (this.trace == trace){
			this.trace = null;
			//might be significant data in memory, need to clear it
			hyper.clearData();
			hyper.clear();
		}
		
	}
	
	@Override
	public void clearTraces() {
		//might be significant data in memory, need to clear it
		hyper.clearData();
		
	}
	
	@Override
	public void reset(boolean force) {
		//might be significant data in memory, need to clear it
		hyper.clearData();
		
	}

	
	protected class HyperDataPackage {
		
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
