package org.dawnsci.mapping.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.MappedData;
import org.dawnsci.mapping.ui.datamodel.MappedDataBlock;
import org.dawnsci.mapping.ui.datamodel.MappedDataFile;
import org.dawnsci.multidimensional.ui.hyper.Hyper4DImageReducer;
import org.dawnsci.multidimensional.ui.hyper.Hyper4DMapReducer;
import org.dawnsci.multidimensional.ui.hyper.HyperComponent;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicDialog extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(DynamicDialog.class);

	HyperComponent component;
	MappedDataBlock block;
	
	List<MappedData> stashed = new ArrayList<>();
	
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
		
		int[] order = new int[shape.length];
		if (shape.length == 4) {
			//default Y1,X1,Y2,X2
			order[3] = block.getDataDimensions()[1];
			order[2] = block.getDataDimensions()[0];
			order[0] = block.getyDim();
			order[1] = block.getxDim();
		} else {
			//default Y,X,Z
			order[2] = block.getDataDimensions()[0];
			order[1] = block.getyDim();
			order[0] = block.getxDim();
		}
		
		

		List<IDataset> dsl = new ArrayList<>();
		try {
			dsl.add(axes[order[0]] == null ? DatasetFactory.createRange(IntegerDataset.class, shape[order[0]]) : axes[order[0]].getSlice().squeeze());
			dsl.add(axes[order[1]] == null ? DatasetFactory.createRange(IntegerDataset.class, shape[order[1]]) : axes[order[1]].getSlice().squeeze());
			dsl.add(axes[order[2]] == null ? DatasetFactory.createRange(IntegerDataset.class, shape[order[2]]) : axes[order[2]].getSlice().squeeze());
			if (shape.length ==  4) {
				dsl.add(axes[order[3]] == null ? DatasetFactory.createRange(IntegerDataset.class, shape[order[3]]) : axes[order[3]].getSlice().squeeze());
			}
		} catch (DatasetException e) {
			logger.error("Could not slice dataset",e);
		}

		if (shape.length == 3) {
			component.setData(lazy, dsl, slices, order);
		} else {
			component.setData(lazy, dsl, slices, order,new Hyper4DMapReducer(), new Hyper4DImageReducer());
		}
		
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
	
	@Override
	public boolean close() {
		component.dispose();
		return super.close();
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		Button axisButton = createButton(parent, IDialogConstants.NO_ID, "Keep Map", false);
		
		axisButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				IDataset d = component.getLeftData();
				MappedData m = new MappedData(d.getName(), d, block, block.getPath(), false);
				stashed.add(m);
				
			}

		});
		
		createButton(parent, IDialogConstants.OK_ID,"Close", false);
	}
	
	public List<MappedData> getStashedMaps() {
		return stashed;
	}
	
	public static void runDialog(Shell shell, MappedDataBlock block, IMapFileController controller) {
		DynamicDialog dialog = new DynamicDialog(shell, block);
		dialog.open();

		List<MappedData> stashedMaps = dialog.getStashedMaps();

		if (!dialog.getStashedMaps().isEmpty()) {
			MappedDataFile pf = block.getParentFile();
			for (MappedData m : stashedMaps) {
				pf.addMapObject(m.toString(), m);
			}

			controller.registerUpdates(pf);
		}
	}
}
