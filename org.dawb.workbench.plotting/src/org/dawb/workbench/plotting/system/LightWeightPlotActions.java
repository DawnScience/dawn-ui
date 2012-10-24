package org.dawb.workbench.plotting.system;

import org.csstudio.swt.xygraph.figures.XYGraphFlags;
import org.csstudio.swt.xygraph.toolbar.AddAnnotationDialog;
import org.csstudio.swt.xygraph.toolbar.RemoveAnnotationDialog;
import org.csstudio.swt.xygraph.toolbar.XYGraphConfigDialog;
import org.csstudio.swt.xygraph.undo.AddAnnotationCommand;
import org.csstudio.swt.xygraph.undo.IOperationsManagerListener;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.csstudio.swt.xygraph.undo.RemoveAnnotationCommand;
import org.csstudio.swt.xygraph.undo.ZoomType;
import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.ActionContainer;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.dawb.workbench.plotting.system.dialog.AddRegionDialog;
import org.dawb.workbench.plotting.system.dialog.RemoveRegionCommand;
import org.dawb.workbench.plotting.system.dialog.RemoveRegionDialog;
import org.dawb.workbench.plotting.system.swtxy.XYRegionConfigDialog;
import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deligating class for light-weight actions.
 * 
 * This class creates actions for the lightweight plotting system.
 * 
 * @author fcp94556
 *
 */
class LightWeightPlotActions {

	private Logger logger = LoggerFactory.getLogger(LightWeightPlotActions.class);
	
	private PlotActionsManagerImpl actionBarManager;
	private XYRegionGraph          xyGraph;
	private LightWeightPlotViewer  viewer;
	private boolean                datasetChoosingRequired = true;
	private Action                 plotIndex, plotX;

	public void init(final LightWeightPlotViewer viewer, XYRegionGraph xyGraph, PlotActionsManagerImpl actionBarManager) {
		this.viewer  = viewer;
		this.xyGraph = xyGraph;
		this.actionBarManager = actionBarManager;
	}

