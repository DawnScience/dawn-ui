package org.dawb.workbench.plotting.tools;

import java.util.List;

import org.dawb.common.ui.plot.region.RegionBounds;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;

public class FittedPeaksBean {

	private List<RegionBounds>       peakBounds;
	private List<? extends IPeak>    peakFunctions;
	
	/**
	 * x and y pairs for the fitted functions.
	 */
	private List<AbstractDataset[]>  functionData;

	
	public List<RegionBounds> getPeakBounds() {
		return peakBounds;
	}
	public void setPeakBounds(List<RegionBounds> peakBounds) {
		this.peakBounds = peakBounds;
	}
	public List<? extends IPeak> getPeakFunctions() {
		return peakFunctions;
	}
	public void setPeakFunctions(List<? extends IPeak> peakFunctions) {
		this.peakFunctions = peakFunctions;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((functionData == null) ? 0 : functionData.hashCode());
		result = prime * result
				+ ((peakBounds == null) ? 0 : peakBounds.hashCode());
		result = prime * result
				+ ((peakFunctions == null) ? 0 : peakFunctions.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FittedPeaksBean other = (FittedPeaksBean) obj;
		if (functionData == null) {
			if (other.functionData != null)
				return false;
		} else if (!functionData.equals(other.functionData))
			return false;
		if (peakBounds == null) {
			if (other.peakBounds != null)
				return false;
		} else if (!peakBounds.equals(other.peakBounds))
			return false;
		if (peakFunctions == null) {
			if (other.peakFunctions != null)
				return false;
		} else if (!peakFunctions.equals(other.peakFunctions))
			return false;
		return true;
	}
	public List<AbstractDataset[]> getFunctionData() {
		return functionData;
	}
	public void setFunctionData(List<AbstractDataset[]> functionData) {
		this.functionData = functionData;
	}
}
