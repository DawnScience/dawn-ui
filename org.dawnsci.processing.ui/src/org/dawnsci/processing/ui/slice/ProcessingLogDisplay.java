package org.dawnsci.processing.ui.slice;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Widget to show log from a processing operation
 */
public class ProcessingLogDisplay {
	Text logDisplay = null;
	private ScrolledComposite sc;

	/**
	 * Construct a log widget. NB parent should have a FillLayout
	 * @param parent
	 */
	public ProcessingLogDisplay(Composite parent) {
		sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		logDisplay = new Text(sc, SWT.READ_ONLY | SWT.MULTI);
		logDisplay.setFont(JFaceResources.getTextFont());
		sc.setContent(logDisplay);
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		sc.setMinSize(logDisplay.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void dispose() {
		logDisplay.dispose();
	}

	/**
	 * Set and display log string
	 * @param string
	 */
	public void setLog(String string) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				logDisplay.setText(string);
				sc.setMinSize(logDisplay.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				sc.showControl(logDisplay);
			}
		});
	}

	/**
	 * Clear log
	 */
	public void clear() {
		setLog("");
	}
}
