/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.widgets.gda.function.descriptors.CustomFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Subtract;

public class ModelTest {

	CustomFunctionDescriptorProvider provider = new CustomFunctionDescriptorProvider(
			new IFunction[] { new Gaussian(), new Lorentzian(), new Add(),
					new Subtract() }, true);

	@Test
	public void testGetChildren_1() {
		CompositeFunction compositeFunction = new CompositeFunction();
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testGetChildren_2() {
		CompositeFunction compositeFunction = new CompositeFunction();
		compositeFunction.addFunction(new Gaussian());
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot, Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testGetChildren_3() {
		CompositeFunction compositeFunction = new CompositeFunction();
		compositeFunction.addFunction(new Gaussian());
		compositeFunction.addFunction(new Fermi());
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot, Node.GAUSSIAN, Node.FERMI,
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testGetChildren_4() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Add add = new Add();
		compositeFunction.addFunction(add);
		add.addFunction(new Gaussian());
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot,
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testGetChildren_5() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Subtract sub = new Subtract();
		compositeFunction.addFunction(sub);
		sub.addFunction(new Gaussian());
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot,
				Node.SUBTACT(Node.GAUSSIAN, Node.SET_FUNCTION),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testGetModelElement_1() {
		CompositeFunction compositeFunction = new CompositeFunction();
		compositeFunction.addFunction(new Gaussian());
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot, Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);

		FunctionModelElement[] modelElements = modelRoot
				.getModelElement(new Gaussian());
		assertTreeLooksLike(modelElements, Node.GAUSSIAN);
	}

	@Test
	public void testGetModelElement_2() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Add add = new Add();
		add.addFunction(new Gaussian());
		compositeFunction.addFunction(add);
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot,
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION),
				Node.ADD_NEW_FUNCTION);

