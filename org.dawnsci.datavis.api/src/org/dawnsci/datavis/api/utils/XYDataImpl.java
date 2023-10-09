package org.dawnsci.datavis.api.utils;

import org.dawnsci.datavis.api.IXYData;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;

public class XYDataImpl implements IXYData {

	private IDataset x;
	private IDataset y;
	private String fileName;
	private String datasetName;
	private String labelName;
	private SliceND slice;
	private String label;
	
	public XYDataImpl(IDataset x, IDataset y, String label, String filename, String datasetName, String labelName, SliceND slice) {
		this.x = x;
		this.y = y;
		this.label = label;
		this.fileName = filename;
		this.datasetName = datasetName;
		this.labelName = labelName;
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
	public String getLabelName() {
		return labelName;
	}

	@Override
	public SliceND getSlice() {
		return slice;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return fileName;
	}
}
