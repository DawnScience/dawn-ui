package org.dawb.workbench.plotting.tools.profile;

import java.util.List;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.IResettableExpansion;
import org.dawnsci.common.widgets.tree.NodeFilter;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool to draw and configure a grid.
 * 
 * GDA will also be able to add custom actions for running the
 * scan to this tool using the extension point for adding actions
 * to tools.
 * 
 * @author fcp94556
 *
 */
public class GridTool extends AbstractToolPage implements IResettableExpansion{

	private static Logger logger = LoggerFactory.getLogger(GridTool.class);
	protected Composite   control;
	private TreeViewer    viewer;
	private GridTreeModel model;
	private ClearableFilteredTree filteredTree;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	

	@Override
	public void createControl(Composite parent) {
		
		final Action reselect = new Action("Create new grid.", getImageDescriptor()) {
			public void run() {
				createNewRegion();
			}
		};
		
		IActionBars actionbars = getSite()!=null?getSite().getActionBars():null;
		if (actionbars != null){
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroup"));
			actionbars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.tools.profile.newProfileGroup", reselect);
			actionbars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.tools.profile.newProfileGroupAfter"));
		}

		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);

		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true);		
		viewer = filteredTree.getViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(viewer);
		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);

		final Label label = new Label(control, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		label.setForeground(new Color(label.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		label.setText("* Click to change value  ");
		
		createGridModel();
		createActions();
	}
	
	private void createActions() {
		// TODO Auto-generated method stub
		
	}


	private void createGridModel() {
		
		model = new GridTreeModel();
		
		viewer.setInput(model.getRoot());
		
        resetExpansion();
		getSite().setSelectionProvider(viewer);
	}


	private void createColumns(TreeViewer viewer) {
		
		viewer.setColumnProperties(new String[] { "Name", "Value", "Unit" });
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new NodeLabelProvider(0));
				
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(1)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
		var.setEditingSupport(new UnitEditingSupport(viewer));
		
	}


	@Override
	public void activate() {
		super.activate();
		createNewRegion();
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
	}
	
	private final void createNewRegion() {
		
		if (getPlottingSystem()==null) return;
		// Start with a selection of the right type
		try {
			IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
			region.setUserObject(getMarker());
		} catch (Exception e) {
			logger.error("Cannot create region for profile tool!");
		}
	}

	
	private Object getMarker() {
	    return getToolPageRole().getClass().getName().intern();
	}


	private String getRegionName() {
		return "Grid";
	}

	private RegionType getCreateRegionType() {
		return RegionType.GRID;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		if (control!=null && !control.isDisposed()) {
			control.setFocus();
		}
	}


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
