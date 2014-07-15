package org.dawb.workbench.ui.transferable;

import org.dawb.common.services.IExpressionObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * 
 * @author fcp94556
 *
 */
public class TransferableDataService extends AbstractServiceFactory implements ITransferableDataService {

	@Override
	public ITransferableDataObject createData(IDataHolder holder, IMetaData meta, String name) {
		return new TransferableDataObject(holder, meta, name);
	}

	@Override
	public ITransferableDataObject createExpression(IDataHolder holder, IMetaData meta) {
		return new TransferableDataObject(holder, meta);
	}

	@Override
	public ITransferableDataObject createExpression(IDataHolder holder, IMetaData meta, IExpressionObject expression) {
		return new TransferableDataObject(holder, meta, expression);
	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (serviceInterface==ITransferableDataService.class) {
			return new TransferableDataService();
		} 
		return null;
	}

	private static ITransferableDataObject currentCopiedData;

	@Override
	public ITransferableDataObject getBuffer() {
		return currentCopiedData;
	}
	
	@Override
	public ITransferableDataObject setBuffer(ITransferableDataObject buf) {
		ITransferableDataObject old = currentCopiedData;
		currentCopiedData = buf;
		return old;
	}

}

