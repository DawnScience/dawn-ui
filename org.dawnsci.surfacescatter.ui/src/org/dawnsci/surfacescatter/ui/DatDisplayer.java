package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DatDisplayer extends Composite {
   
    private Combo comboDropDown0;
    private Button massRunner;
    private Button selectFiles;
    private List list;
    private Group datSelector;
    private SurfaceScatterPresenter ssp;
    private String datFolderPath;
    private Boolean selectAllFlag = true;
    private Table rodDisplayTable;
	private Combo optionsDropDown;
	private String[] options;
	private ArrayList<String> datList;
	private Table folderDisplayTable;
        
    public DatDisplayer (Composite parent, 
    					 int style,
    					 String[] filepaths,
    					 SurfaceScatterPresenter ssp,
    					 String datFolderPath){
    	
        super(parent, style);
        
        new Label(this, SWT.NONE).setText("Source Data");
        
        this.createContents(filepaths, datFolderPath); 
        this.datFolderPath= datFolderPath;
        this.ssp = ssp;

        
    }
    
    public void createContents(String[] filepaths, String datFolderPath) {
        
    	SashForm selectionSash = new SashForm(this, SWT.HORIZONTAL);
    	selectionSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
    	Composite left = new Composite(selectionSash, SWT.FILL);
    	left.setLayout(new GridLayout());
    	left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
    	Composite middle= new Composite(selectionSash,SWT.FILL);
    	middle.setLayout(new GridLayout());
    	middle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
    	Composite right = new Composite(selectionSash,SWT.FILL);
    	right.setLayout(new GridLayout());
    	right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    	
    	Composite farRight = new Composite(selectionSash, SWT.NONE | SWT.FILL);
    	farRight.setLayout(new GridLayout());
    	farRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));  	
    	
        Group datSelector = new Group(left, SWT.NULL | SWT.V_SCROLL | SWT.FILL | SWT.CENTER);
        GridLayout datSelectorLayout = new GridLayout(1,true);
        GridData datSelectorData = new GridData(GridData.GRAB_HORIZONTAL);
	    datSelectorData .minimumWidth = 200;
	    datSelectorData .minimumHeight = 1000;
	    datSelector.setLayout(datSelectorLayout);
	    datSelector.setLayoutData(datSelectorData);
	    datSelector.setText("Selected Dat Files");
//	    selectFiles = new Button(datSelector, SWT.PUSH);
//	    selectFiles.setText("Select Files");
//	    
	    File folder = new File(datFolderPath);
	    File[] listOfFiles = folder.listFiles();
	    datList = new ArrayList<>();

	    CharSequence dat = ".dat";
	    
	    for (int i = 0; i < listOfFiles.length; i++) {
	       if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(dat)){
	    	   datList.add(listOfFiles[i].getName());
	       }
	    }
	  
	    folderDisplayTable = new Table(datSelector, SWT.CHECK |  SWT.V_SCROLL | SWT.H_SCROLL | SWT.FILL);
	    GridData folderDisplayTableData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
	    folderDisplayTableData.minimumWidth = 200;
        folderDisplayTableData.minimumHeight = 750;
	    
	    folderDisplayTable.setLayoutData(folderDisplayTableData);
	    folderDisplayTable.setLayout(new GridLayout(1,true));
	    folderDisplayTable.getVerticalBar().setEnabled(true);
	    
	    Button placeHolder = new Button(datSelector, SWT.PUSH);
	    placeHolder.setText("populate");
	    
	    java.util.Collections.sort(datList);
	    
//	    for(int j = 0; j<datList.size(); j++){
//	    	TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
//	    	t.setText(datList.get(j));
//	    }
//	    
	    
