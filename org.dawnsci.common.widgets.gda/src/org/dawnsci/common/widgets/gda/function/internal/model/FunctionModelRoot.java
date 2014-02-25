/*-
 * Copyright 2013 Diamond Light Source Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.common.widgets.gda.function.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.dawnsci.common.widgets.gda.function.IFittedFunctionInvalidatedEvent;
import org.dawnsci.common.widgets.gda.function.IFunctionModifiedEvent;
import org.dawnsci.common.widgets.gda.function.IModelModifiedListener;
import org.dawnsci.common.widgets.gda.function.IParameterModifiedEvent;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptor;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptorProvider;
import org.eclipse.core.runtime.ListenerList;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;

public class FunctionModelRoot {

	private CompositeFunction root;
	private CompositeFunction fittedRoot;
	private IFunctionDescriptorProvider functionDescriptorProvider;
	private ListenerList modelModifiedListeners;
	private FunctionModelElement[] children = new FunctionModelElement[0];

	/**
	 * The fitted function (existence or value) does not affect the equality.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FunctionModelRoot))
			return false;
		FunctionModelRoot rhs = (FunctionModelRoot) obj;
		return new EqualsBuilder()
				.append(root, rhs.root)
				.append(functionDescriptorProvider,
						rhs.functionDescriptorProvider).isEquals();
	}

	public FunctionModelRoot(CompositeFunction compositeFunction,
			IFunctionDescriptorProvider functionDescriptorProvider) {
		compositeFunction.updateAllParameters();
		this.root = compositeFunction;
		this.functionDescriptorProvider = functionDescriptorProvider;
		this.fittedRoot = null;
	}

	public IFunctionDescriptor getFunctionDescriptor(IFunction function) {
		return functionDescriptorProvider.getDescriptor(function);
	}

	public IFunctionDescriptorProvider getFunctionDescriptorProvider() {
		return functionDescriptorProvider;
	}

	public CompositeFunction getRootOperator() {
		return root;
	}

	public CompositeFunction getFittedRootOperator() {
		return fittedRoot;
	}

	public void setFittedRoot(CompositeFunction fittedCompositeRoot) {
		this.fittedRoot = fittedCompositeRoot;
	}

	/**
	 * Make sure fittedRoot and root have the same structure, if not dump the
	 * fittedRoot
	 */
	private void checkConsitency() {
		if (fittedRoot == null)
			return;
		if (!checkConsitencyOperator(root, fittedRoot)) {
			fittedRoot = null;
			fittedFunctionInvalidated(new IFittedFunctionInvalidatedEvent() {
			});
		}
	}

	private boolean checkConsitencyOperator(IOperator operator,
			IOperator fittedOperator) {
		IFunction[] functions = operator.getFunctions();
		IFunction[] fittedFunctions = fittedOperator.getFunctions();
		if (functions.length != fittedFunctions.length)
			return false;
		for (int i = 0; i < fittedFunctions.length; i++) {
			if (functions[i].getClass() != fittedFunctions[i].getClass())
				return false;
			if (!checkConsitencyFunction(functions[i], fittedFunctions[i]))
				return false;
			if (functions[i] instanceof IOperator) {
				// since getClass() is the same for both, only need to check one
				// of them before typecast
				if (!checkConsitencyOperator((IOperator) functions[i],
						(IOperator) fittedFunctions[i]))
					return false;
			}
		}
		return true;
	}

	private boolean checkConsitencyFunction(IFunction function,
			IFunction fittedFunction) {
		if (function.getParameters().length != fittedFunction.getParameters().length)
			return false;
		// we don't check other aspects of the parameters as we are only looking
		// for structural match, ie. same number of children
		// functions/parameters
		return true;
	}

	protected static FunctionModelElement[] getChildren(
			FunctionModelElement[] lastChildren,
			OperatorModel thisOperatorModel, IOperator operator,
			IOperator fittedOperator, FunctionModelRoot modelRoot) {
		List<FunctionModelElement> childrenItems = new ArrayList<>();

		IFunction[] functions = operator.getFunctions();
		IFunction[] fittedFunctions = null;
		if (fittedOperator != null) {
			fittedFunctions = fittedOperator.getFunctions();
			if (fittedFunctions.length != functions.length)
				fittedFunctions = null;
		}

		for (int i = 0; i < functions.length; i++) {
			IFunction childFunction = functions[i];
			IFunction childFittedFunction = null;
			if (fittedFunctions != null) {
				childFittedFunction = fittedFunctions[i];
			}

			if (childFunction == null) {
				if (lastChildren != null && i < lastChildren.length
						&& lastChildren[i] instanceof SetFunctionModel) {
					childrenItems.add(lastChildren[i]);
				} else {
					childrenItems.add(new SetFunctionModel(modelRoot,
							thisOperatorModel, operator, i));
				}
			} else {

				if (childFunction instanceof IOperator) {
					IOperator childOperator = (IOperator) childFunction;
					IOperator childFittedOperator = (IOperator) childFittedFunction;
					OperatorModel operatorModel = new OperatorModel(modelRoot,
							thisOperatorModel, operator, childOperator,
							fittedOperator, childFittedOperator, i);

					if (lastChildren != null && i < lastChildren.length
							&& lastChildren[i] instanceof OperatorModel) {
						OperatorModel lastModel = (OperatorModel) lastChildren[i];
						if (lastModel.getOperator() == childOperator) {
							operatorModel = lastModel;
							operatorModel.setFittedOperator(fittedOperator,
									childFittedOperator);
						}
					}
					childrenItems.add(operatorModel);
				} else {
					FunctionModel functionModel = new FunctionModel(modelRoot,
							thisOperatorModel, operator, childFunction,
							fittedOperator, childFittedFunction, i);

					if (lastChildren != null && i < lastChildren.length
							&& lastChildren[i] instanceof FunctionModel) {
						FunctionModel lastModel = (FunctionModel) lastChildren[i];
						if (lastModel.getFunction() == childFunction) {
							functionModel = lastModel;
							functionModel.setFittedFunction(fittedOperator,
									childFittedFunction);
						}
					}
					childrenItems.add(functionModel);

				}
			}
		}

		if (operator.isExtendible()) {
			childrenItems.add(new AddNewFunctionModel(modelRoot,
					thisOperatorModel, operator));
		}
		return childrenItems.toArray(new FunctionModelElement[childrenItems
				.size()]);

	}

	public FunctionModelElement[] getChildren() {
		checkConsitency();
		children = getChildren(children, null, root, fittedRoot, this);
		return children;
	}

	/**
	 * Returns all matching FunctionModelElement for the given function
	 *
	 * @param function
	 * @return array (possibly 0 length), never returns null
	 */
	public FunctionModelElement[] getModelElement(IFunction function) {
		List<FunctionModelElement> found = new ArrayList<>();
		for (FunctionModelElement functionModelElement : getChildren()) {
			getElementWorker(found, function, functionModelElement);
		}
		return found.toArray(new FunctionModelElement[found.size()]);
	}

	/**
	 * Returns all matching AddNewFunctionModel for the given function
	 *
	 * @param operator
	 * @return array (possibly 0 length), never returns null
	 */
	public AddNewFunctionModel[] getAddNewFunctionModel(IOperator operator) {
		List<AddNewFunctionModel> found = new ArrayList<>();
		if (getRootOperator().equals(operator)) {
			FunctionModelElement[] children = getChildren();
			found.add((AddNewFunctionModel) children[children.length - 1]);
		}
		FunctionModelElement[] operatorModels = getModelElement(operator);
		for (FunctionModelElement item : operatorModels) {
			OperatorModel operatorModel = (OperatorModel) item;
			if (operatorModel.getOperator().isExtendible()) {
				FunctionModelElement[] children = operatorModel.getChildren();
				found.add((AddNewFunctionModel) children[children.length - 1]);
			}
		}

		return found.toArray(new AddNewFunctionModel[found.size()]);
	}

	/**
	 * Returns all matching SetFunctionModel for the given function/index
	 *
	 * @param operator
	 * @return array (possibly 0 length), never returns null
	 */
	public SetFunctionModel[] getSetFunctionModel(IOperator operator, int index) {
		List<SetFunctionModel> found = new ArrayList<>();
		FunctionModelElement[] operatorModels = getModelElement(operator);
		for (FunctionModelElement item : operatorModels) {
			OperatorModel operatorModel = (OperatorModel) item;
			if (!operatorModel.getOperator().isExtendible()) {
				FunctionModelElement[] children = operatorModel.getChildren();
				if (index < children.length) {
					if (children[index] instanceof SetFunctionModel) {
						found.add((SetFunctionModel) children[index]);
					}
				}
			}
		}

		return found.toArray(new SetFunctionModel[found.size()]);
	}

	/**
	 * Returns all matching ParameterModel for the given function
	 *
	 * @param function
	 * @return array (possibly 0 length), never returns null
	 */
	public ParameterModel[] getParameterModel(IFunction function,
			int parameterIndex) {
		List<ParameterModel> found = new ArrayList<>();
		FunctionModelElement[] functionModels = getModelElement(function);
		for (FunctionModelElement item : functionModels) {
			if (item instanceof FunctionModel) {
				FunctionModel functionModel = (FunctionModel) item;
				ParameterModel[] children = functionModel.getChildren();
				if (parameterIndex < children.length) {
					found.add(children[parameterIndex]);
				}
			}
		}

		return found.toArray(new ParameterModel[found.size()]);
	}

	/**
	 * Returns all matching ParameterModel for the given IParameter
	 *
	 * @param parameter
	 * @return array (possibly 0 length), never returns null
	 */
	public ParameterModel[] getParameterModel(IParameter parameter) {
		return getParameterModel(parameter, false);
	}

	/**
	 * Returns all matching ParameterModel for the given IParameter
	 *
	 * @param parameter
	 * @param byIdentity
	 *            if true, returns only ParameterModels whose parameter is == to
	 *            parameter
	 * @return array (possibly 0 length), never returns null
	 */
	public ParameterModel[] getParameterModel(IParameter parameter,
			boolean byIdentity) {
		List<ParameterModel> found = new ArrayList<>();
		for (FunctionModelElement functionModelElement : getChildren()) {
			getParameterWorker(found, parameter, functionModelElement,
					byIdentity);
		}
		return found.toArray(new ParameterModel[found.size()]);

	}

	private void getParameterWorker(List<ParameterModel> found,
			IParameter parameter, FunctionModelElement element,
			boolean byIdentity) {
		if (element instanceof ParameterModel) {
			ParameterModel parameterModel = (ParameterModel) element;
			if (byIdentity) {
				if (parameterModel.getParameter() == parameter) {
					found.add(parameterModel);
				}
			} else {
				if (parameterModel.getParameter().equals(parameter)) {
					found.add(parameterModel);
				}
			}
		}
		for (FunctionModelElement childElement : element.getChildren()) {
			getParameterWorker(found, parameter, childElement, byIdentity);
		}
	}

	private void getElementWorker(List<FunctionModelElement> found,
			IFunction function, FunctionModelElement element) {
		if (element instanceof OperatorModel) {
			OperatorModel operatorModel = (OperatorModel) element;
			if (operatorModel.getOperator().equals(function)) {
				found.add(element);
			}
			for (FunctionModelElement childElement : operatorModel
					.getChildren()) {
				getElementWorker(found, function, childElement);
			}
		} else if (element instanceof FunctionModel) {
			FunctionModel functionModel = (FunctionModel) element;
			if (functionModel.getFunction().equals(function)) {
				found.add(element);
			}
		}
	}

	public void addModelModifiedListener(IModelModifiedListener listener) {
		if (modelModifiedListeners == null) {
			modelModifiedListeners = new ListenerList();
		}
		modelModifiedListeners.add(listener);
	}

	public void removeModelModifiedListener(IModelModifiedListener listener) {
		if (modelModifiedListeners == null) {
			return;
		}
		modelModifiedListeners.remove(listener);
	}

	protected void fittedFunctionInvalidated(
			IFittedFunctionInvalidatedEvent event) {
		if (modelModifiedListeners == null) {
			return;
		}
		for (Object listener : modelModifiedListeners.getListeners()) {
			((IModelModifiedListener) listener)
					.fittedFunctionInvalidated(event);
		}
	}

	protected void fireFunctionModified(IFunctionModifiedEvent event) {
		if (modelModifiedListeners == null) {
			return;
		}
		for (Object listener : modelModifiedListeners.getListeners()) {
			((IModelModifiedListener) listener).functionModified(event);
		}
	}

	protected void fireParameterModified(IParameterModifiedEvent event) {
		if (modelModifiedListeners == null) {
			return;
		}
		for (Object listener : modelModifiedListeners.getListeners()) {
			((IModelModifiedListener) listener).parameterModified(event);
		}
	}

	public boolean isValid() {
		if (!root.isValid()) {
			return false;
		}

		FunctionModelElement[] children = getChildren();
		for (FunctionModelElement child : children) {
			if (!child.isValid()) {
				return false;
			}
		}

		return true;
	}

	public boolean isFittedValid() {
		return fittedRoot != null;
	}

	public void setRoot(CompositeFunction compositeFunction) {
		root = compositeFunction;
	}

	public void addFunction(IFunction newChild) {
		root.addFunction(newChild);
		fireFunctionModified(new FunctionModifiedEvent(null, newChild, root,
				root.getNoOfFunctions() - 1));
	}
}
