package org.dawb.workbench.ui.data;


import org.dawb.common.services.IExpressionObject;
import org.dawb.common.services.IExpressionObjectService;
import org.dawb.common.services.IVariableManager;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.workbench.ui.transferable.TransferableDataObject;
import org.dawnsci.common.widgets.celleditor.ExpressionFunctionProposalProvider;
import org.dawnsci.common.widgets.celleditor.ExpresionCellEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class ExpressionEditingSupport extends EditingSupport {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionEditingSupport.class);
	private TextCellEditor cellEditor;
	private boolean isExpressionActive   = false;
	private IVariableManager manager;
	

	public ExpressionEditingSupport(ColumnViewer viewer, IVariableManager manager) throws Exception {
		super(viewer);
		
		this.manager  = manager;
		IExpressionObjectService service  = (IExpressionObjectService)ServiceManager.getService(IExpressionObjectService.class);
		IExpressionObject exObj = service.createExpressionObject(null,null,"");
		
		KeyStroke keystroke = null;
		
		try {
			keystroke = KeyStroke.getInstance("Ctrl+Space");
		} catch (ParseException e) {

		}
		
		if (exObj != null) {
			IContentProposalProvider contentProposalProvider = new ExpressionFunctionProposalProvider(exObj.getFunctions());
			cellEditor = new ExpresionCellEditor((Composite)getViewer().getControl(), contentProposalProvider, keystroke, new char[]{':'});
		} else {
			cellEditor = new TextCellEditor((Composite)getViewer().getControl());
			logger.error("Expression Object service returned null");
		}
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (!isExpressionActive) return false;
		return (element instanceof TransferableDataObject) && ((ITransferableDataObject)element).isExpression();
	}

	@Override
	protected Object getValue(Object element) {
		ITransferableDataObject check = (ITransferableDataObject)element;
		String text = check.getExpression().getExpressionString();
		if (text==null) return "";
		return text;
	}

	@Override
	protected void setValue(final Object element, final Object value) {
		
		final Job job = new Job("Please wait while the expression, '"+value+"', is evaluated.") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final TransferableDataObject check = (TransferableDataObject)element;
				try {
					String         expression   = (String)value;
					final IExpressionObject ob   = check.getExpression();
					if (expression!=null) expression = expression.trim();
					if (value==null || "".equals(expression))  return Status.CANCEL_STATUS;
					if (value.equals(ob.getExpressionString()))return Status.CANCEL_STATUS;
					
					ob.setExpressionString(expression);
					check.setChecked(false); // selectionChanged(...) puts it to true
					check.getLazyData(new ProgressMonitorWrapper(monitor));
					
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							// FIXME Hard coded to PlotDataComponent
							((PlotDataComponent)ExpressionEditingSupport.this.manager).selectionChanged(check, true);
							ExpressionEditingSupport.this.manager.saveExpressions();
							getViewer().refresh();
						}
					});

				} catch (Exception e) {
					logger.error("Cannot set expression "+check.getName(), e);

				} 
				return Status.OK_STATUS;
			}
		};
		
		job.setPriority(Job.INTERACTIVE);
		job.setUser(true);
		job.schedule();
	}

	public boolean isExpressionActive() {
		return isExpressionActive;
	}

	public void setExpressionActive(boolean isExpressionActive) {
		this.isExpressionActive = isExpressionActive;
	}

}
