package org.dawnsci.plotting.tools.region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.tree.TreeNode;

import org.dawb.common.ui.menu.MenuAction;
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
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
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
import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
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

	private RegionEditorColorListener viewUpdateListener;
	private ITraceListener traceListener;
	private IPropertyChangeListener propertyListener;

	/**
	 * A map to store dragBounds which are not the official bounds
	 * of the selection until the user lets go.
	 */
	private Map<String,IROI> dragBounds;
	private RegionBoundsUIJob updateJob;

	private Action visibleToggleAction;
	private Action activeToggleAction;
	private Action mobileToggleAction;

	public RegionEditorTool() {
		super();
		dragBounds = new HashMap<String,IROI>(7);
		propertyListener = new FormatChangeListener();
		Activator.getPlottingPreferenceStore().addPropertyChangeListener(propertyListener);
		traceListener = new TraceListener();
	}

	@Override
	public void createControl(Composite parent) {

		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);

		this.filteredTree = new ClearableFilteredTree(control, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true, "Enter search string to filter the tree.\nThis will match on name, value or units");		
		viewer = filteredTree.getViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(viewer);
		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					ITreeSelection selection = (ITreeSelection) viewer.getSelection();
					Iterator<?> it = selection.iterator();
					while (it.hasNext()) {
						Object object = (Object) it.next();
						if (object instanceof RegionEditorNode) {
							RegionEditorNode regionNode = (RegionEditorNode) object;
							IRegion region = getPlottingSystem().getRegion(regionNode.getRegion().getName());
							model.removeRegion(regionNode);
							region.removeROIListener(RegionEditorTool.this);
							getPlottingSystem().removeRegion(region);
						}
					}
				}
			}
		});

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

		this.viewUpdateListener = new RegionEditorColorListener();

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
		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}
		resetExpansion();
		getSite().setSelectionProvider(viewer);
	}

	// Listeners
	class TraceListener implements ITraceListener {
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
	};

	class FormatChangeListener implements IPropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
//			if (isActive()) {
				if(isInterestedProperty(event)) {
					updateFormat();
				}
