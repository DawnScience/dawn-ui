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

import java.util.Collection;
import java.util.Map;

import org.csstudio.swt.xygraph.undo.ZoomType;
import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.ActionContainer;
import org.dawb.common.ui.plot.PlottingActionBarManager;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.trace.IPaletteTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.PaletteData;
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
	protected PlotActionsManagerImpl(PlottingSystemImpl system) {
		super(system);
		this.system = system;
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
	
	protected void createPalleteActions() {
		
    	final Map<String,Integer> names = PaletteFactory.getPaletteNames();
    	
		int paletteIndex = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.P_PALETTE);

		final MenuAction lutCombo = new MenuAction("Color");
		lutCombo.setId(getClass().getName()+lutCombo.getText());
		
		lutCombo.setImageDescriptor(Activator.getImageDescriptor("icons/color_wheel.png"));
		
		CheckableActionGroup group      = new CheckableActionGroup();
		for (final String paletteName : names.keySet()) {
			final Action action = new Action(paletteName, IAction.AS_CHECK_BOX) {
				public void run() {
					int paletteIndex = PaletteFactory.PALETTES.get(paletteName);
					Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.P_PALETTE, paletteIndex);
					try {
						final PaletteData data = PaletteFactory.getPalette(paletteIndex, true);
						final Collection<ITrace> traces = system.getTraces();
						if (traces!=null) for (ITrace trace: traces) {
							if (trace instanceof IPaletteTrace) {
								((IPaletteTrace)trace).setPaletteData(data);
							}
						}
					} catch (Exception ne) {
						logger.error("Cannot create palette data!", ne);
					}
				}
			};
			group.add(action);
			lutCombo.add(action);
			action.setChecked(PaletteFactory.PALETTES.get(paletteName)==paletteIndex);
		}
		lutCombo.setToolTipText("Histogram");

		final IActionBars bars = getActionBars();
		if (bars!=null) {
			final String groupName = lutCombo.getId()+".group";
			bars.getMenuManager().add(new Separator(groupName));
			bars.getMenuManager().insertAfter(groupName, lutCombo);
			final ActionContainer cont = register2DAction(groupName, lutCombo);
			cont.setManager(bars.getMenuManager());
		}
	}
}
