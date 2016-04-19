package org.dawnsci.volumerender.tool;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.mihalis.opal.rangeSlider.RangeSlider;

public class VolumeRenderTool<T> extends AbstractSlicingTool
{	
	private VolumeRenderJob job;
	
	private Composite comp;
	private Button generateButton;
	private Button deleteButton;
	private Scale resolutionSlider;
	private Scale intensitySlider;
	private Scale opacitySlider;
	
	private RangeSlider histogramMinMax;
	private RangeSlider cullingMinMax;
	
	private final DimensionalListener dimensionalListener = (event) -> update();
	private final AxisChoiceListener axisChoiceListener = (event) -> update();
	private final VolumeRenderJobFactory<T> volumeRenderJobFactory;
	
	private final String TRACE_ID = "123456789";

		
	public VolumeRenderTool(){
		this(new VolumeRenderJobFactory<T>());
	}
	
	public VolumeRenderTool(VolumeRenderJobFactory<T> volumeRenderJobFactory){
		this.volumeRenderJobFactory = volumeRenderJobFactory;
	}
	
	public void createToolComponent(Composite parent)
	{
		comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(data);
		data.exclude = true;
		
		/////////////////////////
		/////////// GUI /////////
		/////////////////////////
		
		Label resolutionLabel = new Label(comp, SWT.NONE);
		resolutionLabel.setText("Resolution");
		
		resolutionSlider = new Scale(comp, SWT.NONE);
		resolutionSlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		resolutionSlider.setMaximum(100);
		resolutionSlider.setMinimum(1);
		resolutionSlider.setSelection(50);
		
		Label intensityLabel = new Label(comp, SWT.NONE);
		intensityLabel.setText("Intensity");
		
		intensitySlider = new Scale(comp, SWT.NONE);
		intensitySlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		intensitySlider.setMaximum(100);
		intensitySlider.setMinimum(0);
		intensitySlider.setSelection(50);
		
		Label opacityLabel = new Label(comp, SWT.NONE);
		opacityLabel.setText("Opacity");
		
		opacitySlider = new Scale(comp, SWT.NONE);
		opacitySlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		opacitySlider.setMaximum(100);
		opacitySlider.setMinimum(0);
		opacitySlider.setSelection(50);
		
		// HISTOGRAM
		Label histogramLabel = new Label(comp, SWT.NONE);
		histogramLabel.setText("Histogram Range:");
		
		histogramMinMax = new RangeSlider(comp, SWT.NONE);
		histogramMinMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		histogramMinMax.setMinimum(0);
		histogramMinMax.setMaximum(100);
	
		// CULLING
		Label cullingLabel = new Label(comp, SWT.NONE);
		cullingLabel.setText("Culling Range:");

		cullingMinMax = new RangeSlider(comp, SWT.NONE);
		cullingMinMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		cullingMinMax.setMinimum(0);
		cullingMinMax.setMaximum(100);
		
		// COMPUTE BUTTONS
		generateButton = new Button(comp, SWT.NONE);
		generateButton.setText("Generate");
		generateButton.setVisible(true);
		
		deleteButton = new Button(comp, SWT.NONE);
		deleteButton.setText("Delete");
		deleteButton.setVisible(true);
						
		comp.setVisible(false);
	}
	
	@Override
	public void militarize(boolean newSlice) 
	{
		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList != null)
			dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);
		
		job = volumeRenderJobFactory.build(getSlicingSystem().getPlottingSystem());
		
		generateButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) 
		      {
		          if (e.type == SWT.Selection) 
		          {
		        	  update();
		          }
		        }
		});
		
		deleteButton.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event e) 
		      {
		          if (e.type == SWT.Selection) 
		          {
		        	  destroy(TRACE_ID);
		          }
		        }
		});
		
		comp.setVisible(true);
		((GridData) comp.getLayoutData()).exclude = false;
		comp.getParent().pack();
		comp.getParent().update();
		
		
		getSlicingSystem().update(false);
		getSlicingSystem().addDimensionalListener(dimensionalListener);
		getSlicingSystem().addAxisChoiceListener(axisChoiceListener);
		
	}
	
	public void update()
	{
		if (getSlicingSystem().getSliceType() == getSliceType())
			compute();
	}
	
	private void compute()
	{
		int xIndex = getSlicingSystem().getDimsDataList().getDimsData(0).getPlotAxis().getIndex();
		int yIndex = getSlicingSystem().getDimsDataList().getDimsData(1).getPlotAxis().getIndex();
		int zIndex = getSlicingSystem().getDimsDataList().getDimsData(2).getPlotAxis().getIndex();
		
		ILazyDataset view = getSlicingSystem().getData().getLazySet().getTransposedView(xIndex, yIndex, zIndex);
		
		job.compute(
				new VolumeRenderer(
				  slicingSystem.getPlottingSystem(),
	  			  TRACE_ID,
	  			  resolutionSlider.getSelection()/100.0,
	  			  intensitySlider.getSelection()/100.0,
	  			  opacitySlider.getSelection()/100.0,
	  			  histogramMinMax.getLowerValue()/100.0,
	  			  histogramMinMax.getUpperValue()/100.0,
	  			  cullingMinMax.getLowerValue()/100.0,
	  			  cullingMinMax.getUpperValue()/100.0,
	  			  view
	  			)
			);
	}
	
	public double safeParseDouble(String string){
		return Double.parseDouble(string.isEmpty() ? "0" : string);
	}
	
	@Override
	public void demilitarize()
	{
		comp.setVisible(false);
		((GridData) comp.getLayoutData()).exclude = true;
		comp.getParent().pack();
		comp.getParent().update();
	}
	
	@Override
	public Enum<?> getSliceType() {
		return PlotType.VOLUME;
	}	
	
	public void destroy(String traceID)
	{
		IPlottingSystem<?> plottingSystem = getSlicingSystem().getPlottingSystem();
		if (plottingSystem.getTrace(traceID) != null)
		{ 
			plottingSystem.getTrace(traceID).dispose();
			plottingSystem.removeTrace(plottingSystem.getTrace(traceID));
		}
	}

}
