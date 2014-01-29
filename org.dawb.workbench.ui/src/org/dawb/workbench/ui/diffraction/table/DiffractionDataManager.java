package org.dawb.workbench.ui.diffraction.table;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;

public class DiffractionDataManager {
	
	private List<DiffractionTableData> model;
	private DiffractionTableData currentData;
	private ILoaderService service;
	
	private final static ISchedulingRule mutex = new Mutex();
	
	private HashSet<IDiffractionDataListener> listeners;
	
	public DiffractionDataManager() {
		service = (ILoaderService) PlatformUI.getWorkbench().getService(ILoaderService.class);
		model= new ArrayList<DiffractionTableData>();
		listeners     = new HashSet<IDiffractionDataListener>();
	}
	
	public void setModel(List<DiffractionTableData> model) {
		this.model = model;
	}
	
	public boolean isEmpty() {
		return model.isEmpty();
	}
	
	public void setCurrentData(DiffractionTableData data) {
		this.currentData = data;
	}
	
	public DiffractionTableData getCurrentData() {
		return currentData;
	}
	
	public void setWavelength(double wavelength) {
		for (DiffractionTableData data : model) {
			data.md.getDiffractionCrystalEnvironment().setWavelength(wavelength);
		}
	}
	
	public List<DiffractionTableData> getModel() {
		return model;
	}
	
	public void loadData(String filePath, String dataFullName) {
		if (filePath == null) return;

		for (DiffractionTableData d : model) {
			if (filePath.equals(d.path)) {
				return;
			}
		}

		PowderFileLoaderJob job = new PowderFileLoaderJob(filePath, dataFullName);
		job.setRule(mutex);
		job.schedule();

	}
	
	public void addFileListener(IDiffractionDataListener listener) {
		listeners.add(listener);
	}
	
	public void removeFileListener(IDiffractionDataListener listener) {
		listeners.remove(listener);
	}
	
	private void fireDiffractionDataListeners(DiffractionDataChanged event) {
		for (IDiffractionDataListener listener : listeners) listener.dataChanged(event);
	}
	
	private class PowderFileLoaderJob extends Job {

		private final String path;
		private final String fullName;

		public PowderFileLoaderJob(String filePath, String dataFullName) {
			super("Load powder file");
			this.path = filePath;
			this.fullName = dataFullName;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			IDataset image = PlottingUtils.loadData(path, fullName);
			if (image == null)
				return Status.CANCEL_STATUS;
			
			int j = path.lastIndexOf(File.separator);
			String fileName = j > 0 ? path.substring(j + 1) : null;
			image.setName(fileName + ":" + image.getName());

			DiffractionTableData data = new DiffractionTableData();
			data.path = path;
			data.name = fileName;
			data.image = image;
			String[] statusString = new String[1];
			data.md = DiffractionUtils.getDiffractionMetadata(image, path, service, statusString);
			model.add(data);
			
			fireDiffractionDataListeners(new DiffractionDataChanged(data));

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
