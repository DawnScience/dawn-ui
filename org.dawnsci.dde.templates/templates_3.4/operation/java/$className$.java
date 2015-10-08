package $packageName$;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;

import uk.ac.diamond.scisoft.analysis.processing.operations.AbstractMathsOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.ValueModel;

public class $className$ extends AbstractMathsOperation<ValueModel, OperationData> {

	protected IDataset operation(IDataset a, Object value) {
		return a; // TODO: Operate on "a" and "value"
	}

	@Override
	public String getId() {
		return "$operationId$";
	}

	@Override
    public String getName() {
		return "$operationName$";
	}

}