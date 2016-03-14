package $packageName$;

import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import uk.ac.diamond.scisoft.analysis.processing.operations.EmptyModel;

public class $className$ extends AbstractOperation<EmptyModel, OperationData> {

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {

		return new OperationData(input);
	}

	@Override
	public String getId() {
		return "$operationId$";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ANY;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.SAME;
	}

	

}