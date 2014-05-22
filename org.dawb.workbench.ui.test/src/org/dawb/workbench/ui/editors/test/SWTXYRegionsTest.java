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

import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.FileUtils;
import org.dawb.common.util.text.NumberUtils;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYRegionsTest {

	/**
	 * Looks for a large directory of images and plots them to the same plot.
	 * If directory cannot be found, test fails.
	 */
	@Test
	public void testRegionsDuringImageUpdate() throws Exception {
		
		final Object[] oa = createImagePlot();
		
		final AbstractPlottingSystem system = (AbstractPlottingSystem)oa[0];
		final File dir = (File)oa[2];
		
 		createImagePlot();
		
		final Collection<ITrace> images = system.getTraces(IImageTrace.class);
		final IImageTrace        image  = (IImageTrace)images.iterator().next();
		image.setPaletteData(PaletteFactory.makeGrayScalePalette());
		image.repaint();
		
		final IRegion lineRegion = system.createRegion("Line Profile Test", IRegion.RegionType.LINE);
		lineRegion.setROI(new LinearROI(new double[]{50,50}, new double[]{1200,1200}));
		
		system.addRegion(lineRegion);
		
		// We show the line profile tool
		system.setToolVisible("org.dawb.workbench.plotting.tools.lineProfileTool",
				              ToolPageRole.ROLE_2D, 
				              "org.dawb.workbench.plotting.views.toolPageView.2D");
		
		// We update all the images in the directory.
		final File[] fa = dir.listFiles();
		int i = 0;
		for (File file : fa) {
			if (!file.isFile()) continue;
			if (LoaderFactory.getSupportedExtensions().contains(FileUtils.getFileExtension(file))) {
				final IDataset set = LoaderFactory.getData(file.getAbsolutePath(), null).getDataset(0);
				system.updatePlot2D(set, null, null);
				
				EclipseUtils.delay(1);
				++i;
				if (i>20) break; // that's enough!
			}
		}
		
	}

	
	@Test
	public void regionPositionTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(2, 1000, "Long set "), true);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		
		final List<String> addedRegionNames = new ArrayList<String>();
		sys.addRegionListener(new IRegionListener.Stub() {		
			@Override
			public void regionAdded(RegionEvent evt) {
				addedRegionNames.add(evt.getRegion().getName());
			}
		});

		final IRegion region = sys.createRegion("Box 1", RegionType.BOX);
		
		// Give the region a position
		RectangularROI r1 = new RectangularROI(10, 0.1, 790, 0.9, 0);
