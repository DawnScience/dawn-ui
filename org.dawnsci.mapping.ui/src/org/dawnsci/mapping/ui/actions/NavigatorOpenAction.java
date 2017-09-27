package org.dawnsci.mapping.ui.actions;

import java.nio.file.Files;
import java.nio.file.Path;

import org.dawb.common.util.io.IOpenFileAction;
import org.dawnsci.mapping.ui.FileManagerSingleton;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigatorOpenAction implements IOpenFileAction {
	
	
	private static final Logger logger = LoggerFactory.getLogger(NavigatorOpenAction.class);
	
	@Override
	public void openFile(Path file) {

		if (file==null) return;
		
		if (!Files.isDirectory(file)) {
			MappedFileManager manager = FileManagerSingleton.getFileManager();
			if (manager != null) {
				manager.importFile(file.toAbsolutePath().toString());
			} else {
				logger.error("Could not get file manager");
			}
		}
	}
}
