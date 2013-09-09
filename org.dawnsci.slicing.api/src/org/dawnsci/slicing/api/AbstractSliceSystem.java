package org.dawnsci.slicing.api;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.slicing.api.system.DimensionalEvent;
import org.dawnsci.slicing.api.system.DimensionalListener;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.system.ISliceGallery;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.dawnsci.slicing.api.tool.ISlicingTool;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;

/**
 * Do not expose this class to copying. Instead use ISliceSystem
 * @author fcp94556
 * @internal
 */
public abstract class AbstractSliceSystem implements ISliceSystem {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractSliceSystem.class);

	protected DimsDataList    dimsDataList;
	protected IPlottingSystem plottingSystem;
	protected String          sliceReceiverId;
	private List<IAction>     customActions;
	protected SliceObject     sliceObject;
	
	protected Enum        sliceType=PlotType.IMAGE;
	
	@Override
	public void setPlottingSystem(IPlottingSystem system) {
		this.plottingSystem = system;
	}

	@Override
	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}
	public SliceObject getCurrentSlice() {
		return sliceObject;
	}

	@Override
	public void setDimsDataList(DimsDataList sliceSetup) {
		this.dimsDataList = sliceSetup;
	}

	@Override
	public DimsDataList getDimsDataList() {
		return dimsDataList;
	}
	
	/**
	 * May be implemented to save the current slice set up.
	 */
	protected abstract void saveSliceSettings();
	
	private ISlicingTool activeTool;

	/**
	 * Creates the slice tools by reading extension points
	 * for the slice tools.
	 * 
	 * @return
	 */
	protected IToolBarManager createSliceTools() {
				
		final ToolBarManager man = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		man.add(new Separator("sliceTools"));
		
		final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawnsci.slicing.api.slicingTool");

        final CheckableActionGroup grp = new CheckableActionGroup();
		plotTypeActions= new HashMap<Enum, Action>();

		for (IConfigurationElement e : eles) {
			
			final ISlicingTool slicingTool = createSliceTool(e);
			String toolTip = e.getAttribute("tooltip");
			if (toolTip==null) toolTip = slicingTool.getToolId();
			
			
	        final Action action = new Action(toolTip, IAction.AS_CHECK_BOX) {
	        	public void run() {
	        		saveSliceSettings();
	        		if (activeTool!=null) activeTool.demilitarize();
	        		slicingTool.militarize();
	        		activeTool = slicingTool;
	        	}
	        };
	        
	    	final String   icon  = e.getAttribute("icon");
	    	if (icon!=null) {
		    	final String   id    = e.getContributor().getName();
		    	final Bundle   bundle= Platform.getBundle(id);
		    	final URL      entry = bundle.getEntry(icon);
		    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
		    	action.setImageDescriptor(des);
	    	}

			action.setId(slicingTool.getToolId());
			man.add(action);
			grp.add(action);
			plotTypeActions.put(slicingTool.getSliceType(), action);

		}
								
		return man;
	}
	
	private  Map<Enum, Action> plotTypeActions;
	protected Action getActionByPlotType(Object plotType) {
		if (plotTypeActions==null) return null;
		return plotTypeActions.get(plotType);
	}


	
	/**
	 * 
	 * @param e
	 * @return
	 */
	private ISlicingTool createSliceTool(IConfigurationElement e) {
    	
		ISlicingTool tool = null;
    	try {
    		tool  = (ISlicingTool)e.createExecutableExtension("class");
    	} catch (Throwable ne) {
    		logger.error("Cannot create tool page "+e.getAttribute("class"), ne);
    		return null;
    	}
    	tool.setToolId(e.getAttribute("id"));	       	
    	tool.setSlicingSystem(this);
    	
    	// TODO Provide the tool with a reference to the part with the
    	// slice will end up being showed in?
    	
    	return tool;
	}


	@Override
	public void dispose() {
		if (dimensionalListeners!=null) dimensionalListeners.clear();
		dimensionalListeners = null;
	}

	@Override
	public void setSliceGalleryId(String id) {
		this.sliceReceiverId = id;
	}
	
	protected void openGallery() {
		
		if (sliceReceiverId==null) return;
		SliceObject cs;
		try {
			final SliceObject current = getCurrentSlice();
			final int[] dataShape     = getData().getLazySet().getShape();
			cs = SliceUtils.createSliceObject(dimsDataList, dataShape, current);
		} catch (Exception e1) {
			logger.error("Cannot create a slice!");
			return;
		}
		
		IViewPart view;
		try {
			view = getActivePage().showView(sliceReceiverId);
		} catch (PartInitException e) {
			logger.error("Cannot find view "+sliceReceiverId);
			return;
		}
		if (view instanceof ISliceGallery) {
			((ISliceGallery)view).updateSlice(getData().getLazySet(), cs);
		}
		
	}
	private static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench == null)
			return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}

	protected boolean rangesAllowed = false;
	public void setRangesAllowed(boolean isVis) {
		rangesAllowed = isVis;
	}

	public void addCustomAction(IAction customAction) {
		if (customActions == null)customActions = new ArrayList<IAction>();
		customActions.add(customAction);
	}
	
	protected void createCustomActions(IContributionManager man) {
		if (customActions!=null) {
			man.add(new Separator("group5"));
			for (IAction action : customActions) man.add(action);
		}
	}


	private Collection<DimensionalListener> dimensionalListeners;
	public void addDimensionalListener(DimensionalListener l) {
		if (dimensionalListeners==null) dimensionalListeners= new HashSet<DimensionalListener>(7);
		dimensionalListeners.add(l);
	}
	
	public void removeDimensionalListener(DimensionalListener l) {
		if (dimensionalListeners==null) return;
		dimensionalListeners.remove(l);
	}
	
	protected void fireDimensionalListeners() {
		if (dimensionalListeners==null) return;
		final DimensionalEvent evt = new DimensionalEvent(this, dimsDataList);
		for (DimensionalListener l : dimensionalListeners) {
			l.dimensionsChanged(evt);
		}
	}

	@Override
	public Enum getSliceType() {
		return sliceType;
	}

	@Override
	public void setSliceType(Enum plotType) {
		this.sliceType = plotType;
	}
	
	/**
	 * 
	 * @return true if the current slice type is a 3D one.
	 */
	protected boolean is3D() {
		return sliceType instanceof PlotType && ((PlotType)sliceType).is3D();
	}

	public ISlicingTool getActiveTool() {
		return activeTool;
	}

	/**
	 * Call this method if overriding.
	 */
	@Override
	public void setVisible(final boolean vis) {
		if (activeTool!=null) {
			try {
				if (vis) {
					activeTool.militarize();
				} else {
					activeTool.demilitarize();
				}
			} catch (Throwable ne) {
				logger.error("Cannot change militarized state of slice tool! "+activeTool.getToolId());
			}
		}
	}
}