//		RegionBounds r1 = new RegionBounds(new double[]{10,1},new double[]{800,0.1});
		region.setROI(r1);
		
		// We now add the region.
		sys.addRegion(region);
		
		boolean duplicatedName = false;
		try {
			sys.createRegion("Box 1", RegionType.BOX);
		} catch (Exception ne) {
			duplicatedName = true;
		}
		if (!duplicatedName)  throw new Exception("Plotting system did not identify duplicated names!");
		
		for (int i = 1; i <8; i++) {
			RectangularROI r2 = (RectangularROI) region.getROI();
			if (!equalsWithTolerance(r1, r2, 1d, 0.01))
				throw new Exception("Bounds changed after plotting - difference is " + getDiff(r1, r2));
			r1 = new RectangularROI(10+(i*10), 1d-(i/10), 800d-(100*i),0.1, 0);
		    region.setROI(r1);
		    EclipseUtils.delay(2000);
		}
				
	    
	    if (!addedRegionNames.contains("Box 1")) throw new Exception("Region list must contain 'Box 1'!");
	    
			
		EclipseUtils.getPage().closeEditor(editor, false);

	}

	private static boolean equalsWithTolerance(RectangularROI a, RectangularROI b, double dx, double dy) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		double[] a1 = a.getPointRef();
		double[] a2 = a.getEndPoint();
		double[] b1 = b.getPointRef();
		double[] b2 = b.getEndPoint();
		if (!NumberUtils.equalsTolerance(a1[0], b1[0], dx)) return false;
		if (!NumberUtils.equalsTolerance(a2[0], b2[0], dx)) return false;
		
		if (!NumberUtils.equalsTolerance(a1[1], b1[1], dy)) return false;
		if (!NumberUtils.equalsTolerance(a2[1], b2[1], dy)) return false;
		
		return true;
	}

	private static String getDiff(RectangularROI a, RectangularROI b) {
		double[] a1 = a.getPointRef();
		double[] a2 = a.getEndPoint();
		double[] b1 = b.getPointRef();
		double[] b2 = b.getEndPoint();
		return "(" + (b1[0] - a1[0]) + ", " + (b1[1] - a1[1]) + ") to (" + (b2[0] - a2[0]) + ", " + (b2[1] - a2[1]) + ")";
	}

	/**
	 * Attempts to listener to an initial region, then add other regions with different
	 * colors. This is similar to the logic required for a fitting algorithm.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void regionListenerTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(2, 1000, "Long set "), true);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];

		final List<String> addedNames = new ArrayList<String>();

		sys.addRegionListener(new IRegionListener.Stub() {		
			
			private boolean processingRegions = false;
			@Override
			public void regionAdded(RegionEvent evt) {

				if (processingRegions) return;
				
	            // Do work here to add other regions, hide the original region.
				try {
					processingRegions = true;
					
					sys.removeRegion(evt.getRegion());
					
					for (int i = 1; i <= 5; i++) {
						IRegion region;
						final String name = "Box "+i;
						try {
							region = sys.createRegion(name, RegionType.BOX);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
						
						// Give the region a position
						RectangularROI r1 = new RectangularROI(i*100, 0.1, 50, 0.9, 0);
						region.setROI(r1);
						region.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
						region.setMobile(false);
						region.setAlpha(50);
						
						// We now add the region.
						sys.addRegion(region);
						addedNames.add(name);
					}

					
				} finally {
					processingRegions = false;
				}
 			}
		});

		// We programmatically add a region, similar to if the user defined a region.
		final IRegion region = sys.createRegion("Box 1", RegionType.BOX);
		
		// Give the region a position
		RectangularROI r1 = new RectangularROI(10, 0.1, 790, 0.9, 0);
		region.setROI(r1);

		// We now add the region.
		sys.addRegion(region);

		EclipseUtils.delay(2000);
		
		for (String name : addedNames) {
			if (sys.getRegion(name)==null) throw new Exception("Plotting system does not have region "+name);
		}
	}

	@Test
	public void testRandom1() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysRandom(2, 1000), true);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		
		// We programmatically add a region, similar to if the user defined a region.
		final IRegion region = sys.createRegion("Box random", RegionType.BOX);
		
		EclipseUtils.delay(2000);

		EclipseUtils.getPage().closeEditor(editor, false);

	}



	private Object[] createImagePlot() throws Exception {
		
		// Get some kind of image directory for testing different images. 
		String imagesDirPath = System.getProperty("org.dawb.workbench.ui.editors.test.images.directory");
		if (imagesDirPath==null) imagesDirPath = "\\\\Data.diamond.ac.uk\\i03\\data\\2013\\in5790-8\\35873";
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
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)PlottingFactory.getPlottingSystem(editor.getPartName());
		
 		EclipseUtils.delay(2000); // Wait for image to be plotted...
 		
		return new Object[]{sys,editor,dir};
	}
	
	private Object[] createSomethingPlotted(final List<AbstractDataset> ys, boolean multipleAxes) throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/ascii.dat");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);
		
		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), AsciiEditor.ID);
		
		final AsciiEditor editor       = (AsciiEditor)part;
		final PlotDataEditor plotter   = (PlotDataEditor)editor.getActiveEditor();
		final IPlottingSystem sys = plotter.getPlottingSystem();
		
		//if (!(sys instanceof PlottingSystemImpl)) throw new Exception("This test is designed for "+PlottingSystemImpl.class.getName());
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);
			
		sys.clear();
		
		AbstractDataset indices = ys.get(0).getIndices();

		
		final IAxis primaryY       = sys.getSelectedYAxis();
		final IAxis alternateYaxis = multipleAxes?sys.createAxis("Alternate", true, SWT.LEFT):primaryY;
		alternateYaxis.setForegroundColor(sys.getPlotComposite().getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN));
		sys.setXFirst(true);

		sys.setSelectedYAxis(alternateYaxis);
		sys.setSelectedYAxis(primaryY);

		List<ITrace> traces = new ArrayList<ITrace>();
		for (int i = 0; i < ys.size(); i++) {

			final AbstractDataset y = ys.get(i);
			if (i%2==0) {
				sys.setSelectedYAxis(primaryY);
			} else {
				sys.setSelectedYAxis(alternateYaxis);
			}
			traces.addAll(sys.createPlot1D(indices, Arrays.asList(new IDataset[]{y}), null));

		}
		
		sys.setSelectedYAxis(primaryY);

		return new Object[]{sys,editor,traces};
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

	
	/**
	 * TODO actually test some values...
	 * @throws Throwable
	 */
	@Test
	public void regionRingsTest() throws Throwable {
		
		final Object[] oa = createSomethingPlotted(createTestArraysCoherant(2, 1000, "Long set "), true);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		
		IRegion region = sys.createRegion("Ring 1", RegionType.RING);
		
		// Give the region a position
		SectorROI r1 = new SectorROI(0.1, 0.2);
		r1.setPoint(500, 0.5);
		region.setROI(r1);
		region.setRegionColor(ColorConstants.blue);
		region.setAlpha(100);
		
		// We now add the region.
		sys.addRegion(region);
		
		// Try resizing the region
	    for (int i = 1; i < 10; i++) {
		
	    	r1.setRadii(i*50, i*50+50);
	    	region.setROI(r1);
	    	
	    	EclipseUtils.delay(500);
		}
	    
		// Try resizing the region
	    for (int i = 1; i < 10; i++) {
		
	    	r1.setRadii(i*50, i*50+5);
	    	region.setROI(r1);
	    	
	    	EclipseUtils.delay(500);
		}

		r1 = new SectorROI(0.1, 0.2);
		r1.setPoint(500, 0.5);
    	region.setROI(r1);
		
    	// Multiple rings
	    for (int i = 1; i < 10; i++) {
			
	    	region = sys.createRegion("Ring "+(i+1), RegionType.RING);
			r1 = new SectorROI(i*50, i*50+5);
			r1.setPoint(500, 0.5);
	    	r1.setRadii(i*50, i*50+5);
	    	region.setROI(r1);
			region.setRegionColor(ColorConstants.blue);
			region.setAlpha(100);
			
			// We now add the region.
			sys.addRegion(region);
	    	
	    	EclipseUtils.delay(500);
		}
	    EclipseUtils.delay(2000);
			
		EclipseUtils.getPage().closeEditor(editor, false);

	}

	@Test
	public void regionSectorTest() throws Throwable {
		
		final Object[] oa = createImagePlot();
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)oa[0];
		final IEditorPart         editor = (IEditorPart)oa[1];
		
		IRegion region = sys.createRegion("Sector 1", RegionType.SECTOR);
		
		// Give the region a position
		SectorROI r1 = new SectorROI(0.1, 0.2, 0.0, Math.PI);
		r1.setPoint(500, 0.5);
		region.setROI(r1);
		
		// We now add the region.
		sys.addRegion(region);
		
		// Try resizing the region
	    for (int i = 1; i < 10; i++) {
		
	    	r1.setRadii(i*50, i*50+250);
	    	region.setROI(r1);
	    	
	    	EclipseUtils.delay(500);
		}
	    
		r1 = new SectorROI(0.1, 0.2, 0.0, Math.PI);
		r1.setPoint(500, 0.5);
    	region.setROI(r1);
		
    	// Multiple sectors
	    for (int i = 1; i < 10; i++) {
			
	    	region = sys.createRegion("Sector "+(i+1), RegionType.SECTOR);
			r1 = new SectorROI(50, 250, 0.0, Math.PI);
			r1.setPoint(i*150, i*150+250);
	    	region.setROI(r1);
			
			// We now add the region.
			sys.addRegion(region);
	    	
	    	EclipseUtils.delay(500);
		}
	    EclipseUtils.delay(2000);
			
		EclipseUtils.getPage().closeEditor(editor, false);

	}
}
