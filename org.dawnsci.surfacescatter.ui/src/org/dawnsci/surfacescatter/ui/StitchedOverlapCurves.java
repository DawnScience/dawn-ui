package org.dawnsci.surfacescatter.ui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.OverlapAttenuationObject;
import org.dawnsci.surfacescatter.OverlapDataModel;
import org.dawnsci.surfacescatter.OverlapDisplayObject;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class StitchedOverlapCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private ILineTrace lt1;
    private SurfaceScatterPresenter ssp;
    private CurveStitchDataPackage csdp;
    private double[][] maxMinArray;
//    private ArrayList<IDataset> xArrayList;
    
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
    	
    	
    	csdp = ssp.curveStitchingOutput(null, false, null);
    	
    	Composite setupComposite = new Composite(this, SWT.FILL);
		setupComposite.setLayout(new GridLayout());
		setupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		SashForm form = new SashForm(setupComposite, SWT.FILL);
		form.setLayout(new GridLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		form.setOrientation(SWT.HORIZONTAL);
		
///////////////////////////left		
		SashForm leftForm = new SashForm(form, SWT.VERTICAL);
		leftForm.setLayout(new GridLayout());
		leftForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
		Group overlapSelector = new Group(leftForm, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		GridLayout overlapSelectorLayout = new GridLayout(1, true);
		GridData overlapSelectorData = new GridData((GridData.FILL_BOTH));
		overlapSelector.setLayout(overlapSelectorLayout);
		overlapSelector.setLayoutData(overlapSelectorData);
		overlapSelector.setText("Attenuation Factors");
    	
		Table overlapDisplayTable = new Table(overlapSelector, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		overlapDisplayTable.setEnabled(true);
		
		 for (int i = 0; i < 2; i++) {
		      TableColumn column = new TableColumn(overlapDisplayTable, SWT.NONE);
		      column.setWidth(100);
		 }
		
		GridData overlapDisplayTableData = new GridData(GridData.FILL_BOTH);

		overlapDisplayTable.setLayoutData(overlapDisplayTableData);
		overlapDisplayTable.setLayout(new GridLayout());
		overlapDisplayTable.getVerticalBar().setEnabled(true);

		overlapDisplayTable.getVerticalBar().setEnabled(true);
		overlapDisplayTable.getVerticalBar().setIncrement(1);
		overlapDisplayTable.getVerticalBar().setThumb(1);
		
		ArrayList<OverlapDataModel> odms = csdp.getOverlapDataModels();
		
		for(int i = 0; i<odms.size(); i++){
			 
			OverlapDataModel odm = odms.get(i);
			
			OverlapDisplayObject odo = new OverlapDisplayObject();
			
			odo.generateFromOdmAndTable(odm, 
										i, 
										overlapDisplayTable);
			
			odo.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					
					OverlapDisplayObject odo1 = (OverlapDisplayObject) evt.getSource();
					updateAttenuationFactors(odo1, xArrayList);
					
				}
			});
		}
		
		SashForm rightForm = new SashForm(form, SWT.VERTICAL);
		rightForm.setLayout(new GridLayout());
		rightForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group stitchedCurves = new Group(rightForm, SWT.FILL | SWT.FILL);
        GridLayout stitchedCurvesLayout = new GridLayout(1, true);
	    GridData stitchedCurvesData = new GridData(GridData.FILL_HORIZONTAL);
	    stitchedCurves.setLayout(stitchedCurvesLayout);
	    stitchedCurves.setLayoutData(stitchedCurvesData);
	    stitchedCurves.setText("Stitched Curves");
		
        ActionBarWrapper actionBarComposite = ActionBarWrapper.createActionBars(stitchedCurves, null);;
        
        final GridData gd_secondField = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_secondField.grabExcessVerticalSpace = true;
        gd_secondField.grabExcessVerticalSpace = true;
        
        plotSystem.createPlotPart(stitchedCurves, "Stitched Curves", actionBarComposite, PlotType.IMAGE, null);
    
		lt1 = plotSystem.createLineTrace("Concatenated Curve Test");
						
		lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveY());
		
		plotSystem.addTrace(lt1);
		plotSystem.repaint();
		
		maxMinArray = AttenuationCorrectedOutput.maxMinArrayGenerator(xArrayList,
				 model);


		model.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				maxMinArray = AttenuationCorrectedOutput.maxMinArrayGenerator(xArrayList,
																				model);
				
				csdp = ssp.curveStitchingOutput(maxMinArray, false, null);
										
				lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveY());
				
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
   
   public ILineTrace getLineTrace1(){
	   return lt1;
   }

   public CurveStitchDataPackage getCsdp() {
	   return csdp;
   }
	
   public void setCsdp(CurveStitchDataPackage csdp) {
	   this.csdp = csdp;
   }
   
   private void updateAttenuationFactors(OverlapDisplayObject odo,
		   								 ArrayList<IDataset> xArrayList){
	   
	   
	   OverlapAttenuationObject oAo = odo.getOAo();
	   
		csdp = ssp.curveStitchingOutput(maxMinArray, false, oAo);
		
		lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveY());
		
		plotSystem.clearTraces();
		plotSystem.addTrace(lt1);
		plotSystem.repaint();
   }
   
}