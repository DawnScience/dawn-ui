package org.dawb.workbench.ui.editors.test;

import java.io.File;
import java.util.Collection;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.workbench.ui.editors.ImageEditor;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class SWTXYFigureTest {
	
	@Test
	public void testFigure() throws Exception {
		// Create a Display and Shell
		Display display = Display.getDefault();
		Shell shell = display.getActiveShell();
		// Create Figure Canvas
		Figure container = new Figure();
		container.setFont(shell.getFont());
		XYLayout layout = new XYLayout();
		container.setLayoutManager(layout);
		// Create a canvas to display the root figure
		Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(container);
		IFigure root = lws.getRootFigure();
		
		//create the plotting system
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");

		String path = (bun.getLocation() + "src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:"))
			path = path.substring(1);

		final IWorkbenchPage page = EclipseUtils.getPage();
		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(path));
		final IEditorPart part = page.openEditor(new FileStoreEditorInput(externalFile), ImageEditor.ID);
		page.setPartState(EclipseUtils.getPage().getActivePartReference(), IWorkbenchPage.STATE_MAXIMIZED);

		EclipseUtils.delay(2000);

		IPlottingSystem<IFigure> system = PlottingFactory.getPlottingSystem(part.getTitle());
		system.createPlotPart(root, "Figure Plot", null, PlotType.XY, null);

		// plot an image
		final Collection<ITrace> traces = system.getTraces(IImageTrace.class);
		IImageTrace imt = (IImageTrace) traces.iterator().next();
		IDataset data = imt.getData();
		System.out.println("Setting image data...");
		system.clear();
		EclipseUtils.delay(1000);
		imt = (IImageTrace) system.createPlot2D(data, null, null);
		EclipseUtils.delay(1000);
	}
}
