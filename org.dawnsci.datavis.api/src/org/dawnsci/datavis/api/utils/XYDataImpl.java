package org.dawnsci.datavis.api.utils;

import org.dawnsci.datavis.api.IXYData;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;

public class XYDataImpl implements IXYData {

	private IDataset x;
	private IDataset y;
	private String fileName;
	private String datasetName;
	private SliceND slice;
	
	public XYDataImpl(IDataset x, IDataset y, String filename, String datasetName, SliceND slice) {
		this.x = x;
		this.y = y;
		this.fileName = filename;
		this.datasetName = datasetName;
		this.slice = slice;
	}

	@Override
	public IDataset getX() {
		return x;
	}

	@Override
	public IDataset getY() {
		return y;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getDatasetName() {
		return datasetName;
	}

	@Override
	public SliceND getSlice() {
		return slice;
	}
	
}
