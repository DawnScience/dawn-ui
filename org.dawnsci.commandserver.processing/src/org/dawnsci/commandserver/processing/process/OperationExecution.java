package org.dawnsci.commandserver.processing.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.ILiveOperationInfo;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationBean;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;
import uk.ac.diamond.scisoft.analysis.processing.bean.OperationBean;
import uk.ac.diamond.scisoft.analysis.processing.visitor.NexusFileExecutionVisitor;

public class OperationExecution {

	private static IOperationService   oservice;
	private static IPersistenceService pservice;
	private static ILoaderService      lservice;
	
	// Set by OSGI
	public void setOperationService(IOperationService s) {
		oservice = s;
	}
	// Set by OSGI
	public void setPersistenceService(IPersistenceService s) {
		pservice = s;
	}
	// Set by OSGI
	public void setLoaderService(ILoaderService s) {
		lservice = s;
	}

	private IOperationContext context;

	private final static Logger logger = LoggerFactory.getLogger(OperationExecution.class);

	/**
	 * Can be used to execute an operation process.
	 * @param obean
	 * @throws Exception
	 */
	public void run(final OperationBean obean) throws Exception {
		
		String filePath = obean.getFilePath();
		String datasetPath = obean.getDatasetPath();
		OperationMonitor monitor = null;
		
		logger.debug("Loading processing chain");
		IPersistentFile file = pservice.getPersistentFile(obean.getProcessingPath());
		try {
			// We should get these back exactly as they were defined.
		    final IOperation[] ops = file.getOperations();
		    
		    // Create a context and run the pipeline
		    this.context = oservice.createContext();
		    context.setSeries(ops);
		    context.setExecutionType(obean.getNumberOfCores() == 1 ? ExecutionType.SERIES : ExecutionType.PARALLEL);
		    context.setNumberOfCores(obean.getNumberOfCores());
		    
		    AugmentedPackage augmentedDataset = getAugmentedDataset(filePath, datasetPath, obean);
		    
		    long time = 0;
		    long timeOutShort = obean.getTimeOut()/10;
		    boolean finished = false;
		    
		    while (augmentedDataset == null && time < obean.getTimeOut() && !finished) {
		    	logger.warn("Could not read {} after {} ms",datasetPath,time);
		    	Thread.sleep(timeOutShort/10);
		    	time += timeOutShort/10;
		    	augmentedDataset = getAugmentedDataset(filePath, datasetPath, obean);
		    	finished = isFinished(obean);
		    }
		    
		    if (augmentedDataset == null) {
		    	if (finished) {
		    		logger.error("Building lazydataset {} from {} stopped because scan finished", datasetPath, filePath);
		    	} else {
		    		logger.error("Building lazydataset {} from {} timed out in {} ms", datasetPath, filePath, obean.getTimeOut());
		    	}
		    	
		    	return;
		    }
		    
		    IDataHolder holder = augmentedDataset.holder;
		    ILazyDataset lz = augmentedDataset.lazy;
		    datasetPath = augmentedDataset.name;
		    
		    logger.debug("Building Slice Metadata");
		    SourceInformation si = new SourceInformation(obean.getFilePath(), datasetPath, lz, obean.getDataKey() != null);
		    lz.setMetadata(new SliceFromSeriesMetadata(si));
		    context.setData(lz);
		    
		    if (obean.getDataKey() != null) {
		    	logger.debug("Live Processing key found!");
		    	final IDynamicDataset complete = (IDynamicDataset)holder.getLazyDataset(obean.getDataKey() + Node.SEPARATOR + "scan_finished");
		    	
		    	Node n = holder.getTree().findNodeLink(obean.getDataKey() + Node.SEPARATOR + "keys").getDestination();
		    	final List<IDynamicDataset> dynds = new ArrayList<>();
		    	if (n instanceof GroupNode) {
		    		GroupNode gn = (GroupNode)n;
		    		List<DataNode> dns = gn.getDataNodes();
		    		for (DataNode dn : dns) {
		    			try {
		    				ILazyDataset clone = dn.getDataset().clone();
			    			clone.setMetadata(null);
			    			dynds.add((IDynamicDataset)clone);
		    			} catch (Exception e) {
							logger.error("Could not read unique key");
						}
		    			
		    		}
		    		
		    	}
		    	
		    	final boolean monitorForOverwrite = obean.isMonitorForOverwrite();
		    	
		    	context.setLiveInfo(new ILiveOperationInfo(){

					@Override
					public IDynamicDataset[] getKeys() {
						return dynds.toArray(new IDynamicDataset[dynds.size()]);
					}

					@Override
					public IDynamicDataset getComplete() {
						return complete;
					}

					@Override
					public boolean isMonitorForOverwrite() {
						return monitorForOverwrite;
					}
					
					
		    		
		    	});
		    	
		    	context.setParallelTimeout(obean.getTimeOut());
		    	
		    	logger.debug("Live info populated");
		    }
		    
		    String slicing = obean.getSlicing();
		    if (slicing == null) {
		    	context.setSlicing(null);
		    } else {
		    	Slice[] s = Slice.convertFromString(slicing);
		    	context.setSlicing(new SliceND(lz.getShape(),s));
		    }
		    
		    
		    context.setDataDimensions(obean.getDataDimensionsForRank(lz.getRank()));
		    
		    //Create visitor to save data
		    
		    final IExecutionVisitor visitor = new NexusFileExecutionVisitor(obean.getOutputFilePath(),obean.isReadable(),obean.getLinkParentEntry() ? obean.getFilePath() : null);
		    context.setVisitor(visitor);
		    File fh = new File(obean.getOutputFilePath());
			File parent = fh.getParentFile();
			if (!parent.exists())parent.mkdirs();
		    
		    
		    // We create a monitor which publishes information about what
		    // operation was completed.
		    int[] shape = lz.getShape();
		    SliceND s = context.getSlicing();
		    if (s == null) s = new SliceND(lz.getShape());
		    int work = 1;
		    
		    if (obean.getDataKey() == null) work = getTotalWork(s.convertToSlice(), shape,context.getDataDimensions());
		    monitor = new OperationMonitor(obean, work);
		    context.setMonitor(monitor);
		    
		    monitor.setRunning();
		    logger.debug("Executing");
		    oservice.execute(context);
		} catch (Exception e){
			logger.error("Error running processing", e);
		} finally {
			file.close();
			
			if (monitor != null) monitor.setComplete();
			
			if (obean.isDeleteProcessingFile()) {
			    final File persFile = new File(obean.getProcessingPath());
			    persFile.delete();
			}
		}

	}
	
