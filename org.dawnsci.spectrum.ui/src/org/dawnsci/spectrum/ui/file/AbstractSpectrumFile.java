/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;

public abstract class AbstractSpectrumFile implements ISpectrumFile {

	protected boolean useAxisDataset = false;
	protected boolean showPlot = false;
	protected String xDatasetName;
	protected IPlottingSystem<?> system;
	protected List<String> yDatasetNames;
	protected Map<String, ITrace> traceMap;

	public AbstractSpectrumFile() {
		this.traceMap = new HashMap<String, ITrace>();
	}

	public void setxDatasetName(String xDatasetName) {

		if (xDatasetName == null) {
			useAxisDataset = false;
		} else {
			useAxisDataset = true;
		}

		if (this.xDatasetName != null && this.xDatasetName.equals(xDatasetName))
			return;

		this.xDatasetName = xDatasetName;

		if (!yDatasetNames.isEmpty()) {
			removeAllFromPlot();
			plotAll();
		}
	}

	@Override
	public void addyDatasetName(String name) {
		if (yDatasetNames.contains(name))
			return;
		yDatasetNames.add(name);
		if (showPlot)
			addToPlot(name);
	}

	public void removeyDatasetName(String name) {
		if (!yDatasetNames.contains(name))
			return;
		yDatasetNames.remove(name);
		removeFromPlot(name);
	}

	@Override
	public List<String> getyDatasetNames() {
		return yDatasetNames;
	}

	@Override
	public void setShowPlot(boolean showPlot) {
		this.showPlot = showPlot;
		updatePlot();
	}

	public boolean isShowPlot() {
		return showPlot;
	}

	protected abstract void addToPlot(String name);

	protected abstract String getTraceName(String name);

	protected void removeFromPlot(String name) {
		ITrace trace = traceMap.get(name);
		if (trace != null)
			system.removeTrace(trace);
		traceMap.remove(name);
		if (system.isRescale())
			system.autoscaleAxes();
	}

	public boolean isUsingAxis() {
		return useAxisDataset;
	}

	public void setUseAxis(boolean useAxis) {

		if (useAxisDataset == useAxis)
			return;

		if (xDatasetName == null) {
			useAxisDataset = false;
		} else {
			useAxisDataset = useAxis;
			updatePlot();
		}
	}

	private void updatePlot() {
		removeAllFromPlot();
		if (showPlot)
			plotAll();
	}

	public void removeAllFromPlot() {
		for (String dataset : getyDatasetNames()) {
			ITrace trace = traceMap.get(dataset);
			if (trace != null) system.removeTrace(trace);
			traceMap.remove(dataset);
		}
		if (system.isRescale())
			system.autoscaleAxes();
	}
	
	protected IDataset reduceTo1D(IDataset axis, IDataset data) {

		int matchingDim = getMatchingDim(axis, data);

		Dataset mean = DatasetUtils.convertToDataset(data);
		Dataset std = mean;
		if (axis == null || matchingDim == -1) {
			for (int i = 0; i < data.getRank() - 1; i++) {
				mean = mean.mean(0);
				std = std.stdDeviation(0);
			}
			return mean;
		} else {

			int i = data.getRank() - 1;
			for (; i >= 0; i--) {
				if (i == matchingDim) {
					continue;
				}
				mean = mean.mean(i);
				std = std.stdDeviation(i);
			}
			return mean;
		}
	}

	private int getMatchingDim(IDataset axis, IDataset data) {

		if (axis == null)
			return -1;

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

	public void setSelected(boolean selected) {

		if (selected) {
			for (ITrace trace : traceMap.values()) {
				if (trace instanceof ILineTrace) {
					((ILineTrace) trace).setLineWidth(2);
				}
			}
		} else {
			for (ITrace trace : traceMap.values()) {
				if (trace instanceof ILineTrace) {
					((ILineTrace) trace).setLineWidth(1);
				}
			}
		}

	}
}
