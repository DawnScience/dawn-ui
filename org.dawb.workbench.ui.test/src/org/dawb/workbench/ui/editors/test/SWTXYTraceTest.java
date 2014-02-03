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

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.ui.editors.AsciiEditor;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.filter.IPlottingFilter;
import org.dawnsci.plotting.api.histogram.IPaletteService;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.IVectorTrace;
import org.dawnsci.plotting.api.trace.IVectorTrace.ArrowConfiguration;
import org.dawnsci.plotting.api.trace.IVectorTrace.ArrowHistogram;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

import uk.ac.diamond.scisoft.analysis.dataset.ADataset;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IErrorDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.Random;

/**
 * 
 * Testing scalability of plotting.
 * 
 * @author gerring
 *
 */
public class SWTXYTraceTest {
	
	@Test
    public void testVectorSimple1D() throws Throwable {
		
		final AbstractDataset da1 = DoubleDataset.arange(0, 100, 1);    
		
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		
		AbstractDataset vectors = AbstractDataset.zeros(new int[]{20, 20, 2}, ADataset.FLOAT32);
		
		for (int x = 0; x < 20; x++) {
			for (int y = 0; y < 20; y++) {
				vectors.set(x*100, x, y, 0); // This gets normalized later
				vectors.set(2*Math.PI*((double)x/(20d)), x, y, 1);
			}
		}
		
		final IDataset xAxis = AbstractDataset.zeros(new int[]{20}, ADataset.FLOAT32);
		final IDataset yAxis = AbstractDataset.zeros(new int[]{20}, ADataset.FLOAT32);
		for (int i = 0; i < 20; i++) {
			xAxis.set(i*5, i);
			yAxis.set(i*5, i);
		}
		
		final IVectorTrace vector = sys.createVectorTrace("vector1");
		vector.setData(vectors, Arrays.asList(xAxis, yAxis));
		vector.setArrowColor(200, 0, 0);
		vector.setArrowConfiguration(ArrowConfiguration.TO_CENTER_WITH_CIRCLE);
		vector.setCircleColor(0,200,0);
		//vector.setVectorNormalizationType(VectorNormalizationType.LOGARITHMIC);
		sys.addTrace(vector);
		
		sys.repaint();
		
		EclipseUtils.delay(2000);
		System.out.println("Passed");
	}
	
