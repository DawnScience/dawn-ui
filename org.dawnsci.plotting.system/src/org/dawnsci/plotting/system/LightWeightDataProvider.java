/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IErrorDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.IDataProviderListener;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ISample;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.linearscale.Range;

/**
 * A IDataProvider which uses a Dataset for its data.
 * 
 * @author Matthew Gerring
 *
 */
class LightWeightDataProvider implements IDataProvider {
	
	private Dataset x;
	private Dataset y;
	private Dataset xerr;
	private Dataset yerr;
	private Range cachedXRange, cachedYRange;

	public LightWeightDataProvider() {
		
	}
	public LightWeightDataProvider(final IDataset x, final IDataset y) {
		setDataInternal(x, y);
	}

	@Override
	public int getSize() {
		if (y==null) return 0;
		if (y.getShape()==null || y.getShape().length<1) return 0;
		return y.getSize();
	}

	@Override
	public ISample getSample(int index) {
		if (x==null||y==null) return null;
		try {
			final double xDat = x.getDouble(index);
			final double yDat = y.getDouble(index);

			final double xErr = xerr != null ? xerr.getDouble(index) : 0d;
			final double yErr = yerr != null ? yerr.getDouble(index) : 0d;

			return new Sample(xDat, yDat, yErr, yErr, xErr, xErr);
		} catch (Throwable ne) {
			return null;
		}
	}

	@Override
	public Range getXDataMinMax() {
		if (x==null) return new Range(0,100);
		if (cachedXRange!=null) return cachedXRange;
		try {
			cachedXRange= new Range(getMin(x), getMax(x));
			return cachedXRange;
		} catch (Throwable ne) {
			return new Range(0,100);
		}
	}

	@Override
	public Range getYDataMinMax() {
		if (y==null) return new Range(0,100);
		if (cachedYRange!=null) return cachedYRange;
		try {
			cachedYRange = new Range(getMin(y), getMax(y));
			return cachedYRange;
		} catch (Throwable ne) {
			return new Range(0,100);
		}
	}
	
	private double getMin(Dataset a) {
		return a.min(true).doubleValue();
	}

	private double getMax(Dataset a) {
		return a.max(true).doubleValue();
	}

	@Override
	public boolean isChronological() {
		return false;
	}
	
	private Collection<IDataProviderListener> listeners;

	/**
	 * Does nothing, data not changing!
	 */
	@Override
	public void addDataProviderListener(IDataProviderListener listener) {
		if (listeners==null) listeners = new HashSet<IDataProviderListener>();
		listeners.add(listener);
	}

	/**
	 * Does nothing, data not changing!
	 */
	@Override
	public boolean removeDataProviderListener(IDataProviderListener listener) {
		if (listeners==null) return false;
		return listeners.remove(listener);
	}

	public void setData(IDataset xData, IDataset yData) {
		setDataInternal(xData, yData);
		fireDataProviderListeners();
	}
	
	private void setDataInternal(IDataset xData, IDataset yData) {
		this.x = DatasetUtils.convertToDataset(xData);
		this.y = DatasetUtils.convertToDataset(yData);
		ILazyDataset xel = x.getError();
		ILazyDataset yel = y.getError();
		if (xel != null) this.xerr = DatasetUtils.convertToDataset(xel.getSlice());
		if (yel != null) this.yerr = DatasetUtils.convertToDataset(yel.getSlice());
		this.cachedXRange = null;
		this.cachedYRange = null;
	}

	private void fireDataProviderListeners() {
		if (listeners==null) return;
		for (IDataProviderListener l : listeners) {
			l.dataChanged(this);
		}
	}

	public Dataset getY() {
		return y;
	}
	
	public Dataset getX() {
		return x;
	}

	/**
	 * Works if x and y have not been set yet.
	 * Should be possible to make this faster by adding mode whereby viewed size is constant and
	 * append adds to end and removes from start.
	 * 
	 * @param xValue
	 * @param yValue
	 */
	public void append(Number xValue, Number yValue) {

		final double[] xArray = x!=null && x.getShape()!=null && x.getShape().length>0
				              ? (double[])DatasetUtils.cast(x, Dataset.FLOAT64).getBuffer()
		                      : new double[0];
		final double[] yArray = y!=null && y.getShape()!=null && y.getShape().length>0
	                          ? (double[])DatasetUtils.cast(y, Dataset.FLOAT64).getBuffer()
                              : new double[0];
	                          
	    final double[] xa = new double[xArray.length+1];
	    System.arraycopy(xArray, 0, xa, 0, xArray.length);
	    xa[xa.length-1] = xValue.doubleValue();
	    this.x = new DoubleDataset(xa, xa.length);
	    
	    final double[] ya = new double[yArray.length+1];
	    System.arraycopy(yArray, 0, ya, 0, yArray.length);
	    ya[ya.length-1] = yValue.doubleValue();
	    this.y = new DoubleDataset(ya, ya.length);
	    
		this.cachedXRange = null;
		this.cachedYRange = null;
	    
	    fireDataProviderListeners();
	}

	public boolean hasErrors() {
		if (x instanceof IErrorDataset && ((IErrorDataset) x).hasErrors())
			return true;
		if (y instanceof IErrorDataset && ((IErrorDataset) y).hasErrors())
			return true;
		return false;
	}
}
