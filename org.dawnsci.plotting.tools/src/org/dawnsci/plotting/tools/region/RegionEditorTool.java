package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.plot.roi.data.LinearROIData;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.dawb.common.ui.plot.roi.data.RectangularROIData;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.dawb.common.ui.wizard.persistence.PersistenceImportWizard;
import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.IResettableExpansion;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NodeFilter;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.dawnsci.plotting.tools.preference.RegionEditorPreferencePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * This tool is used to edit regions
 * 
 * @author wqk87977
 *
 */
public class RegionEditorTool extends AbstractToolPage implements IRegionListener, IROIListener, IResettableExpansion {

	private static final Logger logger = LoggerFactory.getLogger(RegionEditorTool.class);

	private IROI roi;
	private Composite control;
	private TreeViewer viewer;
	private RegionEditorTreeModel model;
	private ClearableFilteredTree filteredTree;

	private RegionColorListener viewUpdateListener;

	/**
	 * A map to store dragBounds which are not the official bounds
	 * of the selection until the user lets go.
	 */
	private Map<String,IROI> dragBounds;

	private ITraceListener traceListener;

	private RegionBoundsUIJob updateJob;

	public RegionEditorTool() {
		super();
		dragBounds = new HashMap<String,IROI>(7);

		Activator.getPlottingPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isActive()) {
					if(isInterestedProperty(event))
						viewer.refresh();
				}
 			}

			private boolean isInterestedProperty(PropertyChangeEvent event) {
				final String propName = event.getProperty();
				return RegionEditorConstants.POINT_FORMAT.equals(propName) ||
						RegionEditorConstants.INTENSITY_FORMAT.equals(propName) ||
						RegionEditorConstants.SUM_FORMAT.equals(propName);
			}
		});

		traceListener = new ITraceListener() {
			@Override
			public void tracesUpdated(TraceEvent evt) {
			}
			
			@Override
			public void tracesRemoved(TraceEvent evet) {
			}
			
			@Override
			public void tracesAdded(TraceEvent evt) {
			}
			
			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
			}
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceCreated(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
			
			@Override
			public void traceAdded(TraceEvent evt) {
				if (viewer != null && viewer.getControl().isDisposed())
					return;
				viewer.refresh();
			}
		};
	}

	@Override
	public void createControl(Composite parent) {

		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);

		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true, "Enter search string to filter the tree.\nThis will match on name, value or units");		
		viewer = filteredTree.getViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(viewer);
		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);

		Composite status = new Composite(control, SWT.NONE);
		status.setLayoutData(new GridData(SWT.FILL, GridData.FILL, true, false));
		status.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		status.setLayout(new GridLayout(1, true));
		GridUtils.removeMargins(status);

		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		final Label label = new Label(status, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		label.setForeground(new Color(label.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		label.setText("* Click to change value  ");

		createRegionEditorModel(true);
		createActions();

		getSite().setSelectionProvider(viewer);

		this.viewUpdateListener = new RegionColorListener();

		activate();
	}

	private void createRegionEditorModel(boolean force) {

		if (!force && model != null)
			return;
		if (force && model != null) {
			model.dispose();
			model = null;
		}
		if (viewer == null)
			return;

		Collection<IRegion> regions = new ArrayList<IRegion>();
		try {
			model = new RegionEditorTreeModel(getPlottingSystem(), regions);
			model.setViewer(viewer);
			model.activate();

		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}

		model.activate();

		resetExpansion();
		getSite().setSelectionProvider(viewer);
	}

	public class RegionColorListener implements ISelectionChangedListener {

		private IRegion previousRegion;
		private Color previousColor;

		@Override
		public void selectionChanged(SelectionChangedEvent event) {

			final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			if (!(sel.getFirstElement() instanceof LabelNode))
				return;
			final LabelNode regionNode = (LabelNode) sel.getFirstElement();
			IRegion region = getPlottingSystem().getRegion(regionNode.getLabel());
			if (region == null)
				return;
			updateColorSelection(region);
		}

		private void resetSelectionColor() {
			if (previousRegion != null)
				previousRegion.setRegionColor(previousColor);
			previousRegion = null;
			previousColor  = null;
		}
	}

	private void createNewRegion(boolean force) {
		try {
			if (!force) {
				// We check to see if the region type preferred is already there
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) {
					if (iRegion.isUserRegion() && iRegion.isVisible()) {
						// We have one already, do not go into create mode :)
						if (iRegion.getRegionType() == IRegion.RegionType.BOX) return;
					}
				}
			}

			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
		} catch (Exception e) {
			logger.error("Cannot create line region for selecting in measurement tool!", e);
		}
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	private IAction getReselectAction() {
		final Action reselect = new Action("Create new "+getRegionTypeName(), getImageDescriptor()) {
			public void run() {
				createNewRegion(true);
			}
		};
		return reselect;
	}

	private String getRegionTypeName() {
		return "region";
	}

	private void createActions() {

		final IPreferenceStore store = Activator.getPlottingPreferenceStore();

		IToolBarManager toolBarMan = getSite().getActionBars().getToolBarManager();
		IMenuManager menuMan = getSite().getActionBars().getMenuManager();

		final Action immobileWhenAdded = new Action("Allow regions to be moved graphically", IAction.AS_CHECK_BOX) {
			public void run() {
				store.setValue(RegionEditorConstants.MOBILE_REGION_SETTING, isChecked());
				// We also set all regions mobile or immobile
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions) {
					if (iRegion.isUserRegion() && iRegion.isVisible()) iRegion.setMobile(isChecked());
				}
			}
		};
		immobileWhenAdded.setImageDescriptor(Activator.getImageDescriptor("icons/traffic-light-green.png"));
		immobileWhenAdded.setChecked(store.getBoolean(RegionEditorConstants.MOBILE_REGION_SETTING));
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isActive()) {
					if(isInterestedProperty(event)) {
						boolean isChecked = store.getBoolean(RegionEditorConstants.MOBILE_REGION_SETTING);
						immobileWhenAdded.setChecked(isChecked);
						// We also set all regions mobile or immobile
						final Collection<IRegion> regions = getPlottingSystem().getRegions();
						for (IRegion iRegion : regions) {
							if (iRegion.isUserRegion() && iRegion.isVisible()) iRegion.setMobile(isChecked);
						}
					}
				}
 			}
			private boolean isInterestedProperty(PropertyChangeEvent event) {
				return RegionEditorConstants.MOBILE_REGION_SETTING.equals(event.getProperty());
			}
		});

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

		final Action copy = new Action("Copy region values to clipboard", Activator.getImageDescriptor("icons/plot-tool-measure-copy.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (!(sel.getFirstElement() instanceof IRegion)) return;
				if (sel!=null && sel.getFirstElement()!=null) {
					final IRegion region = (IRegion)sel.getFirstElement();
					if (region==null||region.getROI()==null) return;
					final IROI bounds = region.getROI();
					if (bounds.getPointRef()==null) return;
					
					final Clipboard cb = new Clipboard(control.getDisplay());
					TextTransfer textTransfer = TextTransfer.getInstance();
					cb.setContents(new Object[]{region.getName()+"  "+bounds}, new Transfer[]{textTransfer});
				}
			}
		};
		copy.setToolTipText("Copies the region values as text to clipboard which can then be pasted externally.");

		final Action delete = new Action("Delete selected region", Activator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				if (!isActive()) return;
				final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
				if (sel == null)
					return;
				Object obj = sel.getFirstElement();
				if (!(obj instanceof LabelNode))
					return;
				if (obj instanceof LabelNode && obj instanceof NumericNode<?>)
					return;
				if (obj!=null) {
					final LabelNode regionNode = (LabelNode)sel.getFirstElement();
					String regionName = regionNode.getLabel();
					IRegion region = getPlottingSystem().getRegion(regionName);
					if (region != null) {
						region.removeROIListener(RegionEditorTool.this);
						getPlottingSystem().removeRegion(region);
					}
				}
			}
		};
		delete.setToolTipText("Delete selected region, if there is one.");

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

		final Action preferences = new Action("Preferences...") {
			public void run() {
				if (!isActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), RegionEditorPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		preferences.setToolTipText("Open Region Editor preferences");

		toolBarMan.add(immobileWhenAdded);
		toolBarMan.add(new Separator());
		toolBarMan.add(getReselectAction());
		toolBarMan.add(new Separator());
		toolBarMan.add(importRegion);
		toolBarMan.add(exportRegion);
		toolBarMan.add(new Separator());
		toolBarMan.add(copy);
		toolBarMan.add(delete);
		toolBarMan.add(new Separator());
		toolBarMan.add(show);
		toolBarMan.add(clear);

		menuMan.add(copy);
		menuMan.add(delete);
		menuMan.add(new Separator());
		menuMan.add(show);
		menuMan.add(clear);
		menuMan.add(new Separator());
		menuMan.add(preferences);
		createRightClickMenu();
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		for (IContributionItem item : getSite().getActionBars().getMenuManager().getItems())
			menuManager.add(item);
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
	}

	private void createColumns(final TreeViewer viewer) {

		viewer.setColumnProperties(new String[] { "Name", "Value", "Unit", "Visible", "Active", "Mobile" });
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new NodeLabelProvider(1));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Visible"); // Selected
		var.getColumn().setWidth(60);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(4)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("Active"); // Selected
		var.getColumn().setWidth(60);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(5)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Mobile"); // Selected
		var.getColumn().setWidth(60);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(6)));
		var.setEditingSupport(new ValueEditingSupport(viewer));
	}

	@Override
	public void activate() {
		super.activate();
		if (viewer != null && viewer.getControl().isDisposed())
			return;

		getPlottingSystem().addTraceListener(traceListener);

		if (viewUpdateListener != null)
			viewer.addSelectionChangedListener(viewUpdateListener);
		try {
			try {
				getPlottingSystem().addRegionListener(this);
				final Collection<IRegion> regions = getPlottingSystem().getRegions();
				for (IRegion iRegion : regions)
					iRegion.addROIListener(this);
				if (!isDedicatedView()) {
					createNewRegion(false);
				}
			} catch (Exception e) {
				logger.error("Cannot add region listeners!", e);
			}

			if (viewer != null) {
				viewer.refresh();
			}

		} catch (Exception e) {
			logger.error("Cannot put the selection into fitting region mode!", e);
		}
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (viewer != null && !viewer.getControl().isDisposed() && viewUpdateListener!=null) {
			viewer.removeSelectionChangedListener(viewUpdateListener);
			viewUpdateListener.resetSelectionColor();
		}
		if (dragBounds!=null) dragBounds.clear();
		if (getPlottingSystem()!=null) try {
			getPlottingSystem().removeTraceListener(traceListener);
			getPlottingSystem().removeRegionListener(this);
			final Collection<IRegion> regions = getPlottingSystem().getRegions();
			for (IRegion iRegion : regions) iRegion.removeROIListener(this);
		} catch (Exception e) {
			logger.error("Cannot remove region listeners!", e);
		}		
	}

	@Override
	public void setFocus() {
		if (viewer!=null && !viewer.getControl().isDisposed())
			viewer.getControl().setFocus();
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void regionCreated(RegionEvent evt) {
		IRegion region = evt.getRegion();
		region.setAlpha(51); // 20%
	}

	@Override
	public void regionCancelled(RegionEvent evt) {
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		if (evt.getRegion()!=null) {
			IRegion region = evt.getRegion();
			region.addROIListener(this);
			region.getROI().setPlot(true);
			// set the Region isActive flag
			region.setActive(true);
			model.addRegion(region, getMaxIntensity(region), getSum(region));
			if (model.getRoot().getChildren() != null)
				viewer.setInput(model.getRoot());

		}
		if (viewer!=null) viewer.refresh();
		
		boolean isMobile = Activator.getPlottingPreferenceStore().getBoolean(RegionEditorConstants.MOBILE_REGION_SETTING);
		evt.getRegion().setMobile(isMobile);
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive())
			return;
		IRegion region = evt.getRegion();
		if (region != null) {
			LabelNode regionNode = (LabelNode)model.getNode("/"+region.getName());
			if (regionNode == null)
				return;
			model.removeRegion(regionNode);
			region.removeROIListener(this);
			getPlottingSystem().removeRegion(region);
		}
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive()) return;
		if (viewer!=null) viewer.refresh();
	}
	
	@Override
	public void roiDragged(ROIEvent evt) {
		model.setRegionDragged(true);
		viewer.cancelEditing();
		if (!isActive()) return;
		updateRegion(evt);
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		model.setRegionDragged(false);
		if (!model.isTreeModified()) {
			if (!isActive()) return;
			updateRegion(evt);
			IRegion region = (IRegion)evt.getSource();
			if(region == null) return;
			updateColorSelection(region);
			TreeItem[] treeItems = viewer.getTree().getItems();
			for (int i = 0; i < treeItems.length; i++) {
				String name = treeItems[i].getText();
				if(region.getName().equals(name)){
					viewer.getTree().setSelection(treeItems[i]);
					break;
				}
			}
		}
	}

	@Override
	public void roiSelected(ROIEvent evt) {

	}

	private void updateColorSelection(IRegion region){
		Collection<IRegion> regions = getPlottingSystem().getRegions();
		for (IRegion iRegion : regions) {
			if(region.getName().equals(iRegion.getName())){
				iRegion.setRegionColor(ColorConstants.red);
			} else {
				if(iRegion.isActive()) iRegion.setRegionColor(ColorConstants.green);
				else if (!iRegion.isActive()) iRegion.setRegionColor(ColorConstants.gray);
			}
		}
	}

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
			super("Region update");
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (viewer != null) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				IRegion region = (IRegion) evt.getSource();
				IROI rb = evt.getROI();
				if (model == null)
					return Status.CANCEL_STATUS;
				model.updateRegion(region, getMaxIntensity(region), getSum(region));
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				dragBounds.put(region.getName(), rb);

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				viewer.refresh(true);
			}
			return Status.OK_STATUS;
		}
		
		void setEvent(ROIEvent evt) {
			this.evt = evt;
		}
	};

	/**
	 * Gets intensity for images and lines.
	 * @param region
	 * @return
	 */
	public double getMaxIntensity(IRegion region) {

		final Collection<ITrace> traces = getPlottingSystem().getTraces();
		IROI bounds = null;
		if (dragBounds!=null&&dragBounds.containsKey(region.getName()))
			bounds = dragBounds.get(region.getName());
		else
			bounds = region.getROI();

		if (bounds == null)
			return Double.NaN;

		if (traces != null && traces.size() == 1
				&& traces.iterator().next() instanceof IImageTrace) {
			final IImageTrace trace = (IImageTrace) traces.iterator().next();
			ROIData rd = null;
			RegionType type = region.getRegionType();

			if ((type == RegionType.BOX || type == RegionType.PERIMETERBOX)
					&& bounds instanceof RectangularROI) {
				final RectangularROI roi = (RectangularROI) bounds;
				rd = new RectangularROIData(roi, (AbstractDataset) trace.getData());
			} else if (type == RegionType.LINE && bounds instanceof LinearROI) {
				final LinearROI roi = (LinearROI) bounds;
				rd = new LinearROIData(roi, (AbstractDataset) trace.getData(), 1d);
			} else
				return Double.NaN;
			if (rd != null) {
				try {
					double max2 = rd.getProfileData().length > 1
							&& rd.getProfileData()[1] != null
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
			IROI roi = region.getROI();
			AbstractDataset dataRegion =  (AbstractDataset)trace.getData();
			try {
				if(roi instanceof RectangularROI){
					RectangularROI rroi = (RectangularROI)roi;
					int xStart = (int) rroi.getPoint()[0];
					int yStart = (int) rroi.getPoint()[1];
					int xStop = (int) rroi.getEndPoint()[0];
					int yStop = (int) rroi.getEndPoint()[1];
					int xInc = rroi.getPoint()[0]<rroi.getEndPoint()[0] ? 1 : -1;
					int yInc = rroi.getPoint()[1]<rroi.getEndPoint()[1] ? 1 : -1;
					if (dataRegion == null)
						return result;
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

	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	public double[] getAxisPoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getValueAxisLocation(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	/**
	 * get point in image coords
	 * @param coords
	 * @return
	 */
	public double[] getImagePoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getAxisLocationValue(vals);
		} catch (Exception e) {
			return vals;
		}
	}

	public IROI getRoi() {
		return roi;
	}

	public void setRoi(IROI roi) {
		this.roi = roi;
	}

	@Override
	public void resetExpansion() {
		try {
			if (model == null) return;
			final List<?> top = model.getRoot().getChildren();
			for (Object element : top) {
				filteredTree.expand(element);
			}
		} catch (Throwable ne) {
			// intentionally silent
		}
	}
}
