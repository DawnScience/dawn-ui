package org.dawnsci.spectrum.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.january.dataset.AggregateDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotSystem1Composite  extends Composite {

	private final static Logger logger = LoggerFactory.getLogger(PlotSystem1Composite.class);

    private IPlottingSystem<Composite> plotSystem1;
    private IDataset image1;
    private Button button; 
    
    
    public PlotSystem1Composite(Composite parent, int style
    		, AggregateDataset aggDat, Slider slider, String test0, String test1, ExampleModel model) {
    	
        super(parent, style);
        //composite = new Composite(parent, SWT.NONE);

        new Label(this, SWT.NONE).setText("Operation Window");
        
        
        new Label(this, SWT.NONE).setText("test0: " + test0);
        new Label(this, SWT.NONE).setText("test1: " + test1);
        try {
			plotSystem1 = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			logger.error("Can't make plotting system", e2);
		}
        
        button = new Button (this, SWT.CHECK);
        
        
        this.createContents(aggDat, model); 
//        System.out.println("Test line");
        
    }
     
    public void createContents(AggregateDataset aggDat, ExampleModel model) {

    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
         
//        slider = new Slider(this, SWT.HORIZONTAL);
        
        
        
        final GridData gd_firstField = new GridData(SWT.FILL, SWT.LEFT, true, false);
        button.setLayoutData(gd_firstField);
        
        //new GridData(GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER));
		button.setText ("Tri-state");
		/* Make the button toggle between three states */
		button.addListener (SWT.Selection, e -> {
			if (button.getSelection()) {
				if (!button.getGrayed()) {
					button.setGrayed(true);
				}
			} else {
				if (button.getGrayed()) {
					button.setGrayed(false);
					button.setSelection (true);
				}
			}
		});
		
		SliceND slice = new SliceND(aggDat.getShape());
		
		/* Read the tri-state button (application code) */
		button.addListener (SWT.Selection, e -> {
			if (button.getGrayed()) {
//				System.out.println("Grayed");
			} else {
				if (button.getSelection()) {
//					System.out.println("Selected, trying to modify the image");
					int selection = model.getImageNumber();
					slice.setSlice(0, selection, selection+1, 1);
					IDataset j = null;
					try {
						j = aggDat.getSlice(slice);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					j.squeeze();
					IDataset image1 = DemonstrationImageSubtraction.TestCopy(j);
					plotSystem1.createPlot2D(image1, null, null);
				} else {
//					System.out.println("Not selected");
				}
			}
		});
        
		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				int selection = model.getImageNumber();
				
			    try {
			    	if (button.getSelection()){
			    		slice.setSlice(0, selection, selection+1, 1);
			    		IDataset i = aggDat.getSlice(slice);
			    		i.squeeze();
			    		plotSystem1.createPlot2D(i, null, null);
			    	}
				
			    } 
			    catch (Exception f) {
					// TODO Auto-generated catch block
					f.printStackTrace();
				}
			}
			

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				int selection = model.getImageNumber();
				
			    try {
			    	if (button.getSelection()){
			    		slice.setSlice(0, selection, selection+1, 1);
			    		IDataset i = aggDat.getSlice(slice);
			    		i.squeeze();
			    		plotSystem1.createPlot2D(i, null, null);
			    	}
				
			    } 
			    catch (Exception f) {
					// TODO Auto-generated catch block
					f.printStackTrace();
				}
			}
			
		});
	       
        
        
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);
        
        //plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        

        


        plotSystem1.createPlotPart(this, "ExamplePlot1", actionBarComposite, PlotType.IMAGE, null);
        plotSystem1.getPlotComposite().setLayoutData(gd_secondField);
        //plotSystem1.createPlot2D(image1, null, null);
        


		}
   
   
   public Composite getComposite(){   	
   	return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem1;
   }

   public IDataset getImage(){
	   return image1;
   }
   
}


