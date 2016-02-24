package org.dawnsci.mapping.ui.actions;

import org.dawnsci.mapping.ui.FileManagerSingleton;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.dawnsci.mapping.ui.datamodel.MappedFileManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class FileOpenEventHandler implements EventHandler {

	@Override
	public void handleEvent(final Event event) {
		
		if (Display.getCurrent() == null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				
				@Override
				public void run() {
					handleEvent(event);
					
				}
			});
			return;
		}
		
		MappedFileManager fm = FileManagerSingleton.getFileManager();
		if (fm == null) return;
		
		String path = (String)event.getProperty("path");

		if (event.getTopic().endsWith("CLOSE")) {
			fm.removeFile(path);
			return;
		}
		
		if (event.containsProperty("map_bean")) {
			Object p = event.getProperty("map_bean");
			if (p instanceof MappedDataFileBean) fm.importFile(path, (MappedDataFileBean)p);
		}
		

		fm.importFile(path);
	}

}
