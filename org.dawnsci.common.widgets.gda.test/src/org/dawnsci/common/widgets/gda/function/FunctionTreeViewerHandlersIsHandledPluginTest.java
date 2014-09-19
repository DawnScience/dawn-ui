/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer.COLUMN;
import org.dawnsci.common.widgets.gda.function.descriptors.CustomFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.handlers.CopyHandler;
import org.dawnsci.common.widgets.gda.function.handlers.DeleteHandler;
import org.dawnsci.common.widgets.gda.function.handlers.NewFunctionHandler;
import org.dawnsci.common.widgets.gda.function.handlers.PasteHandler;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Subtract;

public class FunctionTreeViewerHandlersIsHandledPluginTest extends
		FunctionTreeViewerPluginTestBase {
	CustomFunctionDescriptorProvider provider = new CustomFunctionDescriptorProvider(
			new IFunction[] { new Gaussian(), new Fermi(), new Add(),
					new Subtract() }, true);
	FunctionFittingWidget viewer;
	private CompositeFunction actual;
	private Gaussian gaussian;
	private Subtract subtract;
	private JexlExpressionFunction jexl;

	@Override
	protected void createControl(Composite parent) {
		viewer = new FunctionFittingWidget(parent, provider, null);
	}

	@Before
	public void before() {
		actual = new CompositeFunction();
		gaussian = new Gaussian();
		actual.addFunction(gaussian);
		subtract = new Subtract();
		jexl = new JexlExpressionFunction("a+b+x");
		subtract.addFunction(jexl);
		actual.addFunction(subtract);
		viewer.setInput(actual);

		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.ADD_NEW_FUNCTION);
	}

	@Override
	protected FunctionTreeViewer getFunctionTreeViewer() {
		return (FunctionTreeViewer) viewer.getFunctionViewer();
	}

	@Test
	public void testDelete() {
		DeleteHandler deleteHandler = new DeleteHandler(
				viewer.getFunctionViewer());

		setSelection(gaussian);
		assertTrue(deleteHandler.isHandled());
		setSelection(gaussian, 0);
		assertFalse(deleteHandler.isHandled());
		setSelection(gaussian, 1);
		assertFalse(deleteHandler.isHandled());
		setSelectionToAddNew(actual);
		assertFalse(deleteHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertFalse(deleteHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertFalse(deleteHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(deleteHandler.isHandled());
		finishEditElement(text);
		assertFalse(deleteHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(deleteHandler.isHandled());
		finishEditElement(text);
		assertFalse(deleteHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that delete is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Delete button (key
		// mapped) would not delete the text but the whole function
		setSelection(jexl);
		assertTrue(deleteHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(deleteHandler.isHandled());
		finishEditElement(text);
		assertTrue(deleteHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(deleteHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertTrue(deleteHandler.isHandled());
	}

	@Test
	public void testNew() {
		NewFunctionHandler newHandler = new NewFunctionHandler(
				viewer.getFunctionViewer());

		setSelection(gaussian);
		assertTrue(newHandler.isHandled());
		setSelection(gaussian, 0);
		assertTrue(newHandler.isHandled());
		setSelection(gaussian, 1);
		assertTrue(newHandler.isHandled());
		setSelectionToAddNew(actual);
		assertTrue(newHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertTrue(newHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertTrue(newHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(newHandler.isHandled());
		finishEditElement(text);
		assertTrue(newHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(newHandler.isHandled());
		finishEditElement(text);
		assertTrue(newHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that insert is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Insert button (key
		// mapped) would not behave as expected
		setSelection(jexl);
		assertTrue(newHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(newHandler.isHandled());
		finishEditElement(text);
		assertTrue(newHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(newHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertTrue(newHandler.isHandled());
	}

	@Test
	public void testCopy() {
		CopyHandler copyHandler = new CopyHandler(viewer.getFunctionViewer());

		setSelection(gaussian);
		assertTrue(copyHandler.isHandled());
		setSelection(gaussian, 0);
		assertTrue(copyHandler.isHandled());
		setSelection(gaussian, 1);
		assertTrue(copyHandler.isHandled());
		setSelectionToAddNew(actual);
		assertFalse(copyHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertFalse(copyHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertTrue(copyHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(copyHandler.isHandled());
		finishEditElement(text);
		assertTrue(copyHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(copyHandler.isHandled());
		finishEditElement(text);
		assertFalse(copyHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that copy is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Ctrl-C (key
		// mapped) would not copy the text but the whole function
		setSelection(jexl);
		assertTrue(copyHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(copyHandler.isHandled());
		finishEditElement(text);
		assertTrue(copyHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(copyHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertTrue(copyHandler.isHandled());
	}

	private void checkPasteHandlerNeverEnabled() {
		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		setSelection(gaussian);
		assertFalse(pasteHandler.isHandled());
		setSelection(gaussian, 0);
		assertFalse(pasteHandler.isHandled());
		setSelection(gaussian, 1);
		assertFalse(pasteHandler.isHandled());
		setSelectionToAddNew(actual);
		assertFalse(pasteHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertFalse(pasteHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that paste is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Ctrl-V (key
		// mapped) would not paste the text but the whole function
		setSelection(jexl);
		assertFalse(pasteHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(pasteHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertFalse(pasteHandler.isHandled());
	}

	@Test
	public void testPasteEmptyClipboard() {

		clearClipboard();

		checkPasteHandlerNeverEnabled();
	}

	@Test
	public void testPasteIFunction() {

		setClipboard(new Gaussian());

		// TODO: It would be nice if we could paste IFunction
		checkPasteHandlerNeverEnabled();
	}

	@Test
	public void testPasteIParameter() {

		setClipboard(new Parameter());

		// TODO: It would be nice if we could paste IParameter
		checkPasteHandlerNeverEnabled();
	}

	@Test
	public void testPasteParameterModel() {

		Fermi fermi = new Fermi();
		fermi.getParameter(0).setValue(123.4);
		setClipboard(new ParameterModel(mock(FunctionModelRoot.class), null,
				fermi, 0));

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		setSelection(gaussian);
		assertFalse(pasteHandler.isHandled());
		setSelection(gaussian, 0);
		assertTrue(pasteHandler.isHandled());
		setSelection(gaussian, 1);
		assertTrue(pasteHandler.isHandled());
		setSelectionToAddNew(actual);
		assertFalse(pasteHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertTrue(pasteHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertTrue(pasteHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that paste is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Ctrl-V (key
		// mapped) would not paste the text but the whole function
		setSelection(jexl);
		assertFalse(pasteHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(pasteHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertFalse(pasteHandler.isHandled());
	}


	@Test
	public void testPasteFunctionModel() {
		FunctionModel mockFunctionModel = mock(FunctionModel.class);
		when(mockFunctionModel.getFunction()).thenReturn(new Fermi());
		setClipboard(mockFunctionModel);

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		setSelection(gaussian);
		assertFalse(pasteHandler.isHandled());
		setSelection(gaussian, 0);
		assertFalse(pasteHandler.isHandled());
		setSelection(gaussian, 1);
		assertFalse(pasteHandler.isHandled());
		setSelectionToAddNew(actual);
		assertTrue(pasteHandler.isHandled());
		setSelectionToSet(subtract, 1);
		assertTrue(pasteHandler.isHandled());

		// make sure we are not handling when an edit param is in progress
		setSelection(gaussian, 1);
		assertFalse(pasteHandler.isHandled());
		Text text = editElement(COLUMN.VALUE);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handling when an add is in progress
		setSelectionToAddNew(actual);
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertTrue(pasteHandler.isHandled());

		// the above active element is AddNew, make a Jexl function to make sure
		// that paste is not handled while we are editing the contents of the
		// JexlExpression. If it were to be handled, then the Ctrl-V (key
		// mapped) would not paste the text but the whole function
		setSelection(jexl);
		assertFalse(pasteHandler.isHandled());
		text = editElement(COLUMN.FUNCTION);
		assertFalse(pasteHandler.isHandled());
		finishEditElement(text);
		assertFalse(pasteHandler.isHandled());

		// make sure we are not handled when the details is selected
		setSelection(jexl);
		assertTrue(viewer.setFocusToDetails());
		assertFalse(pasteHandler.isHandled());
		// and move focus back to tree and make sure we are handled again
		assertTrue(viewer.setFocusToTreeViewer());
		assertFalse(pasteHandler.isHandled());
	}
}
