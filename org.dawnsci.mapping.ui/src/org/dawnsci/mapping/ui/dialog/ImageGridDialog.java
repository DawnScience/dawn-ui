package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.Activator;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageGridDialog extends Dialog {

	private static Logger logger = LoggerFactory.getLogger(ImageGridDialog.class);

	List<IDataset> data;
	private List<IPlottingSystem> systems = new ArrayList<IPlottingSystem>();

	public ImageGridDialog(Shell parentShell, List<IDataset> data) throws Exception {
		super(parentShell);
		if (data == null || data.isEmpty())
			throw new Exception("No data is available to visualize in the Comparison Image Viewer dialog.");
		this.data = data;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setDefaultImage(Activator.getImageDescriptor("icons/images-stack.png").createImage());
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

	@Override
	public Control createContents(Composite parent) {
		Color white = new Color(Display.getDefault(), 255, 255, 255);
		Composite container = (Composite) super.createDialogArea(parent);
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

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Comparison Viewer");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
}