//	    folderDisplayTable.getColumn(0).pack();
	
	    
	    folderDisplayTable.getVerticalBar().setEnabled(true);
	    folderDisplayTable.getVerticalBar().setIncrement(1);
	    folderDisplayTable.getVerticalBar().setThumb(1);
	    
	    
	    Button transferToRod = new Button(middle, SWT.PUSH);
	    transferToRod.setText("Transfer to Rod \r ->");
	    transferToRod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));	    
	    
	    Group rodComponents = new Group(right, SWT.NULL | SWT.V_SCROLL | SWT.FILL | SWT.CENTER);
        GridLayout rodComponentsLayout = new GridLayout(1,true);
        GridData rodComponentsData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        rodComponentsData.minimumWidth = 200;
        rodComponentsData.minimumHeight = 10000;
        rodComponents.setLayout(rodComponentsLayout);
        rodComponents.setLayoutData(rodComponentsData);
        rodComponents.setText("Rod Components");
	    
	    Table rodDisplayTable = new Table(rodComponents, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    GridData rodDisplayData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        rodDisplayData.minimumWidth = 200;
        rodDisplayData.minimumHeight = 750;
	    rodDisplayTable.setLayoutData(rodDisplayData);
	    
	    Button selectAll = new Button(rodComponents, SWT.PUSH);
	    selectAll.setText("Select All - not working well");
	    selectAll.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	    
	    Button deleteSelected = new Button(rodComponents, SWT.PUSH);
	    deleteSelected.setText("Delete Selected");
	    deleteSelected.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	    
	    Button buildRod = new Button(rodComponents, SWT.PUSH);
	    buildRod.setText("Build Rod From Selected");
	    buildRod.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	    
	    Group scannedVariableOptions = new Group(farRight, SWT.NULL);
        GridLayout scannedVariableOptionsLayout = new GridLayout(1,true);
        GridData scannedVariableOptionsData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        scannedVariableOptionsData .minimumWidth = 50;
        scannedVariableOptionsData .minimumHeight = 2000;
        scannedVariableOptions.setLayout(scannedVariableOptionsLayout);
        scannedVariableOptions.setLayoutData(scannedVariableOptionsData);
        scannedVariableOptions.setText("Scanned Variables");
	    
	    optionsDropDown = new Combo(scannedVariableOptions, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
	    optionsDropDown .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
		optionsDropDown.select(0);
	    
	    
	    transferToRod.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ArrayList<TableItem> checkedList = new ArrayList<>();
				
				for(TableItem d : folderDisplayTable.getItems()){
					if(d.getChecked()){
						checkedList.add(d);
					}
				}
				
				TableItem[] chosenDats = new TableItem[checkedList.size()];
				
				for(int g = 0; g<checkedList.size(); g++){
					chosenDats[g] = checkedList.get(g);
				}
				
				ArrayList<String> itemsToRemove = new ArrayList<>();
				ArrayList<TableItem> tidiedTransferList = new ArrayList<>();
				
				for(TableItem cd : chosenDats){
					for(int ti =0; ti<rodDisplayTable.getItemCount(); ti++){
						if(cd.getText().equals(rodDisplayTable.getItem(ti))){
							itemsToRemove.add(cd.getText());
						}
					}
				}
				
				if (itemsToRemove.size() != 0){
					for(String ti : itemsToRemove){
						for(TableItem it1 : chosenDats){
							if(it1.getText().equals(ti) == false){
								tidiedTransferList.add(it1);
							}
						}
					}
				}
				
				else{
					for(TableItem it1 : chosenDats){
							tidiedTransferList.add(it1);
						}
				}
			
				for(int k = 0; k<tidiedTransferList.size(); k++){
					TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
			    	t.setText(tidiedTransferList.get(k).getText());
			    }
				
				IDataHolder dh1 = null;
				
				try {
					dh1 = LoaderFactory.getData(filepaths[0]);
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				options = dh1.getNames();
				
				for (int t=0; t<options.length; t++){
					optionsDropDown.add(options[t]);
				}
				
				optionsDropDown.select(0);
				
				
				folderDisplayTable.getVerticalBar().setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    deleteSelected.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				TableItem[] itemsToRemove = rodDisplayTable.getSelection();
				ArrayList<String> namesToBeKept= new ArrayList<>();
				
				for(TableItem ra : rodDisplayTable.getItems()){
					for(TableItem rb : itemsToRemove){
						if(ra.getText().equals(rb.getText()) == false){
							namesToBeKept.add(ra.getText());
						}
					}
				}
				
				rodDisplayTable.removeAll();
				
				for(String sr : namesToBeKept){
					TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
			    	t.setText(sr);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(selectAllFlag){
					rodDisplayTable.selectAll();
					
					selectAllFlag = false;
					selectAll.setText("Deselect All - not working well");
					
				}
				else{
					rodDisplayTable.deselectAll();;
					selectAllFlag = true;
					selectAll.setText("Select All - not working well");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    buildRod.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ArrayList<TableItem> checkedList = new ArrayList<>();
				
				for(TableItem d : rodDisplayTable.getItems()){
					if(d.getChecked()){
						checkedList.add(d);
					}
				}
				
				
				TableItem[] rodComponentDats = new TableItem[checkedList.size()];
				
				for(int g = 0; g<checkedList.size(); g++){
					rodComponentDats[g] = checkedList.get(g);
				}
				
				
				
				String[] filepaths = new String[rodComponentDats.length];
				
				for(int f = 0 ; f<rodComponentDats.length; f++){
					String filename = rodComponentDats[f].getText();
					filepaths[f] = datFolderPath + File.separator + filename;
				}
				
				ssp.surfaceScatterPresenterBuild(ssp.getParentShell(), 
												 filepaths, 
												 options[optionsDropDown.getSelectionIndex()], 
												 ssp.getImageFolderPath(), 
												 datFolderPath, 
												 ssp.getCorrectionSelection());
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    
	    
//	    Button selectAll= new Button(parent, SWT.PUSH);
//	    selectAll.setFont(FontUtils.getMsSansSerifFont());
//	    selectAll.setLayoutData(gridData);
//	    selectAll.addSelectionListener(new SelectionAdapter() {
//	        public void widgetSelected(SelectionEvent e) {
//	            table.selectAll();
//	        }
//	    });
	    
	    
	    
//	    list = new List(datSelector, SWT.V_SCROLL);
//	    
//	    for (String i :  datList) {
//	    	list.add(new Button(datSelector, SWT.CHECK));
//	    	list.add(StringUtils.substringAfterLast(i, File.separator));
//	    }
	    
	    // Scroll to the bottom
//	    list.select(list.getItemCount() - 1);
//	    list.showSelection();
	    

//	    ScrollBar sb = list.getVerticalBar();
//
//	    // Add one more item that shows the selection value
//	    //comboDropDown0 = new Combo(datSelector, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
//	   	
//	    massRunner = new Button(datSelector, SWT.NULL);
//	    massRunner.setText("Run all");
//	    
//	    for(String t: sm.getFilepaths()){
//	    	comboDropDown0.add(StringUtils.substringAfterLast(t, "/"));
//	    	
//	    }
	    
	    
//	    list.addSelectionListener(new SelectionListener() {
//	    	@Override
//	    	public void widgetSelected(SelectionEvent e) {
//	          int selection = list.getSelectionIndex();
//	          ssp.setSelection(selection);
////	          sm.setSelection(selection);
////	          System.out.println("!!!!!!!!!!!!!!selection : " + selection +"  !!!!!!!!!!!!!!!!!!!!!!!!!!1");
//	        }
//	    	@Override
//	        public void widgetDefaultSelected(SelectionEvent e) {
//	          
//	        }
//      });
    
	 
	    
	   placeHolder.addSelectionListener(new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			fillTable();
			
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}); 
	    
    }
    
   public Composite getComposite(){   	
	   return this;
   }
   
   public List getList() {
		return list;
	}
   
   
   public void setList(String[] in ) {
	   list.removeAll();
	   for (String i : in) {
		      list.add(StringUtils.substringAfterLast(i, File.separator));
		    }
	   
	   list.setSize(list.computeSize( SWT.DEFAULT, SWT.DEFAULT));
	   datSelector.setSize(datSelector.computeSize( SWT.DEFAULT,  SWT.DEFAULT));
	   this.redraw();
	}
  
   
   
   public Button getMassRunner(){
	   return massRunner;
   }
   
   public Button getSelectFiles(){
	   return selectFiles;
   }
   
   public void fillTable(){
	   for(int j = 0; j<datList.size(); j++){
		   	TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
		   	t.setText(datList.get(j));
	   }
	   
	   folderDisplayTable.getVerticalBar().setEnabled(true);
   }
}
   
