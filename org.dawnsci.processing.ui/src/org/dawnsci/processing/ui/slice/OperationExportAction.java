package org.dawnsci.processing.ui.slice;

import org.dawnsci.processing.ui.Activator;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.jface.action.Action;

/**
 * Class to use for exporting any pipeline to a moml file.
 * @author fcp94556
 *
 */
public abstract class OperationExportAction extends Action {

	public OperationExportAction() {
		super("Export to Workflow", Activator.getImageDescriptor("icons/flow.png"));
	}
	
	@Override
	public void run() {
		
		IOperationContext context = createContext();
		
	}
	
	public abstract IOperationContext createContext();
}