	private boolean isFinished(OperationBean b) {
		if (b.getDataKey() != null) {
			try {
				logger.debug("Trying dataholder");
				IDataHolder holder = lservice.getData(b.getFilePath(), new IMonitor.Stub());
				if (holder == null) return false;
				final IDynamicDataset complete = (IDynamicDataset)holder.getLazyDataset(b.getDataKey() + Node.SEPARATOR + "scan_finished");
				complete.refreshShape();
				return complete.getSlice().getInt(0) == 1;
			}catch (Exception e) {
				logger.debug("Could not read finished status");
			}
		}
		
		return false;
	}

	private AugmentedPackage getAugmentedDataset(String filePath, String datasetPath, OperationBean obean) {

		IDataHolder holder = null;

		try {
			logger.debug("Trying dataholder");
			lservice.clearSoftReferenceCache(filePath);
			holder = lservice.getData(filePath, new IMonitor.Stub());

			ILazyDataset lz = null;
			MetadataFactory.registerClass(DynamicAxesMetadataImpl.class);
			if (!holder.contains(datasetPath)) {
				Tree tree = holder.getTree();
				if (tree == null) {
					logger.error("Dataholder has no Tree!");
					return null;
				}
				logger.debug("Tree read");
				NodeLink nl = tree.findNodeLink(datasetPath);
				if (nl == null) {
					logger.error("Could not get node link for " + datasetPath);
					return null;
				}
				logger.debug("Node link found for {}",datasetPath);
				Node d = nl.getDestination();
				if (!(d instanceof GroupNode)){
					logger.error("Not a group node: " + datasetPath);
					return null;
				}
				logger.debug("Augmenting");
				lz = NexusTreeUtils.getAugmentedSignalDataset((GroupNode)d);
				if (lz == null) {
					logger.error("Could not build augmented dataset from " + datasetPath);
					return null;
				}
				logger.debug("Taking view");
				lz = lz.getSliceView();
				datasetPath = datasetPath + Node.SEPARATOR + lz.getName();
			} else {
				logger.debug("Loading Lazydataset");
				lz = holder.getLazyDataset(datasetPath);
				if (lz == null) {
					logger.error("No dataset called " + datasetPath);
					return null;
				}
				logger.debug("Building AxesMetadata");
				AxesMetadata axm = lservice.getAxesMetadata(lz, obean.getFilePath(), obean.getAxesNames(), obean.getDataKey()!=null);
				lz.setMetadata(axm);

			}

			return new AugmentedPackage(holder, lz, datasetPath);

		} catch (Exception e) {
			logger.error("Read attempt failed", e);
			return null;
		}
	}
	
	public void stop()  {
		
		if (context!=null && context.getMonitor()!=null && context.getMonitor() instanceof OperationMonitor) {
			OperationMonitor mon = (OperationMonitor)context.getMonitor();
			mon.setCancelled(true);
		}
	}

	
	private int getTotalWork(Slice[] slices, int[] shape, int[] datadims) {
		SliceND slice = new SliceND(shape, slices);
		int[] nShape = slice.getShape();

		int[] dd = datadims.clone();
		Arrays.sort(dd);
		
		 int n = 1;
		 for (int i = 0; i < nShape.length; i++) {
			 if (Arrays.binarySearch(dd, i) < 0) n *= nShape[i];
		 }
		return n;
	}
	
	private class AugmentedPackage {
		
		public IDataHolder holder;
		public ILazyDataset lazy;
		public String name;
		
		public AugmentedPackage(IDataHolder holder, ILazyDataset lazy, String name) {
			this.holder = holder;
			this.lazy = lazy;
			this.name = name;
		}
		
	}

}
