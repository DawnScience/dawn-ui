/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ILineTrace.PointStyle;
import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.workbench.plotting.system.LightWeightPlottingSystem;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import fable.framework.toolbox.EclipseUtils;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYTraceTest {
	

	@Test
	public void test1DNans() throws Throwable {

		funnyNumberTest(Double.NaN, "Nan 1");
	}
	@Test
	public void test1DPositiveInfinity() throws Throwable {
        funnyNumberTest(Double.POSITIVE_INFINITY, "Pos Infinite");
	}
	@Test
	public void test1DNegativeInfinity() throws Throwable {
        funnyNumberTest(Double.NEGATIVE_INFINITY, "Neg Infinite");
	}
	
	
	private void funnyNumberTest(double funny, String name) throws Throwable {
		final DoubleDataset da1 = DoubleDataset.arange(0, 100, 1);
		da1.set(funny, 0);
		
		da1.set(funny, 50);

		da1.setName(name);
		final Object[] oa = createSomethingPlotted(Arrays.asList(new AbstractDataset[]{da1}));

		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		final ILineTrace lineTrace = (ILineTrace)traces.get(0);
		EclipseUtils.delay(2000);
		lineTrace.setPointStyle(PointStyle.XCROSS);
		lineTrace.setPointSize(10);
		lineTrace.setTraceType(TraceType.POINT);
		
		EclipseUtils.delay(2000);
	}

	
	@Test
	public void traceTypesTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(1, 40, "Long set "));

		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		
		final ILineTrace trace = (ILineTrace)traces.get(0);
	
		trace.setLineWidth(25);
		trace.setAreaAlpha(200);		
		for (ILineTrace.TraceType type : ILineTrace.TraceType.ALL) {
			if (!type.is1D()) continue;
			trace.setTraceType(type);
			sys.repaint();				
			EclipseUtils.delay(2000);
		}
		
		
		EclipseUtils.getPage().closeEditor(editor, false);
	}
	
	@Test
	public void pointStyleTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(1, 40, "Long set "));

		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		
		final ILineTrace trace = (ILineTrace)traces.get(0);
		
		trace.setPointSize(10);
		for (ILineTrace.PointStyle style : ILineTrace.PointStyle.ALL) {
			trace.setPointStyle(style);
			sys.repaint();				
			EclipseUtils.delay(1000);
		}
		
		
		EclipseUtils.getPage().closeEditor(editor, false);
	}

	@Test
	public void errorBarsTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(1, 40, "Long set "));

		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		
		final ILineTrace trace = (ILineTrace)traces.get(0);
		
		trace.setErrorBarEnabled(true);
		trace.setErrorBarColor(ColorConstants.red);
		trace.setErrorBarWidth(20);
		for (ILineTrace.ErrorBarType type : ILineTrace.ErrorBarType.ALL) {
			trace.setXErrorBarType(type);
			sys.repaint();				
			EclipseUtils.delay(1000);
			
			trace.setYErrorBarType(type);
			sys.repaint();				
			EclipseUtils.delay(1000);
		}
		
		
		EclipseUtils.getPage().closeEditor(editor, false);
	}

	private Object[] createSomethingPlotted(final List<AbstractDataset> ys) throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
		path = path.substring("reference:file:".length());
		
		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), AsciiEditor.ID);
		
		final AsciiEditor editor       = (AsciiEditor)part;
		final PlotDataEditor plotter   = (PlotDataEditor)editor.getActiveEditor();
		final AbstractPlottingSystem sys = plotter.getPlottingSystem();
		
		if (!(sys instanceof LightWeightPlottingSystem)) throw new Exception("This test is designed for "+LightWeightPlottingSystem.class.getName());
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
			
		sys.clear();
		
		AbstractDataset indices = AbstractDataset.arange(0, ys.get(0).getSize(), 1, AbstractDataset.INT32);

		List<ITrace> traces = sys.createPlot1D(indices, ys, null);

		return new Object[]{sys,editor,traces};
	}

	
	private List<AbstractDataset> createTestArraysCoherant(final int numberPlots, final int size, final String name) {
		
		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(numberPlots);
		for (int i = 0; i < numberPlots; i++) {
			
			double rand = Math.random();
			
			final long[] buffer = new long[size];
			for (int j = 0; j < size; j++) buffer[j] = (long)Math.pow(j+rand, 2d)*(i+1);

			final LongDataset ls = (size>0) ? new LongDataset(buffer,size) : new LongDataset();
			if (name!=null) ls.setName(name+i);
			ys.add(ls);
		}
		
		return ys;
	}

	

	
}
