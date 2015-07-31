package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBMixerDialog extends Dialog {

	private static Logger logger = LoggerFactory.getLogger(RGBMixerDialog.class);

	private List<Dataset> data;
	private CompoundDataset compData;
	private IPlottingSystem system;

	private int idxR = -1, idxG = -1, idxB = -1;
	private Dataset zeros;

	public RGBMixerDialog(Shell parentShell, List<IDataset> data) {
		super(parentShell);
		
		this.data = new ArrayList<Dataset>();
		
		for (IDataset d : data) {
			
			double max = d.max().doubleValue();
			double min = d.min().doubleValue();
			
			Dataset da = DatasetUtils.convertToDataset(d.clone());
			da.isubtract(min).idivide(max-min).imultiply(255);
			this.data.add(da);
			
		}
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Error creating RGB plotting system:", e);
		}
	}

	@Override
	public Control createContents(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite leftPane = new Composite(container, SWT.NONE);
		leftPane.setLayout(new GridLayout(2, false));
		leftPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		//generate combos
		String[] dataNames = new String[data.size() + 1];
		dataNames[0] = "None";
		for (int i = 0; i < data.size(); i ++) {
			dataNames[i + 1] = data.get(i).getName();
		}
		Label redLabel = new Label(leftPane, SWT.RIGHT);
		redLabel.setText("Red:");
		final Combo redCombo = new Combo(leftPane, SWT.NONE);
		redCombo.setItems(dataNames);
		redCombo.select(0);
		redCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxR = redCombo.getSelectionIndex() - 1 ;
				updatePlot();
				//TODO plot converted RGB data from dataset
			}
		});

		Label greenLabel = new Label(leftPane, SWT.RIGHT);
		greenLabel.setText("Green:");
		final Combo greenCombo = new Combo(leftPane, SWT.NONE);
		greenCombo.setItems(dataNames);
		greenCombo.select(0);
		greenCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxG = greenCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});

		Label blueLabel = new Label(leftPane, SWT.RIGHT);
		blueLabel.setText("Blue:");
		final Combo blueCombo = new Combo(leftPane, SWT.NONE);
		blueCombo.setItems(dataNames);
		blueCombo.select(0);
		blueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				idxB = blueCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});

		Composite plotContainer = new Composite(container, SWT.NONE);
		plotContainer.setLayout(new GridLayout(1, false));
		plotContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		system.createPlotPart(plotContainer, "RGB Plot", null, PlotType.IMAGE, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button closeButton = new Button(container, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				RGBMixerDialog.this.close();
			}
		});

		return container;
	}

	private void updatePlot() {
		zeros = new IntegerDataset(data.get(0).getSize());
		zeros.setShape(data.get(0).getShape());
		
		if (idxR >= 0 && idxG >= 0 && idxB >= 0) {
			
			compData = new RGBDataset(data.get(idxR), data.get(idxG), data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG < 0 && idxB < 0) {
			compData = new RGBDataset(data.get(idxR), zeros, zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG >= 0 && idxB <0) {
			compData = new RGBDataset(zeros, data.get(idxG), zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG < 0 && idxB >= 0) {
			compData = new RGBDataset(zeros, zeros, data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG >= 0 && idxB < 0) {
			compData = new RGBDataset(data.get(idxR), data.get(idxG), zeros);
			system.updatePlot2D(compData, null, null);
		} else if (idxR >= 0 && idxG < 0 && idxB >= 0) {
			compData = new RGBDataset(data.get(idxR), zeros, data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG >= 0 && idxB >= 0) {
			compData = new RGBDataset(zeros, data.get(idxG), data.get(idxB));
			system.updatePlot2D(compData, null, null);
		} else if (idxR < 0 && idxG < 0 && idxB < 0) {
			system.clear();
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("RGB Mixer");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}
}
