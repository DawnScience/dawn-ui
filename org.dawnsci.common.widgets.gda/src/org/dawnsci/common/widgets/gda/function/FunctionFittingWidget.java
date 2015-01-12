/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function;

import org.dawnsci.common.widgets.gda.function.actions.DuplicateFunctionAction;
import org.dawnsci.common.widgets.gda.function.actions.RemoveFunctionAction;
import org.dawnsci.common.widgets.gda.function.actions.ToggleFixedAction;
import org.dawnsci.common.widgets.gda.function.actions.UpdateFittedParamAction;
import org.dawnsci.common.widgets.gda.function.descriptors.IFunctionDescriptorProvider;
import org.dawnsci.common.widgets.gda.function.detail.IDisplayModelSelection;
import org.dawnsci.common.widgets.gda.function.detail.IFunctionDetailPane;
import org.dawnsci.common.widgets.gda.function.handlers.CopyHandler;
import org.dawnsci.common.widgets.gda.function.handlers.DeleteHandler;
import org.dawnsci.common.widgets.gda.function.handlers.NewFunctionHandler;
import org.dawnsci.common.widgets.gda.function.handlers.PasteHandler;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;

public class FunctionFittingWidget extends Composite implements IFunctionViewer {

	private final class SelectionChangedListener implements
			ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				final Object element = structuredSelection.getFirstElement();

				IDisplayModelSelection display = new DisplayModelSelection(
						element);

