package org.dawnsci.multidimensional.ui.hyper;

import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

/**
 * Base trace for plot views that slice a ND dataset
 * using multiple lightweight plot views
 *
 * Can be extended with empty implementation and marker interface
 * to use produce multiple hyper-plotsystems
 */
public class BaseHyperTrace implements ILazyBlockTrace, ITrace {

	private ILazyDataset lazy;
	private int[] order;
	private SliceND slice;
	private Object userObject;
	
	private AbstractHyperPlotViewer viewer;



	@Override
	public Object getUserObject() {
		return userObject;
	}

	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;

	}

	public void setViewer(AbstractHyperPlotViewer viewer) {
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


	public ILazyDataset getLazyDataset() {
		return lazy;
	}


	public int[] getOrder() {
		return order;
	}
	
	public SliceND getSlice() {
		return slice;
	}
	
	//The below methods are not required for
	//this class of trace
	
	@Override
	public void dispose() {}
	
	@Override
	public void initialize(IAxis... axes) {}

	@Override
	public String getDataName() {return null;}

	@Override
	public void setDataName(String name) {}

	@Override
	public IDataset getData() {return null;}

	@Override
	public boolean isVisible() {return false;}

	@Override
	public void setVisible(boolean isVisible) {}

	@Override
	public boolean isUserTrace() {return false;}

	@Override
	public void setUserTrace(boolean isUserTrace) {}
	
	@Override
	public boolean is3DTrace() {return false;}

	@Override
	public int getRank() {return 0;}

	@Override
	public String getName() {return null;}

	@Override
	public void setName(String name) {}

}
