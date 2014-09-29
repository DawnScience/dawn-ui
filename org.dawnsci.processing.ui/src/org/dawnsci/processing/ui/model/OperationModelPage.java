package org.dawnsci.processing.ui.model;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheetPage;

public class OperationModelPage extends PropertySheetPage {

	private OperationPropertySource source;

	public void createControl(Composite parent) {
		
		super.createControl(parent);
			
		// Correct column widths.
		final Tree tree = (Tree)getControl();
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				// We make sure that the first column has a width that models require.
				TreeColumn col = tree.getColumn(0);
				if (col.getWidth()<200) col.setWidth(200);
				col = tree.getColumn(1);
				if (col.getWidth()<200) col.setWidth(200);		
			}
		});
		
		// Create drop target for file paths.
		DropTarget target = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer};
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			
			private boolean checkLocation(DropTargetEvent event) {
				
				if (event.item==null || !(event.item instanceof TreeItem)) {
					return false;
				}
				
				TreeItem item = (TreeItem)event.item;
				
				// will accept text but prefer to have files dropped
				Rectangle bounds = item.getBounds(1);
				Point coordinates = new Point(event.x, event.y);
				coordinates = tree.toControl(coordinates);
				if (!bounds.contains(coordinates)) {
					return false;
				}
				return true;
			}

			public void drop(DropTargetEvent event) {		
				
				String path = null;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					path = (String)event.data;
				}
				if (fileTransfer.isSupportedType(event.currentDataType)){
					String[] files = (String[])event.data;
					path = files[0];
				}
				if (path==null) return;
				
				if (!checkLocation(event)) return;
				
				TreeItem item = (TreeItem)event.item;
				final int row = tree.indexOf(item);
				
				if (source!=null) {
					OperationPropertyDescriptor des = (OperationPropertyDescriptor)source.getPropertyDescriptors()[row];
					if (des.isFileProperty()) {
						try {
							des.setValue(path);
							refresh();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

	}
	
    public void selectionChanged(IWorkbenchPart part, ISelection sel) {

    	super.selectionChanged(part, sel);
    	
    	if (!(sel instanceof IStructuredSelection)) return;
    	final IStructuredSelection ssel = (IStructuredSelection)sel;
    	final Object selectionContent   = ssel.getFirstElement();
    	if (!(selectionContent instanceof OperationDescriptor)) {
    		return;
    	}

    	OperationDescriptor des = (OperationDescriptor)selectionContent;
    	this.source = (OperationPropertySource)des.getAdapter(IPropertySource.class);
    }
}
