package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

/**
 * Stores various information about the fit, including the IRegions
 * and other GUI things.
 * 
 * @author fcp94556
 *
 */
public class FittedPeaks {

	private List<FittedPeak>         fittedPeakList;
	private IOptimizer               optimizer;
	private FittedPeak               selectedPeak; // Should be FittedPeak
	
	public FittedPeaks() {
		this.fittedPeakList = new ArrayList<FittedPeak>(7);
	}
	
	public FittedPeaks clone() {
		final FittedPeaks ret  = new FittedPeaks();
		if (selectedPeak!=null) ret.selectedPeak = selectedPeak.clone();
		for (FittedPeak fp : fittedPeakList) {
			ret.fittedPeakList.add(fp.clone());
		}
		ret.optimizer = optimizer;
		return ret;
	}

	public void dispose() {
		
		selectedPeak = null;
		
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.dispose();
			}
			fittedPeakList.clear();
			fittedPeakList = null;
		}

		optimizer = null;
	}
	
	/**
	 * Not thread safe, UI call.
	 */
	public void activate() {
		
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.activate();
			}
		}

	}
	/**
	 * Not thread safe, UI call.
	 */
	public void deactivate() {
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.deactivate();
			}
		}
	}

	public void setAreasVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.setFWHMVisible(isVis);
			}
		}
	}

	public void setPeaksVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.setCenterVisible(isVis);
			}
		}
	}

	public void setTracesVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.setTraceVisible(isVis);
			}
		}
	}
	public void setAnnotationsVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedPeak fp : fittedPeakList) {
				fp.setAnnotationVisible(isVis);
			}
		}
	}
	
	public void setSelectedPeak(FittedPeak peak) {
		
		this.selectedPeak = peak;
		
		for (FittedPeak fp : fittedPeakList) {
			fp.getFwhm().setRegionColor(ColorConstants.orange);
			((ILineTrace)fp.getTrace()).setTraceColor(ColorConstants.black);
			fp.getAnnotation().setAnnotationColor(ColorConstants.black);
		}
		
		peak.getFwhm().setRegionColor(ColorConstants.red);
		((ILineTrace)peak.getTrace()).setTraceColor(ColorConstants.darkGreen);
		peak.getAnnotation().setAnnotationColor(ColorConstants.darkGreen);
		
		this.selectedPeak = peak;
	}
		
	public void deleteSelectedPeak(IPlottingSystem sys) {
		
		if (selectedPeak==null) return;
				
		selectedPeak.delete(sys);	
		fittedPeakList.remove(selectedPeak);
		selectedPeak = null;
	}

	/**
	 * Remove stored traces from a plotting system.
	 * @param sys
	 */
	public void removeSelections(IPlottingSystem sys, boolean removeSaved) {
		
		if (fittedPeakList!=null) {
			Collection<FittedPeak> removed = new HashSet<FittedPeak>(3);
			for (FittedPeak fp : fittedPeakList) {
				if (!removeSaved && fp.isSaved()) continue;
				fp.delete(sys);
				removed.add(fp);
			}
			fittedPeakList.removeAll(removed);
		}
	}

	public int size() {
		return fittedPeakList.size();
	}
	
	public String getAlgorithmType() {
		return getOptimizer().getClass().getSimpleName();
	}

	public boolean isEmpty() {
		if (fittedPeakList==null) return true;
		return fittedPeakList.isEmpty();
	}

	public IOptimizer getOptimizer() {
		return optimizer;
	}

	public void setOptimizer(IOptimizer optimizer) {
		this.optimizer = optimizer;
	}

	public List<FittedPeak> getPeakList() {
		return fittedPeakList;
	}

	/**
	 * The traces for the fitted peaks in no particular order
	 * @return
	 */
	public Collection<ITrace> getFittedPeakTraces() {
		
		if (fittedPeakList==null || fittedPeakList.isEmpty()) return Collections.EMPTY_SET;
		final Collection<ITrace> traces = new HashSet<ITrace>(5);
		for (FittedPeak fp : fittedPeakList) {
			traces.add(fp.getTrace());
		}
		
		return traces;
	}

	public FittedPeak[] toArray() {
		return fittedPeakList.toArray(new FittedPeak[fittedPeakList.size()]);
	}

	public void addFittedPeak(FittedPeak fittedPeak) {
		if (fittedPeakList==null) return;
		fittedPeakList.add(fittedPeak);
	}

	public List<IPeak> getPeakFunctions() {
		
		if (fittedPeakList==null || fittedPeakList.isEmpty()) return Collections.emptyList();
		final List<IPeak> peaks = new ArrayList<IPeak>(5);
		for (FittedPeak fp : fittedPeakList) {
			peaks.add(fp.getPeak());
		}
		
		return peaks;
	}

	public void saveSelectedPeak(IPlottingSystem sys) throws Exception {
		if (selectedPeak!=null) {
			selectedPeak.setSaved(sys, true, getAllNames());
		}
	}

	public void addFittedPeaks(List<FittedPeak> peakList) {
		this.fittedPeakList.addAll(peakList);
	}

    /**
     * Gets all the active region and plot names
     * @return
     */
	private String[] getAllNames() {
		
		List<String> names = new ArrayList<String>(7);
		for (FittedPeak fp : fittedPeakList) {
			fp.getUsedNames(names);
		}
		return names.toArray(new String[names.size()]);
	}
}
