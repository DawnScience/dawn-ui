package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.mapping.ui.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.CompoundDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.rangeSlider.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBMixerDialog {

	private static Logger logger = LoggerFactory.getLogger(RGBMixerDialog.class);

	private int result;

	/**
	 * Button id for an "Ok" button (value 0).
	 */
	public static int OK_ID = 0;

	/**
	 * Button id for a "Cancel" button (value 1).
	 */
	public static int CANCEL_ID = 1;

	private List<Dataset> data;
	private CompoundDataset compData;
	private IPlottingSystem system;
	private Image image;

	private int idxR = -1, idxG = -1, idxB = -1;
	private boolean rDirty = true, bDirty = true, gDirty = true;
	private boolean rLog= false, bLog= false, gLog = false;
	private Dataset red, blue, green;
	private Dataset zeros;

	private RangeSlider redRangeSlider;

	private RangeSlider greenRangeSlider;

	private RangeSlider blueRangeSlider;

	private Shell shell;

	public RGBMixerDialog(List<IDataset> data) throws Exception {
		shell = new Shell(Display.getDefault());
		shell.setText("RGB Mixer");
		shell.setImage(image = Activator.getImageDescriptor("icons/rgb.png").createImage());
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				result = CANCEL_ID;
				RGBMixerDialog.this.close();
			}
		});

		if (data.isEmpty())
			throw new Exception("No data is available to visualize in the RGB Mixer dialog.");
		this.data = new ArrayList<Dataset>();
		int width = data.get(0).getShape()[0];
		int height = data.get(0).getShape()[1];
		for (IDataset d : data) {
			if (width != d.getShape()[0] || height != d.getShape()[1]) {
				throw new Exception("Data has not the same size");
			}
			
			Dataset da = DatasetUtils.convertToDataset(d);
			this.data.add(da);
		}
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			String error = "Error creating RGB plotting system:" + e.getMessage();
			logger.error("Error creating RGB plotting system:", e);
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
		system.createPlotPart(topPane, "RGB Plot", actionBarWrapper, PlotType.IMAGE, null);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite bottomPane = new Composite(container, SWT.NONE);
		bottomPane.setLayout(new GridLayout(3, false));
		bottomPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		//generate combos
		String[] dataNames = new String[data.size() + 1];
		dataNames[0] = "None";
		for (int i = 0; i < data.size(); i ++) {
			dataNames[i + 1] = data.get(i).getName();
		}

		Composite redComp = new Composite(bottomPane, SWT.NONE);
		redComp.setLayout(new GridLayout(2, false));
		Label redLabel = new Label(redComp, SWT.RIGHT);
		redLabel.setText("Red:");
		final Combo redCombo = new Combo(redComp, SWT.CENTER);
		redCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		redCombo.setItems(dataNames);
		redCombo.select(0);
		redCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				rDirty = true;
				idxR = redCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		redRangeSlider = new RangeSlider(redComp, SWT.HORIZONTAL);
		redRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		redRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				rDirty = true;
				updatePlot();
				System.out.println("moving");
			}
		});
		final Button redLogButton = new Button(redComp, SWT.CHECK);
		redLogButton.setText("Apply Log on red channel");
		redLogButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		redLogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				rDirty = true;
				rLog = redLogButton.getSelection();
				updatePlot();
				System.out.println("log red");
			}
		});

		Composite greenComp = new Composite(bottomPane, SWT.NONE);
		greenComp.setLayout(new GridLayout(2, false));
		Label greenLabel = new Label(greenComp, SWT.RIGHT);
		greenLabel.setText("Green:");
		final Combo greenCombo = new Combo(greenComp, SWT.CENTER);
		greenCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		greenCombo.setItems(dataNames);
		greenCombo.select(0);
		greenCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				gDirty = true;
				idxG = greenCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		greenRangeSlider = new RangeSlider(greenComp, SWT.HORIZONTAL);
		greenRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		greenRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				gDirty = true;
				updatePlot();
				System.out.println("moving");

			}
		});
		final Button greenLogButton = new Button(greenComp, SWT.CHECK);
		greenLogButton.setText("Apply Log on blue channel");
		greenLogButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		greenLogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				gDirty = true;
				gLog = greenLogButton.getSelection();
				updatePlot();
				System.out.println("log green");
			}
		});

		Composite blueComp = new Composite(bottomPane, SWT.NONE);
		blueComp.setLayout(new GridLayout(2, false));
		Label blueLabel = new Label(blueComp, SWT.RIGHT);
		blueLabel.setText("Blue:");
		final Combo blueCombo = new Combo(blueComp, SWT.CENTER);
		blueCombo.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
		blueCombo.setItems(dataNames);
		blueCombo.select(0);
		blueCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				bDirty = true;
				idxB = blueCombo.getSelectionIndex() - 1 ;
				updatePlot();
			}
		});
		blueRangeSlider = new RangeSlider(blueComp, SWT.HORIZONTAL);
		blueRangeSlider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		blueRangeSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				bDirty = true;
				updatePlot();
				System.out.println("moving");
			}
		});
		blueRangeSlider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				bDirty = true;
				updatePlot();
			System.out.println("mouse wheel");	
			}
		});
		final Button blueLogButton = new Button(blueComp, SWT.CHECK);
		blueLogButton.setText("Apply Log on blue channel");
		blueLogButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		blueLogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				bDirty = true;
				bLog = blueLogButton.getSelection();
				updatePlot();
			}
		});

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
				RGBMixerDialog.this.close();
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
				RGBMixerDialog.this.close();
			}
		});
		return container;
	}
	
	public IDataset getRGBDataset() {
		return compData;
	}

	private void updatePlot() {
		if (data.isEmpty())
			return;
		
		if (zeros == null) {
			zeros = new IntegerDataset(data.get(0).getSize());
			zeros.setShape(data.get(0).getShape());
		}
		
		Dataset r = red;
		Dataset b = blue;
		Dataset g = green;
		
		if (idxR >= 0) {
			if (rDirty) {
				red = r = update(data.get(idxR),redRangeSlider.getLowerValue(),redRangeSlider.getUpperValue(),rLog);
				rDirty = false;
			};
		} else {
			r = zeros;
		}
		
		if (idxG >= 0) {
			if (gDirty) {
				green = g = update(data.get(idxG),greenRangeSlider.getLowerValue(),greenRangeSlider.getUpperValue(),gLog);
				gDirty = false;
			};
		} else {
			g = zeros;
		}
		if (idxB >= 0) {
			if (bDirty) {
				blue = b = update(data.get(idxB),blueRangeSlider.getLowerValue(),blueRangeSlider.getUpperValue(),bLog);
				bDirty = false;
			};
		} else {
			b = zeros;
		}
		

		
		if (idxR < 0 && idxG < 0 && idxB < 0) {
			system.clear();
		} else {
			compData = new RGBDataset(r,g,b);
			system.updatePlot2D(compData, null, null);	
		}
		
//		if (idxR >= 0 && idxG >= 0 && idxB >= 0) {
//			compData = new RGBDataset(data.get(idxR), data.get(idxG), data.get(idxB));
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR >= 0 && idxG < 0 && idxB < 0) {
//			compData = new RGBDataset(data.get(idxR), zeros, zeros);
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR < 0 && idxG >= 0 && idxB <0) {
//			compData = new RGBDataset(zeros, data.get(idxG), zeros);
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR < 0 && idxG < 0 && idxB >= 0) {
//			compData = new RGBDataset(zeros, zeros, data.get(idxB));
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR >= 0 && idxG >= 0 && idxB < 0) {
//			compData = new RGBDataset(data.get(idxR), data.get(idxG), zeros);
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR >= 0 && idxG < 0 && idxB >= 0) {
//			compData = new RGBDataset(data.get(idxR), zeros, data.get(idxB));
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR < 0 && idxG >= 0 && idxB >= 0) {
//			compData = new RGBDataset(zeros, data.get(idxG), data.get(idxB));
//			system.updatePlot2D(compData, null, null);
//		} else if (idxR < 0 && idxG < 0 && idxB < 0) {

	}
	
	private Dataset update(Dataset ds, int lower, int upper, boolean log) {
		double dMin = ds.min().doubleValue();
		double dMax = ds.max().doubleValue();
		
//		if (log) {
//			dMin = Math.log10(dMin);
//			dMax = Math.log10(dMax);
//		}
		
		double dRange = dMax - dMin;
		int min = lower;
		int max = upper;

		double mi = dRange*((double)min/100)+dMin;
		double ma = dMax - dRange*(100-max)/100;
		
//		if (log) {
//			mi = Math.pow(10, mi);
//			ma = Math.pow(10, ma);
//		}
		
		return updateDataset(ds, mi, ma, log);
	}
	
	private Dataset updateDataset(Dataset ds, double min, double max, boolean log) {
		Dataset out = ds.getSlice();
		
		if (log) {
			out = Maths.log10(out);
			min = Math.log10(min);
			max = Math.log10(max);
		}
		
		out.isubtract(min).idivide(max-min).imultiply(255);
		IndexIterator it = out.getIterator();
		while (it.hasNext()) {
			double val = out.getElementDoubleAbs(it.index);
			if (val < 0) out.setObjectAbs(it.index, 0);
			if (val > 255) out.setObjectAbs(it.index, 255);
			if (Double.isNaN(val)) out.setObjectAbs(it.index, 0);
		}
		
		double mi = out.min().doubleValue();
		double ma = out.max().doubleValue();
		System.err.println(mi);
		System.err.println(ma);
		return out;
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
