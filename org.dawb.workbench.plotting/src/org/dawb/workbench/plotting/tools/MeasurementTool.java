package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionBoundsListener;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionBoundsEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tool shows the measurements of selected regions.
 * 
 * @author fcp94556
 *
 */
public class MeasurementTool extends AbstractToolPage implements IRegionListener, IRegionBoundsListener {

	public class RegionColorListener implements ISelectionChangedListener {

		private IRegion previousRegion;
		private Color   previousColor;

		@Override
		public void selectionChanged(SelectionChangedEvent event) {

			resetSelectionColor();

			final IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			final IRegion          region = (IRegion)sel.getFirstElement();
			previousRegion = region;
			previousColor  = region!=null ? region.getRegionColor() : null;
			
			if (region!=null) region.setRegionColor(ColorConstants.red);
		}

		private void resetSelectionColor() {
			if (previousRegion!=null) previousRegion.setRegionColor(previousColor);
			previousRegion = null;
			previousColor  = null;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MeasurementTool.class);
	
	private Composite     composite;
	private TableViewer   viewer;

	private RegionColorListener viewUpdateListener;

	public MeasurementTool() {
		super();
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
				for (IRegion iRegion : regions) if (iRegion.isVisible()) visible.add(iRegion);
				return visible.toArray(new IRegion[visible.size()]);
			}
		});
		viewer.setInput(new Object());
		
		this.viewUpdateListener = new RegionColorListener();
		
		activate();
	}

	private void createActions() {
		
		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/plot-tool-measure-delete.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
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

	private void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

        TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(120);
		var.setLabelProvider(new MeasurementLabelProvider(0));
		
        var   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		var.getColumn().setText("Region Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new MeasurementLabelProvider(1));

        var   = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Coordinates");
		var.getColumn().setWidth(500);
		var.setLabelProvider(new MeasurementLabelProvider(2));
		

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
		
		if (viewUpdateListener!=null) viewer.addSelectionChangedListener(viewUpdateListener);

		
		try {
			try {
				getPlottingSystem().addRegionListener(this);
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) iRegion.addRegionBoundsListener(this);
				
				int i=1;
				while(true) {
					try {
						getPlottingSystem().createRegion("Measurement "+i, IRegion.RegionType.LINE);
						break;
					} catch (Exception ne) {
						++i;
						if (i>500) break;
						continue;
					}
				}
				
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

		try {
			getPlottingSystem().removeRegionListener(this);
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			for (IRegion iRegion : regions) iRegion.removeRegionBoundsListener(this);
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
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().addRegionBoundsListener(this);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
		if (evt.getRegion()!=null) {
			evt.getRegion().removeRegionBoundsListener(this);
		}
	}

	@Override
	public void regionBoundsDragged(RegionBoundsEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh(evt.getSource());
	}

	@Override
	public void regionBoundsChanged(RegionBoundsEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh(evt.getSource());
	}

}
