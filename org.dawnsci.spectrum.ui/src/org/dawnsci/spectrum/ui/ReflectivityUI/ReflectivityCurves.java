package org.dawnsci.spectrum.ui.ReflectivityUI;
import java.util.ArrayList;
import java.util.Iterator;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReflectivityCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private IRegion[] regionArray;
    
     
    public ReflectivityCurves(Composite parent, int style
    		, ArrayList<ILazyDataset> arrayILDy, ArrayList<ILazyDataset> arrayILDx, String[] filepaths,
    		String title, ReflectivityUIModel model) {
        super(parent, style);
        
        new Label(this, SWT.NONE).setText(title);
        
        
        regionArray = new IRegion[arrayILDy.size()-1];
        
        try {
			plotSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        this.createContents(arrayILDy, arrayILDx, filepaths, model); 
//        System.out.println("Test line");
        
    }
     
    public void createContents(ArrayList<ILazyDataset> arrayILDy, ArrayList<ILazyDataset> arrayILDx, String[] filepaths
    		, ReflectivityUIModel model) {
    	
    	final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        setLayout(gridLayout);
        
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(this, null);;
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(this, "ExamplePlot", actionBarComposite, PlotType.IMAGE, null);
        
        SliceND slice = new SliceND(arrayILDy.get(0).getShape());
        
		IDataset i = null;
		IDataset j = null;
//		String Name = "name";
		
		
//		Iterator<ILazyDataset> itr =arrayILDy.iterator();
		int r=0;
		
		for (r =0; r < filepaths.length; r++){
			
			ArrayList<IDataset> arrayIDy =new ArrayList<>();
			ArrayList<IDataset> arrayIDx =new ArrayList<>();
			
			slice = new SliceND(arrayILDy.get(r).getShape());
			
			try {
				i = arrayILDy.get(r).getSlice(slice);
				arrayIDy.add(i);
				j = arrayILDx.get(r).getSlice(slice);
				arrayIDx.add(j);
				//}
			} 
			
			catch (DatasetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			ILineTrace lt = plotSystem.createLineTrace(filepaths[r]);
			lt.setData(j, i);
			
			plotSystem.addTrace(lt);
			plotSystem.repaint();
			
		}


        plotSystem.getPlotComposite().setLayoutData(gd_secondField);

        ArrayList<IROI> roiList = new ArrayList<IROI>();
//        ArrayList<IRegion> regionList = new ArrayList<IRegion>();
        
        String root = "RegionNo:";
        int k=0;
        
        double[][] overlap = OverlapFinder.overlapFinderOperation(arrayILDx);
        
        for (k=0;k<(filepaths.length-1);k++){
        	
        	roiList.add(new RectangularROI(overlap[k][1],0.1,overlap[k][0]-overlap[k][1],0.1,0));
        	
        	String regionName = root +  Integer.toString(k);
        	
	        try {
				regionArray[k] =plotSystem.createRegion(regionName, RegionType.XAXIS);
			
	        
	        }
	        catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        
			plotSystem.addRegion(regionArray[k]);
			
			//RectangularROI startROI = new RectangularROI(10,10,100,100,0);
			regionArray[k].setROI(roiList.get(k));
	 
	        model.setROIListElement(roiList.get(k), k);
	        
	        int ktemp =k;
	        regionArray[k].addROIListener(new IROIListener() {
	
				@Override
				public void roiDragged(ROIEvent evt) {
//					model.setROIListElementEst(regionArray[ktemp].getROI(), ktemp);
//					//model.setROI(region.getROI());
//					System.out.println("roiDragged, ktemp:  " + ktemp);
				}
	
				@Override
				public void roiChanged(ROIEvent evt) {
					// TODO Auto-generated method stub
					model.setROIListElementEst(regionArray[ktemp].getROI(), ktemp);
					//model.setROI(roiList.get(ktemp));
					System.out.println("roiChanged, ktemp:  " + ktemp);
				}
	
				@Override
				public void roiSelected(ROIEvent evt) {
					model.setROIListElementEst(regionArray[ktemp].getROI(), ktemp);
					//model.setROI(roiList.get(ktemp));
					System.out.println("roiSelected, ktemp:  " + ktemp);
				}
			
			});
        }
    }
		
    public Composite getComposite(){
   	
   	return this;
   }
   
   public IPlottingSystem<Composite> getPlotSystem(){
	   return plotSystem;
   }
   
}