	public void createLightWeightActions() {
		
 		createConfigActions(xyGraph);
 		createAnnotationActions(xyGraph);
 		actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_1D, "org.dawb.workbench.plotting.views.toolPageView.1D");
 		actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_2D, "org.dawb.workbench.plotting.views.toolPageView.2D");
 		actionBarManager.createToolDimensionalActions(ToolPageRole.ROLE_1D_AND_2D, "org.dawb.workbench.plotting.views.toolPageView.1D_and_2D");
 		createRegionActions(xyGraph);
 		createZoomActions(xyGraph, XYGraphFlags.COMBINED_ZOOM);
 		createUndoRedoActions(xyGraph);
 		IActionBars bars = actionBarManager.getActionBars();
 		actionBarManager.createExportActionsToolBar(bars!=null?bars.getToolBarManager():null);
 		createAspectHistoAction(xyGraph);
 		actionBarManager.createPalleteActions();
 		createOriginActions(xyGraph);
 		actionBarManager.createExportActionsMenuBar();
 		createAdditionalActions(xyGraph, null);
		
	}

	public void createUndoRedoActions(final XYRegionGraph xyGraph) {
		
		addToolbarSeparator("org.csstudio.swt.xygraph.toolbar.undoredo");		
		
		//undo button		
		final Action undoButton = new Action("Undo", Activator.getImageDescriptor("icons/Undo.png")) {
			public void run() {
				xyGraph.getOperationsManager().undo();
			}
		};
		undoButton.setEnabled(false);
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(undoButton);		
	
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
		final Action redoButton = new Action("Redo", Activator.getImageDescriptor("icons/Redo.png")) {
			public void run() {
				xyGraph.getOperationsManager().redo();
			}
		};
		redoButton.setEnabled(false);
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(redoButton);		

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
		
		addToolbarSeparator("org.csstudio.swt.xygraph.toolbar.configure");	
		
		final Action configButton = new Action("Configure Settings...", Activator.getImageDescriptor("icons/Configure.png")) {
			public void run() {
				XYGraphConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph);
				dialog.open();
			}
		};
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(configButton);
		
		final Action showLegend = new Action("Show Legend", IAction.AS_CHECK_BOX) {
			public void run() {
				xyGraph.setShowLegend(!xyGraph.isShowLegend());
			}
		};
		showLegend.setImageDescriptor(Activator.getImageDescriptor("icons/ShowLegend.png"));
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(showLegend);
		
		showLegend.setChecked(xyGraph.isShowLegend());
		
		
	}
	
	protected void createAnnotationActions(final XYRegionGraph xyGraph) {
		
		addToolbarSeparator("org.csstudio.swt.xygraph.toolbar.annotation");	
		
		final Action addAnnotation = new Action("Add Annotation...", Activator.getImageDescriptor("icons/Add_Annotation.png")) {
			public void run() {
				AddAnnotationDialog dialog = new AddAnnotationDialog(Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK){
					xyGraph.addAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new AddAnnotationCommand(xyGraph, dialog.getAnnotation()));
				}
				
			}
		};
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(addAnnotation);
	
		
		final Action delAnnotation = new Action("Remove Annotation...", Activator.getImageDescriptor("icons/Del_Annotation.png")) {
			public void run() {
				RemoveAnnotationDialog dialog = new RemoveAnnotationDialog(Display.getCurrent().getActiveShell(), xyGraph);
				if(dialog.open() == Window.OK && dialog.getAnnotation() != null){
					xyGraph.removeAnnotation(dialog.getAnnotation());
					xyGraph.getOperationsManager().addCommand(
							new RemoveAnnotationCommand(xyGraph, dialog.getAnnotation()));					
				}
				
			}
		};
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(delAnnotation);
		
		addToolbarSeparator("org.csstudio.swt.xygraph.toolbar.extra");	
	}
	
	protected void createRegionActions(final XYRegionGraph xyGraph) {
		
		
		if (actionBarManager.getActionBars()!=null && actionBarManager.getActionBars().getToolBarManager()!=null)  {
			actionBarManager.getActionBars().getToolBarManager().add(new Separator("lightweight.graph.region.actions"));
		}
		
        final MenuAction regionDropDown = new MenuAction("Selection region");
        regionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions"); // Id used elsewhere...
 
		regionDropDown.add(createRegionAction(xyGraph, RegionType.LINE,       regionDropDown, "Add line selection",     Activator.getImageDescriptor("icons/ProfileLine.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POLYLINE,   regionDropDown, "Add polyline selection", Activator.getImageDescriptor("icons/ProfilePolyline.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POLYGON,    regionDropDown, "Add polygon selection",  Activator.getImageDescriptor("icons/ProfilePolyline.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.BOX,        regionDropDown, "Add box selection",      Activator.getImageDescriptor("icons/ProfileBox.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.SECTOR,     regionDropDown, "Add sector selection",   Activator.getImageDescriptor("icons/ProfileSector.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.RING,       regionDropDown, "Add ring selection",     Activator.getImageDescriptor("icons/ProfileCircle.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.XAXIS,      regionDropDown, "Add X-axis selection",   Activator.getImageDescriptor("icons/Cursor-horiz.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.YAXIS,      regionDropDown, "Add Y-axis selection",   Activator.getImageDescriptor("icons/Cursor-vert.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.FREE_DRAW,  regionDropDown, "Free drawn selection",   Activator.getImageDescriptor("icons/ProfileFree.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.POINT,      regionDropDown, "Single point selection", Activator.getImageDescriptor("icons/ProfilePoint.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.CIRCLE,     regionDropDown, "Add circle selection",   Activator.getImageDescriptor("icons/ProfileCircle.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.ELLIPSE,    regionDropDown, "Add ellipse selection",  Activator.getImageDescriptor("icons/ProfileEllipse.png")));
		regionDropDown.add(createRegionAction(xyGraph, RegionType.ELLIPSEFIT, regionDropDown, "Ellipse fit selection",  Activator.getImageDescriptor("icons/ProfileEllipse.png")));
		
		if (actionBarManager.getActionBars()!=null && actionBarManager.getActionBars().getMenuManager()!=null)  {
			actionBarManager.getActionBars().getMenuManager().add(regionDropDown);
		}
			
        final MenuAction removeRegionDropDown = new MenuAction("Delete selection region(s)");
        removeRegionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.removeRegions");

        final Action removeRegion = new Action("Remove Region...", Activator.getImageDescriptor("icons/RegionDelete.png")) {
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
		
        final Action removeAllRegions = new Action("Remove all regions...", Activator.getImageDescriptor("icons/RegionDeleteAll.png")) {
			public void run() {
				
				final boolean yes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), 
						                  "Please Confirm Delete All",
						                  "Are you sure you would like to delete all selection regions?");
				
				if (yes){
					xyGraph.getOperationsManager().addCommand(
							new RemoveRegionCommand((XYRegionGraph)xyGraph, ((XYRegionGraph)xyGraph).getRegions()));					
					((XYRegionGraph)xyGraph).clearRegions();
				}
			}
		};
		
		removeRegionDropDown.add(removeAllRegions);

		
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(removeRegionDropDown);
		//if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getMenuManager().add(removeRegionDropDown);
		
	}
	
	protected void createRegion(final XYRegionGraph xyGraph, MenuAction regionDropDown, Action action, RegionType type) throws Exception {
		
		if (xyGraph.getXAxisList().size()==1 && xyGraph.getYAxisList().size()==1) {
			xyGraph.createRegion(RegionUtils.getUniqueName(type.getName(), viewer.getSystem()), viewer.getSelectedXAxis(), viewer.getSelectedYAxis(), type, true);
		} else {
			AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, type);
			if (dialog.open() != Window.OK){
				return;
			}
		}
		//regionDropDown.setSelectedAction(action);	
		//regionDropDown.setChecked(true);
	}
	
	private IAction createRegionAction(final XYRegionGraph xyGraph, final RegionType type, final MenuAction regionDropDown, final String label, final ImageDescriptor icon) {
		final Action regionAction = new Action(label, icon) {
			public void run() {				
				try {
					createRegion(xyGraph, regionDropDown, this, type);
				} catch (Exception e) {
					logger.error("Cannot create region!", e);
				}
			}
		};
		regionAction.setId(type.getId());
		return regionAction;
	}

	
	private void addToolbarSeparator(String id) {
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(new Separator(	id ));	
	}

	public void createZoomActions(final XYRegionGraph xyGraph, final int flags) {
		
		addToolbarSeparator("org.csstudio.swt.xygraph.toolbar.zoom");		

        final Action autoScale = new Action("Perform Auto Scale", Activator.getImageDescriptor("icons/AutoScale.png")) {
        	public void run() {
	            xyGraph.performAutoScale();
        	}
        };
        autoScale.setId("org.csstudio.swt.xygraph.autoscale");
        if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(autoScale);
        
        final CheckableActionGroup zoomG = new CheckableActionGroup();
        Action panning = null;
		for(final ZoomType zoomType : ZoomType.values()){
		    if (! zoomType.useWithFlags(flags)) continue;
		 		
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
			
			if (zoomType == ZoomType.PANNING) panning = zoomAction;
			
			if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(zoomAction);
		}
		panning.setChecked(true);
		panning.run(); 
		
	       
        // Add more actions
        // Rescale		
		final Action rescaleAction = new Action("Rescale axis when plotted data changes", Activator.getImageDescriptor("icons/rescale.png")) {
		    public void run() {
				viewer.getSystem().setRescale(!viewer.getSystem().isRescale());
		    }
		};
		rescaleAction.setChecked(viewer.getSystem().isRescale());
		rescaleAction.setId("org.dawb.workbench.plotting.rescale");
		actionBarManager.addXYAction(rescaleAction);
		actionBarManager.addImageAction(rescaleAction);
		
		if (actionBarManager.getActionBars()!=null) actionBarManager.getActionBars().getToolBarManager().add(rescaleAction);
	}

	protected void createAspectHistoAction(final XYRegionGraph xyGraph) {

		final Action histo = new Action("Rehistogram on zoom in or zoom out (F5)", IAction.AS_PUSH_BUTTON) {
			
		    public void run() {		    	
		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.HISTO, isChecked());
		    	final IImageTrace trace = (IImageTrace)viewer.getSystem().getTraces(IImageTrace.class).iterator().next();
		    	trace.rehistogram();
		    }
		};
        
		histo.setImageDescriptor(Activator.getImageDescriptor("icons/histo.png"));
		histo.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.HISTO));
		histo.setAccelerator(SWT.F5);
		
		final IActionBars bars = actionBarManager.getActionBars();
		if (bars!=null) {
			bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.histo.group"));
			histo.setId("org.dawb.workbench.plotting.histo");
			bars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.histo.group", histo);	 
			actionBarManager.register2DAction("org.dawb.workbench.plotting.histo.group", histo);
			actionBarManager.addImageAction(histo);
		}
		
		
		final Action action = new Action("Keep aspect ratio", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.ASPECT, isChecked());
		    	xyGraph.setKeepAspect(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
        
		action.setImageDescriptor(Activator.getImageDescriptor("icons/aspect.png"));
		action.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.ASPECT));
		
		final Action hideAxes = new Action("Show image axes", IAction.AS_CHECK_BOX) {
			
		    public void run() {		    	
		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.SHOW_AXES, isChecked());
		    	xyGraph.setShowAxes(isChecked());
		    	viewer.getSystem().repaint(false);
		    }
		};
		hideAxes.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.SHOW_AXES));
		
		if (bars!=null) {
			bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.aspect.group"));
			action.setId("org.dawb.workbench.plotting.aspect");
			bars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.aspect.group", action);
	 
			actionBarManager.register2DAction("org.dawb.workbench.plotting.aspect.group", action);
			
			actionBarManager.addImageAction(action);
			actionBarManager.addImageSeparator();
			actionBarManager.addImageAction(hideAxes);
			actionBarManager.addImageSeparator();

		}

	}
	

	public void createOriginActions(final XYRegionGraph xyGraph) {

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
       			    xyGraph.setImageOrigin(origin);
       			    setChecked(true);
        		}
        	};
        	origins.add(action);
        	group.add(action);
        	
        	if (imageOrigin==origin) selectedAction = action;
		}
        
        if (selectedAction!=null) selectedAction.setChecked(true);
        
		final IActionBars bars = actionBarManager.getActionBars();
		if (bars!=null) {
			bars.getMenuManager().add(new Separator(origins.getId()+".group"));
			bars.getMenuManager().insertAfter(origins.getId()+".group", origins);
		}
		
		ActionContainer cont = actionBarManager.register2DAction(origins.getId()+".group", origins);
		if (bars!=null) cont.setManager(bars.getMenuManager());

	}


	/**
	 * Also uses 'bars' field to add the actions
	 * @param rightClick
	 */
	protected void createAdditionalActions(final XYRegionGraph xyGraph, final IContributionManager rightClick) {
		
        // Add additional if required
		final IActionBars bars = actionBarManager.getActionBars();
				
		if (datasetChoosingRequired) {
			// By index or using x 
			final CheckableActionGroup group = new CheckableActionGroup();
			plotIndex = new Action("Plot data as separate plots", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, false);
			    	setChecked(true);
			    	viewer.getSystem().setXfirst(false);
			    	setXfirst(false);
			    	viewer.getSystem().fireTracesAltered(new TraceEvent(xyGraph));
			    }
			};
			plotIndex.setImageDescriptor(Activator.getImageDescriptor("icons/plotindex.png"));
			plotIndex.setId("org.dawb.workbench.plotting.plotIndex");
			group.add(plotIndex);
			
			plotX = new Action("Plot using first data set as x-axis", IAction.AS_CHECK_BOX) {
			    public void run() {
			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, true);
			    	setChecked(true);
			    	viewer.getSystem().setXfirst(true);
			    	setXfirst(true);
			    	viewer.getSystem().fireTracesAltered(new TraceEvent(xyGraph));
			    }
			};
			plotX.setImageDescriptor(Activator.getImageDescriptor("icons/plotxaxis.png"));
			plotX.setId("org.dawb.workbench.plotting.plotX");
			group.add(plotX);
			
			boolean xfirst = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.PLOT_X_DATASET);
			if (xfirst) {
				plotX.setChecked(true);
				viewer.getSystem().setXfirst(true);
				setXfirst(true);
			} else {
				plotIndex.setChecked(true);
				viewer.getSystem().setXfirst(false);
				setXfirst(false);
			}
			
			
			actionBarManager.addXYSeparator();
			actionBarManager.addXYAction(plotX);
			actionBarManager.addXYAction(plotIndex);
			actionBarManager.addXYSeparator();
			if (bars!=null) {
			
				bars.getToolBarManager().add(new Separator(plotIndex.getId()+".group"));
		        bars.getToolBarManager().add(new Separator(plotX.getId()+".group"));
				bars.getToolBarManager().add(plotIndex);
	       		actionBarManager.register1DAction(plotIndex.getId()+".group", plotIndex);
				bars.getToolBarManager().add(plotX);
				actionBarManager.register1DAction(plotX.getId()+".group", plotX);
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
	
	public void dispose() {
		
	    plotIndex = null;
	    plotX     = null;
	}

	public void setXfirst(boolean xfirst) {
		if (xfirst) {
			if (plotX!=null) plotX.setChecked(true);
		} else {
			if (plotIndex!=null) plotIndex.setChecked(true);
		}
	}
}
