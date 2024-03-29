/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction.JexlExpressionFunctionError;

public class FunctionLabelProvider extends BaseLabelProvider implements
		IStyledLabelProvider {

	private static final String CURVE = "chart_curve.png";
	private static final String LINK = "link.png";
	private static final String BULLET_BLUE = "bullet_blue.png";
	private static final String BULLET_ORANGE = "bullet_orange.png";

	private static void putIfAbsent(String path) {
		if (IMAGE_REG.get(path) == null) {
			IMAGE_REG.put(path, Activator.getImage(path));
		}
	}
	private static final ImageRegistry IMAGE_REG;
	static {
		IMAGE_REG = JFaceResources.getImageRegistry();
		putIfAbsent(CURVE);
		putIfAbsent(LINK);
		putIfAbsent(BULLET_BLUE);
		putIfAbsent(BULLET_ORANGE);
	}

	public static final Styler ERROR_STYLER = new Styler() {
		@Override
		public void applyStyles(TextStyle textStyle) {
			Font boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
			textStyle.font= boldFont;
			textStyle.underline = true;
			textStyle.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
			textStyle.foreground = JFaceResources.getColorRegistry().get(
					JFacePreferences.ERROR_COLOR);
		}
	};

	@Override
	public Image getImage(Object element) {
		if (element instanceof AddNewFunctionModel) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ADD);
		} else if (element instanceof SetFunctionModel) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ADD);
		} else if (element instanceof FunctionModel) {
			if (((FunctionModel)element).getFunction() instanceof JexlExpressionFunction)

				return IMAGE_REG.get(LINK);
			return IMAGE_REG.get(CURVE);
		} else if (element instanceof OperatorModel) {
			return IMAGE_REG.get(BULLET_BLUE);
		} else if (element instanceof ParameterModel) {
			return IMAGE_REG.get(BULLET_ORANGE);
		}

		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof AddNewFunctionModel) {
			return new StyledString("Add new function",
					StyledString.COUNTER_STYLER);
		} else if (element instanceof SetFunctionModel) {
			return new StyledString("Set function", StyledString.COUNTER_STYLER);
		} else if (element instanceof FunctionModel) {
			FunctionModel functionModel = (FunctionModel) element;
			IFunction function = functionModel.getFunction();
			if (function instanceof JexlExpressionFunction) {
				return getJexlStyledText((JexlExpressionFunction) function);
			} else {
				return new StyledString(function.getName());
			}
		} else if (element instanceof OperatorModel) {
			OperatorModel operatorModel = (OperatorModel) element;
			IOperator operator = operatorModel.getOperator();
			return new StyledString(operator.getName());
		} else if (element instanceof ParameterModel) {
			ParameterModel parameterModel = (ParameterModel) element;
			return new StyledString(parameterModel.getParameter().getName());
		}

		return new StyledString("");
	}

	public StyledString getJexlStyledText(
			JexlExpressionFunction jexlExpressionFunction) {
		String expression = jexlExpressionFunction.getExpression();
		if (expression == null)
			expression = "";
		if (jexlExpressionFunction.getExpressionError() == JexlExpressionFunctionError.NO_ERROR) {
			return new StyledString(expression);
		} else {

			return new StyledString(expression, ERROR_STYLER);
		}
	}

}
