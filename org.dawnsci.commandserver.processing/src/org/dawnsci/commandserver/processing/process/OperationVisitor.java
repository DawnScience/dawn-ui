package org.dawnsci.commandserver.processing.process;

import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceViewIterator;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.event.core.IPublisher;

import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;

/**
 * Deals with sending percent complete from the pipeline
 * over the notification topic.
 * 
 * @author Matthew Gerring
 *
 */
public class OperationVisitor implements IExecutionVisitor {

	private OperationBean obean;
	private IPublisher<OperationBean>  broadcaster;
	private int           total;
	private int           count;

	public OperationVisitor(ILazyDataset lz, OperationBean obean, IPublisher<OperationBean> broadcaster) throws Exception {
		this.obean       = obean;
		this.broadcaster = broadcaster;
		SliceND s = null;
		
		if (obean.getSlicing() != null && !obean.getSlicing().isEmpty()){
			Slice[] sa = Slice.convertFromString(obean.getSlicing());
	    	s = new SliceND(lz.getShape(),sa);
		}
		
		
		SliceViewIterator generator = new SliceViewIterator(lz, s, obean.getDataDimensionsForRank(lz.getRank()));
		this.total       = generator.getTotal();
	}


	@Override
	public void close() throws Exception {
		
	}


	@Override
	public void init(IOperation<? extends IOperationModel, ? extends OperationData>[] series,
			         ILazyDataset dataset) throws Exception {
		this.count = 0;
	
	}

	@Override
	public void notify(IOperation<? extends IOperationModel, ? extends OperationData> intermediateData,  OperationData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executed(OperationData result, IMonitor monitor) throws Exception {
		++count;
		double done = (double)count / (double)total;
		System.out.println(obean);
		obean.setPercentComplete(done);
		broadcaster.broadcast(obean);	
	}

}
