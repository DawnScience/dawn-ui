package org.dawnsci.plotting.tools.expressions;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.text.contentassist.ContentAssistant;

public class ExpressionTool extends AbstractToolPage {
	
	ITextViewer consoleViewer;
	ExpressionConsole console;
	Composite mainComposite;
	Map<String,String> variables;
	TableViewer viewer;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		
		variables = new HashMap<String,String>();
		mainComposite = new Composite(parent, SWT.None);
		mainComposite.setLayout(new GridLayout());;
		console = new ExpressionConsole();
		console.addListener(new IExpressionVariableListener() {
			
			@Override
			public void variableCreated(ExpressionVariableEvent evt) {
				for (String name : evt.getNames()) {
					variables.put(name, console.getVariableValue(name));
				}
							
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						viewer.refresh();		
					}
				});
			}
		});
		
		Composite  composite = new Composite(mainComposite, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		consoleViewer = new ExpressionConsoleViewer(composite, console.getConsole());
		
//		ContentAssistant contentAssistant = new ContentAssistant();
//		
//		contentAssistant.install(consoleViewer);
		
		
		Map<String,Object> functions = console.getFunctions();
		functions.put("plt", getPlottingSystem());
		
		Composite tableComposite = new Composite(mainComposite, SWT.NONE);
		tableComposite.setLayout(new FillLayout());
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer = new TableViewer(tableComposite, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		createColumns();

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 

		viewer.setContentProvider(getContentProvider());
		
		Collection<ITrace> traces =  getPlottingSystem().getTraces(IImageTrace.class);
		
		int i = 0;
		
		for (ITrace trace : traces) {
			console.addToContext("trace" + String.valueOf(i), trace.getData());
			variables.put("trace" + String.valueOf(i), trace.getData().toString());
			i++;
		}
		viewer.setInput(variables);
		console.setFunctions(functions);
	}

	@Override
	public Control getControl() {
		if (mainComposite==null) return null;
		return mainComposite;
	}

	@Override
	public void setFocus() {
		if (consoleViewer != null) {
			mainComposite.setFocus();
		}
	}
	
	private void createColumns(){
		
		TableViewerColumn var   = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Name");
		var.getColumn().setWidth(150);
		var.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof NameValuePair) {
					return ((NameValuePair)element).name;
				}
				return null;
			}
		});

		var   = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Data");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof NameValuePair) {
					return ((NameValuePair)element).value;
				}
				return null;
			}
		});
	}
	
	private IStructuredContentProvider getContentProvider() {
		return new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {

				NameValuePair[] list = new NameValuePair[variables.size()];

				int i = 0;
				for(String key : variables.keySet()) {
					list[i] = new NameValuePair(key, variables.get(key));
					i++;
				}

				return list;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		};
	}
	
	private class NameValuePair {
		
		public String name;
		public String value;
		
		public NameValuePair(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}
}
