package org.dawb.workbench.ui.editors.test;

import org.dawb.common.ui.ServiceLoader;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.richbeans.widgets.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class SWTXYFigureTest {
	
	@Test
	public void testFigure() throws Exception {
		
		// Create a Display and Shell
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setText("Test draw2d figure");
		
		// Create Figure Canvas
		Figure container = new Figure();
		container.setFont(shell.getFont());
		org.eclipse.draw2d.GridLayout grid = new org.eclipse.draw2d.GridLayout();
		grid.setConstraint(container, new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayoutManager(grid);
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
		IDataHolder holder = ServiceLoader.getLoaderService().getData(path, new IMonitor.Stub());

		IPlottingSystem<IFigure> system = PlottingFactory.createPlottingSystem();
		system.createPlotPart(root, "Figure Plot", null, PlotType.XY, null);

		// plot an image
		system.createPlot2D(holder.getDataset(0), null, null);
		
		SWTUtils.showCenteredShell(shell);
		
	}
}
