package uk.ac.diamond.screenshot.gui;

public class FileNumberTracker {

	private static int globalFileCount = 0;

	public static int getNextFileNumber() {
		return ++globalFileCount;
	}
}
