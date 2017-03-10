package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageGridDialog extends Dialog{
	
	private List<IDataset> data;
	private List<IPlottingSystem<Composite>> systems = new ArrayList<IPlottingSystem<Composite>>();
	
	private static Logger logger = LoggerFactory.getLogger(ImageGridDialog.class);

	public ImageGridDialog(Shell parentShell, List<IDataset> data) {
		super(parentShell);
		if (data == null || data.isEmpty()) return;
		this.data = data;
		try {
			for (int i = 0; i < data.size(); i++) {
				systems.add(PlottingFactory.createPlottingSystem(Composite.class));
			}
		} catch (Exception e) {
			logger.error("Error creating Image Grid plotting systems:", e);
		}
	}

	
	

	@Override
	public Control createDialogArea(Composite parent)  {
		Composite container = (Composite) super.createDialogArea(parent);
		Display display = Display.getDefault();
		Color white = new Color(display, 255, 255, 255);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setBackground(white);

		Composite plotsComp = new Composite(container, SWT.NONE);
		plotsComp.setLayout(new GridLayout(3, false));
		plotsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotsComp.setBackground(white);
		try {
			int i = 0;
			for (IPlottingSystem<Composite> system : systems) {
				system.createPlotPart(plotsComp, "Plot " + i, null, PlotType.IMAGE, null);
				system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//				system.updatePlot2D(data.get(i), null, null);
				MetadataPlotUtils.plotDataWithMetadata(data.get(i), system);
				i++;
			}
		} catch (Exception e) {
			logger.error("Error plotting data:", e);
			e.printStackTrace();
		}
		return container;
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
	
	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	@Override
	public boolean close() {
		for (IPlottingSystem<Composite> system : systems) if (system != null && !system.isDisposed()) system.dispose();
		return super.close();
	}
}
