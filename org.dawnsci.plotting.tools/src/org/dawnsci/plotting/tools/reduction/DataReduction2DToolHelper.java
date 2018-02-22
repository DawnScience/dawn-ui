package org.dawnsci.plotting.tools.reduction;

import java.io.File;
import java.text.DecimalFormat;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;

class DataReduction2DToolHelper {
	public static final int DEFAULT_DECIMAL_PLACE = 5;
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#########");

	private DataReduction2DToolHelper() {
		
	}
	
	public static void showError(final String message, final String reason) {
		showMessage(MessageDialog.ERROR, message, reason);
	}

	public static void showWarning(final String message, final String reason) {
		showMessage(MessageDialog.WARNING, message, reason);
	}

	private static void showMessage(final int messageDialogType, final String message, final String reason) {
		Display.getDefault().syncExec(() -> {
			StringBuilder messageString = new StringBuilder();
			messageString.append(message);
			if (reason != null) {
				messageString.append("\n\nReason:\n" + reason);
			}
			if (messageDialogType == MessageDialog.ERROR) {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", messageString.toString());
			} else if (messageDialogType == MessageDialog.WARNING) {
				MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warning", messageString.toString());
			}
		});
	}

	public static String toString(double[] value) {
		return toString(value, ',');
	}

	public static String toString(double[] value, char delimiter) {
		StringBuilder stringBuilder = new StringBuilder();
		if (value.length > 0) {
			for (double selected : value) {
				stringBuilder.append(selected);
				stringBuilder.append(delimiter);
			}
			removeLastChar(stringBuilder);
		}
		return stringBuilder.toString();
	}

	public static <T> String toString(T[] value) {
		return toString(value, ',');
	}

	public static <T> String toString(T[] value, char delimiter) {
		StringBuilder stringBuilder = new StringBuilder();
		if (value.length > 0) {
			for (T selected : value) {
				stringBuilder.append(selected);
				stringBuilder.append(delimiter);
			}
			removeLastChar(stringBuilder);
		}
		return stringBuilder.toString();
	}

	public static String toString(int[] value) {
		StringBuilder stringBuilder = new StringBuilder();
		if (value.length > 0) {
			for (int selected : value) {
				stringBuilder.append(selected);
				stringBuilder.append(",");
			}
			removeLastChar(stringBuilder);
		}
		return stringBuilder.toString();
	}

	public static int[] toArray(String commaSepString) {
		String[] strValues = commaSepString.split(",");
		int[] values = new int[strValues.length];
		for (int i = 0; i < strValues.length; i++) {
			values[i] = Integer.parseInt(strValues[i]);
		}
		return values;
	}

	public static StringBuilder removeLastChar(StringBuilder stringBuilder) {
		return stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
	}

	public static int getDecimalPlacePowValue(int decimalPlace) {
		return (int) Math.pow(10, decimalPlace);
	}

	public static String roundDoubletoString(double value) {
		return String.format("%." + DEFAULT_DECIMAL_PLACE + "f", value);
	}

	public static String roundDoubletoStringWithOptionalDigits(double value) {
		return DECIMAL_FORMAT.format(value);
	}

	public static String roundDoubletoString(double value, int decimalPlaces) {
		return String.format("%." + decimalPlaces + "f", value);
	}

	public static double roundDouble(double value) {
		int defaultDecimal = getDecimalPlacePowValue(DEFAULT_DECIMAL_PLACE);
		return Math.round(value * defaultDecimal) / (double) defaultDecimal;
	}

	public static GridLayout createGridLayoutWithNoMargin(int columns, boolean equal) {
		GridLayout layout = new GridLayout(columns, equal);
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.marginLeft = 0;
		layout.marginTop = 0;
		return layout;
	}
	
	public static String getUniqueFilenameWithSuffixInDirectory(File file, String suffix, String destinationFolder) {
		int counter = 0;
		String baseName = file.getName();
		File newFileBase = new File(destinationFolder, baseName);
		while (true) {
			String newFileName = getFileNameWithSuffix(newFileBase, counter == 0 ? suffix : suffix + "_" + Integer.toString(counter));
			File newFile = new File(destinationFolder, newFileName);
			if (!newFile.exists()) {
				return newFile.getAbsolutePath();
			}
			counter++;
		}
	}
	
	public static String getFileNameWithSuffix(File file, String suffix) {
		return FilenameUtils.removeExtension(file.getName()) + "_" + suffix + "." +  FilenameUtils.getExtension(file.getName());
	}
}
