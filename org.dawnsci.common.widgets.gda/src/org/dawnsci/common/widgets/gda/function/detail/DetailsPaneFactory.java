package org.dawnsci.common.widgets.gda.function.detail;

import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.eclipse.core.runtime.IAdapterFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

@SuppressWarnings("rawtypes")
public class DetailsPaneFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IFunctionDetailPane.class) {
			if (adaptableObject instanceof JexlExpressionFunction) {
				return new JexlExpressionFunctionDetailPane();
			} else if (adaptableObject instanceof Polynomial) {
				return new PolynomialFunctionDetailPane();
			} else if (adaptableObject instanceof IFunction) {
				return new FunctionDetailPane();
			} else if (adaptableObject instanceof IParameter) {
				return new ParameterDetailPane();
			}
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IFunctionDetailPane.class };
	}
}
