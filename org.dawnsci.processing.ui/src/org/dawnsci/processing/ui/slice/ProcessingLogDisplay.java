package org.dawnsci.processing.ui.slice;

import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Widget to show log from a processing operation
 */
public class ProcessingLogDisplay {
	StyledText logDisplay = null;

	/**
	 * Construct a log widget. NB parent should have a FillLayout
	 * @param parent
	 */
	public ProcessingLogDisplay(Composite parent) {
		logDisplay = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		logDisplay.setFont(JFaceResources.getTextFont());
	}

	public void dispose() {
		logDisplay.dispose();
	}

	/**
	 * Set and display log string
	 * @param string
	 */
	public void setLog(String string) {
		setLog(string, null, null);
	}

	/**
	 * Set and display log string
	 * @param string
	 * @param good
	 * @param bad
	 */
	public void setLog(String string, List<Integer> good, List<Integer> bad) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				logDisplay.setText(string);
				logDisplay.setStyleRanges(createStyleRanges(good, bad));
				logDisplay.setTopIndex(logDisplay.getLineCount() - 1);
			}
		});
	}

	private StyleRange[] createStyleRanges(List<Integer> listA, List<Integer> listB) {
		int maxA = listA == null ? 0 : listA.size() / 2;
		int maxB = listB == null ? 0 : listB.size() / 2;
		StyleRange[] ranges = maxA + maxB > 0 ? new StyleRange[maxA + maxB] : null;

		Color colorA = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		for (int i = 0; i < maxA; i++) {
			StyleRange sr = new StyleRange();
			sr.start = listA.get(2 * i);
			sr.length = listA.get(2 * i + 1);
			sr.fontStyle = SWT.BOLD;
			sr.foreground = colorA;
			ranges[i] = sr;
		}

		Color colorB = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		for (int i = 0; i < maxB; i++) {
			StyleRange sr = new StyleRange();
			sr.start = listB.get(2 * i);
			sr.length = listB.get(2 * i + 1);
			sr.fontStyle = SWT.BOLD;
			sr.foreground = colorB;
			ranges[i + maxA] = sr;
		}

		return ranges;
	}

	/**
	 * Clear log
	 */
	public void clear() {
		setLog("");
	}
}
