package org.dawnsci.surfacescatter.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.dawnsci.surfacescatter.BatchRodModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class BatchDisplay extends Composite {

	
	private SurfaceScatterPresenter ssp;
	private SurfaceScatterViewStart ssvs;
	private BatchSetupWindow bsw;
	private Table folderDisplayTable;
	private BatchRodModel brm;

	
	
	public BatchDisplay(Composite parent, 
			int style, 
			SurfaceScatterPresenter ssp,
			SurfaceScatterViewStart ssvs,
			BatchSetupWindow rsw,
			BatchRodModel brm) {

		super(parent, style);

		this.ssp = ssp;
		this.ssvs = ssvs;
		this.bsw = rsw;
		this.brm =brm;
		
		this.createContents();
		

	}

	public void createContents() {

		Group batchTableGroup = new Group(this, SWT.V_SCROLL | SWT.FILL);
		GridLayout batchTableGroupLayout = new GridLayout(1, true);
		GridData batchTableGroupData = new GridData((GridData.FILL_BOTH));
		batchTableGroup.setLayout(batchTableGroupLayout);
		batchTableGroup.setLayoutData(batchTableGroupData);
		batchTableGroup.setText("Batch");
			
		Button process = new Button(batchTableGroup, SWT.PUSH);
		process.setText("Build and Process Batch");
		process.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		process.setEnabled(true);		
		
		Button check = new Button(batchTableGroup, SWT.PUSH);
		check.setText("<- Check");
		check.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		check.setEnabled(true);
		
		Button clear = new Button(batchTableGroup, SWT.PUSH);
		clear.setText("Clear Batch");
		clear.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		clear.setEnabled(true);
		
		
		folderDisplayTable = new Table(batchTableGroup, SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		folderDisplayTable.setEnabled(true);

		GridData folderDisplayTableData = new GridData(GridData.FILL_BOTH);

		folderDisplayTable.setLayoutData(folderDisplayTableData);
		folderDisplayTable.setLayout(new GridLayout());
		folderDisplayTable.getVerticalBar().setEnabled(true);

		folderDisplayTable.getVerticalBar().setEnabled(true);
		folderDisplayTable.getVerticalBar().setIncrement(1);
		folderDisplayTable.getVerticalBar().setThumb(1);
		
		brm.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				ArrayList<String> checked = new ArrayList<>();
				
				
				if(folderDisplayTable.getItems().length>0){
					for(TableItem de : folderDisplayTable.getItems()){
						if(de.getChecked()){
							checked.add(de.getText()); 
						}
					}
				
				
					for(int cv = 0; cv<folderDisplayTable.getItems().length; cv++){
						folderDisplayTable.remove(cv);
					}
				
					folderDisplayTable.removeAll();	
				
				}
				
				
				if(!brm.getBrdtoList().isEmpty()){
					for (int j = 0; j < brm.getBrdtoList().size(); j++) {
						
						TableItem t = new TableItem(folderDisplayTable, SWT.NONE);
						t.setText(brm.getBrdtoList().get(j).getRodName());
						String probe = brm.getBrdtoList().get(j).getRodName();
						
						for(String g : checked){
							if(probe.equals(g)){
								t.setChecked(true); 
							}
						}	
					}	
				}
			}
		});



	}
}