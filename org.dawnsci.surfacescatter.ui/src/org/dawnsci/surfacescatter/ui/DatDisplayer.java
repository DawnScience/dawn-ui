package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.dawnsci.surfacescatter.DisplayLabelStrings;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.GeometricCorrectionsReflectivityMethod;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DatDisplayer extends Composite implements IDatDisplayer {

	private Button selectFiles;
	private SurfaceScatterPresenter ssp;
	private Table rodDisplayTable;
	private Table paramFileTable;
	private Combo optionsDropDown;
	private String[] options;
	private ArrayList<String> paramFileList;
	private Table folderDisplayTable;
	private Button buildRod;
	private SashForm selectionSash;
	private Button paramFileSelection;
	private Button refreshTable;
	private Button deSelectAll;
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
	private Boolean promptedForImageFolder = false;
	private boolean r;
	private String filepath;
	private Button selectAll;
	private String option;
	private RodSetupWindow rsw;
	private Group parameterFiles;
	private ArrayList<TableItem> paramFilesChecked;
	private Group numericalDatSelection;
	private InputTileGenerator[] itgArray;
	private Button transferUsingIncrement;
	private boolean useTrajectory = true;
	private Button useTrajectoryButton;
	private InputTileGenerator useTrajectoryTile;
	private InputTileGenerator parameterFilesTile;
	private boolean useIncrement;
	private InputTileGenerator searchBox;
	private String[] in0;
	private String[] in1;
	

	public DatDisplayer(Composite parent, int style, SurfaceScatterPresenter ssp, SurfaceScatterViewStart ssvs,
			RodSetupWindow rsw) {

		super(parent, style);

		this.createContents();
		this.ssp = ssp;
		this.ssvs = ssvs;
		this.rsw = rsw;

	}

	public void createContents() {

		useIncrement = false;

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

		Button datFolderSelection = new Button(datFolders, SWT.PUSH | SWT.FILL);

		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		datFolderSelection.setText("Select .dat File Folder");

		datFolderSelection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				DirectoryDialog dlg = new DirectoryDialog(ssvs.getShell(), SWT.OPEN);

				if (ssvs.getDatFolderPath() != null) {

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
				for (Control c : numericalDatSelection.getChildren()) {
					c.setEnabled(true);
				}

				numericalDatSelection.setEnabled(true);

				for (InputTileGenerator itg : itgArray) {
					itg.setEnabled(true, false);
				}

				sortOutEnabling(itgArray[2], itgArray[1]);
				sortOutEnabling(itgArray[2], itgArray[1]);
				transferUsingIncrement.setEnabled(true);
				deSelectAll.setEnabled(true);
				searchBox.setEnabled(true, false);
				
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

		refreshTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				clearTable();
				fillTable();

			}

		});

		numericalDatSelection = new Group(datSelector, SWT.NONE);
		GridLayout numericalDatSelectionLayout = new GridLayout(2, true);
		numericalDatSelection.setLayout(numericalDatSelectionLayout);
		GridData numericalDatSelectionData = new GridData(SWT.FILL, SWT.NULL, true, false);
		numericalDatSelection.setLayoutData(numericalDatSelectionData);
		numericalDatSelection.setEnabled(false);

		searchBox = new InputTileGenerator("Search Scan Command For String: ", "", datSelector, 0);
		searchBox.setEnabled(false, false);
		
		searchBox.getText().addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(!searchBox.getText().getText().isEmpty()) {
					fillTable(SearchForString.search(searchBox.getText().getText(), in0, in1));
				}
				else{
					fillTable(new String[][] {in0, in1});
				}
			}
		});
		
		transferToRod = new Button(datSelector, SWT.PUSH);
		transferToRod.setText(DisplayLabelStrings.getTransferSelectedToRod());
		transferToRod.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		transferToRod.setEnabled(false);
			
		deSelectAll = new Button(datSelector, SWT.PUSH);
		deSelectAll.setText(DisplayLabelStrings.getDeselectAll());
		deSelectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deSelectAll.setEnabled(false);

		deSelectAll.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				folderDisplayTable.deselectAll();
				for(TableItem t: folderDisplayTable.getItems()) {
					t.setChecked(false);
				}
			}
			
		});
		
		folderDisplayTable = new Table(datSelector, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI);
		folderDisplayTable.setEnabled(false);
		folderDisplayTable.setLinesVisible(true);

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

		//// folder table setup///

		folderDisplayTable.setHeaderVisible(true);

		TableColumn datColumn = new TableColumn(folderDisplayTable, SWT.MULTI | SWT.BORDER);
		datColumn.setText(".dat File");

		TableColumn commandColumn = new TableColumn(folderDisplayTable, SWT.MULTI | SWT.BORDER);
		commandColumn.setText("Scan Command");

		for (Control c : numericalDatSelection.getChildren()) {
			c.setEnabled(false);
		}

		InputTileGenerator startDat = new InputTileGenerator("Starting .Dat: ", "", numericalDatSelection, 0);
		startDat.setEnabled(false, false);
		InputTileGenerator endDat = new InputTileGenerator("Ending .Dat: ", "", numericalDatSelection, 0);
		endDat.setEnabled(false, false);
		InputTileGenerator increment = new InputTileGenerator("", "Apply Increment: ", numericalDatSelection, true);
		increment.setEnabled(false, false);

		startDat.getText().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (useIncrement) {
					int inc = Integer.parseInt(increment.getText().getText());
					int start = Integer.parseInt(startDat.getText().getText());

					endDat.getText().setText(String.valueOf(start + inc));
				}

			}
		});

		itgArray = new InputTileGenerator[] { startDat, endDat, increment };

		increment.getText().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (increment.isStateOfText()) {
					int inc = Integer.parseInt(increment.getText().getText());
					int start = Integer.parseInt(startDat.getText().getText());

					endDat.getText().setText(String.valueOf(start + inc));
				}
			}
		});

		increment.getRadio().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				useIncrement = !useIncrement;

				int inc = Integer.parseInt(increment.getText().getText());
				int start = Integer.parseInt(startDat.getText().getText());

				endDat.getText().setText(String.valueOf(start + inc));

				increment.setStateOfText(!increment.isStateOfText());

				increment.getText().setEnabled(true);

				increment.getRadio().setText("Apply Increment: ");

				if (increment.isStateOfText()) {
					increment.getRadio().setText("Deactivate Increment");

				}

				endDat.setEnabled(!increment.isStateOfText(), false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		transferUsingIncrement = new Button(numericalDatSelection, SWT.PUSH);
		transferUsingIncrement.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		transferUsingIncrement.setText(DisplayLabelStrings.getTransferSeriesToRod());
		transferUsingIncrement.setEnabled(false);

		transferUsingIncrement.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				rodDisplayTable.removeAll();

				String startDatString = "";
				String endDatString = "";
				String dat = ".dat";

				if (StringUtils.contains(startDat.getText().getText(), dat)) {
					startDatString = startDat.getText().getText();
				} else {
					startDatString = startDat.getText().getText() + dat;
				}
				if (StringUtils.contains(endDat.getText().getText(), dat)) {
					endDatString = startDat.getText().getText();
				} else {
					endDatString = endDat.getText().getText() + dat;
				}

				boolean checkStart = false;
				boolean checkEnd = false;

				int startDatInt = 0;
				int endDatInt = 0;

				ArrayList<String> tidiedTransferList = new ArrayList<>();

				for (int i = 0; i < in0.length; i++) {

					if (in0[i].equals(startDatString)) {
						startDatInt = i;
						checkStart = true;
					}

					if (in0[i].equals(endDatString)) {
						endDatInt = i;
						checkEnd = true;
					}

					if (checkEnd && checkStart) {
						break;
					}

				}

				
				if (!checkEnd || !checkStart) {
					RegionOutOfBoundsWarning aah = new RegionOutOfBoundsWarning(ssvs.getShell(), 12, null);
					aah.open();
				}

				for (int cv = 0; cv < rodDisplayTable.getItems().length; cv++) {
					rodDisplayTable.remove(cv);
				}

				rodDisplayTable.removeAll();

				rodDisplayTable.clearAll();

				for (int j = startDatInt; j <= endDatInt; j++) {
					tidiedTransferList.add(in0[j]);
				}

				r = true;

				String[] tidiedTransferArray = new String[tidiedTransferList.size()];
				tidiedTransferList.toArray(tidiedTransferArray);
				
				prepareToBuildRod(tidiedTransferArray);

			}

		});

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

		clearRodTable = new Button(rodComponents, SWT.PUSH | SWT.FILL);
		clearRodTable.setText("Clear Rod Table");
		clearRodTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clearRodTable.setEnabled(false);

		selectAll = new Button(rodComponents, SWT.PUSH | SWT.FILL);
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

		transferToRod.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				r = true;

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
				if (!itemsToRemove.isEmpty()) {
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

				prepareToBuildRod(tidiedTransferList);

			}
		});

		selectAll.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				rodDisplayTable.selectAll();

				for (TableItem f : rodDisplayTable.getItems()) {
					f.setChecked(true);
				}
			}

		});

		clearTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				clearTable();
				ssvs.setupRightEnabled(false);
				enableRodConstruction(false);
				transferToRod.setEnabled(false);
				clearTable.setEnabled(false);
				ssp.setImageFolderPath(null);
				rodDisplayTable.removeAll();
				for (Control c : numericalDatSelection.getChildren()) {
					c.setEnabled(false);
				}
				numericalDatSelection.setEnabled(false);

				for (InputTileGenerator itg : itgArray) {
					if (itg.getRadio() != null) {
						itg.setEnabled(false, false);
					} else {
						itg.setEnabled(false, true);
					}
				}
				sortOutEnabling(itgArray[2], itgArray[1]);
				sortOutEnabling(itgArray[2], itgArray[1]);
				transferUsingIncrement.setEnabled(false);
				refreshTable.setEnabled(false);
				parameterFilesTile.setEnabled(false);
				clearParameterTable.setEnabled(false);
				deSelectAll.setEnabled(false);
				searchBox.setEnabled(false, false);
			}

		});

		clearRodTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				rodDisplayTable.removeAll();
				enableRodConstruction(false);
				rsw.setupRightEnabled(false);

			}
		});

		deleteSelected.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<String> itemsToKeepList = new ArrayList<>();

				for (TableItem ra : rodDisplayTable.getItems()) {
					if (!ra.getChecked()) {
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

		});

		parameterFiles = new Group(rodConstruction, SWT.NULL | SWT.V_SCROLL | SWT.FILL);
		GridLayout parameterFilesLayout = new GridLayout(1, true);
		GridData parameterFilesData = new GridData(GridData.FILL_BOTH);
		parameterFiles.setLayout(parameterFilesLayout);
		parameterFiles.setLayoutData(parameterFilesData);
		parameterFiles.setText("Parameter Files");
		parameterFiles.setEnabled(false);

		parameterFilesTile = new InputTileGenerator("Select Parameter File", parameterFiles, 2);

		paramFileSelection = parameterFilesTile.getPush();
		paramFileText = parameterFilesTile.getText();

		parameterFilesTile.setEnabled(false);

		paramFileSelection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog dlg = new FileDialog(ssvs.getShell(), SWT.OPEN);

				if (paramFolderPath != null) {
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

		});

		clearParameterTable = new Button(parameterFiles, SWT.PUSH | SWT.FILL);
		clearParameterTable.setText("Clear Table");
		clearParameterTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		clearParameterTable.setEnabled(false);

		clearParameterTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				for (int cv = 0; cv < paramFileTable.getItems().length; cv++) {
					paramFileTable.remove(cv);
				}
				paramFileList.clear();
				paramFileTable.removeAll();
				paramFilesChecked.clear();
				clearParameterTable.setEnabled(false);
			}
		});

		useTrajectoryTile = new InputTileGenerator("Use Trajectory From File:", parameterFiles, true);

		useTrajectoryTile.setEnabled(false);

		useTrajectoryButton = useTrajectoryTile.getRadio();

		useTrajectoryButton.setSelection(useTrajectory);

		useTrajectoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				useTrajectory = useTrajectoryButton.getSelection();
			}

		});

		paramFileTable = new Table(parameterFiles, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData paramFileTableData = new GridData(GridData.FILL_BOTH);
		paramFileTable.setLayoutData(paramFileTableData);
		paramFileTable.setLayout(new GridLayout());
		paramFileTable.setEnabled(false);

		paramFileTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				TableItem ip = (TableItem) e.item;

				for (TableItem yu : paramFileTable.getItems()) {
					if (yu != ip) {
						yu.setChecked(false);
					}
				}

				try {
					NexusFile file = new NexusFileFactoryHDF5().newNexusFile(ip.getText());

					FittingParametersInputReader.geometricalParametersReaderFromNexus(file, ssp.getGm(), ssp.getDrm());

					rsw.getParamField().setUpdateOn(false);
					rsw.getParamField().updateDisplayFromGm(ssp.getGm());
					rsw.getAnglesAliasWindow().setFluxPath(ssp.getGm().getFluxPath());
					rsw.getParamField().setUpdateOn(true);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

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

	private void enableRodConstruction(boolean enabled) {
		rodConstruction.setEnabled(enabled);
		scannedVariableOptions.setEnabled(enabled);
		rodComponents.setEnabled(enabled);
		rodDisplayTable.setEnabled(enabled);
		buildRod.setEnabled(enabled);
		deleteSelected.setEnabled(enabled);
		optionsDropDown.setEnabled(enabled);
		clearRodTable.setEnabled(enabled);
		selectAll.setEnabled(enabled);
		useTrajectoryTile.setEnabled(enabled);
		parameterFiles.setEnabled(enabled);
		parameterFilesTile.setEnabled(enabled);

		
		for (Control c : parameterFiles.getChildren()) {
			c.setEnabled(enabled);
		}
	}

	public Button getSelectFiles() {
		return selectFiles;
	}

	private void fillTable(String[][] in) {
		 
		String[] in0local = in[0];
		String[] in1local = in[1];
	
		
		folderDisplayTable.removeAll();
		
		for (int j = 0; j < in[0].length; j++) {			
			TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
			t.setText(0, in0local[j]);
			t.setText(1, in1local[j]);
		}

		for (int loopIndex = 0; loopIndex < folderDisplayTable.getColumnCount(); loopIndex++) {
			folderDisplayTable.getColumn(loopIndex).pack();
		}
		
	}
	
		
	private void fillTable() {

		File folder = new File(datFolderPath);
		File[] listOfFiles = folder.listFiles();
		ArrayList<String> datList = new ArrayList<>();

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
		
		clearTable();

		in0 = new String[datList.size()];
		in1 = new String[datList.size()];
		
		for (int j = 0; j < datList.size(); j++) {

			Path from = Paths.get(datFolderPath + File.separator + datList.get(j));

			Charset charset = StandardCharsets.UTF_8;

			String content = "";

			try {
				content = new String(Files.readAllBytes(from), charset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String scanCommand = " ";
			try {
				scanCommand = StringUtils.substringBetween(content, "scan", "\n");
			} catch (Exception t) {

			}
			
			if(scanCommand == null) {
				scanCommand = " ";
			}
			TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
			
			t.setText(0, datList.get(j));
			in0[j] = datList.get(j);
			
			t.setText(1, scanCommand);
			in1[j] = scanCommand;
			

		}

		for (int loopIndex = 0; loopIndex < folderDisplayTable.getColumnCount(); loopIndex++) {
			folderDisplayTable.getColumn(loopIndex).pack();
		}

		folderDisplayTable.getVerticalBar().setEnabled(true);

	}

	public void addToTable(String in) {

		for (int cv = 0; cv < paramFileTable.getItems().length; cv++) {
			paramFileTable.remove(cv);
		}

		paramFileTable.removeAll();

		if (paramFileList == null) {
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
		ssp.getStm().setImageFolderPath(null);
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

	public void setR(boolean r) {
		this.r = r;

	}

	public boolean getR() {
		return this.r;
	}

	public ArrayList<MethodSetting> checkCorrections() {

		ArrayList<MethodSetting> output = new ArrayList<>();

		boolean notCaught = true;

		try {

			SXRDGeometricCorrections.lorentz(filepath).getDouble(0);

			SXRDGeometricCorrections.areacor(filepath, ssvs.getParamField().getBeamCorrection().getSelection(),
					ssvs.getParamField().getSpecular().getSelection(),
					Double.valueOf(ssvs.getParamField().getSampleSize().getText()),
					Double.valueOf(ssvs.getParamField().getOutPlaneSlits().getText()),
					Double.valueOf(ssvs.getParamField().getInPlaneSlits().getText()),
					Double.valueOf(ssvs.getParamField().getBeamInPlane().getText()),
					Double.valueOf(ssvs.getParamField().getBeamOutPlane().getText()),
					Double.valueOf(ssvs.getParamField().getDetectorSlits().getText())).getDouble(0);

			SXRDGeometricCorrections
					.polarisation(filepath, Double.valueOf(ssvs.getParamField().getInplanePolarisation().getText()),
							Double.valueOf(ssvs.getParamField().getOutplanePolarisation().getText()))
					.getDouble(0);

			notCaught = false;
			output.add(MethodSetting.SXRD);

		} catch (Exception i) {
			System.out.println(i.getMessage());
		}

		try {

			IDataHolder dh1 = LoaderFactory.getData(filepath);

			ILazyDataset dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getdcdtheta());

			ILazyDataset qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqdcd());

			if (dcdtheta == null) {
				try {
					dcdtheta = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getsdcdtheta());

				} catch (Exception e2) {

				}
			}
			if (qdcd == null) {
				try {
					qdcd = dh1.getLazyDataset(ReflectivityMetadataTitlesForDialog.getqsdcd());

				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}
			}

			GeometricCorrectionsReflectivityMethod.reflectivityCorrectionsBatchGaussianPofile(dcdtheta, 0,
					Double.valueOf(ssvs.getParamField().getAngularFudgeFactor().getText()),
					Double.valueOf(ssvs.getParamField().getBeamHeight().getText()),
					Double.valueOf(ssvs.getParamField().getFootprint().getText()));

			notCaught = false;
			output.add(MethodSetting.Reflectivity_with_Flux_Correction_Gaussian_Profile);
			output.add(MethodSetting.Reflectivity_without_Flux_Correction_Gaussian_Profile);

			output.add(MethodSetting.Reflectivity_with_Flux_Correction_Simple_Scaling);
			output.add(MethodSetting.Reflectivity_without_Flux_Correction_Simple_Scaling);

		} catch (Exception i) {

		}

		if (notCaught) {
			output.add(MethodSetting.Reflectivity_NO_Correction);
		} else {
			output.add(MethodSetting.Reflectivity_NO_Correction);
		}

		for (MethodSetting m : MethodSetting.values()) {
			boolean add = true;
			for (MethodSetting n : output) {
				if (n == m) {
					add = false;
				}
			}
			if (add) {
				output.add(m);
			}
		}

		return output;
	}

	public int[] comboPositionToEnumInt(ArrayList<MethodSetting> input) {

		int[] output = new int[input.size()];

		for (int i = 0; i < input.size(); i++) {
			output[i] = MethodSetting.toInt(input.get(i));
		}

		return output;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public void setPromptedForImageFolder(Boolean promptedForImageFolder) {
		this.promptedForImageFolder = promptedForImageFolder;
	}

	private void prepareToBuildRod(ArrayList<TableItem> tidiedTransferList) {

		IDataHolder dh1 = null;
		ILazyDataset ild = null;

		String filename = tidiedTransferList.get(0).getText();

		try {
			String filepath1 = datFolderPath + File.separator + filename;
			filepath = filepath1;
			dh1 = LoaderFactory.getData(filepath);

		} catch (Exception e2) {
			e2.printStackTrace();
		}

		optionsDropDown.removeAll();

		options = dh1.getNames();

		rsw.getAnglesAliasWindow().updateAllWithOptions(options, true);

		ssp.setOptions(options);
		ssvs.populateThetaOptionsDropDown();
		ssvs.getParamField().getSelectedOption().select(0);
		ssvs.getParamField().getTheta().select(0);

		List<String> pb = Arrays.asList(options);

		while (r) {

			ild = null;

			if (pb.contains(ssp.getImageName())) {

				ild = dh1.getLazyDataset(ssp.getImageName());

				if (ild == null) {
					ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);

					try {

						dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
						ild = dh1.getLazyDataset(ssp.getImageName());

					} catch (Exception e2) {
						e2.printStackTrace();
						ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);
					}
				}
			}

			if (ild == null && r == true && ssp.getImageFolderPath() != null) {

				try {

					dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
					ild = dh1.getLazyDataset(ssp.getImageName());

				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}

			if (ild == null && r == true) {

				ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);

				try {

					dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
					ild = dh1.getLazyDataset(ssp.getImageName());

				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

			if (ild != null) {
				r = false;
				promptedForImageFolder = false;
			}
		}

		if (ild != null) {

			for (int k = 0; k < tidiedTransferList.size(); k++) {
				TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
				t.setText(tidiedTransferList.get(k).getText());
			}

			try {

				String filename2 = rodDisplayTable.getItem(0).getText();
				String filep = datFolderPath + File.separator + filename2;
				dh1 = LoaderFactory.getData(filep);

			} catch (Exception e2) {
				e2.printStackTrace();
			}

			for (int t = 0; t < options.length; t++) {
				optionsDropDown.add(options[t]);
			}

			boolean isThePreviousOptionAvailable = false;

			if (option != null) {
				for (int y = 0; y < options.length; y++) {
					if (StringUtils.equals(options[y], option)) {
						isThePreviousOptionAvailable = true;
						optionsDropDown.select(y);
					}
				}
			} else {
				optionsDropDown.select(0);
			}

			if (isThePreviousOptionAvailable == false) {
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

		if (cC.get(0) == MethodSetting.SXRD) {
			rsw.getAnglesAliasWindow().getFolder().setSelection(0);
			rsw.getParamField().getFolder().setSelection(0);
		} else {
			rsw.getAnglesAliasWindow().getFolder().setSelection(1);
			rsw.getParamField().getFolder().setSelection(1);
		}

	}
	
	private void prepareToBuildRod(String[] in0Local) {
		IDataHolder dh1 = null;
		ILazyDataset ild = null;

		String filename = in0Local[0];
		String filepath1 = null;
		
		try {
			filepath1 = datFolderPath + File.separator + filename;
			filepath = filepath1;
			dh1 = LoaderFactory.getData(filepath);

		} catch (Exception e2) {
			e2.printStackTrace();
		}

		optionsDropDown.removeAll();

		options = dh1.getNames();

		rsw.getAnglesAliasWindow().updateAllWithOptions(options, true);

		ssp.setOptions(options);
		ssvs.populateThetaOptionsDropDown();
		ssvs.getParamField().getSelectedOption().select(0);
		ssvs.getParamField().getTheta().select(0);

		List<String> pb = Arrays.asList(options);

		while (r) {

			ild = null;

			if (pb.contains(ssp.getImageName())) {

				ild = dh1.getLazyDataset(ssp.getImageName());

				if (ild == null) {
					ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);

					try {

						dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
						ild = dh1.getLazyDataset(ssp.getImageName());

					} catch (Exception e2) {
						e2.printStackTrace();
						ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);
					}
				}
			}

			if (ild == null && r == true && ssp.getImageFolderPath() != null) {

				try {

					dh1 = ssp.copiedDatWithCorrectedTifs(filepath1, datFolderPath);
					ild = dh1.getLazyDataset(ssp.getImageName());

				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}

			if (ild == null && r == true) {

				ssp.dialogToChangeImageFolder(promptedForImageFolder, DatDisplayer.this);

				try {

					dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
					ild = dh1.getLazyDataset(ssp.getImageName());

				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}

			if (ild != null) {
				r = false;
				promptedForImageFolder = false;
			}
		}

		if (ild != null) {

			for (int k = 0; k < in0Local.length; k++) {
				TableItem t = new TableItem(rodDisplayTable, SWT.NONE);
				t.setText(in0Local[k]);
			}

			try {

				String filename2 = rodDisplayTable.getItem(0).getText();
				String filep = datFolderPath + File.separator + filename2;
				dh1 = LoaderFactory.getData(filep);

			} catch (Exception e2) {
				e2.printStackTrace();
			}

			for (int t = 0; t < options.length; t++) {
				optionsDropDown.add(options[t]);
			}

			boolean isThePreviousOptionAvailable = false;

			if (option != null) {
				for (int y = 0; y < options.length; y++) {
					if (StringUtils.equals(options[y], option)) {
						isThePreviousOptionAvailable = true;
						optionsDropDown.select(y);
					}
				}
			} else {
				optionsDropDown.select(0);
			}

			if (isThePreviousOptionAvailable == false) {
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

		if (cC.get(0) == MethodSetting.SXRD) {
			rsw.getAnglesAliasWindow().getFolder().setSelection(0);
			rsw.getParamField().getFolder().setSelection(0);
		} else {
			rsw.getAnglesAliasWindow().getFolder().setSelection(1);
			rsw.getParamField().getFolder().setSelection(1);
		}

	}

	private void sortOutEnabling(InputTileGenerator increment, InputTileGenerator endDat) {

		increment.setStateOfText(!increment.isStateOfText());

		increment.getText().setEnabled(true);

		increment.getRadio().setText("Apply Increment: ");

		if (increment.isStateOfText()) {
			increment.getRadio().setText("Deactivate Increment");

		}

		endDat.setEnabled(!increment.isStateOfText(), false);

	}

	public boolean getUseTrajectory() {
		return useTrajectory;
	}

	public void setUseTrajectory(boolean f) {
		this.useTrajectory = f;
	}

	@Override
	public void setImageFolderPath(String g) {

	}

	public void setSsp(SurfaceScatterPresenter in) {
		this.ssp = in;
	}
}
