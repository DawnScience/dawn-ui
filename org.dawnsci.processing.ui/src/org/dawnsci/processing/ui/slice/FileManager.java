package org.dawnsci.processing.ui.slice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;


public class FileManager {
	
	private List<String> filePaths = new ArrayList<String>();
	//TODO image/1d stacks
//	private List<ILazyDataset> dataStacks;
	private String datasetName;
	private Map<Integer, String> axesNames;
	
	private HashSet<IFilesAddedListener> listeners;
	
	private final static Logger logger = LoggerFactory.getLogger(FileManager.class);
	private final static ISchedulingRule mutex = new Mutex();
	
	public FileManager(IConversionContext context) {
		
		filePaths = context.getFilePaths();
		datasetName = context.getDatasetNames().get(0);
		axesNames = context.getAxesNames();
		listeners = new HashSet<IFilesAddedListener>();
		
	}
	
	public boolean addFile(String path) {
		return false;
	}
	
	public boolean[] addFiles(String[] paths) {
		
		FileLoaderJob job = new FileLoaderJob(datasetName, paths);
		job.setRule(mutex);
		job.schedule();
		
		return null;
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
				} catch (Exception e) {
					//TODO logger
				}
				
				//TODO test axes
				
				out[i] = goodFile;
				
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

