package org.dawb.workbench.ui.editors.plotting.swtxy;


import java.util.List;

import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionDialog;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class XYRegionToolbar extends XYGraphToolbar {


	public XYRegionToolbar(XYRegionGraph xyGraph) {
		super(xyGraph);
	}

	public XYRegionToolbar(XYRegionGraph xyGraph, int flags) {
		super(xyGraph, flags);
	}
	
	public void createGraphActions(final IContributionManager tool, final IContributionManager men) {

        super.createGraphActions(tool, men);
        
        final MenuAction regionDropDown = new MenuAction("Add a selection region");
        regionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
 
		final Action addLine = new Action("Add Line Selection...", Activator.getImageDescriptor("icons/ProfileLine.png")) {
			public void run() {
				
				if (xyGraph.getPlotArea().getTraceList().size()==0) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Selections must be added to plots", "Please plot something before selecting a region.");
				} if (xyGraph.getPlotArea().getTraceList().size()==1) {
					final Region region = new LineSelection(getUniqueName("Line"), xyGraph.getPlotArea().getTraceList().get(0));
					addRegion(region);
				} else {
					AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, 0);
					if(dialog.open() == Window.OK){
						final Region region = dialog.getRegion();
						addRegion(region);
					}
				}
				regionDropDown.setSelectedAction(this);
			}
		};
		regionDropDown.add(addLine);
		
		final Action addBox = new Action("Add Box Selection...", Activator.getImageDescriptor("icons/ProfileBox.png")) {
			public void run() {
				
				if (xyGraph.getPlotArea().getTraceList().size()==0) {
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Selections must be added to plots", "Please plot something before selecting a region.");
				} if (xyGraph.getPlotArea().getTraceList().size()==1) {
					final Region region = new BoxSelection(getUniqueName("Box"), xyGraph.getPlotArea().getTraceList().get(0));
					addRegion(region);
				} else {
					AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, 1);
					if(dialog.open() == Window.OK){
						final Region region = dialog.getRegion();
						addRegion(region);
					}
				}
				regionDropDown.setSelectedAction(this);
			}
		};
		regionDropDown.add(addBox);
		regionDropDown.setSelectedAction(addLine);
		
		tool.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
		men.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
			
		final Action removeRegion = new Action("Remove Region...", Activator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				RemoveRegionDialog dialog = new RemoveRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph);
				if(dialog.open() == Window.OK && dialog.getRegion() != null){
					((XYRegionGraph)xyGraph).removeRegion(dialog.getRegion());
					xyGraph.getOperationsManager().addCommand(
							new RemoveRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));					
				}
			}
		};
		
		tool.insertAfter("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions", removeRegion);
		men.insertAfter("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions", removeRegion);
	}

	protected String getUniqueName(String base) {
		int val = 1;
		final List<String> regions = ((RegionArea)xyGraph.getPlotArea()).getRegionNames();
		if (regions==null) return base+" "+val;
		while(regions.contains(base+" "+val)) val++;
		return base+" "+val;
	}

	protected void addRegion(Region region) {
		((XYRegionGraph)xyGraph).addRegion(region);
		((XYRegionGraph)xyGraph).getOperationsManager().addCommand(
				new AddRegionCommand((XYRegionGraph)xyGraph, region));
	}
}
