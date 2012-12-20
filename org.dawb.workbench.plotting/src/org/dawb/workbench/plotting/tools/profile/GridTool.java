package org.dawb.workbench.plotting.tools.profile;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool to draw and configure a grid.
 * 
 * GDA will also be able to add custom actions for running the
 * scan to this tool using the extension point for adding actions
 * to tools.
 * 
 * @author fcp94556
 *
 */
public class GridTool extends AbstractToolPage {

	private static Logger logger = LoggerFactory.getLogger(GridTool.class);
	protected Composite control;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	

	@Override
	public void createControl(Composite parent) {
		
		final Action reselect = new Action("Create new grid.", getImageDescriptor()) {
			public void run() {
				createNewRegion();
			}
		};
		
		IActionBars actionbars = getSite()!=null?getSite().getActionBars():null;
		if (actionbars != null){
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
			actionbars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
		}

		control = new Composite(parent, SWT.NONE);
		

	}
	
	@Override
	public void activate() {
		super.activate();
		createNewRegion();
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
	}
	
	private final void createNewRegion() {
		
		if (getPlottingSystem()==null) return;
		// Start with a selection of the right type
		try {
			IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
			region.setUserObject(getMarker());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
	}

	
	private Object getMarker() {
	    return getToolPageRole().getClass().getName().intern();
	}


	private String getRegionName() {
		return "Grid";
	}

	private RegionType getCreateRegionType() {
		return RegionType.GRID;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		if (control!=null && !control.isDisposed()) {
			control.setFocus();
		}
	}

}
