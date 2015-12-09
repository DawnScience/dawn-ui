/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.dawnsci.plotting.system.dialog.AddAxisDialog;
import org.dawnsci.plotting.system.dialog.AddRegionDialog;
import org.dawnsci.plotting.system.dialog.RemoveAxisDialog;
import org.dawnsci.plotting.system.dialog.RemoveRegionCommand;
import org.dawnsci.plotting.system.dialog.RemoveRegionDialog;
import org.dawnsci.plotting.system.dialog.XYRegionConfigDialog;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.ActionType;
import org.eclipse.dawnsci.plotting.api.ManagerType;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.AxisUtils;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionAction;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraphFlags;
import org.eclipse.nebula.visualization.xygraph.toolbar.AddAnnotationDialog;
import org.eclipse.nebula.visualization.xygraph.toolbar.RemoveAnnotationDialog;
import org.eclipse.nebula.visualization.xygraph.toolbar.XYGraphConfigDialog;
import org.eclipse.nebula.visualization.xygraph.undo.AddAnnotationCommand;
import org.eclipse.nebula.visualization.xygraph.undo.IOperationsManagerListener;
import org.eclipse.nebula.visualization.xygraph.undo.OperationsManager;
import org.eclipse.nebula.visualization.xygraph.undo.RemoveAnnotationCommand;
import org.eclipse.nebula.visualization.xygraph.undo.ZoomType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Delegating class for light-weight actions.
 * 
 * This class creates actions for the lightweight plotting system.
 * 
 * @author Matthew Gerring
 * @param <T>
 *
 */
class LightWeightPlotActions {

	private Logger logger = LoggerFactory.getLogger(LightWeightPlotActions.class);
	
	private PlotActionsManagerImpl actionBarManager;
	private XYRegionGraph          xyGraph;
	private LightWeightPlotViewer<?>  viewer;
	private boolean                datasetChoosingRequired = true;
	private Action                 plotIndex, plotX, lockHisto;
	
	private Shell fullScreenShell;
	private IPropertyChangeListener propertyListener, switchListener;

	public void init(final LightWeightPlotViewer<?> viewer, XYRegionGraph xyGraph, PlotActionsManagerImpl actionBarManager) {
		this.viewer  = viewer;
		this.xyGraph = xyGraph;
		this.actionBarManager = actionBarManager;
	}

