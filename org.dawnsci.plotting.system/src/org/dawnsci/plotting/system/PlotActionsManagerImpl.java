/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.PlotDataConversionWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawnsci.plotting.PlottingActionBarManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.plotting.api.ActionType;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.ManagerType;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.nebula.visualization.xygraph.figures.ZoomType;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages switching actions between different plotting system
 * modes.
 * 
 * @author Matthew Gerring
 *
 */
public class PlotActionsManagerImpl extends PlottingActionBarManager {

	private static final Logger logger = LoggerFactory.getLogger(PlotActionsManagerImpl.class);

	private PlottingSystemImpl<?>        system;

	protected PlotActionsManagerImpl(PlottingSystemImpl<?> system) {
		super(system);
		this.system = system;
	}

	public IPlottingSystem<?> getSystem() {
		return system;
	}

	private static String lastscreeshot_filename;

	public void createExportActions() {
		final IAction exportActionDropDown = getExportActions();
		registerToolBarGroup(ToolbarConfigurationConstants.EXPORT.getId());
		registerAction(ToolbarConfigurationConstants.EXPORT.getId(), exportActionDropDown, ActionType.XYANDIMAGE, ManagerType.TOOLBAR);

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

	private static String lastPath;
	private IAction getExportActions() {
		
		final MenuAction exportActionsDropDown = new MenuAction("Export/Print");
		
		Action exportSaveButton = new Action("Save screenshot as...", PlottingSystemActivator.getImageDescriptor("icons/picture_save.png")){
			// Cache file name otherwise they have to keep
			// choosing the folder.
			public void run(){
				try {
					lastscreeshot_filename = system.savePlotting(lastscreeshot_filename);
				} catch (Exception e) {
					final Status status = new Status(IStatus.ERROR, "org.dawnsci.plotting.system", e.getMessage());
					ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot save screenshot", 
							                "Cannot save file\n\n"+
									        "The location might be in a read only directory or invalid.", status);
					return;
				}
				exportActionsDropDown.setSelectedAction(this);
			}
		};

		Action copyToClipboardButton = new Action("Copy to clip-board", PlottingSystemActivator.getImageDescriptor("icons/copy_edit_on.gif")) {
			@Override
			public void run() {
				system.copyPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};
		copyToClipboardButton.setActionDefinitionId("org.eclipse.ui.edit.copy");

		Action snapShotButton = new Action("Print with preview", PlottingSystemActivator.getImageDescriptor("icons/camera.gif")) {
			@Override
			public void run(){
				system.printScaledPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};
		Action printButton = new Action("Print plot", PlottingSystemActivator.getImageDescriptor("icons/printer.png")) {
			@Override
			public void run() {
				system.printPlotting();
				exportActionsDropDown.setSelectedAction(this);
			}
		};
		printButton.setActionDefinitionId("org.eclipse.ui.file.print");

		final Action export = new Action("Export plot data to Nexus (HDF5)...", PlottingSystemActivator.getImageDescriptor("icons/save_edit.png")) {
			@Override
			public void run() {
				try {
					system.setFocus();
					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
					
					if (wiz instanceof PersistenceExportWizard) {
						((PersistenceExportWizard) wiz).setPlottingSystem(system);
					}
					
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
					exportActionsDropDown.setSelectedAction(this);
				} catch (Exception e) {
					
					final Status status = new Status(IStatus.ERROR, "org.dawnsci.plotting.system", e.getMessage());
					ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot Export Plot", 
							                "Cannot export the plot to HDF5.\n\n"+
									        "The location might be in a read only directory or invalid.", status);

					logger.error("Problem exporting!", e);
				}
			}			
		};
		
		final Action convert = new Action("Export plot data to tif/dat/csv...", PlottingSystemActivator.getImageDescriptor("icons/save_edit.png")) {
			public void run() {
				try {
					PlotDataConversionWizard wiz = (PlotDataConversionWizard)EclipseUtils.openWizard(PlotDataConversionWizard.ID, false);
					wiz.setFilePath(lastPath);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					if (wiz instanceof PlotDataConversionWizard) ((PlotDataConversionWizard)wiz).setPlottingSystem(system);
					wd.open();
					lastPath = wiz.getFilePath();
					
					exportActionsDropDown.setSelectedAction(this);
				} catch (Exception e) {
					final Status status = new Status(IStatus.ERROR, "org.dawnsci.plotting.system", e.getMessage());
					ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot Export Plot", 
							                "Cannot export the plot to tif/dat/csv.\n\n"+
									        "The location might be in a read only directory or invalid.", status);

					logger.error("Problem exporting!", e);
				}
			}			
		};
		
		exportActionsDropDown.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/printer.png"));
		exportActionsDropDown.setSelectedAction(printButton);
		exportActionsDropDown.add(exportSaveButton);
		exportActionsDropDown.add(copyToClipboardButton);
		exportActionsDropDown.addSeparator();
		exportActionsDropDown.add(snapShotButton);
		exportActionsDropDown.add(printButton);
		exportActionsDropDown.addSeparator();
		exportActionsDropDown.add(export);
		exportActionsDropDown.add(convert);
		return exportActionsDropDown;
	}

