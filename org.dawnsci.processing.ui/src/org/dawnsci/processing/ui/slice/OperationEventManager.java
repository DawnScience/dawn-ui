package org.dawnsci.processing.ui.slice;

import java.util.HashSet;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;

public class OperationEventManager {

	private HashSet<IOperationGUIRunnerListener> listeners;
	
	public OperationEventManager() {
		listeners = new HashSet<IOperationGUIRunnerListener>();
	}
	
	public void addOperationRunnerListener(IOperationGUIRunnerListener listener) {
		listeners.add(listener);
	}
	
	public void removeOperationRunnerListener(IOperationGUIRunnerListener listener) {
		listeners.remove(listener);
	}
	
	public void sendInputDataUpdate(IOperationInputData data) {
		for (IOperationGUIRunnerListener l : listeners) {
			l.inputDataAvaliable(data);
		}
	}
	
	public void sendErrorUpdate(OperationException op) {
		for (IOperationGUIRunnerListener l : listeners) {
			l.inErrorState(op);
		}
	}
	
	public void sendInitialDataUpdate(IDataset data) {
		for (IOperationGUIRunnerListener l : listeners) {
			l.initialDataAvaliable(data);
		}
	}
	
	public void requestUpdate() {
		for (IOperationGUIRunnerListener l : listeners) {
			l.updateRequested();
		}
	}
}
