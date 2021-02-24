package org.dawnsci.datavis.view.perspective;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.datavis.view.ActionServiceManager;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class FileOpenHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
		
		IRecentPlaces recentPlaces = ActionServiceManager.getRecentPlaces();
		final EventAdmin admin = ActionServiceManager.getEventAdmin();

		if (!recentPlaces.getRecentDirectories().isEmpty()) {
			dialog.setFilterPath(recentPlaces.getRecentDirectories().get(0));
		}
		
		if (dialog.open() == null) return null;

		String[] fileNames = dialog.getFileNames();
		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];

		Map<String,String[]> props = new HashMap<>();
		props.put(PlottingEventConstants.MULTIPLE_FILE_PROPERTY, fileNames);
		
		admin.sendEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
		
		return null;
	}


}
