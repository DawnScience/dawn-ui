package org.dawnsci.mapping.ui.actions;

import java.io.File;

import org.dawnsci.datavis.api.IRecentPlaces;
import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenPixelSpaceImageHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell,SWT.MULTI);
		
		IRecentPlaces recentPlaces = Activator.getService(IRecentPlaces.class);

		if (!recentPlaces.getRecentDirectories().isEmpty()) {
			dialog.setFilterPath(recentPlaces.getRecentDirectories().get(0));
		}
		
		if (dialog.open() == null) return null;

		String[] fileNames = dialog.getFileNames();
		for (int i = 0; i < fileNames.length; i++) fileNames[i] = dialog.getFilterPath() + File.separator + fileNames[i];
		IMapFileController fc = Activator.getService(IMapFileController.class);
		fc.loadFiles(fileNames, null, true);
		
		return null;
	}

}
