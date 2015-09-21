package org.dawnsci.spectrum.ui.dialogs;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.spectrum.ui.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombineDialog {

	private static Logger logger = LoggerFactory.getLogger(CombineDialog.class);

	private int result;

	/**
	 * Button id for an "Ok" button (value 0).
	 */
	public static int OK_ID = 0;

	/**
	 * Button id for a "Cancel" button (value 1).
	 */
	public static int CANCEL_ID = 1;

	private IDataset data;
	private IDataset combDataset;
	private IPlottingSystem system;
	private Image image;

	private Shell shell;

	public CombineDialog(IDataset data) throws Exception {
		shell = new Shell(Display.getDefault());
		shell.setText("Combine Dialog");
		shell.setImage(image = Activator.getImageDescriptor("icons/spectrum.png").createImage());
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				result = CANCEL_ID;
				CombineDialog.this.close();
			}
		});

		this.data = data;
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			String error = "Error creating Combine plotting system:" + e.getMessage();
			logger.error("Error creating Combine plotting system:", e);
			throw new Exception(error);
		}
	}

	/**
	 * Create the content of the Shell dialog
	 * 
	 * @return
	 */
	public Control createContents() {
		// Shell setting
		shell.setLayout(new GridLayout());
		shell.setSize(800, 600);
		Monitor primary = Display.getDefault().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);

		Composite container = new Composite(shell, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topPane = new Composite(container, SWT.NONE);
		topPane.setLayout(new GridLayout(1, false));
		topPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(topPane, null);
		system.createPlotPart(topPane, "Combine Plot", actionBarWrapper, PlotType.IMAGE, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		system.updatePlot2D(data, null, null);
		system.setKeepAspect(false);
		Composite buttonComp = new Composite(container, SWT.NONE);
		buttonComp.setLayout(new GridLayout(2, false));
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Button cancelButton = new Button(buttonComp, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		cancelButton.setSize(100, 50);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				result = CANCEL_ID;
				CombineDialog.this.close();
			}
		});

		Button okButton = new Button(buttonComp, SWT.NONE);
		okButton.setText("OK");
		okButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		okButton.setSize(100, 50);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				result = OK_ID;
				CombineDialog.this.close();
			}
		});
		return container;
	}
	
	public IDataset getCombinedDataset() {
		return combDataset;
	}

	public void close() {
		image.dispose();
		if (shell != null)
			shell.dispose();
	}

	/**
	 *open the shell dialog
	 */
	public int open() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
		return result;
	}
}
