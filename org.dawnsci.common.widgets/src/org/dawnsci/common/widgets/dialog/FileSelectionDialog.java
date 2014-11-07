package org.dawnsci.common.widgets.dialog;

import org.dawnsci.common.widgets.file.SelectorWidget;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class FileSelectionDialog extends Dialog {

	private String path;
	private boolean newFile = false;
	private boolean isFolderSelector = true;
	private boolean hasResourceButton = true;
	private String[] files = new String[] {"All Files"};
	private String[] extensions = new String[]{"*.*"};
	
	public FileSelectionDialog(Shell parentShell) {
		this(parentShell, System.getProperty("user.home"));
	}
	
	public FileSelectionDialog(Shell parentShell, String path) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.path = path;
	}
	
	  public void setPath(String path) {
		this.path = path;
	}

	@Override
	  protected Control createDialogArea(Composite parent) {
		
	    final SelectorWidget sw = new SelectorWidget(parent, isFolderSelector, hasResourceButton, files, extensions){
			
			@Override
			public void pathChanged(String path, TypedEvent event) {

					Button button = FileSelectionDialog.this.getButton(OK);
					if (button != null) {
						boolean canOK = true;
						if (isFolderSelector) canOK = this.checkDirectory(path, false);
						button.setEnabled(canOK);
						FileSelectionDialog.this.path = path;
					}
				
			}
		};
		sw.setNewFile(newFile);
		sw.setText(path);
		
	    return parent;
	  }
	  
	  public String getPath() {
		  return path;
	  }
	  

	  public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}

	public boolean isFolderSelector() {
		return isFolderSelector;
	}

	public void setFolderSelector(boolean isFolderSelector) {
		this.isFolderSelector = isFolderSelector;
	}

	public boolean hasResourceButton() {
		return hasResourceButton;
	}

	public void setHasResourceButton(boolean hasResourceButton) {
		this.hasResourceButton = hasResourceButton;
	}

	@Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    String post = isFolderSelector ? "directory" : "file";
	    newShell.setText("Please select a " + post);
	  }
	
	@Override
	  protected Point getInitialSize() {
	    return new Point(500, 180);
	  }

}
