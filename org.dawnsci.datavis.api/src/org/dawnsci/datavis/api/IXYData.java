package org.dawnsci.datavis.api;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;

public interface IXYData {

	SliceND getSlice();

	String getDatasetName();

	String getLabelName();

	String getFileName();

	IDataset getY();

	IDataset getX();

	String getLabel();
}
