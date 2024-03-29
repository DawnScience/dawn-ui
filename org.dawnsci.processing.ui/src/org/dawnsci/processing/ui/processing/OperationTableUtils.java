package org.dawnsci.processing.ui.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.slice.DataFileSliceView;
import org.dawnsci.processing.ui.slice.FileManager;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.january.metadata.OriginMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

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
	
	private static String readOperationsToSeriesTableFromFile(Shell shell, String filename, SeriesTable table, OperationFilter opFilter) throws Exception {
		IPersistenceService service = ServiceProvider.getService(IPersistenceService.class);
		IOperationService os = ServiceProvider.getService(IOperationService.class);
		try (IPersistentFile pf = service.getPersistentFile(filename)) {
			IOperation<? extends IOperationModel, ? extends OperationData>[] operations = pf.getOperations();
			if (operations == null) return null;

			if (pf.hasConfiguredFields()) {
				int code = MessageDialog.open(MessageDialog.QUESTION, shell, "Apply auto-configured fields", "Override some fields that are automatically set?",
						SWT.NONE, IDialogConstants.NO_LABEL, IDialogConstants.YES_LABEL);
				if (code == 1) {
					pf.applyConfiguredFields(operations);
				}
			}

			OriginMetadata dataOrigin = pf.getOperationDataOrigin();
			List<OperationDescriptor> list = new ArrayList<OperationDescriptor>(operations.length);
			for (IOperation<? extends IOperationModel, ? extends OperationData> op : operations) list.add(new OperationDescriptor(op, os));

			if (opFilter == null) opFilter = new OperationFilter();

			if (operations != null) table.setInput(list, opFilter);

			return dataOrigin == null ? null : dataOrigin.getFilePath();
		}
	}

	private static String readDataOriginFromFile(Shell shell, String filename) {
		try {
			IPersistenceService service = ServiceProvider.getService(IPersistenceService.class);
			try (IPersistentFile pf = service.getPersistentFile(filename)) {
				OriginMetadata dataOrigin = pf.getOperationDataOrigin();
				return dataOrigin == null ? null : dataOrigin.getFilePath();
			}
		} catch (Exception e) {
			logger.error("Could not read data origin from file", e);
			if (shell != null)
				MessageDialog.openInformation(shell, "Exception while reading data origin from file", "An exception occurred while reading the operations from a file.\n" + e.getMessage());
		}
		return null;
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
				List<String> dataFile = new ArrayList<>();
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					int i = 0;
					for (; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i]; 
							dataFile.add(readOperationsFromFile(shell, file.getLocation().toOSString(), table, opFilter, logger));
							i++;
							break;
						}
					}
					for (; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							dataFile.add(readDataOriginFromFile(shell, file.getLocation().toOSString()));
						}
					}
				} else if (dropData instanceof String[]) {
					String[] paths = (String[]) dropData;
					dataFile.add(readOperationsFromFile(shell, paths[0], table, opFilter, logger));
					for (int i = 1; i < paths.length; i++) {
						dataFile.add(readDataOriginFromFile(shell, paths[i]));
					}
				}
				confirmAddFileForProcessing(shell, dataFile.toArray(new String[dataFile.size()]));
			}
		});
	}

	/**
	 * Pops up dialog for user to confirm given file is added for processing
	 * @param shell
	 * @param dataFile
	 */
	public static void confirmAddFileForProcessing(Shell shell, String... dataFile) {
		if (dataFile == null || dataFile.length == 0) {
			return;
		}
		List<String> validFiles = new ArrayList<>();
		for (String d : dataFile) {
			if (new File(d).canRead()) {
				validFiles.add(d);
			}
		}
		if (validFiles.isEmpty()) {
			return;
		}

		dataFile = validFiles.toArray(new String[validFiles.size()]);
		StringBuilder text = new StringBuilder("Add following files:\n");
		for (String v : validFiles) {
			text.append('\t');
			text.append(v);
			text.append('\n');
		}
		boolean ok = MessageDialog.openQuestion(shell, "Add file for processing", text.toString());
		if (ok) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView(DataFileSliceView.ID);
			if (view==null) return;

			final FileManager manager = (FileManager)view.getAdapter(FileManager.class);
			if (manager != null) {
				manager.addFiles(dataFile);
			}
		}
	}

	public static String readOperationsFromFile(Shell shell, String fileName, SeriesTable table, OperationFilter opFilter, Logger logger) {
		try {
			return readOperationsToSeriesTableFromFile(shell, fileName, table, opFilter);
		} catch (Exception e) {
			logger.error("Could not read operations from file", e);
			if (shell != null)
				MessageDialog.openInformation(shell, "Exception while reading operations from file", "An exception occurred while reading the operations from a file.\n" + e.getMessage());
		}
		return null;
	}

	public static void addMenuItems(IMenuManager mm, final SeriesTable seriesTable, Shell shell) {
		
		mm.add(getAddAction(seriesTable));
		mm.add(getDeleteAction(seriesTable));
		mm.add(getClearAction(seriesTable, shell));
		mm.add(new Separator());
		
		IOperation<? extends IOperationModel, ? extends OperationData> op = null;
		
		OperationDescriptor selected = null;
		try {
			ISeriesItemDescriptor s = seriesTable.getSelected();
			if (!(s instanceof OperationDescriptor)) {
				return;
			}
			selected = (OperationDescriptor) s;
			op = selected.getSeriesObject();
		} catch (InstantiationException e1) {
		}

		final IAction saveInter = new Action("Save output", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					try {
						((OperationDescriptor) current).getSeriesObject().setStoreOutput(isChecked());
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
						((OperationDescriptor) current).getSeriesObject().setPassUnmodifiedData(isChecked());
						seriesTable.refreshTable();
					} catch (InstantiationException e) {
						logger.error("Could not set pass through");
					}
				}
			}
		};
		
		if (op != null && op.isPassUnmodifiedData()) passUnMod.setChecked(true);
		mm.add(passUnMod);

		final IAction enable = new Action("Enabled", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					((OperationDescriptor) current).setEnabled(isChecked());
					seriesTable.refreshTable();
				}
			}
		};

		if (op != null) enable.setChecked(selected.isEnabled());
		mm.add(enable);
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

	public static IOperation<?, ?>[] getOperations(Logger logger, List<ISeriesItemDescriptor> series) {
		List<?> ops = series.stream()
		.filter(s -> OperationDescriptor.class.isInstance(s))
		.map(s -> OperationDescriptor.class.cast(s))
		.filter(o -> o.isEnabled()).map(o -> {
			try {
				return o.getSeriesObject();
			} catch (InstantiationException e) {
				return null;
			}
		}).collect(Collectors.toList());

		if (ops.stream().anyMatch(Objects::isNull)) {
			logger.error("Could not get series object");
			return null;
		}
		return ops.toArray(new IOperation[ops.size()]);
	}
}
