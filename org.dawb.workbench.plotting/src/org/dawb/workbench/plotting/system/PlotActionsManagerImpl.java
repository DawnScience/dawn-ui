/*-
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.workbench.plotting.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.csstudio.swt.xygraph.undo.ZoomType;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.ITraceActionProvider;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingActionBarManager;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.widgets.EmptyActionBars;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages swithcing actions between different plotting system
 * modes.
 * 
 * @author fcp94556
 *
 */
public class PlotActionsManagerImpl extends PlottingActionBarManager {


	private static final Logger logger = LoggerFactory.getLogger(PlotActionsManagerImpl.class);
	
	private PlottingSystemImpl        system;
	private List<ActionContainer>     oneDimensionalActions;
	private List<ActionContainer>     twoDimensionalActions;
	private MenuAction                imageMenu;
	private MenuAction                xyMenu;

	private ITraceActionProvider traceActionProvider;

	protected PlotActionsManagerImpl(PlottingSystemImpl system) {
		super(system);
		this.system = system;
		oneDimensionalActions = new ArrayList<ActionContainer>();
		twoDimensionalActions = new ArrayList<ActionContainer>();
	}

    /**
     * 
     * @param traceActionProvider may be null
     */
	public void init(ITraceActionProvider traceActionProvider) {
		
		xyMenu =  new MenuAction("X/Y Plot");
		system.getActionBars().getMenuManager().add(xyMenu);
		system.getActionBars().getMenuManager().add(new Separator());

		imageMenu = new MenuAction("Image");
		system.getActionBars().getMenuManager().add(imageMenu);
		system.getActionBars().getMenuManager().add(new Separator());
		
		this.traceActionProvider = traceActionProvider;
	}       

	private PlotType lastPlotTypeUpdate = null;
	
	protected void switchActions(final PlotType type) {
		
		if (type == lastPlotTypeUpdate) return;
		lastPlotTypeUpdate = type;
		
		final IActionBars bars = system.getActionBars();
    	if (bars==null) return;
    	
    	imageMenu.setEnabled(type==PlotType.IMAGE);
    	xyMenu.setEnabled(type.is1D());
    	
    	if (oneDimensionalActions!=null) for (ActionContainer ac : oneDimensionalActions) {
    		if (type.is1D() && ac.getManager().find(ac.getId())==null) {
    			ac.getManager().insertAfter(ac.getGroupName(), ac.getAction());
    		} else if (!type.is1D()) {
    			ac.getManager().remove(ac.getId());
    		}
		}
    	
    	final boolean is2D = !type.is1D();
    	if (twoDimensionalActions!=null) for (ActionContainer ac : twoDimensionalActions) {
    		if (is2D && ac.getManager().find(ac.getId())==null) {
    			ac.getManager().insertAfter(ac.getGroupName(), ac.getAction());
    		} else if (!is2D) {
    			ac.getManager().remove(ac.getId());
      		}
		}
    	
    	if (bars.getToolBarManager()!=null)    bars.getToolBarManager().update(true);
    	if (bars.getMenuManager()!=null)       bars.getMenuManager().update(true);
    	if (bars.getStatusLineManager()!=null) bars.getStatusLineManager().update(true);
    	bars.updateActionBars();
    	
    	// If we are 1D we must deactivate 2D tools. If we are 
    	// 2D we must deactivate 1D tools.
    	if (type.is1D()) {
    		clearTool(ToolPageRole.ROLE_2D);
    	} else if (is2D) {
    		clearTool(ToolPageRole.ROLE_1D);
    	}
  	}
	
	public void createToolDimensionalActions(final ToolPageRole role,
			                                    final String       viewId) {

		final IActionBars bars = system.getActionBars();
		if (bars!=null) {
       	
			try {
				MenuAction toolSet = createToolActions(role, viewId);
				if (toolSet==null) return;

				final String groupName=role.getId()+".group";
				bars.getToolBarManager().add(new Separator(groupName));
				bars.getToolBarManager().insertAfter(groupName, toolSet);
				if (role.is1D()&&!role.is2D()) oneDimensionalActions.add(new ActionContainer(groupName, toolSet, bars.getToolBarManager()));
				if (role.is2D()&&!role.is1D()) twoDimensionalActions.add(new ActionContainer(groupName, toolSet, bars.getToolBarManager()));

				if (role.is2D()) {
					toolSet.addActionsTo(imageMenu);
					this.imageMenu.addSeparator();
				}
				if (role.is1D()) {
					toolSet.addActionsTo(xyMenu);
					this.xyMenu.addSeparator();
				}
	        	
			} catch (Exception e) {
				logger.error("Reading extensions for plotting tools", e);
			}
       }	
	}
	
