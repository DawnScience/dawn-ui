package org.dawnsci.plotting.jreality;

import org.dawb.common.ui.plot.ActionType;
import org.dawb.common.ui.plot.IPlotActionSystem;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.ManagerType;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;


public class JRealityPlotActions {

	private JRealityPlotViewer        plotter;
	private IPlottingSystem           system;
	private IPlotActionSystem         actionMan;

	public JRealityPlotActions(JRealityPlotViewer jRealityPlotViewer,
			                   IPlottingSystem    system) {
		this.plotter            = jRealityPlotViewer;
		this.system             = system;
		this.actionMan          = system.getPlotActionSystem();
	}

	public void createActions() {
				
		// Tools
 		actionMan.createToolDimensionalActions(ToolPageRole.ROLE_3D, "org.dawb.workbench.plotting.views.toolPageView.3D");

		// Configure
        createGridLineActions();

        // Zooms
 		
 		// Others
	}
	
	private Action xCoordGrid, yCoordGrid, zCoordGrid;

	private void createGridLineActions() {
		
		actionMan.registerGroup("jreality.plotting.grid.line.actions", ManagerType.TOOLBAR);
		
		// Configure
		xCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
			}
		};
		xCoordGrid.setChecked(true);
		xCoordGrid.setText("X grid lines");
		xCoordGrid.setToolTipText("Toggle x axis grid lines");
		xCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/xgrid.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", xCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
		
		yCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
			}
		};
		yCoordGrid.setChecked(true);
		yCoordGrid.setText("Y grid lines");
		yCoordGrid.setToolTipText("Toggle y axis grid line");
		yCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/ygrid.png"));		
		actionMan.registerAction("jreality.plotting.grid.line.actions", yCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);

		zCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
				
			}
		};
		zCoordGrid.setChecked(true);
		zCoordGrid.setText("Z grid lines");
		zCoordGrid.setToolTipText("Toggle z axis grid lines");
		zCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/zgrid.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", zCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
		
	}

	public void dispose() {
		plotter            = null;
		system             = null;
	}

}
