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
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IAxis;
import org.dawb.workbench.plotting.system.LightWeightPlottingSystem;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import fable.framework.toolbox.EclipseUtils;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYAxisTest {


	@Test
	public void testChoerant1() throws Throwable {
		
		createAxisTest(createTestArraysCoherant(10, 1000, "Long set "), false);
	}

	@Test
	public void testChoerant2() throws Throwable {
		
		createAxisTest(createTestArraysCoherant(5, 1000, "Long set "), true);
	}


	@Test
	public void testRandom1() throws Throwable {
		
		createAxisTest(createTestArraysRandom(10, 1000), false);
	}

	@Test
	public void testRandom2() throws Throwable {
		
		createAxisTest(createTestArraysRandom(5, 1000), true);
	}


	private void createAxisTest(final List<AbstractDataset> ys, boolean multipleAxes) throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
		
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

		
		if (!multipleAxes) {
			final IAxis primaryY       = sys.getSelectedYAxis();
			final IAxis alternateYaxis = sys.createAxis("Alternate", true, SWT.LEFT);
			alternateYaxis.setForegroundColor(sys.getPlotComposite().getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN));
			alternateYaxis.setLog10(true);
			sys.setXfirst(true);

			sys.setSelectedYAxis(alternateYaxis);
			sys.setSelectedYAxis(primaryY);
		
			for (int i = 0; i < ys.size(); i++) {
				
				final AbstractDataset y = ys.get(i);
				if (i%2==0) {
					sys.setSelectedYAxis(primaryY);
				} else {
					sys.setSelectedYAxis(alternateYaxis);
				}
				sys.createPlot1D(indices, Arrays.asList(new AbstractDataset[]{y}), null);

			}

		} else {
			for (int i = 0; i < ys.size(); i++) {
				
				final AbstractDataset y = ys.get(i);
				
				final IAxis alternateYaxis = sys.createAxis("Alternate Y "+y.getName(), true, SWT.RIGHT);
				sys.setSelectedYAxis(alternateYaxis);
				if (i%2==0) alternateYaxis.setLog10(true);
				
				sys.createPlot1D(indices, Arrays.asList(new AbstractDataset[]{y}), null);
				sys.setXfirst(true);
				
				final Color colour = sys.get1DPlotColor(y.getName());
				if (colour!=null) alternateYaxis.setForegroundColor(colour);
		    }
		}
			
	    EclipseUtils.delay(2000);	
			
		EclipseUtils.getPage().closeEditor(editor, false);
		System.out.println("Closed: "+path);
	}

	
	private List<AbstractDataset> createTestArraysRandom(final int numberPlots, final int size) {
		
		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(numberPlots);
		for (int i = 0; i < numberPlots; i++) {
			final long[] buffer = new long[size];
			for (int j = 0; j < size; j++) buffer[j] = Math.round(Math.random()*10000);
			final LongDataset ls = new LongDataset(buffer,size);
			ls.setName("Test long set "+i);
			ys.add(ls);
		}
		return ys;
	}
	
	private List<AbstractDataset> createTestArraysCoherant(final int numberPlots, final int size, final String name) {
		
		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(numberPlots);
		for (int i = 0; i < numberPlots; i++) {
			
			double rand = Math.random();
			
			final long[] buffer = new long[size];
			for (int j = 0; j < size; j++) buffer[j] = (long)Math.pow(j+rand, 2d)*i;

			final LongDataset ls = (size>0) ? new LongDataset(buffer,size) : new LongDataset();
			if (name!=null) ls.setName(name+i);
			ys.add(ls);
		}
		
		return ys;
	}

	
}
