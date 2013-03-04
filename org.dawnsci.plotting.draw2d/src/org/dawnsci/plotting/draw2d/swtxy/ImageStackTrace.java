package org.dawnsci.plotting.draw2d.swtxy;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.widgets.figureparts.ColorMapRamp;
import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.trace.IImageStackTrace;
import org.dawb.common.ui.plot.trace.IStackPositionListener;
import org.dawb.common.ui.plot.trace.StackPositionEvent;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

public class ImageStackTrace extends ImageTrace implements IImageStackTrace {

	private int          index=0;
	private ILazyDataset stack;

	public ImageStackTrace(String       name, 
			               Axis         xAxis, 
			               Axis         yAxis,
			               ColorMapRamp intensityScale) {
		super(name, xAxis, yAxis, intensityScale);
	}

	@Override
	public void setStack(ILazyDataset stack) {
		this.stack = stack;
	}

	@Override
	public int getStackSize() {
		return stack.getShape()[0];
	}

	@Override
	public int getStackIndex() {
		return index;
	}

	@Override
	public void setStackIndex(int index) {
		
		this.index = index;
		IDataset set = stack.getSlice(new int[]{index,0,0}, 
									new int[]{index+1,stack.getShape()[1], stack.getShape()[2]},
									new int[]{1,1,1});
		set = (IDataset)set.squeeze();
		setData((AbstractDataset)set, getAxes(), false);
		fireStackPositionListeners(index);
	}

	private void fireStackPositionListeners(int i) {
		if (listeners==null) return;
		final StackPositionEvent evt = new StackPositionEvent(this, i);
		for (IStackPositionListener l : listeners) {
			try {
				l.stackPositionChanged(evt);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private Collection<IStackPositionListener> listeners;
	@Override
	public void addStackPositionListener(IStackPositionListener l) {
		if (listeners==null) listeners = new HashSet<IStackPositionListener>(7);
		listeners.add(l);
	}

	@Override
	public void removeStackPositionListener(IStackPositionListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	public void remove() {
        super.remove();
        if (listeners!=null) listeners.clear();
        listeners = null;
	}
}
