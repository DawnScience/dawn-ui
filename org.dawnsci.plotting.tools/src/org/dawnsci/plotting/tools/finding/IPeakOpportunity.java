package org.dawnsci.plotting.tools.finding;

import java.util.List;

import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * TODO: use instead of Peak
 * 
 * @author Dean P. Ottewell
 */
public interface IPeakOpportunity {

	public List<Peak> getPeaks();
	public void setPeaks(List<Peak> peaks);
		
	public IDataset getXData();
	public void  setXData(IDataset xData);
	
	public IDataset getYData();
	public void setYData(IDataset yData);

	
	//TODO: can bound changes exists without data set?
	public double getUpperBound();
	public double getLowerBound();

	public void setUpperBound(double upper);
	public void setLowerBound(double lower);
}
