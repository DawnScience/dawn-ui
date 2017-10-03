package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import org.dawnsci.surfacescatter.BatchRodModel;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class BatchDisplay extends Composite {

	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private BatchSetupWindow bsw;
	private Table batchDisplayTable;
	private BatchRodModel brm;
	private String nxsFolderPath;

	public BatchDisplay(Composite parent, int style, SurfaceScatterPresenter ssp, SurfaceScatterViewStart ssvs,
			BatchSetupWindow rsw, BatchRodModel brm) {

		super(parent, style);

		this.ssp = ssp;
		this.ssvs = ssvs;
		this.bsw = rsw;
		this.brm = brm;

		this.createContents();

	}

	public void createContents() {

		Group batchTableGroup = new Group(this, SWT.V_SCROLL | SWT.FILL);
		GridLayout batchTableGroupLayout = new GridLayout(1, true);
		GridData batchTableGroupData = new GridData((GridData.FILL_BOTH));
		batchTableGroup.setLayout(batchTableGroupLayout);
		batchTableGroup.setLayoutData(batchTableGroupData);
		batchTableGroup.setText("Batch");

		Group datFolders = new Group(batchTableGroup, SWT.NONE);
		GridLayout datFoldersLayout = new GridLayout(2, true);
		GridData datFoldersData = new GridData((GridData.FILL_HORIZONTAL));
		datFolders.setLayout(datFoldersLayout);
		datFolders.setLayoutData(datFoldersData);

		nxsFolderPath = null;

		Button datFolderSelection = new Button(datFolders, SWT.PUSH | SWT.FILL);

		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		datFolderSelection.setText("Select .dat File Folder");

		
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
				nxsFolderText.setEnabled(true);

			}
		});
		
		
		Button process = new Button(batchTableGroup, SWT.PUSH);
		process.setText("Build and Process Batch");
		process.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		process.setEnabled(true);
		
		process.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				BatchRunner.batchRun(brm);
			}
		});
		
		Button check = new Button(batchTableGroup, SWT.PUSH);
		check.setText("<- Check");
		check.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		check.setEnabled(true);
		
		check.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
//				bsw.checkRod();
			}
		});
		

		Button clear = new Button(batchTableGroup, SWT.PUSH);
		clear.setText("Clear Batch");
		clear.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		clear.setEnabled(true);

		clear.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				batchDisplayTable.clearAll();
				brm.getBrdtoList().clear();
			}
		});

		Button remove = new Button(batchTableGroup, SWT.PUSH);
		remove.setText("Clear Batch");
		remove.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		remove.setEnabled(true);

		remove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<String> itemsToKeepList = new ArrayList<>();

				for (TableItem ra : batchDisplayTable.getItems()) {
					if (ra.getChecked() == false) {
						itemsToKeepList.add(ra.getText());
					}
				}

				batchDisplayTable.removeAll();

				if (itemsToKeepList.size() != batchDisplayTable.getItems().length) {
					for (String ra : itemsToKeepList) {
						TableItem rat = new TableItem(batchDisplayTable, SWT.NONE);
						rat.setText(ra);
					}
				}
			}
		});

		batchDisplayTable = new Table(batchTableGroup, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		batchDisplayTable.setEnabled(true);

		GridData folderDisplayTableData = new GridData(GridData.FILL_BOTH);

		batchDisplayTable.setLayoutData(folderDisplayTableData);
		batchDisplayTable.setLayout(new GridLayout());
		batchDisplayTable.getVerticalBar().setEnabled(true);

		batchDisplayTable.getVerticalBar().setEnabled(true);
		batchDisplayTable.getVerticalBar().setIncrement(1);
		batchDisplayTable.getVerticalBar().setThumb(1);

		brm.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

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

	}
}