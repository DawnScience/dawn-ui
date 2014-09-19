/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import java.text.DecimalFormat;

import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.internal.FunctionLabelProvider;
import org.dawnsci.common.widgets.gda.function.internal.FunctionSelectionEditingSupport;
import org.dawnsci.common.widgets.gda.function.internal.FunctionTreeViewerContentProvider;
import org.dawnsci.common.widgets.gda.function.internal.IGetSetValueOnParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.ITextEditingSupport;
import org.dawnsci.common.widgets.gda.function.internal.ValueColumnEditingSupport;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;

public class FunctionTreeViewer implements IFunctionViewer {
	protected static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat(
			"##0.0####");;

	public enum COLUMN {
		FUNCTION(0), VALUE(1), LOWERLIMIT(2), UPPERLIMIT(3), FITTEDVALUE(4);
		public final int COLUMN_INDEX;

		COLUMN(int column) {
			this.COLUMN_INDEX = column;
		}
	}

	final private TreeViewer treeViewer;
	private FunctionModelRoot modelRoot;
	private ValueColumnEditingSupport[] valueColumnEditingSupport = new ValueColumnEditingSupport[COLUMN
			.values().length];
	private FunctionSelectionEditingSupport functionSelectionEditingSupport;
	private IFunctionDescriptorProvider functionDescriptorProvider;
	private ListenerList selectionChangedListeners;

