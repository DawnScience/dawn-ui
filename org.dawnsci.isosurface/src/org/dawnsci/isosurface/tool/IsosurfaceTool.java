package org.dawnsci.isosurface.tool;

import javafx.embed.swt.FXCanvas;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.dawnsci.isosurface.GeneratorFactory;
import org.dawnsci.isosurface.IsosurfaceGenerator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceEvent;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalEvent;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

/**
 * 
 * @author nnb55016
 * Class for visualising isosurfaces in DAWN
 */
public class IsosurfaceTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(IsosurfaceTool.class);

    // Listeners
	private DimensionalListener dimensionalListener;
	private AxisChoiceListener  axisChoiceListener;
	
	// Maths
	private IsosurfaceGenerator generator;
	private IsosurfaceJob       job;
	
	// UI Stuff
	private Control   originalPlotControl;
	private FXCanvas  canvas;
	private Composite controls;
	private Scale     isovalue;
	private Text      isoText, xDim, yDim, zDim;
	
	public IsosurfaceTool() {
		
		generator = GeneratorFactory.createMarchingCubes();

		this.dimensionalListener = new DimensionalListener() {			
			@Override
			public void dimensionsChanged(DimensionalEvent evt) {
				update();
			}
		};

		this.axisChoiceListener = new AxisChoiceListener() {
			@Override
			public void axisChoicePerformed(AxisChoiceEvent evt) {
				update();
			}
		};
	}
	
	@Override
	public void dispose() {
		super.dispose();
        if (canvas!=null) canvas.dispose();
	}
	
	/**
	 *  Create controls for the surface in the user interface
	 */
	public void createToolComponent(Composite parent) {

		controls = new Composite(parent, SWT.NONE);
		controls.setLayout(new GridLayout(4, false));
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label isovalueLabel = new Label(controls, SWT.NONE);
		isovalueLabel.setText("Isovalue");
		
		this.isovalue = new Scale(controls, SWT.NONE);		
 		isovalue.setMaximum(1000);
		isovalue.setMinimum(0);
		isovalue.setIncrement(1);
		isovalue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		this.isoText  = new Text(controls, SWT.BORDER);
		isoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Label boxLabel = new Label(controls, SWT.NONE);
		boxLabel.setText("Box Size   ");

	    this.zDim = new Text(controls, SWT.BORDER);
		zDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		this.xDim = new Text(controls, SWT.BORDER);
		xDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		this.yDim = new Text(controls, SWT.BORDER);
	    yDim.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    
		isovalue.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!isOn()) return;
				int currentValue = isovalue.getSelection();
				double isoVal = ((generator.getIsovalueMax() - generator.getIsovalueMin()) / 1000.0) * currentValue + generator.getIsovalueMin();
				isoText.setText(String.valueOf(isoVal));
				generator.setIsovalue(isoVal);
				job.compute();
			}

		});

		isoText.addModifyListener(new ModifyListener(){

			FloatDecorator floatText = new FloatDecorator(isoText);

			@Override
			public void modifyText(ModifyEvent e) {
				if (!isOn()) return;
				double currentValue = floatText.getValue().doubleValue();
				isovalue.setSelection((int) ((currentValue-generator.getIsovalueMin())*1000.0 / (generator.getIsovalueMax() - generator.getIsovalueMin())));
				generator.setIsovalue(currentValue);
				job.compute();
			}

		});

		xDim.addModifyListener(new ModifyListener(){

			IntegerDecorator intText = new IntegerDecorator(xDim);

			@Override
			public void modifyText(ModifyEvent e) {
				if (!isOn()) return;
				double xSize =  intText.getValue().doubleValue();
				if (xSize > 0 && xSize < generator.getData().getShape()[2]){
					int[] boxSize = new int[] {(int) xSize, generator.getBoxSize()[1], generator.getBoxSize()[2]};
					generator.setBoxSize(boxSize);
				}
			}

		});
		xDim.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				job.compute();
			}
		});

		yDim.addModifyListener(new ModifyListener(){

			IntegerDecorator intText = new IntegerDecorator(yDim);

			@Override
			public void modifyText(ModifyEvent e) {
				if (!isOn()) return;
				double ySize = intText.getValue().doubleValue();
				if(ySize > 0 && ySize < generator.getData().getShape()[1]){
					int[] boxSize = new int[] {generator.getBoxSize()[0], (int) ySize, generator.getBoxSize()[2]};
					generator.setBoxSize(boxSize);
				}
			}

		});
		yDim.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				job.compute();
			}
		});

		zDim.addModifyListener(new ModifyListener() {

			IntegerDecorator intText = new IntegerDecorator(zDim);

			@Override
			public void modifyText(ModifyEvent e) {
				if (!isOn()) return;
				double zSize = intText.getValue().doubleValue();
				if(zSize > 0 && zSize < generator.getData().getShape()[0]){
					int[] boxSize = new int[] {generator.getBoxSize()[0], generator.getBoxSize()[1], (int) zSize};
					generator.setBoxSize(boxSize);
				}
			}
		});
		zDim.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				job.compute();
			}
		});
		
		setControlsVisible(false);

	}

	/**
	 * Method that shows the display of the isosurface while the corresponding button is selected
	 */
	@Override
	public void militarize() {
		
	    getSlicingSystem().setSliceType(getSliceType());

		final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
        
		// We make the 3D canvas and job the first time.
		if (canvas==null){
			canvas = new FXCanvas(plotSystem.getPlotComposite(), SWT.NONE);
			job    = new IsosurfaceJob("Computing isosurface", this);
		}
		originalPlotControl = plotSystem.setControl(canvas, false);
				
		setControlsVisible(true);
		
 		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);   		
		
 		getSlicingSystem().update(false);
		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
		
		update();
	}


	private void setControlsVisible(boolean vis) {
		GridUtils.setVisible(controls, vis);
		controls.getParent().layout();
	}

	/**
	 * Called to update when lazy data changed.
	 */
	private void update() {
		
		try {
			final SliceSource data = getSlicingSystem().getData();
			if (data.getLazySet()==generator.getData()) return;
			
			ILazyDataset slice = data.getLazySet().getSliceView(getSlices());
			slice = slice.squeeze();
			slice.setName("Sliced "+data.getLazySet().getName());
			if (slice.getRank()!=3) throw new RuntimeException("Invalid slice for isosurface tool!");
			if (slice==generator.getData()) return; // Unlikely, will be new instances
			
			job.compute(slice);
			
		} catch (Exception e) {
			logger.error("Cannot compute iso-surface!", e);
		}
	}

	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {

		if (dimensionalListener!=null) {
			getSlicingSystem().removeDimensionalListener(dimensionalListener);
		}
		if (axisChoiceListener!=null) {
			getSlicingSystem().removeAxisChoiceListener(axisChoiceListener);
		}

		setControlsVisible(false);
		
		if (originalPlotControl==null) return;
        final IPlottingSystem plotSystem = getSlicingSystem().getPlottingSystem();
		plotSystem.setControl(originalPlotControl, true);
		
		originalPlotControl = null;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getSliceType() {
		return PlotType.ISOSURFACE;
	}
	
	@Override
	public boolean isSliceRequired() {
		return false;
	}
	
	@Override
	public boolean isAdvancedSupported() {
		return false;
	}
	
	protected IsosurfaceGenerator getGenerator() {
		return generator;
	}

	protected FXCanvas getCanvas() {
		return canvas;
	}

	protected void updateUI() {
		
		if (!isOn()) return;
		try {
			setOn(false);
			isovalue.setSelection((int) ((generator.getIsovalue()- generator.getIsovalueMin())*1000/(generator.getIsovalueMax()-generator.getIsovalueMin()) ));
			isoText.setText(String.valueOf(generator.getIsovalue()));
			xDim.setText(String.valueOf(generator.getBoxSize()[0]));
			yDim.setText(String.valueOf(generator.getBoxSize()[1]));
			zDim.setText(String.valueOf(generator.getBoxSize()[2]));
			
		} finally {
			setOn(true);
		}
	}
}
