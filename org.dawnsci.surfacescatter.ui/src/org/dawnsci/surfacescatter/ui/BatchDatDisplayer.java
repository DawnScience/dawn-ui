package org.dawnsci.surfacescatter.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.FileCounter;
import org.dawnsci.surfacescatter.FittingParametersInputReader;
import org.dawnsci.surfacescatter.GeometricCorrectionsReflectivityMethod;
import org.dawnsci.surfacescatter.MethodSettingEnum.MethodSetting;
import org.dawnsci.surfacescatter.ReflectivityMetadataTitlesForDialog;
import org.dawnsci.surfacescatter.SXRDGeometricCorrections;
import org.dawnsci.surfacescatter.StareModeSelection;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class BatchDatDisplayer extends Composite implements IDatDisplayer {

	private Button selectFiles;
	private SurfaceScatterPresenter ssp;
	private Table rodDisplayTable;
	private Table paramFileTable;
	private String[] options;
	private ArrayList<String> datList;
	private ArrayList<String> paramFileList;
	private Table folderDisplayTable;
	private Button transferToBatch;
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
	private Group rodComponents;
	private Text datFolderText;
	private Text paramFileText;
	private String imageName;
	private Boolean promptedForImageFolder = false;
	private boolean r;
	private String filepath;
	private Button selectAll;
	private String option;
	private Group parameterFiles;
	private ArrayList<TableItem> paramFilesChecked;
	private Group numericalDatSelection;
	private InputTileGenerator[] itgArray;
	private Button transferUsingIncrement;
	private BatchRodModel brm;
	private String imageFolderPath;
	private String rodName;
	private boolean useTrajectory = true;
	private Button useTrajectoryButton;

	public BatchDatDisplayer(Composite parent, int style, SurfaceScatterPresenter ssp, SurfaceScatterViewStart ssvs,
			BatchSetupWindow rsw, BatchRodModel brm) {

		super(parent, style);

		this.createContents();
		this.ssp = ssp;
		this.ssvs = ssvs;
		this.brm = brm;

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

		numericalDatSelection = new Group(left, SWT.NONE);
		GridLayout numericalDatSelectionLayout = new GridLayout(2, true);
		numericalDatSelection.setLayout(numericalDatSelectionLayout);
		GridData numericalDatSelectionData = new GridData(SWT.FILL, SWT.NULL, true, false);
		numericalDatSelection.setLayoutData(numericalDatSelectionData);
		numericalDatSelection.setEnabled(false);

		for (Control c : numericalDatSelection.getChildren()) {
			c.setEnabled(false);
		}

		InputTileGenerator startDat = new InputTileGenerator("Starting .Dat: ", "", numericalDatSelection, 0);
		startDat.setEnabled(false, false);
		InputTileGenerator endDat = new InputTileGenerator("Ending .Dat: ", "", numericalDatSelection, 0);
		endDat.setEnabled(false, false);
		InputTileGenerator increment = new InputTileGenerator("", "Apply Increment: ", numericalDatSelection, true);
		increment.setEnabled(false, false);

		itgArray = new InputTileGenerator[] { startDat, endDat, increment };

		increment.getText().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (increment.isStateOfText()) {
					int inc = Integer.valueOf(increment.getText().getText());
					int start = Integer.valueOf(startDat.getText().getText());

					endDat.getText().setText(String.valueOf(start + inc));
				}
			}
		});

		increment.getRadio().addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int inc = Integer.valueOf(increment.getText().getText());
				int start = Integer.valueOf(startDat.getText().getText());

				endDat.getText().setText(String.valueOf(start + inc));

				increment.setStateOfText(!increment.isStateOfText());

				increment.getText().setEnabled(true);

				increment.getRadio().setText("Apply Increment: ");

				if (increment.isStateOfText()) {
					increment.getRadio().setText("Deactivate Increment");

				}

				endDat.setEnabled(!increment.isStateOfText(), false);
			}
		});

		transferUsingIncrement = new Button(numericalDatSelection, SWT.PUSH);
		transferUsingIncrement.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		transferUsingIncrement.setText("Transfer to Rod ->");
		transferUsingIncrement.setEnabled(false);

		transferUsingIncrement.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String startDatString = startDat.getText().getText() + ".dat";
				String endDatString = endDat.getText().getText() + ".dat";

				boolean checkStart = false;
				boolean checkEnd = false;

				int startDatInt = 0;
				int endDatInt = 0;

				ArrayList<TableItem> tidiedTransferList = new ArrayList<>();

				for (int i = 0; i < folderDisplayTable.getItems().length; i++) {

					TableItem d = folderDisplayTable.getItems()[i];

					if (d.getText().equals(startDatString)) {
						startDatInt = i;
						checkStart = true;
					}

					if (d.getText().equals(endDatString)) {
						endDatInt = i;
						checkEnd = true;
					}

					if (checkEnd && checkStart) {
						break;
					}

				}

				for (int cv = 0; cv < rodDisplayTable.getItems().length; cv++) {
					rodDisplayTable.remove(cv);
				}

				rodDisplayTable.removeAll();

				rodDisplayTable.clearAll();

				for (int j = startDatInt; j <= endDatInt; j++) {
					tidiedTransferList.add(folderDisplayTable.getItems()[j]);
				}

				r = true;

				prepareToAddToRod(tidiedTransferList);

			}

		});

		rodConstruction = new Group(right, SWT.V_SCROLL | SWT.FILL | SWT.FILL);
		GridLayout rodConstructionLayout = new GridLayout(1, true);
		GridData rodConstructionData = new GridData(GridData.FILL_BOTH);
		rodConstruction.setLayout(rodConstructionLayout);
		rodConstruction.setLayoutData(rodConstructionData);
		rodConstruction.setText("Rod Construction, Tracking and Background Options");
		rodConstruction.setEnabled(false);

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

		transferToBatch = new Button(rodComponents, SWT.PUSH);
		transferToBatch.setText("Transfer To Batch");
		transferToBatch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		transferToBatch.setEnabled(false);

		transferToBatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (getParamFile() != null) {

					boolean matchingNoFrames = new FileCounter(getDatFilepaths(), getParamFile()).getGood();

					if (matchingNoFrames) {

						// evaluate number of frames

						String namePrompt = StringUtils.substringBetween(getDatFilepaths()[0], "/", ".");

						ssp.dialogToChangeRodName(namePrompt, BatchDatDisplayer.this);
						addToBatch();
					}

					else{
						StareModeSelection smsn = new StareModeSelection();
						StareModeSelector sms = new StareModeSelector(ssvs.getShell(), smsn);

						sms.open();

						try {
							while (!sms.getShell().isDisposed()) {
							
							}
						} catch (Exception p) {

						}

						if (smsn.getAccept()) {
							String namePrompt = StringUtils.substringBetween(getDatFilepaths()[0], "/", ".");

							ssp.dialogToChangeRodName(namePrompt, BatchDatDisplayer.this);
							addToBatch(true);

						}

					}
				} else {

					RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(ssvs.getShell(), 8, null);
					roobw.open();
				}
			}
		});

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

				prepareToAddToRod(tidiedTransferList);

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
			}

		});

		clearRodTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				rodDisplayTable.removeAll();
				enableRodConstruction(false);

			}
		});

		deleteSelected.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				ArrayList<String> itemsToKeepList = new ArrayList<>();

				for (TableItem ra : rodDisplayTable.getItems()) {
					if (ra.getChecked() == false) {
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

					options = dh1.getNames();
					ssp.setOptions(options);
					ssvs.populateThetaOptionsDropDown();

				} catch (Exception e2) {
					e2.printStackTrace();
				}

				if (rodDisplayTable.getItemCount() == 0) {
					enableRodConstruction(false);
					ssvs.setupRightEnabled(false);
					// bsw.setupRightEnabled(false);
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

		Group parameterFilesSelect = new Group(parameterFiles, SWT.NONE);
		GridLayout parameterFilesSelectLayout = new GridLayout(2, true);
		GridData parameterFilesSelectData = new GridData((GridData.FILL_HORIZONTAL));
		parameterFilesSelect.setLayout(parameterFilesSelectLayout);
		parameterFilesSelect.setLayoutData(parameterFilesSelectData);

		paramFileSelection = new Button(parameterFilesSelect, SWT.PUSH | SWT.FILL);
		paramFileSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		paramFileSelection.setText("Select Parameter File");
		paramFileSelection.setEnabled(false);

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

		paramFileText = new Text(parameterFilesSelect, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		paramFileText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		paramFileText.setEnabled(false);
		paramFileText.setEditable(false);

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

		InputTileGenerator useTrajectoryTile = new InputTileGenerator("Use Trajectory From File:", parameterFiles,
				true);

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

		paramFileTable.addSelectionListener(new SelectionAdapter() {

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

				} catch (Exception e1) {

					e1.printStackTrace();
				}
				return;
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
		return transferToBatch;
	}

	public void enableRodConstruction(boolean enabled) {
		rodConstruction.setEnabled(enabled);
		rodComponents.setEnabled(enabled);
		rodDisplayTable.setEnabled(enabled);
		transferToBatch.setEnabled(enabled);
		deleteSelected.setEnabled(enabled);
		clearRodTable.setEnabled(enabled);
		selectAll.setEnabled(enabled);
	}

	public Button getSelectFiles() {
		return selectFiles;
	}

	private void fillTable() {

		File folder = new File(datFolderPath);
		File[] arrayOfFiles = folder.listFiles();
		datList = new ArrayList<>();

		CharSequence dat = ".dat";

		for (int i = 0; i < arrayOfFiles.length; i++) {
			if (arrayOfFiles[i].isFile() && arrayOfFiles[i].getName().contains(dat)) {
				datList.add(arrayOfFiles[i].getName());
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
	}

	public Table getRodDisplayTable() {
		return rodDisplayTable;
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
			} else {
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
			Dataset QdcdDat;

			try {
				QdcdDat = (Dataset) qdcd.getSlice(sl);

			} catch (DatasetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

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
			// output.set(1,MethodSetting.Reflectivity_with_Flux_Correction);
			// output.set(2,MethodSetting.Reflectivity_without_Flux_Correction);
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

	private void prepareToAddToRod(ArrayList<TableItem> tidiedTransferList) {

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

		options = dh1.getNames();

		List<String> pb = Arrays.asList(options);

		while (r) {

			ild = null;

			if (pb.contains(ssp.getImageName())) {

				ild = dh1.getLazyDataset(ssp.getImageName());

				if (ild == null) {
					ssp.dialogToChangeImageFolder(promptedForImageFolder, BatchDatDisplayer.this);

					try {

						dh1 = ssp.copiedDatWithCorrectedTifs(filename, datFolderPath);
						ild = dh1.getLazyDataset(ssp.getImageName());

					} catch (Exception e2) {
						e2.printStackTrace();
						ssp.dialogToChangeImageFolder(promptedForImageFolder, BatchDatDisplayer.this);
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

				imageFolderPath = ssp.getImageFolderPath();

			}

			if (ild == null && r == true) {

				ssp.dialogToChangeImageFolder(promptedForImageFolder, BatchDatDisplayer.this);

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

			clearRodTable.setEnabled(true);
			clearParameterTable.setEnabled(true);
			rodConstruction.setEnabled(true);
			deleteSelected.setEnabled(true);
			transferToBatch.setEnabled(true);
			rodDisplayTable.setEnabled(true);
			rodComponents.setEnabled(true);
			parameterFiles.setEnabled(true);
			paramFileTable.setEnabled(true);
			paramFileSelection.setEnabled(true);
			folderDisplayTable.getVerticalBar().setEnabled(true);
			enableRodConstruction(true);

		}

	}

	private void addToBatch() {
		addToBatch(false);
	}

	private void addToBatch(boolean useStareMode) {

		BatchRodDataTransferObject brdto = new BatchRodDataTransferObject();

		brdto.setUseStareMode(useStareMode);

		String[] f = getDatFilepaths();
		brdto.setDatFiles(f);
		brdto.setImageFolderPath(imageFolderPath);

		String p = getParamFile();
		brdto.setParamFiles(p);

		brdto.setUseTrajectory(useTrajectory);

		brdto.setRodName(rodName);

		boolean good = true;

		for (BatchRodDataTransferObject b : brm.getBrdtoList()) {
			if (rodName.equals(b.getRodName())) {
				good = false;
				ssp.overlappingRodNames();
				break;
			}
		}

		if (good) {
			brm.addToBrdtoList(brdto);
		}
	}

	private String[] getDatFilepaths() {

		ArrayList<TableItem> checkedList = new ArrayList<>();

		for (TableItem d : rodDisplayTable.getItems()) {
			if (d.getChecked()) {
				checkedList.add(d);
			}
		}

		TableItem[] rodComponentDats = new TableItem[checkedList.size()];

		for (int g = 0; g < checkedList.size(); g++) {
			rodComponentDats[g] = checkedList.get(g);
		}

		String[] filepaths = new String[rodComponentDats.length];

		for (int f = 0; f < rodComponentDats.length; f++) {
			String filename = rodComponentDats[f].getText();
			filepaths[f] = datFolderPath + File.separator + filename;
		}

		return filepaths;
	}

	private String getParamFile() {

		for (TableItem jh : paramFileTable.getItems()) {
			if (jh.getChecked()) {
				return jh.getText();
			}
		}
		return null;

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

	public void setRodName(String rodName) {
		this.rodName = rodName;
	}

	public boolean getUseTrajectory() {
		return useTrajectory;
	}

	public void setUseTrajectory(boolean f) {
		this.useTrajectory = f;
	}

	public Button getUseTrajectoryButton() {
		return useTrajectoryButton;
	}

	public void setUseTrajectoryButton(Button useTrajectoryButton) {
		this.useTrajectoryButton = useTrajectoryButton;
	}

	public void setImageFolderPath(String imageFolderPath) {
		this.imageFolderPath = imageFolderPath;
	}

}
