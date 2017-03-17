package org.dawnsci.surfacescatter.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;



public class ImageFolderChangeDialog extends Dialog {
	
	private SurfaceScatterPresenter ssp;
	private Button imageFolderSelection;
	private Button saveFolderSelection;
	private String imageFolderPath;
	private String saveFolderPath;
	private Text imageFolderText;
	private Text saveFolderText;
	private String imageName;
//	private String saveName;
	private Text imageNameText;
	private Boolean hasBeenOpenedBefore;
	private DatDisplayer dd;
//	private boolean r = true;
	
	public ImageFolderChangeDialog(Shell parentShell, 
								   SurfaceScatterPresenter ssp,
								   Boolean t,
								   DatDisplayer dd) {
		
		super(parentShell);
		this.ssp = ssp;
		this.hasBeenOpenedBefore =t;
		this.dd= dd;
		this.imageName = ssp.getImageName();
				
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		getShell().setDefaultButton(null);
		
		this.getButton(IDialogConstants.CANCEL_ID).addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				dd.setR(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		return c;
	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
		if(hasBeenOpenedBefore){
			
			Label still = new Label(container, SWT.NONE);
			GridData stillData = new GridData((GridData.FILL_HORIZONTAL));
			still.setLayoutData(stillData);
			
			still.setText("There is still an error");
		}
		
		Label explanation = new Label(container, SWT.NONE);
		GridData explanationData = new GridData((GridData.FILL_HORIZONTAL));
		explanation.setLayoutData(explanationData);
		
		explanation.setText("Image files (.tif) cannot be located. Check the column name in .dat files." +"\r" +"If the absolute paths to the .tif files in the .dat file are incorrect, please locate the images folder and provide a copy folder into which corrected"+"\r"+".dat files can be written.");
		
		InputTileGenerator tile1 = new InputTileGenerator("Column Name in .dat file:", imageName, container, 0);
		imageNameText = tile1.getText();
		
		imageNameText.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageName = imageNameText.getText();
				ssp.setImageName(imageName);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
		
		Group imageFolders = new Group(container, SWT.NONE);
		GridLayout imageFoldersLayout = new GridLayout(2, true);
		GridData imageFoldersData = new GridData((GridData.FILL_HORIZONTAL));
		imageFolders.setLayout(imageFoldersLayout);
		imageFolders.setLayoutData(imageFoldersData);
		
		imageFolderSelection = new Button(imageFolders, SWT.PUSH | SWT.FILL);

		imageFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		imageFolderSelection.setText("Select Images Folder");

		imageFolderSelection.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(), SWT.OPEN);
				
				// Set the initial filter path according
		        // to anything they've selected or typed in
				
				if(ssp.getImageFolderPath() != null){
				
					dlg.setFilterPath(ssp.getImageFolderPath());
				}
		        // Change the title bar text
		        dlg.setText(".tif image file directory");

		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		       
		        String path = dlg.open();
				
				imageFolderPath = path;		
				imageFolderText.setText(imageFolderPath);
				imageFolderText.setEnabled(true);
				ssp.setImageFolderPath(imageFolderPath);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		imageFolderText = new Text(imageFolders, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		imageFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		imageFolderText.setEnabled(false);
		imageFolderText.setEditable(false);
		
		Group saveFolders = new Group(container, SWT.NONE);
		GridLayout saveFoldersLayout = new GridLayout(2, true);
		GridData saveFoldersData = new GridData((GridData.FILL_HORIZONTAL));
		saveFolders.setLayout(saveFoldersLayout);
		saveFolders.setLayoutData(saveFoldersData);
		
		saveFolderSelection = new Button(saveFolders, SWT.PUSH | SWT.FILL);

		saveFolderSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		saveFolderSelection.setText("Select Copy Folder");

		saveFolderSelection.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				DirectoryDialog dlg = new DirectoryDialog(parent.getShell(), SWT.OPEN);
				
				// Set the initial filter path according
		        // to anything they've selected or typed in
				
				if(ssp.getSaveFolder() != null){
				
					dlg.setFilterPath(ssp.getSaveFolder());
				}
				
		        // Change the title bar text
		        dlg.setText("Foder into which ammended .dat files are saved");

		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		       
		        String path = dlg.open();

				saveFolderPath = path;		
				saveFolderText.setText(saveFolderPath);
				saveFolderText.setEnabled(true);
				ssp.setSaveFolder(saveFolderPath);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		saveFolderText = new Text(saveFolders, SWT.SINGLE | SWT.BORDER | SWT.FILL);
		saveFolderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		saveFolderText.setEnabled(false);
		saveFolderText.setEditable(false);
		
		dd.setPromptedForImageFolder(true);
	
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select Images Folder");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle rect = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		int h = rect.height;
		int w = rect.width;
		
		return new Point((int) Math.round(0.6*w), (int) Math.round(0.6*h));
	}
	
	@Override
	  protected boolean isResizable() {
	    return true;
	}
	
}