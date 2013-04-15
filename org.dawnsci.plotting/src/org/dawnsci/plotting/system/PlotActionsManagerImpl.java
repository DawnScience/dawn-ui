/*-
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.system;

import java.util.Collection;

import org.csstudio.swt.xygraph.undo.ZoomType;
import org.dawb.common.services.IPaletteService;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.PlottingActionBarManager;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.api.ActionType;
import org.dawnsci.plotting.api.ManagerType;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.trace.IPaletteTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.preference.PlottingConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages switching actions between different plotting system
 * modes.
 * 
 * @author fcp94556
 *
 */
public class PlotActionsManagerImpl extends PlottingActionBarManager {


	private static final Logger logger = LoggerFactory.getLogger(PlotActionsManagerImpl.class);
	
	private PlottingSystemImpl        system;
	protected PlotActionsManagerImpl(PlottingSystemImpl system) {
		super(system);
		this.system = system;
	}
	

	private static String lastscreeshot_filename;

	public void createExportActions() {
        final IAction exportActionDropDown = getExportActions();
        registerToolBarGroup("lightweight.plotting.print.action.toolbar");
        registerAction("lightweight.plotting.print.action.toolbar", exportActionDropDown, ActionType.XYANDIMAGE, ManagerType.TOOLBAR);
        
        registerMenuBarGroup("lightweight.plotting.print.action.menubar");
        registerAction("lightweight.plotting.print.action.menubar", exportActionDropDown, ActionType.XYANDIMAGE, ManagerType.MENUBAR);
		
	}

	/**
	 *  Create export and print buttons in tool bar 
	 */
	protected void createExportActions(IContributionManager toolbarManager) {

		if (toolbarManager==null) return;
        final IAction exportActionDropDown = getExportActions();
		toolbarManager.add(exportActionDropDown);

	}
	

	private IAction getExportActions() {
		
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
		return exportActionsDropDown;
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
		createExportActions(man);
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
	
	private boolean updatingColorSchemeInternally = false;
	protected void createPalleteActions() {
		
    	
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
    	final Collection<String> names = pservice.getColorSchemes();
		final String schemeName = Activator.getDefault().getPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);	

		final MenuAction lutCombo = new MenuAction("Color");
		lutCombo.setId(getClass().getName()+lutCombo.getText());
		
		lutCombo.setImageDescriptor(Activator.getImageDescriptor("icons/color_wheel.png"));
		
		CheckableActionGroup group      = new CheckableActionGroup();
		for (final String paletteName : names) {
			final Action action = new Action(paletteName, IAction.AS_CHECK_BOX) {
				public void run() {
					try {
						updatingColorSchemeInternally = true;
						Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.COLOUR_SCHEME, paletteName);
						final PaletteData data = pservice.getPaletteData(paletteName);
						final Collection<ITrace> traces = system.getTraces();
						if (traces!=null) for (ITrace trace: traces) {
							if (trace instanceof IPaletteTrace) {
								((IPaletteTrace)trace).setPaletteData(data);
							}
						}
					} catch (Exception ne) {
						logger.error("Cannot create palette data!", ne);
					} finally {
						updatingColorSchemeInternally = false;
					}
				}
			};
			action.setId(paletteName);
			group.add(action);
			lutCombo.add(action);
			action.setChecked(paletteName.equals(schemeName));
		}
		lutCombo.setToolTipText("Histogram");
		
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (PlottingConstants.COLOUR_SCHEME.equals(event.getProperty())) {
					if (updatingColorSchemeInternally) return;
					final IAction action = lutCombo.findAction((String)event.getNewValue());
					action.setChecked(true);
				}
			}
		});

		registerMenuBarGroup(lutCombo.getId()+".group");
		registerAction(lutCombo.getId()+".group", lutCombo, ActionType.ALL, ManagerType.MENUBAR);
	}



}
