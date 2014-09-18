package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer.COLUMN;
import org.dawnsci.common.widgets.gda.function.internal.ITextEditingSupport;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public abstract class FunctionTreeViewerPluginTestBase extends PluginTestBase {

	/**
	 * Return the FunctionTreeViewer in use, this allows the utilities in this
	 * class to simulate GUI events on it
	 *
	 * @return
	 */
	protected abstract FunctionTreeViewer getFunctionTreeViewer();

	/**
	 * Use to edit an existing function in the tree
	 */
	protected void editFunctionGUI(IFunction selection, String textValue) {
		setSelection(selection);
		Text text = editElement(COLUMN.FUNCTION);
		text.setText(textValue);
		finishEditElement(text);
	}

	/**
	 * Finish an edit event on the given text (which was returned by editElement()
	 */
	protected void finishEditElement(Text text) {
		text.notifyListeners(SWT.DefaultSelection, new Event());
	}

	/**
	 * Use to edit set function
	 */
	protected void setFunctionGUI(IOperator selection, String textValue,
			int childFunctionIndex) {
		setSelectionToSet(selection, childFunctionIndex);
		Text text = editElement(COLUMN.FUNCTION);
		text.setText(textValue);
		finishEditElement(text);
	}

	/** Add a new function by simulating the GUI operations */
	protected void addNewFunctionGUI(IOperator selection, String textValue) {
		setSelectionToAddNew(selection);
		Text text = editElement(COLUMN.FUNCTION);
		text.setText(textValue);
		finishEditElement(text);
	}

	/** Set value column by simulating the GUI operations */
	protected void setParameterTextGUI(COLUMN column, IFunction function,
			int parameterIndex, String textValue) {
		setSelection(function, parameterIndex);
		Text text = editElement(column);
		text.setText(textValue);
		finishEditElement(text);
	}

	/**
	 * Get value column. Note that this triggers an Edit to get the editable
	 * value rather than the displayed value (i.e. "Max Double" is displayed
	 * value, "1.7976931348623157E308" is editable value)
	 */
	protected String getParameterTextGUI(COLUMN column, IFunction function,
			int parameterIndex) {
		setSelection(function, parameterIndex);
		Text text = editElement(column);
		String result = text.getText();
		finishEditElement(text);
		return result;
	}

	/**
	 * Get value column. This gets the displayed value (i.e. "Max Double" is
	 * displayed value, "1.7976931348623157E308" is editable value)
	 */
	protected String getParameterLabelGUI(COLUMN column, IFunction function,
			int parameterIndex) {
		setSelection(function, parameterIndex);
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		TreeViewer treeViewer = functionTreeViewer.getTreeViewer();
		TreeItem[] selection = treeViewer.getTree().getSelection();
		assertEquals(1, selection.length);
		String result = selection[0].getText(column.COLUMN_INDEX);
		return result;
	}

	/** Trigger edit on currently selected row for the given column */
	protected Text editElement(COLUMN column) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		TreeViewer treeViewer = functionTreeViewer.getTreeViewer();
		ISelection selection = treeViewer.getSelection();
		assertTrue("Row must already be selected!",
				selection instanceof StructuredSelection);
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		Object element = structuredSelection.getFirstElement();
		assertTrue("Selection is of unexpected type",
				element instanceof FunctionModelElement);
		treeViewer.editElement(element, column.COLUMN_INDEX);

		ITextEditingSupport support = functionTreeViewer
				.getColumnEditingSupport(column);
		Text text = (Text) support.getTextCellEditor().getControl();
		return text;
	}

	/** Simulate the user setting the selection to function */
	protected void setSelection(IFunction function) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		FunctionModelElement[] elementModel = functionTreeViewer.getModelRoot()
				.getModelElement(function);
		assertEquals(1, elementModel.length);
		setSelection(elementModel[0]);
	}

	/**
	 * Simulate the user setting the selection to the AddNewFunction child of
	 * operator
	 */
	protected void setSelectionToAddNew(IOperator operator) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		AddNewFunctionModel[] addNewFunctionModel = functionTreeViewer
				.getModelRoot().getAddNewFunctionModel(operator);
		assertEquals(1, addNewFunctionModel.length);
		setSelection(addNewFunctionModel[0]);
	}

	/**
	 * Simulate the user setting the selection to the SetFunction child of
	 * operator at index
	 */
	protected void setSelectionToSet(IOperator operator, int index) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		FunctionModelRoot modelRoot = functionTreeViewer.getModelRoot();
		SetFunctionModel[] setFunctionModel = modelRoot.getSetFunctionModel(
				operator, index);
		assertEquals(1, setFunctionModel.length);
		setSelection(setFunctionModel[0]);
	}

	/** Simulate the user setting the selection to parameter index on function */
	protected void setSelection(IFunction function, int parameterIndex) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		ParameterModel[] paramModel = functionTreeViewer.getModelRoot()
				.getParameterModel(function, parameterIndex);
		assertEquals(1, paramModel.length);
		setSelection(paramModel[0]);
	}

	/**
	 * Simulate setting the selection to the given element in the tree
	 */
	protected void setSelection(FunctionModelElement element) {
		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		expandAll();
		TreeViewer treeViewer = functionTreeViewer.getTreeViewer();
		treeViewer.setSelection(new StructuredSelection(element));
	}

	protected String toString(Node... nodes) {
		StringBuilder builder = new StringBuilder();
		for (Node node : nodes) {
			builder.append(node.toString());
			builder.append("\n");
		}
		return builder.toString();
	}

	protected void assertTreeLooksLike(Node... nodes) {
		Node[] treeLooksLike = getTreeLooksLike();
		try {
			assertArrayEquals(nodes, treeLooksLike);
		} catch (Throwable e) {
			// Only print on error
			System.out.println("expect: ");
			System.out.print(toString(nodes));
			System.out.println("actual: ");
			System.out.print(toString(treeLooksLike));
			throw e;
		}
	}

	protected void expandAll() {
		getFunctionTreeViewer().getTreeViewer().expandAll();
	}

	protected Node[] getTreeLooksLike() {
		expandAll();
		List<Node> children = new ArrayList<>();
		Tree tree = (Tree) getFunctionTreeViewer().getControl();
		for (TreeItem treeItem : tree.getItems()) {
			children.add(getTreeLooksLike(treeItem));
		}
		return children.toArray(new Node[children.size()]);
	}

	protected Node getTreeLooksLike(TreeItem ti) {
		List<Node> children = new ArrayList<>();
		for (TreeItem treeItem : ti.getItems()) {
			children.add(getTreeLooksLike(treeItem));
		}
		return new Node(ti.getText(),
				children.toArray(new Node[children.size()]));
	}

}