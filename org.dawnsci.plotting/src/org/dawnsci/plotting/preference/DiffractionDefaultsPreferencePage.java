package org.dawnsci.plotting.preference;

import javax.measure.quantity.Quantity;

import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.NumericNode;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.tools.diffraction.DiffractionDefaultMetadata;
import org.dawnsci.plotting.tools.diffraction.DiffractionTreeModel;
import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

//Uses the Diffraction tools treeview to allow the diffraction metadata values in the
// preference store to be edited
public class DiffractionDefaultsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private FilteredTree filteredTree;
	private TreeViewer viewer;
	private DiffractionTreeModel model;
	private IDiffractionMetadata metaData;
	
	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.defaultsPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(DiffractionDefaultsPreferencePage.class);

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());

	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		//Use the DiffractionTool treeview to display the default meta values
		Label label = new Label(main, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		label.setText("Set default diffraction meta data values:");
		
		PatternFilter filter = new PatternFilter();
		this.filteredTree = new FilteredTree (main, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);		
		viewer = filteredTree.getViewer();
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createColumns(viewer);
		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		
		initializePage();
		
		return main;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();

		//reset tree to original values (the defaults in the store)
		model.reset();
		viewer.refresh();
	}
	
	private void initializePage() {
		createDiffractionModel();
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}
	
	private void storePreferences() {
		setPersistedDetectorPropertes(metaData.getDetector2DProperties());
		setPersistedEnvironment(metaData.getDiffractionCrystalEnvironment());
	}
	
	//Getters and setters for detector properites/crystal environment store
	private void setPersistedDetectorPropertes(DetectorProperties detprop){
		DiffractionDefaultMetadata.setPersistedDetectorPropertieValues(detprop);
	}
	
	private void setPersistedEnvironment(DiffractionCrystalEnvironment dce){
		DiffractionDefaultMetadata.setPersistedDiffractionCrystalEnvironmentValues(dce);
	}
	
	private void createDiffractionModel() {
		
		if (model!=null)  return;
		if (viewer==null) return;
		
		metaData = DiffractionDefaultMetadata.getDiffractionMetadata(null,new int[] {1000,1000});
		try {
			model = new DiffractionTreeModel(metaData);
			model.setViewer(viewer);
		} catch (Exception e) {
			logger.error("Cannot create model!", e);
			return;
		}
				
		viewer.setInput(model.getRoot());
		
	}
	
	private void createColumns(TreeViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
		viewer.setColumnProperties(new String[] { "Name", "Original", "Value", "Unit" });

		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(200);
		var.setLabelProvider(new ColumnLabelProvider());
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Original"); // Selected
		var.getColumn().setWidth(0);
		var.getColumn().setResizable(false);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new NodeLabelProvider(1)));
		
		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new NodeLabelProvider(2)));
		var.setEditingSupport(new ValueEditingSupport(viewer));

		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingStyledCellLabelProvider(new NodeLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(viewer));
	}

	@SuppressWarnings("unchecked")
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
				fse.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						node.setValue((Double)fse.getValue(), null);
					}
				});
				return fse;
			}
			return null;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (!(element instanceof NumericNode)) return false;
			return ((NumericNode<?>)element).isEditable();
		}

		@Override
		protected Object getValue(Object element) {
			if (!(element instanceof NumericNode)) return null;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			
			return node.getDoubleValue();
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (!(element instanceof NumericNode)) return;
			
			NumericNode<? extends Quantity> node = (NumericNode<? extends Quantity>)element;
			node.setDoubleValue((Double)value);
			viewer.refresh(element);
		}

		
	}
	
	@SuppressWarnings("unchecked")
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

}
