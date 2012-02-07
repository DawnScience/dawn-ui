package org.dawb.workbench.ui.editors.plotting.swtxy;


import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.AddRegionDialog;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionCommand;
import org.dawb.workbench.ui.editors.plotting.dialog.RemoveRegionDialog;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.Label;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
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
 
		final Action addLine = new Action("Add Line Selection...", Activator.getImageDescriptor("icons/ProfileLine.png")) {
			public void run() {
				AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, 0);
				if(dialog.open() == Window.OK){
					((XYRegionGraph)xyGraph).addRegion(dialog.getRegion());
					((XYRegionGraph)xyGraph).getOperationsManager().addCommand(
							new AddRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));
				}
				regionDropDown.setSelectedAction(this);
			}
		};
		regionDropDown.add(addLine);
		
		final Action addBox = new Action("Add Box Selection...", Activator.getImageDescriptor("icons/ProfileBox.png")) {
			public void run() {
				AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, 1);
				if(dialog.open() == Window.OK){
					((XYRegionGraph)xyGraph).addRegion(dialog.getRegion());
					((XYRegionGraph)xyGraph).getOperationsManager().addCommand(
							new AddRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));
				}
				regionDropDown.setSelectedAction(this);
			}
		};
		regionDropDown.add(addBox);
		regionDropDown.setSelectedAction(addLine);
		
		tool.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
		men.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
			
		
	}
}
