package org.dawnsci.processing.ui.fileactions;

import java.nio.file.Files;
import java.nio.file.Path;

import org.dawb.common.util.io.IOpenFileAction;
import org.dawnsci.processing.ui.slice.FileManager;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigatorOpen implements IOpenFileAction {
	
	
	private static final Logger logger = LoggerFactory.getLogger(NavigatorOpen.class);
	
	@Override
	public void openFile(Path file) {

		if (file==null) return;
		
		if (!Files.isDirectory(file)) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart view = page.findView("org.dawnsci.processing.ui.DataFileSliceView");
			if (view==null) return;
			
			final FileManager manager = (FileManager)view.getAdapter(FileManager.class);
			if (manager != null) {
				manager.addFiles(new String[]{file.toAbsolutePath().toString()});
			} else {
				logger.error("Could not get file manager");
			}
		}
	}
}