	public void createLightWeightActions() {
		ICommandService cmdService = null;
		try {
			cmdService = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
		} catch (IllegalStateException ie) {
			logger.error(ie.getMessage());
		}
		createActionsByExtensionPoint(cmdService);
 		createConfigActions(xyGraph);
 		createAnnotationActions(xyGraph);
 		actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_1D, "org.dawb.workbench.plotting.views.toolPageView.1D");
 		actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_2D, "org.dawb.workbench.plotting.views.toolPageView.2D");
 		//actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_1D_AND_2D, "org.dawb.workbench.plotting.views.toolPageView.1D_and_2D");
		createAxisActions(cmdService);
 		createRegionActions(xyGraph);
 		createZoomActions(xyGraph, XYGraphFlags.COMBINED_ZOOM);
 		createUndoRedoActions(xyGraph);
 		actionBarManager.createExportActions();
 		createAspectHistoAction(xyGraph);
 		actionBarManager.createPaletteActions();
 		createOriginActions(xyGraph);
 		createSpecialImageActions(xyGraph, cmdService);
 		createAdditionalActions(xyGraph, null);
 		createFullScreenActions(xyGraph);
 		
 		updateToolbarPreferences();
 		
 		this.propertyListener = new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateToolbarPreferences();
			}
		};
 		PlottingSystemActivator.getLocalPreferenceStore().addPropertyChangeListener(propertyListener);
 		
 		this.switchListener = new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				updateToolbarPreferences();
			}
		};
		actionBarManager.addPropertyChangeListener(switchListener);
		
 		createPreferencesAction(); // Must be last thing
	}

	/**
	 * Create some special image manipulation 
	 * @param xyGraph2
	 */
	private void createSpecialImageActions(XYRegionGraph xyGraph2, ICommandService cmdService) {
		final Command command = cmdService != null ? cmdService.getCommand("org.embl.cca.dviewer.phaCommand") : null;

		if (command!=null) {
			final Action action = new Action("Run PHA algorithm to highlight spots.", IAction.AS_CHECK_BOX) {
				public void run() {
					final ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, actionBarManager.getSystem());
					try {
						command.executeWithChecks(event);
					} catch (Throwable e) {
						logger.error("Cannot execute command '"+command.getId(), e);
					}
				}
			};
			action.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/pha.png"));
			
			final Action prefs = new Action("PHA Preferences...") {
				public void run() {
					try {
						PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.embl.cca.dviewer.rcp.preference.pha", null, null);
						if (pref != null) pref.open();
					} catch(IllegalStateException e) {
						logger.error(e.getMessage());
					}
				}
			};
			prefs.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/pha-preferences.png"));
	        
			actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.SPECIALS.getId());
	      	actionBarManager.registerAction(ToolbarConfigurationConstants.SPECIALS.getId(), action, ActionType.IMAGE, ManagerType.TOOLBAR);
	      	actionBarManager.registerAction(ToolbarConfigurationConstants.SPECIALS.getId(), prefs, ActionType.IMAGE, ManagerType.TOOLBAR);
		}
	}

	/**
	 * Reads any extended actions
	 */
	private void createActionsByExtensionPoint(ICommandService cmdService) {
		final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.plotting.api.plottingAction");
	    if (eles==null) return;
	    
	    for (IConfigurationElement ie : eles) {
	    	
			final String name = ie.getAttribute("plot_name");
			if (name!=null && !name.equals(actionBarManager.getSystem().getPlotName())) continue;
			
			final String commandId = ie.getAttribute("command_id");
			final Command command = cmdService != null ? cmdService.getCommand(commandId) : null;
			final String action_id = ie.getAttribute("id");
			if (command==null) {
				logger.error("Cannot find command '"+commandId+"' in plot action "+action_id);
				continue;
			}
			
			final String iconPath = ie.getAttribute("icon");
			ImageDescriptor icon=null;
	    	if (iconPath!=null) {
		    	final String   id    = ie.getContributor().getName();
		    	final Bundle   bundle= Platform.getBundle(id);
		    	final URL      entry = bundle.getEntry(iconPath);
		    	icon = ImageDescriptor.createFromURL(entry);
	    	}
	    	String label = ie.getAttribute("label");
	    	if (label==null||"".equals(label)) {
	    		try {
	    			label = command.getName();
	    		} catch (Throwable ne) {
		    		try {
	    			    label = command.getDescription();
		    		} catch (Throwable neOther) {
		    			label = "Unknown command";
		    		}
	    		}
	    	}
	    	
			final Action action = new Action(label) {
				public void run() {
					final ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, actionBarManager.getSystem());
					try {
						command.executeWithChecks(event);
					} catch (Throwable e) {
						logger.error("Cannot execute command '"+command.getId()+" from action "+action_id, e);
					}
				}
			};
			if (icon!=null) action.setImageDescriptor(icon);
			
	    	String type = ie.getAttribute("action_type");
	    	ManagerType manType;
            if (type!=null && !"".equals(type) && "MENUBAR".equals(type)) {
            	manType = ManagerType.MENUBAR;
            } else {
            	manType = ManagerType.TOOLBAR;
            }
            
            ActionType actionType = ActionType.ALL;
            type = ie.getAttribute("plot_type");
            if (type!=null) {
            	for (ActionType at : ActionType.values()) {
					if (at.toString().equals(type)) {
						actionType = at;
						break;
					}
				}
            }
			
        	actionBarManager.registerAction(action, actionType, manType);

		}
	}

	private void createPreferencesAction() {
		actionBarManager.registerMenuBarGroup("org.dawnsci.plotting.system.toolbar.preferences");		
		final Action openPreferences = new Action("Toolbar Preferences...") {
			public void run() {
				try {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "org.dawnsci.plotting.system.toolbarPreferencePage", null, null);
					if (pref != null) pref.open();
				} catch (IllegalStateException e) {
					logger.error(e.getMessage());
				}
			}
		};
		actionBarManager.registerAction("org.dawnsci.plotting.system.toolbar.preferences", openPreferences, ActionType.ALL, ManagerType.MENUBAR);
	}

	private void updateToolbarPreferences() {
		
		final Map<String, Boolean> visMap = new HashMap<String,Boolean>(ToolbarConfigurationConstants.values().length);
		
		for (ToolbarConfigurationConstants constant : ToolbarConfigurationConstants.values()) {
			final boolean isVisible = PlottingSystemActivator.getLocalPreferenceStore().getBoolean(constant.getId());
			visMap.put(constant.getId(), isVisible);	
		}
		
		actionBarManager.updateGroupVisibility(visMap);
	}

	private void createAxisActions(final ICommandService cmdService) {
		
		final Action createAxis = new Action("Create Axis...") {
			public void run() {
				AddAxisDialog addAxis = new AddAxisDialog(Display.getDefault().getActiveShell(), viewer.getSystem());
				int ok = addAxis.open();
				if (ok == Dialog.OK) {
					viewer.getSystem().createAxis(addAxis.getTitle(), addAxis.isY(), addAxis.getSide());
				}
			}
		};
		actionBarManager.addXYAction(createAxis);
		
		final Action deleteAxis = new Action("Remove Axis...") {
			public void run() {
				List<IAxis> axes = AxisUtils.getUserAxes(viewer.getSystem());
				if (axes==null || axes.isEmpty()) {
					MessageDialog.openWarning(Display.getDefault().getActiveShell(), "No Axes", "There are no axes currently available to delete.");
					return;
				}
				RemoveAxisDialog delAxis = new RemoveAxisDialog(Display.getDefault().getActiveShell(), viewer.getSystem());
				int ok = delAxis.open();
				if (ok == Dialog.OK) {
					viewer.getSystem().removeAxis(delAxis.getAxis());
				}
				
			}
		};
		
		final IAxis yAxis = getCurrentYAxis();
		final Action logY = new Action("Log Y", IAction.AS_CHECK_BOX) {
			public void run() {
				final Command command = cmdService != null ? cmdService.getCommand("org.dawnsci.plotting.system.logYToggle") : null;
				if (command == null)
					return;
				final ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, actionBarManager.getSystem());
				try {
					boolean success = (Boolean) command.executeWithChecks(event);
					if (success) {
						this.setChecked(yAxis.isLog10());
					}
				} catch (ExecutionException|NotDefinedException|NotEnabledException|NotHandledException e) {
					logger.error("Exception while attempting to toggle log on Y axis", e);
				}
			}
		};
		
		final IAxis xAxis = getCurrentXAxis();
		final Action logX = new Action("Log X", IAction.AS_CHECK_BOX) {
			public void run() {
				final Command command = cmdService != null ? cmdService.getCommand("org.dawnsci.plotting.system.logXToggle") : null;
				if (command == null)
					return;
				final ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, actionBarManager.getSystem());
				try {
					boolean success = (Boolean) command.executeWithChecks(event);
					if (success) {
						this.setChecked(xAxis.isLog10());
					}
				} catch (ExecutionException|NotDefinedException|NotEnabledException|NotHandledException e) {
					logger.error("Exception while attempting to toggle log on X axis", e);
				}
			}
		};
		
		viewer.getSystem().getAxis(xAxis.getTitle()).addAxisListener(new IAxisListener() {
			
			@Override
			public void revalidated(AxisEvent evt) {
				if (logX.isChecked() != xAxis.isLog10()) {
					logX.setChecked(xAxis.isLog10());
				}
			}
			
			@Override
			public void rangeChanged(AxisEvent evt) {
				// not required
			}
		});
		
		viewer.getSystem().getAxis(yAxis.getTitle()).addAxisListener(new IAxisListener() {
			
			@Override
			public void revalidated(AxisEvent evt) {
				if (logY.isChecked() != yAxis.isLog10()) {
					logY.setChecked(yAxis.isLog10());
				}
			}
			
			@Override
			public void rangeChanged(AxisEvent evt) {
				// not required
			}
		});
		
		actionBarManager.addXYAction(deleteAxis);
		actionBarManager.addXYSeparator();
		actionBarManager.addXYAction(logX);
		actionBarManager.addXYAction(logY);
		actionBarManager.addXYSeparator();
	}

	public void createUndoRedoActions(final XYRegionGraph xyGraph) {
		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.UNDO.getId());		
		
		//undo button		
		final Action undoButton = new Action("Undo", PlottingSystemActivator.getImageDescriptor("icons/Undo.png")) {
			public void run() {
				xyGraph.getOperationsManager().undo();
			}
		};
		undoButton.setEnabled(false);
		actionBarManager.registerAction(ToolbarConfigurationConstants.UNDO.getId(), undoButton, ActionType.XYANDIMAGE);		
	
		xyGraph.getOperationsManager().addListener(new IOperationsManagerListener(){
			public void operationsHistoryChanged(OperationsManager manager) {
				if(manager.getUndoCommandsSize() > 0){
					undoButton.setEnabled(true);
					final String cmd_name = manager.getUndoCommands()[
					           manager.getUndoCommandsSize() -1].toString();
                    undoButton.setText(NLS.bind("Undo {0}", cmd_name));
				}else{
					undoButton.setEnabled(false);
					undoButton.setText("Undo");
				}			
			}
		});
		
		// redo button
		final Action redoButton = new Action("Redo", PlottingSystemActivator.getImageDescriptor("icons/Redo.png")) {
			public void run() {
				xyGraph.getOperationsManager().redo();
			}
		};
		redoButton.setEnabled(false);
		actionBarManager.registerAction(ToolbarConfigurationConstants.UNDO.getId(), redoButton, ActionType.XYANDIMAGE);		

		xyGraph.getOperationsManager().addListener(new IOperationsManagerListener(){
			public void operationsHistoryChanged(OperationsManager manager) {
				if(manager.getRedoCommandsSize() > 0){
					redoButton.setEnabled(true);
					final String cmd_name = manager.getRedoCommands()[
					           manager.getRedoCommandsSize() -1].toString();
                    redoButton.setText(NLS.bind("Redo {0}", cmd_name));
				}else{
					redoButton.setEnabled(false);
					redoButton.setText("Redo");
				}					
			}
		});
	}

	public void createConfigActions(final XYRegionGraph xyGraph) {
		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.CONFIG.getId());	
		
		final Action configButton = new Action(BasePlottingConstants.CONFIG_SETTINGS, PlottingSystemActivator.getImageDescriptor("icons/Configure.png")) {
			public void run() {
				XYGraphConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph, viewer.getSystem().isRescale());
				dialog.open();
			}
		};
		configButton.setToolTipText("Configure Settings...");
		actionBarManager.registerAction(ToolbarConfigurationConstants.CONFIG.getId(), configButton, ActionType.XYANDIMAGE);		
		
		final Action showLegend = new Action(BasePlottingConstants.XY_SHOWLEGEND, IAction.AS_CHECK_BOX) {
			public void run() {
				xyGraph.setShowLegend(!xyGraph.isShowLegend());
			}
		};
		showLegend.setToolTipText("Show Legend");
		showLegend.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/ShowLegend.png"));
		actionBarManager.registerAction(ToolbarConfigurationConstants.CONFIG.getId(), showLegend, ActionType.XY);		
		
		showLegend.setChecked(xyGraph.isShowLegend());
		
	}
	
	protected void createAnnotationActions(final XYRegionGraph xyGraph) {
		
		actionBarManager.registerMenuBarGroup(ToolbarConfigurationConstants.ANNOTATION.getId());	
		
		final Action addAnnotation = new Action("Add Annotation...", PlottingSystemActivator.getImageDescriptor("icons/Add_Annotation.png")) {
			public void run() {
				AddAnnotationDialog dialog = new AddAnnotationDialog(Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK){
					xyGraph.addAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new AddAnnotationCommand(xyGraph, dialog.getAnnotation()));
				}
				
			}
		};
		actionBarManager.registerAction(ToolbarConfigurationConstants.ANNOTATION.getId(), addAnnotation, ActionType.XYANDIMAGE, ManagerType.MENUBAR);		
	
		
		final Action delAnnotation = new Action("Remove Annotation...", PlottingSystemActivator.getImageDescriptor("icons/Del_Annotation.png")) {
			public void run() {
				RemoveAnnotationDialog dialog = new RemoveAnnotationDialog(Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK && dialog.getAnnotation() != null){
					xyGraph.removeAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new RemoveAnnotationCommand(xyGraph, dialog.getAnnotation()));					
				}
				
			}
		};
		actionBarManager.registerAction(ToolbarConfigurationConstants.ANNOTATION.getId(), delAnnotation, ActionType.XYANDIMAGE, ManagerType.MENUBAR);		
		
		//actionBarManager.registerToolBarGroup("org.csstudio.swt.xygraph.toolbar.extra");	
	}
	
	protected void createRegionActions(final XYRegionGraph xyGraph) {
		
		
		
        final MenuAction regionDropDown = new MenuAction("Selection region");
        regionDropDown.setId(BasePlottingConstants.ADD_REGION); // Id used elsewhere...
 
		regionDropDown.add(createRegionAction(xyGraph, RegionType.LINE,       regionDropDown, "Line selection",     PlottingSystemActivator.getImageDescriptor("icons/ProfileLine.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POLYLINE,   regionDropDown, "Polyline selection", PlottingSystemActivator.getImageDescriptor("icons/ProfilePolyline.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POLYGON,    regionDropDown, "Polygon selection",  PlottingSystemActivator.getImageDescriptor("icons/ProfilePolygon.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.BOX,        regionDropDown, "Box selection",      PlottingSystemActivator.getImageDescriptor("icons/ProfileBox.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.PERIMETERBOX,   regionDropDown, "Perimeter box selection",PlottingSystemActivator.getImageDescriptor("icons/ProfileColorbox.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.GRID,       regionDropDown, "Grid selection",     PlottingSystemActivator.getImageDescriptor("icons/ProfileGrid.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.SECTOR,     regionDropDown, "Sector selection",   PlottingSystemActivator.getImageDescriptor("icons/ProfileSector.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.RING,       regionDropDown, "Ring selection",     PlottingSystemActivator.getImageDescriptor("icons/ProfileCircle.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.XAXIS,      regionDropDown, "X-axis selection",   PlottingSystemActivator.getImageDescriptor("icons/Cursor-horiz.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.YAXIS,      regionDropDown, "Y-axis selection",   PlottingSystemActivator.getImageDescriptor("icons/Cursor-vert.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.FREE_DRAW,  regionDropDown, "Free drawn selection",   PlottingSystemActivator.getImageDescriptor("icons/ProfileFree.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POINT,      regionDropDown, "Single point selection", PlottingSystemActivator.getImageDescriptor("icons/ProfilePoint.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.CIRCLE,     regionDropDown, "Circle selection",   PlottingSystemActivator.getImageDescriptor("icons/ProfileCircle.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.CIRCLEFIT,  regionDropDown, "Circle fit selection",   PlottingSystemActivator.getImageDescriptor("icons/ProfileCircle.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.ELLIPSE,    regionDropDown, "Ellipse selection",  PlottingSystemActivator.getImageDescriptor("icons/ProfileEllipse.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.ELLIPSEFIT, regionDropDown, "Ellipse fit selection",  PlottingSystemActivator.getImageDescriptor("icons/ProfileEllipse.png")));
		
		actionBarManager.registerAction(regionDropDown, ActionType.XYANDIMAGE, ManagerType.MENUBAR);
			
        final MenuAction removeRegionDropDown = new MenuAction("Delete selection region(s)");
        removeRegionDropDown.setId(BasePlottingConstants.REMOVE_REGION);

        final Action removeRegion = new Action("Remove Region...", PlottingSystemActivator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				RemoveRegionDialog dialog = new RemoveRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph);
				if(dialog.open() == Window.OK && dialog.getRegion() != null){
					((XYRegionGraph)xyGraph).removeRegion(dialog.getRegion());
					xyGraph.getOperationsManager().addCommand(
							new RemoveRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));					
				}
			}
		};
		
		removeRegionDropDown.add(removeRegion);
		removeRegionDropDown.setSelectedAction(removeRegion);
		
        final Action removeAllRegions = new Action("Remove all regions...", PlottingSystemActivator.getImageDescriptor("icons/RegionDeleteAll.png")) {
			public void run() {
				
				final boolean yes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), 
						                  "Please Confirm Delete All",
						                  "Are you sure you would like to delete all selection regions?");
				
				if (yes){
					xyGraph.getOperationsManager().addCommand(
							new RemoveRegionCommand((XYRegionGraph)xyGraph, ((XYRegionGraph)xyGraph).getRegions()));					
					((XYRegionGraph)xyGraph).clearRegions(false);
				}
			}
		};
		
		removeRegionDropDown.add(removeAllRegions);

		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.REGION.getId());
		actionBarManager.registerAction(ToolbarConfigurationConstants.REGION.getId(), removeRegionDropDown, ActionType.XYANDIMAGE);
		
	}
	
	protected void createRegion(final XYRegionGraph xyGraph, 
			                    MenuAction regionDropDown, 
			                    Action action, 
			                    RegionType type,
			                    Object userObject) throws Exception {
		
		// There is just an x and y axis - VISIBLE - then we know which axes they intended.
		// Otherwise we show the dialog
		List<Axis> visX = getVisibleAxisList(xyGraph.getXAxisList());
		List<Axis> visY = getVisibleAxisList(xyGraph.getYAxisList());
		if (visX.size()==1 && visY.size()==1) {
			AbstractSelectionRegion<?> region = xyGraph.createRegion(RegionUtils.getUniqueName(type.getName(), viewer.getSystem()), viewer.getSelectedXAxis(), viewer.getSelectedYAxis(), type, true);
			// Set the plottype to know which plot type the region was created with
			region.setPlotType(viewer.getSystem().getPlotType());
			if (userObject!=null) region.setUserObject(userObject);
			
		} else {
			AddRegionDialog dialog = new AddRegionDialog(viewer.getSystem(), Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, type);
			if (dialog.open() != Window.OK){
				return;
			}
		}
		//regionDropDown.setSelectedAction(action);	
		//regionDropDown.setChecked(true);
	}
	
	private List<Axis> getVisibleAxisList(List<Axis> axisList) {
		List<Axis> ret = new ArrayList<Axis>(3);
		for (Axis iAxis : axisList) if (iAxis.isVisible()) ret.add(iAxis);
		return ret;
	}

	private IRegionAction createRegionAction(final XYRegionGraph xyGraph, 
			                                 final RegionType type, 
			                                 final MenuAction regionDropDown, 
			                                 final String label, 
			                                 final ImageDescriptor icon) {
		
		final RegionAction regionAction = new RegionAction(this, xyGraph, type, regionDropDown, label, icon);
		regionAction.setId(type.getId());
		return regionAction;
	}

	
	public void createZoomActions(final XYRegionGraph xyGraph, final int flags) {
		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.ZOOM.getId());		

        final Action autoScale = new Action("Perform Auto Scale", PlottingSystemActivator.getImageDescriptor("icons/AutoScale.png")) {
        	public void run() {
	            xyGraph.performAutoScale();
        	}
        };
        autoScale.setId(BasePlottingConstants.AUTO_SCALE);
        actionBarManager.registerAction(ToolbarConfigurationConstants.ZOOM.getId(), autoScale, ActionType.XYANDIMAGE);
        
        final CheckableActionGroup zoomG = new CheckableActionGroup();
        final MenuAction zoomMenu = new MenuAction("Zoom Types");
 		
        Action rubberBand = null;
        for(final ZoomType zoomType : ZoomType.values()){
		    if (! zoomType.useWithFlags(flags)) continue;
		    if (!zoomType.isZoom()) continue;
		 		
			final ImageDescriptor icon = new ImageDescriptor() {				
				@Override
				public ImageData getImageData() {
					return zoomType.getIconImage().getImageData();
				}
			};
			final Action zoomAction = new Action(zoomType.getDescription(), IAction.AS_PUSH_BUTTON) {
				public void run() {
					xyGraph.setZoomType(zoomType);
					zoomMenu.setSelectedAction(this);
					zoomMenu.setId(zoomType.getId());
				}
			};
			zoomAction.setImageDescriptor(icon);
			zoomAction.setId(zoomType.getId());
			zoomG.add(zoomAction);
			zoomMenu.add(zoomAction);
			if (zoomType==ZoomType.RUBBERBAND_ZOOM) rubberBand = zoomAction;
		}
		if (rubberBand!=null) {
			zoomMenu.setSelectedAction(rubberBand);
			zoomMenu.setId(rubberBand.getId());
		}
        actionBarManager.registerAction(ToolbarConfigurationConstants.ZOOM.getId(), zoomMenu, ActionType.XYANDIMAGE);
		
        Action none = null;
        for(final ZoomType zoomType : ZoomType.values()){
		    if (! zoomType.useWithFlags(flags)) continue;
		    if (zoomType.isZoom()) continue;
		 		
			final ImageDescriptor icon = new ImageDescriptor() {				
				@Override
				public ImageData getImageData() {
					return zoomType.getIconImage().getImageData();
				}
			};
			final Action zoomAction = new Action(zoomType.getDescription(), IAction.AS_CHECK_BOX) {
				public void run() {
					xyGraph.setZoomType(zoomType);
				}
			};
			zoomAction.setImageDescriptor(icon);
			zoomAction.setId(zoomType.getId());
			zoomG.add(zoomAction);	
			
			if (zoomType == ZoomType.NONE) none = zoomAction;
			
	        actionBarManager.registerAction(ToolbarConfigurationConstants.ZOOM.getId(), zoomAction, ActionType.XYANDIMAGE);
		}
		none.setChecked(true);
	       
        // Add more actions
        // Rescale		
		final Action rescaleAction = new Action("Rescale axis when plotted data changes", PlottingSystemActivator.getImageDescriptor("icons/rescale.png")) {
		    public void run() {
				viewer.getSystem().setRescale(!viewer.getSystem().isRescale());
		    }
		};
		rescaleAction.setChecked(viewer.getSystem().isRescale());
		rescaleAction.setId(BasePlottingConstants.RESCALE);
		actionBarManager.addXYAction(rescaleAction);
		actionBarManager.addImageAction(rescaleAction);
		
        actionBarManager.registerAction(ToolbarConfigurationConstants.ZOOM.getId(), rescaleAction, ActionType.XYANDIMAGE);
	
	}

	protected void createAspectHistoAction(final XYRegionGraph xyGraph) {

		final Action gridSnap = new Action("Snap selection(s) to grid", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.SNAP_TO_GRID, isChecked());
				((XYRegionGraph)xyGraph).setGridSnap(isChecked());
				viewer.getSystem().repaint(false);
			}
		};
		gridSnap.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/grid-snap.png"));
		boolean isSnapped = PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.SNAP_TO_GRID);
		gridSnap.setChecked(isSnapped);
		((XYRegionGraph)xyGraph).setGridSnap(isSnapped);
		gridSnap.setId(PlottingConstants.SNAP_TO_GRID);

		final Action histo = new Action("Rehistogram (F5)", IAction.AS_PUSH_BUTTON) {
			
		    public void run() {		    	
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.HISTO, isChecked());
		    	final IImageTrace trace = (IImageTrace)viewer.getSystem().getTraces(IImageTrace.class).iterator().next();
		    	trace.rehistogram();
		    }
		};
        
		histo.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/histo.png"));
		histo.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.HISTO));
		histo.setAccelerator(SWT.F5);
		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.HISTO.getId());
		histo.setId("org.dawb.workbench.plotting.histo");
		actionBarManager.registerAction(ToolbarConfigurationConstants.HISTO.getId(), histo, ActionType.IMAGE);	 
		actionBarManager.addImageAction(histo);
		
		
		final Action aspect = new Action("Keep aspect ratio", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.ASPECT, isChecked());
		    	xyGraph.setKeepAspect(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
        
		aspect.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/aspect.png"));
		aspect.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.ASPECT));
	    aspect.setId(PlottingConstants.ASPECT);
		
		final Action hideAxes = new Action("Show image axes", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.SHOW_AXES, isChecked());
		    	xyGraph.setShowAxes(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
		hideAxes.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.SHOW_AXES));
		
		final Action hideIntensity = new Action("Show intensity scale", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.SHOW_INTENSITY, isChecked());
		    	viewer.setShowIntensity(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
		hideIntensity.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.SHOW_INTENSITY));
		
		final Action showPixelValues = new Action("Show values on pixels", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	viewer.getSystem().setShowValueLabels(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
		showPixelValues.setChecked(viewer.getSystem().isShowValueLabels());

		lockHisto = new Action("Lock histogram", IAction.AS_CHECK_BOX) {
			public void run() {
				final IImageTrace trace = xyGraph.getRegionArea().getImageTrace();
				if (trace != null) {
					trace.setRescaleHistogram(!isChecked());
				}
			}
		};
		
		final Action zoomWhitespace = new Action("Use whitespace when zooming with mouse wheel", IAction.AS_CHECK_BOX) {
		    public void run() {
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.ZOOM_INTO_WHITESPACE, isChecked());
		    }
		};
		zoomWhitespace.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.ZOOM_INTO_WHITESPACE));

		
		final Action ignoreRGB = new Action("Ignore RBG information", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.IGNORE_RGB, isChecked());
		    	Collection<ITrace> traces = viewer.getSystem().getTraces(IImageTrace.class);
		    	if (traces==null || traces.isEmpty()) return;
		    	IImageTrace image = (IImageTrace)traces.iterator().next();
		    	IDataset data = image.getRGBData();
		    	if (data == null) data = image.getData();
		    	image.setData(data, image.getAxes(), false);
		    	viewer.getSystem().repaint(false);
		    }
		};
		ignoreRGB.setToolTipText("Ignores RGB information in the data file if it has been provided.");
		ignoreRGB.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.IGNORE_RGB));
		
		PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.LOAD_IMAGE_STACKS, false);