		FunctionModelElement[] modelElements = modelRoot
				.getModelElement(new Gaussian());
		assertTreeLooksLike(modelElements, Node.GAUSSIAN);
		modelElements = modelRoot.getModelElement(add);
		assertTreeLooksLike(modelElements,
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION));
	}

	@Test
	public void testGetAddNewFunctionModel() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Add add = new Add();
		add.addFunction(new Gaussian());
		compositeFunction.addFunction(add);
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot,
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION),
				Node.ADD_NEW_FUNCTION);

		// Positive tests
		AddNewFunctionModel[] addNewFunctionModels = modelRoot
				.getAddNewFunctionModel(compositeFunction);
		assertEquals(1, addNewFunctionModels.length);
		assertTrue(compositeFunction == addNewFunctionModels[0].getParent());

		addNewFunctionModels = modelRoot.getAddNewFunctionModel(add);
		assertEquals(1, addNewFunctionModels.length);
		assertTrue(add == addNewFunctionModels[0].getParent());

		// Positive tests using non-original operator
		Add addOther = new Add();
		addOther.addFunction(new Gaussian());
		addNewFunctionModels = modelRoot.getAddNewFunctionModel(addOther);
		assertEquals(1, addNewFunctionModels.length);
		// make sure we have been returned an instance of the add we put in the
		// composite function
		assertTrue(add == addNewFunctionModels[0].getParent());

		// Failed to find tests
		assertEquals(0, modelRoot.getAddNewFunctionModel(new Add()).length);
		assertEquals(0, modelRoot.getAddNewFunctionModel(new Subtract()).length);
		assertEquals(
				0,
				modelRoot.getAddNewFunctionModel(new CompositeFunction()).length);

		// Find multiple tests
		Add add2 = new Add();
		add2.addFunction(new Gaussian());
		compositeFunction.addFunction(add2);
		assertTreeLooksLike(modelRoot,
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION),
				Node.ADD(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION),
				Node.ADD_NEW_FUNCTION);
		addNewFunctionModels = modelRoot.getAddNewFunctionModel(add);
		assertEquals(2, addNewFunctionModels.length);

		// make sure we have been returned an instance of the add we put in the
		// composite function
		assertTrue(add == addNewFunctionModels[0].getParent());
		assertTrue(add2 == addNewFunctionModels[1].getParent());
	}

	@Test
	public void testGetSetFunctionModel() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Subtract sub = new Subtract();
		sub.addFunction(new Gaussian());
		compositeFunction.addFunction(sub);
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);
		assertTreeLooksLike(modelRoot,
				Node.SUBTACT(Node.GAUSSIAN, Node.SET_FUNCTION),
				Node.ADD_NEW_FUNCTION);

		SetFunctionModel[] setFunctionModels = modelRoot.getSetFunctionModel(
				sub, 1);
		assertEquals(1, setFunctionModels.length);
		assertTrue(sub == setFunctionModels[0].getParent());
		assertEquals(1, setFunctionModels[0].getFunctionIndex());

		assertEquals(0, modelRoot.getSetFunctionModel(sub, 0).length);
		assertEquals(0, modelRoot.getSetFunctionModel(new Subtract(), 0).length);
		assertEquals(0, modelRoot.getSetFunctionModel(new Subtract(), 1).length);
		assertEquals(0, modelRoot.getSetFunctionModel(new Add(), 0).length);
	}

	@Test
	public void testModifyListenerFires() {
		CompositeFunction compositeFunction = new CompositeFunction();
		Gaussian gaussian = new Gaussian();
		compositeFunction.addFunction(gaussian);
		FunctionModelRoot modelRoot = new FunctionModelRoot(compositeFunction,
				provider);

		IModelModifiedListener listener = mock(IModelModifiedListener.class);
		modelRoot.addModelModifiedListener(listener);
		ParameterModel[] parameterModel = modelRoot.getParameterModel(gaussian,
				0);
		assertEquals(1, parameterModel.length);
		parameterModel[0].setParameterValue("456.7");
		verify(listener, times(1)).parameterModified(
				any(IParameterModifiedEvent.class));
		parameterModel[0].setParameterLowerLimit("123.4");
		verify(listener, times(2)).parameterModified(
				any(IParameterModifiedEvent.class));
		parameterModel[0].setParameterUpperLimit("890.1");
		verify(listener, times(3)).parameterModified(
				any(IParameterModifiedEvent.class));
		parameterModel[0].setParameterFixed(true);
		verify(listener, times(4)).parameterModified(
				any(IParameterModifiedEvent.class));

		// check (demonstrate!) that modifying the underlying IParameter does
		// not fire event (i.e. if you bypass the model, you don't get events)
		parameterModel[0].getParameter().setValue(123.456);
		verify(listener, times(4)).parameterModified(
				any(IParameterModifiedEvent.class));

	}

	@Test
	public void testParameterModel() {
		Gaussian gaussian = new Gaussian();
		ParameterModel parameterModel = new ParameterModel(
				createMockModelRoot(), mock(FunctionModel.class), gaussian, 0);

		parameterModel.setParameterValue("123.4");
		assertEquals(123.4, parameterModel.getParameterValue(), 0);
		assertEquals(null, parameterModel.getParameterValueError());
		parameterModel.setParameterLowerLimit("-456.7");
		assertEquals(-456.7, parameterModel.getParameterLowerLimit(), 0);
		assertEquals(null, parameterModel.getParameterLowerLimitError());
		parameterModel.setParameterUpperLimit("789.1");
		assertEquals(789.1, parameterModel.getParameterUpperLimit(), 0);
		assertEquals(null, parameterModel.getParameterUpperLimitError());

		parameterModel.setParameterValue("invalid1");
		assertEquals(123.4, parameterModel.getParameterValue(), 0);
		assertEquals("invalid1", parameterModel.getParameterValueError());
		parameterModel.setParameterLowerLimit("invalid2");
		assertEquals(-456.7, parameterModel.getParameterLowerLimit(), 0);
		assertEquals("invalid2", parameterModel.getParameterLowerLimitError());
		parameterModel.setParameterUpperLimit("invalid3");
		assertEquals(789.1, parameterModel.getParameterUpperLimit(), 0);
		assertEquals("invalid3", parameterModel.getParameterUpperLimitError());

	}

	@Test
	public void testParameterModelParsingSpecialValues() {
		Gaussian gaussian = new Gaussian();
		ParameterModel parameterModel = new ParameterModel(
				createMockModelRoot(), mock(FunctionModel.class), gaussian, 0);

		parameterModel.setParameterValue("Min Double");
		assertEquals(-Double.MAX_VALUE, parameterModel.getParameterValue(), 0);
		parameterModel.setParameterValue("Max Double");
		assertEquals(Double.MAX_VALUE, parameterModel.getParameterValue(), 0);
	}

	@Test
	public void testSetFunctionOutOfOrder() {
		// Test that we can set the second function of a subtract
		CompositeFunction actual = new CompositeFunction();
		FunctionModelRoot modelRoot = new FunctionModelRoot(actual, provider);
		Subtract subtract = new Subtract();
		modelRoot.addFunction(subtract);
		SetFunctionModel[] setFunctionModel = modelRoot.getSetFunctionModel(subtract, 1);
		assertEquals(1, setFunctionModel.length);

		// If the ABinaryOperator's setFunction were to disallow setting index 1 before 0
		// then this would probably raise an exception
		setFunctionModel[0].setEditingValue("Gaussian");

		// If ABinaryOperator's setFunction put the function in the wrong place this would fail
		assertTreeLooksLike(modelRoot, Node.SUBTACT(Node.SET_FUNCTION, Node.GAUSSIAN), Node.ADD_NEW_FUNCTION);
	}

	private FunctionModelRoot createMockModelRoot() {
		FunctionModelRoot modelRoot = mock(FunctionModelRoot.class);
		when(
				modelRoot.getParameterModel(any(IParameter.class),
						any(Boolean.class))).thenReturn(new ParameterModel[0]);
		return modelRoot;
	}

	protected void assertTreeLooksLike(FunctionModelRoot modelRoot,
			Node... nodes) {
		Node[] treeLooksLike = getTreeLooksLike(modelRoot);
		// System.out.println("expect: ");
		// System.out.print(toString(nodes));
		// System.out.println("actual: ");
		// System.out.print(toString(treeLooksLike));
		assertArrayEquals(nodes, treeLooksLike);
	}

	protected void assertTreeLooksLike(FunctionModelElement[] modelElements,
			Node... nodes) {
		Node[] treeLooksLike = new Node[modelElements.length];
		for (int i = 0; i < treeLooksLike.length; i++) {
			treeLooksLike[i] = getTreeLooksLike(modelElements[i]);
		}
		// System.out.println("expect: ");
		// System.out.print(toString(node));
		// System.out.println("actual: ");
		// System.out.print(toString(treeLooksLike));
		assertArrayEquals(nodes, treeLooksLike);
	}

	protected Node[] getTreeLooksLike(FunctionModelRoot modelRoot) {
		List<Node> children = new ArrayList<>();
		for (FunctionModelElement functionModelElement : modelRoot
				.getChildren()) {
			children.add(getTreeLooksLike(functionModelElement));
		}
		return children.toArray(new Node[children.size()]);
	}

	protected Node getTreeLooksLike(FunctionModelElement ti) {
		List<Node> children = new ArrayList<>();
		for (FunctionModelElement child : ti.getChildren()) {
			children.add(getTreeLooksLike(child));
		}
		return new Node(ti.toSimpleString(), children.toArray(new Node[children
				.size()]));
	}
}
