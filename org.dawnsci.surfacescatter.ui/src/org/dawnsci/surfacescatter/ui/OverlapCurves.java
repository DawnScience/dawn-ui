package org.dawnsci.surfacescatter.ui;
import java.util.ArrayList;
import java.util.Iterator;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class OverlapCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private IRegion[] regionArray;
    private int DEBUG =1;
    private Group controls;
    private Button errors;
    private ArrayList<ILineTrace> ltList;
    
     
    public OverlapCurves(Composite parent, 
    					int style, 
    					ArrayList<IDataset> arrayILDy, 
    					ArrayList<IDataset> arrayILDx, 
    					String title, 
    					OverlapUIModel model) {
    	
        super(parent, style);
        
        new Label(this, SWT.NONE).setText(title);
        
        
        regionArray = new IRegion[arrayILDy.size()-1];
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
        
        this.createContents(arrayILDy, arrayILDx,  model);       
    }
     
    
    public void createContents(ArrayList<IDataset> arrayILDy, 
    						   ArrayList<IDataset> arrayILDx, 
    						   OverlapUIModel model) {
    	
    	model.getROIList().clear();
    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        controls = new Group(this, SWT.NULL);
        GridLayout controlsSelectionLayout = new GridLayout();
		GridData controlsSelectionData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        controls.setLayoutData(controlsSelectionData);
        controls.setLayout(controlsSelectionLayout);

		errors = new Button(controls, SWT.PUSH);
		errors.setText("Errors");
		
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        
        SliceND slice = new SliceND(arrayILDy.get(0).getShape());
        
		IDataset i = null;
		IDataset j = null;

		
		ltList = new ArrayList<ILineTrace>();
		
		int r=0;
	
		for (r =0; r < arrayILDy.size(); r++){
			
			ArrayList<IDataset> arrayIDy =new ArrayList<>();
			ArrayList<IDataset> arrayIDx =new ArrayList<>();
			
			slice = new SliceND(arrayILDy.get(r).getShape());
			
			try {
				i = arrayILDy.get(r);
				arrayIDy.add(i);
				j = arrayILDx.get(r).getSlice(slice);
				arrayIDx.add(j);
				//}
			} 
			
			catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			ILineTrace lt = plotSystem.createLineTrace(Double.toString(arrayIDx.get(0).getDouble(0)) +"-" +Double.toString(arrayIDx.get(0).getDouble(arrayIDx.size()-1)));
			lt.setData(j, i);
			
			ltList.add(lt);
			
			plotSystem.addTrace(lt);
			plotSystem.repaint();
			
		}


       plotSystem.getPlotComposite().setLayoutData(gd_secondField);

       ArrayList<IRectangularROI> roiList = new ArrayList<IRectangularROI>();
       IRectangularROI nullROI = null;
        
        
        for(int yr =0; yr<regionArray.length;yr++){
        	roiList.add(nullROI);
        }

        model.setROIList(roiList);
        
        String root = "RegionNo:";
        int k=0;
        
        double[][] overlap = OverlapFinder.overlapFinderOperation(arrayILDx);
        
        for (k=0;k<(model.getROIList().size());k++){
        	
        	if(overlap[k][1]<999999){
        		
        		if(DEBUG ==1 ){
        			System.out.println("k in overlapCurves: " + k);
        			System.out.println("overlap[k][1]: " + overlap[k][1]);
        			System.out.println("overlap[k][0]: " + overlap[k][0]);
        			System.out.println("roiList.size() : " + roiList.size());
        		}
        		
        		
        		roiList.set(k,  new RectangularROI(overlap[k][1],0.1,overlap[k][0]-overlap[k][1],0.1,0));

	        	String regionName = root +  Integer.toString(k);
	        
		        try {
					regionArray[k] =plotSystem.createRegion(regionName, RegionType.XAXIS);
					regionArray[k].setROI(roiList.get(k));
					plotSystem.addRegion(regionArray[k]);
					
		        }
		        catch (Exception e1) {
					e1.printStackTrace();
				}
		       
		        model.setROIList(roiList);
		        
		        int ktemp =k;
		        ((IRegion) regionArray[k]).addROIListener(new IROIListener() {
		
					@Override
					public void roiDragged(ROIEvent evt) {
						model.setROIListElementEst(regionArray[ktemp].getROI().getBounds(), ktemp);
					}
		
					@Override
					public void roiChanged(ROIEvent evt) {
						model.setROIListElementEst(regionArray[ktemp].getROI().getBounds(), ktemp);
						
					}
		
					@Override
					public void roiSelected(ROIEvent evt) {
						model.setROIListElementEst(regionArray[ktemp].getROI().getBounds(), ktemp);
					}
				
				});
        	}
        }

        
    
        
    }
		
    public Composite getComposite(){
   	
   	return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem;
   }
   
	public Button getErrorsButton(){
		return errors;
	}
   
	public ArrayList<ILineTrace> getILineTraceList(){
		return ltList;
	}
}

