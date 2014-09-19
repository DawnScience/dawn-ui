/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.detail;

import java.util.Collections;

import org.dawnsci.common.widgets.celleditor.OpenableContentAssistCommandAdapter;
import org.dawnsci.common.widgets.gda.function.IFittedFunctionInvalidatedEvent;
import org.dawnsci.common.widgets.gda.function.IFunctionModifiedEvent;
import org.dawnsci.common.widgets.gda.function.IModelModifiedListener;
import org.dawnsci.common.widgets.gda.function.IParameterModifiedEvent;
import org.dawnsci.common.widgets.gda.function.internal.ContentProposalLabelProvider;
import org.dawnsci.common.widgets.gda.function.internal.JexlContentProposalListener;
import org.dawnsci.common.widgets.gda.function.jexl.ExpressionFunctionProposalProvider;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionError;
import org.dawnsci.common.widgets.gda.function.jexl.JexlExpressionFunction.JexlExpressionFunctionException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JexlExpressionFunctionDetailPane implements IFunctionDetailPane {

	private final class IModelModifiedListenerImplementation implements
			IModelModifiedListener {
		@Override
		public void parameterModified(IParameterModifiedEvent event) {
			// do nothing
		}

		@Override
		public void functionModified(IFunctionModifiedEvent event) {
			if (event.getAfterFunction() == func) {
				jexlTextEditor.removeModifyListener(modifyListener);
				jexlTextEditor.setText(func.getExpression());
				jexlTextEditor.addModifyListener(modifyListener);
			}
		}

		@Override
		public void fittedFunctionInvalidated(
				IFittedFunctionInvalidatedEvent event) {
			// do nothing
		}
	}

	private final class FocusAdapterExtension extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			// On losing the focus we always refresh the model because we may
			// have deferred the refresh while the editing was ongoing
			if (displayModel != null) {
				displayModel.refreshElement();
			}
		}
	}

	private final class ModifyListenerImplementation implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent event) {
			String string = jexlTextEditor.getText();
			try {
				if (func != null) {
					func.setExpression(string);
					if (displayModel != null) {
						// We only refresh the model if the expression is valid
						// this is to make the parameters not appear/disappear
						// as the expression goes valid/invalid.
						// See the focusLost above
						if (func.getExpressionError() == JexlExpressionFunctionError.NO_ERROR) {
							displayModel.getFunctionWidget()
									.removeModelModifiedListener(
											modelModifiedListener);
							displayModel.refreshElement();
							displayModel.getFunctionWidget()
									.addModelModifiedListener(
											modelModifiedListener);
						}
					}
				}
			} catch (JexlExpressionFunctionException e) {
				// ignore error here, we handle it in the display
			}
		}
	}

	private Text jexlTextEditor;
	private JexlExpressionFunction func;
	private IDisplayModelSelection displayModel;
	private ModifyListenerImplementation modifyListener;
	private IModelModifiedListenerImplementation modelModifiedListener;
	private FocusAdapterExtension focusListener;
	private Font fxyFont;
	private OpenableContentAssistCommandAdapter contentProposalAdapter;
	private ExpressionFunctionProposalProvider proposalProvider;

	@Override
	public Control createControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		Label fxy = new Label(composite, SWT.NONE);
		fxy.setText("f(x)=");
		FontData fontData = fxy.getFont().getFontData()[0];
		fxyFont = new Font(fxy.getDisplay(), new FontData(fontData.getName(),
				fontData.getHeight() * 3 / 2, SWT.ITALIC));
		fxy.setFont(fxyFont);

		jexlTextEditor = new Text(composite, SWT.V_SCROLL | SWT.WRAP);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(jexlTextEditor);

		modifyListener = new ModifyListenerImplementation();
		jexlTextEditor.setFont(JFaceResources.getTextFont());
		jexlTextEditor.setText("");
		jexlTextEditor.addModifyListener(modifyListener);
		focusListener = new FocusAdapterExtension();
		jexlTextEditor.addFocusListener(focusListener);

		proposalProvider = new ExpressionFunctionProposalProvider(
				Collections.<String, Object> emptyMap());
		contentProposalAdapter = new OpenableContentAssistCommandAdapter(
				jexlTextEditor, new TextContentAdapter(), proposalProvider,
				null, null, true);
		contentProposalAdapter
				.setLabelProvider(new ContentProposalLabelProvider());
		contentProposalAdapter
				.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
		contentProposalAdapter
				.addContentProposalListener(new JexlContentProposalListener(jexlTextEditor));

		return composite;
	}

	@Override
	public void display(IDisplayModelSelection displayModel) {
		if (this.displayModel != null) {
			displayModel.getFunctionWidget().removeModelModifiedListener(
					modelModifiedListener);
		}

		this.displayModel = displayModel;
		Object element = displayModel.getElement();
		if (element instanceof JexlExpressionFunction) {
			func = (JexlExpressionFunction) element;

			jexlTextEditor.removeModifyListener(modifyListener);
			jexlTextEditor.setText(func.getExpression());
			jexlTextEditor.addModifyListener(modifyListener);

			modelModifiedListener = new IModelModifiedListenerImplementation();
			displayModel.getFunctionWidget().addModelModifiedListener(
					modelModifiedListener);
			proposalProvider.setProposals(func.getEngine().getFunctions());
		}
	}

	@Override
	public void dispose() {
		if (fxyFont != null) {
			fxyFont.dispose();
		}

		if (jexlTextEditor != null && !jexlTextEditor.isDisposed()) {
			if (modifyListener != null) {
				jexlTextEditor.removeModifyListener(modifyListener);
			}
			if (focusListener != null) {
				jexlTextEditor.removeFocusListener(focusListener);
			}
		}
		if (displayModel != null) {
			if (modelModifiedListener != null) {
				displayModel.getFunctionWidget().removeModelModifiedListener(
						modelModifiedListener);
			}
		}

		fxyFont = null;
		jexlTextEditor = null;
		modifyListener = null;
		focusListener = null;
		displayModel = null;
		modelModifiedListener = null;
		contentProposalAdapter = null;
		proposalProvider = null;
	}
}
