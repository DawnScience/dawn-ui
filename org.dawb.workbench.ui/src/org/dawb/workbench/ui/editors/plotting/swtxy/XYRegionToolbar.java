package org.dawb.workbench.ui.editors.plotting.swtxy;


import java.util.Collection;
import java.util.List;

import org.csstudio.swt.xygraph.toolbar.XYGraphConfigDialog;
import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionDialog;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XYRegionToolbar extends XYGraphToolbar {


	private static Logger logger = LoggerFactory.getLogger(XYRegionToolbar.class);
	
	private XYRegionGraph regionGraph;


	public XYRegionToolbar(XYRegionGraph xyGraph) {
		super(xyGraph);
		this.regionGraph = xyGraph;
	}

	public XYRegionToolbar(XYRegionGraph xyGraph, int flags) {
		super(xyGraph, flags);
		this.regionGraph = xyGraph;
	}
	
	public void createGraphActions(final IContributionManager tool, final IContributionManager men) {

        super.createGraphActions(tool, men);
        
        final MenuAction regionDropDown = new MenuAction("Add a selection region");
        regionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
 
		final Action addLine = new Action("Add Line Selection...", Activator.getImageDescriptor("icons/ProfileLine.png")) {
			public void run() {				
				try {
					createRegion(regionDropDown, this, RegionType.LINE);
				} catch (Exception e) {
					logger.error("Cannot create region!", e);
				}
			}
		};
		regionDropDown.add(addLine);
		
		final Action addBox = new Action("Add Box Selection...", Activator.getImageDescriptor("icons/ProfileBox.png")) {
			public void run() {				
				try {
					createRegion(regionDropDown, this, RegionType.BOX);
				} catch (Exception e) {
					logger.error("Cannot create region!", e);
				}
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

	protected void createRegion(MenuAction regionDropDown, Action action, RegionType type) throws Exception {
		
		if (xyGraph.getXAxisList().size()==1 && xyGraph.getYAxisList().size()==1) {
			final Region region = regionGraph.createRegion(getUniqueName(type.getName()), xyGraph.primaryXAxis, xyGraph.primaryYAxis, type);
			addRegion(region);
		} else {
			AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, type);
			if(dialog.open() == Window.OK){
				final Region region = dialog.getRegion();
				addRegion(region);
			}
		}
		regionDropDown.setSelectedAction(action);		
	}

	protected String getUniqueName(String base) {
		int val = 1;
		final Collection<String> regions = ((RegionArea)xyGraph.getPlotArea()).getRegionNames();
		if (regions==null) return base+" "+val;
		while(regions.contains(base+" "+val)) val++;
		return base+" "+val;
	}

	protected void addRegion(Region region) {
		((XYRegionGraph)xyGraph).addRegion(region);
		((XYRegionGraph)xyGraph).getOperationsManager().addCommand(
				new AddRegionCommand((XYRegionGraph)xyGraph, region));
	}
	
	
	protected void openConfigurationDialog() {
		XYGraphConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph);
		dialog.open();
	}
}
