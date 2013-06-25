package org.dawnsci.plotting.tools.grid;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import javax.measure.quantity.Length;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.tree.AmountEvent;
import org.dawnsci.common.widgets.tree.AmountListener;
import org.dawnsci.common.widgets.tree.BooleanNode;
import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
import org.dawnsci.common.widgets.tree.ColorNode;
import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.IResettableExpansion;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NodeFilter;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceDialog;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.roi.GridPreferences;
import uk.ac.diamond.scisoft.analysis.roi.GridROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

/**
 * Tool to draw and configure a grid.
 * 
 * GDA will also be able to add custom actions for running the
 * scan to this tool using the extension point for adding actions
 * to tools.
 * 
 * Instead of using the preference store to get the GridPreferences (beam position, resolution)
 * it should now be passed in as metadata on the abstract dataset which is plotted.
 * 
 * The metadata dictionary should contain a populated GridPreferences object value associated
 * with the key "GDA_GRID_METADATA" (public static on this class)
 * 
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
	private ITraceListener        traceListener;
	public static final String    GDA_GRID_METADATA ="GDA_GRID_METADATA";  

	public GridTool() {
		
		this.roiListener    = new IROIListener.Stub() {
			@Override
			public void update(ROIEvent evt) {
				if (!isActive()) return;
				if (!(evt.getROI() instanceof GridROI)) return;
				if (model!=null) {
					GridROI roi = (GridROI)evt.getROI();
					if (gridPreferences != null) {
						roi.setGridPreferences(gridPreferences);
					}
					model.setRegion((IRegion)evt.getSource(), roi);
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
		
		this.traceListener = new ITraceListener.Stub() {
			protected void update(TraceEvent evt) {
				if (getImageTrace()!=null) {
					updateGridPreferences();
				}
			}
			
			@Override
			public void traceAdded(TraceEvent evt) {
				if (getImageTrace()!=null) {
					updateGridPreferences();
				}
			}
		};
		
	}


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
//  Example metadata object expected by the grid tool to correctly populate the grid preferences
//	private IMetaData getGDAGridPreferences() {
//
//		Map<String,GridPreferences> gdaMap = new HashMap<String, GridPreferences>();
//		
//		GridPreferences gp = new GridPreferences(100000, 10000, Math.random()*1000, Math.random()*1000);
//
//		gdaMap.put(GDA_GRID_METADATA, gp);
//
//		return new Metadata(gdaMap);
//	}

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

		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true, "Enter search string to filter the tree.\nThis will match on name, value or units");		
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
				if (item==null) return;
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
		gridPreferences = getGridPreferences();
		connectBeamCenterControls();
	}
	
	private void updateGridPreferences() {
		gridPreferences = getGridPreferences();
		updateBeamCentre();
		drawBeamCentre(beamCenterAction.isChecked());
	}
	
	private void updateBeamCentre() {
		this.beamCenter = getBeamCenter();
		@SuppressWarnings("unchecked")
		final NumericNode<Length> x = (NumericNode<Length>)model.getNode("/Detector/Beam Centre/X");
		x.setValue(Amount.valueOf(beamCenter[0], x.getUnit()));
		viewer.update(x, new String[]{"Value"});
		
		@SuppressWarnings("unchecked")
		final NumericNode<Length> y = (NumericNode<Length>)model.getNode("/Detector/Beam Centre/Y");
		y.setValue(Amount.valueOf(beamCenter[1], y.getUnit()));
		viewer.update(y, new String[]{"Value"});
	}
	
	private void connectBeamCenterControls() {
		// TODO FIXME Define beamCenter differently to actual center?
		//Now gets beamcentre from GDA metadata if supplied
		this.beamCenter = getBeamCenter();
		
		@SuppressWarnings("unchecked")
		final NumericNode<Length> x = (NumericNode<Length>)model.getNode("/Detector/Beam Centre/X");
		x.setDefault(Amount.valueOf(beamCenter[0], x.getUnit()));
		x.setLowerBound(0);
		x.setUpperBound(getMaxX());
		x.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				beamCenter[0] = x.getDoubleValue();
				drawBeamCentre(beamCenterAction.isChecked());
			}
		});
		
		@SuppressWarnings("unchecked")
		final NumericNode<Length> y = (NumericNode<Length>)model.getNode("/Detector/Beam Centre/Y");
		y.setDefault(Amount.valueOf(beamCenter[1], y.getUnit()));
		y.setLowerBound(0);
		y.setUpperBound(getMaxY());
		y.addAmountListener(new AmountListener<Length>() {		
			@Override
			public void amountChanged(AmountEvent<Length> evt) {
				beamCenter[1] = y.getDoubleValue();
				drawBeamCentre(beamCenterAction.isChecked());
			}
		});
		
	}

	private double[] beamCenter;
	private Action   beamCenterAction;
	private GridPreferences gridPreferences;

	/**
	 * Gets beam center from centre of custom axes, if any or
	 * 
	 * @return
	 */
	private double[] getBeamCenter() {
		
		if (gridPreferences != null) {
			double x = gridPreferences.getBeamlinePosX();
			double y = gridPreferences.getBeamlinePosY();
			
			return new double[]{x,y};
		}
		
		double[] ret = new double[2];
		final List<IDataset> axes = getImageTrace().getAxes();
		if (axes!=null) {
			ret[0] = axes.get(0).getDouble((axes.get(0).getSize() / 2));
			ret[1] = axes.get(1).getDouble((axes.get(1).getSize() / 2));
			return ret;
		}
		final int[] shape = getImageTrace().getData().getShape();
		ret[0] = shape[1]/2;
		ret[1] = shape[0]/2;
		
		return ret;
	}
	
	private double getMaxX() {
		final List<IDataset> axes = getImageTrace().getAxes();
		if (axes!=null) return axes.get(0).max().doubleValue();
		return getImageTrace().getData().getShape()[1];
	}
	private double getMaxY() {
		final List<IDataset> axes = getImageTrace().getAxes();
		if (axes!=null) return axes.get(1).max().doubleValue();
		return getImageTrace().getData().getShape()[0];
	}


	private void createActions() {

		createToolPageActions();
		
		this.beamCenterAction = new Action("Beam centre", Activator.getImageDescriptor("/icons/beam_centre.png")) {
			@Override
			public void run() {
	    		drawBeamCentre(isChecked());
			}
		};
		beamCenterAction.setChecked(false);
		
		getSite().getActionBars().getToolBarManager().add(beamCenterAction);
		getSite().getActionBars().getToolBarManager().add(new Separator());

		final Action preferences = new Action("Preferences...") {
			public void run() {
				//if (!isActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "uk.ac.diamond.scisoft.analysis.rcp.gridScanPreferencePage", null, null);
				if (pref != null) pref.open();
        	}
        };
        getSite().getActionBars().getMenuManager().add(preferences);
	}
	
	
	protected void drawBeamCentre(boolean isChecked) {
		if (!isActive()) return; // We are likely off screen.
		
		IRegion beamCentreRegion = getBeamCentre();
		if (beamCentreRegion != null)
			getPlottingSystem().removeRegion(beamCentreRegion);
			
		if (isChecked) { 
			if (beamCenter!=null){
				DecimalFormat df = new DecimalFormat("#.##");
				String label = df.format(beamCenter[0]) + "px, " + df.format(beamCenter[1])+"px";
				
				int imx = 2000;
				int imy = 2000;
				
				if (getImageTrace() != null && getImageTrace().getData() != null) {
					int[] shape = getImageTrace().getData().getShape();
					imx = shape[0];
					imy = shape[0];
				}
				
				double length = (1 + Math.sqrt(imx * imx + imy * imy) * 0.02);
				
				drawCrosshairs(beamCenter, length, ColorConstants.red, ColorConstants.black, "beam centre", label);
			}
		}
	}
	enum RING_TYPE {
		BEAM_CENTRE;
	}

	protected IRegion getBeamCentre() {
		IRegion region=null;
		final Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.LINE);
		if (regions==null) return null;
		for (IRegion iRegion : regions) {
			if (iRegion.getUserObject()==RING_TYPE.BEAM_CENTRE) {
				region = iRegion;
				break;
			}
		}
        return region;
	}

	protected IRegion drawCrosshairs(double[] beamCentre, double length, Color colour, Color labelColour, String nameStub, String labelText) {
		if (!isActive()) return null; // We are likely off screen.
		IRegion region=null;
		
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, getPlottingSystem());
			region = getPlottingSystem().createRegion(regionName, RegionType.LINE);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return null;
		}
	
		final LinearROI lroi = new LinearROI(length, 0);
		double dbc[] = {(double)beamCentre[0], (double)beamCentre[1]};
		lroi.setMidPoint(dbc);
		lroi.setCrossHair(true);
		region.setROI(lroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);
		region.setShowPosition(false);
		region.setUserObject(RING_TYPE.BEAM_CENTRE);
		
		region.setLabel(labelText);
		region.setShowLabel(false);
		
		getPlottingSystem().addRegion(region);
		region.setMobile(false); // NOTE: Must be done **AFTER** calling the addRegion method.
	
		return region;
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
			Collection<IRegion> regions = getPlottingSystem().getRegions();
			if (regions!=null) for (IRegion region : regions) {
				if (region!=null && region.getRegionType()==RegionType.GRID) {
					region.addROIListener(roiListener);
				}
			}
			getPlottingSystem().addRegionListener(regionListener);
			getPlottingSystem().addTraceListener(traceListener);
		}
		createNewRegion();
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			Collection<IRegion> regions = getPlottingSystem().getRegions();
		    if (regions!=null) for (IRegion region : regions) {
				if (region!=null && region.getRegionType()==RegionType.GRID) {
					region.removeROIListener(roiListener);
				}
			}
			getPlottingSystem().removeRegionListener(regionListener);
			getPlottingSystem().removeTraceListener(traceListener);
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
	
	private GridPreferences getGridPreferences() {
		
		GridPreferences gp = null;
		
		try {
			IMetaData m = getImageTrace().getData().getMetadata();
			if (m != null && m.getMetaNames().contains(GDA_GRID_METADATA)) {
				Object ob = m.getMetaValue(GDA_GRID_METADATA);
				if (ob instanceof GridPreferences) {
					gp = (GridPreferences)ob;
				}
			}
		} catch (Exception e) {
			return getGridFromStore();
		}
		return gp;
		
	}
	
	private GridPreferences getGridFromStore() {
		final IPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
		double xRes = preferenceStore.getDouble(GridTreeModel.GRIDSCAN_RESOLUTION_X);
		double yRes = preferenceStore.getDouble(GridTreeModel.GRIDSCAN_RESOLUTION_Y);
		double xbeam = preferenceStore.getDouble(GridTreeModel.GRIDSCAN_BEAMLINE_POSX);
		double ybeam = preferenceStore.getDouble(GridTreeModel.GRIDSCAN_BEAMLINE_POSY);
		
		return new GridPreferences(xRes, yRes, xbeam, ybeam);
	}

}
