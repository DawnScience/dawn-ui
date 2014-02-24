package org.dawnsci.common.widgets.gda.function.jexl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;

import org.dawb.common.services.expressions.IExpressionEngine;
import org.dawb.common.services.expressions.IExpressionService;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionError;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionException;
import org.dawnsci.jexl.internal.ExpressionServiceImpl;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

@SuppressWarnings("restriction")
public class JexlExpressionFunctionTest {
	private void checkNoExpression(JexlExpressionFunction f) {
		assertEquals(0, f.getParameters().length);
		assertEquals(JexlExpressionFunctionError.NO_EXPRESSION,
				f.getExpressionError());
	}

	private void checkInvalidExpression(JexlExpressionFunction f) {
		assertEquals(0, f.getParameters().length);
		assertEquals(JexlExpressionFunctionError.INVALID_EXPRESSION,
				f.getExpressionError());
	}

	private void checkInvalidExpressionNoX(JexlExpressionFunction f) {
		assertEquals(0, f.getParameters().length);
		assertEquals(JexlExpressionFunctionError.NO_X, f.getExpressionError());
	}

	private void checkNoEngine(JexlExpressionFunction f) {
		assertEquals(0, f.getParameters().length);
		assertEquals(JexlExpressionFunctionError.NO_ENGINE,
				f.getExpressionError());
	}

