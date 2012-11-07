package org.dawb.workbench.plotting.tools.diffraction;

import java.util.List;

import javax.measure.quantity.Quantity;
import javax.swing.tree.TreeNode;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawb.workbench.plotting.Activator;
import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.MetaDataAdapter;


public class DiffractionTool extends AbstractToolPage {

	private static final Logger logger = LoggerFactory.getLogger(DiffractionTool.class);
	
	private TreeViewer viewer;
	private Composite  control;
	private DiffractionTreeModel model;
	
	//Region and region listener added for 1-click beam centring
	private IRegion tmpRegion;
	private IRegionListener regionListener;
	private IPaletteListener.Stub paletteListener;
	private ITraceListener.Stub   traceListener;
	
	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}
	
	public DiffractionTool() {
		super();
		
        this.paletteListener = new IPaletteListener.Stub() {
        	protected void updateEvent(PaletteEvent evt) {
        		updateIntensity();
        	}
        };

		this.traceListener = new ITraceListener.Stub() {
			protected void update(TraceEvent evt) {
				if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
				updateIntensity();
			}
		};
      
	}

	protected void updateIntensity() {
		try {
			if (model==null) return;
			model.setIntensityValues(getImageTrace());
			if (!(viewer.getControl().isDisposed())) viewer.refresh();
		} catch (Exception e) {
			logger.error("Updating intensity values!", e);
		}
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
		if (getPlottingSystem()!=null && this.traceListener != null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
		if (viewer!=null) viewer.refresh();
	}
	
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(this.regionListener);
		}
		if (getPlottingSystem()!=null && this.traceListener != null) {
			getPlottingSystem().addTraceListener(traceListener);
		}
	}
	
	public void dispose() {
		super.dispose();
		if (model!=null) model.dispose();
	}

	private void createDiffractionModel() {
		
		if (model!=null)  return;
		if (viewer==null) return;
		
		IMetaData meta = getMetaData();
		try {
			model = new DiffractionTreeModel(meta);
			model.setViewer(viewer);
		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}
				
		viewer.setInput(model.getRoot());
		
		final List<?> top = model.getRoot().getChildren();
		for (Object element : top) {
		   expand(element, viewer);
		}

	}
	
	

	private void expand(Object element, TreeViewer viewer) {
		
        if (element instanceof LabelNode) {
        	if (((LabelNode)element).isDefaultExpanded()) {
        		viewer.setExpandedState(element, true);
        	}
        }
        if (element instanceof TreeNode) {
        	TreeNode node = (TreeNode)element;
        	for (int i = 0; i < node.getChildCount(); i++) {
        		expand(node.getChildAt(i), viewer);
			}
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
		
		//ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		//viewer.setColumnProperties(new String[] { "Name", "Original", "Value", "Unit" });

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new ColumnLabelProvider());
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Original"); // Selected
		var.getColumn().setWidth(0);
		var.getColumn().setResizable(false);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(1)));
		defaultColumn = var;
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new DiffractionLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(viewer));
	}
	
	private class ValueEditingSupport extends EditingSupport {

		public ValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (element instanceof NumericNode) {
				final NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
				final FloatSpinnerCellEditor fse = new FloatSpinnerCellEditor(viewer.getTree(), SWT.NONE);
				fse.setFormat(7, node.getDecimalPlaces()+1);
				fse.setIncrement(node.getIncrement());
				fse.setMaximum(node.getUpperBoundDouble());
				fse.setMinimum(node.getLowerBoundDouble());
				fse.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.character=='\n') {
							setValue(element, fse.getValue());
						}
					}
				});
				return fse;
			}
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
			node.setValue((Double)value);
			viewer.refresh(element);
		}

		
	}
	
	private class UnitEditingSupport extends EditingSupport {

		public UnitEditingSupport(ColumnViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (element instanceof NumericNode) {
				NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
				final CComboCellEditor cce = new CComboCellEditor(viewer.getTree(), node.getUnitsString(), SWT.READ_ONLY);
				cce.getCombo().addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setValue(element, cce.getValue());
					}
				});
				return cce;
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (!(element instanceof NumericNode)) return false;
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			return node.isEditable() && node.getUnits()!=null;
		}

		@Override
		protected Object getValue(Object element) {
			if (!(element instanceof NumericNode)) return null;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			
			return node.getUnitIndex();
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (!(element instanceof NumericNode)) return;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			node.setUnitIndex((Integer)value);
			viewer.refresh(element);
		}

		
	}

	private TreeNode copiedNode;
	
	private void createActions() {
		
		final IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
		final MenuManager     menuMan = new MenuManager();
		
		final Action showDefault = new Action("Show the original/default value column", Activator.getImageDescriptor("icons/plot-tool-diffraction-default.gif")) {
			public void run() {
				defaultColumn.getColumn().setWidth(isChecked()?80:0);
				defaultColumn.getColumn().setResizable(!isChecked());
			}
		};
		showDefault.setChecked(false);
		
		final Action reset = new Action("Reset selected field", Activator.getImageDescriptor("icons/reset.gif")) {
			@Override
			public void run() {
				final TreeNode node = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
				if (node instanceof NumericNode) {
					((NumericNode)node).reset();
					viewer.refresh(node);
				}
			}
		};
		final Action resetAll = new Action("Reset all fields", Activator.getImageDescriptor("icons/reset_red.png")) {
			@Override
			public void run() {
				
				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Reset All", "Are you sure that you would like to reset all values?");
				if (!ok) return;
				model.reset();
				viewer.refresh();
			}
		};
		
		final Action copy = new Action("Copy value", Activator.getImageDescriptor("icons/copy.gif")) {
			@Override
			public void run() {
				copiedNode = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
			}
		};

		final Action paste = new Action("Paste value", Activator.getImageDescriptor("icons/paste.gif")) {
			@Override
			public void run() {
				if (copiedNode!=null) {
					// TODO Paste value into something in the table.
				}
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
		toolMan.add(new Separator());
		toolMan.add(reset);
		toolMan.add(resetAll);
		toolMan.add(new Separator());
		toolMan.add(centre);
		
		menuMan.add(showDefault);
		menuMan.add(new Separator());
		menuMan.add(reset);
		menuMan.add(resetAll);
		menuMan.add(new Separator());
		menuMan.add(copy);
		menuMan.add(paste);
		menuMan.add(new Separator());
		menuMan.add(centre);
		final Menu menu = menuMan.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
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
