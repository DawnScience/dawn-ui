package org.dawnsci.processing.ui.slice;

import java.util.ArrayList;
import java.util.Arrays;
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
				StyleRange[] ranges = createStyleRanges(good, bad);
				if (ranges != null) {
					logDisplay.setStyleRanges(ranges);
				}
				logDisplay.setTopIndex(logDisplay.getLineCount() - 1);
			}
		});
	}

	private StyleRange[] createStyleRanges(List<Integer> listA, List<Integer> listB) {
		if ((listA == null || listA.isEmpty()) && (listB == null || listB.isEmpty())) {
			return null;
		}

		List<StyleRange> ranges = new ArrayList<>();

		if (listA != null) {
			addStyleRanges(listA, ranges, Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
		}

		if (listB != null) {
			addStyleRanges(listB, ranges, Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		}

		StyleRange[] array = ranges.toArray(new StyleRange[ranges.size()]);
		Arrays.sort(array, (s,t) -> s.start < t.start ? -1 : 1);
		return array;
	}

	private void addStyleRanges(List<Integer> list, List<StyleRange> ranges, Color color) {
		for (int i = 0, max = list.size() / 2; i < max; i++) {
			StyleRange sr = new StyleRange();
			sr.start = list.get(2 * i);
			sr.length = list.get(2 * i + 1);
			sr.fontStyle = SWT.BOLD;
			sr.foreground = color;
			ranges.add(sr);
		}
	}

	/**
	 * Clear log
	 */
	public void clear() {
		setLog("");
	}
}
