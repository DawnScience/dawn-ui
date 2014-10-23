package org.dawnsci.processing.ui.slice;

import org.dawnsci.common.widgets.table.SeriesTable;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.swt.widgets.Display;

public class OperationInformerImpl implements IOperationErrorInformer {

	SeriesTable table;
	OperationException e;
	IDataset slice;
	
	public OperationInformerImpl(SeriesTable table) {

		this.table = table;
	}
	
	
	@Override
	public void setInErrorState(OperationException e) {
		this.e = e;
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				table.refreshTable();
				
			}
		});
		

	}

	@Override
	public OperationException getInErrorState() {
		return this.e;
	}


	@Override
	public IDataset getTestData() {
		return slice;
	}


	@Override
	public void setTestData(IDataset test) {
		slice = test;
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				table.refreshTable();
				
			}
		});
	}

}
