package org.dawnsci.plotting.tools.fitting;

import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * A bean to hold information required to do a fit.
 * 
 * @author fcp94556
 *
 */
public class FittedPeaksInfo {

	private List<IdentifiedPeak> identifiedPeaks;
	private Dataset  x; 
	private Dataset  y;
	private IMonitor monitor;

	private IPlottingSystem plottingSystem;
	private ILineTrace selectedTrace;
	private int numPeaks = -1;
	
	public FittedPeaksInfo(Dataset x, Dataset y, IMonitor monitor) {
		this.x = x;
		this.y = y;
		this.monitor = monitor;
	}

	public FittedPeaksInfo(final Dataset  x, 
			final Dataset  y,
			final IMonitor monitor,
			IPlottingSystem plottingSystem, 
			ILineTrace selectedTrace){
		this(x, y, monitor);
		this.setPlottingSystem(plottingSystem);
		this.setSelectedTrace(selectedTrace);
	}

	public FittedPeaksInfo(final Dataset  x, 
			final Dataset  y,
			final IMonitor monitor,
			IPlottingSystem plottingSystem,
			ILineTrace selectedTrace,
			int numPeaks){
		this(x, y, monitor);
		this.setPlottingSystem(plottingSystem);
		this.setSelectedTrace(selectedTrace);
		this.setNumPeaks(numPeaks);
	}

	public List<IdentifiedPeak> getIdentifiedPeaks() {
		return identifiedPeaks;
	}
	public void setIdentifiedPeaks(List<IdentifiedPeak> ideedPeaks) {
		this.identifiedPeaks = ideedPeaks;
	}
	public Dataset getX() {
		return (Dataset) x;
	}
	public void setX(Dataset x) {
		this.x = x;
	}
	public Dataset getY() {
		return (Dataset) y;
	}
	public void setY(Dataset y) {
		this.y = y;
	}
	public IMonitor getMonitor() {
		return monitor;
	}
	public void setMonitor(IMonitor monitor) {
		this.monitor = monitor;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifiedPeaks == null) ? 0 : identifiedPeaks.hashCode());
		result = prime * result + ((monitor == null) ? 0 : monitor.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
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
		FittedPeaksInfo other = (FittedPeaksInfo) obj;
		if (identifiedPeaks == null) {
			if (other.identifiedPeaks != null)
				return false;
		} else if (!identifiedPeaks.equals(other.identifiedPeaks))
			return false;
		if (monitor == null) {
			if (other.monitor != null)
				return false;
		} else if (!monitor.equals(other.monitor))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}

	public ILineTrace getSelectedTrace() {
		return selectedTrace;
	}

	public void setSelectedTrace(ILineTrace selectedTrace) {
		this.selectedTrace = selectedTrace;
	}

	public int getNumPeaks() {
		if(numPeaks == -1)
			return Activator.getPlottingPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
		return numPeaks;
	}

	public void setNumPeaks(int numPeaks) {
		this.numPeaks = numPeaks;
	}

	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public void setPlottingSystem(IPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
	}
}
