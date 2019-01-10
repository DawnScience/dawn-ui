package org.dawnsci.datavis.view;

import java.io.File;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.DataOptionsUtils;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataOptionsUIUtils {

	private static final Logger logger = LoggerFactory.getLogger(DataOptionsUIUtils.class);

	public static void saveToFile(DataOptions dataOptions, String path) {

		Shell shell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(shell,SWT.SAVE);
		if (path != null) dialog.setFilterPath(path);

		if (dialog.open() == null) return;

		String[] fileNames = dialog.getFileNames();

		INexusFileFactory service = Activator.getService(INexusFileFactory.class);

		try {

			DataOptionsUtils.saveToFile(dataOptions, dialog.getFilterPath() + File.separator + fileNames[0], service);

		} catch (DatasetException e) {
			MessageDialog.openError(shell, "Error slicing data!", e.getMessage());
			logger.error("Could not slice dataset",e);
		} catch (NexusException e) {
			MessageDialog.openError(shell, "Error saving file!", e.getMessage());
			logger.error("Could not save file",e);
		}
	}
}
