package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.slicing.tools.hyper.HyperComponent;
import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class DynamicDialog extends Dialog {

	HyperComponent component;
	MappedDataBlock block;
	
	public DynamicDialog(Shell parentShell, MappedDataBlock block) {
		super(parentShell);
		this.block = block;
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		component = new HyperComponent(null);
		component.createControl(container);
		ILazyDataset lazy = block.getLazy();
		AxesMetadata metadata = lazy.getFirstMetadata(AxesMetadata.class);
		ILazyDataset[] axes = metadata.getAxes();
		int[] shape = lazy.getShape();
		
		Slice[] slices = new Slice[shape.length];
		for (int i = 0; i< shape.length; i++) slices[i] = new Slice(0, shape[i]);
		
		int[] order = new int[3];
		order[2] = block.getDataDimensions()[0];
		order[1] = block.getyDim();
		order[0] = block.getxDim();

		List<IDataset> dsl = new ArrayList<>();
		try {
			dsl.add(axes[order[0]] == null ? DatasetFactory.createRange(shape[order[0]], Dataset.INT32) : axes[order[0]].getSlice().squeeze());
			dsl.add(axes[order[1]] == null ? DatasetFactory.createRange(shape[order[1]], Dataset.INT32) : axes[order[1]].getSlice().squeeze());
			dsl.add(axes[order[2]] == null ? DatasetFactory.createRange(shape[order[2]], Dataset.INT32) : axes[order[2]].getSlice().squeeze());
		} catch (DatasetException e) {
			e.printStackTrace();
		}

		component.setData(lazy, dsl, slices, order);
		
		return container;
	}

	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Dynamic View");
	}

	@Override
	protected Point getInitialSize() {
		Rectangle bounds = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell().getBounds();
		return new Point((int)(bounds.width*0.8),(int)(bounds.height*0.8));
	}
	
	
	@Override
	  protected boolean isResizable() {
	    return true;
	  }
	
}
