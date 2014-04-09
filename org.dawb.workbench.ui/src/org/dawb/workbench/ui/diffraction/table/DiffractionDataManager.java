package org.dawb.workbench.ui.diffraction.table;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.tools.diffraction.DiffractionUtils;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class DiffractionDataManager {
	
	private List<DiffractionTableData> model;
	private DiffractionTableData currentData;
	private ILoaderService service;
	
	private final static ISchedulingRule mutex = new Mutex();
	
	// Logger
	private final static Logger logger = LoggerFactory.getLogger(DiffractionDataManager.class);
	
	private HashSet<IDiffractionDataListener> listeners;
	
	public DiffractionDataManager() {
		this(new ArrayList<DiffractionTableData>(7));
	}
	public DiffractionDataManager(List<DiffractionTableData> model) {
		this.model = model;
		service    = (ILoaderService) PlatformUI.getWorkbench().getService(ILoaderService.class);
		listeners  = new HashSet<IDiffractionDataListener>();
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
			data.getMetaData().getDiffractionCrystalEnvironment().setWavelength(wavelength);
		}
	}
	
	// Encapsulation is goooood.
	public DiffractionTableData[] toArray() {
		return model.toArray(new DiffractionTableData[model.size()]);
	}

	// Encapsulation is goooood.
	public Iterable<DiffractionTableData> iterable() {
		return model; // Cannot get at data unless they cast. Could provide protection against this in future.
	}
	
	// Encapsulation is goooood.
	public int getSize() {
		return model.size();
	}
	
	// Encapsulation is goooood.
	public boolean remove(DiffractionTableData selectedData) {
		return model.remove(selectedData);
	}

	// Encapsulation is goooood.
	public boolean isValidModel() {
		return model!=null && getSize()>0;
	}
	
	// Encapsulation is goooood.
	public DiffractionTableData getLast() {
		return isValidModel() ? model.get(model.size()-1) : null;
	}
    
	/**
	 * Resets the meta data
	 */
	public void reset() {
		for (DiffractionTableData model : iterable()) {
			// Restore original metadata
			DetectorProperties originalProps = model.getMetaData().getOriginalDetector2DProperties();
			DiffractionCrystalEnvironment originalEnvironment =model.getMetaData().getOriginalDiffractionCrystalEnvironment();
			model.getMetaData().getDetector2DProperties().restore(originalProps);
			model.getMetaData().getDiffractionCrystalEnvironment().restore(originalEnvironment);
		}		
	}

	public void loadData(String filePath, String dataFullName) {
		if (filePath == null) return;

		for (DiffractionTableData d : model) {
			if (filePath.equals(d.getPath())) {
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
			
			final String[] outName = new String[1];
			
			if (image == null &&  fullName == null) {
				try {
					IMetaData metaData = LoaderFactory.getMetaData(path, null);
					Map<String, int[]> dataShapes = metaData.getDataShapes();
					final List<String> dataNames = new ArrayList<String>();
					for (String name : dataShapes.keySet()) {
						if (dataShapes.get(name).length > 1) {
							dataNames.add(name);
						}
					}
					
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							ListDialog dia = new ListDialog(Display.getDefault().getActiveShell());
							dia.setTitle("Multiple dataset file!");
							dia.setMessage("Select dataset to calibrate:");
							dia.setContentProvider(new ArrayContentProvider());
							dia.setLabelProvider(new LabelProvider());
							dia.setInput(dataNames);
							if (dia.open() == ListDialog.OK) {
								outName[0] = dia.getResult()[0].toString();
							}
						}
					});
					
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			
			if (outName[0] != null) image = PlottingUtils.loadData(path, outName[0]);
			
			if (image == null)
				return Status.CANCEL_STATUS;
			
			int j = path.lastIndexOf(File.separator);
			String fileName = j > 0 ? path.substring(j + 1) : null;
			image.setName(fileName + ":" + image.getName());

			DiffractionTableData data = new DiffractionTableData();
			data.setPath(path);
			data.setName(fileName);
			data.setImage(image);
			String[] statusString = new String[1];
			data.setMetaData(DiffractionUtils.getDiffractionMetadata(image, path, service, statusString));
			data.getImage().setMetadata(data.getMetaData());
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

	public void dispose() {
		if (model!=null) model.clear(); // Helps garbage collector.
	}

	public AbstractDataset getDistances() {
		
		if (!isValidModel()) return null; // Or raise exception?
		
		double[] deltaDistance = new double[getSize()];
		
		for (int i = 0; i < model.size(); i++) deltaDistance[i] = model.get(i).getDistance();
		
		return new DoubleDataset(deltaDistance, new int[]{deltaDistance.length});
	}
	
	public void clear(IDetectorPropertyListener listener) {
		if (!isValidModel()) return;
		if (listener!=null) for (DiffractionTableData d : iterable()) {
			d.getMetaData().getDetector2DProperties().removeDetectorPropertyListener(listener);
		}
		model.clear();
	}


}