	@Override
	public void fillZoomActions(IContributionManager man) {
		IToolBarManager tbm = system.getActionBars().getToolBarManager();
		IContributionItem action;

		action = tbm.find(BasePlottingConstants.AUTO_SCALE);
		if (action!=null) man.add(((ActionContributionItem)action).getAction());

		action = tbm.find(ZoomType.RUBBERBAND_ZOOM.getId());
		if (action!=null) man.add(((ActionContributionItem)action).getAction());

		action = tbm.find(BasePlottingConstants.RESCALE);
		if (action!=null) {
			man.add(((ActionContributionItem)action).getAction());
		}
		
		action = tbm.find(ZoomType.PANNING.getId());
		if (action!=null) {
			man.add(((ActionContributionItem)action).getAction());
		}

		action = tbm.find(ZoomType.NONE.getId());
		if (action!=null) {
			man.add(((ActionContributionItem)action).getAction());
		}
	}


	@Override
	public void fillRegionActions(IContributionManager man) {
			
		IContributionItem action = system.getActionBars().getToolBarManager().find(BasePlottingConstants.ADD_REGION);
		if (action!=null) man.add(((ActionContributionItem)action).getAction());
		
		action = system.getActionBars().getToolBarManager().find(BasePlottingConstants.REMOVE_REGION);
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

	protected void selectedPaletteChanged(final String paletteName) {
		final Collection<ITrace> traces = system.getTraces();
		if (traces!=null) for (final ITrace trace: traces) {
			if (trace instanceof IPaletteTrace) {
				final IPaletteTrace paletteTrace = (IPaletteTrace) trace;
				paletteTrace.setPalette(paletteName);
				PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.COLOUR_SCHEME, paletteName);
			}
		}
	}

	private IMenuListener paletteMenuListener;

	protected void createPaletteActions() {

		final IPaletteService pservice = PlottingSystemActivator.getService(IPaletteService.class);
		final Collection<String> names = pservice.getColorSchemes();

		String schemeName = PlottingSystemActivator.getPlottingPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);

		final MenuAction lutCombo = new MenuAction("Color");
		lutCombo.setId(getClass().getName()+lutCombo.getText());
		lutCombo.setImageDescriptor(PlottingSystemActivator.getImageDescriptor("icons/color_wheel.png"));

		final Map<String, IAction> paletteActions = new HashMap<String, IAction>(names.size());
		CheckableActionGroup group      = new CheckableActionGroup();
		
		Collection<String> categoryNames = pservice.getColorCategories();
		for (String c : categoryNames) {
			// do not add a submenu for all
			if (!c.equals("All")) {
				MenuAction subMenu = new MenuAction(c);
				Collection<String> colours = pservice.getColorsByCategory(c);
				for (final String colour : colours) {
					final Action action = new Action(colour, IAction.AS_CHECK_BOX) {
						public void run() {
							try {
								selectedPaletteChanged(colour);
							} catch (Exception ne) {
								logger.error("Cannot create palette data!", ne);
							}
						}
					};
					action.setId(colour);
					subMenu.add(action);
					group.add(action);
					action.setChecked(colour.equals(schemeName));
					paletteActions.put(colour, action);
				}
				lutCombo.add(subMenu);
			}
		}
		lutCombo.setToolTipText("Histogram");

