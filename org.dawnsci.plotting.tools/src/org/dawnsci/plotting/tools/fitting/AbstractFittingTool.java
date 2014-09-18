package org.dawnsci.plotting.tools.fitting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.util.list.SortNatural;
import org.dawnsci.plotting.tools.preference.FittingPreferencePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionSquirts;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionSquirts.Squirt;


public abstract class AbstractFittingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(AbstractFittingTool.class);
	
	protected Composite         composite;
	protected FittedFunctions   fittedFunctions;

	protected MenuAction        tracesMenu;
	protected List<ILineTrace>  selectedTraces;
	protected TableViewer       viewer;
	protected Link              algorithmMessage;
	protected FittingJob        fittingJob;

	private RectangularROI fitBounds;
	protected IRegion        fitRegion;

	private ISelectionChangedListener viewUpdateListener;
	private ITraceListener traceListener;

	private boolean addingPeaks=false;

	public AbstractFittingTool() {
		super();
		this.fittingJob = new FittingJob();
		
		/**
		 * Use Vector here intentionally. It is slower but
		 * synchronized which is required in this instance. 
		 */
		this.selectedTraces = new Vector<ILineTrace>(31);
		
		this.traceListener = new ITraceListener.Stub() {
			@Override
			public void tracesUpdated(TraceEvent evt) {
				fittingJob.schedule();
			}
			@Override
			public void tracesAdded(TraceEvent evt) {
				
				@SuppressWarnings("unchecked")
				final List<ITrace> traces = evt.getSource() instanceof List
				                          ? (List<ITrace>)evt.getSource()
				                          : null;
				if (traces!=null && fittedFunctions!=null && !fittedFunctions.isEmpty()) {
					traces.removeAll(fittedFunctions.getFittedTraces());
				}
				if (traces!=null && !traces.isEmpty()) {
					final int size = updateTracesChoice(traces.get(traces.size()-1));	
					if (size>0) fittingJob.fit(true);
				}
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (evt.getSource() instanceof ITrace) {
					if (!((ITrace)evt.getSource()).isUserTrace()) return;
				}
				updateTracesChoice(null);
			}
			
			@Override
			public void tracesRemoved(TraceEvent evet) {
				if (tracesMenu!=null) tracesMenu.clear();
				if (getSite()!=null) getSite().getActionBars().updateActionBars();
			}
			
			@Override
			public void traceAdded(TraceEvent evt) {
				//Get trace from event
				final ITrace trace = evt.getSource() instanceof ITrace ? ((ITrace)evt.getSource()): null;
				//Ignore event if null
				if (trace == null) return;
				//Ignore if not user trace
				if (!trace.isUserTrace()) return;
				
				if (fittedFunctions!=null && !fittedFunctions.isEmpty()) {
					if (fittedFunctions.getFittedTraces().contains(trace)) return;
				}

				final int size = updateTracesChoice(trace);	
				if (size>0) fittingJob.fit(true);

			}
		};

	}
	

	/**
	 * The fitted functions from the table for exporting.
	 */
	public List<FittedFunction> getSortedFunctionList() {
		final List<FittedFunction> ret = new ArrayList<FittedFunction>(3);
		for (int i = 0; i < viewer.getTable().getItemCount(); i++) {
			final FittedFunction f = (FittedFunction)viewer.getElementAt(i);
			ret.add(f);
		}
		return ret;
	}

	
	public void sync(IToolPage with) {
		if (!with.getClass().equals(getClass())) return;
		final AbstractFittingTool other = (AbstractFittingTool)with;
		this.fittedFunctions = other.fittedFunctions.clone();
		this.fitRegion   = other.fitRegion;
		this.tracesMenu = other.tracesMenu;
		this.selectedTraces = other.selectedTraces; 
		viewer.setInput(fittedFunctions);
        viewer.refresh();
        fittingJob.schedule();
	}
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(composite);

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        createColumns(viewer);
		
		FittingViewerComparator vc = new FittingViewerComparator();
		viewer.setComparator(vc);

		for (int i = 0; i < viewer.getTable().getColumnCount(); i++) {
			viewer.getTable().getColumn(i).addSelectionListener(getTableColumnSortListener(vc, i));
		}
        
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(createContentProvider());
		createActions();
				
		getSite().setSelectionProvider(viewer);
		
		this.viewUpdateListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection sel = (StructuredSelection)event.getSelection();
				if (fittedFunctions!=null && sel!=null && sel.getFirstElement()!=null) {
					if (sel.getFirstElement() instanceof NullFunction) return;
					fittedFunctions.setSelectedFit((FittedFunction)sel.getFirstElement());
					viewer.refresh();
				}
			}
		};
		viewer.addSelectionChangedListener(viewUpdateListener);
		
		algorithmMessage = new Link(composite, SWT.NONE);
		algorithmMessage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		algorithmMessage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				if (e.text!=null && e.text.startsWith("configure")) {
					if (!isActive()) return;
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), FittingPreferencePage.ID, null, null);
					if (pref != null) pref.open();
				} else {
					
				}
			}
		});
		activate();
	}
	
	protected SelectionListener getTableColumnSortListener(final FittingViewerComparator vc, final int index) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				vc.setColumn(index);
				int dir = vc.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.refresh();
			}
		};
	}

	/**
	 * Implement method to provide colums with information on the type of fit
	 * being done.
	 * 
	 * @param viewer
	 */
	protected abstract List<TableViewerColumn> createColumns(TableViewer viewer);

	private IContentProvider createContentProvider() {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {

				if (fittedFunctions==null)    return new NullFunction[]{new NullFunction()};
				if (fittedFunctions.size()<1) return new NullFunction[]{new NullFunction()};
				
				return fittedFunctions.toArray();
			}
		};
	}
	

	@Override
	public void activate() {
		
		if (getPlottingSystem()==null) return;
		if (isDisposed()) return;
		
		super.activate();
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		
		if (viewUpdateListener!=null) viewer.addSelectionChangedListener(viewUpdateListener);
		if (this.traceListener!=null) getPlottingSystem().addTraceListener(traceListener);
		updateTracesChoice(null);
		
		createNewFit();
	}
	
	/**
	 * Method to start new selection area for fitting.
	 */
	protected void createNewFit() {
		try {
			if (fittedFunctions!=null) fittedFunctions.activate();
			getPlottingSystem().addRegionListener(this);
			this.fitRegion = getPlottingSystem().createRegion(RegionUtils.getUniqueName("Fit selection", getPlottingSystem()), 
					                                          getPlottingSystem().is2D() ? IRegion.RegionType.BOX : IRegion.RegionType.XAXIS);
			fitRegion.setRegionColor(ColorConstants.green);

			if (viewer!=null) {
				viewer.refresh();
			}

		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}
		
	}

	@Override
	public void deactivate() {
		
		super.deactivate();
		if (viewer!=null && !viewer.getControl().isDisposed()) {		
		    if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
		}
		
		if (getPlottingSystem()!=null) {
			if (this.traceListener!=null) getPlottingSystem().removeTraceListener(traceListener);
	
			try {
				getPlottingSystem().removeRegionListener(this);
				if (fittedFunctions!=null) fittedFunctions.deactivate();
				
			} catch (Exception e) {
				logger.error("Cannot put the selection into fitting region mode!", e);
			}		
		}
	}

	@Override
	public void setFocus() {
        if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	public void dispose() {
		deactivate();
		
        // Using clear and setting to null helps the garbage collector.
        if (fittedFunctions!=null) {
			fittedFunctions.removeSelections(getPlottingSystem(), true);
        	fittedFunctions.dispose();
        }
        fittedFunctions = null;

		viewUpdateListener = null;
		selectedTraces.clear();
        if (viewer!=null) viewer.getControl().dispose();
              
		super.dispose();
	}


	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		
		
	}
	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void regionNameChanged(RegionEvent evt, String oldName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		if (evt==null || evt.getRegion()==null) {
			getPlottingSystem().clearRegions();
			return;
		}
		if (evt.getRegion()==fitRegion) {
			fittingJob.fit(false);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		
		
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		
	}
	
	protected void setAddingPeaks(boolean addPeaksToList) {
		this.addingPeaks = addPeaksToList;
	}

	protected final class FittingJob extends Job {

		boolean autoUpdate;
		private List<FittedFunction> previousFittedFunctions;

		public FittingJob() {
			super("Fit peaks");
			setPriority(Job.INTERACTIVE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (composite==null)        return Status.CANCEL_STATUS;
			if (composite.isDisposed()) return Status.CANCEL_STATUS;

			if (fitRegion == null) return Status.CANCEL_STATUS;
			RectangularROI bounds = (RectangularROI) fitRegion.getROI();
			if (bounds == null && !autoUpdate) return Status.CANCEL_STATUS;
			if (bounds == null && autoUpdate) {
				if (fittedFunctions == null) return Status.CANCEL_STATUS;
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						previousFittedFunctions = new ArrayList<FittedFunction>(fittedFunctions.getFunctionList());
						Collections.sort(previousFittedFunctions, new SortNatural<FittedFunction>(true));
						clearAll();
					}
				});
				for (FittedFunction function : previousFittedFunctions) {
					IRegion region = function.getFwhm();
					if (region != null && region.getROI() instanceof RectangularROI) {
						// change the fwhm by 2xfwhm where
						// <--width--> becomes <--width/2--><--width--><--width/2-->
						RectangularROI rectangle = (RectangularROI)region.getROI();
						double width = rectangle.getLength(0);
						double xPoint = rectangle.getPointX();
						double[] startPoint = rectangle.getPoint();
						double[] endPoint = rectangle.getEndPoint();
						double newStartXPoint = xPoint - (width/2);
						double newEndXPoint = endPoint[0] + (width/2);
						rectangle.setPoint(newStartXPoint, startPoint[1]);
						rectangle.setEndPoint(new double[]{newEndXPoint, endPoint[1]});
						IStatus status = doFit(rectangle, monitor);
						if (status == Status.CANCEL_STATUS) return Status.CANCEL_STATUS;
					}
				}
			} else if (bounds != null)
				return doFit(bounds, monitor);
			return Status.OK_STATUS;
		}

		private IStatus doFit(RectangularROI bounds, IProgressMonitor monitor) {
			setFitBounds(bounds);
			getPlottingSystem().removeRegionListener(AbstractFittingTool.this);

			composite.getDisplay().syncExec(new Runnable() {
				public void run() {
					getPlottingSystem().removeRegion(fitRegion);
					if (fittedFunctions!=null && !addingPeaks) {
						fittedFunctions.removeSelections(getPlottingSystem(), false);
					}
				}
			});
			if (selectedTraces.isEmpty())    return Status.CANCEL_STATUS;

			if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
			for (ILineTrace selectedTrace : selectedTraces) {
				
				// We chop x and y by the region bounds. We assume the
				// plot is an XAXIS selection therefore the indices in
				// y = indices chosen in x.
				final double[] p1 = bounds.getPointRef();
				final double[] p2 = bounds.getEndPoint();
	
				// We peak fit only the first of the data sets plotted for now.
				if (selectedTrace==null || selectedTrace.getXData()==null || selectedTrace.getYAxis()==null) continue;
				Dataset x  = (Dataset)selectedTrace.getXData().squeeze();
				Dataset y  = (Dataset)selectedTrace.getYData().squeeze();
				
				if (monitor.isCanceled()) break;
	
				try {
					Dataset[] a= Generic1DFitter.xintersection(x,y,p1[0],p2[0]);
					x = a[0]; y=a[1];
				} catch (Throwable npe) {
					logger.debug("Cannot fit!", npe);
					continue;
				}
	
				try {
					final FittedFunctions bean = getFittedFunctions(new FittedPeaksInfo(x, y, new ProgressMonitorWrapper(monitor), getPlottingSystem(), selectedTrace));
					if (bean!=null) for (FittedFunction p : bean.getFunctionList()) {
		    			p.setX((Dataset)selectedTrace.getXData());
		    			p.setY((Dataset)selectedTrace.getYData());
		    			p.setDataTrace(selectedTrace);
					}
					// Add saved peaks if any.
		    		if (fittedFunctions!=null && !fittedFunctions.isEmpty() && bean!=null) {
		    			bean.addFittedFunctions(fittedFunctions.getFunctionList());
		    		}
					createFittedFunctionUI(bean);
					pushFunctionsToPlotter();
				} catch (Exception ne) {
					logger.error("Cannot fit functions!", ne);
					return Status.CANCEL_STATUS;
				}
			}
			
			if (addingPeaks) composite.getDisplay().syncExec(new Runnable() {
				public void run() {
					createNewFit();
				}
			});
			return Status.OK_STATUS;
		}

		/**
		 * 
		 * @param autoUpdate
		 */
		public void fit(boolean autoUpdate) {
			this.autoUpdate = autoUpdate;
			cancel();
			schedule();
		}
	};

	/**
	 * If the plottingsystem is in a PlotView, we push the functions to the GuiBean
	 */
	abstract void pushFunctionsToPlotter();

	/**
	 * 
	 * @param fittedPeaksInfo
	 * @return
	 */
	protected abstract FittedFunctions getFittedFunctions(FittedPeaksInfo fittedPeaksInfo) throws Exception;

	/**
	 * Creates specific UI for the function.
	 * @param newBean
	 */
	protected abstract void createFittedFunctionUI(final FittedFunctions newBean);
	
	/**
	 * Used by the wizard to write the tools results to a file.
	 * @param path
	 * @return
	 * @throws Exception
	 */
	abstract String exportFittedData(final String path) throws Exception;


	protected int updateTracesChoice(ITrace selected) {
		
		if (tracesMenu==null) return 0;
		
		tracesMenu.clear();
		
		final Collection<ITrace> traces = getPlottingSystem().getTraces();
		if (traces==null || traces.size()<0) return 0;
		if (fittedFunctions!=null) traces.removeAll(fittedFunctions.getFittedTraces());
		
		selectedTraces.clear();
		int index = 0;
		//int selectionIndex=0;
		
		if (traces.size()>3) {
			final Action selectAll = new Action("Select all", IAction.AS_PUSH_BUTTON) {
				public void run() {
					selectedTraces.clear();
					for (int i = 2; i < tracesMenu.size(); i++) {
						TraceSelectAction ta = (TraceSelectAction)tracesMenu.getAction(i);
						ta.setChecked(true);
						selectedTraces.add(ta.iTrace);
					}
					if (fittingJob!=null&&isActive()) {
						fittingJob.fit(false);
					}
				}
			};
			tracesMenu.add(selectAll);			
			final Action selectNone = new Action("Select none", IAction.AS_PUSH_BUTTON) {
				public void run() {
					clearAll();
					selectedTraces.clear();
					for (int i = 2; i < tracesMenu.size(); i++) {
						TraceSelectAction ta = (TraceSelectAction)tracesMenu.getAction(i);
						ta.setChecked(false);
					}
					if (fittingJob!=null&&isActive()) {
						fittingJob.fit(false);
					}
				}
			};
			tracesMenu.add(selectNone);			
		}
		
		for (final ITrace iTrace : traces) {
			if (!(iTrace instanceof ILineTrace)) continue;
			
			final ILineTrace lineTrace = (ILineTrace)iTrace;
			
			if (!lineTrace.isUserTrace()) continue;
			
			//if no trace selected, use first valid trace
			if (selected == null) selected = lineTrace;
			
			//Make the selected trace the fitted trace
			if (iTrace==selected) {
				selectedTraces.add(lineTrace);
			}		
			
			final Action action = new TraceSelectAction(lineTrace);
			
			if (iTrace==selected) {
				action.setChecked(true);
			}	
			tracesMenu.add(action);
			
			index++;
		}

		getSite().getActionBars().updateActionBars();
		
		return index;
	}

	private class TraceSelectAction extends Action {
		ILineTrace iTrace;
		TraceSelectAction(ILineTrace iTrace) {
			super(iTrace.getName(), IAction.AS_CHECK_BOX);
			this.iTrace = iTrace;
		}
	
		public void run() {
			if (iTrace instanceof ILineTrace) {
				ILineTrace lt = (ILineTrace)iTrace;
				if (isChecked()) {
					selectedTraces.add(lt);
				} else {
					selectedTraces.remove(lt);
				}
			}
			tracesMenu.setSelectedAction(this);
			if (fittingJob!=null&&isActive()) {
				fittingJob.fit(false);
			}
		}
	};

	/**
	 * Called to create actions for this tool.
	 */
	protected abstract void createActions();
	
	protected void clearAll() {
		if (!isActive()) return;
		if (fittedFunctions!=null) {
			fittedFunctions.removeSelections(getPlottingSystem(), true);
			fittedFunctions.dispose();
			fittedFunctions = null;
						
			pushFunctionsToPlotter();
		}
		
		// We sometimes get lagging regions, delete these too.
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion iRegion : regions) {
			if (iRegion.getUserObject()==FittedFunction.class) {
				getPlottingSystem().removeRegion(iRegion);
			}
		}

		viewer.refresh();
	}

	public void setFittedFunctions(FittedFunctions fittedFunctions) {
		this.fittedFunctions = fittedFunctions;
	}

	public RectangularROI getFitBounds() {
		return fitBounds;
	}

	public void setFitBounds(RectangularROI fitBounds) {
		this.fitBounds = fitBounds;
	}

	@Override
	public Serializable getToolData() {
		
		if (fittedFunctions==null) return null;
		final FunctionSquirts fs = new FunctionSquirts();
		
		for (FittedFunction ff : fittedFunctions.getFunctionList()) {
			fs.addSquirt(getSquirt(ff));
		}
		fs.setSelected(getSquirt(fittedFunctions.getSelectedPeak()));
		
		return fs;
	}


	private Squirt getSquirt(FittedFunction ff) {
		if (ff == null) return null;
		final Squirt skwert = new Squirt();
		skwert.setBounds(ff.getRoi());
		skwert.setFunction(ff.getFunction());
		skwert.setName(ff.getPeakName());
		skwert.setPeakFunctions(ff.getPeakFunctions());
		skwert.setRegions(ff.getRegions());
		skwert.setX(ff.getX());
		skwert.setY(ff.getY());
		return skwert;
	}

}
