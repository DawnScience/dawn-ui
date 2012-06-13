package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.annotation.IAnnotation;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.trace.ITrace;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

class FittedPeak {

	private RectangularROI roi;
	private IPeak          peak;
	private IRegion        fwhm;
	private IRegion        center;
	private ITrace         trace;
	private IAnnotation    annotation;
	private AbstractDataset[] peakFunctions;
	
	public FittedPeak(IPeak peak, RectangularROI bounds, AbstractDataset[] pf) {
		this.peak = peak;
		this.roi  = bounds;
		this.peakFunctions = pf;
	}

	public AbstractDataset[] getPeakFunctions() {
		return peakFunctions;
	}

	public void setPeakFunctions(AbstractDataset[] peakFunctions) {
		this.peakFunctions = peakFunctions;
	}

	public void dispose() {
		roi = null;
		peak = null;
		fwhm = null;
		center = null;
		trace = null;
		annotation = null;		
	}

	public void activate() {
		fwhm.setVisible(true);
		center.setVisible(true);
		trace.setVisible(true);
		annotation.setVisible(true);
	}
	
	public void deactivate() {
		fwhm.setVisible(false);
		center.setVisible(false);
		trace.setVisible(false);
		annotation.setVisible(false);
	}
	
	
	public String getPeakName() {
		try {
		    return trace.getName();
		} catch (IndexOutOfBoundsException ne) {
			return null;
		}
	}

	public double getPosition() {
		try {
			return peak.getPosition();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}
	
	public double getFWHM() {
		try {
			return peak.getFWHM();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}
	
	public double getArea() {
		try {
			return peak.getArea();
		} catch (IndexOutOfBoundsException ne) {
			return Double.NaN;
		}
	}	
	
	public String getPeakType() {
		try {
		    return peak.getClass().getSimpleName();
		} catch (IndexOutOfBoundsException ne) {
			return null;
		}
	}

	public RectangularROI getRoi() {
		return roi;
	}
	public void setRoi(RectangularROI roi) {
		this.roi = roi;
	}
	public IPeak getPeak() {
		return peak;
	}
	public void setPeak(IPeak peak) {
		this.peak = peak;
	}
	public IRegion getFwhm() {
		return fwhm;
	}
	public void setFwhm(IRegion fwhm) {
		this.fwhm = fwhm;
	}
	public IRegion getCenter() {
		return center;
	}
	public void setCenter(IRegion center) {
		this.center = center;
	}
	public ITrace getTrace() {
		return trace;
	}
	public void setTrace(ITrace trace) {
		this.trace = trace;
	}
	public IAnnotation getAnnotation() {
		return annotation;
	}
	public void setAnnotation(IAnnotation annotation) {
		this.annotation = annotation;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((center == null) ? 0 : center.hashCode());
		result = prime * result + ((fwhm == null) ? 0 : fwhm.hashCode());
		result = prime * result + ((peak == null) ? 0 : peak.hashCode());
		result = prime * result + ((roi == null) ? 0 : roi.hashCode());
		result = prime * result + ((trace == null) ? 0 : trace.hashCode());
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
		FittedPeak other = (FittedPeak) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		if (center == null) {
			if (other.center != null)
				return false;
		} else if (!center.equals(other.center))
			return false;
		if (fwhm == null) {
			if (other.fwhm != null)
				return false;
		} else if (!fwhm.equals(other.fwhm))
			return false;
		if (peak == null) {
			if (other.peak != null)
				return false;
		} else if (!peak.equals(other.peak))
			return false;
		if (roi == null) {
			if (other.roi != null)
				return false;
		} else if (!roi.equals(other.roi))
			return false;
		if (trace == null) {
			if (other.trace != null)
				return false;
		} else if (!trace.equals(other.trace))
			return false;
		return true;
	}

	public void setFWHMVisible(boolean isVis) {
		fwhm.setVisible(isVis);
	}
	public void setCenterVisible(boolean isVis) {
		center.setVisible(isVis);
	}
	public void setTraceVisible(boolean isVis) {
		trace.setVisible(isVis);
	}
	public void setAnnotationVisible(boolean isVis) {
		annotation.setVisible(isVis);
	}

	public void delete(IPlottingSystem sys) {
		sys.removeRegion(fwhm);
		sys.removeTrace(trace);
		sys.removeRegion(center);
		sys.removeAnnotation(annotation);		
	}
	
	
}
