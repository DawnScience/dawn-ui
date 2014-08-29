/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
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
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Selector widget made of a SWT Text with drag and drop listeners and content proposal and SWT Button
 * which opens a Directory or file dialog
 * The method loadPath(String) needs to be implemented and whatever action needs to be done once a path 
 * is loaded can be put in it.
 * 
 * @author wqk87977
 *
 */
public abstract class SelectorWidget {

	private static final Logger logger = LoggerFactory.getLogger(SelectorWidget.class);
	private Text inputLocation;
	private boolean isFolderSelector;
	private String[] fileExtensions;
	private String[] fileTypes;
	private String path = "";
	private String textTooltip = "";
	private String buttonTooltip = "";
	private Composite container;
	private Button fileButton;
	private Button resourceButton;
	private boolean newFile;
	private Label label;

	/**
	 * 
	 * @param parent
	 */
	public SelectorWidget(Composite parent) {
		this(parent, true, new String[] {""}, new String[] {""});
	}

	/**
	 * 
	 * @param parent
	 *           parent composite
	 * @param isFolderSelector
	 *           if True, the button will be a Folder selector, if False, a File selector
	 * @param hasResourceButton
	 *           if True, the widget will also have a Project type of link, if False only 
	 *           the button opening the external file dialog will be there.
	 */
	public SelectorWidget(Composite parent, boolean isFolderSelector, boolean hasResourceButton) {
		this(parent, isFolderSelector, hasResourceButton, new String[] {"All Files"}, new String[] {"*.*"});
	}

	/**
	 * 
	 * @param parent
	 *           parent composite
	 * @param isFolderSelector
	 *           if True, the button will be a Folder selector, if False, a File selector
	 * @param extensions
	 *           Array of Strings defining possible file extensions and names if isFolderSelector is False
	 */
	public SelectorWidget(Composite parent, boolean isFolderSelector, String[]... extensions) {
		this(parent, isFolderSelector, true, new String[] {"All Files"}, new String[] {"*.*"});
	}

