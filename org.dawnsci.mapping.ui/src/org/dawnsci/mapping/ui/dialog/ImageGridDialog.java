package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageGridDialog {

	private static Logger logger = LoggerFactory.getLogger(ImageGridDialog.class);

	private List<IDataset> data;
	private List<IPlottingSystem> systems = new ArrayList<IPlottingSystem>();
	private Image image;
	private Shell shell;

	public ImageGridDialog(List<IDataset> data) throws Exception {
		shell = new Shell(Display.getDefault());
		shell.setText("Comparison Viewer");
		shell.setImage(image = Activator.getImageDescriptor("icons/images-stack.png").createImage());
		if (data == null || data.isEmpty())
			throw new Exception("No data is available to visualize in the Comparison Image Viewer dialog.");
		this.data = data;
		try {
			for (int i = 0; i < data.size(); i++) {
				systems.add(PlottingFactory.createPlottingSystem());
			}
		} catch (Exception e) {
			String error = "Error creating Image Grid plotting systems:" + e.getMessage();
			logger.error("Error creating Image Grid plotting systems:", e);
			throw new Exception(error);
		}
	}

	/**
	 * Create the content of the Shell dialog
	 * 
	 * @return
	 */
	public Control createContents() {
		Color white = new Color(Display.getDefault(), 255, 255, 255);
		Composite container = new Composite(shell, SWT.NONE);
		shell.setLayout(new GridLayout());
		shell.setLocation(800, 600);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setBackground(white);

		Composite plotsComp = new Composite(container, SWT.NONE);
		plotsComp.setLayout(new GridLayout(3, false));
		plotsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotsComp.setBackground(white);
		try {
			int i = 0;
			for (IPlottingSystem system : systems) {
				system.createPlotPart(plotsComp, "Plot " + i, null, PlotType.IMAGE, null);
				system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//				system.updatePlot2D(data.get(i), null, null);
				MappingUtils.plotDataWithMetadata(data.get(i), system, null);
				i++;
			}
		} catch (Exception e) {
			logger.error("Error plotting data:", e);
			e.printStackTrace();
		}
		Button closeButton = new Button(container, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				ImageGridDialog.this.close();
			}
		});
		return container;
	}

	public void close() {
		image.dispose();
		if (shell != null)
			shell.dispose();
	}

	/**
	 *open the shell dialog
	 */
	public void open() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch())
				shell.getDisplay().sleep();
		}
	}
}
