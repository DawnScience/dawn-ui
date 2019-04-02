/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.dawb.common.ui.plot.tools.HistoryType;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.views.PlotDataView;
import org.dawb.workbench.ui.Activator;
import org.dawb.workbench.ui.data.PlotDataComponent;
import org.dawb.workbench.ui.editors.preference.EditorConstants;
import org.dawb.workbench.ui.views.PlotDataPage;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.ProgressMonitorWrapper;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.expressions.IVariableManager;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataManager;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.dawnsci.slicing.api.editor.ISelectedPlotting;
import org.eclipse.dawnsci.slicing.api.editor.ISlicablePlottingPart;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.plotclient.ScriptingConnection;


/**
 * An editor which combines a plot with a graph of data sets.
 * 
 * 
 */
public class PlotDataEditor extends EditorPart implements IReusableEditor, ISlicablePlottingPart, ITitledEditor, ISelectedPlotting {
	
	private static Logger logger = LoggerFactory.getLogger(PlotDataEditor.class);
	
	// This view is a composite of two other views.
	private IPlottingSystem<Composite>  plottingSystem;	
	private PlotType                    defaultPlotType;
	private PlotJob                     plotJob;
	private InitJob                     initJob;
	private ActionBarWrapper            wrapper;
	private ReentrantLock               lock;
	private ITitledEditor               parent;

	private ScriptingConnection connection;

	public PlotDataEditor(final PlotType defaultPlotType) {
		this(defaultPlotType, null);
	}
	
	public PlotDataEditor(final PlotType defaultPlotType, ITitledEditor parent) {
		
	    this.plotJob = new PlotJob();
	    this.initJob = new InitJob();
	    this.lock    = new ReentrantLock();
	    this.parent  = parent;
		try {
			this.defaultPlotType= defaultPlotType;
	        this.plottingSystem = PlottingFactory.createPlottingSystem();
	        
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
 	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input, false);
	}

	@Override
	public Map<String, IDataset> getSelected() {
		
		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
        final Map<String,IDataset> ret = new HashMap<String, IDataset>(3);
		if (dataSetComponent==null) return ret;
		
        final List<ITransferableDataObject> selectedNames = dataSetComponent.getSelections();
        for (ITransferableDataObject object : selectedNames) {
        	final IDataset set = object.getData(null);
        	if (set==null) continue;
         	ret.put(set.getName(), set);
        }
		return ret;
	}

	@Override
	public boolean isDirty() {
		return false;
	}
	

	public void setToolbarsVisible(boolean isVisible) {
		wrapper.setVisible(isVisible);
	}

