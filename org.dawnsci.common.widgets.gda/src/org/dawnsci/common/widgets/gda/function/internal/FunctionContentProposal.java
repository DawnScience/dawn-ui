package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.descriptors.FunctionInstantiationFailedException;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;

/**
 * Content proposals for simple functions (not jexl)
 *
 */
public class FunctionContentProposal implements IContentProposal, IAdaptable {
	private static final Image CURVE = Activator.getImage("chart_curve.png");
	private static final Image BULLET_BLUE = Activator.getImage("bullet_blue.png");

	private IFunction function;
	private IFunctionDescriptor functionDescriptor;

	public FunctionContentProposal(IFunction function, IFunctionDescriptor functionDescriptor) {
		super();
		this.function = function;
		this.functionDescriptor = functionDescriptor;
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return functionDescriptor.getAdapter(adapter);
	}

	@Override
	public String getContent() {
		return functionDescriptor.getName();
	}

	@Override
	public int getCursorPosition() {
		return functionDescriptor.getName().length();
	}

	@Override
	public String getLabel() {
		return functionDescriptor.getName();
	}

	@Override
	public String getDescription() {
		try {
			IFunction function2 = functionDescriptor.getFunction();
			StringBuilder desc = new StringBuilder(function2.getDescription() + System.lineSeparator());

			IParameter[] parameters = function2.getParameters();
			if (parameters != null && parameters.length != 0){
				desc.append(System.lineSeparator() + "Parameters:" + System.lineSeparator());
				for (IParameter param : function2.getParameters()) {
					desc.append("  " + param.getName() + System.lineSeparator());
				}
			}
			return desc.toString();
		} catch (FunctionInstantiationFailedException e) {
		}
		return null;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	/**
	 * Returns an image for the content proposal
	 *
	 * @return images for IOperators and IFunctions, else null
	 */
	public Image getImage() {
		if (function instanceof IOperator) {
			return BULLET_BLUE;
		} else if (function instanceof IFunction) {
			return CURVE;
		}
		return null;
	}

}