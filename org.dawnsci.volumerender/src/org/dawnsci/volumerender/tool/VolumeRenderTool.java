package org.dawnsci.volumerender.tool;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisChoiceListener;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimensionalListener;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

public class VolumeRenderTool<T> extends AbstractSlicingTool
{	
	private VolumeRenderJob job;
	
	private Composite comp;
	private Button generateButton;
	private Button deleteButton;
	private Slider resolutionSlider;
	private Slider intensitySlider;
	private Slider opacitySlider;
	
	private Text histogramMaxText;
	private Text histogramMinText;
	
	private Text cullingMaxText;
	private Text cullingMinText;
	
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
		comp.setLayout(new GridLayout(6, false));
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(data);
		data.exclude = true;
		
		Label disclaimer1 = new Label(comp, SWT.NONE);
		disclaimer1.setText("Volume renderer pre-Alpha snapshot:");
		disclaimer1.setForeground(new Color(parent.getDisplay(), new RGB(255,0,0)));
		disclaimer1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1));
				
		Label disclaimer2 = new Label(comp, SWT.NONE);
		disclaimer2.setText("- Please use nightly build if possible");
		disclaimer2.setForeground(new Color(parent.getDisplay(), new RGB(255,0,0)));
		disclaimer2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 6, 1));
		
		
		/////////////////////////
		/////////// GUI /////////
		/////////////////////////
		
		Label resolutionLabel = new Label(comp, SWT.NONE);
		resolutionLabel.setText("Resolution");
		
		resolutionSlider = new Slider(comp, SWT.NONE);
		resolutionSlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		resolutionSlider.setMaximum(100);
		resolutionSlider.setMinimum(0);
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		
		Label intensityLabel = new Label(comp, SWT.NONE);
		intensityLabel.setText("Intensity");
		
		intensitySlider = new Slider(comp, SWT.NONE);
		intensitySlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		intensitySlider.setMaximum(100);
		intensitySlider.setMinimum(0);
		
		Label opacityLabel = new Label(comp, SWT.NONE);
		opacityLabel.setText("Opacity");
		
		opacitySlider = new Slider(comp, SWT.NONE);
		opacitySlider.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		opacitySlider.setMaximum(100);
		opacitySlider.setMinimum(0);
		
		
		// HISTOGRAM
		Label histogramLabel = new Label(comp, SWT.NONE);
		histogramLabel.setText("Histogram Range:");
		
		Label histogramMinLabel = new Label(comp, SWT.NONE);
		histogramMinLabel.setText("Min");

		histogramMinText = new Text(comp, SWT.BORDER | SWT.FILL);
		histogramMinText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label histogramMaxLabel = new Label(comp, SWT.NONE);
		histogramMaxLabel.setText("Max");

		histogramMaxText = new Text(comp, SWT.BORDER | SWT.FILL);
		histogramMaxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(comp, SWT.NONE);
		
		// CULLING
		Label cullingLabel = new Label(comp, SWT.NONE);
		cullingLabel.setText("Culling Range:");
		
		Label cullingMinLabel = new Label(comp, SWT.NONE);
		cullingMinLabel.setText("Min");

		cullingMinText = new Text(comp, SWT.BORDER | SWT.FILL);
		cullingMinText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		Label cullingMaxLabel = new Label(comp, SWT.NONE);
		cullingMaxLabel.setText("Max");

		cullingMaxText = new Text(comp, SWT.BORDER | SWT.FILL);
		cullingMaxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(comp, SWT.NONE);
		
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
		        	  job.destroy(TRACE_ID);
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
		
		double[] minMaxValue = {
					safeParseDouble(histogramMinText.getText()),
					safeParseDouble(histogramMaxText.getText()),
	   	};
	   	  
		double[] minMaxCulling = {
					safeParseDouble(cullingMinText.getText()),
					safeParseDouble(cullingMaxText.getText()),
		};
			
		job.compute(
	  			  TRACE_ID,
	  			  resolutionSlider.getSelection(),
	  			  intensitySlider.getSelection(),
	  			  opacitySlider.getSelection(),
	  			  getSlicingSystem().getData().getLazySet().getTransposedView(xIndex, yIndex, zIndex),
	  			  minMaxValue,
	  			  minMaxCulling);
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
}