//		final Action showStack = new Action("Show other images in the same directory", IAction.AS_CHECK_BOX) {
//			
//		    public void run() {		    	
//		    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.LOAD_IMAGE_STACKS, isChecked());
//		    	IEditorReference[] refs = EclipseUtils.getActivePage().getEditorReferences();
//		    	for (IEditorReference iEditorReference : refs) {
//		    		IEditorPart part = iEditorReference.getEditor(false);
//		    		if (part instanceof IReusableEditor) {
//		    			((IReusableEditor)part).setInput(part.getEditorInput());
//		    		}
//				}
//		    }
//		};
		//showStack.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.LOAD_IMAGE_STACKS));

		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.ASPECT.getId());
	    actionBarManager.registerAction(ToolbarConfigurationConstants.ASPECT.getId(), aspect, ActionType.IMAGE);
		actionBarManager.registerAction(ToolbarConfigurationConstants.ASPECT.getId(), gridSnap, ActionType.IMAGE);
	    
	    actionBarManager.addImageAction(aspect);
		actionBarManager.addImageAction(gridSnap);

	    actionBarManager.addImageSeparator();
	    actionBarManager.addImageAction(hideAxes);
	    actionBarManager.addImageAction(hideIntensity);
	    //actionBarManager.addImageAction(showStack);
	    actionBarManager.addImageAction(showPixelValues);
	    actionBarManager.addImageAction(ignoreRGB);
	    actionBarManager.addImageAction(lockHisto);
		actionBarManager.addImageAction(zoomWhitespace);
	    actionBarManager.addImageSeparator();

	}
	
	public void createFullScreenActions(final XYRegionGraph xyGraph) {
		
		actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.FULLSCREEN.getId());
		
		final Action fullScreen = new Action("View plot in full screen (F11)", IAction.AS_CHECK_BOX) {
			
		    public void run() {
		    	
		    	if (!isChecked() && fullScreenShell != null && !fullScreenShell.isDisposed()) {
		    		fullScreenShell.close();
		    		return;
		    	}
		    	
		    	fullScreenShell = new Shell(Display.getCurrent(), SWT.NO_TRIM);
		    	fullScreenShell.setText("Full screen image");
		    	
		    	final Rectangle rect = Display.getDefault().getPrimaryMonitor().getBounds();
		    	//setFullScreen seems to have issues on linux
		    	fullScreenShell.setBounds(rect);
		    	final PlotType plotType = viewer.getSystem().getPlotType();
		    	updateShellBackground(fullScreenShell, rect ,plotType);
		    	
		    	fullScreenShell.addKeyListener(new KeyListener() {
					@Override
					public void keyPressed(KeyEvent e) {
						//ESC key to close
						if (e.keyCode == SWT.ESC || e.keyCode ==SWT.F11){
							fullScreenShell.close();
						}	
					}
					@Override
					public void keyReleased(KeyEvent e) {
						// do nothing
						
					}
				});
		    	
		    	final ITraceListener traceListener = new ITraceListener.Stub() {
		    		
		    		@Override
		    		public void tracesUpdated(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    		@Override
		    		public void tracesRemoved(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    		@Override
		    		public void tracesAdded(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    		@Override
		    		public void traceUpdated(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    		@Override
		    		public void traceAdded(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    		@Override
		    		public void traceRemoved(TraceEvent evt) {
		    			updateShellBackground(fullScreenShell, rect, viewer.getSystem().getPlotType());
		    		}
		    	};
		    	
		    	viewer.getSystem().addTraceListener(traceListener);
		    	
		    	fullScreenShell.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						viewer.getSystem().removeTraceListener(traceListener);
						Image oldbg = fullScreenShell.getBackgroundImage();
						if (oldbg!=null && oldbg.isDisposed() == false) {oldbg.dispose();}
						if (actionBarManager.findAction("org.dawb.workbench.fullscreen") != null) {
							actionBarManager.findAction("org.dawb.workbench.fullscreen").setChecked(false);
						}
					}
				});
		    	fullScreenShell.open();
		    }
		};
        
		fullScreen.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/fullscreen.png"));
		fullScreen.setId("org.dawb.workbench.fullscreen");
		fullScreen.setAccelerator(SWT.F11);
		actionBarManager.registerAction(ToolbarConfigurationConstants.FULLSCREEN.getId(), fullScreen, ActionType.XYANDIMAGE);
	}
	
	private void updateShellBackground(final Shell shell, final Rectangle rect, PlotType plotType) {
		
		Image oldbg = shell.getBackgroundImage();
		shell.setBackgroundImage(null);
		
		//remove axes for images
		if (plotType == PlotType.IMAGE) {

			boolean xVis = xyGraph.primaryXAxis.isVisible();
			boolean yVis = xyGraph.primaryYAxis.isVisible();

			xyGraph.primaryXAxis.setVisible(false);
			xyGraph.primaryYAxis.setVisible(false);

			try {
				shell.setBackgroundImage(xyGraph.getImage(rect));
			} finally {
				xyGraph.primaryXAxis.setVisible(xVis);
				xyGraph.primaryYAxis.setVisible(yVis);
			}
		} else {
			
			shell.setBackgroundImage(xyGraph.getImage(rect));
		}
		
		if (oldbg != null && !oldbg.isDisposed()) {oldbg.dispose();}
	}

	public void createOriginActions(final XYRegionGraph xyGraph) {

		final MenuAction origins = new MenuAction("Image Origin");
		origins.setId(getClass().getName()+".imageOrigin");
		
		origins.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/origins.png"));
		
		CheckableActionGroup group      = new CheckableActionGroup();
        ImageOrigin imageOrigin = ImageOrigin.forLabel(PlottingSystemActivator.getPlottingPreferenceStore().getString(PlottingConstants.ORIGIN_PREF));
        IAction selectedAction  = null;
        
        for (final ImageOrigin origin : ImageOrigin.origins) {
			
        	final IAction action = new Action(origin.getLabel(), IAction.AS_CHECK_BOX) {
        		public void run() {
        			PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.ORIGIN_PREF, origin.getLabel());
       			    xyGraph.setImageOrigin(origin);
       			    setChecked(true);
        		}
        	};
        	origins.add(action);
        	group.add(action);
        	
        	if (imageOrigin==origin) selectedAction = action;
		}
        
        if (selectedAction!=null) selectedAction.setChecked(true);
        
        actionBarManager.registerMenuBarGroup(origins.getId()+".group");
        actionBarManager.registerAction(origins.getId()+".group", origins, ActionType.IMAGE, ManagerType.MENUBAR);

	}


	/**
	 * Also uses 'bars' field to add the actions
	 * @param rightClick
	 */
	protected void createAdditionalActions(final XYRegionGraph xyGraph, final IContributionManager rightClick) {
						
		if (datasetChoosingRequired) {
			// By index or using x 
			final CheckableActionGroup group = new CheckableActionGroup();
			plotIndex = new Action("Plot data as separate plots", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, false);
			    	setChecked(true);
			    	viewer.getSystem().setXFirst(false);
			    	viewer.getSystem().fireTracesAltered(new TraceEvent(xyGraph));
			    }
			};
			plotIndex.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/plotindex.png"));
			plotIndex.setId(BasePlottingConstants.PLOT_INDEX);
			group.add(plotIndex);
			
			plotX = new Action("Plot using first data set as x-axis", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, true);
			    	setChecked(true);
			    	viewer.getSystem().setXFirst(true);
			    	viewer.getSystem().fireTracesAltered(new TraceEvent(xyGraph));
			    }
			};
			plotX.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/plotxaxis.png"));
			plotX.setId(BasePlottingConstants.PLOT_X_AXIS);
			group.add(plotX);
			
			boolean xfirst = PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.PLOT_X_DATASET);
			if (xfirst) {
				plotX.setChecked(true);
				viewer.getSystem().setXFirst(true);
			} else {
				plotIndex.setChecked(true);
				viewer.getSystem().setXFirst(false);
			}
			
			final Action autoHideRegions = new Action("Automatically hide regions", IAction.AS_CHECK_BOX) {
				public void run() {
					viewer.getSystem().setAutoHideRegions(isChecked());
				}
			};
			autoHideRegions.setChecked(viewer.getSystem().isAutoHideRegions());
			autoHideRegions.setToolTipText("Automatically hide regions when the plot dimensionality changes.");
			actionBarManager.registerAction(autoHideRegions, ActionType.ALL, ManagerType.MENUBAR);
			
			MenuAction filters = PlotFilterActions.getXYFilterActions(actionBarManager.getSystem());
			
			actionBarManager.addXYSeparator();
			actionBarManager.addXYAction(plotX);
			actionBarManager.addXYAction(plotIndex);
			actionBarManager.addXYSeparator();
			actionBarManager.addXYAction(filters);
			actionBarManager.addXYSeparator();

			
			actionBarManager.registerToolBarGroup(ToolbarConfigurationConstants.XYPLOT.getId());
		    actionBarManager.registerAction(ToolbarConfigurationConstants.XYPLOT.getId(), plotIndex, ActionType.XY);
		    actionBarManager.registerAction(ToolbarConfigurationConstants.XYPLOT.getId(), plotX,     ActionType.XY);
			
			
			if (rightClick!=null){
				rightClick.add(new Separator(plotIndex.getId()+".group"));
				rightClick.add(new Separator(plotX.getId()+".group"));
				rightClick.add(plotIndex);
				rightClick.add(plotX);
				rightClick.add(new Separator());
			}
		}
		
				
	}
	
	public void dispose() {
 		PlottingSystemActivator.getLocalPreferenceStore().removePropertyChangeListener(propertyListener);
		actionBarManager.removePropertyChangeListener(switchListener);
	    plotIndex = null;
	    plotX     = null;
	}

	public void setXfirstButtons(boolean xfirst) {
		if (xfirst) {
			if (plotX!=null) plotX.setChecked(true);
		} else {
			if (plotIndex!=null) plotIndex.setChecked(true);
		}
	}

	public void setRescaleButton(boolean rescale) {
		IAction action = actionBarManager.findAction(BasePlottingConstants.RESCALE);
		if (action==null) return; // they are allowed to remove it.
		action.setChecked(rescale);
	}

	private IAxis getCurrentYAxis() {
		IAxis yAxis = null;
		List<IAxis> axisList = viewer.getSystem().getAxes();
		for (IAxis axis : axisList) {
			if (axis.isYAxis() && yAxis == null) {
				yAxis = axis;
			}
		}
		return yAxis;
	}

	private IAxis getCurrentXAxis() {
		IAxis xAxis = null;
		List<IAxis> axisList = viewer.getSystem().getAxes();
		for (IAxis axis : axisList) {
			if (!axis.isYAxis() && xAxis == null) {
				xAxis = axis;
			}
		}
		return xAxis;
	}

	public Action getHistoLock() {
		return lockHisto;
	}
}
