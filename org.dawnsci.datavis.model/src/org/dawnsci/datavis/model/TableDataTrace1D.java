package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.trace.ITableDataTrace;
import org.eclipse.january.dataset.IDataset;

public class TableDataTrace1D implements ITableDataTrace {
	private final IDataset d;
	private Object userObject = null;

	public TableDataTrace1D(IDataset d) {
		this.d = d;
	}

	@Override
	public void setName(String name) {
		
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setVisible(boolean isVisible) {
		
	}

	@Override
	public void setUserTrace(boolean isUserTrace) {
		
	}

	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
		
	}

	@Override
	public void setDataName(String name) {
		
	}

	@Override
	public boolean isVisible() {
		return false;
	}

	@Override
	public boolean isUserTrace() {
		return false;
	}

	@Override
	public boolean is3DTrace() {
		return false;
	}

	@Override
	public Object getUserObject() {
		return userObject;
	}

	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public String getDataName() {
		return null;
	}

	@Override
	public IDataset getData() {
		return d;
	}

	@Override
	public void dispose() {
		
	}
}