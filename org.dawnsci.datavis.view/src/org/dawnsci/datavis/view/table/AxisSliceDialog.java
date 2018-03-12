package org.dawnsci.datavis.view.table;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.NDimensions;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;
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
	private Text tmin;
	private Text tmax;
	private Dataset axis;
	private Integer start;
	private Integer stop;
	
	protected AxisSliceDialog(Shell parentShell, NDimensions nDims, int dim) {
		super(parentShell);
		this.dim = dim;
		this.nDimensions = nDims;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(4, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label l1 = new Label(container, SWT.NONE);
		l1.setText("Start");
		tmin = new Text(container,SWT.BORDER);

		Label l2 = new Label(container, SWT.NONE);
		l2.setText("Stop");
		tmax = new Text(container,SWT.BORDER);
		
		BundleContext bundleContext =
                FrameworkUtil.
                getBundle(this.getClass()).
                getBundleContext();
		
		IFileController fileController = (IFileController)bundleContext.getService(bundleContext.getServiceReference(IFileController.class));
		
		DataOptions d = fileController.getCurrentDataOption();
		
		SliceND slice = nDimensions.buildSliceND();
		slice.setSlice(dim, 0, null, 1);
		
		ILazyDataset sliceView = d.getLazyDataset().getSliceView(slice);
		AxesMetadata meta = sliceView.getFirstMetadata(AxesMetadata.class);
		ILazyDataset ax = meta.getAxes()[dim];
		try {
			IDataset dax = ax.getSlice();
			axis = DatasetUtils.convertToDataset(dax.squeeze());
			tmin.setText(axis.getStringAbs(0));
			tmax.setText(axis.getStringAbs(axis.getSize()-1));
		} catch (DatasetException e) {

		}
	
		return container;
	}
	
	@Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Set Start:Stop from Axis");
//	    newShell.setImage(image = Activator.getImageDescriptor("icons/spectrum.png").createImage());
	  }
	
	private Double parseDouble(Text widget) {
		String text = widget.getText();
		
		try {
			return Double.parseDouble(text);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	
	protected void okPressed() {
		
		Double min = parseDouble(tmin);
		Double max = parseDouble(tmax);
		
		if (min != null) {
			start = Maths.abs(Maths.subtract(axis, min)).argMin();
		}
		
		if (max != null) {
			stop = Maths.abs(Maths.subtract(axis, max)).argMin();
			stop.toString();
		}
		
		if (start != null && stop != null && start > stop) {
			Integer tmp = stop;
			stop = start;
			start = tmp;
		}

		super.okPressed();
	}
	
	public Integer getStart() {
		return start;
	}

	public Integer getStop() {
		return stop;
	}
}
