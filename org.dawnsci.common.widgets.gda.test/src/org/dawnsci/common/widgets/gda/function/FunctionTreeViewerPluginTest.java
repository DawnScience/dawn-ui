package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer.COLUMN;
import org.dawnsci.common.widgets.gda.function.descriptors.CustomFunctionDescriptorProvider;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Subtract;

public class FunctionTreeViewerPluginTest extends
		FunctionTreeViewerPluginTestBase {
	CustomFunctionDescriptorProvider provider = new CustomFunctionDescriptorProvider(
			new IFunction[] { new Gaussian(), new Fermi(), new Add(),
					new Subtract() }, false);
	IFunctionViewer viewer;

	@Override
	protected void createControl(Composite parent) {
		viewer = new FunctionTreeViewer(parent, provider);
	}

	@Before
	public void before() {
		viewer.setInput((CompositeFunction) null);
	}

	@Override
	protected FunctionTreeViewer getFunctionTreeViewer() {
		return (FunctionTreeViewer) viewer;
	}

	@Test
	public void testAddFunction() throws Exception {
		CompositeFunction actual = new CompositeFunction();
		viewer.setInput(actual);

		addNewFunctionGUI(actual, "Gaussian");

		CompositeFunction expected = new CompositeFunction();
		expected.addFunction(new Gaussian());
		assertEquals(expected, actual);
	}

	@Test
	public void testSetFunction() throws Exception {
		CompositeFunction actual = new CompositeFunction();
		Subtract subtract = new Subtract();
		actual.addFunction(subtract);
		viewer.setInput(actual);

		setFunctionGUI(subtract, "Gaussian", 0);
		setFunctionGUI(subtract, "Fermi", 1);

		CompositeFunction expected = new CompositeFunction();
		Subtract subtractExpected = new Subtract();
		expected.addFunction(subtractExpected);
		subtractExpected.addFunction(new Gaussian());
		subtractExpected.addFunction(new Fermi());
		assertEquals(expected, actual);
	}

	@Test
	public void testRemoveFunction() throws Exception {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian();
		actual.addFunction(gaussian);
		viewer.setInput(actual);
		assertTreeLooksLike(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);

		actual.removeFunction(0);
		viewer.refresh();
		assertTreeLooksLike(Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testRemoveFunctionMultipleA() throws Exception {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian1 = new Gaussian(1, 2, 3);
		Gaussian gaussian2 = new Gaussian(2, 3, 4);
		actual.addFunction(gaussian1);
		actual.addFunction(gaussian2);
		viewer.setInput(actual);
		assertTreeLooksLike(Node.GAUSSIAN, Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);

		actual.removeFunction(0);
		viewer.refresh();
		assertTreeLooksLike(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testRemoveFunctionMultipleB() throws Exception {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian1 = new Gaussian(1, 2, 3);
		Gaussian gaussian2 = new Gaussian(2, 3, 4);
		actual.addFunction(gaussian1);
		actual.addFunction(gaussian2);
		viewer.setInput(actual);
		assertTreeLooksLike(Node.GAUSSIAN, Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);

		actual.removeFunction(1);
		viewer.refresh();
		assertTreeLooksLike(Node.GAUSSIAN, Node.ADD_NEW_FUNCTION);
	}

	/**
	 * Make sure that we can select an item in the GUI and that the expected
	 * item is returned by getSelection.
	 */
	@Test
	public void testSelectedFunction() {
		CompositeFunction actual = new CompositeFunction();
		Add add = new Add();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		add.addFunction(gaussian);
		actual.addFunction(add);
		viewer.setInput(actual);

		setSelection(add);
		assertEquals(add, viewer.getSelectedFunction());
		setSelection(gaussian);
		assertEquals(gaussian, viewer.getSelectedFunction());

	}

	@Test
	public void testSelectedParameter() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		actual.addFunction(gaussian);
		viewer.setInput(actual);

		setSelection(gaussian, 0);
		assertEquals(gaussian, viewer.getSelectedFunction());
		assertEquals(gaussian.getParameter(0), viewer.getSelectedParameter());
		assertEquals(0, viewer.getSelectedParameterIndex());

		setSelection(gaussian, 2);
		assertEquals(gaussian, viewer.getSelectedFunction());
		assertEquals(gaussian.getParameter(2), viewer.getSelectedParameter());
		assertEquals(2, viewer.getSelectedParameterIndex());
	}

	@Test
	public void testEditParameterValue() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		actual.addFunction(gaussian);
		viewer.setInput(actual);

		setParameterTextGUI(COLUMN.VALUE, gaussian, 0, "4");
		setParameterTextGUI(COLUMN.VALUE, gaussian, 1, "5");
		setParameterTextGUI(COLUMN.VALUE, gaussian, 2, "6");

		CompositeFunction expected = new CompositeFunction();
		expected.addFunction(new Gaussian(4, 5, 6));
		assertEquals(expected, actual);
	}

	@Test
	public void testEditLimits() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian();
		actual.addFunction(gaussian);
		viewer.setInput(actual);

		setParameterTextGUI(COLUMN.LOWERLIMIT, gaussian, 0, "-1");
		setParameterTextGUI(COLUMN.UPPERLIMIT, gaussian, 0, "1");

		CompositeFunction expected = new CompositeFunction();
		Gaussian expectedGaussian = new Gaussian();
		expectedGaussian.getParameter(0).setLimits(-1.0, 1.0);
		expected.addFunction(expectedGaussian);
		assertEquals(expected, actual);
	}

	private void testInvalidValueHelper(CompositeFunction actual,
			Gaussian gaussian) {
		// Check that we display the invalid value, and that when we trigger
		// edit we are editing the invalid value
		assertEquals("invalidnumber",
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals("invalidnumber",
				getParameterTextGUI(COLUMN.VALUE, gaussian, 0));
		// Check that the viewer knows it is invalid
		assertTrue(!viewer.isValid());
		// Check that the invalid value has not been applied
		CompositeFunction expected = new CompositeFunction();
		expected.addFunction(new Gaussian(1, 2, 3));
		assertEquals(expected, actual);
	}

	@Test
	public void testInvalidValue() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		actual.addFunction(gaussian);
		viewer.setInput(actual);

		assertTrue(viewer.isValid());
		setParameterTextGUI(COLUMN.VALUE, gaussian, 0, "invalidnumber");
		testInvalidValueHelper(actual, gaussian);
		// issue a refresh to make sure we don't lose the invalid value
		viewer.refresh();
		testInvalidValueHelper(actual, gaussian);
	}

	@Test
	public void testMinMaxDoubleDisplaysAndEdits() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		actual.addFunction(gaussian);
		gaussian.getParameter(0).setValue(0);
		gaussian.getParameter(0).setLimits(-Double.MAX_VALUE, Double.MAX_VALUE);
		gaussian.getParameter(0).setFixed(false);
		viewer.setInput(actual);

		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(0),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(Double.toString(0),
				getParameterTextGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals("Min Double",
				getParameterLabelGUI(COLUMN.LOWERLIMIT, gaussian, 0));
		assertEquals(Double.toString(-Double.MAX_VALUE),
				getParameterTextGUI(COLUMN.LOWERLIMIT, gaussian, 0));
		assertEquals("Max Double",
				getParameterLabelGUI(COLUMN.UPPERLIMIT, gaussian, 0));
		assertEquals(Double.toString(Double.MAX_VALUE),
				getParameterTextGUI(COLUMN.UPPERLIMIT, gaussian, 0));
	}

	@Test
	public void testFixedDisplaysAndEdits() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		actual.addFunction(gaussian);
		gaussian.getParameter(0).setValue(0);
		gaussian.getParameter(0).setLimits(-Double.MAX_VALUE, Double.MAX_VALUE);
		gaussian.getParameter(0).setFixed(true);
		viewer.setInput(actual);

		assertTrue(getParameterLabelGUI(COLUMN.VALUE, gaussian, 0).startsWith(
				FunctionTreeViewer.DOUBLE_FORMAT.format(0) + " "));
		assertTrue(getParameterLabelGUI(COLUMN.VALUE, gaussian, 0).endsWith(
				" (Fixed)"));
		assertEquals(Double.toString(0),
				getParameterTextGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals("(Fixed)",
				getParameterLabelGUI(COLUMN.LOWERLIMIT, gaussian, 0));
		assertEquals(Double.toString(-Double.MAX_VALUE),
				getParameterTextGUI(COLUMN.LOWERLIMIT, gaussian, 0));
		assertEquals("(Fixed)",
				getParameterLabelGUI(COLUMN.UPPERLIMIT, gaussian, 0));
		assertEquals(Double.toString(Double.MAX_VALUE),
				getParameterTextGUI(COLUMN.UPPERLIMIT, gaussian, 0));
	}

	@Test
	public void testLinkedParamters() {
		CompositeFunction actual = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1, 2, 3);
		// link parameter 0 and 1 (by assigning the same instance of IParameter to them)
		gaussian.setParameter(0, gaussian.getParameter(1));
		actual.addFunction(gaussian);
		viewer.setInput(actual);

		// test that initial display is as expected
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(2),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(2),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(3),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));

		// change one of the linked parameters
		setParameterTextGUI(COLUMN.VALUE, gaussian, 0, "5");

		// make sure the linked parameters update
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(5),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(5),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(3),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));

		// change the linked parameter to invalid
		setParameterTextGUI(COLUMN.VALUE, gaussian, 0, "invalidnumber");

		// make sure the linked parameters update
		assertEquals("invalidnumber",
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals("invalidnumber",
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(3),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));

		// change one of the linked parameters
		setParameterTextGUI(COLUMN.VALUE, gaussian, 0, "7");

		// make sure the linked parameters update
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(7),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 0));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(7),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 1));
		assertEquals(FunctionTreeViewer.DOUBLE_FORMAT.format(3),
				getParameterLabelGUI(COLUMN.VALUE, gaussian, 2));

	}
}
