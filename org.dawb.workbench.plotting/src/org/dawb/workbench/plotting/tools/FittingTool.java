package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class FittingTool extends AbstractToolPage implements IRegionListener {

	private static final Logger logger = LoggerFactory.getLogger(FittingTool.class);
	
	private Composite     composite;
	private TableViewer   viewer;
	private IRegion       fitRegion;
	private Job           fittingJob;
	private FittedPeaksBean bean;

	public FittingTool() {
		super();
		this.fittingJob = createFittingJob();
	}

	@Override
	public void createControl(Composite parent) {
		
		this.composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        createColumns(viewer);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		createActions();
				
		getSite().setSelectionProvider(viewer);
		
		activate();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection sel = (StructuredSelection)event.getSelection();
				if (bean!=null) bean.setSelectedPeak((Integer)sel.getFirstElement());
			}
		});
	}

	private void createActions() {
		
		final MenuAction numberPeaks = new MenuAction("Number peaks to fit");
		final CheckableActionGroup group = new CheckableActionGroup();
		
		final int npeak = Activator.getDefault().getPreferenceStore().getDefaultInt(FittingConstants.PEAK_NUMBER_CHOICES);
		for (int ipeak = 1; ipeak <= npeak; ipeak++) {
			
			final Action action = new Action(String.valueOf(ipeak), IAction.AS_CHECK_BOX) {
				public void run() {
					Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_NUMBER, Integer.valueOf(getText()));
					numberPeaks.setSelectedAction(this);
					setChecked(true);
					fittingJob.schedule();
				}
			};
			numberPeaks.add(action);
			group.add(action);
			action.setChecked(false);
			action.setToolTipText("Fit "+ipeak+" peak(s)");
			
		}

		final int ipeak = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
		numberPeaks.setSelectedAction(ipeak);
		numberPeaks.setCheckedAction(ipeak, true);
		

		getSite().getActionBars().getToolBarManager().add(numberPeaks);
		getSite().getActionBars().getMenuManager().add(numberPeaks);
		
		
		final Action clear = new Action("Clear", Activator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
			public void run() {
				if (bean!=null) bean.removeSelections(getPlottingSystem());
				bean.dispose();
				bean = null;
				viewer.setContentProvider(createActorContentProvider(0));
				viewer.setInput(null);
			}
		};
		clear.setToolTipText("Clear all regions found in the fitting");
		
		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);
		
		createRightClickMenu();
	}
	
	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
	    for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems()) menuManager.add(item);
	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
	}

	private void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new FittingLabelProvider(0));
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Position");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FittingLabelProvider(1));

        var   = new TableViewerColumn(viewer, SWT.CENTER, 2);
		var.getColumn().setText("FWHM");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FittingLabelProvider(2));
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Area");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FittingLabelProvider(3));

        var   = new TableViewerColumn(viewer, SWT.CENTER, 4);
		var.getColumn().setText("Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FittingLabelProvider(4));
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 5);
		var.getColumn().setText("Algorithm");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new FittingLabelProvider(5));

	}
	
	private IContentProvider createActorContentProvider(final int numerOfPeaks) {
		return new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {

				if (numerOfPeaks<0) return new Integer[]{0};
				
				List<Integer> indices = new ArrayList<Integer>(numerOfPeaks);
                for (int ipeak = 0; ipeak < numerOfPeaks; ipeak++) {
                	indices.add(ipeak); // autoboxing
				}
				return indices.toArray(new Integer[indices.size()]);
			}
		};
	}
	

	@Override
	public void activate() {
		super.activate();
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		try {
			if (bean!=null) bean.activate();
			getPlottingSystem().addRegionListener(this);
			this.fitRegion = getPlottingSystem().createRegion("Fit selection", IRegion.RegionType.XAXIS);
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
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		try {
			getPlottingSystem().removeRegionListener(this);
			if (bean!=null) bean.deactivate();
			
		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}		
	}

	@Override
	public void setFocus() {
        if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
	}
	
	public void dispose() {
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(this);
		}
        if (viewer!=null) viewer.getControl().dispose();
       
        // Using clear and setting to null helps the garbage collector.
        if (bean!=null) bean.dispose();
        bean = null;
        
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
	public void regionAdded(RegionEvent evt) {
		if (evt==null || evt.getRegion()==null) {
			getPlottingSystem().clearRegions();
			return;
		}
		if (evt.getRegion()==fitRegion) {
			fittingJob.schedule();
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		
		
	}


	public Job createFittingJob() {
		
		final Job fit = new Job("Fit peaks") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				if (composite==null)        return Status.CANCEL_STATUS;
				if (composite.isDisposed()) return Status.CANCEL_STATUS;
				
				
				final RegionBounds bounds = fitRegion.getRegionBounds();
				if (fitRegion==null || bounds==null) return Status.CANCEL_STATUS;
				
				getPlottingSystem().removeRegionListener(FittingTool.this);
				
				composite.getDisplay().syncExec(new Runnable() {
					public void run() {
						getPlottingSystem().removeRegion(fitRegion);
						if (bean!=null) bean.removeSelections(getPlottingSystem());
				    }
				});
				
				final Collection<ITrace> traces = getPlottingSystem().getTraces();
				if (traces==null || traces.size()<0) return Status.CANCEL_STATUS;

				// We chop x and y by the region bounds. We assume the
				// plot is an XAXIS selection therefore the indices in
				// y = indices chosen in x.
				final double[] p1 = bounds.getP1();
				final double[] p2 = bounds.getP2();
				
				// We peak fit only the first of the data sets plotted for now.
				final ILineTrace   line  = (ILineTrace)traces.iterator().next(); //TODO What if more than one dataset?
				AbstractDataset x  = line.getXData();
				AbstractDataset y  = line.getYData();
				
				AbstractDataset[] a= FittingUtils.xintersection(x,y,p1[0],p2[0]);
				x = a[0]; y=a[1];
				
				final FittedPeaksBean bean = FittingUtils.getFittedPeaks(x, y, monitor);
				createFittedPeaks(bean);
				
				return Status.OK_STATUS;
			}
		};
		
		fit.setSystem(true);
		fit.setUser(true);
		fit.setPriority(Job.INTERACTIVE);
		return fit;
	}

	/**
	 * Thread safe
	 * @param peaks
	 */
	protected synchronized void createFittedPeaks(final FittedPeaksBean newBean) {
		
		if (newBean==null) {
			bean = null;
			logger.error("Cannot find peaks in the given selection.");
			return;
		}
		composite.getDisplay().syncExec(new Runnable() {
			
		    public void run() {
		    	try {
		    		
					int ipeak = 1;
					// Draw the regions
					for (RegionBounds rb : newBean.getPeakBounds()) {
						
						final IRegion area = getPlottingSystem().createRegion("Peak Area "+ipeak, RegionType.XAXIS);
						area.setRegionColor(ColorConstants.orange);
						area.setRegionBounds(rb);
						area.setMotile(false);
						getPlottingSystem().addRegion(area);
						newBean.addAreaRegion(area);
						
						
						final IRegion line = getPlottingSystem().createRegion("Peak Line "+ipeak, RegionType.XAXIS_LINE);
						line.setRegionBounds(new RegionBounds(rb.getCentre(), rb.getCentre()));
						line.setRegionColor(ColorConstants.black);
						line.setMotile(false);
						line.setAlpha(150);
						getPlottingSystem().addRegion(line);
						newBean.addLineRegion(line);
					

					    ++ipeak;
					}

					ipeak = 1;
					// Create some traces for the fitted function
					for (AbstractDataset[] pair : newBean.getFunctionData()) {
						
						final ILineTrace trace = getPlottingSystem().createLineTrace("Peak "+ipeak);
						trace.setData(pair[0], pair[1]);
						trace.setLineWidth(1);
						trace.setTraceColor(ColorConstants.black);
						getPlottingSystem().addTrace(trace);
						newBean.addTrace(trace);
						
					    ++ipeak;
  				    }
					
					FittingTool.this.bean = newBean;
					viewer.setContentProvider(createActorContentProvider(newBean.size()));
					viewer.setInput(newBean);
                    viewer.refresh();
                    
		    	} catch (Exception ne) {
		    		logger.error("Cannot create fitted peaks!", ne);
		    	}
		    } 
		});
	}
}
