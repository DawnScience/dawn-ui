package org.dawnsci.processing.ui.service;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.api.IOperationUIService;
import org.dawnsci.processing.ui.model.ConfigureOperationModelWizardPage;
import org.dawnsci.processing.ui.model.OperationModelWizard;
import org.dawnsci.processing.ui.slice.DataFileSliceView;
import org.dawnsci.processing.ui.slice.ExtendedFileSelectionDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.Atomic;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.ISavesToFile;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.plotting.api.PlottingEventConstants;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.visitor.NexusFileExecutionVisitor;
import uk.ac.diamond.scisoft.analysis.utils.FileUtils;

public class OperationUIServiceImpl implements IOperationUIService {

	static {
		System.out.println("Starting operationUI service");
	}

	private final static Logger logger = LoggerFactory.getLogger(OperationUIServiceImpl.class);
	
	private final static String PROCESSED = "_processed";
	private final static String EXT= ".nxs";

	private Map<String, Class<? extends IOperationSetupWizardPage>> operationSetupWizardPages;

	public OperationUIServiceImpl() {
		// Intentionally do nothing -> OSGI...
	}

	@Override
	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation, boolean returnDefaultIfNotFound) {
		checkOperationSetupWizardPages();
		
		// The hash only contains the Class wizardPage belongs to.
		// We need to construct it here.
		Class <? extends IOperationSetupWizardPage> klazz = operationSetupWizardPages.get(operation.getId());
		if (klazz == null) {
			logger.info("No OperationSetupWizardPage found for {}", operation.getId());
			return returnDefaultIfNotFound ? new ConfigureOperationModelWizardPage(operation) : null;
		}
			
		try {
			Constructor<?> constructor = klazz.getConstructor(IOperation.class);
			IOperationSetupWizardPage rv = (IOperationSetupWizardPage) constructor.newInstance(operation);
			logger.info("{}-arg constructor instance generated for {}", 1, klazz.getName());
			return rv;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Cannot construct instance of {}. ", klazz.getName());
		}
	
		return returnDefaultIfNotFound ? new ConfigureOperationModelWizardPage(operation) : null;
	}

	private synchronized void checkOperationSetupWizardPages() {
		if (operationSetupWizardPages != null)
			return;

		operationSetupWizardPages = new HashMap<>();

		// look for the extension points
		IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.operationsetupwizardpage");
		for (IConfigurationElement e : eles) {
			if (!e.getName().equals("operationsetupwizardpage")) continue;
			final String klazz = e.getAttribute("class");
			final String operationid = e.getAttribute("operationid");
			Class<? extends IOperationSetupWizardPage> old = null;
			try {
				old = operationSetupWizardPages.put(operationid, ((IOperationSetupWizardPage) e.createExecutableExtension("class")).getClass());
			} catch (CoreException e1) {
				logger.error("Exception in createExecutableExtension", e1);
				continue;
			}
			if (old != null) {
				logger.warn("IOperationSetupWizardPage has been changed from {} to {} for operation {}", old.getName(), klazz, operationid);
			}
		}
	}

	@Override
	public IOperationSetupWizardPage getWizardPage(
			IOperation<? extends IOperationModel, ? extends OperationData> operation) {
		return getWizardPage(operation, true);
	}

	@Override
	public IOperationModelWizard getWizard(IDataset initialData,
			List<IOperationSetupWizardPage> startPages,
			List<IOperation<? extends IOperationModel, ? extends OperationData>> operations,
			List<IOperationSetupWizardPage> endPages) {
		
		// not all three arguments may be null
		if ((startPages == null || startPages.isEmpty()) && (operations == null || operations.isEmpty()) && (endPages == null || endPages.isEmpty())) {
			logger.error("Not all arguments may be null or empty lists");
			return null;
		}
		
		List<IOperationSetupWizardPage> allPages = new ArrayList<>();
		
		if (startPages != null) {
			allPages.addAll(startPages);
		}
		
		if (operations != null) {
			for (IOperation<? extends IOperationModel, ? extends OperationData> op : operations) {
				allPages.add(getWizardPage(op)); // if no dedicated page is available, the default will be used!
			}
		}
		
		if (endPages != null) {
			allPages.addAll(endPages);
		}
		
		return new OperationModelWizard(initialData, allPages);
	}

	@Override
	public IOperationModelWizard getWizard(IDataset initialData, 
			List<IOperationSetupWizardPage> startPages,
			String operationsFile,
			List<IOperationSetupWizardPage> endPages) {
		
		// the challenging part here is processing the operationsFile
		IOperation<? extends IOperationModel, ? extends OperationData>[] operations;
		List <IOperation<? extends IOperationModel, ? extends OperationData>> operationsList;
	
		if (operationsFile != null) {
			try {
				IPersistenceService service = ServiceHolder.getPersistenceService();
				IPersistentFile pf = service.getPersistentFile(operationsFile);
				operations = pf.getOperations();
				operationsList = Arrays.asList(operations);
			} catch (Exception e) {
				logger.error("Could not get operations from " + operationsFile, e);
				return null;
			}
		} else {
			operationsList = Collections.emptyList();
		}
		
		return getWizard(initialData, startPages, operationsList, endPages);
	}
	
	@Override
	public void runProcessingWithUI(IOperation[] operations, SliceFromSeriesMetadata metadata, ProcessingOutputType outputType) {
		if (metadata == null) return;
		
		String p = getPathNoExtension(metadata.getFilePath());

		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
		String timeStamp = "_" +dateFormat.format(date);
		String full = p + PROCESSED+ timeStamp + EXT;
		
		boolean isHDF5 = false;
		try {
			Tree tree = ServiceHolder.getLoaderService().getData(metadata.getFilePath(), null).getTree();
			isHDF5 = tree != null;
		} catch (Exception e2) {
			logger.error("Could not read tree",e2);
		}
		
		ExtendedFileSelectionDialog fsd = new ExtendedFileSelectionDialog(Display.getCurrent().getActiveShell(),isHDF5,false, false, null);
		
		fsd.setPath(full);
		fsd.setFolderSelector(false);
		fsd.setNewFile(true);
		fsd.create();
		if (fsd.open() == Dialog.CANCEL) return;
		final ProcessingOutputType foutputType = fsd.getProcessingOutputType();
		
		final String path = fsd.getPath();
		File fh = new File(path);
		fh.getParentFile().mkdirs();
		
		ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

		try {
			dia.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
					
					try {
						runProcessing(metadata, path, monitor, operations, foutputType);
						Map<String,String> props = new HashMap<>();
						props.put("path", path);
						EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
						eventAdmin.postEvent(new Event(PlottingEventConstants.FILE_OPEN_EVENT, props));
					} catch (final Exception e) {
						
						logger.error(e.getMessage(), e);
					}
				}
			});
		} catch (InvocationTargetException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (InterruptedException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	private void runProcessing(SliceFromSeriesMetadata meta, String outputFile, IProgressMonitor monitor, IOperation[] operations, ProcessingOutputType outputType){
		
		try {
			IMonitor mon = new ProgressMonitorWrapper(monitor);
			IOperationService service = ServiceHolder.getOperationService();
			IOperationContext cc = service.createContext();
			ILazyDataset local = meta.getParent().getSliceView();
			SliceFromSeriesMetadata ssm = new SliceFromSeriesMetadata(meta.getSourceInfo());
			local.setMetadata(ssm);
			cc.setData(local);
			
			NexusFileExecutionVisitor vis = new NexusFileExecutionVisitor(outputFile);
			
			if (outputType == ProcessingOutputType.LINK_ORIGINAL && vis instanceof ISavesToFile) {
				((ISavesToFile)vis).includeLinkTo(meta.getFilePath());
			} else if (outputType == ProcessingOutputType.ORIGINAL_AND_PROCESSED) {
				File source = new File(meta.getFilePath());
				File dest = new File(outputFile);
				logger.debug("Copying original data ({}) to output file ({})",source.getAbsolutePath(),dest.getAbsolutePath());
				long start = System.currentTimeMillis();
				FileUtils.copyNio(source, dest);
				logger.debug("Copy ran in: {} s : Thread {}", (System.currentTimeMillis()-start)/1000. ,Thread.currentThread().toString());
			}
			
			cc.setVisitor(vis);
			cc.setDataDimensions(meta.getDataDimensions());
			cc.setSeries(operations);
			cc.setMonitor(mon);
			
			monitor.beginTask("Processing", DataFileSliceView.getWork(new SliceND(local.getShape()), meta.getDataDimensions()));
			
			if (canRunParallel(operations)) cc.setExecutionType(ExecutionType.PARALLEL);
			else cc.setExecutionType(ExecutionType.SERIES);
			
			service.execute(cc);
		} catch (Exception e) {
			logger.error("Could no run processing",e);
		}
	}
	
	private String getPathNoExtension(String path) {
		int posExt = path.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? path : path.substring(0, posExt);
	}
	
	private boolean canRunParallel(IOperation[] ops) {
		for (IOperation op : ops) {
			Atomic atomic = op.getClass().getAnnotation(Atomic.class);
			if (atomic == null) {
				return false;
			}
		}
		
		return true;
	}
}
