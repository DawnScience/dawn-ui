/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.plotting;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.gda.extensions.util.DatasetTitleUtils;
import org.dawb.workbench.ui.editors.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;


/**
 * Link between EDNA 1D plotting and JL plotter from ESRF.
 * 
 * @author gerring
 *
 */
public class JLPlottingSystem extends AbstractPlottingSystem {

	private Logger logger = LoggerFactory.getLogger(JLPlottingSystem.class);
	
	private JLChart          chart;
	private Frame            chartFrame;
	private List<JLDataView> data;
	
	public JLPlottingSystem() {
		data = new ArrayList<JLDataView>(7);
	}
	
	public void createPlotPart(final Composite      parent,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {
		
		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		swtAwtComponent.setLayout(new GridLayout());
		
		chartFrame = SWT_AWT.new_Frame(swtAwtComponent);

		chart = new JLChart();
		chartFrame.add(chart);
	}
	
	/**
	 * 
	 */
	@Override
	public void createPlot(final AbstractDataset       data, 
			               final List<AbstractDataset> axes,
			               final PlotType              mode, 
			               final IProgressMonitor      monitor) {
		
		final AbstractDataset x;
		final List<AbstractDataset> ys;
		if (axes==null) {
			ys = new ArrayList<AbstractDataset>(1);
			ys.add(data);
			x = DoubleDataset.arange(ys.get(0).getSize());
			x.setName("Index of "+data.getName());
		} else {
			x  = data;
			ys = axes;
		}

		if (mode.is1D()) {
			create1DPlot(x,ys,monitor);
		} else {

		}
	}

	private void create1DPlot(final AbstractDataset       x, 
			                  final List<AbstractDataset> ys,
			                  final IProgressMonitor      monitor) {
		
		reset();
		
		// Set Title
		chart.setHeader(DatasetTitleUtils.getTitle(x, ys, true));
		chart.getY1Axis().setAutoScale(true);
		chart.getY1Axis().setGridVisible(true);
		chart.getY1Axis().setSubGridVisible(true);
	    
		chart.getXAxis().setAutoScale(true);
		chart.getXAxis().setName(x.getName());
		chart.getXAxis().setGridVisible(true);
		chart.getXAxis().setSubGridVisible(true);
		chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);

		data.clear();
		int plotIndex = 0;
		for (AbstractDataset y : ys) {
			
		    final JLDataView dv = new JLDataView();
		    data.add(dv);
		    dv.setName(y.getName());
		    chart.getY1Axis().addDataView(dv);
		    dv.setData(getDoubleArray(x), getDoubleArray(y));
		    
		    dv.setColor(ColorUtility.getDefaultColour(plotIndex));
		    ++plotIndex;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                chart.repaint();
			}
		});

	}

	private double[] getDoubleArray(AbstractDataset x) {
		return (double[])DatasetUtils.cast(x, AbstractDataset.FLOAT64).getBuffer();
	}

	@Override
	public void reset() {
		try {
			chart.setHeader("");
			for (JLDataView view : data) {
				chart.getY1Axis().removeDataView(view);
			}
			data.clear();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
                    chart.repaint();
				}
			});
			
		} catch (Throwable e) {
			logger.error("Cannot remove plots!", e);
		}
		
	}

	@Override
	public void dispose() {
		chartFrame.dispose();	
 	}

}
