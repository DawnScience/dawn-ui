package org.dawnsci.spectrum.ui.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ITrace;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;


public abstract class AbstractSpectrumFile implements ISpectrumFile {
	
	protected boolean useAxisDataset = false;
	protected String xDatasetName;
	protected IPlottingSystem system;
	protected List<String> yDatasetNames;
	protected Map<String,ITrace> traceMap;
	
	public AbstractSpectrumFile() {
		this.traceMap = new HashMap<String, ITrace>(); 
	}
	 
	public void setxDatasetName(String xDatasetName) {

		if (xDatasetName == null) {
			useAxisDataset = false;
		} else {
			useAxisDataset = true;
		}
		
		if (this.xDatasetName != null && this.xDatasetName.equals(xDatasetName)) return;

		this.xDatasetName = xDatasetName;

		if (!yDatasetNames.isEmpty()) {
			removeAllFromPlot();
			plotAll();
		}
	}
	
	@Override
	public void addyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.add(name);
		addToPlot(name);
	}
	
	public void removeyDatasetName(String name) {
		//TODO check contains before adding removing
		yDatasetNames.remove(name);
		removeFromPlot(name);
	}
	
	@Override
	public List<String> getyDatasetNames() {
		return yDatasetNames;
	}
	
	protected abstract void addToPlot(String name);
	

	protected abstract String getTraceName(String name);
	
	protected void removeFromPlot(String name) {
		//ITrace trace = system.getTrace(getPath() + " : " + name);
		
		ITrace trace = traceMap.get(name);
		if (trace != null) system.removeTrace(trace);
		traceMap.remove(name);
		system.autoscaleAxes();
	}

	public boolean isUsingAxis() {
		return useAxisDataset;
	}

	public void setUseAxis(boolean useAxis) {
		
		if (useAxisDataset == useAxis) return;
		
		if (xDatasetName == null) {
			useAxisDataset = false;
		} else {
			useAxisDataset = useAxis;
			updatePlot();
		}
	}
	
	private void updatePlot() {
		removeAllFromPlot();
		plotAll();
	}
	
	public void removeAllFromPlot() {
		for (String dataset : getyDatasetNames()) {
			ITrace trace = traceMap.get(dataset);
			//ITrace trace = system.getTrace(getTraceName(dataset));
			if (trace != null) system.removeTrace(trace);
			traceMap.remove(dataset);
		}
		system.autoscaleAxes();
	}
	
	protected IDataset reduceTo1D(IDataset axis, IDataset data) {
		
		int matchingDim = getMatchingDim(axis, data);
		
		if (axis == null || matchingDim == -1) {
			AbstractDataset mean = (AbstractDataset)data;
			AbstractDataset std = (AbstractDataset)data;
			for (int i = 0; i < data.getRank() -1; i++ ) {
				mean = mean.mean(0);
				std = std.stdDeviation(0);
			}
			mean.setError(std);
			return mean;
		} else {
			
			int i = data.getRank()-1 ;
			AbstractDataset mean = (AbstractDataset)data;
			AbstractDataset std = (AbstractDataset)data;
			for (; i >= 0; i--) {
				if (i == matchingDim) {
					continue;
				}
				mean = mean.mean(i);
				std = std.stdDeviation(i);
			}
			mean.setError(std);
			return mean;
		}
	}
	
	private int getMatchingDim(IDataset axis, IDataset data) {
		
		if (axis == null) return -1;
	
		int max = getMaxValue(axis.getShape());
		
		int[] shape = data.getShape();
		
		int match = -1;
		for (int i = 0; i < shape.length; i++) {
			if (shape[i] == max) {
				match = i;
			}
		}
		
		return match;
	}
	
	private int getMaxValue(int[] shape) {
		int max = 0;
		for (int i : shape) {
			if (i > max) max = i;
		}
		
		return max;
	}

}
