package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.roi.data.LinearROIData;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.dawb.common.ui.plot.roi.data.RectangularROIData;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
import org.dawnsci.plotting.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * This tool shows the measurements of selected regions.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractRegionTableTool extends AbstractToolPage implements IRegionListener, IROIListener {

	protected ROIBase roi;

	public class RegionColorListener implements ISelectionChangedListener {

		private IRegion previousRegion;
		private Color   previousColor;

		@Override
		public void selectionChanged(SelectionChangedEvent event) {

			resetSelectionColor();

			final IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			if (!(sel.getFirstElement() instanceof IRegion)) return;
			final IRegion          region = (IRegion)sel.getFirstElement();
			previousRegion = region;
			if((region != null) && region.isActive()) region.setRegionColor(ColorConstants.green);
			else if ((region != null) && !region.isActive()) region.setRegionColor(ColorConstants.gray);
			previousColor  = region!=null ? region.getRegionColor() : null;

			if (region!=null) region.setRegionColor(ColorConstants.red);
		}

		private void resetSelectionColor() {
			if (previousRegion!=null) previousRegion.setRegionColor(previousColor);
			previousRegion = null;
			previousColor  = null;
		}
	}
	
	protected abstract void createNewRegion();

	protected static final Logger logger = LoggerFactory.getLogger(AbstractRegionTableTool.class);
	
	private Composite     composite;
	private TableViewer   viewer;

	private RegionColorListener viewUpdateListener;
	
	/**
	 * A map to store dragBounds which are not the official bounds
	 * of the selection until the user lets go.
	 */
	private Map<String,ROIBase> dragBounds;

	public AbstractRegionTableTool() {
		super();
		dragBounds = new HashMap<String,ROIBase>(7);
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
		
		viewer.setContentProvider(new IStructuredContentProvider() {			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}		
			@Override
			public Object[] getElements(Object inputElement) {
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				if (regions==null || regions.isEmpty()) return new Object[]{"-"};
				final List<IRegion> visible = new ArrayList<IRegion>(regions.size());
				for (IRegion iRegion : regions) {
					if (iRegion.isVisible() && iRegion.isUserRegion()) visible.add(iRegion);
				}
				return visible.toArray(new IRegion[visible.size()]);
			}
		});
		viewer.setInput(new Object());
		
		this.viewUpdateListener = new RegionColorListener();
		
		activate();
	}

	private void createActions() {

		final Action exportRegion = new Action("Export region to file", Activator.getImageDescriptor("icons/mask-export-wiz.png")) {
			public void run() {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceExportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e) {
					logger.error("Problem opening import!", e);
				}
			}			
		};
		getSite().getActionBars().getToolBarManager().add(exportRegion);
		
		final Action importRegion = new Action("Import region from file", Activator.getImageDescriptor("icons/mask-import-wiz.png")) {
			public void run() {
				try {
					IWizard wiz = EclipseUtils.openWizard(PersistenceImportWizard.ID, false);
					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
					wd.setTitle(wiz.getWindowTitle());
					wd.open();
				} catch (Exception e) {
					logger.error("Problem opening import!", e);
				}
			}			
		};

		getSite().getActionBars().getToolBarManager().add(importRegion);
		getSite().getActionBars().getToolBarManager().add(new Separator());

		final Action reselect = new Action("Create new measurement.", getImageDescriptor()) {
			public void run() {
				createNewRegion();
			}
		};
		getSite().getActionBars().getToolBarManager().add(reselect);
		getSite().getActionBars().getToolBarManager().add(new Separator());

		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (!(sel.getFirstElement() instanceof IRegion)) return;
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					if (region==null||region.getROI()==null) return;
					final ROIBase bounds = region.getROI();
					if (bounds.getPointRef()==null) return;
					
					final Clipboard cb = new Clipboard(composite.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
				}
			}
		};
		copy.setToolTipText("Copies the region values as text to clipboard which can then be pasted externally.");
		
		getSite().getActionBars().getToolBarManager().add(copy);
		getSite().getActionBars().getMenuManager().add(copy);

		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (!(sel.getFirstElement() instanceof IRegion)) return;
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					getPlottingSystem().removeRegion(region);
				}
			}
		};
		delete.setToolTipText("Delete selected region, if there is one.");
		
		getSite().getActionBars().getToolBarManager().add(delete);
		getSite().getActionBars().getMenuManager().add(delete);

		final Separator sep = new Separator(getClass().getName()+".separator1");
		getSite().getActionBars().getToolBarManager().add(sep);
		getSite().getActionBars().getMenuManager().add(sep);
		
		final Action show = new Action("Show all vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png")) {
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(true);
				}
			}
		};
		show.setToolTipText("Show vertices in all visible regions");
		
		getSite().getActionBars().getToolBarManager().add(show);
		getSite().getActionBars().getMenuManager().add(show);
		
		
		final Action clear = new Action("Show no vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-clear.png")) {
			public void run() {
				if (!isActive()) return;
				final Object[] oa = ((IStructuredContentProvider)viewer.getContentProvider()).getElements(null);
				for (Object object : oa) {
					if (object instanceof IRegion) ((IRegion)object).setShowPosition(false);
				}
			}
		};
		clear.setToolTipText("Clear all vertices shown in the plotting");
		
		getSite().getActionBars().getToolBarManager().add(clear);
		getSite().getActionBars().getMenuManager().add(clear);

		createRightClickMenu();
	}
	
	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
	    for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems()) menuManager.add(item);
	    viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
	}
	
	@SuppressWarnings("unused")
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
	
	protected abstract void createColumns(final TableViewer viewer);


	@Override
	public void activate() {
		super.activate();
		if (viewer!=null && viewer.getControl().isDisposed()) return;
		
		if (viewUpdateListener!=null) viewer.addSelectionChangedListener(viewUpdateListener);

		
		try {
			try {
				getPlottingSystem().addRegionListener(this);
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) iRegion.addROIListener(this);
				
				createNewRegion();
				
			} catch (Exception e) {
				logger.error("Cannot add region listeners!", e);
			}		
			
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
		
		if (viewUpdateListener!=null) {
			viewer.removeSelectionChangedListener(viewUpdateListener);
			viewUpdateListener.resetSelectionColor();
		}
		if (dragBounds!=null) dragBounds.clear();
		if (getPlottingSystem()!=null) try {
			getPlottingSystem().removeRegionListener(this);
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			for (IRegion iRegion : regions) iRegion.removeROIListener(this);
		} catch (Exception e) {
			logger.error("Cannot remove region listeners!", e);
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
		if (viewUpdateListener!=null) viewer.removeSelectionChangedListener(viewUpdateListener);
		viewUpdateListener = null;
		
        if (viewer!=null) viewer.getControl().dispose();
               
		dragBounds.clear();
		dragBounds = null;
		
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
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			IRegion region = evt.getRegion();
			region.addROIListener(this);
			region.getROI().setPlot(true);
			// set the Region isActive flag
			region.setActive(true);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().removeROIListener(this);
		}
	}
	@Override
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		if (!isActive()) return;
		updateRegion(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		if (!isActive()) return;
		updateRegion(evt);
	}
	@Override
	public void roiSelected(ROIEvent evt) {
		// TODO Auto-generated method stub

	}
	private RegionBoundsUIJob updateJob;
	/**
	 * Uses cancellable UIJob
	 * 
	 * @param evt
	 */
	private void updateRegion(final ROIEvent evt) {
		if(viewer == null) return;
		if(viewer.isCellEditorActive()) return; 
		if (updateJob==null) {
			updateJob = new RegionBoundsUIJob();
			updateJob.setPriority(UIJob.INTERACTIVE);
			//updateJob.setUser(false);
		}
		updateJob.setEvent(evt);
		updateJob.cancel();
		updateJob.schedule();
	}
	
	private final class RegionBoundsUIJob extends UIJob {
		
		private ROIEvent evt;
		RegionBoundsUIJob() {
			super("Measurement update");
		}
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (viewer!=null) {
				if(monitor.isCanceled())	return Status.CANCEL_STATUS;

				IRegion  region = (IRegion)evt.getSource();
				ROIBase rb = evt.getROI();
				
				if(monitor.isCanceled())	return Status.CANCEL_STATUS;
				dragBounds.put(region.getName(), rb);
				
				if(monitor.isCanceled())	return Status.CANCEL_STATUS;
				viewer.refresh(region);
			}
			return Status.OK_STATUS;
		}
		
		void setEvent(ROIEvent evt) {
			this.evt = evt;
		}
	};

	public ROIBase getROI(IRegion region) {
		if (dragBounds!=null&&dragBounds.containsKey(region.getName())) return dragBounds.get(region.getName());
		return region.getROI();
	}

	/**
	 * Gets intensity for images and lines.
	 * @param region
	 * @return
	 */
	public double getMaxIntensity(IRegion region) {

        final Collection<ITrace> traces = getPlottingSystem().getTraces();
        final ROIBase bounds = getROI(region);
        if (bounds==null) return Double.NaN;
        
        if (traces!=null&&traces.size()==1&&traces.iterator().next() instanceof IImageTrace) {
        	final IImageTrace     trace        = (IImageTrace)traces.iterator().next();
        	
        	ROIData rd = null;
        	RegionType type = region.getRegionType();

        	if ((type == RegionType.BOX || type == RegionType.PERIMETERBOX) && bounds instanceof RectangularROI) {
	    		final RectangularROI roi = (RectangularROI) bounds;
	    		rd = new RectangularROIData(roi, trace.getData());
        	} else if (type == RegionType.LINE && bounds instanceof LinearROI) {
        		final LinearROI roi = (LinearROI) bounds;
        		rd = new LinearROIData(roi, trace.getData(), 1d);     
        	}
        	
        	if (rd!=null) {
	    		try {
	    			double max2 = rd.getProfileData().length>1 && rd.getProfileData()[1]!=null
	    					    ? rd.getProfileData()[1].max().doubleValue()
	    					    : -Double.MAX_VALUE;
	         	    return Math.max(rd.getProfileData()[0].max().doubleValue(), max2);
	    		} catch (Throwable ne) {
	    			return Double.NaN;
	    		}
        	}

        }
        
        return Double.NaN;
      
	}

	/**
	 * Method that gets the sum of all pixels for the region
	 * @param region
	 * @return
	 */
	public double getSum(IRegion region){
		double result = Double.NaN;
		Collection<ITrace> traces = getPlottingSystem().getTraces();
		
		if (traces!=null&&traces.size()==1&&traces.iterator().next() instanceof IImageTrace) {
			final IImageTrace     trace        = (IImageTrace)traces.iterator().next();
			ROIBase roi = region.getROI();
			AbstractDataset dataRegion = trace.getData();
			try {
				if(roi instanceof RectangularROI){
					RectangularROI rroi = (RectangularROI)roi;
					int xStart = (int) rroi.getPoint()[0];
					int yStart = (int) rroi.getPoint()[1];
					int xStop = (int) rroi.getEndPoint()[0];
					int yStop = (int) rroi.getEndPoint()[1];
					int xInc = rroi.getPoint()[0]<rroi.getEndPoint()[0] ? 1 : -1;
					int yInc = rroi.getPoint()[1]<rroi.getEndPoint()[1] ? 1 : -1;
					dataRegion = dataRegion.getSlice(
							new int[] { yStart, xStart },
							new int[] { yStop, xStop },
							new int[] {yInc, xInc});
					result = (Double)dataRegion.sum(true);
				} else if (roi instanceof LinearROI){
//					LinearROI lroi = (LinearROI)roi;
//					int xStart = (int) lroi.getPoint()[0];
//					int yStart = (int) lroi.getPoint()[1];
//					int xStop = (int) lroi.getEndPoint()[0];
//					int yStop = (int) lroi.getEndPoint()[1];
//					int xInc = lroi.getPoint()[0]<lroi.getEndPoint()[0] ? 1 : -1;
//					int yInc = lroi.getPoint()[1]<lroi.getEndPoint()[1] ? 1 : -1;
//					dataRegion = dataRegion.getSlice(
//							new int[] { yStart, xStart },
//							new int[] { yStop, xStop },
//							new int[] {yInc, xInc});
				}
				
			} catch (IllegalArgumentException e) {
				logger.debug("Error getting region data:"+ e);
			}
			
		}
		return result;
	}
}
