package org.dawnsci.surfacescatter.ui;

import org.dawnsci.surfacescatter.SuperModel;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class PresenterInitialSetup extends Dialog {


	private Combo correctionsDropDown;
	private Button goButton;
	private Button imageFolderSelection;
	private Button datFolderSelection;
	private Shell parentShell; 
    private String[] filepaths;
    private int correctionSelection;
    private String imageFolderPath = null;
    private String datFolderPath = null;
    private SurfaceScatterPresenter ssp; 
	
	protected PresenterInitialSetup(Shell parentShell, 
								    String[] filepaths) {
		
		super(parentShell);

		this.parentShell = parentShell;
		this.filepaths = filepaths;
		

		setShellStyle(getShellStyle() | SWT.RESIZE);
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
	
		
//		SuperModel sm = new SuperModel();
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
//////////////////////////Group methodSetting//////////////////////////////
		
//		Group methodSetting = new Group(container, SWT.FILL);
//	    GridLayout methodSettingLayout = new GridLayout(1, true);
//		GridData methodSettingData = new GridData();
//		methodSettingData.minimumWidth = 50;
//		methodSetting.setLayout(methodSettingLayout);
//		methodSetting.setLayoutData(methodSettingData);
//		
//		Label correctionsLabel = new Label(methodSetting, SWT.FILL);
//		correctionsLabel.setText("SXRD / Reflectivity:");
//		
//		
//		correctionsDropDown = new Combo(methodSetting, SWT.DROP_DOWN | SWT.BORDER | SWT.LEFT);
//		correctionsDropDown.add("SXRD");
//		correctionsDropDown.add("Reflectivity with Flux Correction");
//		correctionsDropDown.add("Reflectivity without Flux Correction");
//		correctionsDropDown.add("Reflectivity with NO Correction");
//		correctionsDropDown.select(0);

		
///////////////////////////.dat Folder selector///////////////////////////////////////////////////
		
		datFolderSelection = new Button(container, SWT.PUSH | SWT.FILL);
		
		datFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		datFolderSelection.setText("Select .dat File Folder");
		
		datFolderSelection.addSelectionListener(new SelectionListener() {
		
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN); 
				
				String path = "p";
				
				if (fd.open() != null) {
					path = fd.getFilterPath();
				}
				
				datFolderPath = path;
				
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			
			}
		});


///////////////////////////////////////////////////////////////////////////////
		
		
		
///////////////////////////Image Folder selector///////////////////////////////////////////////////
				
		imageFolderSelection = new Button(container, SWT.PUSH | SWT.FILL);
		
		imageFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		imageFolderSelection.setText("Select Images Folder");
		
		imageFolderSelection.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog fd = new FileDialog(getParentShell(), SWT.OPEN); 
				
				String path = "p";
				
				if (fd.open() != null) {
					path = fd.getFilterPath();
				}
				
				imageFolderPath = path;
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
///////////////////////////////////////////////////////////////////////////////
		
		
		goButton = new Button(container, SWT.PUSH | SWT.FILL);
		
		goButton.setText("GO!");
		goButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		goButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				try{
//					correctionSelection = correctionsDropDown.getSelectionIndex();
//
//				}
//				catch(ArrayIndexOutOfBoundsException e1){
//					correctionSelection = 0;
// 
//				}
				
				
//				sm.setCorrectionSelection(0);

//				ssp.surfaceScatterPresenterBuild(parentShell,
//												 new String[] {null},
//												 "test",
//												 imageFolderPath,
//												 datFolderPath,
//												 correctionSelection);
				
				ssp = new SurfaceScatterPresenter();
//				ssp.setFilepaths();
				
				ssp.setImageFolderPath(imageFolderPath);
				
				SurfaceScatterViewStart ssvs = new SurfaceScatterViewStart(parentShell, 
						   filepaths, 
						   ssp.getNumberOfImages(), 
						   ssp.getImage(0),
						   ssp,
						   datFolderPath);
				
				ssp.setSsvs(ssvs);
				
				
//				ssvs.get
				
//				Control[]  ssps3cChildren = ssvs.getSsps3c().getChildren();
//				Control[]  plotSystemViewChildren = ssvs.getPlotSystemCompositeView().getChildren();
//				
////				for (Control control : ssps3cChildren){
////					try{
////						GridData data = (GridData) control.getLayoutData ();
////					}
////					catch(Exception g){
////						System.out.println("data error in ssps3c " + control.getParent().toString());
////					}
////					
////				}
////				
//				
//				for (Control control : plotSystemViewChildren){
//					try{
//						GridData data = (GridData) control.getLayoutData ();
//					}
//					catch(Exception g){
//						System.out.println("plotSystemViewChildren " + control.getParent().toString());
//					}
//					
//				}
				
				
				ssvs.open();
				
				
		}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return container;
		
	}

	
}
