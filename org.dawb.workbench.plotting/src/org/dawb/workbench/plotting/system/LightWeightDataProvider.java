package org.dawb.workbench.plotting.system;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
import org.csstudio.swt.xygraph.dataprovider.IDataProviderListener;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.linearscale.Range;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public class LightWeightDataProvider implements IDataProvider {
	
	private AbstractDataset x;
	private AbstractDataset y;

	public LightWeightDataProvider() {
		
	}
	public LightWeightDataProvider(final AbstractDataset x, final AbstractDataset y) {
		this.x = x;
		this.y = y;
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
		final double xDat = x.getElementDoubleAbs(index);
		final double yDat = y.getElementDoubleAbs(index);
		return new Sample(xDat, yDat);
	}

	@Override
	public Range getXDataMinMax() {
		if (x==null) return new Range(0,100);
		try {
		    return new Range(x.min().doubleValue(), x.max().doubleValue());
		} catch (Throwable ne) {
			return new Range(0,100);
		}
	}

	@Override
	public Range getYDataMinMax() {
		if (y==null) return new Range(0,100);
		try {
			return new Range(y.min().doubleValue(), y.max().doubleValue());
		} catch (Throwable ne) {
			return new Range(0,100);
		}
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

	public void setData(AbstractDataset xData, AbstractDataset yData) {
		this.x = xData;
		this.y = yData;
		fireDataProviderListeners();
	}

	private void fireDataProviderListeners() {
		if (listeners==null) return;
		for (IDataProviderListener l : listeners) {
			l.dataChanged(this);
		}
	}

	public AbstractDataset getY() {
		return y;
	}
	
	public AbstractDataset getX() {
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
				              ? (double[])DatasetUtils.cast(x, AbstractDataset.FLOAT64).getBuffer()
		                      : new double[0];
		final double[] yArray = y!=null && y.getShape()!=null && y.getShape().length>0
	                          ? (double[])DatasetUtils.cast(y, AbstractDataset.FLOAT64).getBuffer()
                              : new double[0];
	                          
	    final double[] xa = new double[xArray.length+1];
	    System.arraycopy(xArray, 0, xa, 0, xArray.length);
	    xa[xa.length-1] = xValue.doubleValue();
	    this.x = new DoubleDataset(xa, xa.length);
	    
	    final double[] ya = new double[yArray.length+1];
	    System.arraycopy(yArray, 0, ya, 0, yArray.length);
	    ya[ya.length-1] = yValue.doubleValue();
	    this.y = new DoubleDataset(ya, ya.length);
	    
	    fireDataProviderListeners();
	}

}