	/**
	 * 
	 * @param parent
	 *           parent composite
	 * @param isFolderSelector
	 *           if True, the button will be a Folder selector, if False, a File selector
	 * @param hasResourceButton
	 *           if True, the widget will also have a Project type of link, if False only 
	 *           the button opening the external file dialog will be there.
	 * @param extensions
	 *           Array of Strings defining possible file extensions and names if isFolderSelector is False
	 */
	public SelectorWidget(Composite parent, boolean isFolderSelector, boolean hasResourceButton, String[]... extensions) {
		this.isFolderSelector = isFolderSelector;
		if (extensions != null && extensions.length == 2) {
			this.fileTypes = extensions[0];
			this.fileExtensions = extensions[1];
		} else {
			this.fileTypes = new String[] {"All Files"};
			this.fileExtensions = new String[] {"*.*"};
		}
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		if (hasResourceButton)
			layout.numColumns = 4;
		else
			layout.numColumns = 3;
		container.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		container.setLayoutData(gridData);

		label = new Label(container, SWT.NONE);
		label.setText((isFolderSelector ? "Folder:" : "File:"));
		inputLocation = new Text(container, SWT.BORDER);
		inputLocation.setText(path);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.widthHint = 150;
		inputLocation.setLayoutData(gridData);
		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(inputLocation, new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		inputLocation.setToolTipText(textTooltip);
		inputLocation.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				File tmp = new File(inputLocation.getText());
				if ((SelectorWidget.this.isFolderSelector && tmp.isDirectory())
						|| (!SelectorWidget.this.isFolderSelector && tmp.isFile())) {
					inputLocation.setForeground(new Color(Display.getDefault(), new RGB(0, 0, 0)));
				} else {
					inputLocation.setForeground(new Color(Display.getDefault(), new RGB(255, 80, 80)));
				}
				pathChanged(inputLocation.getText(), e);
			}
		});
		DropTarget dt = new DropTarget(inputLocation, DND.DROP_MOVE| DND.DROP_DEFAULT| DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance (), FileTransfer.getInstance()});
		dt.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object data = event.data;
				if (data instanceof String[]) {
					String[] stringData = (String[]) data;
					if (stringData.length > 0) {
						File dir = new File(stringData[0]);
						if ((SelectorWidget.this.isFolderSelector && dir.isDirectory())
								|| (!SelectorWidget.this.isFolderSelector && dir.isFile())) {
							setText(dir.getAbsolutePath());
							inputLocation.notifyListeners(SWT.Modify, null);
							pathChanged(dir.getAbsolutePath(), event);
						}
					}
				}
			}
		});

		if (hasResourceButton) {
			resourceButton = new Button(container, SWT.PUSH);
			resourceButton.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
			resourceButton.setToolTipText("Browse to "+(isFolderSelector?"folder":"file")+" inside a project");
			resourceButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleResourceBrowse(e);
				}
			});
		}

		fileButton = new Button(container, SWT.PUSH);
		fileButton.setImage(Activator.getImageDescriptor("icons/folder.png").createImage());
		fileButton.setToolTipText("Browse to an external "+(isFolderSelector?"folder":"file"));
		fileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleFileBrowse(e);
			}
		});
	}

	/**
	 * 
	 * @param inputText text of the main text widget
	 */
	public void setText(String inputText) {
		this.path = inputText;
		if (inputLocation != null)
			inputLocation.setText(inputText);
	}

	/**
	 * 
	 * @param textLabel label in front of the text widget.
	 *    If none, will be defaulted to File or Folder
	 */
	public void setLabel(String textLabel) {
		if (label != null)
			label.setText(textLabel);
	}

	public String getText() {
		if (inputLocation != null)
			return inputLocation.getText();
		return null;
	}

	public void setEnabled (boolean isEnabled) {
		if (inputLocation != null)
			inputLocation.setEnabled(isEnabled);
		if (resourceButton != null)
			resourceButton.setEnabled(isEnabled);
		if (fileButton != null)
			fileButton.setEnabled(isEnabled);
	}

	/**
	 * Checks whether the path is a directory
	 * @param path
	 * @param forRead
	 * @return boolean
	 */
	public boolean checkDirectory(final String path, boolean forRead) {
		if (path == null || path.length() == 0) {
			logger.warn("No path given");
			return false;
		}
		File f = new File(path);
		if (!f.exists() || f.isFile()) {
			logger.warn("Path does not exist or is not a directory");
			return false;
		}
		return forRead ? f.canRead() : f.canWrite();
	}

	/**
	 * To be overridden with the action that needs to be run once that a path is chosen
	 * @param path
	 *          path to file or folder
	 * @param event
	 *          used to check the type of event
	 */
	public abstract void pathChanged(String path, TypedEvent event);

	public boolean isDisposed() {
		if (inputLocation != null && !inputLocation.isDisposed())
			return false;
		return true;
	}

	/**
	 * 
	 * @param textTooltip 
	 *             Tooltip of the path text field
	 */
	public void setTextToolTip(String textTooltip) {
		this.textTooltip = textTooltip;
		if (inputLocation != null)
			inputLocation.setToolTipText(textTooltip);
	}

	/**
	 * 
	 * @param buttonTooltip 
	 *             Tooltip of the browse button
	 */
	public void setButtonToolTip(String buttonTooltip) {
		this.buttonTooltip = buttonTooltip;
		if (resourceButton != null) {
			resourceButton.setToolTipText(this.buttonTooltip + " (from project location)");
		}
		if (fileButton != null) {
			fileButton.setToolTipText(this.buttonTooltip + " (from external location)");
		}
	}

	public Composite getComposite() {
		return container;
	}

	public boolean isNewFile() {
		return newFile;
	}

	public void setNewFile(boolean newFile) {
		this.newFile = newFile;
	}

	public void setEditable(boolean isEditable) {
		if (inputLocation != null)
			inputLocation.setEditable(isEditable);
	}

	private void handleResourceBrowse(TypedEvent event) {
		IResource[] res = null;
		if (isFolderSelector) {
			res = WorkspaceResourceDialog.openFolderSelection(PlatformUI
					.getWorkbench().getDisplay().getActiveShell(),
					"Directory location", "Please choose a location.", false,
					new Object[] { getIResource() }, null);
		} else {
			if (isNewFile()) {
				final IResource cur = getIResource();
				final IPath path = cur != null ? cur.getFullPath() : null;
				IFile file = WorkspaceResourceDialog.openNewFile(PlatformUI
						.getWorkbench().getDisplay().getActiveShell(),
						"File location", "Please choose a location.", path,
						null);
				res = file != null ? new IResource[] { file } : null;
			} else {
				res = WorkspaceResourceDialog.openFileSelection(PlatformUI
						.getWorkbench().getDisplay().getActiveShell(),
						"File location", "Please choose a location.", false,
						new Object[] { getIResource() }, null);
			}
		}

		if (res != null && res.length > 0) {
			setText(res[0].getLocation().toOSString());
			pathChanged(path, event);
		}
	}

	protected IResource getIResource() {
		IResource res = null;
		if (path != null) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		}
		if (res == null && path != null) {
			final String workspace = ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toOSString();
			if (path.startsWith(workspace)) {
				String relPath = path.substring(workspace.length());
				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
			}
		}
		return res;
	}

	private void handleFileBrowse(TypedEvent event) {
		String path = null;
		if (isFolderSelector) {
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
						dialog.setFilterNames(fileTypes);
						dialog.setFilterExtensions(fileExtensions);
						dialog.setFilterPath(file.getParent());
						dialog.setFileName(file.getName());
					}
				}
			}
			path = dialog.open();
		}
		if (path!=null) {
			setText(path);
			pathChanged(path, event);
		}
	}

	public String getAbsoluteFilePath() {
		try {
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(this.path);
			if (res != null)
				return res.getLocation().toOSString();
			if (isNewFile()) { // We try for a new file
				final File file = new File(this.path);
				String parDir = file.getParent();
				IContainer folder = (IContainer) ResourcesPlugin.getWorkspace()
						.getRoot().findMember(parDir);
				if (folder != null) {
					final IFile newFile = folder.getFile(new Path(file.getName()));
					if (newFile.exists())
						newFile.touch(null);
					return newFile.getLocation().toOSString();
				}
			}
			return path;
		} catch (Throwable ignored) {
			return null;
		}
	}

	public void setVisible(boolean isVisible) {
		if (inputLocation != null && !inputLocation.isDisposed())
			inputLocation.setVisible(isVisible);
		if (resourceButton != null && !resourceButton.isDisposed())
			resourceButton.setVisible(isVisible);
		if (fileButton != null && !fileButton.isDisposed())
			fileButton.setVisible(isVisible);
	}
}