	@Test
	public void testJexlUserErrorCases() {
		checkNoExpression(new JexlExpressionFunction(
				new ExpressionServiceImpl()));
		checkInvalidExpression(new JexlExpressionFunction(
				new ExpressionServiceImpl(), "a nonsense expression"));
		checkInvalidExpression(new JexlExpressionFunction(
				new ExpressionServiceImpl(), "((missing_bracket)"));
		checkInvalidExpressionNoX(new JexlExpressionFunction(
				new ExpressionServiceImpl(), ""));
		checkInvalidExpressionNoX(new JexlExpressionFunction(
				new ExpressionServiceImpl(), "a"));
		checkInvalidExpressionNoX(new JexlExpressionFunction(
				new ExpressionServiceImpl(), "a+b"));
		JexlExpressionFunction noEngine = new JexlExpressionFunction(
				new IExpressionService() {
					@Override
					public IExpressionEngine getExpressionEngine() {
						return null;
					}
				}, "a+x");
		checkNoEngine(noEngine);
		try {
			noEngine.setExpression("b+x");
			fail();
		} catch (JexlExpressionFunctionException e) {
			assertEquals(e.getError(), JexlExpressionFunctionError.NO_ENGINE);
		}
		checkNoEngine(noEngine);

		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl());
		checkNoExpression(f);
		try {
			f.setExpression("a");
			fail();
		} catch (JexlExpressionFunctionException e) {
			assertEquals(e.getError(), JexlExpressionFunctionError.NO_X);
		}
		checkInvalidExpressionNoX(f);
		try {
			f.setExpression("(a+");
			fail();
		} catch (JexlExpressionFunctionException e) {
			assertEquals(e.getError(),
					JexlExpressionFunctionError.INVALID_EXPRESSION);
		}
		checkInvalidExpression(f);

	}

	@Test
	public void testEquals() throws JexlExpressionFunctionException {
		assertTrue(new JexlExpressionFunction(new ExpressionServiceImpl(),"a")
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl(),"a")));
		assertTrue(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")));
		assertTrue(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")
				.equals(new JexlExpressionFunction(null, "x")));
		assertTrue(new JexlExpressionFunction(new ExpressionServiceImpl())
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl())));
		assertFalse(new JexlExpressionFunction(new ExpressionServiceImpl(),"a")
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")));
		assertFalse(new JexlExpressionFunction(new ExpressionServiceImpl()).equals(null));
		assertFalse(new JexlExpressionFunction(new ExpressionServiceImpl())
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")));
		assertFalse(new JexlExpressionFunction(new ExpressionServiceImpl(),"x")
				.equals(new JexlExpressionFunction(new ExpressionServiceImpl())));
		assertFalse(new JexlExpressionFunction(new ExpressionServiceImpl(),"x").equals(""));

		JexlExpressionFunction f1 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b+x");
		JexlExpressionFunction f2 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+c+x");
		assertFalse(f1.equals(f2));
		f1.setExpression("a+c+x");
		assertTrue(f1.equals(f2));
		f1.setExpression("d+x");
		f2.setExpression("d+x");
		assertTrue(f1.equals(f2));
	}

	@Test
	public void testHashCode() throws JexlExpressionFunctionException {
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"a").hashCode(),
				new JexlExpressionFunction(new ExpressionServiceImpl(),"a").hashCode());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x").hashCode(),
				new JexlExpressionFunction(new ExpressionServiceImpl(),"x").hashCode());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x").hashCode(),
				new JexlExpressionFunction(null, "x").hashCode());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl()).hashCode(),
				new JexlExpressionFunction(new ExpressionServiceImpl()).hashCode());

		JexlExpressionFunction expected = new JexlExpressionFunction(new ExpressionServiceImpl());
		expected.setExpression("abcd+x");
		JexlExpressionFunction actual = new JexlExpressionFunction(new ExpressionServiceImpl());
		actual.setExpression("abcd+x");
		assertEquals(expected.hashCode(), actual.hashCode());

		JexlExpressionFunction f1 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b+x");
		JexlExpressionFunction f2 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+c+x");
		f1.setExpression("a+c+x");
		assertEquals(f1.hashCode(), f2.hashCode());
		f1.setExpression("d+x");
		f2.setExpression("d+x");
		assertEquals(f1.hashCode(), f2.hashCode());
	}

	@Test
	public void testCopy() throws Exception {
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b"),
				new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b").copy());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"(a+x"),
				new JexlExpressionFunction(new ExpressionServiceImpl(),"(a+x").copy());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl(),"x"),
				new JexlExpressionFunction(new ExpressionServiceImpl(),"x").copy());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl()),
				new JexlExpressionFunction(new ExpressionServiceImpl()).copy());
		assertEquals(new JexlExpressionFunction(new ExpressionServiceImpl()),
				new JexlExpressionFunction(new ExpressionServiceImpl()).copy());
		JexlExpressionFunction copyParameters = new JexlExpressionFunction(new ExpressionServiceImpl(),
				"a+x");
		assertTrue(copyParameters.getParameter(0) != copyParameters.copy()
				.getParameter(0));
		assertEquals(copyParameters.getParameter(0), copyParameters.copy()
				.getParameter(0));
	}

	@Test
	public void testCopyRespectsService() throws Exception {

		// Make sure copy respects the service I passed in
		JexlExpressionFunction noEngine = new JexlExpressionFunction(
				new IExpressionService() {
					@Override
					public IExpressionEngine getExpressionEngine() {
						return null;
					}
				}, "x");
		checkNoEngine(noEngine);
		checkNoEngine(noEngine.copy());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCopyUniqueEngines() throws Exception {
		// Make sure the copy has its own engine

		// make the mocks
		IExpressionEngine engine1 = mock(IExpressionEngine.class);
		when(engine1.getFunctions()).thenReturn(new HashMap<String, Object>());
		when(engine1.getVariableNamesFromExpression())
				.thenReturn(Arrays.asList(new String[] { "a", "x" }))
				.thenReturn(Arrays.asList(new String[] { "a1", "x" }))
				.thenThrow(Exception.class);
		IExpressionEngine engine2 = mock(IExpressionEngine.class);
		when(engine2.getFunctions()).thenReturn(new HashMap<String, Object>());
		when(engine2.getVariableNamesFromExpression())
				.thenReturn(Arrays.asList(new String[] { "a", "x" }))
				.thenReturn(Arrays.asList(new String[] { "a2", "x" }))
				.thenThrow(Exception.class);
		IExpressionService expressionServiceMock = mock(IExpressionService.class);
		when(expressionServiceMock.getExpressionEngine()).thenReturn(engine1,
				engine2, null);

		// make the copy
		JexlExpressionFunction uniqueEngine1 = new JexlExpressionFunction(
				expressionServiceMock, "a+x");
		JexlExpressionFunction uniqueEngine2 = uniqueEngine1.copy();

		// now if we the JexlExpressionFunction tries to use the wrong engine
		// (more times than expected, defined by when thenThrow above) then an
		// exception will be thrown

		// check engine1
		assertEquals(1, uniqueEngine1.getNoOfParameters());
		assertEquals("a", uniqueEngine1.getParameter(0).getName());
		uniqueEngine1.setExpression("a1+x");
		assertEquals(1, uniqueEngine1.getNoOfParameters());
		assertEquals("a1", uniqueEngine1.getParameter(0).getName());

		// check engine2
		assertEquals(1, uniqueEngine2.getNoOfParameters());
		assertEquals("a", uniqueEngine2.getParameter(0).getName());
		uniqueEngine2.setExpression("a2+x");
		assertEquals(1, uniqueEngine2.getNoOfParameters());
		assertEquals("a2", uniqueEngine2.getParameter(0).getName());

		// make sure that all the calls to the engine are used up
		try {
			engine1.getVariableNamesFromExpression();
			fail("engine1 not called enough");
		} catch (Exception e) {
			// pass
		}
		try {
			engine2.getVariableNamesFromExpression();
			fail("engine2 not called enough");
		} catch (Exception e) {
			// pass
		}
	}

	@Test
	public void testParameters() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertEquals(1, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());

		f.setExpression("b+x");
		assertEquals(1, f.getNoOfParameters());
		assertEquals("b", f.getParameter(0).getName());
	}

	@Test
	public void testParametersDottedName()
			throws JexlExpressionFunctionException {

		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a.b.c+x");
		assertEquals(1, f.getNoOfParameters());
		assertEquals("a.b.c", f.getParameter(0).getName());
	}

	@Test
	public void testParametersOrder() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"x+a+b+c");
		assertEquals(3, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		assertEquals("b", f.getParameter(1).getName());
		assertEquals("c", f.getParameter(2).getName());

		f = new JexlExpressionFunction(new ExpressionServiceImpl(),"x+c+b+a");
		assertEquals(3, f.getNoOfParameters());
		assertEquals("c", f.getParameter(0).getName());
		assertEquals("b", f.getParameter(1).getName());
		assertEquals("a", f.getParameter(2).getName());

		f = new JexlExpressionFunction(new ExpressionServiceImpl(),"x+a+b+a");
		assertEquals(2, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		assertEquals("b", f.getParameter(1).getName());

		f = new JexlExpressionFunction(new ExpressionServiceImpl(),"x+b+a+b");
		assertEquals(2, f.getNoOfParameters());
		assertEquals("b", f.getParameter(0).getName());
		assertEquals("a", f.getParameter(1).getName());
	}

	@Test
	public void testParametersAddNew() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertEquals(1, f.getNoOfParameters());
		f.setParameterValues(4.5);

		f.setExpression("a+b+x");
		assertEquals(2, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		assertEquals(4.5, f.getParameterValue(0), 0.0);
	}

	@Test
	public void testParametersRemove() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b+x");
		assertEquals(2, f.getNoOfParameters());
		f.setParameterValues(2.5, 5.2);

		f.setExpression("a+x");
		assertEquals(1, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		assertEquals(2.5, f.getParameterValue(0), 0.0);
	}

	@Test
	public void testParametersErrorChange()
			throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertEquals(1, f.getNoOfParameters());
		f.setParameterValues(16.0);

		try {
			f.setExpression("a");
			fail();
		} catch (JexlExpressionFunctionException e) {
			// expected
		}
		checkInvalidExpressionNoX(f);
		try {
			f.setExpression("(a+");
			fail();
		} catch (JexlExpressionFunctionException e) {
			// expected
		}
		checkInvalidExpression(f);

		f.setExpression("a+x");
		assertEquals(1, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		assertEquals(16.0, f.getParameterValue(0), 0.0);
	}

	@Test
	public void testVal() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertEquals(1.0, f.val(1.0), 0.0);
		assertEquals(2.0, f.val(2.0), 0.0);
		f.setParameterValues(1.0);
		assertEquals(2.0, f.val(1.0), 0.0);
		assertEquals(3.0, f.val(2.0), 0.0);
	}

	@Test
	public void testValDottedName() throws JexlExpressionFunctionException {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a.b.c+x");
		f.setParameterValues(2.0);
		assertEquals(3.0, f.val(1.0), 0.0);
		assertEquals(4.0, f.val(2.0), 0.0);
	}

	@Test
	public void testErrorDuringEval() {
		// try using a method that expects Datasets instead of double
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"dnp:add(a,x)");
		// even though the function returns the wrong type/takes the wrong
		// parameter
		// we still parse successfully
		assertEquals(1, f.getNoOfParameters());
		assertEquals("a", f.getParameter(0).getName());
		// Only at this point does the error kick in
		f.val(1.0);
		fail("TODO: This should be unreachable perhaps??? What happens when evaluation goes wrong");
	}

	@Test
	public void testFillWithValues() {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		fail("TODO: fillWithValues unreviewed");
		// f.fillWithValues(null, null);
	}

	@Test
	public void testGetSetExpression() {
		JexlExpressionFunction f1 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertEquals("a+x", f1.getExpression());

		JexlExpressionFunction f2 = new JexlExpressionFunction(new ExpressionServiceImpl());
		assertEquals(null, f2.getExpression());

		JexlExpressionFunction f3 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		try {
			f3.setExpression(null);
		} catch (JexlExpressionFunctionException e) {
		}
		assertEquals(null, f3.getExpression());

		JexlExpressionFunction f4 = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+b");
		assertEquals("a+b", f4.getExpression());

		JexlExpressionFunction f5 = new JexlExpressionFunction(new ExpressionServiceImpl(),"(a+x");
		assertEquals("(a+x", f5.getExpression());
	}

	@Test
	public void testDirty() {
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),"a+x");
		assertTrue(f.isDirty());
		assertEquals(1.0, f.val(1.0), 0.0);
		assertFalse(f.isDirty());
		assertEquals(2.0, f.val(2.0), 0.0);
		assertFalse(f.isDirty());
		f.setParameterValues(1.0);
		assertTrue(f.isDirty());
		assertEquals(2.0, f.val(1.0), 0.0);
		assertFalse(f.isDirty());
		assertEquals(3.0, f.val(2.0), 0.0);
		assertFalse(f.isDirty());
	}

	@Test
	public void testValGaussian() {
		// This test isn't attempting to test that Gaussian produces the correct
		// result, rather it is making sure that the parameters are passed as
		// expected from the expression through the system
		JexlExpressionFunction f = new JexlExpressionFunction(new ExpressionServiceImpl(),
				"func:Gaussian(x,a,b,c)");
		f.setParameterValues(1.0, 2.0, 3.0);
		assertEquals(new Gaussian(1.0, 2.0, 3.0).val(4.0), f.val(4.0), 0.0);
	}
}
