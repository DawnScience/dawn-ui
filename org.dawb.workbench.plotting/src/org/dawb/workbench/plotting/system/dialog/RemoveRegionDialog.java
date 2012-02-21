package org.dawb.workbench.plotting.system.dialog;

import org.dawb.workbench.plotting.system.swtxy.Region;
import org.dawb.workbench.plotting.system.swtxy.RegionArea;
import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**The dialog for removing annotation.
 * @author Xihui Chen
 * @author Kay Kasemir layout tweaks
 */
public class RemoveRegionDialog extends Dialog {
	
	private XYRegionGraph xyGraph;
	private Combo regionCombo;
	private Region removedRegion;
	
	public RemoveRegionDialog(Shell parentShell, XYRegionGraph xyGraph) {
		super(parentShell);	
		this.xyGraph = xyGraph;
        // Allow resize
        setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Remove Region");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite parent_composite = (Composite) super.createDialogArea(parent);
        final Composite composite = new Composite(parent_composite, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
        final Label removeLabel = new Label(composite, SWT.None);
        removeLabel.setLayoutData(new GridData());
        if(((RegionArea)xyGraph.getPlotArea()).getRegionMap().size() > 0){        	
	        removeLabel.setText("Select the region to be removed: ");        
	        regionCombo = new Combo(composite, SWT.DROP_DOWN);
	        regionCombo.setLayoutData(new GridData(SWT.FILL, 0, true, false));
	        for(String name : ((RegionArea)xyGraph.getPlotArea()).getRegionMap().keySet())
	        	regionCombo.add(name);
	        regionCombo.select(0);
        }else{
        	removeLabel.setText("There are no selection regions on the graph."); 
        }
        
		return parent_composite;
	}
	 
	@Override
	protected void okPressed() {
		if(regionCombo != null)
			removedRegion = ((RegionArea)xyGraph.getPlotArea()).getRegions().get(regionCombo.getSelectionIndex());
		super.okPressed();
	}
	 
	/**
	 * @return the annotation to be removed.
	 */
	public Region getRegion() {
		return removedRegion;
	}
}
