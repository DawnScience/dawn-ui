package org.dawnsci.processing.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ResourceTransfer;
import org.slf4j.Logger;

public class OperationTableUtils {
	
	/**
	 *  Create an operation series table without breaking the encapsulation of the
	 * validators etc being private to the package
	 * 
	 * @param table
	 * @param comp
	 * 
	 * @return informer
	 */
	public static IOperationErrorInformer initialiseOperationTable(SeriesTable table, Composite comp) {
		OperationValidator val = new OperationValidator();
		IOperationErrorInformer info = new OperationInformerImpl(table);
		val.setOperationErrorInformer(info);
		final OperationLabelProvider prov = new OperationLabelProvider(0);
		table.setValidator(val);
		table.createControl(comp, prov);
		OperationFilter f = new OperationFilter();
		f.setOperationErrorInformer(info);
		table.setInput(null, f);
		return info;
	}
	
	private static void readOperationsToSeriesTableFromFile(String filename, SeriesTable table, OperationFilter opFilter) throws Exception {
		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
		IOperationService os = (IOperationService)ServiceManager.getService(IOperationService.class);
		IPersistentFile pf = service.getPersistentFile(filename);
		IOperation<? extends IOperationModel, ? extends OperationData>[] operations = pf.getOperations();
		if (operations == null) return;
		List<OperationDescriptor> list = new ArrayList<OperationDescriptor>(operations.length);
		for (IOperation<? extends IOperationModel, ? extends OperationData> op : operations) list.add(new OperationDescriptor(op, os));
		
		if (opFilter == null) opFilter = new OperationFilter();
		
		if (operations != null) table.setInput(list, opFilter);

	}

	
	public static void setupPipelinePaneDropTarget(final SeriesTable table, final OperationFilter opFilter, final Logger logger, final Shell shell) {
		DropTarget dt = table.getDropTarget();
		
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(new DropTargetAdapter() {
			
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							readOperationsFromFile(file.getLocation().toOSString(), table, opFilter, logger, shell);
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					for (String path : (String[])dropData){
						readOperationsFromFile(path, table, opFilter, logger, shell);
						return;
					}
				}
			}
		});
		
		
	}
	
	public static void readOperationsFromFile(String fileName, SeriesTable table, OperationFilter opFilter, Logger logger, Shell shell) {
		try {
			OperationTableUtils.readOperationsToSeriesTableFromFile(fileName, table, opFilter);
		} catch (Exception e) {
			logger.error("Could not read operations from file", e);
			if (shell != null)
				MessageDialog.openInformation(shell, "Exception while writing operations to file", "An exception occurred while writing the operations to a file.\n" + e.getMessage());
		}
	}

}