	@Override
	public void dispose() {
		super.dispose();
			    
	    if (oneDimensionalActions!=null) oneDimensionalActions.clear();
	    oneDimensionalActions = null;
	       
	    if (twoDimensionalActions!=null) twoDimensionalActions.clear();
	    twoDimensionalActions = null;

	}
	
	

 
	private static String lastscreeshot_filename;
	/**
	 *  Create export and print buttons in tool bar 
	 */
	public void createExportActionsToolBar(IContributionManager toolbarManager) {

		if (toolbarManager==null) return;
		final MenuAction exportActionsDropDown = new MenuAction("Export/Print");

		Action exportSaveButton = new Action("Save plot screenshot as...", Activator.getImageDescriptor("icons/picture_save.png")){
			// Cache file name otherwise they have to keep
			// choosing the folder.
			public void run(){
				try {
					lastscreeshot_filename = system.savePlotting(lastscreeshot_filename);
				} catch (Exception e) {
					logger.error("Cannot save "+lastscreeshot_filename, e);
					return;
				}
				exportActionsDropDown.setSelectedAction(this);
			}
		};

		Action copyToClipboardButton = new Action("Copy to clip-board       Ctrl+C", Activator.getImageDescriptor("icons/copy_edit_on.gif")) {
			public void run() {
				system.copyPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};

		Action snapShotButton = new Action("Print plot", Activator.getImageDescriptor("icons/camera.gif")) {
			public void run(){
				system.printScaledPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};
		Action printButton = new Action("Print scaled plot         Ctrl+P", Activator.getImageDescriptor("icons/printer.png")) {
			public void run() {
				system.printPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};

		exportActionsDropDown.setImageDescriptor(Activator.getImageDescriptor("icons/printer.png"));
		exportActionsDropDown.setSelectedAction(printButton);
		exportActionsDropDown.add(exportSaveButton);
		exportActionsDropDown.add(copyToClipboardButton);
		exportActionsDropDown.addSeparator();
		exportActionsDropDown.add(snapShotButton);
		exportActionsDropDown.add(printButton);

		if (this.system.getActionBars()!=null) {
		    toolbarManager.add(exportActionsDropDown);
		}
	}

	/**
	 * Create export and print buttons in menu bar
	 */
	public void createExportActionsMenuBar() {
		
		Action exportSaveButton = new Action("Screenshot of the plot", Activator.getImageDescriptor("icons/picture_save.png")){
			// Cache file name otherwise they have to keep
			// choosing the folder.
			public void run(){
				try {
					lastscreeshot_filename = system.savePlotting(lastscreeshot_filename);
				} catch (Exception e) {
					logger.error("Cannot savePlotting to "+lastscreeshot_filename, e);
				}
			}
		};
		Action copyToClipboardButton = new Action("Copy to clip-board       Ctrl+C", Activator.getImageDescriptor("icons/copy_edit_on.gif")) {
			public void run() {
				system.copyPlotting();
			}
		};
		Action snapShotButton = new Action("Print plot", Activator.getImageDescriptor("icons/camera.gif")) {
			public void run(){
				system.printScaledPlotting();
			}
		};
		Action printButton = new Action("Print scaled plot            Ctrl+P", Activator.getImageDescriptor("icons/printer.png")) {
			public void run() {
				system.printPlotting();
			}
		};
		if (this.system.getActionBars()!=null) {
			this.system.getActionBars().getMenuManager().add(new Separator(exportSaveButton.getId()+".group"));
			this.system.getActionBars().getMenuManager().add(exportSaveButton);
			this.system.getActionBars().getMenuManager().add(copyToClipboardButton);
			this.system.getActionBars().getMenuManager().add(snapShotButton);
			this.system.getActionBars().getMenuManager().add(printButton);
			this.system.getActionBars().getMenuManager().add(new Separator(printButton.getId()+".group"));
		}
	}

	

	@Override
	public void fillZoomActions(IContributionManager man) {

		IContributionItem action = system.getActionBars().getToolBarManager().find("org.csstudio.swt.xygraph.autoscale");
		if (action!=null) man.add(((ActionContributionItem)action).getAction());

		for(final ZoomType zoomType : ZoomType.values()) {
			action = system.getActionBars().getToolBarManager().find(zoomType.getId());
			if (action!=null) man.add(((ActionContributionItem)action).getAction());
		}
	}


	@Override
	public void fillRegionActions(IContributionManager man) {
			
		IContributionItem action = system.getActionBars().getToolBarManager().find("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
		if (action!=null) man.add(((ActionContributionItem)action).getAction());
		
		action = system.getActionBars().getToolBarManager().find("org.dawb.workbench.ui.editors.plotting.swtxy.removeRegions");
		if (action!=null) man.add(((ActionContributionItem)action).getAction());
	}

	@Override
	public void fillUndoActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillPrintActions(IContributionManager man) {
		createExportActionsToolBar(man);
	}


	@Override
	public void fillAnnotationActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillToolActions(IContributionManager man, ToolPageRole role) {

        // Find the drop down action for the role.
		final IContributionItem action = system.getActionBars().getToolBarManager().find(role.getId());
		if (action!=null) man.add(((ActionContributionItem)action).getAction());
	}


	public IActionBars createEmptyActionBars() {
		return new EmptyActionBars(new ToolBarManager(), new MenuManager(), new StatusLineManager());
	}

	
	@Override
	public void remove(String id) {
        //super.remove(id);
		if (oneDimensionalActions!=null) for (Iterator<ActionContainer> it= this.oneDimensionalActions.iterator(); it.hasNext(); ) {
			ActionContainer ac = it.next();
			if (ac.getAction().getId()!=null && ac.getAction().getId().equals(id)) {
				it.remove();
				break;
			}
		}
		if (twoDimensionalActions!=null) for (Iterator<ActionContainer> it= this.twoDimensionalActions.iterator(); it.hasNext(); ) {
			ActionContainer ac = it.next();
			if (ac.getAction().getId()!=null && ac.getAction().getId().equals(id)) {
				it.remove();
				break;
			}
		}
		if (system.getActionBars()!=null) {
			system.getActionBars().getToolBarManager().remove(id);
			system.getActionBars().getMenuManager().remove(id);
			system.getActionBars().getStatusLineManager().remove(id);
		}
	}


	public IActionBars getActionBars() {
		return system.getActionBars();
	}


	public void addXYAction(IAction a) {
		xyMenu.add(a);
	}
	public void addXYSeparator() {
		xyMenu.addSeparator();
	}
	public void addImageAction(IAction a) {
		imageMenu.add(a);
	}
	public void addImageSeparator() {
		imageMenu.addSeparator();
	}

	/**
	 * Registers with the toolbar
	 * @param groupName
	 * @param action
	 * @return
	 */
	public ActionContainer register1DAction(String groupName, IAction action) {
		final IActionBars bars = getActionBars();
		final ActionContainer ac = new ActionContainer(groupName, action, bars.getToolBarManager());
		oneDimensionalActions.add(ac);
		return ac;
	}

	/**
	 * Registers with the toolbar
	 * @param groupName
	 * @param action
	 * @return
	 */
	public ActionContainer register2DAction(String groupName, IAction action) {
		final IActionBars bars = getActionBars();
		final ActionContainer ac = new ActionContainer(groupName, action, bars.getToolBarManager());
		twoDimensionalActions.add(ac);
		return ac;
	}


	@Override
	public void fillTraceActions(IContributionManager toolBarManager, ITrace trace, IPlottingSystem system) {
		if (traceActionProvider!=null) traceActionProvider.fillTraceActions(toolBarManager, trace, system);
	}

}