	public FunctionTreeViewer(final Composite parent,
			IFunctionDescriptorProvider functionDescriptorProvider) {
		Composite composite = new Composite(parent, 0);
		composite.setLayout(new FillLayout());
		treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(
				treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
				treeViewer) {
			@Override
			protected boolean isEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				// TODO see AbstractComboBoxCellEditor for how list is made visible
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && (event.keyCode == KeyLookupFactory
								.getDefault().formalKeyLookup(
										IKeyLookup.ENTER_NAME)));
			}
		};

		TreeViewerEditor.create(treeViewer, focusCellManager, actSupport,
				ColumnViewerEditor.TABBING_HORIZONTAL
						| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
						| ColumnViewerEditor.TABBING_VERTICAL
						| ColumnViewerEditor.KEYBOARD_ACTIVATION);

		createFunctionNameColumn();
		createParameterValueColumn("Value", COLUMN.VALUE, false,
				new IGetSetValueOnParameterModel() {
					@Override
					public void setValue(ParameterModel param, String value) {
						param.setParameterValue(value);
					}

					@Override
					public double getValue(ParameterModel param) {
						return param.getParameterValue();
					}

					@Override
					public String getErrorValue(ParameterModel param) {
						return param.getParameterValueError();
					}
				});
		createParameterValueColumn("Lower Limit", COLUMN.LOWERLIMIT, true,
				new IGetSetValueOnParameterModel() {
					@Override
					public void setValue(ParameterModel param, String value) {
						param.setParameterLowerLimit(value);
					}

					@Override
					public double getValue(ParameterModel param) {
						return param.getParameterLowerLimit();
					}

					@Override
					public String getErrorValue(ParameterModel param) {
						return param.getParameterLowerLimitError();
					}
				});
		createParameterValueColumn("Upper Limit", COLUMN.UPPERLIMIT, true,
				new IGetSetValueOnParameterModel() {
					@Override
					public void setValue(ParameterModel param, String value) {
						param.setParameterUpperLimit(value);
					}

					@Override
					public double getValue(ParameterModel param) {
						return param.getParameterUpperLimit();
					}

					@Override
					public String getErrorValue(ParameterModel param) {
						return param.getParameterUpperLimitError();
					}
				});

		createFittedParamsColumn();

		treeViewer.setContentProvider(new FunctionTreeViewerContentProvider());
		treeViewer.expandToLevel(2);
		// IFunctions are not safely hashable for this use (despite their having
		// a custom hashCode)
		treeViewer.setUseHashlookup(false);
		setInput(null);
		this.functionDescriptorProvider = functionDescriptorProvider;
		functionSelectionEditingSupport
				.setFunctionDesciptorProvider(functionDescriptorProvider);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection selection = (IStructuredSelection) event
							.getSelection();
					ISelection realSelection = convertSelection(selection);
					fireSelectionChanged(new SelectionChangedEvent(
							FunctionTreeViewer.this, realSelection));
				}

			}
		});
	}

	private void createFittedParamsColumn() {
		TreeViewerColumn fittedParametersColumn = new TreeViewerColumn(
				treeViewer, SWT.NONE);
		fittedParametersColumn.getColumn().setWidth(200);
		fittedParametersColumn.getColumn().setMoveable(true);
		fittedParametersColumn.getColumn().setText("Fitted Parameters");
		fittedParametersColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof ParameterModel) {
					ParameterModel parameterModel = (ParameterModel) element;
					IParameter fittedParameter = parameterModel
							.getFittedParameter();
					if (fittedParameter != null) {
						return DOUBLE_FORMAT.format(fittedParameter.getValue());
					} else {
						return "Not defined";
					}

				} else {
					return "";
				}
			}

		});
	}

	private static final class ParameterValueColumnLabelProvider extends
			BaseLabelProvider implements IStyledLabelProvider {
		private static StyledString FIXED = new StyledString("(Fixed)",
				StyledString.QUALIFIER_STYLER);
		private boolean hideValueOnFixed;
		private IGetSetValueOnParameterModel getSetParameterModel;

		public ParameterValueColumnLabelProvider(boolean hideValueOnFixed,
				IGetSetValueOnParameterModel getSetParameterModel) {
			this.hideValueOnFixed = hideValueOnFixed;
			this.getSetParameterModel = getSetParameterModel;
		}

		@Override
		public StyledString getStyledText(Object element) {
			if (element instanceof ParameterModel) {
				ParameterModel param = (ParameterModel) element;
				StyledString styledString;
				String errorValue = getSetParameterModel.getErrorValue(param);
				if (hideValueOnFixed && param.isParameterFixed()) {
					styledString = FIXED;
				} else if (errorValue != null) {
					styledString = new StyledString(errorValue,
							FunctionLabelProvider.ERROR_STYLER);
				} else {
					double value = getSetParameterModel.getValue(param);
					if (value == Double.MAX_VALUE)
						styledString = new StyledString("Max Double",
								StyledString.QUALIFIER_STYLER);
					else if (value == -Double.MAX_VALUE)
						styledString = new StyledString("Min Double",
								StyledString.QUALIFIER_STYLER);
					else {
						styledString = new StyledString(
								DOUBLE_FORMAT.format(value));
					}
					if (param.getParameter().isFixed()) {
						styledString.append("   ");
						styledString.append(FIXED);
					}
				}
				return styledString;
			} else {
				return new StyledString();
			}
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}

	private void createParameterValueColumn(String columnTitle, COLUMN column,
			boolean hideValueOnFixed,
			final IGetSetValueOnParameterModel getSetParameterModel) {
		TreeViewerColumn valueColumn = new TreeViewerColumn(treeViewer,
				SWT.NONE);
		valueColumn.getColumn().setWidth(100);
		valueColumn.getColumn().setMoveable(true);
		valueColumn.getColumn().setText(columnTitle);
		valueColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ParameterValueColumnLabelProvider(hideValueOnFixed,
						getSetParameterModel)));
		valueColumnEditingSupport[column.COLUMN_INDEX] = new ValueColumnEditingSupport(
				this) {

			@Override
			public double getValue(ParameterModel param) {
				return getSetParameterModel.getValue(param);
			}

			@Override
			public void setValue(ParameterModel param, String value) {
				getSetParameterModel.setValue(param, value);
			}

			@Override
			public String getErrorValue(ParameterModel param) {
				return getSetParameterModel.getErrorValue(param);
			}

		};
		valueColumn
				.setEditingSupport(valueColumnEditingSupport[column.COLUMN_INDEX]);
	}

	private void createFunctionNameColumn() {
		TreeViewerColumn functionNameColumn = new TreeViewerColumn(treeViewer,
				SWT.NONE);
		functionNameColumn.getColumn().setWidth(250);
		functionNameColumn.getColumn().setMoveable(true);
		functionNameColumn.getColumn().setText("Function Name");
		functionNameColumn
				.setLabelProvider(new DelegatingStyledCellLabelProvider(
						new FunctionLabelProvider()));

		functionSelectionEditingSupport = new FunctionSelectionEditingSupport(
				this);
		functionNameColumn.setEditingSupport(functionSelectionEditingSupport);
	}

	/**
	 * Returns this viewer's tree control
	 *
	 * @return Tree
	 */
	public Tree getTreeControl() {
		return treeViewer.getTree();
	}

	/**
	 * Return the Tree Viewer control
	 */
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public void setInput(CompositeFunction compositeFunction) {
		if (compositeFunction == null) {
			modelRoot = null;
		} else {
			if (modelRoot == null) {
				modelRoot = new FunctionModelRoot(compositeFunction,
						functionDescriptorProvider);
			} else {
				modelRoot.setRoot(compositeFunction);
			}
		}
		treeViewer.setInput(modelRoot);
		treeViewer.refresh();
	}

	@Override
	public void setFittedInput(CompositeFunction fittedCompositeFunction) {
		if (modelRoot == null) {
			setInput(fittedCompositeFunction);
		}

		modelRoot.setFittedRoot(fittedCompositeFunction);
		treeViewer.refresh();
	}

	/**
	 * Refreshes this viewer completely
	 */
	@Override
	public void refresh() {
		treeViewer.refresh();
	}

	public void refresh(Object element) {
		if (element == null) {
			refresh();
		}

		FunctionModelElement[] modelElements = null;
		if (element instanceof IFunction) {
			modelElements = modelRoot.getModelElement((IFunction) element);
		} else if (element instanceof IParameter) {
			modelElements = modelRoot.getParameterModel((IParameter) element,
					true);
		}
		if (modelElements == null || modelElements.length == 0) {
			refresh();
		} else {
			for (FunctionModelElement functionModelElement : modelElements) {
				treeViewer.refresh(functionModelElement, true);
			}
		}
	}

	public FunctionModelElement getSelectedFunctionModel() {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object selectedElement = structuredSelection.getFirstElement();
			if (selectedElement instanceof FunctionModelElement) {
				FunctionModelElement modelElement = (FunctionModelElement) selectedElement;
				return modelElement;
			}
		}
		return null;
	}

	@Override
	public IFunction getSelectedFunction() {
		FunctionModelElement model = getSelectedFunctionModel();
		if (model == null)
			return null;
		return model.getFunction();
	}

	@Override
	public IOperator getSelectedFunctionParent() {
		FunctionModelElement model = getSelectedFunctionModel();
		if (model == null)
			return null;
		return model.getParentOperator();
	}

	@Override
	public int getSelectedFunctionParentIndex() {
		FunctionModelElement model = getSelectedFunctionModel();
		if (model == null)
			return 0;
		return model.getFunctionIndexInParentOperator();
	}

	private ParameterModel getSelectedParameterModel() {
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection treeSelection = (ITreeSelection) selection;
			TreePath[] paths = treeSelection.getPaths();
			// we only support single selection
			if (paths.length == 1) {
				TreePath path = paths[0];
				Object lastSegment = path.getLastSegment();
				if (lastSegment instanceof ParameterModel) {
					return (ParameterModel) lastSegment;
				}
			}
		}
		return null;
	}

	@Override
	public IParameter getSelectedParameter() {
		ParameterModel parameterModel = getSelectedParameterModel();
		if (parameterModel != null) {
			return parameterModel.getParameter();
		}
		return null;
	}

	@Override
	public int getSelectedParameterIndex() {
		ParameterModel parameterModel = getSelectedParameterModel();
		if (parameterModel != null) {
			return parameterModel.getParameterIndex();
		}
		return 0;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (selectionChangedListeners == null) {
			selectionChangedListeners = new ListenerList();
		}
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (selectionChangedListeners == null) {
			return;
		}
		selectionChangedListeners.remove(listener);
	}

	protected void fireSelectionChanged(SelectionChangedEvent event) {
		if (selectionChangedListeners == null) {
			return;
		}
		for (Object listener : selectionChangedListeners.getListeners()) {
			((ISelectionChangedListener) listener).selectionChanged(event);
		}
	}

	public Composite getControl() {
		return getTreeControl();
	}


	@Override
	public void expandAll(){
		treeViewer.expandAll();
	}

	/**
	 * If function is an identity match to a function in the view, reveal and
	 * fully expand it, otherwise expand all instances equal to function in the
	 * tree
	 *
	 * @param function
	 */
	public void expandFunction(IFunction function) {
		FunctionModelElement[] elementModel = modelRoot
				.getModelElement(function);
		for (FunctionModelElement element : elementModel) {
			if (element.getFunction() == function) {
				treeViewer
						.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
				return;
			}
		}
		// no identity match, expand all equals functions
		for (FunctionModelElement element : elementModel) {
			treeViewer.expandToLevel(element, AbstractTreeViewer.ALL_LEVELS);
		}
	}

	/** Exposed for testing only */
	public ITextEditingSupport getColumnEditingSupport(COLUMN column) {
		if (column == COLUMN.FUNCTION)
			return functionSelectionEditingSupport;
		else
			return valueColumnEditingSupport[column.COLUMN_INDEX];
	}

	/** Exposed for testing only */
	public FunctionModelRoot getModelRoot() {
		return modelRoot;
	}

	@Override
	public void addModelModifiedListener(
			IModelModifiedListener modelModifiedListener) {
		modelRoot.addModelModifiedListener(modelModifiedListener);
	}

	@Override
	public void removeModelModifiedListener(
			IModelModifiedListener modelModifiedListener) {
		modelRoot.removeModelModifiedListener(modelModifiedListener);
	}

	@Override
	public ISelection getSelection() {
		return convertSelection(treeViewer.getSelection());
	}

	@Override
	public void setSelection(ISelection selection) {
		throw new UnsupportedOperationException("TODO"); // TODO
	}

	/**
	 * Convert a selection from internal model types to external equivalents
	 *
	 * @param selection
	 *            of FunctionModel/ParameterModel/etc...
	 * @return non-null selection of IFunction/IParameter
	 */
	private ISelection convertSelection(ISelection selection) {
		Object realSelection = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object element = structuredSelection.getFirstElement();
			if (element instanceof FunctionModel) {
				FunctionModel functionModel = (FunctionModel) element;
				realSelection = functionModel.getFunction();
			} else if (element instanceof OperatorModel) {
				OperatorModel operatorModel = (OperatorModel) element;
				realSelection = operatorModel.getFunction();
			} else if (element instanceof ParameterModel) {
				ParameterModel parameterModel = (ParameterModel) element;
				realSelection = parameterModel.getParameter();
			}
		}
		if (realSelection != null) {
			return new StructuredSelection(realSelection);
		} else {
			return StructuredSelection.EMPTY;
		}
	}

	@Override
	public boolean isValid() {
		return modelRoot.isValid();
	}
	@Override
	public boolean isFittedValid() {
		return modelRoot.isFittedValid();
	}

}
