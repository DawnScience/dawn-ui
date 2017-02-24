package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DatDisplayer extends Composite {
   
    private Combo comboDropDown0;
    private Button massRunner;
    private Button selectFiles;
    private List list;
    private Group datSelector;
    private SurfaceScatterPresenter ssp;
    private Boolean selectAllFlag = true;
    private Table rodDisplayTable;
	private Combo optionsDropDown;
	private String[] options;
	private ArrayList<String> datList;
	private Table folderDisplayTable;
	private Button buildRod;
	private SashForm selectionSash;
	private Button populateTable;
	private Button imageFolderSelection;
	private Button datFolderSelection;
	private String imageFolderPath = null;
	private SurfaceScatterViewStart ssvs;
	private String datFolderPath = null;
	private Button transferToRod;
	private Group rodConstrucion;
	private Button deleteSelected;
	private Button clearTable;
	private Group scannedVariableOptions;
	private Group rodComponents;
	private Text datFolderText;
	private Text imageFolderText;
	
	public String getImageFolderPath() {
		return imageFolderPath;
	}

	public void setImageFolderPath(String imageFolderPath) {
		this.imageFolderPath = imageFolderPath;
	}
        
    public Button getpopulateTable() {
		return populateTable;
	}

	public void setpopulateTable(Button populateTable) {
		this.populateTable = populateTable;
	}

	public DatDisplayer (Composite parent, 
    					 int style,
    					 String[] filepaths,
    					 SurfaceScatterPresenter ssp,
    					 String datFolderPath,
    					 SurfaceScatterViewStart ssvs){
    	
        super(parent, style);
        
//        new Label(this, SWT.NONE).setText("Source Data");
        
        this.createContents(); 
//        this.datFolderPath= datFolderPath;
        this.ssp = ssp;
        this.ssvs = ssvs;

        
    }
    
    public void createContents() {
        
    	selectionSash = new SashForm(this, SWT.FILL);
    	selectionSash.setLayoutData(new GridData(GridData.FILL_BOTH));
    	Composite left = new Composite(selectionSash, SWT.FILL);
    	left.setLayout(new GridLayout());
    	left.setLayoutData(new GridData(GridData.FILL));
    	    	
    	Composite right = new Composite(selectionSash,SWT.FILL);
    	right.setLayout(new GridLayout());
    	right.setLayoutData(new GridData(GridData.FILL));

        Group datSelector = new Group(left, SWT.V_SCROLL | SWT.FILL );
        GridLayout datSelectorLayout = new GridLayout(1,true);
        GridData datSelectorData =new GridData((GridData.FILL_BOTH));
	    datSelectorData .minimumWidth = 200;
	    datSelectorData .minimumHeight = 850;
	    datSelector.setLayout(datSelectorLayout);
	    datSelector.setLayoutData(datSelectorData);
	    datSelector.setText("Selected Dat Files");
    
	    Group datFolders  =new Group(datSelector, SWT.NONE);
	    GridLayout datFoldersLayout = new GridLayout(2,true);
        GridData datFoldersData =new GridData((GridData.FILL_HORIZONTAL));
        datFolders.setLayout(datFoldersLayout);
        datFolders.setLayoutData(datFoldersData);
	    
	    
	    
	    datFolderPath =null;
	    
	    datFolderSelection = new Button(datFolders, SWT.PUSH | SWT.FILL);
		
		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		datFolderSelection.setText("Select .dat File Folder");
		
		datFolderSelection.addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				FileDialog fd = new FileDialog(ssvs.getShell(), SWT.OPEN); 
				
				String path = "p";
				
				if (fd.open() != null) {
					path = fd.getFilterPath();
				}
				
				datFolderPath = path;
				ssvs.setDatFolderPath(path);
				
				if(datFolderPath != null && imageFolderPath != null){
					populateTable.setEnabled(true);
					clearTable.setEnabled(true);
				}
				
				datFolderText.setText(datFolderPath);
				datFolderText.setEnabled(true);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			
			}
		});
	    
		datFolderText = new Text(datFolders,SWT.SINGLE | SWT.BORDER | SWT.FILL);
		datFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		datFolderText.setEnabled(false);
		datFolderText.setEditable(false);
		
		Group imageFolders  =new Group(datSelector, SWT.NONE);
		GridLayout imageFoldersLayout = new GridLayout(2,true);
	    GridData imageFoldersData =new GridData((GridData.FILL_HORIZONTAL));
	    imageFolders.setLayout(imageFoldersLayout);
	    imageFolders.setLayoutData(imageFoldersData);
		
		imageFolderSelection = new Button(imageFolders, SWT.PUSH | SWT.FILL);
		
		imageFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		imageFolderSelection.setText("Select Images Folder");
		
		imageFolderSelection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog fd = new FileDialog(ssvs.getShell(), SWT.OPEN); 
				
				String path = "p";
				
				if (fd.open() != null) {
					path = fd.getFilterPath();
				}
				
				imageFolderPath = path;
				
				if(datFolderPath != null && imageFolderPath != null){
					populateTable.setEnabled(true);
					
				}
				
				imageFolderText.setText(imageFolderPath);
				imageFolderText.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		imageFolderText = new Text(imageFolders,SWT.SINGLE | SWT.BORDER | SWT.FILL);
		imageFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		imageFolderText.setEnabled(false);
		imageFolderText.setEditable(false);
		
	    populateTable = new Button(datSelector, SWT.PUSH | SWT.FILL);
	    populateTable.setText("Populate Table");	  
	    populateTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    populateTable.setEnabled(false);
	    

	    clearTable = new Button(datSelector, SWT.PUSH | SWT.FILL);
	    clearTable.setText("Clear Table");	  
	    clearTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    clearTable.setEnabled(false);
	    
	    transferToRod = new Button(datSelector, SWT.PUSH);
	    transferToRod.setText("Transfer to Rod ->");
	    transferToRod.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
	    transferToRod.setEnabled(false);
	    
	    folderDisplayTable = new Table(datSelector, SWT.CHECK |  SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
	    folderDisplayTable.setEnabled(false);
	    
	    selectionSash.getParent().layout(true,true);
		selectionSash.redraw();
		selectionSash.update();
		
	    GridData folderDisplayTableData = new GridData(GridData.FILL_BOTH);
	    folderDisplayTableData.minimumWidth = 200;
        folderDisplayTableData.minimumHeight = 550;
	    
	    folderDisplayTable.setLayoutData(folderDisplayTableData);
	    folderDisplayTable.setLayout(new GridLayout());
	    folderDisplayTable.getVerticalBar().setEnabled(true);
	    
	    try{
	    	java.util.Collections.sort(datList);
	    }
	    catch(Exception g){	
	    }
	    
	    folderDisplayTable.getVerticalBar().setEnabled(true);
	    folderDisplayTable.getVerticalBar().setIncrement(1);
	    folderDisplayTable.getVerticalBar().setThumb(1);
	    
	    rodConstrucion = new Group(right, SWT.V_SCROLL | SWT.FILL | SWT.FILL);
        GridLayout rodConstrucionLayout = new GridLayout(1,true);
        GridData rodConstrucionData = new GridData(GridData.FILL_BOTH);
        rodConstrucionData.minimumWidth = 200;
        rodConstrucionData.minimumHeight = 600;
        rodConstrucion.setLayout(rodConstrucionLayout);
        rodConstrucion.setLayoutData(rodConstrucionData);
        rodConstrucion.setText("Rod Construcion");
        rodConstrucion.setEnabled(false);
        
        scannedVariableOptions = new Group(rodConstrucion, SWT.NULL);
        GridLayout scannedVariableOptionsLayout = new GridLayout(1,true);
        GridData scannedVariableOptionsData = new GridData(GridData.FILL_HORIZONTAL);
        scannedVariableOptionsData .minimumWidth = 50;
        scannedVariableOptionsData .minimumHeight = 500;
        scannedVariableOptions.setLayout(scannedVariableOptionsLayout);
        scannedVariableOptions.setLayoutData(scannedVariableOptionsData);
        scannedVariableOptions.setText("Scanned Variables");
        scannedVariableOptions.setEnabled(false);
        
	    optionsDropDown = new Combo(scannedVariableOptions, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
	    optionsDropDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    optionsDropDown.setEnabled(false);
	    
		optionsDropDown.select(0);
	    
	    rodComponents = new Group(rodConstrucion, SWT.NULL | SWT.V_SCROLL | SWT.FILL );
        GridLayout rodComponentsLayout = new GridLayout(1,true);
        GridData rodComponentsData = new GridData(GridData.FILL_BOTH);
        rodComponentsData.minimumWidth = 200;
        rodComponentsData.minimumHeight = 500;
        rodComponents.setLayout(rodComponentsLayout);
        rodComponents.setLayoutData(rodComponentsData);
        rodComponents.setText("Rod Components");
        rodComponents.setEnabled(false);
	    

	    deleteSelected = new Button(rodComponents, SWT.PUSH);
	    deleteSelected.setText("Delete Selected");
	    deleteSelected.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	    deleteSelected.setEnabled(false);
	    
	    buildRod = new Button(rodComponents, SWT.PUSH);
	    buildRod.setText("Build Rod From Selected");
	    buildRod.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	    buildRod.setEnabled(false);
	    
	    rodDisplayTable = new Table(rodComponents, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    GridData rodDisplayData = new GridData(GridData.FILL_BOTH);
        rodDisplayData.minimumWidth = 200;
        rodDisplayData.minimumHeight = 600;
	    rodDisplayTable.setLayoutData(rodDisplayData);
	    rodDisplayTable.setLayout(new GridLayout());
	    rodDisplayTable.setEnabled(false);
	    
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
					
					String filename = rodDisplayTable.getItem(0).getText();
					String filep = datFolderPath + File.separator + filename;
					dh1 = LoaderFactory.getData(filep );
					
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				optionsDropDown.removeAll();
				
				options = dh1.getNames();
				
				for (int t=0; t<options.length; t++){
					optionsDropDown.add(options[t]);
				}
				
				optionsDropDown.select(0);
				
				rodConstrucion.setEnabled(true);
				deleteSelected.setEnabled(true);
				buildRod.setEnabled(true);
				optionsDropDown.setEnabled(true);
				rodDisplayTable.setEnabled(true);
				rodComponents.setEnabled(true);
				scannedVariableOptions.setEnabled(true);
				folderDisplayTable.getVerticalBar().setEnabled(true);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    clearTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearTable();
				
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
				
				
				

				IDataHolder dh1 = null;
				
				try {
					
					String filename = rodDisplayTable.getItem(0).getText();
					String filep = datFolderPath + File.separator + filename;
					dh1 = LoaderFactory.getData(filep );
					
					optionsDropDown.removeAll();
					
					options = dh1.getNames();
					
					for (int t=0; t<options.length; t++){
						optionsDropDown.add(options[t]);
					}
					
					optionsDropDown.select(0);
					
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				
				if(rodDisplayTable.getItemCount() ==0){
					enableRodConstruction(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	    
//	    selectAll.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if(selectAllFlag){
//					rodDisplayTable.selectAll();
//					
//					selectAllFlag = false;
//					selectAll.setText("Deselect All - not working well");
//					
//				}
//				else{
//					rodDisplayTable.deselectAll();;
//					selectAllFlag = true;
//					selectAll.setText("Select All - not working well");
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});	 
	    
	   populateTable.addSelectionListener(new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			fillTable();
			folderDisplayTable.setEnabled(true);
			transferToRod.setEnabled(true);
			clearTable.setEnabled(true);
			ssp.setImageFolderPath(imageFolderPath);
		
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}); 
	   
	   
	   selectionSash.setWeights(new int[] {50,50});
    }
    
   public Composite getComposite(){   	
	   return this;
   }
   
   public List getList() {
		return list;
   }
   
   public Button getBuildRod(){
	   return buildRod;
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
  
   public void enableRodConstruction(boolean enabled){
	   rodConstrucion.setEnabled(enabled);
	   scannedVariableOptions.setEnabled(enabled);
	   rodComponents.setEnabled(enabled);
	   rodDisplayTable.setEnabled(enabled);
	   buildRod.setEnabled(enabled);
	   deleteSelected.setEnabled(enabled);
	   optionsDropDown.setEnabled(enabled);
   }
   
   public Button getMassRunner(){
	   return massRunner;
   }
   
   public Button getSelectFiles(){
	   return selectFiles;
   }
   
   public void fillTable(){
	   
	   	File folder = new File(datFolderPath);
	    File[] listOfFiles = folder.listFiles();
	    datList = new ArrayList<>();

	    CharSequence dat = ".dat";
	    
	    for (int i = 0; i < listOfFiles.length; i++) {
	       if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(dat)){
	    	   datList.add(listOfFiles[i].getName());
	       }
	    }
	   
	    
	    try{
		   java.util.Collections.sort(datList);
		}
		catch(Exception g){
		    	
		 }
	    
	   
		 for(int j = 0; j<datList.size(); j++){
			 TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
			 t.setText(datList.get(j));
		  }
		   
		  folderDisplayTable.getVerticalBar().setEnabled(true);
	 
   }
   
   public void clearTable(){
	   
	   TableItem[] itemsToRemove = folderDisplayTable.getItems();
		
//		for(TableItem ra : folderDisplayTable.getItems()){
//			for(TableItem rb : itemsToRemove){
//				if(ra.getText().equals(rb.getText())){
//					ra.dispose();
//				}
//			}
//		}
//		
		folderDisplayTable.removeAll();
  }
   
   public Table getRodDisplayTable(){
	   return rodDisplayTable;
   }
   
   public String getSelectedOption(){
	   return options[optionsDropDown.getSelectionIndex()] ;
   }

	public SashForm getSelectionSash() {
		return selectionSash;
	}
	
	public void setSelectionSash(SashForm selectionSash) {
		this.selectionSash = selectionSash;
	}
  
	
	public void redrawDatDisplayerFolderView(){
		
		this.getParent().layout(true, true);
		this.redraw();
		this.update();
		this.pack();
		
		selectionSash.getParent().layout(true,true);
		selectionSash.redraw();
		selectionSash.update();
	}
}
   
