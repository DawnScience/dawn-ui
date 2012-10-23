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
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.dawb.workbench.ui.editors.PlotImageEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import fable.framework.toolbox.EclipseUtils;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYUpdateTest {
	
	/**
	 * Does update on lots of images.
	 * If directory cannot be found, test fails.
	 */
	@Test
	public void testImageUpdate() throws Exception {
		
		// Get some kind of image directory for testing different images. 
		String imagesDirPath = System.getProperty("org.dawb.workbench.ui.editors.test.images.directory");
		if (imagesDirPath==null) imagesDirPath = "C:/Work/results/ID22-ODA";
		File dir = new File(imagesDirPath);
		if (!dir.exists()) {
			// TODO check on linux
			dir = new File("/dls/sci-scratch/i12/27_nir_tomo15/sinograms");
		}
		if (!dir.exists()||!dir.isDirectory()) throw new Exception("Cannot find test folder for test!");
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
		
		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final ImageEditor editor       = (ImageEditor)page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);		
		
		final PlotImageEditor ed = editor.getPlotImageEditor();
		final AbstractPlottingSystem system = ed.getPlottingSystem();
		
		// We update all the images in the directory.
		final File[] fa = dir.listFiles();
		for (File file : fa) {
			if (!file.isFile()) continue;
			
			final AbstractDataset set = LoaderFactory.getData(file.getAbsolutePath(), null).getDataset(0);
			system.updatePlot2D(set, null, null);
			
			EclipseUtils.delay(20);
		}
		
	}
	
	/** TODO
	 * 1. Test large data.
	 * 2. Test threads.
	 * 3. Get window functionality working, monitors last 100 points for instance.
	 */

	/**
	 * Used to compare append versus replot.
	 */
	private static boolean APPEND = true;
	
	/**
	 * Unamed data sets cannot be updated
	 * @throws Throwable
	 */
	@Test
	public void testUpdateUnnamed() throws Throwable {
		
		try {
		    createUpdateTest(createTestArraysCoherant(2, 1000, null), false, 10);
		} catch (IllegalArgumentException ne) {
			return; // Everything ok
		}
		throw new Exception("Update now allowed for a nameless dataset");
	}
	
    
	@Test
	public void testUpdateChoerant() throws Throwable {
		
		createUpdateTest(createTestArraysCoherant(10, 1000, "Long set "), false, 1000);
	}

	@Test
	public void testUpdateChoerantStartSmall() throws Throwable {
		
		createUpdateTest(createTestArraysCoherant(10, 1, "Long set "), false, 100);
	}

	@Test
	public void testUpdateChoerantStartZero() throws Throwable {
		
		createUpdateTest(createTestArraysCoherant(10, 0, "Long set "), false, 100);
	}

	
	@Test
	public void testUpdateRandom() throws Throwable {
		
		createUpdateTest(createTestArraysRandom(25, 10), true, 200);
	}


	private void createUpdateTest(final List<AbstractDataset> ys, boolean isRandom, int updateAmount) throws Throwable {
		
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
		
		//if (!(sys instanceof PlottingSystemImpl)) throw new Exception("This test is designed for "+PlottingSystemImpl.class.getName());
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
				
		if (ys.get(0).getBuffer()==null || ys.get(0).getSize()<1) {
		    sys.createPlot1D(new IntegerDataset(), ys, null);
		} else {
		    sys.createPlot1D(AbstractDataset.arange(0, ys.get(0).getSize(), 1, AbstractDataset.INT32), ys, null);
		}
		EclipseUtils.delay(10);
		
		for (int total = 0; total < updateAmount; total++) {
			
			// We now loop and add a 1000 random points to each set.
			for (int i = 0; i < ys.size(); i++) {
				
				final AbstractDataset y = ys.get(i);
				
				final int index =  (ys.get(0).getBuffer()==null || ys.get(0).getSize()<1) 
						        ? total+1
						        : y.getSize()+total+1;
				
				// Adding values to AbstractDatasets is really cumbersome :(
				final double yValue;
				if (isRandom) {
					yValue = Math.round(Math.random()*10000d);
				} else {
					yValue = Math.pow(index, 2d)*i;
				}
				
				if (APPEND) { // Normally true, set APPEND to compare append vs re-plot.
					
					sys.append(y.getName(), index, yValue, null);	
					
				} else {
					final LongDataset ls  = (LongDataset)ys.get(i);
					final long[]      la  = ls.getData();
					final long[]      la2 = new long[la.length+1];
					System.arraycopy(la, 0, la2, 0, la.length);
					la2[la.length] = Math.round(yValue);
					
					final LongDataset ls2 = new LongDataset(la2, la2.length);
					ls2.setName(ls.getName());
				    ys.set(i, ls2);
				}
				
			}
			sys.repaint();

			
			if (!APPEND) {
			    sys.createPlot1D(AbstractDataset.arange(0, ys.get(0).getSize(), 1, AbstractDataset.INT32), ys, null);
			}
			
			EclipseUtils.delay(10);
		}	
		
		// Check sizes
		for (AbstractDataset a : ys) {
			final AbstractDataset set = sys.getData(a.getName());
			if (set==null) throw new Exception("There must be a data set called "+set.getName());
			try {
				if (set.getSize()!=(updateAmount+a.getSize())) {
					throw new Exception("The size must be the original size plus the update amount. Data set looks wrong: "+set);
				}
			} catch (NullPointerException ignored) {
				continue; // Some of the tests send empty data on purpose.
			}
		}
			
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
