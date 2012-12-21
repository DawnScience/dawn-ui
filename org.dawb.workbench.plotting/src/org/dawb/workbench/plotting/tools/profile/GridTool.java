package org.dawb.workbench.plotting.tools.profile;

import java.util.List;

import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.tree.BooleanNode;
import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
import org.dawnsci.common.widgets.tree.ColorNode;
import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.IResettableExpansion;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NodeFilter;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.roi.GridROI;

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
	
	protected Composite           control;
	private TreeViewer            viewer;
	private GridTreeModel         model;
	private ClearableFilteredTree filteredTree;
	private IRegionListener       regionListener;
	private IROIListener          roiListener;

	public GridTool() {
		
		this.roiListener    = new IROIListener.Stub() {
			@Override
			public void update(ROIEvent evt) {
				if (!isActive()) return;
				if (!(evt.getROI() instanceof GridROI)) return;
				if (model!=null) {
					model.setRegion((IRegion)evt.getSource(), (GridROI)evt.getROI());
				}
			}
		};
		
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionRemoved(RegionEvent evt) {
				if (evt.getRegion()!=null) evt.getRegion().removeROIListener(roiListener);
			}
			@Override
			public void regionCreated(RegionEvent evt) {
				if (evt.getRegion()!=null && evt.getRegion().getRegionType()==RegionType.GRID) {
					evt.getRegion().addROIListener(roiListener);
				}
			}
		};
	}
	
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

		// Allow the colours to be drawn nicely.
		final Tree tree = viewer.getTree();
		tree.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					GC gc = event.gc;
					Rectangle area = tree.getClientArea();
					/*
					 * If you wish to paint the selection beyond the end of last column,
					 * you must change the clipping region.
					 */
					int columnCount = tree.getColumnCount();
					if (event.index == columnCount - 1 || columnCount == 0) {
						int width = area.x + area.width - event.x;
						if (width > 0) {
							Region region = new Region();
							gc.getClipping(region);
							region.add(event.x, event.y, width, event.height);
							gc.setClipping(region);
							region.dispose();
						}
					}
					gc.setAdvanced(true);
					if (gc.getAdvanced()) gc.setAlpha(50);
					Rectangle rect = event.getBounds();
					Color foreground = gc.getForeground();
					Color background = gc.getBackground();
					gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
					gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					gc.fillGradientRectangle(0, rect.y, 500, rect.height, false);

					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					// Draw the colour in the Value column
					if (item!=null && item.getData() instanceof ColorNode) {
						gc.setAlpha(255);
						Rectangle col = item.getBounds(1);
						ColorNode cn = (ColorNode)item.getData();
						gc.setBackground(cn.getColor());
						gc.fillRectangle(col);
					}

					// restore colors for subsequent drawing
					gc.setForeground(foreground);
					gc.setBackground(background);
					event.detail &= ~SWT.SELECTED;
					return;
				}
				
				if ((event.detail & SWT.HOT) != 0) {
					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					// Draw the colour in the Value column
					if (item!=null && item.getData() instanceof LabelNode) {
						LabelNode ln = (LabelNode)item.getData();
						GC gc = event.gc;
						Color foreground = gc.getForeground();
						Color background = gc.getBackground();
						gc.setAdvanced(true);
						gc.setForeground(ColorConstants.black);
						gc.drawText(ln.getLabel(), item.getBounds().x+2, item.getBounds().y+1);
						event.doit = false;
						event.detail &= ~SWT.HOT;
						gc.setForeground(foreground);
						gc.setBackground(background);
					}
				}
			}
		});
		
		tree.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				final TreeItem item = tree.getItem(new Point(e.x, e.y));
				if (item.getData() instanceof BooleanNode) {
					if (item!=null) {
						Rectangle r = item.getBounds(1);
						if (r.contains(new Point(e.x, e.y))) {
							BooleanNode bn = (BooleanNode)item.getData();
							bn.setValue(!bn.isValue());
							viewer.update(bn, new String[]{"Value"});
						}
					}
					
				}
			}			
		});
	}
	
	private void createActions() {
		// TODO Auto-generated method stub
		
	}


	private void createGridModel() {
		
		model = new GridTreeModel();
		model.setViewer(viewer);
		viewer.setInput(model.getRoot());
		
        resetExpansion();
		getSite().setSelectionProvider(viewer);
	}


	private void createColumns(TreeViewer viewer) {
		
		viewer.setColumnProperties(new String[] { "Name", "Value", "Unit" });
		ColumnViewerToolTipSupport.enableFor(viewer);

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(220);
		var.setLabelProvider(new NodeLabelProvider(0));
				
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(140);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(viewer));
		
	}


	@Override
	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(regionListener);
		}
		createNewRegion();
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(regionListener);
		}
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
		model.dispose();
		model = null;
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
