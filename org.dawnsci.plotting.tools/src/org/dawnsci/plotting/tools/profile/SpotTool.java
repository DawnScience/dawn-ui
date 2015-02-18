/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotTool extends ZoomTool {
	
	private static Logger logger = LoggerFactory.getLogger(SpotTool.class);

	private IPlottingSystem topSystem;
	private IPlottingSystem rightSystem;
	
	public SpotTool() {
		
		super();
		
		try {
			topSystem = PlottingFactory.createPlottingSystem();
			rightSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Cannot create plotting systems!", e);
		}
	}

	private Composite content;
	
	public void createControl(Composite parent, IActionBars actionbars) {
		
		this.content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(content);
		content.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		
		// Make some sashes
		final SashForm horiz  = new SashForm(content, SWT.VERTICAL);
		horiz.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		horiz.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));		
		final SashForm top    = new SashForm(horiz, SWT.HORIZONTAL);
		final SashForm bottom = new SashForm(horiz, SWT.HORIZONTAL);
		
		// Fill the sashes
		topSystem.createPlotPart(top, "Integration", null, PlotType.XY, getPart());
		Label label = new Label(top, SWT.NONE);
		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		super.createControl(bottom, actionbars);
		rightSystem.createPlotPart(bottom, "Integration", null, PlotType.XY, getPart());		
		
		horiz.setWeights(new int[]{30,70});
		top.setWeights(new int[]{70,30});
		bottom.setWeights(new int[]{70,30});

		topSystem.setShowLegend(false);
		rightSystem.setShowLegend(false);
		profilePlottingSystem.setShowIntensity(false);
	}
	
	public Control getControl() {
		return content;
	}

	@Override
	protected String getRegionName() {
		return "Spot";
	}
	
	public void dispose() {
		super.dispose();
		topSystem.dispose();
		rightSystem.dispose();
	}

	@Override
	protected Collection<? extends ITrace> createProfile(final IImageTrace  image, 
					            IRegion      region,
					            IROI         rbs, 
					            boolean      tryUpdate, 
					            boolean      isDrag,
					            IProgressMonitor monitor) {
		
		if (monitor.isCanceled()) return null;
		if (image==null) return null;
		
		if ((region.getRegionType()!=RegionType.BOX)&&(region.getRegionType()!=RegionType.PERIMETERBOX)) return null;

		Dataset slice = createZoom(image, region, rbs, tryUpdate, isDrag, monitor);
		
        Dataset yData = slice.sum(0);
		yData.setName("Intensity");
        Dataset xData = slice.sum(1);
        xData.setName("Intensity");
	
		final RectangularROI bounds = (RectangularROI) (rbs==null ? region.getROI() : rbs);
 		final Dataset y_indices   = AbstractDataset.arange(bounds.getPoint()[0], bounds.getPoint()[0]+bounds.getLength(0), 1, Dataset.FLOAT);
		y_indices.setName("X Location");
		
		topSystem.updatePlot1D(y_indices, Arrays.asList(new IDataset[]{yData}), monitor);
		topSystem.repaint();

		final Dataset x_indices   = AbstractDataset.arange(bounds.getPoint()[1]+bounds.getLength(1), bounds.getPoint()[1], -1, Dataset.FLOAT);
		x_indices.setName("Y Location");
	
		final Collection<ITrace> right = rightSystem.updatePlot1D(xData, Arrays.asList(new IDataset[]{x_indices}), monitor);
		rightSystem.repaint();		

		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				topSystem.setTitle("");
				rightSystem.setTitle("");
				
				ILineTrace line = (ILineTrace)right.iterator().next();
				line.setTraceColor(ColorConstants.red);
			}
		});
		
        return profilePlottingSystem.getTraces();

	}

}
