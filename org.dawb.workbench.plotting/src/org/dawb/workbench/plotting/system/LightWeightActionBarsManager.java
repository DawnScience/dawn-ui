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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.Annotation;
import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingActionBarManager;
import org.dawb.common.ui.plot.annotation.AnnotationUtils;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ILineTrace.PointStyle;
import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.dawb.workbench.plotting.printing.PlotExportPrintUtil;
import org.dawb.workbench.plotting.printing.PlotPrintPreviewDialog;
import org.dawb.workbench.plotting.printing.PrintSettings;
import org.dawb.workbench.plotting.system.swtxy.XYRegionConfigDialog;
import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LightWeightActionBarsManager extends PlottingActionBarManager {


	private static final Logger logger = LoggerFactory.getLogger(LightWeightActionBarsManager.class);
	
	private LightWeightPlottingSystem system;
	private Action                    plotIndex, plotX;
	private boolean                   datasetChoosingRequired = true;
	private List<ActionContainer>     oneDimensionalActions;
	private List<ActionContainer>     twoDimensionalActions;

	protected LightWeightActionBarsManager(LightWeightPlottingSystem system) {
		super(system);
		this.system = system;
		oneDimensionalActions = new ArrayList<ActionContainer>();
		twoDimensionalActions = new ArrayList<ActionContainer>();
	}

	private PlotType lastPlotTypeUpdate = null;
	
	protected void switchActions(final PlotType type) {
		
		if (type == lastPlotTypeUpdate) return;
		lastPlotTypeUpdate = type;
		
		final IActionBars bars = system.getActionBars();
    	if (bars==null) return;
    	 
    	if (oneDimensionalActions!=null) for (ActionContainer ac : oneDimensionalActions) {
    		if (type.is1D() && ac.getManager().find(ac.getId())==null) {
    			ac.getManager().insertAfter(ac.getId()+".group", ac.getAction());
    		} else if (!type.is1D()) {
    			ac.getManager().remove(ac.getId());
    		}
		}
    	
    	final boolean is2D = !type.is1D();
    	if (twoDimensionalActions!=null) for (ActionContainer ac : twoDimensionalActions) {
    		if (is2D && ac.getManager().find(ac.getId())==null) {
    			ac.getManager().insertAfter(ac.getId()+".group", ac.getAction());
    		} else if (!is2D) {
    			ac.getManager().remove(ac.getId());
      		}
		}
    	
    	if (bars.getToolBarManager()!=null)    bars.getToolBarManager().update(true);
    	if (bars.getMenuManager()!=null)       bars.getMenuManager().update(true);
    	if (bars.getStatusLineManager()!=null) bars.getStatusLineManager().update(true);
    	bars.updateActionBars();
	}
	
	
	protected void createToolDimensionalActions(final ToolPageRole role,
			                                    final String       viewId) {

		final IActionBars bars = system.getActionBars();
		if (bars!=null) {
       	
			try {
				IAction toolSet = createToolActions(role, viewId);
				if (toolSet==null) return;

				bars.getToolBarManager().add(new Separator(role.getId()+".group"));
				bars.getToolBarManager().insertAfter(role.getId()+".group", toolSet);
				if (role.is1D()&&!role.is2D()) oneDimensionalActions.add(new ActionContainer(toolSet, bars.getToolBarManager()));
				if (role.is2D()&&!role.is1D()) twoDimensionalActions.add(new ActionContainer(toolSet, bars.getToolBarManager()));

	        	
			} catch (Exception e) {
				logger.error("Reading extensions for plotting tools", e);
			}
       }	
	}
	
	protected void createAspectHistoAction() {

		final Action histo = new Action("Rehistogram on zoom in or zoom out (F5)", IAction.AS_PUSH_BUTTON) {
			
		    public void run() {		    	
		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.HISTO, isChecked());
		    	final IImageTrace trace = (IImageTrace)system.getTraces(IImageTrace.class).iterator().next();
		    	trace.rehistogram();
		    }
		};
        
		histo.setImageDescriptor(Activator.getImageDescriptor("icons/histo.png"));
		histo.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.HISTO));
		histo.setAccelerator(SWT.F5);
		
		final IActionBars bars = system.getActionBars();
		if (bars!=null) {
			bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.histo.group"));
			histo.setId("org.dawb.workbench.plotting.histo");
			bars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.histo.group", histo);
	 
			twoDimensionalActions.add(new ActionContainer(histo, bars.getToolBarManager()));
		}
		
		
		final Action action = new Action("Keep aspect ratio", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.ASPECT, isChecked());
		    	system.getGraph().setKeepAspect(isChecked());
		    	system.repaint(false);
		    }
		};
        
		action.setImageDescriptor(Activator.getImageDescriptor("icons/aspect.png"));
		action.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.ASPECT));
		
		if (bars!=null) {
			bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.aspect.group"));
			action.setId("org.dawb.workbench.plotting.aspect");
			bars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.aspect.group", action);
	 
			twoDimensionalActions.add(new ActionContainer(action, bars.getToolBarManager()));
		}

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
						system.getGraph().setPaletteData(data);
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

		final IActionBars bars = system.getActionBars();
		if (bars!=null) {
			bars.getMenuManager().add(new Separator(lutCombo.getId()+".group"));
			bars.getMenuManager().insertAfter(lutCombo.getId()+".group", lutCombo);
			
			twoDimensionalActions.add(new ActionContainer(lutCombo, bars.getMenuManager()));
		}
	}
	

	public void createOriginActions() {

		final MenuAction origins = new MenuAction("Image Origin");
		origins.setId(getClass().getName()+".imageOrigin");
		
		origins.setImageDescriptor(Activator.getImageDescriptor("icons/origins.png"));
		
		CheckableActionGroup group      = new CheckableActionGroup();
        ImageOrigin imageOrigin = ImageOrigin.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.ORIGIN_PREF));
        IAction selectedAction  = null;
        
        for (final ImageOrigin origin : ImageOrigin.origins) {
			
        	final IAction action = new Action(origin.getLabel(), IAction.AS_CHECK_BOX) {
        		public void run() {
        			Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.ORIGIN_PREF, origin.getLabel());
       			    system.getGraph().setImageOrigin(origin);
       			    setChecked(true);
        		}
        	};
        	origins.add(action);
        	group.add(action);
        	
        	if (imageOrigin==origin) selectedAction = action;
		}
        
        if (selectedAction!=null) selectedAction.setChecked(true);
        
		final IActionBars bars = system.getActionBars();
		bars.getMenuManager().add(new Separator(origins.getId()+".group"));
		bars.getMenuManager().insertAfter(origins.getId()+".group", origins);
		
		twoDimensionalActions.add(new ActionContainer(origins, bars.getMenuManager()));

	}


	/**
	 * Also uses 'bars' field to add the actions
	 * @param rightClick
	 */
	protected void createAdditionalActions(final IContributionManager rightClick) {
		
        // Add additional if required
		final IActionBars bars = system.getActionBars();
			
        if (extra1DActions!=null&&!extra1DActions.isEmpty()){
    		bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.extra1D.group"));
         	for (IAction action : extra1DActions) {
        		bars.getToolBarManager().add(action);
        		action.setId("org.dawb.workbench.plotting.extra1D");
        		oneDimensionalActions.add(new ActionContainer(action, bars.getToolBarManager()));
        	}
        }
        
        // Add more actions
        // Rescale		
		final Action rescaleAction = new Action("Rescale axis when plotted data changes", Activator.getImageDescriptor("icons/rescale.png")) {
		    public void run() {
				system.setRescale(!system.isRescale());
		    }
		};
		rescaleAction.setChecked(this.system.isRescale());
		rescaleAction.setId("org.dawb.workbench.plotting.rescale");
		if (bars!=null) oneDimensionalActions.add(new ActionContainer(rescaleAction, bars.getToolBarManager()));

        if (bars!=null) bars.getToolBarManager().add(new Separator(rescaleAction.getId()+".group"));
		if (rightClick!=null) rightClick.add(new Separator(rescaleAction.getId()+".group"));

		if (bars!=null) bars.getToolBarManager().insertAfter(rescaleAction.getId()+".group", rescaleAction);
		if (rightClick!=null)rightClick.insertAfter(rescaleAction.getId()+".group", rescaleAction);

		
		if (datasetChoosingRequired) {
			// By index or using x 
			final CheckableActionGroup group = new CheckableActionGroup();
			plotIndex = new Action("Plot using indices", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, false);
			    	setChecked(true);
			    	system.setXfirst(false);
			    	setXfirst(false);
			    	system.fireTracesAltered(new TraceEvent(system.getGraph()));
			    }
			};
			plotIndex.setImageDescriptor(Activator.getImageDescriptor("icons/plotindex.png"));
			plotIndex.setId("org.dawb.workbench.plotting.plotIndex");
			group.add(plotIndex);
			
			plotX = new Action("Plot using first data set selected as x-axis", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, true);
			    	setChecked(true);
			    	system.setXfirst(true);
			    	setXfirst(true);
			    	system.fireTracesAltered(new TraceEvent(system.getGraph()));
			    }
			};
			plotX.setImageDescriptor(Activator.getImageDescriptor("icons/plotxaxis.png"));
			plotX.setId("org.dawb.workbench.plotting.plotX");
			group.add(plotX);
			
			boolean xfirst = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.PLOT_X_DATASET);
			if (xfirst) {
				plotX.setChecked(true);
			} else {
				plotIndex.setChecked(true);
			}
			
			
			if (bars!=null) {
			
				bars.getToolBarManager().add(new Separator(plotIndex.getId()+".group"));
		        bars.getToolBarManager().add(new Separator(plotX.getId()+".group"));
				bars.getToolBarManager().add(plotIndex);
	       		oneDimensionalActions.add(new ActionContainer(plotIndex, bars.getToolBarManager()));
				bars.getToolBarManager().add(plotX);
	       		oneDimensionalActions.add(new ActionContainer(plotX, bars.getToolBarManager()));
		        bars.getToolBarManager().add(new Separator());
			}
			
			if (rightClick!=null){
				rightClick.add(new Separator(plotIndex.getId()+".group"));
				rightClick.add(new Separator(plotX.getId()+".group"));
				rightClick.add(plotIndex);
				rightClick.add(plotX);
				rightClick.add(new Separator());
			}
		}
		
				
	}

	public void setXfirst(boolean xfirst) {
		if (xfirst) {
			if (plotX!=null) plotX.setChecked(true);
		} else {
			if (plotIndex!=null) plotIndex.setChecked(true);
		}
	}
	
	public void setDatasetChoosingRequired(boolean choosingRequired) {
		if (plotX!=null)     plotX.setEnabled(choosingRequired);
		if (plotIndex!=null) plotIndex.setEnabled(choosingRequired);
		this.datasetChoosingRequired  = choosingRequired;
	}	

	@Override
	public void dispose() {
		super.dispose();
		
	    plotIndex = null;
	    plotX     = null;
	    
	    if (oneDimensionalActions!=null) oneDimensionalActions.clear();
	    oneDimensionalActions = null;
	       
	    if (twoDimensionalActions!=null) twoDimensionalActions.clear();
	    twoDimensionalActions = null;

	}
	
	
	private final class ActionContainer {
        private IAction action;
        private IContributionManager manager;
        
        public ActionContainer(IAction action, IContributionManager manager) {
        	this.action  = action;
        	this.manager = manager;
        }
        
		public String getId() {
			return action.getId();
		}

		public IAction getAction() {
			return action;
		}
		public void setAction(IAction action) {
			this.action = action;
		}
		public IContributionManager getManager() {
			return manager;
		}
		public void setManager(IContributionManager manager) {
			this.manager = manager;
		}
		public String toString() {
			return action.toString();
		}
	}

    /**
     * 
     * Problems:
     * 1. Line trace bounds extend over other line traces so the last line trace added, will
     * always be the figure that the right click detects.
     * 
     * Useful things, visible, annotation, quick set to line or points, open configure page.
     * 
     * @param manager
     * @param trace
     * @param xyGraph
     */
	public void fillTraceActions(final IMenuManager manager, final ITrace trace, final XYRegionGraph xyGraph) {

		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.start"));

		if (trace instanceof ILineTrace) { // Does actually work for images but may confuse people.
			final Action visible = new Action("Hide '"+trace.getName()+"'", Activator.getImageDescriptor("icons/TraceVisible.png")) {
				public void run() {
					trace.setVisible(false);
				}
			};
			manager.add(visible);
		}
		
		final Action addAnnotation = new Action("Add annotation to '"+trace.getName()+"'", Activator.getImageDescriptor("icons/TraceAnnotation.png")) {
			public void run() {
				final String annotName = AnnotationUtils.getUniqueAnnotation(trace.getName()+" annotation ", system);
				if (trace instanceof LineTraceImpl) {
					final LineTraceImpl lt = (LineTraceImpl)trace;
					xyGraph.addAnnotation(new Annotation(annotName, lt.getTrace()));
				} else {
					xyGraph.addAnnotation(new Annotation(annotName, xyGraph.primaryXAxis, xyGraph.primaryYAxis));
				}
			}
		};
		manager.add(addAnnotation);
		
		if (trace instanceof ILineTrace) {
			final ILineTrace lt = (ILineTrace)trace;
			if (lt.getTraceType()!=TraceType.POINT) { // Give them a quick change to points
				final Action changeToPoints = new Action("Plot '"+trace.getName()+"' as scatter", Activator.getImageDescriptor("icons/TraceScatter.png")) {
					public void run() {
						lt.setTraceType(TraceType.POINT);
						lt.setPointSize(8);
						lt.setPointStyle(PointStyle.XCROSS);
					}
				};
				manager.add(changeToPoints);
			} else if (lt.getTraceType()!=TraceType.SOLID_LINE) {
				final Action changeToLine = new Action("Plot '"+trace.getName()+"' as line", Activator.getImageDescriptor("icons/TraceLine.png")) {
					public void run() {
						lt.setTraceType(TraceType.SOLID_LINE);
						lt.setLineWidth(1);
						lt.setPointSize(1);
						lt.setPointStyle(PointStyle.NONE);
					}
				};
				manager.add(changeToLine);
			}
		}

		final Action configure = new Action("Configure '"+trace.getName()+"'", Activator.getImageDescriptor("icons/TraceProperties.png")) {
			public void run() {
				final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph);
				dialog.setPlottingSystem(system);
				dialog.setSelectedTrace(trace);
				dialog.open();
			}
		};
		manager.add(configure);

		manager.add(new Separator("org.dawb.workbench.plotting.system.trace.end"));
	}

	/**
	 *  Create export and print buttons in tool bar 
	 */
	public void createExportActionsToolBar() {

		Action exportSaveButton = new Action("Export/save the plotting", Activator.getImageDescriptor("icons/picture_save.png")){
			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;
			public void run(){
				system.savePlotting(filename);
			}
		};

		Action copyToClipboardButton = new Action("Copy to clip-board       Ctrl+C", Activator.getImageDescriptor("icons/copy_edit_on.gif")) {
			public void run() {
				system.copyPlotting();
			}
		};

		// TODO implement within the Print Action
		Action snapShotButton = new Action("Print scaled image to printer", Activator.getImageDescriptor("icons/camera.gif")) {
			public void run(){
				system.printSnapshotPlotting();
			}
		};

		Action printButton = new Action("Print the plotting            Ctrl+P", Activator.getImageDescriptor("icons/printer.png")) {
			public void run() {
				system.printPlotting();
			}
		};

		MenuAction exportActionsDropDown = new MenuAction("Export/Print");
		exportActionsDropDown.setImageDescriptor(Activator.getImageDescriptor("icons/printer.png"));
		exportActionsDropDown.setSelectedAction(printButton);
		exportActionsDropDown.add(exportSaveButton);
		exportActionsDropDown.add(copyToClipboardButton);
		exportActionsDropDown.addSeparator();
		exportActionsDropDown.add(snapShotButton);
		exportActionsDropDown.add(printButton);

		this.system.getActionBars().getToolBarManager().add(exportActionsDropDown);
	}

	/**
	 * Create export and print buttons in menu bar
	 */
	public void createExportActionsMenuBar() {
		Action exportSaveButton = new Action("Export/save the plotting", Activator.getImageDescriptor("icons/picture_save.png")){
			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;
			public void run(){
				system.savePlotting(filename);
			}
		};
		Action copyToClipboardButton = new Action("Copy to clip-board       Ctrl+C", Activator.getImageDescriptor("icons/copy_edit_on.gif")) {
			public void run() {
				system.copyPlotting();
			}
		};
		// TODO implement within the Print Action
		Action snapShotButton = new Action("Print scaled image to printer", Activator.getImageDescriptor("icons/camera.gif")) {
			public void run(){
				system.printSnapshotPlotting();
			}
		};
		Action printButton = new Action("Print the plotting            Ctrl+P", Activator.getImageDescriptor("icons/printer.png")) {
			public void run() {
				system.printPlotting();
			}
		};
		this.system.getActionBars().getMenuManager().add(new Separator(exportSaveButton.getId()+".group"));
		this.system.getActionBars().getMenuManager().add(exportSaveButton);
		this.system.getActionBars().getMenuManager().add(copyToClipboardButton);
		this.system.getActionBars().getMenuManager().add(new Separator(snapShotButton.getId()+".group"));
		this.system.getActionBars().getMenuManager().add(snapShotButton);
		this.system.getActionBars().getMenuManager().add(printButton);
	}

}
