package org.dawnsci.datavis.view.table;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.NDimensions;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class AxisSliceDialog extends Dialog {

	private NDimensions nDimensions;
	private int dim = 0;
	
	protected AxisSliceDialog(Shell parentShell, NDimensions nDims, int dim) {
		super(parentShell);
		this.dim = dim;
		this.nDimensions = nDims;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(3, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		String axis = nDimensions.getAxis(dim);
		Label l1 = new Label(container, SWT.NONE);
		l1.setText("Start");
		Text tmin = new Text(container,SWT.NONE);

		Label l2 = new Label(container, SWT.NONE);
		l2.setText("Stop");
		Text tmax = new Text(container,SWT.NONE);
		
		if (NDimensions.INDICES.equals(nDimensions.getAxis(dim))){
			System.out.println();
		}
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IFileController fileController = (IFileController)bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
		
		DataOptions d = fileController.getCurrentDataOption();
		
		ILazyDataset sliceView = d.getLazyDataset().getSliceView(nDimensions.buildSliceND());
		AxesMetadata meta = sliceView.getFirstMetadata(AxesMetadata.class);
		ILazyDataset ax = meta.getAxes()[dim];
		try {
			IDataset dax = ax.getSlice();
			Dataset squeeze = DatasetUtils.convertToDataset(dax.squeeze());
			tmin.setText(squeeze.getStringAbs(0));
			tmax.setText(squeeze.getStringAbs(squeeze.getSize()-1));
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return container;
	}
	
	@Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Set Start:Stop from Axis");
//	    newShell.setImage(image = Activator.getImageDescriptor("icons/spectrum.png").createImage());
	  }
	

}
