package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.mapping.ui.datamodel.AbstractMapData;
import org.dawnsci.mapping.ui.datamodel.RGBMapData;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.mihalis.opal.rangeSlider.RangeSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RGBMixerDialog extends Dialog  {

	private static Logger logger = LoggerFactory.getLogger(RGBMixerDialog.class);

	
	private List<AbstractMapData> maps;
	private RGBMapData rgbMap;
	
	private List<Dataset> data;
	private IPlottingSystem<Composite> system;

	private int idxR = -1, idxG = -1, idxB = -1;
	private boolean rDirty = true, bDirty = true, gDirty = true;
	private boolean rLog= false, bLog= false, gLog = false;

	private RangeSlider redRangeSlider;
	private RangeSlider greenRangeSlider;
	private RangeSlider blueRangeSlider;


	public RGBMixerDialog(Shell parent, List<AbstractMapData> data) throws Exception {
		super(parent);
		if (data.isEmpty())
			throw new Exception("No data is available to visualize in the RGB Mixer dialog.");
		this.data = new ArrayList<Dataset>();
		int width = data.get(0).getMap().getShape()[0];
		int height = data.get(0).getMap().getShape()[1];
		for (AbstractMapData d : data) {
			if (width != d.getMap().getShape()[0] || height != d.getMap().getShape()[1]) {
				throw new Exception("Data has not the same size");
			}
			
			Dataset da = DatasetUtils.convertToDataset(d.getMap());
			this.data.add(da);
		}
		this.maps = data;
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
	@Override
	public Control createDialogArea(Composite parent)  {
		Composite container = (Composite) super.createDialogArea(parent);
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
		String[] dataNames = new String[maps.size() + 1];
		dataNames[0] = "None";
		for (int i = 0; i < maps.size(); i ++) {
			dataNames[i + 1] = maps.get(i).toString();
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
			}
		});
		redRangeSlider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				rDirty = true;
				updatePlot();
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
			public void widgetSelected(SelectionEvent event) {
				gDirty = true;
				updatePlot();
			}
		});
		greenRangeSlider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseScrolled(MouseEvent e) {
				gDirty = true;
				updatePlot();
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

		return container;
	}
	
	public RGBMapData getRGBMap() {
		return rgbMap;
	}

	private void updatePlot() {
		if (data.isEmpty())
			return;

		if (idxR >= 0) {
			if (rDirty) {
				AbstractMapData amd = maps.get(idxR);
				if (rgbMap == null) rgbMap = new RGBMapData(amd.toString(), amd,amd.getParent().getPath());
				rgbMap.switchRed(amd, new int[]{redRangeSlider.getLowerValue(),redRangeSlider.getUpperValue()}, rLog);
				rDirty = false;
			};
		} else {
			rgbMap.clearRed();
		}
		
		if (idxG >= 0) {
			if (gDirty) {
				AbstractMapData amd = maps.get(idxG);
				if (rgbMap == null) rgbMap = new RGBMapData(amd.toString(), amd,amd.getParent().getPath());
				rgbMap.switchGreen(amd, new int[]{greenRangeSlider.getLowerValue(),greenRangeSlider.getUpperValue()}, gLog);
				gDirty = false;
			};
		} else {
			rgbMap.clearGreen();
		}
		if (idxB >= 0) {
			if (bDirty) {
				AbstractMapData amd = maps.get(idxB);
				if (rgbMap == null) rgbMap = new RGBMapData(amd.toString(), amd,amd.getParent().getPath());
				rgbMap.switchBlue(amd, new int[]{blueRangeSlider.getLowerValue(),blueRangeSlider.getUpperValue()}, bLog);
				bDirty = false;
			};
		} else {
			rgbMap.clearBlue();
		}
		

		
		if (idxR < 0 && idxG < 0 && idxB < 0) {
			system.clear();
		} else {
			system.updatePlot2D(rgbMap.getMap(), null, null);	
		}
		

	}
	
	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
}
