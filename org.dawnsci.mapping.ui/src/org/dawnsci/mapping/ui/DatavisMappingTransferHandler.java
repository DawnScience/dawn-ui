package org.dawnsci.mapping.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dawnsci.datavis.api.IDataFilePackage;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.wizards.LegacyMapBeanBuilder;
import org.dawnsci.mapping.ui.wizards.MapBeanBuilder;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatavisMappingTransferHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(DatavisMappingTransferHandler.class);
	
	private static final String LOADEDFILE_PART ="org.dawnsci.datavis.view.parts.LoadedFilePart";
	private static final String PERSPECTIVE_ID = "org.dawnsci.mapping.ui.dawn.mappingperspective";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveSite(event).getWorkbenchWindow().getSelectionService().getSelection(LOADEDFILE_PART);
		List<IDataFilePackage> list = getSelectedFiles(selection);
		
		if (list.isEmpty()) {
			showMessageBox("No file selected.");
			logger.debug("No files selected");
			return null;
		}
		
		List<IDataFilePackage> mapFiles = new ArrayList<>();
		
		for (IDataFilePackage l : list) {
			if (l.getTree() == null) continue;
			
			if (MapBeanBuilder.buildBean(l.getTree())!= null) {
				mapFiles.add(l);
				continue;
			}
			
			try {
				if (LegacyMapBeanBuilder.tryLegacyLoaders(Activator.getService(ILoaderService.class).getData(l.getFilePath(), null)) != null) {
					mapFiles.add(l);
				}
			} catch (Exception e) {
			}
		}
		
		if (mapFiles.isEmpty()) {
				showMessageBox("File metadata not consistent with mapping scan.");
			logger.debug("No map files selected");
			return null;
		}
		
		IMapFileController fc = Activator.getService(IMapFileController.class);
		
		if (fc == null) {
			//shouldn't happen since this is in the mapping code
			logger.error("No file controller!");
			return null;
		}
		
		List<String> collect = mapFiles.stream().map(IDataFilePackage::getFilePath).collect(Collectors.toList());
		String[] array = collect.toArray(new String[collect.size()]);
		
		logger.info("Loading files:  {}", Arrays.toString(array));
		
		fc.loadFiles(array, null);
		
		try {
			PlatformUI.getWorkbench().showPerspective(PERSPECTIVE_ID,PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (WorkbenchException e) {
			logger.error("Could not switch workbench to mapping perspective",e);
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
