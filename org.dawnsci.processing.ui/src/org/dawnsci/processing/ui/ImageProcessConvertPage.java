package org.dawnsci.processing.ui;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.wizard.AbstractSliceConversionPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public class ImageProcessConvertPage extends AbstractSliceConversionPage  {

	IWorkbench workbench;
	
	public ImageProcessConvertPage() {
		super("wizardPage", "Page for processing HDF5 data.", null);
		setTitle("Process");
		setDirectory(true);
		setFileLabel("Export to");
	}

	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void createAdvanced(Composite parent) {
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");

	}

	public void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}
	
	@Override
	public boolean isPageComplete() {
    	return true;
    }
	
	@Override
	public void setContext(IConversionContext context) {
		
		super.setContext(context);
		if (context.getOutputPath()!=null) {
			setPath(context.getOutputPath());
		}
	}
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		return context;
		
//		if (workbench == null) return context;
//		
//		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
//		IViewPart view = page.findView("org.dawnsci.processing.ui.processingView");
//		
//		Object ob = view.getAdapter(IOperation.class);
//		IOperation[] ops = null;
//		
//		if (ob.getClass().isArray() && Array.get(ob, 0) instanceof IOperation) {
//			ops = (IOperation[])ob;
//		}
//		
//		if (ops != null) {
//			
//			final IOperation[] fop = ops;
//			
//			context.setUserObject(new IProcessingConversionInfo() {
//
//				@Override
//				public IOperation[] getOperationSeries() {
//					return fop;
//				}
//
//				@Override
//				public IExecutionVisitor getExecutionVisitor(String fileName) {
//					//return new HierarchicalFileExecutionVisitor(fileName);
//					return null;
//				}
//
//			});
//		}
//		return context;
	}
	
}
