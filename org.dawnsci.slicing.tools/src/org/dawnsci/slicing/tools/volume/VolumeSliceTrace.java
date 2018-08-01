package org.dawnsci.slicing.tools.volume;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class VolumeSliceTrace implements IVolumeSlicesTrace {

	private Object userObject;
	private ILazyDataset lazydataset;

	@Override
	public void setData(ILazyDataset data) {
		this.lazydataset = data;
	}
	
	public ILazyDataset getLazyData() {
		return lazydataset;
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

	@Override
	public IDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}
}
