/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.history;

import org.dawb.common.services.IExpressionObject;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * TODO This class is actually generic, could be used elsewhere.
 * @author fcp94556
 *
 */
public class ExpressionValueLabelProvider extends ColumnLabelProvider {
	
	private Color BLUE, RED;

	ExpressionValueLabelProvider() {
		BLUE = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
		RED  = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	}

	@Override
	public String getText(Object ob) {
        return ((HistoryBean)ob).getVariable();
	}
	
	@Override
	public Color getForeground(Object ob) {
		final  IExpressionObject o = ((HistoryBean)ob).getExpression();
		if (o==null) return null;
		return o.isValid(new IMonitor.Stub()) ? BLUE : RED;
	}
}
