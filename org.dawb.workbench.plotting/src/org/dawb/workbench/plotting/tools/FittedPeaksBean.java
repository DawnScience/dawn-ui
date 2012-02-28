package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

/**
 * Stores various information about the fit, including the IRegions
 * and other GUI things.
 * 
 * @author fcp94556
 *
 */
public class FittedPeaksBean {

	private List<RegionBounds>       peakBounds;
	private List<? extends IPeak>    peakFunctions;
	private List<IRegion>            peakAreaRegions;
	private List<IRegion>            peakLineRegions;
	private List<ITrace>             peakTraces;
	private IOptimizer               optimizer;
	
	public FittedPeaksBean() {
		this.peakAreaRegions = new ArrayList<IRegion>(7);
		this.peakLineRegions = new ArrayList<IRegion>(7);
		this.peakTraces  = new ArrayList<ITrace>(7);
	}

	public void dispose() {
		if (peakBounds!=null) peakBounds.clear();
		peakBounds = null;
		
		if (peakFunctions!=null) peakFunctions.clear();
		peakFunctions = null;
		
		if (peakAreaRegions!=null) peakAreaRegions.clear();
		peakAreaRegions = null;

		if (peakLineRegions!=null) peakLineRegions.clear();
		peakLineRegions = null;
		
		if (peakTraces!=null) peakTraces.clear();
		peakTraces = null;
		
		optimizer = null;
	}
	
	/**
	 * Not thread safe, UI call.
	 */
	public void activate() {
		for (IRegion region : peakAreaRegions) region.setVisible(true);
		for (IRegion region : peakLineRegions) region.setVisible(true);
		for (ITrace  trace  : peakTraces)  trace.setVisible(true);
	}
	/**
	 * Not thread safe, UI call.
	 */
	public void deactivate() {
		for (IRegion region : peakAreaRegions) region.setVisible(false);
		for (IRegion region : peakLineRegions) region.setVisible(false);
		for (ITrace  trace  : peakTraces)  trace.setVisible(false);
	}

	public void setSelectedPeak(int ipeak) {
		for (IRegion region : peakAreaRegions) region.setRegionColor(ColorConstants.orange);
		peakAreaRegions.get(ipeak).setRegionColor(ColorConstants.red);
		
		for (ITrace trace : peakTraces) ((ILineTrace)trace).setTraceColor(ColorConstants.black);
		
		final ILineTrace trace = ((ILineTrace)peakTraces.get(ipeak));
		trace.setTraceColor(ColorConstants.darkGreen);

		trace.repaint();
	}

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
				+ ((optimizer == null) ? 0 : optimizer.hashCode());
		result = prime * result
				+ ((peakAreaRegions == null) ? 0 : peakAreaRegions.hashCode());
		result = prime * result
				+ ((peakBounds == null) ? 0 : peakBounds.hashCode());
		result = prime * result
				+ ((peakFunctions == null) ? 0 : peakFunctions.hashCode());
		result = prime * result
				+ ((peakLineRegions == null) ? 0 : peakLineRegions.hashCode());
		result = prime * result
				+ ((peakTraces == null) ? 0 : peakTraces.hashCode());
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
		if (optimizer == null) {
			if (other.optimizer != null)
				return false;
		} else if (!optimizer.equals(other.optimizer))
			return false;
		if (peakAreaRegions == null) {
			if (other.peakAreaRegions != null)
				return false;
		} else if (!peakAreaRegions.equals(other.peakAreaRegions))
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
		if (peakLineRegions == null) {
			if (other.peakLineRegions != null)
				return false;
		} else if (!peakLineRegions.equals(other.peakLineRegions))
			return false;
		if (peakTraces == null) {
			if (other.peakTraces != null)
				return false;
		} else if (!peakTraces.equals(other.peakTraces))
			return false;
		return true;
	}
	public List<AbstractDataset[]> getFunctionData() {
		return functionData;
	}
	public void setFunctionData(List<AbstractDataset[]> functionData) {
		this.functionData = functionData;
	}
	public List<ITrace> getPeakTraces() {
		return peakTraces;
	}
	public void setPeakTraces(ArrayList<ITrace> peakTraces) {
		this.peakTraces = peakTraces;
	}

	/**
	 * Remove stored traces from a plotting system.
	 * @param sys
	 */
	public void removeSelections(IPlottingSystem sys) {
		for (ITrace  trace   : peakTraces)   sys.removeTrace(trace);
		for (IRegion region  : peakAreaRegions)  sys.removeRegion(region);
		for (IRegion region  : peakLineRegions)  sys.removeRegion(region);
		peakAreaRegions.clear();
		peakLineRegions.clear();
		peakTraces.clear();
	}

	public void addAreaRegion(IRegion region) {
		peakAreaRegions.add(region);
	}
	public void addLineRegion(IRegion region) {
		peakLineRegions.add(region);
	}

	public void addTrace(ILineTrace trace) {
		peakTraces.add(trace);
	}

	public int size() {
		return peakFunctions.size();
	}

	public String getPeakName(int peakNumber) {
		try {
		    return peakTraces.get(peakNumber).getName();
		} catch (IndexOutOfBoundsException ne) {
			return null;
		}
	}

	public double getPosition(Integer peakNumber) {
		try {
			return ((APeak)this.peakFunctions.get(peakNumber)).getPosition();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}
	
	public double getFWHM(Integer peakNumber) {
		try {
			return ((APeak)this.peakFunctions.get(peakNumber)).getFWHM();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}
	
	public double getArea(Integer peakNumber) {
		try {
			return ((APeak)this.peakFunctions.get(peakNumber)).getArea();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}
	
	public String getPeakType(Integer peakNumber) {
		try {
		    IPeak peak =  peakFunctions.get(peakNumber);
		    return peak.getClass().getSimpleName();
		} catch (IndexOutOfBoundsException ne) {
			return null;
		}
	}
	
	public String getAlgorithmType(Integer peakNumber) {
		return getOptimizer().getClass().getSimpleName();
	}

	public boolean isEmpty() {
		if (peakFunctions==null) return true;
		return peakFunctions.isEmpty();
	}

	public IOptimizer getOptimizer() {
		return optimizer;
	}

	public void setOptimizer(IOptimizer optimizer) {
		this.optimizer = optimizer;
	}


}
