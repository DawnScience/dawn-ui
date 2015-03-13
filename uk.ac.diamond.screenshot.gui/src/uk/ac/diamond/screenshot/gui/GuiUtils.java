package uk.ac.diamond.screenshot.gui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiUtils {

	private static final long TIMEOUT_MILLIS = 3000;

	private static final Logger logger = LoggerFactory.getLogger(GuiUtils.class);

	/**
	 * Waits for the UI to be idle. If this method is called and returns <code>true</code> before
	 * a screenshot is taken, then any recent changes to the UI should have been fully repainted
	 * and will be included in the screenshot.
	 * 
	 * This implementation calls Display#readAndDispatch repeatedly to clear the UI event queue.
	 * Must be called from the UI thread.
	 * 
	 * @return <code>true</code> if the queue was successfully emptied, <code>false</code> if the
	 * operation timed out
	 */
	public static boolean flushUIEventQueue() {
		logger.debug("Flushing UI event queue...");
		final Display display = getDisplay();
		int count = 0;
		final long endTime = System.currentTimeMillis() + TIMEOUT_MILLIS;
		boolean result = true;
		while (display.readAndDispatch()) {
			count++;
			if (System.currentTimeMillis() > endTime) {
				logger.debug("Timed out waiting for UI event queue to be empty");
				result = false;
				break;
			}
		}
		logger.debug("Dispatched {} UI events", count);
		return result;
	}
	
	/**
	 * Gets the active workbench window.
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Gets the Display instance for the workbench.
	 * 
	 * @return the Display
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

}
