package org.dawnsci.surfacescatter.ui;

import java.util.ArrayList;

import org.dawnsci.surfacescatter.GeometricParametersModel;
import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class PresenterInitialSetup extends Dialog {

	private IDataHolder dh1;
	private Combo optionsDropDown;
	private Combo correctionsDropDown;
	private Button goButton;
	private Shell parentShell; 
    private String[] filepaths;
    private String[] options;
    private int correctionSelection;
    private String xName;
	
	protected PresenterInitialSetup(Shell parentShell, 
								    String[] filepaths) {
		
		super(parentShell);

		this.parentShell = parentShell;
		this.filepaths = filepaths;
		
		try {
			dh1 = LoaderFactory.getData(filepaths[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		options = dh1.getNames();

//		this.createDialogArea(parentShell);     
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group methodSetting = new Group(container, SWT.FILL);
	    GridLayout methodSettingLayout = new GridLayout(2, true);
		GridData methodSettingData = new GridData();
		methodSettingData.minimumWidth = 50;
		methodSetting.setLayout(methodSettingLayout);
		methodSetting.setLayoutData(methodSettingData);
		
		Label correctionsLabel = new Label(methodSetting, SWT.FILL);
		correctionsLabel.setText("SXRD / Reflectivity:");
		
		Label optionsLabel = new Label(methodSetting, SWT.FILL);
		optionsLabel.setText("Scanned Parameter:");
		
		correctionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
		correctionsDropDown.add("SXRD");
		correctionsDropDown.add("Reflectivity");
	
		
		optionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
		optionsDropDown.setText(options[0]); 
		
		for (int t=0; t<options.length; t++){
			optionsDropDown.add(options[t]);
		}

		goButton = new Button(container, SWT.PUSH | SWT.FILL);
		
		goButton.setText("GO!");
		goButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		goButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try{
					correctionSelection = correctionsDropDown.getSelectionIndex();
					xName = options[optionsDropDown.getSelectionIndex()]; 
				}
				catch(ArrayIndexOutOfBoundsException e1){
					correctionSelection = 0;
					xName = options[0]; 
				}
				
				SuperModel sm = new SuperModel();
				ArrayList<GeometricParametersModel> gms = new ArrayList<GeometricParametersModel>();
				
				sm.setCorrectionSelection(correctionSelection);
				
				for(GeometricParametersModel gm : gms){
					gm.setxName(xName);
					gm.setxNameRef(xName);
				}
				
				SurfaceScatterPresenter ssp = new SurfaceScatterPresenter(parentShell,
																		  filepaths,
																		  sm,
																		  gms);
			
				
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return container;
		
	}

	
}
