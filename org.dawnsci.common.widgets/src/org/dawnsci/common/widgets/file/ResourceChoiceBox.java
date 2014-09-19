/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.file;

import java.io.File;

import org.dawnsci.common.widgets.Activator;
import org.dawnsci.common.widgets.content.FileContentProposalProvider;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ResourceChoiceBox {

	private Label  txtLabel;
	private Text   txtPath;
	private Button resourceButton;
	private Button fileButton;
	
	private boolean directory=false;
	private boolean newFile=false;
	private boolean pathEditable=false;
	private boolean buttonsEnabled=true;
	private String  path;
	private String  fileLabel;

	public ResourceChoiceBox() {
		this(null, false);
	}

	public ResourceChoiceBox(String path, boolean isDirectory) {
		setPath(path);
		setDirectory(isDirectory);
	}

	public void createContents(Composite parent) {
		
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns      = 4;
		layout.verticalSpacing = 9;
		composite.setLayout(layout);
		
 		
		this.txtLabel = new Label(composite, SWT.NULL);
		txtLabel.setText(getFileLabel()!=null ? getFileLabel() : (isDirectory() ? "&Folder  " : "&File  "));
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		this.txtPath = new Text(composite, SWT.BORDER);
		txtPath.setEditable(pathEditable);
		
		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(txtPath, new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        if (getPath()!=null) txtPath.setText(getPath());
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPath.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				path = txtPath.getText();
				pathChanged();
			}
		});

		this.resourceButton = new Button(composite, SWT.PUSH);
		resourceButton.setText("...");
		resourceButton.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
		resourceButton.setToolTipText("Browse to "+(isDirectory()?"folder":"file")+" inside a project");
		resourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleResourceBrowse();
			}
		});
		resourceButton.setEnabled(buttonsEnabled);
		
		this.fileButton = new Button(composite, SWT.PUSH);
		fileButton.setText("...");
		fileButton.setImage(Activator.getImageDescriptor("icons/data_folder_link.gif").createImage());
		fileButton.setToolTipText("Browse to an external "+(isDirectory()?"folder":"file")+".");
		fileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleFileBrowse();
			}
		});
		fileButton.setEnabled(buttonsEnabled);
	}

	
	public String getFileLabel() {
		return fileLabel;
	}

	public void setFileLabel(String fileLabel) {
		this.fileLabel = fileLabel;
		if (txtLabel!=null  && !txtLabel.isDisposed()) {
			txtLabel.setText(fileLabel);
		}
	}
	
	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isPathEditable() {
		return pathEditable;
	}

	public void setPathEditable(boolean pathEnabled) {
		this.pathEditable = pathEnabled;
		if (txtPath!=null && !txtPath.isDisposed()) {
			txtPath.setEditable(pathEnabled);
		}
	}

	

	/**
	 * Call to update, override to do custom update things.
	 */
	protected void pathChanged() {
		// TODO Auto-generated method stub
		
	}

	public String getPath() {
		return path;
	}
	
	public String getAbsoluteFilePath() {
		try{
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
			if (res!=null) return res.getLocation().toOSString();
			if (isNewFile()) { // We try for a new file
				final File file = new File(getPath());
				String parDir = file.getParent();
				IContainer folder = (IContainer)ResourcesPlugin.getWorkspace().getRoot().findMember(parDir);
				if (folder!=null) {
					final IFile newFile = folder.getFile(new Path(file.getName()));
					if (newFile.exists()) newFile.touch(null);
					return newFile.getLocation().toOSString();
				}
			}
			return getPath();
		} catch (Throwable ignored) {
			return null;
		}
	}

	public void setPath(String path) {
		this.path = path;
		if (txtPath!=null) txtPath.setText(path);
	}
	
	protected IResource getIResource() {
		IResource res = null;
		if (path!=null) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
		}
		if (res == null && getPath()!=null) {
			final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			if (getPath().startsWith(workspace)) {
				String relPath = getPath().substring(workspace.length());
				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
			}
		}
		return res;
	}
	
	public void setButtonsEnabled(boolean enabled) {
		this.buttonsEnabled = enabled;
		if (resourceButton!=null && !resourceButton.isDisposed()) {
			resourceButton.setEnabled(enabled);
		}
		if (fileButton!=null && !fileButton.isDisposed()) {
			fileButton.setEnabled(enabled);
		}
	}


	public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}
	
	

	protected void handleResourceBrowse() {
		
		IResource[] res = null;
		if (isDirectory()) {
			res = WorkspaceResourceDialog.openFolderSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
					"Directory location", "Please choose a location.", false, 
					    new Object[]{getIResource()}, null);	
			
		} else {
			if (isNewFile()) {
				final IResource cur = getIResource();
				final IPath path = cur!=null ? cur.getFullPath() : null;
			    IFile file = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
			    		                                  "File location", "Please choose a location.",
			    		                                   path, null);	
				res = file !=null ? new IResource[]{file} : null;
			} else {
			    res = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
				           "File location", "Please choose a location.", false,
				            new Object[]{getIResource()}, null);	
			}
		}
		
		
		if (res!=null && res.length>0) {
			this.path = res[0].getFullPath().toOSString();
		    txtPath.setText(this.path);
			pathChanged();
		}
	}
	
	protected void handleFileBrowse() {
		
		String path = null;
		if (isDirectory()) {
			final DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
			dialog.setText("Choose folder");
			final String filePath = getAbsoluteFilePath();
			if (filePath!=null) {
				File file = new File(filePath);
				if (file.exists()) {
					// Nothing
				} else if (file.getParentFile().exists()) {
					file = file.getParentFile();
				}
				if (file.isDirectory()) {
					dialog.setFilterPath(file.getAbsolutePath());
				} else {
					dialog.setFilterPath(file.getParent());
				}
			}
			path = dialog.open();
			
		} else {
			final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), (isNewFile()?SWT.SAVE:SWT.OPEN));
			dialog.setText("Choose file");
			final String filePath = getAbsoluteFilePath();
			if (filePath!=null) {
				final File file = new File(filePath);
				if (file.exists()) {
					if (file.isDirectory()) {
						dialog.setFilterPath(file.getAbsolutePath());
					} else {
						dialog.setFilterPath(file.getParent());
						dialog.setFileName(file.getName());
					}
				}
				
			}
			path = dialog.open();
		}
		if (path!=null) {
			setPath(path);
		    txtPath.setText(this.path);
			pathChanged();
		}
	}

	public void setResource(IResource res) {
		setPath(res.getFullPath().toOSString());
	}

}
