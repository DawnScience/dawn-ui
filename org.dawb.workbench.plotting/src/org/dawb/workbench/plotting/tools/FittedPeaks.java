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

	private List<FittedPeak>         fittedPeaks;
	private IOptimizer               optimizer;
	private FittedPeak               selectedPeak; // Should be FittedPeak
	
	public FittedPeaks() {
		this.fittedPeaks = new ArrayList<FittedPeak>(7);
	}

	public void dispose() {
		
		selectedPeak = null;
		
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.dispose();
			}
			fittedPeaks.clear();
			fittedPeaks = null;
		}

		optimizer = null;
	}
	
	/**
	 * Not thread safe, UI call.
	 */
	public void activate() {
		
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.activate();
			}
		}

	}
	/**
	 * Not thread safe, UI call.
	 */
	public void deactivate() {
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.deactivate();
			}
		}
	}

	public void setAreasVisible(boolean isVis) {
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.setFWHMVisible(isVis);
			}
		}
	}

	public void setPeaksVisible(boolean isVis) {
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.setCenterVisible(isVis);
			}
		}
	}

	public void setTracesVisible(boolean isVis) {
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.setTraceVisible(isVis);
			}
		}
	}
	public void setAnnotationsVisible(boolean isVis) {
		if (fittedPeaks!=null) {
			for (FittedPeak fp : fittedPeaks) {
				fp.setAnnotationVisible(isVis);
			}
		}
	}
	
	public void setSelectedPeak(FittedPeak peak) {
		
		this.selectedPeak = peak;
		
		for (FittedPeak fp : fittedPeaks) {
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
		fittedPeaks.remove(selectedPeak);
		selectedPeak = null;
	}

	/**
	 * Remove stored traces from a plotting system.
	 * @param sys
	 */
	public void removeSelections(IPlottingSystem sys, boolean removeSaved) {
		
		if (fittedPeaks!=null) {
			Collection<FittedPeak> removed = new HashSet<FittedPeak>(3);
			for (FittedPeak fp : fittedPeaks) {
				if (!removeSaved && fp.isSaved()) continue;
				fp.delete(sys);
				removed.add(fp);
			}
			fittedPeaks.removeAll(removed);
		}
	}

	public int size() {
		return fittedPeaks.size();
	}
	
	public String getAlgorithmType() {
		return getOptimizer().getClass().getSimpleName();
	}

	public boolean isEmpty() {
		if (fittedPeaks==null) return true;
		return fittedPeaks.isEmpty();
	}

	public IOptimizer getOptimizer() {
		return optimizer;
	}

	public void setOptimizer(IOptimizer optimizer) {
		this.optimizer = optimizer;
	}

	public List<FittedPeak> getPeakList() {
		return fittedPeaks;
	}

	/**
	 * The traces for the fitted peaks in no particular order
	 * @return
	 */
	public Collection<ITrace> getFittedPeakTraces() {
		
		if (fittedPeaks==null || fittedPeaks.isEmpty()) return Collections.EMPTY_SET;
		final Collection<ITrace> traces = new HashSet<ITrace>(5);
		for (FittedPeak fp : fittedPeaks) {
			traces.add(fp.getTrace());
		}
		
		return traces;
	}

	public FittedPeak[] toArray() {
		return fittedPeaks.toArray(new FittedPeak[fittedPeaks.size()]);
	}

	public void addFittedPeak(FittedPeak fittedPeak) {
		if (fittedPeaks==null) return;
		fittedPeaks.add(fittedPeak);
	}

	public List<IPeak> getPeakFunctions() {
		
		if (fittedPeaks==null || fittedPeaks.isEmpty()) return Collections.emptyList();
		final List<IPeak> peaks = new ArrayList<IPeak>(5);
		for (FittedPeak fp : fittedPeaks) {
			peaks.add(fp.getPeak());
		}
		
		return peaks;
	}

	public void saveSelectedPeak(IPlottingSystem sys) {
		if (selectedPeak!=null) {
			selectedPeak.setSaved(sys, true);
		}
	}

	public void addFittedPeaks(List<FittedPeak> peakList) {
		this.fittedPeaks.addAll(peakList);
	}


}