	@Test
    public void testVectorSimple2D() throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);

		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);

		EclipseUtils.delay(2000);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)PlottingFactory.getPlottingSystem(part.getTitle());
		
		AbstractDataset vectors = AbstractDataset.zeros(new int[]{20, 20, 2}, ADataset.FLOAT32);
		
		for (int x = 0; x < 20; x++) {
			for (int y = 0; y < 20; y++) {
				vectors.set(y*100, x, y, 0); // This gets normalized later
				vectors.set(2*Math.PI*((double)x/(20d)), x, y, 1);
			}
		}
		
		final IDataset xAxis = AbstractDataset.zeros(new int[]{20}, ADataset.FLOAT32);
		final IDataset yAxis = AbstractDataset.zeros(new int[]{20}, ADataset.FLOAT32);
		for (int i = 0; i < 20; i++) {
			xAxis.set(i*100, i);
			yAxis.set(i*100, i);
		}
		
		final IVectorTrace vector = sys.createVectorTrace("vector1");
		vector.setData(vectors, Arrays.asList(xAxis, yAxis));
		vector.setArrowColor(200, 0, 0);
		
		IPaletteService service = (IPaletteService)ServiceManager.getService(IPaletteService.class);
		final PaletteData   jet = service.getDirectPaletteData("NCD");
		vector.setArrowPalette(jet);
		vector.setArrowHistogram(ArrowHistogram.COLOR_BY_ANGLE);
		
		sys.addTrace(vector);
		
		sys.repaint();
		
		EclipseUtils.delay(200000);
		System.out.println("Passed");
	}


	@Test
	public void testImageNans() throws Throwable {
		testImage(Double.NaN);
	}
	public Object[] testImage(double value) throws Throwable {
		
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation()+"src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:")) path = path.substring(1);

		final IWorkbenchPage page      = EclipseUtils.getPage();		
		final IFileStore  externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part         = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);

		EclipseUtils.delay(2000);
		
		final AbstractPlottingSystem sys = (AbstractPlottingSystem)PlottingFactory.getPlottingSystem(part.getTitle());

		final Collection<ITrace>   traces = sys.getTraces(IImageTrace.class);
		IImageTrace                imt    = (IImageTrace)traces.iterator().next();
		IDataset                   data   = imt.getData();
		
		// Create a short line of invalid values...
		data = ((AbstractDataset)data).cast(AbstractDataset.FLOAT64);
		for (int i = 0; i <20; i++) {
			data.set(value, i*10,i*10);
		}
	
		System.out.println("Setting image data...");
		sys.clear();
		EclipseUtils.delay(1000);
		imt = (IImageTrace)sys.createPlot2D(data, null, null);
		
		EclipseUtils.delay(1000);
		
		return new Object[]{sys,imt}; 
		
	}
	
	@Test
    public void testErrorBarsSimple() throws Throwable {
		
		final DoubleDataset da1 = DoubleDataset.arange(0, 100, 1);
        da1.setError(5d);
        
      
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		final ILineTrace lineTrace = (ILineTrace)traces.get(0);
		if (((IErrorDataset)lineTrace.getData()).getError(50)!=5d) throw new Exception("Unexpected error!");
		
		sys.repaint();
		EclipseUtils.delay(2000);
		System.out.println("Passed");
	}
	
	@Test
    public void testFilterDectorator() throws Throwable {
		
		final IDataset y = DoubleDataset.arange(0, 100, 1);
		final IDataset x = AbstractDataset.arange(0, y.getSize(), 1, AbstractDataset.INT32);
     
      
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{y}));
		
		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		// Add a decorator that squares the data.
		IFilterDecorator dec = PlottingFactory.createFilterDecorator(sys);	
		final IPlottingFilter filter = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 1;
			}
			protected IDataset[] filter(IDataset x,    IDataset y) {
				return new IDataset[]{null, Maths.square((AbstractDataset)y)};
			}
		};
		dec.addFilter(filter);
		
		((ILineTrace)traces.get(0)).setData(x, y);
		sys.autoscaleAxes();
		
		IDataset ySquared = ((ILineTrace)traces.get(0)).getYData();
		if (ySquared.getDouble(99)!=Math.pow(99, 2)) throw new Exception("Data of plot not filtered! Value is "+ySquared.getDouble(99));
		
		EclipseUtils.delay(2000);
		System.out.println("Passed");
		
	}
	
	@Test
    public void testFilterDectoratorMultiple() throws Throwable {
		
		final IDataset y = DoubleDataset.arange(0, 100, 1);
		y.setName("Test Data");
		final IDataset x = AbstractDataset.arange(0, y.getSize(), 1, AbstractDataset.INT32);
     
      
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{y}));
		
		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		((ILineTrace)traces.get(0)).setData(x, y);
		
		// Add a decorator that squares the data.
		final IFilterDecorator dec = PlottingFactory.createFilterDecorator(sys);	
		final IPlottingFilter filter1 = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 1;
			}
			protected IDataset[] filter(IDataset x,    IDataset y) {
				y = Maths.square((AbstractDataset)y);
				y.setName("Test Data");

				return new IDataset[]{null, y};
			}
		};
		dec.addFilter(filter1);
		final IPlottingFilter filter2 = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 1;
			}
			protected IDataset[] filter(IDataset x,    IDataset y) {
				y = Maths.sqrt((AbstractDataset)y);
				y.setName("Test Data");
				return new IDataset[]{null, y};
			}
		};
		dec.addFilter(filter2);
		final IPlottingFilter filter3 = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 1;
			}
			protected IDataset[] filter(IDataset x,    IDataset y) {
				y = Maths.add((AbstractDataset)y, 10);
				y.setName("Test Data");
				return new IDataset[]{null, y};
			}
		};
		dec.addFilter(filter3);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				dec.apply();
				sys.autoscaleAxes();
			}
		});
		
		IDataset ySquared = ((ILineTrace)traces.get(0)).getYData();
		if (ySquared.getDouble(99)!=Math.pow(Math.pow(99, 2), 0.5)+10) {
			throw new Exception("Data of plot not filtered! Value is "+ySquared.getDouble(99));
		}
		
		EclipseUtils.delay(2000);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				dec.reset();
			}
		});
		
		IDataset yReset = ((ILineTrace)traces.get(0)).getYData();
		if (yReset.getDouble(99)!=99) {
			throw new Exception("Data of plot not filtered! Value is "+yReset.getDouble(99));
		}

		
		System.out.println("Passed");
		
	}

	
	@Test
    public void testFilterDectoratorDirect() throws Throwable {
		
		
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

		final IDataset y = DoubleDataset.arange(0, 100, 1);
		final IDataset x = AbstractDataset.arange(0, y.getSize(), 1, AbstractDataset.INT32);
  	
		// Add a decorator that squares the data.
		IFilterDecorator dec = PlottingFactory.createFilterDecorator(sys);	
		final IPlottingFilter filter = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 1;
			}
			protected IDataset[] filter(IDataset x,    IDataset y) {
				return new IDataset[]{null, Maths.square((AbstractDataset)y)};
			}
		};
		dec.addFilter(filter);
		
		List<ITrace> traces = sys.createPlot1D(x, Arrays.asList(y), null);
		IDataset ySquared = ((ILineTrace)traces.get(0)).getYData();
		
		EclipseUtils.delay(2000);
		
		if (ySquared.getDouble(99)!=Math.pow(99, 2)) throw new Exception("Data of plot not filtered! Value is "+ySquared.getDouble(99));
		System.out.println("Passed");
		
	}

	@Test
    public void testImageFilterDectorator() throws Throwable {
		
		final Object[] oa = testImage(0d);
		
		final IPlottingSystem     sys  = (IPlottingSystem)oa[0];
		final IImageTrace       image  = (IImageTrace)oa[1];
		
		final IDataset orginalData = image.getData();
		EclipseUtils.delay(2000);

		final IFilterDecorator dec = PlottingFactory.createFilterDecorator(sys);	
		final IPlottingFilter filter = new AbstractPlottingFilter() {
			@Override
			public int getRank() {
				return 2;
			}
			protected Object[] filter(IDataset data, List<IDataset> axes) {
				System.out.println("Processing image filter...");
				// Lets make it really noisy
				for (int i = 0; i < 10; i++) {
					data = Maths.multiply((AbstractDataset)data, Random.rand(0, 100, data.getShape()));
				}
				return new Object[]{data, axes};
			}
		};
		dec.addFilter(filter);

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				image.setData(image.getData(), image.getAxes(), true);
				image.repaint();				
			}
		});
		
		System.out.println("Plotted filter...");
		
		EclipseUtils.delay(2000);

		IDataset data = image.getData();
		if (orginalData.getDouble(500,500)==data.getByte(500,500)) {
			throw new Exception("The processed data is the same as the original data!");
		}
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
		        dec.reset();
			}
		});
		
		data = image.getData();
		if (orginalData.getDouble(500,500)!=data.getByte(500,500)) {
			throw new Exception("The reset data is not the same as the original data!");
		}

		EclipseUtils.delay(2000);

	}

	@Test
    public void testErrorBarsExponential() throws Throwable {
		
		final AbstractDataset da1 = Maths.square(DoubleDataset.arange(0, 100, 1));    
		final AbstractDataset err = Maths.square(DoubleDataset.arange(0, 100, 1).imultiply(0.2d));
		da1.setError(err);
		
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		final ILineTrace lineTrace = (ILineTrace)traces.get(0);
		double errorAt50 = ((IErrorDataset)lineTrace.getData()).getError(49);
		if (Math.round(errorAt50)!=96) throw new Exception("Incorrect error, found "+errorAt50);
		lineTrace.setErrorBarColor(ColorConstants.red);

		sys.repaint();
		EclipseUtils.delay(2000);
		System.out.println("Passed");
	}

	@Test
    public void testErrorBarsExponentialLogAxes() throws Throwable {
		
		final AbstractDataset da1 = Maths.square(DoubleDataset.arange(0, 100, 1));    
		final AbstractDataset err = Maths.square(DoubleDataset.arange(0, 100, 1).imultiply(0.2d));
		da1.setError(err);
		
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		sys.getSelectedYAxis().setLog10(true);
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		final ILineTrace lineTrace = (ILineTrace)traces.get(0);
		double errorAt50 = ((IErrorDataset)lineTrace.getData()).getError(49);
		if (Math.round(errorAt50)!=96) throw new Exception("Incorrect error, found "+errorAt50);
		lineTrace.setErrorBarColor(ColorConstants.red);

		sys.repaint();
		EclipseUtils.delay(2000);
		System.out.println("Passed");
	}

	@Test
    public void testXErrorBars() throws Throwable {
		
		final DoubleDataset da1 = DoubleDataset.arange(0, 100, 1);     
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

		final IPlottingSystem     sys    = (IPlottingSystem)oa[0];
		final List<ITrace>        traces = (List<ITrace>)oa[2];
		
		final ILineTrace lineTrace = (ILineTrace)traces.get(0);
		if (((IErrorDataset)lineTrace.getData()).getError(50)!=0d) throw new Exception("Unexpected error!");
		
		IErrorDataset es = (IErrorDataset)lineTrace.getXData();
		es.setError(4d);
		lineTrace.setData(es, lineTrace.getYData());
		
		sys.repaint();
		EclipseUtils.delay(2000);
		System.out.println("Passed");
	}


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
		final Object[] oa = createSomethingPlotted(Arrays.asList(new IDataset[]{da1}));

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

	private Object[] createSomethingPlotted(final List<IDataset> ys) throws Throwable {
		
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
		
		IDataset indices = AbstractDataset.arange(0, ys.get(0).getSize(), 1, AbstractDataset.INT32);

		List<ITrace> traces = sys.createPlot1D(indices, ys, null);

		return new Object[]{sys,editor,traces};
	}

	
	private List<IDataset> createTestArraysCoherant(final int numberPlots, final int size, final String name) {
		
		final List<IDataset> ys = new ArrayList<IDataset>(numberPlots);
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
