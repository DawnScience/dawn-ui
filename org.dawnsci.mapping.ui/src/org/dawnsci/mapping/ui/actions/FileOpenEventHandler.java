package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.FileManagerSingleton;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class FileOpenEventHandler implements EventHandler {

	@Override
	public void handleEvent(Event event) {
		
		String path = (String)event.getProperty("path");
		MappedFileManager fm = FileManagerSingleton.getFileManager();
		if (fm != null) fm.importFile(path);
	}

}
