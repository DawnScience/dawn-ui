package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.slicing.tools.hyper.HyperComponent;
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
		
		Slice[] slices = new Slice[lazy.getRank()];
		for (int i = 0; i< lazy.getRank(); i++) slices[i] = new Slice(0, lazy.getShape()[i]);
		
		int[] order = new int[3];
		order[2] = block.getDataDimensions()[0];
		order[1] = block.getyDim();
		order[0] = block.getxDim();

		List<IDataset> dsl = new ArrayList<>();
		dsl.add(axes[order[0]] == null ? DatasetFactory.createRange(lazy.getShape()[order[0]], Dataset.INT32) : axes[order[0]].getSlice().squeeze());
		dsl.add(axes[order[1]] == null ? DatasetFactory.createRange(lazy.getShape()[order[1]], Dataset.INT32) : axes[order[1]].getSlice().squeeze());
		dsl.add(axes[order[2]] == null ? DatasetFactory.createRange(lazy.getShape()[order[2]], Dataset.INT32) : axes[order[2]].getSlice().squeeze());

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