package org.dawb.workbench.ui.editors.plotting.dialog;

import org.dawb.workbench.ui.editors.plotting.swtxy.RegionFigure;
import org.dawb.workbench.ui.editors.plotting.swtxy.XYRegionGraph;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class AddRegionDialog extends Dialog {
	
	private XYRegionGraph xyGraph;
	private RegionComposite regionComposite;

	public AddRegionDialog(final Shell parentShell, final XYRegionGraph xyGraph) {
		super(parentShell);	
		
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.xyGraph = xyGraph;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Region");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite parent_composite = (Composite) super.createDialogArea(parent);
        this.regionComposite = new RegionComposite(parent_composite, SWT.NONE, xyGraph);
         
		return parent_composite;
	}
	
	@Override
	protected void okPressed() {	
		region = regionComposite.createRegion();
		super.okPressed();
	}

	private RegionFigure region;
	

	/**
	 * @return the annotation
	 */
	public RegionFigure getRegion() {
		return region;
	}
}
