/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer.COLUMN;
import org.dawnsci.common.widgets.gda.function.descriptors.CustomFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.handlers.CopyHandler;
import org.dawnsci.common.widgets.gda.function.handlers.DeleteHandler;
import org.dawnsci.common.widgets.gda.function.handlers.NewFunctionHandler;
import org.dawnsci.common.widgets.gda.function.handlers.PasteHandler;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.expressions.IExpressionService;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Subtract;

public class FunctionTreeViewerHandlersExecutePluginTest extends
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
	public void before() throws Exception {
		actual = new CompositeFunction();
		gaussian = new Gaussian();
		actual.addFunction(gaussian);
		subtract = new Subtract();
		IExpressionService service = Activator.getContext().getService(
				Activator.getContext().getServiceReference(IExpressionService.class));
		jexl = new JexlExpressionFunction(service,"a+b+x");
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
	public void testDeleteSelectionGaussian() throws ExecutionException {
		DeleteHandler deleteHandler = new DeleteHandler(
				viewer.getFunctionViewer());

		setSelection(gaussian);
		deleteHandler.execute(null);

		assertTreeLooksLike(
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testDeleteSelectionSubtract() throws ExecutionException {
		DeleteHandler deleteHandler = new DeleteHandler(
				viewer.getFunctionViewer());

		setSelection(subtract);
		deleteHandler.execute(null);

		assertTreeLooksLike(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testDeleteJexl() throws ExecutionException {
		DeleteHandler deleteHandler = new DeleteHandler(
				viewer.getFunctionViewer());

		setSelection(jexl);
		deleteHandler.execute(null);

		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.SET_FUNCTION, Node.SET_FUNCTION),
				Node.ADD_NEW_FUNCTION);
	}

	// the following test runs "New" on each node in the tree to ensure that the
	// added function is in the correct place

	/**
	 * Custom NewHandler that handles the actual edit of the model custom way. I
	 * previously had the referenced tests doing "typing" but that was
	 * unreliable and timing dependent.
	 */
	private static class NewFunctionHandlerWithHookedEdit extends
			NewFunctionHandler {

		private String editingValue;

		public NewFunctionHandlerWithHookedEdit(IFunctionViewer viewer,
				String editingValue) {
			super(viewer);
			this.editingValue = editingValue;
		}

		@Override
		protected void editModel(FunctionModelElement functionModelElement) {
			assertTrue(functionModelElement instanceof SetFunctionModel
					|| functionModelElement instanceof AddNewFunctionModel);
			assertTrue(functionModelElement.canEdit());
			functionModelElement.setEditingValue(editingValue);
			viewer.refresh(true);
		}

	}

	@Test
	public void testNewSelectionGaussian() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(gaussian);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionGaussian0() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(gaussian, 0);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionGaussian1() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(gaussian, 1);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionGaussian2() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(gaussian, 2);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionSubtract() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(subtract);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);

		setSelection(subtract);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionJexl() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(jexl);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);

		setSelection(jexl);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionJexl0() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(jexl, 0);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);

		setSelection(jexl, 0);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionJexl1() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelection(jexl, 1);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);

		setSelection(jexl, 1);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionSet() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelectionToSet(subtract, 1);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testNewSelectionAddNew() throws ExecutionException {
		NewFunctionHandler newHandler = new NewFunctionHandlerWithHookedEdit(
				viewer.getFunctionViewer(), "Fermi");

		setSelectionToAddNew(actual);
		newHandler.execute(null);
		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	private FunctionModelElement getModelElement(IFunction function) {
		FunctionModelElement[] modelElements = ((FunctionTreeViewer) viewer
				.getFunctionViewer()).getModelRoot().getModelElement(function);
		assertEquals(1, modelElements.length);
		return modelElements[0];
	}

	private FunctionModelElement getModelElement(IFunction function,
			int parameterIndex) {
		FunctionModelElement[] modelElements = ((FunctionTreeViewer) viewer
				.getFunctionViewer()).getModelRoot().getParameterModel(
				function, parameterIndex);
		assertEquals(1, modelElements.length);
		return modelElements[0];
	}

	private void checkCopySelection(IFunction function)
			throws ExecutionException {
		CopyHandler copyHandler = new CopyHandler(viewer.getFunctionViewer());

		setSelection(function);
		copyHandler.execute(null);
		// These tests are somewhat self testing, but we rely on ModelTest to
		// make sure getModelElement is returning the correct thing
		assertEquals(getModelElement(function), getClipboardLocalSelection());
		assertEquals(getModelElement(function).toString(), getClipboardText());
	}

	private void checkCopySelection(IFunction function, int parameterIndex)
			throws ExecutionException {
		CopyHandler copyHandler = new CopyHandler(viewer.getFunctionViewer());

		setSelection(function, parameterIndex);
		copyHandler.execute(null);
		// These tests are somewhat self testing, but we rely on ModelTest to
		// make sure getModelElement is returning the correct thing
		assertEquals(getModelElement(function, parameterIndex),
				getClipboardLocalSelection());
		assertEquals(getModelElement(function, parameterIndex).toString(),
				getClipboardText());
	}

	@Test
	public void testCopySelectionGaussian() throws ExecutionException {
		checkCopySelection(gaussian);
	}

	@Test
	public void testCopySelectionGaussian0() throws ExecutionException {
		checkCopySelection(gaussian, 0);
	}

	@Test
	public void testCopySelectionGaussian1() throws ExecutionException {
		checkCopySelection(gaussian, 1);
	}

	@Test
	public void testCopySelectionGaussian2() throws ExecutionException {
		checkCopySelection(gaussian, 2);
	}

	@Test
	public void testCopySelectionSubtract() throws ExecutionException {
		checkCopySelection(subtract);
	}

	@Test
	public void testCopySelectionJexl() throws ExecutionException {
		checkCopySelection(jexl);
	}

	@Test
	public void testCopySelectionJexl0() throws ExecutionException {
		checkCopySelection(jexl, 0);
	}

	@Test
	public void testCopySelectionJexl1() throws ExecutionException {
		checkCopySelection(jexl, 1);
	}

	@Test
	public void testPasteIFunction() {

		setClipboard(new Gaussian());

		// TODO: It would be nice if we could paste IFunction
	}

	@Test
	public void testPasteIParameter() {

		setClipboard(new Parameter());

		// TODO: It would be nice if we could paste IParameter
	}

	@Test
	public void testPasteParameterModel() throws ExecutionException {
		Fermi fermi = new Fermi();
		fermi.getParameter(0).setValue(123.4);
		setClipboard(new ParameterModel(mock(FunctionModelRoot.class), null,
				fermi, 0));

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		setSelection(gaussian, 0);
		pasteHandler.execute(null);
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(123.4),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
	}

	@Test
	public void testPasteParameterModelLinkedDestination()
			throws ExecutionException {
		Fermi fermi = new Fermi();
		fermi.getParameter(0).setValue(123.4);
		setClipboard(new ParameterModel(mock(FunctionModelRoot.class), null,
				fermi, 0));

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		gaussian.setParameter(0, gaussian.getParameter(1));
		viewer.refresh();

		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));
		setSelection(gaussian, 0);
		pasteHandler.execute(null);
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(123.4),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(123.4),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));
	}

	@Test
	public void testPasteFunctionModelSelectionAddNew()
			throws ExecutionException {
		FunctionModel mockFunctionModel = mock(FunctionModel.class);
		when(mockFunctionModel.getFunction()).thenReturn(new Fermi());
		setClipboard(mockFunctionModel);

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		setSelectionToAddNew(actual);
		pasteHandler.execute(null);

		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.SET_FUNCTION),
				Node.FERMI, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testPasteFunctionModelSelectionSet() throws ExecutionException {
		FunctionModel mockFunctionModel = mock(FunctionModel.class);
		when(mockFunctionModel.getFunction()).thenReturn(new Fermi());
		setClipboard(mockFunctionModel);

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		setSelectionToSet(subtract, 1);
		pasteHandler.execute(null);

		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testPasteFunctionLinkedModelSelectionSet()
			throws ExecutionException {
		FunctionModel mockFunctionModel = mock(FunctionModel.class);
		when(mockFunctionModel.getFunction()).thenReturn(new Fermi());
		setClipboard(mockFunctionModel);

		PasteHandler pasteHandler = new PasteHandler(viewer.getFunctionViewer());

		// duplicate the subtract in the composite function
		actual.addFunction(subtract);
		viewer.refresh();

		FunctionTreeViewer functionTreeViewer = getFunctionTreeViewer();
		FunctionModelRoot modelRoot = functionTreeViewer.getModelRoot();
		SetFunctionModel[] setFunctionModel = modelRoot.getSetFunctionModel(
				subtract, 1);
		assertEquals(2, setFunctionModel.length);
		assertEquals(setFunctionModel[0].getFunction(),
				setFunctionModel[1].getFunction());
		setSelection(setFunctionModel[0]);
		pasteHandler.execute(null);

		assertTreeLooksLike(Node.GAUSSIAN,
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.SUBTACT(Node.JEXL("a+b+x", "a", "b"), Node.FERMI),
				Node.ADD_NEW_FUNCTION);
	}

}
