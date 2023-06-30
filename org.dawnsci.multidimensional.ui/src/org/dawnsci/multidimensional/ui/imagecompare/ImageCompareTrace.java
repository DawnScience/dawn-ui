package org.dawnsci.multidimensional.ui.imagecompare;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class ImageCompareTrace implements IImageCompareTrace {

	private List<ILazyDataset> images;

	@Override
	public void initialize(IAxis... axes) {
		// TODO Auto-generated method stub

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
		return null;
	}

	@Override
	public void setUserObject(Object userObject) {
		// TODO Auto-generated method stub

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
	public void setImages(List<ILazyDataset> images) {
		this.images = images;
		
	}

	@Override
	public List<ILazyDataset> getImages() {
		return images;
	}

}
