/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.cell;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A Chooser for XML files in a folder. It can also fire an action if the choice is not there.
 */
public class XMLChooserEditor extends CellEditor {

	private IFolder containingFolder;
	private IXMLFileListProvider[] fileListProviders;
	private CCombo comboBox;
	private List<String> names;

	/**
	 * @param parent
	 * @param containingFolder
	 * @param fileListProviders
	 */
	public XMLChooserEditor(final Composite parent, final IFolder containingFolder,
			final IXMLFileListProvider[] fileListProviders) {
		super(parent);
		this.containingFolder = containingFolder;
		this.fileListProviders = fileListProviders;
	}

	@Override
	protected Control createControl(Composite parent) {

		final Composite content = new Composite(parent, SWT.NONE);

		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.numColumns = 2;
		content.setLayout(gridLayout);

		this.comboBox = new CCombo(content, SWT.READ_ONLY);
		comboBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		comboBox.setFont(parent.getFont());

		this.keyListener = new KeyAdapter() {
			// hook key pressed - see PR 14201
			@Override
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);
			}
		};
		comboBox.addKeyListener(keyListener);

		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				applyEditorValueAndDeactivate();
			}

			@Override
			public void widgetSelected(SelectionEvent event) {
				// applyEditorValueAndDeactivate();
			}
		};
		comboBox.addSelectionListener(selectionListener);

		this.traverseListener = new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		};
		comboBox.addTraverseListener(traverseListener);

		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				XMLChooserEditor.this.focusLost();
			}
		};
		comboBox.addFocusListener(focusListener);

		// final Button button = new Button(content, SWT.NONE);
		// button.setText("..");

		return content;
	}

	private KeyAdapter keyListener;
	private SelectionAdapter selectionListener;
	private TraverseListener traverseListener;
	private FocusAdapter focusListener;
	private IResourceChangeListener resourceChangeListener;

	@Override
	public void dispose() {
		if (keyListener != null)
			comboBox.removeKeyListener(keyListener);
		if (selectionListener != null)
			comboBox.removeSelectionListener(selectionListener);
		if (traverseListener != null)
			comboBox.removeTraverseListener(traverseListener);
		if (focusListener != null)
			comboBox.removeFocusListener(focusListener);
		if (resourceChangeListener != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		super.dispose();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this <code>CellEditor</code> framework method sets the
	 * minimum width of the cell. The minimum width is 10 characters if <code>comboBox</code> is not <code>null</code>
	 * or <code>disposed</code> else it is 60 pixels to make sure the arrow button and some text is visible. The list of
	 * CCombo will be wide enough to show its longest item.
	 */
	@Override
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((comboBox == null) || comboBox.isDisposed()) {
			layoutData.minimumWidth = 60;
		} else {
			// make the comboBox 10 characters wide
			GC gc = new GC(comboBox);
			layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
			gc.dispose();
		}
		return layoutData;
	}

	@Override
	protected Object doGetValue() {
		if (comboBox != null) {
			final int sel = comboBox.getSelectionIndex();
			if (sel >= 0 && sel < getNames().size()) {
				return getNames().get(sel);
			}
		}
		return null;
	}

	@Override
	protected void doSetFocus() {
		if (comboBox != null)
			comboBox.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		if (comboBox != null) {
			comboBox.setItems(getNames().toArray(new String[getNames().size()]));
			comboBox.select(getNames().indexOf(value));
		}
	}

	private List<String> getNames() {
//		if (names == null) {
			names = new ArrayList<String>();
			try {
				if (containingFolder != null) {
					for (IXMLFileListProvider fileListProvider : fileListProviders) {
						List<IFile> sortedFileList = fileListProvider.getSortedFileList(containingFolder);
						for (IFile iFile : sortedFileList) {
							names.add(iFile.getName());
						}
					}
				}
			} catch (Exception e) {
				// NOTE: Intentionally no logging - we are in an API here
				e.printStackTrace();
			}
			
			if (resourceChangeListener == null) {
				resourceChangeListener = new IResourceChangeListener() {
					@Override
					public void resourceChanged(IResourceChangeEvent event) {
						// the contents of the workspace has changed, need to invalidate
						// current list of names
						names = null;
					}
				};
				ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
			}
//		}
		return names;
	}

	@Override
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\t') { // tab key
			applyEditorValueAndDeactivate();
		}
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		int selection = comboBox.getSelectionIndex();
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			// Only format if the 'index' is valid
			if (getNames().size() > 0 && selection >= 0 && selection < getNames().size()) {
				// try to insert the current value into the error message.
				setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { getNames().get(selection) }));
			} else {
				// Since we don't have a valid index, assume we're using an
				// 'edit'
				// combo so format using its text value
				setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { comboBox.getText() }));
			}
		}

		fireApplyEditorValue();
		deactivate();
	}

}