				if (detailsPanel != null) {
					detailsPanel.dispose();
					detailsPanel = null;
				}
				if (detailsControl != null && !detailsControl.isDisposed()) {
					detailsControl.dispose();
					detailsControl = null;
				}
				if (element != null) {
					detailsPanel = (IFunctionDetailPane) Platform
							.getAdapterManager().getAdapter(element,
									IFunctionDetailPane.class);
					if (detailsPanel != null) {
						detailsControl = detailsPanel
								.createControl(detailsPanelContainer);
						detailsPanelContainer.layout(true);
						detailsPanel.display(display);
					}
				}
			}
		}
	}

	private final class DisplayModelSelection implements IDisplayModelSelection {
		private final Object element;

		private DisplayModelSelection(Object element) {
			this.element = element;
		}

		@Override
		public Object getElement() {
			return element;
		}

		@Override
		public void refreshElement() {
			funcTree.refresh(element);
			if (element instanceof IParameter) {
				final IParameter parameter = (IParameter) element;
				fireParameterModified(new IParameterModifiedEvent() {

					@Override
					public IParameter getParameter() {
						return parameter;
					}

					@Override
					public int getIndexInFunction() {
						return 0;
					}

					@Override
					public IFunction getFunction() {
						return null;
					}
				});

			} else {
				final IFunction function = (IFunction) element;
				fireFunctionModified(new IFunctionModifiedEvent() {

					@Override
					public IOperator getParentOperator() {
						return null;
					}

					@Override
					public int getIndexInParentOperator() {
						return 0;
					}

					@Override
					public IFunction getBeforeFunction() {
						return function;
					}

					@Override
					public IFunction getAfterFunction() {
						return function;
					}
				});
			}
		}

		@Override
		public FunctionFittingWidget getFunctionWidget() {
			return FunctionFittingWidget.this;
		}
	}

	private FunctionTreeViewer funcTree;
	private ListenerList modelModifiedListeners;
	private IModelModifiedListener modelModifiedListener;
	private Composite detailsPanelContainer;
	private Control detailsControl;
	private IFunctionDetailPane detailsPanel;

	/**
	 * Create a new Function Widget (2)
	 *
	 * @param composite
	 *            parent composite to add widget to. Must not be
	 *            <code>null</code>
	 * @param functionDescriptorProvider
	 *            a provider that limits and describes the available functions
	 *            to the Widget. Must not be <code>null</code>
	 * @param site
	 *            the workbench site this widget sits in. This is used to set
	 *            commands and handlers. Can be <code>null</code>, in which case
	 *            handlers will not be added and a pop-up menu will not be
	 *            added.
	 */
	public FunctionFittingWidget(final Composite composite,
			IFunctionDescriptorProvider functionDescriptorProvider,
			IWorkbenchSite site) {
		super(composite, SWT.NONE);

		setLayout(new FillLayout());

		SashForm sashForm = new SashForm(this, SWT.VERTICAL);

		funcTree = new FunctionTreeViewer(sashForm, functionDescriptorProvider);
		funcTree.addSelectionChangedListener(new SelectionChangedListener());

		//XXX What is the details panel used for?
		detailsPanelContainer = new Composite(sashForm, SWT.NONE);
		detailsPanelContainer.setBackground(ColorConstants.white);
		detailsPanelContainer.setLayout(new FillLayout());

		sashForm.setBackground(ColorConstants.white);
		
		//Formerly {6, 10}, but having more space for functions seemed more important
		sashForm.setWeights(new int[] { 9, 1 });

		if (site != null) {
			IHandlerService serv = (IHandlerService) site
					.getService(IHandlerService.class);
			serv.activateHandler(IFunctionCommandConstants.NEW_FUNCTION,
					new NewFunctionHandler(funcTree));
			serv.activateHandler(IWorkbenchCommandConstants.EDIT_DELETE,
					new DeleteHandler(funcTree));
			serv.activateHandler(IWorkbenchCommandConstants.EDIT_COPY,
					new CopyHandler(funcTree));
			serv.activateHandler(IWorkbenchCommandConstants.EDIT_PASTE,
					new PasteHandler(funcTree));

			MenuManager menuManager = createPopUpMenu(site);
			Composite control = ((FunctionTreeViewer) funcTree).getControl();
			control.setMenu(menuManager.createContextMenu(control));
		}
	}

	protected MenuManager createPopUpMenu(IServiceLocator site) {
		final MenuManager menuManager = new MenuManager();
		menuManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(site, null,
						IFunctionCommandConstants.NEW_FUNCTION,
						CommandContributionItem.STYLE_PUSH)));
		menuManager.add(new Separator());
		menuManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(site, null,
						IWorkbenchCommandConstants.EDIT_DELETE,
						CommandContributionItem.STYLE_PUSH)));
		menuManager.add(new RemoveFunctionAction(funcTree));
		menuManager.add(new UpdateFittedParamAction(funcTree));
		menuManager.add(new DuplicateFunctionAction(funcTree));
		menuManager.add(new ToggleFixedAction(funcTree));
		menuManager.add(new Separator());
		menuManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(site, null,
						IWorkbenchCommandConstants.EDIT_COPY,
						CommandContributionItem.STYLE_PUSH)));
		menuManager.add(new CommandContributionItem(
				new CommandContributionItemParameter(site, null,
						IWorkbenchCommandConstants.EDIT_PASTE,
						CommandContributionItem.STYLE_PUSH)));

		return menuManager;
	}

	/**
	 * Get the underlying FunctionTreeViewer.
	 * <p>
	 * Generally this should not be obtained other than for accessing
	 * SWT/Eclipse event/functionality directly. For example, listeners must be
	 * added directly on FunctionWidget2 otherwise the added functionality
	 * FunctionWidget2 will not generate the expected events.
	 *
	 * @return
	 */
	public IFunctionViewer getFunctionViewer() {
		return funcTree;
	}

	@Override
	public void expandAll(){
		funcTree.expandAll();
	}

	@Override
	public void setInput(Add compFunction) {
		funcTree.setInput(compFunction);
	}

	@Override
	public void refresh() {
		funcTree.refresh();
	}

	@Override
	public void setFittedInput(Add resultFunction) {
		funcTree.setFittedInput(resultFunction);
	}

	@Override
	public boolean isValid() {
		return funcTree.isValid();
	}

	@Override
	public boolean isFittedValid() {
		return funcTree.isValid();
	}

	@Override
	public void addModelModifiedListener(IModelModifiedListener listener) {
		if (modelModifiedListeners == null) {
			modelModifiedListeners = new ListenerList();
			modelModifiedListener = new IModelModifiedListener() {

				@Override
				public void functionModified(IFunctionModifiedEvent event) {
					FunctionFittingWidget.this.fireFunctionModified(event);
				}

				@Override
				public void parameterModified(IParameterModifiedEvent event) {
					FunctionFittingWidget.this.fireParameterModified(event);
				}

				@Override
				public void fittedFunctionInvalidated(
						IFittedFunctionInvalidatedEvent event) {
					FunctionFittingWidget.this.fireFittedFunctionInvalidated(event);
				}

			};
			funcTree.addModelModifiedListener(modelModifiedListener);
		}
		modelModifiedListeners.add(listener);
	}

	@Override
	public void removeModelModifiedListener(IModelModifiedListener listener) {
		if (modelModifiedListeners == null) {
			return;
		}
		modelModifiedListeners.remove(listener);
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

	protected void fireFittedFunctionInvalidated(
			IFittedFunctionInvalidatedEvent event) {
		if (modelModifiedListeners == null) {
			return;
		}
		for (Object listener : modelModifiedListeners.getListeners()) {
			((IModelModifiedListener) listener)
					.fittedFunctionInvalidated(event);
		}
	}

	@Override
	public void dispose() {
		if (modelModifiedListener != null) {
			funcTree.removeModelModifiedListener(modelModifiedListener);
			modelModifiedListener = null;
		}
	}

	@Override
	public boolean setFocus() {
		return setFocusToTreeViewer();
	}

	/**
	 * Set the focus to the Tree Viewer within the Function Widget
	 */
	public boolean setFocusToTreeViewer() {
		return ((FunctionTreeViewer) funcTree).getControl().setFocus();
	}

	/**
	 * Set the focus to the details composite
	 */
	public boolean setFocusToDetails() {
		return detailsPanelContainer.setFocus();
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		funcTree.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return funcTree.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		funcTree.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		funcTree.setSelection(selection);
	}

	@Override
	public IFunction getSelectedFunction() {
		return funcTree.getSelectedFunction();
	}

	@Override
	public IParameter getSelectedParameter() {
		return funcTree.getSelectedParameter();
	}

	@Override
	public int getSelectedParameterIndex() {
		return funcTree.getSelectedParameterIndex();
	}

	@Override
	public IOperator getSelectedFunctionParent() {
		return funcTree.getSelectedFunctionParent();
	}

	@Override
	public int getSelectedFunctionParentIndex() {
		return funcTree.getSelectedFunctionParentIndex();
	}
}
