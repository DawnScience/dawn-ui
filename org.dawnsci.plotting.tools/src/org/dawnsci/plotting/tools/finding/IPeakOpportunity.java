package org.dawnsci.plotting.tools.finding;

import java.util.List;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * @author Dean P. Ottewell
 */
public interface IPeakOpportunity {
	public List<IdentifiedPeak>  getPeaksId();
	public void setPeaksId(List<IdentifiedPeak> peaks);
	
	public Dataset getXData();
	public void  setXData(Dataset xData);
	
	public Dataset getYData();
	public void setYData(Dataset yData);
	
	public double getUpperBound();
	public double getLowerBound();

	public void setUpperBound(double upper);
	public void setLowerBound(double lower);

	public void setPeakSearching(boolean searching);
	public Boolean getSearchingStatus();
	
	
}
