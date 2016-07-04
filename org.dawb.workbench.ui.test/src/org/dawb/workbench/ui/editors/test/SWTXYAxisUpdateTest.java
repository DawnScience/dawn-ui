/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.editors.test;

import java.io.File;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LongDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYAxisUpdateTest {
	
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
		    createUpdateTest(SWTXYTestUtils.createTestArraysCoherent(2, 1000, null), false, 10);
		} catch (IllegalArgumentException ne) {
			return; // Everything ok
		}
		throw new Exception("Update now allowed for a nameless dataset");
	}
	
    
	@Test
	public void testUpdateChoerant() throws Throwable {
		
		createUpdateTest(SWTXYTestUtils.createTestArraysCoherent(10, 1000, "Long set "), false, 1000);
	}

	@Test
	public void testUpdateChoerantStartSmall() throws Throwable {
		
		createUpdateTest(SWTXYTestUtils.createTestArraysCoherent(10, 1, "Long set "), false, 100);
	}

	@Test
	public void testUpdateChoerantStartZero() throws Throwable {
		
		createUpdateTest(SWTXYTestUtils.createTestArraysCoherent(10, 0, "Long set "), false, 100);
	}

	
	@Test
	public void testUpdateRandom() throws Throwable {
		
		createUpdateTest(SWTXYTestUtils.createTestArraysRandom(25, 10), true, 200);
	}


	private void createUpdateTest(final List<IDataset> ys, boolean isRandom, int updateAmount) throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
	
		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), AsciiEditor.ID);
		
		final AsciiEditor editor       = (AsciiEditor)part;
		final PlotDataEditor plotter   = (PlotDataEditor)editor.getActiveEditor();
		final IPlottingSystem<Composite> sys = plotter.getPlottingSystem();
		
		//if (!(sys instanceof PlottingSystemImpl)) throw new Exception("This test is designed for "+PlottingSystemImpl.class.getName());
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
			
		
		if (ys.get(0)==null || ys.get(0).getSize()<1) {
		    sys.createPlot1D(DatasetFactory.zeros(new int[0], Dataset.INT32), ys, null);
		} else {
		    sys.createPlot1D(DatasetFactory.createRange(0, ys.get(0).getSize(), 1, Dataset.INT32), ys, null);
		}
		final IAxis alternateYaxis = sys.createAxis("Alternate", true, SWT.RIGHT);
		sys.setSelectedYAxis(alternateYaxis);

		EclipseUtils.delay(10);
		
		for (int total = 0; total < updateAmount; total++) {
			
			// We now loop and add a 1000 random points to each set.
			for (int i = 0; i < ys.size(); i++) {
				
				final IDataset y = ys.get(i);
				
				final int index =  (ys.get(0)==null || ys.get(0).getSize()<1) 
						        ? total+1
						        : y.getSize()+total+1;
				
				// Adding values to Datasets is really cumbersome :(
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
					
					final Dataset ls2 = DatasetFactory.createFromObject(la2);
					ls2.setName(ls.getName());
				    ys.set(i, ls2);
				}
				
			}
			
			if (!APPEND) {
			    sys.createPlot1D(DatasetFactory.createRange(0, ys.get(0).getSize(), 1, Dataset.INT32), ys, null);
			}
			
			EclipseUtils.delay(10);
		}	
			
		EclipseUtils.getPage().closeEditor(editor, false);
		System.out.println("Closed: "+path);
	}

}
