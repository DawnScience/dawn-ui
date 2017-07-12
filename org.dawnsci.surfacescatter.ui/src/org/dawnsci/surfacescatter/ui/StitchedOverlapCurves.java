package org.dawnsci.surfacescatter.ui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.surfacescatter.CurveStitchDataPackage;
import org.dawnsci.surfacescatter.OverlapAttenuationObject;
import org.dawnsci.surfacescatter.OverlapDataModel;
import org.dawnsci.surfacescatter.OverlapDisplayObjects;
import org.dawnsci.surfacescatter.OverlapUIModel;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class StitchedOverlapCurves extends Composite {

    private IPlottingSystem<Composite> plotSystem;
    private ILineTrace lt1;
    private SurfaceScatterPresenter ssp;
    private CurveStitchDataPackage csdp;
    private double[][] maxMinArray;
    private ArrayList<OverlapAttenuationObject> oAos;
    private ArrayList<OverlapDataModel> odms;
    private ArrayList<OverlapDisplayObjects> odos;
    private boolean modify = true;
    private Button go;
    private Button resetAll;
    private Table overlapDisplayTable;
    private TableViewer viewer;
    private int selector;
    private OverlapDisplayObjects odo;
    
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
		
		form.setOrientation(SWT.VERTICAL);
		
///////////////////////////TOP		
		
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
				
				plotSystem.clear();
				
				getTheRightCurve();
				
				plotSystem.clearTraces();
				plotSystem.addTrace(lt1);
				plotSystem.repaint();
				
				updateTable(odos,
							xArrayList, 
							viewer);
				
			}
		});
		

        plotSystem.getPlotComposite().setLayoutData(gd_secondField);
        
