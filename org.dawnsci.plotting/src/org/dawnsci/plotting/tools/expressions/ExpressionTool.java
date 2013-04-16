package org.dawnsci.plotting.tools.expressions;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.jexl2.JexlEngine;
import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.text.ITextViewer;

public class ExpressionTool extends AbstractToolPage {
	
	ITextViewer viewer;
	JexlEngine engine;
	ExpressionConsole console;
	Composite composite;

	@Override
	public ToolPageRole getToolPageRole() {
		// TODO Auto-generated method stub
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		console = new ExpressionConsole();
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, true));
		
		Composite cComposite = new Composite(composite, SWT.NONE);
		cComposite.setLayout(new FillLayout());
		cComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		viewer = new ExpressionConsoleViewer(composite, console.getConsole());
		Map<String,Object> functions = console.getFunctions();
		functions.put("plt", getPlottingSystem());

		Collection<ITrace> traces =  getPlottingSystem().getTraces(IImageTrace.class);
		
		int i = 0;
		
		for (ITrace trace : traces) {
			console.addToContext("trace" + String.valueOf(i), trace.getData());
			i++;
		}
		
		console.setFuctions(functions);
	}

	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}

	@Override
	public void setFocus() {
		if (viewer != null) {
			composite.setFocus();
		}
		
	}
}
