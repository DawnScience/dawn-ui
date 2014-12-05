package org.dawnsci.processing.ui.slice;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationExporter;
import org.eclipse.dawnsci.analysis.api.processing.IOperationExporterService;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

/**
 * Class to use for exporting any pipeline to a moml file.
 * @author fcp94556
 *
 */
public class OperationExportAction extends Action{
	
	private static IOperationExporterService eservice;
	public static void setOperationExporter(IOperationExporterService s) {
		eservice = s;
	}

	public OperationExportAction() {
		super();
	}
	
	public OperationExportAction(String label, ImageDescriptor icon) {
		super(label, icon);
	}
	
	public void run() {
		
		IOperationContext context = createContext();
		
		// TODO Add filter to enforce .moml
		IFile newFile = WorkspaceResourceDialog.openNewFile(Display.getDefault().getActiveShell(), "Export Pipeline to Workflows", 
				                           "Please create a workflow file (*.moml) to export the operation pipeline to.", null, null);
	
	    if (newFile==null) return;
	    
	    if (!newFile.getName().toLowerCase().endsWith(".moml")) {
	    	newFile = newFile.getParent().getFile(new Path(newFile.getName()+".moml"));
	    }
	    
	    if (newFile.exists()) {
	    	boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Overwrite", "Would you like to overwrite '"+newFile.getName()+"'?");
	        if (!ok) return;	
	    }
	    
	    try {
		    final IOperationExporter exp = eservice.getExporter(ExecutionType.GRAPH);
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
	 * Override to return a fully configured.
	 * This is not abstract because OSGI injects a service here which cannot happen to an abstract class.
	 * @return
	 */
	public IOperationContext createContext() {
		return null;
	}
}
