/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.javafx.trace;

import java.awt.image.BufferedImage;

import org.dawnsci.plotting.javafx.axis.volume.VolumeRender;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;

/**
 * 
 * @author Joel Ogden
 *
 * @Internal
 */
public class VolumeTrace  extends Image3DTrace implements IVolumeRenderTrace
{
	
	private VolumeRender volume; 
	
	public VolumeTrace(IPlottingSystemViewer plotter, String name) {
		super(plotter, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPalette(String paletteName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDataset getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(final IDataset dataset)
	{
		volume = new VolumeRender(dataset);
	}
	
	public VolumeRender getVolume()
	{
		return volume;
	}
	
}


