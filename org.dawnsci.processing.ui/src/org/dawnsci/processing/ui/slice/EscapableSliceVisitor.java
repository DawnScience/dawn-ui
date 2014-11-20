package org.dawnsci.processing.ui.slice;

import org.dawb.common.services.conversion.IConversionContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IExportOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.slice.SliceVisitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EscapableSliceVisitor implements SliceVisitor {


	private ILazyDataset lz;
	private UIExecutionVisitor visitor;
	private int[] dataDims;
	private IOperation<? extends IOperationModel, ? extends OperationData>[] series;
	private IOperation<? extends IOperationModel, ? extends OperationData> endOperation;
	private IProgressMonitor monitor;
	private IConversionContext context;
	private IPlottingSystem output;
	private IOperationInputData inputData = null;
	
	private final static Logger logger = LoggerFactory.getLogger(EscapableSliceVisitor.class);

	public EscapableSliceVisitor(ILazyDataset lz, int[] dataDims, IOperation<? extends IOperationModel, ? extends OperationData>[] series, 
			IProgressMonitor monitor, IConversionContext context, IPlottingSystem system) {
		this.lz = lz;
		this.visitor = new UIExecutionVisitor();
		this.dataDims = dataDims;
		this.series = series;
		this.monitor= monitor;
		this.context= context;
		this.output = system;
	}

	public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> op) {
		endOperation = op;
		visitor.setEndOperation(op);
	}

	@Override
	public void visit(IDataset slice, Slice[] slices, int[] shape) throws Exception {

		OriginMetadata om = null;

		try {
			om = lz.getMetadata(OriginMetadata.class).get(0);
		} catch (Exception e1) {
			throw new IllegalArgumentException("No origin!!!!");
		}

		slice.setMetadata(om);

		OperationData  data = new OperationData(slice);

		for (IOperation<? extends IOperationModel, ? extends OperationData> i : series) {

			if (i instanceof IExportOperation) {
				visitor.notify(i, data, slices, shape, dataDims);
			} else if (i.isPassUnmodifiedData() && i != endOperation) {
				//do nothing
			} else {
				
				if (i == endOperation) inputData = new OperationInputDataImpl(data.getData(),i); 
				
				OperationData tmp = i.execute(data.getData(), null);
				visitor.notify(i, tmp, slices, shape, dataDims); // Optionally send intermediate result
				data = i.isPassUnmodifiedData() ? data : tmp;
			}

			if (i == endOperation) break;
		}


		visitor.executed(data, null, slices, shape, dataDims); // Send result.

	}
	
	public IOperationInputData getOperationInputData() {
		return inputData;
	}

	@Override
	public boolean isCancelled() {
		if (monitor != null && monitor.isCanceled()) return true;
		// Overkill warning, context probably is being used here without a monitor, but just in case:
		if (context != null && context.getMonitor()!=null && context.getMonitor().isCancelled()) return true;
		return false;
	}
	
	private class UIExecutionVisitor implements IExecutionVisitor {

		private IOperation<? extends IOperationModel, ? extends OperationData> endOp;
		
		public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> op) {
			endOp = op;
		}
		
		@Override
		public void notify(IOperation<? extends IOperationModel, ? extends OperationData> intermediateData, OperationData data,
				Slice[] slices, int[] shape, int[] dataDims) {
			
			try {
				if (intermediateData == endOp) displayData(data,dataDims);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			
		}
		
		@Override
		public void init(IOperation<? extends IOperationModel, ? extends OperationData>[] series, OriginMetadata origin) throws Exception {
			inputData = null;
		}
		
		@Override
		public void executed(OperationData result, IMonitor monitor,
				Slice[] slices, int[] shape, int[] dataDims) throws Exception {
			
			if (endOp == null) displayData(result,dataDims);
		}
		
		@Override
		public void close() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		private void displayData(OperationData result, int[] dataDims) throws Exception {
			IDataset out = result.getData();
			
			SlicedDataUtils.plotDataWithMetadata(out, output, dataDims);

		}

	}
	
	private class OperationInputDataImpl implements IOperationInputData {

		private IDataset ds;
		private IOperation<? extends IOperationModel, ? extends OperationData> op;
		
		public OperationInputDataImpl(IDataset ds, IOperation<? extends IOperationModel, ? extends OperationData> op) {
			this.ds = ds;
			this.op = op;
		}
		
		@Override
		public IDataset getInputData() {
			return ds;
		}

		@Override
		public IOperation<? extends IOperationModel, ? extends OperationData> getCurrentOperation() {
			return op;
		}
		
	}
}

