package org.dawnsci.plotting.system;

import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.api.region.IRegionAction;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RegionAction extends Action implements IRegionAction{

    private static Logger logger = LoggerFactory.getLogger(RegionAction.class);
    
	private Object                 userObject;
	private LightWeightPlotActions lightWeightPlotActions;
	private XYRegionGraph          xyGraph;
	private RegionType             type;
	private MenuAction             regionDropDown;

	public RegionAction(LightWeightPlotActions lightWeightPlotActions,
			            XYRegionGraph xyGraph,
			            final RegionType type, 
                        final MenuAction regionDropDown,
			            String label, ImageDescriptor icon) {
		
		super(label, icon);
		this.lightWeightPlotActions = lightWeightPlotActions;
		this.xyGraph                = xyGraph;
		this.type                   = type;
		this.regionDropDown         = regionDropDown;
	}

	public void run() {				
		try {
			lightWeightPlotActions.createRegion(xyGraph, regionDropDown, this, type, userObject);
		} catch (Exception e) {
			logger.error("Cannot create region!", e);
		}
	}

	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

}
