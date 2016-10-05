package $packageName$;

import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;

public class $className$ extends AbstractOperation<$className$Model, OperationData> {

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {

		DoubleDataset rand = Random.rand(input.getShape());
		rand.imultiply(model.getMyVar());
		rand.iadd(input);
		rand.setName("NoisyData");
		
		copyMetadata(input, rand);
		
		return new OperationData(rand);
	}

	@Override
	public String getId() {
		return "$extensionId$";
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