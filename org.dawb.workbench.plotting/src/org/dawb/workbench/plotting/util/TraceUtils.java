package org.dawb.workbench.plotting.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.csstudio.swt.xygraph.dataprovider.IDataProvider;
import org.csstudio.swt.xygraph.dataprovider.ISample;
import org.dawb.common.util.io.IFileUtils;
import org.dawb.workbench.plotting.system.LineTraceImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class TraceUtils {


	public static void doExport(LineTraceImpl trace) throws Exception {

		IFile exportTo = WorkspaceResourceDialog.openNewFile(Display.getDefault().getActiveShell(), 
				"Create file to export to", 
				"Export data from "+trace.getName()+"'", 
				null, null);

		if (exportTo==null) return;
		
		final IFile dat  = IFileUtils.getUniqueIFile(exportTo.getParent(), exportTo.getName(), "dat");
		
		final StringBuilder contents = new StringBuilder();
		final IDataProvider prov = trace.getDataProvider();
		final NumberFormat format = new DecimalFormat("##0.#####E0");
		for (int i = 0; i < prov.getSize(); i++) {
			final ISample isample = prov.getSample(i);
			contents.append(format.format(isample.getXValue()));
			contents.append("\t");
			contents.append(format.format(isample.getYValue()));
			contents.append("\n");
		}

		InputStream stream = new ByteArrayInputStream(contents.toString().getBytes());
		dat.create(stream, true, new NullProgressMonitor());
		dat.getParent().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName());
		if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(dat.getName()+".txt");
		page.openEditor(new FileEditorInput(dat), desc.getId());
	}

}
