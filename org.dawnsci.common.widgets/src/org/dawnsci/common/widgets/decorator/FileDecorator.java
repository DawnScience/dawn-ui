package org.dawnsci.common.widgets.decorator;

import org.eclipse.swt.widgets.Text;

public class FileDecorator extends RegexDecorator {

	public FileDecorator(Text text) {
		this(text, createRegex(isWindowsOS()));
	}
	
	/**
	 * 
	 * @param text
	 * @param iswindows false for linux and MacOS paths without the drive letter.
	 */
	public FileDecorator(Text text, boolean iswindows) {
		this(text, createRegex(iswindows));
	}
	
	public FileDecorator(Text text, String regex) {
		super(text, regex);
	}

	private static String createRegex(boolean isWin) {
		return isWin ? "([a-zA-Z]?\\:?)\\?[a-zA-Z_0-9\\\\]+" : "/*?([a-zA-Z_0-9\\/]+)/*?";
	}

	static private boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}
	
	/**
	 * Please override this method to provide additional checking when a character is entered.
	 * @return true if ok, false otherwise.
	 */
	protected boolean check(String value, String delta) {
		return true;
	}

	private boolean allowInvalidValues = false;
	
	public boolean isAllowInvalidValues() {
		return allowInvalidValues;
	}

	/**
	 * You can set the bounds checker not to accept invalid values or
	 * to accept them and color them red. Coloring red is the default.
	 * @param allowInvalidValues
	 */
	public void setAllowInvalidValues(boolean allowInvalidValues) {
		this.allowInvalidValues = allowInvalidValues;
	}

	public boolean isError() {
        if (allowInvalidValues) return false;
        return super.isError();
	}
}
