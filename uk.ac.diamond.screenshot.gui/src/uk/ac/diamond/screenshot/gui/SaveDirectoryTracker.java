package uk.ac.diamond.screenshot.gui;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SaveDirectoryTracker {

	private static final String DIRECTORY_DIALOG_TITLE = "Choose screenshot directory for this session";
	private static final String DEFAULT_SAVE_DIR = System.getProperty("user.home") + "/screenshots/";

	private static final Logger logger = LoggerFactory.getLogger(SaveDirectoryTracker.class);
	
	private static Path saveDir = null;
	
	/**
	 * Returns the save directory. If none has been specified, this method asks
	 * the user for a directory, but can still return null if the user cancels
	 * the dialog.
	 */
	public static Path checkOrGetSaveDirectory() {
		if (saveDir == null) {
			getSaveDirFromUser();
		}
		return saveDir;
	}
	
	private static void getSaveDirFromUser() {
		final DirectoryDialog dialog = new DirectoryDialog(GuiUtils.getDisplay().getActiveShell());
		dialog.setText(DIRECTORY_DIALOG_TITLE);
		dialog.setFilterPath(DEFAULT_SAVE_DIR);
		final String dir = dialog.open();
		
		// If the user selected a directory, update the stored path
		if (dir != null) {
			saveDir = Paths.get(dir);
			logger.debug("Selected save directory: {}", saveDir);
			// Flush UI event queue to ensure dialog is closed and focus is returned to the previous window
			GuiUtils.flushUIEventQueue();
		}
	}
}
