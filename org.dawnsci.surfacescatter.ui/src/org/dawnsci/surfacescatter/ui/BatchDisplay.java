package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.dawnsci.surfacescatter.BatchRodDataTransferObject;
import org.dawnsci.surfacescatter.BatchRodModel;
import org.dawnsci.surfacescatter.BatchSavingAdvancedSettings;
import org.dawnsci.surfacescatter.BatchSetupMiscellaneousProperties;
import org.dawnsci.surfacescatter.SavingFormatEnum.SaveFormatSetting;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class BatchDisplay extends Composite {

	private SurfaceScatterViewStart ssvs;
	private BatchSetupWindow bsw;
	private Table batchDisplayTable;
	private BatchRodModel brm;
	private String nxsFolderPath;
	private Group batchTableGroup;
	private Group datFolders;
	private InputTileGenerator batchNameTile;

	public BatchDisplay(Composite parent, int style, SurfaceScatterViewStart ssvs, BatchSetupWindow rsw,
			BatchRodModel brm) {

		super(parent, style);

		this.ssvs = ssvs;
		this.bsw = rsw;
		this.brm = brm;

		this.createContents();

	}

	public void createContents() {

		brm.setBsas(new BatchSavingAdvancedSettings[SaveFormatSetting.values().length]);
		brm.setBsmps(new BatchSetupMiscellaneousProperties());

		batchTableGroup = new Group(this, SWT.V_SCROLL | SWT.FILL);
		GridLayout batchTableGroupLayout = new GridLayout(1, true);
		GridData batchTableGroupData = new GridData((GridData.FILL_BOTH));
		batchTableGroup.setLayout(batchTableGroupLayout);
		batchTableGroup.setLayoutData(batchTableGroupData);
		batchTableGroup.setText("Batch");

		datFolders = new Group(batchTableGroup, SWT.NONE);
		GridLayout datFoldersLayout = new GridLayout(2, true);
		GridData datFoldersData = new GridData((GridData.FILL_HORIZONTAL));
		datFolders.setLayout(datFoldersLayout);
		datFolders.setLayoutData(datFoldersData);

		nxsFolderPath = null;

		Button datFolderSelection = new Button(datFolders, SWT.PUSH | SWT.FILL);
		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		datFolderSelection.setText("Select .dat File Folder");
		datFolderSelection.setEnabled(false);

		Text nxsFolderText = new Text(datFolders, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		nxsFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nxsFolderText.setEnabled(false);
		nxsFolderText.setEditable(false);

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
				nxsFolderPath = dir;
				brm.setNxsFolderPath(nxsFolderPath);

				nxsFolderText.setText(nxsFolderPath);
				nxsFolderText.setEnabled(false);

			}
		});

	
		Button process = new Button(datFolders, SWT.PUSH);
		process.setText("Build and Process Batch");
		process.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		process.setEnabled(false);

		process.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				File file = null;
				File logFile = null;
				PrintWriter writer = null;

				if(brm.getBatchTitle() ==null || brm.getBatchTitle().isEmpty()) {
					RegionOutOfBoundsWarning r = new RegionOutOfBoundsWarning(ssvs.getShell(), 10, null);
					r.open();
				}
				
				if(brm.getNxsFolderPath() ==null ||brm.getNxsFolderPath().isEmpty()) {
					RegionOutOfBoundsWarning r = new RegionOutOfBoundsWarning(ssvs.getShell(), 11, null);
					r.open();
				}
				
				try {
					String folderPath =brm.getNxsFolderPath() + File.separator + brm.getBatchTitle(); 
					file = new File(folderPath);
					
				
					SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
					Date now = new Date();
					String strDate = sdfDate.format(now);
					
					String rightNow = StringUtils.deleteWhitespace(strDate);
					
					String logFilePath =folderPath + File.separator + brm.getBatchTitle()+rightNow; 
					logFile = new File(logFilePath);
					
					boolean b = false;
					
					if(!Files.isDirectory(Paths.get(folderPath))) {
						b = file.mkdirs();
					}
					
					b = Files.isDirectory(Paths.get(folderPath));
						 
					boolean a = logFile.createNewFile();
					
					
					if (a && b) {
						writer = new PrintWriter(logFile);

						BatchTrackingProgressAndAbortViewImproved btpaavi = new BatchTrackingProgressAndAbortViewImproved(
								ssvs.getShell(), brm, writer);
						btpaavi.open();
						try {
							while (!btpaavi.getShell().isDisposed()) {
	
							}
						}
						catch(NullPointerException f) {
							
						}
						writer.close();
						
					}

					else {
						RegionOutOfBoundsWarning roobw = new RegionOutOfBoundsWarning(ssvs.getShell(), 9, null);
						roobw.open();
					}
					
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		});

		Button advancedSettings = new Button(datFolders, SWT.PUSH);
		advancedSettings.setText("Advanced Process Settings");
		advancedSettings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		advancedSettings.setEnabled(false);

		advancedSettings.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				BatchConfigurationSettings bcs = new BatchConfigurationSettings(ssvs.getShell(), brm);
				bcs.open();
			}
		});
		
		batchNameTile = new InputTileGenerator("Set batch Name", "", batchTableGroup);

		batchNameTile.getText().addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				brm.setBatchTitle(batchNameTile.getText().getText());

			}
		});


		Button check = new Button(batchTableGroup, SWT.PUSH);
		check.setText("<- Check");
		check.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		check.setEnabled(false);

		check.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				String r = "s";

				for (TableItem ra : batchDisplayTable.getItems()) {
					if (ra.getChecked() == true) {
						r = ra.getText();
					}
				}

				BatchRodDataTransferObject bd = brm.getDTO(r);

				bsw.pushTocheck(bd);
			}
		});

		Button clear = new Button(batchTableGroup, SWT.PUSH);
		clear.setText("Clear Batch");
		clear.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		clear.setEnabled(false);

		clear.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				batchDisplayTable.removeAll();
				brm.getBrdtoList().clear();
				brm.setBatchDisplayOn(false);
			}
		});

		Button remove = new Button(batchTableGroup, SWT.PUSH);
		remove.setText("Remove Selected");
		remove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		remove.setEnabled(false);

		remove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<String> itemsToKeepList = new ArrayList<>();

				for (TableItem ra : batchDisplayTable.getItems()) {
					if (!ra.getChecked()) {
						itemsToKeepList.add(ra.getText());
					}
				}

				batchDisplayTable.removeAll();

				for (BatchRodDataTransferObject g : brm.getBrdtoList()) {
					for (String t : itemsToKeepList) {

						if (g.getRodName().equals(t)) {
							break;
						}

						brm.getBrdtoList().remove(g);
					}
				}

				if (!itemsToKeepList.isEmpty()) {
					for (String ra : itemsToKeepList) {
						TableItem rat = new TableItem(batchDisplayTable, SWT.NONE);
						rat.setText(ra);
					}
				} else {
					brm.setBatchDisplayOn(false);
				}

			}
		});

		batchDisplayTable = new Table(batchTableGroup, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		batchDisplayTable.setEnabled(false);

		GridData folderDisplayTableData = new GridData(GridData.FILL_BOTH);

		batchDisplayTable.setLayoutData(folderDisplayTableData);
		batchDisplayTable.setLayout(new GridLayout());
		batchDisplayTable.getVerticalBar().setEnabled(false);

		batchDisplayTable.getVerticalBar().setEnabled(false);
		batchDisplayTable.getVerticalBar().setIncrement(1);
		batchDisplayTable.getVerticalBar().setThumb(1);

		brm.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				BatchDisplay.this.setElementsEnabled(brm.isBatchDisplayOn());

				if (brm.isUpdateControl()) {
					ArrayList<String> checked = new ArrayList<>();

					if (batchDisplayTable.getItems().length > 0) {
						for (TableItem de : batchDisplayTable.getItems()) {
							if (de.getChecked()) {
								checked.add(de.getText());
							}
						}

						for (int cv = 0; cv < batchDisplayTable.getItems().length; cv++) {
							batchDisplayTable.remove(cv);
						}

						batchDisplayTable.removeAll();

					}

					if (!brm.getBrdtoList().isEmpty()) {
						for (int j = 0; j < brm.getBrdtoList().size(); j++) {

							TableItem t = new TableItem(batchDisplayTable, SWT.NONE);
							t.setText(brm.getBrdtoList().get(j).getRodName());
							String probe = brm.getBrdtoList().get(j).getRodName();

							for (String g : checked) {
								if (probe.equals(g)) {
									t.setChecked(true);
								}
							}
						}
					}
				}
			}
		});

		setElementsEnabled(false);
	}

	private void setElementsEnabled(boolean t) {

		batchDisplayTable.setEnabled(t);
		batchTableGroup.setEnabled(t);
		batchNameTile.setEnabled(t);

		for (Control c : batchTableGroup.getChildren()) {
			c.setEnabled(t);
		}

		for (Control c : datFolders.getChildren()) {
			c.setEnabled(t);
		}
	}
}