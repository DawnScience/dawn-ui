package org.dawnsci.processing.ui.slice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.bcel.internal.generic.CPInstruction;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class FileManager {
	
	private List<String> filePaths = new ArrayList<String>();
	IConversionContext context;
	//TODO image/1d stacks
//	private List<ILazyDataset> dataStacks;
	private Map<Integer, String> axesNames;
	ISetupContext contextHelper;
	
	private HashSet<IFilesAddedListener> listeners;
	
	private final static Logger logger = LoggerFactory.getLogger(FileManager.class);
	private final static ISchedulingRule mutex = new Mutex();
	
	public FileManager(ISetupContext contextHelper) {
		this.contextHelper = contextHelper;
//		filePaths = context.getFilePaths();
//		datasetName = context.getDatasetNames().get(0);
//		axesNames = context.getAxesNames();
		listeners = new HashSet<IFilesAddedListener>();
		
	}
	
	public boolean[] addFiles(String[] paths) {
		
		if (filePaths.isEmpty()) {
			context = contextHelper.init(paths[0]);
			if (context == null) {
				boolean[] falses = new boolean[paths.length];
				Arrays.fill(falses, false);
				return falses;
			}
		}
		
		FileLoaderJob job = new FileLoaderJob(context.getDatasetNames().get(0), paths);
		job.setRule(mutex);
		job.schedule();
		
		return null;
	}
	
	public boolean setUpContext(){
		return contextHelper.setup(context);
	}
	
	public void addFileListener(IFilesAddedListener listener) {
		listeners.add(listener);
	}
	
	public List<String> getFilePaths() {
		return filePaths;
	}
	
	public void removeFileListener(IFilesAddedListener listener) {
		listeners.remove(listener);
	}
	
	private void fireFileListeners(FileAddedEvent event) {
		for (IFilesAddedListener listener : listeners) listener.filesAdded(event);
	}
	
	public void clear() {
		context = null;
		filePaths.clear();
		if (axesNames != null) axesNames.clear();
	}
	
	public void setProcessingConversionInfo(IProcessingConversionInfo info) {
		if (context != null) context.setUserObject(info);
	}
	
	public IConversionContext getContext() {
		return context;
	}
	
	public void setOutputPath(String path) {
		if (context != null) context.setOutputPath(path);
	}
	
	private class FileLoaderJob extends Job {

		private final String[] paths;
		private final String datasetName;

		public FileLoaderJob(String datasetName, final String[] paths) {
			super(datasetName);
			this.paths = paths;
			this.datasetName = datasetName;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			boolean[] out = new boolean[paths.length];
			
			for (int i = 0; i < paths.length; i++)  {
				
				boolean goodFile = false;
				try {
					IDataHolder holder = LoaderFactory.getData(paths[i], null);
					goodFile = holder.contains(datasetName);
					filePaths.add(paths[i]);
					goodFile = true;
				} catch (Exception e) {
					//TODO logger
				}
				
				//TODO test axes
				
				out[i] = goodFile;
				try {
					context.setFilePaths(filePaths.toArray(new String[filePaths.size()]));
				} catch (Exception e) {
					logger.error("Files not added to context!", e);
				}
				
			}
			
			fireFileListeners(new FileAddedEvent(this, paths, out));
			
			return Status.OK_STATUS;
		}

	}

	public static class Mutex implements ISchedulingRule {

		public boolean contains(ISchedulingRule rule) {
			return (rule == this);
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return (rule == this);
		}

	}
	
}

