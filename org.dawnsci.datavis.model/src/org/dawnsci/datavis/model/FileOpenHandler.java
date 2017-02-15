package org.dawnsci.datavis.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;

public class FileOpenHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell,SWT.MULTI);

		if (dialog.open() == null) return null;

		String[] fileNames = dialog.getFileNames();
		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];

		Map<String,String[]> props = new HashMap<>();
		props.put("paths", fileNames);

		EventAdmin eventAdmin = ServiceManager.getEventAdmin();
		eventAdmin.sendEvent(new Event("org/dawnsci/events/file/OPEN", props));
		return null;
	}


}
