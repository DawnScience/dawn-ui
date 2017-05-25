package org.dawnsci.surfacescatter.ui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StitchedOverlapCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private Dataset[] output;
    private String title;
    private ArrayList<IDataset> xArrayList;
    private ArrayList<IDataset> yArrayList;
    private ArrayList<IDataset> yArrayListError;
    private ArrayList<IDataset> yArrayListFhkl;
    private ArrayList<IDataset> yArrayListFhklError;
    private ArrayList<IDataset> yArrayListRaw;
    private ArrayList<IDataset> yArrayListRawError;
    private ILineTrace lt1;
    private SurfaceScatterPresenter ssp;
    private IDataset[] attenuatedDatasets;
    
    public StitchedOverlapCurves(Composite parent, 
    		int style,
    		ArrayList<IDataset> xArrayList,
			ArrayList<IDataset> yArrayList,
			ArrayList<IDataset> yArrayListError,
			ArrayList<IDataset> yArrayListFhkl,
			ArrayList<IDataset> yArrayListFhklError,
			ArrayList<IDataset> yArrayListRaw,
			ArrayList<IDataset> yArrayListRawError,
    		String title, 
    		OverlapUIModel model,
    		SurfaceScatterPresenter ssp) {
    	
        super(parent, style);
        
        new Label(this, SWT.NONE).setText(title);
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {

			e2.printStackTrace();
		}
        
        this.xArrayList = xArrayList;
        this.yArrayList = yArrayList;
        this.yArrayListError = yArrayListError;
        this.yArrayListFhkl = yArrayListFhkl;
        this.yArrayListFhklError = yArrayListFhklError;
        this.yArrayListRaw = yArrayListRaw;
        this.yArrayListRawError = yArrayListRawError;
        this.ssp = ssp;
        
        this.createContents(xArrayList,
    			yArrayList,
    			yArrayListError,
    			yArrayListFhkl,
    			yArrayListFhklError,
    			yArrayListRaw,
    			yArrayListRawError,
        		title, model); 
    }
     
    public void createContents(ArrayList<IDataset> xArrayList,
			ArrayList<IDataset> yArrayList,
			ArrayList<IDataset> yArrayListError,
			ArrayList<IDataset> yArrayListFhkl,
			ArrayList<IDataset> yArrayListFhklError, 
			ArrayList<IDataset> yArrayListRaw,
			ArrayList<IDataset> yArrayListRawError,
			String filepaths, 
			OverlapUIModel model) {
    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
    
		lt1 = plotSystem.createLineTrace("Concatenated Curve Test");
		
		attenuatedDatasets = ssp.curveStitchingOutput();
		
		Dataset[] sortedAttenuatedDatasets = new Dataset[2];
	
		lt1.setData(attenuatedDatasets[1], attenuatedDatasets[0]);
		
		
		output = sortedAttenuatedDatasets;

		plotSystem.addTrace(lt1);
		plotSystem.repaint();
        
		title = "Overlap Window";

		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
						
				
				double[][] maxMinArray = AttenuationCorrectedOutput.maxMinArrayGenerator(xArrayList,
																						 model);
				
				
				
				attenuatedDatasets = ssp.curveStitchingOutput(maxMinArray);
				
//				attenuatedDatasets =  AttenuationCorrectedOutput.StitchingOverlapProcessMethod(yArrayList, 
//																								xArrayList, 
//																								model);
				
				Dataset[] sortedAttenuatedDatasets = new Dataset[2];
										
				lt1.setData(attenuatedDatasets[1], attenuatedDatasets[0]);
				
				output = sortedAttenuatedDatasets;
				
				plotSystem.clearTraces();
				plotSystem.addTrace(lt1);
				plotSystem.repaint();
				
			}
		});
		

        plotSystem.getPlotComposite().setLayoutData(gd_secondField);
    } 
		
    public Composite getComposite(){
    	return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem;
   }
   
   public List<IContain1DData> getOutput(){

	output[1].setName("x");
	output[0].setName("y");
	
	List<IContain1DData> output1 = new ArrayList<IContain1DData>();
	output1.add(new Contain1DDataImpl(output[1], Arrays.asList(new IDataset[]{output[0]}), title +"_output", title +"_output_longname"));
	
	return output1;
   }
   
   public ILineTrace getLineTrace1(){
	   return lt1;
   }
   
   public IDataset[] getAttenuatedDatasets(){
	   return attenuatedDatasets;
   }
   
}