		registerMenuBarGroup(lutCombo.getId()+".group");
		registerAction(lutCombo.getId()+".group", lutCombo, ActionType.IMAGE, ManagerType.MENUBAR);

		this.paletteMenuListener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				Collection<ITrace> traces = system.getTraces();
				for (Iterator<ITrace> iterator = traces.iterator(); iterator.hasNext();) {
					ITrace trace = (ITrace) iterator.next();
					if (trace instanceof IImageTrace) {
						IImageTrace image = (IImageTrace) trace;
						String colorSchemeName = image.getPaletteName();
						if (colorSchemeName!=null) {
							IAction scheme = paletteActions.get(colorSchemeName);
							if (scheme!=null) scheme.setChecked(true);
						}
					}
				}
			}
		};
		getActionBars().getMenuManager().addMenuListener(paletteMenuListener);
		PlottingSystemActivator.getPlottingPreferenceStore()
				.addPropertyChangeListener(new IPropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent event) {
						if (isInterestedProperty(event)) {
							final Object newValue = event.getNewValue();
							if (newValue instanceof String)
								selectedPaletteChanged((String) newValue);
						}
					}
					private boolean isInterestedProperty(
							final PropertyChangeEvent event) {
						final String propName = event.getProperty();
						return PlottingConstants.COLOUR_SCHEME.equals(propName);
					}
		});
		
		
		final Action logColorScale = new Action("Log color scale", IAction.AS_CHECK_BOX) {
			public void run() {
				Collection<IPaletteTrace> ps = system.getTracesByClass(IPaletteTrace.class);
				
				if (ps != null && !ps.isEmpty()) {
					for (IPaletteTrace p : ps) {
					// TODO: There should be a method on image to
					// setLogColorScale so that
					// it just does the right thing.
					p.getImageServiceBean().setLogColorScale(this.isChecked());
					if (p.isRescaleHistogram() && p instanceof IImageTrace) {
						((IImageTrace)p).rehistogram();
					} else {
						// XXX: Doing a image.repaint() is not sufficient, force
						// more
						// work to be done by resetting the palette data to what
						// it already has
						p.setPaletteData(p.getPaletteData());
					}
					}
				}
				PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_LOGSCALE, this.isChecked());
			}
		};
		
		logColorScale.setId(getClass().getName()+".logColorScale");
		logColorScale.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_LOGSCALE));
		registerAction(lutCombo.getId()+".group",logColorScale, ActionType.IMAGE, ManagerType.MENUBAR);
		
		final Action invertColorScale = new Action("Invert color scale", IAction.AS_CHECK_BOX) {
			public void run() {
				PlottingSystemActivator.getService(IPaletteService.class).setInverted(this.isChecked());
				Collection<IPaletteTrace> ps = system.getTracesByClass(IPaletteTrace.class);

				if (ps != null && !ps.isEmpty()) {
					for (IPaletteTrace p : ps) {
						p.setPalette(p.getPaletteName());
					}
					PlottingSystemActivator.getPlottingPreferenceStore().setValue(PlottingConstants.CM_INVERTED, this.isChecked());
				}
			}
		};
		
		invertColorScale.setId(getClass().getName()+".invertColorScale");
		invertColorScale.setChecked(PlottingSystemActivator.getPlottingPreferenceStore().getBoolean(PlottingConstants.CM_INVERTED));
		registerAction(lutCombo.getId()+".group",invertColorScale, ActionType.IMAGE, ManagerType.MENUBAR);

		
	}

	public void dispose() {
		if (paletteMenuListener!=null && getActionBars()!=null) {
			getActionBars().getMenuManager().removeMenuListener(paletteMenuListener);
		}
		super.dispose();
	}
}
