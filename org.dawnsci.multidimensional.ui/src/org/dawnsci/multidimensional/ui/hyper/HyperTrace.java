package org.dawnsci.multidimensional.ui.hyper;

import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class HyperTrace implements IHyperTrace {

	private ILazyDataset lazy;
	private int[] order;
	private SliceND slice;
	private Object userObject;
	
	private HyperPlotViewer viewer;

	@Override
	public void initialize(IAxis... axes) {
	}

	@Override
	public String getDataName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDataName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisible(boolean isVisible) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isUserTrace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getUserObject() {
		// TODO Auto-generated method stub
		return userObject;
	}

	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;

	}

	@Override
	public boolean is3DTrace() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	public void setViewer(HyperPlotViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public void setData(ILazyDataset lazy, int[] order, SliceND slice) {
		
		boolean keepRegions = false;
		
		if (this.lazy != null && this.order != null && this.slice != null) {
			keepRegions = Arrays.equals(lazy.getShape(), this.lazy.getShape()) && Arrays.equals(order, this.order);
		}
		
		this.lazy = lazy;
		this.order = order;
		this.slice = slice;
		if (viewer != null) viewer.update(keepRegions);
	}

	@Override
	public ILazyDataset getLazyDataset() {
		return lazy;
	}

	@Override
	public int[] getOrder() {
		return order;
	}
	
	@Override
	public SliceND getSlice() {
		return slice;
	}

}
