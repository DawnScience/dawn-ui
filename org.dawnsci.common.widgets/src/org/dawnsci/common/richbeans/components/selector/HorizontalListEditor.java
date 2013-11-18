/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.selector;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.richbeans.components.EventManagerDelegate;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.widgets.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 * This class is not completed as yet but can be used and improved.
 */
public final class HorizontalListEditor extends ListEditor {
	
	private final TableViewer regionViewer;
	private final ISelectionChangedListener selectionChangedListener;
	private final Button add, delete, left, right;
	private SelectionAdapter addListener;
	private SelectionAdapter deleteListener;
	private SelectionAdapter leftListener;
	private SelectionAdapter rightListener;

	private int selectedIndex = 0;

	public HorizontalListEditor(Composite parent, int style) {

		super(parent, style, HorizontalListEditor.class.getName());
		this.eventDelegate = new EventManagerDelegate(this);

		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.regionViewer = new TableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		regionViewer.getTable().setHeaderVisible(false);
		regionViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return new Object[] { new Object() }; // One object then the columns are the beans.
			}
		});
		regionViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				if (!HorizontalListEditor.this.isOn())
					return false;
				if ("Spacer".equals(property))
					return false;
				selectedIndex = Integer.parseInt(property);
				final BeanWrapper bean = beans.get(selectedIndex);
				setSelectedBean(bean, false);

				regionViewer.refresh();

				if (listeners != null) {
					final BeanSelectionEvent evt = new BeanSelectionEvent(this, selectedIndex, bean.getBean());
					for (BeanSelectionListener l : listeners)
						l.selectionChanged(evt);
				}

				return false;
			}

			@Override
			public Object getValue(Object element, String property) {
				return null;
			}

			@Override
			public void modify(Object item, String property, Object value) {
			}
		});

		this.selectionChangedListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!HorizontalListEditor.this.isOn())
					return;
				final BeanWrapper bean = getSelectedBeanWrapper();
				setSelectedBean(bean, true);
			}
		};
		regionViewer.addSelectionChangedListener(selectionChangedListener);

		regionViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		final Composite buttonsPanel = new Composite(this, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		buttonsPanel.setLayout(gridLayout_1);
		buttonsPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		add = new Button(buttonsPanel, SWT.NONE);
		add.setImage(Activator.getImage("icons/add.png"));
		GridData addGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
		addGD.horizontalSpan = 2;
		add.setLayoutData(addGD);
		add.setText("Add");
		this.addListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addBean();
			}
		};
		add.addSelectionListener(addListener);

		delete = new Button(buttonsPanel, SWT.NONE);
		delete.setImage(Activator.getImage("icons/delete.png"));
		GridData deleteGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
		deleteGD.horizontalSpan = 2;
		delete.setLayoutData(deleteGD);
		delete.setText("Delete");
		this.deleteListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteBean();
			}
		};
		delete.addSelectionListener(deleteListener);

		left = new Button(buttonsPanel, SWT.ARROW | SWT.LEFT);
		left.setText("button");
		this.leftListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!getListEditorUI().isReorderAllowed(HorizontalListEditor.this))
					return;

				moveBean(-1);
			}
		};
		left.addSelectionListener(leftListener);

		right = new Button(buttonsPanel, SWT.ARROW | SWT.RIGHT);
		right.setText("button");
		this.rightListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (editorUI instanceof ListEditorUI)
					if (!((ListEditorUI) editorUI).isReorderAllowed(HorizontalListEditor.this)) {
						return;
					}
				moveBean(1);
			}
		};
		right.addSelectionListener(rightListener);

		final MenuManager man = new MenuManager();
		man.add(new Action("Add", Activator.getImageDescriptor("icons/add.png")) {
			@Override
			public void run() {
				addBean();
			}
		});
		man.add(new Action("Delete", Activator.getImageDescriptor("icons/delete.png")) {
			@Override
			public void run() {
				deleteBean();
			}
		});
		final Menu menu = man.createContextMenu(regionViewer.getControl());
		regionViewer.getControl().setMenu(menu);

		// Must set this rather than subclassing (if do not set to null default is this).
		editorUI = null;

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
			regionViewer.getControl().setFocus();
		}
		notifyValueListeners();
		createColumns();
		// Do last
		regionViewer.refresh();
	}

	/**
	 * @return the index
	 */
	@Override
	public int getSelectedIndex() {
		return selectedIndex;
	}

	@Override
	protected BeanWrapper getSelectedBeanWrapper() {
		return beans.get(selectedIndex);
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
		if (!getListEditorUI().isAddAllowed(this))
			return;
		try {
			final BeanWrapper wrapper = new BeanWrapper(bean);
			wrapper.setName(getFreeName(wrapper, getTemplateName(), index));
			if (index < 0)
				beans.add(wrapper);
			else
				beans.add(index, wrapper);

			createColumns();

			setSelectedBean(wrapper, true);
			regionViewer.getControl().setFocus();
			updateEditingUIVisibility();
			notifyValueListeners();
		} catch (Exception ne) {
			// Internal error, it should possible to instantiate another beanTemplate
			// having done one already.
			ne.printStackTrace();
		}
	}

	/**
	 * Can be called to delete the selected bean, normally just for testing.
	 */
	public void deleteBean() {
		if (!getListEditorUI().isDeleteAllowed(this)) {
			return;
		}
		final Object bean = getSelectedBeanWrapper();
		int index = beans.indexOf(bean);
		beans.remove(bean);

		createColumns();

		lastSelectionBean = null;// Stops save
		if (!beans.isEmpty()) {
			if (index > beans.size() - 1)
				index -= 1;
			if (index < 0)
				index = 0;
			setSelectedBean(beans.get(index), true);
			regionViewer.getControl().setFocus();
		}
		updateEditingUIVisibility();
		notifyValueListeners();
	}

	@Override
	public StructuredViewer getViewer() {
		return regionViewer;
	}

	@Override
	public void dispose() {

		if (regionViewer != null && !regionViewer.getControl().isDisposed()) {
			regionViewer.removeSelectionChangedListener(selectionChangedListener);
		}
		if (regionViewer != null && !add.isDisposed())
			add.removeSelectionListener(addListener);
		if (delete != null && !delete.isDisposed())
			delete.removeSelectionListener(deleteListener);
		if (right != null && !right.isDisposed())
			right.removeSelectionListener(rightListener);
		if (left != null && !left.isDisposed())
			left.removeSelectionListener(leftListener);

		super.dispose();
	}

	@Override
	public void setEnabled(final boolean isEnabled) {

		super.setEnabled(isEnabled);
		regionViewer.getTable().setEnabled(isEnabled);
		add.setEnabled(isEnabled);
		delete.setEnabled(isEnabled);
		right.setEnabled(isEnabled);
		left.setEnabled(isEnabled);

		if (isEnabled) {
			this.updateButtons();
		}
	}

	@Override
	protected void setSelectedBean(BeanWrapper wrapper, boolean fireListeners) {
		super.setSelectedBean(wrapper, fireListeners);
		this.selectedIndex = beans.indexOf(wrapper);
		regionViewer.refresh();
	}

	@Override
	protected void valueChanged(ValueEvent e) throws Exception {
		super.valueChanged(e);

		if (this.getNameField() != null && this.getNameField().equalsIgnoreCase(e.getFieldName())) {
			updateName(lastSelectionBean);
			regionViewer.refresh();
		}
	}

	@Override
	protected void updateButtons() {
		final int selected = getSelectedIndex();
		if (selected < 0)
			return;

		boolean addAllowed = getListEditorUI().isAddAllowed(this);
		boolean deleteAllowed = getListEditorUI().isDeleteAllowed(this);

		if (maxItems > 0) {
			add.setEnabled(addAllowed && beans.size() < maxItems && isEnabled());
		} else {
			add.setEnabled(addAllowed && isEnabled());
		}

		if (beans.size() > 1 && isEnabled()) {
			right.setEnabled(true);
			left.setEnabled(true);
		} else {
			right.setEnabled(false);
			left.setEnabled(false);
		}

		if (minItems > 0) {
			delete.setEnabled(deleteAllowed && beans.size() > minItems && isEnabled());
		} else if (beans.isEmpty()) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(deleteAllowed && isEnabled());
		}
	}

	@Override
	public void setValue(Object value) {

		super.setValue(value);

		createColumns();

		if (!beans.isEmpty()) {
			selectedIndex = 0;
			setSelectedBean(beans.get(0), true);
		} else {
			if (listeners != null) {
				final BeanSelectionEvent evt = new BeanSelectionEvent(this, -1, null);
				for (BeanSelectionListener l : listeners)
					l.selectionChanged(evt);
			}
		}
		if (!regionViewer.getControl().isDisposed())
			regionViewer.refresh();
		updateEditingUIVisibility();
		notifyValueListeners();
	}

	private void createColumns() {

		ColumnViewerToolTipSupport.enableFor(regionViewer, ToolTip.NO_RECREATE);
		// Remove any previous columns
		for (; regionViewer.getTable().getColumns().length > 0;)
			regionViewer.getTable().getColumns()[0].dispose();

		final List<String> props = new ArrayList<String>(beans.size());
		for (int i = 0; i < beans.size(); i++) {

			final BeanWrapper wrap = beans.get(i);
			final TableViewerColumn col = new TableViewerColumn(regionViewer, SWT.NONE, i);
			col.getColumn().setText(" ");
			col.setLabelProvider(new BeanColumnProvider(wrap));
			col.getColumn().pack();
			col.getColumn().setWidth(150);
			props.add(String.valueOf(i));
		}

		// Add column to make up unwanted size.
		final TableViewerColumn col = new TableViewerColumn(regionViewer, SWT.NONE, beans.size());
		col.getColumn().setText(" ");
		col.getColumn().setWidth(10);
		col.setLabelProvider(new BeanColumnProvider());
		props.add("Spacer");
		regionViewer.setColumnProperties(props.toArray(new String[props.size()]));
		regionViewer.setInput(new Object());

	}

	public class BeanColumnProvider extends ColumnLabelProvider {

		private final BeanWrapper wrapper;

		public BeanColumnProvider(final BeanWrapper wrap) {
			this.wrapper = wrap;
		}

		public BeanColumnProvider() {
			this(null);
		}

		@Override
		public String getText(final Object element) {
			if (wrapper == null)
				return "";
			return wrapper.getName();
		}

		private final Color blue  = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
		private final Color white = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

		@Override
		public Color getBackground(Object element) {
			if (wrapper == null)
				return null;
			if (beans.indexOf(wrapper) == selectedIndex) {
				return blue;
			}
			return null;
		}

		@Override
		public Color getForeground(Object element) {
			if (wrapper == null)
				return null;
			if (beans.indexOf(wrapper) == selectedIndex) {
				return white;
			}
			return null;
		}
	}
}
