package org.dawnsci.multidimensional.ui.hyper;

import java.util.List;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;

/**
 * Bean to hold the NDimensional lazydataset, its axes,
 * a slice defining a sub-array and the dimension ordering
 *
 */
public class HyperBean {

	public ILazyDataset data;
	public List<IDataset> axes;
	public int[] order;
	public Slice[] slices;

	public HyperBean(ILazyDataset data, List<IDataset> axes, Slice[] slices, int[] order) {
		this.data = data;
		this.axes = axes;
		this.order = order;
		this.slices = slices;
	}
}
