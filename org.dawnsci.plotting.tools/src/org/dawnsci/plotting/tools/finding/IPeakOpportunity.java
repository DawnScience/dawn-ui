package org.dawnsci.plotting.tools.finding;

import java.util.List;

import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * TODO: use instead of Peak
 * 
 * @author Dean P. Ottewell
 *
 */
public interface IPeakOpportunity {

	public List<Peak> getPeaks();
	
	//TODO: one method get
	public IDataset getRawXData();
	public IDataset getRawYData();

//TODO:  Do below later
//	public double getUpperBound();
//	public void setUpperBound();
//
//	public double getLowerBound();
//	public void setLowerBound();

}
