package org.dawnsci.plotting.tools.preference;

import java.text.DecimalFormat;

import org.dawb.common.ui.viewers.TreeNodeContentProvider;
import org.dawnsci.common.widgets.tree.LabelNode;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.ObjectNode;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEditingSupport;
import org.dawnsci.common.widgets.tree.ValueEvent;
import org.dawnsci.common.widgets.tree.ValueListener;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.diffraction.DiffractionDefaultMetadata;
import org.dawnsci.plotting.tools.diffraction.DiffractionTreeModel;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
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
import uk.ac.diamond.scisoft.analysis.metadata.IDiffractionMetadata;

//Uses the Diffraction tools treeview to allow the diffraction metadata values in the
// preference store to be edited
public class DiffractionDefaultsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private FilteredTree filteredTree;
	private TreeViewer viewer;
	private DiffractionTreeModel model;
	private IDiffractionMetadata metaData;
	
	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.defaultsPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(DiffractionDefaultsPreferencePage.class);

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getPlottingPreferenceStore());

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
			
			final LabelNode  format  = new LabelNode("Format", model.getRoot());
			final ObjectNode numeric = new ObjectNode("Numbers", Activator.getPlottingPreferenceStore().getString(DiffractionToolConstants.NUMBER_FORMAT), format);
			numeric.setEditable(true);
			
			numeric.addValueListener(new ValueListener() {			
				@Override
				public void valueChanged(ValueEvent evt) {
					try {
						final String formatString = evt.getValue().toString();
						final DecimalFormat f     = new DecimalFormat(formatString);
						Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.NUMBER_FORMAT, formatString);
					} catch (Exception ne) {
						logger.error("Cannot set "+DiffractionToolConstants.NUMBER_FORMAT+" to "+evt.getValue());
					}
				}
			});
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

	
}
