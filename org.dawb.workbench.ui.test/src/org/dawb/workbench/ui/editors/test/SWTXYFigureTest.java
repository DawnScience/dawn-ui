package org.dawb.workbench.ui.editors.test;

import org.dawb.common.ui.ServiceLoader;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.plotting.api.EmptyWorkbenchPart;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.richbeans.widgets.util.SWTUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class SWTXYFigureTest {
	
	@Test
	public void testFigure() throws Exception {
		
		// Create a Display and Shell
		Display display = new Display();
		Shell shell = new Shell(display);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		shell.setText("Test draw2d figure");

		//toolbar
		Composite mainComp = new Composite(shell, SWT.NONE);
		mainComp.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
		mainComp.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));
		ActionBarWrapper wrapper = ActionBarWrapper.createActionBars(mainComp, null);

		// Create Figure Canvas
		Figure container = new Figure();
		container.setFont(shell.getFont());
		Canvas canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(ColorConstants.white);
		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(container);
		IFigure root = lws.getRootFigure();

		//create the plotting system
		IPlottingSystem<IFigure> system = PlottingFactory.createPlottingSystem();
		system.createPlotPart(root, "Figure Plot", wrapper, PlotType.XY, (IWorkbenchPart)new EmptyWorkbenchPart<IFigure>(system));

		// load the data
		final Bundle bun  = Platform.getBundle("org.dawb.workbench.ui.test");
		String path = (bun.getLocation() + "src/org/dawb/workbench/ui/editors/test/billeA.edf");
		path = path.substring("reference:file:".length());
		if (path.startsWith("/C:"))
		path = path.substring(1);
		IDataHolder holder = ServiceLoader.getLoaderService().getData(path, new IMonitor.Stub());

		// plot an image
		system.createPlot2D(holder.getDataset(0), null, null);

		// Add a composite to display tools on
		Composite toolComp = new Composite(shell, SWT.NONE);
		((IToolPageSystem)system.getAdapter(IToolPageSystem.class)).setToolComposite(toolComp);

		SWTUtils.showCenteredShell(shell);
	}
}
