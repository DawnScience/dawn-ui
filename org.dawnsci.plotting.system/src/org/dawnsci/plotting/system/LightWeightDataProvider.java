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
import org.eclipse.january.dataset.DoubleDataset;
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
	private int size = 0;
	private boolean hasExtraXPoint;

	private Range cachedXRange, cachedYRange;
	private double[] positiveMins = new double[2]; // +inf indicate need to update

	public LightWeightDataProvider() {
		Arrays.fill(positiveMins, Double.POSITIVE_INFINITY);
	}

	public LightWeightDataProvider(final IDataset x, final IDataset y) {
		setDataInternal(x, y);
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public ISample getSample(int index) {
		if (x==null||y==null) return null;
		if (hasExtraXPoint && index == size-1) {
			return new Sample(x.getDouble(index), y.getDouble(index-1));
		}
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
						if (Double.isInfinite(positiveMins[idx])) {
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
		Arrays.fill(positiveMins, Double.POSITIVE_INFINITY); // reset
		this.x = DatasetUtils.convertToDataset(xData);
		this.y = DatasetUtils.convertToDataset(yData);
		ILazyDataset xel = x.getErrors();
		ILazyDataset yel = y.getErrors();
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

		hasExtraXPoint = false;
		if (y==null || y.getShape()==null) {
			size = 0;
		} else {
			size = y.getSize();
		}

		if (size > 0 && x != null) {
			if (y.getRank() == 0) {
				y.setShape(1);
			}
			if (x.getRank() == 0) {
				x.setShape(1);
			}
			if (x.getSize() == size + 1) {
				size++;
				hasExtraXPoint = true;
			}
		}
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

		final double[] xArray = x!=null && x.getShapeRef()!=null && x.getRank()>0
				              ? (double[])x.cast(DoubleDataset.class).getBuffer()
		                      : new double[0];
		final double[] yArray = y!=null && y.getShapeRef()!=null && y.getRank()>0
	                          ? (double[]) y.cast(DoubleDataset.class).getBuffer()
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
		size++;
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
