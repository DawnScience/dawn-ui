package org.dawnsci.spectrum.ui.wizard;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ExampleDialog extends Dialog {
	
	private String[] filenames;
	
	public ExampleDialog(Shell parentShell, String[] filenames) {
		super(parentShell);
		this.filenames = filenames;
	}

	@Override
	  protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
	    container.setLayout(new GridLayout(2, false));
	    new Label(container, SWT.NONE).setText(filenames[0]);
	    ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(container, null);
	    new Label(container, SWT.NONE);
	    try {
			IPlottingSystem<Composite> system = PlottingFactory.createPlottingSystem();
			system.createPlotPart(container, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
			system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return container;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ExampleDialog");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }

}
