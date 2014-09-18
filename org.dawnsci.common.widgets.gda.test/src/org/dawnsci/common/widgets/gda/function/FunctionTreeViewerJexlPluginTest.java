package org.dawnsci.common.widgets.gda.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.expressions.IExpressionService;
import org.dawnsci.common.widgets.gda.function.descriptors.CustomFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Subtract;

public class FunctionTreeViewerJexlPluginTest extends
		FunctionTreeViewerPluginTestBase {
	CustomFunctionDescriptorProvider provider = new CustomFunctionDescriptorProvider(
			new IFunction[] { new Gaussian(), new Fermi(), new Add(),
					new Subtract() }, true);
	IFunctionViewer viewer;

	@Override
	protected void createControl(Composite parent) {
		viewer = new FunctionTreeViewer(parent, provider);
	}

	@Before
	public void before() throws Exception {
		viewer.setInput((CompositeFunction) null);

		// Check that Jexl is working/wired up
		// If it fails here, then the Jexl service may not be running, try
		// setting org.dawnsci.jexl autoStart to true and level to 2, as it is
		// in uk.ac.diamond.dawn.product
		ServiceManager.getService(IExpressionService.class, true);
	}

	@Override
	protected FunctionTreeViewer getFunctionTreeViewer() {
		return (FunctionTreeViewer) viewer;
	}

	@Test
	public void testRendering_x() {
		CompositeFunction actual = new CompositeFunction();
		actual.addFunction(new JexlExpressionFunction("x"));
		viewer.setInput(actual);

		assertTreeLooksLike(new Node("x"), Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testRendering_xa() {
		CompositeFunction actual = new CompositeFunction();
		actual.addFunction(new JexlExpressionFunction("x+a"));
		viewer.setInput(actual);

		assertTreeLooksLike(new Node("x+a", "a"), Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testSortOrderOfParams_abc() {
		// Test that we infer parameters in the order declared
		// (At the time of this writing this relies on improved
		// ExpressionEngineImpl.getVariableNamesFromExpression that
		// maintains insertion order of the set

		CompositeFunction declaredAlphabetically = new CompositeFunction();
		declaredAlphabetically
				.addFunction(new JexlExpressionFunction("x+a+b+c"));
		viewer.setInput(declaredAlphabetically);

		assertTreeLooksLike(new Node("x+a+b+c", "a", "b", "c"),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testSortOrderOfParams_cba() {
		CompositeFunction declaredReverse = new CompositeFunction();
		declaredReverse.addFunction(new JexlExpressionFunction("x+c+b+a"));
		viewer.setInput(declaredReverse);

		assertTreeLooksLike(new Node("x+c+b+a", "c", "b", "a"),
				Node.ADD_NEW_FUNCTION);
	}

	@Test
	public void testEditFunction() {
		CompositeFunction actual = new CompositeFunction();
		JexlExpressionFunction function = new JexlExpressionFunction("x+a");
		actual.addFunction(function);
		viewer.setInput(actual);

		editFunctionGUI(function, "x+a+b");
		assertEquals(new JexlExpressionFunction("x+a+b"), actual.getFunction(0));

		// Note we can't compare against function above because the GUI may
		// recreate the function as it is not required to maintain it (although
		// it currently does)
		assertTrue(actual.getFunction(0) == function);
	}

	@Test
	public void testFunctionError() {
		CompositeFunction actual = new CompositeFunction();
		JexlExpressionFunction function = new JexlExpressionFunction("missing_x");
		actual.addFunction(function);
		viewer.setInput(actual);

		assertTreeLooksLike(new Node("missing_x"), Node.ADD_NEW_FUNCTION);
		assertTrue(!actual.isValid());
	}
}
