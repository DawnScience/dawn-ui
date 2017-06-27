package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.GeometricCorrectionsReflectivityMethod;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DatDisplayer extends Composite {

	private Button massRunner;
	private Button selectFiles;
	private SurfaceScatterPresenter ssp;
	private Table rodDisplayTable;
	private Table paramFileTable;
	private Combo optionsDropDown;
	private String[] options;
	private ArrayList<String> datList;
	private ArrayList<String> paramFileList;
	private Table folderDisplayTable;
	private Button buildRod;
	private SashForm selectionSash;
	private Button datFolderSelection;
	private Button paramFileSelection;
	private Button refreshTable;
	private Button clearParameterTable;
	private SurfaceScatterViewStart ssvs;
	private String datFolderPath = null;
	private String paramFolderPath = null;
	private Button transferToRod;
	private Group rodConstruction;
	private Button deleteSelected;
	private Button clearTable;
	private Button clearRodTable;
	private Group scannedVariableOptions;
	private Group rodComponents;
	private Text datFolderText;
	private Text paramFileText;
	private String imageName;
	private Boolean promptedForImageFolder = false;
	private boolean r;
	private String filepath;
	private Button selectAll;
	private String option;
	private RodSetupWindow rsw;
	private Group parameterFiles;
	private ArrayList<TableItem> paramFilesChecked;
	private SelectionListener timothy;



	public DatDisplayer(Composite parent, 
						int style, 
						SurfaceScatterPresenter ssp,
						SurfaceScatterViewStart ssvs,
						RodSetupWindow rsw) {

		super(parent, style);

		this.createContents();
		this.ssp = ssp;
		this.ssvs = ssvs;
		this.rsw = rsw;

	}

	public void createContents() {

		selectionSash = new SashForm(this, SWT.FILL);
		selectionSash.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite left = new Composite(selectionSash, SWT.NONE);///// was
																///// SWT.FiLL
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(GridData.FILL));

		Composite right = new Composite(selectionSash, SWT.FILL);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(GridData.FILL));

		Group datSelector = new Group(left, SWT.V_SCROLL | SWT.FILL);
		GridLayout datSelectorLayout = new GridLayout(1, true);
		GridData datSelectorData = new GridData((GridData.FILL_BOTH));
		datSelector.setLayout(datSelectorLayout);
		datSelector.setLayoutData(datSelectorData);
		datSelector.setText("Selected Dat Files");

		Group datFolders = new Group(datSelector, SWT.NONE);
		GridLayout datFoldersLayout = new GridLayout(2, true);
		GridData datFoldersData = new GridData((GridData.FILL_HORIZONTAL));
		datFolders.setLayout(datFoldersLayout);
		datFolders.setLayoutData(datFoldersData);

		datFolderPath = null;

		datFolderSelection = new Button(datFolders, SWT.PUSH | SWT.FILL);

		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		datFolderSelection.setText("Select .dat File Folder");

		datFolderSelection.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				DirectoryDialog dlg = new DirectoryDialog(ssvs.getShell(), SWT.OPEN);
				
				if(ssvs.getDatFolderPath() != null){
				
					dlg.setFilterPath(ssvs.getDatFolderPath());
				}

		        dlg.setText(".dat file directory");

		        dlg.setMessage("Select a directory");

		        String dir = dlg.open();
				datFolderPath = dir;
				ssvs.setDatFolderPath(dir);

				if (datFolderPath != null) {

					clearTable.setEnabled(true);
					refreshTable.setEnabled(true);
				}

				datFolderText.setText(datFolderPath);
				datFolderText.setEnabled(true);

				fillTable();
				folderDisplayTable.setEnabled(true);
				transferToRod.setEnabled(true);
				clearTable.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		datFolderText = new Text(datFolders, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		datFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		datFolderText.setEnabled(false);
		datFolderText.setEditable(false);

		clearTable = new Button(datSelector, SWT.PUSH | SWT.FILL);
		clearTable.setText("Clear Table");
		clearTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clearTable.setEnabled(false);
		
		refreshTable = new Button(datSelector, SWT.PUSH | SWT.FILL);
		refreshTable.setText("Refresh Table");
		refreshTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		refreshTable.setEnabled(false);

		refreshTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearTable();
				fillTable();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		transferToRod = new Button(datSelector, SWT.PUSH);
		transferToRod.setText("Transfer to Rod ->");
		transferToRod.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		transferToRod.setEnabled(false);

		folderDisplayTable = new Table(datSelector, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		folderDisplayTable.setEnabled(false);

		selectionSash.getParent().layout(true, true);
		selectionSash.redraw();
		selectionSash.update();

		GridData folderDisplayTableData = new GridData(GridData.FILL_BOTH);

		folderDisplayTable.setLayoutData(folderDisplayTableData);
		folderDisplayTable.setLayout(new GridLayout());
		folderDisplayTable.getVerticalBar().setEnabled(true);

		folderDisplayTable.getVerticalBar().setEnabled(true);
		folderDisplayTable.getVerticalBar().setIncrement(1);
		folderDisplayTable.getVerticalBar().setThumb(1);

		rodConstruction = new Group(right, SWT.V_SCROLL | SWT.FILL | SWT.FILL);
		GridLayout rodConstructionLayout = new GridLayout(1, true);
		GridData rodConstructionData = new GridData(GridData.FILL_BOTH);
		rodConstruction.setLayout(rodConstructionLayout);
		rodConstruction.setLayoutData(rodConstructionData);
		rodConstruction.setText("Rod Construction, Tracking and Background Options");
		rodConstruction.setEnabled(false);

		scannedVariableOptions = new Group(rodConstruction, SWT.NULL);
		GridLayout scannedVariableOptionsLayout = new GridLayout(1, true);
		GridData scannedVariableOptionsData = new GridData(GridData.FILL_HORIZONTAL);
		scannedVariableOptions.setLayout(scannedVariableOptionsLayout);
		scannedVariableOptions.setLayoutData(scannedVariableOptionsData);
		scannedVariableOptions.setText("Scanned Variables");
		scannedVariableOptions.setEnabled(false);

		optionsDropDown = new Combo(scannedVariableOptions, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
		optionsDropDown.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		optionsDropDown.setEnabled(false);

		optionsDropDown.select(0);

		rodComponents = new Group(rodConstruction, SWT.NULL | SWT.V_SCROLL | SWT.FILL);
		GridLayout rodComponentsLayout = new GridLayout(1, true);
		GridData rodComponentsData = new GridData(GridData.FILL_BOTH);
		rodComponents.setLayout(rodComponentsLayout);
		rodComponents.setLayoutData(rodComponentsData);
		rodComponents.setText("Rod Components");
		rodComponents.setEnabled(false);

		clearRodTable= new Button(rodComponents, SWT.PUSH | SWT.FILL);
		clearRodTable.setText("Clear Rod Table");
		clearRodTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clearRodTable.setEnabled(false);
		
		selectAll= new Button(rodComponents, SWT.PUSH | SWT.FILL);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.setEnabled(false);
		
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
		rodDisplayTable.setLayoutData(rodDisplayData);
		rodDisplayTable.setLayout(new GridLayout());
		rodDisplayTable.setEnabled(false);

		transferToRod.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				r= true;
				
				ArrayList<TableItem> checkedList = new ArrayList<>();

				for (TableItem d : folderDisplayTable.getItems()) {
					if (d.getChecked()) {
						checkedList.add(d);
					}
				}

				TableItem[] chosenDats = new TableItem[checkedList.size()];

				for (int g = 0; g < checkedList.size(); g++) {
					chosenDats[g] = checkedList.get(g);
				}

				ArrayList<String> itemsToRemove = new ArrayList<>();
				ArrayList<TableItem> tidiedTransferList = new ArrayList<>();

				for (TableItem cd : chosenDats) {
					for (int ti = 0; ti < rodDisplayTable.getItemCount(); ti++) {
						if (cd.getText().equals(rodDisplayTable.getItem(ti))) {
							itemsToRemove.add(cd.getText());
						}
					}
				}

				if (itemsToRemove.size() != 0) {
					for (String ti : itemsToRemove) {
						for (TableItem it1 : chosenDats) {
							if (it1.getText().equals(ti) == false) {
								tidiedTransferList.add(it1);
							}
						}
					}
				}

				else {
					for (TableItem it1 : chosenDats) {
						tidiedTransferList.add(it1);
					}
				}
				
				IDataHolder dh1 = null;
				ILazyDataset ild = null;
				
				try {

					String filename = tidiedTransferList.get(0).getText();
					String filepath1 = datFolderPath + File.separator + filename;
					filepath = filepath1;
					dh1 = LoaderFactory.getData(filepath);

				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
				optionsDropDown.removeAll();

				options = dh1.getNames();
				
				ssp.setOptions(options);
				ssvs.populateThetaOptionsDropDown();
				ssvs.getParamField().getSelectedOption().select(0);
				ssvs.getParamField().getTheta().select(0);
				
				List<String> pb = Arrays.asList(options);
				
				while(r){
					
					ild = null;
					
					if(pb.contains(ssp.getImageName())){
				
						ild = dh1.getLazyDataset(ssp.getImageName());
						
						if(ild == null){
							ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);	
							
							try {

								dh1 = ssp.copiedDatWithCorrectedTifs(tidiedTransferList.get(0).getText(), datFolderPath);
								ild = dh1.getLazyDataset(ssp.getImageName());
								
							} catch (Exception e2) {
								e2.printStackTrace();
								ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);	
							}
						}
					}
					
					if (ild == null && r ==true && ssp.getImageFolderPath() != null ){
						
						try {
							
							dh1 = ssp.copiedDatWithCorrectedTifs(tidiedTransferList.get(0).getText(), datFolderPath);
							ild = dh1.getLazyDataset(ssp.getImageName());
	
						} catch (Exception e2) {
							e2.printStackTrace();
						}
						
					}
					
					if(ild == null && r ==true){
						
						ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);
						
						try {
	
							dh1 = ssp.copiedDatWithCorrectedTifs(tidiedTransferList.get(0).getText(), datFolderPath);
							ild = dh1.getLazyDataset(ssp.getImageName());
	
						} catch (Exception e2) {
							e2.printStackTrace();
						}	
					}
					
					if(ild != null){
						r=false;
						promptedForImageFolder = false;
					}
				}
				
				if(ild != null){
					
					
					for (int k = 0; k < tidiedTransferList.size(); k++) {
						TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
						t.setText(tidiedTransferList.get(k).getText());
					}
					
					
					try {

						String filename = rodDisplayTable.getItem(0).getText();
						String filep = datFolderPath + File.separator + filename;
						dh1 = LoaderFactory.getData(filep);

					} catch (Exception e2) {
						e2.printStackTrace();
					}
					
					for (int t = 0; t < options.length; t++) {
						optionsDropDown.add(options[t]);
					}

					boolean isThePreviousOptionAvailable = false;
					
					if(option != null){
						for(int y = 0; y<options.length; y++){
							if(StringUtils.equals(options[y], option)){
								isThePreviousOptionAvailable =true;
								optionsDropDown.select(y);
							}
						}
					}
					else{
						optionsDropDown.select(0);
					}
					
					if (isThePreviousOptionAvailable == false){
						optionsDropDown.select(0);
					}
					
					clearRodTable.setEnabled(true);
					clearParameterTable.setEnabled(true);
					rodConstruction.setEnabled(true);
					deleteSelected.setEnabled(true);
					buildRod.setEnabled(true);
					optionsDropDown.setEnabled(true);
					rodDisplayTable.setEnabled(true);
					rodComponents.setEnabled(true);
					parameterFiles.setEnabled(true);
					paramFileTable.setEnabled(true);
					paramFileSelection.setEnabled(true);
					scannedVariableOptions.setEnabled(true);
					folderDisplayTable.getVerticalBar().setEnabled(true);
					ssvs.setupRightEnabled(true);
					enableRodConstruction(true);
					rsw.setupRightEnabled(true);
				}
				
				ArrayList<MethodSetting> cC = checkCorrections();
				
				ssvs.resetSXRDReflectivityCombo(comboPositionToEnumInt(cC));
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		selectAll.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				rodDisplayTable.selectAll();
		
				for(TableItem f :rodDisplayTable.getItems()){
					f.setChecked(true);
				}				
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
				ssvs.setupRightEnabled(false);
				enableRodConstruction(false);
				transferToRod.setEnabled(false);
				clearTable.setEnabled(false);
				ssp.setImageFolderPath(null);
				rodDisplayTable.removeAll();				
			}
			
			

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		
		clearRodTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				rodDisplayTable.removeAll();
				enableRodConstruction(false);
				rsw.setupRightEnabled(false);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		deleteSelected.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<String> itemsToKeepList = new ArrayList<>(); 
				
				
				for (TableItem ra : rodDisplayTable.getItems()) {
					if(ra.getChecked() == false){
						itemsToKeepList.add(ra.getText());
					}
				}

				rodDisplayTable.removeAll();

				for (String ra : itemsToKeepList) {
					TableItem rat = new TableItem(rodDisplayTable, SWT.NONE);
					rat.setText(ra);
				}

				IDataHolder dh1 = null;

				try {

					String filename = rodDisplayTable.getItem(0).getText();
					String filep = datFolderPath + File.separator + filename;
					dh1 = LoaderFactory.getData(filep);

					optionsDropDown.removeAll();

					options = dh1.getNames();
					ssp.setOptions(options);
					ssvs.populateThetaOptionsDropDown();

					for (int t = 0; t < options.length; t++) {
						optionsDropDown.add(options[t]);
					}

					optionsDropDown.select(0);

				} catch (Exception e2) {
					e2.printStackTrace();
				}

				if (rodDisplayTable.getItemCount() == 0) {
					enableRodConstruction(false);
					ssvs.setupRightEnabled(false);
					rsw.setupRightEnabled(false);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		parameterFiles = new Group(rodConstruction, SWT.NULL | SWT.V_SCROLL | SWT.FILL);
		GridLayout parameterFilesLayout = new GridLayout(1, true);
		GridData parameterFilesData = new GridData(GridData.FILL_BOTH);
		parameterFiles.setLayout(parameterFilesLayout);
		parameterFiles.setLayoutData(parameterFilesData);
		parameterFiles.setText("Parameter Files");
		parameterFiles.setEnabled(false);
		
		Group parameterFilesSelect = new Group(parameterFiles, SWT.NONE);
		GridLayout parameterFilesSelectLayout = new GridLayout(2, true);
		GridData parameterFilesSelectData = new GridData((GridData.FILL_HORIZONTAL));
		parameterFilesSelect.setLayout(parameterFilesSelectLayout);
		parameterFilesSelect.setLayoutData(parameterFilesSelectData);

		paramFileSelection = new Button(parameterFilesSelect, SWT.PUSH | SWT.FILL);
		paramFileSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		paramFileSelection.setText("Select Parameter File");
		paramFileSelection.setEnabled(false);
		
		paramFileSelection.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog dlg = new FileDialog(ssvs.getShell(), SWT.OPEN);
				
				if(paramFolderPath != null){
					dlg.setFilterPath(paramFolderPath);
				}

		        dlg.setText("parameter file");

		        String dir = dlg.open();
		        
		        paramFolderPath = dir;
		        
		        paramFileText.setText(paramFolderPath);
		        paramFileText.setEnabled(true);

		        
		        clearParameterTable.setEnabled(true);
				
				addToTable(dir);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		paramFileText = new Text(parameterFilesSelect, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		paramFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		paramFileText.setEnabled(false);
		paramFileText.setEditable(false);

		clearParameterTable = new Button(parameterFiles, SWT.PUSH | SWT.FILL);
		clearParameterTable.setText("Clear Table");
		clearParameterTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clearParameterTable.setEnabled(false);
		
		clearParameterTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				for(int cv = 0; cv<paramFileTable.getItems().length; cv++){
					paramFileTable.remove(cv);
				}
				paramFileList.clear();
				paramFileTable.removeAll();	
				paramFilesChecked.clear();
				clearParameterTable.setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		
		paramFileTable = new Table(parameterFiles, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData paramFileTableData = new GridData(GridData.FILL_BOTH);
		paramFileTable.setLayoutData(paramFileTableData);
		paramFileTable.setLayout(new GridLayout());
		paramFileTable.setEnabled(false);

		timothy = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				paramFileTable.removeSelectionListener(timothy);
				thereCanBeOnlyOne();
				paramFileTable.addSelectionListener(timothy);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		
		
//		paramFileTable.addSelectionListener(timothy);
		
		paramFileTable.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				Object r = e.item;
				
				for(TableItem yu : paramFileTable.getItems()){
					if(yu != r){
						yu.setChecked(false);
					}

				}
				
				return;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
//		selectionSash.setWeights(new int[] { 30, 30, 30 });
	}

	public Table getParamFileTable() {
		return paramFileTable;
	}

	public Composite getComposite() {
		return this;
	}


	public Button getBuildRod() {
		return buildRod;
	}


	public void enableRodConstruction(boolean enabled) {
		rodConstruction.setEnabled(enabled);
		scannedVariableOptions.setEnabled(enabled);
		rodComponents.setEnabled(enabled);
		rodDisplayTable.setEnabled(enabled);
		buildRod.setEnabled(enabled);
		deleteSelected.setEnabled(enabled);
		optionsDropDown.setEnabled(enabled);
		clearRodTable.setEnabled(enabled);
		selectAll.setEnabled(enabled);
	}

	public Button getMassRunner() {
		return massRunner;
	}

	public Button getSelectFiles() {
		return selectFiles;
	}

	private void fillTable() {

		File folder = new File(datFolderPath);
		File[] listOfFiles = folder.listFiles();
		datList = new ArrayList<>();

		CharSequence dat = ".dat";

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(dat)) {
				datList.add(listOfFiles[i].getName());
			}
		}

		try {
			java.util.Collections.sort(datList);
		} catch (Exception g) {

		}

		for (int j = 0; j < datList.size(); j++) {
			TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
			t.setText(datList.get(j));
		}

		folderDisplayTable.getVerticalBar().setEnabled(true);

	}
	
	public void addToTable(String in) {
		
		
		for(int cv = 0; cv<paramFileTable.getItems().length; cv++){
			paramFileTable.remove(cv);
		}
		
		paramFileTable.removeAll();	

		if(paramFileList == null){
			paramFileList = new ArrayList<String>();
		}
		
		paramFileList.add(in);

		try {
			java.util.Collections.sort(paramFileList);
		} catch (Exception g) {

		}

		paramFileTable.clearAll();
		
		for (int j = 0; j < paramFileList.size(); j++) {
			TableItem t = new TableItem(paramFileTable, SWT.NONE);
			t.setText(paramFileList.get(j));
		}

		paramFileTable.getVerticalBar().setEnabled(true);

	}

	public void clearTable() {
		folderDisplayTable.removeAll();
	}

	public Table getRodDisplayTable() {
		return rodDisplayTable;
	}

	public String getSelectedOption() {
		return options[optionsDropDown.getSelectionIndex()];
	}

	public SashForm getSelectionSash() {
		return selectionSash;
	}

	public void setSelectionSash(SashForm selectionSash) {
		this.selectionSash = selectionSash;
	}

	public void redrawDatDisplayerFolderView() {

		this.getParent().layout(true, true);
		this.redraw();
		this.update();
		this.pack();

		selectionSash.getParent().layout(true, true);
		selectionSash.redraw();
		selectionSash.update();
	}
	
	public void setR(boolean r){
		this.r = r;
		
	}

	public boolean getR(){
		return this.r;
	}

	public ArrayList<MethodSetting> checkCorrections(){
	
		ArrayList<MethodSetting> output = new ArrayList<>();
		
		try{
			
			double lorentz = SXRDGeometricCorrections.lorentz(filepath).getDouble(0);
			
			double areaCorrection = SXRDGeometricCorrections.areacor(filepath,
																	 ssvs.getParamField().getBeamCorrection().getSelection(), 
																	 ssvs.getParamField().getSpecular().getSelection(), 
																	 Double.valueOf(ssvs.getParamField().getSampleSize().getText()),
																	 Double.valueOf(ssvs.getParamField().getOutPlaneSlits().getText()), 
																	 Double.valueOf(ssvs.getParamField().getInPlaneSlits().getText()), 
																	 Double.valueOf(ssvs.getParamField().getBeamInPlane().getText()),
																	 Double.valueOf(ssvs.getParamField().getBeamOutPlane().getText()), 
																	 Double.valueOf(ssvs.getParamField().getDetectorSlits().getText())).getDouble(0);
			
			double polarisation = SXRDGeometricCorrections.polarisation(filepath, 
					 												    Double.valueOf(ssvs.getParamField().getInplanePolarisation().getText()), 
					 												    Double.valueOf(ssvs.getParamField().getOutplanePolarisation().getText())).getDouble(0);
			
			
			output.add(MethodSetting.SXRD);
			
		}
		catch(Exception i){
			
		}
		
		
		try{
			
			IDataHolder dh1 = LoaderFactory.getData(filepath);
			
			ILazyDataset dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getdcdtheta());
			
			ILazyDataset qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqdcd());
		
			if (dcdtheta == null) {
				try {
					dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());
		
				} catch (Exception e2) {

				}
			} 
			else {
			}
			
			if (qdcd == null) {
				try {
					qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());
		
				} catch (Exception e2) {

				}
			} 
			
			else {
			}
			
			
			SliceND sl = new SliceND(qdcd.getShape());
			Dataset QdcdDat = null;
			
			try {
				QdcdDat = (Dataset) qdcd.getSlice(sl);
				
			} catch (DatasetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			
			double geometricForReflectivity 
				= GeometricCorrectionsReflectivityMethod.reflectivityCorrectionsBatch(dcdtheta, 
																					  0,  
																					  Double.valueOf(ssvs.getParamField().getAngularFudgeFactor().getText()), 
																					  Double.valueOf(ssvs.getParamField().getBeamHeight().getText()), 
																					  Double.valueOf(ssvs.getParamField().getFootprint().getText()));
			
			
			output.add(MethodSetting.Reflectivity_with_Flux_Correction);
			output.add(MethodSetting.Reflectivity_without_Flux_Correction);
			
		}
		catch(Exception i){
			
		}
		
		output.add(MethodSetting.Reflectivity_NO_Correction);
		
		
		return output;
	}
	
	public int[] comboPositionToEnumInt(ArrayList<MethodSetting> input){
		
		int[] output = new int[input.size()];
		
		for(int i =0; i <input.size(); i++){
			output[i] = MethodSetting.toInt(input.get(i));
		}
		
		return output;
	}
	
	private void thereCanBeOnlyOne(){
		if(paramFilesChecked == null){
			paramFilesChecked = new ArrayList<>();
		}
		
		if (paramFilesChecked.size()>0){
			for(TableItem ti : paramFilesChecked){
				ti.setChecked(false);
			}
		}
		
		paramFilesChecked = new ArrayList<>();
		
		for (TableItem ra : paramFileTable.getItems()) {
			if(ra.getChecked() == true){
				paramFilesChecked.add(ra);
			}
		}
		
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public Boolean getPromptedForImageFolder() {
		return promptedForImageFolder;
	}

	public void setPromptedForImageFolder(Boolean promptedForImageFolder) {
		this.promptedForImageFolder = promptedForImageFolder;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	
}