/////////////BOTTOM
       
        SashForm leftForm = new SashForm(form, SWT.VERTICAL);
		leftForm.setLayout(new GridLayout());
		leftForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group overlapSelector = new Group(leftForm, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		GridLayout overlapSelectorLayout = new GridLayout(1, true);
		GridData overlapSelectorData = new GridData(GridData.FILL_BOTH);
		overlapSelector.setLayout(overlapSelectorLayout);
		overlapSelector.setLayoutData(overlapSelectorData);
		overlapSelector.setText("Attenuation Factors");
    
		odms = csdp.getOverlapDataModels();
		
		odos = new ArrayList<OverlapDisplayObjects>();
		
		oAos = new ArrayList<OverlapAttenuationObject>();
		
		viewer = buildTable1(overlapSelector,
				   			 xArrayList);
		
		viewer.getTable().setLayout(overlapSelectorLayout);
		viewer.getTable().setLayoutData(overlapSelectorData);
		
		resetAll = new Button(leftForm, SWT.PUSH);
		 
		resetAll.setText("Reset All");
		
	  
		resetAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(OverlapDisplayObjects odo: odos){
					odo.setModified(false);
				}
				for(OverlapAttenuationObject oAo: oAos){
					oAo.setModified(false);
				}
				
				resetAttenuationFactors(overlapSelector, xArrayList);
				
//				updateTable(odos, xArrayList, viewer);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		go = new Button(leftForm, SWT.PUSH);
		 
	    go.setText("Go");
		
	  
	    go.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateAttenuationFactors(xArrayList);
				
//				updateTable(odos, xArrayList, viewer);
				
//				viewer.setInput(odos);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
		
			}
		});
	    
	    leftForm.setWeights(new int[]{70,15,15});
	    
	    form.setWeights(new int[]{70,30});
	  
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
   
   private void updateAttenuationFactors(//ArrayList<OverlapDisplayObjects> odos,
		   								 ArrayList<IDataset> xArrayList){
	  
	   for(int i =0; i<odos.size();i++ ){
		   
		   String test =odos.get(i).getTextCorrected().getText();
		   
		   
		   if(!odos.get(i).getTextCorrected().getText().equals(String.valueOf(oAos.get(i).getAttenuationFactorCorrected()))){
				modify = false;
				oAos.get(i).setAttenuationFactorCorrected(Double.valueOf(odos.get(i).getTextCorrected().getText()));
				
				
//				odos.get(i).getTextCorrected().setText(String.valueOf(oAos.get(i).getAttenuationFactorCorrected()));
				modify = true;
			}
			if(!odos.get(i).getTextRaw().getText().equals(String.valueOf(oAos.get(i).getAttenuationFactorRaw()))){
				modify = false;
				oAos.get(i).setAttenuationFactorRaw(Double.valueOf(odos.get(i).getTextRaw().getText()));
				
//				odos.get(i).getTextRaw().setText(String.valueOf(oAos.get(i).getAttenuationFactorRaw()));
				modify = true;
			}
			if(!odos.get(i).getTextFhkl().getText().equals(String.valueOf(oAos.get(i).getAttenuationFactorFhkl()))){
				modify = false;
				oAos.get(i).setAttenuationFactorFhkl(Double.valueOf(odos.get(i).getTextFhkl().getText()));
				
//				odos.get(i).getTextFhkl().setText(String.valueOf(oAos.get(i).getAttenuationFactorFhkl()));
				modify = true;
			}
	   }
	   
	   for(OverlapDisplayObjects odo :odos){
		   if(odo.isModified()){
			   oAos.set(odo.getOdoNumber(), odo.getOAo());
		   }
	   }
	   
	   csdp = ssp.curveStitchingOutput(maxMinArray, true, oAos);
		
	   getTheRightCurve();
		
	   plotSystem.clearTraces();
	   plotSystem.addTrace(lt1);
	   plotSystem.repaint();
   }
   
   
   private void resetAttenuationFactors(Group group,
//		   								ArrayList<OverlapDisplayObjects> odos,
		   								ArrayList<IDataset> xArrayList){

	   	csdp = ssp.curveStitchingOutput(maxMinArray, true, null);

		
		getTheRightCurve();
		
		plotSystem.clearTraces();
		plotSystem.addTrace(lt1);
		plotSystem.repaint();
	   
		for(int i =0; i<odos.size(); i++){
			
			OverlapDisplayObjects odo = odos.get(i);
			OverlapDataModel odm = odms.get(i);
			
			double l = odm.getAttenuationFactor(); 
			odo.getTextCorrected().setText(String.valueOf(l));
			odo.setTextCorrectedContent(l);
			
			double m = odm.getAttenuationFactorRaw(); 
			odo.getTextRaw().setText(String.valueOf(m));
			odo.setTextRawContent(m);
			
			double n = odm.getAttenuationFactorFhkl(); 
			odo.getTextFhkl().setText(String.valueOf(n));
			odo.setTextFhklContent(n);
			
			odo.setModified(false);
			
		}
		
		oAos = new ArrayList<>();
		
		for(int i =0; i<odos.size(); i++){
			OverlapAttenuationObject oAo = odo.getOAo();
			oAo.setModified(false);
			oAos.add(oAo);
		}
		

	}
	   
   
   private void updateTable(ArrayList<OverlapDisplayObjects> odos,
		   					ArrayList<IDataset> xArrayList, 
		   					TableViewer table){

//	   csdp = ssp.getDrm().getCsdp();
//	   odms = csdp.getOverlapDataModels();
////	   
////	   table.removeAll();
////	  
////	   for(int cv = 0; cv<table.getItems().length; cv++){
////			table.remove(cv);
////	   }
////	  
////	   table.removeAll();
////		  
////	   buildTable(table, xArrayList);
////	   
////	   table.redraw();
//	   
//	   table.refresh();
//	   
////	   if(!odos.get(0).getTextCorrected().getText().equals(String.valueOf(oAos.get(0).getAttenuationFactorCorrected()))){
////		   System.out.println("why not");
////
////	   }
	}
   

   
   private void generateOdosFromOdms(ArrayList<IDataset> xArrayList){
	   
	   ArrayList<OverlapAttenuationObject> oAos1= new ArrayList<>();
//	   odos = new ArrayList<>();
	   
	   for(int i = 0; i<odms.size(); i++){
			
			OverlapDataModel odm = odms.get(i);
			
			OverlapDisplayObjects odo = new OverlapDisplayObjects();
			
			odo.generateFromOdmAndTable(odm, 
									i, 
									overlapDisplayTable);
			
			odos.add(odo);
			
			oAos1.add(odo.getOAo());
			try{
				if(oAos.size()>=(i-1) && oAos.size()>0 ){
					if(oAos.get(i) != null){
						oAos1.get(i).setModified(oAos.get(i).isModified());
					}
				}
			}
			catch(Exception h){
				
			}
						
			odo.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(modify){
						OverlapDisplayObjects odo = (OverlapDisplayObjects) evt.getSource();
						odo.setModified(true);
				
						
						if(odo.isButtonPushed()){
							odo.setButtonPushed(false);
							odo.setModified(false);
							try{
								oAos.get(odo.getOdoNumber()).setModified(false);
							}
							catch(Exception m){
								
							}
						}
						
						updateAttenuationFactors(//odos, 
												 xArrayList);
						
						updateTable(odos, xArrayList, viewer);
					}
				}
			});
	   }
		
	   oAos = oAos1;
   }
   
   
   private TableViewer buildTable1(Group overlapSelector,
			   					   ArrayList<IDataset> xArrayList){
	
		ArrayList<OverlapAttenuationObject> oAos1= new ArrayList<>();
		
		viewer = new TableViewer(overlapSelector, SWT.MULTI | SWT.H_SCROLL
	            | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		for(int i = 0; i<odms.size(); i++){
			
			OverlapDataModel odm = odms.get(i);
			
			OverlapDisplayObjects odo = new OverlapDisplayObjects();
			
			odo.generateFromOdmAndTable(odm, 
									i, 
									overlapDisplayTable);
			
			odos.add(odo);
			
			oAos1.add(odo.getOAo());
			try{
				if(oAos.size()>=(i-1) && oAos.size()>0 ){
					if(oAos.get(i) != null){
						oAos1.get(i).setModified(oAos.get(i).isModified());
					}
				}
			}
			catch(Exception h){
				
			}
						
			odo.addPropertyChangeListener(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if(modify){
						OverlapDisplayObjects odo = (OverlapDisplayObjects) evt.getSource();
						odo.setModified(true);
				
						
						if(odo.isButtonPushed()){
							odo.setButtonPushed(false);
							odo.setModified(false);
							try{
								oAos.get(odo.getOdoNumber()).setModified(false);
							}
							catch(Exception m){
								
							}
						}
						
						updateAttenuationFactors(//odos, 
												 xArrayList);
						
						updateTable(odos, xArrayList, viewer);
					}
				}
			});
			
			oAos = oAos1;
		}
		
		// create the columns
		createColumns(overlapSelector, viewer);
		
		// make lines and header visible
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		 viewer.setContentProvider(new ArrayContentProvider());
	     
		 // get the content for the viewer, setInput will call getElements in the
	     viewer.setInput(odos);
		
		return viewer;
   }
   
   private void createColumns(final Composite parent, 
		   					  final TableViewer viewer) {
       
	   String[] titles = { "Overlap: ", 
			    		   "Corrected Attenuation: ", 
			    		   "Raw Attenuation: ", 
			    		   "Fhkl Attenuation: ", 
			    		   "Local Reset: " };
       
       int[] bounds = { 100, 100, 100, 100, 100 };

       // first column is for the overlap name
       TableViewerColumn col = createTableViewerColumn(viewer, 
    		   										   titles[0], 
    		   										   bounds[0], 
    		   										   0);
       
       
       col.setLabelProvider(new ColumnLabelProvider() {
           @Override
           public String getText(Object element) {
        	   odo = (OverlapDisplayObjects) element;
               return odo.getLabel();
           }
       });

       // second column is for the corrected data attenuation factor
       col = createTableViewerColumn(viewer,
    		   					     titles[1], 
    		   						 bounds[1], 
    		   						 1);
       
       col.setLabelProvider(new ColumnLabelProvider() {
    	   
    	   @Override
           public void update(ViewerCell cell) {
        	   
    		   TableItem item = (TableItem) cell.getItem();
        	   TableEditor editor = new TableEditor(item.getParent());
        	   Text attenuationCorrected = new Text(item.getParent(), SWT.BORDER);
        	   
        	   odo.buildTextCorrected(attenuationCorrected);
        	   
        	   editor.grabHorizontal  = true;
               editor.grabVertical = true;
               editor.setEditor(odo.getTextCorrected(), item, cell.getColumnIndex());
               editor.layout();
        
           }
       });
       
       // third column is for the raw data attenuation factor
       col = createTableViewerColumn(viewer,
    		   						 titles[2], 
    		   						 bounds[2], 
    		   						 2);
       
       col.setLabelProvider(new ColumnLabelProvider() {
    	   
    	   @Override
           public void update(ViewerCell cell) {
        	   
    		   TableItem item = (TableItem) cell.getItem();
        	   TableEditor editor = new TableEditor(item.getParent());
        	   Text attenuationRaw = new Text(item.getParent(), SWT.BORDER);
        	   
        	   odo.buildTextRaw(attenuationRaw);
        	   
        	   editor.grabHorizontal  = true;
               editor.grabVertical = true;
               editor.setEditor(odo.getTextRaw(), item, cell.getColumnIndex());
               editor.layout();
        
           }
       });

       // fourth column is for the Fhkl data attenuation factor
       col = createTableViewerColumn(viewer,
    		   						 titles[3], 
    		   						 bounds[3], 
    		   						 3);
       col.setLabelProvider(new ColumnLabelProvider() {
    	   
    	   @Override
           public void update(ViewerCell cell) {
        	   
    		   TableItem item = (TableItem) cell.getItem();
        	   TableEditor editor = new TableEditor(item.getParent());
        	   Text attenuationFhkl = new Text(item.getParent(), SWT.BORDER);
        	   
        	   odo.buildTextFhkl(attenuationFhkl);
        	   
        	   editor.grabHorizontal  = true;
               editor.grabVertical = true;
               editor.setEditor(odo.getTextFhkl(), item, cell.getColumnIndex());
               editor.layout();
        
           }
       });

       // fifth column is for the reset button
       col = createTableViewerColumn(viewer,
    		   						 titles[4], 
    		   						 bounds[4], 
    		   						 4);
       col.setLabelProvider(new ColumnLabelProvider() {
    	   
    	   @Override
           public void update(ViewerCell cell) {
        	   
    		   TableItem item = (TableItem) cell.getItem();
        	   TableEditor editor = new TableEditor(item.getParent());
        	   Button resetOverlap = new Button(item.getParent(), SWT.PUSH);
        	   resetOverlap.setText("Reset Overlap");
        	   odo.addResetListener(resetOverlap);
        	   
        	   editor.grabHorizontal  = true;
               editor.grabVertical = true;
               editor.setEditor(odo.getResetOverlap(), item, cell.getColumnIndex());
               editor.layout();
        
           }
       });
 
   }
   
   private TableViewerColumn createTableViewerColumn(TableViewer viewer, 
		   											 String title, 
		   											 int bound, 
		   											 final int colNumber) {
      
	   final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
	       final TableColumn column = viewerColumn.getColumn();
	       column.setText(title);
	       column.setWidth(bound);
	       column.setResizable(true);
	       column.setMoveable(true);
	       return viewerColumn;
   }
	
	
   public void changeCurves(int selector){
		
	   this.selector =selector;
	   
		Display display = Display.getCurrent();
		
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		Color green = display.getSystemColor(SWT.COLOR_GREEN);
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		
		plotSystem.clear();
		
		switch(selector){
			case 0:
				ILineTrace lt = plotSystem.createLineTrace("Spliced Corrected Intensity Curve");
			
				lt.setData(csdp.getSplicedCurveX(),
						   csdp.getSplicedCurveY());
					
				lt.setTraceColor(blue);
				plotSystem.addTrace(lt);
			
				break;
			
			case 1:
				
				ILineTrace lt1 = plotSystem.createLineTrace("Spliced Fhkl Intensity Curve");
			
				lt1.setData(csdp.getSplicedCurveX(),
						   csdp.getSplicedCurveYFhkl());
				
				
				lt1.setTraceColor(green);
				plotSystem.addTrace(lt1);
				
				break;
			
			case 2:
			
				ILineTrace lt2 = plotSystem.createLineTrace("Spliced Raw Intensity Curve");
			
				lt2.setData(csdp.getSplicedCurveX(),
						   	csdp.getSplicedCurveYRaw());
				
				lt2.setTraceColor(black);
				plotSystem.addTrace(lt2);

				break;
			
			default:
				// Purely defensive
				break;
		}
   }
   
   private void getTheRightCurve(){
	  
	   switch(selector){
		case 0:
			lt1 = plotSystem.createLineTrace("Spliced Corrected Curve");
			lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveY());
			break;
		case 1:
			lt1 = plotSystem.createLineTrace("Spliced Fhkl Curve");
			lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveYFhkl());
			break;
		case 2:
			lt1 = plotSystem.createLineTrace("Spliced Raw Curve");
			lt1.setData(csdp.getSplicedCurveX(), csdp.getSplicedCurveYRaw());
			break;
		default:
			//purely defensive
	   }
	
   }
   
}