//			}
		}

		private boolean isInterestedProperty(PropertyChangeEvent event) {
			final String propName = event.getProperty();
			return RegionEditorConstants.POINT_FORMAT.equals(propName) ||
					RegionEditorConstants.ANGLE_FORMAT.equals(propName) ||
					RegionEditorConstants.INTENSITY_FORMAT.equals(propName) ||
					RegionEditorConstants.SUM_FORMAT.equals(propName);
		}

		private void updateFormat() {
			IPreferenceStore store = Activator.getPlottingPreferenceStore();
			String pointFormat = store.getString(RegionEditorConstants.POINT_FORMAT);
			String angleFormat = store.getString(RegionEditorConstants.ANGLE_FORMAT);
			String maxIntensityFormat = store.getString(RegionEditorConstants.INTENSITY_FORMAT);
			String sumFormat = store.getString(RegionEditorConstants.SUM_FORMAT);
			if(model == null)
				return;
			LabelNode root = model.getRoot();
			List<TreeNode> nodes = root.getChildren();
			for (TreeNode node : nodes) {
				RegionEditorNode regionNode = (RegionEditorNode) node;
				List<TreeNode> regionChildren = regionNode.getChildren();
				for (TreeNode child : regionChildren) {
					if (child instanceof NumericNode<?>) {
						NumericNode<?> numNode = (NumericNode<?>) child;
						Unit<?> unit = numNode.getUnit();
						if (unit.equals(Dimensionless.UNIT)) {
							if (numNode.getLabel().contains(RegionEditorNodeFactory.INTENSITY))
								numNode.setFormat(maxIntensityFormat);
							else if (numNode.getLabel().contains(RegionEditorNodeFactory.SUM))
								numNode.setFormat(sumFormat);
						} else if (unit.equals(NonSI.DEGREE_ANGLE)
								|| unit.equals(SI.RADIAN)) {
							numNode.setIncrement(ToolUtils.getDecimal(angleFormat));
							numNode.setFormat(angleFormat);
						} else if (unit.equals(NonSI.PIXEL)) {
							numNode.setIncrement(ToolUtils.getDecimal(pointFormat));
							numNode.setFormat(pointFormat);
						}
					}
				}
			}
			viewer.refresh();
		}
	};

	class RegionEditorColorListener extends RegionColorListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			if (!(sel.getFirstElement() instanceof RegionEditorNode))
				return;
			final RegionEditorNode regionNode = (RegionEditorNode) sel.getFirstElement();
			IRegion region = getPlottingSystem().getRegion(regionNode.getLabel());
			if (region == null)
				return;
			updateColorSelection(region);
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
						if (iRegion.getRegionType() == IRegion.RegionType.BOX)
							return;
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

		final Action expandAll = new Action("Expand All", Activator.getImageDescriptor("icons/expand_all.png")) {
			public void run() {
				if (viewer != null) {
					viewer.expandAll();
				}
			}
		};
		expandAll.setToolTipText("Expand All");

		final Action collapseAll = new Action("Collapse All", Activator.getImageDescriptor("icons/collapse_all.png")) {
			public void run() {
				if (viewer != null) {
					viewer.collapseAll();
				}
			}
		};
		collapseAll.setToolTipText("Collapse All");

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
				if (!isActive())
					return;
				ITreeSelection selected = (ITreeSelection)viewer.getSelection();
				Iterator<?> it = selected.iterator();
				String txtCopy = "";
				while (it.hasNext()) {
					Object object = (Object) it.next();
					if (object instanceof RegionEditorNode) {
						RegionEditorNode regionNode = (RegionEditorNode) object;
						IRegion region = regionNode.getRegion();
						if (region == null || region.getROI() == null)
							return;
						IROI bounds = region.getROI();
						if (bounds.getPointRef() == null)
							return;
						txtCopy = txtCopy + region.getName() + "  " + bounds + System.getProperty("line.separator");
					}
				}
				if (txtCopy.isEmpty())
					return;
				Clipboard cb = new Clipboard(control.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[]{ txtCopy }, new Transfer[]{textTransfer});
			}
		};
		copy.setToolTipText("Copies the selected region values as text to clipboard which can then be pasted externally");

		final MenuAction removeRegionDropDown = new MenuAction("Delete selection region(s)");
		removeRegionDropDown.setId(BasePlottingConstants.REMOVE_REGION);

		final Action show = new Action("Show all vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-vertices.png")) {
			public void run() {
				if (!isActive())
					return;
				showRegionVertices(true);
			}
		};
		show.setToolTipText("Show vertices in all visible regions");

		final Action clear = new Action("Show no vertex values", Activator.getImageDescriptor("icons/plot-tool-measure-clear.png")) {
			public void run() {
				if (!isActive())
					return;
				showRegionVertices(false);
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

		//toolbar buttons
		toolBarMan.add(new Separator());
		toolBarMan.add(getReselectAction());
		toolBarMan.add(new Separator());
		toolBarMan.add(expandAll);
		toolBarMan.add(collapseAll);
		toolBarMan.add(new Separator());
		toolBarMan.add(importRegion);
		toolBarMan.add(exportRegion);
		toolBarMan.add(new Separator());
		toolBarMan.add(copy);
		toolBarMan.add(show);
		toolBarMan.add(clear);
		toolBarMan.add(new Separator());

		ActionContributionItem deleteMenu = (ActionContributionItem)getPlottingSystem().getActionBars().getToolBarManager().find(BasePlottingConstants.REMOVE_REGION);
		MenuAction deleteMenuAction = (MenuAction) deleteMenu.getAction();
		toolBarMan.add(deleteMenuAction);

		// right click menu buttons
		MenuManager rightClickMenuMan = new MenuManager();
		addRightClickMenuCheckActions(rightClickMenuMan);
		rightClickMenuMan.add(copy);
		rightClickMenuMan.add(show);
		rightClickMenuMan.add(clear);
		rightClickMenuMan.add(deleteMenuAction);
		viewer.getControl().setMenu(rightClickMenuMan.createContextMenu(viewer.getControl()));

		// menu buttons
		menuMan.add(copy);
		menuMan.add(show);
		menuMan.add(clear);
		menuMan.add(new Separator());
		menuMan.add(deleteMenuAction);
		menuMan.add(new Separator());
		menuMan.add(preferences);
	}

	private void addRightClickMenuCheckActions(MenuManager menuManager) {
		visibleToggleAction = new Action("Enable/disable region visibility", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (!isActive())
					return;
				ITreeSelection selected = (ITreeSelection) viewer.getSelection();
				Iterator<?> it = selected.iterator();
				while (it.hasNext()) {
					Object object = (Object) it.next();
					if (object instanceof RegionEditorNode) {
						RegionEditorNode regionNode = (RegionEditorNode) object;
						regionNode.setVisible(visibleToggleAction.isChecked());
						viewer.refresh();
					}
				}
			}
		};
		visibleToggleAction.setChecked(true);
		activeToggleAction = new Action("Enable/disable region Active flag", IAction.AS_CHECK_BOX) {
			public void run() {
				if (!isActive())
					return;
				ITreeSelection selected = (ITreeSelection) viewer.getSelection();
				Iterator<?> it = selected.iterator();
				while (it.hasNext()) {
					Object object = (Object) it.next();
					if (object instanceof RegionEditorNode) {
						RegionEditorNode regionNode = (RegionEditorNode) object;
						regionNode.setActive(activeToggleAction.isChecked());
						viewer.refresh();
					}
				}
			}
		};
		activeToggleAction.setChecked(true);
		mobileToggleAction = new Action("Enable/disable region mobility", IAction.AS_CHECK_BOX) {
			public void run() {
				if (!isActive())
					return;
				ITreeSelection selected = (ITreeSelection) viewer.getSelection();
				Iterator<?> it = selected.iterator();
				while (it.hasNext()) {
					Object object = (Object) it.next();
					if (object instanceof RegionEditorNode) {
						RegionEditorNode regionNode = (RegionEditorNode) object;
						regionNode.setMobile(mobileToggleAction.isChecked());
						viewer.refresh();
					}
				}
			}
		};
		mobileToggleAction.setChecked(true);
		menuManager.add(visibleToggleAction);
		menuManager.add(activeToggleAction);
		menuManager.add(mobileToggleAction);
		menuManager.add(new Separator());
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				if (!isActive())
					return;
				ITreeSelection selected = (ITreeSelection) viewer.getSelection();
				Iterator<?> it = selected.iterator();
				while (it.hasNext()) {
					Object object = (Object) it.next();
					if (object instanceof RegionEditorNode) {
						RegionEditorNode regionNode = (RegionEditorNode) object;
						visibleToggleAction.setChecked(regionNode.isVisible());
						activeToggleAction.setChecked(regionNode.isActive());
						mobileToggleAction.setChecked(regionNode.isMobile());
					}
				}
			}
		});
	}

	private void showRegionVertices(boolean isShown) {
		if (model == null)
			return;
		LabelNode root = model.getRoot();
		if (root == null)
			return;
		List<TreeNode> nodes = root.getChildren();
		for (TreeNode node : nodes) {
			if (node instanceof RegionEditorNode) {
				RegionEditorNode regionNode = (RegionEditorNode) node;
				IRegion region = regionNode.getRegion();
				region.setShowPosition(isShown);
			}
		}
	}

	private void createColumns(final TreeViewer viewer) {
		viewer.setColumnProperties(new String[] { "Name", "Value", "Unit", "Visible", "Active", "Mobile" });
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(190);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(0)));
		var.setEditingSupport(new RegionEditorEditingSupport(viewer, 0));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value");
		var.getColumn().setWidth(100);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(1)));
		var.setEditingSupport(new RegionEditorEditingSupport(viewer, 1));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Unit");
		var.getColumn().setWidth(90);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(2)));
		var.setEditingSupport(new UnitEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Visible");
		var.getColumn().setWidth(60);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(3)));
		var.setEditingSupport(new RegionEditorEditingSupport(viewer, 3));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 4);
		var.getColumn().setText("Active");
		var.getColumn().setWidth(60);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(4)));
		var.setEditingSupport(new RegionEditorEditingSupport(viewer, 4));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 5);
		var.getColumn().setText("Mobile");
		var.getColumn().setWidth(60);
		var.getColumn().setMoveable(true);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new RegionEditorLabelProvider(5)));
		var.setEditingSupport(new RegionEditorEditingSupport(viewer, 5));
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
				// Clean the model if there is no region
				if (viewer != null) {
					TreeItem[] items = viewer.getTree().getItems();
					for (int i = 0; i < items.length; i++) {
						RegionEditorNode regionNode = (RegionEditorNode) items[i].getData();
						if (model != null)
							model.removeRegion(regionNode);
					}
				}
				for (IRegion iRegion : regions) {
					iRegion.addROIListener(this);
					if (model != null) {
						double[] intensitySum = getIntensityAndSum(iRegion);
						model.addRegion(iRegion, intensitySum[0], intensitySum[1]);
					}
				}
				if (!isDedicatedView()) {
					createNewRegion(false);
				}
				if (model != null && model.getRoot() != null && model.getRoot().getChildren() != null)
					viewer.setInput(model.getRoot());
			} catch (Exception e) {
				logger.error("Cannot add region listeners!", e);
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
	public void regionNameChanged(RegionEvent evt, String oldName) {
		if (!isActive())
			return;
		IRegion region = evt.getRegion();
		if (region == null)
			return;
		LabelNode root = model.getRoot();
		if (root == null)
			return;
		List<TreeNode> nodes = root.getChildren();
		if (nodes == null)
			return;
		for (TreeNode node : nodes) {
			if (node instanceof RegionEditorNode) {
				RegionEditorNode regionNode = (RegionEditorNode) node;
				if (regionNode.getLabel().equals(oldName)) {
					regionNode.setLabel(region.getName());
					if (viewer!=null)
						viewer.refresh();
					break;
				}
			}
		}
	}

	@Override
	public void regionAdded(RegionEvent evt) {
		if (!isActive()) return;
		IRegion region = evt.getRegion();
		if (region != null) {
			region.addROIListener(this);
			IROI roi = region.getROI();
			if (roi == null)
				return;
			roi.setPlot(true);
			// set the Region isActive flag
			region.setActive(true);

			double[] intensitySum = getIntensityAndSum(region);
			model.addRegion(region, intensitySum[0], intensitySum[1]);

			if (model.getRoot().getChildren() != null)
				viewer.setInput(model.getRoot());
			if (viewer!=null)
				viewer.refresh();
			boolean isMobile = Activator.getPlottingPreferenceStore().getBoolean(RegionEditorConstants.MOBILE_REGION_SETTING);
			region.setMobile(isMobile);
		}
	}

	@Override
	public void regionRemoved(RegionEvent evt) {
		if (!isActive())
			return;
		IRegion region = evt.getRegion();
		if (region != null) {
			if (model ==  null)
				return;
			LabelNode root = model.getRoot();
			if (root == null)
				return;
			List<TreeNode> nodes = root.getChildren();
			if (nodes == null)
				return;
			for (TreeNode node : nodes) {
				if (node instanceof RegionEditorNode) {
					RegionEditorNode regionNode = (RegionEditorNode) node;
					if (regionNode.getLabel().equals(region.getName())) {
						model.removeRegion(regionNode);
						region.removeROIListener(this);
						getPlottingSystem().removeRegion(region);
						break;
					}
				}
			}
		}
	}

	@Override
	public void regionsRemoved(RegionEvent evt) {
		if (!isActive())
			return;
		Collection<IRegion> regions = evt.getRegions();
		for (IRegion region : regions) {
			RegionEditorNode regionNode = (RegionEditorNode) model.getNode("/" + region.getName());
			if (regionNode == null)
				return;
			model.removeRegion(regionNode);
			region.removeROIListener(this);
			getPlottingSystem().removeRegion(region);
		}
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
				RegionEditorNode node = (RegionEditorNode)treeItems[i].getData();
				String name = node.getLabel();
				if(region.getName().equals(name)){
					viewer.getTree().setSelection(treeItems[i]);
					break;
				}
			}
			updateRegion(evt);
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
				if (iRegion.isActive())
					iRegion.setRegionColor(ColorConstants.green);
				else if (!iRegion.isActive())
					iRegion.setRegionColor(ColorConstants.gray);
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

				double[] intensitySum = getIntensityAndSum(region);
				model.updateRegion(region, intensitySum[0], intensitySum[1]);

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
	 * Returns the Intensity and the Sum of the region for Rectangle, lines and Point
	 * @param region
	 * @return
	 */
	private double[] getIntensityAndSum(IRegion region) {
		double[] intensityAndSum = new double[] {0, 0};
		Collection<ITrace> traces = getPlottingSystem().getTraces();
		if (traces != null && traces.size() == 1
				&& traces.iterator().next() instanceof IImageTrace) {
			final IImageTrace trace = (IImageTrace) traces.iterator().next();
			IROI roi = region.getROI();
			if (roi instanceof RectangularROI) {
				RectangularROI rroi = (RectangularROI) roi;
					AbstractDataset dataRegion = (AbstractDataset) ToolUtils
							.getClippedSlice(trace.getData(), rroi);
					intensityAndSum[0] = ToolUtils.getRectangleMaxIntensity(dataRegion);
					intensityAndSum[1] = ToolUtils.getRectangleSum(dataRegion);
			} else if (roi instanceof LinearROI) {
				LinearROI lroi = (LinearROI) roi;
				intensityAndSum[0] = ToolUtils.getLineIntensity(trace.getData(), lroi);
			} else if (roi instanceof PointROI) {
				PointROI proi = (PointROI) roi;
				intensityAndSum[0] = ((AbstractDataset) trace.getData()).getDouble((int)proi.getPointY(), (int)proi.getPointX());
			}
		}
		return intensityAndSum;
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
