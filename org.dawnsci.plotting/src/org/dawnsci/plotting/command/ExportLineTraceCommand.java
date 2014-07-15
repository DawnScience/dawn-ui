package org.dawnsci.plotting.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.dawb.common.util.io.IFileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

public class ExportLineTraceCommand extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			final ILineTrace trace = (ILineTrace)event.getApplicationContext();
			
			IFile exportTo = WorkspaceResourceDialog.openNewFile(Display.getDefault().getActiveShell(), 
					"Create file to export to", 
					"Export data from "+trace.getName()+"'", 
					null, null);
	
			if (exportTo==null) return Boolean.FALSE;
			
			final IFile dat  = IFileUtils.getUniqueIFile(exportTo.getParent(), exportTo.getName(), "dat");
			
			final StringBuilder contents = new StringBuilder();
			final IDataset x = trace.getXData();
			final IDataset y = trace.getXData();
			final NumberFormat format = new DecimalFormat("##0.#####E0");
			for (int i = 0; i < x.getSize(); i++) {
				contents.append(format.format(x.getDouble(i)));
				contents.append("\t");
				contents.append(format.format(y.getDouble(i)));
				contents.append("\n");
			}
	
			InputStream stream = new ByteArrayInputStream(contents.toString().getBytes());
			dat.create(stream, true, new NullProgressMonitor());
			dat.getParent().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
	
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName());
			if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName()+".txt");
			page.openEditor(new FileEditorInput(dat), desc.getId());
			
			return Boolean.TRUE;
		} catch (Throwable ne) {
			throw new ExecutionException("Cannot export trace!", ne);
		}
	}

}
