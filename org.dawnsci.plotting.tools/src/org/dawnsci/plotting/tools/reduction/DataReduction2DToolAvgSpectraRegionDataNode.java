package org.dawnsci.plotting.tools.reduction;

import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;

class DataReduction2DToolAvgSpectraRegionDataNode extends DataReduction2DToolSpectraRegionDataNode {

	public DataReduction2DToolAvgSpectraRegionDataNode(IRegion plotRegion, final DataReduction2DToolModel toolModel, final DataReduction2DToolRegionData regionData) {
		super(plotRegion, toolModel, regionData);
	}
	
	@Override
	public Dataset getDataset(Dataset fullData) {
		Dataset result = DatasetFactory.zeros(fullData.getClass(), 0, fullData.getShapeRef()[1]);
		for (DataReduction2DToolSpectrumDataNode node : this.getSpectra()) {
			int i = node.getIndex();
			Dataset data = fullData.getSliceView(new int[]{i, 0}, new int[]{i + 1, fullData.getShape()[1]}, new int[]{1,1});
			data.setShape(1, fullData.getShape()[1]);
			result = DatasetUtils.append(result, data, 0);
		}
		result = result.mean(0);
		result.setShape(1, fullData.getShape()[1]);
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
