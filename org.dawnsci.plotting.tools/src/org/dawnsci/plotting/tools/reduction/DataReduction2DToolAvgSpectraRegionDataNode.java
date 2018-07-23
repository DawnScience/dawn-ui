package org.dawnsci.plotting.tools.reduction;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

class DataReduction2DToolAvgSpectraRegionDataNode extends DataReduction2DToolSpectraRegionDataNode {

	public DataReduction2DToolAvgSpectraRegionDataNode(IRegion plotRegion, final DataReduction2DToolModel toolModel, final DataReduction2DToolRegionData regionData) {
		super(plotRegion, toolModel, regionData);
	}
	
	@Override
	public Dataset getDataset(Dataset fullData) {
		Dataset result = DatasetFactory.zeros(fullData.getClass(), 1, fullData.getShapeRef()[1]);
		for (DataReduction2DToolSpectrumDataNode node : getSpectra()) {
			int i = node.getIndex();
			Dataset data = fullData.getSliceView(new int[]{i, 0}, new int[]{i + 1, fullData.getShape()[1]}, new int[]{1,1});
			result.iadd(data);
		}
		result.idivide(getSpectra().size());
		return result;
	}
	
	@Override
	public String toString() {
		
		int noAve = getTotalSpectra();
		
		return super.toString() + " avg(" + noAve + ")";
	}

	public int getNoOfSpectraToAvg() {
		return getTotalSpectra();
	}
}
