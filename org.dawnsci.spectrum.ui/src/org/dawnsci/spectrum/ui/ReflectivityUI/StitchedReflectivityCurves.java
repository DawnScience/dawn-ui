package org.dawnsci.spectrum.ui.ReflectivityUI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.spectrum.ui.file.IContain1DData;
import org.dawnsci.spectrum.ui.utils.Contain1DDataImpl;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StitchedReflectivityCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private Dataset[] output;
    private String title;
    

    
     
    public StitchedReflectivityCurves(Composite parent, int style
    		, ArrayList<ILazyDataset> arrayILDy, ArrayList<ILazyDataset> arrayILDx,
    		String title, ReflectivityUIModel model) {
        super(parent, style);
        
        new Label(this, SWT.NONE).setText(title);
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {

			e2.printStackTrace();
		}
        
        this.createContents(arrayILDy, arrayILDx, title, model); 

        
    }
     
    public void createContents(ArrayList<ILazyDataset> arrayILDy, ArrayList<ILazyDataset> arrayILDx, String filepaths
    		, ReflectivityUIModel model) {
    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        


        
        

		ILineTrace lt1 = plotSystem.createLineTrace("Concatenated Curve Test");
		
		
		IDataset[][] attenuatedDatasets = AttenuationCorrectedOutput.StitchingOverlapProcessMethod(arrayILDy, arrayILDx, model);
		
		Dataset[] sortedAttenuatedDatasets = new Dataset[2];
		
		sortedAttenuatedDatasets[0]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[0], 0));
		sortedAttenuatedDatasets[1]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[1], 0));
		
		DatasetUtils.sort(sortedAttenuatedDatasets[0],
				sortedAttenuatedDatasets[1]);
		
		
		lt1.setData(sortedAttenuatedDatasets[1], sortedAttenuatedDatasets[0]);
		output = sortedAttenuatedDatasets;

		plotSystem.addTrace(lt1);
		plotSystem.repaint();
        
		title = model.getFilepaths()[0];
		
		
		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				

				IDataset[][] attenuatedDatasets = AttenuationCorrectedOutput.StitchingOverlapProcessMethod(arrayILDy, arrayILDx, model);
				
				Dataset[] sortedAttenuatedDatasets = new Dataset[2];
				
				sortedAttenuatedDatasets[0]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[0], 0));
				sortedAttenuatedDatasets[1]=DatasetUtils.convertToDataset(DatasetUtils.concatenate(attenuatedDatasets[1], 0));
				
				DatasetUtils.sort(sortedAttenuatedDatasets[0],
						sortedAttenuatedDatasets[1]);
								
				lt1.setData(sortedAttenuatedDatasets[1], sortedAttenuatedDatasets[0]);
				
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
   
}

