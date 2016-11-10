package org.dawnsci.processing.ui.slice;

import java.util.EventListener;

import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.january.dataset.IDataset;

public interface IOperationGUIRunnerListener extends EventListener {
	
	public void initialDataAvaliable(IDataset data);

	public void inputDataAvaliable(IOperationInputData data);
	
	public void inErrorState(OperationException operation);
	
	public void updateRequested();
	
	public class Stub implements IOperationGUIRunnerListener {

		@Override
		public void initialDataAvaliable(IDataset data) {
		}

		@Override
		public void inputDataAvaliable(IOperationInputData data) {
		}

		@Override
		public void inErrorState(OperationException operation) {
		}

		@Override
		public void updateRequested() {
		}
		
	}
	
}
