package org.dawb.workbench.plotting.tools.diffraction;

import java.util.List;

import javax.measure.quantity.Quantity;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public class DiffractionTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(DiffractionTool.class);
	
	private TreeViewer viewer;
	private Composite  control;
	private DiffractionTreeModel model;
	
	//Region and region listener added for 1-click beam centring
	private IRegion tmpRegion;
	private IRegionListener regionListener;
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		this.control = new Composite(parent, SWT.NONE);
		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		control.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(control);
		
		viewer = new TreeViewer(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
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
		
		getSite().setSelectionProvider(viewer);

		createDiffractionModel();
		createActions();
		createListeners();

	}
	
	public void activate() {
		super.activate();
		createDiffractionModel();
		
		if (getPlottingSystem()!=null && this.regionListener != null) {
			getPlottingSystem().addRegionListener(this.regionListener);
		}
	}
	
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(this.regionListener);
		}
	}
	
	public void dispose() {
		super.dispose();
		if (model!=null) model.dispose();
	}

	private void createDiffractionModel() {
		
		if (model!=null)  return;
		if (viewer==null) return;
		
		if (getImageTrace()==null) return;

		IMetaData meta = getMetaData();
		try {
			model = new DiffractionTreeModel(meta, getImageTrace(), viewer);
		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}
				
		viewer.setInput(model.getRoot());
		
		final List<?> top = model.getRoot().getChildren();
		for (Object element : top) {
			if (element instanceof LabelNode) {
				LabelNode ln = (LabelNode)element;
				if (ln.getLabel().toLowerCase().startsWith("raw")) continue;
			}
			viewer.setExpandedState(element, true);
		}

	}

	private IMetaData getMetaData() {
		
		IMetaData md = null;
		if (getPart() instanceof IEditorPart) {
			try {
				md = LoaderFactory.getMetaData(EclipseUtils.getFilePath(((IEditorPart)getPart()).getEditorInput()), null);
			} catch (Exception e) {
				logger.error("Cannot read meta data from "+getPart().getTitle(), e);
			}
		}
		if (md!=null) return md;

		IImageTrace imageTrace = getImageTrace();
		if (imageTrace==null) return new MetaDataAdapter();
		md = imageTrace.getData().getMetadata();	

		return md;
	}

	private TreeViewerColumn defaultColumn;
	
	private void createColumns(TreeViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.setColumnProperties(new String[] { "Name", "Default", "Value", "Unit" });

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new ColumnLabelProvider());
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Default"); // Selected
		var.getColumn().setWidth(0);
		var.getColumn().setResizable(false);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(1)));
		defaultColumn = var;
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(80);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(50);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(3)));
	}
	
	private class ValueEditingSupport extends EditingSupport {

		public ValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (!(element instanceof NumericNode)) return false;
			return ((NumericNode)element).isEditable();
		}

		@Override
		protected Object getValue(Object element) {
			if (!(element instanceof NumericNode)) return null;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			
			return node.getValue();
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (!(element instanceof NumericNode)) return;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			
			// TODO
		}

		
	}
	
	private void createActions() {
		final IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
		
		final Action showDefault = new Action("Show the original/default value column", Activator.getImageDescriptor("icons/plot-tool-diffraction-default.gif")) {
			public void run() {
				defaultColumn.getColumn().setWidth(isChecked()?80:0);
				defaultColumn.getColumn().setResizable(!isChecked());
			}
		};
		showDefault.setChecked(false);// TODO Remember that?
		
		final Action reset = new Action("Reset all fields", Activator.getImageDescriptor("icons/book_previous.png")) {
			@Override
			public void run() {
				model.reset();
				viewer.refresh();
			}
		};
		
		final Action centre = new Action("One-click beam centre",IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				logger.debug("1-click clicked");
				
				try {
					if (tmpRegion != null) {
						getPlottingSystem().removeRegion(tmpRegion);
					}
					tmpRegion = getPlottingSystem().createRegion(RegionUtils.getUniqueName("BeamCentrePicker", getPlottingSystem()), IRegion.RegionType.POINT);
					tmpRegion.setUserRegion(false);
					tmpRegion.setVisible(false);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			}
		};
		
		centre.setImageDescriptor(Activator.getImageDescriptor("icons/centre.png"));
		
		toolMan.add(showDefault);
		toolMan.add(reset);
		toolMan.add(centre);
	}
	
	private void createListeners() {
		
		this.regionListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				//test if our region
				if (evt.getRegion() == tmpRegion) {
					//update beam position and remove region
					logger.debug("1-Click region added");
					double[] point = evt.getRegion().getROI().getPoint();
					logger.debug("Clicked here X: " + point[0] + " Y : " + point[1]);
					//TODO update beam positions
					//diffMetadataComp.updateBeamPositionPixels(point);
					getPlottingSystem().removeRegion(tmpRegion);
				}
			}
		};
	}


	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