	@Override
	public void createPartControl(final Composite parent) {

		final Composite  main       = new Composite(parent, SWT.NONE);
		Color bgdColour = parent.getBackground();
		main.setBackground(bgdColour!=null? bgdColour:new Color(Display.getDefault(), 255, 255, 255));

		final GridLayout gridLayout = new GridLayout(1, false);
		main.setLayout(gridLayout);
		GridUtils.removeMargins(main);
		
		final IActionBars bars = this.getEditorSite().getActionBars();
		this.wrapper = ActionBarWrapper.createActionBars(main,(IActionBars2)bars);
				
		// NOTE use name of input. This means that although two files of the same
		// name could be opened, the editor name is clearly visible in the GUI and
		// is usually short.
		final String plotName = this.getEditorInput().getName();
		
		final Composite plot  = new Composite(main, SWT.NONE);
		plot.setLayout(new FillLayout());
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plottingSystem.createPlotPart(plot, plotName, wrapper, defaultPlotType, this);
		((AbstractPlottingSystem<?>)plottingSystem).addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (IPlottingSystem.RESCALE_ID.equals(event.getProperty())) {
					Activator.getDefault().getPreferenceStore().setValue(EditorConstants.RESCALE_SETTING, (Boolean)event.getNewValue());
				} else {
					setAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
					setAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());
				}
				
			}
		});
		getAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
		getAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());
       
		// Finally
		if (wrapper!=null)  wrapper.update(true);
        initJob.schedule(getEditorInput());	
	    
		// We ensure that the view for choosing data sets is visible
		if (EclipseUtils.getActivePage()!=null) {
			try {
				EclipseUtils.getActivePage().showView(PlotDataView.ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				logger.error("Cannot open "+PlotDataView.ID);
			}
		}
		
		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());

		plottingSystem.setRescale(Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.RESCALE_SETTING));
		
		// Script connection
		this.connection = new ScriptingConnection(getPartName());
		connection.setPlottingSystem(plottingSystem);

 	}

	protected void setAxisSettings(String propertyStub, IAxis axis) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean isDateTime = axis.isDateFormatEnabled();
		store.setValue(propertyStub+"isDateTime", isDateTime);
		if (isDateTime) {
			String format = axis.getFormatPattern();
			store.setValue(propertyStub + "dateFormat", format);
		}
		boolean isLog = axis.isLog10();
		store.setValue(propertyStub + "log10", isLog);
	}

	protected void getAxisSettings(String propertyStub, IAxis axis) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		boolean isDateTime = false;
		if (store.contains(propertyStub+"isDateTime")) {
			isDateTime = store.getBoolean(propertyStub+"isDateTime");
			axis.setDateFormatEnabled(isDateTime);
		}
		if (isDateTime && store.contains(propertyStub+"dateFormat")) {
			axis.setFormatPattern(store.getString(propertyStub+"dateFormat"));
		}
		if (store.contains(propertyStub+"log10")) {
			axis.setLog10(store.getBoolean(propertyStub+"log10"));
		}
	}
	/**
	 * Call to change plot default by looking at data
	 */
	public void createPlotTypeDefaultFromData() {
	    // If the data is only 2D we tell the PlottingSystem to switch to 2D mode.
		boolean is1D = false;
		
		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
 		if (dataSetComponent!=null) {
			for (ITransferableDataObject set : dataSetComponent.getData()) {
				final int[] shape = set.getShape(true);
				if (shape.length==1)	{
					is1D=true;
					break;
				}
			}
 		}
		if (!is1D) {
			getPlottingSystem().setPlotType(PlotType.IMAGE);
		}

	}

	
	private boolean doingUpdate = false;	
	/**
	 * Must be called in UI thread
	 * @param selections
	 * @param useTask
	 */
	@Override
	public void updatePlot(final ITransferableDataObject[]     selections, 
			               final ISliceSystem           sliceSystem,
			               final boolean                useTask) {
		
		if (doingUpdate) return;
		
		if (selections==null || selections.length<1) {
			if (sliceSystem!=null) sliceSystem.setVisible(false);
		}
		
		try {
			doingUpdate = true;
			if (selections==null||selections.length<1) {
				plottingSystem.reset();
				return;
			}
			
			final int[] shape = selections[0].getShape(true);
			if (selections.length==1 && shape.length!=1) {
				
				ITransferableDataObject object = selections[0];
				sliceSystem.setVisible(true);
				
				final IVariableManager man  = (IVariableManager)getAdapter(IVariableManager.class);
				final ILazyDataset     lazy = selections[0].getLazyData(null);
				sliceSystem.setData(new SliceSource(man, lazy, object.getName(), EclipseUtils.getFilePath(getEditorInput()), object.isExpression()));

				return;
			}
			
			sliceSystem.setVisible(false);
			
			if (useTask) {
				plotJob.plot(selections, sliceSystem); 
			} else {
				createPlot(selections, sliceSystem, new NullProgressMonitor());
			}
		} finally {
			doingUpdate = false;
		}
	}

	private class PlotJob extends Job {

		public PlotJob() {
			super("Plot update");
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		private ITransferableDataObject[] selections;
		private ISliceSystem system;

		public void plot(ITransferableDataObject[] selections,
				         ISliceSystem system) {
			
			cancel();
            this.selections  = selections;
            this.system = system;
            schedule();
			
		}
		
		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Updating selected DataSets", 100);		
			createPlot(selections, system, monitor);
			return Status.OK_STATUS;
		}
	}
	
	
	private void createPlot(final ITransferableDataObject[] selections, final ISliceSystem system, final IProgressMonitor monitor) {
		

		if (monitor.isCanceled()) return;
		
		final ITransferableDataObject first = selections[0];
		IDataset        data = first.getData(new ProgressMonitorWrapper(monitor));	
		if (data==null)             return;
		data = data.squeeze();
		try {
		    if (data.getSize()<0) return;
		} catch (Exception ne) {
			return;
		}
		if (data.getRank()>2)       return; // Cannot plot more that 2 dims!
		
		if (data.getRank()==2) {
			
			plottingSystem.clear();
			// TODO Data Name
		    plottingSystem.createPlot2D(data, null, first.getName(), monitor);
		    
		} else {
			List<ITransferableDataObject> sels = new ArrayList<ITransferableDataObject>(Arrays.asList(selections));

			final IDataset x;
			if (plottingSystem.isXFirst() && sels.size()>1) {
				x = data;
				sels.remove(0);
			} else {
				x = null;
			}
			
			if (sels.isEmpty() || (!plottingSystem.isXFirst() && sels.size()==1)) { // why is this a special case???
				
				// TODO Data Name
				
				final List<ITrace> traces = plottingSystem.updatePlot1D(x, Arrays.asList(data), Arrays.asList(first.getName()), monitor);
				removeOldTraces(traces, null);
		        sync(sels,traces);
		        if (plottingSystem.isRescale()) plottingSystem.repaint();
				return;
			}
			

            final Map<Integer,List<IDataset>> ys = sels.isEmpty()
                                                 ? null
                                		         : new HashMap<Integer,List<IDataset>>(4);

            final Map<Integer,List<String>> dataNames = sels.isEmpty()
                                                 ? null
   		                                         : new HashMap<Integer,List<String>>(4);

            // Sort ys by axes (for 2D there is one y)
            if (!sels.isEmpty()) {
        	   for (int i = 1; i <= 4; i++) {
        		   getYS(i, sels, monitor, ys, dataNames);
         	   }
            }

    		final List<ITrace> traces = createPlotSeparateAxes(x,ys,dataNames,monitor);
	        sync(sels,traces);

		}
		
	    plottingSystem.repaint();
		
		monitor.done();
	}

	/**
	 * Records any expression names so that they can be available in the history tool.
	 * @param sels
	 * @param traces
	 */
	private void sync(List<ITransferableDataObject> sels, List<ITrace> traces) {
		if (sels==null || traces==null) return ;
		if (sels.size() == traces.size()) {
			for (int i = 0; i < sels.size(); i++) {
				if (traces.get(i).getUserObject()==null || traces.get(i).getUserObject() instanceof String) {
					traces.get(i).setUserObject(sels.get(i).getVariable());
				}
			}
		}
		
	}

	/**
	 * @return current y axes
	 */
	private List<IAxis> getYAxes() {
		final List<IAxis> yAxes = new ArrayList<>();
		for (IAxis a : plottingSystem.getAxes()) {
			if (a.isYAxis()) {
				yAxes.add(a);
			}
		}
		return yAxes;
	}

	protected List<ITrace> createPlotSeparateAxes(final IDataset                    x,
										          final Map<Integer,List<IDataset>> ysMap,
										          final Map<Integer,List<String>>   dNameMap,
			                                      final IProgressMonitor            monitor) {

		final List<ITrace> traces = new ArrayList<ITrace>();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IAxis ax = plottingSystem.getSelectedXAxis();
				if (ax != null) {
					ax.setTitle(x == null ? "X-Axis" : x.getName());
				}
				List<IAxis> yAxes = getYAxes();
				try { // TODO magic constant!!!
					for (int i = 1; i <= 4; i++) {
						final List<IDataset> ys = ysMap.get(i);
						int n = yAxes.size();
						IAxis a = i <= n ? yAxes.get(i-1) : null;

						if (ys == null) {
							if (a != null) {
								a.setVisible(false);
							}
							continue;
						}

						if (a == null) {
							String an = "Y" + i;
							a = plottingSystem.createAxis(an, true, i==3||i==4?SWT.RIGHT:SWT.LEFT);
							a.setTitle(an);
						}

						a.setVisible(true);
						plottingSystem.setSelectedYAxis(a);
						final List<String> dn = dNameMap.get(i);
						
                        // Tell traces its data name.
						final List<ITrace> plotted = plottingSystem.updatePlot1D(x, ys, dn, monitor);
						traces.addAll(plotted);
					}

					// Remove traces in the plotting system that were not
					// in this round of plotting.
					removeOldTraces(traces, yAxes);

				} finally {
					plottingSystem.setSelectedYAxis(yAxes.get(0));
				}
			}
		});
		
        if (plottingSystem.isRescale()) plottingSystem.repaint();
		return traces;

	}

	private void removeOldTraces(final List<ITrace> traces, final List<IAxis> yAxes) {
        Display.getDefault().syncExec(new Runnable() {
        	public void run() {
        		List<IAxis> axes = yAxes != null ? yAxes : getYAxes();
				final Collection<ITrace> existing = plottingSystem.getTraces();
				existing.removeAll(traces);
				for (ITrace iTrace : existing) {
					String tn = iTrace.getName();
					if (iTrace.getUserObject()==HistoryType.HISTORY_PLOT) continue;
					for (IAxis a : axes) {
						if (tn.equals(a.getTitle())) {
							a.setVisible(false);
							break;
						}
					}
					plottingSystem.removeTrace(iTrace);
				}

				// ensure selected y is visible and has a default name if plotting more than one 
				IAxis ay = plottingSystem.getSelectedYAxis();
				if (!ay.isVisible() || traces.size() > 1) {
					ay.setTitle("Y-Axis");
					ay.setVisible(true);
				}
        	}
        });
	}

	private void getYS(int iyaxis, List<ITransferableDataObject> selections, IProgressMonitor monitor, 
			           Map<Integer,List<IDataset>> yMap, Map<Integer,List<String>> dMap) {
		
		List<IDataset> ys = new ArrayList<IDataset>(3);
		List<String>   dn = new ArrayList<String>(3);
		for (ITransferableDataObject co : selections) {
			
			if (co.getYaxis()!=iyaxis) continue;
			final IDataset y = co.getData(new ProgressMonitorWrapper(monitor));
			ys.add(y.squeeze());
			dn.add(co.getName());
			if (monitor.isCanceled()) return;
			monitor.worked(1);
		}
		yMap.put(iyaxis, ys.isEmpty()?null:ys);
		dMap.put(iyaxis, dn.isEmpty()?null:dn);
	}

	@Override
	public void setInput(final IEditorInput input) {
		setInput(input, true);
	}

	private void setInput(final IEditorInput input, boolean createData) {
		
		super.setInput(input);
		setPartName(input.getName());

		if (createData) initJob.schedule(input);
	}
	
    /**
     * Method used to select all 1D plots in the order they are extracted.
     * If overide then current selected plots will be ignored.
     */
	public void setAll1DSelected(final boolean overide) {
		
		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
		dataSetComponent.setAll1DSelected(overide);
	}

	private class InitJob extends Job {
		
		private IEditorInput input;
		InitJob() {
			super("Update data");
			setUser(false);
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}
		
		public void schedule(IEditorInput i) {
			cancel();
			input = i;
			schedule();
		}

		@Override
		public IStatus run(final IProgressMonitor monitor) {
			try {					
				lock.lock();
				
				// Load data in Job
				final String       path       = EclipseUtils.getFilePath(input);
				final IDataHolder  dataHolder = LoaderFactory.getData(path, true, true, new ProgressMonitorWrapper(monitor));
				if (dataHolder == null) return Status.CANCEL_STATUS;
				final IMetadata    meta       = dataHolder.getMetadata();
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;

				// Update UI
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							if (monitor.isCanceled()) return;
							setFocus();
							final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
					 		if (dataSetComponent==null) return;
						    dataSetComponent.setData(dataHolder, meta, true);
							dataSetComponent.refresh();
							((AbstractPlottingSystem<?>)getPlottingSystem()).setRootName(dataSetComponent.getRootName());
						} catch (Throwable ignored) {
							// Editor might not be valid but still open.
						}
					}
				});
				return Status.OK_STATUS;
				
			} catch (Exception ne) {
				logger.error("Cannot open nexus", ne);
				return Status.CANCEL_STATUS;
			} finally {
				lock.unlock();
			}
		}
	}


	@Override
	public void setFocus() {
		
		if (plottingSystem!=null) {
			plottingSystem.setFocus();
		}
		
		final PlotDataComponent pc = (PlotDataComponent)getDataSetComponent();
		if (pc!=null && (pc.getData()==null || pc.getData().isEmpty())) {
			// Fix for jira.diamond.ac.uk/browse/SCI-5313
			IWorkbenchPart part = pc.getEditor();
			if (part instanceof IEditorPart) {
				IEditorInput input = ((IEditorPart)part).getEditorInput();
				initJob.schedule(input);
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

    @Override
    public void dispose() {

    	connection.dispose();
    	
     	if (plottingSystem!=null) plottingSystem.dispose();

     	super.dispose();
    }

	public IVariableManager getDataSetComponent() {
		
		final IWorkbenchPage wb =EclipseUtils.getActivePage();
		if (wb==null) return null;
		
		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
		if (view==null) return null;
		
		IPage page = view.getCurrentPage();
		if (!(page instanceof PlotDataPage)) return null;
		return ((PlotDataPage)page).getDataSetComponent();
	}
	
	public ISliceSystem getSliceComponent() {
		
		final IWorkbenchPage wb =EclipseUtils.getActivePage();
		if (wb==null) return null;
		
		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
		if (view==null) return null;
		
		IPage page = view.getCurrentPage();
		if (!(page instanceof PlotDataPage)) return null;
		return ((PlotDataPage)page).getSliceComponent();
	}

	public IPlottingSystem<Composite> getPlotWindow() {
		return plottingSystem;
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return this.plottingSystem;
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(final Class clazz) {
		
    	
		if (clazz == Page.class) {
			try {
				lock.lock();
				return PlotDataPage.getPageFor(this);
			} finally {
				lock.unlock();
			}
		} else if (clazz == IToolPageSystem.class || clazz == IPlottingSystem.class) {
			return getPlottingSystem();
		} else if (clazz == ISliceSystem.class) {
			return getSliceComponent();
		}else if (clazz == IVariableManager.class) {
			return getDataSetComponent();
		}else if (clazz == ITransferableDataManager.class) {
			return (ITransferableDataManager)getDataSetComponent();
		}
		
		return super.getAdapter(clazz);
	}

	public String toString(){
		if (getEditorInput()!=null) return getEditorInput().getName();
		return super.toString();
	}

	@Override
	public void setPartTitle(String name) {
		super.setPartName(name);
		if (parent!=null) parent.setPartTitle(name);
	}	
}
