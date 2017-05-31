package org.dawnsci.common.widgets.diffraction;

import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
import org.dawnsci.common.widgets.tree.NodeLabelProvider;
import org.dawnsci.common.widgets.tree.UnitEditingSupport;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DetectorPropertiesWidget {

	private TableViewer tableViewer;
	private Composite tableComposite;
	
	private DetectorPropertiesTreeModel detectorModel;
	
	public DetectorPropertiesWidget(){
		
		
	}
	
	public void createControl(Composite parent) {
		tableViewer = new TableViewer(parent,SWT.BORDER);
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				
				return ((DetectorPropertiesTreeModel)inputElement).getNodes();

			}
		});
		createColumns();
	}
	
	private void createColumns() {
		
		tableViewer.setColumnProperties(new String[] { "Name", "Value", "Unit" });
		ColumnViewerToolTipSupport.enableFor(tableViewer);

		TableViewerColumn var = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		var.getColumn().setText("Name"); // Selected
		var.getColumn().setWidth(260);
		var.setLabelProvider(new NodeLabelProvider(0));
		
		var = new TableViewerColumn(tableViewer, SWT.LEFT, 1);
		var.getColumn().setText("Value"); // Selected
		var.getColumn().setWidth(100);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));

		var = new TableViewerColumn(tableViewer, SWT.LEFT, 2);
		var.getColumn().setText("Unit"); // Selected
		var.getColumn().setWidth(90);
		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
		var.setEditingSupport(new UnitEditingSupport(tableViewer));
	}
	
	public void setDetectorProperties(DetectorProperties dp) {
		detectorModel = new DetectorPropertiesTreeModel(dp);
		tableViewer.setInput(detectorModel);
		
		
	}
	
	public Control getControl() {
		return tableViewer.getControl();
	}
	
}
