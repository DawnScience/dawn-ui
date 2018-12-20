package org.dawnsci.processing.ui.api;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.january.dataset.IDataset;

public interface IOperationUIService {

	/** Get the IOperationSetupWizardPage that corresponds to the operation
	 * 
	 * If no dedicated page was found for this operation, an instance of the default page (ConfigureOperationModelWizardPage) will be returned.
	 * 
	 * @param operation The operation for which a page will be returned
	 * @return a newly instantiated IOperationSetupWizardPage
	 */
	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation);

	/** Get the IOperationSetupWizardPage that corresponds to the operation
	 * 
	 * If no dedicated page was found for this operation, an instance of the default page (ConfigureOperationModelWizardPage) will be returned.
	 * 
	 * @param operation The operation for which a page will be returned
	 * @param returnDefaultIfNotFound if true and if no dedicated page was found, an instance of the default page will be returned, otherwise null
	 * @return a newly instantiated IOperationSetupWizardPage
	 */
	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation, boolean returnDefaultIfNotFound);
	
	
	/** Get an IOperationModelWizard for the provided pages and/or operations
	 * 
	 * startPages, operations and endPages are not allowed to be all null or empty!
	 * 
	 * @param initialData the initial dataset to be plotted on the first page of the wizard. May be set to null
	 * @param startPages a list of pages that will make up the leading block of the wizard.
	 * @param operations a list of operations for which IOperationSetupWizardPages will be generated and added to the wizard.
	 * @param endPages a list of pages that will make up the terminating block of the wizard.
	 * @return an IOperationModelWizard containing the requested pages
	 */
	public IOperationModelWizard getWizard(IDataset initialData, List<IOperationSetupWizardPage> startPages, List<IOperation<? extends IOperationModel, ? extends OperationData> > operations, List<IOperationSetupWizardPage> endPages);
	
	/** Get an IOperationModelWizard for the provided pages and/or operations
	 * 
	 * startPages and endPages are not allowed to be all null or empty!
	 * 
	 * @param initialData the initial dataset to be plotted on the first page of the wizard. May be set to null
	 * @param startPages a list of pages that will make up the leading block of the wizard.
	 * @param operationsFile a NXS file containing an operations pipeline for which IOperationSetupWizardPages will be generated and added to the wizard.
	 * @param endPages a list of pages that will make up the terminating block of the wizard.
	 * @return an IOperationModelWizard containing the requested pages
	 */
	public IOperationModelWizard getWizard(IDataset initialData, List<IOperationSetupWizardPage> startPages, String operationsFile, List<IOperationSetupWizardPage> endPages);
	
	public void runProcessingWithUI(IOperation[] operations, SliceFromSeriesMetadata metadata, ProcessingOutputType outputType);
}
