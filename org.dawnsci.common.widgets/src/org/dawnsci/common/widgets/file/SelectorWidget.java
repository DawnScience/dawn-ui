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

import org.dawnsci.common.widgets.content.FileContentProposalProvider;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	private Button inputBrowse;
	private boolean isFolderSelector;
	private String[] fileExtensions;
	private String[] fileTypes;
	private String text = "";
	private String textTooltip = "";
	private String buttonTooltip = "";
	private Composite container;

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
	 * @param extensions
	 *           Array of Strings defining possible file extensions and names if isFolderSelector is False
	 */
	public SelectorWidget(Composite parent, boolean isFolderSelector, String[]... extensions) {
		this.isFolderSelector = isFolderSelector;
		this.fileTypes = extensions[0];
		this.fileExtensions = extensions[1];
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.widthHint = 150;
		container.setLayoutData(gridData);
		inputLocation = new Text(container, SWT.BORDER);
		inputLocation.setText(text);
		inputLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
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
					loadPath(inputLocation.getText(), e);
				} else {
					inputLocation.setForeground(new Color(Display.getDefault(), new RGB(255, 80, 80)));
				}
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
							inputLocation.setText(dir.getAbsolutePath());
							inputLocation.notifyListeners(SWT.Modify, null);
							loadPath(dir.getAbsolutePath(), event);
						}
					}
				}
			}
		});

		inputBrowse = new Button(container, SWT.NONE);
		inputBrowse.setText("...");
		inputBrowse.setToolTipText(buttonTooltip);
		inputBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openDialog(e);
			}
		});
	}

	public void setText(String inputText) {
		if (inputLocation != null)
			inputLocation.setText(inputText);
	}

	public String getText() {
		if (inputLocation != null)
			return inputLocation.getText();
		return null;
	}

	public void setEnabled (boolean isEnabled) {
		if (inputLocation != null)
			inputLocation.setEnabled(isEnabled);
		if (inputBrowse != null)
			inputBrowse.setEnabled(isEnabled);
	}

	private void openDialog(TypedEvent event) {
		Shell shell = Display.getDefault().getActiveShell();
		String path = inputLocation.getText();
		if (isFolderSelector) {
			DirectoryDialog dChooser = new DirectoryDialog(shell);
			dChooser.setText(inputBrowse.getToolTipText());
			dChooser.setFilterPath(inputLocation.getText());
			path = dChooser.open();
		} else {
			FileDialog fChooser = new FileDialog(Display.getDefault()
					.getActiveShell());
			fChooser.setText(inputBrowse.getToolTipText());
			fChooser.setFilterPath(inputLocation.getText());
			fChooser.setFilterNames(fileTypes);
			fChooser.setFilterExtensions(fileExtensions);
			path = fChooser.open();
		}
		if (path != null)
			inputLocation.setText(path);
		else
			path = inputLocation.getText();
		loadPath(path, event);
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
	public abstract void loadPath(String path, TypedEvent event);

	public boolean isDisposed() {
		if (inputLocation != null && !inputLocation.isDisposed()
				&& inputBrowse != null && !inputBrowse.isDisposed())
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
		if (inputBrowse != null)
			inputBrowse.setToolTipText(buttonTooltip);
	}

	public Composite getComposite() {
		return container;
	}
}
