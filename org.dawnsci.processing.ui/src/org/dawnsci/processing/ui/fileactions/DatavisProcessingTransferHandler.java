package org.dawnsci.processing.ui.fileactions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.processing.ui.slice.DataFileSliceView;
import org.dawnsci.processing.ui.slice.FileManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatavisProcessingTransferHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(DatavisProcessingTransferHandler.class);
	
	private static final String LOADEDFILE_PART ="org.dawnsci.datavis.view.parts.LoadedFilePart";
	private static final String PERSPECTIVE_ID = "org.dawnsci.processing.ui.ProcessingPerspective";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveSite(event).getWorkbenchWindow().getSelectionService().getSelection(LOADEDFILE_PART);
		List<IDataFilePackage> list = getSelectedFiles(selection);
		
		if (list.isEmpty()) {
			showMessageBox("No file selected.");
			logger.debug("No files selected");
			return null;
		}
		
		try {
			//Need to switch perspective first since manager accessed via view.
			PlatformUI.getWorkbench().showPerspective(PERSPECTIVE_ID,PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (WorkbenchException e) {
			showMessageBox("Could not switch to processing perspective.");
			logger.error("Could not switch to processing perspective", e);
		}
	
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = page.findView(DataFileSliceView.ID);

		List<String> collect = list.stream().map(IDataFilePackage::getFilePath).collect(Collectors.toList());
		String[] array = collect.toArray(new String[collect.size()]);
		
		final FileManager manager = view.getAdapter(FileManager.class);
		if (manager != null) {
			manager.addFiles(array);
		}
		
		return null;
	}

	private List<IDataFilePackage> getSelectedFiles(ISelection selection){

		List<IDataFilePackage> list = new ArrayList<>();
		
		if (selection instanceof StructuredSelection) {
			Object[] array = ((StructuredSelection)selection).toArray();
			for (Object o : array) {
				if (o instanceof IDataFilePackage) {
					list.add((IDataFilePackage)o);
				}
			}
			
		}
		
		return list;
	}

	private void showMessageBox(String message) {
		MessageBox dialog =
				new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK);
		dialog.setText("Transfer error!");
		dialog.setMessage(message);
		dialog.open();
	}
}
