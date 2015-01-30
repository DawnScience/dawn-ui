/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.selector;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawnsci.common.richbeans.beans.BeansFactory;
import org.dawnsci.common.richbeans.components.EventManagerDelegate;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

/**
 * A list editor that edits the beans with a user interface. Note that the beans in the bean list should have hashCode()
 * and equals() implemented correctly. Not designed to be extended in general. Instead use setEditorUI(...) to provide a
 * composite template for editing each item.
 * 
 * @author Matthew Gerring
 */
public final class VerticalListEditor extends ListEditor {

	protected TableViewer listViewer;
	protected final Button add, delete, up, down;

	private ISelectionChangedListener selectionChangedListener;
	private SelectionAdapter addListener;
	private SelectionAdapter deleteListener; 
	private SelectionAdapter upListener;
	private SelectionAdapter downListener;
    private boolean          requireSelectionPack=true;

	/**
	 * @param par
	 * @param switches
	 */
	public VerticalListEditor(final Composite par, final int switches) {

		super(par, switches, VerticalListEditor.class.getName());
		this.eventDelegate = new EventManagerDelegate(this);

		setLayout(new GridLayout(1, false));

		this.listViewer = new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		this.selectionChangedListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!VerticalListEditor.this.isOn())
					return;
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				final BeanWrapper bean = (BeanWrapper) selection.getFirstElement();
				VerticalListEditor.super.setSelectedBean(bean, true);
				if (requireSelectionPack) VerticalListEditor.this.pack(true);
			}
		};
		listViewer.addSelectionChangedListener(selectionChangedListener);

		listViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setListHeight(100); // Default

		final Composite buttonsPanel = new Composite(this, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 4;
		buttonsPanel.setLayout(gridLayout_1);
		buttonsPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		add = new Button(buttonsPanel, SWT.NONE);
		add.setImage(getImageDescriptor("add.png").createImage());
		add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		add.setText("Add");
		this.addListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!getListEditorUI().isAddAllowed(VerticalListEditor.this))
					return;

				addBean();
			}
		};
		add.addSelectionListener(addListener);

		delete = new Button(buttonsPanel, SWT.NONE);
		delete.setImage(getImageDescriptor("delete.png").createImage());
		delete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		delete.setText("Delete");
		this.deleteListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!getListEditorUI().isDeleteAllowed(VerticalListEditor.this))
					return;

				deleteBean();
			}
		};
		delete.addSelectionListener(deleteListener);

		up = new Button(buttonsPanel, SWT.ARROW);
		this.upListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!getListEditorUI().isReorderAllowed(VerticalListEditor.this))
					return;

				moveBean(-1);
			}
		};
		up.addSelectionListener(upListener);

		down = new Button(buttonsPanel, SWT.ARROW | SWT.DOWN);
		this.downListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (editorUI instanceof ListEditorUI)
					if (!((ListEditorUI) editorUI).isReorderAllowed(VerticalListEditor.this)) {
						return;
					}
				moveBean(1);
			}
		};
		down.addSelectionListener(downListener);

		final MenuManager man = new MenuManager();
		man.add(new Action("Compare", IAction.AS_CHECK_BOX) {
			private boolean show = false;

			@Override
			public void run() {
				show = !show;
				setShowAdditionalFields(show);
			}
		});
		final Menu menu = man.createContextMenu(listViewer.getControl());
		listViewer.getControl().setMenu(menu);

		// Must set this rather than subclassing (if do not
		// set to null default is this).
		editorUI = null;
	}
	
	public void setAddButtonText(String label) {
		add.setText(label);
	}
	public void setRemoveButtonText(String label) {
		delete.setText(label);
	}

	@Override
	public void dispose() {

		if (listViewer != null && !listViewer.getControl().isDisposed()) {
			listViewer.removeSelectionChangedListener(selectionChangedListener);
		}
		if (listViewer != null && !add.isDisposed())
			add.removeSelectionListener(addListener);
		if (delete != null && !delete.isDisposed())
			delete.removeSelectionListener(deleteListener);
		if (up != null && !up.isDisposed())
			up.removeSelectionListener(upListener);
		if (down != null && !down.isDisposed())
			down.removeSelectionListener(downListener);

		super.dispose();
	}

	@Override
	public void setEnabled(final boolean isEnabled) {

		super.setEnabled(isEnabled);
		listViewer.getTable().setEnabled(isEnabled);
		add.setEnabled(isEnabled);
		delete.setEnabled(isEnabled);
		up.setEnabled(isEnabled);
		down.setEnabled(isEnabled);

		if (isEnabled) {
			this.updateButtons();
		}
	}

	@Override
	public StructuredViewer getViewer() {
		return listViewer;
	}

	/**
	 * Call to programmatically press the add button
	 */
	public void addBean() {
		try {
			addBean(beanTemplate.getClass().newInstance());
		} catch (Exception ne) {
			// Internal error, it should possible to instantiate another beanTemplate
			// having done one already.
			ne.printStackTrace();
		}
	}

	/**
	 * Call to programmatically press the add button
	 * 
	 * @param bean
	 *            the contents of the new bean to insert
	 * @throws ClassCastException
	 *             is bean is not an instance of beanTemplate
	 */
	public void addBean(final Object bean) throws ClassCastException {
		addBean(bean, getSelectedIndex() + 1);
	}

	/**
	 * Call to programmatically press the add button
	 * 
	 * @param bean
	 *            the contents of the new bean to insert
	 * @param index
	 *            index in the table to place the new bean or -1 for the end of the list
	 * @throws ClassCastException
	 *             is bean is not an instance of beanTemplate
	 */
	public void addBean(final Object bean, int index) throws ClassCastException {
		if (!beanTemplate.getClass().isInstance(bean)) {
			throw new ClassCastException("Bean passed to addBean is not an instance of beanTemplate.getClass()");
		}
		final BeanWrapper wrapper = new BeanWrapper(bean);
		String wrapperName = getFreeName(wrapper, getTemplateName(), index);
		wrapper.setName(wrapperName);

		// use a default name if supplied
		updateName(wrapper);

		if (index < 0)
			beans.add(wrapper);
		else
			beans.add(index, wrapper);

		createProviders();
		listViewer.refresh();
		setSelectedBean(wrapper, true);
		listViewer.getControl().setFocus();
		updateEditingUIVisibility();
		notifyValueListeners();

	}

	/**
	 * Can be called to delete the selected bean, normally just for testing.
	 */
	public void deleteBean() {
		final Object bean = getSelectedBeanWrapper();
		int index = beans.indexOf(bean);
		beans.remove(bean);

		lastSelectionBean = null;// Stops save
		if (!beans.isEmpty()) {
			if (index > beans.size() - 1)
				index -= 1;
			if (index < 0)
				index = 0;
			setSelectedBean(beans.get(index), true);
			listViewer.getControl().setFocus();
		}
		updateEditingUIVisibility();
		notifyValueListeners();

		// Do last
		listViewer.refresh();
	}

	@Override
	protected void setSelectedBean(BeanWrapper wrapper, boolean fireListeners) {
		listViewer.setSelection(new StructuredSelection(wrapper), true);
		super.setSelectedBean(wrapper, fireListeners);
	}

	/**
	 * Called internally, publicly used in testing harness
	 * 
	 * @param moveAmount
	 */
	public void moveBean(final int moveAmount) {
		BeanWrapper bean = getSelectedBeanWrapper();
		final int index = beans.indexOf(bean);
		bean = beans.remove(index);

		final int newIndex = index + moveAmount;
		beans.add(newIndex, bean);

		lastSelectionBean = null;// Stops save
		if (!beans.isEmpty()) {
			setSelectedBean(beans.get(newIndex), true);
			listViewer.getControl().setFocus();
		}
		notifyValueListeners();

		// Do last
		listViewer.refresh();
	}

	@Override
	protected void valueChanged(ValueEvent e) throws Exception {
		super.valueChanged(e);

		if (this.getNameField() != null && this.getNameField().equalsIgnoreCase(e.getFieldName())
				|| isShowingAdditionalFields) {
			updateName(lastSelectionBean);
			listViewer.refresh(lastSelectionBean);
		}
	}

	@Override
	protected void updateButtons() {
		final int selected = getSelectedIndex();
		if (selected < 0)
			return;

		boolean reorderAllowed = getListEditorUI().isReorderAllowed(this);
		boolean addAllowed = getListEditorUI().isAddAllowed(this);
		boolean deleteAllowed = getListEditorUI().isDeleteAllowed(this);

		up.setEnabled(selected > 0 && reorderAllowed && isEnabled());
		down.setEnabled(reorderAllowed && selected < (beans.size() - 1) && isEnabled());

		if (maxItems > 0) {
			add.setEnabled(addAllowed && beans.size() < maxItems && isEnabled());
		} else {
			add.setEnabled(addAllowed && isEnabled());
		}
		if (minItems > 0) {
			delete.setEnabled(deleteAllowed && beans.size() > minItems && isEnabled());
		} else if (beans.isEmpty()) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(deleteAllowed && isEnabled());
		}
	}

	/**
	 * @return index
	 */
	@Override
	public int getSelectedIndex() {
		return listViewer.getTable().getSelectionIndex();
	}

	@Override
	public void setValue(Object value) {

		if (value==null) value = Collections.emptyList();
		super.setValue(value);
		createProviders();
		if (!listViewer.getControl().isDisposed())
			listViewer.refresh();

		try {
			if (!beans.isEmpty()) {
				setSelectedBean(beans.get(0), true);
			} else {
				if (listeners != null) {
					final BeanSelectionEvent evt = new BeanSelectionEvent(this, -1, null);
					for (BeanSelectionListener l : listeners)
						l.selectionChanged(evt);
				}
			}
			if (!listViewer.getControl().isDisposed())
				listViewer.refresh();
		} finally {
			updateEditingUIVisibility();
			notifyValueListeners();
		}
	}

	private void createProviders() {
		if (listViewer.getContentProvider() == null) {
			listViewer.setContentProvider(new BeanListProvider());
			createLabelProvider();
			if (!listViewer.getControl().isDisposed()) {
				listViewer.setInput(new Object());
			}
		}
	}

	private String[] additionalFields;
	private int[] columnWidths = new int[] { 300 };

	/**
	 * @param fields
	 */
	public void setAdditionalFields(final String[] fields) {
		this.additionalFields = fields;
	}

	/**
	 * You must set the column widths size to number of additional fields+1 or this will not work.
	 * 
	 * @param widths
	 */
	public void setColumnWidths(final int... widths) {
		this.columnWidths = widths;
	}
	private String[] columnNames;
	/**
	 * @param columnNames
	 */
	public void setColumnNames(final String... columnNames) {
		this.columnNames = columnNames;
	}


	protected boolean labelProivderAdded = false;
	protected List<TableViewerColumn> extraColumns;

	protected void createLabelProvider() {

		if (labelProivderAdded)
			return;

		ColumnViewerToolTipSupport.enableFor(listViewer, ToolTip.NO_RECREATE);

		final TableViewerColumn name = new TableViewerColumn(listViewer, SWT.NONE, 0);
		if (getNameField() != null) {
			name.getColumn().setText(BeansFactory.getFieldWithUpperCaseFirstLetter(getNameField()));
		} else {
			name.getColumn().setText("Name");
		}
		if (columnNames!=null) try {
			name.getColumn().setText(columnNames[0]);
		} catch (Throwable ignored) {}
		
		name.getColumn().setWidth(columnWidths[0]);

		name.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final BeanWrapper bean = (BeanWrapper) element;
				final String name = bean.getName();
				return name != null ? name : "null";
			}

			@Override
			public String getToolTipText(Object element) {
				return additionalFields != null ? "Right click to choose compare mode." : "";
			}
		});

		if (additionalFields != null) {
			extraColumns = new ArrayList<TableViewerColumn>(additionalFields.length);
			for (int i = 0; i < additionalFields.length; i++) {
				final String additionalField = BeansFactory.getFieldWithUpperCaseFirstLetter(additionalFields[i]);

				final TableViewerColumn col = new TableViewerColumn(listViewer, SWT.NONE, i + 1);
				extraColumns.add(col);
				col.getColumn().setText(additionalField);
				col.getColumn().setWidth(columnWidths==null ? 0 : columnWidths[i+1]);

				col.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						final BeanWrapper bean = (BeanWrapper) element;
						final Object ob = bean.getBean();
						try {
							Method method = ob.getClass().getMethod("get" + additionalField);
							Object val = method.invoke(ob);
							if (val instanceof Double && columnFormat!=null) {
								val = columnFormat.format(((Number)val).doubleValue());
							}
							return val.toString();

						} catch (Exception e) {
							return e.getMessage();
						}
					}
				});

			}
		}

		labelProivderAdded = true;
	}
	
	private NumberFormat columnFormat = new DecimalFormat("#0.###");

	public NumberFormat getColumnFormat() {
		return columnFormat;
	}

	/**
	 * 
	 * @param columnFormat DecimalFormat string e.g. #0.###
	 */
	public void setColumnFormat(String columnFormat) {
		this.columnFormat = new DecimalFormat(columnFormat);
	}

	private boolean isShowingAdditionalFields = false;

	/**
	 * @param b
	 */
	public void setShowAdditionalFields(final boolean b) {
		isShowingAdditionalFields = b;
		listViewer.getTable().setHeaderVisible(b);
		int colIndex = 1; // intentional 1 based
		if (extraColumns==null && additionalFields!=null) {
			
		}
		if (extraColumns!=null) for (TableViewerColumn col : extraColumns) {
			if (b) {
				col.getColumn().setWidth((colIndex < columnWidths.length) ? columnWidths[colIndex] : 200);
			} else {
				col.getColumn().setWidth(0);
			}
			
			if (columnNames!=null) try {
				col.getColumn().setText(columnNames[colIndex]);
			} catch (Throwable ignored) {}

			++colIndex;
		}
	}

	private class BeanListProvider implements IStructuredContentProvider {

		@Override
		@SuppressWarnings("cast")
		public Object[] getElements(Object ignored) {
			return ((List<BeanWrapper>) beans).toArray();
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
		}
	}
	public boolean isRequireSelectionPack() {
		return requireSelectionPack;
	}

	public void setRequireSelectionPack(boolean requireSelectionPack) {
		this.requireSelectionPack = requireSelectionPack;
	}

}
