package org.dawnsci.processing.ui.slice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.dawb.common.ui.util.DisplayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IExportOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationDataForDisplay;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceVisitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.metadata.OperationMetadataImpl;

@SuppressWarnings("rawtypes")
public class EscapableSliceVisitor implements SliceVisitor {

	private UIExecutionVisitor visitor;
	private int[] dataDims;
	private IOperation[] fullSeries;
	private IOperation[] series;
	private IOperation endOperation;
	private IProgressMonitor monitor;
	private IConversionContext context;
	private IPlottingSystem<?> output;
	private IPlottingSystem<?> display;
	private ProcessingLogDisplay logDisplay;
	private IOperationInputData inputData = null;
	
	private final static Logger logger = LoggerFactory.getLogger(EscapableSliceVisitor.class);

	public EscapableSliceVisitor(ILazyDataset lz, int[] dataDims, IOperation[] series, IOperation[] fullSeries,
			IProgressMonitor monitor, IConversionContext context, IPlottingSystem<?> system) {
		this(lz, dataDims, series, fullSeries, monitor, context, system, null, null);
	}

	public EscapableSliceVisitor(ILazyDataset lz, int[] dataDims, IOperation[] series, IOperation[] fullSeries,
			IProgressMonitor monitor, IConversionContext context, IPlottingSystem<?> system, IPlottingSystem<?> display, ProcessingLogDisplay logDisplay) {
		this.visitor = new UIExecutionVisitor();
		this.dataDims = dataDims;
		this.series = series;
		this.fullSeries = fullSeries;
		this.monitor= monitor;
		this.context= context;
		this.output = system;
		this.display = display;
		this.logDisplay = logDisplay;
	}

	public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> op) {
		endOperation = op;
		visitor.setEndOperation(op);
	}

	@Override
	public void visit(IDataset slice) throws Exception {

		OperationData data = new OperationData(slice);
		SliceFromSeriesMetadata ssm = slice.getMetadata(SliceFromSeriesMetadata.class).get(0);

		try {
			if (series == null)
				return;
			for (IOperation op : series)
				op.init();

			for (IOperation<? extends IOperationModel, ? extends OperationData> i : series) {
				
				if (monitor != null && monitor.isCanceled()) {
					logger.debug("Cancelled");
					inputData = null;
					return;
				}
				
				OperationMetadataImpl operationMeta = new OperationMetadataImpl(null, fullSeries, i);
				data.getData().setMetadata(operationMeta);
				if (i instanceof IExportOperation) {
					visitor.notify(i, data);
				} else if (i.isPassUnmodifiedData() && i != endOperation) {
					// do nothing
				} else {

					if (i == endOperation)
						inputData = new OperationInputDataImpl(data.getData(), i);

					OperationData tmp = i.execute(data.getData(), null);
					if (tmp != null && tmp.getData() != null) {
						tmp.getData().setMetadata(ssm);
					}
					visitor.notify(i, tmp); // Optionally send intermediate
											// result
					data = i.isPassUnmodifiedData() ? data : tmp;
				}
				if (i == endOperation)
					break;
			}
			visitor.executed(data, null); // Send result.
		} finally {
			for (IOperation op : series)
				op.dispose();
		}
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
		public void notify(IOperation<? extends IOperationModel, ? extends OperationData> intermediateData, OperationData data) {
			
			try {
				if (intermediateData == endOp) displayData(data,dataDims);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			
		}
		
		@Override
		public void init(IOperation<? extends IOperationModel, ? extends OperationData>[] series, ILazyDataset dataset) throws Exception {
			inputData = null;
		}
		
		@Override
		public void executed(OperationData result, IMonitor monitor) throws Exception {
			
			if (endOp == null) displayData(result,dataDims);
		}
		
		@Override
		public void close() throws Exception {
			//do nothing
			
		}
		
		private void displayData(OperationData result, int[] dataDims) throws Exception {
			if (display != null) {
				display.clear();
			}
			if (logDisplay != null) {
				logDisplay.clear();
			}
			if (result == null) {
				output.clear();
				return;
			}

			IDataset out = result.getData();
			if (out != null) {
				try {
					SliceFromSeriesMetadata ssm = out.getMetadata(SliceFromSeriesMetadata.class).get(0);
					Slice[] s = ssm.getSliceFromInput();
					String n = ssm.getFilePath();
					if (n != null && !n.isEmpty()) {
						File f = new File(n);
						String name = FilenameUtils.getBaseName(f.getAbsolutePath());
//						name = name + "_" + Slice.createString(s);
						if (endOp != null) {
							name = name + "_" + endOp.getName();
						}
						out.setName(name);
					}
					
				} catch (Exception e) {
					logger.debug("Could not build dataset name",e);
				}

				if (out.getName() == null) {
					out.setName("output");
				}
				MetadataPlotUtils.plotDataWithMetadata(out, output);
			}

			if (logDisplay != null && result.getLog() != null) {
				logDisplay.setLog(result.getLog().toString());
			}

			if (result instanceof OperationDataForDisplay) {
				IDataset[] dd = ((OperationDataForDisplay)result).getDisplayData();
				if (dd != null) {
					for (IDataset d : dd) {
						IDataset view = d.getSliceView();
						view = view.squeeze();
						if (view.getRank() == 1) {
							if (out.getRank() != 1 && display != null) {
								MetadataPlotUtils.plotDataWithMetadata(view, display, false);
								display.repaint();
							} else {
								MetadataPlotUtils.plotDataWithMetadata(view, output, false);
							}
						}
					}
				}
			}

			DisplayUtils.asyncExec(new Runnable() {
				@Override
				public void run() {
					output.setTitle("Output");
				}
			});
		}
	}
	
	private class OperationInputDataImpl implements IOperationInputData {

		final private IDataset ds;
		final private List<IOperation> ops;
		
		public OperationInputDataImpl(IDataset ds, IOperation<? extends IOperationModel, ? extends OperationData> op) {
			this.ds = ds;
			this.ops = new ArrayList<>();
			this.ops.add(op);
		}
		
		@Override
		public IDataset getInputData() {
			return ds;
		}
		
		@Override
		public List<IOperation> getCurrentOperations() {
			return ops;
		}
		
	}
}

