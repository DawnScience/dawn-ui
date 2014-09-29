/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dawnsci.common.richbeans.internal.GridUtils;
import org.dawnsci.common.widgets.Activator;
import org.dawnsci.common.widgets.content.FileContentProposalProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * A TextCellEditor with a file path auto-complete and
 * two buttons, one to choose projects the other to choose externally.
 * 
 * Use setNewFile(...) to define a path for a new file, rather than choose an existing one (default).
 * Use setDirectory(...) to define a path for a directory (default is file).
 * 
 * @author Matthew Gerring
 *
 */
public class FileDialogCellEditor extends TextCellEditor {

	/**
	 * File choosing by default
	 */
	private boolean directory=false;
	
	/**
	 * Existing files by default.
	 */
	private boolean newFile=false;
	
	/**
	 * By defualt we edit string paths with this editor, however you may 
	 * change the class of object chosen by setting it here.
	 */
	private Class<? extends Object> clazz = String.class;

	private boolean isBrowsing = false;;

	public FileDialogCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	public FileDialogCellEditor(Composite parent) {
		super(parent);
	}
	
	protected void doSetValue(Object value) {
		
		if (value==null) return;
		if (value instanceof File)        value = ((File)value).getAbsolutePath();
		if (value instanceof IResource)   value = ((IResource)value).getLocation().toOSString();
		if (value instanceof Path)        value = ((Path)value).toAbsolutePath().toString();
		
		super.doSetValue(value);
	}
	
	protected Object doGetValue() {

		String stringValue = (String)super.doGetValue();

		if (stringValue==null || "".equals(stringValue)) return null;
		stringValue = stringValue.trim();

		if (IResource.class.isAssignableFrom(clazz))  return getIResource();
		if (File.class.isAssignableFrom(clazz))       return new File(stringValue);
		if (Path.class.isAssignableFrom(clazz))       return Paths.get(stringValue);
		if (String.class.isAssignableFrom(clazz))     return stringValue;

		throw new RuntimeException("Only String, java.io.File, java.nio.file.Path and IResource are allowed classes!");
	}


	protected Control createControl(Composite parent) {
		
		final Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(3, false));
		comp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridUtils.removeMargins(comp);
		
		super.createControl(comp);	
		
		// File drop down list
		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(text, new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
		
		DropTarget target = new DropTarget(text, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		final FileTransfer fileTransfer = FileTransfer.getInstance();
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer};
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			 public void drop(DropTargetEvent event) {		
				 if (textTransfer.isSupportedType(event.currentDataType)) {
					 String txt = (String)event.data;
					 text.setText(txt);
				 }
				 if (fileTransfer.isSupportedType(event.currentDataType)){
					 String[] files = (String[])event.data;
					 text.setText(files[0]);
				 }
			 }
		});
		
		// Couple of buttons
		final ToolBarManager toolbar = new ToolBarManager(SWT.FLAT);
		
		IAction resourceButton = new Action("Browse to "+(isDirectory()?"folder":"file")+" inside a project", Activator.getImageDescriptor("icons/Project-data.png")) {
            public void run() {
				handleResourceBrowse();
            }
		};
		toolbar.add(resourceButton);

		IAction fileButton = new Action("Browse to an external "+(isDirectory()?"folder":"file")+".", Activator.getImageDescriptor("icons/folder.png")) {
            public void run() {
				handleFileBrowse();
            }
		};
		toolbar.add(fileButton);
		
		Control tb = toolbar.createControl(comp);
		tb.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		return comp;
	}
	
	protected void focusLost() {
		if (isActivated() && !isBrowsing) {
			fireApplyEditorValue();
		}
	}

	protected void handleResourceBrowse() {
		
		try {
			isBrowsing  = true;

			IResource[] res = null;
			if (isDirectory()) {
				res = WorkspaceResourceDialog.openFolderSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						"Directory location", "Please choose a location.", false, 
						    new Object[]{getIResource()}, null);	
				
			} else {
				if (isNewFile()) {
					final IResource cur = getIResource();
					final IPath path = cur!=null ? cur.getLocation() : null;
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
				String path = res[0].getLocation().toOSString();
			    text.setText(path);
			}
		} finally {
			text.setFocus();
			isBrowsing = false;
		}
	}
	
	protected IResource getIResource() {
		IResource res = null;
		String path = text.getText();
		if (path!=null) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		}
		if (res == null && path!=null) {
			final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			if (path.startsWith(workspace)) {
				String relPath = path.substring(workspace.length());
				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
			}
		}
		return res;
	}

	
	protected void handleFileBrowse() {
		
		try {
			isBrowsing  = true;
			
			String path = null;
			if (isDirectory()) {
				final DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
				dialog.setText("Choose folder");
				final String filePath = text.getText();
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
				final String filePath = text.getText();
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
			    text.setText(path);
			}
		} finally {
			text.setFocus();
			isBrowsing  = false;
		}
	}


	public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public Class<? extends Object> getValueClass() {
		return clazz;
	}

	/**
	 * Default is String.class but may be java.io.File, IResource or
	 * java.nio.file.Path which is returned by get value instead.
	 * 
	 * @param clazz
	 */
	public void setValueClass(Class<? extends Object> clazz) {
		this.clazz = clazz;
	}

	public static boolean isEditorFor(Class<? extends Object> clazz) {
		if (IResource.class.isAssignableFrom(clazz))  return true;
		if (File.class.isAssignableFrom(clazz))       return true;
		if (Path.class.isAssignableFrom(clazz))       return true;
		return false;
	}
}
