package org.dawnsci.processing.ui.slice;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.processing.ui.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationExporter;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import uk.ac.diamond.scisoft.analysis.processing.runner.OperationExporterFactory;

/**
 * Class to use for exporting any pipeline to a moml file.
 * @author fcp94556
 *
 */
public abstract class OperationExportAction extends Action{

	public OperationExportAction() {
		super("Export to Workflow", Activator.getImageDescriptor("icons/flow.png"));
	}
	
	public void run() {
		
		IOperationContext context = createContext();
		
		// TODO Add filter to enforce .moml
		IFile newFile = WorkspaceResourceDialog.openNewFile(Display.getDefault().getActiveShell(), "Export Pipeline to Workflows", 
				                           "Please select a workflow file to export the operation pipeline to.", null, null);
	
	    if (newFile==null) return;
	    
	    if (!newFile.getName().toLowerCase().endsWith(".moml")) {
	    	MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Must end with '.moml.", "Please make sure that the file exported has the extension '.moml'.");
	        return;
	    }
	    
	    if (newFile.exists()) {
	    	boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Overwrite", "Would you like to overwrite '"+newFile.getName()+"'?");
	        if (!ok) return;	
	    }
	    
	    try {
		    final IOperationExporter exp = OperationExporterFactory.getExporter(ExecutionType.GRAPH);
		    exp.init(context);
		    exp.export(newFile.getLocation().toOSString());
			
			newFile.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
			
			EclipseUtils.openEditor(newFile);
			
	    } catch (Exception e) {
			
			ErrorDialog dialog = new ErrorDialog(Display.getDefault().getActiveShell(), "Cannot Export to Workflows",
					                             "", new Status(IStatus.ERROR, "org.dawnsci.processing.ui", e.getMessage(), e), SWT.NONE);
		
		    dialog.open();
		}
	}
	
	/**
	 * Implement to return a fully configured 
	 * @return
	 */
	public abstract IOperationContext createContext();
}
