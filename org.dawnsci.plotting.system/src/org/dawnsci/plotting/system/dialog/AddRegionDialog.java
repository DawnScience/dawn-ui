package org.dawnsci.plotting.system.dialog;

import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class AddRegionDialog extends Dialog {
	
	private XYRegionGraph xyGraph;
	private RegionEditComposite regionComposite;
	private RegionType type;

	public AddRegionDialog(final Shell parentShell, final XYRegionGraph xyGraph, RegionType type) {
		super(parentShell);	
		
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.xyGraph = xyGraph;
        this.type = type;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Region");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite parent_composite = (Composite) super.createDialogArea(parent);
        this.regionComposite = new RegionEditComposite(parent_composite, SWT.NONE, xyGraph, type, false);
         
		return parent_composite;
	}
	
	@Override
	protected void okPressed() {	
		try {
		    region = regionComposite.createRegion();
		} catch (Exception ne) {
			MessageDialog.openError(getShell(), "Name in use", "The region cannot be created. "+ne.getMessage()+"\n\nPlease correct this or press cancel.");
			regionComposite.disposeRegion(region);
			return;
		}
		super.okPressed();
	}

	private AbstractSelectionRegion region;
	

	/**
	 * @return the annotation
	 */
	public AbstractSelectionRegion getRegion() {
		return region;
	}
}
