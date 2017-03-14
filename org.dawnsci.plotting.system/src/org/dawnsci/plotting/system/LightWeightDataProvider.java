/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
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
	private double[] positiveMins = new double[2]; // zeros indicate need to update

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
		return getXDataMinMax(false);
	}

	@Override
	public Range getXDataMinMax(boolean positiveOnly) {
		return getDataMinMax(true, positiveOnly);
	}

	@Override
	public Range getYDataMinMax() {
		return getYDataMinMax(false);
	}

	@Override
	public Range getYDataMinMax(boolean positiveOnly) {
		return getDataMinMax(false, positiveOnly);
	}
	
	private Range getDataMinMax(boolean isX, boolean positiveOnly) {
		Dataset d = isX ? x : y;
		if (d == null) return new Range(0, 100);
		int idx = isX ? 0 : 1;

		try {
			Range range;
			range = isX ? cachedXRange : cachedYRange;
			if (positiveOnly || range == null) {
				double max = getMax(d);
				double min = getMin(d);
				if (positiveOnly) {
					if (min <= 0) {
						min = positiveMins[idx];
						if (positiveMins[idx] == 0) {
							min = positiveMin(d);
							positiveMins[idx] = min;
						}
					}
					if (max <= 0) {
						max = min;
					}
					range = new Range(min, max);
				} else {
					range = new Range(min, max);
					if (isX) {
						cachedXRange = range;
					} else {
						cachedYRange = range;
					}
				}
			}
			return range;
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

	private double positiveMin(Dataset a) {
		IndexIterator it = a.getIterator();
		double min = Double.POSITIVE_INFINITY; 
		while (it.hasNext()) {
			double val = a.getElementDoubleAbs(it.index);
			if (Double.isFinite(val) && val > 0) {
				if (min > val) {
					min = val;
				}
			}
		}

		return min;
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
		Arrays.fill(positiveMins, 0); // reset
		this.x = DatasetUtils.convertToDataset(xData);
		this.y = DatasetUtils.convertToDataset(yData);
		ILazyDataset xel = x.getError();
		ILazyDataset yel = y.getError();
		if (xel != null) {
			try {
				this.xerr = DatasetUtils.convertToDataset(xel.getSlice());
			} catch (DatasetException e) {
			}
		}
		if (yel != null) {
			try {
				this.yerr = DatasetUtils.convertToDataset(yel.getSlice());
			} catch (DatasetException e) {
			}
		}
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
	    double value = xValue.doubleValue();
	    if (value > 0 && positiveMins[0] > value) {
	    	positiveMins[0] = value;
	    }
	    xa[xa.length-1] = value;
	    this.x = DatasetFactory.createFromObject(xa);
	    
	    final double[] ya = new double[yArray.length+1];
	    System.arraycopy(yArray, 0, ya, 0, yArray.length);
	    value = yValue.doubleValue();
	    if (value > 0 && positiveMins[1] > value) {
	    	positiveMins[1] = value;
	    }
	    ya[ya.length-1] = value;
	    this.y = DatasetFactory.createFromObject(ya);
	    
		this.cachedXRange = null;
		this.cachedYRange = null;
	    fireDataProviderListeners();
	}

	public boolean hasErrors() {
		if (x != null && x.hasErrors())
			return true;
		if (y != null && y.hasErrors())
			return true;
		return false;
	}
}
