package org.dawnsci.plotting.tools.finding;

import java.util.List;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * @author Dean P. Ottewell
 */
public class PeakOpportunity implements IPeakOpportunity {

	List<IdentifiedPeak> peaksId = null;
	
	private Dataset xData; 
	private Dataset yData;
	
	private double upperBound;
	private double lowerBound;
	
	private Boolean isSearching = null;

	@Override
	public Dataset getXData() {
		return xData;
	}

	@Override
	public Dataset getYData() {
		return yData;
	}


	@Override
	public void setXData(Dataset xData) {
		this.xData = xData;
	}

	@Override
	public void setYData(Dataset yData) {
		this.yData = yData;
	}

	@Override
	public double getUpperBound() {
		return upperBound;
	}

	@Override
	public double getLowerBound() {
		return lowerBound;
	}

	@Override
	public void setUpperBound(double upper) {
		this.upperBound = upper;
	}

	@Override
	public void setLowerBound(double lower) {
		this.lowerBound = lower;
	}

	@Override
	public void setPeakSearching(boolean searching) {
		this.isSearching = searching;
	}

	@Override
	public Boolean getSearchingStatus() {
		return this.isSearching;
	}

	@Override
	public List<IdentifiedPeak> getPeaksId() {
		return peaksId;
	}

	@Override
	public void setPeaksId(List<IdentifiedPeak> peaks) {
		this.peaksId = peaks;
	}


}
