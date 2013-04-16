package org.dawnsci.plotting.tools.fitting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Vector3d;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
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
public class FittedFunctions {	
	
	private List<FittedFunction>     fittedPeakList;
	private FittedFunction           selectedPeak; // Should be FittedPeak
	
	private transient IOptimizer     optimizer;
	
	public FittedFunctions() {
		this.fittedPeakList = new ArrayList<FittedFunction>(7);
	}
	
	public FittedFunctions clone() {
		final FittedFunctions ret  = new FittedFunctions();
		if (selectedPeak!=null) ret.selectedPeak = selectedPeak.clone();
		for (FittedFunction fp : fittedPeakList) {
			ret.fittedPeakList.add(fp.clone());
		}
		ret.optimizer = optimizer;
		return ret;
	}

	public void dispose() {
		
		selectedPeak = null;
		
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
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
			for (FittedFunction fp : fittedPeakList) {
				fp.activate();
			}
		}

	}
	/**
	 * Not thread safe, UI call.
	 */
	public void deactivate() {
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
				fp.deactivate();
			}
		}
	}

	public void setAreasVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
				fp.setFWHMVisible(isVis);
			}
		}
	}

	IAnnotation distAnnotation=null;

	public void setDistAnnotation(IAnnotation ann){
		distAnnotation=ann;
	}

	public void setPeaksVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
				fp.setCenterVisible(isVis);
			}
			if (distAnnotation !=null)
				distAnnotation.setVisible(isVis);
		}
	}

	public void setTracesVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
				fp.setTraceVisible(isVis);
			}
		}
	}

	public Vector3d calcReflectionDistance() {
		double max=-1;
		double thFactor=0.1;
		FittedFunction fpmax=null;
		FittedFunction fpmin=null;
		if (fittedPeakList!=null) {
			//look for max
			for (FittedFunction fp : fittedPeakList) {
				max=Math.max(max, fp.getDataValue());
			}
			//select and sort
			Vector<Double> pos= new Vector<Double>();
			Vector<Vector3d> qVec=new Vector<Vector3d>();
			for (int ct=fittedPeakList.size()-1;ct>=0;ct--) {
				if ( fittedPeakList.get(ct).getDataValue() < max*thFactor) {
					fittedPeakList.remove(ct);
				}
			}
			double min=max;
			for (FittedFunction fp : fittedPeakList) {
				if (fp.getDataValue()==max)
					fpmax=fp;
				if (fp.getDataValue()<min)
					fpmin=fp;
				//if ( fp.getPeakValue() >= max*thFactor) {
					boolean last=true;
					for (int i=0; i<pos.size();i++) {
						if ( ((Double)(pos.elementAt(i))).doubleValue() > fp.getPosition() ) {
							pos.insertElementAt(new Double(fp.getPosition()), i);
							qVec.insertElementAt(fp.getQ(), i);
							last=false;
							break;
						}
					}
					if (last) {
						pos.addElement(new Double(fp.getPosition()));
						qVec.addElement(fp.getQ());
					}
							
				//}
			}
			//calc avg distance
			double avg=0;
			for (int i=1; i<pos.size();i++) {
				Vector3d t= new Vector3d((Vector3d)(qVec.elementAt(i)));
				t.sub((Vector3d)(qVec.elementAt(i-1)));
				double len=t.length()-avg;
				avg+=len/i;
			}
			//convert to real space
			if (avg!=0) {
				avg=1/avg;
				
				//display
				return new Vector3d(fpmax.getPosition(), fpmin.getDataValue(),avg);                  	
			}
		}
		return new Vector3d(0,0,0);
	}

	public void setAnnotationsVisible(boolean isVis) {
		if (fittedPeakList!=null) {
			for (FittedFunction fp : fittedPeakList) {
				fp.setAnnotationVisible(isVis);
			}
		}
	}
	
	public void setSelectedFit(FittedFunction peak) {
		
		this.selectedPeak = peak;
		
		for (FittedFunction fp : fittedPeakList) {
			fp.getFwhm().setRegionColor(ColorConstants.orange);
			((ILineTrace)fp.getTrace()).setTraceColor(ColorConstants.black);
			fp.getAnnotation().setAnnotationColor(ColorConstants.black);
		}
		
		peak.getFwhm().setRegionColor(ColorConstants.red);
		((ILineTrace)peak.getTrace()).setTraceColor(ColorConstants.darkGreen);
		peak.getAnnotation().setAnnotationColor(ColorConstants.darkGreen);
		
		this.selectedPeak = peak;
	}
		
	public void deleteSelectedFunction(IPlottingSystem sys) {
		
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
			Collection<FittedFunction> removed = new HashSet<FittedFunction>(3);
			for (FittedFunction fp : fittedPeakList) {
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

	public List<FittedFunction> getFunctionList() {
		return fittedPeakList;
	}

	/**
	 * The traces for the fitted peaks in no particular order
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Collection<ITrace> getFittedTraces() {
		
		if (fittedPeakList==null || fittedPeakList.isEmpty()) return Collections.EMPTY_SET;
		final Collection<ITrace> traces = new HashSet<ITrace>(5);
		for (FittedFunction fp : fittedPeakList) {
			traces.add(fp.getTrace());
		}
		
		return traces;
	}

	public FittedFunction[] toArray() {
		return fittedPeakList.toArray(new FittedFunction[fittedPeakList.size()]);
	}

	public void addFittedPeak(FittedFunction fittedPeak) {
		if (fittedPeakList==null) return;
		fittedPeakList.add(fittedPeak);
	}

	public List<IPeak> getPeakFunctions() {
		
		if (fittedPeakList==null || fittedPeakList.isEmpty()) return Collections.emptyList();
		final List<IPeak> peaks = new ArrayList<IPeak>(5);
		for (FittedFunction fp : fittedPeakList) {
			peaks.add(fp.getPeak());
		}
		
		return peaks;
	}

	public void saveSelectedPeak(IPlottingSystem sys) throws Exception {
		if (selectedPeak!=null) {
			selectedPeak.setSaved(sys, true, getAllNames());
		}
	}

	public void addFittedFunctions(List<FittedFunction> peakList) {
		this.fittedPeakList.addAll(peakList);
	}

    /**
     * Gets all the active region and plot names
     * @return
     */
	private String[] getAllNames() {
		
		List<String> names = new ArrayList<String>(7);
		for (FittedFunction fp : fittedPeakList) {
			fp.getUsedNames(names);
		}
		return names.toArray(new String[names.size()]);
	}
	
	public FittedFunction getSelectedPeak() {
		return selectedPeak;
	}
}
