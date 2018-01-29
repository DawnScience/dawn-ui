package org.dawnsci.processing.ui.processing;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
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
import org.slf4j.LoggerFactory;

public class OperationTableUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(OperationTableUtils.class);
	
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
		IPersistenceService service = ServiceHolder.getPersistenceService();
		IOperationService os = ServiceHolder.getOperationService();
		IPersistentFile pf = service.getPersistentFile(filename);
		IOperation<? extends IOperationModel, ? extends OperationData>[] operations = pf.getOperations();
		pf.close();
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
	
	public static void addMenuItems(IMenuManager mm, final SeriesTable seriesTable, Shell shell) {
		
		mm.add(getAddAction(seriesTable));
		mm.add(getDeleteAction(seriesTable));
		mm.add(getClearAction(seriesTable, shell));
		mm.add(new Separator());
		
		IOperation<? extends IOperationModel, ? extends OperationData> op = null;
		
		try {
			ISeriesItemDescriptor selected = seriesTable.getSelected();
			if (!(selected instanceof OperationDescriptor)) return;
			op = ((OperationDescriptor)selected).getSeriesObject();
		} catch (InstantiationException e1) {
		}
		
		final IAction saveInter = new Action("Save output", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					try {
						((OperationDescriptor)current).getSeriesObject().setStoreOutput(isChecked());
						seriesTable.refreshTable();
					} catch (InstantiationException e) {
						logger.error("Could not create series object",e);
					}
				}
			}
		};
		
		if (op != null && op.isStoreOutput()) saveInter.setChecked(true);
		
		mm.add(saveInter);
		
		final IAction passUnMod = new Action("Pass through", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					try {
						((OperationDescriptor)current).getSeriesObject().setPassUnmodifiedData(isChecked());
						seriesTable.refreshTable();
					} catch (InstantiationException e) {
						logger.error("Could not set pass through");
					}
				}
			}
		};
		
		if (op != null && op.isPassUnmodifiedData()) passUnMod.setChecked(true);
		mm.add(passUnMod);
		
	}
	
	public static Action getAddAction(final SeriesTable seriesTable) {
		return new Action("Insert operation", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				seriesTable.addNewBeforeSelected();
			}
		};
	}
	
	public static Action getDeleteAction(final SeriesTable seriesTable) {
		return new Action("Delete selected operation", Activator.getImageDescriptor("icons/clipboard--minus.png")) {
			public void run() {
				seriesTable.delete();
			}
		};
	}

	public static Action getClearAction(final SeriesTable seriesTable, final Shell shell) {
		return new Action("Clear list of operations", Activator.getImageDescriptor("icons/clipboard-empty.png")) {
			public void run() {
				boolean ok = MessageDialog.openQuestion(shell, "Confirm Clear Pipeline", "Do you want to clear the pipeline?");
			    if (!ok) return;
				seriesTable.clear();
			}
		};
	}

}
