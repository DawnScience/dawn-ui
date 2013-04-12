/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.api.trace;

import java.util.EventObject;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Note the source to this event can either be the ITrace
 * or the plotting system.
 * 
 * @author fcp94556
 *
 */
public class TraceWillPlotEvent extends EventObject {

	public  boolean         doit = true;
	
	private IDataset image=null;
	private boolean         newImageDataSet = false;
	
	private List<IDataset> axes=null;
	private IDataset xLineData=null, yLineData=null;
	private boolean         newLineDataSet = false;
	
	private final boolean applyStraightAway;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6103365099398209061L;

	public TraceWillPlotEvent(Object source, boolean applyStraightAway) {
		super(source);
		this.applyStraightAway = applyStraightAway;
		if (source instanceof IImageTrace) {
			IImageTrace it = (IImageTrace)source;
			image = it.getData();
			axes  = it.getAxes();
		}
		
		if (source instanceof ILineTrace) {
			ILineTrace lt = (ILineTrace)source;
			this.xLineData = lt.getXData();
			this.yLineData = lt.getYData();
		}

	}

	public IDataset getImage() {
		return image;
	}

	@SuppressWarnings("unchecked")
	public void setImageData(IDataset image, List<? extends IDataset> axes) {
		this.image = image;
		this.axes = (List<IDataset>) axes;
		newImageDataSet = true;
		
		if (applyStraightAway && source instanceof IImageTrace) {
			IImageTrace it = (IImageTrace)source;
            it.setData(image, axes, false);		
		}
	}

	public void setNewImageDataSet(boolean newImageDataSet) {
		this.newImageDataSet = newImageDataSet;
	}

	public List<IDataset> getAxes() {
		return axes;
	}

	public IDataset getXData() {
		return xLineData;
	}
	public IDataset getYData() {
		return yLineData;
	}

	public void setLineData(IDataset xLineData, IDataset yLineData) {
		this.xLineData = xLineData;
		this.yLineData = yLineData;
		newLineDataSet = true;
		
		if (applyStraightAway && source instanceof ILineTrace) {
			ILineTrace lt = (ILineTrace)source;
			lt.setData(xLineData, yLineData);
		}
	}

	public boolean isNewImageDataSet() {
		return newImageDataSet;
	}

	public boolean isNewLineDataSet() {
		return newLineDataSet;
	}

	public ITrace getTrace() {
		return (ITrace)getSource();
	}

	public IImageTrace getImageTrace() {
		ITrace trace = getTrace();
		return trace instanceof IImageTrace
			   ? (IImageTrace)trace
			   : null;
	}
	

}
